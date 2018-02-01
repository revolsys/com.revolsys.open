package com.revolsys.geometry.cs;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import com.revolsys.geometry.cs.epsg.EpsgAuthority;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class CompoundCoordinateSystem implements CoordinateSystem {
  private static final long serialVersionUID = 8655274386401351222L;

  private final Area area;

  private final Authority authority;

  private final CoordinateSystem horizontalCoordinateSystem;

  private boolean deprecated;

  private final int id;

  private final String name;

  private final CoordinateSystem verticalCoordinateSystem;

  private final List<Axis> axis = new ArrayList<>();

  public CompoundCoordinateSystem(final int id, final String name,
    final CoordinateSystem horizontalCoordinateSystem,
    final CoordinateSystem verticalCoordinateSystem, final Area area, final boolean deprecated) {
    this.id = id;
    this.name = name;
    this.horizontalCoordinateSystem = horizontalCoordinateSystem;
    this.verticalCoordinateSystem = verticalCoordinateSystem;
    this.axis.addAll(horizontalCoordinateSystem.getAxis());
    this.axis.addAll(verticalCoordinateSystem.getAxis());
    this.area = area;
    this.authority = new EpsgAuthority(id);
  }

  @Override
  public CompoundCoordinateSystem clone() {
    try {
      return (CompoundCoordinateSystem)super.clone();
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
    } else if (object instanceof CompoundCoordinateSystem) {
      final CompoundCoordinateSystem cs = (CompoundCoordinateSystem)object;
      if (!equals(this.horizontalCoordinateSystem, cs.horizontalCoordinateSystem)) {
        return false;
      } else if (!equals(this.verticalCoordinateSystem, cs.verticalCoordinateSystem)) {
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

  public boolean equalsExact(final CompoundCoordinateSystem cs) {
    if (cs == null) {
      return false;
    } else if (cs == this) {
      return true;
    } else {
      if (!equals(this.area, cs.area)) {
        return false;
      } else if (!equals(this.authority, cs.authority)) {
        return false;
      } else if (!equals(this.horizontalCoordinateSystem, cs.horizontalCoordinateSystem)) {
        return false;
      } else if (!equals(this.verticalCoordinateSystem, cs.verticalCoordinateSystem)) {
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
  public boolean equalsExact(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem instanceof CompoundCoordinateSystem) {
      final CompoundCoordinateSystem compoundCoordinateSystem = (CompoundCoordinateSystem)coordinateSystem;
      return equalsExact(compoundCoordinateSystem);
    }
    return false;
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
    return "Compound";
  }

  public CoordinateSystem getHorizontalCoordinateSystem() {
    return this.horizontalCoordinateSystem;
  }

  @Override
  public Unit<Length> getLengthUnit() {
    return this.horizontalCoordinateSystem.getLengthUnit();
  }

  @Override
  public <Q extends Quantity> Unit<Q> getUnit() {
    return this.horizontalCoordinateSystem.getUnit();
  }

  public CoordinateSystem getVerticalCoordinateSystem() {
    return this.verticalCoordinateSystem;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    if (this.horizontalCoordinateSystem != null) {
      result = prime * result + this.horizontalCoordinateSystem.hashCode();
    }
    if (this.verticalCoordinateSystem != null) {
      result = prime * result + this.verticalCoordinateSystem.hashCode();
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
