package com.revolsys.swing.map.overlay;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.SwingUtilities;
import javax.swing.undo.UndoableEdit;

import org.jdesktop.swingx.color.ColorUtil;

import com.revolsys.awt.WebColors;
import com.revolsys.comparator.IntArrayComparator;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.algorithm.index.PointQuadTree;
import com.revolsys.gis.algorithm.index.quadtree.QuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.property.DirectionalAttributes;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.gis.model.geometry.util.GeometryEditUtil;
import com.revolsys.gis.model.geometry.util.IndexedLineSegment;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;
import com.revolsys.swing.undo.AbstractUndoableEdit;
import com.revolsys.swing.undo.MultipleUndo;
import com.revolsys.util.CollectionUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

@SuppressWarnings("serial")
public class EditGeometryOverlay extends SelectRecordsOverlay implements
  PropertyChangeListener, MouseListener, MouseMotionListener {

  private class AddGeometryUndoEdit extends AbstractUndoableEdit {

    private final Geometry oldGeometry = addGeometry;

    private final Geometry newGeometry;

    private final int[] geometryPartIndex = addGeometryPartIndex;

    private final DataType geometryPartDataType = addGeometryPartDataType;

    private AddGeometryUndoEdit(final Geometry geometry) {
      this.newGeometry = geometry;
    }

    @Override
    public boolean canRedo() {
      if (super.canRedo()) {
        if (JtsGeometryUtil.equalsExact3D(oldGeometry, addGeometry)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean canUndo() {
      if (super.canUndo()) {
        if (JtsGeometryUtil.equalsExact3D(newGeometry, addGeometry)) {
          return true;
        }
      }
      return false;
    }

    @Override
    protected void doRedo() {
      addGeometry = newGeometry;
      setXorGeometry(null);
      repaint();
    }

    @Override
    protected void doUndo() {
      addGeometry = oldGeometry;
      addGeometryPartDataType = geometryPartDataType;
      addGeometryPartIndex = geometryPartIndex;
      setXorGeometry(null);
      repaint();
    }
  }

  private static final Color COLOR = WebColors.Aqua;

  private static final Color COLOR_TRANSPARENT = ColorUtil.setAlpha(COLOR, 127);

  private static final Cursor CURSOR_NODE_ADD = SilkIconLoader.getCursor(
    "cursor_node_add", 8, 7);

  private static final Cursor CURSOR_MOVE = SilkIconLoader.getCursor(
    "cursor_move", 8, 7);

  private static final Cursor CURSOR_NODE_EDIT = SilkIconLoader.getCursor(
    "cursor_node_edit", 8, 7);

  private static final Cursor CURSOR_NODE_SNAP = SilkIconLoader.getCursor(
    "cursor_node_snap", 8, 7);

  private static final Cursor CURSOR_LINE_ADD_NODE = SilkIconLoader.getCursor(
    "cursor_line_node_add", 8, 6);

  private static final Cursor CURSOR_LINE_SNAP = SilkIconLoader.getCursor(
    "cursor_line_snap", 8, 4);

  private static final IntArrayComparator INT_ARRAY_COMPARATOR = new IntArrayComparator();

  private static final GeometryStyle STYLE_HIGHLIGHT = GeometryStyle.polygon(
    COLOR, 3, COLOR_TRANSPARENT);

  private static final GeometryStyle STYLE_OUTLINE = GeometryStyle.line(COLOR_OUTLINE);

  private static final MarkerStyle STYLE_VERTEX = MarkerStyle.marker("ellipse",
    6, COLOR_OUTLINE, 1, COLOR);

  static {
    MarkerStyle.setMarker(STYLE_HIGHLIGHT, "ellipse", 6,
      COLOR_OUTLINE_TRANSPARENT, 1, COLOR_TRANSPARENT);
    MarkerStyle.setMarker(STYLE_OUTLINE, "ellipse", 6,
      COLOR_OUTLINE_TRANSPARENT, 1, COLOR_TRANSPARENT);
  }

  private int actionId = 0;

  private AddGeometryCompleteAction addCompleteAction;

  private Geometry addGeometry;

  private DataType addGeometryDataType;

  private DataType addGeometryPartDataType;

  /** Index to the part of the addGeometry that new points should be added too. */
  private int[] addGeometryPartIndex = {};

  private DataObjectLayer addLayer;

  private Point snapPoint;

  private boolean dragged = false;

  private java.awt.Point moveGeometryStart;

  private List<CloseLocation> mouseOverLocations = Collections.emptyList();

  public EditGeometryOverlay(final MapPanel map) {
    super(map);
  }

  protected void actionGeometryCompleted() {
    if (isModeAddGeometry()) {
      if (isGeometryValid(addGeometry)) {
        try {
          setXorGeometry(null);
          if (addCompleteAction != null) {
            final Geometry geometry = addLayer.getGeometryFactory().copy(
              this.addGeometry);
            addCompleteAction.addComplete(this, geometry);
            clearAddGeometry();
          }
        } finally {
          clearMapCursor();
        }
      }
    }
  }

  /**
   * Set the addLayer that a new feature is to be added to.
   * 
   * @param addLayer 
   */
  public void addObject(final DataObjectLayer layer,
    final AddGeometryCompleteAction addCompleteAction) {
    if (layer != null) {
      final DataObjectMetaData metaData = layer.getMetaData();
      final Attribute geometryAttribute = metaData.getGeometryAttribute();
      if (geometryAttribute != null) {
        this.addLayer = layer;
        this.addCompleteAction = addCompleteAction;
        final GeometryFactory geometryFactory = metaData.getGeometryFactory();
        this.setGeometryFactory(geometryFactory);
        clearUndoHistory();
        this.addGeometry = geometryFactory.createEmptyGeometry();
        setAddGeometryDataType(geometryAttribute.getType());
        setMapCursor(CURSOR_NODE_ADD);

        if (Arrays.asList(DataTypes.POINT, DataTypes.LINE_STRING).contains(
          addGeometryDataType)) {
          addGeometryPartIndex = new int[0];
        } else if (Arrays.asList(DataTypes.MULTI_POINT,
          DataTypes.MULTI_LINE_STRING, DataTypes.POLYGON).contains(
          addGeometryDataType)) {
          addGeometryPartIndex = new int[] {
            0
          };
        } else {
          addGeometryPartIndex = new int[] {
            0, 0
          };
        }
      }
    }
  }

  protected void addSelectedObjects(final List<LayerDataObject> objects,
    final LayerGroup group, final BoundingBox boundingBox) {
    final double scale = getViewport().getScale();
    for (final Layer layer : group.getLayers()) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        addSelectedObjects(objects, childGroup, boundingBox);
      } else if (layer instanceof DataObjectLayer) {
        final DataObjectLayer dataObjectLayer = (DataObjectLayer)layer;
        if (dataObjectLayer.isEditable(scale)) {
          final List<LayerDataObject> selectedObjects = dataObjectLayer.getSelectedRecords(boundingBox);
          objects.addAll(selectedObjects);
        }
      }
    }
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
        text.append("<table cellspacing=\"0\" cellpadding=\"1\"style=\"border: solid #999999 1px;margin: 3px 0px 3px 0px;width: 100%\"><thead style=\"background-color:#dddddd\"><tr><th style=\"border-right: solid #999999 1px\">"
          + idAttributeName + "</th><th>INDEX</th></tr></th><tbody>");
        for (final CloseLocation location : locations) {
          text.append("<tr style=\"border-top: solid #999999 1px\"><td style=\"border-right: solid #999999 1px\">");
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

  protected Geometry appendVertex(final Point newPoint) {
    final GeometryFactory geometryFactory = addLayer.getGeometryFactory();
    Geometry geometry = addGeometry;
    if (geometry.isEmpty()) {
      geometry = geometryFactory.createPoint(newPoint);
    } else {
      final DataType geometryDataType = addGeometryDataType;
      final int[] geometryPartIndex = addGeometryPartIndex;
      if (DataTypes.MULTI_POINT.equals(geometryDataType)) {
        if (geometry instanceof Point) {
          final Point point = (Point)geometry;
          geometry = geometryFactory.createMultiPoint(point, newPoint);
        } else {
          geometry = GeometryEditUtil.appendVertex(geometry, newPoint,
            geometryPartIndex);
        }
      } else if (DataTypes.LINE_STRING.equals(geometryDataType)
        || DataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
        if (geometry instanceof Point) {
          final Point point = (Point)geometry;
          geometry = geometryFactory.createLineString(point, newPoint);
        } else if (geometry instanceof LineString) {
          final LineString line = (LineString)geometry;
          geometry = GeometryEditUtil.appendVertex(line, newPoint,
            geometryPartIndex);
        } // TODO MultiLineString
      } else if (DataTypes.POLYGON.equals(geometryDataType)
        || DataTypes.MULTI_POLYGON.equals(geometryDataType)) {
        if (geometry instanceof Point) {
          final Point point = (Point)geometry;
          geometry = geometryFactory.createLineString(point, newPoint);
        } else if (geometry instanceof LineString) {
          final LineString line = (LineString)geometry;
          final Point p0 = line.getPointN(0);
          final Point p1 = line.getPointN(1);
          final LinearRing ring = geometryFactory.createLinearRing(p0, p1,
            newPoint, p0);
          geometry = geometryFactory.createPolygon(ring);
        } else if (geometry instanceof Polygon) {
          final Polygon polygon = (Polygon)geometry;
          geometry = GeometryEditUtil.appendVertex(polygon, newPoint,
            geometryPartIndex);
        }
        // TODO MultiPolygon
        // TODO Rings
      } else {
        // TODO multi point, oldGeometry collection
      }
    }
    return geometry;
  }

  protected void clearAddGeometry() {
    this.addCompleteAction = null;
    this.addLayer = null;
    this.addGeometry = null;
    this.addGeometryDataType = null;
    this.addGeometryPartDataType = null;
    setXorGeometry(null);
    repaint();
  }

  public void clearMouseOverGeometry() {
    clearMapCursor();
    this.mouseOverLocations = Collections.emptyList();
  }

  protected void clearMouseOverLocations() {
    setXorGeometry(null);
    clearMouseOverGeometry();
    repaint();
  }

  protected boolean clearMoveGeometry() {
    if (moveGeometryStart == null) {
      return false;
    } else {
      clearMouseOverGeometry();
      moveGeometryStart = null;
      repaint();
      return true;
    }
  }

  protected LineString createXorLine(final GeometryFactory geometryFactory,
    final Coordinates c0, final Point p1) {
    final Viewport2D viewport = getViewport();
    final GeometryFactory viewportGeometryFactory = viewport.getGeometryFactory();
    final Coordinates c1 = CoordinatesUtil.get(p1);
    final LineSegment line = new LineSegment(geometryFactory, c0, c1).convert(viewportGeometryFactory);
    final double length = line.getLength();
    final double cursorRadius = viewport.getModelUnitsPerViewUnit() * 6;
    final Coordinates newC1 = line.pointAlongOffset((length - cursorRadius)
      / length, 0);
    Point point = viewportGeometryFactory.createPoint(newC1);
    point = geometryFactory.copy(point);
    return geometryFactory.createLineString(c0, point);
  }

  protected CloseLocation findCloseLocation(final DataObjectLayer layer,
    final LayerDataObject object, final Geometry geometry,
    final BoundingBox boundingBox) {
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
      final DataObjectLayer layer = object.getLayer();
      final Geometry geometryValue = object.getGeometryValue();
      return findCloseLocation(layer, object, geometryValue, boundingBox);

    }
    return null;
  }

  private CloseLocation findCloseSegmentLocation(final DataObjectLayer layer,
    final LayerDataObject object, final Geometry geometry,
    final BoundingBox boundingBox) {

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
        return new CloseLocation(layer, object, geometry, null, closestSegment,
          null);
      }
    }
    return null;
  }

  protected CloseLocation findCloseVertexLocation(final DataObjectLayer layer,
    final LayerDataObject object, final Geometry geometry,
    final BoundingBox boundingBox) {
    final PointQuadTree<int[]> index = GeometryEditUtil.getPointQuadTree(geometry);
    if (index != null) {
      int[] closestVertexIndex = null;
      Coordinates closeVertex = null;
      final Coordinates centre = boundingBox.getCentre();

      final List<int[]> closeVertices = index.findWithin(boundingBox);
      Collections.sort(closeVertices, INT_ARRAY_COMPARATOR);
      double minDistance = Double.MAX_VALUE;
      for (final int[] vertexIndex : closeVertices) {
        final Coordinates vertex = GeometryEditUtil.getVertex(geometry,
          vertexIndex);
        if (vertex != null) {
          final double distance = vertex.distance(centre);
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

  protected void fireActionPerformed(final ActionListener listener,
    final String command) {
    if (listener != null) {
      final ActionEvent actionEvent = new ActionEvent(this, actionId++, command);
      listener.actionPerformed(actionEvent);
    }
  }

  public DataType getAddGeometryPartDataType() {
    return addGeometryPartDataType;
  }

  public DataObjectLayer getAddLayer() {
    return addLayer;
  }

  public Point getClosestPoint(final GeometryFactory geometryFactory,
    final LineSegment closestSegment, final Point point,
    final double maxDistance) {
    final Coordinates coordinates = CoordinatesUtil.get(point);
    final LineSegment segment = closestSegment.convert(geometryFactory);
    final Point fromPoint = segment.getPoint(0);
    final Point toPoint = segment.getPoint(1);
    final double fromPointDistance = point.distance(fromPoint);
    final double toPointDistance = point.distance(toPoint);
    if (fromPointDistance < maxDistance) {
      if (fromPointDistance <= toPointDistance) {
        return fromPoint;
      } else {
        return toPoint;
      }
    } else if (toPointDistance <= maxDistance) {
      return toPoint;
    } else {
      final Coordinates pointOnLine = segment.project(coordinates);
      return geometryFactory.createPoint(pointOnLine);
    }
  }

  private IndexedLineSegment getClosetSegment(
    final QuadTree<IndexedLineSegment> index, final BoundingBox boundingBox,
    final double maxDistance, final double... previousDistance) {
    final Point point = boundingBox.getCentrePoint();
    final Coordinates coordinates = CoordinatesUtil.get(point);

    double closestDistance = previousDistance[0];
    if (index == null) {
      return null;
    } else {
      final List<IndexedLineSegment> segments = index.query(boundingBox,
        "isWithinDistance", point, maxDistance);
      if (segments.isEmpty()) {
        return null;
      } else {
        IndexedLineSegment closestSegment = null;
        for (final IndexedLineSegment segment : segments) {
          final double distance = segment.distance(coordinates);
          if (distance < closestDistance) {
            closestSegment = segment;
            closestDistance = distance;
            previousDistance[0] = distance;
          }
        }
        return closestSegment;
      }
    }
  }

  public DataType getGeometryPartDataType(final DataType dataType) {
    if (Arrays.asList(DataTypes.POINT, DataTypes.MULTI_POINT)
      .contains(dataType)) {
      return DataTypes.POINT;
    } else if (Arrays.asList(DataTypes.LINE_STRING, DataTypes.MULTI_LINE_STRING)
      .contains(dataType)) {
      return DataTypes.LINE_STRING;
    } else if (Arrays.asList(DataTypes.POLYGON, DataTypes.MULTI_POLYGON)
      .contains(dataType)) {
      return DataTypes.POLYGON;
    } else {
      return DataTypes.GEOMETRY;
    }
  }

  protected Graphics2D getGraphics2D() {
    return getGraphics();
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

  private double getMaxDistance(final BoundingBox boundingBox) {
    return Math.max(boundingBox.getWidth() / 2, boundingBox.getHeight()) / 2;
  }

  protected Point getPoint(final GeometryFactory geometryFactory,
    final MouseEvent event) {
    final Viewport2D viewport = getViewport();
    final java.awt.Point eventPoint = event.getPoint();
    final Point point = viewport.toModelPointRounded(geometryFactory,
      eventPoint);
    return point;
  }

  protected List<LayerDataObject> getSelectedObjects(
    final BoundingBox boundingBox) {
    final List<LayerDataObject> objects = new ArrayList<LayerDataObject>();
    addSelectedObjects(objects, getProject(), boundingBox);
    return objects;
  }

  @SuppressWarnings("unchecked")
  protected Set<DataObjectLayer> getSnapLayers() {
    final Set<DataObjectLayer> layers = new LinkedHashSet<DataObjectLayer>();
    if (isModeAddGeometry()) {
      layers.add(addLayer);
    } else {
      for (final CloseLocation location : mouseOverLocations) {
        final DataObjectLayer layer = location.getLayer();
        if (layer != null) {
          final List<String> layerNames = (List<String>)layer.getProperty("snapLayers");
          if (layerNames == null) {
            layers.add(layer);
          } else {
            final LayerGroup project = layer.getProject();
            final MapPanel map = MapPanel.get(project);
            final double scale = map.getScale();
            for (final String layerName : layerNames) {
              final Layer snapLayer = project.getLayer(layerName);
              if (snapLayer instanceof DataObjectLayer) {
                if (snapLayer.isVisible(scale)) {
                  layers.add((DataObjectLayer)snapLayer);
                }
              }
            }
          }
        }
      }
    }
    return layers;
  }

  protected Geometry getVertexGeometry(final MouseEvent event,
    final CloseLocation location) {
    final Geometry geometry = location.getGeometry();
    final DataType geometryDataType = DataTypes.getType(geometry);
    final DataType geometryPartDataType = getGeometryPartDataType(geometryDataType);

    int previousPointOffset;
    int nextPointOffset;
    int[] index = location.getVertexIndex();
    if (index == null) {
      previousPointOffset = 0;
      nextPointOffset = 1;
      index = location.getSegment().getIndex();
    } else {
      previousPointOffset = -1;
      nextPointOffset = 1;
    }
    final GeometryFactory geometryFactory = location.getGeometryFactory();
    if (DataTypes.GEOMETRY.equals(geometryPartDataType)) {
    } else if (DataTypes.POINT.equals(geometryPartDataType)) {
    } else {
      final Point point = getPoint(geometryFactory, event);
      final CoordinatesList points = GeometryEditUtil.getPoints(geometry, index);
      final int pointIndex = index[index.length - 1];
      int previousPointIndex = pointIndex + previousPointOffset;
      int nextPointIndex = pointIndex + nextPointOffset;
      Coordinates previousPoint = null;
      Coordinates nextPoint = null;

      final int numPoints = points.size();
      if (DataTypes.LINE_STRING.equals(geometryPartDataType)) {
        if (numPoints > 1) {
          previousPoint = points.get(previousPointIndex);
          nextPoint = points.get(nextPointIndex);
        }
      } else if (DataTypes.POLYGON.equals(geometryPartDataType)) {
        if (numPoints == 2) {
          previousPoint = points.get(previousPointIndex);
          nextPoint = points.get(nextPointIndex);
        } else if (numPoints > 3) {
          while (previousPointIndex < 0) {
            previousPointIndex += numPoints - 1;
          }
          previousPointIndex = previousPointIndex % (numPoints - 1);
          previousPoint = points.get(previousPointIndex);

          while (nextPointIndex < 0) {
            nextPointIndex += numPoints - 1;
          }
          nextPointIndex = nextPointIndex % (numPoints - 1);
          nextPoint = points.get(nextPointIndex);
        }
      }

      final List<LineString> pointsList = new ArrayList<LineString>();
      if (previousPoint != null) {
        pointsList.add(createXorLine(geometryFactory, previousPoint, point));
      }
      if (nextPoint != null) {
        pointsList.add(createXorLine(geometryFactory, nextPoint, point));
      }
      if (!pointsList.isEmpty()) {
        return geometryFactory.createMultiLineString(pointsList);
      }
    }
    return null;
  }

  private boolean hasSnapPoint(final BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = Project.get().getGeometryFactory();
    final Point point = boundingBox.getCentrePoint();
    final Set<DataObjectLayer> layers = getSnapLayers();
    snapPoint = null;
    Boolean nodeSnap = null;
    for (final DataObjectLayer layer : layers) {
      final List<LayerDataObject> objects = layer.query(boundingBox);
      final double maxDistance = getMaxDistance(boundingBox);
      double closestDistance = Double.MAX_VALUE;
      for (final LayerDataObject object : objects) {
        if (layer.isVisible(object)) {
          // if (object != this.mouseOverObject) {
          final Geometry geometry = geometryFactory.copy(object.getGeometryValue());
          if (geometry != null) {
            final QuadTree<IndexedLineSegment> index = GeometryEditUtil.getLineSegmentQuadTree(geometry);
            final IndexedLineSegment closeSegment = getClosetSegment(index,
              boundingBox, maxDistance, Double.MAX_VALUE);
            if (closeSegment != null) {
              final Point closestPoint = getClosestPoint(geometryFactory,
                closeSegment, point, maxDistance);
              final double distance = point.distance(closestPoint);
              if (JtsGeometryUtil.isFromPoint(geometry, snapPoint)
                || JtsGeometryUtil.isToPoint(geometry, snapPoint)) {
                if (distance < closestDistance || nodeSnap != Boolean.TRUE) {
                  snapPoint = closestPoint;
                  closestDistance = distance;
                }
                nodeSnap = true;
              } else if (nodeSnap != Boolean.TRUE) {
                if (distance < closestDistance) {
                  snapPoint = closestPoint;
                  nodeSnap = false;
                  closestDistance = distance;
                }
              }
            }
            // }
          }
        }
      }
    }
    if (nodeSnap == Boolean.FALSE) {
      setMapCursor(CURSOR_LINE_SNAP);
    } else if (nodeSnap == Boolean.TRUE) {
      setMapCursor(CURSOR_NODE_SNAP);
    }
    return snapPoint != null;

  }

  protected boolean isEditable(final DataObjectLayer dataObjectLayer) {
    return dataObjectLayer.isVisible() && dataObjectLayer.isCanEditRecords();
  }

  protected boolean isGeometryValid(final Geometry geometry) {
    if (DataTypes.POINT.equals(addGeometryDataType)) {
      if (geometry instanceof Point) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.MULTI_POINT.equals(addGeometryDataType)) {
      if ((geometry instanceof Point) || (geometry instanceof MultiPoint)) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.LINE_STRING.equals(addGeometryDataType)) {
      if (geometry instanceof LineString) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.MULTI_LINE_STRING.equals(addGeometryDataType)) {
      if ((geometry instanceof LineString)
        || (geometry instanceof MultiLineString)) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.POLYGON.equals(addGeometryDataType)) {
      if (geometry instanceof Polygon) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.MULTI_POLYGON.equals(addGeometryDataType)) {
      if ((geometry instanceof Polygon) || (geometry instanceof MultiPolygon)) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  protected boolean isModeAddGeometry() {
    return addLayer != null;
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    super.keyPressed(e);
    final int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_ALT) {
      mouseMoved(e);
    }
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    super.keyReleased(e);
    final int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
      if (!mouseOverLocations.isEmpty()) {
        final MultipleUndo edit = new MultipleUndo();
        for (final CloseLocation location : mouseOverLocations) {
          final Geometry geometry = location.getGeometry();
          final int[] vertexIndex = location.getVertexIndex();
          if (vertexIndex != null) {
            final Geometry newGeometry = GeometryEditUtil.deleteVertex(
              geometry, vertexIndex);
            final UndoableEdit geometryEdit = setGeometry(location, newGeometry);
            edit.addEdit(geometryEdit);
          }
        }
        if (!edit.isEmpty()) {
          addUndo(edit);
        }
        clearMouseOverLocations();
      }
    } else if (keyCode == KeyEvent.VK_ESCAPE) {
      if (!mouseOverLocations.isEmpty()) {
        clearMouseOverLocations();
      } else if (clearMoveGeometry()) {
      } else if (isModeAddGeometry()) {
        if (addCompleteAction != null) {
          addCompleteAction.addComplete(this, null);
        }
        clearAddGeometry();
      }
    } else if (keyCode == KeyEvent.VK_CONTROL) {
      if (!isModeAddGeometry()) {
        clearMouseOverLocations();
      }
    } else if (keyCode == KeyEvent.VK_ALT) {
      if (moveGeometryStart != null) {
        mouseMoved(e);
      }
    }
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    if (splitLineKeyPress(e)) {
    } else {
      super.keyTyped(e);
    }
  }

  protected boolean modeAddMouseClick(final MouseEvent event) {
    if (SwingUtilities.isLeftMouseButton(event)) {
      if (isModeAddGeometry()) {
        if (mouseOverLocations.isEmpty()) {
          final Point point;
          if (snapPoint == null) {
            point = getPoint(event);
          } else {
            point = snapPoint;
          }
          if (addGeometry.isEmpty()) {
            setAddGeometry(appendVertex(point));
          } else {
            final Coordinates previousPoint = GeometryEditUtil.getVertex(
              addGeometry, addGeometryPartIndex, -1);
            if (!CoordinatesUtil.get(point).equals(previousPoint)) {
              setAddGeometry(appendVertex(point));
            }
          }

          setXorGeometry(null);
          event.consume();
          if (DataTypes.POINT.equals(addGeometryDataType)) {
            actionGeometryCompleted();
            repaint();
          }
          if (event.getClickCount() == 2 && isGeometryValid(addGeometry)) {
            actionGeometryCompleted();
            repaint();
          }
          return true;
        } else {
          Toolkit.getDefaultToolkit().beep();
        }
      }
    }
    return false;
  }

  protected boolean modeAddMouseMoved(final MouseEvent event) {
    if (isModeAddGeometry()) {
      final BoundingBox boundingBox = getHotspotBoundingBox(event);
      Point point = getPoint(event);
      // TODO make work with multi-part
      final Point fromPoint = JtsGeometryUtil.getFromPoint(addGeometry);
      final boolean snapToFirst = !event.isControlDown()
        && boundingBox.contains(fromPoint);
      if (snapToFirst
        || !updateAddMouseOverGeometry(event.getPoint(), boundingBox)) {
        if (snapToFirst) {
          setMapCursor(CURSOR_NODE_SNAP);
          snapPoint = fromPoint;
          point = fromPoint;
        } else if (!hasSnapPoint(boundingBox)) {
          setMapCursor(CURSOR_NODE_ADD);
        }
        final Coordinates firstPoint = GeometryEditUtil.getVertex(addGeometry,
          addGeometryPartIndex, 0);
        Geometry xorGeometry = null;
        if (DataTypes.POINT.equals(addGeometryPartDataType)) {
        } else {
          final GeometryFactory geometryFactory = addLayer.getGeometryFactory();
          if (DataTypes.LINE_STRING.equals(addGeometryPartDataType)) {
            final Coordinates previousPoint = GeometryEditUtil.getVertex(
              addGeometry, addGeometryPartIndex, -1);
            if (previousPoint != null) {
              xorGeometry = createXorLine(geometryFactory, previousPoint, point);
            }
          } else if (DataTypes.POLYGON.equals(addGeometryPartDataType)) {
            final Coordinates previousPoint = GeometryEditUtil.getVertex(
              addGeometry, addGeometryPartIndex, -1);
            if (previousPoint != null) {
              if (previousPoint.equals(firstPoint)) {
                xorGeometry = createXorLine(geometryFactory, previousPoint,
                  point);
              } else {
                final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory();
                xorGeometry = viewportGeometryFactory.createLineString(
                  previousPoint, point, firstPoint);
              }
            }
          } else {

          }
        }
        final Graphics2D graphics = getGraphics();
        setXorGeometry(graphics, xorGeometry);
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (modeAddMouseClick(event)) {
    } else {
      super.mouseClicked(event);
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    if (moveGeometryStart != null) {
      repaint();
      return;
    } else if (SwingUtilities.isLeftMouseButton(event)) {
      dragged = true;
      if (event.isAltDown() && !mouseOverLocations.isEmpty()) {
        moveGeometryStart = event.getPoint();
        return;
      }
    }

    final BoundingBox boundingBox = getHotspotBoundingBox(event);

    if (!mouseOverLocations.isEmpty()) {
      Geometry xorGeometry = null;
      for (final CloseLocation location : mouseOverLocations) {
        final Geometry locationGeometry = getVertexGeometry(event, location);
        if (locationGeometry != null) {
          if (xorGeometry == null) {
            xorGeometry = locationGeometry;
          } else {
            xorGeometry = xorGeometry.union(locationGeometry);
          }
        }
      }
      final Graphics2D graphics = getGraphics();
      setXorGeometry(graphics, xorGeometry);
      if (!hasSnapPoint(boundingBox)) {
        setMapCursor(CURSOR_NODE_ADD);
      }
    } else {
      super.mouseDragged(event);
    }
  }

  protected void mouseMoved(final KeyEvent e) {
    int modifiers;
    if (e.getID() == KeyEvent.KEY_PRESSED) {
      modifiers = MouseEvent.ALT_MASK;
    } else {
      modifiers = 0;
    }

    final java.awt.Point mousePosition = getMap().getMapMousePosition();
    final MouseEvent event = new MouseEvent((Component)e.getSource(),
      MouseEvent.MOUSE_MOVED, e.getWhen(), modifiers, mousePosition.x,
      mousePosition.y, 0, false);
    mouseMoved(event);
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
    final java.awt.Point point = event.getPoint();
    if (modeAddMouseMoved(event)) {
    } else {
      final Graphics2D graphics = getGraphics();
      final BoundingBox boundingBox = getHotspotBoundingBox(event);
      if (updateMouseOverGeometry(point, graphics, boundingBox)) {

      } else {
        clearMapCursor();
      }
    }
    if (event.isAltDown()) {
      if (!mouseOverLocations.isEmpty()) {
        setMapCursor(CURSOR_MOVE);
      }
    }
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    if (SwingUtilities.isLeftMouseButton(event)) {
      dragged = false;
    }
    if (SwingUtil.isLeftButtonAndNoModifiers(event)) {
      if (isModeAddGeometry()) {
      } else if (!mouseOverLocations.isEmpty()) {
        repaint();
        event.consume();
        return;
      }
    }
    super.mousePressed(event);
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (moveGeometryStart != null) {
      moveGeometryFinish(event);
    } else if (dragged && !mouseOverLocations.isEmpty()) {
      vertexDragFinish(event);
    } else {
      super.mouseReleased(event);
    }
    if (SwingUtilities.isLeftMouseButton(event)) {
      dragged = false;
    }
  }

  protected void moveGeometryFinish(final MouseEvent event) {
    // TODO move geometry
    for (final CloseLocation location : mouseOverLocations) {
      final GeometryFactory geometryFactory = location.getGeometryFactory();
      final Point startPoint = geometryFactory.copy(getViewportPoint(moveGeometryStart));
      final Point endPoint = geometryFactory.copy(getViewportPoint(event));

      final double deltaX = endPoint.getX() - startPoint.getX();
      final double deltaY = endPoint.getY() - startPoint.getY();
      final Geometry newGeometry = GeometryEditUtil.moveGeometry(
        location.getGeometry(), deltaX, deltaY);
      final UndoableEdit geometryEdit = setGeometry(location, newGeometry);
      addUndo(geometryEdit);
      clearMoveGeometry();
    }
  }

  @Override
  public void paintComponent(final Graphics2D graphics) {
    final LayerGroup layerGroup = getProject();
    paint(graphics, layerGroup);
    if (moveGeometryStart != null) {
      final AffineTransform transform = graphics.getTransform();
      try {
        final java.awt.Point mousePoint = getMap().getMapMousePosition();
        if (mousePoint != null) {
          final int deltaX = mousePoint.x - moveGeometryStart.x;
          final int deltaY = mousePoint.y - moveGeometryStart.y;
          graphics.translate(deltaX, deltaY);
          paintSelected(graphics, addGeometry, STYLE_HIGHLIGHT, STYLE_OUTLINE,
            STYLE_VERTEX);
        }
      } finally {
        graphics.setTransform(transform);
      }
    } else {
      paintSelected(graphics, addGeometry, STYLE_HIGHLIGHT, STYLE_OUTLINE,
        STYLE_VERTEX);
    }
    paintSelectBox(graphics);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    final Object source = event.getSource();
    final String propertyName = event.getPropertyName();

    if ("preEditable".equals(propertyName)) {
      actionGeometryCompleted();
    } else if ("editable".equals(propertyName)) {
      repaint();
      if (source == addLayer) {
        // if (!isEditable(addLayer)) {
        // setEditingObject(null, null);
        // }
      }
    } else if (source instanceof LayerDataObject) {
      if (event.getNewValue() instanceof Geometry) {
        // TODO update mouse over locations
        // clearMouseOverLocations();
      }
    }
  }

  private void setAddGeometry(final Geometry geometry) {
    if (!JtsGeometryUtil.equalsExact3D(geometry, addGeometry)) {
      addUndo(new AddGeometryUndoEdit(geometry));
      repaint();
    }
  }

  protected void setAddGeometryDataType(final DataType dataType) {
    this.addGeometryDataType = dataType;
    this.addGeometryPartDataType = getGeometryPartDataType(dataType);
  }

  protected UndoableEdit setGeometry(final CloseLocation location,
    final Geometry newGeometry) {
    if (isModeAddGeometry()) {
      if (JtsGeometryUtil.equalsExact3D(newGeometry, addGeometry)) {
        return null;
      } else {
        return new AddGeometryUndoEdit(newGeometry);
      }
    } else {
      final LayerDataObject object = location.getObject();
      final DataObjectMetaData metaData = location.getMetaData();
      final String geometryAttributeName = metaData.getGeometryAttributeName();
      final Geometry oldValue = object.getValue(geometryAttributeName);
      if (JtsGeometryUtil.equalsExact3D(newGeometry, oldValue)) {
        return null;
      } else {
        final DataObjectLayer layer = location.getLayer();
        return layer.createPropertyEdit(object, geometryAttributeName,
          oldValue, newGeometry);
      }
    }
  }

  protected boolean setMouseOverLocations(final java.awt.Point eventPoint,
    final List<CloseLocation> closeLocations) {
    this.mouseOverLocations = closeLocations;
    if (mouseOverLocations.isEmpty()) {
      clearMapCursor();
      return false;
    } else {
      snapPoint = null;
      final Graphics2D graphics = getGraphics();
      setXorGeometry(graphics, null);
      final Map<String, Set<CloseLocation>> vertexLocations = new TreeMap<String, Set<CloseLocation>>();
      final Map<String, Set<CloseLocation>> segmentLocations = new TreeMap<String, Set<CloseLocation>>();

      for (final CloseLocation location : closeLocations) {
        final String typePath = location.getTypePath();
        if (location.getVertexIndex() == null) {
          CollectionUtil.addToSet(segmentLocations, typePath, location);
        } else {
          CollectionUtil.addToSet(vertexLocations, typePath, location);
        }
      }
      final StringBuffer text = new StringBuffer("<html>");
      appendLocations(text, "Move Verticies", vertexLocations);
      appendLocations(text, "Insert Verticies", segmentLocations);
      text.append("</html>");
      getMap().setToolTipText(eventPoint, 18, -12, text);

      if (vertexLocations.isEmpty()) {
        setMapCursor(CURSOR_LINE_ADD_NODE);
      } else {
        setMapCursor(CURSOR_NODE_EDIT);
      }
      return true;
    }
  }

  protected boolean splitLineKeyPress(final KeyEvent e) {
    if ((e.getKeyCode() == KeyEvent.VK_K)
      && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
      if (!isModeAddGeometry() && !mouseOverLocations.isEmpty()) {
        for (final CloseLocation mouseLocation : mouseOverLocations) {
          final LayerDataObject object = mouseLocation.getObject();
          final DataObjectLayer layer = object.getLayer();
          final Geometry geometry = mouseLocation.getGeometry();
          if (geometry instanceof LineString) {
            final LineString line = (LineString)geometry;
            final int[] vertexIndex = mouseLocation.getVertexIndex();
            final Coordinates coordinates = mouseLocation.getPoint();
            final LineString[] lines = null;
            if (vertexIndex == null) {

            } else {

            }
            final DirectionalAttributes property = DirectionalAttributes.getProperty(object);
            final LineString line1 = lines[0];
            final LineString line2 = lines[2];

            final LayerDataObject object2 = layer.copyObject(object);
            object.setGeometryValue(line1);
            object2.setGeometryValue(line1);

            property.setSplitAttributes(line, coordinates, object);
            property.setSplitAttributes(line, coordinates, object2);

            layer.saveChanges(object2);
          }
          e.consume();
          return true;
        }
      }
    }
    return false;
  }

  private boolean updateAddMouseOverGeometry(final java.awt.Point eventPoint,
    final BoundingBox boundingBox) {
    final CloseLocation location = findCloseLocation(addLayer, null,
      addGeometry, boundingBox);
    final List<CloseLocation> locations = new ArrayList<CloseLocation>();
    if (location != null) {
      locations.add(location);
    }

    return setMouseOverLocations(eventPoint, locations);
  }

  private boolean updateMouseOverGeometry(final java.awt.Point eventPoint,
    final Graphics2D graphics, final BoundingBox boundingBox) {
    final List<LayerDataObject> selectedObjects = getSelectedObjects(boundingBox);
    final List<CloseLocation> closeLocations = new ArrayList<CloseLocation>();
    for (final LayerDataObject object : selectedObjects) {
      final CloseLocation closeLocation = findCloseLocation(object, boundingBox);
      if (closeLocation != null) {
        closeLocations.add(closeLocation);
      }
    }
    return setMouseOverLocations(eventPoint, closeLocations);
  }

  protected void vertexDragFinish(final MouseEvent event) {
    try {
      if (event != null) {
        final MultipleUndo edit = new MultipleUndo();
        for (final CloseLocation location : mouseOverLocations) {
          final Geometry geometry = location.getGeometry();
          final GeometryFactory geometryFactory = location.getGeometryFactory();
          final Point point;
          if (snapPoint == null) {
            point = getPoint(geometryFactory, event);
          } else {
            point = geometryFactory.copy(snapPoint);
          }
          final int[] vertexIndex = location.getVertexIndex();
          Geometry newGeometry;
          final Coordinates newPoint = CoordinatesUtil.get(point);
          if (vertexIndex == null) {
            final int[] segmentIndex = location.getSegment().getIndex();
            final int[] newIndex = segmentIndex.clone();
            newIndex[newIndex.length - 1] = newIndex[newIndex.length - 1] + 1;
            newGeometry = GeometryEditUtil.insertVertex(geometry, newPoint,
              newIndex);
          } else {
            newGeometry = GeometryEditUtil.moveVertex(geometry, newPoint,
              vertexIndex);
          }
          final UndoableEdit geometryEdit = setGeometry(location, newGeometry);
          edit.addEdit(geometryEdit);
        }
        if (!edit.isEmpty()) {
          addUndo(edit);
        }
      }
    } finally {
      clearMouseOverLocations();
    }
  }

}
