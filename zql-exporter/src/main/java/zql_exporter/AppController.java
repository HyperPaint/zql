package zql_exporter;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@RestController
public class AppController {
    private final QueryHandler queryHandler;
    private final MetricsHandler metricsHandler;

    @GetMapping("/")
    public String index() {
        return """
                <html>
                    <body>
                        <h1>ZQL Exporter</h1>
                        <p>query as table -> <a href="/query-as-table?query=select%20path,%20data%20from%20ls/;">GET /query-as-table?query=...</a></p>
                        <p>query as metrics -> <a href="/query-as-metrics?&query=select%20path,%20data,%201as%20one%20from%20ls/;">GET /query-as-metrics?query=...</a></p>
                        <p>queries -> <a href="/queries">GET /queries</a></p>
                        <p>metrics -> <a href="/metrics">GET /metrics</a></p>
                    </body>
                </html>
                """;
    }

    @GetMapping("/query-as-table")
    public ResponseEntity<String> queryAsTable(
            @RequestParam String query
    ) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(queryHandler.handleAsTable(query));
    }

    @GetMapping("/query-as-metrics")
    public ResponseEntity<String> queryAsMetrics(
            @RequestParam String query,
            @RequestParam(defaultValue = "metric") String name,
            @RequestParam(required = false) String help,
            @RequestParam(defaultValue = "gauge") String type
    ) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(queryHandler.handleAsMetrics(query, help, type, name));
    }

    @GetMapping("/queries")
    public ResponseEntity<String> queries() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(queryHandler.queries());
    }

    @GetMapping("/metrics")
    public ResponseEntity<String> metrics() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(metricsHandler.metrics());
    }


}
