package karsch.lukas.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfiguration {

    @Bean
    @Primary
    public Serializer serializer() {
        var serializer = JacksonSerializer.builder()
                .defaultTyping()
                .lenientDeserialization()
                .build();

        serializer
                .getObjectMapper()
                .registerModule(new Jdk8Module())
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        return serializer;
    }
}
