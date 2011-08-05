package com.revolsys.gis.esri.gdb.file.arcobjects.type;

import java.io.IOException;

import com.esri.arcgis.geodatabase.IField;
import com.esri.arcgis.interop.AutomationException;
import com.revolsys.gis.data.model.types.DataTypes;

public class XmlAttribute extends AbstractFileGdbAttribute {

  public XmlAttribute(final IField field) throws AutomationException,
    IOException {
    super(field.getName(), DataTypes.STRING, field.getLength(),
      field.isRequired() == Boolean.TRUE || !field.isNullable(), field.isEditable());
  }
}
