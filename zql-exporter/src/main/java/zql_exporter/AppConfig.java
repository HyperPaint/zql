package zql_exporter;

import lombok.AllArgsConstructor;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import zql_exporter.config.ZqlZkConfig;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

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
    @Scope("prototype")
    public Socket zookeeperSocket(ZqlZkConfig zqlZkConfig) throws IOException {
        final Socket socket;

        if (zqlZkConfig.isSslEnabled()) {
//            props.put("keyStore", System.getProperty("javax.net.ssl.keyStore", ""));
//            props.put("keyStoreType", System.getProperty("javax.net.ssl.keyStoreType", KeyStore.getDefaultType()));
//            props.put("keyStoreProvider", System.getProperty("javax.net.ssl.keyStoreProvider", ""));
//            props.put("keyStorePasswd", System.getProperty("javax.net.ssl.keyStorePassword", ""));
            System.setProperty("javax.net.ssl.keyStore", zqlZkConfig.getSslKeyStoreLocation());
            System.setProperty("javax.net.ssl.keyStorePassword", zqlZkConfig.getSslKeyStorePassword());

//            String storePropName = System.getProperty("javax.net.ssl.trustStore", TrustStoreManager.TrustStoreDescriptor.jsseDefaultStore);
//            String storePropType = System.getProperty("javax.net.ssl.trustStoreType", KeyStore.getDefaultType());
//            String storePropProvider = System.getProperty("javax.net.ssl.trustStoreProvider", "");
//            String storePropPassword = System.getProperty("javax.net.ssl.trustStorePassword", "");
            System.setProperty("javax.net.ssl.trustStore", zqlZkConfig.getSslTrustStoreLocation());
            System.setProperty("javax.net.ssl.trustStorePassword", zqlZkConfig.getSslTrustStorePassword());

            socket = SSLSocketFactory.getDefault().createSocket();
        } else {
            socket = new Socket();
        }

        socket.setSoTimeout(zqlZkConfig.getSessionTimeout());
        socket.connect(new InetSocketAddress(zqlZkConfig.getHost(), zqlZkConfig.getPort()), zqlZkConfig.getConnectionTimeout());

        return socket;
    }
}
