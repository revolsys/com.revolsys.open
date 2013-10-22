package com.revolsys.swing.map.overlay;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.SwingUtilities;
import javax.swing.undo.UndoableEdit;

import com.revolsys.famfamfam.silk.SilkIconLoader;
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
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.gis.model.geometry.util.GeometryEditUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.layer.dataobject.undo.SplitRecordUndo;
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

public class EditGeometryOverlay extends AbstractOverlay implements
  PropertyChangeListener, MouseListener, MouseMotionListener {

  private class AddGeometryUndoEdit extends AbstractUndoableEdit {

    private static final long serialVersionUID = 1L;

    private final Geometry oldGeometry = EditGeometryOverlay.this.addGeometry;

    private final Geometry newGeometry;

    private final int[] geometryPartIndex = EditGeometryOverlay.this.addGeometryPartIndex;

    private final DataType geometryPartDataType = EditGeometryOverlay.this.addGeometryPartDataType;

    private AddGeometryUndoEdit(final Geometry geometry) {
      this.newGeometry = geometry;
    }

    @Override
    public boolean canRedo() {
      if (super.canRedo()) {
        if (JtsGeometryUtil.equalsExact3D(this.oldGeometry,
          EditGeometryOverlay.this.addGeometry)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean canUndo() {
      if (super.canUndo()) {
        if (JtsGeometryUtil.equalsExact3D(this.newGeometry,
          EditGeometryOverlay.this.addGeometry)) {
          return true;
        }
      }
      return false;
    }

    @Override
    protected void doRedo() {
      EditGeometryOverlay.this.addGeometry = this.newGeometry;
      setXorGeometry(null);
      repaint();
    }

    @Override
    protected void doUndo() {
      EditGeometryOverlay.this.addGeometry = this.oldGeometry;
      EditGeometryOverlay.this.addGeometryPartDataType = this.geometryPartDataType;
      EditGeometryOverlay.this.addGeometryPartIndex = this.geometryPartIndex;
      setXorGeometry(null);
      repaint();
    }
  }

  private static final long serialVersionUID = 1L;

  private static final Cursor CURSOR_NODE_ADD = SilkIconLoader.getCursor(
    "cursor_node_add", 8, 7);

  private static final Cursor CURSOR_MOVE = SilkIconLoader.getCursor(
    "cursor_move", 8, 7);

  private int actionId = 0;

  private AddGeometryCompleteAction addCompleteAction;

  private Geometry addGeometry;

  private DataType addGeometryDataType;

  private DataType addGeometryPartDataType;

  /** Index to the part of the addGeometry that new points should be added too. */
  private int[] addGeometryPartIndex = {};

  private AbstractDataObjectLayer addLayer;

  private boolean dragged = false;

  private java.awt.Point moveGeometryStart;

  private int vertexDragModifiers;

  public EditGeometryOverlay(final MapPanel map) {
    super(map);
  }

  protected void actionGeometryCompleted() {
    if (isModeAddGeometry()) {
      if (isGeometryValid(this.addGeometry)) {
        try {
          setXorGeometry(null);
          if (this.addCompleteAction != null) {
            final Geometry geometry = this.addLayer.getGeometryFactory().copy(
              this.addGeometry);
            this.addCompleteAction.addComplete(this, geometry);
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
  public void addObject(final AbstractDataObjectLayer layer,
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
          this.addGeometryDataType)) {
          this.addGeometryPartIndex = new int[0];
        } else if (Arrays.asList(DataTypes.MULTI_POINT,
          DataTypes.MULTI_LINE_STRING, DataTypes.POLYGON).contains(
          this.addGeometryDataType)) {
          this.addGeometryPartIndex = new int[] {
            0
          };
        } else {
          this.addGeometryPartIndex = new int[] {
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
      } else if (layer instanceof AbstractDataObjectLayer) {
        final AbstractDataObjectLayer dataObjectLayer = (AbstractDataObjectLayer)layer;
        if (dataObjectLayer.isEditable(scale)) {
          final List<LayerDataObject> selectedObjects = dataObjectLayer.getSelectedRecords(boundingBox);
          objects.addAll(selectedObjects);
        }
      }
    }
  }

  protected boolean addSnapLayers(final Set<AbstractDataObjectLayer> layers,
    final Project project, final AbstractDataObjectLayer layer,
    final double scale) {
    if (layer != null) {
      if (layer.isSnapToAllLayers()) {
        return true;
      } else {
        layers.add(layer);
        final Collection<String> layerPaths = layer.getSnapLayerPaths();
        if (layerPaths != null) {
          for (final String layerPath : layerPaths) {
            final Layer snapLayer = project.getLayer(layerPath);
            if (snapLayer instanceof AbstractDataObjectLayer) {
              if (snapLayer.isVisible(scale)) {
                layers.add((AbstractDataObjectLayer)snapLayer);
              }
            }
          }
        }
      }
    }
    return false;
  }

  protected Geometry appendVertex(final Point newPoint) {
    final GeometryFactory geometryFactory = this.addLayer.getGeometryFactory();
    Geometry geometry = this.addGeometry;
    if (geometry.isEmpty()) {
      geometry = geometryFactory.createPoint(newPoint);
    } else {
      final DataType geometryDataType = this.addGeometryDataType;
      final int[] geometryPartIndex = this.addGeometryPartIndex;
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

  @Override
  public void clearMouseOverGeometry() {
    super.clearMouseOverGeometry();
  }

  protected void clearMouseOverLocations() {
    setXorGeometry(null);
    clearMouseOverGeometry();
    repaint();
  }

  protected boolean clearMoveGeometry() {
    if (this.moveGeometryStart == null) {
      return false;
    } else {
      clearMouseOverGeometry();
      this.moveGeometryStart = null;
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

  protected void fireActionPerformed(final ActionListener listener,
    final String command) {
    if (listener != null) {
      final ActionEvent actionEvent = new ActionEvent(this, this.actionId++,
        command);
      listener.actionPerformed(actionEvent);
    }
  }

  public DataType getAddGeometryPartDataType() {
    return this.addGeometryPartDataType;
  }

  public AbstractDataObjectLayer getAddLayer() {
    return this.addLayer;
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

  @Override
  protected List<AbstractDataObjectLayer> getSnapLayers() {
    final Project project = getProject();
    final double scale = MapPanel.get(project).getScale();
    final Set<AbstractDataObjectLayer> layers = new LinkedHashSet<AbstractDataObjectLayer>();
    boolean snapAll = false;
    if (isModeAddGeometry()) {
      snapAll = addSnapLayers(layers, project, this.addLayer, scale);
    } else {
      for (final CloseLocation location : getMouseOverLocations()) {
        final AbstractDataObjectLayer layer = location.getLayer();
        snapAll |= addSnapLayers(layers, project, layer, scale);
      }
    }
    if (snapAll) {
      final List<AbstractDataObjectLayer> visibleDescendants = project.getVisibleDescendants(
        AbstractDataObjectLayer.class, scale);
      return visibleDescendants;
    }
    return new ArrayList<AbstractDataObjectLayer>(layers);
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

  protected boolean isEditable(final AbstractDataObjectLayer dataObjectLayer) {
    return dataObjectLayer.isExists() && dataObjectLayer.isVisible()
      && dataObjectLayer.isCanEditRecords();
  }

  protected boolean isGeometryValid(final Geometry geometry) {
    if (DataTypes.POINT.equals(this.addGeometryDataType)) {
      if (geometry instanceof Point) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.MULTI_POINT.equals(this.addGeometryDataType)) {
      if (geometry instanceof Point || geometry instanceof MultiPoint) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.LINE_STRING.equals(this.addGeometryDataType)) {
      if (geometry instanceof LineString) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.MULTI_LINE_STRING.equals(this.addGeometryDataType)) {
      if (geometry instanceof LineString || geometry instanceof MultiLineString) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.POLYGON.equals(this.addGeometryDataType)) {
      if (geometry instanceof Polygon) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.MULTI_POLYGON.equals(this.addGeometryDataType)) {
      if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  protected boolean isModeAddGeometry() {
    return this.addLayer != null;
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
    final int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
      if (!getMouseOverLocations().isEmpty()) {
        final MultipleUndo edit = new MultipleUndo();
        for (final CloseLocation location : getMouseOverLocations()) {
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
      clearMouseOverLocations();
      clearMoveGeometry();
      if (this.addCompleteAction != null) {
        this.addCompleteAction.addComplete(this, null);
      }
      clearAddGeometry();

    } else if (keyCode == KeyEvent.VK_CONTROL) {
      if (!isModeAddGeometry()) {
        clearMouseOverLocations();
      }
    } else if (keyCode == KeyEvent.VK_ALT) {
      if (this.moveGeometryStart != null) {
        mouseMoved(e);
      }
    } else if (splitLineKeyPress(e)) {
    }
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    final char keyChar = e.getKeyChar();
    if (keyChar >= '1' && keyChar <= '9') {
      final int snapPointIndex = keyChar - '1';
      if (snapPointIndex < this.snapPointLocationMap.size()) {
        this.snapPointIndex = snapPointIndex;
        setSnapLocations(snapPointLocationMap);
      }
    } else {
      super.keyTyped(e);
    }
  }

  protected boolean modeAddMouseClick(final MouseEvent event) {
    if (SwingUtilities.isLeftMouseButton(event)) {
      if (isModeAddGeometry()) {
        if (getMouseOverLocations().isEmpty()) {
          Point point;
          if (getSnapPoint() == null) {
            point = getPoint(event);
          } else {
            point = getSnapPoint();
          }
          final GeometryFactory geometryFactory = this.addLayer.getGeometryFactory();

          point = geometryFactory.copy(point);
          if (this.addGeometry.isEmpty()) {
            setAddGeometry(appendVertex(point));
          } else {
            final Coordinates previousPoint = GeometryEditUtil.getVertex(
              this.addGeometry, this.addGeometryPartIndex, -1);
            if (!CoordinatesUtil.get(point).equals(previousPoint)) {
              setAddGeometry(appendVertex(point));
            }
          }

          setXorGeometry(null);
          event.consume();
          if (DataTypes.POINT.equals(this.addGeometryDataType)) {
            actionGeometryCompleted();
            repaint();
          }
          if (event.getClickCount() == 2 && isGeometryValid(this.addGeometry)) {
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
      final Point fromPoint = JtsGeometryUtil.getFromPoint(this.addGeometry);
      final boolean snapToFirst = !event.isControlDown()
        && boundingBox.contains(fromPoint);
      if (snapToFirst
        || !updateAddMouseOverGeometry(event.getPoint(), boundingBox)) {
        if (snapToFirst) {
          setMapCursor(CURSOR_NODE_SNAP);
          setSnapPoint(fromPoint);
          point = fromPoint;
        } else if (!hasSnapPoint(event, boundingBox)) {
          setMapCursor(CURSOR_NODE_ADD);
        }
        final Coordinates firstPoint = GeometryEditUtil.getVertex(
          this.addGeometry, this.addGeometryPartIndex, 0);
        Geometry xorGeometry = null;
        if (DataTypes.POINT.equals(this.addGeometryPartDataType)) {
        } else {
          final GeometryFactory geometryFactory = this.addLayer.getGeometryFactory();
          if (DataTypes.LINE_STRING.equals(this.addGeometryPartDataType)) {
            final Coordinates previousPoint = GeometryEditUtil.getVertex(
              this.addGeometry, this.addGeometryPartIndex, -1);
            if (previousPoint != null) {
              xorGeometry = createXorLine(geometryFactory, previousPoint, point);
            }
          } else if (DataTypes.POLYGON.equals(this.addGeometryPartDataType)) {
            final Coordinates previousPoint = GeometryEditUtil.getVertex(
              this.addGeometry, this.addGeometryPartIndex, -1);
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
        setXorGeometry(xorGeometry);
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (modeAddMouseClick(event)) {
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    if (!SwingUtilities.isMiddleMouseButton(event)) {
      if (this.moveGeometryStart != null) {
        repaint();
        return;
      } else if (SwingUtilities.isLeftMouseButton(event)) {
        this.dragged = true;
        if (event.isAltDown() && !getMouseOverLocations().isEmpty()) {
          this.moveGeometryStart = event.getPoint();
          return;
        }
      }

      final BoundingBox boundingBox = getHotspotBoundingBox(event);

      if (!getMouseOverLocations().isEmpty()) {
        Geometry xorGeometry = null;
        for (final CloseLocation location : getMouseOverLocations()) {
          final Geometry locationGeometry = getVertexGeometry(event, location);
          if (locationGeometry != null) {
            if (xorGeometry == null) {
              xorGeometry = locationGeometry;
            } else {
              xorGeometry = xorGeometry.union(locationGeometry);
            }
          }
        }
        setXorGeometry(xorGeometry);
        if (!hasSnapPoint(event, boundingBox)) {
          setMapCursor(CURSOR_NODE_ADD);
        }
      }
    }
  }

  protected void mouseMoved(final KeyEvent e) {
    int modifiers;
    if (e.getID() == KeyEvent.KEY_PRESSED) {
      modifiers = InputEvent.ALT_MASK;
    } else {
      modifiers = 0;
    }

    final java.awt.Point mousePosition = getMap().getMapMousePosition();
    if (mousePosition != null) {
      final MouseEvent event = new MouseEvent((Component)e.getSource(),
        MouseEvent.MOUSE_MOVED, e.getWhen(), modifiers, mousePosition.x,
        mousePosition.y, 0, false);
      mouseMoved(event);
    }
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
    if (!SwingUtilities.isMiddleMouseButton(event)) {
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
        if (!getMouseOverLocations().isEmpty()) {
          setMapCursor(CURSOR_MOVE);
        }
      }
    }
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    if (SwingUtilities.isLeftMouseButton(event)) {
      this.dragged = false;
    }
    if (SwingUtil.isLeftButtonAndNoModifiers(event)) {
      if (!getMouseOverLocations().isEmpty()) {
        this.vertexDragModifiers = event.getModifiers();
      }
      if (!getMouseOverLocations().isEmpty()) {

        repaint();
        event.consume();
        return;
      }
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (this.moveGeometryStart != null) {
      moveGeometryFinish(event);
    } else if (this.dragged && !getMouseOverLocations().isEmpty()) {
      vertexDragFinish(event);
      return;
    }
    if (SwingUtilities.isLeftMouseButton(event)) {
      this.dragged = false;
    }
  }

  protected void moveGeometryFinish(final MouseEvent event) {
    for (final CloseLocation location : getMouseOverLocations()) {
      final GeometryFactory geometryFactory = location.getGeometryFactory();
      final Point startPoint = geometryFactory.copy(getViewportPoint(this.moveGeometryStart));
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
    final Viewport2D viewport = getViewport();
    final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory();

    if (this.moveGeometryStart != null) {
      final AffineTransform transform = graphics.getTransform();
      try {
        final java.awt.Point mousePoint = getMap().getMapMousePosition();
        if (mousePoint != null) {
          final int deltaX = mousePoint.x - this.moveGeometryStart.x;
          final int deltaY = mousePoint.y - this.moveGeometryStart.y;
          graphics.translate(deltaX, deltaY);
          SelectRecordsOverlay.SELECT_RENDERER.paintSelected(viewport,
            viewportGeometryFactory, graphics, this.addGeometry);
        }
      } finally {
        graphics.setTransform(transform);
      }
    } else {
      SelectRecordsOverlay.SELECT_RENDERER.paintSelected(viewport,
        viewportGeometryFactory, graphics, this.addGeometry);
    }
    drawXorGeometry(graphics);
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
      if (source == this.addLayer) {
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
    if (!JtsGeometryUtil.equalsExact3D(geometry, this.addGeometry)) {
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
      if (JtsGeometryUtil.equalsExact3D(newGeometry, this.addGeometry)) {
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
        final AbstractDataObjectLayer layer = location.getLayer();
        return layer.createPropertyEdit(object, geometryAttributeName,
          oldValue, newGeometry);
      }
    }
  }

  @Override
  protected boolean setMouseOverLocations(final java.awt.Point eventPoint,
    final List<CloseLocation> mouseOverLocations) {
    if (super.setMouseOverLocations(eventPoint, mouseOverLocations)) {
      final Map<String, Set<CloseLocation>> vertexLocations = new TreeMap<String, Set<CloseLocation>>();
      final Map<String, Set<CloseLocation>> segmentLocations = new TreeMap<String, Set<CloseLocation>>();

      for (final CloseLocation location : mouseOverLocations) {
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
      getMap().setToolTipText(eventPoint, text);

      if (vertexLocations.isEmpty()) {
        setMapCursor(CURSOR_LINE_ADD_NODE);
      } else {
        setMapCursor(CURSOR_NODE_EDIT);
      }
      return true;
    } else {
      return false;
    }
  }

  // K key to split a record
  protected boolean splitLineKeyPress(final KeyEvent e) {
    final int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_K) {
      if (!isModeAddGeometry() && !getMouseOverLocations().isEmpty()) {
        final MultipleUndo undo = new MultipleUndo();
        for (final CloseLocation mouseLocation : getMouseOverLocations()) {
          final LayerDataObject record = mouseLocation.getObject();
          undo.addEdit(new SplitRecordUndo(record, mouseLocation));
        }
        addUndo(undo);
        e.consume();
        return true;
      }
    }
    return false;
  }

  private boolean updateAddMouseOverGeometry(final java.awt.Point eventPoint,
    final BoundingBox boundingBox) {
    final CloseLocation location = findCloseLocation(this.addLayer, null,
      this.addGeometry, boundingBox);
    final List<CloseLocation> locations = new ArrayList<CloseLocation>();
    if (location != null) {
      locations.add(location);
    }

    return setMouseOverLocations(eventPoint, locations);
  }

  protected boolean updateMouseOverGeometry(final java.awt.Point eventPoint,
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
    if (event == null) {
      clearMouseOverLocations();
    } else if (event.getModifiers() == vertexDragModifiers) {
      try {
        final MultipleUndo edit = new MultipleUndo();
        for (final CloseLocation location : getMouseOverLocations()) {
          final Geometry geometry = location.getGeometry();
          final GeometryFactory geometryFactory = location.getGeometryFactory();
          final Point point;
          if (getSnapPoint() == null) {
            point = getPoint(geometryFactory, event);
          } else {
            point = geometryFactory.copy(getSnapPoint());
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
      } finally {
        clearMouseOverLocations();
      }
    }

  }
}
