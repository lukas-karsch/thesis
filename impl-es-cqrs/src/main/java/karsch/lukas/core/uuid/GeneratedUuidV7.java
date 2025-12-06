package karsch.lukas.core.uuid;

import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

// taken from https://manbunder.medium.com/streamline-uuid-v7-generation-in-spring-boot-entities-with-custom-annotations-hibernate-6-5-4ddc018895cf

@IdGeneratorType(UuidV7Generator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, METHOD})
public @interface GeneratedUuidV7 {
}
