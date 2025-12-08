package karsch.lukas;

import org.axonframework.test.server.AxonServerContainer;
import org.slf4j.LoggerFactory;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

@TestConfiguration(proxyBeanMethods = false)
public class AxonTestcontainerConfiguration {

    @Bean
    @ServiceConnection
    @RestartScope
    AxonServerContainer axonServerContainer() {
        var logger = LoggerFactory.getLogger(AxonTestcontainerConfiguration.class);
        logger.info("axonServerContainer bean was called");
        return new AxonServerContainer(
                DockerImageName.parse("axoniq/axonserver:latest")
        )
                .withEnv(Map.of("axoniq.axonserver.devmode.enabled", "true"));
    }
}
