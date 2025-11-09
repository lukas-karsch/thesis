package karsch.lukas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class CrudApplicationRunner {
    void main(String[] args) {
        SpringApplication.from(CrudApplication::main)
                .with(PostgresTestcontainerConfiguration.class)
                .run(args);
    }
}
