package com.revolsys.swing.map.tree.coordinatesystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.cs.Projection;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.util.CollectionUtil;

public class CoordinateSystemTreeTableModel extends AbstractTreeTableModel {
  private final List<GeographicCoordinateSystem> geographicCoordinateSystems;

  private final Map<Projection, List<ProjectedCoordinateSystem>> projectedCoordinateSystemsByProjection = new TreeMap<Projection, List<ProjectedCoordinateSystem>>();

  private final List<Projection> projections = new ArrayList<Projection>();

  private static final Object ROOT = new Object();

  public CoordinateSystemTreeTableModel() {
    super(ROOT);
    geographicCoordinateSystems = EpsgCoordinateSystems.getGeographicCoordinateSystems();

    for (final ProjectedCoordinateSystem projectedCoordinateSystem : EpsgCoordinateSystems.getProjectedCoordinateSystems()) {
      final Projection projection = projectedCoordinateSystem.getProjection();
      CollectionUtil.addToList(projectedCoordinateSystemsByProjection,
        projection, projectedCoordinateSystem);
    }
    this.projections.addAll(projectedCoordinateSystemsByProjection.keySet());
  }

  @Override
  public Object getChild(final Object parent, final int index) {
    if (parent == ROOT) {
      switch (index) {
        case 0:
          return geographicCoordinateSystems;
        case 1:
          return projections;
        default:
          return null;
      }
    } else if (parent == geographicCoordinateSystems) {
      return geographicCoordinateSystems.get(index);
    } else if (parent == projections) {
      return projections.get(index);
    } else if (parent instanceof Projection) {
      final Projection projection = (Projection)parent;
      final List<ProjectedCoordinateSystem> projectedCoordinateSystems = this.projectedCoordinateSystemsByProjection.get(projection);
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
    } else if (parent == geographicCoordinateSystems) {
      return geographicCoordinateSystems.size();
    } else if (parent == projections) {
      return projections.size();
    } else if (parent instanceof Projection) {
      final Projection projection = (Projection)parent;
      final List<ProjectedCoordinateSystem> projectedCoordinateSystems = this.projectedCoordinateSystemsByProjection.get(projection);
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
      if (child == geographicCoordinateSystems) {
        return 0;
      } else if (child == projectedCoordinateSystemsByProjection) {
        return 1;
      } else {
        return -1;
      }
    } else if (parent == geographicCoordinateSystems) {
      return geographicCoordinateSystems.indexOf(child);
    } else if (parent == projections) {
      return projections.indexOf(child);
    } else if (parent instanceof Projection) {
      final Projection projection = (Projection)parent;
      final List<ProjectedCoordinateSystem> projectedCoordinateSystems = this.projectedCoordinateSystemsByProjection.get(projection);
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
    } else if (node == geographicCoordinateSystems) {
      if (column == 0) {
        return "Geographic Coordinate Systems";
      }
    } else if (node == projections) {
      if (column == 0) {
        return "Projected Coordinate Systems";
      }
    } else if (node instanceof Projection) {
      final Projection projection = (Projection)node;
      if (column == 0 && projection != null) {
        return projection.getName().replaceAll("_", " ");
      }
    } else if (node instanceof CoordinateSystem) {
      final CoordinateSystem coordinateSystem = (CoordinateSystem)node;
      if (column == 0) {
        final String name = coordinateSystem.getName();
        return name;
      } else if (column == 1) {
        return coordinateSystem.getId();
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
