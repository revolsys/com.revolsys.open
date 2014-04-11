package com.revolsys.swing.table.geometry;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import com.revolsys.gis.model.geometry.GeometryCollection;
import com.revolsys.gis.model.geometry.util.GeometryEditUtil;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.swing.map.form.DataObjectLayerForm;
import com.revolsys.swing.table.TablePanel;

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

  private com.revolsys.jts.geom.GeometryFactory geometryFactory;

  private int columnCount = 0;

  private Map<int[], Coordinates> vertexIndexMap = Collections.emptyMap();

  private List<int[]> vertexIndices = Collections.emptyList();

  private int numAxis;

  private int numIndexItems;

  private Reference<DataObjectLayerForm> form;

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
    return this.columnCount;
  }

  @Override
  public String getColumnName(final int column) {
    return this.axisNames.get(column);
  }

  private Coordinates getCoordinates(final int rowIndex) {
    final int[] vertexIndex = getVertexIndex(rowIndex);
    if (vertexIndex == null) {
      return null;
    } else {
      return this.vertexIndexMap.get(vertexIndex);
    }
  }

  public DataObjectLayerForm getForm() {
    return this.form.get();
  }

  public Geometry getGeometry() {
    return this.geometry;
  }

  public int getNumAxis() {
    return this.numAxis;
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
    return this.vertexIndices.get(rowIndex);
  }

  public int getVertexIndexColumn() {
    return this.vertexIndexColumn;
  }

  public void setForm(final DataObjectLayerForm form) {
    this.form = new WeakReference<DataObjectLayerForm>(form);
  }

  public void setGeometry(final Geometry geometry) {
    this.geometry = geometry;
    if (geometry == null) {
      this.geometryFactory = GeometryFactory.getFactory();
      this.vertexIndexMap = Collections.emptyMap();
      this.vertexIndices = Collections.emptyList();
    } else {
      this.vertexIndexMap = GeometryEditUtil.getIndexOfVertices(geometry);
      this.vertexIndices = new ArrayList<int[]>(this.vertexIndexMap.keySet());
    }
    this.numAxis = this.geometryFactory.getNumAxis();
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
    if (this.numAxis > 2) {
      this.axisNames.add("Z");
    }
    if (this.numAxis > 3) {
      this.axisNames.add("M");
    }
    this.columnCount = this.axisNames.size();
    fireTableStructureChanged();
  }

  public void setVertexIndexColumn(final int vertexIndexColumn) {
    this.vertexIndexColumn = vertexIndexColumn;
  }
}
