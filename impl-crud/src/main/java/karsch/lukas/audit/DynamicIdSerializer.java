package karsch.lukas.audit;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

class DynamicIdSerializer extends JsonSerializer<Object> {

    private final Method idGetter;
    private static final ConcurrentHashMap<Class<?>, Method> methodCache = new ConcurrentHashMap<>();

    public DynamicIdSerializer(Class<?> type) {
        idGetter = methodCache.computeIfAbsent(type, k -> {
            try {
                return k.getMethod("getId");
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Type " + k.getName() + " missing expected getId() method. Create a getter for the field.", e);
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
            Object idValue = idGetter.invoke(value);
            gen.writeObject(idValue);
        } catch (Exception e) {
            throw new IOException("Failed to dynamically access ID on object of type " + value.getClass().getName(), e);
        }
    }
}
