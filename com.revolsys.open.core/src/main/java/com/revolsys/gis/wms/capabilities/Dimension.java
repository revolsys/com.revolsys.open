package com.revolsys.gis.wms.capabilities;

public class Dimension {
  private String name;

  private String units;

  private String unitSymbol;

  public String getName() {
    return this.name;
  }

  public String getUnits() {
    return this.units;
  }

  public String getUnitSymbol() {
    return this.unitSymbol;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setUnits(final String units) {
    this.units = units;
  }

  public void setUnitSymbol(final String unitSymbol) {
    this.unitSymbol = unitSymbol;
  }

}
