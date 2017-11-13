package com.revolsys.gis.esri.gdb.file.capi.type;

import java.util.WeakHashMap;

import com.revolsys.datatype.DataTypes;
import com.revolsys.gis.esri.gdb.file.capi.swig.Guid;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
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

  public GuidFieldDefinition(final Field field) {
    this(field.getName(), field.getLength(),
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  public GuidFieldDefinition(final String name, final int length, final boolean required) {
    super(name, DataTypes.STRING, length, required);
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    synchronized (getSync()) {
      if (row.isNull(name)) {
        return null;
      } else {
        final Guid guid = row.getGuid(name);
        addGuid(guid);
        return guid.toString();
      }
    }
  }

  @Override
  public void setValue(final Record record, final Row row, final Object value) {
    final String name = getName();
    if (value == null) {
      setNull(row);
    } else {
      final String guidString = value.toString();
      final Guid guid = getGuid(guidString);
      synchronized (getSync()) {
        row.setGuid(name, guid);
      }
    }
  }

}
