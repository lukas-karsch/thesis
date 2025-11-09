package karsch.lukas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class EsCqrsApplicationRunner {
    void main(String[] args) {
        SpringApplication.from(EsCqrsApplication::main)
                .with(PostgresTestcontainerConfiguration.class)
                .with(AxonTestcontainerConfiguration.class)
                .run(args);
    }
}
