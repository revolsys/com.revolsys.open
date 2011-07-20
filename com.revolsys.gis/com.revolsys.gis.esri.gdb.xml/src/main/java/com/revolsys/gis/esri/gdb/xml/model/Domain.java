package com.revolsys.gis.esri.gdb.xml.model;

public class Domain implements Cloneable {

  @Override
  public Domain clone() {
    try {
      final Domain clone = (Domain)super.clone();
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
