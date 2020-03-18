package com.revolsys.swing.map.overlay;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.jeometry.common.awt.WebColors;
import org.jeometry.common.data.type.DataType;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.graph.RecordGraph;
import com.revolsys.geometry.graph.algorithm.ShortestPath;
import com.revolsys.geometry.graph.linemerge.LineMerger;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineCap;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.record.Record;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.events.KeyEvents;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.LayerRecordMenu;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.marker.MarkerRenderer;
import com.revolsys.swing.map.layer.record.table.model.RecordLayerTableModel;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;
import com.revolsys.swing.parallel.Invoke;

public class ShortestPathOverlay extends AbstractOverlay {

  private static final Cursor CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

  private static final long serialVersionUID = 1L;

  public static final String SHORTEST_PATH = "Shortest Path";

  private static final GeometryStyle STYLE_LINE = GeometryStyle.line(WebColors.DarkMagenta, 2);

  private static final GeometryStyle STYLE_LINE_HIGHLIGHT = GeometryStyle.line(WebColors.Violet, 12)
    .setLineCap(LineCap.BUTT);

  private static final GeometryStyle STYLE_LINE_HIGHLIGHT1 = GeometryStyle.line(WebColors.Magenta,
    12);

  private static final GeometryStyle STYLE_LINE_HIGHLIGHT2 = GeometryStyle.line(WebColors.DeepPink,
    12);

  private static final MarkerStyle STYLE_VERTICES = MarkerStyle.marker("circle", 8,
    WebColors.DarkMagenta, 1, WebColors.Pink);

  private static ShortestPathOverlay getOverlay(final AbstractRecordLayer layer) {
    final MapPanel map = layer.getMapPanel();
    final ShortestPathOverlay routingOverlay = map.getMapOverlay(ShortestPathOverlay.class);
    return routingOverlay;
  }

  public static void initMenuItems(final AbstractRecordLayer layer, final LayerRecordMenu menu) {
    if (isLayerApplicable(layer)) {

      menu.addMenuItem("shortestPath", "Shortest Path From Record", "route_from",
        (final LayerRecord record) -> {
          final ShortestPathOverlay routingOverlay = getOverlay(layer);
          final int updateIndex = routingOverlay.setRecord1Index.incrementAndGet();
          routingOverlay.setRecord1(record, updateIndex);
        })//
        .setAcceleratorAltKey(KeyEvent.VK_1);

      final Predicate<LayerRecord> pathMode = record -> {
        final ShortestPathOverlay routingOverlay = getOverlay(layer);
        return routingOverlay.isHasRecord1();
      };

      menu.addMenuItem("shortestPath", "Shortest Path To Record", "route_to", pathMode,
        (final LayerRecord record) -> {
          final ShortestPathOverlay routingOverlay = getOverlay(layer);
          final int updateIndex = routingOverlay.setRecord2Index.incrementAndGet();
          routingOverlay.setRecord2(record, updateIndex);
        })//
        .setAcceleratorAltKey(KeyEvent.VK_2);
    }
  }

  private static boolean isLayerApplicable(final AbstractRecordLayer layer) {
    final DataType geometryType = layer.getGeometryType();
    return geometryType == GeometryDataTypes.LINE_STRING
      || geometryType == GeometryDataTypes.MULTI_LINE_STRING;
  }

  private AbstractRecordLayer layer;

  private LayerRecord record1;

  private LayerRecord record2;

  private List<LayerRecord> records = Collections.emptyList();

  private Lineal mergedLine;

  private Punctual vertices;

  private final AtomicInteger setRecord1Index = new AtomicInteger();

  private final AtomicInteger setRecord2Index = new AtomicInteger();

  public ShortestPathOverlay(final MapPanel map) {
    super(map);
    addOverlayAction( //
      SHORTEST_PATH, //
      CURSOR, //
      ZoomOverlay.ACTION_PAN, //
      ZoomOverlay.ACTION_ZOOM, //
      ZoomOverlay.ACTION_ZOOM_BOX //
    );
  }

  public void actionAddToSelectRecords() {
    this.layer.addSelectedRecords(this.records);
    cancel();
  }

  public void actionSelectRecords() {
    this.layer.setSelectedRecords(this.records);
    this.layer.showRecordsTable(RecordLayerTableModel.MODE_RECORDS_SELECTED, true);
    cancel();
  }

  public void actionZoomToRecords() {
    final BoundingBox boundingBox = BoundingBox.bboxNew(this.records);
    final MapPanel map = getMap();
    map.zoomToBoundingBox(boundingBox);
  }

  private void addGeometry(final List<LineString> lines, final Set<Point> points,
    final Geometry geometry) {
    if (geometry instanceof Lineal) {
      addGeometry(lines, points, (Lineal)geometry);
    } else if (geometry instanceof GeometryCollection) {
      for (final Geometry part : geometry.geometries()) {
        addGeometry(lines, points, part);
      }
    }
  }

  private void addGeometry(final List<LineString> lines, final Set<Point> points,
    final Lineal lineal) {
    for (final LineString line : lineal.lineStrings()) {
      lines.add(line);
      points.add(line.getFromPoint());
      points.add(line.getToPoint());
    }
  }

  @Override
  public void cancel() {
    modeClear();
  }

  @Override
  protected void canelMenuDo() {
    if (!isOverlayAction(SHORTEST_PATH)) {
      super.canelMenuDo();
    }
  }

  public List<LayerRecord> getAllRecords() {
    return this.records;
  }

  private LayerRecord getCloseRecord() {
    final AbstractRecordLayer layer = this.layer;
    if (layer == null) {
      final MapPanel map = getMap();
      for (final CloseLocation location : map.getCloseSelectedLocations()) {
        final LayerRecord record = location.getRecord();
        if (record != null) {
          final Geometry geometry = record.getGeometry();
          if (geometry != null && !geometry.isEmpty()) {
            if (geometry instanceof Lineal) {
              return record;
            }
          }
        }
      }
      final BoundingBox hotspotBoundingBox = getHotspotBoundingBox();
      return getRecord(map.getProject(), hotspotBoundingBox);
    } else {
      final BoundingBox hotspotBoundingBox = getHotspotBoundingBox();
      return getRecord(this.layer, hotspotBoundingBox);
    }
  }

  public AbstractRecordLayer getLayer() {
    return this.layer;
  }

  private LayerRecord getRecord(final AbstractRecordLayer layer, final BoundingBox boundingBox) {
    for (final LayerRecord record : layer.getRecords(boundingBox)) {
      final Geometry geometry = record.getGeometry();
      if (geometry != null && !geometry.isEmpty()) {
        if (geometry instanceof Lineal) {
          return record;
        }
      }
    }
    return null;
  }

  private LayerRecord getRecord(final LayerGroup layerGroup, final BoundingBox boundingBox) {
    final double scale = getViewportScale();
    if (layerGroup.isVisible(scale)) {
      for (final Layer layer : layerGroup) {
        if (layer.isVisible(scale)) {
          if (layer instanceof LayerGroup) {
            final LayerGroup childGroup = (LayerGroup)layer;
            final LayerRecord record = getRecord(childGroup, boundingBox);
            if (record != null) {
              return record;
            }
          } else if (layer instanceof AbstractRecordLayer) {
            final AbstractRecordLayer recordLayer = (AbstractRecordLayer)layer;
            if (isLayerApplicable(recordLayer)) {
              final LayerRecord record = getRecord(recordLayer, boundingBox);
              if (record != null) {
                return record;
              }
            }
          }
        }
      }
    }
    return null;
  }

  public boolean isHasRecord1() {
    return this.record1 != null;
  }

  @Override
  public void keyPressed(final KeyEvent event) {
    final int keyCode = event.getKeyCode();
    if (keyCode == KeyEvent.VK_ESCAPE) {
      if (isOverlayAction(SHORTEST_PATH)) {
        cancel();
      }

    } else if (KeyEvents.altKey(event, KeyEvent.VK_1)) {
      final int updateIndex = this.setRecord1Index.incrementAndGet();
      Invoke.background("Set Record 1", this::getCloseRecord,
        record -> setRecord1(record, updateIndex));

    } else if (KeyEvents.altKey(event, KeyEvent.VK_2)) {
      if (this.layer == null) {
        SwingUtil.beep();
      } else {
        final int updateIndex = this.setRecord2Index.incrementAndGet();
        Invoke.background("Set Record 2", this::getCloseRecord,
          record -> setRecord2(record, updateIndex));
      }
    } else if (!this.records.isEmpty()) {
      if (KeyEvents.altKey(event, KeyEvent.VK_A)) {
        actionAddToSelectRecords();
      } else if (KeyEvents.altKey(event, KeyEvent.VK_S)) {
        actionSelectRecords();
      } else if (KeyEvents.altKey(event, KeyEvent.VK_Z)) {
        actionZoomToRecords();
      }
    }
  }

  private void modeClear() {
    clearOverlayAction(SHORTEST_PATH);
    this.layer = null;
    this.record1 = null;
    this.record2 = null;
    this.records.clear();
    this.mergedLine = null;
    this.vertices = null;
    repaint();
  }

  private boolean modePopupMenu(final MouseEvent event) {
    if (event.isPopupTrigger() && this.layer != null) {
      final LayerRecord record = getCloseRecord();
      if (showMenu(record, event)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void mouseClicked(final MouseEvent event) {

  }

  @Override
  public void mouseDragged(final MouseEvent event) {

  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    if (modePopupMenu(event)) {
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (modePopupMenu(event)) {
    }
  }

  @Override
  protected void paintComponent(final Graphics2DViewRenderer view, final Graphics2D graphics) {
    if (this.layer != null) {
      view.drawGeometry(this.mergedLine, STYLE_LINE_HIGHLIGHT);
      if (this.record2 != null) {
        view.drawGeometryOutline(STYLE_LINE_HIGHLIGHT2, this.record2.getGeometry());
      }
      if (this.record1 != null) {
        view.drawGeometryOutline(STYLE_LINE_HIGHLIGHT1, this.record1.getGeometry());
      }
      view.drawGeometry(this.mergedLine, STYLE_LINE);

      try (
        MarkerRenderer renderer = STYLE_VERTICES.newMarkerRenderer(view)) {
        renderer.renderMarkerVertices(this.vertices);
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getPropertyName();
    if (propertyName.equals("overlayAction")) {
      final MapPanel map = getMap();
      if (!map.hasOverlayAction(SHORTEST_PATH)) {
        cancel();
      }
    }
  }

  public void setRecord1(final LayerRecord record1, final int updateIndex) {
    if (record1 == null) {
      SwingUtil.beep();
    } else if (updateIndex == this.setRecord1Index.get()) {
      final Geometry geometry1 = record1.getGeometry();
      if (geometry1 instanceof Lineal) {
        getMap().clearToolTipText();
        setOverlayActionClearOthers(SHORTEST_PATH);
        this.layer = record1.getLayer();
        this.record1 = record1;
        updateShortestPath();
      } else {
        Toolkit.getDefaultToolkit().beep();
      }
    }
  }

  public void setRecord2(final LayerRecord record2, final int updateIndex) {
    if (record2 == null) {
      SwingUtil.beep();
    } else if (updateIndex == this.setRecord2Index.get()) {
      final Geometry geometry2 = record2.getGeometry();
      if (geometry2 instanceof Lineal) {
        this.record2 = record2;
        updateShortestPath();
      } else {
        Toolkit.getDefaultToolkit().beep();
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void updateShortestPath() {
    final LayerRecord record1 = this.record1;
    final LayerRecord record2 = this.record2;
    final AbstractRecordLayer layer = this.layer;
    final MapPanel map = getMap();
    final BoundingBox viewBoundingBox = getViewport().getBoundingBox();
    Invoke.background("Calculate Shortest Path", () -> {
      final List<LayerRecord> records = new ArrayList<>();
      records.add(record1);
      final Geometry geometry1 = record1.getGeometry();

      final List<LineString> lines = new ArrayList<>();
      final Set<Point> points = new HashSet<>();
      addGeometry(lines, points, geometry1);

      if (record2 != null && !record1.isSame(record2)) {
        final Geometry geometry2 = record2.getGeometry();
        final BoundingBox boundingBox = viewBoundingBox.bboxEditor()//
          .addBbox(record1) //
          .addBbox(record2) //
        ;
        final List<LayerRecord> viewRecords = layer.getRecords(boundingBox);
        final RecordGraph graph = new RecordGraph(viewRecords);
        final Node<Record> fromNode = graph.getNode(geometry1.getPoint());
        final ShortestPath<Record> paths = new ShortestPath<>(graph, fromNode);
        final Node<Record> toNode = graph.getNode(geometry2.getPoint());
        final List<Edge<Record>> path = paths.getPath(toNode);
        for (final Edge<Record> edge : path) {
          final LayerRecord record = (LayerRecord)edge.getObject();
          if (!record1.isSame(record) && !record2.isSame(record)) {
            if (!records.contains(record)) {
              records.add(record);
              final Geometry geometry = record.getGeometry();
              addGeometry(lines, points, geometry);
            }
          }
        }
        records.add(record2);
        addGeometry(lines, points, geometry2);
      }

      final GeometryFactory geometryFactory = layer.getGeometryFactory();
      final Lineal mergedLine = geometryFactory.lineal(LineMerger.merge(lines));
      final Punctual vertices = geometryFactory.punctual(points);

      return Lists.newArray(records, mergedLine, vertices);
    }, result -> {
      if (record1 == this.record1 && record2 == this.record2) {
        this.records = (List<LayerRecord>)result.get(0);
        this.mergedLine = (Lineal)result.get(1);
        this.vertices = (Punctual)result.get(2);
        if (record2 == null) {
          map.setMessage(SHORTEST_PATH, "Select Shortest Path to Record", WebColors.Orange);
        } else if (record1.isSame(record2)) {
          map.setMessage(SHORTEST_PATH, "From/to records are the same", WebColors.Orange);
        } else if (this.mergedLine.isGeometryCollection()) {
          map.setMessage(SHORTEST_PATH, "No Shortest Path Found", WebColors.Red);
          SwingUtil.beep();
        } else {
          map.setMessage(SHORTEST_PATH, this.records.size() + " records in shortest path",
            WebColors.Green);
        }
      }
      repaint();
    });

  }
}
