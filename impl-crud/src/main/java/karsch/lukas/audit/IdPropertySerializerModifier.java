package karsch.lukas.audit;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.util.Collection;
import java.util.List;

class IdPropertySerializerModifier extends BeanSerializerModifier {

    /**
     * @return true if the <code>type</code> has an "id" field
     */
    private boolean hasIsField(Class<?> type) {
        if (type == null || type.isPrimitive() || type == String.class) {
            return false;
        }
        try {
            type.getDeclaredField("id");
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        for (BeanPropertyWriter writer : beanProperties) {
            JavaType propertyType = writer.getType();
            Class<?> rawPropertyClass = propertyType.getRawClass();

            // Case 1: Single POJO property (e.g., Project.manager: User)
            if (hasIsField(rawPropertyClass)) {
                writer.assignSerializer(new DynamicIdSerializer(rawPropertyClass));
            }

            // Case 2: Collection of POJOs (e.g., CourseEntity.timeSlots: Collection<TimeSlotValueObject>)
            else if (Collection.class.isAssignableFrom(rawPropertyClass)) {
                JavaType elementType = propertyType.getContentType();
                if (elementType != null) {
                    Class<?> rawElementClass = elementType.getRawClass();

                    if (hasIsField(rawElementClass)) {
                        writer.assignSerializer(new CollectionWithIdSerializer(rawElementClass));
                    }
                }
            }
        }
        return beanProperties;
    }
}
