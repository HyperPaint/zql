package zql_exporter;

import lombok.AllArgsConstructor;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import zql_exporter.config.ZqlZkConfig;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;

@Configuration
@AllArgsConstructor
public class AppConfig {
    @Bean
    public CuratorFramework curator(ZqlZkConfig zqlZkConfig) {
        if (zqlZkConfig.isSslEnabled()) {
            // SSL is only supported on top of Netty communication, which means if you want to use SSL you have to enable Netty
            System.setProperty("zookeeper.clientCnxnSocket", "org.apache.zookeeper.ClientCnxnSocketNetty");
            // Setting this to "true" will enable encrypted client-server communication
            System.setProperty("zookeeper.client.secure", "true");

            System.setProperty("zookeeper.ssl.keyStore.location", zqlZkConfig.getSslKeyStoreLocation());
            System.setProperty("zookeeper.ssl.keyStore.password", zqlZkConfig.getSslKeyStorePassword());
            System.setProperty("zookeeper.ssl.trustStore.location", zqlZkConfig.getSslTrustStoreLocation());
            System.setProperty("zookeeper.ssl.trustStore.password", zqlZkConfig.getSslTrustStorePassword());

            // Specifies whether the hostname verification is enabled in client TLS negotiation process
            System.setProperty("zookeeper.ssl.hostnameVerification", String.valueOf(zqlZkConfig.isSslHostnameVerification()));
        }

        var curator = CuratorFrameworkFactory.newClient(
                zqlZkConfig.getHost() + ":" + zqlZkConfig.getPort(),
                zqlZkConfig.getSessionTimeout(),
                zqlZkConfig.getConnectionTimeout(),
                new ExponentialBackoffRetry(1000, 3)
        );
        curator.start();
        return curator;
    }

    @Bean
    public SocketFactory socketFactory(ZqlZkConfig zqlZkConfig) throws Exception {
        if (zqlZkConfig.isSslEnabled()) {
            final var keystore = KeyStore.getInstance(new File(zqlZkConfig.getSslKeyStoreLocation()), zqlZkConfig.getSslKeyStorePassword().toCharArray());

            final var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keystore, zqlZkConfig.getSslKeyStorePassword().toCharArray());

            final var truststore = KeyStore.getInstance(new File(zqlZkConfig.getSslTrustStoreLocation()), zqlZkConfig.getSslTrustStorePassword().toCharArray());

            final var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(truststore);

            final var context = SSLContext.getInstance("TLS");
            context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            return context.getSocketFactory();
        } else {
            return SocketFactory.getDefault();
        }
    }

    @Bean
    @Scope("prototype")
    public Socket socket(ZqlZkConfig zqlZkConfig, SocketFactory socketFactory) throws IOException {
        final Socket socket = socketFactory.createSocket();

        socket.setSoTimeout(zqlZkConfig.getSessionTimeout());
        socket.connect(new InetSocketAddress(zqlZkConfig.getHost(), zqlZkConfig.getPort()), zqlZkConfig.getConnectionTimeout());

        return socket;
    }
}
