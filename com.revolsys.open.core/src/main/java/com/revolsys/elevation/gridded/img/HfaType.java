package com.revolsys.elevation.gridded.img;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;

public class HfaType {
  private boolean inCompleteDefn;

  protected int byteCount;

  private List<HfaField> fields = new ArrayList<>();

  private final String typeName;

  public HfaType(final String typeName, final List<HfaField> fields) {
    this.typeName = typeName;
    this.fields = fields;
  }

  HfaType addField(final int byteCount, final char itemType, final String fieldName) {
    final HfaField field = new HfaField(byteCount, itemType, fieldName);
    this.fields.add(field);
    return this;
  }

  HfaType addField(final int byteCount, final char itemType, final String fieldName,
    final List<String> enumValues) {
    final HfaField field = new HfaField(byteCount, itemType, fieldName, enumValues);
    this.fields.add(field);
    return this;
  }

  boolean completeDefn(final HfaDictionary dictionary) {
    if (this.byteCount == 0) {

      if (this.inCompleteDefn) {
        return false;
      } else {
        this.inCompleteDefn = true;

        boolean bRet = true;
        for (final HfaField field : this.fields) {
          if (!field.completeDefn(dictionary)) {
            bRet = false;
            break;
          }
          if (field.byteCount < 0 || this.byteCount == -1) {
            this.byteCount = -1;
          } else if (this.byteCount < Integer.MAX_VALUE - field.byteCount) {
            this.byteCount += field.byteCount;
          } else {
            this.byteCount = -1;
          }
        }

        this.inCompleteDefn = false;
        return bRet;
      }
    } else {
      return true;
    }
  }

  public boolean equalsTypeName(final String typeName) {
    if (typeName == null) {
      return false;
    } else {
      return typeName.equals(this.typeName);
    }
  }

  public List<HfaField> getFields() {
    return this.fields;
  }

  public MapEx readFieldValues(final ImgGriddedElevationReader reader) {
    final MapEx fieldValues = new LinkedHashMapEx();
    for (final HfaField field : this.fields) {
      final String name = field.getName();
      final Object value = field.readValue(reader);
      fieldValues.put(name, value);
    }
    return fieldValues;
  }

  @Override
  public String toString() {
    return this.typeName;
  }
}
