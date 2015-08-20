package com.revolsys.gis.oracle.io;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import com.revolsys.spring.resource.FileSystemResource;
import com.revolsys.spring.resource.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;
import com.revolsys.jdbc.field.JdbcFieldDefinition;
import com.revolsys.spring.resource.SpringUtil;

public class OracleJdbcClobFieldDefinition extends JdbcFieldDefinition {
  public OracleJdbcClobFieldDefinition(final String dbName, final String name, final int sqlType,
    final int length, final boolean required, final String description) {
    super(dbName, name, DataTypes.STRING, sqlType, length, 0, required, description,
      Collections.<String, Object> emptyMap());
  }

  @Override
  public int setFieldValueFromResultSet(final ResultSet resultSet, final int columnIndex,
    final Record object) throws SQLException {
    final Clob value = resultSet.getClob(columnIndex);
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
      if (value instanceof Clob) {
        final Clob clob = (Clob)value;
        statement.setClob(parameterIndex, clob);
      } else {
        Reader in;
        if (value instanceof Resource) {
          final Resource resource = (Resource)value;
          in = resource.newReader();
        } else if (value instanceof Clob) {
          final Clob clob = (Clob)value;
          in = clob.getCharacterStream();
        } else if (value instanceof String) {
          final String string = (String)value;
          in = new StringReader(string);
        } else if (value instanceof File) {
          final File file = (File)value;
          final FileSystemResource resource = new FileSystemResource(file);
          in = resource.newReader();
        } else {
          throw new IllegalArgumentException("Not valid for a clob column");
        }
        statement.setCharacterStream(parameterIndex, in);
      }
    }
    return parameterIndex + 1;
  }
}
