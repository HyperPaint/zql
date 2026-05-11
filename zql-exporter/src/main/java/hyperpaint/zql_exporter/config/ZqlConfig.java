package hyperpaint.zql_exporter.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties("zql-exporter")
@Getter
@Setter(AccessLevel.PACKAGE)
public class ZqlConfig {
    @Value("${zql-exporter.zookeeper.host:127.0.0.1}")
    private String zookeeperHost;
    @Value("${zql-exporter.zookeeper.port:2181}")
    private int zookeeperPort;

    @Value("${zql-exporter.zookeeper.timeout.session:10000}")
    private int zookeeperSessionTimeout;
    @Value("${zql-exporter.zookeeper.timeout.connection:10000}")
    private int zookeeperConnectionTimeout;

    @Value("${zql-exporter.zookeeper.ssl.enabled:false}")
    private boolean zookeeperSslEnabled;
    @Value("${zql-exporter.zookeeper.ssl.allow-insecure:false}")
    private boolean zookeeperSslAllowInsecure;
    @Value("${zql-exporter.zookeeper.ssl.paths.ca:./ca.crt}")
    private String zookeeperSslCaPath;
    @Value("${zql-exporter.zookeeper.ssl.paths.private-key:./certificate.key}")
    private String zookeeperSslPrivateKeyPath;
    @Value("${zql-exporter.zookeeper.ssl.paths.certificate:./certificate.crt}")
    private String zookeeperSslCertificatePath;

    /* */

    @Value("${zql-exporter.exporter.endpoints.query-as-table.enabled}")
    private boolean exporterQueryAsTableEnabled;
    @Value("${zql-exporter.exporter.endpoints.query-as-metrics.enabled}")
    private boolean exporterQueryAsMetricsEnabled;
    @Value("${zql-exporter.exporter.endpoints.queries.enabled}")
    private boolean exporterQueriesEnabled;
    @Value("${zql-exporter.exporter.endpoints.metrics.enabled}")
    private boolean exporterMetricsEnabled;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    public static class Metric {
        private String query;
        private String help;
        private String type;
        private String name;
    }

    private List<Metric> exporterMetrics;
}
