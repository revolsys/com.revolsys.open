package com.revolsys.gis.esri.gdb.file.arcobjects.type;

import java.io.IOException;
import java.util.Date;

import com.esri.arcgis.geodatabase.IField;
import com.esri.arcgis.geodatabase.IRowBuffer;
import com.esri.arcgis.interop.AutomationException;
import com.revolsys.gis.data.model.types.DataTypes;

public class DateAttribute extends AbstractFileGdbAttribute {
  @SuppressWarnings("deprecation")
  public static final Date MIN_DATE = new Date(70, 0, 1);

  @SuppressWarnings("deprecation")
  public static final Date MAX_DATE = new Date(138, 1, 19);

  public DateAttribute(final IField field) throws AutomationException,
    IOException {
    super(field.getName(), DataTypes.DATE, field.isRequired() == Boolean.TRUE
      || !field.isNullable(), field.isEditable());
  }

  @Override
  public void setValue(final IRowBuffer row, final Object value) {
    final String name = getName();
    if (value instanceof Date) {
      final Date date = (Date)value;
      if (date.before(MIN_DATE)) {
        throw new IllegalArgumentException(name + "=" + date + " is before "
          + MIN_DATE + " which is not supported by ESRI File Geodatabases");
      } else if (date.after(MAX_DATE)) {
        throw new IllegalArgumentException(name + "=" + date + " is after "
          + MAX_DATE + " which is not supported by ESRI File Geodatabases");
      } else {
        long time = date.getTime() / 1000;
        super.setValue(row, time);
      }
    } else if (value == null) {
      super.setValue(row, value);
    } else {
      throw new IllegalArgumentException("Expecting a java,util.Date not "
        + value.getClass() + " " + value);
    }
  }

}
