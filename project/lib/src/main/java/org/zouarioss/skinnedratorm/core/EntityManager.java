package org.zouarioss.skinnedratorm.core;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.zouarioss.skinnedratorm.annotations.EnumType;
import org.zouarioss.skinnedratorm.annotations.GenerationType;
import org.zouarioss.skinnedratorm.engine.QueryBuilder;
import org.zouarioss.skinnedratorm.metadata.EntityMetadata;
import org.zouarioss.skinnedratorm.metadata.FieldMetadata;
import org.zouarioss.skinnedratorm.metadata.MetadataRegistry;

public class EntityManager {

  private final Connection connection;

  public EntityManager(final Connection connection) {
    this.connection = connection;
  }

  public <T> void persist(final T entity) throws Exception {

    final EntityMetadata metadata = MetadataRegistry.getMetadata(entity.getClass());

    // Invoke lifecycle callbacks (@PrePersist)
    for (final Method method : metadata.getPrePersistMethods()) {
      method.invoke(entity);
    }

    // Handle timestamps (CreationTimestamp / UpdateTimestamp)
    handleTimestamps(entity, metadata, false);

    // Handle cascading persists for OneToOne relationships (only on owning side)
    for (final FieldMetadata relationshipMeta : metadata.getRelationshipFields()) {
      
      if (relationshipMeta.isOwningSide()) { // Only cascade on owning side
        final Object relatedEntity = relationshipMeta.getField().get(entity);
        
        if (relatedEntity != null) {
          // Check if the related entity has an ID (already persisted)
          final EntityMetadata relatedMetadata = MetadataRegistry.getMetadata(relatedEntity.getClass());
          final Object relatedId = relatedMetadata.getIdField().getField().get(relatedEntity);
          
          // If no ID, cascade persist
          if (relatedId == null) {
            persist(relatedEntity); // Recursive cascade
          }
        }
      }
    }

    // Build SQL
    final StringBuilder columns = new StringBuilder();
    final StringBuilder values = new StringBuilder();

    for (final FieldMetadata fieldMeta : metadata.getFields()) {
      columns.append(fieldMeta.getColumnName()).append(",");
      values.append("?,");
    }

    // Add foreign key columns for OneToOne relationships (only owning side)
    for (final FieldMetadata relationshipMeta : metadata.getRelationshipFields()) {
      if (relationshipMeta.isOwningSide() && relationshipMeta.getJoinColumnName() != null) {
        columns.append(relationshipMeta.getJoinColumnName()).append(",");
        values.append("?,");
      }
    }

    columns.setLength(columns.length() - 1);
    values.setLength(values.length() - 1);

    final String sql = "INSERT INTO " + metadata.getTableName() +
        " (" + columns + ") VALUES (" + values + ")";


    final PreparedStatement stmt = connection.prepareStatement(sql);

    // Set values
    int index = 1;
    for (final FieldMetadata fieldMeta : metadata.getFields()) {
      Object value = fieldMeta.getField().get(entity);

      // Handle generated value
      if (fieldMeta.isGeneratedValue() && value == null) {
        if (fieldMeta.getGenerationType() == GenerationType.UUID) {
          // Check if field type is UUID or String
          if (fieldMeta.getField().getType() == java.util.UUID.class) {
            value = java.util.UUID.randomUUID();
          } else {
            value = java.util.UUID.randomUUID().toString();
          }
          fieldMeta.getField().set(entity, value); // set it back into the entity
        }
      }

      // Handle enums
      value = convertIfEnum(fieldMeta, value);

      // Convert UUID to String for storage
      if (value instanceof java.util.UUID) {
        value = value.toString();
      }

      stmt.setObject(index++, value);

    }

    // Set foreign key values for OneToOne relationships (only owning side)
    for (final FieldMetadata relationshipMeta : metadata.getRelationshipFields()) {
      if (relationshipMeta.isOwningSide() && relationshipMeta.getJoinColumnName() != null) {
        final Object relatedEntity = relationshipMeta.getField().get(entity);
        
        if (relatedEntity != null) {
          // Get the ID of the related entity
          final EntityMetadata relatedMetadata = MetadataRegistry.getMetadata(relatedEntity.getClass());
          Object relatedId = relatedMetadata.getIdField().getField().get(relatedEntity);
          
          // Convert UUID to String for storage
          if (relatedId instanceof java.util.UUID) {
            relatedId = relatedId.toString();
          }
          
          stmt.setObject(index++, relatedId);
        } else {
          stmt.setObject(index++, null);
        }
      }
    }

    // Execute
    stmt.executeUpdate();
  }

  public <T> T findById(final Class<T> clazz, final Object id) throws Exception {

    final EntityMetadata metadata = MetadataRegistry.getMetadata(clazz);

    final String sql = "SELECT * FROM " + metadata.getTableName() +
        " WHERE " + metadata.getIdField().getColumnName() + " = ?";

    final PreparedStatement stmt = connection.prepareStatement(sql);

    // Convert UUID to String for database query
    final Object queryId = (id instanceof java.util.UUID) ? id.toString() : id;
    stmt.setObject(1, queryId);

    final ResultSet rs = stmt.executeQuery();

    if (!rs.next())
      return null;

    return mapResultToEntity(clazz, metadata, rs);
  }

  public <T> void update(final T entity) throws Exception {

    final EntityMetadata metadata = MetadataRegistry.getMetadata(entity.getClass());

    handleTimestamps(entity, metadata, true);

    final StringBuilder setClause = new StringBuilder();

    for (final FieldMetadata fieldMeta : metadata.getFields()) {
      if (!fieldMeta.isId() && fieldMeta.isUpdatable()) {
        setClause.append(fieldMeta.getColumnName()).append("=?,");
      }
    }

    setClause.setLength(setClause.length() - 1);

    final String sql = "UPDATE " + metadata.getTableName() +
        " SET " + setClause +
        " WHERE " + metadata.getIdField().getColumnName() + "=?";

    final PreparedStatement stmt = connection.prepareStatement(sql);

    int index = 1;

    for (final FieldMetadata fieldMeta : metadata.getFields()) {

      if (!fieldMeta.isId() && fieldMeta.isUpdatable()) {

        Object value = fieldMeta.getField().get(entity);
        value = convertIfEnum(fieldMeta, value);

        // Convert UUID to String for storage
        if (value instanceof java.util.UUID) {
          value = value.toString();
        }

        stmt.setObject(index++, value);
      }
    }

    Object idValue = metadata.getIdField().getField().get(entity);

    // Convert UUID to String for storage
    if (idValue instanceof java.util.UUID) {
      idValue = idValue.toString();
    }

    stmt.setObject(index, idValue);

    stmt.executeUpdate();
  }

  public <T> void delete(final T entity) throws Exception {

    final EntityMetadata metadata = MetadataRegistry.getMetadata(entity.getClass());

    final String sql = "DELETE FROM " + metadata.getTableName() +
        " WHERE " + metadata.getIdField().getColumnName() + "=?";

    final PreparedStatement stmt = connection.prepareStatement(sql);

    Object idValue = metadata.getIdField().getField().get(entity);

    // Convert UUID to String for storage
    if (idValue instanceof java.util.UUID) {
      idValue = idValue.toString();
    }

    stmt.setObject(1, idValue);

    stmt.executeUpdate();
  }

  public <T> void deleteById(final Class<T> clazz, final Object id) throws Exception {

    final EntityMetadata metadata = MetadataRegistry.getMetadata(clazz);

    final String sql = "DELETE FROM " + metadata.getTableName() +
        " WHERE " + metadata.getIdField().getColumnName() + "=?";

    final PreparedStatement stmt = connection.prepareStatement(sql);

    // Convert UUID to String for database query
    final Object queryId = (id instanceof java.util.UUID) ? id.toString() : id;
    stmt.setObject(1, queryId);

    stmt.executeUpdate();
  }

  public <T> List<T> findAll(final Class<T> clazz) throws Exception {

    final EntityMetadata metadata = MetadataRegistry.getMetadata(clazz);

    final String sql = "SELECT * FROM " + metadata.getTableName();

    final PreparedStatement stmt = connection.prepareStatement(sql);
    final ResultSet rs = stmt.executeQuery();

    final List<T> results = new ArrayList<>();

    while (rs.next()) {
      final T instance = mapResultToEntity(clazz, metadata, rs);
      results.add(instance);
    }

    return results;
  }

  public <T> QueryBuilder<T> createQuery(final Class<T> clazz) {
    return new QueryBuilder<>(clazz, connection);
  }

  public Connection getConnection() {
    return connection;
  }

  private <T> T mapResultToEntity(final Class<T> clazz, final EntityMetadata metadata, final ResultSet rs)
      throws Exception {

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
          final Object relatedEntity = findById(relatedClass, foreignKeyValue);
          
          // Set the related entity
          relationshipMeta.getField().set(instance, relatedEntity);
        } else {
        }
      }
    }

    return instance;
  }

  private Object convertIfEnum(final FieldMetadata fieldMeta, final Object value) {
    if (value != null && fieldMeta.getEnumType() != null) {

      if (fieldMeta.getEnumType() == EnumType.STRING) {
        return ((Enum<?>) value).name();
      } else {
        return ((Enum<?>) value).ordinal();
      }
    }

    return value;
  }

  @SuppressWarnings("unchecked")
  private Object convertToEnum(final FieldMetadata fieldMeta, final Object value) {
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

  private void handleTimestamps(final Object entity, final EntityMetadata metadata, final boolean isUpdate)
      throws Exception {

    for (final FieldMetadata fieldMeta : metadata.getFields()) {

      if (fieldMeta.isCreationTimestamp() && !isUpdate) {
        fieldMeta.getField().set(entity, java.time.Instant.now());
      }

      if (fieldMeta.isUpdateTimestamp()) {
        fieldMeta.getField().set(entity, java.time.Instant.now());
      }
    }
  }
}
