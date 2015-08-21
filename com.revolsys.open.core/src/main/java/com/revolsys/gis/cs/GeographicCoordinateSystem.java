package com.revolsys.gis.cs;

import java.util.ArrayList;
import java.util.List;

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.gis.cs.projection.CoordinatesProjection;

public class GeographicCoordinateSystem implements CoordinateSystem {
  public static final double EARTH_RADIUS = 6378137;

  /**
   *
   */
  private static final long serialVersionUID = 8655274386401351222L;

  public static double distanceMetres(final double lon1, final double lat1, final double lon2,
    final double lat2) {
    final double lon1Radians = Math.toRadians(lon1);
    final double lon2Radians = Math.toRadians(lon2);
    final double width = lon2Radians - lon1Radians;

    final double lat1Radians = Math.toRadians(lat1);
    final double lat2Radians = Math.toRadians(lat2);

    final double height = lat2Radians - lat1Radians;

    final double sinHeightOver2 = Math.sin(height / 2);
    final double sinWidthOver2 = Math.sin(width / 2);
    final double distance = 2 * EARTH_RADIUS * Math.asin(Math.sqrt(sinHeightOver2 * sinHeightOver2
      + Math.cos(lat1Radians) * Math.cos(lat2Radians) * sinWidthOver2 * sinWidthOver2));
    return distance;
  }

  private final AngularUnit angularUnit;

  private Area area;

  private final Authority authority;

  private final List<Axis> axis = new ArrayList<Axis>();

  private final Datum datum;

  private boolean deprecated;

  private final int id;

  private final String name;

  private final PrimeMeridian primeMeridian;

  public GeographicCoordinateSystem(final int id, final String name, final Datum datum,
    final AngularUnit angularUnit, final List<Axis> axis, final Area area,
    final Authority authority, final boolean deprecated) {
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

  public GeographicCoordinateSystem(final int id, final String name, final Datum datum,
    final PrimeMeridian primeMeridian, final AngularUnit angularUnit, final List<Axis> axis,
    final Area area, final Authority authority, final boolean deprecated) {
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

  public GeographicCoordinateSystem(final int id, final String name, final Datum datum,
    final PrimeMeridian primeMeridian, final AngularUnit angularUnit, final List<Axis> axis,
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
  public GeographicCoordinateSystem clone() {
    try {
      return (GeographicCoordinateSystem)super.clone();
    } catch (final Exception e) {
      return null;
    }
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof GeographicCoordinateSystem) {
      final GeographicCoordinateSystem cs = (GeographicCoordinateSystem)object;
      if (!equals(this.datum, cs.datum)) {
        return false;
      } else if (!equals(getPrimeMeridian(), cs.getPrimeMeridian())) {
        return false;
      } else if (!equals(this.angularUnit, cs.angularUnit)) {
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
      if (!equals(this.angularUnit, cs.angularUnit)) {
        return false;
      } else if (!equals(this.area, cs.area)) {
        return false;
      } else if (!equals(this.authority, cs.authority)) {
        return false;
      } else if (!equals(this.axis, cs.axis)) {
        return false;
      } else if (!equals(this.datum, cs.datum)) {
        return false;
      } else if (this.deprecated != cs.deprecated) {
        return false;
      } else if (this.id != cs.id) {
        return false;
      } else if (!equals(this.name, cs.name)) {
        return false;
      } else if (!equals(getPrimeMeridian(), cs.getPrimeMeridian())) {
        return false;
      } else {
        return true;
      }
    }
  }

  public AngularUnit getAngularUnit() {
    return this.angularUnit;
  }

  @Override
  public Area getArea() {
    return this.area;
  }

  @Override
  public BoundingBox getAreaBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (this.area != null) {
      return this.area.getLatLonBounds().convert(geometryFactory);
    } else {
      return new BoundingBoxDoubleGf(geometryFactory, 2, -180, -90, 180, 90);
    }
  }

  @Override
  public Authority getAuthority() {
    return this.authority;
  }

  @Override
  public List<Axis> getAxis() {
    return this.axis;
  }

  @Override
  public CoordinatesProjection getCoordinatesProjection() {
    return null;
  }

  public Datum getDatum() {
    return this.datum;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return GeometryFactory.floating3(this);
  }

  @Override
  public int getId() {
    return this.id;
  }

  @Override
  public Unit<Length> getLengthUnit() {
    final Unit<Angle> unit = this.angularUnit.getUnit();
    final UnitConverter radianConverter = unit.getConverterTo(SI.RADIAN);

    final Spheroid spheroid = this.datum.getSpheroid();
    final double radius = spheroid.getSemiMajorAxis();
    final double radianFactor = radianConverter.convert(1);
    return SI.METRE.times(radius).times(radianFactor);
  }

  @Override
  public String getName() {
    return this.name;
  }

  public PrimeMeridian getPrimeMeridian() {
    if (this.primeMeridian == null) {
      if (this.datum == null) {
        return null;
      } else {
        return this.datum.getPrimeMeridian();
      }
    } else {
      return this.primeMeridian;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Unit<Angle> getUnit() {
    return this.angularUnit.getUnit();
  }

  // public Unit<Angle> getUnit() {
  // return angularUnit.getUnit();
  // }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    if (this.datum != null) {
      result = prime * result + this.datum.hashCode();
    }
    if (getPrimeMeridian() != null) {
      result = prime * result + getPrimeMeridian().hashCode();
    }
    return result;
  }

  @Override
  public boolean isDeprecated() {
    return this.deprecated;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
