package com.revolsys.jdbc.exception;

import java.sql.SQLException;

import org.springframework.jdbc.CannotGetJdbcConnectionException;

public class UsernameOrPasswordInvalidException extends CannotGetJdbcConnectionException {
  public UsernameOrPasswordInvalidException(final String message, final SQLException exception) {
    super(message, exception);
  }
}
