package com.revolsys.gis.esri.gdb.file.capi.type;

import java.util.WeakHashMap;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.esri.filegdb.jni.Guid;
import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;

public class GuidFieldDefinition extends AbstractFileGdbFieldDefinition {
  private static final WeakHashMap<String, Guid> GUID_CACHE = new WeakHashMap<>();

  public static void addGuid(final Guid guid) {
    synchronized (GUID_CACHE) {
      final String guidString = guid.toString();
      if (!GUID_CACHE.containsKey(guidString)) {
        GUID_CACHE.put(guidString, guid);
      }
    }
  }

  public static Guid getGuid(final String guidString) {
    synchronized (GUID_CACHE) {
      Guid guid = GUID_CACHE.get(guidString);
      if (guid == null) {
        guid = new Guid();
        guid.FromString(guidString);
        GUID_CACHE.put(guidString, guid);
      }
      return guid;
    }
  }

  public GuidFieldDefinition(final int fieldNumber, final Field field) {
    this(fieldNumber, field.getName(), field.getLength(),
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  public GuidFieldDefinition(final int fieldNumber, final String name, final int length,
    final boolean required) {
    super(fieldNumber, name, DataTypes.STRING, length, required);
  }

  @Override
  public Object getValue(final Row row) {
    synchronized (row) {
      if (row.isNull(this.fieldNumber)) {
        return null;
      } else {
        final Guid guid = row.getGuid(this.fieldNumber);
        addGuid(guid);
        return guid.toString();
      }
    }
  }

  @Override
  public void setValue(final Record record, final Row row, final Object value) {
    if (value == null) {
      setNull(row);
    } else {
      final String guidString = value.toString();
      final Guid guid = getGuid(guidString);
      synchronized (row) {
        row.setGuid(this.fieldNumber, guid);
      }
    }
  }

}
