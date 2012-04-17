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

  public Object setInsertValue(
    final DataObject object,
    final Row row,
    final Object value) {
    return setValue(object, row, value);
  }

  public void setPostInsertValue(final DataObject object, final Row row) {
  }

  public Object setUpdateValue(
    final DataObject object,
    final Row row,
    final Object value) {
    return setValue(object, row, value);
  }

  public abstract Object setValue(DataObject object, Row row, Object value);

}
