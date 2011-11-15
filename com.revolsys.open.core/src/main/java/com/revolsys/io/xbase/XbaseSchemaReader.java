package com.revolsys.io.xbase;

import java.io.IOException;
import java.util.List;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.io.EndianInput;

public class XbaseSchemaReader {
  private List<FieldDefinition> fieldDefinitions;

  private final EndianInput in;

  private DataObjectMetaDataImpl metaData;

  private final String typeName;

  public XbaseSchemaReader(
    final EndianInput in,
    final String typeName) {
    this.in = in;
    this.typeName = typeName;
  }

  public XbaseSchemaReader(
    final EndianInput in,
    final String typeName,
    final List<FieldDefinition> fieldDefinitions) {
    this.in = in;
    this.typeName = typeName;
    this.fieldDefinitions = fieldDefinitions;
  }

  protected DataObjectMetaData getMetaData()
    throws IOException {
    if (metaData == null) {
      metaData = new DataObjectMetaDataImpl(new QName(typeName));
      int b = in.read();
      while (b != 0x0D) {
        final StringBuffer fieldName = new StringBuffer();
        boolean endOfName = false;
        for (int i = 0; i < 11; i++) {
          if (!endOfName && b != 0) {
            fieldName.append((char)b);
          } else {

            endOfName = true;
          }
          if (i != 10) {
            b = in.read();
          }
        }
        final char fieldType = (char)in.read();
        in.skipBytes(4);
        final int length = in.read();
        in.skipBytes(15);
        b = in.read();
        final FieldDefinition field = new FieldDefinition(fieldName.toString(),
          fieldType, length);
        if (fieldDefinitions != null) {
          fieldDefinitions.add(field);
        }
        metaData.addAttribute(fieldName.toString(), field.getDataType(),
          length, true);
      }
    }
    return metaData;
  }

}
