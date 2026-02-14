/**
 * User.java
 *
 * <p>
 * Represents a system user within the Access Control application.
 * This entity is mapped to the {@code users} database table
 * </p>
 *
 * @author  @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since   2026-02-02
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/model/User.java"
 * target="_blank">
 * User.java
 * </a>
 */

// `User` package name
package org.zouarioss.skinnedratorm.example;

import org.zouarioss.skinnedratorm.annotations.*;
import org.zouarioss.skinnedratorm.flag.*;
import org.zouarioss.skinnedratorm.example.base.TimestampedEntity;

@Entity
@Table(name = "users")
public final class User extends TimestampedEntity {
  @Column(nullable = false, unique = true, length = 150)
  private String email;

  @Column(name = "password", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role;

  @Enumerated(EnumType.STRING)
  @Column(name = "presence_status", nullable = false)
  private PresenceStatus presenceStatus; // Pre-persist

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AccountStatus accountStatus; // Pre-persist

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = false) // User must have a profile
  private Profile profile;

  // #########################
  // ### GETTERS & SETTERS ###
  // #########################

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public PresenceStatus getPresenceStatus() {
    return presenceStatus;
  }

  public void setPresenceStatus(PresenceStatus presenceStatus) {
    this.presenceStatus = presenceStatus;
  }

  public AccountStatus getAccountStatus() {
    return accountStatus;
  }

  // #############################
  // ### PRE_PERSIST METHOD(S) ###
  // #############################

  @PrePersist
  protected void onAction() {
    // Set accountStatus if not already set
    if (this.accountStatus == null) {
      this.accountStatus = AccountStatus.ACTIVE;
    }

    // Set presenceStatus if not already set
    if (this.presenceStatus == null)
      this.presenceStatus = PresenceStatus.ONLINE;
  }
} // User final class
