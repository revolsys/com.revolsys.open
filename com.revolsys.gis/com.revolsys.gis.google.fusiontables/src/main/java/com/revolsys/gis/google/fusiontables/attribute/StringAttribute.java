package com.revolsys.gis.google.fusiontables.attribute;

import com.revolsys.gis.data.model.types.DataTypes;

public class StringAttribute extends FusionTablesAttribute {
  public StringAttribute(String name) {
    super(name, DataTypes.STRING);
  }

  public void appendValue(StringBuffer sql, Object object) {
    if (object == null) {
      sql.append("''");
    } else {
      String string = object.toString();
      sql.append('\'');
      for (int i = 0; i < string.length(); i++) {
        char c = string.charAt(i);
        if (c == '\'') {
          sql.append("\\'");
        } else {
          sql.append(c);
        }
      }
      sql.append('\'');
    }
  }

  public Object parseString(String string) {
    return string;
  }

  @Override
  public StringAttribute clone() {
    return new StringAttribute(getName());
  }
}
