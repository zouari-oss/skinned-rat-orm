package org.zouarioss.skinnedratorm.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.zouarioss.skinnedratorm.core.EntityManager;
import org.zouarioss.skinnedratorm.engine.QueryBuilder;
import org.zouarioss.skinnedratorm.engine.SQLDialect;
import org.zouarioss.skinnedratorm.engine.SchemaGenerator;
import org.zouarioss.skinnedratorm.example.AccountStatus;
import org.zouarioss.skinnedratorm.example.PresenceStatus;
import org.zouarioss.skinnedratorm.example.User;
import org.zouarioss.skinnedratorm.example.UserRole;

/**
 * Integration test for User CRUD operations with MariaDB.
 * 
 * Prerequisites:
 * - MariaDB server running on localhost:3306
 * - Database 'testdb' created
 * - User 'testuser' with password 'testpass' and permissions
 * 
 * To set up MariaDB:
 * ```sql
 * CREATE DATABASE IF NOT EXISTS testdb;
 * CREATE USER IF NOT EXISTS 'testuser'@'localhost' IDENTIFIED BY 'testpass';
 * GRANT ALL PRIVILEGES ON testdb.* TO 'testuser'@'localhost';
 * FLUSH PRIVILEGES;
 * ```
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("integration")
public class UserCRUDTest {

  private static Connection connection;
  private static EntityManager entityManager;
  private static SchemaGenerator schemaGenerator;
  private static java.util.UUID testUserId;

  // Database configuration - adjust these to match your MariaDB setup
  private static final String DB_URL = "jdbc:mariadb://localhost:3306/testdb";
  private static final String DB_USER = "root";
  private static final String DB_PASSWORD = "root";

  @BeforeAll
  static void setUp() throws Exception {
    // Load MariaDB driver
    Class.forName("org.mariadb.jdbc.Driver");

    // Create connection
    connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    connection.setAutoCommit(true);

    // Initialize EntityManager and SchemaGenerator
    entityManager = new EntityManager(connection);
    schemaGenerator = new SchemaGenerator(connection, SQLDialect.MYSQL);

    // Drop and recreate table for clean test
    try {
      schemaGenerator.dropTable(User.class);
    } catch (final Exception e) {
      // Table might not exist, ignore
    }

    schemaGenerator.createTable(User.class);

    System.out.println("✓ Database setup complete");
  }

  @AfterAll
  static void tearDown() throws Exception {
    if (connection != null && !connection.isClosed()) {
      // Optionally clean up
      // schemaGenerator.dropTable(User.class);
      connection.close();
      System.out.println("✓ Database connection closed");
    }
  }

  @Test
  @Order(1)
  @DisplayName("1. CREATE - Persist a new user")
  void testPersistUser() throws Exception {
    System.out.println("\n=== TEST: CREATE USER ===");

    // Create a new user
    final User user = new User();
    user.setEmail("john.doe@example.com");
    user.setPasswordHash("$2a$10$hashed_password_here");
    user.setRole(UserRole.USER);
    // presenceStatus and accountStatus will be set by @PrePersist

    // Persist the user
    entityManager.persist(user);

    // Verify the user was assigned an ID
    assertNotNull(user.getId(), "User ID should be generated");
    testUserId = user.getId();

    System.out.println("✓ User created with ID: " + testUserId);
    System.out.println("  Email: " + user.getEmail());
    System.out.println("  Role: " + user.getRole());
    System.out.println("  Account Status: " + user.getAccountStatus());
    System.out.println("  Presence Status: " + user.getPresenceStatus());
    System.out.println("  Created At: " + user.getCreatedAt());
  }

  @Test
  @Order(2)
  @DisplayName("2. READ - Retrieve user by ID")
  void testFindUserById() throws Exception {
    System.out.println("\n=== TEST: READ USER BY ID ===");

    // Find the user by ID
    final User foundUser = entityManager.findById(User.class, testUserId);

    // Verify the user was found
    assertNotNull(foundUser, "User should be found");
    assertEquals(testUserId, foundUser.getId());
    assertEquals("john.doe@example.com", foundUser.getEmail());
    assertEquals(UserRole.USER, foundUser.getRole());
    assertEquals(AccountStatus.ACTIVE, foundUser.getAccountStatus());
    assertEquals(PresenceStatus.ONLINE, foundUser.getPresenceStatus());

    System.out.println("✓ User retrieved successfully");
    System.out.println("  ID: " + foundUser.getId());
    System.out.println("  Email: " + foundUser.getEmail());
    System.out.println("  Role: " + foundUser.getRole());
  }

  @Test
  @Order(3)
  @DisplayName("3. READ ALL - Retrieve all users")
  void testFindAllUsers() throws Exception {
    System.out.println("\n=== TEST: READ ALL USERS ===");

    // Create a second user
    final User user2 = new User();
    user2.setEmail("jane.smith@example.com");
    user2.setPasswordHash("$2a$10$another_hash");
    user2.setRole(UserRole.ADMIN);
    entityManager.persist(user2);

    // Find all users
    final List<User> allUsers = entityManager.findAll(User.class);

    // Verify we have at least 2 users
    assertTrue(allUsers.size() >= 2, "Should have at least 2 users");

    System.out.println("✓ Found " + allUsers.size() + " user(s):");
    for (final User u : allUsers) {
      System.out.println("  - " + u.getEmail() + " (" + u.getRole() + ")");
    }
  }

  @Test
  @Order(4)
  @DisplayName("4. QUERY - Find users with QueryBuilder")
  void testQueryUsers() throws Exception {
    System.out.println("\n=== TEST: QUERY USERS ===");

    // Query users with ADMIN role
    final List<User> admins = entityManager.createQuery(User.class)
        .where("role", "ADMIN")
        .orderBy("email")
        .getResultList();

    assertTrue(admins.size() >= 1, "Should find at least one admin");
    assertEquals(UserRole.ADMIN, admins.get(0).getRole());

    System.out.println("✓ Found " + admins.size() + " admin user(s):");
    for (final User admin : admins) {
      System.out.println("  - " + admin.getEmail());
    }

    // Query users with specific email pattern
    final QueryBuilder<User> query = entityManager.createQuery(User.class);
    final long userCount = query.count();

    System.out.println("✓ Total user count: " + userCount);
  }

  @Test
  @Order(5)
  @DisplayName("5. UPDATE - Modify user properties")
  void testUpdateUser() throws Exception {
    System.out.println("\n=== TEST: UPDATE USER ===");

    // Find the user
    final User user = entityManager.findById(User.class, testUserId);
    assertNotNull(user);

    // Update user properties
    user.setRole(UserRole.MODERATOR);
    user.setPresenceStatus(PresenceStatus.AWAY);

    // Save changes
    entityManager.update(user);

    // Verify the update by fetching again
    final User updatedUser = entityManager.findById(User.class, testUserId);
    assertEquals(UserRole.MODERATOR, updatedUser.getRole());
    assertEquals(PresenceStatus.AWAY, updatedUser.getPresenceStatus());
    assertNotNull(updatedUser.getUpdatedAt(), "Updated timestamp should be set");

    System.out.println("✓ User updated successfully");
    System.out.println("  New Role: " + updatedUser.getRole());
    System.out.println("  New Presence: " + updatedUser.getPresenceStatus());
    System.out.println("  Updated At: " + updatedUser.getUpdatedAt());
  }

  @Test
  @Order(6)
  @DisplayName("6. DELETE - Remove user by ID")
  void testDeleteUserById() throws Exception {
    System.out.println("\n=== TEST: DELETE USER BY ID ===");

    // Create a user to delete
    final User userToDelete = new User();
    userToDelete.setEmail("temp.user@example.com");
    userToDelete.setPasswordHash("$2a$10$temp_hash");
    userToDelete.setRole(UserRole.USER);
    entityManager.persist(userToDelete);

    final java.util.UUID deleteId = userToDelete.getId();
    assertNotNull(deleteId);

    // Delete by ID
    entityManager.deleteById(User.class, deleteId);

    // Verify deletion
    final User deletedUser = entityManager.findById(User.class, deleteId);
    assertNull(deletedUser, "User should be deleted");

    System.out.println("✓ User deleted successfully (ID: " + deleteId + ")");
  }

  @Test
  @Order(7)
  @DisplayName("7. DELETE - Remove user entity")
  void testDeleteUser() throws Exception {
    System.out.println("\n=== TEST: DELETE USER ENTITY ===");

    // Find the original test user
    final User user = entityManager.findById(User.class, testUserId);
    assertNotNull(user);

    // Delete the entity
    entityManager.delete(user);

    // Verify deletion
    final User deletedUser = entityManager.findById(User.class, testUserId);
    assertNull(deletedUser, "User should be deleted");

    System.out.println("✓ User entity deleted successfully");
  }
}
