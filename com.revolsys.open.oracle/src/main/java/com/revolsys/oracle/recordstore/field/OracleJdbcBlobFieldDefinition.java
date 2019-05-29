package com.revolsys.oracle.recordstore.field;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.record.Record;
import com.revolsys.spring.resource.Resource;

public class OracleJdbcBlobFieldDefinition extends JdbcFieldDefinition {
  public OracleJdbcBlobFieldDefinition(final String dbName, final String name, final int sqlType,
    final int length, final boolean required, final String description) {
    super(dbName, name, DataTypes.BLOB, sqlType, length, 0, required, description,
      Collections.<String, Object> emptyMap());
  }

  @Override
  public int setFieldValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final Record object) throws SQLException {
    final Blob value = resultSet.getBlob(columnIndex);
    object.setValue(getIndex(), value);
    return columnIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement, final int parameterIndex,
    final Object value) throws SQLException {
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      if (value instanceof Blob) {
        final Blob blob = (Blob)value;
        statement.setBlob(parameterIndex, blob);
      } else {
        InputStream in;
        if (value instanceof Blob) {
          final Blob blob = (Blob)value;
          in = blob.getBinaryStream();
        } else if (value instanceof byte[]) {
          final byte[] bytes = (byte[])value;
          in = new ByteArrayInputStream(bytes);
        } else if (value instanceof CharSequence) {
          final String string = ((CharSequence)value).toString();
          final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
          in = new ByteArrayInputStream(bytes);
        } else {
          try {
            final Resource resource = Resource.getResource(value);
            in = resource.newBufferedInputStream();
          } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(value.getClass() + " not valid for a blob column");
          }
        }
        statement.setBinaryStream(parameterIndex, in);
      }
    }
    return parameterIndex + 1;
  }
}
