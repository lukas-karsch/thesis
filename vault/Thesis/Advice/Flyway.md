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
Hibernate offers a schema-diff tool that may be used to quickly generate migration files after making changes to `@Entity` classes.
```bash
./mvnw spring-boot:run -Dspring.jpa.properties.hibernate.hbm2ddl.auto=none \
  -Dspring.jpa.properties.hibernate.hbm2ddl.schema_gen_script_source=file:myschema.sql
```
(untested code)