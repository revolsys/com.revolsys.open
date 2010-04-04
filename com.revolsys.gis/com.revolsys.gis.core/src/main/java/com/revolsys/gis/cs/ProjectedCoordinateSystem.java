package com.revolsys.gis.cs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

import com.vividsolutions.jts.geom.Envelope;

public class ProjectedCoordinateSystem implements CoordinateSystem {
  private Area area;

  private final Authority authority;

  private final List<Axis> axis = new ArrayList<Axis>();

  private boolean deprecated;

  private final GeographicCoordinateSystem geographicCoordinateSystem;

  private final LinearUnit linearUnit;

  private final int id;

  private final String name;

  private final Map<String, Object> parameters = new TreeMap<String, Object>();

  private final Projection projection;

  public ProjectedCoordinateSystem(
    final int id,
    final String name,
    final GeographicCoordinateSystem geographicCoordinateSystem,
    final Area area,
    final Projection projection,
    final Map<String, Object> parameters,
    final LinearUnit linearUnit,
    final List<Axis> axis,
    final Authority authority,
    final boolean deprecated) {
    this.id = id;
    this.name = name;
    this.area = area;
    this.geographicCoordinateSystem = geographicCoordinateSystem;
    this.projection = projection;
    this.parameters.putAll(parameters);
    this.linearUnit = linearUnit;
    if (axis != null && !axis.isEmpty()) {
      this.axis.add(axis.get(0));
      this.axis.add(axis.get(1));
    }
    this.authority = authority;
    this.deprecated = deprecated;
  }

  public ProjectedCoordinateSystem(
    final int id,
    final String name,
    final GeographicCoordinateSystem geographicCoordinateSystem,
    final Projection projection,
    final Map<String, Object> parameters,
    final LinearUnit linearUnit,
    final List<Axis> axis,
    final Authority authority) {
    this.id = id;
    this.name = name;
    this.geographicCoordinateSystem = geographicCoordinateSystem;
    this.projection = projection;
    this.parameters.putAll(parameters);
    this.linearUnit = linearUnit;
    if (axis != null && !axis.isEmpty()) {
      this.axis.add(axis.get(0));
      this.axis.add(axis.get(1));
    }
    this.authority = authority;
  }

  public int getId() {
    return id;
  }

  @Override
  public boolean equals(
    final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem cs = (ProjectedCoordinateSystem)object;
      if (!geographicCoordinateSystem.equals(cs.geographicCoordinateSystem)) {
        return false;
      } else if (!equals(projection, cs.projection)) {
        return false;
      } else if (!equals(parameters, cs.parameters)) {
        return false;
      } else if (!equals(linearUnit, cs.linearUnit)) {
        return false;
      } else if (!equals(axis, cs.axis)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  private boolean equals(
    final Object object1,
    final Object object2) {
    if (object1 == object2) {
      return true;
    } else if (object1 == null || object2 == null) {
      return false;
    } else {
      return object1.equals(object2);
    }
  }

  public Area getArea() {
    return area;
  }

  public BoundingBox getAreaBoundingBox() {
    BoundingBox boundingBox;
    if (area != null) {
      final Envelope latLonBounds = area.getLatLonBounds();
      boundingBox = new BoundingBox(geographicCoordinateSystem, latLonBounds);
    } else {
      boundingBox = new BoundingBox(this, -180, -90, 180, 90);
    }
    final BoundingBox projectedBoundingBox = boundingBox.convert(this);
    return projectedBoundingBox;
  }

  public Authority getAuthority() {
    return authority;
  }

  public List<Axis> getAxis() {
    return axis;
  }

  public double getDoubleParameter(
    final String key) {
    final Number value = getParameter(key);
    if (value == null) {
      return Double.NaN;
    } else {
      return value.doubleValue();
    }
  }

  public GeographicCoordinateSystem getGeographicCoordinateSystem() {
    return geographicCoordinateSystem;
  }

  public Unit<Length> getLengthUnit() {
    return linearUnit.getUnit();
  }

  public LinearUnit getLinearUnit() {
    return linearUnit;
  }

  public String getName() {
    return name;
  }

  @SuppressWarnings("unchecked")
  public <V> V getParameter(
    final String key) {
    return (V)parameters.get(key);
  }

  public Map<String, Object> getParameters() {
    return parameters;
  }

  public Projection getProjection() {
    return projection;
  }

  @SuppressWarnings("unchecked")
  public Unit<Length> getUnit() {
    return linearUnit.getUnit();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + geographicCoordinateSystem.hashCode();
    if (projection != null) {
      result = prime * result + projection.hashCode();
    }
    if (parameters != null) {
      for (Entry<String, Object> entry : parameters.entrySet()) {
        final String key = entry.getKey();
        result = prime * result + key.toLowerCase().hashCode();
        result = prime * result + entry.getValue().hashCode();
      }
    }
    if (linearUnit != null) {
      result = prime * result + linearUnit.hashCode();
    }
    result = prime * result + axis.hashCode();
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
