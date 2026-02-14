// `IdentifiableEntity` package name
package org.zouarioss.skinnedratorm.example.base;

// `java` import(s)
import java.util.UUID;

import org.zouarioss.skinnedratorm.annotations.*;
import org.zouarioss.skinnedratorm.flag.GenerationType;

@MappedSuperclass
public abstract class IdentifiableEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  protected UUID id;

  public UUID getId() {
    return id;
  }
} // IdentifiableEntity abstract class
