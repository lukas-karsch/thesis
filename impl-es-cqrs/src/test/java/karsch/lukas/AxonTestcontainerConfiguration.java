package karsch.lukas;

import org.axonframework.test.server.AxonServerContainer;
import org.slf4j.LoggerFactory;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.utility.DockerImageName;

public class AxonTestcontainerConfiguration {

    @Bean
    @ServiceConnection
    @RestartScope
    AxonServerContainer axonServerContainer() {
        var logger = LoggerFactory.getLogger(AxonTestcontainerConfiguration.class);
        logger.info("axonServerContainer bean was called");
        return new AxonServerContainer(
                DockerImageName.parse("axoniq/axonserver:latest")
        );
    }
}
