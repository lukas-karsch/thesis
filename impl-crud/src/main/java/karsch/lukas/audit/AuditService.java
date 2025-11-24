package karsch.lukas.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public List<AuditLogEntry> getByEntityId(Class<?> entityClass, Long entityId) {
        return getByEntityIdAndStartEndDate(entityClass, entityId, null, null);
    }

    public List<AuditLogEntry> getByEntityIdAndStartEndDate(Class<?> entityClass, Long entityId, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findAllByEntityNameAndEntityId(
                AuditHelper.getNameFromEntityClass(entityClass),
                entityId,
                Sort.by(
                        Sort.Order.desc("timestamp")
                )
        );
    }
}
