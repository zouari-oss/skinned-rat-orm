package org.zouarioss.skinnedratorm.core;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.zouarioss.skinnedratorm.flag.EnumType;
import org.zouarioss.skinnedratorm.flag.GenerationType;
import org.zouarioss.skinnedratorm.engine.QueryBuilder;
import org.zouarioss.skinnedratorm.metadata.EntityMetadata;
import org.zouarioss.skinnedratorm.metadata.FieldMetadata;
import org.zouarioss.skinnedratorm.metadata.MetadataRegistry;
import org.zouarioss.skinnedratorm.util.ResultSetMapper;

public class EntityManager {

  private final Connection connection;

  public EntityManager(final Connection connection) {
    this.connection = connection;
  }

  public <T> void persist(final T entity) throws Exception {

    final EntityMetadata metadata = MetadataRegistry.getMetadata(entity.getClass());

    // Validate entity
    org.zouarioss.skinnedratorm.util.EntityValidator.validate(entity);

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

    try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
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

    // Invoke post-persist callbacks
    for (final Method method : metadata.getPostPersistMethods()) {
      method.invoke(entity);
    }
  }

  public <T> T findById(final Class<T> clazz, final Object id) throws Exception {

    final EntityMetadata metadata = MetadataRegistry.getMetadata(clazz);

    final String sql = "SELECT * FROM " + metadata.getTableName() +
        " WHERE " + metadata.getIdField().getColumnName() + " = ?";

    try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
      // Convert UUID to String for database query
      final Object queryId = (id instanceof java.util.UUID) ? id.toString() : id;
      stmt.setObject(1, queryId);

      try (final ResultSet rs = stmt.executeQuery()) {
        if (!rs.next())
          return null;

        return ResultSetMapper.mapResultToEntity(clazz, metadata, rs, connection);
      }
    }
  }

  public <T> void update(final T entity) throws Exception {

    final EntityMetadata metadata = MetadataRegistry.getMetadata(entity.getClass());

    // Validate entity
    org.zouarioss.skinnedratorm.util.EntityValidator.validate(entity);

    // Invoke pre-update callbacks
    for (final Method method : metadata.getPreUpdateMethods()) {
      method.invoke(entity);
    }

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

    try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
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

    // Invoke post-update callbacks
    for (final Method method : metadata.getPostUpdateMethods()) {
      method.invoke(entity);
    }
  }

  public <T> void delete(final T entity) throws Exception {

    final EntityMetadata metadata = MetadataRegistry.getMetadata(entity.getClass());

    final String sql = "DELETE FROM " + metadata.getTableName() +
        " WHERE " + metadata.getIdField().getColumnName() + "=?";

    try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
      Object idValue = metadata.getIdField().getField().get(entity);

      // Convert UUID to String for storage
      if (idValue instanceof java.util.UUID) {
        idValue = idValue.toString();
      }

      stmt.setObject(1, idValue);

      stmt.executeUpdate();
    }
  }

  public <T> void deleteById(final Class<T> clazz, final Object id) throws Exception {

    final EntityMetadata metadata = MetadataRegistry.getMetadata(clazz);

    final String sql = "DELETE FROM " + metadata.getTableName() +
        " WHERE " + metadata.getIdField().getColumnName() + "=?";

    try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
      // Convert UUID to String for database query
      final Object queryId = (id instanceof java.util.UUID) ? id.toString() : id;
      stmt.setObject(1, queryId);

      stmt.executeUpdate();
    }
  }

  public <T> List<T> findAll(final Class<T> clazz) throws Exception {

    final EntityMetadata metadata = MetadataRegistry.getMetadata(clazz);

    final String sql = "SELECT * FROM " + metadata.getTableName();

    try (final PreparedStatement stmt = connection.prepareStatement(sql);
        final ResultSet rs = stmt.executeQuery()) {

      final List<T> results = new ArrayList<>();

      while (rs.next()) {
        final T instance = ResultSetMapper.mapResultToEntity(clazz, metadata, rs, connection);
        results.add(instance);
      }

      return results;
    }
  }

  public <T> org.zouarioss.skinnedratorm.util.Page<T> findAll(
      final Class<T> clazz,
      final org.zouarioss.skinnedratorm.util.PageRequest pageRequest) throws Exception {

    final EntityMetadata metadata = MetadataRegistry.getMetadata(clazz);

    // Count total
    final long total;
    try (
        final PreparedStatement countStmt = connection
            .prepareStatement("SELECT COUNT(*) FROM " + metadata.getTableName());
        final ResultSet countRs = countStmt.executeQuery()) {
      countRs.next();
      total = countRs.getLong(1);
    }

    // Get page
    final String sql = "SELECT * FROM " + metadata.getTableName() +
        " LIMIT ? OFFSET ?";

    try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
      stmt.setInt(1, pageRequest.getSize());
      stmt.setInt(2, pageRequest.getOffset());

      try (final ResultSet rs = stmt.executeQuery()) {
        final List<T> results = new ArrayList<>();

        while (rs.next()) {
          final T instance = ResultSetMapper.mapResultToEntity(clazz, metadata, rs, connection);
          results.add(instance);
        }

        return new org.zouarioss.skinnedratorm.util.Page<>(
            results, pageRequest.getPage(), pageRequest.getSize(), total);
      }
    }
  }

  public <T> QueryBuilder<T> createQuery(final Class<T> clazz) {
    return new QueryBuilder<>(clazz, connection);
  }

  public <T> void persistBatch(final List<T> entities) throws Exception {
    if (entities == null || entities.isEmpty()) {
      return;
    }

    final Class<?> entityClass = entities.get(0).getClass();
    final EntityMetadata metadata = MetadataRegistry.getMetadata(entityClass);

    // Invoke lifecycle callbacks and handle timestamps for all entities
    for (final T entity : entities) {
      for (final Method method : metadata.getPrePersistMethods()) {
        method.invoke(entity);
      }
      handleTimestamps(entity, metadata, false);
    }

    // Build SQL
    final StringBuilder columns = new StringBuilder();
    final StringBuilder values = new StringBuilder();

    for (final FieldMetadata fieldMeta : metadata.getFields()) {
      columns.append(fieldMeta.getColumnName()).append(",");
      values.append("?,");
    }

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

    try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
      for (final T entity : entities) {
        int index = 1;

        for (final FieldMetadata fieldMeta : metadata.getFields()) {
          Object value = fieldMeta.getField().get(entity);

          if (fieldMeta.isGeneratedValue() && value == null) {
            if (fieldMeta.getGenerationType() == GenerationType.UUID) {
              if (fieldMeta.getField().getType() == java.util.UUID.class) {
                value = java.util.UUID.randomUUID();
              } else {
                value = java.util.UUID.randomUUID().toString();
              }
              fieldMeta.getField().set(entity, value);
            }
          }

          value = convertIfEnum(fieldMeta, value);
          if (value instanceof java.util.UUID) {
            value = value.toString();
          }

          stmt.setObject(index++, value);
        }

        for (final FieldMetadata relationshipMeta : metadata.getRelationshipFields()) {
          if (relationshipMeta.isOwningSide() && relationshipMeta.getJoinColumnName() != null) {
            final Object relatedEntity = relationshipMeta.getField().get(entity);

            if (relatedEntity != null) {
              final EntityMetadata relatedMetadata = MetadataRegistry.getMetadata(relatedEntity.getClass());
              Object relatedId = relatedMetadata.getIdField().getField().get(relatedEntity);

              if (relatedId instanceof java.util.UUID) {
                relatedId = relatedId.toString();
              }

              stmt.setObject(index++, relatedId);
            } else {
              stmt.setObject(index++, null);
            }
          }
        }

        stmt.addBatch();
      }

      stmt.executeBatch();
    }
  }

  public <T> void updateBatch(final List<T> entities) throws Exception {
    if (entities == null || entities.isEmpty()) {
      return;
    }

    final Class<?> entityClass = entities.get(0).getClass();
    final EntityMetadata metadata = MetadataRegistry.getMetadata(entityClass);

    // Handle timestamps for all entities
    for (final T entity : entities) {
      handleTimestamps(entity, metadata, true);
    }

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

    try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
      for (final T entity : entities) {
        int index = 1;

        for (final FieldMetadata fieldMeta : metadata.getFields()) {
          if (!fieldMeta.isId() && fieldMeta.isUpdatable()) {
            Object value = fieldMeta.getField().get(entity);
            value = convertIfEnum(fieldMeta, value);

            if (value instanceof java.util.UUID) {
              value = value.toString();
            }

            stmt.setObject(index++, value);
          }
        }

        Object idValue = metadata.getIdField().getField().get(entity);
        if (idValue instanceof java.util.UUID) {
          idValue = idValue.toString();
        }

        stmt.setObject(index, idValue);
        stmt.addBatch();
      }

      stmt.executeBatch();
    }
  }

  public <T> void deleteBatch(final List<T> entities) throws Exception {
    if (entities == null || entities.isEmpty()) {
      return;
    }

    final Class<?> entityClass = entities.get(0).getClass();
    final EntityMetadata metadata = MetadataRegistry.getMetadata(entityClass);

    final String sql = "DELETE FROM " + metadata.getTableName() +
        " WHERE " + metadata.getIdField().getColumnName() + "=?";

    try (final PreparedStatement stmt = connection.prepareStatement(sql)) {
      for (final T entity : entities) {
        Object idValue = metadata.getIdField().getField().get(entity);

        if (idValue instanceof java.util.UUID) {
          idValue = idValue.toString();
        }

        stmt.setObject(1, idValue);
        stmt.addBatch();
      }

      stmt.executeBatch();
    }
  }

  public Connection getConnection() {
    return connection;
  }

  public void beginTransaction() throws Exception {
    connection.setAutoCommit(false);
  }

  public void commit() throws Exception {
    connection.commit();
    connection.setAutoCommit(true);
  }

  public void rollback() throws Exception {
    connection.rollback();
    connection.setAutoCommit(true);
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
