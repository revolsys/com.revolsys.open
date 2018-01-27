package com.revolsys.geometry.cs;

import java.util.Collections;
import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

import com.revolsys.geometry.cs.datum.VerticalDatum;
import com.revolsys.geometry.cs.epsg.EpsgAuthority;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.unit.LinearUnit;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class VerticalCoordinateSystem implements CoordinateSystem {
  private final LinearUnit linearUnit;

  private final Area area;

  private final Authority authority;

  private final List<Axis> axis;

  private final VerticalDatum verticalDatum;

  private boolean deprecated;

  private final int id;

  private final String name;

  public VerticalCoordinateSystem(final int id, final String name,
    final VerticalDatum verticalDatum, final List<Axis> axis, final Area area,
    final boolean deprecated) {
    this.id = id;
    this.name = name;
    this.verticalDatum = verticalDatum;
    this.linearUnit = (LinearUnit)axis.get(0).getUnit();
    this.axis = axis;
    this.area = area;
    this.authority = new EpsgAuthority(id);
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
    } else if (object instanceof VerticalCoordinateSystem) {
      final VerticalCoordinateSystem cs = (VerticalCoordinateSystem)object;
      if (!equals(this.verticalDatum, cs.verticalDatum)) {
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

  public boolean equalsExact(final VerticalCoordinateSystem cs) {
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
      } else if (!equals(this.verticalDatum, cs.verticalDatum)) {
        return false;
      } else if (this.deprecated != cs.deprecated) {
        return false;
      } else if (this.id != cs.id) {
        return false;
      } else if (!equals(this.name, cs.name)) {
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
    return Collections.unmodifiableList(this.axis);
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

  public VerticalDatum getDatum() {
    return this.verticalDatum;
  }

  @Override
  public Unit<Length> getLengthUnit() {
    return this.linearUnit.getUnit();
  }

  public LinearUnit getLinearUnit() {
    return this.linearUnit;
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
    if (this.verticalDatum != null) {
      result = prime * result + this.verticalDatum.hashCode();
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
