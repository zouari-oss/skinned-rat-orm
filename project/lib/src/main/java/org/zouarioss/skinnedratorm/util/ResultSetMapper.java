package org.zouarioss.skinnedratorm.util;

import java.sql.Connection;
import java.sql.ResultSet;

import org.zouarioss.skinnedratorm.metadata.EntityMetadata;
import org.zouarioss.skinnedratorm.flag.EnumType;
import org.zouarioss.skinnedratorm.metadata.FieldMetadata;

public class ResultSetMapper {

  public static <T> T mapResultToEntity(
      final Class<T> clazz,
      final EntityMetadata metadata,
      final ResultSet rs,
      final Connection connection) throws Exception {

    final T instance = clazz.getDeclaredConstructor().newInstance();

    for (final FieldMetadata fieldMeta : metadata.getFields()) {

      Object value = rs.getObject(fieldMeta.getColumnName());

      if (value != null) {
        // Handle UUID conversion
        if (fieldMeta.getField().getType() == java.util.UUID.class && value instanceof String) {
          value = java.util.UUID.fromString((String) value);
        }
        // Handle Timestamp to Instant conversion
        else if (fieldMeta.getField().getType() == java.time.Instant.class && value instanceof java.sql.Timestamp) {
          value = ((java.sql.Timestamp) value).toInstant();
        }
        // Handle enum conversion
        else if (fieldMeta.getEnumType() != null) {
          value = convertToEnum(fieldMeta, value);
        }
      }

      fieldMeta.getField().set(instance, value);
    }

    // Load OneToOne relationships (only owning side has join column)
    for (final FieldMetadata relationshipMeta : metadata.getRelationshipFields()) {

      if (relationshipMeta.isOwningSide() && relationshipMeta.getJoinColumnName() != null) {
        // Get the foreign key value from the result set
        Object foreignKeyValue = rs.getObject(relationshipMeta.getJoinColumnName());

        if (foreignKeyValue != null) {
          // Convert to UUID if needed
          if (foreignKeyValue instanceof String) {
            foreignKeyValue = java.util.UUID.fromString((String) foreignKeyValue);
          }

          // Load the related entity
          final Class<?> relatedClass = relationshipMeta.getField().getType();
          final org.zouarioss.skinnedratorm.core.EntityManager em = new org.zouarioss.skinnedratorm.core.EntityManager(
              connection);
          final Object relatedEntity = em.findById(relatedClass, foreignKeyValue);

          // Set the related entity
          relationshipMeta.getField().set(instance, relatedEntity);
        }
      }
    }

    return instance;
  }

  @SuppressWarnings("unchecked")
  private static Object convertToEnum(final FieldMetadata fieldMeta, final Object value) {
    final Class<?> enumType = fieldMeta.getField().getType();

    if (!enumType.isEnum()) {
      return value;
    }

    if (fieldMeta.getEnumType() == EnumType.STRING) {
      return Enum.valueOf((Class<? extends Enum>) enumType, value.toString());
    } else {
      final Object[] constants = enumType.getEnumConstants();
      return constants[(Integer) value];
    }
  }
}
