package com.revolsys.gis.esri.gdb.file.capi.type;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.jdbc.ByteArrayBlob;

import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;

public class BinaryFieldDefinition extends AbstractFileGdbFieldDefinition {

  public BinaryFieldDefinition(final int fieldNumber, final Field field) {
    super(fieldNumber, field.getName(), DataTypes.BLOB, field.getLength(),
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 40;
  }

  @Override
  public Object getValue(final Row row) {
    if (row.isNull(this.fieldNumber)) {
      return null;
    } else {
      final byte[] bytes = row.getBinary(this.fieldNumber);
      return new ByteArrayBlob(bytes);
    }
  }

  @Override
  public void setValue(final Record record, final Row row, final Object value) {
    if (value == null) {
      setNull(row);
    } else {
      setNull(row);
      // byte[] bytes;
      // if (value instanceof byte[]) {
      // bytes = (byte[])value;
      // } else if (value instanceof ByteArrayBlob) {
      // final ByteArrayBlob blob = (ByteArrayBlob)value;
      // bytes = blob.getBytes();
      // } else if (value instanceof Blob) {
      // final Blob blob = (Blob)value;
      // try (
      // InputStream in = blob.getBinaryStream()) {
      // bytes = in.readAllBytes();
      // } catch (final SQLException | IOException e) {
      // throw Exceptions.wrap("Unable to get bytes", e);
      // }
      // } else {
      // bytes = value.toString().getBytes();
      // }
      // final int length = getLength();
      // if (length > 0 && bytes.length > length) {
      // Logs.warn(this, "Value is to long for: " + this + ":" + length + " " +
      // bytes.length);
      // final byte[] newBytes = new byte[length];
      // System.arraycopy(bytes, 0, newBytes, 0, length);
      // bytes = newBytes;
      // }
      // synchronized (row) {
      // // row.setBinary(this.fieldNumber, bytes);
      // }
    }
  }
}
