<!-- PROJECT SHIELDS -->

[![Contributors](https://img.shields.io/badge/CONTRIBUTORS-01-blue?style=plastic)](https://github.com/zouari-oss/skinned-rat-orm/graphs/contributors)
[![Forks](https://img.shields.io/badge/FORKS-00-blue?style=plastic)](https://github.com/zouari-oss/skinned-rat-orm/network/members)
[![Stargazers](https://img.shields.io/badge/STARS-01-blue?style=plastic)](https://github.com/zouari-oss/skinned-rat-orm/stargazers)
[![Issues](https://img.shields.io/badge/ISSUES-00-blue?style=plastic)](https://github.com/zouari-oss/skinned-rat-orm/issues)
[![GPL3.0 License](https://img.shields.io/badge/LICENSE-GPL3.0-blue?style=plastic)](LICENSE)
[![Linkedin](https://img.shields.io/badge/Linkedin-6.7k-blue?style=plastic)](https://www.linkedin.com/in/zouari-omar)

<!-- PROJECT HEADER -->
<div align="center">
  <a href="https://github.com/zouari-oss/skinned-rat-orm">
    <img src="res/img/logo-without-bg.png" alt="skinned-rat-orm-logo" width="300">
  </a>
  <h1>Skinned Rat ORM</h1>
  <h6>A lightweight, annotation-based ORM framework for Java with zero dependencies (excluding JDBC drivers).</h6>
  <br />
</div>

<!-- PROJECT LINKS -->

<p align="center">
  <a href="#overview">Overview</a> •
  <a href="#key-features">Key Features</a> •
  <a href="#quick-start">Quick Start</a> •
  <a href="#supported-annotations">Supported Annotations</a> •
  <a href="#advanced-features">Advanced Features</a> •
  <a href="#supported-databases">Supported Databases</a> •
  <a href="#examples">Examples</a> •
  <a href="#performance">Performance</a> •
  <a href="#roadmap">Roadmap</a> •
  <a href="#contributing">Contributing</a> •
  <a href="#license">License</a> •
  <a href="#contact">Contact</a> •
  <a href="#acknowledgments">Acknowledgments</a>
</p>

<!-- PROJECT TAGS -->

<p align="center">
  <img src="https://img.shields.io/badge/Java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Gradle-%2342A948.svg?style=for-the-badge&logo=gradle&logoColor=white"/>
  <img src="https://img.shields.io/badge/Bash-%23121011.svg?style=for-the-badge&logo=gnu-bash&logoColor=white"/>
  <img src="https://img.shields.io/badge/Git-%23F05032.svg?style=for-the-badge&logo=git&logoColor=white"/>
  <img src="https://img.shields.io/badge/JitPack-0052CC.svg?style=for-the-badge&logo=jitpack&logoColor=white"/>
  <img src="https://img.shields.io/badge/Maven-%23007DCE.svg?style=for-the-badge&logo=apachemaven&logoColor=white"/>
  <img src="https://img.shields.io/badge/JUnit-%23A61E22.svg?style=for-the-badge&logo=junit5&logoColor=white"/>
  <img src="https://img.shields.io/badge/Open%20Source-3DA639?style=for-the-badge&logo=opensourceinitiative&logoColor=white"/>
</p>

## Overview

Skinned Rat ORM is a lightweight, annotation-based ORM framework for Java with zero external dependencies (excluding JDBC drivers).

I developed this package because my instructor explicitly told me not to use **Hibernate** or **any external ORM libraries** and instead work directly with JDBC even if it meant **implementing the ORM manually :)**. This project demonstrates how to build a simple, type-safe, and flexible ORM from scratch while still supporting essential features like CRUD operations, entity relationships, and schema generation.

> [!NOTE]
> Some parts of this project developed with the assistance of AI tools:
> **GitHub Copilot CLI + Claude sonnet 4.5**
> While AI helped accelerate development, a big part of the core ORM logic, design decisions,
> and implementation were authored and reviewed manually (+[youtube](https://www.youtube.com) & [stackoverflow](https://stackoverflow.com) & .. :))
> to ensure correctness and learning outcomes.

## Key Features

- **Simple & Lightweight** - No complex configuration, just annotations
- **Fast** - Direct JDBC with minimal overhead
- **Flexible** - Works with any JDBC-compatible database
- **Zero Dependencies** - Only requires a JDBC driver
- **Type-Safe** - Full compile-time type checking
- **Production-Ready** - Tested with MariaDB, MySQL, PostgreSQL

## Quick Start

### Installation

**Option 1: JitPack (Easiest)**

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.zouari-oss:skinned-rat-orm:v1.0.0'

    // Add your JDBC driver
    implementation 'org.mariadb.jdbc:mariadb-java-client:3.3.2'
}
```

**Option 2: Maven Local (for testing)**

```bash
git clone https://github.com/zouari-oss/skinned-rat-orm.git
cd skinned-rat-orm/project
./gradlew publishToMavenLocal
```

```gradle
repositories {
    mavenLocal()
}

dependencies {
    implementation 'org.zouarioss:skinned-rat-orm:1.0.0'
}
```

### Basic Usage

#### 1. Define Your Entity

```java
import org.zouarioss.skinnedratorm.annotations.*;

@Entity
@Table(name = "users")
public class User extends TimestampedEntity {

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @PrePersist
    protected void onCreate() {
        if (this.role == null) {
            this.role = UserRole.USER;
        }
    }

    // Getters and setters...
}
```

#### 2. Use the EntityManager

```java
import org.zouarioss.skinnedratorm.core.EntityManager;
import org.zouarioss.skinnedratorm.engine.*;
import java.sql.*;

// Setup
Connection connection = DriverManager.getConnection(
    "jdbc:mariadb://localhost:3306/mydb",
    "user",
    "password"
);
EntityManager em = new EntityManager(connection);

// Create table (first time only)
SchemaGenerator generator = new SchemaGenerator(connection, SQLDialect.MYSQL);
generator.createTable(User.class);

// CREATE
User user = new User();
user.setEmail("john@example.com");
user.setPasswordHash("hashed_password");
user.setRole(UserRole.ADMIN);
em.persist(user);

// READ
User found = em.findById(User.class, user.getId());

// UPDATE
found.setRole(UserRole.MODERATOR);
em.update(found);

// DELETE
em.delete(found);

// QUERY
List<User> admins = em.createQuery(User.class)
    .where("role", "ADMIN")
    .orderBy("email")
    .limit(10)
    .getResultList();
```

## Supported Annotations

### Entity Mapping

- `@Entity` - Mark class as an entity
- `@Table(name = "...")` - Specify table name
- `@MappedSuperclass` - Inherit fields without creating table

### Field Mapping

- `@Id` - Primary key field
- `@Column(name, nullable, unique, length, updatable)` - Column configuration
- `@GeneratedValue(strategy = UUID)` - Auto-generate IDs
- `@Enumerated(STRING|ORDINAL)` - Enum mapping strategy

### Timestamps

- `@CreationTimestamp` - Auto-set on creation
- `@UpdateTimestamp` - Auto-update on modification

### Relationships

- `@OneToOne` - One-to-one relationship
- `@ManyToOne` - Many-to-one relationship
- `@JoinColumn` - Foreign key configuration

### Lifecycle

- `@PrePersist` - Called before entity is persisted

### Indexes & Constraints

- `@Index(name, columnList)` - Create indexes
- `@UniqueConstraint(name, columnNames)` - Unique constraints

## Advanced Features

### QueryBuilder API

```java
List<User> users = em.createQuery(User.class)
    .where("role", "=", UserRole.ADMIN)
    .where("email", "LIKE", "%@example.com")
    .orderBy("createdAt", "DESC")
    .limit(20)
    .offset(10)
    .getResultList();

long count = em.createQuery(User.class)
    .where("role", "ADMIN")
    .count();
```

### Schema Generation

```java
SchemaGenerator generator = new SchemaGenerator(connection, SQLDialect.MYSQL);

// Create tables
generator.createTable(User.class);
generator.createTable(Profile.class);

// Drop tables
generator.dropTable(User.class);
```

### Entity Inheritance

```java
@MappedSuperclass
public abstract class IdentifiableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID id;
}

@MappedSuperclass
public abstract class TimestampedEntity extends IdentifiableEntity {
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    protected Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    protected Instant updatedAt;
}

@Entity
@Table(name = "users")
public class User extends TimestampedEntity {
    // Automatically inherits id, createdAt, updatedAt
}
```

## Supported Databases

- MariaDB / MySQL
- PostgreSQL
- H2
- SQLite
- Any JDBC-compatible database

## Examples

See the `/lib/src/test/java/org/zouarioss/skinnedratorm/` directory for complete examples:

- **User Entity** - Basic CRUD operations
- **Profile Entity** - One-to-one relationships
- **AuditLog Entity** - Timestamps and pre-persist callbacks
- **Integration Tests** - Full CRUD test suite with MariaDB

## Performance

Skinned Rat ORM is designed for simplicity and performance:

- **Direct JDBC** - No reflection overhead at runtime
- **Prepared Statements** - SQL injection protection
- **Lazy Initialization** - Metadata cached after first use
- **No Proxy Objects** - Work with real POJOs

## Roadmap

- Lazy loading for relationships
- Batch operations
- Criteria API
- Connection pooling integration
- Query caching
- Multi-database support in one application

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This repository is licensed under the **GPL-3.0 License**. You are free to use, modify, and distribute the content. See the [LICENSE](LICENSE) file for details.

## Contact

For questions or suggestions, feel free to reach out:

- **GitHub**: [ZouariOmar](https://github.com/ZouariOmar)
- **Email**: [zouariomar20@gmail.com](mailto:zouariomar20@gmail.com)
- **LinkedIn**: [Zouari Omar](https://www.linkedin.com/in/zouari-omar)

## Acknowledgments

Built with ❤️ for the Java community.

---

<div align="center">
  <a href="https://github.com/zouari-oss/skinned-rat-orm">
    <img src="res/img/meme.png" alt="skinned-rat-orm-meme" width="600">
  </a>
</div>

After my haircut, I feel like a skinned rat...
**⭐ Star this repo if you find it useful!** and save me from this look!
