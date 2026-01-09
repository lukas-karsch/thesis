package karsch.lukas.config.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect to automatically wrap function calls in the query layer in log statements.
 */
@Aspect
@Component
public class LoggingAspect {
    // generated using Gemini

    /**
     * Pointcut Definition:
     * execution(* ...)                -> Any return type
     * karsch.lukas.features           -> Base package
     * .* -> Matches any single sub-package (e.g., 'users', 'products')
     * .queries                        -> Must be in a 'queries' package
     * ..* -> Any class in 'queries' or its sub-packages
     * (..)                            -> Any method arguments
     */
    @SuppressWarnings("EmptyMethod")
    @Pointcut("execution(* karsch.lukas.features.*.queries..*(..))")
    public void queryLayer() {
    }

    @Around("queryLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        var log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());

        if (!log.isEnabledForLevel(Level.TRACE)) {
            return joinPoint.proceed();
        }

        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.trace("Entry: {}(args: {})", methodName, Arrays.toString(args));

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - startTime;
            log.trace("Exit: {}(return: {}) - {}ms", methodName, result, duration);

            return result;
        } catch (Throwable e) {
            log.error("Exception in {}: {}", methodName, e.getMessage());
            throw e;
        }
    }
}
