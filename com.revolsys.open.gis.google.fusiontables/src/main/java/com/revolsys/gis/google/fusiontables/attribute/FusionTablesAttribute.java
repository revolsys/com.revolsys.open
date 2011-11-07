package com.revolsys.gis.google.fusiontables.attribute;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.types.DataType;

public abstract class FusionTablesAttribute extends Attribute {
  public FusionTablesAttribute(final String name, final DataType type) {
    super(name, type, false);
  }

  public abstract void appendValue(StringBuffer sql, Object object);

  public abstract Object parseString(String string);
}
