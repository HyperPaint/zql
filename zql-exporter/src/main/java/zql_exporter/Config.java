package zql_exporter;

import org.apache.zookeeper.ZooKeeper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@Configuration
public class Config {
    @Bean
    public String zookeeperHost(Environment environment) throws IllegalStateException {
        return environment.getProperty("zql-exporter.zookeeper.host", "localhost");
    }

    @Bean
    public int zookeeperPort(Environment environment) throws IllegalStateException {
        return Integer.parseInt(environment.getProperty("zql-exporter.zookeeper.port", "2181"));
    }

    @Bean
    public int zookeeperTimeout(Environment environment) throws IllegalStateException, NumberFormatException {
        return Integer.parseInt(environment.getProperty("zql-exporter.zookeeper.timeout", "10000"));
    }

    @Bean
    public boolean zookeeperSslEnabled(Environment environment) throws IllegalStateException {
        return Boolean.parseBoolean(environment.getProperty("zql-exporter.zookeeper.ssl.enabled", "false"));
    }

    @Bean
    public String zookeeperSslKeyStoreLocation(Environment environment) {
        return environment.getProperty("zql-exporter.zookeeper.ssl.key-store-location", "keystore.jks");
    }

    @Bean
    public String zookeeperSslKeyStorePassword(Environment environment) {
        return environment.getProperty("zql-exporter.zookeeper.ssl.key-store-password", "password");
    }

    @Bean
    public String zookeeperSslTrustStoreLocation(Environment environment) {
        return environment.getProperty("zql-exporter.zookeeper.ssl.trust-store-location", "truststore.jks");
    }

    @Bean
    public String zookeeperSslTrustStorePassword(Environment environment) {
        return environment.getProperty("zql-exporter.zookeeper.ssl.trust-store-password", "password");
    }

    @Bean
    public boolean zookeeperSslHostnameVerification(Environment environment) {
        return Boolean.parseBoolean(environment.getProperty("zql-exporter.zookeeper.ssl.hostname-verification", "true"));
    }

    @Bean
    @Scope("prototype")
    public ZooKeeper zookeeper(
            String zookeeperHost,
            int zookeeperPort,
            int zookeeperTimeout,
            boolean zookeeperSslEnabled,
            String zookeeperSslKeyStoreLocation,
            String zookeeperSslKeyStorePassword,
            String zookeeperSslTrustStoreLocation,
            String zookeeperSslTrustStorePassword,
            boolean zookeeperSslHostnameVerification
    ) throws IOException {
        if (zookeeperSslEnabled) {
            // SSL is only supported on top of Netty communication, which means if you want to use SSL you have to enable Netty
            System.setProperty("zookeeper.clientCnxnSocket", "org.apache.zookeeper.ClientCnxnSocketNetty");
            // Setting this to "true" will enable encrypted client-server communication
            System.setProperty("zookeeper.client.secure", "true");

            System.setProperty("zookeeper.ssl.keyStore.location", zookeeperSslKeyStoreLocation);
            System.setProperty("zookeeper.ssl.keyStore.password", zookeeperSslKeyStorePassword);
            System.setProperty("zookeeper.ssl.trustStore.location", zookeeperSslTrustStoreLocation);
            System.setProperty("zookeeper.ssl.trustStore.password", zookeeperSslTrustStorePassword);

            // Specifies whether the hostname verification is enabled in client TLS negotiation process
            System.setProperty("zookeeper.ssl.hostnameVerification", String.valueOf(zookeeperSslHostnameVerification));
        }

        return new ZooKeeper(zookeeperHost + ":" + zookeeperPort, zookeeperTimeout, null);
    }

    @Bean
    @Scope("prototype")
    public Socket zookeeperMonitoringSocket(
            String zookeeperHost,
            int zookeeperPort,
            int zookeeperTimeout,
            boolean zookeeperSslEnabled,
            String zookeeperSslKeyStoreLocation,
            String zookeeperSslKeyStorePassword,
            String zookeeperSslTrustStoreLocation,
            String zookeeperSslTrustStorePassword
    ) throws IOException {
        final Socket socket;

        if (zookeeperSslEnabled) {
//            props.put("keyStore", System.getProperty("javax.net.ssl.keyStore", ""));
//            props.put("keyStoreType", System.getProperty("javax.net.ssl.keyStoreType", KeyStore.getDefaultType()));
//            props.put("keyStoreProvider", System.getProperty("javax.net.ssl.keyStoreProvider", ""));
//            props.put("keyStorePasswd", System.getProperty("javax.net.ssl.keyStorePassword", ""));
            System.setProperty("javax.net.ssl.keyStore", zookeeperSslKeyStoreLocation);
            System.setProperty("javax.net.ssl.keyStorePassword", zookeeperSslKeyStorePassword);

//            String storePropName = System.getProperty("javax.net.ssl.trustStore", TrustStoreManager.TrustStoreDescriptor.jsseDefaultStore);
//            String storePropType = System.getProperty("javax.net.ssl.trustStoreType", KeyStore.getDefaultType());
//            String storePropProvider = System.getProperty("javax.net.ssl.trustStoreProvider", "");
//            String storePropPassword = System.getProperty("javax.net.ssl.trustStorePassword", "");
            System.setProperty("javax.net.ssl.trustStore", zookeeperSslTrustStoreLocation);
            System.setProperty("javax.net.ssl.trustStorePassword", zookeeperSslTrustStorePassword);

            socket = SSLSocketFactory.getDefault().createSocket();
        } else {
            socket = new Socket();
        }

        socket.setSoLinger(true, zookeeperTimeout);
        socket.setSoTimeout(zookeeperTimeout);
        socket.setKeepAlive(true);
        socket.connect(new InetSocketAddress(zookeeperHost, zookeeperPort), zookeeperTimeout);

        return socket;
    }
}
