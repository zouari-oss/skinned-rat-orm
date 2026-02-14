module org.zouarioss.skinnedratorm {
  // Java base dependencies
  requires java.sql;
  requires java.base;

  // Export public API packages
  exports org.zouarioss.skinnedratorm.annotations;
  exports org.zouarioss.skinnedratorm.core;
  exports org.zouarioss.skinnedratorm.engine;
  exports org.zouarioss.skinnedratorm.metadata;
  exports org.zouarioss.skinnedratorm.util;
  exports org.zouarioss.skinnedratorm.exception;
  exports org.zouarioss.skinnedratorm.flag;
}
