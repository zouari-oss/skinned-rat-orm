// `TimestampedEntity` pckage name
package org.zouarioss.skinnedratorm.example.base;

import java.time.Instant;

import org.zouarioss.skinnedratorm.annotations.Column;
import org.zouarioss.skinnedratorm.annotations.CreationTimestamp;
import org.zouarioss.skinnedratorm.annotations.MappedSuperclass;
import org.zouarioss.skinnedratorm.annotations.UpdateTimestamp;

@MappedSuperclass
public abstract class TimestampedEntity extends IdentifiableEntity {
  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  protected Instant createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  protected Instant updatedAt;

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
} // TimestampedEntity abstract class
