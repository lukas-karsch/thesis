package karsch.lukas;

import org.springframework.boot.SpringApplication;

public class TestCrudApplication {

    void main(String[] args) {
        SpringApplication.from(CrudApplication::main)
                .with(PostgresTestcontainerConfiguration.class)
                .run(args);
    }
}
