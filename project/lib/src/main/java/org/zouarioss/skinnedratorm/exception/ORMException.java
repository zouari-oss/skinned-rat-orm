package org.zouarioss.skinnedratorm.exception;

public class ORMException extends RuntimeException {
  public ORMException(String message) { super(message); }
  public ORMException(String message, Throwable cause) { super(message, cause); }
  public ORMException(Throwable cause) { super(cause); }
}
