package com.revolsys.gis.google.fusiontables.attribute;

import com.revolsys.gis.data.model.types.DataTypes;

public class StringAttribute extends FusionTablesAttribute {
  public static void appendString(final StringBuffer buffer, final Object value) {
    if (value == null) {
      buffer.append("''");
    } else {
      final String string = value.toString();
      buffer.append('\'');
      for (int i = 0; i < string.length(); i++) {
        final char c = string.charAt(i);
        if (c == '\'') {
          buffer.append("\\'");
        } else {
          buffer.append(c);
        }
      }
      buffer.append('\'');
    }
  }

  public StringAttribute(final String name) {
    super(name, DataTypes.STRING);
  }

  @Override
  public void appendValue(final StringBuffer sql, final Object object) {
    appendString(sql, object);
  }

  @Override
  public StringAttribute clone() {
    return new StringAttribute(getName());
  }

  @Override
  public Object parseString(final String string) {
    return string;
  }
}
