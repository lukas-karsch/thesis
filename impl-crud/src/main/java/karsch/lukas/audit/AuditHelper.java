package karsch.lukas.audit;

class AuditHelper {
    static String getNameFromEntityClass(Class<?> clz) {
        return clz.getSimpleName();
    }
}
