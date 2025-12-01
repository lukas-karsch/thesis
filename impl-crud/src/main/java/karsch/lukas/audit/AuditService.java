package karsch.lukas.audit;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    private static Specification<AuditLogEntry> hasEntityName(String entityName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("entityName"), entityName);
    }

    private static Specification<AuditLogEntry> hasEntityId(Long entityId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("entityId"), entityId);
    }

    private static Specification<AuditLogEntry> isAfter(LocalDateTime startDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), startDate);
    }

    private static Specification<AuditLogEntry> isBefore(LocalDateTime endDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThan(root.get("timestamp"), endDate);
    }

    public List<AuditLogEntry> getByEntityId(Class<?> entityClass, Long entityId) {
        return getByEntityId(entityClass, entityId, null, null);
    }

    public List<AuditLogEntry> getByEntityId(Class<?> entityClass, Long entityId, @Nullable LocalDateTime startDate, @Nullable LocalDateTime endDate) {
        var entityName = AuditHelper.getNameFromEntityClass(entityClass);
        var sort = Sort.by(Sort.Order.desc("timestamp"));

        Specification<AuditLogEntry> spec = hasEntityName(entityName).and(hasEntityId(entityId));

        if (startDate != null) {
            spec = spec.and(isAfter(startDate));
        }

        if (endDate != null) {
            spec = spec.and(isBefore(endDate));
        }

        return auditLogRepository.findAll(spec, sort);
    }

    /**
     * When adding audit context from a system thread (e.g. cron job), make sure to call {@link #clearContext()} in a finally block
     */
    public void addAuditContext(Object entity, Object context) {
        AuditContext.setContextForEntity(entity, context);
    }

    /**
     * The audit context is stored in a ThreadLocal. It's the caller's responsibility to clear the context
     * after the operation is complete. Use a finally block to ensure it's always called.
     */
    public void clearContext() {
        AuditContext.clearContext();
    }
}
