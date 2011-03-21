/*
 * $URL:https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/main/java/com/revolsys/gis/format/xbase/io/XbaseFileWriter.java $
 * $Author:paul.austin@revolsys.com $
 * $Date:2007-06-09 09:28:28 -0700 (Sat, 09 Jun 2007) $
 * $Revision:265 $

 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.gis.format.xbase.io;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.io.ResourceEndianOutput;
import com.revolsys.io.AbstractWriter;
import com.revolsys.spring.NonExistingResource;

public class XbaseDataObjectWriter extends AbstractWriter<DataObject> {
  private static final Logger log = Logger.getLogger(XbaseDataObjectWriter.class);

  private final List<FieldDefinition> fields = new ArrayList<FieldDefinition>();

  private final List<String> fieldNames = new ArrayList<String>();

  private Resource resource;

  private DataObjectMetaData metaData;

  private int numRecords = 0;

  private ResourceEndianOutput out;

  private boolean useZeroForNull = true;

  private boolean initialized;

  public XbaseDataObjectWriter(final DataObjectMetaData metaData,
    final Resource resource) {
    this.metaData = metaData;
    this.resource = resource;
  }

  public void flush() {
    out.flush();
  }

  protected int addDbaseField(final String name, final Class<?> typeJavaClass,
    final int length, final int scale) {

    FieldDefinition field = null;
    if (typeJavaClass == String.class) {
      if (length > 254) {
        field = new FieldDefinition(name, FieldDefinition.MEMO_TYPE, 10);
      } else {
        field = new FieldDefinition(name, FieldDefinition.CHARACTER_TYPE,
          length);
      }

    } else if (typeJavaClass == Boolean.class) {
      field = new FieldDefinition(name, FieldDefinition.LOGICAL_TYPE, 1);
    } else if (typeJavaClass == java.sql.Date.class) {
      field = new FieldDefinition(name, FieldDefinition.DATE_TYPE, 8);
    } else if (typeJavaClass == BigDecimal.class) {
      field = new FieldDefinition(name, FieldDefinition.NUMBER_TYPE, length,
        scale);
    } else if (typeJavaClass == BigInteger.class) {
      field = new FieldDefinition(name, FieldDefinition.NUMBER_TYPE, length);
    } else if (typeJavaClass == Float.class) {
      field = new FieldDefinition(name, FieldDefinition.FLOAT_TYPE, 20);
    } else if (typeJavaClass == Double.class) {
      field = new FieldDefinition(name, FieldDefinition.FLOAT_TYPE, 20);
    } else if (typeJavaClass == Byte.class) {
      field = new FieldDefinition(name, FieldDefinition.NUMBER_TYPE, 3);
    } else if (typeJavaClass == Short.class) {
      field = new FieldDefinition(name, FieldDefinition.NUMBER_TYPE, 5);
    } else if (typeJavaClass == Integer.class) {
      field = new FieldDefinition(name, FieldDefinition.NUMBER_TYPE, 10);
    } else if (typeJavaClass == Long.class) {
      field = new FieldDefinition(name, FieldDefinition.NUMBER_TYPE, 19);
    } else {
      log.warn("Writing " + typeJavaClass + " is not supported");
      field = new FieldDefinition(name, FieldDefinition.OBJECT_TYPE, 0);
    }
    if (field != null) {
      addField(field);
      return field.getLength();
    } else {
      return 0;
    }
  }

  protected boolean hasField(final String name) {
    return fieldNames.contains(name);
  }

  protected void addField(final FieldDefinition field) {
    fieldNames.add(field.getName());
    fields.add(field);
  }

  public void close() {
    try {
      if (out != null) {
        try {
          out.write(0x1a);
          out.seek(1);
          final Date now = new Date();
          out.write(now.getYear());
          out.write(now.getMonth() + 1);
          out.write(now.getDate());
          out.writeLEInt(numRecords);
        } finally {
          out.close();
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public String toString() {
    return resource.toString();
  }

  protected void init() throws IOException {
    if (!initialized) {
      initialized = true;
      if (!(resource instanceof NonExistingResource)) {
        this.out = new ResourceEndianOutput(resource);
        writeHeader();
      }
    }
  }

  public boolean isUseZeroForNull() {
    return useZeroForNull;
  }

  public void setUseZeroForNull(final boolean useZeroForNull) {
    this.useZeroForNull = useZeroForNull;
  }

  public void write(final DataObject object) {
    try {
      if (!initialized) {
        init();
        preFirstWrite(object);
      }
      if (out != null) {
        out.write(' ');
      }
      for (final FieldDefinition field : fields) {
        if (!writeField(object, field)) {
          log.warn("Unable to write attribute '" + field.getName()
            + "' with value " + object.getValue(field.getName()));
        }
      }
      numRecords++;
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  protected void preFirstWrite(DataObject object) throws IOException {
  }

  protected boolean writeField(final DataObject object,
    final FieldDefinition field) throws IOException {
    if (out == null) {
      return true;
    } else {
      final Object value = object.getValue(field.getName());

      switch (field.getType()) {
        case FieldDefinition.NUMBER_TYPE:
          String numString = "";
          if (value == null) {
            if (useZeroForNull) {
              numString = field.getNumberFormat().format(0);
            }
          } else {
            numString = field.getNumberFormat().format(value);
          }
          for (int i = numString.length(); i < field.getLength(); i++) {
            out.write(' ');
          }
          out.writeBytes(numString);
          return true;
        case FieldDefinition.FLOAT_TYPE:
          String floatString = "";
          if (value != null) {
            floatString = value.toString();
          }
          for (int i = floatString.length(); i < field.getLength(); i++) {
            out.write(' ');
          }
          out.writeBytes(floatString);
          return true;

        case FieldDefinition.CHARACTER_TYPE:
          String string = "";
          if (value != null) {
            string = value.toString();
          }
          if (string.length() > field.getLength()) {
            out.writeBytes(string.substring(0, field.getLength()));
          } else {
            out.writeBytes(string);
            for (int i = string.length(); i < field.getLength(); i++) {
              out.write(' ');
            }
          }
          return true;

        case FieldDefinition.DATE_TYPE:
          if (value instanceof Date) {
            final Date date = (Date)value;
            final DateFormat format = new SimpleDateFormat("yyyyMMdd");
            final String dateString = format.format(date);
            out.writeBytes(dateString);

          } else if (value != null) {
            out.writeBytes(value.toString().substring(0, 8));
          } else {
            out.writeBytes("        ");
          }
          return true;

        case FieldDefinition.LOGICAL_TYPE:
          boolean logical = false;
          if (value instanceof Boolean) {
            final Boolean boolVal = (Boolean)value;
            logical = boolVal.booleanValue();
          } else if (value != null) {
            logical = Boolean.getBoolean(value.toString());
          }
          if (logical) {
            out.write('T');
          } else {
            out.write('F');
          }
          return true;

        default:
          return false;
      }
    }
  }

  private void writeHeader() throws IOException {
    if (out != null) {
      int recordLength = 1;

      fields.clear();
      int numFields = 0;
      for (final String name : metaData.getAttributeNames()) {
        final int index = metaData.getAttributeIndex(name);
        final int length = metaData.getAttributeLength(index);
        final int scale = metaData.getAttributeScale(index);
        final DataType attributeType = metaData.getAttributeType(index);
        final Class<?> typeJavaClass = attributeType.getJavaClass();
        final int fieldLength = addDbaseField(name, typeJavaClass, length,
          scale);
        if (fieldLength > 0) {
          recordLength += fieldLength;
          numFields++;
        }
      }
      out.write(0x03);
      final Date now = new Date();
      out.write(now.getYear());
      out.write(now.getMonth() + 1);
      out.write(now.getDate());
      // Write 0 as the number of records, come back and update this when closed
      out.writeLEInt(0);
      final short headerLength = (short)(33 + numFields * 32);

      out.writeLEShort(headerLength);
      out.writeLEShort((short)recordLength);
      out.writeLEShort((short)0);
      out.write(0);
      out.write(0);
      out.writeLEInt(0);
      out.writeLEInt(0);
      out.writeLEInt(0);
      out.write(0);
      out.write(1);
      out.writeLEShort((short)0);
      int offset = 1;
      for (final FieldDefinition field : fields) {
        if (field.getDataType() != DataTypes.OBJECT) {
          String name = field.getName();
          if (name.length() > 10) {
            name = name.substring(0, 10);
          }
          final int length = field.getLength();
          final int decimalPlaces = field.getDecimalPlaces();
          out.writeBytes(name.toUpperCase());
          for (int i = name.length(); i < 11; i++) {
            out.write(0);
          }
          out.write(field.getType());
          out.writeLEInt(0);
          out.write(length);
          out.write(decimalPlaces);
          out.writeLEShort((short)0);
          out.write(0);
          out.writeLEShort((short)0);
          out.write(0);
          out.write(0);
          out.write(0);
          out.write(0);
          out.write(0);
          out.write(0);
          out.write(0);
          out.write(0);
          out.write(0);
          offset += length;
        }
      }
      out.write(0x0d);
    }
  }
}
