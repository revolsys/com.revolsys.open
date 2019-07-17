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

import org.jeometry.common.awt.WebColors;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.graph.RecordGraph;
import com.revolsys.geometry.graph.algorithm.ShortestPath;
import com.revolsys.geometry.graph.linemerge.LineMerger;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryCollection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineCap;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.record.Record;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.LayerRecordMenu;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.marker.MarkerRenderer;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;
import com.revolsys.swing.parallel.Invoke;

public class ShortestRouteOverlay extends AbstractOverlay {

  private static final Cursor CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

  private static final long serialVersionUID = 1L;

  public static final String SHORTEST_ROUTE = "Shortest Route";

  private static final GeometryStyle STYLE_LINE = GeometryStyle.line(WebColors.DarkMagenta, 2);

  private static final GeometryStyle STYLE_LINE_HIGHLIGHT = GeometryStyle.line(WebColors.Violet, 12)
    .setLineCap(LineCap.BUTT);

  private static final GeometryStyle STYLE_LINE_HIGHLIGHT1 = GeometryStyle.line(WebColors.Magenta,
    12);

  private static final GeometryStyle STYLE_LINE_HIGHLIGHT2 = GeometryStyle.line(WebColors.DeepPink,
    12);

  private static final MarkerStyle STYLE_VERTICES = MarkerStyle.marker("circle", 8,
    WebColors.DarkMagenta, 1, WebColors.Pink);

  private AbstractRecordLayer layer;

  private LayerRecord record1;

  private LayerRecord record2;

  private List<LayerRecord> records = Collections.emptyList();

  private Lineal mergedLine;

  private Punctual vertices;

  public ShortestRouteOverlay(final MapPanel map) {
    super(map);
    addOverlayAction( //
      SHORTEST_ROUTE, //
      CURSOR, //
      ZoomOverlay.ACTION_PAN, //
      ZoomOverlay.ACTION_ZOOM, //
      ZoomOverlay.ACTION_ZOOM_BOX //
    );
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

  private void cancel() {
    modeClear();
  }

  public List<LayerRecord> getAllRecords() {
    final List<LayerRecord> allRecords = new ArrayList<>();
    allRecords.add(this.record1);
    allRecords.addAll(this.records);
    allRecords.add(this.record2);
    return allRecords;
  }

  public AbstractRecordLayer getLayer() {
    return this.layer;
  }

  public boolean isHasRecord1() {
    return this.record1 != null;
  }

  @Override
  public void keyPressed(final KeyEvent event) {
    final int keyCode = event.getKeyCode();
    if (keyCode == KeyEvent.VK_ESCAPE) {
      if (isOverlayAction(SHORTEST_ROUTE)) {
        cancel();
      }
    }
  }

  private void modeClear() {
    clearOverlayAction(SHORTEST_ROUTE);
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
      for (final LayerRecord record : this.layer.getRecords(getHotspotBoundingBox())) {
        final LayerRecordMenu menu = record.getMenu();
        menu.showMenu(record, event);
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
      if (!map.hasOverlayAction(SHORTEST_ROUTE)) {
        cancel();
      }
    }
  }

  public void setRecord1(final LayerRecord record1) {
    final Geometry geometry1 = record1.getGeometry();
    if (geometry1 instanceof Lineal) {
      clearOverlayActions();
      setOverlayAction(SHORTEST_ROUTE);
      this.layer = record1.getLayer();
      this.record1 = record1;
      updateRoute();
    } else {
      Toolkit.getDefaultToolkit().beep();
    }
  }

  public void setRecord2(final LayerRecord record2) {
    final Geometry geometry2 = record2.getGeometry();
    if (geometry2 instanceof Lineal) {
      this.record2 = record2;
      updateRoute();
    } else {
      Toolkit.getDefaultToolkit().beep();
    }
  }

  @SuppressWarnings("unchecked")
  private void updateRoute() {
    final LayerRecord record1 = this.record1;
    final LayerRecord record2 = this.record2;
    final AbstractRecordLayer layer = this.layer;
    final BoundingBox viewBoundingBox = getViewport().getBoundingBox();
    if (record1 != null && record2 != null) {
      Invoke.background("Calculate Route", () -> {
        final List<LayerRecord> records = new ArrayList<>();
        final Geometry geometry1 = record1.getGeometry();
        final Geometry geometry2 = record2.getGeometry();
        final BoundingBox boundingBox = viewBoundingBox.bboxEditor()//
          .addBbox(record1) //
          .addBbox(record2) //
        ;

        final List<LineString> lines = new ArrayList<>();
        final Set<Point> points = new HashSet<>();
        addGeometry(lines, points, geometry1);

        final List<LayerRecord> viewRecords = layer.getRecords(boundingBox);
        final RecordGraph graph = new RecordGraph(viewRecords);
        final Node<Record> fromNode = graph.getNode(geometry1.getPoint());
        final ShortestPath<Record> routes = new ShortestPath<>(graph, fromNode);
        final Node<Record> toNode = graph.getNode(geometry2.getPoint());
        final List<Edge<Record>> path = routes.getPath(toNode);
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

        addGeometry(lines, points, geometry2);

        final GeometryFactory geometryFactory = layer.getGeometryFactory();
        final Lineal mergedLine = geometryFactory.lineal(LineMerger.merge(lines));
        final Punctual vertices = geometryFactory.punctual(points);

        return Lists.newArray(records, mergedLine, vertices);
      }, result -> {
        if (record1 == this.record1 && record2 == this.record2) {
          this.records = (List<LayerRecord>)result.get(0);
          this.mergedLine = (Lineal)result.get(1);
          this.vertices = (Punctual)result.get(2);
        }
        repaint();
      });
    } else {
      this.records = Collections.emptyList();
    }
    repaint();

  }
}
