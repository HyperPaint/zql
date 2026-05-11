package hyperpaint.zql_exporter;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import hyperpaint.zql_exporter.config.ZqlConfig;

@Slf4j
@AllArgsConstructor
@RestController
public class AppController {
    private final QueryHandler queryHandler;
    private final MetricsHandler metricsHandler;

    private final ZqlConfig zqlConfig;

    @GetMapping("/")
    public String index() {
        return """
                <html>
                    <body>
                        <h1>ZQL Exporter</h1>
                        <p>-> <a href="/query-as-table?query=select%20path,%20data%20from%20ls/;">GET /query-as-table</a></p>
                        <p>-> <a href="/query-as-metrics?&query=select%20path,%20data,%201as%20one%20from%20ls/;">GET /query-as-metrics</a></p>
                        <p>-> <a href="/queries">GET /queries</a></p>
                        <p>-> <a href="/metrics">GET /metrics</a></p>
                    </body>
                </html>
                """;
    }

    @GetMapping("/favicon.ico")
    public String favicon() {
        return "";
    }

    @GetMapping("/query-as-table")
    public ResponseEntity<String> queryAsTable(
            @RequestParam String query
    ) throws Exception {
        if (!zqlConfig.isExporterQueryAsTableEnabled()) {
            return null;
        }

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(queryHandler.handleAsTable(query));
    }

    @GetMapping("/query-as-metrics")
    public ResponseEntity<String> queryAsMetrics(
            @RequestParam String query,
            @RequestParam(defaultValue = "metric") String name,
            @RequestParam(required = false) String help,
            @RequestParam(required = false) String type
    ) throws Exception {
        if (!zqlConfig.isExporterQueryAsMetricsEnabled()) {
            return null;
        }

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(queryHandler.handleAsMetrics(query, help, type, name));
    }

    @GetMapping("/queries")
    public ResponseEntity<String> queries() throws Exception {
        if (!zqlConfig.isExporterQueriesEnabled()) {
            return null;
        }

        final var stringBuilder = new StringBuilder();

        for (var item : zqlConfig.getExporterMetrics()) {
            stringBuilder.append(queryHandler.handleAsMetrics(item.getQuery(), item.getHelp(), item.getType(), item.getName()));
        }

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(stringBuilder.toString());
    }

    @GetMapping("/metrics")
    public ResponseEntity<String> metrics() throws Exception {
        if (!zqlConfig.isExporterMetricsEnabled()) {
            return null;
        }

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(metricsHandler.metrics());
    }
}
