package com.revolsys.geometry.cs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.measure.Unit;
import javax.measure.quantity.Length;

import com.revolsys.datatype.DataType;
import com.revolsys.geometry.cs.projection.ChainedCoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesProjection;
import com.revolsys.geometry.cs.projection.NoOpOperation;
import com.revolsys.geometry.cs.projection.UnitConverstionOperation;
import com.revolsys.geometry.cs.unit.AngularUnit;
import com.revolsys.geometry.cs.unit.LinearUnit;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

import tec.uom.se.unit.Units;

public class ProjectedCoordinateSystem extends AbstractHorizontalCoordinateSystem {
  private static final long serialVersionUID = 1902383026085071877L;

  private CoordinatesProjection coordinatesProjection;

  private final GeographicCoordinateSystem geographicCoordinateSystem;

  private final LinearUnit linearUnit;

  private final Map<ParameterName, Object> parameters = new LinkedHashMap<>();

  private final Map<ParameterName, ParameterValue> parameterValues;

  private final CoordinateOperationMethod coordinateOperationMethod;

  public ProjectedCoordinateSystem(final int id, final String name,
    final GeographicCoordinateSystem geographicCoordinateSystem, final Area area,
    final CoordinateOperationMethod coordinateOperationMethod,
    final Map<ParameterName, ParameterValue> parameterValues, final LinearUnit linearUnit,
    final List<Axis> axis, final Authority authority, final boolean deprecated) {
    super(id, name, axis, area, deprecated);
    this.geographicCoordinateSystem = geographicCoordinateSystem;
    this.coordinateOperationMethod = coordinateOperationMethod;
    if (parameterValues == null) {
      this.parameterValues = new LinkedHashMap<>();
    } else {
      this.parameterValues = parameterValues;
    }
    for (final Entry<ParameterName, ParameterValue> entry : this.parameterValues.entrySet()) {
      final ParameterName parameterName = entry.getKey();
      final ParameterValue parameterValue = entry.getValue();
      final Object value;
      if (parameterValue == null) {
        value = parameterName.getDefaultValue();
      } else {
        value = parameterValue.getValue();
      }
      this.parameters.put(parameterName, value);
    }
    this.linearUnit = linearUnit;
  }

  public ProjectedCoordinateSystem(final int id, final String name,
    final GeographicCoordinateSystem geographicCoordinateSystem,
    final CoordinateOperationMethod coordinateOperationMethod,
    final Map<ParameterName, ParameterValue> parameters, final LinearUnit linearUnit) {
    this(id, name, geographicCoordinateSystem, null, coordinateOperationMethod, parameters,
      linearUnit, null, null, false);
  }

  public ProjectedCoordinateSystem(final int id, final String name,
    final GeographicCoordinateSystem geographicCoordinateSystem,
    final CoordinateOperationMethod coordinateOperationMethod,
    final Map<ParameterName, ParameterValue> parameters, final LinearUnit linearUnit,
    final List<Axis> axis, final Authority authority) {
    this(id, name, geographicCoordinateSystem, null, coordinateOperationMethod, parameters,
      linearUnit, axis, authority, false);
  }

  public ProjectedCoordinateSystem(final int id, final String name,
    final GeographicCoordinateSystem geographicCoordinateSystem, final String methodName,
    final Map<ParameterName, ParameterValue> parameterValues, final LinearUnit linearUnit,
    final Authority authority) {
    this(id, name, geographicCoordinateSystem, null, new CoordinateOperationMethod(methodName),
      parameterValues, linearUnit, Collections.emptyList(), authority, false);
  }

  @Override
  public ProjectedCoordinateSystem clone() {
    return (ProjectedCoordinateSystem)super.clone();
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
      } else if (this.coordinateOperationMethod == null ? cs.coordinateOperationMethod != null
        : !this.coordinateOperationMethod.equals(cs.coordinateOperationMethod)) {
        return false;
      } else if (!this.parameters.equals(cs.parameters)) {
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

  @Override
  public boolean equalsExact(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem instanceof GeocentricCoordinateSystem) {
      final GeocentricCoordinateSystem geocentricCoordinateSystem = (GeocentricCoordinateSystem)coordinateSystem;
      return equalsExact(geocentricCoordinateSystem);
    }
    return false;
  }

  public boolean equalsExact(final ProjectedCoordinateSystem cs) {
    if (super.equalsExact(cs)) {
      if (!this.geographicCoordinateSystem.equals(cs.geographicCoordinateSystem)) {
        return false;
      } else if (!DataType.equal(this.linearUnit, cs.linearUnit)) {
        return false;
      } else if (!DataType.equal(this.parameters, cs.parameters)) {
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

  public CoordinateOperationMethod getCoordinateOperationMethod() {
    return this.coordinateOperationMethod;
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
      if (!linearUnit1.equals(Units.METRE)) {
        operations.add(new UnitConverstionOperation(linearUnit1, Units.METRE));
      }
      operations.add(inverseOperation);

      if (coordinateSystem instanceof ProjectedCoordinateSystem) {
        // TODO datum conversion
        final ProjectedCoordinateSystem projectedCoordinateSystem = (ProjectedCoordinateSystem)coordinateSystem;
        final CoordinatesOperation projectOperation = projectedCoordinateSystem
          .getProjectCoordinatesOperation();
        if (projectOperation != null) {
          operations.add(projectOperation);
        }
        final Unit<Length> linearUnit2 = projectedCoordinateSystem.getLengthUnit();
        if (!linearUnit2.equals(Units.METRE)) {
          operations.add(new UnitConverstionOperation(Units.METRE, linearUnit2));
        }
      } else if (coordinateSystem instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem geographicCoordinateSystem = (GeographicCoordinateSystem)coordinateSystem;
        // TODO datum conversion
        final AngularUnit angularUnit = geographicCoordinateSystem.getAngularUnit();
        operations.add(angularUnit.fromRadiansOperation);
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

  @SuppressWarnings("unchecked")
  public synchronized <P extends CoordinatesProjection> P getCoordinatesProjection() {
    if (this.coordinatesProjection == null) {
      this.coordinatesProjection = this.coordinateOperationMethod.newCoordinatesProjection(this);
    }
    return (P)this.coordinatesProjection;
  }

  @Override
  public String getCoordinateSystemType() {
    return "Projected";
  }

  public double getDoubleParameter(final ParameterName key) {
    final Number value = getParameter(key);
    if (value == null) {
      return Double.NaN;
    } else {
      return value.doubleValue();
    }
  }

  public Ellipsoid getEllipsoid() {
    return this.geographicCoordinateSystem.getDatum().getEllipsoid();
  }

  public GeographicCoordinateSystem getGeographicCoordinateSystem() {
    return this.geographicCoordinateSystem;
  }

  public int getGeographicCoordinateSystemId() {
    return this.geographicCoordinateSystem.getCoordinateSystemId();
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

  @Override
  public LinearUnit getLinearUnit() {
    return this.linearUnit;
  }

  @SuppressWarnings("unchecked")
  public <V> V getParameter(final ParameterName key) {
    return (V)key.getValue(this.parameters);
  }

  public Map<ParameterName, Object> getParameters() {
    return this.parameters;
  }

  public Map<ParameterName, ParameterValue> getParameterValues() {
    return this.parameterValues;
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
      return new NoOpOperation();
    } else {
      return projection.getProjectOperation();
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
    if (this.geographicCoordinateSystem != null) {
      result = prime * result + this.geographicCoordinateSystem.hashCode();
    }
    if (this.coordinateOperationMethod != null) {
      result = prime * result + this.coordinateOperationMethod.hashCode();
    }
    for (final Entry<ParameterName, Object> entry : this.parameters.entrySet()) {
      final ParameterName key = entry.getKey();
      result = prime * result + key.hashCode();
      final Object value = entry.getValue();
      if (value != null) {
        result = prime * result + value.hashCode();
      }
    }
    if (this.linearUnit != null) {
      result = prime * result + this.linearUnit.hashCode();
    }
    return result;
  }

  @Override
  protected BoundingBox newAreaBoundingBox() {
    final GeometryFactory geographicGeometryFactory = this.geographicCoordinateSystem
      .getGeometryFactory();
    BoundingBox boundingBox;
    final Area area = getArea();
    if (area == null) {
      boundingBox = geographicGeometryFactory.newBoundingBox(-180, -90, 180, 90);
    } else {
      final BoundingBox latLonBounds = area.getLatLonBounds();
      boundingBox = latLonBounds.convert(geographicGeometryFactory);
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    return boundingBox.convert(geometryFactory);
  }
}
