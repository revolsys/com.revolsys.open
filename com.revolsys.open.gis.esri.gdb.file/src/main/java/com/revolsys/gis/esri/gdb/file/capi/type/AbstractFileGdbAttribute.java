package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;

public abstract class AbstractFileGdbAttribute extends Attribute {

  public AbstractFileGdbAttribute(final String name, final DataType dataType,
    final boolean required) {
    super(name, dataType, required);
  }

  public AbstractFileGdbAttribute(final String name, final DataType dataType,
    final int length, final boolean required) {
    super(name, dataType, length, required);
  }

  public abstract Object getValue(Row row);

  public abstract Object setValue(Row row, Object value);

  public Object setInsertValue(Row row, Object value) {
    return setValue(row, value);
  }

  public Object setUpdateValue(Row row, Object value) {
    return setValue(row, value);
  }

  public void setPostInsertValue(DataObject object, Row row) {
  }

}
