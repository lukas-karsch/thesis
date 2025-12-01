package karsch.lukas.audit;

import java.util.IdentityHashMap;
import java.util.Map;

public final class AuditContext {
    // TODO maybe allow not referencing the context with an entity? `setContext(Object context);`
    private static final ThreadLocal<Map<Object, Object>> CONTEXT_HOLDER = new ThreadLocal<>();

    private AuditContext() {
    }

    public static void setContextForEntity(Object entity, Object context) {
        getContextMap().put(entity, context);
    }

    public static Object getContextForEntity(Object entity) {
        Map<Object, Object> contextMap = CONTEXT_HOLDER.get();
        if (contextMap == null) {
            return null;
        }
        return contextMap.get(entity);
    }

    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }

    private static Map<Object, Object> getContextMap() {
        if (CONTEXT_HOLDER.get() == null) {
            CONTEXT_HOLDER.set(new IdentityHashMap<>());
        }
        return CONTEXT_HOLDER.get();
    }
}
