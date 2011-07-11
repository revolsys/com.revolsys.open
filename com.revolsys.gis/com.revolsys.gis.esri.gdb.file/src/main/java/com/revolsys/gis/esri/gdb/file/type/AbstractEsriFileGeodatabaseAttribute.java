package com.revolsys.gis.esri.gdb.file.type;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.esri.gdb.file.swig.Row;

public abstract class AbstractEsriFileGeodatabaseAttribute extends Attribute {

  public AbstractEsriFileGeodatabaseAttribute(String name, DataType dataType,
    int length, boolean required) {
    super(name, dataType, length, required);
  }

  public abstract void setValue(Row row, Object object);

  public abstract Object getValue(Row row);

}
