package org.zouarioss.skinnedratorm.exception;

public class TransactionException extends ORMException {
  public TransactionException(String message) { super(message); }
  public TransactionException(String message, Throwable cause) { super(message, cause); }
}
