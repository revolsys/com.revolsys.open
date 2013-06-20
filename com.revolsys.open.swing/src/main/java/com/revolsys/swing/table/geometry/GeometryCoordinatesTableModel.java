package com.revolsys.swing.table.geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.geometry.GeometryCollection;
import com.revolsys.gis.model.geometry.util.GeometryEditUtil;
import com.revolsys.swing.table.TablePanel;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryCoordinatesTableModel extends AbstractTableModel {
  private static final long serialVersionUID = 1L;

  public static int[] getEventRowObject(final TablePanel panel) {
    final GeometryCoordinatesTableModel model = panel.getTableModel();
    final int row = panel.getEventRow();
    final int[] object = model.getVertexIndex(row);
    return object;
  }

  private List<String> axisNames = Arrays.asList("#", "X", "Y", "Z", "M");

  private Geometry geometry;

  private GeometryFactory geometryFactory;

  private int columnCount = 0;

  private Map<int[], Coordinates> vertexIndexMap = Collections.emptyMap();

  private List<int[]> vertexIndices = Collections.emptyList();

  private int numAxis;

  private int numIndexItems;

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
    return columnCount;
  }

  @Override
  public String getColumnName(final int column) {
    return axisNames.get(column);
  }

  private Coordinates getCoordinates(final int rowIndex) {
    final int[] vertexIndex = getVertexIndex(rowIndex);
    if (vertexIndex == null) {
      return null;
    } else {
      return vertexIndexMap.get(vertexIndex);
    }
  }

  public Geometry getGeometry() {
    return geometry;
  }

  public int getNumAxis() {
    return numAxis;
  }

  public int getNumIndexItems() {
    return numIndexItems;
  }

  @Override
  public int getRowCount() {
    return vertexIndices.size();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    if (columnIndex < numIndexItems) {
      final int[] vertexIndex = getVertexIndex(rowIndex);
      if (vertexIndex.length == numIndexItems) {
        return vertexIndex[columnIndex];
      } else {
        if (columnIndex == 0) {
          return vertexIndex[0];
        } else if (columnIndex == 2) {
          return vertexIndex[1];
        } else {
          return 0;
        }
      }
    } else {
      final int axisIndex = columnIndex - numIndexItems;
      final Coordinates point = getCoordinates(rowIndex);
      if (point != null) {
        final double coordinate = point.getValue(axisIndex);
        if (!Double.isNaN(coordinate)) {
          return coordinate;
        }
      }
      return 0;
    }
  }

  public int[] getVertexIndex(final int rowIndex) {
    return vertexIndices.get(rowIndex);
  }

  public void setGeometry(final Geometry geometry) {
    this.geometry = geometry;
    if (geometry == null) {
      this.geometryFactory = GeometryFactory.getFactory();
      this.vertexIndexMap = Collections.emptyMap();
      this.vertexIndices = Collections.emptyList();
    } else {
      this.vertexIndexMap = GeometryEditUtil.getIndexOfVertices(geometry);
      this.vertexIndices = new ArrayList<int[]>(vertexIndexMap.keySet());
    }
    numAxis = geometryFactory.getNumAxis();
    if (geometry instanceof Polygon) {
      axisNames = Arrays.asList("R", "#", "X", "Y", "Z", "M");
      numIndexItems = 2;
    } else if (geometry instanceof MultiPoint) {
      axisNames = Arrays.asList("P", "#", "X", "Y", "Z", "M");
      numIndexItems = 2;
    } else if (geometry instanceof MultiLineString) {
      axisNames = Arrays.asList("P", "#", "X", "Y", "Z", "M");
      numIndexItems = 2;
    } else if (geometry instanceof MultiPolygon) {
      axisNames = Arrays.asList("P", "R", "#", "X", "Y", "Z", "M");
      numIndexItems = 3;
    } else if (geometry instanceof GeometryCollection) {
      axisNames = Arrays.asList("P", "R", "#", "X", "Y", "Z", "M");
      numIndexItems = 3;
    } else {
      axisNames = Arrays.asList("#", "X", "Y", "Z", "M");
      numIndexItems = 1;
    }
    columnCount = numAxis + numIndexItems;
    fireTableStructureChanged();
  }

}
