package com.revolsys.swing.map.overlay;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.undo.UndoableEdit;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.algorithm.index.quadtree.GeometrySegmentQuadTree;
import com.revolsys.gis.algorithm.index.quadtree.GeometryVertexQuadTree;
import com.revolsys.gis.jts.GeometryEditUtil;
import com.revolsys.io.wkt.WktWriter;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.geom.segment.Segment;
import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.jts.geom.vertex.VertexIndexComparator;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.undo.SetObjectProperty;
import com.revolsys.util.CollectionUtil;

public class AbstractOverlay extends JComponent implements
  PropertyChangeListener, MouseListener, MouseMotionListener,
  MouseWheelListener, KeyListener {
  public static final Cursor CURSOR_LINE_ADD_NODE = SilkIconLoader.getCursor(
    "cursor_line_node_add", 8, 6);

  public static final Cursor CURSOR_LINE_SNAP = SilkIconLoader.getCursor(
    "cursor_line_snap", 8, 4);

  public static final Cursor CURSOR_NODE_EDIT = SilkIconLoader.getCursor(
    "cursor_node_edit", 8, 7);

  public static final Cursor CURSOR_NODE_SNAP = SilkIconLoader.getCursor(
    "cursor_node_snap", 8, 7);

  private static final VertexIndexComparator VERTEX_INDEX_COMPARATOR = new VertexIndexComparator();

  private static final long serialVersionUID = 1L;

  public static final GeometryStyle XOR_LINE_STYLE = GeometryStyle.line(
    new Color(0, 0, 255), 2);

  private GeometryFactory geometryFactory;

  private final int hotspotPixels = 6;

  private MapPanel map;

  private List<CloseLocation> mouseOverLocations = Collections.emptyList();

  private Project project;

  private java.awt.Point snapEventPoint;

  private Point snapPoint;

  private int snapPointIndex;

  private Map<Point, Set<CloseLocation>> snapPointLocationMap = Collections.emptyMap();

  private Viewport2D viewport;

  private Geometry xorGeometry;

  private Point snapCentre;

  private final List<Point> snapPoints = new ArrayList<Point>();

  protected AbstractOverlay(final MapPanel map) {
    this.map = map;
    this.viewport = map.getViewport();
    this.project = map.getProject();

    map.addMapOverlay(this);
  }

  protected void addUndo(final UndoableEdit edit) {
    this.map.addUndo(edit);
  }

  protected void appendLocations(final StringBuffer text, final String title,
    final Map<String, Set<CloseLocation>> vertexLocations) {
    if (!vertexLocations.isEmpty()) {
      text.append("<div style=\"border-bottom: solid black 1px; font-weight:bold;padding: 1px 3px 1px 3px\">");
      text.append(title);
      text.append("</div>");
      text.append("<div style=\"padding: 1px 3px 1px 3px\">");
      for (final Entry<String, Set<CloseLocation>> entry : vertexLocations.entrySet()) {
        final String typePath = entry.getKey();
        final Set<CloseLocation> locations = entry.getValue();
        final CloseLocation firstLocation = CollectionUtil.get(locations, 0);
        final String idAttributeName = firstLocation.getIdAttributeName();
        text.append("<b><i>");
        text.append(typePath);
        text.append("</i></b>\n");
        text.append("<table cellspacing=\"0\" cellpadding=\"1\" style=\"border: solid black 1px;margin: 3px 0px 3px 0px;padding: 0px;width: 100%\">"
          + "<thead><tr style=\"border-bottom: solid black 3px\"><th style=\"border-right: solid black 1px\">"
          + idAttributeName
          + "</th><th style=\"border-right: solid black 1px\">INDEX</th><th>POINT</th></tr></th><tbody>");
        for (final CloseLocation location : locations) {
          text.append("<tr style=\"border-bottom: solid black 1px\"><td style=\"border-right: solid black 1px\">");
          text.append(location.getId());
          text.append("</td><td style=\"border-right: solid black 1px\">");
          text.append(location.getIndexString());
          text.append("</td></td>");
          text.append(location.getPoint());
          text.append("</td></tr>");
        }
        text.append("</tbody></table>");
      }
      text.append("</div>");
    }
  }

  protected void clearMapCursor() {
    getMap().clearToolTipText();
    setMapCursor(Cursor.getDefaultCursor());
  }

  protected void clearMapCursor(final Cursor cursor) {
    if (getMapCursor() == cursor) {
      clearMapCursor();
    }
  }

  public void clearMouseOverGeometry() {
    if (!hasOverlayAction()) {
      clearMapCursor();
    }
    this.mouseOverLocations = Collections.emptyList();
    this.snapPointLocationMap = Collections.emptyMap();
    this.snapPoint = null;
    this.snapEventPoint = null;
  }

  public boolean clearOverlayAction(final String overlayAction) {
    if (this.map == null) {
      return false;
    } else {
      return this.map.clearOverlayAction(overlayAction);
    }

  }

  protected void clearUndoHistory() {
    getMap().getUndoManager().discardAllEdits();
  }

  protected void createPropertyUndo(final Object object,
    final String propertyName, final Object oldValue, final Object newValue) {
    final SetObjectProperty edit = new SetObjectProperty(object, propertyName,
      oldValue, newValue);
    addUndo(edit);
  }

  public void destroy() {
    this.map = null;
    mouseOverLocations.clear();
    this.project = null;
    this.snapEventPoint = null;
    this.snapPoint = null;
    this.snapPointLocationMap.clear();
    this.viewport = null;
    this.xorGeometry = null;
  }

  protected void drawXorGeometry(final Graphics2D graphics) {
    Geometry geometry = this.xorGeometry;
    if (geometry != null) {
      geometry = geometry.copy(getViewport().getGeometryFactory());
      final Paint paint = graphics.getPaint();
      try {
        graphics.setXORMode(Color.WHITE);
        if (geometry instanceof Point) {
          final Point point = (Point)geometry;
          final Point2D screenPoint = this.viewport.toViewPoint(point);

          final double x = screenPoint.getX() - getHotspotPixels();
          final double y = screenPoint.getY() - getHotspotPixels();
          final int diameter = 2 * getHotspotPixels();
          final Shape shape = new Ellipse2D.Double(x, y, diameter, diameter);

          graphics.setPaint(new Color(0, 0, 255));
          graphics.fill(shape);
        } else {
          GeometryStyleRenderer.renderGeometry(this.viewport, graphics,
            geometry, XOR_LINE_STYLE);
        }
      } finally {
        graphics.setPaint(paint);
      }
    }
  }

  protected CloseLocation findCloseLocation(
    final AbstractRecordLayer layer, final LayerRecord object,
    final Geometry geometry, final BoundingBox boundingBox) {
    CloseLocation closeLocation = findCloseVertexLocation(layer, object,
      geometry, boundingBox);
    if (closeLocation == null) {
      closeLocation = findCloseSegmentLocation(layer, object, geometry,
        boundingBox);
    }
    return closeLocation;
  }

  protected CloseLocation findCloseLocation(final LayerRecord object,
    final BoundingBox boundingBox) {
    if (object.isGeometryEditable()) {
      final AbstractRecordLayer layer = object.getLayer();
      final Geometry geometryValue = object.getGeometryValue();
      return findCloseLocation(layer, object, geometryValue, boundingBox);

    }
    return null;
  }

  private CloseLocation findCloseSegmentLocation(
    final AbstractRecordLayer layer, final LayerRecord object,
    final Geometry geometry, final BoundingBox boundingBox) {

    final GeometryFactory viewportGeometryFactory = getViewport().getGeometryFactory();
    final Geometry convertedGeometry = geometry.copy(viewportGeometryFactory);

    final double maxDistance = getMaxDistance(boundingBox);
    final GeometrySegmentQuadTree lineSegments = GeometryEditUtil.getGeometrySegmentIndex(convertedGeometry);
    final Point point = boundingBox.getCentre();
    double closestDistance = Double.MAX_VALUE;
    final List<Segment> segments = lineSegments.query(boundingBox,
      "isWithinDistance", point, maxDistance);
    Segment closestSegment = null;
    for (final Segment segment : segments) {
      final double distance = segment.distance(point);
      if (distance < closestDistance) {
        closestSegment = segment;
        closestDistance = distance;
      }
    }
    if (closestSegment != null) {
      final Point pointOnLine = viewportGeometryFactory.point(closestSegment.project(point));
      final GeometryFactory geometryFactory = layer.getGeometryFactory();
      final Point closePoint = pointOnLine.convert(geometryFactory);
      return new CloseLocation(layer, object, closestSegment, closePoint);
    }
    return null;
  }

  protected CloseLocation findCloseVertexLocation(
    final AbstractRecordLayer layer, final LayerRecord object,
    final Geometry geometry, final BoundingBox boundingBox) {
    final GeometryVertexQuadTree index = GeometryEditUtil.getGeometryVertexIndex(geometry);
    if (index != null) {
      final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
      Vertex closeVertex = null;
      final Point centre = boundingBox.getCentre();

      final List<Vertex> closeVertices = index.query(boundingBox);
      Collections.sort(closeVertices, VERTEX_INDEX_COMPARATOR);
      double minDistance = Double.MAX_VALUE;
      for (final Vertex vertex : closeVertices) {
        if (vertex != null) {
          final double distance = ((Point)vertex.copy(geometryFactory)).distance(centre);
          if (distance < minDistance) {
            minDistance = distance;
            closeVertex = vertex;
          }
        }
      }
      if (closeVertex != null) {
        return new CloseLocation(layer, object, closeVertex);
      }
    }
    return null;
  }

  protected double getDistance(final MouseEvent event) {
    final int x = event.getX();
    final int y = event.getY();
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Point p1 = this.viewport.toModelPoint(x, y).convert(geometryFactory,
      2);
    final Point p2 = this.viewport.toModelPoint(x + getHotspotPixels(),
      y + getHotspotPixels()).convert(geometryFactory, 2);

    return p1.distance(p2);
  }

  protected GeometryFactory getGeometryFactory() {
    if (this.geometryFactory == null) {
      return this.project.getGeometryFactory();
    }
    return this.geometryFactory;
  }

  @Override
  public Graphics2D getGraphics() {
    return (Graphics2D)super.getGraphics();
  }

  protected BoundingBox getHotspotBoundingBox(final MouseEvent event) {
    final Viewport2D viewport = getViewport();
    final GeometryFactory geometryFactory = getViewport().getGeometryFactory();
    final BoundingBox boundingBox;
    if (geometryFactory != null) {
      final int hotspotPixels = getHotspotPixels();
      boundingBox = viewport.getBoundingBox(geometryFactory, event,
        hotspotPixels);
    } else {
      boundingBox = new BoundingBoxDoubleGf();
    }
    return boundingBox;
  }

  public int getHotspotPixels() {
    return this.hotspotPixels;
  }

  public MapPanel getMap() {
    return this.map;
  }

  protected Cursor getMapCursor() {
    if (this.map == null) {
      return null;
    } else {
      return this.map.getCursor();
    }
  }

  private double getMaxDistance(final BoundingBox boundingBox) {
    return Math.max(boundingBox.getWidth() / 2, boundingBox.getHeight()) / 2;
  }

  public List<CloseLocation> getMouseOverLocations() {
    return mouseOverLocations;
  }

  protected Point getMousePoint() {
    final java.awt.Point mousePosition = getMap().getMapMousePosition();
    final Point mousePoint = getPoint(mousePosition);
    return mousePoint;
  }

  protected Point getPoint(final java.awt.Point eventPoint) {
    if (eventPoint == null) {
      return null;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Point point = this.viewport.toModelPointRounded(geometryFactory,
        eventPoint);
      return point;
    }
  }

  protected Point getPoint(final MouseEvent event) {
    if (event == null) {
      return null;
    } else {
      final java.awt.Point eventPoint = event.getPoint();
      return getPoint(eventPoint);
    }
  }

  public Project getProject() {
    return this.project;
  }

  protected List<AbstractRecordLayer> getSnapLayers() {
    return AbstractRecordLayer.getVisibleLayers(project);
  }

  public Point getSnapPoint() {
    return snapPoint;
  }

  public Map<Point, Set<CloseLocation>> getSnapPointLocationMap() {
    return snapPointLocationMap;
  }

  public Viewport2D getViewport() {
    return this.viewport;
  }

  protected GeometryFactory getViewportGeometryFactory() {
    if (this.viewport == null) {
      return GeometryFactory.floating3();
    } else {
      return this.viewport.getGeometryFactory();
    }
  }

  protected Point getViewportPoint(final java.awt.Point eventPoint) {
    final Point point = this.viewport.toModelPoint(eventPoint);
    return point;
  }

  protected Point getViewportPoint(final MouseEvent event) {
    if (event == null) {
      return null;
    } else {
      final java.awt.Point eventPoint = event.getPoint();
      return getViewportPoint(eventPoint);
    }
  }

  public Geometry getXorGeometry() {
    return this.xorGeometry;
  }

  public boolean hasOverlayAction() {
    if (this.map == null) {
      return false;
    } else {
      return this.map.hasOverlayAction();
    }
  }

  protected boolean hasSnapPoint(final MouseEvent event,
    final BoundingBox boundingBox) {

    final java.awt.Point eventPoint = event.getPoint();
    snapEventPoint = eventPoint;
    new TreeMap<Point, List<CloseLocation>>();
    this.snapCentre = boundingBox.getCentre();
    final List<AbstractRecordLayer> layers = getSnapLayers();
    final Map<Point, Set<CloseLocation>> snapLocations = new HashMap<Point, Set<CloseLocation>>();
    this.snapPoint = null;
    for (final AbstractRecordLayer layer : layers) {
      final List<LayerRecord> objects = layer.queryBackground(boundingBox);
      for (final LayerRecord object : objects) {
        if (layer.isVisible(object)) {
          final CloseLocation closeLocation = findCloseLocation(object,
            boundingBox);
          if (closeLocation != null) {
            final Point closePoint = closeLocation.getPoint();
            CollectionUtil.addToSet(snapLocations, closePoint, closeLocation);

          }
        }
      }
    }
    snapPointIndex = 0;
    return setSnapLocations(snapLocations);

  }

  public boolean isOverlayAction(final String overlayAction) {
    if (this.map == null) {
      return false;
    } else {
      return this.map.getOverlayAction() == overlayAction;
    }
  }

  @Override
  public void keyPressed(final KeyEvent e) {
  }

  @Override
  public void keyReleased(final KeyEvent e) {
  }

  @Override
  public void keyTyped(final KeyEvent e) {
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
  public void mouseMoved(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent event) {
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
  }

  @Override
  protected void paintComponent(final Graphics graphics) {
    paintComponent((Graphics2D)graphics);
  }

  protected void paintComponent(final Graphics2D graphics) {
    super.paintComponent(graphics);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
  }

  protected void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  protected void setMapCursor(final Cursor cursor) {
    if (this.map != null) {
      this.map.setCursor(cursor);
    }
  }

  protected boolean setMouseOverLocations(final java.awt.Point eventPoint,
    final List<CloseLocation> mouseOverLocations) {
    this.mouseOverLocations = mouseOverLocations;
    if (this.mouseOverLocations.isEmpty()) {
      if (!hasOverlayAction()) {
        clearMapCursor();
      }
      return false;
    } else {
      this.snapPoint = null;
      setXorGeometry(null);
      return true;
    }
  }

  public boolean setOverlayAction(final String overlayAction) {
    if (this.map == null) {
      return false;
    } else {
      return this.map.setOverlayAction(overlayAction);
    }
  }

  protected boolean setSnapLocations(
    final Map<Point, Set<CloseLocation>> snapLocations) {
    if (snapLocations != snapPointLocationMap) {
      this.snapPointLocationMap = snapLocations;
      snapPoints.clear();
      snapPoints.addAll(snapLocations.keySet());
      Collections.sort(snapPoints, new Comparator<Point>() {
        @Override
        public int compare(final Point point1, final Point point2) {
          final Collection<CloseLocation> locations1 = snapLocations.get(point1);
          final Collection<CloseLocation> locations2 = snapLocations.get(point2);
          final boolean hasVertex1 = hasVertex(locations1);
          final boolean hasVertex2 = hasVertex(locations2);
          if (hasVertex1) {
            if (!hasVertex2) {
              return -1;
            }
          } else if (hasVertex2) {
            return 0;
          }
          final double distance1 = snapCentre.distance(point1);
          final double distance2 = snapCentre.distance(point2);
          if (distance1 <= distance2) {
            return -1;
          } else {
            return 0;
          }
        }

        private boolean hasVertex(final Collection<CloseLocation> locations) {
          for (final CloseLocation location : locations) {
            if (location.getVertex() != null) {
              return true;
            }
          }
          return false;
        }
      });

    }
    if (this.snapPointLocationMap.isEmpty()) {
      this.snapCentre = null;
      this.snapPoint = null;
      snapPoints.clear();
      if (!hasOverlayAction()) {
        clearMapCursor();
      }
      return false;
    } else {

      this.snapPoint = snapPoints.get(snapPointIndex);

      boolean nodeSnap = false;
      final StringBuffer text = new StringBuffer(
        "<html><ol style=\"margin: 2px 2px 2px 15px\">");
      int i = 0;
      for (final Point snapPoint : this.snapPoints) {
        if (this.snapPointIndex == i) {
          text.append("<li style=\"border: 3px solid maroon; padding: 2px\">");
        } else {
          text.append("<li style=\"padding: 3px\">");
        }
        i++;
        text.append("<b>Snap to (");
        text.append(WktWriter.toString(snapPoint, true));
        text.append(")</b><ul style=\"margin: 2px 2px 2px 15px\">");

        final Map<String, Set<CloseLocation>> typeLocationsMap = new TreeMap<String, Set<CloseLocation>>();
        for (final CloseLocation snapLocation : snapPointLocationMap.get(snapPoint)) {
          final String typePath = snapLocation.getTypePath();
          final String locationType = snapLocation.getType();
          if ("Point".equals(locationType) || "End-Vertex".equals(locationType)) {
            nodeSnap = true;
          }
          CollectionUtil.addToSet(typeLocationsMap, typePath
            + " (<b style=\"color:red\">" + locationType + "</b>)",
            snapLocation);
        }

        for (final Entry<String, Set<CloseLocation>> typeLocations : typeLocationsMap.entrySet()) {
          final String type = typeLocations.getKey();
          text.append("<li>");
          text.append(type);
          text.append("</i><");
        }

        text.append("</ul></li>");
      }
      text.append("</ol></html>");
      getMap().setToolTipText(snapEventPoint, text);

      if (BooleanStringConverter.getBoolean(nodeSnap)) {
        setMapCursor(CURSOR_NODE_SNAP);
      } else {
        setMapCursor(CURSOR_LINE_SNAP);
      }
      return true;
    }
  }

  protected void setSnapPoint(final Point snapPoint) {
    this.snapPoint = snapPoint;
  }

  public void setSnapPointIndex(final int snapPointIndex) {
    this.snapPointIndex = snapPointIndex;
  }

  public void setXorGeometry(final Geometry xorGeometry) {
    this.xorGeometry = xorGeometry;
    repaint();
  }
}
