package com.revolsys.geometry.cs;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.beans.Classes;
import com.revolsys.geometry.cs.projection.ChainedCoordinatesOperation;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;

public abstract class AbstractHorizontalCoordinateSystem extends AbstractCoordinateSystem
  implements HorizontalCoordinateSystem {
  private static final long serialVersionUID = 1L;

  public AbstractHorizontalCoordinateSystem(final int id, final String name, final List<Axis> axis,
    final Area area, final boolean deprecated) {
    super(id, name, axis, area, deprecated);
  }

  public AbstractHorizontalCoordinateSystem(final int id, final String name, final List<Axis> axis,
    final Area area, final boolean deprecated, final Authority authority) {
    super(id, name, axis, area, deprecated, authority);
  }

  public AbstractHorizontalCoordinateSystem(final int id, final String name, final List<Axis> axis,
    final Authority authority) {
    super(id, name, axis, authority);
  }

  protected void addCoordinatesOperations(final List<CoordinatesOperation> operations,
    final GeographicCoordinateSystem coordinateSystem) {
    throw new IllegalArgumentException("Coordinate system type not supported\n"
      + Classes.className(coordinateSystem) + "\n" + coordinateSystem);
  }

  protected void addCoordinatesOperations(final List<CoordinatesOperation> operations,
    final ProjectedCoordinateSystem coordinateSystem) {
    throw new IllegalArgumentException("Coordinate system type not supported\n"
      + Classes.className(coordinateSystem) + "\n" + coordinateSystem);
  }

  @Override
  public CoordinatesOperation getCoordinatesOperation(final CoordinateSystem coordinateSystem) {
    if (coordinateSystem == null || this == coordinateSystem) {
      return null;
    } else {
      final List<CoordinatesOperation> operations = new ArrayList<>();
      if (coordinateSystem instanceof GeographicCoordinateSystem) {
        addCoordinatesOperations(operations, (GeographicCoordinateSystem)coordinateSystem);
      } else if (coordinateSystem instanceof ProjectedCoordinateSystem) {
        addCoordinatesOperations(operations, (ProjectedCoordinateSystem)coordinateSystem);
      } else {
        throw new IllegalArgumentException("Coordinate system type not supported\n"
          + Classes.className(coordinateSystem) + "\n" + coordinateSystem);
      }
      final int operationCount = operations.size();
      if (operationCount == 0) {
        return null;
      } else if (operationCount == 1) {
        return operations.get(0);
      } else {
        return new ChainedCoordinatesOperation(operations);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <C extends CoordinateSystem> C getHorizontalCoordinateSystem() {
    return (C)this;
  }
}
