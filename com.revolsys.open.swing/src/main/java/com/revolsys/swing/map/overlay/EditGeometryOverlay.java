package com.revolsys.swing.map.overlay;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.revolsys.comparator.IntArrayComparator;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.algorithm.index.PointQuadTree;
import com.revolsys.gis.algorithm.index.quadtree.QuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
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
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.renderer.MarkerStyleRenderer;
import com.revolsys.swing.undo.AbstractUndoableEdit;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

@SuppressWarnings("serial")
public class EditGeometryOverlay extends SelectFeaturesOverlay implements
  PropertyChangeListener, MouseListener, MouseMotionListener {

  private class AddGeometryUndoEdit extends AbstractUndoableEdit {

    private final Geometry oldGeometry = addGeometry;

    private final Geometry newGeometry;

    private final int[] geometryPartIndex = addGeometryPartIndex;

    private final DataType geometryPartDataType = addGeometryPartDataType;

    private AddGeometryUndoEdit(final Geometry geometry) {
      this.newGeometry = geometry;
      System.out.println("UC\t" + oldGeometry);
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
    public void redo() throws CannotRedoException {
      super.redo();
      System.out.println("UR\t" + newGeometry);
      addGeometry = newGeometry;
      setXorGeometry(null);
      repaint();
    }

    @Override
    public void undo() throws CannotUndoException {
      super.undo();
      System.out.println("UU\t" + oldGeometry);
      addGeometry = oldGeometry;
      addGeometryPartDataType = geometryPartDataType;
      addGeometryPartIndex = geometryPartIndex;
      setXorGeometry(null);
      repaint();
    }
  }

  private static final Cursor ADD_NODE_CURSOR = SilkIconLoader.getCursor(
    "cursor_new_node", 8, 7);

  private static final Cursor EDIT_NODE_CURSOR = SilkIconLoader.getCursor(
    "cursor_edit_node", 8, 7);

  private static final IntArrayComparator INT_ARRAY_COMPARATOR = new IntArrayComparator();

  private static final Cursor SNAP_CURSOR = SilkIconLoader.getCursor(
    "cursor_snap", 8, 7);

  private int actionId = 0;

  private AddGeometryCompleteAction addCompleteAction;

  private Geometry addGeometry;

  private DataType addGeometryDataType;

  private DataType addGeometryPartDataType;

  /** Index to the part of the addGeometry that new points should be added too. */
  private int[] addGeometryPartIndex = {};

  private DataObjectLayer addLayer;

  private String mode = "edit";

  private Geometry mouseOverGeometry;

  private LayerDataObject mouseOverObject;

  private Point mouseOverPoint;

  private IndexedLineSegment mouseOverSegment;

  private int[] mouseOverVertexIndex;

  private final Project project;

  private Point snapPoint;

  private final Viewport2D viewport;

  public EditGeometryOverlay(final MapPanel map) {
    super(map);

    this.viewport = map.getViewport();
    this.project = map.getProject();
    setGeometryFactory(viewport.getGeometryFactory());
  }

  protected void actionGeometryCompleted() {
    if (isGeometryValid()) {
      try {
        setXorGeometry(null);
        if (isModeAddGeometry()) {
          if (addCompleteAction != null) {
            final Geometry geometry = getGeometryFactory().copy(
              this.addGeometry);
            final LayerDataObject newObject = addCompleteAction.addComplete(
              this, geometry);
            addLayer.addSelectedObjects(newObject);
            this.addCompleteAction = null;
            this.addLayer = null;
            this.addGeometry = null;
            this.addGeometryDataType = null;
            this.addGeometryPartDataType = null;
            mode = "edit";
          }
        }
      } finally {
        clearMapCursor();
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
        mode = "add";
        this.addLayer = layer;
        this.addCompleteAction = addCompleteAction;
        final GeometryFactory geometryFactory = metaData.getGeometryFactory();
        this.setGeometryFactory(geometryFactory);
        clearUndoHistory();
        this.addGeometry = geometryFactory.createEmptyGeometry();
        setAddGeometryDataType(geometryAttribute.getType());
        setMapCursor(ADD_NODE_CURSOR);

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
          final List<LayerDataObject> selectedObjects = dataObjectLayer.getSelectedObjects(boundingBox);
          objects.addAll(selectedObjects);
        }
      }
    }
  }

  protected Geometry appendVertex(final Point newPoint) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    Geometry geometry = getGeometry();
    if (geometry.isEmpty()) {
      geometry = geometryFactory.createPoint(newPoint);
    } else {
      final DataType geometryDataType = getGeometryDataType();
      final int[] geometryPartIndex = getGeometryPartIndex();
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

  protected void clearMouseOverVertex() {
    setXorGeometry(null);
    clearMapCursor();
    mouseOverObject = null;
    mouseOverPoint = null;
    mouseOverGeometry = null;
    mouseOverVertexIndex = null;
    mouseOverSegment = null;
    repaint();
  }

  protected LineString createXorLine(final Coordinates c0, final Point p1) {
    final GeometryFactory geometryFactory = getCurrentGeometryFactory();
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

  private void drawVertexXor(final MouseEvent event, final int[] vertexIndex,
    final int previousPointOffset, final int nextPointOffset) {
    Geometry xorGeometry = null;

    DataType geometryPartDataType = addGeometryPartDataType;
    Geometry geometry;
    if (isModeAddGeometry()) {
      geometry = addGeometry;
      geometryPartDataType = addGeometryPartDataType;
    } else if (mouseOverObject != null) {
      geometry = mouseOverGeometry;
      // TODO need to improve this based on the part
      final DataType geometryDataType = DataTypes.getType(mouseOverGeometry);
      geometryPartDataType = getGeometryPartDataType(geometryDataType);
    } else {
      geometry = null;
    }
    final GeometryFactory geometryFactory = getCurrentGeometryFactory();
    if (geometry == null) {
    } else if (DataTypes.GEOMETRY.equals(geometryPartDataType)) {
    } else if (DataTypes.POINT.equals(geometryPartDataType)) {
    } else {
      final Point point = getPoint(geometryFactory, event);
      if (geometry != null) {
        final CoordinatesList points = GeometryEditUtil.getPoints(geometry,
          vertexIndex);
        final int pointIndex = vertexIndex[vertexIndex.length - 1];
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
          pointsList.add(createXorLine(previousPoint, point));
        }
        if (nextPoint != null) {
          pointsList.add(createXorLine(nextPoint, point));
        }
        if (!pointsList.isEmpty()) {
          xorGeometry = geometryFactory.createMultiLineString(pointsList);
        }
      }
    }
    final Graphics2D graphics = getGraphics();
    setXorGeometry(graphics, xorGeometry);
  }

  private boolean findCloseSegment(final LayerDataObject object,
    final BoundingBox boundingBox, final double[] previousDistance) {

    final GeometryFactory viewportGeometryFactory = getViewport().getGeometryFactory();
    final Geometry geometry = object.getGeometryValue();
    final Geometry convertedGeometry = viewportGeometryFactory.copy(geometry);

    final double maxDistance = getMaxDistance(boundingBox);
    final QuadTree<IndexedLineSegment> lineSegments = GeometryEditUtil.createLineSegmentQuadTree(convertedGeometry);

    final IndexedLineSegment closetSegment = getClosetSegment(lineSegments,
      boundingBox, maxDistance, previousDistance);
    if (closetSegment == null) {
      return false;
    } else {
      mouseOverGeometry = geometry;
      mouseOverObject = object;
      mouseOverSegment = closetSegment;
      return true;
    }
  }

  protected boolean findCloseVertex(final LayerDataObject object,
    final BoundingBox boundingBox, final Point previousPoint) {
    final Geometry geometry = object.getGeometryValue();
    return findCloseVertex(object, geometry, boundingBox, previousPoint);
  }

  protected boolean findCloseVertex(final LayerDataObject object,
    final Geometry geometry, final BoundingBox boundingBox,
    final Point previousPoint) {
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);

    Coordinates currentCoordinates = CoordinatesUtil.get(geometryFactory.copy(previousPoint));
    final PointQuadTree<int[]> index = GeometryEditUtil.createPointQuadTree(geometry);
    if (index != null) {
      final Coordinates centre = boundingBox.getCentre();

      final List<int[]> closeVertices = index.findWithin(boundingBox);
      Collections.sort(closeVertices, INT_ARRAY_COMPARATOR);
      if (!closeVertices.isEmpty()) {
        double minDistance = Double.MAX_VALUE;
        for (final int[] vertexIndex : closeVertices) {
          final Coordinates vertex = GeometryEditUtil.getVertex(geometry,
            vertexIndex);
          if (vertex != null) {
            final double distance = vertex.distance(centre);
            if (distance < minDistance) {
              minDistance = distance;
              if (currentCoordinates == null
                || !currentCoordinates.equals(vertex)
                || this.mouseOverVertexIndex == null) {
                currentCoordinates = vertex;
                this.mouseOverObject = object;
                this.mouseOverGeometry = geometry;
                this.mouseOverVertexIndex = vertexIndex;
                this.mouseOverPoint = geometryFactory.createPoint(vertex);
              }
            }
          }
        }
      }
    }
    return currentCoordinates != null;
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

  protected GeometryFactory getCurrentGeometryFactory() {
    if (isModeAddGeometry()) {
      return getGeometryFactory();
    } else if (mouseOverObject != null) {
      final DataObjectMetaData metaData = mouseOverObject.getMetaData();
      return metaData.getGeometryFactory();
    } else {
      return project.getGeometryFactory();
    }
  }

  private Geometry getGeometry() {
    if (isModeAddGeometry()) {
      return addGeometry;
    } else {
      return mouseOverGeometry;
    }
  }

  public String getGeometryAttributeName() {
    final DataObjectMetaData metaData = getMetaData();
    if (metaData == null) {
      return null;
    } else {
      return metaData.getGeometryAttributeName();
    }
  }

  protected DataType getGeometryDataType() {
    if (isModeAddGeometry()) {
      return addGeometryDataType;
    } else {
      final DataObjectMetaData metaData = getMetaData();
      if (metaData != null) {
        final Attribute geometryAttribute = metaData.getGeometryAttribute();
        if (geometryAttribute != null) {
          return geometryAttribute.getType();
        }
      }
    }
    return null;
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

  protected int[] getGeometryPartIndex() {
    if (isModeAddGeometry()) {
      return addGeometryPartIndex;
    } else if (mouseOverVertexIndex != null) {
      final int[] partIndex = new int[mouseOverVertexIndex.length - 1];
      System.arraycopy(mouseOverVertexIndex, 0, partIndex, 0, partIndex.length);
      return partIndex;
    } else {
      return null;
    }
  }

  protected Graphics2D getGraphics2D() {
    return getGraphics();
  }

  protected BoundingBox getHotspotBoundingBox(final MouseEvent event) {
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

  protected DataObjectMetaData getMetaData() {
    if (isModeAddGeometry()) {
      return addLayer.getMetaData();
    } else if (mouseOverGeometry == null) {
      return null;
    } else {
      return mouseOverObject.getMetaData();
    }
  }

  protected Point getPoint(final GeometryFactory geometryFactory,
    final MouseEvent event) {
    final Point point;
    final java.awt.Point eventPoint = event.getPoint();
    point = viewport.toModelPointRounded(geometryFactory, eventPoint);
    return point;
  }

  protected List<LayerDataObject> getSelectedObjects(
    final BoundingBox boundingBox) {
    final List<LayerDataObject> objects = new ArrayList<LayerDataObject>();
    addSelectedObjects(objects, project, boundingBox);
    return objects;
  }

  private boolean hasSnapPoint(final BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getCurrentGeometryFactory();
    final Point point = boundingBox.getCentrePoint();
    final DataObjectLayer layer;
    if (isModeAddGeometry()) {
      layer = this.addLayer;
    } else if (this.mouseOverObject == null) {
      return false;
    } else {
      layer = this.mouseOverObject.getLayer();
    }
    final List<LayerDataObject> objects = layer.getDataObjects(boundingBox);
    snapPoint = null;
    final double maxDistance = getMaxDistance(boundingBox);
    double closestDistance = Double.MAX_VALUE;
    for (final LayerDataObject object : objects) {
      if (object != this.mouseOverObject) {
        final Geometry geometry = geometryFactory.copy(object.getGeometryValue());
        if (geometry != null) {
          final QuadTree<IndexedLineSegment> index = GeometryEditUtil.createLineSegmentQuadTree(geometry);
          final IndexedLineSegment closeSegment = getClosetSegment(index,
            boundingBox, maxDistance, closestDistance);
          if (closeSegment != null) {
            snapPoint = getClosestPoint(geometryFactory, closeSegment, point,
              maxDistance);
            closestDistance = point.distance(snapPoint);
          }
        }
      }
    }
    return snapPoint != null;

  }

  protected boolean isEditable(final DataObjectLayer dataObjectLayer) {
    return dataObjectLayer.isVisible() && dataObjectLayer.isCanEditObjects();
  }

  protected boolean isGeometryValid() {
    final Geometry geometry = getGeometry();
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
    return "add".equals(mode);
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    super.keyReleased(e);
    final int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_BACK_SPACE) {
      if (mouseOverVertexIndex != null) {
        final Geometry geometry = getGeometry();
        final Geometry newGeometry = GeometryEditUtil.deleteVertex(geometry,
          mouseOverVertexIndex);
        clearMouseOverVertex();
        if (newGeometry != null && !geometry.isEmpty()) {
          setGeometry(newGeometry);
        }
      }
    } else if (keyCode == KeyEvent.VK_ESCAPE) {
      if (mouseOverVertexIndex != null) {
        vertexMoveFinish(null);
      } else if (mouseOverSegment != null) {
        vertexAddFinish(null);
      }
    } else if (keyCode == KeyEvent.VK_CONTROL) {
      if (!isModeAddGeometry()) {
        clearMouseOverVertex();
      }
    }
  }

  protected void modeAddMouseClick(final MouseEvent event) {
    if (SwingUtilities.isLeftMouseButton(event)) {
      if (isModeAddGeometry()) {

        final Point point;
        if (snapPoint == null) {
          point = getPoint(event);
        } else {
          point = viewport.getRoundedGeometry(snapPoint);
        }
        if (addGeometry.isEmpty()) {
          setGeometry(appendVertex(point));
        } else {
          final Coordinates previousPoint = GeometryEditUtil.getVertex(
            addGeometry, getGeometryPartIndex(), -1);
          if (!CoordinatesUtil.get(point).equals(previousPoint)) {
            setGeometry(appendVertex(point));
          }
        }

        setXorGeometry(null);
        event.consume();
        if (DataTypes.POINT.equals(addGeometryDataType)) {
          actionGeometryCompleted();
          repaint();
        }
        if (event.getClickCount() == 2 && isGeometryValid()) {
          actionGeometryCompleted();
          repaint();
        }
      }
    }
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (event.isControlDown()
      || ((mouseOverVertexIndex == null && mouseOverSegment == null) && isModeAddGeometry())) {
      modeAddMouseClick(event);
    } else {
      super.mouseClicked(event);
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    final BoundingBox boundingBox = getHotspotBoundingBox(event);

    if (mouseOverVertexIndex != null) {
      drawVertexXor(event, mouseOverVertexIndex, -1, 1);
      if (hasSnapPoint(boundingBox)) {
        setMapCursor(SNAP_CURSOR);
      } else {
        setMapCursor(ADD_NODE_CURSOR);
      }
    } else if (mouseOverSegment != null) {
      final int[] index = mouseOverSegment.getIndex();
      drawVertexXor(event, index, 0, 1);
      if (hasSnapPoint(boundingBox)) {
        setMapCursor(SNAP_CURSOR);
      } else {
        setMapCursor(ADD_NODE_CURSOR);
      }
    } else {
      super.mouseDragged(event);
    }
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
    final Graphics2D graphics = getGraphics();
    final Point point = getPoint(event);
    final BoundingBox boundingBox = getHotspotBoundingBox(event);
    if (!updateMouseOverGeometry(graphics, boundingBox)) {
      if (addGeometry != null) {
        if (!updateMouseOverGeometry(graphics, boundingBox)) {
          if (isModeAddGeometry() || event.isControlDown()) {
            if (hasSnapPoint(boundingBox)) {
              setMapCursor(SNAP_CURSOR);
            } else {
              setMapCursor(ADD_NODE_CURSOR);
            }
            final Coordinates firstPoint = GeometryEditUtil.getVertex(
              addGeometry, getGeometryPartIndex(), 0);
            Geometry xorGeometry = null;
            if (DataTypes.POINT.equals(addGeometryPartDataType)) {
            } else if (DataTypes.LINE_STRING.equals(addGeometryPartDataType)) {
              final Coordinates previousPoint = GeometryEditUtil.getVertex(
                addGeometry, getGeometryPartIndex(), -1);
              if (previousPoint != null) {
                xorGeometry = createXorLine(previousPoint, point);
              }
            } else if (DataTypes.POLYGON.equals(addGeometryPartDataType)) {
              final Coordinates previousPoint = GeometryEditUtil.getVertex(
                addGeometry, getGeometryPartIndex(), -1);
              if (previousPoint != null) {
                if (previousPoint.equals(firstPoint)) {
                  xorGeometry = createXorLine(previousPoint, point);
                } else {
                  final GeometryFactory geometryFactory = getGeometryFactory();
                  xorGeometry = geometryFactory.createLineString(previousPoint,
                    point, firstPoint);
                }
              }
            } else {

            }
            setXorGeometry(graphics, xorGeometry);
          } else {
            clearMapCursor();
          }
        }
      }
    }
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    if (mode != null) {
      if (SwingUtil.isLeftButtonAndNoModifiers(event)) {
        if (isModeAddGeometry() || mouseOverVertexIndex != null
          || mouseOverSegment != null) {
          repaint();
          event.consume();
          return;
        }
      }
    }
    super.mousePressed(event);
  }

  /*
   * @Override public void selectObjects(final BoundingBox boundingBox) { final
   * Project project = getProject(); if (!selectObjects(project, boundingBox)) {
   * setEditingObject(null, null); } } protected boolean selectObjects(final
   * LayerGroup group, final BoundingBox boundingBox) { boolean found = false;
   * for (final Layer addLayer : group.getLayers()) { final double scale =
   * getViewport().getScale(); if (addLayer instanceof LayerGroup) { final
   * LayerGroup childGroup = (LayerGroup)addLayer; found |=
   * selectObjects(childGroup, boundingBox); } else if (addLayer instanceof
   * DataObjectLayer) { final DataObjectLayer dataObjectLayer =
   * (DataObjectLayer)addLayer; if (dataObjectLayer.isEditable(scale)) { final
   * DataObjectMetaData metaData = dataObjectLayer.getMetaData(); if (metaData
   * != null) { if (metaData.getGeometryAttributeIndex() != -1) { final
   * List<LayerDataObject> objects =
   * dataObjectLayer.getDataObjects(boundingBox); for (final LayerDataObject
   * object : objects) { final Geometry oldGeometry = object.getGeometryValue();
   * if (oldGeometry != null) { final Polygon selectPolygon =
   * boundingBox.toPolygon(1); if (getViewport().getGeometryFactory()
   * .project(oldGeometry) .intersects(selectPolygon)) {
   * dataObjectLayer.setEditingObjects(Collections.singleton(object));
   * setEditingObject(dataObjectLayer, object); return true; } } } } } } } }
   * return found; }
   */
  // public void setEditingObject(final DataObjectLayer layer,
  // final LayerDataObject object) {
  // clearEditingObjects(project);
  // this.addLayer = layer;
  // final LayerDataObject oldValue = this.object;
  // if (oldValue != null) {
  // actionGeometryCompleted();
  // }
  // this.object = object;
  // Geometry oldGeometry = null;
  //
  // if (object != null) {
  // final DataObjectMetaData metaData = layer.getMetaData();
  // final Attribute geometryAttribute = metaData.getGeometryAttribute();
  // if (geometryAttribute != null) {
  // setAddGeometryDataType(geometryAttribute.getType());
  // oldGeometry = object.getGeometryValue();
  // } else {
  // setAddGeometryDataType(DataTypes.GEOMETRY);
  // }
  // layer.setEditingObjects(Collections.singletonList(object));
  // }
  // setGeometry(oldGeometry);
  //
  // mode = "edit";
  // firePropertyChange("object", oldValue, object);
  // }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (mouseOverVertexIndex != null) {
      vertexMoveFinish(event);
    } else if (mouseOverSegment != null) {
      vertexAddFinish(event);
    } else {
      super.mouseReleased(event);
    }
  }

  @Override
  public void paintComponent(final Graphics2D graphics) {
    final Project layerGroup = getProject();
    paint(graphics, layerGroup);

    if (addGeometry != null) {
      final GeometryFactory viewGeometryFactory = viewport.getGeometryFactory();
      final Geometry mapGeometry = viewGeometryFactory.copy(addGeometry);
      for (int i = 0; i < mapGeometry.getNumGeometries(); i++) {
        final Geometry part = mapGeometry.getGeometryN(i);
        if (!(part instanceof Point)) {
          GeometryStyleRenderer.renderGeometry(viewport, graphics, part,
            getHighlightStyle());
          GeometryStyleRenderer.renderOutline(viewport, graphics, part,
            getOutlineStyle());
        }
      }
      MarkerStyleRenderer.renderMarkerVertices(viewport, graphics, mapGeometry,
        getVertexStyle());
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
    } else if (source == this.mouseOverObject) {
      if (EqualsRegistry.equal(propertyName, getGeometryAttributeName())) {
        setGeometry(this.mouseOverObject.getGeometryValue());
      }
    }
  }

  protected void setAddGeometryDataType(final DataType dataType) {
    this.addGeometryDataType = dataType;
    this.addGeometryPartDataType = getGeometryPartDataType(dataType);
  }

  private void setGeometry(final Geometry geometry) {
    System.out.println("GS\t" + geometry);
    if (isModeAddGeometry()) {
      if (!JtsGeometryUtil.equalsExact3D(geometry, addGeometry)) {
        addUndo(new AddGeometryUndoEdit(geometry));
        this.addGeometry = geometry;
      }
    } else if (mouseOverObject != null) {
      final String geometryAttributeName = getGeometryAttributeName();
      final Geometry oldValue = mouseOverObject.getValue(geometryAttributeName);
      if (!JtsGeometryUtil.equalsExact3D(geometry, oldValue)) {
        mouseOverObject.setGeometryValue(geometry);
        this.mouseOverGeometry = geometry;
        createPropertyUndo(mouseOverObject, geometryAttributeName, oldValue,
          geometry);
      }
    }
    repaint();
  }

  private boolean updateMouseOverGeometry(final Graphics2D graphics,
    final BoundingBox boundingBox) {
    final Point previousPoint = this.mouseOverPoint;
    this.mouseOverSegment = null;
    this.mouseOverObject = null;
    this.mouseOverGeometry = null;
    this.mouseOverVertexIndex = null;
    this.mouseOverPoint = null;
    if (!isModeAddGeometry()
      || !findCloseVertex(null, addGeometry, boundingBox, previousPoint)) {
      final List<LayerDataObject> selectedObjects = getSelectedObjects(boundingBox);
      if (!selectedObjects.isEmpty()) {
        for (final LayerDataObject object : selectedObjects) {
          findCloseVertex(object, boundingBox, previousPoint);
        }
        if (mouseOverVertexIndex == null) {
          final double[] previousDistance = {
            Double.MAX_VALUE
          };
          for (final LayerDataObject object : selectedObjects) {
            findCloseSegment(object, boundingBox, previousDistance);
          }
        }
      }
    }
    if (mouseOverVertexIndex == null && mouseOverSegment == null) {
      this.mouseOverObject = null;
      clearMapCursor();
      return false;
    } else {
      snapPoint = null;
      setXorGeometry(graphics, null);
      setMapCursor(EDIT_NODE_CURSOR);
      return true;
    }
  }

  protected void vertexAddFinish(final MouseEvent event) {
    try {
      if (event != null) {
        final GeometryFactory geometryFactory = getCurrentGeometryFactory();
        final Point point = getPoint(geometryFactory, event);
        final Coordinates coordinates = CoordinatesUtil.get(point);
        int[] index = mouseOverSegment.getIndex();
        index = index.clone();
        index[index.length - 1] = index[index.length - 1] + 1;
        final Geometry geometry = getGeometry();
        final Geometry newGeometry = GeometryEditUtil.insertVertex(geometry,
          coordinates, index);
        setGeometry(newGeometry);
      }
    } finally {
      clearMouseOverVertex();
    }
  }

  protected void vertexMoveFinish(final MouseEvent event) {
    try {
      if (event != null) {
        final Point point;
        final GeometryFactory geometryFactory = getCurrentGeometryFactory();
        if (snapPoint == null) {
          point = getPoint(geometryFactory, event);
        } else {
          point = geometryFactory.copy(snapPoint);
        }

        final Geometry geometry = getGeometry();
        final Geometry newGeometry = GeometryEditUtil.moveVertex(geometry,
          CoordinatesUtil.get(point), mouseOverVertexIndex);
        setGeometry(newGeometry);
      }
    } finally {
      clearMouseOverVertex();
    }
  }
}
