package org.zouarioss.skinnedratorm.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.zouarioss.skinnedratorm.annotations.Column;
import org.zouarioss.skinnedratorm.annotations.Entity;
import org.zouarioss.skinnedratorm.annotations.Id;
import org.zouarioss.skinnedratorm.annotations.ManyToOne;
import org.zouarioss.skinnedratorm.annotations.OneToOne;
import org.zouarioss.skinnedratorm.annotations.PrePersist;
import org.zouarioss.skinnedratorm.annotations.PreUpdate;
import org.zouarioss.skinnedratorm.annotations.PostPersist;
import org.zouarioss.skinnedratorm.annotations.PostUpdate;
import org.zouarioss.skinnedratorm.annotations.Table;

public class EntityMetadata {

  private final String tableName;
  private final List<FieldMetadata> fields = new ArrayList<>();
  private final List<FieldMetadata> relationshipFields = new ArrayList<>();
  private FieldMetadata idField;
  private final List<Method> prePersistMethods = new ArrayList<>();
  private final List<Method> preUpdateMethods = new ArrayList<>();
  private final List<Method> postPersistMethods = new ArrayList<>();
  private final List<Method> postUpdateMethods = new ArrayList<>();

  public EntityMetadata(final Class<?> clazz) {

    if (!clazz.isAnnotationPresent(Entity.class)) {
      throw new RuntimeException("Class is not annotated with @Entity");
    }

    final Table table = clazz.getAnnotation(Table.class);
    this.tableName = (table != null) ? table.name() : clazz.getSimpleName().toLowerCase();

    extractRecursively(clazz);
  }

  private void extractRecursively(final Class<?> clazz) {
    if (clazz == null || clazz == Object.class)
      return;

    extractRecursively(clazz.getSuperclass());

    for (final Field field : clazz.getDeclaredFields()) {
      // Check if it's a relationship field
      if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class)) {
        final FieldMetadata metadata = new FieldMetadata(field);
        relationshipFields.add(metadata);
        // Don't add to regular fields - handle separately
        continue;
      }

      if (field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(Id.class)) {

        final FieldMetadata metadata = new FieldMetadata(field);

        if (metadata.isId()) {
          idField = metadata;
        }

        fields.add(metadata);
      }
    }

    for (final Method method : clazz.getDeclaredMethods()) {
      if (method.isAnnotationPresent(PrePersist.class)) {
        method.setAccessible(true);
        prePersistMethods.add(method);
      }
      if (method.isAnnotationPresent(PreUpdate.class)) {
        method.setAccessible(true);
        preUpdateMethods.add(method);
      }
      if (method.isAnnotationPresent(PostPersist.class)) {
        method.setAccessible(true);
        postPersistMethods.add(method);
      }
      if (method.isAnnotationPresent(PostUpdate.class)) {
        method.setAccessible(true);
        postUpdateMethods.add(method);
      }
    }
  }

  public String getTableName() {
    return tableName;
  }

  public List<FieldMetadata> getFields() {
    return fields;
  }

  public FieldMetadata getIdField() {
    return idField;
  }

  public List<Method> getPrePersistMethods() {
    return prePersistMethods;
  }

  public List<Method> getPreUpdateMethods() {
    return preUpdateMethods;
  }

  public List<Method> getPostPersistMethods() {
    return postPersistMethods;
  }

  public List<Method> getPostUpdateMethods() {
    return postUpdateMethods;
  }

  public List<FieldMetadata> getRelationshipFields() {
    return relationshipFields;
  }
}
