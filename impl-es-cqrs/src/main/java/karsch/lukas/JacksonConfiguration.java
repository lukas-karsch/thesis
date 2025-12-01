package karsch.lukas;

import com.fasterxml.jackson.databind.SerializationFeature;
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
        var j = JacksonSerializer.builder()
                .defaultTyping()
                .lenientDeserialization()
                .build();

        j.getObjectMapper()
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        return j;
    }
}
