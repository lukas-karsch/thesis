package karsch.lukas.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import karsch.lukas.config.SpringContext;
import karsch.lukas.context.RequestContext;
import karsch.lukas.time.DateTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@RequiredArgsConstructor
@Slf4j
public class AuditEntityListener {

    private static final String CREATE = "CREATE";
    private static final String UPDATE = "UPDATE";
    private static final String DELETE = "DELETE";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // better LocalDateTime serialization
        objectMapper.registerModule(new JavaTimeModule());

        // module which handles serialization for the audit log: only store the ids of nested entities
        objectMapper.registerModule(new IdSerializationModule());
    }

    @PostLoad
    public void postLoad(Object entity) {
        try {
            if (entity instanceof AuditableEntity auditable) {
                auditable.setSnapshotJson(objectMapper.writeValueAsString(entity));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @PostPersist
    public void postPersist(Object entity) {
        // should guarantee that the audit log entry is written within the same transaction
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void beforeCommit(boolean readOnly) {
                saveAuditLog(entity, CREATE, null);
            }

            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK || status == TransactionSynchronization.STATUS_UNKNOWN) {
                    log.warn("@PostPersist: Commit status is {}! Audit log should not be written", status);
                }
            }
        });
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof AuditableEntity auditable) {
            saveAuditLog(entity, UPDATE, auditable.getSnapshotJson());
        } else {
            saveAuditLog(entity, UPDATE, null);
        }
    }

    @PreRemove
    public void preRemove(Object entity) {
        if (entity instanceof AuditableEntity auditable) {
            saveAuditLog(entity, DELETE, auditable.getSnapshotJson());
        } else {
            saveAuditLog(entity, DELETE, null);
        }
    }

    private void saveAuditLog(Object entity, String operation, String oldJson) {
        try {
            final AuditLogRepository auditLogRepository = SpringContext.getBean(AuditLogRepository.class);
            final RequestContext requestContext = SpringContext.getBean(RequestContext.class);
            final DateTimeProvider dateTimeProvider = SpringContext.getBean(DateTimeProvider.class);

            var entry = new AuditLogEntry();
            entry.setEntityName(AuditHelper.getNameFromEntityClass(entity.getClass()));
            entry.setOperation(operation);
            LocalDateTime now = LocalDateTime.ofInstant(
                    Instant.now(dateTimeProvider.getClock()), ZoneOffset.UTC
            );
            entry.setTimestamp(now);
            if (RequestContextHolder.getRequestAttributes() != null) {
                entry.setModifiedBy(
                        String.format("%s_%d", requestContext.getUserType(), requestContext.getUserId())
                );
            } else {
                entry.setModifiedBy("SYSTEM");
            }

            Object context = AuditContext.getContextForEntity(entity);
            if (context != null) {
                entry.setContextJson(objectMapper.writeValueAsString(context));
            }

            entry.setOldValueJson(oldJson);
            if (!DELETE.equals(operation)) {
                entry.setNewValueJson(objectMapper.writeValueAsString(entity));
            }

            try {
                Field idField = entity.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                entry.setEntityId((Long) idField.get(entity));
            } catch (Exception ignored) {
            }

            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
