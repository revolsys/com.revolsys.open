package com.revolsys.gis.esri.gdb.file.arcobjects.type;

import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geodatabase.IRowBuffer;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataType;

public abstract class AbstractFileGdbAttribute extends Attribute {

  private boolean editable;

  public AbstractFileGdbAttribute(final String name, final DataType dataType,
    final boolean required, boolean editable) {
    super(name, dataType, required);
    this.editable = editable;
  }

  public AbstractFileGdbAttribute(final String name, final DataType dataType,
    final int length, final boolean required, boolean editable) {
    super(name, dataType, length, required);
    this.editable = editable;
  }

  public Object getValue(final IRow row) {
    try {
      return row.getValue(getIndex());
    } catch (final Exception e) {
      throw new RuntimeException("Unable to get value for " + getName(), e);
    }
  }

  public void setInsertValue(final IRowBuffer row, final Object value) {
    if (editable) {
      setValue(row, value);
    }
  }

  public void setPostInsertValue(final DataObject object, final IRowBuffer row) {
  }

  public void setUpdateValue(final IRow row, final Object value) {
    if (editable) {
      setValue(row, value);
    }
  }

  public void setValue(final IRowBuffer row, final Object value) {
    if (value == null) {
      if (!isEditable() && isRequired()) {
        throw new IllegalArgumentException(getName()
          + " is required and cannot be null");
      }
    }
    try {
      row.setValue(getIndex(), value);
    } catch (final Exception e) {
      throw new RuntimeException("Unable to set value " + getName() + "="
        + value, e);
    }
  }

  public boolean isEditable() {
    return editable;
  }
}
