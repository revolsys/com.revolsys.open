package com.revolsys.geometry.cs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import com.revolsys.datatype.DataType;
import com.revolsys.geometry.cs.projection.ChainedCoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesProjection;
import com.revolsys.geometry.cs.projection.CopyOperation;
import com.revolsys.geometry.cs.projection.ProjectionFactory;
import com.revolsys.geometry.cs.projection.UnitConverstionOperation;
import com.revolsys.geometry.cs.unit.LinearUnit;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class ProjectedCoordinateSystem implements CoordinateSystem {
  private static final long serialVersionUID = 1902383026085071877L;

  private Area area;

  private final Authority authority;

  private final List<Axis> axis = new ArrayList<>();

  private CoordinatesProjection coordinatesProjection;

  private boolean deprecated;

  private final GeographicCoordinateSystem geographicCoordinateSystem;

  private int id;

  private BoundingBox areaBoundingBox;

  private final LinearUnit linearUnit;

  private final String name;

  private final Map<String, Object> normalizedParameters = new TreeMap<>();

  private final Map<String, Object> parameters = new LinkedHashMap<>();

  private final CoordinateOperationMethod coordinateOperationMethod;

  public ProjectedCoordinateSystem(final int id, final String name,
    final GeographicCoordinateSystem geographicCoordinateSystem, final Area area,
    final CoordinateOperationMethod coordinateOperationMethod, final Map<String, Object> parameters, final LinearUnit linearUnit,
    final List<Axis> axis, final Authority authority, final boolean deprecated) {
    this.id = id;
    this.name = name;
    this.area = area;
    this.geographicCoordinateSystem = geographicCoordinateSystem;
    this.coordinateOperationMethod = coordinateOperationMethod;
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
    final GeographicCoordinateSystem geographicCoordinateSystem, final CoordinateOperationMethod coordinateOperationMethod,
    final Map<String, Object> parameters, final LinearUnit linearUnit, final List<Axis> axis,
    final Authority authority) {
    this.id = id;
    this.name = name;
    this.geographicCoordinateSystem = geographicCoordinateSystem;
    this.coordinateOperationMethod = coordinateOperationMethod;
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
      if (!this.geographicCoordinateSystem.equals(cs.geographicCoordinateSystem)) {
        return false;
      } else if (!this.coordinateOperationMethod.equals(cs.coordinateOperationMethod)) {
        return false;
      } else if (!this.normalizedParameters.equals(cs.normalizedParameters)) {
        return false;
      } else if (!this.linearUnit.equals(cs.linearUnit)) {
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
      } else if (!DataType.equal(this.axis, cs.axis)) {
        return false;
      } else if (!this.geographicCoordinateSystem.equals(cs.geographicCoordinateSystem)) {
        return false;
      } else if (this.id != cs.id) {
        return false;
      } else if (!DataType.equal(this.linearUnit, cs.linearUnit)) {
        return false;
      } else if (!DataType.equal(this.name, cs.name)) {
        return false;
      } else if (!DataType.equal(this.normalizedParameters, cs.normalizedParameters)) {
        return false;
      } else if (!DataType.equal(this.coordinateOperationMethod, cs.coordinateOperationMethod)) {
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
    if (this.areaBoundingBox == null) {
      final GeometryFactory geographicGeometryFactory = this.geographicCoordinateSystem
        .getGeometryFactory();
      BoundingBox boundingBox;
      if (this.area == null) {
        boundingBox = geographicGeometryFactory.newBoundingBox(-180, -90, 180, 90);
      } else {
        final BoundingBox latLonBounds = this.area.getLatLonBounds();
        boundingBox = latLonBounds.convert(geographicGeometryFactory);
      }
      final GeometryFactory geometryFactory = getGeometryFactory();
      this.areaBoundingBox = boundingBox.convert(geometryFactory);
    }
    return this.areaBoundingBox;
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
    if (coordinateSystem == null || this == coordinateSystem) {
      return null;
    } else {
      final List<CoordinatesOperation> operations = new ArrayList<>();
      final CoordinatesOperation inverseOperation = this.getInverseCoordinatesOperation();
      if (inverseOperation == null) {
        return null;
      }
      final Unit<Length> linearUnit1 = this.getLengthUnit();
      if (!linearUnit1.equals(SI.METRE)) {
        operations.add(new UnitConverstionOperation(linearUnit1, SI.METRE));
      }
      operations.add(inverseOperation);

      if (coordinateSystem instanceof ProjectedCoordinateSystem) {
        final ProjectedCoordinateSystem projectedCoordinateSystem = (ProjectedCoordinateSystem)coordinateSystem;
        final CoordinatesOperation projectOperation = projectedCoordinateSystem.getProjectCoordinatesOperation();
        if (projectOperation != null) {
          operations.add(projectOperation);
        }
        final Unit<Length> linearUnit2 = projectedCoordinateSystem.getLengthUnit();
        if (!linearUnit2.equals(SI.METRE)) {
          operations.add(new UnitConverstionOperation(SI.METRE, linearUnit2));
        }
      } else if (coordinateSystem instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem geographicCoordinateSystem = (GeographicCoordinateSystem)coordinateSystem;
        final Unit<Angle> angularUnit2 = geographicCoordinateSystem.getUnit();
        if (!angularUnit2.equals(NonSI.DEGREE_ANGLE)) {
          operations.add(new UnitConverstionOperation(NonSI.DEGREE_ANGLE, angularUnit2, 2));
        }
      } else {
        return null;
      }
      switch (operations.size()) {
        case 0:
          return null;
        case 1:
          return operations.get(0);
        default:
          return new ChainedCoordinatesOperation(operations);
      }
    }
  }

  public synchronized CoordinatesProjection getCoordinatesProjection() {
    if (this.coordinatesProjection == null) {
      this.coordinatesProjection = ProjectionFactory.newCoordinatesProjection(this);
    }
    return this.coordinatesProjection;
  }

  @Override
  public int getCoordinateSystemId() {
    return this.id;
  }

  @Override
  public String getCoordinateSystemName() {
    return this.name;
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

  /**
   * Get the operation to convert coordinates to geographics coordinates.
   *
   * @return The coordinates operation.
   */
  public CoordinatesOperation getInverseCoordinatesOperation() {
    final CoordinatesProjection projection = getCoordinatesProjection();
    if (projection == null) {
      return null;
    } else {
      return projection.getInverseOperation();
    }
  }

  @Override
  public Unit<Length> getLengthUnit() {
    return this.linearUnit.getUnit();
  }

  public LinearUnit getLinearUnit() {
    return this.linearUnit;
  }

  @SuppressWarnings("unchecked")
  public <V> V getParameter(final String key) {
    return (V)this.normalizedParameters.get(key);
  }

  public Map<String, Object> getParameters() {
    return this.parameters;
  }

  /**
   * Get the operation to convert geographics coordinates to projected
   * coordinates.
   *
   * @return The coordinates operation.
   */
  public CoordinatesOperation getProjectCoordinatesOperation() {
    final CoordinatesProjection projection = getCoordinatesProjection();
    if (projection == null) {
      return new CopyOperation();
    } else {
      return projection.getProjectOperation();
    }
  }

  public CoordinateOperationMethod getCoordinateOperationMethod() {
    return this.coordinateOperationMethod;
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
    if (this.coordinateOperationMethod != null) {
      result = prime * result + this.coordinateOperationMethod.hashCode();
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
