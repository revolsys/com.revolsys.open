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

    "X", "Y", "Z", "M"
  };

  private Geometry geometry;

  private GeometryFactory geometryFactory;

  private List<Coordinates> coordinatesList;;

  public GeometryCoordinatesTableModel() {
    this(null);
  }

  public GeometryCoordinatesTableModel(Geometry geometry) {
    setGeometry(geometry);
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return Double.class;
  }

  @Override
  public String getColumnName(int column) {
    return AXIS_NAMES[column];
  }

  public void setGeometry(Geometry geometry) {
    this.geometry = geometry;
    coordinatesList = new ArrayList<Coordinates>();
    if (geometry == null) {
      this.geometryFactory = GeometryFactory.getFactory();
    } else {
      this.geometryFactory = GeometryFactory.getFactory(geometry);
      List<CoordinatesList> pointsList = CoordinatesListUtil.getAll(geometry);
      boolean first = true;
      for (CoordinatesList points : pointsList) {
        if (first) {
          first = false;
        } else {
          coordinatesList.add(null);
        }
        for (Coordinates point : points) {
          coordinatesList.add(point);
        }
      }
    }
    fireTableStructureChanged();
  }

  public Geometry getGeometry() {
    return geometry;
  }

  @Override
  public int getRowCount() {
    return coordinatesList.size();
  }

  @Override
  public int getColumnCount() {
    return geometryFactory.getNumAxis();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    Coordinates coordinates = getCoordinates(rowIndex);
    if (coordinates == null) {
      return "-";
    } else {
      return coordinates.getValue(columnIndex);
    }
  }

  private Coordinates getCoordinates(int rowIndex) {
    return coordinatesList.get(rowIndex);
  }

}
