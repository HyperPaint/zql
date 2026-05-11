package hyperpaint.zql_exporter;

import hyperpaint.zql_exporter.config.ZqlConfig;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

@Slf4j
@Configuration
@AllArgsConstructor
public class AppConfig {
    @Bean
    public KeyStore truststore(ZqlConfig zqlConfig) throws Exception {
        if (!zqlConfig.isZookeeperSslEnabled()) {
            return null;
        }

        Security.addProvider(new BouncyCastleProvider());
        final var x509CertificateConverter = new JcaX509CertificateConverter().setProvider("BC");

        final Certificate ca;
        final byte[] caBytes = Files.readAllBytes(Paths.get(zqlConfig.getZookeeperSslCaPath()));
        try (
                var byteArrayInputStream = new ByteArrayInputStream(caBytes);
                var inputStreamReader = new InputStreamReader(byteArrayInputStream);
                var pemParser = new PEMParser(inputStreamReader)
        ) {
            final var pem = pemParser.readObject();
            ca = x509CertificateConverter.getCertificate((X509CertificateHolder) pem);
        }

        final KeyStore truststore = KeyStore.getInstance("JKS");
        truststore.load(null, null);
        truststore.setCertificateEntry("ca", ca);

        return truststore;
    }

    @Bean
    public KeyStore keystore(ZqlConfig zqlConfig) throws Exception {
        if (!zqlConfig.isZookeeperSslEnabled()) {
            return null;
        }

        Security.addProvider(new BouncyCastleProvider());
        final var pemKeyConverter = new JcaPEMKeyConverter().setProvider("BC");
        final var x509CertificateConverter = new JcaX509CertificateConverter().setProvider("BC");

        final PrivateKey privateKey;
        final byte[] privateKeyBytes = Files.readAllBytes(Paths.get(zqlConfig.getZookeeperSslPrivateKeyPath()));
        try (
                var byteArrayInputStream = new ByteArrayInputStream(privateKeyBytes);
                var inputStreamReader = new InputStreamReader(byteArrayInputStream);
                var pemParser = new PEMParser(inputStreamReader)
        ) {
            final var pem = pemParser.readObject();
            privateKey = pemKeyConverter.getPrivateKey((PrivateKeyInfo) pem);
        }

        final Certificate certificate;
        final byte[] certificateBytes = Files.readAllBytes(Paths.get(zqlConfig.getZookeeperSslCertificatePath()));
        try (
                var byteArrayInputStream = new ByteArrayInputStream(certificateBytes);
                var inputStreamReader = new InputStreamReader(byteArrayInputStream);
                var pemParser = new PEMParser(inputStreamReader)
        ) {
            final var pem = pemParser.readObject();
            certificate = x509CertificateConverter.getCertificate((X509CertificateHolder) pem);
        }

        final KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(null, null);
        keystore.setKeyEntry("certificate", privateKey, "password".toCharArray(), new Certificate[] { certificate });

        return keystore;
    }

    @Bean
    public TrustManagerFactory trustManagerFactory(KeyStore truststore) throws Exception {
        if (truststore != null) {
            final var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(truststore);
            return trustManagerFactory;
        } else {
            return null;
        }
    }

    @Bean
    public KeyManagerFactory keyManagerFactory(KeyStore keystore) throws Exception {
        if (keystore != null) {
            final var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, "password".toCharArray());
            return keyManagerFactory;
        } else {
            return null;
        }
    }

    @Bean
    public SSLContext sslContext(ZqlConfig zqlConfig, TrustManagerFactory trustManagerFactory, KeyManagerFactory keyManagerFactory) throws Exception {
        if (trustManagerFactory != null && keyManagerFactory != null) {
            final var context = SSLContext.getInstance("TLS");
            if (zqlConfig.isZookeeperSslAllowInsecure()) {
                context.init(keyManagerFactory.getKeyManagers(), InsecureTrustManagerFactory.INSTANCE.getTrustManagers(), null);
            } else {
                context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            }

            return context;
        } else {
            return null;
        }
    }

    @Bean
    public SocketFactory socketFactory(ZqlConfig zqlConfig, SSLContext sslContext) {
        if (zqlConfig.isZookeeperSslEnabled()) {
            return sslContext.getSocketFactory();
        } else {
            return SocketFactory.getDefault();
        }
    }

    @Bean
    @Scope("prototype")
    public Socket socket(ZqlConfig zqlConfig, SocketFactory socketFactory) throws IOException {
        final Socket socket = socketFactory.createSocket();

        socket.setSoTimeout(zqlConfig.getZookeeperSessionTimeout());
        socket.setTcpNoDelay(true);
        socket.connect(new InetSocketAddress(zqlConfig.getZookeeperHost(), zqlConfig.getZookeeperPort()), zqlConfig.getZookeeperConnectionTimeout());

        return socket;
    }

    @Bean
    public CuratorFramework curator(ZqlConfig zqlConfig) {
        if (zqlConfig.isZookeeperSslEnabled()) {
            // Setting this to "true" will enable encrypted client-server communication
            System.setProperty("zookeeper.client.secure", "true");
            // SSL is only supported on top of Netty communication, which means if you want to use SSL you have to enable Netty
            System.setProperty("zookeeper.clientCnxnSocket", "org.apache.zookeeper.ClientCnxnSocketNetty");

            // Agent PEM
            System.setProperty("zql.agent_pem.caPath", zqlConfig.getZookeeperSslCaPath());
            System.setProperty("zql.agent_pem.privateKeyPath", zqlConfig.getZookeeperSslPrivateKeyPath());
            System.setProperty("zql.agent_pem.certificatePath", zqlConfig.getZookeeperSslCertificatePath());
            System.setProperty("zql.agent_pem.allowInsecure", "true");
        }

        var curator = CuratorFrameworkFactory.newClient(
                zqlConfig.getZookeeperHost() + ":" + zqlConfig.getZookeeperPort(),
                zqlConfig.getZookeeperSessionTimeout(),
                zqlConfig.getZookeeperConnectionTimeout(),
                new ExponentialBackoffRetry(1000, 3)
        );
        curator.start();

        return curator;
    }
}
