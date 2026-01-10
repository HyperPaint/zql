package zql_exporter;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
class MetricsHandler {
    private final ZKCommandsHandler zkCommandsHandler;

    public ResponseEntity<String> metrics() throws Exception {
        final var result = zkCommandsHandler.exec(ZKCommandsHandler.ZKCommands.MNTR)
                .lines()
                .map(s -> {
                    final int delimiterIndex = s.indexOf("\t");
                    if (delimiterIndex != -1) {
                        String key = s.substring(0, delimiterIndex);
                        if (key.startsWith("zk_")) {
                            key = "zookeeper_" + key.substring(3);
                        }

                        final String value = s.substring(delimiterIndex + 1);

                        try {
                            // Отображение как число (в корректном формате)
                            return "# TYPE zql_%s gauge\nzql_%s %s".formatted(key, key, String.valueOf(Float.parseFloat(value)));
                        } catch (NumberFormatException ignored) {
                            // Отображение как метка
                            return "# TYPE zql_%s gauge\nzql_%s{value=\"%s\"} 1.0".formatted(key, key, value);
                        }
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(result);
    }
}
