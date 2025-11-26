package karsch.lukas;

import org.springframework.boot.SpringApplication;

public class EsCqrsApplicationRunner {

    static void main(String[] args) {
        SpringApplication.from(EsCqrsApplication::main)
                .with(AxonTestcontainerConfiguration.class)
                .with(PostgresTestcontainerConfiguration.class)
                .run(args);
    }
}
