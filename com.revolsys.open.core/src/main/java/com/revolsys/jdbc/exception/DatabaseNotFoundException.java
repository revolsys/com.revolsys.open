package com.revolsys.jdbc.exception;

import java.sql.SQLException;

import org.springframework.jdbc.CannotGetJdbcConnectionException;

public class DatabaseNotFoundException extends CannotGetJdbcConnectionException {
  public DatabaseNotFoundException(final String message, final SQLException exception) {
    super(message, exception);
  }
}
