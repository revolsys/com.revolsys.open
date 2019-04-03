package com.revolsys.swing.tree.node.coordinatesystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;
import org.jeometry.coordinatesystem.model.CoordinateOperationMethod;
import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.GeographicCoordinateSystem;
import org.jeometry.coordinatesystem.model.ProjectedCoordinateSystem;
import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;

import com.revolsys.collection.map.Maps;

public class CoordinateSystemTreeTableModel extends AbstractTreeTableModel {
  private static final Object ROOT = new Object();

  private final List<GeographicCoordinateSystem> geographicCoordinateSystems;

  private final Map<CoordinateOperationMethod, List<ProjectedCoordinateSystem>> projectedCoordinateSystemsByProjection = new TreeMap<>();

  private final List<CoordinateOperationMethod> coordinateOperationMethods = new ArrayList<>();

  public CoordinateSystemTreeTableModel() {
    super(ROOT);
    this.geographicCoordinateSystems = EpsgCoordinateSystems.getGeographicCoordinateSystems();

    for (final ProjectedCoordinateSystem projectedCoordinateSystem : EpsgCoordinateSystems
      .getProjectedCoordinateSystems()) {
      final CoordinateOperationMethod coordinateOperationMethod = projectedCoordinateSystem
        .getCoordinateOperationMethod();
      Maps.addToList(this.projectedCoordinateSystemsByProjection, coordinateOperationMethod,
        projectedCoordinateSystem);
    }
    this.coordinateOperationMethods.addAll(this.projectedCoordinateSystemsByProjection.keySet());
  }

  @Override
  public Object getChild(final Object parent, final int index) {
    if (parent == ROOT) {
      switch (index) {
        case 0:
          return this.geographicCoordinateSystems;
        case 1:
          return this.coordinateOperationMethods;
        default:
          return null;
      }
    } else if (parent == this.geographicCoordinateSystems) {
      return this.geographicCoordinateSystems.get(index);
    } else if (parent == this.coordinateOperationMethods) {
      return this.coordinateOperationMethods.get(index);
    } else if (parent instanceof CoordinateOperationMethod) {
      final CoordinateOperationMethod coordinateOperationMethod = (CoordinateOperationMethod)parent;
      final List<ProjectedCoordinateSystem> projectedCoordinateSystems = this.projectedCoordinateSystemsByProjection
        .get(coordinateOperationMethod);
      if (projectedCoordinateSystems == null) {
        return null;
      } else {
        return projectedCoordinateSystems.get(index);
      }
    } else {
      return null;
    }
  }

  @Override
  public int getChildCount(final Object parent) {
    if (parent == ROOT) {
      return 2;
    } else if (parent == this.geographicCoordinateSystems) {
      return this.geographicCoordinateSystems.size();
    } else if (parent == this.coordinateOperationMethods) {
      return this.coordinateOperationMethods.size();
    } else if (parent instanceof CoordinateOperationMethod) {
      final CoordinateOperationMethod coordinateOperationMethod = (CoordinateOperationMethod)parent;
      final List<ProjectedCoordinateSystem> projectedCoordinateSystems = this.projectedCoordinateSystemsByProjection
        .get(coordinateOperationMethod);
      if (projectedCoordinateSystems == null) {
        return 0;
      } else {
        return projectedCoordinateSystems.size();
      }
    } else {
      return 0;
    }
  }

  @Override
  public Class<?> getColumnClass(final int column) {
    switch (column) {
      case 0:
        return String.class;
      case 1:
        return String.class;
      default:
        return Object.class;
    }
  }

  @Override
  public int getColumnCount() {
    return 2;
  }

  @Override
  public String getColumnName(final int column) {
    switch (column) {
      case 0:
        return "Name";
      case 1:
        return "ID";
      default:
        return "";
    }
  }

  @Override
  public int getIndexOfChild(final Object parent, final Object child) {
    if (parent == ROOT) {
      if (child == this.geographicCoordinateSystems) {
        return 0;
      } else if (child == this.projectedCoordinateSystemsByProjection) {
        return 1;
      } else {
        return -1;
      }
    } else if (parent == this.geographicCoordinateSystems) {
      return this.geographicCoordinateSystems.indexOf(child);
    } else if (parent == this.coordinateOperationMethods) {
      return this.coordinateOperationMethods.indexOf(child);
    } else if (parent instanceof CoordinateOperationMethod) {
      final CoordinateOperationMethod coordinateOperationMethod = (CoordinateOperationMethod)parent;
      final List<ProjectedCoordinateSystem> projectedCoordinateSystems = this.projectedCoordinateSystemsByProjection
        .get(coordinateOperationMethod);
      if (projectedCoordinateSystems == null) {
        return -1;
      } else {
        return projectedCoordinateSystems.indexOf(child);
      }
    } else {
      return -1;
    }
  }

  @Override
  public Object getValueAt(final Object node, final int column) {
    if (node == ROOT) {
      if (column == 0) {
        return "Coordinate Systems";
      }
    } else if (node == this.geographicCoordinateSystems) {
      if (column == 0) {
        return "Geographic Point Systems";
      }
    } else if (node == this.coordinateOperationMethods) {
      if (column == 0) {
        return "Projected Point Systems";
      }
    } else if (node instanceof CoordinateOperationMethod) {
      final CoordinateOperationMethod coordinateOperationMethod = (CoordinateOperationMethod)node;
      if (column == 0 && coordinateOperationMethod != null) {
        return coordinateOperationMethod.getName().replaceAll("_", " ");
      }
    } else if (node instanceof CoordinateSystem) {
      final CoordinateSystem coordinateSystem = (CoordinateSystem)node;
      if (column == 0) {
        final String name = coordinateSystem.getCoordinateSystemName();
        return name;
      } else if (column == 1) {
        return coordinateSystem.getHorizontalCoordinateSystemId();
      } else {
        return null;
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isLeaf(final Object node) {
    if (node instanceof CoordinateSystem) {
      return true;
    } else {
      return false;
    }
  }
}
