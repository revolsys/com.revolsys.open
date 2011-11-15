package com.revolsys.gis.cs;

import java.util.ArrayList;
import java.util.List;

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

public class GeographicCoordinateSystem implements CoordinateSystem {
  /**
   * 
   */
  private static final long serialVersionUID = 8655274386401351222L;

  private final AngularUnit angularUnit;

  private Area area;

  private final Authority authority;

  private final List<Axis> axis = new ArrayList<Axis>();

  private final Datum datum;

  private boolean deprecated;

  private final int id;

  private final String name;

  private final PrimeMeridian primeMeridian;

  public GeographicCoordinateSystem(
    final int id,
    final String name,
    final Datum datum,
    final PrimeMeridian primeMeridian,
    final AngularUnit angularUnit,
    final List<Axis> axis,
    final Area area,
    final Authority authority,
    final boolean deprecated) {
    this.id = id;
    this.name = name;
    this.datum = datum;
    this.primeMeridian = primeMeridian;
    this.angularUnit = angularUnit;
    if (axis != null && !axis.isEmpty()) {
      this.axis.add(axis.get(0));
      this.axis.add(axis.get(1));
    }
    this.area = area;
    this.authority = authority;
    this.deprecated = deprecated;
  }

  public GeographicCoordinateSystem(
    final int id,
    final String name,
    final Datum datum,
    final PrimeMeridian primeMeridian,
    final AngularUnit angularUnit,
    final List<Axis> axis,
    final Authority authority) {
    this.id = id;
    this.name = name;
    this.datum = datum;
    this.primeMeridian = primeMeridian;
    this.angularUnit = angularUnit;
    if (axis != null && !axis.isEmpty()) {
      this.axis.add(axis.get(0));
      this.axis.add(axis.get(1));
    }
    this.authority = authority;
  }

  @Override
  public boolean equals(
    final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof GeographicCoordinateSystem) {
      final GeographicCoordinateSystem cs = (GeographicCoordinateSystem)object;
      if (datum != null && !datum.equals(cs.datum)) {
        return false;
      } else if (primeMeridian != null
        && !primeMeridian.equals(cs.primeMeridian)) {
        return false;
      } else if (angularUnit != null && !angularUnit.equals(cs.angularUnit)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public AngularUnit getAngularUnit() {
    return angularUnit;
  }

  public Area getArea() {
    return area;
  }

  public BoundingBox getAreaBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (area != null) {
      return new BoundingBox(geometryFactory, area.getLatLonBounds());
    } else {
      return new BoundingBox(geometryFactory, -180, -90, 180, 90);
    }
  }

  public GeometryFactory getGeometryFactory() {
    return GeometryFactory.getFactory(this);
  }

  public Authority getAuthority() {
    return authority;
  }

  public List<Axis> getAxis() {
    return axis;
  }

  public Datum getDatum() {
    return datum;
  }

  public int getId() {
    return id;
  }

  public Unit<Length> getLengthUnit() {
    final Unit<Angle> unit = angularUnit.getUnit();
    final UnitConverter radianConverter = unit.getConverterTo(SI.RADIAN);

    final Spheroid spheroid = datum.getSpheroid();
    final double radius = spheroid.getSemiMajorAxis();
    final double radianFactor = radianConverter.convert(1);
    return SI.METRE.times(radius).times(radianFactor);
  }

  public String getName() {
    return name;
  }

  public PrimeMeridian getPrimeMeridian() {
    return primeMeridian;
  }

  // public Unit<Angle> getUnit() {
  // return angularUnit.getUnit();
  // }

  @SuppressWarnings("unchecked")
  public Unit<Angle> getUnit() {
    return angularUnit.getUnit();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    if (datum != null) {
      result = prime * result + datum.hashCode();
    }
    if (primeMeridian != null) {
      result = prime * result + primeMeridian.hashCode();
    }
    return result;
  }

  public boolean isDeprecated() {
    return deprecated;
  }

  @Override
  public String toString() {
    return name;
  }
}
