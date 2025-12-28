#migration 
How to work with Flyway.
## Dependency 
```xml
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-core</artifactId>
</dependency>
```
Should then be auto-configured by SpringBoot 
## Configuration in application.properties
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

> [!Warning] ddl-auto
> Important that ddl-auto is set to `validate`! App will fail fast at startup if schema does not match.
## Migrations 
Name migration files like this:
`V1__add_users_table.sql`
Write regular SQL.

Make sure to add constraints to the SQL! 
### Auto-generate diffs 
Diffs from code changes can be generated using IntelliJ 