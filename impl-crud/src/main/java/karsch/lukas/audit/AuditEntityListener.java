package karsch.lukas.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import karsch.lukas.config.SpringContext;
import karsch.lukas.context.RequestContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class AuditEntityListener {

    private static final String CREATE = "CREATE";
    private static final String UPDATE = "UPDATE";
    private static final String DELETE = "DELETE";

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(AuditEntityListener.class);

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
            logger.error(e.getMessage(), e);
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

            var log = new AuditLogEntry();
            log.setEntityName(entity.getClass().getSimpleName());
            log.setOperation(operation);
            log.setTimestamp(LocalDateTime.now());
            if (RequestContextHolder.getRequestAttributes() != null) {
                log.setModifiedBy(
                        String.format("%s_%d", requestContext.getUserType(), requestContext.getUserId())
                );
            } else {
                log.setModifiedBy("SYSTEM");
            }

            log.setOldValueJson(oldJson);
            if (!DELETE.equals(operation)) {
                log.setNewValueJson(objectMapper.writeValueAsString(entity));
            }

            try {
                Field idField = entity.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                log.setEntityId((Long) idField.get(entity));
            } catch (Exception ignored) {
            }

            auditLogRepository.save(log);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
