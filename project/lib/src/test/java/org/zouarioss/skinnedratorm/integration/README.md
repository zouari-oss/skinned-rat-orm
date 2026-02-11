# MariaDB Integration Test Setup

## Prerequisites

1. **Install MariaDB** (if not already installed)

   ```bash
   # Ubuntu/Debian
   sudo apt-get update
   sudo apt-get install mariadb-server

   # macOS
   brew install mariadb

   # Start MariaDB
   sudo systemctl start mariadb  # Linux
   brew services start mariadb   # macOS
   ```

2. **Configure MariaDB**

   ```bash
   sudo mysql_secure_installation
   ```

3. **Create Test Database and User**

   ```bash
   sudo mysql -u root -p
   ```

   Then run these SQL commands:

   ```sql
   CREATE DATABASE IF NOT EXISTS testdb;
   CREATE USER IF NOT EXISTS 'testuser'@'localhost' IDENTIFIED BY 'testpass';
   GRANT ALL PRIVILEGES ON testdb.* TO 'testuser'@'localhost';
   FLUSH PRIVILEGES;
   EXIT;
   ```

4. **Verify Connection**

   ```bash
   mysql -u testuser -p -D testdb
   # Enter password: testpass
   ```

## Running the Tests

### Run regular unit tests (excludes integration tests)

```bash
./gradlew test
```

### Run ONLY integration tests

```bash
./gradlew test --tests "UserCRUDTest"
# OR
./gradlew test -Dtest.profile=integration
```

### Run all tests including integration

```bash
./gradlew test --tests "*"
```

### Run specific test method

```bash
./gradlew test --tests "UserCRUDTest.testPersistUser"
```

## Test Configuration

If your MariaDB is running on a different host/port, update these constants in `UserCRUDTest.java`:

```java
private static final String DB_URL = "jdbc:mariadb://localhost:3306/testdb";
private static final String DB_USER = "testuser";
private static final String DB_PASSWORD = "testpass";
```

## What the Test Does

The `UserCRUDTest` demonstrates all CRUD operations:

1. **CREATE** - Persist a new user with auto-generated ID and timestamps
2. **READ** - Retrieve user by ID
3. **READ ALL** - Get all users from database
4. **QUERY** - Use QueryBuilder to filter users
5. **UPDATE** - Modify user and save changes
6. **DELETE by ID** - Remove user using ID
7. **DELETE** - Remove user using entity

## Expected Output

When tests run successfully, you'll see:

```
✓ Database setup complete

=== TEST: CREATE USER ===
✓ User created with ID: <uuid>
  Email: john.doe@example.com
  Role: USER
  Account Status: ACTIVE
  Presence Status: ONLINE
  Created At: <timestamp>

=== TEST: READ USER BY ID ===
✓ User retrieved successfully
  ID: <uuid>
  Email: john.doe@example.com
  Role: USER

... (and so on for all 7 tests)

✓ Database connection closed
```

## Troubleshooting

### Connection refused

- Ensure MariaDB is running: `sudo systemctl status mariadb`
- Check port: `sudo netstat -tulpn | grep 3306`

### Access denied

- Verify credentials: `mysql -u testuser -p`
- Re-grant permissions if needed

### Table doesn't exist

- The test automatically creates/drops tables
- Check user has CREATE/DROP privileges

### Driver not found

- Run `./gradlew build` to download MariaDB JDBC driver
