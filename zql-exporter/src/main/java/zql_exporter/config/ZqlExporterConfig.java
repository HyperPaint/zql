package zql_exporter.config;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "zql-exporter.exporter")
@Getter
@Setter(AccessLevel.PACKAGE)
public class ZqlExporterConfig {
    @Value("${zql-exporter.exporter.endpoints.query-as-table.enabled}")
    private boolean queryAsTableEnabled;
    @Value("${zql-exporter.exporter.endpoints.query-as-metrics.enabled}")
    private boolean queryAsMetricsEnabled;
    @Value("${zql-exporter.exporter.endpoints.queries.enabled}")
    private boolean queriesEnabled;
    @Value("${zql-exporter.exporter.endpoints.metrics.enabled}")
    private boolean metricsEnabled;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    public static class Metric {
        private String query;
        private String help;
        private String type;
        private String name;
    }

    private List<Metric> metrics;
}
