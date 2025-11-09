package zql_exporter;

import org.apache.zookeeper.ZooKeeper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public ZooKeeper zookeeperConnection() throws IOException {
        final String connectionString = "localhost:2181";

//        System.setProperty("zookeeper.clientCnxnSocket", "org.apache.zookeeper.ClientCnxnSocketNetty");
//        System.setProperty("zookeeper.ssl.keyStore.location", "/path/to/your/client.keystore");
//        System.setProperty("zookeeper.ssl.keyStore.password", "your_client_keystore_password");
//        System.setProperty("zookeeper.ssl.trustStore.location", "/path/to/your/client.truststore");
//        System.setProperty("zookeeper.ssl.trustStore.password", "your_client_truststore_password");
//        System.setProperty("zookeeper.ssl.hostnameVerification", "true"); // Optional, for hostname verification

        return new ZooKeeper(connectionString, 86400000, null);
    }
}
