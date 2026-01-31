package karsch.lukas.audit;

import karsch.lukas.config.SpringContext;
import karsch.lukas.context.RequestContext;
import karsch.lukas.time.DateTimeProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.context.request.RequestContextHolder;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "jpaDateTimeProvider")
public class AuditingConfig {

    /**
     * This bean makes sure JPA Auditing (createdAt, updatedAt) uses the "correct" system time as defined by my
     * custom DateTimeProvider instance
     */
    @Bean
    public org.springframework.data.auditing.DateTimeProvider jpaDateTimeProvider() {
        return () -> {
            Instant now = DateTimeProvider.getInstance().getClock().instant();
            return Optional.of(now.atZone(ZoneId.systemDefault()));
        };
    }

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            if (RequestContextHolder.getRequestAttributes() == null) {
                return Optional.empty();
            }

            RequestContext requestContext = SpringContext.getBean(RequestContext.class);

            if (requestContext == null || requestContext.getUserId() == null || requestContext.getUserType() == null) {
                return Optional.empty();
            }
            return Optional.of(String.format("%s_%s", requestContext.getUserType(), requestContext.getUserId()));
        };
    }
}
