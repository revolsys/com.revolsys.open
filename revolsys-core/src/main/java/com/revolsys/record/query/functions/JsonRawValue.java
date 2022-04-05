package com.revolsys.record.query.functions;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.ColumnIndexes;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.Value;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class JsonRawValue extends SimpleFunction {

  public static final String NAME = "JSON_VALUE";

  private String path;

  private String displayPath;

  public JsonRawValue(final List<QueryValue> parameters) {
    super(NAME, 2, parameters);
    final QueryValue pathParameter = parameters.get(1);
    if (Value.isString(pathParameter)) {
      this.displayPath = (String)((Value)pathParameter).getValue();
      if (this.displayPath.matches("\\w+(\\.\\w+)*")) {
        this.path = "$." + this.displayPath;
      } else if (this.displayPath.matches("\\$(\\.\\w+)*")) {
        this.path = this.displayPath;
      } else {
        throw new IllegalArgumentException(
          "JSON_VALUE path parameter must match $(.propertyName)* (e.g. $.address.city): "
            + pathParameter);
      }
    } else {
      throw new IllegalArgumentException(
        "JSON_VALUE path parameter is not a string: " + pathParameter);
    }
  }

  @Override
  public void appendDefaultSql(final Query query, final RecordStore recordStore,
    final Appendable buffer) {
    try {
      final QueryValue jsonParameter = getParameter(0);

      buffer.append(getName());
      buffer.append("(");
      jsonParameter.appendSql(query, recordStore, buffer);
      buffer.append(", '");
      buffer.append(this.path);
      buffer.append("')");
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    final QueryValue jsonParameter = getParameter(0);
    index = jsonParameter.appendParameters(index, statement);
    return index;
  }

  public String getPath() {
    return this.path;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final MapEx record) {

    final JsonObject value = getParameterValue(0, record, Json.JSON_OBJECT);
    final String path = getParameterStringValue(1, record);
    if (value != null) {
      final String[] names = path.split("\\.");
      final Object result = value.getByPath(names);
      if (result != null) {
        return (V)result.toString();
      }
    }
    return null;

  }

  @Override
  public Object getValueFromResultSet(final RecordDefinition recordDefinition,
    final ResultSet resultSet, final ColumnIndexes indexes, final boolean internStrings)
    throws SQLException {
    return resultSet.getObject(indexes.incrementAndGet());
  }

  @Override
  public String toString() {
    final List<QueryValue> parameters = getParameters();
    return NAME + "(" + parameters.get(0) + ", '" + this.displayPath + "')";
  }
}
