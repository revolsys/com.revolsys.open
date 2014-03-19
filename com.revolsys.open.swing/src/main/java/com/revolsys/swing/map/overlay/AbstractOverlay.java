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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.undo.UndoableEdit;

import com.revolsys.comparator.IntArrayComparator;
import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.algorithm.index.PointQuadTree;
import com.revolsys.gis.algorithm.index.quadtree.QuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.comparator.GeometryDistanceComparator;
import com.revolsys.gis.model.geometry.util.GeometryEditUtil;
import com.revolsys.gis.model.geometry.util.IndexedLineSegment;
import com.revolsys.io.wkt.WktWriter;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.undo.SetObjectProperty;
import com.revolsys.util.CollectionUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

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

  private static final IntArrayComparator INT_ARRAY_COMPARATOR = new IntArrayComparator();

  private static final long serialVersionUID = 1L;

  public static final GeometryStyle XOR_LINE_STYLE = GeometryStyle.line(
    new Color(0, 0, 255), 2);

  private GeometryFactory geometryFactory;

  private final int hotspotPixels = 6;

  private MapPanel map;

  private List<CloseLocation> mouseOverLocations = Collections.emptyList();

  private Project project;

  protected java.awt.Point snapEventPoint;

  protected Point snapPoint;

  protected int snapPointIndex;

  protected Map<Point, Set<CloseLocation>> snapPointLocationMap = Collections.emptyMap();

  private Viewport2D viewport;

  private Geometry xorGeometry;

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
          + idAttributeName + "</th><th>INDEX</th></tr></th><tbody>");
        for (final CloseLocation location : locations) {
          text.append("<tr style=\"border-bottom: solid black 1px\"><td style=\"border-right: solid black 1px\">");
          text.append(location.getId());
          text.append("</td></td>");
          text.append(location.getIndexString());
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
      geometry = getViewport().getGeometryFactory().copy(geometry);
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
    final AbstractDataObjectLayer layer, final LayerDataObject object,
    final Geometry geometry, final BoundingBox boundingBox) {
    CloseLocation closeLocation = findCloseVertexLocation(layer, object,
      geometry, boundingBox);
    if (closeLocation == null) {
      closeLocation = findCloseSegmentLocation(layer, object, geometry,
        boundingBox);
    }
    return closeLocation;
  }

  protected CloseLocation findCloseLocation(final LayerDataObject object,
    final BoundingBox boundingBox) {
    if (object.isGeometryEditable()) {
      final AbstractDataObjectLayer layer = object.getLayer();
      final Geometry geometryValue = object.getGeometryValue();
      return findCloseLocation(layer, object, geometryValue, boundingBox);

    }
    return null;
  }

  private CloseLocation findCloseSegmentLocation(
    final AbstractDataObjectLayer layer, final LayerDataObject object,
    final Geometry geometry, final BoundingBox boundingBox) {

    final GeometryFactory viewportGeometryFactory = getViewport().getGeometryFactory();
    final Geometry convertedGeometry = viewportGeometryFactory.copy(geometry);

    final double maxDistance = getMaxDistance(boundingBox);
    final QuadTree<IndexedLineSegment> lineSegments = GeometryEditUtil.getLineSegmentQuadTree(convertedGeometry);
    if (lineSegments != null) {
      final Point point = boundingBox.getCentrePoint();
      final Coordinates coordinates = CoordinatesUtil.get(point);

      double closestDistance = Double.MAX_VALUE;
      final List<IndexedLineSegment> segments = lineSegments.query(boundingBox,
        "isWithinDistance", point, maxDistance);
      IndexedLineSegment closestSegment = null;
      for (final IndexedLineSegment segment : segments) {
        final double distance = segment.distance(coordinates);
        if (distance < closestDistance) {
          closestSegment = segment;
          closestDistance = distance;
        }
      }
      if (closestSegment != null) {
        Coordinates pointOnLine = closestSegment.project(coordinates);
        final GeometryFactory geometryFactory = layer.getGeometryFactory();
        pointOnLine = ProjectionFactory.convert(pointOnLine,
          viewportGeometryFactory, geometryFactory);
        final Point closePoint = geometryFactory.createPoint(pointOnLine);
        return new CloseLocation(layer, object, geometry, null, closestSegment,
          closePoint);
      }
    }
    return null;
  }

  protected CloseLocation findCloseVertexLocation(
    final AbstractDataObjectLayer layer, final LayerDataObject object,
    final Geometry geometry, final BoundingBox boundingBox) {
    final PointQuadTree<int[]> index = GeometryEditUtil.getPointQuadTree(geometry);
    if (index != null) {
      final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
      int[] closestVertexIndex = null;
      Point closeVertex = null;
      final Point centre = boundingBox.getCentrePoint();

      final List<int[]> closeVertices = index.findWithin(boundingBox);
      Collections.sort(closeVertices, INT_ARRAY_COMPARATOR);
      double minDistance = Double.MAX_VALUE;
      for (final int[] vertexIndex : closeVertices) {
        final Point vertex = GeometryEditUtil.getVertexPoint(geometry,
          vertexIndex);
        if (vertex != null) {
          final double distance = geometryFactory.copy(vertex).distance(centre);
          if (distance < minDistance) {
            minDistance = distance;
            closestVertexIndex = vertexIndex;
            closeVertex = vertex;
          }
        }
      }
      if (closestVertexIndex != null) {
        return new CloseLocation(layer, object, geometry, closestVertexIndex,
          null, closeVertex);
      }
    }
    return null;
  }

  protected double getDistance(final MouseEvent event) {
    final int x = event.getX();
    final int y = event.getY();
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Point p1 = geometryFactory.project(this.viewport.toModelPoint(x, y));
    final Point p2 = geometryFactory.project(this.viewport.toModelPoint(x
      + getHotspotPixels(), y + getHotspotPixels()));

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
      boundingBox = new BoundingBox();
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

  protected List<AbstractDataObjectLayer> getSnapLayers() {
    return AbstractDataObjectLayer.getVisibleLayers(project);
  }

  public Point getSnapPoint() {
    return snapPoint;
  }

  public Viewport2D getViewport() {
    return this.viewport;
  }

  protected GeometryFactory getViewportGeometryFactory() {
    if (this.viewport == null) {
      return GeometryFactory.getFactory();
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
    new TreeMap<Coordinates, List<CloseLocation>>();
    final Point point = boundingBox.getCentrePoint();
    final List<AbstractDataObjectLayer> layers = getSnapLayers();
    final TreeMap<Point, Set<CloseLocation>> snapLocations = new TreeMap<Point, Set<CloseLocation>>(
      new GeometryDistanceComparator(point));
    this.snapPoint = null;
    for (final AbstractDataObjectLayer layer : layers) {
      final List<LayerDataObject> objects = layer.queryBackground(boundingBox);
      for (final LayerDataObject object : objects) {
        if (layer.isVisible(object)) {
          final CloseLocation closeLocation = findCloseLocation(object,
            boundingBox);
          if (closeLocation != null) {
            boolean found = false;
            final Point closePoint = closeLocation.getPoint();
            for (final Entry<Point, Set<CloseLocation>> entry : snapLocations.entrySet()) {
              if (entry.getKey().equals(closePoint)) {
                final Set<CloseLocation> pointSnapLocations = entry.getValue();
                pointSnapLocations.add(closeLocation);
                found = true;
              }
            }
            if (!found) {
              final Set<CloseLocation> pointSnapLocations = new LinkedHashSet<CloseLocation>();
              snapLocations.put(closePoint, pointSnapLocations);
              pointSnapLocations.add(closeLocation);
            }
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
    this.snapPointLocationMap = snapLocations;
    if (this.snapPointLocationMap.isEmpty()) {
      this.snapPoint = null;
      if (!hasOverlayAction()) {
        clearMapCursor();
      }
      return false;
    } else {
      this.snapPoint = CollectionUtil.get(snapLocations.keySet(),
        snapPointIndex);

      boolean nodeSnap = false;
      final StringBuffer text = new StringBuffer(
        "<html><ol style=\"margin: 2px 2px 2px 15px\">");
      int i = 0;
      for (final Entry<Point, Set<CloseLocation>> entry : this.snapPointLocationMap.entrySet()) {
        final Point snapPoint = entry.getKey();
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
        for (final CloseLocation snapLocation : entry.getValue()) {
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

  public void setXorGeometry(final Geometry xorGeometry) {
    this.xorGeometry = xorGeometry;
    repaint();
  }
}
