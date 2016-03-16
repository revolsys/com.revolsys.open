package com.revolsys.gis.postgresql;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.dbcp2.DelegatingConnection;
import org.postgresql.PGConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import com.revolsys.jdbc.field.JdbcBlobFieldDefinition;
import com.revolsys.util.Exceptions;

public class PostgreSQLJdbcBlobFieldDefinition extends JdbcBlobFieldDefinition {
  public PostgreSQLJdbcBlobFieldDefinition(final String dbName, final String name,
    final String dataType, final int sqlType, final int length, final int scale,
    final boolean required, final String description, final Map<String, Object> properties) {
    super(dbName, name, sqlType, length, required, description, properties);
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      if (value instanceof InputStream) {
        try {
          final InputStream in = (InputStream)value;

          final PGConnection pgConnection = (PGConnection)((DelegatingConnection<?>)statement
            .getConnection()).getInnermostDelegate();
          final LargeObjectManager lobManager = pgConnection.getLargeObjectAPI();

          final long lobId = lobManager
            .createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);

          final LargeObject lob = lobManager.open(lobId, LargeObjectManager.WRITE);
          try {
            final byte buffer[] = new byte[2048];
            int readCount = 0;
            while ((readCount = in.read(buffer, 0, 2048)) > 0) {
              lob.write(buffer, 0, readCount);
            }
          } finally {
            lob.close();
          }
          statement.setLong(parameterIndex, lobId);
        } catch (final IOException e) {
          Exceptions.throwUncheckedException(e);
        }
      } else {
        super.setPreparedStatementValue(statement, parameterIndex, value);
      }
    }
    return parameterIndex + 1;
  }
}
