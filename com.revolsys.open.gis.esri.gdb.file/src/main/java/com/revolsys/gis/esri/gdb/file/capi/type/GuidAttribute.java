package com.revolsys.gis.esri.gdb.file.capi.type;

import java.util.WeakHashMap;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.capi.swig.Guid;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.io.esri.gdb.xml.model.Field;

public class GuidAttribute extends AbstractFileGdbAttribute {
  private static final WeakHashMap<String, Guid> GUID_CACHE = new WeakHashMap<String, Guid>();

  public static Guid getGuid(String guidString) {
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

  public static void addGuid(Guid guid) {
    synchronized (GUID_CACHE) {
      String guidString = guid.toString();
      if (!GUID_CACHE.containsKey(guidString)) {
        GUID_CACHE.put(guidString, guid);
      }
    }
  }

  public GuidAttribute(final Field field) {
    this(field.getName(), field.getLength(),
      field.getRequired() == Boolean.TRUE || !field.isIsNullable());
  }

  public GuidAttribute(final String name, final int length,
    final boolean required) {
    super(name, DataTypes.STRING, length, required);
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    if (row.isNull(name)) {
      return null;
    } else {
      final Guid guid = row.getGuid(name);
      addGuid(guid);
      return guid.toString();
    }
  }

  @Override
  public Object setValue(final Row row, final Object value) {
    final String name = getName();
    if (value == null) {
      if (isRequired()) {
        throw new IllegalArgumentException(name
          + " is required and cannot be null");
      } else {
        row.setNull(name);
      }
      return null;
    } else {
      final String guidString = value.toString();
      final Guid guid = getGuid(guidString);
      row.setGuid(name, guid);
      return guid;
    }
  }

}
