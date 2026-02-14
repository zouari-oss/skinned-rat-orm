package org.zouarioss.skinnedratorm.exception;

public class PersistenceException extends ORMException {
  public PersistenceException(String message) { super(message); }
  public PersistenceException(String message, Throwable cause) { super(message, cause); }
}
