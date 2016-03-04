package com.revolsys.jdbc.exception;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import com.revolsys.util.Property;
import com.revolsys.util.function.Function2;

public class JdbcExceptionTranslator extends SQLErrorCodeSQLExceptionTranslator {
  private static final Map<String, Function2<String, SQLException, DataAccessException>> ERROR_CODE_TO_FUNCTION = new HashMap<>();

  static {
    ERROR_CODE_TO_FUNCTION.put("28000", UsernameInvalidException::new);
  }

  public JdbcExceptionTranslator(final DataSource dataSource) {
    super(dataSource);
  }

  @Override
  protected DataAccessException customTranslate(final String task, final String sql,
    final SQLException exception) {
    String sqlState = exception.getSQLState();
    if (sqlState == null) {
      final Throwable cause = exception.getCause();
      if (cause instanceof SQLException) {
        sqlState = ((SQLException)cause).getSQLState();
      }
    }
    final Function2<String, SQLException, DataAccessException> function = ERROR_CODE_TO_FUNCTION
      .get(sqlState);
    if (function == null) {
      return null;
    } else {
      final StringBuilder message = new StringBuilder();
      if (Property.hasValue(task)) {
        message.append(task);
      }
      if (Property.hasValue(sql)) {
        if (message.length() > 0) {
          message.append("\n  ");
        }
        message.append(sql);
      }
      if (message.length() == 0) {

      }
      return function.apply(message.toString(), exception);
    }
  }
}
