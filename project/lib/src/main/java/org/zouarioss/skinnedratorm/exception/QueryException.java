package org.zouarioss.skinnedratorm.exception;

public class QueryException extends ORMException {
  public QueryException(String message) { super(message); }
  public QueryException(String message, Throwable cause) { super(message, cause); }
}
