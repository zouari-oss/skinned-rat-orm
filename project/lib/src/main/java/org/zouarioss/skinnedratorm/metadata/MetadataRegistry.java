package org.zouarioss.skinnedratorm.metadata;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetadataRegistry {

  private static final Map<Class<?>, EntityMetadata> CACHE = new ConcurrentHashMap<>();

  public static EntityMetadata getMetadata(final Class<?> clazz) {
    return CACHE.computeIfAbsent(clazz, EntityMetadata::new);
  }
}
