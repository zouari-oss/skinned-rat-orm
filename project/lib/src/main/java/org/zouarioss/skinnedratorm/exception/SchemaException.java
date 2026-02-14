package org.zouarioss.skinnedratorm.exception;

public class SchemaException extends ORMException {
  public SchemaException(String message) { super(message); }
  public SchemaException(String message, Throwable cause) { super(message, cause); }
}
