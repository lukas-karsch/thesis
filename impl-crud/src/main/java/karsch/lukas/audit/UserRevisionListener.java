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
            final RequestContext context;
            if (RequestContextHolder.getRequestAttributes() == null) {
                context = null;
            } else {
                context = SpringContext.getBean(RequestContext.class);
            }
            if (context != null && context.getUserId() != null) {
                rev.setRevisionMadeBy(String.format("%s_%s", context.getUserType(), context.getUserId()));
            } else {
                rev.setRevisionMadeBy("SYSTEM");
            }
        } catch (Exception e) {
            rev.setRevisionMadeBy("Unknown");
        }
    }
}
