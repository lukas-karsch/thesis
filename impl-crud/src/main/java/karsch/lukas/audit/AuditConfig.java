package karsch.lukas.audit;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AuditConfig {

    public AuditConfig(AuditLogRepository auditLogRepository) {
        AuditEntityListener.setAuditLogRepository(auditLogRepository);
    }
}
