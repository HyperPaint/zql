package hyperpaint.zql_exporter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "zookeeper-exporter")
public class ApplicationProperties {
    private ZookeeperProperties zookeeper;
    private ExporterProperties exporter;

    @Data
    public static final class ZookeeperProperties {
        private String connectionString;
        private RetryPolicyProperties retryPolicy;

        @Data
        public static final class RetryPolicyProperties {
            private int sleepTimeMilliseconds;
            private int maxRetries;
        }
    }

    @Data
    public static final class ExporterProperties {
        private String lister;
        private String[] metrics;
    }
}
