package zql_exporter;

import hyperpaint.zql.lang.ZQLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@Slf4j
@RestControllerAdvice
public class AppControllerAdvice {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() + " - " + e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handle(IOException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase() + " - " + e.getMessage());
    }

    @ExceptionHandler(ZQLException.class)
    public ResponseEntity<String> handle(ZQLException e) {
        log.warn(e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
