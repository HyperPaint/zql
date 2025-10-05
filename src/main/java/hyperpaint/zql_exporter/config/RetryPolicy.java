package hyperpaint.zql_exporter.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RetryPolicy {
    private int sleepTimeMilliseconds;
    private int maxRetries;
}
