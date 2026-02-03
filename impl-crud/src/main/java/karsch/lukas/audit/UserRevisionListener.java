package karsch.lukas.audit;

import karsch.lukas.config.SpringContext;
import karsch.lukas.context.RequestContext;
import karsch.lukas.time.DateTimeProvider;
import org.hibernate.envers.RevisionListener;
import org.springframework.web.context.request.RequestContextHolder;

public class UserRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity rev = (CustomRevisionEntity) revisionEntity;

        // Set the time defined by the SpringBoot app (controlled via actuator)
        long systemTime = DateTimeProvider.getInstance().getClock().millis();
        rev.setTimestamp(systemTime);

        try {
            final RequestContext requestContext;
            final AuditContext auditContext;
            if (!isInsideRequestScope()) {
                requestContext = null;
                auditContext = null;
            } else {
                requestContext = SpringContext.getBean(RequestContext.class);
                auditContext = SpringContext.getBean(AuditContext.class);
            }
            if (requestContext != null && requestContext.getUserId() != null) {
                rev.setRevisionMadeBy(String.format("%s_%s", requestContext.getUserType(), requestContext.getUserId()));
            } else {
                rev.setRevisionMadeBy("SYSTEM");
            }

            if (auditContext != null) {
                rev.setAdditionalContext(auditContext.getAdditionalContext());
            }
        } catch (Exception e) {
            rev.setRevisionMadeBy("Unknown");
        }
    }

    private static boolean isInsideRequestScope() {
        return RequestContextHolder.getRequestAttributes() != null;
    }
}
