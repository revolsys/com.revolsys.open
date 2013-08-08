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
import com.revolsys.swing.map.form.DataObjectLayerForm;
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

  private DataObjectLayerForm form;

  private int vertexIndexColumn;

  private int segmentIndexColumn;

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

  public DataObjectLayerForm getForm() {
    return form;
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

  public int getSegmentIndexColumn() {
    return segmentIndexColumn;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final int[] vertexIndex = getVertexIndex(rowIndex);
    if (columnIndex < vertexIndex.length) {
      return vertexIndex[columnIndex];
    } else if (columnIndex == segmentIndexColumn) {
      return vertexIndex[vertexIndex.length - 1] + " \u2193";
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

  public int getVertexIndexColumn() {
    return vertexIndexColumn;
  }

  public void setForm(final DataObjectLayerForm form) {
    this.form = form;
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
    axisNames = new ArrayList<String>();
    if (geometry instanceof Polygon) {
      axisNames.add("R");
    } else if (geometry instanceof MultiPoint) {
      axisNames.add("P");
    } else if (geometry instanceof MultiLineString) {
      axisNames.add("P");
    } else if (geometry instanceof MultiPolygon) {
      axisNames.add("P");
      axisNames.add("R");
    } else if (geometry instanceof GeometryCollection) {
      axisNames.add("P");
      axisNames.add("R");
    } else {
    }
    vertexIndexColumn = axisNames.size();
    axisNames.add("#");
    segmentIndexColumn = axisNames.size();
    axisNames.add("S #");
    numIndexItems = axisNames.size();
    axisNames.add("X");
    axisNames.add("Y");
    if (numAxis > 2) {
      axisNames.add("Z");
    }
    if (numAxis > 3) {
      axisNames.add("M");
    }
    columnCount = axisNames.size();
    fireTableStructureChanged();
  }

  public void setVertexIndexColumn(final int vertexIndexColumn) {
    this.vertexIndexColumn = vertexIndexColumn;
  }
}
