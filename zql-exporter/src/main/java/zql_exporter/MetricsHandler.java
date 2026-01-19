package zql_exporter;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
class MetricsHandler {
    private final ZkCommandsHandler zkCommandsHandler;

    public String metrics() throws Exception {
        return zkCommandsHandler.exec(ZkCommandsHandler.ZKCommands.MNTR)
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
                            return "# HELP zql_%s %s\n# TYPE zql_%s gauge\nzql_%s %s".formatted(key, key, key, key, String.valueOf(Float.parseFloat(value)));
                        } catch (NumberFormatException ignored) {
                            // Отображение как метка
                            return "# HELP zql_%s %s\n# TYPE zql_%s gauge\nzql_%s{value=\"%s\"} 1.0".formatted(key, key, key, key, value);
                        }
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));
    }
}
