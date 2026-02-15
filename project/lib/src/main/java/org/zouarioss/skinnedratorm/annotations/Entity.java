// `Entity` package name
package org.zouarioss.skinnedratorm.annotations;

// `java` import(s)
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a persistent entity managed by the Skinned Rat ORM
 * framework.
 *
 * <p>
 * The {@code @Entity} annotation designates a Java class as a database-mapped
 * entity. Classes annotated with {@code @Entity} are scanned by the ORM engine
 * and mapped to corresponding database tables.
 * </p>
 *
 * <p>
 * An entity typically represents a table in the relational database, where:
 * </p>
 * <ul>
 * <li>The class corresponds to a database table</li>
 * <li>Fields correspond to table columns</li>
 * <li>One field is usually annotated with {@code @Id} to represent the primary
 * key</li>
 * </ul>
 *
 * <p>
 * This annotation is retained at runtime, allowing the ORM framework to
 * detect and process entity metadata using reflection.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * 
 * <pre>
 * &#64;Entity
 * public class User {
 *   &#64;Id
 *   private Long id;
 *
 *   &#64;Column(nullable = false)
 *   private String username;
 * }
 * </pre>
 *
 * <p>
 * Entities may optionally be combined with other mapping annotations such as
 * {@code @Table}, {@code @Column}, {@code @CreationTimestamp}, and
 * {@code @UpdateTimestamp} to provide detailed schema configuration.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-15
 *
 * @see org.zouarioss.skinnedratorm.annotations.Table
 * @see org.zouarioss.skinnedratorm.annotations.Id
 *
 *      <a href=
 *      "https://github.com/zouari-oss/skinned-rat-orm/tree/main/project/lib/src/main/java/org/zouarioss/skinnedratorm/annotations/Entity.java"
 *      target="_blank">
 *      Entity.java
 *      </a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {
} // Entity @interface
