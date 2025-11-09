package karsch.lukas;

import org.axonframework.test.server.AxonServerContainer;
import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.utility.DockerImageName;

public class AxonTestcontainerConfiguration {

    @Bean
    @ServiceConnection
    @RestartScope
    AxonServerContainer axonServerContainer() {
        return new AxonServerContainer(
                DockerImageName.parse("axoniq/axonserver:latest")
        );
    }
}
