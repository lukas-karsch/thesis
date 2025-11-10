package karsch.lukas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class EsCqrsApplicationRunner {

    static void main(String[] args) {
        SpringApplication.from(EsCqrsApplication::main)
                .with(AxonTestcontainerConfiguration.class)
                .with(PostgresTestcontainerConfiguration.class)
                .run(args);
    }
}
