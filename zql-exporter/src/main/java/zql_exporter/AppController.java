package zql_exporter;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
                        <p>query as table -> <a href="/query?format=table&query=select%20path,%20data%20from%20ls/;">GET /query?format=table&query=...</a></p>
                        <p>query as metrics -> <a href="/query?format=metrics&query=select%20path,%20data,%201as%20one%20from%20ls/;">GET /query?format=metrics&query=...</a></p>
                        <p>metrics -> <a href="/metrics">GET /metrics</a></p>
                    </body>
                </html>
                """;
    }

    @GetMapping("/query")
    public ResponseEntity<String> query(
            @RequestParam(required = false) String format,
            @RequestParam String query
    ) throws Exception {
        //noinspection SwitchStatementWithTooFewBranches
        return switch (format) {
            //case "table" -> queryHandler.handleAsTable(query);
            case "metrics" -> queryHandler.handleAsMetrics(query);
            default -> queryHandler.handleAsTable(query);
        };
    }

    @GetMapping("/metrics")
    public ResponseEntity<String> metrics() throws Exception {
        return metricsHandler.metrics();
    }


}
