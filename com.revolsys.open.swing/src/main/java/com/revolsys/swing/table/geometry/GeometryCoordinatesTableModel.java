package com.revolsys.swing.table.geometry;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.swing.map.form.LayerRecordForm;
import com.revolsys.swing.table.AbstractTableModel;
import com.revolsys.swing.table.TablePanel;

public class GeometryCoordinatesTableModel extends AbstractTableModel {
  public static int[] getEventRowObject(final TablePanel panel) {
    final GeometryCoordinatesTableModel model = panel.getTableModel();
    final int row = panel.getEventRow();
    final int[] object = model.getVertexIndex(row);
    return object;
  }

  public static Map<int[], Point> getIndexOfVertices(final Geometry geometry) {
    final Map<int[], Point> pointIndexes = new LinkedHashMap<int[], Point>();
    if (geometry == null || geometry.isEmpty()) {
    } else {
      for (final Vertex vertex : geometry.vertices()) {
        final int[] vertexId = vertex.getVertexId();
        final Vertex clone = vertex.clone();
        pointIndexes.put(vertexId, clone);
      }
    }
    return pointIndexes;
  }

  private static final long serialVersionUID = 1L;

  private List<String> axisNames = Arrays.asList("#", "X", "Y", "Z", "M");

  private Geometry geometry;

  private GeometryFactory geometryFactory;

  private int columnCount = 0;

  private Map<int[], Point> vertexIndexMap = Collections.emptyMap();

  private List<int[]> vertexIndices = Collections.emptyList();

  private int axisCount;

  private int numIndexItems;

  private Reference<LayerRecordForm> form;

  private int vertexIndexColumn;

  private int segmentIndexColumn;

  public GeometryCoordinatesTableModel() {
    this(null);
  }

  public GeometryCoordinatesTableModel(final Geometry geometry) {
    setGeometry(geometry);
  }

  public int getAxisCount() {
    return this.axisCount;
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    return Double.class;
  }

  @Override
  public int getColumnCount() {
    return this.columnCount;
  }

  @Override
  public String getColumnName(final int column) {
    return this.axisNames.get(column);
  }

  private Point getCoordinates(final int rowIndex) {
    final int[] vertexIndex = getVertexIndex(rowIndex);
    if (vertexIndex == null) {
      return null;
    } else {
      return this.vertexIndexMap.get(vertexIndex);
    }
  }

  public LayerRecordForm getForm() {
    return this.form.get();
  }

  public Geometry getGeometry() {
    return this.geometry;
  }

  public int getNumIndexItems() {
    return this.numIndexItems;
  }

  @Override
  public int getRowCount() {
    return this.vertexIndices.size();
  }

  public int getSegmentIndexColumn() {
    return this.segmentIndexColumn;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final int[] vertexIndex = getVertexIndex(rowIndex);
    if (columnIndex < vertexIndex.length) {
      return vertexIndex[columnIndex];
    } else if (columnIndex == this.segmentIndexColumn) {
      return vertexIndex[vertexIndex.length - 1] + " \u2193";
    } else {
      final int axisIndex = columnIndex - this.numIndexItems;
      final Point point = getCoordinates(rowIndex);
      if (point != null) {
        final double coordinate = point.getCoordinate(axisIndex);
        if (!Double.isNaN(coordinate)) {
          return coordinate;
        }
      }
      return 0;
    }
  }

  public int[] getVertexIndex(final int rowIndex) {
    if (rowIndex >= 0 && rowIndex < getRowCount()) {
      return this.vertexIndices.get(rowIndex);
    } else {
      return new int[0];
    }
  }

  public int getVertexIndexColumn() {
    return this.vertexIndexColumn;
  }

  public void setForm(final LayerRecordForm form) {
    this.form = new WeakReference<LayerRecordForm>(form);
  }

  public void setGeometry(final Geometry geometry) {
    this.geometry = geometry;
    if (geometry == null) {
      this.geometryFactory = GeometryFactory.floating3();
      this.vertexIndexMap = Collections.emptyMap();
      this.vertexIndices = Collections.emptyList();
    } else {
      this.vertexIndexMap = getIndexOfVertices(geometry);
      this.vertexIndices = new ArrayList<int[]>(this.vertexIndexMap.keySet());
    }
    this.axisCount = this.geometryFactory.getAxisCount();
    this.axisNames = new ArrayList<String>();
    if (geometry instanceof Polygon) {
      this.axisNames.add("R");
    } else if (geometry instanceof MultiPoint) {
      this.axisNames.add("P");
    } else if (geometry instanceof MultiLineString) {
      this.axisNames.add("P");
    } else if (geometry instanceof MultiPolygon) {
      this.axisNames.add("P");
      this.axisNames.add("R");
    } else if (geometry instanceof GeometryCollection) {
      this.axisNames.add("P");
      this.axisNames.add("R");
    } else {
    }
    this.vertexIndexColumn = this.axisNames.size();
    this.axisNames.add("#");
    this.segmentIndexColumn = this.axisNames.size();
    this.axisNames.add("S #");
    this.numIndexItems = this.axisNames.size();
    this.axisNames.add("X");
    this.axisNames.add("Y");
    if (this.axisCount > 2) {
      this.axisNames.add("Z");
    }
    if (this.axisCount > 3) {
      this.axisNames.add("M");
    }
    this.columnCount = this.axisNames.size();
    fireTableStructureChanged();
  }

  public void setVertexIndexColumn(final int vertexIndexColumn) {
    this.vertexIndexColumn = vertexIndexColumn;
  }
}
