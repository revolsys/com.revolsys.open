package com.revolsys.gis.cs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.gis.cs.projection.CoordinatesProjection;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;

public class ProjectedCoordinateSystem implements CoordinateSystem {
  /**
   *
   */
  private static final long serialVersionUID = 1902383026085071877L;

  private Area area;

  private final Authority authority;

  private final List<Axis> axis = new ArrayList<Axis>();

  private boolean deprecated;

  private final GeographicCoordinateSystem geographicCoordinateSystem;

  private final LinearUnit linearUnit;

  private int id;

  private final String name;

  private final Map<String, Object> parameters = new LinkedHashMap<String, Object>();

  private final Map<String, Object> normalizedParameters = new TreeMap<String, Object>();

  private final Projection projection;

  private CoordinatesProjection coordinatesProjection;

  public ProjectedCoordinateSystem(final int id, final String name,
    final GeographicCoordinateSystem geographicCoordinateSystem, final Area area,
    final Projection projection, final Map<String, Object> parameters, final LinearUnit linearUnit,
    final List<Axis> axis, final Authority authority, final boolean deprecated) {
    this.id = id;
    this.name = name;
    this.area = area;
    this.geographicCoordinateSystem = geographicCoordinateSystem;
    this.projection = projection;
    setParameters(parameters);
    this.linearUnit = linearUnit;
    if (axis != null && !axis.isEmpty()) {
      this.axis.add(axis.get(0));
      this.axis.add(axis.get(1));
    }
    this.authority = authority;
    this.deprecated = deprecated;
  }

  public ProjectedCoordinateSystem(final int id, final String name,
    final GeographicCoordinateSystem geographicCoordinateSystem, final Projection projection,
    final Map<String, Object> parameters, final LinearUnit linearUnit, final List<Axis> axis,
    final Authority authority) {
    this.id = id;
    this.name = name;
    this.geographicCoordinateSystem = geographicCoordinateSystem;
    this.projection = projection;
    setParameters(parameters);
    this.linearUnit = linearUnit;
    if (axis != null && !axis.isEmpty()) {
      this.axis.add(axis.get(0));
      this.axis.add(axis.get(1));
    }
    this.authority = authority;
  }

  @Override
  public ProjectedCoordinateSystem clone() {
    try {
      return (ProjectedCoordinateSystem)super.clone();
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
    } else if (object instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem cs = (ProjectedCoordinateSystem)object;
      if (!EqualsRegistry.equal(this.geographicCoordinateSystem, cs.geographicCoordinateSystem)) {
        return false;
      } else if (!EqualsRegistry.equal(this.projection, cs.projection)) {
        return false;
      } else if (!EqualsRegistry.equal(this.normalizedParameters, cs.normalizedParameters)) {
        return false;
      } else if (!EqualsRegistry.equal(this.linearUnit, cs.linearUnit)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public boolean equalsExact(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else if (object instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem cs = (ProjectedCoordinateSystem)object;
      if (!this.area.equals(cs.area)) {
        return false;
      } else if (!this.authority.equals(cs.authority)) {
        return false;
      } else if (!EqualsRegistry.equal(this.axis, cs.axis)) {
        return false;
      } else if (!this.geographicCoordinateSystem.equals(cs.geographicCoordinateSystem)) {
        return false;
      } else if (this.id != cs.id) {
        return false;
      } else if (!EqualsRegistry.equal(this.linearUnit, cs.linearUnit)) {
        return false;
      } else if (!EqualsRegistry.equal(this.name, cs.name)) {
        return false;
      } else if (!EqualsRegistry.equal(this.normalizedParameters, cs.normalizedParameters)) {
        return false;
      } else if (!EqualsRegistry.equal(this.projection, cs.projection)) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public Area getArea() {
    return this.area;
  }

  @Override
  public BoundingBox getAreaBoundingBox() {
    BoundingBox boundingBox;
    final GeometryFactory geographicGeometryFactory = this.geographicCoordinateSystem.getGeometryFactory();
    if (this.area == null) {
      boundingBox = new BoundingBoxDoubleGf(geographicGeometryFactory, 2, -180, -90, 180, 90);
    } else {
      final BoundingBoxDoubleGf latLonBounds = this.area.getLatLonBounds();
      boundingBox = latLonBounds.convert(geographicGeometryFactory);
    }
    final BoundingBox projectedBoundingBox = boundingBox.convert(getGeometryFactory());
    return projectedBoundingBox;
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
  public synchronized CoordinatesProjection getCoordinatesProjection() {
    if (this.coordinatesProjection == null) {
      this.coordinatesProjection = ProjectionFactory.createCoordinatesProjection(this);
    }
    return this.coordinatesProjection;
  }

  public double getDoubleParameter(final String key) {
    final Number value = getParameter(key);
    if (value == null) {
      return Double.NaN;
    } else {
      return value.doubleValue();
    }
  }

  public GeographicCoordinateSystem getGeographicCoordinateSystem() {
    return this.geographicCoordinateSystem;
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
    return this.linearUnit.getUnit();
  }

  public LinearUnit getLinearUnit() {
    return this.linearUnit;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @SuppressWarnings("unchecked")
  public <V> V getParameter(final String key) {
    return (V)this.normalizedParameters.get(key);
  }

  public Map<String, Object> getParameters() {
    return this.parameters;
  }

  public Projection getProjection() {
    return this.projection;
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
    if (this.geographicCoordinateSystem != null) {
      result = prime * result + this.geographicCoordinateSystem.hashCode();
    }
    if (this.projection != null) {
      result = prime * result + this.projection.hashCode();
    }
    for (final Entry<String, Object> entry : this.normalizedParameters.entrySet()) {
      final String key = entry.getKey();
      result = prime * result + key.hashCode();
      result = prime * result + entry.getValue().hashCode();
    }
    if (this.linearUnit != null) {
      result = prime * result + this.linearUnit.hashCode();
    }
    return result;
  }

  @Override
  public boolean isDeprecated() {
    return this.deprecated;
  }

  public void setId(final int id) {
    this.id = id;
  }

  public void setParameters(final Map<String, Object> parameters) {
    for (final Entry<String, Object> param : parameters.entrySet()) {
      final String name = param.getKey().intern();
      final Object value = param.getValue();

      this.parameters.put(name, value);

      final String normalizedName = ProjectionParameterNames.getParameterName(name);
      this.normalizedParameters.put(normalizedName, value);
    }
  }

  @Override
  public String toString() {
    return this.name;
  }
}
