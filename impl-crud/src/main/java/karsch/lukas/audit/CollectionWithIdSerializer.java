package karsch.lukas.audit;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;
import java.util.Collection;

class CollectionWithIdSerializer extends JsonSerializer<Object> implements ContextualSerializer {

    private final Class<?> elementClass;

    public CollectionWithIdSerializer(Class<?> elementClass) {
        this.elementClass = elementClass;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        DynamicIdSerializer elementSerializer = new DynamicIdSerializer(elementClass);

        if (value instanceof Collection<?> c) {
            gen.writeStartArray();
            for (Object element : c) {
                elementSerializer.serialize(element, gen, serializers);
            }
            gen.writeEndArray();
        } else {
            elementSerializer.serialize(value, gen, serializers);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) {
        return this;
    }
}
