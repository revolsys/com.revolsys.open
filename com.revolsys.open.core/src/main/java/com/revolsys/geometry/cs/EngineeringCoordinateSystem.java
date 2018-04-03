package com.revolsys.geometry.cs;

import java.util.ArrayList;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import com.revolsys.geometry.cs.datum.EngineeringDatum;
import com.revolsys.geometry.cs.epsg.EpsgAuthority;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.unit.LinearUnit;
import com.revolsys.geometry.cs.unit.UnitOfMeasure;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class EngineeringCoordinateSystem implements CoordinateSystem {
  private static final long serialVersionUID = 8655274386401351222L;

  private final UnitOfMeasure unit;

  private final Area area;

  private final Authority authority;

  private final List<Axis> axis = new ArrayList<>();

  private final EngineeringDatum engineeringDatum;

  private boolean deprecated;

  private final int id;

  private final String name;

  public EngineeringCoordinateSystem(final int id, final String name,
    final EngineeringDatum engineeringDatum, final List<Axis> axis, final Area area,
    final boolean deprecated) {
    this.id = id;
    this.name = name;
    this.engineeringDatum = engineeringDatum;
    this.unit = axis.get(0).getUnit();
    if (axis != null && !axis.isEmpty()) {
      this.axis.addAll(axis);
    }
    this.area = area;
    this.authority = new EpsgAuthority(id);
  }

  @Override
  public EngineeringCoordinateSystem clone() {
    try {
      return (EngineeringCoordinateSystem)super.clone();
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
    } else if (object instanceof EngineeringCoordinateSystem) {
      final EngineeringCoordinateSystem cs = (EngineeringCoordinateSystem)object;
      if (!equals(this.engineeringDatum, cs.engineeringDatum)) {
        return false;
      } else if (!equals(this.unit, cs.unit)) {
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

  @Override
  public boolean equalsExact(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem instanceof EngineeringCoordinateSystem) {
      final EngineeringCoordinateSystem engineeringCoordinateSystem = (EngineeringCoordinateSystem)coordinateSystem;
      return equalsExact(engineeringCoordinateSystem);
    }
    return false;
  }

  public boolean equalsExact(final EngineeringCoordinateSystem cs) {
    if (cs == null) {
      return false;
    } else if (cs == this) {
      return true;
    } else {
      if (!equals(this.unit, cs.unit)) {
        return false;
      } else if (!equals(this.area, cs.area)) {
        return false;
      } else if (!equals(this.authority, cs.authority)) {
        return false;
      } else if (!equals(this.axis, cs.axis)) {
        return false;
      } else if (!equals(this.engineeringDatum, cs.engineeringDatum)) {
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

  @Override
  public String getCoordinateSystemType() {
    return "Engineering";
  }

  public EngineeringDatum getDatum() {
    return this.engineeringDatum;
  }

  @Override
  public Unit<Length> getLengthUnit() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LinearUnit getLinearUnit() {
    if (this.unit instanceof LinearUnit) {
      return (LinearUnit)this.unit;
    } else {
      return null;
    }
  }

  @Override
  public <Q extends Quantity<Q>> Unit<Q> getUnit() {
    if (this.unit instanceof LinearUnit) {
      final LinearUnit linearUnit = (LinearUnit)this.unit;
      return (Unit<Q>)linearUnit.getUnit();
    } else {
      return null;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    if (this.engineeringDatum != null) {
      result = prime * result + this.engineeringDatum.hashCode();
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
