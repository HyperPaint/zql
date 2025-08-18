package hyperpaint.zookeeper_exporter;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
    @Bean
    public CuratorFramework getCurator() {
        final String connectionString = "localhost:2181";
        final RetryPolicy retryPolicy = new RetryOneTime(1000);

//        System.setProperty("zookeeper.clientCnxnSocket", "org.apache.zookeeper.ClientCnxnSocketNetty");
//        System.setProperty("zookeeper.ssl.keyStore.location", "/path/to/your/client.keystore");
//        System.setProperty("zookeeper.ssl.keyStore.password", "your_client_keystore_password");
//        System.setProperty("zookeeper.ssl.trustStore.location", "/path/to/your/client.truststore");
//        System.setProperty("zookeeper.ssl.trustStore.password", "your_client_truststore_password");
//        System.setProperty("zookeeper.ssl.hostnameVerification", "true"); // Optional, for hostname verification

        CuratorFramework curator = CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
        curator.start();

        return curator;
    }
}
