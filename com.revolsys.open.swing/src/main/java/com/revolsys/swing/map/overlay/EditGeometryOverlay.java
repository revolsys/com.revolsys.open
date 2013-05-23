package com.revolsys.swing.map.overlay;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
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
import java.util.Comparator;
import java.util.List;

import javax.swing.SwingUtilities;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.algorithm.index.PointQuadTree;
import com.revolsys.gis.algorithm.index.quadtree.QuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
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
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.renderer.MarkerStyleRenderer;
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

  private final Project project;

  final Viewport2D viewport;

  private Geometry geometry;

  private Point firstPoint;

  private Point previousPoint;

  private DataType geometryDataType;

  private DataObjectLayer layer;

  private String mode;

  private int actionId = 0;

  private DataObject object;

  private final Cursor addNodeCursor = SilkIconLoader.getCursor(
    "cursor_new_node", 8, 7);

  private ActionListener completedAction;

  private int[] mouseOverVertexId;

  private IndexedLineSegment mouseOverSegment;

  private PointQuadTree<int[]> vertices;

  private QuadTree<IndexedLineSegment> lineSegments;

  public EditGeometryOverlay(final MapPanel map) {
    super(map, new Color(0, 255, 255));

    this.viewport = map.getViewport();
    this.project = map.getProject();
    this.setGeometryFactory(viewport.getGeometryFactory());
  }

  protected void actionGeometryCompleted() {
    if (isGeometryValid()) {
      try {
        previousPoint = null;
        setXorGeometry(null);
        if ("add".equals(mode)) {
          if (layer != null) {
            final DataObject object = layer.createObject();
            if (object != null) {
              object.setGeometryValue(geometry);
            }
            this.object = object;
            fireActionPerformed(completedAction, "Geometry Complete");
            this.object = null;
            setEditingObject(layer, object);
          }
        } else if ("edit".equals(mode)) {
          if (object != null) {
            object.setGeometryValue(geometry);
            fireActionPerformed(completedAction, "Geometry Complete");
          }
        }

      } finally {
        clearMapCursor();
      }
    }
  }

  /**
   * Set the layer that a new feature is to be added to.
   * 
   * @param layer 
   */
  public void addObject(final DataObjectLayer layer,
    final ActionListener completedAction) {
    setEditingObject(null, null);
    if (layer != null) {
      final DataObjectMetaData metaData = layer.getMetaData();
      final Attribute geometryAttribute = metaData.getGeometryAttribute();
      if (geometryAttribute != null) {
        mode = "add";
        this.layer = layer;
        this.completedAction = completedAction;
        GeometryFactory geometryFactory = metaData.getGeometryFactory();
        this.setGeometryFactory(geometryFactory);
        this.geometry = geometryFactory.createEmptyGeometry();
        this.geometryDataType = geometryAttribute.getType();
        this.vertices = new PointQuadTree<int[]>();
        this.lineSegments = new QuadTree<IndexedLineSegment>();
        setMapCursor(addNodeCursor);

        if (Arrays.asList(DataTypes.POINT, DataTypes.LINE_STRING).contains(
          geometryDataType)) {
          appendVertexGeometryIndex = new int[0];
        } else if (Arrays.asList(DataTypes.MULTI_POINT,
          DataTypes.MULTI_LINE_STRING, DataTypes.POLYGON).contains(
          geometryDataType)) {
          appendVertexGeometryIndex = new int[] {
            0
          };
        } else {
          appendVertexGeometryIndex = new int[] {
            0, 0
          };
        }
      }
    }
  }

  private void clearEditingObjects(final LayerGroup layerGroup) {
    for (final Layer layer : layerGroup.getLayers()) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        clearEditingObjects(childGroup);
      }
      if (layer instanceof DataObjectLayer) {
        final DataObjectLayer dataObjectLayer = (DataObjectLayer)layer;
        dataObjectLayer.clearEditingObjects();
      }
    }

  }

  protected void clearMouseOverVertex() {
    setXorGeometry(null);
    mouseOverVertexId = null;
    mouseOverSegment = null;
    repaint();
  }

  /** Index to the part of the geometry that new points should be added too. */
  private int[] appendVertexGeometryIndex = {};

  // TODO indexes may get switched for clockwise vs not and when vertices are
  // moved
  protected Geometry createGeometry(Point newPoint) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    Geometry geometry = this.geometry;
    if (geometry.isEmpty()) {
      geometry = geometryFactory.createPoint(newPoint);
    } else if (DataTypes.MULTI_POINT.equals(geometryDataType)) {
      if (geometry instanceof Point) {
        Point point = (Point)geometry;
        geometry = geometryFactory.createMultiPoint(point, newPoint);
      } else {
        geometry = GeometryEditUtil.appendVertex(geometry, newPoint,
          appendVertexGeometryIndex);
      }
    } else if (DataTypes.LINE_STRING.equals(geometryDataType)
      || DataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
      if (geometry instanceof Point) {
        Point point = (Point)geometry;
        geometry = geometryFactory.createLineString(point, newPoint);
      } else if (geometry instanceof LineString) {
        LineString line = (LineString)geometry;
        geometry = GeometryEditUtil.appendVertex(line, newPoint,
          appendVertexGeometryIndex);
      } // TODO MultiLineString
    } else if (DataTypes.POLYGON.equals(geometryDataType)
      || DataTypes.MULTI_POLYGON.equals(geometryDataType)) {
      if (geometry instanceof Point) {
        Point point = (Point)geometry;
        geometry = geometryFactory.createLineString(point, newPoint);
      } else if (geometry instanceof LineString) {
        LineString line = (LineString)geometry;
        Point p0 = line.getPointN(0);
        Point p1 = line.getPointN(1);
        LinearRing ring = geometryFactory.createLinearRing(p0, p1, newPoint, p0);
        geometry = geometryFactory.createPolygon(ring);
      } else if (geometry instanceof Polygon) {
        Polygon polygon = (Polygon)geometry;
        geometry = GeometryEditUtil.appendVertex(polygon, newPoint,
          appendVertexGeometryIndex);
      }
      // TODO MultiPolygon
      // TODO Rings
    } else {
      // TODO multi point, geometry collection
    }
    vertices = GeometryEditUtil.createPointQuadTree(geometry);
    lineSegments = GeometryEditUtil.createLineSegmentQuadTree(geometry);
    return geometry;
  }

  protected LineString createXorLine(final Coordinates c0, final Point p1) {
    final GeometryFactory viewportGeometryFactory = viewport.getGeometryFactory();
    final Coordinates c1 = CoordinatesUtil.get(viewportGeometryFactory.copy(p1));
    final LineSegment line = new LineSegment(viewportGeometryFactory, c0, c1);
    final double length = line.getLength();
    final double cursorRadius = viewport.getModelUnitsPerViewUnit() * 6;
    final Coordinates newC1 = line.pointAlongOffset((length - cursorRadius)
      / length, 0);
    Point point = viewportGeometryFactory.createPoint(newC1);
    point = getGeometryFactory().copy(point);
    return getGeometryFactory().createLineString(c0, point);
  }

  private void drawVertexXor(final MouseEvent event, final int[] vertexIndex,
    final int previousPointOffset, final int nextPointOffset) {
    final Graphics2D graphics = getGraphics();

    final Point point = getPoint(event);

    final List<LineString> pointsList = new ArrayList<LineString>();
    final Coordinates previousPoint = GeometryEditUtil.getCoordinatesOffset(
      geometry, vertexIndex, previousPointOffset);
    if (previousPoint != null) {
      pointsList.add(createXorLine(previousPoint, point));
    }
    final Coordinates nextPoint = GeometryEditUtil.getCoordinatesOffset(
      geometry, vertexIndex, nextPointOffset);
    if (nextPoint != null) {
      pointsList.add(createXorLine(nextPoint, point));
    }
    if (pointsList.isEmpty()) {
      setXorGeometry(graphics, point);
    } else {
      setXorGeometry(graphics,
        getGeometryFactory().createMultiLineString(pointsList));
    }
  }

  protected void fireActionPerformed(final ActionListener listener,
    final String command) {
    if (listener != null) {
      final ActionEvent actionEvent = new ActionEvent(this, actionId++, command);
      listener.actionPerformed(actionEvent);
    }
  }

  private Geometry getCloseSegment(final MouseEvent event) {
    final Point point = getPoint(event);
    final double maxDistance = getDistance(event);

    final BoundingBox boundingBox = BoundingBox.getBoundingBox(point).expand(
      maxDistance);

    final Coordinates coordinates = CoordinatesUtil.get(point);

    final List<IndexedLineSegment> segments = lineSegments.query(boundingBox,
      "isWithinDistance", point, maxDistance);
    if (segments.isEmpty()) {
      mouseOverSegment = null;
      return null;
    } else {
      double closestDistance = Double.MAX_VALUE;
      IndexedLineSegment closestSegment = null;
      for (final IndexedLineSegment segment : segments) {
        final double distance = segment.distance(coordinates);
        if (distance < closestDistance) {
          closestSegment = segment;
          closestDistance = distance;
        }
      }
      mouseOverSegment = closestSegment;
      final GeometryFactory viewportGeometryFactory = viewport.getGeometryFactory();
      final Coordinates viewportCoordinates = CoordinatesUtil.get(viewportGeometryFactory.project(point));
      final LineSegment segment = closestSegment.convert(viewportGeometryFactory);
      final Coordinates pointOnLine = segment.project(viewportCoordinates);
      return viewportGeometryFactory.createPoint(pointOnLine);
    }
  }

  private Geometry getCloseVertex(final MouseEvent event) {
    Geometry currentPoint = null;
    Coordinates currentCoordinates = null;
    if (mouseOverVertexId != null && getXorGeometry() instanceof Point) {
      currentPoint = getXorGeometry();
      currentCoordinates = CoordinatesUtil.get(getXorGeometry());
    }
    mouseOverVertexId = null;
    if (vertices != null) {
      final Point point = getPoint(event);
      final double maxDistance = getDistance(event);
      final BoundingBox boundingBox = BoundingBox.getBoundingBox(point).expand(
        maxDistance);

      final List<int[]> closeVertices = vertices.findWithin(boundingBox);
      Collections.sort(closeVertices, new Comparator<int[]>() {
        @Override
        public int compare(final int[] object1, final int[] object2) {
          for (int i = 0; i < Math.max(object1.length, object2.length); i++) {
            if (i >= object1.length) {
              return -1;
            } else if (i >= object2.length) {
              return 1;
            } else {
              final int value1 = object1[i];
              final int value2 = object2[i];
              if (value1 < value2) {
                return -1;
              } else if (value1 > value2) {
                return 1;
              }
            }
          }
          return 0;
        }
      });
      if (!closeVertices.isEmpty()) {
        double minDistance = Double.MAX_VALUE;
        for (final int[] vertexIndex : closeVertices) {
          final Coordinates vertex = GeometryEditUtil.getVertex(geometry,
            vertexIndex);
          final double distance = vertex.distance(CoordinatesUtil.get(point));
          if (distance < minDistance) {
            mouseOverVertexId = vertexIndex;
            minDistance = distance;
            if (currentPoint == null || currentCoordinates == null
              || !currentCoordinates.equals(vertex)) {
              currentPoint = getGeometryFactory().createPoint(vertex);
            }
          }
        }
      }
    }
    return currentPoint;
  }

  protected Graphics2D getGraphics2D() {
    return getGraphics();
  }

  public DataObjectLayer getLayer() {
    return layer;
  }

  public DataObject getObject() {
    return object;
  }

  protected Point getPoint(final MouseEvent event) {
    final Point point = getViewportPoint(event);
    return getGeometryFactory().copy(point);
  }

  protected boolean isEditable(final DataObjectLayer dataObjectLayer) {
    return dataObjectLayer.isVisible() && dataObjectLayer.isCanEditObjects();
  }

  protected boolean isGeometryValid() {
    if (DataTypes.POINT.equals(geometryDataType)) {
      if (geometry instanceof Point) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.MULTI_POINT.equals(geometryDataType)) {
      if ((geometry instanceof Point) || (geometry instanceof MultiPoint)) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.LINE_STRING.equals(geometryDataType)) {
      if (geometry instanceof LineString) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
      if ((geometry instanceof LineString)
        || (geometry instanceof MultiLineString)) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.POLYGON.equals(geometryDataType)) {
      if (geometry instanceof Polygon) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.MULTI_POLYGON.equals(geometryDataType)) {
      if ((geometry instanceof Polygon) || (geometry instanceof MultiPolygon)) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  @Override
  protected boolean isSelectable(final DataObjectLayer dataObjectLayer) {
    return isEditable(dataObjectLayer);
  }

  @Override
  public boolean isSelectEvent(final MouseEvent event) {
    if (!"add".equals(mode) && SwingUtilities.isLeftMouseButton(event)) {
      final boolean keyPress = event.isAltDown();
      return keyPress;
    }
    return false;
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    super.keyReleased(e);
    final int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_BACK_SPACE) {
      if (mouseOverVertexId != null) {
        setGeometry(GeometryEditUtil.deleteVertex(geometry, mouseOverVertexId));
        clearMouseOverVertex();
        repaint();
      }
    } else if (keyCode == KeyEvent.VK_ESCAPE) {
      if (mouseOverVertexId != null) {
        vertexMoveFinish(null);
      } else if (mouseOverSegment != null) {
        vertexAddFinish(null);
      }
    }
  }

  protected void modeAddMouseClick(final MouseEvent event) {
    if (SwingUtilities.isLeftMouseButton(event)) {
      if ("add".equals(mode)) {
        final Point point = getPoint(event);
        if (geometry.isEmpty()) {
          previousPoint = point;
          geometry = createGeometry(point);
        } else {
          if (!point.equals(previousPoint)) {
            geometry = createGeometry(point);
            previousPoint = point;
          }
        }

        setXorGeometry(null);
        event.consume();
        if (DataTypes.POINT.equals(geometryDataType)) {
          actionGeometryCompleted();
        }
        if (event.getClickCount() == 2 && isGeometryValid()) {
          actionGeometryCompleted();
        }
        repaint();
      }
    }
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if ((mouseOverVertexId == null && mouseOverSegment == null)
      && "add".equals(mode)) {
      modeAddMouseClick(event);
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    if (mouseOverVertexId != null) {
      drawVertexXor(event, mouseOverVertexId, -1, 1);
    } else if (mouseOverSegment != null) {
      final int[] index = mouseOverSegment.getIndex();
      drawVertexXor(event, index, 0, 1);
    } else {
      super.mouseDragged(event);
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
    super.mouseEntered(e);
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    super.mouseExited(e);
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
    if (geometry != null) {

      final Graphics2D graphics = getGraphics();

      final Point point = getPoint(event);

      if (!updateMouseOverGeometry(graphics, event)) {
        if ("add".equals(mode)) {
          setMapCursor(addNodeCursor);
          Coordinates firstPoint = GeometryEditUtil.getVertex(geometry,
            appendVertexGeometryIndex, 0);
          if (firstPoint == null
            || Arrays.asList(DataTypes.POINT, DataTypes.MULTI_POINT).contains(
              geometryDataType)) {
            setXorGeometry(graphics, null);
          } else if (previousPoint == null) {
            setXorGeometry(graphics, createXorLine(firstPoint, point));
          } else if (DataTypes.LINE_STRING.equals(geometryDataType)
            || DataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
            setXorGeometry(graphics, createXorLine(CoordinatesUtil.get(previousPoint), point));
          } else {
            setXorGeometry(
              graphics,
              getGeometryFactory().createLineString(previousPoint, point,
                firstPoint));
          }
        }
      }
    }
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    if (mode != null) {
      if (SwingUtil.isLeftButtonAndNoModifiers(event)) {
        if ("add".equals(mode) || mouseOverVertexId != null
          || mouseOverSegment != null) {
          repaint();
          event.consume();
          return;
        }
      }
    }
    super.mousePressed(event);
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (mouseOverVertexId != null) {
      vertexMoveFinish(event);
    } else if (mouseOverSegment != null) {
      vertexAddFinish(event);
    } else {
      super.mouseReleased(event);
    }
  }

  @Override
  public void paintComponent(final Graphics graphics) {
    final Graphics2D graphics2d = (Graphics2D)graphics;
    if (geometry != null) {
      final GeometryFactory viewGeometryFactory = viewport.getGeometryFactory();
      final Geometry mapGeometry = viewGeometryFactory.copy(geometry);
      for (int i = 0; i < mapGeometry.getNumGeometries(); i++) {
        Geometry part = mapGeometry.getGeometryN(i);
        if (!(part instanceof Point)) {
          GeometryStyleRenderer.renderGeometry(viewport, graphics2d, part,
            getHighlightStyle());
          GeometryStyleRenderer.renderOutline(viewport, graphics2d, part,
            getOutlineStyle());
        }
      }
      MarkerStyleRenderer.renderMarkerVertices(viewport, graphics2d,
        mapGeometry, getVertexStyle());
    }
    paintSelectBox(graphics2d);
    drawXorGeometry(graphics2d);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    final String propertyName = event.getPropertyName();
    if ("preEditable".equals(propertyName)) {
      actionGeometryCompleted();
    } else if ("editable".equals(propertyName)) {
      if (event.getSource() == layer) {
        if (!isEditable(layer)) {
          setEditingObject(null, null);
        }
      }
    }
  }

  @Override
  public void selectObjects(final BoundingBox boundingBox) {
    final Project project = getProject();
    if (!selectObjects(project, boundingBox)) {
      setEditingObject(null, null);
    }
  }

  protected boolean selectObjects(final LayerGroup group,
    final BoundingBox boundingBox) {
    boolean found = false;
    for (final Layer layer : group.getLayers()) {
      final double scale = getViewport().getScale();
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        found |= selectObjects(childGroup, boundingBox);
      } else if (layer instanceof DataObjectLayer) {
        final DataObjectLayer dataObjectLayer = (DataObjectLayer)layer;
        if (dataObjectLayer.isEditable(scale)) {
          final DataObjectMetaData metaData = dataObjectLayer.getMetaData();
          if (metaData != null) {
            if (metaData.getGeometryAttributeIndex() != -1) {
              final List<DataObject> objects = dataObjectLayer.getDataObjects(boundingBox);
              for (final DataObject object : objects) {
                final Geometry geometry = object.getGeometryValue();
                if (geometry != null) {
                  final Polygon selectPolygon = boundingBox.toPolygon(1);
                  if (getViewport().getGeometryFactory()
                    .project(geometry)
                    .intersects(selectPolygon)) {
                    dataObjectLayer.setEditingObjects(Collections.singleton(object));
                    setEditingObject(dataObjectLayer, object);
                    return true;
                  }
                }
              }
            }
          }
        }
      }
    }
    return found;
  }

  public void setEditingObject(final DataObjectLayer layer,
    final DataObject object) {
    clearEditingObjects(project);
    this.layer = layer;
    final DataObject oldValue = this.object;
    if (oldValue != null) {
      actionGeometryCompleted();
    }
    this.completedAction = null;
    this.object = object;
    Geometry geometry = null;

    if (object != null) {
      final DataObjectMetaData metaData = layer.getMetaData();
      final Attribute geometryAttribute = metaData.getGeometryAttribute();
      if (geometryAttribute != null) {
        geometryDataType = geometryAttribute.getType();
        geometry = object.getGeometryValue();
      }
      layer.setEditingObjects(Collections.singletonList(object));
    }
    setGeometry(geometry);

    mode = "edit";
    firstPoint = null;
    firePropertyChange("object", oldValue, object);
  }

  protected void setGeometry(final Geometry geometry) {
    this.geometry = geometry;
    setXorGeometry(null);
    mouseOverVertexId = null;
    mouseOverSegment = null;
    if (geometry == null) {
      vertices = null;
      lineSegments = null;
      setGeometryFactory(null);
    } else {
      vertices = GeometryEditUtil.createPointQuadTree(geometry);
      lineSegments = GeometryEditUtil.createLineSegmentQuadTree(geometry);
      setGeometryFactory(GeometryFactory.getFactory(geometry));
    }
    repaint();
  }

  private boolean updateMouseOverGeometry(final Graphics2D graphics,
    final MouseEvent event) {
    Geometry currentGeometry = getCloseVertex(event);
    if (currentGeometry == null) {
      currentGeometry = getCloseSegment(event);
    }
    setXorGeometry(graphics, currentGeometry);
    if (getXorGeometry() == null) {
      clearMapCursor();
      return false;
    } else {
      setMapCursor(addNodeCursor);
      return true;
    }
  }

  protected void vertexAddFinish(final MouseEvent event) {
    try {
      if (event != null) {
        final Point point = getPoint(event);
        final Coordinates coordinates = CoordinatesUtil.get(point);
        int[] index = mouseOverSegment.getIndex();
        index = index.clone();
        index[index.length - 1] = index[index.length - 1] + 1;
        final Geometry newGeometry = GeometryEditUtil.insertVertex(geometry,
          coordinates, index);
        setGeometry(newGeometry);
      }
    } finally {
      clearMapCursor();
      clearMouseOverVertex();
    }
  }

  protected void vertexMoveFinish(final MouseEvent event) {
    // TODO if moved vertex is part of the current xor line update that too
    try {
      if (event != null) {
        final Point point = getPoint(event);
        final Geometry newGeometry = GeometryEditUtil.moveVertex(geometry,
          CoordinatesUtil.get(point), mouseOverVertexId);
        setGeometry(newGeometry);
      }
    } finally {
      clearMapCursor();
      clearMouseOverVertex();
    }
  }
}
