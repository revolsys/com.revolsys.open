package com.revolsys.gis.oracle.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.spring.resource.FileSystemResource;
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
        if (value instanceof Resource) {
          final Resource resource = (Resource)value;
          in = resource.getInputStream();
        } else if (value instanceof Blob) {
          final Blob blob = (Blob)value;
          in = blob.getBinaryStream();
        } else if (value instanceof byte[]) {
          final byte[] bytes = (byte[])value;
          in = new ByteArrayInputStream(bytes);
        } else if (value instanceof File) {
          final File file = (File)value;
          final FileSystemResource resource = new FileSystemResource(file);
          in = resource.getInputStream();
        } else {
          throw new IllegalArgumentException("Not valid for a blob column");
        }
        statement.setBinaryStream(parameterIndex, in);
      }
    }
    return parameterIndex + 1;
  }
}
