package com.revolsys.gis.esri.gdb.file.arcobjects.type;

import java.io.IOException;

import com.esri.arcgis.geodatabase.IField;
import com.esri.arcgis.geodatabase.IRowBuffer;
import com.esri.arcgis.interop.AutomationException;
import com.revolsys.gis.data.model.types.DataTypes;

public class DoubleAttribute extends AbstractFileGdbAttribute {
  public DoubleAttribute(final IField field) throws AutomationException,
    IOException {
    super(field.getName(), DataTypes.DOUBLE,
      field.isRequired() == Boolean.TRUE || !field.isNullable(), field.isEditable());
  }

  @Override
  public void setValue(final IRowBuffer row, Object value) {
    if (value != null) {
      if (value instanceof Number) {
        final Number number = (Number)value;
        value = number.doubleValue();
      } else {
        final String string = value.toString();
        value = Double.parseDouble(string);
      }
    }
    super.setValue(row, value);
  }

}
