package com.revolsys.gis.esri.gdb.file.type;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.esri.gdb.file.swig.Row;

public abstract class AbstractFileGdbAttribute extends Attribute {

  public AbstractFileGdbAttribute(final String name,
    final DataType dataType, final boolean required) {
    super(name, dataType, required);
  }

  public AbstractFileGdbAttribute(final String name,
    final DataType dataType, final int length, final boolean required) {
    super(name, dataType, length, required);
  }

  public abstract Object getValue(Row row);

  public abstract void setValue(Row row, Object value);

  public void setInsertValue(Row row, Object value) {
    setValue(row, value);
  }

  public void setUpdateValue(Row row, Object value) {
    setValue(row, value);
  }

  public void setPostInsertValue(DataObject object, Row row) {
  }

}
