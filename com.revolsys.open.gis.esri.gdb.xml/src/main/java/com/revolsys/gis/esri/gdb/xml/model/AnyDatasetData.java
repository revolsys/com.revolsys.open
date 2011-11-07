package com.revolsys.gis.esri.gdb.xml.model;

public class AnyDatasetData implements Cloneable {
  @Override
  public AnyDatasetData clone() {
    try {
      final AnyDatasetData clone = (AnyDatasetData)super.clone();
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

}
