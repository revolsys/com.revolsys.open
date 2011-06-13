package com.revolsys.gis.google.fusiontables.attribute;

import java.math.BigDecimal;

import com.revolsys.gis.data.model.types.DataTypes;

public class NumberAttribute extends FusionTablesAttribute {
  public static void appendString(final StringBuffer buffer, final Object value) {
    if (value == null) {
      buffer.append("''");
    } else if (value instanceof Number) {
      buffer.append(value);
    } else {
      final String string = value.toString();
      final Number number = new BigDecimal(string);
      buffer.append(number);
    }
  }

  public NumberAttribute(final String name) {
    super(name, DataTypes.DECIMAL);
  }

  @Override
  public void appendValue(final StringBuffer sql, final Object object) {
    appendString(sql, object);
  }

  @Override
  public NumberAttribute clone() {
    return new NumberAttribute(getName());
  }

  @Override
  public Object parseString(final String string) {
    if (string.trim().length() == 0) {
      return null;
    } else {
      return new BigDecimal(string);
    }
  }
}
