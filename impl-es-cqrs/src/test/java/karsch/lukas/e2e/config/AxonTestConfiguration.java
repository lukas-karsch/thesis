package karsch.lukas.e2e.config;

import org.axonframework.config.ConfigurerModule;
import org.axonframework.config.EventProcessingConfigurer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class AxonTestConfiguration {
    @Bean
    public ConfigurerModule processorDefaultConfigurerModule() {
        return configurer -> configurer.eventProcessing(EventProcessingConfigurer::usingSubscribingEventProcessors);
    }
}
