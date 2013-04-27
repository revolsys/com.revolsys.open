package com.revolsys.gis.cs;

import java.util.ArrayList;
import java.util.List;

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

public class GeographicCoordinateSystem implements CoordinateSystem {

  public double distanceMetres(double lat1, double lon1, double lat2,
    double lon2) {
    double RADIUS = 63781370; 
    double latDistance = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(latDistance / 2)
      * Math.sin(latDistance / 2)
      + Math.cos(Math.toRadians(lat1) * Math.cos(Math.toRadians(lat2))
        * Math.sin(dLon / 2) * Math.sin(dLon / 2));
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return RADIUS * c;
  }

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

  public GeographicCoordinateSystem(final int id, final String name,
    final Datum datum, final AngularUnit angularUnit, final List<Axis> axis,
    final Area area, final Authority authority, final boolean deprecated) {
    this.id = id;
    this.name = name;
    this.datum = datum;
    this.primeMeridian = null;
    this.angularUnit = angularUnit;
    if (axis != null && !axis.isEmpty()) {
      this.axis.addAll(axis);
    }
    this.area = area;
    this.authority = authority;
  }

  public GeographicCoordinateSystem(final int id, final String name,
    final Datum datum, final PrimeMeridian primeMeridian,
    final AngularUnit angularUnit, final List<Axis> axis, final Area area,
    final Authority authority, final boolean deprecated) {
    this.id = id;
    this.name = name;
    this.datum = datum;
    this.primeMeridian = primeMeridian;
    this.angularUnit = angularUnit;
    if (axis != null && !axis.isEmpty()) {
      this.axis.addAll(axis);
    }
    this.area = area;
    this.authority = authority;
    this.deprecated = deprecated;
  }

  public GeographicCoordinateSystem(final int id, final String name,
    final Datum datum, final PrimeMeridian primeMeridian,
    final AngularUnit angularUnit, final List<Axis> axis,
    final Authority authority) {
    this.id = id;
    this.name = name;
    this.datum = datum;
    this.primeMeridian = primeMeridian;
    this.angularUnit = angularUnit;
    if (axis != null && !axis.isEmpty()) {
      this.axis.addAll(axis);
    }
    this.authority = authority;
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof GeographicCoordinateSystem) {
      final GeographicCoordinateSystem cs = (GeographicCoordinateSystem)object;
      if (!equals(datum, cs.datum)) {
        return false;
      } else if (!equals(getPrimeMeridian(), cs.getPrimeMeridian())) {
        return false;
      } else if (!equals(angularUnit, cs.angularUnit)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  private boolean equals(final Object object1, final Object object2) {
    if (object1 == object2) {
      return true;
    } else if (object1 == null || object2 == null) {
      return false;
    } else {
      return object1.equals(object2);
    }
  }

  public boolean equalsExact(final GeographicCoordinateSystem cs) {
    if (cs == null) {
      return false;
    } else if (cs == this) {
      return true;
    } else {
      if (!equals(angularUnit, cs.angularUnit)) {
        return false;
      } else if (!equals(area, cs.area)) {
        return false;
      } else if (!equals(authority, cs.authority)) {
        return false;
      } else if (!equals(axis, cs.axis)) {
        return false;
      } else if (!equals(datum, cs.datum)) {
        return false;
      } else if (deprecated != cs.deprecated) {
        return false;
      } else if (id != cs.id) {
        return false;
      } else if (!equals(name, cs.name)) {
        return false;
      } else if (!equals(getPrimeMeridian(), cs.getPrimeMeridian())) {
        return false;
      } else {
        return true;
      }
    }
  }

  public AngularUnit getAngularUnit() {
    return angularUnit;
  }

  @Override
  public Area getArea() {
    return area;
  }

  @Override
  public BoundingBox getAreaBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (area != null) {
      return new BoundingBox(geometryFactory, area.getLatLonBounds());
    } else {
      return new BoundingBox(geometryFactory, -180, -90, 180, 90);
    }
  }

  @Override
  public Authority getAuthority() {
    return authority;
  }

  @Override
  public List<Axis> getAxis() {
    return axis;
  }

  public Datum getDatum() {
    return datum;
  }

  public GeometryFactory getGeometryFactory() {
    return GeometryFactory.getFactory(this);
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public Unit<Length> getLengthUnit() {
    final Unit<Angle> unit = angularUnit.getUnit();
    final UnitConverter radianConverter = unit.getConverterTo(SI.RADIAN);

    final Spheroid spheroid = datum.getSpheroid();
    final double radius = spheroid.getSemiMajorAxis();
    final double radianFactor = radianConverter.convert(1);
    return SI.METRE.times(radius).times(radianFactor);
  }

  @Override
  public String getName() {
    return name;
  }

  public PrimeMeridian getPrimeMeridian() {
    if (primeMeridian == null) {
      if (datum == null) {
        return null;
      } else {
        return datum.getPrimeMeridian();
      }
    } else {
      return primeMeridian;
    }
  }

  // public Unit<Angle> getUnit() {
  // return angularUnit.getUnit();
  // }

  @Override
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
    if (getPrimeMeridian() != null) {
      result = prime * result + getPrimeMeridian().hashCode();
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
