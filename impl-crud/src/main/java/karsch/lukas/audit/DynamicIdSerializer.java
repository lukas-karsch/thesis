package karsch.lukas.audit;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

class DynamicIdSerializer extends JsonSerializer<Object> {

    private final Field idField;
    private static final ConcurrentHashMap<Class<?>, Field> fieldCache = new ConcurrentHashMap<>();

    public DynamicIdSerializer(Class<?> type) {
        idField = fieldCache.computeIfAbsent(type, k -> {
            try {
                return k.getDeclaredField("id");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("Type " + k.getName() + " missing expected 'id' field.", e);
            }
        });
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        try {
            idField.setAccessible(true);
            Object idValue = idField.get(value);

            gen.writeObject(idValue);
        } catch (Exception e) {
            throw new IOException("Failed to dynamically access ID on object of type " + value.getClass().getName(), e);
        }
    }
}
