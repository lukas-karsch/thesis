package karsch.lukas.config.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RetryLoggingAspect {

    @Pointcut("@annotation(retryable)")
    public void retryableMethod(Retryable retryable) {
        // pointcut definition
    }

    @Before("retryableMethod(retryable)")
    public void logRetryOnMethodEntry(JoinPoint joinPoint, Retryable retryable) {
        logRetries(joinPoint);
    }

    private static void logRetries(JoinPoint joinPoint) {
        var log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());

        var context = RetrySynchronizationManager.getContext();
        if (context != null && context.getRetryCount() > 0) {
            log.debug("Retry #{} - {}", context.getRetryCount(), joinPoint.getSignature().toLongString());
        }
    }
}
