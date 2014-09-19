package com.revolsys.swing.map.overlay;

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
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.undo.UndoableEdit;

import com.revolsys.awt.WebColors;
import com.revolsys.data.equals.GeometryEqualsExact3d;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.jts.GeometryEditUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.segment.LineSegment;
import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.undo.AbstractUndoableEdit;
import com.revolsys.swing.undo.MultipleUndo;
import com.revolsys.util.CollectionUtil;

public class EditGeometryOverlay extends AbstractOverlay implements
PropertyChangeListener, MouseListener, MouseMotionListener {

  private class AddGeometryUndoEdit extends AbstractUndoableEdit {

    private static final long serialVersionUID = 1L;

    private final DataType geometryPartDataType = EditGeometryOverlay.this.addGeometryPartDataType;

    private final int[] geometryPartIndex = EditGeometryOverlay.this.addGeometryPartIndex;

    private final Geometry newGeometry;

    private final Geometry oldGeometry = EditGeometryOverlay.this.addGeometry;

    private AddGeometryUndoEdit(final Geometry geometry) {
      this.newGeometry = geometry;
    }

    @Override
    public boolean canRedo() {
      if (super.canRedo()) {
        if (GeometryEqualsExact3d.equal(this.oldGeometry,
          EditGeometryOverlay.this.addGeometry)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean canUndo() {
      if (super.canUndo()) {
        if (GeometryEqualsExact3d.equal(this.newGeometry,
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

  private static final String ACTION_ADD_GEOMETRY = "addGeometry";

  private static final String ACTION_EDIT_GEOMETRY_VERTICES = "editGeometryVertices";

  private static final String ACTION_MOVE_GEOMETRY = "moveGeometry";

  private static final Cursor CURSOR_MOVE = Icons.getCursor("cursor_move", 8, 7);

  public static final SelectedRecordsRenderer MOVE_GEOMETRY_RENDERER = new SelectedRecordsRenderer(
    WebColors.Black, WebColors.Aqua);

  private static final long serialVersionUID = 1L;

  private int actionId = 0;

  private AddGeometryCompleteAction addCompleteAction;

  private Geometry addGeometry;

  private DataType addGeometryDataType;

  private DataType addGeometryPartDataType;

  /** Index to the part of the addGeometry that new points should be added too. */
  private int[] addGeometryPartIndex = {};

  private AbstractRecordLayer addLayer;

  private boolean addSnapToFirst = false;

  private boolean dragged = false;

  private boolean editGeometryVerticesStart;

  private List<CloseLocation> mouseOverLocations = Collections.emptyList();

  private List<CloseLocation> moveGeometryLocations;

  private Point moveGeometryStart;

  private Point moveGeometryEnd;

  public EditGeometryOverlay(final MapPanel map) {
    super(map);
    setOverlayActionCursor(ACTION_ADD_GEOMETRY, CURSOR_NODE_ADD);
    setOverlayActionCursor(ACTION_MOVE_GEOMETRY, CURSOR_MOVE);
    setOverlayActionCursor(ACTION_EDIT_GEOMETRY_VERTICES, CURSOR_NODE_EDIT);

    addOverlayActionOverride(ACTION_ADD_GEOMETRY, ZoomOverlay.ACTION_PAN,
      ZoomOverlay.ACTION_ZOOM, ZoomOverlay.ACTION_ZOOM_BOX);
    addOverlayActionOverride(ACTION_MOVE_GEOMETRY, ZoomOverlay.ACTION_PAN,
      ZoomOverlay.ACTION_ZOOM);
    addOverlayActionOverride(ACTION_EDIT_GEOMETRY_VERTICES,
      ZoomOverlay.ACTION_PAN, ZoomOverlay.ACTION_ZOOM, ACTION_MOVE_GEOMETRY);
  }

  /**
   * Set the addLayer that a new feature is to be added to.
   *
   * @param addLayer
   */
  public void addRecord(final AbstractRecordLayer layer,
    final AddGeometryCompleteAction addCompleteAction) {
    if (layer != null) {
      final RecordDefinition recordDefinition = layer.getRecordDefinition();
      final Attribute geometryAttribute = recordDefinition.getGeometryAttribute();
      if (geometryAttribute != null) {
        this.addLayer = layer;
        this.addCompleteAction = addCompleteAction;
        final GeometryFactory geometryFactory = recordDefinition.getGeometryFactory();
        this.setGeometryFactory(geometryFactory);
        clearUndoHistory();
        this.addGeometry = geometryFactory.geometry();
        setOverlayAction(ACTION_ADD_GEOMETRY);
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

  private void addRecords(final List<LayerRecord> results,
    final LayerGroup group, final Geometry boundingBox) {
    final double scale = getViewport().getScale();
    final List<Layer> layers = group.getLayers();
    Collections.reverse(layers);
    for (final Layer layer : layers) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        addRecords(results, childGroup, boundingBox);
      } else if (layer instanceof AbstractRecordLayer) {
        final AbstractRecordLayer recordLayer = (AbstractRecordLayer)layer;
        if (recordLayer.isSelectable(scale)) {
          final List<LayerRecord> selectedRecords = recordLayer.getSelectedRecords();
          for (final LayerRecord selectedRecord : selectedRecords) {
            final Geometry geometry = selectedRecord.getGeometryValue();
            if (boundingBox.intersects(geometry)) {
              results.add(selectedRecord);
            }
          }
        }
      }
    }
  }

  protected void addSelectedObjects(final List<LayerRecord> objects,
    final LayerGroup group, final BoundingBox boundingBox) {
    final double scale = getViewport().getScale();
    for (final Layer layer : group.getLayers()) {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        addSelectedObjects(objects, childGroup, boundingBox);
      } else if (layer instanceof AbstractRecordLayer) {
        final AbstractRecordLayer recordLayer = (AbstractRecordLayer)layer;
        if (recordLayer.isEditable(scale)) {
          final List<LayerRecord> selectedObjects = recordLayer.getSelectedRecords(boundingBox);
          objects.addAll(selectedObjects);
        }
      }
    }
  }

  protected boolean addSnapLayers(final Set<AbstractRecordLayer> layers,
    final Project project, final AbstractRecordLayer layer, final double scale) {
    if (layer != null) {
      if (layer.isSnapToAllLayers()) {
        return true;
      } else {
        layers.add(layer);
        final Collection<String> layerPaths = layer.getSnapLayerPaths();
        if (layerPaths != null) {
          for (final String layerPath : layerPaths) {
            final Layer snapLayer = project.getLayer(layerPath);
            if (snapLayer instanceof AbstractRecordLayer) {
              if (snapLayer.isVisible(scale)) {
                layers.add((AbstractRecordLayer)snapLayer);
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
      geometry = newPoint.convert(geometryFactory);
    } else {
      final DataType geometryDataType = this.addGeometryDataType;
      final int[] geometryPartIndex = this.addGeometryPartIndex;
      if (DataTypes.MULTI_POINT.equals(geometryDataType)) {
        if (geometry instanceof Point) {
          final Point point = (Point)geometry;
          geometry = geometryFactory.multiPoint(point, newPoint);
        } else {
          geometry = geometry.appendVertex(newPoint, geometryPartIndex);
        }
      } else if (DataTypes.LINE_STRING.equals(geometryDataType)
          || DataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
        if (geometry instanceof Point) {
          final Point point = (Point)geometry;
          geometry = geometryFactory.lineString(point, newPoint);
        } else if (geometry instanceof LineString) {
          final LineString line = (LineString)geometry;
          geometry = line.appendVertex(newPoint, geometryPartIndex);
        } // TODO MultiLineString
      } else if (DataTypes.POLYGON.equals(geometryDataType)
          || DataTypes.MULTI_POLYGON.equals(geometryDataType)) {
        if (geometry instanceof Point) {
          final Point point = (Point)geometry;
          geometry = geometryFactory.lineString(point, newPoint);
        } else if (geometry instanceof LineString) {
          final LineString line = (LineString)geometry;
          final Point p0 = line.getPoint(0);
          final Point p1 = line.getPoint(1);
          final LinearRing ring = geometryFactory.linearRing(Arrays.asList(p0,
            p1, newPoint, p0));
          geometry = geometryFactory.polygon(ring);
        } else if (geometry instanceof Polygon) {
          final Polygon polygon = (Polygon)geometry;
          geometry = polygon.appendVertex(newPoint, geometryPartIndex);
        }
        // TODO MultiPolygon
        // TODO Rings
      } else {
        // TODO multi point, oldGeometry collection
      }
    }
    return geometry;
  }

  protected void cancel() {
    clearMouseOverLocations();
    if (this.addCompleteAction != null) {
      this.addCompleteAction.addComplete(this, null);
    }
    modeMoveGeometryClear();
    modeAddGeometryClear();
    modeEditGeometryVerticesClear();
  }

  public void clearMouseOverGeometry() {
    if (!hasOverlayAction()) {
      clearMapCursor();
    }
    this.mouseOverLocations = Collections.emptyList();
    clearSnapLocations();
  }

  protected void clearMouseOverLocations() {
    setXorGeometry(null);
    clearMouseOverGeometry();
    getMap().clearToolTipText();
    repaint();
  }

  protected LineString createXorLine(final GeometryFactory geometryFactory,
    final Point c0, final Point p1) {
    final Viewport2D viewport = getViewport();
    final GeometryFactory viewportGeometryFactory = viewport.getGeometryFactory();
    final LineSegment line = viewportGeometryFactory.lineSegment(c0, p1);
    final double length = line.getLength();
    if (length > 0) {
      final double cursorRadius = viewport.getModelUnitsPerViewUnit() * 6;
      final Point newC1 = line.pointAlongOffset((length - cursorRadius)
        / length, 0);
      return geometryFactory.lineString(c0, newC1);
    } else {
      return null;
    }
  }

  @Override
  public void destroy() {
    super.destroy();
    this.mouseOverLocations.clear();

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

  public AbstractLayer getAddLayer() {
    return this.addLayer;
  }

  public Point getClosestPoint(final GeometryFactory geometryFactory,
    final LineSegment closestSegment, final Point point,
    final double maxDistance) {
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
      final Point pointOnLine = segment.project(point);
      return geometryFactory.point(pointOnLine);
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

  public Point getLineNextVertex(final LineString line, final int vertexIndex,
    final int offset) {
    final int nextVertexIndex = vertexIndex + offset;
    if (nextVertexIndex < line.getVertexCount()) {
      return line.getPoint(nextVertexIndex);
    } else {
      return null;
    }
  }

  public Point getLinePreviousVertex(final LineString line,
    final int vertexIndex, final int offset) {
    final int previousVertexIndex = vertexIndex + offset;
    if (previousVertexIndex < 0) {
      return null;
    } else {
      return line.getPoint(previousVertexIndex);
    }
  }

  public List<CloseLocation> getMouseOverLocations() {
    return this.mouseOverLocations;
  }

  protected Point getPoint(final GeometryFactory geometryFactory,
    final MouseEvent event) {
    final Viewport2D viewport = getViewport();
    final Point point = viewport.toModelPointRounded(geometryFactory,
      event.getX(), event.getY());
    return point;
  }

  protected List<LayerRecord> getSelectedRecords(final BoundingBox boundingBox) {
    final List<LayerRecord> objects = new ArrayList<LayerRecord>();
    addSelectedObjects(objects, getProject(), boundingBox);
    return objects;
  }

  @Override
  protected List<AbstractRecordLayer> getSnapLayers() {
    final Project project = getProject();
    final double scale = MapPanel.get(project).getScale();
    final Set<AbstractRecordLayer> layers = new LinkedHashSet<AbstractRecordLayer>();
    boolean snapAll = false;
    if (isOverlayAction(ACTION_ADD_GEOMETRY)) {
      snapAll = addSnapLayers(layers, project, this.addLayer, scale);
    } else {
      for (final CloseLocation location : getMouseOverLocations()) {
        final AbstractRecordLayer layer = location.getLayer();
        snapAll |= addSnapLayers(layers, project, layer, scale);
      }
    }
    if (snapAll) {
      final List<AbstractRecordLayer> visibleDescendants = project.getVisibleDescendants(
        AbstractRecordLayer.class, scale);
      return visibleDescendants;
    }
    return new ArrayList<AbstractRecordLayer>(layers);
  }

  protected Geometry getVertexGeometry(final MouseEvent event,
    final CloseLocation location) {
    final Geometry geometry = location.getGeometry();
    final DataType geometryDataType = DataTypes.getType(geometry);
    final DataType geometryPartDataType = getGeometryPartDataType(geometryDataType);

    int previousPointOffset;
    int[] vertexId = location.getVertexIndex();
    if (vertexId == null) {
      previousPointOffset = 0;
      vertexId = location.getSegmentId();
    } else {
      previousPointOffset = -1;
    }
    final GeometryFactory geometryFactory = location.getGeometryFactory();
    if (DataTypes.GEOMETRY.equals(geometryPartDataType)) {
    } else if (DataTypes.POINT.equals(geometryPartDataType)) {
    } else {
      final Point point = getPoint(geometryFactory, event);

      final Vertex vertex = geometry.getVertex(vertexId);
      Point previousPoint = null;
      Point nextPoint = null;

      if (DataTypes.LINE_STRING.equals(geometryPartDataType)
          || DataTypes.POLYGON.equals(geometryPartDataType)) {
        if (previousPointOffset == 0) {
          previousPoint = vertex;
        } else {
          previousPoint = vertex.getLinePrevious();
        }
        nextPoint = vertex.getLineNext();
      }

      final List<LineString> lines = new ArrayList<LineString>();
      if (previousPoint != null && !previousPoint.isEmpty()) {
        lines.add(createXorLine(geometryFactory, previousPoint, point));
      }
      if (nextPoint != null && !nextPoint.isEmpty()) {
        lines.add(createXorLine(geometryFactory, nextPoint, point));
      }
      if (!lines.isEmpty()) {
        return geometryFactory.multiLineString(lines);
      }
    }
    return null;
  }

  protected boolean isEditable(final AbstractRecordLayer recordLayer) {
    return recordLayer.isExists() && recordLayer.isVisible()
        && recordLayer.isCanEditRecords();
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

  @Override
  public void keyPressed(final KeyEvent e) {
    final int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_ESCAPE) {
      cancel();
    } else if (keyCode == KeyEvent.VK_ALT) {
      if (!getMouseOverLocations().isEmpty() && !this.editGeometryVerticesStart) {
        if (setOverlayAction(ACTION_MOVE_GEOMETRY)) {
          updateMouseOverLocations();
        }
      }
    }
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    final int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_ALT) {
      if (!getMouseOverLocations().isEmpty() && !this.editGeometryVerticesStart) {
        if (clearOverlayAction(ACTION_MOVE_GEOMETRY)) {
          updateMouseOverLocations();
        }
      }
    } else if (keyCode == KeyEvent.VK_BACK_SPACE
        || keyCode == KeyEvent.VK_DELETE) {
      if (!getMouseOverLocations().isEmpty()) {
        final MultipleUndo edit = new MultipleUndo();
        for (final CloseLocation location : getMouseOverLocations()) {
          final Geometry geometry = location.getGeometry();
          final int[] vertexIndex = location.getVertexIndex();
          if (vertexIndex != null) {
            try {
              final Geometry newGeometry = geometry.deleteVertex(vertexIndex);
              final UndoableEdit geometryEdit = setGeometry(location,
                newGeometry);
              edit.addEdit(geometryEdit);
            } catch (final Throwable t) {
              Toolkit.getDefaultToolkit().beep();
            }
          }
        }
        if (!edit.isEmpty()) {
          addUndo(edit);
        }
        clearMouseOverLocations();
      }
    } else if (keyCode == KeyEvent.VK_F2 || keyCode == KeyEvent.VK_F) {
      clearMouseOverLocations();
      modeMoveGeometryClear();
      if (this.addCompleteAction != null) {
        this.addCompleteAction.addComplete(this, this.addGeometry);
      }
      modeAddGeometryClear();
    } else if (keyCode == KeyEvent.VK_CONTROL) {
      if (!isOverlayAction(ACTION_ADD_GEOMETRY)) {
        clearMouseOverLocations();
      }
    } else if (splitLineKeyPress(e)) {
    }
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    final char keyChar = e.getKeyChar();
    if (keyChar >= '0' && keyChar <= '9') {
      final int snapPointIndex = keyChar - '0';
      if (snapPointIndex <= getSnapPointLocationMap().size()) {
        setSnapPointIndex(snapPointIndex);
        setSnapLocations(getSnapPointLocationMap());
      }
    } else {
      super.keyTyped(e);
    }
  }

  protected void modeAddGeometryClear() {
    if (clearOverlayAction(ACTION_ADD_GEOMETRY) || !hasOverlayAction()) {
      this.addCompleteAction = null;
      this.addGeometry = null;
      this.addGeometryDataType = null;
      this.addGeometryPartDataType = null;
      this.addGeometryPartIndex = null;
      this.addLayer = null;
      setGeometryFactory(null);
      clearMouseOverLocations();
      setXorGeometry(null);
      repaint();
    }
  }

  protected boolean modeAddGeometryClick(final MouseEvent event) {
    final int modifiers = event.getModifiersEx();
    if (modifiers == 0 && event.getButton() == MouseEvent.BUTTON1) {
      if (isOverlayAction(ACTION_ADD_GEOMETRY)) {
        if (getMouseOverLocations().isEmpty()) {
          final int clickCount = event.getClickCount();
          if (clickCount == 1) {
            if (this.addSnapToFirst || getMouseOverLocations().isEmpty()) {
              Point point = getSnapPoint();
              if (point == null) {
                point = getPoint(event);
              }
              final GeometryFactory geometryFactory = this.addLayer.getGeometryFactory();

              point = (Point)point.copy(geometryFactory);
              if (this.addGeometry.isEmpty()) {
                setAddGeometry(point);
              } else {
                final Point previousPoint = GeometryEditUtil.getVertex(
                  this.addGeometry, this.addGeometryPartIndex, -1);
                if (!point.equals(previousPoint)) {
                  final Geometry newGeometry = appendVertex(point);
                  setAddGeometry(newGeometry);
                }
              }

              setXorGeometry(null);
              event.consume();
              if (DataTypes.POINT.equals(this.addGeometryDataType)) {
                if (isOverlayAction(ACTION_ADD_GEOMETRY)) {
                  if (isGeometryValid(this.addGeometry)) {
                    try {
                      setXorGeometry(null);
                      if (this.addCompleteAction != null) {
                        final Geometry geometry = this.addGeometry.copy(this.addLayer.getGeometryFactory());
                        this.addCompleteAction.addComplete(this, geometry);
                        modeAddGeometryClear();
                      }
                    } finally {
                      clearMapCursor();
                    }
                  }
                }
                repaint();
              }
              return true;
            } else {
              Toolkit.getDefaultToolkit().beep();
            }
          }
          if (clickCount == 2) {
            setXorGeometry(null);
            event.consume();
            if (isGeometryValid(this.addGeometry)) {
              if (isOverlayAction(ACTION_ADD_GEOMETRY)) {
                if (isGeometryValid(this.addGeometry)) {
                  try {
                    setXorGeometry(null);
                    if (this.addCompleteAction != null) {
                      final Geometry geometry = this.addGeometry.copy(this.addLayer.getGeometryFactory());
                      this.addCompleteAction.addComplete(this, geometry);
                      modeAddGeometryClear();
                    }
                  } finally {
                    clearMapCursor();
                  }
                }
              }
              repaint();
            }
            return true;
          }
        }
      }
    }
    return false;
  }

  protected boolean modeAddGeometryDrag(final MouseEvent event) {
    if (isOverlayAction(ACTION_ADD_GEOMETRY)) {
      if (!getMouseOverLocations().isEmpty()) {
        final BoundingBox boundingBox = getHotspotBoundingBox(event);

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
        return true;
      }
    }
    return false;
  }

  protected boolean modeAddGeometryFinish(final MouseEvent event) {
    if (isOverlayAction(ACTION_ADD_GEOMETRY)) {
      if (!getMouseOverLocations().isEmpty()) {
        if (event.getButton() == MouseEvent.BUTTON1) {
          for (final CloseLocation location : getMouseOverLocations()) {
            final Geometry geometry = location.getGeometry();
            final GeometryFactory geometryFactory = location.getGeometryFactory();
            final Point point;
            if (getSnapPoint() == null) {
              point = getPoint(geometryFactory, event);
            } else {
              point = (Point)getSnapPoint().copy(geometryFactory);
            }
            final int[] vertexIndex = location.getVertexIndex();
            Geometry newGeometry;
            final Point newPoint = point;
            if (vertexIndex == null) {
              final int[] segmentIndex = location.getSegmentId();
              final int[] newIndex = segmentIndex.clone();
              newIndex[newIndex.length - 1] = newIndex[newIndex.length - 1] + 1;
              newGeometry = geometry.insertVertex(newPoint, newIndex);
            } else {
              newGeometry = geometry.moveVertex(newPoint, vertexIndex);
            }
            setAddGeometry(newGeometry);
          }
          return true;
        }
      }
    }
    return false;
  }

  protected boolean modeAddGeometryMove(final MouseEvent event) {
    if (isOverlayAction(ACTION_ADD_GEOMETRY) && this.addGeometry != null) {
      final BoundingBox boundingBox = getHotspotBoundingBox(event);
      Point point = getPoint(event);
      // TODO make work with multi-part
      final Point fromPoint = this.addGeometry.getPoint();
      this.addSnapToFirst = !SwingUtil.isControlDown(event)
          && boundingBox.covers(fromPoint);
      final boolean hasMouseOver = updateAddMouseOverGeometry(boundingBox);
      if (this.addSnapToFirst || !hasMouseOver) {
        if (this.addSnapToFirst) {
          setMapCursor(CURSOR_NODE_SNAP);
          setSnapPoint(fromPoint);
          point = fromPoint;
        } else if (!hasSnapPoint(event, boundingBox)) {
          setMapCursor(CURSOR_NODE_ADD);
        }
        final Point firstPoint = GeometryEditUtil.getVertex(this.addGeometry,
          this.addGeometryPartIndex, 0);
        Geometry xorGeometry = null;
        if (DataTypes.POINT.equals(this.addGeometryPartDataType)) {
        } else {
          final GeometryFactory geometryFactory = this.addLayer.getGeometryFactory();
          if (DataTypes.LINE_STRING.equals(this.addGeometryPartDataType)) {
            final Point previousPoint = GeometryEditUtil.getVertex(
              this.addGeometry, this.addGeometryPartIndex, -1);
            if (previousPoint != null && !previousPoint.isEmpty()) {
              xorGeometry = createXorLine(geometryFactory, previousPoint, point);
            }
          } else if (DataTypes.POLYGON.equals(this.addGeometryPartDataType)) {
            final Point previousPoint = GeometryEditUtil.getVertex(
              this.addGeometry, this.addGeometryPartIndex, -1);
            if (previousPoint != null && !previousPoint.isEmpty()) {
              if (previousPoint.equals(firstPoint)) {
                xorGeometry = createXorLine(geometryFactory, previousPoint,
                  point);
              } else {
                final Point p1 = geometryFactory.point(previousPoint);
                final Point p3 = geometryFactory.point(firstPoint);
                final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory();
                xorGeometry = viewportGeometryFactory.lineString(p1, point, p3);
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

  protected boolean modeAddGeometryStart(final MouseEvent event) {
    final int modifiers = event.getModifiersEx();
    if (modifiers == MouseEvent.BUTTON1_DOWN_MASK) {
      if (isOverlayAction(ACTION_ADD_GEOMETRY)) {
        if (!getMouseOverLocations().isEmpty()) {
          repaint();
          return true;
        }
      }
    }
    return false;
  }

  protected void modeEditGeometryVerticesClear() {
    clearOverlayAction(ACTION_EDIT_GEOMETRY_VERTICES);
    clearMouseOverLocations();
    this.editGeometryVerticesStart = false;
    this.dragged = false;
  }

  protected boolean modeEditGeometryVerticesDrag(final MouseEvent event) {
    if (this.editGeometryVerticesStart
        && isOverlayAction(ACTION_EDIT_GEOMETRY_VERTICES)) {
      this.dragged = true;

      final BoundingBox boundingBox = getHotspotBoundingBox(event);

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
      return true;
    }
    return false;
  }

  protected boolean modeEditGeometryVerticesFinish(final MouseEvent event) {
    if (this.dragged && clearOverlayAction(ACTION_EDIT_GEOMETRY_VERTICES)) {
      if (event.getButton() == MouseEvent.BUTTON1) {
        try {
          final MultipleUndo edit = new MultipleUndo();
          for (final CloseLocation location : getMouseOverLocations()) {
            final Geometry geometry = location.getGeometry();
            final GeometryFactory geometryFactory = location.getGeometryFactory();
            final Point point;
            if (getSnapPoint() == null) {
              point = getPoint(geometryFactory, event);
            } else {
              point = (Point)getSnapPoint().copy(geometryFactory);
            }
            final int[] vertexIndex = location.getVertexIndex();
            Geometry newGeometry;
            final Point newPoint = point;
            if (vertexIndex == null) {
              final int[] segmentIndex = location.getSegmentId();
              final int[] newIndex = segmentIndex.clone();
              newIndex[newIndex.length - 1] = newIndex[newIndex.length - 1] + 1;
              newGeometry = geometry.insertVertex(newPoint, newIndex);
            } else {
              newGeometry = geometry.moveVertex(newPoint, vertexIndex);
            }
            final UndoableEdit geometryEdit = setGeometry(location, newGeometry);
            edit.addEdit(geometryEdit);
          }
          if (!edit.isEmpty()) {
            addUndo(edit);
          }
        } finally {
          modeEditGeometryVerticesClear();
        }
        return true;
      }
    }
    return false;
  }

  protected boolean modeEditGeometryVerticesMove(final MouseEvent event) {
    if (canOverrideOverlayAction(ACTION_EDIT_GEOMETRY_VERTICES)
        || isOverlayAction(ACTION_MOVE_GEOMETRY)) {
      final BoundingBox boundingBox = getHotspotBoundingBox(event);
      final List<LayerRecord> selectedRecords = getSelectedRecords(boundingBox);
      final List<CloseLocation> closeLocations = new ArrayList<CloseLocation>();
      for (final LayerRecord record : selectedRecords) {
        final CloseLocation closeLocation = findCloseLocation(record,
          boundingBox);
        if (closeLocation != null) {
          closeLocations.add(closeLocation);
        }
      }
      if (closeLocations.isEmpty()) {
        modeMoveGeometryClear();
        modeEditGeometryVerticesClear();
      } else if (event.isAltDown()) {
        setOverlayAction(ACTION_MOVE_GEOMETRY);
      } else {
        setOverlayAction(ACTION_EDIT_GEOMETRY_VERTICES);
      }
      return setMouseOverLocations(closeLocations);
    }
    return false;
  }

  protected boolean modeEditGeometryVerticesStart(final MouseEvent event) {
    final int modifiers = event.getModifiersEx();
    if (modifiers == MouseEvent.BUTTON1_DOWN_MASK) {
      if (isOverlayAction(ACTION_EDIT_GEOMETRY_VERTICES)) {
        this.editGeometryVerticesStart = true;
        repaint();
        return true;
      }
    }
    return false;
  }

  protected void modeMoveGeometryClear() {
    clearOverlayAction(ACTION_MOVE_GEOMETRY);
    this.moveGeometryStart = null;
    this.moveGeometryEnd = null;
    this.moveGeometryLocations = null;
    clearMouseOverGeometry();
  }

  protected boolean modeMoveGeometryDrag(final MouseEvent event) {
    if (isOverlayAction(ACTION_MOVE_GEOMETRY)) {
      this.moveGeometryEnd = getEventPoint();
      repaint();
      return true;
    }
    return false;
  }

  protected boolean modeMoveGeometryFinish(final MouseEvent event) {
    if (event.getButton() == MouseEvent.BUTTON1) {
      if (clearOverlayAction(ACTION_MOVE_GEOMETRY)) {
        for (final CloseLocation location : this.moveGeometryLocations) {
          final GeometryFactory geometryFactory = location.getGeometryFactory();
          final Point from = this.moveGeometryStart.convert(geometryFactory);
          final Point to = this.moveGeometryEnd.convert(geometryFactory);

          final double deltaX = to.getX() - from.getX();
          final double deltaY = to.getY() - from.getY();
          if (deltaX != 0 || deltaY != 0) {
            final Geometry geometry = location.getGeometry();
            final Geometry newGeometry = geometry.move(deltaX, deltaY);
            final UndoableEdit geometryEdit = setGeometry(location, newGeometry);
            addUndo(geometryEdit);
          }
        }
        modeMoveGeometryClear();
        return true;
      }
    }
    return false;
  }

  protected boolean modeMoveGeometryStart(final MouseEvent event) {
    if (isOverlayAction(ACTION_MOVE_GEOMETRY)
        && event.getButton() == MouseEvent.BUTTON1) {
      this.moveGeometryStart = this.moveGeometryEnd = getEventPoint();
      this.moveGeometryLocations = getMouseOverLocations();
      clearMouseOverLocations();
      return true;
    }
    return false;
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (modeAddGeometryClick(event)) {
    } else if (SwingUtil.isLeftButtonAndNoModifiers(event)
        && event.getClickCount() == 2) {
      final List<LayerRecord> records = new ArrayList<LayerRecord>();
      final BoundingBox boundingBox = getHotspotBoundingBox(event);
      final Geometry boundary = boundingBox.toPolygon().prepare();
      addRecords(records, getProject(), boundary);

      final int size = records.size();
      if (size == 0) {

      } else if (size < 10) {
        for (final LayerRecord record : records) {
          final AbstractRecordLayer layer = record.getLayer();
          layer.showForm(record);

        }
        event.consume();
      } else {
        JOptionPane.showMessageDialog(
          this,
          "There are too many "
              + size
              + " selected to view. Maximum 10. Select fewer records or move mouse to middle of geometry.",
              "Too Many Selected Records", JOptionPane.ERROR_MESSAGE);
        event.consume();
      }
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    if (modeAddGeometryDrag(event)) {
    } else if (modeMoveGeometryDrag(event)) {
    } else if (modeEditGeometryVerticesDrag(event)) {
    }
  }

  @Override
  public void mouseMoved(final MouseEvent event) {
    if (modeAddGeometryMove(event)) {
    } else if (modeEditGeometryVerticesMove(event)) {
    }
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    if (modeAddGeometryStart(event)) {
    } else if (modeMoveGeometryStart(event)) {
    } else if (modeEditGeometryVerticesStart(event)) {
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (modeAddGeometryFinish(event)) {
    } else if (modeMoveGeometryFinish(event)) {
    } else if (modeEditGeometryVerticesFinish(event)) {
    }
  }

  @Override
  public void paintComponent(final Graphics2D graphics) {
    final Viewport2D viewport = getViewport();
    final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory();

    if (isOverlayAction(ACTION_MOVE_GEOMETRY)) {
      final AffineTransform transform = graphics.getTransform();
      try {
        if (this.moveGeometryStart != null) {
          for (final CloseLocation location : this.moveGeometryLocations) {
            final GeometryFactory geometryFactory = location.getGeometryFactory();
            final Point from = this.moveGeometryStart.convert(geometryFactory);
            final Point to = this.moveGeometryEnd.convert(geometryFactory);
            final double deltaX = to.getX() - from.getX();
            final double deltaY = to.getY() - from.getY();
            Geometry geometry = location.getGeometry();
            geometry = geometry.move(deltaX, deltaY);
            MOVE_GEOMETRY_RENDERER.paintSelected(viewport, graphics,
              viewportGeometryFactory, geometry);
          }
        }
      } finally {
        graphics.setTransform(transform);
      }
    } else {
      SelectRecordsOverlay.SELECT_RENDERER.paintSelected(viewport, graphics,
        viewportGeometryFactory, this.addGeometry);
    }
    drawXorGeometry(graphics);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    final Object source = event.getSource();
    final String propertyName = event.getPropertyName();

    if ("preEditable".equals(propertyName)) {
      if (isOverlayAction(ACTION_ADD_GEOMETRY)) {
        if (isGeometryValid(this.addGeometry)) {
          try {
            setXorGeometry(null);
            if (this.addCompleteAction != null) {
              final Geometry geometry = this.addGeometry.copy(this.addLayer.getGeometryFactory());
              this.addCompleteAction.addComplete(this, geometry);
              modeAddGeometryClear();
            }
          } finally {
            clearMapCursor();
          }
        }
      }
    } else if ("editable".equals(propertyName)) {
      repaint();
      if (source == this.addLayer) {
        // if (!isEditable(addLayer)) {
        // setEditingObject(null, null);
        // }
      }
    } else if (source instanceof LayerRecord) {
      if (event.getNewValue() instanceof Geometry) {
        // TODO update mouse over locations
        // clearMouseOverLocations();
      }
    }
  }

  private void setAddGeometry(final Geometry geometry) {
    if (!GeometryEqualsExact3d.equal(geometry, this.addGeometry)) {
      final AddGeometryUndoEdit undo = new AddGeometryUndoEdit(geometry);
      addUndo(undo);
      repaint();
    }
  }

  protected void setAddGeometryDataType(final DataType dataType) {
    this.addGeometryDataType = dataType;
    this.addGeometryPartDataType = getGeometryPartDataType(dataType);
  }

  protected UndoableEdit setGeometry(final CloseLocation location,
    final Geometry newGeometry) {
    if (isOverlayAction(ACTION_ADD_GEOMETRY)) {
      if (GeometryEqualsExact3d.equal(newGeometry, this.addGeometry)) {
        return null;
      } else {
        return new AddGeometryUndoEdit(newGeometry);
      }
    } else {
      final LayerRecord object = location.getRecord();
      final RecordDefinition recordDefinition = location.getRecordDefinition();
      final String geometryAttributeName = recordDefinition.getGeometryAttributeName();
      final Geometry oldValue = object.getValue(geometryAttributeName);
      if (GeometryEqualsExact3d.equal(newGeometry, oldValue)) {
        return null;
      } else {
        final AbstractRecordLayer layer = location.getLayer();
        return layer.createPropertyEdit(object, geometryAttributeName,
          oldValue, newGeometry);
      }
    }
  }

  protected boolean setMouseOverLocations(
    final List<CloseLocation> mouseOverLocations) {
    if (this.mouseOverLocations.equals(mouseOverLocations)) {
      return !this.mouseOverLocations.isEmpty();
    } else {
      this.mouseOverLocations = mouseOverLocations;
      setSnapPoint(null);
      setXorGeometry(null);

      return updateMouseOverLocations();
    }
  }

  // K key to split a record
  protected boolean splitLineKeyPress(final KeyEvent e) {
    final int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_K) {
      if (!isOverlayAction(ACTION_ADD_GEOMETRY)
          && !getMouseOverLocations().isEmpty()) {
        for (final CloseLocation mouseLocation : getMouseOverLocations()) {
          final LayerRecord record = mouseLocation.getRecord();
          final AbstractRecordLayer layer = record.getLayer();
          layer.splitRecord(record, mouseLocation);
        }
        e.consume();
        return true;
      }
    }
    return false;
  }

  private boolean updateAddMouseOverGeometry(final BoundingBox boundingBox) {
    final CloseLocation location = findCloseLocation(this.addLayer, null,
      this.addGeometry, boundingBox);
    final List<CloseLocation> locations = new ArrayList<>();
    if (location != null) {
      locations.add(location);
    }
    return setMouseOverLocations(locations);
  }

  private boolean updateMouseOverLocations() {
    if (this.mouseOverLocations.isEmpty()) {
      final MapPanel map = getMap();
      map.clearToolTipText();
      return false;
    } else {
      final MapPanel map = getMap();
      if (isOverlayAction(ACTION_MOVE_GEOMETRY)) {
        map.clearToolTipText();
      } else {
        final Map<String, Set<CloseLocation>> vertexLocations = new TreeMap<String, Set<CloseLocation>>();
        final Map<String, Set<CloseLocation>> segmentLocations = new TreeMap<String, Set<CloseLocation>>();

        for (final CloseLocation location : this.mouseOverLocations) {
          final String typePath = location.getTypePath();
          if (location.getVertexIndex() == null) {
            CollectionUtil.addToSet(segmentLocations, typePath, location);
          } else {
            CollectionUtil.addToSet(vertexLocations, typePath, location);
          }
        }
        final StringBuffer text = new StringBuffer("<html>");
        appendLocations(text, "Move Vertices", vertexLocations);
        appendLocations(text, "Insert Vertices", segmentLocations);
        text.append("</html>");

        final Point2D eventPoint = getEventPosition();
        map.setToolTipText(eventPoint, text);
        if (vertexLocations.isEmpty()) {
          setMapCursor(CURSOR_LINE_ADD_NODE);
        } else {
          setMapCursor(CURSOR_NODE_EDIT);
        }
      }
      return true;
    }
  }
}
