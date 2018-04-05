package com.revolsys.geometry.cs;

import java.util.List;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import com.revolsys.geometry.cs.datum.GeodeticDatum;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.unit.LinearUnit;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class GeocentricCoordinateSystem extends AbstractHorizontalCoordinateSystem {
  private static final long serialVersionUID = 8655274386401351222L;

  private final LinearUnit linearUnit;

  private final GeodeticDatum geodeticDatum;

  private final PrimeMeridian primeMeridian;

  public GeocentricCoordinateSystem(final int id, final String name,
    final GeodeticDatum geodeticDatum, final LinearUnit linearUnit, final List<Axis> axis,
    final Area area, final Authority authority, final boolean deprecated) {
    super(id, name, axis, area, deprecated, authority);
    this.geodeticDatum = geodeticDatum;
    this.primeMeridian = null;
    this.linearUnit = linearUnit;
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

  @Override
  public boolean equalsExact(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem instanceof GeocentricCoordinateSystem) {
      final GeocentricCoordinateSystem geocentricCoordinateSystem = (GeocentricCoordinateSystem)coordinateSystem;
      return equalsExact(geocentricCoordinateSystem);
    }
    return false;
  }

  public boolean equalsExact(final GeocentricCoordinateSystem cs) {
    if (super.equalsExact(cs)) {
      if (!equals(this.linearUnit, cs.linearUnit)) {
        return false;
      } else if (!equals(this.geodeticDatum, cs.geodeticDatum)) {
        return false;
      } else if (!equals(getPrimeMeridian(), cs.getPrimeMeridian())) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public CoordinatesOperation getCoordinatesOperation(final CoordinateSystem coordinateSystem) {
    return null;
  }

  @Override
  public String getCoordinateSystemType() {
    return "Geocentric";
  }

  public GeodeticDatum getDatum() {
    return this.geodeticDatum;
  }

  @Override
  public Unit<Length> getLengthUnit() {
    return this.linearUnit.getUnit();
  }

  @Override
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
  protected BoundingBox newAreaBoundingBox() {
    final Area area = getArea();
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (area != null) {
      return area.getLatLonBounds().convert(geometryFactory);
    } else {
      return geometryFactory.newBoundingBox(-180, -90, 180, 90);
    }
  }
}
