package com.revolsys.gis.google.fusiontables.attribute;

import java.math.BigDecimal;

import com.revolsys.gis.data.model.types.DataTypes;

public class NumberAttribute extends FusionTablesAttribute {
  public NumberAttribute(String name) {
    super(name, DataTypes.DECIMAL);
  }

  public void appendValue(StringBuffer sql, Object object) {
    if (object == null) {
      sql.append("''");
    } else if (object instanceof Number) {
      sql.append(object);
    } else {
      String string = object.toString();
      Number number = new BigDecimal(string);
      sql.append(number);
    }
  }

  public Object parseString(String string) {
    if (string.trim().length() == 0) {
      return null;
    } else {
      return new BigDecimal(string);
    }
  }

  @Override
  public NumberAttribute clone() {
    return new NumberAttribute(getName());
  }
}
