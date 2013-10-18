package com.revolsys.gis.oracle.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import oracle.sql.BLOB;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.jdbc.attribute.JdbcAttribute;
import com.revolsys.spring.SpringUtil;

public class OracleJdbcBlobAttribute extends JdbcAttribute {
  public OracleJdbcBlobAttribute(final String name, final int sqlType,
    final int length, final boolean required) {
    super(name, DataTypes.BLOB, sqlType, length, 0, required,
      Collections.<String, Object> emptyMap());
  }

  @Override
  public int setAttributeValueFromResultSet(final ResultSet resultSet,
    final int columnIndex, final DataObject object) throws SQLException {
    final Blob value = resultSet.getBlob(columnIndex);
    object.setValue(getIndex(), value);
    return columnIndex + 1;
  }

  @Override
  public int setPreparedStatementValue(final PreparedStatement statement,
    final int parameterIndex, final Object value) throws SQLException {
    if (value == null) {
      final int sqlType = getSqlType();
      statement.setNull(parameterIndex, sqlType);
    } else {
      if (value instanceof BLOB) {
        final BLOB blob = (BLOB)value;
        statement.setBlob(parameterIndex, blob);
      } else {
        InputStream in;
        if (value instanceof Resource) {
          final Resource resource = (Resource)value;
          in = SpringUtil.getInputStream(resource);
        } else if (value instanceof Blob) {
          final Blob blob = (Blob)value;
          in = blob.getBinaryStream();
        } else if (value instanceof byte[]) {
          final byte[] bytes = (byte[])value;
          in = new ByteArrayInputStream(bytes);
        } else if (value instanceof File) {
          final File file = (File)value;
          final FileSystemResource resource = new FileSystemResource(file);
          in = SpringUtil.getInputStream(resource);
        } else {
          throw new IllegalArgumentException("Not valid for a blob column");
        }
        statement.setBinaryStream(parameterIndex, in);
      }
    }
    return parameterIndex + 1;
  }
}
