package org.zouarioss.skinnedratorm.exception;

public class ValidationException extends ORMException {
  public ValidationException(String message) { super(message); }
  public ValidationException(String message, Throwable cause) { super(message, cause); }
}
