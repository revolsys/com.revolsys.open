package com.revolsys.jdbc.field;

import java.io.File;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;
import com.revolsys.jdbc.LocalBlob;

public class JdbcBlobFieldDefinition extends JdbcFieldDefinition {
  public JdbcBlobFieldDefinition(final String dbName, final String name, final int sqlType,
    final int length, final boolean required, final String description,
    final Map<String, Object> properties) {
    super(dbName, name, DataTypes.BLOB, sqlType, length, 0, required, description, properties);
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
      Blob blob;
      if (value instanceof Resource) {
        final Resource resource = (Resource)value;
        blob = new LocalBlob(resource);
      } else if (value instanceof Blob) {
        blob = (Blob)value;
      } else if (value instanceof byte[]) {
        final byte[] bytes = (byte[])value;
        blob = new LocalBlob(bytes);
      } else if (value instanceof File) {
        final File file = (File)value;
        final FileSystemResource resource = new FileSystemResource(file);
        blob = new LocalBlob(resource);
      } else {
        throw new IllegalArgumentException("Not valid for a blob column");
      }
      statement.setBlob(parameterIndex, blob);

    }
    return parameterIndex + 1;
  }
}
