package karsch.lukas.core.time;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.*;

// TODO fix this duplication (same code exists in impl-crud)

/**
 * Actuator endpoints (available under /actuator/date-time) to control system time. <br>
 * Adapted from <a href="https://sahan-r.medium.com/time-travel-in-spring-boot-6bba96d38e03">Sahan Ranasinghe</a>
 */
@Component
@Endpoint(id = "date-time")
@Slf4j
@RequiredArgsConstructor
public class DateTimeActuatorEndpoints {

    private final DateTimeProvider dateTimeProvider;

    @ReadOperation()
    public ResponseEntity<Instant> currentTime() {
        return ResponseEntity.ok(Instant.now(dateTimeProvider.getClock()));
    }

    @WriteOperation
    public ResponseEntity<Void> setTime(
            int year,
            int month,
            int dayOfMonth,
            int hour,
            int minutes,
            int seconds //
    ) {
        final Clock clock = Clock.fixed(
                LocalDateTime
                        .of(year, month, dayOfMonth, hour, minutes, seconds)
                        .toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")
        );
        log.info("Changing the clock to: {}", clock);

        dateTimeProvider.setClock(clock);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteOperation()
    public ResponseEntity<Void> resetTime() {
        dateTimeProvider.resetTime();
        return ResponseEntity.ok().build();
    }
}
