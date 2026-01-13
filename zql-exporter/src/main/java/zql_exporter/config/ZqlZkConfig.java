package zql_exporter.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "zql-exporter.zookeeper")
@Getter
@Setter(AccessLevel.PACKAGE)
public class ZqlZkConfig {
    private String host;
    private int port;

    @Value("${zql-exporter.zookeeper.timeout.session}")
    private int sessionTimeout;
    @Value("${zql-exporter.zookeeper.timeout.connection}")
    private int connectionTimeout;

    @Value("${zql-exporter.zookeeper.ssl.enabled}")
    private boolean sslEnabled;
    @Value("${zql-exporter.zookeeper.ssl.key-store-location}")
    private String sslKeyStoreLocation;
    @Value("${zql-exporter.zookeeper.ssl.key-store-password}")
    private String sslKeyStorePassword;
    @Value("${zql-exporter.zookeeper.ssl.trust-store-location}")
    private String sslTrustStoreLocation;
    @Value("${zql-exporter.zookeeper.ssl.trust-store-password}")
    private String sslTrustStorePassword;
    @Value("${zql-exporter.zookeeper.ssl.hostname-verification}")
    private boolean sslHostnameVerification;
}
