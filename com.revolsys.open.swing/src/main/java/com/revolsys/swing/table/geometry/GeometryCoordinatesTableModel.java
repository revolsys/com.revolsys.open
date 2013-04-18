package com.revolsys.swing.table.geometry;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryCoordinatesTableModel extends AbstractTableModel {
  private static final long serialVersionUID = 1L;

  private static final String[] AXIS_NAMES = {
    "#", "X", "Y", "Z", "M"
  };

  private Geometry geometry;

  private GeometryFactory geometryFactory;

  private List<Coordinates> coordinatesList;;

  public GeometryCoordinatesTableModel() {
    this(null);
  }

  public GeometryCoordinatesTableModel(final Geometry geometry) {
    setGeometry(geometry);
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    return Double.class;
  }

  @Override
  public int getColumnCount() {
    return geometryFactory.getNumAxis() + 1;
  }

  @Override
  public String getColumnName(final int column) {
    return AXIS_NAMES[column];
  }

  private Coordinates getCoordinates(final int rowIndex) {
    return coordinatesList.get(rowIndex);
  }

  public Geometry getGeometry() {
    return geometry;
  }

  @Override
  public int getRowCount() {
    return coordinatesList.size();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    if (columnIndex == 0) {
      return rowIndex;
    } else {
      final Coordinates coordinates = getCoordinates(rowIndex);
      if (coordinates == null) {
        return "-";
      } else {
        return coordinates.getValue(columnIndex - 1);
      }
    }
  }

  public void setGeometry(final Geometry geometry) {
    this.geometry = geometry;
    coordinatesList = new ArrayList<Coordinates>();
    if (geometry == null) {
      this.geometryFactory = GeometryFactory.getFactory();
    } else {
      this.geometryFactory = GeometryFactory.getFactory(geometry);
      final List<CoordinatesList> pointsList = CoordinatesListUtil.getAll(geometry);
      boolean first = true;
      for (final CoordinatesList points : pointsList) {
        if (first) {
          first = false;
        } else {
          coordinatesList.add(null);
        }
        for (final Coordinates point : points) {
          coordinatesList.add(point);
        }
      }
    }
    fireTableStructureChanged();
  }

}
