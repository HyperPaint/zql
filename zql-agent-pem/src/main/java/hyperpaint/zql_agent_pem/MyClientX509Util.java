package hyperpaint.zql_agent_pem;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

public class MyClientX509Util {
    public static SslContext createSSLContextAndOptionsFromConfig(Object object) throws Exception {
        final String caPath = System.getProperty("zql.agent_pem.caPath");
        final String privateKeyPath = System.getProperty("zql.agent_pem.privateKeyPath");
        final String certificatePath = System.getProperty("zql.agent_pem.certificatePath");
        final boolean allowInsecure = Boolean.parseBoolean(System.getProperty("zql.agent_pem.allowInsecure", "true"));

        final var x509CertificateConverter = new JcaX509CertificateConverter().setProvider("BC");

        // region ca

        final Certificate ca;
        final byte[] caBytes = Files.readAllBytes(Paths.get(caPath));
        try (
                var byteArrayInputStream = new ByteArrayInputStream(caBytes);
                var inputStreamReader = new InputStreamReader(byteArrayInputStream);
                var pemParser = new PEMParser(inputStreamReader)
        ) {
            final var pem = pemParser.readObject();
            ca = x509CertificateConverter.getCertificate((X509CertificateHolder) pem);
        }

        // endregion

        final var pemKeyConverter = new JcaPEMKeyConverter().setProvider("BC");

        // region key & certificate

        final PrivateKey privateKey;
        final byte[] privateKeyBytes = Files.readAllBytes(Paths.get(privateKeyPath));
        try (
                var byteArrayInputStream = new ByteArrayInputStream(privateKeyBytes);
                var inputStreamReader = new InputStreamReader(byteArrayInputStream);
                var pemParser = new PEMParser(inputStreamReader)
        ) {
            final var pem = pemParser.readObject();
            privateKey = pemKeyConverter.getPrivateKey((PrivateKeyInfo) pem);
        }

        final Certificate certificate;
        final byte[] certificateBytes = Files.readAllBytes(Paths.get(certificatePath));
        try (
                var byteArrayInputStream = new ByteArrayInputStream(certificateBytes);
                var inputStreamReader = new InputStreamReader(byteArrayInputStream);
                var pemParser = new PEMParser(inputStreamReader)
        ) {
            final var pem = pemParser.readObject();
            certificate = x509CertificateConverter.getCertificate((X509CertificateHolder) pem);
        }

        // endregion

        final KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(null, null);
        keystore.setKeyEntry("certificate", privateKey, "password".toCharArray(), new Certificate[] { certificate });

        final KeyStore truststore = KeyStore.getInstance("JKS");
        truststore.load(null, null);
        truststore.setCertificateEntry("ca", ca);

        final var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(truststore);

        final var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keystore, "password".toCharArray());

        final var context = SSLContext.getInstance("TLS");
        context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        if (allowInsecure) {
            return SslContextBuilder.forClient().keyManager(keyManagerFactory).trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            return SslContextBuilder.forClient().keyManager(keyManagerFactory).trustManager(trustManagerFactory).build();
        }
    }
}
