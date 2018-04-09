package com.revolsys.geometry.cs.epsg;

import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import com.revolsys.geometry.cs.unit.AngularUnit;
import com.revolsys.geometry.cs.unit.LinearUnit;
import com.revolsys.geometry.cs.unit.UnitOfMeasure;

import tec.uom.se.AbstractSystemOfUnits;

class EpsgSystemOfUnits extends AbstractSystemOfUnits {
  protected EpsgSystemOfUnits() {
  }

  void addUnit(final UnitOfMeasure unitOfMeasure, final String name, final String symbol) {
    if (unitOfMeasure instanceof AngularUnit) {
      final AngularUnit angularUnit = (AngularUnit)unitOfMeasure;
      final Unit<Angle> unit = angularUnit.getUnit();
      Helper.addUnit(this.units, unit, name, symbol);
    } else if (unitOfMeasure instanceof LinearUnit) {
      final LinearUnit linearUnit = (LinearUnit)unitOfMeasure;
      final Unit<Length> unit = linearUnit.getUnit();
      Helper.addUnit(this.units, unit, name, symbol);
    }

  }

  @Override
  public String getName() {
    return "EPSG Units";
  }
}
