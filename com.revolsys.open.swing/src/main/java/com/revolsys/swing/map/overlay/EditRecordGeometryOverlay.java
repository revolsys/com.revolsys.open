package com.revolsys.swing.map.overlay;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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
import com.revolsys.collection.map.Maps;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.model.editor.GeometryEditor;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.io.BaseCloseable;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.elevation.gridded.IGriddedElevationModelLayer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.LayerRecordMenu;
import com.revolsys.swing.map.layer.record.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.undo.AbstractUndoableEdit;
import com.revolsys.swing.undo.MultipleUndo;

public class EditRecordGeometryOverlay extends AbstractOverlay
  implements PropertyChangeListener, MouseListener, MouseMotionListener {

  private class AddGeometryUndoEdit extends AbstractUndoableEdit {

    private static final long serialVersionUID = 1L;

    private final DataType geometryPartDataType = EditRecordGeometryOverlay.this.addGeometryPartDataType;

    private final int[] geometryPartIndex = EditRecordGeometryOverlay.this.addGeometryPartIndex;

    private final Geometry newGeometry;

    private final Geometry oldGeometry = EditRecordGeometryOverlay.this.addGeometry;

    private AddGeometryUndoEdit(final Geometry geometry) {
      this.newGeometry = geometry;
    }

    @Override
    public boolean canRedo() {
      if (super.canRedo()) {
        if (DataTypes.GEOMETRY.equals(this.oldGeometry,
          EditRecordGeometryOverlay.this.addGeometry)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean canUndo() {
      if (super.canUndo()) {
        if (DataTypes.GEOMETRY.equals(this.newGeometry,
          EditRecordGeometryOverlay.this.addGeometry)) {
          return true;
        }
      }
      return false;
    }

    @Override
    protected void redoDo() {
      EditRecordGeometryOverlay.this.addGeometry = this.newGeometry;
      setXorGeometry(null);
      repaint();
    }

    @Override
    protected void undoDo() {
      EditRecordGeometryOverlay.this.addGeometry = this.oldGeometry;
      EditRecordGeometryOverlay.this.addGeometryPartDataType = this.geometryPartDataType;
      EditRecordGeometryOverlay.this.addGeometryPartIndex = this.geometryPartIndex;
      setXorGeometry(null);
      repaint();
    }
  }

  private static final String ACTION_ADD_GEOMETRY = "addGeometry";

  private static final String ACTION_ADD_GEOMETRY_EDIT_VERTICES = "addGeometryEditVertices";

  private static final String ACTION_EDIT_GEOMETRY_VERTICES = "editGeometryVertices";

  static final String ACTION_MOVE_GEOMETRY = "moveGeometry";

  private static final Cursor CURSOR_MOVE = Icons.getCursor("cursor_move", 8, 7);

  private static final VertexStyleRenderer GEOMETRY_CLOSE_VERTEX_RENDERER = new VertexStyleRenderer(
    WebColors.RoyalBlue);

  private static final SelectedRecordsRenderer GEOMETRY_RENDERER = new SelectedRecordsRenderer(
    WebColors.Aqua, 127);

  private static final SelectedRecordsVertexRenderer GEOMETRY_VERTEX_RENDERER = new SelectedRecordsVertexRenderer(
    WebColors.Aqua, true);

  private static final long serialVersionUID = 1L;

  private int actionId = 0;

  private AddGeometryCompleteAction addCompleteAction;

  private Geometry addGeometry;

  private DataType addGeometryDataType;

  private DataType addGeometryPartDataType;

  /** Index to the part of the addGeometry that new points should be added too. */
  private int[] addGeometryPartIndex = {};

  private AbstractRecordLayer addLayer;

  private boolean dragged = false;

  private boolean editGeometryVerticesStart;

  private boolean addGeometryEditVerticesStart;

  private List<CloseLocation> mouseOverLocations = Collections.emptyList();

  private Point moveGeometryEnd;

  private List<CloseLocation> moveGeometryLocations;

  private Point moveGeometryStart;

  public EditRecordGeometryOverlay(final MapPanel map) {
    super(map);
    addOverlayAction( //
      ACTION_ADD_GEOMETRY, //
      CURSOR_NODE_ADD, //
      ZoomOverlay.ACTION_PAN, //
      ZoomOverlay.ACTION_ZOOM, //
      ZoomOverlay.ACTION_ZOOM_BOX, //
      ACTION_MOVE_GEOMETRY, //
      ACTION_ADD_GEOMETRY_EDIT_VERTICES //
    );

    addOverlayAction(//
      ACTION_MOVE_GEOMETRY, //
      CURSOR_MOVE, //
      ZoomOverlay.ACTION_PAN, //
      ZoomOverlay.ACTION_ZOOM //
    );

    for (final String overlayAction : Arrays.asList(ACTION_EDIT_GEOMETRY_VERTICES,
      ACTION_ADD_GEOMETRY_EDIT_VERTICES)) {
      addOverlayAction( //
        overlayAction, //
        CURSOR_NODE_EDIT, //
        ZoomOverlay.ACTION_PAN, //
        ZoomOverlay.ACTION_ZOOM, //
        ACTION_MOVE_GEOMETRY //
      );
    }
  }

  private Point addElevation(final GeometryFactory geometryFactory, final Point newPoint) {
    if (geometryFactory.getAxisCount() > 2) {
      final double elevation = getElevation(newPoint);
      if (Double.isFinite(elevation)) {
        return newPoint.newPointZ(elevation);
      }
    }
    return newPoint;
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
      final FieldDefinition geometryField = recordDefinition.getGeometryField();
      if (geometryField != null) {
        this.addLayer = layer;
        this.addCompleteAction = addCompleteAction;
        final GeometryFactory geometryFactory = recordDefinition.getGeometryFactory();
        this.setGeometryFactory(geometryFactory);
        clearUndoHistory();
        this.addGeometry = geometryFactory.geometry();
        setOverlayAction(ACTION_ADD_GEOMETRY);
        setAddGeometryDataType(geometryField.getDataType());
        setMapCursor(CURSOR_NODE_ADD);

        if (Arrays
          .asList(DataTypes.POINT, DataTypes.LINE_STRING, DataTypes.MULTI_POINT,
            DataTypes.MULTI_LINE_STRING)
          .contains(this.addGeometryDataType)) {
          this.addGeometryPartIndex = new int[0];
        } else if (Arrays.asList(DataTypes.MULTI_POLYGON, DataTypes.POLYGON)
          .contains(this.addGeometryDataType)) {
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

  private void addRecords(final List<LayerRecord> results, final LayerGroup group,
    final Geometry boundingBox) {
    final double scale = getViewportScale();
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
            final Geometry geometry = selectedRecord.getGeometry();
            if (boundingBox.intersects(geometry)) {
              results.add(selectedRecord);
            }
          }
        }
      }
    }
  }

  protected boolean addSnapLayers(final Set<AbstractRecordLayer> layers, final Project project,
    final AbstractRecordLayer layer, final double scale) {
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
      geometry = newPoint.convertGeometry(geometryFactory);
    } else {
      final DataType geometryDataType = this.addGeometryDataType;
      final int[] geometryPartIndex = this.addGeometryPartIndex;
      if (DataTypes.MULTI_POINT.equals(geometryDataType)) {
        if (geometry instanceof Point) {
          final Point point = (Point)geometry;
          geometry = geometryFactory.punctual(point, newPoint);
        } else {
          geometry = geometry.appendVertex(newPoint, this.addGeometryPartIndex);
        }
      } else if (DataTypes.LINE_STRING.equals(geometryDataType)) {
        if (geometry instanceof Point) {
          final Point point = (Point)geometry;
          geometry = geometryFactory.lineString(point, newPoint);
        } else if (geometry instanceof LineString) {
          final LineString line = (LineString)geometry;
          geometry = line.appendVertex(newPoint, geometryPartIndex);
        }
      } else if (DataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
        if (geometry instanceof Point) {
          final Point point = (Point)geometry;
          geometry = geometryFactory.lineString(point, newPoint);
        } else if (geometry instanceof LineString) {
          final LineString line = (LineString)geometry;
          geometry = line.appendVertex(newPoint);
        } else if (geometry instanceof Lineal) {
          final Lineal line = (Lineal)geometry;
          geometry = line.appendVertex(newPoint, geometryPartIndex);
        }
      } else if (DataTypes.POLYGON.equals(geometryDataType)
        || DataTypes.MULTI_POLYGON.equals(geometryDataType)
        || DataTypes.GEOMETRY.equals(geometryDataType)) {
        if (geometry instanceof Point) {
          final Point point = (Point)geometry;
          geometry = geometryFactory.lineString(point, newPoint);
        } else if (geometry instanceof LineString) {
          final LineString line = (LineString)geometry;
          final Point p0 = line.getPoint(0);
          final Point p1 = line.getPoint(1);
          final LinearRing ring = geometryFactory.linearRing(Arrays.asList(p0, p1, newPoint, p0));
          geometry = geometryFactory.polygon(ring);
        } else if (geometry instanceof Polygon) {
          final Polygon polygon = (Polygon)geometry;
          geometry = polygon.appendVertex(newPoint, geometryPartIndex[0]);
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
    setMouseOverLocationsDo(Collections.emptyList());
    clearSnapLocations();
  }

  protected void clearMouseOverLocations() {
    setXorGeometry(null);
    clearMouseOverGeometry();
    final MapPanel map = getMap();
    map.clearCloseSelected();
    map.clearToolTipText();
    repaint();
  }

  @Override
  public void destroy() {
    super.destroy();
    setMouseOverLocationsDo(Collections.emptyList());
  }

  protected void fireActionPerformed(final ActionListener listener, final String command) {
    if (listener != null) {
      final ActionEvent actionEvent = new ActionEvent(this, this.actionId++, command);
      listener.actionPerformed(actionEvent);
    }
  }

  private GeometryEditor geometryEdit(final MouseEvent event, final CloseLocation location) {
    final Geometry geometry = location.getGeometry();
    final Point newPoint = getSnapOrEventPointWithElevation(event, location);
    int[] vertexId = location.getVertexId();
    final GeometryEditor geometryEditor = geometry.newGeometryEditor();
    if (vertexId == null) {
      vertexId = location.getSegmentIdNext();
      geometryEditor.insertVertex(newPoint, vertexId);
    } else {
      geometryEditor.setVertex(newPoint, vertexId);
    }
    if (geometryEditor.isModified()) {
      if (geometry.getGeometryFactory().getAxisCount() > 2) {

        for (final Vertex vertex : geometryEditor.vertices()) {
          double z = vertex.getZ();
          if (z == 0 || !Double.isFinite(z)) {
            z = getElevation(vertex);
            if (Double.isFinite(z)) {
              vertex.setZ(z);
            }
          }
        }
      }
    }
    return geometryEditor;
  }

  public DataType getAddGeometryPartDataType() {
    return this.addGeometryPartDataType;
  }

  public AbstractLayer getAddLayer() {
    return this.addLayer;
  }

  public Point getClosestPoint(final GeometryFactory geometryFactory,
    final LineSegment closestSegment, final Point point, final double maxDistance) {
    final LineSegment segment = closestSegment.convertGeometry(geometryFactory);
    final Point fromPoint = segment.getPoint(0);
    final Point toPoint = segment.getPoint(1);
    final double fromPointDistance = point.distancePoint(fromPoint);
    final double toPointDistance = point.distancePoint(toPoint);
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

  private double getElevation(final Point point) {
    final Project project = getProject();
    final double scale = getViewportScale();
    return IGriddedElevationModelLayer.getElevation(project, scale, point);
  }

  public DataType getGeometryPartDataType(final DataType dataType) {
    if (Arrays.asList(DataTypes.POINT, DataTypes.MULTI_POINT).contains(dataType)) {
      return DataTypes.POINT;
    } else if (Arrays.asList(DataTypes.LINE_STRING, DataTypes.MULTI_LINE_STRING)
      .contains(dataType)) {
      return DataTypes.LINE_STRING;
    } else if (Arrays.asList(DataTypes.POLYGON, DataTypes.MULTI_POLYGON).contains(dataType)) {
      return DataTypes.POLYGON;
    } else {
      return DataTypes.GEOMETRY;
    }
  }

  protected Graphics2D getGraphics2D() {
    return getGraphics();
  }

  public Point getLineNextVertex(final LineString line, final int vertexIndex, final int offset) {
    final int nextVertexIndex = vertexIndex + offset;
    if (nextVertexIndex < line.getVertexCount()) {
      return line.getPoint(nextVertexIndex);
    } else {
      return null;
    }
  }

  public Point getLinePreviousVertex(final LineString line, final int vertexIndex,
    final int offset) {
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

  @Override
  protected List<LayerRecord> getSelectedRecords(final BoundingBox boundingBox) {
    return super.getSelectedRecords(boundingBox);
  }

  @Override
  protected List<AbstractRecordLayer> getSnapLayers() {
    final Project project = getProject();
    final double scale = project.getMapPanel().getScale();
    final Set<AbstractRecordLayer> layers = new LinkedHashSet<>();
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
      final List<AbstractRecordLayer> visibleDescendants = project
        .getVisibleDescendants(AbstractRecordLayer.class, scale);
      return visibleDescendants;
    }
    return new ArrayList<>(layers);
  }

  private Point getSnapOrEventPointWithElevation(final MouseEvent event,
    final CloseLocation location) {
    final GeometryFactory geometryFactory = location.getGeometryFactory();
    final Point snapPoint = getSnapPoint();
    Point newPoint;
    if (snapPoint == null) {
      newPoint = getPoint(geometryFactory, event);
    } else {
      newPoint = snapPoint.newGeometry(geometryFactory);
    }
    newPoint = addElevation(geometryFactory, newPoint);
    return newPoint;
  }

  protected Geometry getVertexGeometry(final MouseEvent event, final CloseLocation location) {
    final Geometry geometry = location.getGeometry();
    final DataType geometryDataType = DataTypes.getDataType(geometry);
    final DataType geometryPartDataType = getGeometryPartDataType(geometryDataType);

    int previousPointOffset;
    int[] vertexId = location.getVertexId();
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

      final List<LineString> lines = new ArrayList<>();
      if (previousPoint != null && !previousPoint.isEmpty()) {
        lines.add(newXorLine(geometryFactory, previousPoint, point));
      }
      if (nextPoint != null && !nextPoint.isEmpty()) {
        lines.add(newXorLine(geometryFactory, nextPoint, point));
      }
      if (!lines.isEmpty()) {
        return geometryFactory.lineal(lines);
      }
    }
    return null;
  }

  private boolean hasMouseOverLocation() {
    return !getMouseOverLocations().isEmpty();
  }

  protected boolean isEditable(final AbstractRecordLayer recordLayer) {
    return recordLayer.isExists() && recordLayer.isVisible() && recordLayer.isCanEditRecords();
  }

  protected boolean isGeometryValid(final Geometry geometry) {
    if (DataTypes.POINT.equals(this.addGeometryDataType)) {
      if (geometry instanceof Point) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.MULTI_POINT.equals(this.addGeometryDataType)) {
      if (geometry instanceof Punctual) {
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
      if (geometry instanceof Lineal) {
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
      if (geometry instanceof Polygonal) {
        return true;
      } else {
        return false;
      }
    } else if (DataTypes.GEOMETRY.equals(this.addGeometryDataType)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void keyPressed(final KeyEvent e) {
    final int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_ESCAPE) {
      if (this.dragged) {
        clearMouseOverLocations();
        modeMoveGeometryClear();
        modeEditGeometryVerticesClear();
        modeAddGeometryEditVerticesClear();
        if (this.addLayer == null) {
        } else {
          modeAddGeometryMove(null);
        }

      } else {
        cancel();
      }
    } else if (keyCode == KeyEvent.VK_ALT) {
      if (!this.dragged) {
        if (hasMouseOverLocation() && !this.editGeometryVerticesStart) {
          if (setOverlayAction(ACTION_MOVE_GEOMETRY)) {
            updateMouseOverLocations();
          }
        } else {
          if (isOverlayAction(ACTION_ADD_GEOMETRY)) {
            clearMapCursor();
            setXorGeometry(null);
          }
        }
      }
    }
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    final int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_ALT) {
      if (!this.dragged) {
        if (isOverlayAction(ACTION_MOVE_GEOMETRY)) {
          if (clearOverlayAction(ACTION_MOVE_GEOMETRY)) {
            if (this.addLayer != null) {
              modeAddGeometryMove(null);
            } else {
              updateMouseOverLocations();
            }
          }
        } else {
          if (this.addLayer == null) {
            updateMouseOverLocations();
          } else {
            modeAddGeometryMove(null);
          }
        }
      }
    } else if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
      if (hasMouseOverLocation()) {
        final MultipleUndo edit = new MultipleUndo();
        for (final CloseLocation location : getMouseOverLocations()) {
          final Geometry geometry = location.getGeometry();
          final int[] vertexIndex = location.getVertexId();
          if (vertexIndex != null) {
            try {
              final Geometry newGeometry = geometry.deleteVertex(vertexIndex);
              if (newGeometry.isEmpty()) {
                Toolkit.getDefaultToolkit().beep();
              } else {
                final UndoableEdit geometryEdit = setGeometry(location, newGeometry);
                edit.addEdit(geometryEdit);
              }
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
    } else if (splitLineKeyPress(e)) {
    }
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    super.keyTyped(e);
  }

  protected void modeAddGeometryClear() {
    boolean cleared = clearOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES);
    cleared |= clearOverlayAction(ACTION_ADD_GEOMETRY);

    if (cleared || !hasOverlayAction()) {
      this.addCompleteAction = null;
      this.addGeometry = null;
      this.addGeometryDataType = null;
      this.addGeometryEditVerticesStart = false;
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
      final int clickCount = event.getClickCount();
      if (clickCount == 1) {
        if (isOverlayAction(ACTION_ADD_GEOMETRY) && !hasMouseOverLocation()) {
          Point point = getSnapPoint();
          if (point == null) {
            point = getPoint(event);
          }
          final GeometryFactory geometryFactory = this.addLayer.getGeometryFactory();
          point = point.newGeometry(geometryFactory);
          if (this.addGeometry.isEmpty()) {
            setAddGeometry(point);
          } else {
            final int[] toVertexId = Geometry.newVertexId(this.addGeometryPartIndex, 0);
            final Point previousPoint = this.addGeometry.getToVertex(toVertexId);
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
                    final Geometry geometry = this.addGeometry
                      .newGeometry(this.addLayer.getGeometryFactory());
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
      } else if (clickCount == 2) {
        if (isOverlayAction(ACTION_ADD_GEOMETRY)
          || isOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES)) {
          setXorGeometry(null);
          event.consume();
          Point point = getSnapPoint();
          if (point == null) {
            point = getPoint(event);
          }
          final GeometryFactory geometryFactory = this.addLayer.getGeometryFactory();
          point = point.newGeometry(geometryFactory);
          final int[] toVertexId = Geometry.newVertexId(this.addGeometryPartIndex, 0);
          final Point previousPoint = this.addGeometry.getToVertex(toVertexId);
          if (!point.equals(previousPoint)) {
            final Geometry newGeometry = appendVertex(point);
            setAddGeometry(newGeometry);
          }
          if (isGeometryValid(this.addGeometry)) {
            try {
              setXorGeometry(null);
              if (this.addCompleteAction != null) {
                final Geometry geometry = this.addGeometry
                  .newGeometry(this.addLayer.getGeometryFactory());
                this.addCompleteAction.addComplete(this, geometry);
                modeAddGeometryClear();
              }
            } finally {
              clearMapCursor();
            }
          }
          repaint();
          return true;
        }
      }
    }
    return false;
  }

  protected boolean modeAddGeometryDrag(final MouseEvent event) {
    if (isOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES)) {
      if (this.addGeometryEditVerticesStart) {
        Geometry xorGeometry = null;
        final List<CloseLocation> mouseOverLocations = getMouseOverLocations();
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
        setXorGeometry(xorGeometry);
        if (!hasSnapPoint()) {
          setMapCursor(CURSOR_NODE_EDIT);
        }
        return true;
      }
    } else if (isOverlayAction(ACTION_ADD_GEOMETRY)) {
      modeAddGeometryUpdateXorGeometry();
    }
    return false;
  }

  protected void modeAddGeometryEditVerticesClear() {
    clearOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES);
    clearMouseOverLocations();
    this.addGeometryEditVerticesStart = false;
  }

  protected boolean modeAddGeometryFinish(final MouseEvent event) {
    if (isOverlayAction(ACTION_ADD_GEOMETRY)) {
    } else if (isOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES)) {
      if (this.addGeometryEditVerticesStart && hasMouseOverLocation()) {
        if (event.getButton() == MouseEvent.BUTTON1) {
          this.addGeometryEditVerticesStart = false;
          for (final CloseLocation location : getMouseOverLocations()) {
            final GeometryEditor geometryEditor = geometryEdit(event, location);
            final Geometry newGeometry = geometryEditor.newGeometry();
            setAddGeometry(newGeometry);
          }
          setMouseOverLocationsDo(Collections.emptyList());
          clearOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES);
          return true;
        }
      }
    }
    return false;
  }

  protected boolean modeAddGeometryMove(final MouseEvent event) {
    if (this.addGeometry != null) {
      if (isOverlayAction(ACTION_ADD_GEOMETRY) || isOverlayAction(ACTION_MOVE_GEOMETRY)
        || isOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES)) {

        final MapPanel map = getMap();
        final CloseLocation location = map.findCloseLocation(this.addLayer, null, this.addGeometry);
        final List<CloseLocation> locations = new ArrayList<>();
        if (location != null) {
          locations.add(location);
        }
        final boolean hasMouseOver = setMouseOverLocations(locations);
        if (event != null && event.isAltDown()) {
          if (hasMouseOver) {
            setOverlayAction(ACTION_MOVE_GEOMETRY);
          } else {
            clearOverlayAction(ACTION_MOVE_GEOMETRY);
            if (!hasMouseOverLocation()) {
              clearMapCursor();
            }
          }
        } else {
          clearOverlayAction(ACTION_MOVE_GEOMETRY);

          // TODO make work with multi-part
          if (hasMouseOver) {
            setOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES);
          } else {
            clearOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES);
            modeAddGeometryUpdateXorGeometry();
          }
        }
        return true;
      }

    }
    return false;
  }

  protected boolean modeAddGeometryStart(final MouseEvent event) {
    final int modifiers = event.getModifiersEx();
    if (modifiers == InputEvent.BUTTON1_DOWN_MASK) {
      if (isOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES)) {
        if (hasMouseOverLocation()) {
          this.addGeometryEditVerticesStart = true;
          repaint();
          return true;
        }
      }
    }
    return false;
  }

  protected void modeAddGeometryUpdateXorGeometry() {
    final Point point = getOverlayPoint();
    if (!hasSnapPoint()) {
      setMapCursor(CURSOR_NODE_ADD);
    }
    final int[] firstVertexId = Geometry.newVertexId(this.addGeometryPartIndex, 0);
    Geometry xorGeometry = null;

    if (DataTypes.POINT.equals(this.addGeometryPartDataType)) {
    } else {
      Vertex firstVertex;
      final Vertex toVertex;
      if (this.addGeometry instanceof LineString) {
        firstVertex = this.addGeometry.getVertex(0);
        toVertex = this.addGeometry.getToVertex(0);
      } else {
        firstVertex = this.addGeometry.getVertex(firstVertexId);
        toVertex = this.addGeometry.getToVertex(firstVertexId);
      }
      final GeometryFactory geometryFactory = this.addLayer.getGeometryFactory();
      if (toVertex != null && !toVertex.isEmpty()) {
        if (DataTypes.LINE_STRING.equals(this.addGeometryPartDataType)) {
          xorGeometry = newXorLine(geometryFactory, toVertex, point);
        } else if (DataTypes.POLYGON.equals(this.addGeometryPartDataType)) {
          if (toVertex.equals(firstVertex)) {
            xorGeometry = newXorLine(geometryFactory, toVertex, point);
          } else {
            final Point p1 = geometryFactory.point(toVertex);
            final Point p3 = geometryFactory.point(firstVertex);
            final GeometryFactory viewportGeometryFactory = getViewportGeometryFactory2d();
            xorGeometry = viewportGeometryFactory.lineString(p1, point, p3);
          }
        }
      }
    }
    setXorGeometry(xorGeometry);
  }

  protected void modeEditGeometryVerticesClear() {
    clearOverlayAction(ACTION_EDIT_GEOMETRY_VERTICES);
    clearMouseOverLocations();
    this.editGeometryVerticesStart = false;
  }

  protected boolean modeEditGeometryVerticesDrag(final MouseEvent event) {
    if (this.editGeometryVerticesStart && isOverlayAction(ACTION_EDIT_GEOMETRY_VERTICES)) {
      this.dragged = true;

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
      if (!hasSnapPoint()) {
        setMapCursor(CURSOR_NODE_ADD);
      }
      return true;
    }
    return false;
  }

  protected boolean modeEditGeometryVerticesFinish(final MouseEvent event) {
    if (this.editGeometryVerticesStart && clearOverlayAction(ACTION_EDIT_GEOMETRY_VERTICES)) {
      if (this.dragged && event.getButton() == MouseEvent.BUTTON1) {
        try {
          final MultipleUndo edit = new MultipleUndo();
          final List<CloseLocation> locations = getMouseOverLocations();
          for (final CloseLocation location : locations) {
            final GeometryEditor geometryEditor = geometryEdit(event, location);
            if (geometryEditor.isModified()) {
              Geometry newGeometry = geometryEditor.newGeometry();
              final UndoableEdit geometryEdit = setGeometry(location, newGeometry);
              edit.addEdit(geometryEdit);
            }
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
      final double scale = getViewportScale();
      final List<CloseLocation> closeLocations = new ArrayList<>();
      final MapPanel map = getMap();
      for (final CloseLocation location : map.getCloseSelectedLocations()) {
        final AbstractRecordLayer layer = location.getLayer();
        if (layer.isEditable(scale)) {
          closeLocations.add(location);
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
    if (modifiers == InputEvent.BUTTON1_DOWN_MASK) {
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
    clearMouseOverLocations();
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
        clearOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES);
        clearOverlayAction(ACTION_EDIT_GEOMETRY_VERTICES);
        for (final CloseLocation location : this.moveGeometryLocations) {
          final GeometryFactory geometryFactory = location.getGeometryFactory();
          final Point from = this.moveGeometryStart.convertGeometry(geometryFactory);
          final Point to = this.moveGeometryEnd.convertGeometry(geometryFactory);

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
        repaint();
        return true;
      }
    }
    return false;
  }

  protected boolean modeMoveGeometryStart(final MouseEvent event) {
    if (isOverlayAction(ACTION_MOVE_GEOMETRY) && event.getButton() == MouseEvent.BUTTON1) {
      this.moveGeometryStart = this.moveGeometryEnd = getEventPoint();
      this.moveGeometryLocations = getMouseOverLocations();
      clearMouseOverLocations();
      return true;
    }
    return false;
  }

  private boolean modePopupMenu(final MouseEvent event) {
    if (event.isPopupTrigger()) {
      for (final CloseLocation location : getMouseOverLocations()) {
        final LayerRecord record = location.getRecord();
        if (record != null) {
          final LayerRecordMenu menu = record.getMenu();
          menu.showMenu(record, event);
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public void mouseClicked(final MouseEvent event) {
    if (modeAddGeometryClick(event)) {
    } else if (SwingUtil.isLeftButtonAndNoModifiers(event) && event.getClickCount() == 2) {
      final List<LayerRecord> records = new ArrayList<>();
      final BoundingBox boundingBox = getHotspotBoundingBox();
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
        JOptionPane.showMessageDialog(this, "There are too many " + size
          + " selected to view. Maximum 10. Select fewer records or move mouse to middle of geometry.",
          "Too Many Selected Records", JOptionPane.ERROR_MESSAGE);
        event.consume();
      }
    }
  }

  @Override
  public void mouseDragged(final MouseEvent event) {
    if ((event.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == InputEvent.BUTTON1_DOWN_MASK) {
      this.dragged = true;
      if (modeMoveGeometryDrag(event)) {
      } else if (modeAddGeometryDrag(event)) {
      } else if (modeEditGeometryVerticesDrag(event)) {
      }
    }
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    if (isOverlayAction(ACTION_EDIT_GEOMETRY_VERTICES)) {
    } else if (isOverlayAction(ACTION_MOVE_GEOMETRY)) {
    } else if (isOverlayAction(ACTION_ADD_GEOMETRY)) {
    } else if (isOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES)) {
    } else {
      cancel();
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
    } else if (modePopupMenu(event)) {
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (modeAddGeometryFinish(event)) {
    } else if (modeMoveGeometryFinish(event)) {
    } else if (modeEditGeometryVerticesFinish(event)) {
    } else if (modePopupMenu(event)) {
    }
    if (event.getButton() == MouseEvent.BUTTON1) {
      if (this.dragged) {
        this.dragged = false;
      }
    }
  }

  @Override
  public void paintComponent(final Viewport2D viewport, final Graphics2D graphics) {
    final GeometryFactory geometryFactory2dFloating = getViewportGeometryFactory2d();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (isOverlayAction(ACTION_MOVE_GEOMETRY) && this.moveGeometryStart != null) {
      try (
        BaseCloseable transformCloseable = viewport.setUseModelCoordinates(graphics, true)) {
        for (final CloseLocation location : this.moveGeometryLocations) {
          Geometry geometry = location.getGeometry();
          final GeometryFactory geometryFactory = location.getGeometryFactory();
          final Point from = this.moveGeometryStart.convertGeometry(geometryFactory);
          final Point to = this.moveGeometryEnd.convertGeometry(geometryFactory);
          final double deltaX = to.getX() - from.getX();
          final double deltaY = to.getY() - from.getY();
          geometry = geometry.move(deltaX, deltaY);
          GEOMETRY_RENDERER.paintSelected(viewport, graphics, geometryFactory2dFloating, geometry);
          GEOMETRY_VERTEX_RENDERER.paintSelected(viewport, graphics, geometryFactory2dFloating,
            geometry);
        }
      }
    } else if (this.addGeometry != null) {
      try (
        BaseCloseable transformCloseable = viewport.setUseModelCoordinates(graphics, true)) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          RenderingHints.VALUE_ANTIALIAS_ON);

        GEOMETRY_RENDERER.paintSelected(viewport, graphics, geometryFactory2dFloating,
          this.addGeometry);
        GEOMETRY_VERTEX_RENDERER.paintSelected(viewport, graphics, geometryFactory2dFloating,
          this.addGeometry);
      }
    }
    if (this.moveGeometryStart == null) {
      final List<CloseLocation> mouseOverLocations = getMouseOverLocations();
      try (
        BaseCloseable transformCloseable = viewport.setUseModelCoordinates(graphics, true)) {
        for (final CloseLocation location : mouseOverLocations) {
          final Geometry geometry = location.getGeometry();
          GEOMETRY_RENDERER.paintSelected(viewport, graphics, geometryFactory2dFloating, geometry);
        }
      }
      for (final CloseLocation location : mouseOverLocations) {
        final Geometry geometry = location.getGeometry();
        GEOMETRY_VERTEX_RENDERER.paintSelected(viewport, graphics, geometryFactory2dFloating,
          geometry);
        if (!isOverlayAction(ACTION_MOVE_GEOMETRY) && !this.addGeometryEditVerticesStart
          && !this.editGeometryVerticesStart) {
          final Vertex vertex = location.getVertex();
          if (vertex == null) {
            final MarkerStyle style = MarkerStyle.marker("xLine", 9, WebColors.Blue, 3,
              WebColors.Blue);
            final double orientation = location.getSegment().getOrientaton();
            final Point pointOnLine = location.getViewportPoint();
            MarkerStyleRenderer.renderMarker(viewport, graphics, pointOnLine, style, orientation);
          } else {
            GEOMETRY_CLOSE_VERTEX_RENDERER.paintSelected(viewport, graphics,
              geometryFactory2dFloating, vertex);
          }
        }
      }
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
              final Geometry geometry = this.addGeometry
                .newGeometry(this.addLayer.getGeometryFactory());
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
    if (!DataTypes.GEOMETRY.equals(geometry, this.addGeometry)) {
      final AddGeometryUndoEdit undo = new AddGeometryUndoEdit(geometry);
      addUndo(undo);
      repaint();
    }
  }

  protected void setAddGeometryDataType(final DataType dataType) {
    this.addGeometryDataType = dataType;
    this.addGeometryPartDataType = getGeometryPartDataType(dataType);
  }

  protected UndoableEdit setGeometry(final CloseLocation location, final Geometry newGeometry) {
    if (isOverlayAction(ACTION_ADD_GEOMETRY)
      || isOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES)) {
      if (DataTypes.GEOMETRY.equals(newGeometry, this.addGeometry)) {
        return null;
      } else {
        return new AddGeometryUndoEdit(newGeometry);
      }
    } else {
      final LayerRecord record = location.getRecord();
      final String geometryFieldName = record.getGeometryFieldName();
      final Geometry oldValue = record.getGeometry();
      if (newGeometry == oldValue || newGeometry != null && newGeometry.equalsExact(oldValue)) {
        return null;
      } else {
        final AbstractRecordLayer layer = location.getLayer();
        return layer.newSetFieldUndo(record, geometryFieldName, oldValue, newGeometry);
      }
    }
  }

  protected boolean setMouseOverLocations(final List<CloseLocation> mouseOverLocations) {
    if (this.mouseOverLocations.equals(mouseOverLocations)) {
      return !this.mouseOverLocations.isEmpty();
    } else {
      setMouseOverLocationsDo(mouseOverLocations);
      setSnapPoint(null);
      setXorGeometry(null);

      return updateMouseOverLocations();
    }
  }

  private void setMouseOverLocationsDo(final List<CloseLocation> mouseOverLocations) {
    this.mouseOverLocations = mouseOverLocations;
  }

  // K key to split a record
  protected boolean splitLineKeyPress(final KeyEvent e) {
    final int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_K) {
      if (!isOverlayAction(ACTION_ADD_GEOMETRY) && hasMouseOverLocation()) {
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

  private boolean updateMouseOverLocations() {
    final MapPanel map = getMap();
    if (hasMouseOverLocation()) {
      if (isOverlayAction(ACTION_MOVE_GEOMETRY)) {
        map.clearToolTipText();
      } else {
        final Map<String, Set<CloseLocation>> vertexLocations = new TreeMap<>();
        final Map<String, Set<CloseLocation>> segmentLocations = new TreeMap<>();

        for (final CloseLocation location : getMouseOverLocations()) {
          final String typePath = location.getLayerPath();
          if (location.getVertexId() == null) {
            Maps.addToSet(segmentLocations, typePath, location);
          } else {
            Maps.addToSet(vertexLocations, typePath, location);
          }
        }
        final StringBuilder text = new StringBuilder("<html>");
        appendLocations(text, "Move Vertices", vertexLocations);
        appendLocations(text, "Insert Vertices", segmentLocations);
        text.append("</html>");

        final Point2D eventPoint = getEventPosition();
        map.setToolTipText(eventPoint, text);
      }
      return true;
    } else {
      map.clearToolTipText();
      return false;
    }
  }
}
