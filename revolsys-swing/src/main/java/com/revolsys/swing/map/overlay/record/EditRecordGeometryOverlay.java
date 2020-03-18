package com.revolsys.swing.map.overlay.record;

import java.awt.Cursor;
import java.awt.Graphics2D;
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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.undo.UndoableEdit;

import org.jeometry.common.awt.WebColors;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataType;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.editor.GeometryEditor;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.Dialogs;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.events.KeyEvents;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.elevation.ElevationModelLayer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.marker.MarkerRenderer;
import com.revolsys.swing.map.overlay.AbstractOverlay;
import com.revolsys.swing.map.overlay.AddGeometryCompleteAction;
import com.revolsys.swing.map.overlay.CloseLocation;
import com.revolsys.swing.map.overlay.VertexStyleRenderer;
import com.revolsys.swing.map.overlay.ZoomOverlay;
import com.revolsys.swing.map.overlay.record.geometryeditor.AppendVertexUndoEdit;
import com.revolsys.swing.map.overlay.record.geometryeditor.DeleteVertexUndoEdit;
import com.revolsys.swing.map.overlay.record.geometryeditor.InsertVertexUndoEdit;
import com.revolsys.swing.map.overlay.record.geometryeditor.MoveGeometryUndoEdit;
import com.revolsys.swing.map.overlay.record.geometryeditor.SetVertexUndoEdit;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;
import com.revolsys.swing.undo.AbstractUndoableEdit;
import com.revolsys.swing.undo.MultipleUndo;

public class EditRecordGeometryOverlay extends AbstractOverlay
  implements PropertyChangeListener, MouseListener, MouseMotionListener {

  private class ClearXorUndoEdit extends AbstractUndoableEdit {
    private static final long serialVersionUID = 1L;

    @Override
    protected void redoDo() {
      clearXor();
    }

    @Override
    protected void undoDo() {
      clearXor();
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

  private static final MarkerStyle GEOMETRY_INSERT_VERTEX_STYLE = MarkerStyle.marker("xLine", 9,
    WebColors.Blue, 3, WebColors.Blue);

  private int actionId = 0;

  private AddGeometryCompleteAction addCompleteAction;

  private GeometryDataType<?, ?> addGeometryPartDataType;

  /** Index to the part of the addGeometry that new points should be added too. */
  private int[] addGeometryPartIndex = {};

  private int addGeometryAddVertexPressCount;

  private boolean addGeometryAddVertexActive;

  private AbstractRecordLayer addLayer;

  private boolean dragged = false;

  private boolean editGeometryVerticesStart;

  private boolean addGeometryEditVerticesStart;

  private List<CloseLocation> mouseOverLocations = Collections.emptyList();

  private Point moveGeometryEnd;

  private List<CloseLocation> moveGeometryLocations;

  private Point moveGeometryStart;

  private GeometryEditor<?> addGeometryEditor;

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

  private void addEdit(final MultipleUndo allEdits, final Map<Layer, MultipleUndo> editsByLayer,
    final AbstractRecordLayer layer, final UndoableEdit edit) {
    MultipleUndo layerEdits = editsByLayer.get(layer);
    if (layerEdits == null) {
      layerEdits = layer.newMultipleUndo();
      editsByLayer.put(layer, layerEdits);
      allEdits.addEdit(layerEdits);
    }
    layerEdits.addEdit(edit);
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
  @SuppressWarnings("unchecked")
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
        setOverlayAction(ACTION_ADD_GEOMETRY);
        final GeometryDataType<Geometry, GeometryEditor<?>> geometryDataType = (GeometryDataType<Geometry, GeometryEditor<?>>)geometryField
          .getDataType();
        setAddGeometryDataType(geometryDataType, geometryFactory);
        setMapCursor(CURSOR_NODE_ADD);
      }
    }
  }

  private void addRecords(final List<LayerRecord> results, final LayerGroup group,
    final Geometry boundingBox) {
    final double scale = getViewportScale();
    group.forEachReverse((layer) -> {
      if (layer instanceof LayerGroup) {
        final LayerGroup childGroup = (LayerGroup)layer;
        addRecords(results, childGroup, boundingBox);
      } else if (layer instanceof AbstractRecordLayer) {
        final AbstractRecordLayer recordLayer = (AbstractRecordLayer)layer;
        if (recordLayer.isSelectable(scale)) {
          recordLayer.forEachSelectedRecord((final LayerRecord selectedRecord) -> {
            final Geometry geometry = selectedRecord.getGeometry();
            if (boundingBox.intersects(geometry)) {
              results.add(selectedRecord);
            }
          });
        }
      }
    });
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

  @Override
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

  private void clearXor() {
    setXorGeometry(null);
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

  private GeometryEditor<?> geometryEdit(final CloseLocation location, final Point newPoint) {
    final Geometry geometry = location.getGeometry();
    int[] vertexId = location.getVertexId();
    final GeometryEditor<?> geometryEditor = geometry.newGeometryEditor();
    if (vertexId == null) {
      vertexId = location.getSegmentIdNext();
      geometryEditor.insertVertex(vertexId, newPoint);
    } else {
      geometryEditor.setVertex(vertexId, newPoint);
    }
    if (geometryEditor.isModified()) {
      final int axisCount = geometryEditor.getAxisCount();
      if (axisCount > 2) {
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
    return ElevationModelLayer.getElevation(project, scale, point);
  }

  public GeometryDataType<?, ?> getGeometryPartDataType(final DataType dataType) {
    if (Arrays.asList(GeometryDataTypes.POINT, GeometryDataTypes.MULTI_POINT).contains(dataType)) {
      return GeometryDataTypes.POINT;
    } else if (Arrays.asList(GeometryDataTypes.LINE_STRING, GeometryDataTypes.MULTI_LINE_STRING)
      .contains(dataType)) {
      return GeometryDataTypes.LINE_STRING;
    } else if (Arrays.asList(GeometryDataTypes.POLYGON, GeometryDataTypes.MULTI_POLYGON)
      .contains(dataType)) {
      return GeometryDataTypes.POLYGON;
    } else {
      return GeometryDataTypes.GEOMETRY;
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
    if (GeometryDataTypes.GEOMETRY.equals(geometryPartDataType)) {
    } else if (GeometryDataTypes.POINT.equals(geometryPartDataType)) {
    } else {
      final Point point = getPoint(geometryFactory, event);

      final Vertex vertex = geometry.getVertex(vertexId);
      Point previousPoint = null;
      Point nextPoint = null;

      if (GeometryDataTypes.LINE_STRING.equals(geometryPartDataType)
        || GeometryDataTypes.POLYGON.equals(geometryPartDataType)) {
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
        final MultipleUndo allEdits = new MultipleUndo();
        final Map<Layer, MultipleUndo> editsByLayer = new HashMap<>();
        for (final CloseLocation location : getMouseOverLocations()) {
          final AbstractRecordLayer layer = location.getLayer();
          final Geometry geometry = location.getGeometry();
          final int[] vertexId = location.getVertexId();
          if (vertexId != null) {
            try {
              if (this.addGeometryEditor == null) {
                final GeometryEditor<?> geometryEditor = geometry.newGeometryEditor();
                geometryEditor.deleteVertex(vertexId);
                if (geometryEditor.isModified()) {
                  final Geometry newGeometry = geometryEditor.newGeometry();
                  if (newGeometry.isEmpty()) {
                    SwingUtil.beep();
                  } else {
                    final UndoableEdit edit = setGeometry(location, newGeometry);
                    addEdit(allEdits, editsByLayer, layer, edit);
                  }
                }
              } else {
                final DeleteVertexUndoEdit edit = new DeleteVertexUndoEdit(this.addGeometryEditor,
                  vertexId);
                addEdit(allEdits, editsByLayer, layer, edit);
              }
            } catch (final Exception t) {
              SwingUtil.beep();
            }
          }
        }
        if (!allEdits.isEmpty()) {
          allEdits.addEdit(new ClearXorUndoEdit());
          addUndo(allEdits);
        }
        clearMouseOverLocations();
      }
    } else if (keyCode == KeyEvent.VK_F2 || KeyEvents.altKey(e, KeyEvent.VK_F)) {
      clearMouseOverLocations();
      modeMoveGeometryClear();
      if (this.addCompleteAction != null) {
        final Geometry addGeometry = this.addGeometryEditor.newGeometry();
        this.addCompleteAction.addComplete(this, addGeometry);
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
      this.addGeometryEditor = null;
      this.addGeometryEditVerticesStart = false;
      this.addGeometryPartDataType = null;
      this.addGeometryPartIndex = null;
      this.addGeometryAddVertexPressCount = 0;
      this.addGeometryAddVertexActive = true;
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
      if (clickCount == 2) {
        if (isOverlayAction(ACTION_ADD_GEOMETRY)
          || isOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES)) {
          setXorGeometry(null);
          event.consume();
          modeAddGeometryCompleted();
          return true;
        }
      }
    }
    return false;
  }

  private void modeAddGeometryCompleted() {
    if (this.addGeometryEditor.isValid()) {
      try {
        setXorGeometry(null);
        try {
          setElevations(this.addGeometryEditor);
        } catch (final Exception e) {
          Logs.error(this, "Error setting elevations:" + this.addGeometryEditor);
        }
        if (this.addCompleteAction != null) {
          final Geometry geometry = this.addGeometryEditor.newGeometry();
          this.addCompleteAction.addComplete(this, geometry);
          modeAddGeometryClear();
        }
      } finally {
        clearMapCursor();
      }
    }
    repaint();
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
      if (this.addGeometryAddVertexActive) {
        modeAddGeometryUpdateXorGeometry();
      }
    }
    return false;
  }

  protected void modeAddGeometryEditVerticesClear() {
    clearOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES);
    clearMouseOverLocations();
    this.addGeometryEditVerticesStart = false;
  }

  protected boolean modeAddGeometryFinish(final MouseEvent event) {
    final int button = event.getButton();
    if (isOverlayAction(ACTION_ADD_GEOMETRY)) {
      if (button == MouseEvent.BUTTON3 && !event.isAltDown()) {
        this.addGeometryAddVertexActive = false;
        clearXor();
      } else if (button == MouseEvent.BUTTON1) {
        this.addGeometryAddVertexPressCount--;
        if (this.addGeometryAddVertexPressCount <= 0) {
          this.addGeometryAddVertexPressCount = 0;
          if (this.addGeometryAddVertexActive) {
            Point point = getSnapPoint();
            if (point == null) {
              point = getPoint(event);
            }
            final GeometryFactory geometryFactory = this.addLayer.getGeometryFactory();
            point = point.newGeometry(geometryFactory);
            addUndo(new MultipleUndo( //
              new AppendVertexUndoEdit(this.addGeometryEditor, this.addGeometryPartIndex,
                this.addGeometryPartDataType, point), //
              new ClearXorUndoEdit() //
            ));

            event.consume();
            if (GeometryDataTypes.POINT.equals(this.addGeometryEditor.getDataType())) {
              if (isOverlayAction(ACTION_ADD_GEOMETRY)) {
                modeAddGeometryCompleted();
              }
            }
          }
          this.addGeometryAddVertexActive = true;
          modeAddGeometryUpdateXorGeometry();
          return true;
        }
      }
    } else if (isOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES)) {
      if (this.addGeometryEditVerticesStart && hasMouseOverLocation()) {
        if (button == MouseEvent.BUTTON1) {
          this.addGeometryEditVerticesStart = false;
          final MultipleUndo allEdits = new MultipleUndo();
          final Map<Layer, MultipleUndo> editsByLayer = new HashMap<>();
          final List<CloseLocation> locations = getMouseOverLocations();
          if (this.addGeometryPartDataType == GeometryDataTypes.LINE_STRING && !this.dragged
            && locations.size() == 1 && locations.get(0).isFromVertex()) {
            final CloseLocation location = locations.get(0);
            final AbstractRecordLayer layer = location.getLayer();
            final AppendVertexUndoEdit edit = new AppendVertexUndoEdit(this.addGeometryEditor,
              this.addGeometryPartIndex, this.addGeometryPartDataType, location.getVertex());
            addEdit(allEdits, editsByLayer, layer, edit);
          } else {
            for (final CloseLocation location : locations) {
              final AbstractRecordLayer layer = location.getLayer();
              Point newPoint;
              if (this.dragged) {
                newPoint = getSnapOrEventPointWithElevation(event, location);
              } else {
                newPoint = location.getViewportPoint();
              }
              int[] vertexId = location.getVertexId();
              AbstractUndoableEdit locationEdit;
              if (vertexId == null) {
                vertexId = location.getSegmentIdNext();
                locationEdit = new InsertVertexUndoEdit(this.addGeometryEditor, vertexId, newPoint);
              } else {
                locationEdit = new SetVertexUndoEdit(this.addGeometryEditor, vertexId, newPoint);
              }
              addEdit(allEdits, editsByLayer, layer, locationEdit);
            }
          }
          if (!allEdits.isEmpty()) {
            allEdits.addEdit(new ClearXorUndoEdit());
            addUndo(allEdits);
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
    if (this.addGeometryEditor != null) {
      if (isOverlayAction(ACTION_ADD_GEOMETRY) || isOverlayAction(ACTION_MOVE_GEOMETRY)
        || isOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES)) {

        final MapPanel map = getMap();
        final CloseLocation location = map.findCloseLocation(this.addLayer, null,
          this.addGeometryEditor);
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
            if (this.addGeometryPartDataType == GeometryDataTypes.LINE_STRING && location != null
              && location.isFromVertex()) {
              modeAddGeometryUpdateXorGeometry();
            }
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
    if (modifiers == InputEvent.BUTTON1_DOWN_MASK && event.getClickCount() == 1) {
      if (isOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES)) {
        if (hasMouseOverLocation()) {
          this.addGeometryEditVerticesStart = true;
          repaint();
          return true;
        }
      }
    }
    if (isOverlayAction(ACTION_ADD_GEOMETRY)) {
      if (event.getButton() == 1) {
        this.addGeometryAddVertexPressCount++;
        event.consume();
        return true;
      }
    }
    return false;
  }

  protected void modeAddGeometryUpdateXorGeometry() {
    if (this.addGeometryPartIndex == null) {
      setXorGeometry(null);
    } else {
      final Point point = getOverlayPoint();
      if (!hasSnapPoint()) {
        setMapCursor(CURSOR_NODE_ADD);
      }
      final int[] firstVertexId = Geometry.newVertexId(this.addGeometryPartIndex, 0);
      Geometry xorGeometry = null;

      if (GeometryDataTypes.POINT.equals(this.addGeometryPartDataType)) {
      } else {
        final Vertex firstVertex = this.addGeometryEditor.getVertex(firstVertexId);
        final Vertex toVertex = this.addGeometryEditor.getToVertex(firstVertexId);

        final GeometryFactory geometryFactory = this.addLayer.getGeometryFactory();
        if (toVertex != null && !toVertex.isEmpty()) {
          if (GeometryDataTypes.LINE_STRING.equals(this.addGeometryPartDataType)) {
            xorGeometry = newXorLine(geometryFactory, toVertex, point);
          } else if (GeometryDataTypes.POLYGON.equals(this.addGeometryPartDataType)) {
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
  }

  protected void modeEditGeometryVerticesClear() {
    if (clearOverlayAction(ACTION_EDIT_GEOMETRY_VERTICES)) {
      clearMouseOverLocations();
    }
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
      if (event.getButton() == MouseEvent.BUTTON1) {
        if (this.dragged) {
          try {
            final MultipleUndo allEdits = new MultipleUndo();
            final Map<Layer, MultipleUndo> editsByLayer = new HashMap<>();
            final List<CloseLocation> locations = getMouseOverLocations();
            for (final CloseLocation location : locations) {
              final Point newPoint = getSnapOrEventPointWithElevation(event, location);
              final GeometryEditor<?> geometryEditor = geometryEdit(location, newPoint);
              if (geometryEditor.isModified()) {
                final Geometry newGeometry = geometryEditor.newGeometry();
                final UndoableEdit geometryEdit = setGeometry(location, newGeometry);
                final AbstractRecordLayer layer = location.getLayer();
                addEdit(allEdits, editsByLayer, layer, geometryEdit);
              }
            }
            if (!allEdits.isEmpty()) {
              addUndo(allEdits);
            }
          } finally {
            clearMouseOverLocations();
            modeEditGeometryVerticesClear();
          }
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
    final boolean cleared = clearOverlayAction(ACTION_MOVE_GEOMETRY);
    this.moveGeometryStart = null;
    this.moveGeometryEnd = null;
    this.moveGeometryLocations = null;
    if (cleared) {
      clearMouseOverLocations();
    }
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
        final MultipleUndo allEdits = new MultipleUndo();
        final Map<Layer, MultipleUndo> editsByLayer = new HashMap<>();
        final List<CloseLocation> moveGeometryLocations = this.moveGeometryLocations;
        if (moveGeometryLocations != null) {
          for (final CloseLocation location : moveGeometryLocations) {
            final AbstractRecordLayer layer = location.getLayer();
            final GeometryFactory geometryFactory = location.getGeometryFactory();
            final Point from = this.moveGeometryStart.convertGeometry(geometryFactory);
            final Point to = this.moveGeometryEnd.convertGeometry(geometryFactory);

            final double deltaX = to.getX() - from.getX();
            final double deltaY = to.getY() - from.getY();
            if (deltaX != 0 || deltaY != 0) {
              final Geometry geometry = location.getGeometry();
              if (geometry instanceof GeometryEditor<?>) {
                final GeometryEditor<?> geometryEditor = (GeometryEditor<?>)geometry;
                final MoveGeometryUndoEdit edit = new MoveGeometryUndoEdit(geometryEditor, deltaX,
                  deltaY);
                addEdit(allEdits, editsByLayer, layer, edit);
              } else {
                final Geometry newGeometry = geometry.edit(editor -> {
                  editor.move(deltaX, deltaY);
                  for (final Vertex vertex : editor.vertices()) {
                    final double z = getElevation(vertex);
                    if (Double.isFinite(z)) {
                      vertex.setZ(z);
                    }
                  }
                  return editor;
                });
                final UndoableEdit edit = setGeometry(location, newGeometry);
                addEdit(allEdits, editsByLayer, layer, edit);
              }
            }
          }
          if (!allEdits.isEmpty()) {
            allEdits.addEdit(new ClearXorUndoEdit());
            addUndo(allEdits);
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
        if (showMenu(record, event)) {
          return true;
        }
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
      final Geometry boundary = boundingBox.toRectangle().prepare();
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
        Dialogs.showMessageDialog("There are too many " + size
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
      modeEditGeometryVerticesClear();
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
  public void paintComponent(final Graphics2DViewRenderer view, final Graphics2D graphics) {
    final GeometryFactory geometryFactory2dFloating = getViewportGeometryFactory2d();
    if (isOverlayAction(ACTION_MOVE_GEOMETRY) && this.moveGeometryStart != null) {
      for (final CloseLocation location : this.moveGeometryLocations) {
        Geometry geometry = location.getGeometry();
        final GeometryFactory geometryFactory = location.getGeometryFactory();
        final Point from = this.moveGeometryStart.convertGeometry(geometryFactory);
        final Point to = this.moveGeometryEnd.convertGeometry(geometryFactory);
        final double deltaX = to.getX() - from.getX();
        final double deltaY = to.getY() - from.getY();
        geometry = geometry.edit(editor -> editor.move(deltaX, deltaY));
        GEOMETRY_RENDERER.paintSelected(view, geometryFactory2dFloating, geometry);
        GEOMETRY_VERTEX_RENDERER.paintSelected(view, geometry);
      }
    } else if (this.addGeometryEditor != null) {
      final Geometry addGeometry = this.addGeometryEditor.getCurrentGeometry();
      GEOMETRY_RENDERER.paintSelected(view, geometryFactory2dFloating, addGeometry);
      GEOMETRY_VERTEX_RENDERER.paintSelected(view, addGeometry);
    }
    if (this.moveGeometryStart == null) {
      final List<CloseLocation> mouseOverLocations = getMouseOverLocations();
      for (final CloseLocation location : mouseOverLocations) {
        final Geometry geometry = location.getGeometry();
        GEOMETRY_RENDERER.paintSelected(view, geometryFactory2dFloating, geometry);
      }
      for (final CloseLocation location : mouseOverLocations) {
        final Geometry geometry = location.getGeometry();
        GEOMETRY_VERTEX_RENDERER.paintSelected(view, geometry);
        if (!isOverlayAction(ACTION_MOVE_GEOMETRY) && !this.addGeometryEditVerticesStart
          && !this.editGeometryVerticesStart) {
          final Vertex vertex = location.getVertex();
          if (vertex == null) {
            final double orientation = location.getSegment().getOrientaton();
            final Point pointOnLine = location.getViewportPoint();
            try (
              MarkerRenderer markerRenderer = GEOMETRY_INSERT_VERTEX_STYLE
                .newMarkerRenderer(view)) {
              markerRenderer.renderMarkerPoint(pointOnLine, orientation);
            }
          } else {
            GEOMETRY_CLOSE_VERTEX_RENDERER.paintSelected(view, graphics, geometryFactory2dFloating,
              vertex);
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
        if (this.addGeometryEditor.isValid()) {
          try {
            setXorGeometry(null);
            if (this.addCompleteAction != null) {
              final Geometry geometry = this.addGeometryEditor.newGeometry();
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

  protected void setAddGeometryDataType(
    final GeometryDataType<Geometry, GeometryEditor<?>> dataType,
    final GeometryFactory geometryFactory) {
    this.addGeometryPartDataType = getGeometryPartDataType(dataType);
    this.addGeometryEditor = dataType.newGeometryEditor(geometryFactory);
    this.addGeometryPartIndex = this.addGeometryEditor.getFirstGeometryId();
  }

  private void setElevations(final GeometryEditor<?> geometryEditor) {
    final int axisCount = geometryEditor.getAxisCount();
    if (axisCount > 2) {
      for (final Vertex vertex : geometryEditor.vertices()) {
        final double z = getElevation(vertex);
        if (Double.isFinite(z)) {
          vertex.setZ(z);
        }
      }
    }
  }

  protected UndoableEdit setGeometry(final CloseLocation location, final Geometry newGeometry) {
    if (isOverlayAction(ACTION_ADD_GEOMETRY)
      || isOverlayAction(ACTION_ADD_GEOMETRY_EDIT_VERTICES)) {
      if (GeometryDataTypes.GEOMETRY.equals(newGeometry, this.addGeometryEditor)) {
        return null;
      } else {
        // TODO
        return null;
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
        final List<CloseLocation> locations = getMouseOverLocations();
        final Map<AbstractRecordLayer, List<CloseLocation>> locationsByLayer = new TreeMap<>();
        for (final CloseLocation location : locations) {
          final LayerRecord record = location.getRecord();
          final AbstractRecordLayer layer = record.getLayer();
          Maps.addToList(locationsByLayer, layer, location);
        }
        for (final AbstractRecordLayer layer : locationsByLayer.keySet()) {
          final List<CloseLocation> layerLocations = locationsByLayer.get(layer);
          layer.processTasks("Split Records", layerLocations, location -> {
            final LayerRecord record = location.getRecord();
            layer.splitRecord(record, location);
          });
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
