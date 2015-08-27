package com.revolsys.swing.map.overlay;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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

import com.revolsys.collection.map.Maps;
import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.equals.Equals;
import com.revolsys.geometry.algorithm.index.quadtree.GeometrySegmentQuadTree;
import com.revolsys.geometry.algorithm.index.quadtree.GeometryVertexQuadTree;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.model.vertex.VertexIndexComparator;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.LineCap;
import com.revolsys.swing.undo.SetObjectProperty;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Property;

public class AbstractOverlay extends JComponent implements PropertyChangeListener, MouseListener,
  MouseMotionListener, MouseWheelListener, KeyListener, FocusListener {
  public static final Cursor CURSOR_LINE_ADD_NODE = Icons.getCursor("cursor_line_node_add", 8, 6);

  public static final Cursor CURSOR_LINE_SNAP = Icons.getCursor("cursor_line_snap", 8, 4);

  public static final Cursor CURSOR_NODE_ADD = Icons.getCursor("cursor_node_add", 8, 8);

  public static final Cursor CURSOR_NODE_EDIT = Icons.getCursor("cursor_node_edit", 8, 7);

  public static final Cursor CURSOR_NODE_SNAP = Icons.getCursor("cursor_node_snap", 8, 7);

  public static final Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();

  private static final long serialVersionUID = 1L;

  private static final VertexIndexComparator VERTEX_INDEX_COMPARATOR = new VertexIndexComparator();

  public static final GeometryStyle XOR_LINE_STYLE = GeometryStyle.line(new Color(0, 0, 255), 1);

  private GeometryFactory geometryFactory;

  private final int hotspotPixels = 6;

  private MapPanel map;

  private Point snapCentre;

  private java.awt.Point snapEventPoint;

  private Point snapPoint;

  private int snapPointIndex;

  private Map<Point, Set<CloseLocation>> snapPointLocationMap = Collections.emptyMap();

  private final List<Point> snapPoints = new ArrayList<Point>();

  private Viewport2D viewport;

  private Geometry xorGeometry;

  protected AbstractOverlay(final MapPanel map) {
    this.map = map;
    this.viewport = map.getViewport();

    map.addMapOverlay(this);
  }

  public void addOverlayActionOverride(final String overlayAction,
    final String... overrideOverlayActions) {
    if (this.map != null) {
      this.map.addOverlayActionOverride(overlayAction, overrideOverlayActions);
    }
  }

  protected void addUndo(final UndoableEdit edit) {
    this.map.addUndo(edit);
  }

  protected void appendLocations(final StringBuilder text, final String title,
    final Map<String, Set<CloseLocation>> vertexLocations) {
    if (!vertexLocations.isEmpty()) {
      text.append(
        "<div style=\"border-bottom: solid black 1px; font-weight:bold;padding: 1px 3px 1px 3px\">");
      text.append(title);
      text.append("</div>");
      text.append("<div style=\"padding: 1px 3px 1px 3px\">");
      for (final Entry<String, Set<CloseLocation>> entry : vertexLocations.entrySet()) {
        final String typePath = entry.getKey();
        final Set<CloseLocation> locations = entry.getValue();
        final CloseLocation firstLocation = CollectionUtil.get(locations, 0);
        final String idFieldName = firstLocation.getIdFieldName();
        text.append("<b><i>");
        text.append(typePath);
        text.append("</i></b>\n");
        text.append(
          "<table cellspacing=\"0\" cellpadding=\"1\" style=\"border: solid black 1px;margin: 3px 0px 3px 0px;padding: 0px;width: 100%\">");
        text.append(
          "<thead><tr style=\"border-bottom: solid black 3px\"><th style=\"border-right: solid black 1px\">");
        final boolean hasId = Property.hasValue(idFieldName);
        if (hasId) {
          text.append(idFieldName);
          text.append("</th><th style=\"border-right: solid black 1px\">");
        }
        text.append(
          "INDEX</th><th style=\"border-right: solid black 1px\">SRID</th><th>POINT</th></tr></th><tbody>");
        for (final CloseLocation location : locations) {
          text.append(
            "<tr style=\"border-bottom: solid black 1px\"><td style=\"border-right: solid black 1px\">");
          final Object id = location.getId();
          if (hasId) {
            text.append(id);
            text.append("</td><td style=\"border-right: solid black 1px\">");
          }
          text.append(location.getIndexString());
          text.append("</td><td style=\"border-right: solid black 1px\">");
          final Point point = location.getPoint();
          text.append(point.getSrid());
          text.append("</td><td>");
          appendPoint(text, point);
          text.append("</td></tr>");
        }
        text.append("</tbody></table>");
      }
      text.append("</div>");
    }
  }

  protected void appendPoint(final StringBuilder text, final Point point) {
    final double unitsPerPixel = getViewport().getUnitsPerPixel();
    double scale = getGeometryFactory().getScaleXY();
    if (getGeometryFactory().isProjected()) {
      if (unitsPerPixel > 2) {
        scale = 1.0;
      }
    }
    text.append(MathUtil.toString(MathUtil.makePrecise(scale, point.getX())));
    text.append(",");
    text.append(MathUtil.toString(MathUtil.makePrecise(scale, point.getY())));
  }

  public boolean canOverrideOverlayAction(final String newAction) {
    if (this.map == null) {
      return false;
    } else {
      return this.map.canOverrideOverlayAction(newAction);
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

  public boolean clearOverlayAction(final String overlayAction) {
    if (this.map == null) {
      return false;
    } else {
      return this.map.clearOverlayAction(overlayAction);
    }
  }

  protected void clearSnapLocations() {
    this.snapPointLocationMap = Collections.emptyMap();
    this.snapPoint = null;
    this.snapEventPoint = null;
  }

  protected void clearUndoHistory() {
    getMap().getUndoManager().discardAllEdits();
  }

  protected void createPropertyUndo(final Object object, final String propertyName,
    final Object oldValue, final Object newValue) {
    final SetObjectProperty edit = new SetObjectProperty(object, propertyName, oldValue, newValue);
    addUndo(edit);
  }

  protected LineString createXorLine(final GeometryFactory geometryFactory, final Point c0,
    final Point p1) {
    final Viewport2D viewport = getViewport();
    final GeometryFactory viewportGeometryFactory = viewport.getGeometryFactory();
    final LineSegment line = viewportGeometryFactory.lineSegment(c0, p1);
    final double length = line.getLength();
    if (length > 0) {
      final double cursorRadius = viewport.getModelUnitsPerViewUnit() * 6;
      final Point newC1 = line.pointAlongOffset((length - cursorRadius) / length, 0);
      return geometryFactory.lineString(c0, newC1);
    } else {
      return null;
    }
  }

  public void destroy() {
    this.map = null;
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
          XOR_LINE_STYLE.setLineCapEnum(LineCap.BUTT);
          GeometryStyleRenderer.renderGeometry(this.viewport, graphics, geometry, XOR_LINE_STYLE);
        }
      } finally {
        graphics.setPaint(paint);
      }
    }
  }

  protected CloseLocation findCloseLocation(final AbstractRecordLayer layer,
    final LayerRecord record, final Geometry geometry, final BoundingBox boundingBox) {
    CloseLocation closeLocation = findCloseVertexLocation(layer, record, geometry, boundingBox);
    if (closeLocation == null) {
      closeLocation = findCloseSegmentLocation(layer, record, geometry, boundingBox);
    }
    return closeLocation;
  }

  protected CloseLocation findCloseLocation(final LayerRecord record,
    final BoundingBox boundingBox) {
    if (record.isGeometryEditable()) {
      final AbstractRecordLayer layer = record.getLayer();
      final Geometry geometry = record.getGeometry();
      return findCloseLocation(layer, record, geometry, boundingBox);

    }
    return null;
  }

  private CloseLocation findCloseSegmentLocation(final AbstractRecordLayer layer,
    final LayerRecord record, final Geometry geometry, final BoundingBox boundingBox) {

    final GeometryFactory viewportGeometryFactory = getViewport().getGeometryFactory();
    final Geometry convertedGeometry = geometry.copy(viewportGeometryFactory);

    final double maxDistance = getMaxDistance(boundingBox);
    final GeometrySegmentQuadTree lineSegments = GeometrySegmentQuadTree.get(convertedGeometry);
    final Point point = boundingBox.getCentre();
    double closestDistance = Double.MAX_VALUE;
    final List<Segment> segments = lineSegments.query(boundingBox, (segment) -> {
      return segment.isWithinDistance(point, maxDistance);
    });
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
      Point closePoint = pointOnLine;
      if (layer != null) {
        final GeometryFactory geometryFactory = layer.getGeometryFactory();
        closePoint = pointOnLine.convert(geometryFactory);
      }
      final int[] segmentId = closestSegment.getSegmentId();
      final Segment segment = geometry.getSegment(segmentId);
      return new CloseLocation(layer, record, segment, closePoint);
    }
    return null;
  }

  protected CloseLocation findCloseVertexLocation(final AbstractRecordLayer layer,
    final LayerRecord record, final Geometry geometry, final BoundingBox boundingBox) {
    final GeometryVertexQuadTree index = GeometryVertexQuadTree.getGeometryVertexIndex(geometry);
    if (index != null) {
      final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
      Vertex closeVertex = null;
      final Point centre = boundingBox.getCentre();

      final List<Vertex> closeVertices = index.query(boundingBox);
      Collections.sort(closeVertices, VERTEX_INDEX_COMPARATOR);
      double minDistance = Double.MAX_VALUE;
      for (final Vertex vertex : closeVertices) {
        if (vertex != null) {
          final double distance = ((Point)vertex.convert(geometryFactory)).distance(centre);
          if (distance < minDistance) {
            minDistance = distance;
            closeVertex = vertex;
          }
        }
      }
      if (closeVertex != null) {
        return new CloseLocation(layer, record, closeVertex);
      }
    }
    return null;
  }

  @Override
  public void focusGained(final FocusEvent e) {
  }

  @Override
  public void focusLost(final FocusEvent e) {
  }

  protected double getDistance(final MouseEvent event) {
    final int x = event.getX();
    final int y = event.getY();
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Point p1 = this.viewport.toModelPoint(x, y).convert(geometryFactory, 2);
    final Point p2 = this.viewport.toModelPoint(x + getHotspotPixels(), y + getHotspotPixels())
      .convert(geometryFactory, 2);

    return p1.distance(p2);
  }

  protected Point getEventPoint() {
    final MouseOverlay mouseOverlay = this.map.getMouseOverlay();
    return mouseOverlay.getEventPoint();
  }

  protected java.awt.Point getEventPosition() {
    final MouseOverlay mouseOverlay = this.map.getMouseOverlay();
    return mouseOverlay.getEventPosition();
  }

  protected GeometryFactory getGeometryFactory() {
    GeometryFactory geometryFactory = this.geometryFactory;
    if (geometryFactory == null) {
      geometryFactory = getProject().getGeometryFactory();
      if (geometryFactory == null) {
        geometryFactory = getViewportGeometryFactory();
      }
    }
    return geometryFactory;
  }

  @Override
  public Graphics2D getGraphics() {
    return (Graphics2D)super.getGraphics();
  }

  protected BoundingBox getHotspotBoundingBox() {
    final Viewport2D viewport = getViewport();
    final GeometryFactory geometryFactory = getViewport().getGeometryFactory();
    final BoundingBox boundingBox;
    if (geometryFactory != null) {
      final int hotspotPixels = getHotspotPixels();
      boundingBox = viewport.getBoundingBox(geometryFactory, hotspotPixels);
    } else {
      boundingBox = BoundingBox.EMPTY;
    }
    return boundingBox;
  }

  protected BoundingBox getHotspotBoundingBox(final MouseEvent event) {
    final Viewport2D viewport = getViewport();
    final GeometryFactory geometryFactory = getViewport().getGeometryFactory();
    final BoundingBox boundingBox;
    if (geometryFactory != null) {
      final int hotspotPixels = getHotspotPixels();
      boundingBox = viewport.getBoundingBox(geometryFactory, event, hotspotPixels);
    } else {
      boundingBox = BoundingBox.EMPTY;
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

  public String getOverlayAction() {
    if (this.map == null) {
      return null;
    } else {
      return this.map.getOverlayAction();
    }
  }

  protected Cursor getOverlayActionCursor(final String name) {
    if (this.map == null) {
      return DEFAULT_CURSOR;
    } else {
      return this.map.getOverlayActionCursor(name);
    }
  }

  protected Point getOverlayPoint() {
    final int x = MouseOverlay.getEventX();
    final int y = MouseOverlay.getEventY();
    return getPoint(x, y);
  }

  protected Point getPoint(final GeometryFactory geometryFactory, final MouseEvent event) {
    final Viewport2D viewport = getViewport();
    final Point point = viewport.toModelPointRounded(geometryFactory, event.getX(), event.getY());
    return point;
  }

  protected Point getPoint(final int x, final int y) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Point point = this.viewport.toModelPointRounded(geometryFactory, x, y);
    return point;
  }

  protected Point getPoint(final MouseEvent event) {
    if (event == null) {
      return null;
    } else {
      final int x = event.getX();
      final int y = event.getY();
      return getPoint(x, y);
    }
  }

  public Project getProject() {
    return this.map.getProject();
  }

  protected List<AbstractRecordLayer> getSnapLayers() {
    final Project project = getProject();
    return AbstractRecordLayer.getVisibleLayers(project);
  }

  public Point getSnapPoint() {
    return this.snapPoint;
  }

  public Map<Point, Set<CloseLocation>> getSnapPointLocationMap() {
    return this.snapPointLocationMap;
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

  protected boolean hasSnapPoint(final BoundingBox boundingBox) {
    final java.awt.Point eventPoint = getEventPosition();
    return hasSnapPoint(eventPoint, boundingBox);
  }

  protected boolean hasSnapPoint(final java.awt.Point eventPoint, final BoundingBox boundingBox) {
    this.snapEventPoint = eventPoint;
    new TreeMap<Point, List<CloseLocation>>();
    this.snapCentre = boundingBox.getCentre();
    final List<AbstractRecordLayer> layers = getSnapLayers();
    final Map<Point, Set<CloseLocation>> snapLocations = new HashMap<>();
    this.snapPoint = null;
    for (final AbstractRecordLayer layer : layers) {
      final List<LayerRecord> records = layer.queryBackground(boundingBox);
      for (final LayerRecord record : records) {
        if (layer.isVisible(record)) {
          final CloseLocation closeLocation = findCloseLocation(record, boundingBox);
          if (closeLocation != null) {
            final Point closePoint = closeLocation.getPoint();
            Maps.addToSet(snapLocations, closePoint, closeLocation);

          }
        }
      }
    }
    return setSnapLocations(snapLocations);
  }

  protected boolean hasSnapPoint(final MouseEvent event, final BoundingBox boundingBox) {
    final java.awt.Point eventPoint = event.getPoint();
    return hasSnapPoint(eventPoint, boundingBox);
  }

  public boolean isMouseInMap() {
    return MouseOverlay.isMouseInMap();
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
    if (geometryFactory == null) {
      this.geometryFactory = getViewportGeometryFactory();
    } else {
      this.geometryFactory = geometryFactory;
    }
  }

  protected void setMapCursor(final Cursor cursor) {
    if (this.map != null) {
      this.map.setMapCursor(cursor);
    }
  }

  public boolean setOverlayAction(final String overlayAction) {
    if (this.map == null) {
      return false;
    } else {
      final boolean set = this.map.setOverlayAction(overlayAction);
      return set;
    }
  }

  protected void setOverlayActionCursor(final String name, final Cursor cursor) {
    if (this.map != null) {
      this.map.setOverlayActionCursor(name, cursor);
    }
  }

  protected boolean setSnapLocations(final Map<Point, Set<CloseLocation>> snapLocations) {
    if (snapLocations.isEmpty()) {
      if (!this.snapPointLocationMap.isEmpty()) {
        this.snapCentre = null;
        this.snapPoint = null;
        this.snapPointLocationMap = snapLocations;
        this.snapPoints.clear();
        if (!hasOverlayAction()) {
          clearMapCursor();
        }
        this.map.clearToolTipText();
      }
      return false;
    } else {
      if (!Equals.equal(snapLocations, this.snapPointLocationMap)) {
        this.snapPointIndex = 1;
        this.snapPointLocationMap = snapLocations;
        this.snapPoints.clear();
        this.snapPoints.addAll(snapLocations.keySet());
        Collections.sort(this.snapPoints, new Comparator<Point>() {
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
            final double distance1 = AbstractOverlay.this.snapCentre.distance(point1);
            final double distance2 = AbstractOverlay.this.snapCentre.distance(point2);
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
      final MapPanel map = getMap();
      if (this.snapPointIndex == 0) {
        this.snapPoint = getEventPoint();
      } else {
        this.snapPoint = this.snapPoints.get(this.snapPointIndex - 1);
      }

      boolean nodeSnap = false;
      final StringBuilder text = new StringBuilder(
        "<html><ol start=\"0\" style=\"margin: 2px 2px 2px 15px\">");
      text.append("<li style=\"padding: 2px; margin:1px;");
      if (0 == this.snapPointIndex) {
        text.append("border: 2px solid maroon");
      } else {
        text.append("border: 2px solid #FFFF33");
      }

      text.append("\">Not snapped: ");
      final Point mousePoint = getEventPoint();
      appendPoint(text, mousePoint);
      text.append(" (");
      text.append(mousePoint.getSrid());
      text.append(")</li>");
      int i = 1;
      for (final Point snapPoint : this.snapPoints) {
        text.append("<li style=\"padding: 2px; margin:1px;");
        if (i == this.snapPointIndex) {
          text.append("border: 2px solid maroon");
        } else {
          text.append("border: 2px solid #FFFF33");
        }

        i++;
        text.append("\"><b>Snap: ");
        appendPoint(text, snapPoint);
        text.append(" (");
        text.append(snapPoint.getSrid());
        text.append(")</b><ul style=\"margin: 2px 2px 2px 15px\">");

        final Map<String, Set<CloseLocation>> typeLocationsMap = new TreeMap<String, Set<CloseLocation>>();
        for (final CloseLocation snapLocation : this.snapPointLocationMap.get(snapPoint)) {
          final String typePath = snapLocation.getLayerPath();
          final String locationType = snapLocation.getType();
          if ("Point".equals(locationType) || "End-Vertex".equals(locationType)) {
            nodeSnap = true;
          }
          Maps.addToSet(typeLocationsMap,
            typePath + " (<b style=\"color:red\">" + locationType + "</b>)", snapLocation);
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
      map.setToolTipText(this.snapEventPoint, text);

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
