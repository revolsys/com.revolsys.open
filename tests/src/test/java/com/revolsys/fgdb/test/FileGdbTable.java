package com.revolsys.fgdb.test;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.revolsys.beans.Classes;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.io.endian.LittleEndianRandomAccessFile;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;

public class FileGdbTable {
  private final long fieldHeaderOffset = 40;

  private final LittleEndianRandomAccessFile out;

  public FileGdbTable(final File file) {
    this.out = new LittleEndianRandomAccessFile(file, "rw");
  }

  public FileGdbTable(final File file, final RecordDefinition recordDefinition) {
    this.out = new LittleEndianRandomAccessFile(file, "rw");
    writeFieldHeader(recordDefinition);
  }

  private void writeFieldHeader(final FieldDefinition field) {
    final String name = field.getName();
    writeUtf16String255(name);
    final Class<?> fieldClass = field.getTypeClass();
    if (Byte.class.isAssignableFrom(fieldClass) || Short.class.isAssignableFrom(fieldClass)) {
      this.out.write(0);
    } else if (Integer.class.isAssignableFrom(fieldClass) || Long.class.isAssignableFrom(fieldClass)
      || BigInteger.class.isAssignableFrom(fieldClass)) {
      this.out.write(1);
    } else if (Float.class.isAssignableFrom(fieldClass)) {
      this.out.write(2);
    } else if (Double.class.isAssignableFrom(fieldClass)
      || BigDecimal.class.isAssignableFrom(fieldClass)) {
      this.out.write(3);
    } else if (Date.class.isAssignableFrom(fieldClass)) {
      this.out.write(5);
    } else if (Geometry.class.isAssignableFrom(fieldClass)) {
      this.out.write(7);
    } else {
      this.out.write(4);
      int fieldLength = field.getLength();
      if (fieldLength == 0) {
        fieldLength = 255;
      }
      this.out.writeLEInt(fieldLength);
    }
  }

  private void writeFieldHeader(final RecordDefinition recordDefinition) {
    byte geometryType = 0;

    final FieldDefinition geometryField = recordDefinition.getGeometryField();
    if (geometryField != null) {
      final Class<?> geometryClass = geometryField.getTypeClass();
      if (Punctual.class.isAssignableFrom(geometryClass)) {
        geometryType = 1;
      } else if (LineString.class.isAssignableFrom(geometryClass)) {
        geometryType = 3;
      } else if (Lineal.class.isAssignableFrom(geometryClass)) {
        geometryType = 3;
      } else if (Polygonal.class.isAssignableFrom(geometryClass)) {
        geometryType = 4;
      } else {
        throw new IllegalArgumentException(
          "Geometry type not supported " + Classes.className(geometryClass));
      }
    }

    final short fieldCount = (short)(recordDefinition.getFieldCount() + 1);

    this.out.writeLEInt(0);
    this.out.writeLEInt(4); // Version
    this.out.write(geometryType);
    this.out.write(0x03);
    this.out.write(0);
    this.out.write(0);

    this.out.writeLEShort(fieldCount);

    for (final FieldDefinition field : recordDefinition.getFields()) {
      writeFieldHeader(field);

    }
    // Update field header size
    final long offset = this.out.getFilePointer();
    final int fieldHeaderSize = (int)(offset - 14 - this.fieldHeaderOffset);
    this.out.seek(this.fieldHeaderOffset);
    this.out.write(fieldHeaderSize);
    this.out.seek(offset);

    this.out.write(0xDE);
    this.out.write(0xAD);
    this.out.write(0xBE);
    this.out.write(0xEF);
  }

  private void writeUtf16String255(final String string) {
    final int length = string.length();
    if (length > 255) {
      throw new IllegalArgumentException("Exceeds maximum string length " + length + " > 255");
    } else {
      this.out.write(length);
      for (int i = 0; i < length; i++) {
        final char c = string.charAt(i);
        this.out.writeLEShort((short)c);
      }
    }
  }
}
