package com.revolsys.gis.esri.gdb.file.arcobjects.type;

import java.io.IOException;

import com.esri.arcgis.geodatabase.IField;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geodatabase.IRowBuffer;
import com.esri.arcgis.interop.AutomationException;
import com.revolsys.gis.data.model.types.DataTypes;

public class OidAttribute extends AbstractFileGdbAttribute {
  public OidAttribute(final IField field) throws AutomationException,
    IOException {
    super(field.getName(), DataTypes.INT, field.isRequired() == Boolean.TRUE
      || !field.isNullable(), field.isEditable());
  }

  @Override
  public void setValue(IRowBuffer row, Object value) {
  }

  @Override
  public Object getValue(final IRow row) {
    try {
      return row.getOID();
    } catch (final Exception e) {
      throw new RuntimeException("Unable to get OID", e);
    }
  }
}
