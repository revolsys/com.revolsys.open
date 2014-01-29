package com.revolsys.gis.esri.gdb.file.test.field;

import java.io.IOException;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.EndianInput;

public class ObjectIdField extends FgdbField {
  public ObjectIdField(final String name, final boolean required) {
    super(name, DataTypes.INT, required);
  }

  @Override
  public <T> T read(final EndianInput in) throws IOException {
    return null;
  }

  @Override
  public void setValue(final DataObject record, final EndianInput in)
    throws IOException {
  }
}
