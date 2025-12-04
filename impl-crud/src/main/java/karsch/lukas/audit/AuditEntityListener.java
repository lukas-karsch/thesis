package karsch.lukas.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import karsch.lukas.config.SpringContext;
import karsch.lukas.context.RequestContext;
import karsch.lukas.time.DateTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

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

    @PrePersist
    public void prePersist(Object entity) {
        saveAuditLog(entity, CREATE, null);
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
                        String.format("%s_%s", requestContext.getUserType(), requestContext.getUserId())
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
                Object idValue = idField.get(entity);
                if (idValue == null) {
                    log.error("Entity of type {} does not have an ID when persisting.", entity.getClass().getSimpleName());
                }
                if (idValue instanceof UUID uuid) {
                    entry.setEntityId(uuid);
                } else {
                    log.error("Entity {} does not have id field of type UUID", entity.getClass().getSimpleName());
                }
            } catch (Exception ignored) {
            }

            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
