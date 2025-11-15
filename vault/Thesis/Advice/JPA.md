#jpa
Best Practices and code examples. 
## 1. Database Migrations
**Use Flyway.
- **Flyway** is simple, version-based (`V1__init.sql`, `V2__add_user.sql`, …).
- Place migration files in `src/main/resources/db/migration` 
- Avoid `hibernate.hbm2ddl.auto` in production; it conflicts with migrations.
- Use SQL migrations for transparency; use Java migrations only when logic is needed.
- Always create **repeatable migrations** (`R__normalize.sql`) for views, functions, seed data.
- Don’t let JPA auto-create schema—migrate first, then run the app.
---
## 2. Defining Entities
**Basic Rules**
- Annotate with `@Entity` + `@Table`.
- Always define an `@Id` with a proper strategy (`@GeneratedValue(strategy = GenerationType.IDENTITY)`; avoid `AUTO`).
- Add `@Column(nullable = …, unique = …)` to enforce integrity—don’t rely solely on DB.
- Prefer `LocalDate/LocalDateTime` over `Date`.
- Keep entities **lean**: no business logic, no large helper methods.
- Use `@Embeddable` for value objects (addresses, money, coordinates)
- Mark collections as `Set<>` over `List<>` unless ordering matters (better change detection).
---
## 3. Relationships
### One-To-Many (Bidirectional)
- Use only when needed. Bidirectional relationships are notorious for bugs.
- In `@OneToMany(mappedBy = "parent")`, the **child owns the foreign key**, not the parent.
- When you add a child, **set both sides**:  
    `child.setParent(parent); parent.getChildren().add(child);`
- Keep the collection `LAZY`.
```java 
@Entity
public class Order {
    @Id @GeneratedValue
    private Long id;

    @OneToMany(
        mappedBy = "order",
        cascade = { CascadeType.PERSIST, CascadeType.MERGE },
        orphanRemoval = true
    )
    private Set<OrderItem> items = new HashSet<>();

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this); // maintain both sides
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
} ```

```java 
@Entity
public class OrderItem {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // setter required for bidirectional maintenance
    public void setOrder(Order order) {
        this.order = order;
    }
}```
### Many-To-One (Unidirectional)
- Almost always `LAZY`.
- This is the owning side; put `@JoinColumn` here.
- Avoid unnecessary cascade types—`PERSIST` and `MERGE` are often enough.
- no back-navigation
```java
@Entity
public class Comment {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post; // Post does not have a relationship field 
} ```
### Many-To-Many
Avoid it _whenever possible_. Use a join entity instead (turn M:N into two 1:N).
**Join entity:**
```java
@Entity
public class Enrollment {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    private LocalDate enrolledOn;
}```
### One-To-One
- Default to `LAZY` (but some providers force `EAGER`, check).
- Prefer putting the FK on the child entity.
```java
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id")
    private UserProfile profile;

    public void setProfile(UserProfile profile) {
        this.profile = profile;
        profile.setUser(this);
    }
}
```

```java
@Entity
public class UserProfile {
    @Id @GeneratedValue
    private Long id;

    @OneToOne(mappedBy = "profile", fetch = FetchType.LAZY)
    private User user;

    public void setUser(User user) {
        this.user = user;
    }
}
```

---
## 4. Repositories
- Use `interface UserRepository extends JpaRepository<User, Long>`.
- Derive queries using method names (`findByEmail(String email)`).
- For complex queries:
    - Use `@Query` with JPQL.
    - For dynamic filters: use **Specification** or **QueryDSL**.
- Return `Optional<T>` for single entities, not `null`.

---
## 5. Transactions
#locking #transaction
- Annotate service layer methods with `@Transactional`.
- Never place `@Transactional` on controller classes.
- Lazy-loaded relationships require an active transaction—avoid accessing them in controllers.
- For read-only operations: `@Transactional(readOnly = true)` improves performance.
### Locking examples - Pessimistic Locking 
```java
@Entity
public class Item {
    @Id @GeneratedValue
    private Long id;

    private int stock;
}
```

```java
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Item i WHERE i.id = :id")
    Item findByIdForUpdate(@Param("id") Long id);
}
```
Generates SQL like this: `... FOR UPDATE;`
Service method: 
```java
@Transactional
    public void placeOrder(Long itemId, int quantity) {
        // Locks the row until the transaction ends
        Item item = itemRepository.findByIdForUpdate(itemId);
        if (item.getStock() < quantity) {
            throw new IllegalStateException("Not enough stock");
        }

        item.setStock(item.getStock() - quantity);

        Order order = new Order(itemId, quantity);
        orderRepository.save(order);
    }
```
### Locking examples - optimistic locking 
Add a `version` field to the entity:
```java
@Entity
public class Item {
    @Id @GeneratedValue
    private Long id;

    private int stock;

    @Version
    private long version;
}
```
Service method: 
```java
@Transactional
public void placeOrder(Long itemId, int quantity) {
    Item item = itemRepository.findById(itemId)
        .orElseThrow();

    if (item.getStock() < quantity) {
        throw new IllegalStateException("Not enough stock");
    }

    item.setStock(item.getStock() - quantity);

    // On commit, Hibernate checks version.
    // If someone else changed the row -> OptimisticLockException
}
```
---
## 6. Performance Essentials
- **N+1 problem** is the most common issue:
    - Fix with `@EntityGraph` or explicit `JOIN FETCH`. 
    - Entity Graph https://www.baeldung.com/jpa-entity-graph
- Avoid `CascadeType.REMOVE` on large graphs—it can result in massive cascade deletes.
- Use batch fetching:  
    `spring.jpa.properties.hibernate.default_batch_fetch_size = 50`
- Consider DTO projections for heavy reads.
- **Hypersistence Utils** to detect N+1 queries 

---
## 7. Testing
- Use `@DataJpaTest` for lightweight JPA testing.
- Prefer **Testcontainers** over H2 to avoid dialect differences.
- Load minimal schema/data via migrations so prod/test schemas match.

---
## 8. Common Pitfalls
- Don’t expose entities through REST (use DTOs).
- Avoid mutating entity relationships outside the transaction.
- HashCode/equals:
    - Don’t use mutable fields.
    - Ideal: use only the identifier **once assigned** or use business keys.
- Don’t rely on `CascadeType.ALL`. Be explicit! 
## Configuration 
`application.yml` (needs reformatting to application.properties)
```yaml
spring:
  jpa:
    show-sql: false     # NEVER use this—it shows only SQL, not bindings
    properties:
      hibernate:
        format_sql: true
        generate_statistics: true

logging:
  level:
    org.hibernate.SQL: debug               # actual SQL
    org.hibernate.orm.jdbc.bind: trace     # SQL parameters
    org.hibernate.stat: debug              # second-level stats
```

Hypersistence Utils: 
```xml
<dependency>
	<groupId>io.hypersistence</groupId>
	<artifactId>hypersistence-utils-hibernate-63</artifactId>
	<version>3.8.1</version>
</dependency>
```
`application.properties`
```properties
spring.jpa.properties.hibernate.session_factory.statement_inspector=com.vladmihalcea.hibernate.type.util.CamelCaseToSnakeCaseNamingStrategy # select N+1 inspector
```
## Related 
- Migrations with [[Flyway]]
