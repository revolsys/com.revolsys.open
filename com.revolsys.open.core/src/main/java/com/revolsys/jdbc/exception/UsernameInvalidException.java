package com.revolsys.jdbc.exception;

import java.sql.SQLException;

import org.springframework.jdbc.CannotGetJdbcConnectionException;

public class UsernameInvalidException extends CannotGetJdbcConnectionException {
  public UsernameInvalidException(final String message, final SQLException exception) {
    super(message, exception);
  }
}
