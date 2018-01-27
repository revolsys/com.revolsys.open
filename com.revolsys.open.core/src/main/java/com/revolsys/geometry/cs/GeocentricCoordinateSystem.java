package com.revolsys.geometry.cs;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

import com.revolsys.geometry.cs.datum.GeodeticDatum;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.unit.LinearUnit;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class GeocentricCoordinateSystem implements CoordinateSystem {
  private static final long serialVersionUID = 8655274386401351222L;

  private final LinearUnit linearUnit;

  private final Area area;

  private final Authority authority;

  private final List<Axis> axis = new ArrayList<>();

  private final GeodeticDatum geodeticDatum;

  private boolean deprecated;

  private final int id;

  private final String name;

  private final PrimeMeridian primeMeridian;

  public GeocentricCoordinateSystem(final int id, final String name,
    final GeodeticDatum geodeticDatum, final LinearUnit linearUnit, final List<Axis> axis,
    final Area area, final Authority authority, final boolean deprecated) {
    this.id = id;
    this.name = name;
    this.geodeticDatum = geodeticDatum;
    this.primeMeridian = null;
    this.linearUnit = linearUnit;
    if (axis != null && !axis.isEmpty()) {
      this.axis.addAll(axis);
    }
    this.area = area;
    this.authority = authority;
  }

  @Override
  public GeocentricCoordinateSystem clone() {
    try {
      return (GeocentricCoordinateSystem)super.clone();
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
    } else if (object instanceof GeocentricCoordinateSystem) {
      final GeocentricCoordinateSystem cs = (GeocentricCoordinateSystem)object;
      if (!equals(this.geodeticDatum, cs.geodeticDatum)) {
        return false;
      } else if (!equals(getPrimeMeridian(), cs.getPrimeMeridian())) {
        return false;
      } else if (!equals(this.linearUnit, cs.linearUnit)) {
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

  public boolean equalsExact(final GeocentricCoordinateSystem cs) {
    if (cs == null) {
      return false;
    } else if (cs == this) {
      return true;
    } else {
      if (!equals(this.linearUnit, cs.linearUnit)) {
        return false;
      } else if (!equals(this.area, cs.area)) {
        return false;
      } else if (!equals(this.authority, cs.authority)) {
        return false;
      } else if (!equals(this.axis, cs.axis)) {
        return false;
      } else if (!equals(this.geodeticDatum, cs.geodeticDatum)) {
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
      return geometryFactory.newBoundingBox(-180, -90, 180, 90);
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
  public CoordinatesOperation getCoordinatesOperation(final CoordinateSystem coordinateSystem) {
    return null;
  }

  @Override
  public int getCoordinateSystemId() {
    return this.id;
  }

  @Override
  public String getCoordinateSystemName() {
    return this.name;
  }

  public GeodeticDatum getDatum() {
    return this.geodeticDatum;
  }

  @Override
  public Unit<Length> getLengthUnit() {
    return this.linearUnit.getUnit();
  }

  public LinearUnit getLinearUnit() {
    return this.linearUnit;
  }

  public PrimeMeridian getPrimeMeridian() {
    if (this.primeMeridian == null) {
      if (this.geodeticDatum == null) {
        return null;
      } else {
        return this.geodeticDatum.getPrimeMeridian();
      }
    } else {
      return this.primeMeridian;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Unit<Length> getUnit() {
    return this.linearUnit.getUnit();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    if (this.geodeticDatum != null) {
      result = prime * result + this.geodeticDatum.hashCode();
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
