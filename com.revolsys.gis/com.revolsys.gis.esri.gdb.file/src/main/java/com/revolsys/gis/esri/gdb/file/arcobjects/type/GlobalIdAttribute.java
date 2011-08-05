package com.revolsys.gis.esri.gdb.file.arcobjects.type;

import java.io.IOException;

import com.esri.arcgis.geodatabase.IField;
import com.esri.arcgis.geodatabase.IRowBuffer;
import com.esri.arcgis.interop.AutomationException;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataTypes;

public class GlobalIdAttribute extends AbstractFileGdbAttribute {
  public GlobalIdAttribute(final IField field) throws AutomationException,
    IOException {
    super(field.getName(), DataTypes.STRING, field.getLength(),
      field.isRequired() == Boolean.TRUE || !field.isNullable(),
      field.isEditable());
  }

  @Override
  public void setPostInsertValue(final DataObject object, final IRowBuffer row) {
    final String name = getName();
    try {
      final int index = getIndex();
      final Object value = row.getValue(index);
      object.setValue(name, value);
    } catch (final Exception e) {
      throw new RuntimeException("Unable to set " + name, e);
    }
  }

  @Override
  public void setValue(final IRowBuffer row, final Object value) {
  }
}
