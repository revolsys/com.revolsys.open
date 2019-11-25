package com.revolsys.swing.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.undo.UndoableEdit;

import org.jdesktop.swingx.JXBusyLabel;
import org.jeometry.common.awt.WebColors;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.number.Doubles;

import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryComponent;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.util.RectangleUtil;
import com.revolsys.io.BaseCloseable;
import com.revolsys.record.Record;
import com.revolsys.swing.Dialogs;
import com.revolsys.swing.Icons;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.field.BaseComboBox;
import com.revolsys.swing.listener.ConsumerSelectedItemListener;
import com.revolsys.swing.listener.EnableComponentListener;
import com.revolsys.swing.map.border.FullSizeLayoutManager;
import com.revolsys.swing.map.border.MapRulerBorder;
import com.revolsys.swing.map.component.GeometryFactoryField;
import com.revolsys.swing.map.component.MapPointerElevation;
import com.revolsys.swing.map.component.MapPointerLocation;
import com.revolsys.swing.map.component.SelectMapCoordinateSystem;
import com.revolsys.swing.map.component.SelectMapScale;
import com.revolsys.swing.map.component.SelectMapUnitsPerPixel;
import com.revolsys.swing.map.layer.BaseMapLayerGroup;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.NullLayer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.list.LayerGroupListModel;
import com.revolsys.swing.map.listener.FileDropTargetListener;
import com.revolsys.swing.map.overlay.AbstractOverlay;
import com.revolsys.swing.map.overlay.CloseLocation;
import com.revolsys.swing.map.overlay.EditGeoreferencedImageOverlay;
import com.revolsys.swing.map.overlay.LayerMapOverlay;
import com.revolsys.swing.map.overlay.LayerRendererOverlay;
import com.revolsys.swing.map.overlay.MapOverlay;
import com.revolsys.swing.map.overlay.MeasureOverlay;
import com.revolsys.swing.map.overlay.MouseOverlay;
import com.revolsys.swing.map.overlay.ShortestPathOverlay;
import com.revolsys.swing.map.overlay.ToolTipOverlay;
import com.revolsys.swing.map.overlay.ZoomOverlay;
import com.revolsys.swing.map.overlay.record.EditRecordGeometryOverlay;
import com.revolsys.swing.map.overlay.record.SelectRecordsOverlay;
import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.parallel.SwingWorkerProgressBar;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Pair;
import com.revolsys.util.Preferences;
import com.revolsys.util.Property;
import com.revolsys.value.GlobalBooleanValue;

public class MapPanel extends JPanel implements GeometryFactoryProxy, PropertyChangeListener {
  public static final String MAP_PANEL = "INTERNAL_layeredPanel";

  private static final long serialVersionUID = 1L;

  public static MapPanel getMapPanel(final Layer layer) {
    if (layer == null) {
      return null;
    } else {
      final LayerGroup project = layer.getProject();
      if (project == null) {
        return null;
      } else {
        return project.getProperty(MAP_PANEL);
      }
    }
  }

  public static void zoomToBoundingBox(final String baseName,
    final BoundingBoxProxy boundingBoxProxy) {
    GeometryFactoryField.promptGeometryFactory(baseName, boundingBoxProxy, geometryFactory -> {
      final Project project = Project.get();
      final MapPanel mapPanel = project.getMapPanel();
      final BoundingBox boundingBox = boundingBoxProxy.getBoundingBox() //
        .bboxEdit(editor -> editor.setGeometryFactory(geometryFactory));
      mapPanel.zoomToBoundingBox(boundingBox);
    });
  }

  private JLabel messageLabel;

  private BaseComboBox<Layer> baseMapLayerField;

  private BaseMapLayerGroup baseMapLayers;

  private LayerMapOverlay baseMapOverlay;

  private final JXBusyLabel busyLabel = new JXBusyLabel(new Dimension(100, 100));

  private List<CloseLocation> closeSelectedLocations = Collections.emptyList();

  private List<LayerRecord> closeSelectedRecords = Collections.emptyList();

  private FileDropTargetListener fileDropListener;

  private boolean initializing = true;

  private final JLayeredPane layeredPane;

  private final BasePanel layeredPanel;

  private LayerMapOverlay layerOverlay;

  private BasePanel leftStatusBar = new BasePanel(new FlowLayout(FlowLayout.LEFT, 2, 2));

  private MouseOverlay mouseOverlay;

  private final Map<String, Cursor> overlayActionCursors = new HashMap<>();

  private final LinkedList<Cursor> overlayActionCursorStack = new LinkedList<>();

  private JLabel overlayActionLabel;

  /** Map from an overlay action (current) to the overlay actions that can override it (new). */
  private final Map<String, Set<String>> overlayActionOverrides = new HashMap<>();

  private final LinkedList<String> overlayActionStack = new LinkedList<>();

  private int overlayIndex = 1;

  private final Preferences preferences;

  private SwingWorkerProgressBar progressBar;

  private final Project project;

  private final ProjectFrame projectFrame;

  private BasePanel rightStatusBar = new BasePanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));

  private double scale = 500000000;

  private SelectMapCoordinateSystem selectCoordinateSystem;

  private final List<AbstractRecordLayer> selectedRecordLayers = new ArrayList<>();

  private final GlobalBooleanValue settingBoundingBox = new GlobalBooleanValue(false);

  private boolean settingScale;

  private JPanel statusBarPanel;

  private final ToolBar toolBar = new ToolBar();

  private ToolTipOverlay toolTipOverlay;

  private final UndoManager undoManager = new UndoManager();

  private final GlobalBooleanValue updateZoomHistory = new GlobalBooleanValue(true);

  private final ComponentViewport2D viewport;

  private Component visibleOverlay;

  private JButton zoomBookmarkButton;

  private final LinkedList<BoundingBox> zoomHistory = new LinkedList<>();

  private int zoomHistoryIndex = -1;

  private final LinkedList<Pair<String, Color>> messageStack = new LinkedList<>();

  private boolean menuVisible;

  public MapPanel(final ProjectFrame projectFrame, final Preferences preferences,
    final Project project) {
    super(new BorderLayout());
    this.projectFrame = projectFrame;
    this.preferences = preferences;
    this.project = project;

    this.baseMapLayers = project.getBaseMapLayers();
    project.setProperty(MAP_PANEL, this);
    this.layeredPane = new JLayeredPane();
    this.layeredPane.setOpaque(true);
    this.layeredPane.setBackground(Color.WHITE);
    this.layeredPane.setVisible(true);
    this.layeredPane.setLayout(new FullSizeLayoutManager());

    this.layeredPanel = new BasePanel(new GridLayout(1, 1), this.layeredPane);

    this.busyLabel.setDelay(200);
    this.busyLabel.setBusy(true);
    this.busyLabel.setHorizontalAlignment(SwingConstants.CENTER);
    this.busyLabel.setVerticalAlignment(SwingConstants.CENTER);
    this.busyLabel.setBackground(WebColors.White);
    this.busyLabel.setOpaque(true);
    add(this.busyLabel, BorderLayout.CENTER);

    this.viewport = new ComponentViewport2D(project, this.layeredPane);
    final BoundingBox boundingBox = project.getViewBoundingBox();
    if (boundingBox != null && !boundingBox.isEmpty()) {
      this.zoomHistory.add(boundingBox);
      this.zoomHistoryIndex = 0;
    }
    Property.addListener(this.viewport, this);

    final MapRulerBorder border = new MapRulerBorder(this.viewport);
    this.layeredPanel.setBorder(border);

    this.baseMapOverlay = new LayerRendererOverlay(this);
    this.baseMapOverlay.setLayer(NullLayer.INSTANCE);
    this.layeredPane.add((JComponent)this.baseMapOverlay, Integer.valueOf(0));
    Property.addListener(this.baseMapOverlay, "layer", this);

    this.layerOverlay = newLayerOverlay();
    this.layerOverlay.setShowAreaBoundingBox(project.getProperty("showAreaBoundingBox", true));
    this.layeredPane.add((JComponent)this.layerOverlay, Integer.valueOf(1));

    Property.addListener(this.baseMapLayers, this);
    Property.addListener(project, this);

    addMapOverlays();

    newToolBar();

    newStatusBar();

    this.fileDropListener = new FileDropTargetListener(this);
    this.undoManager.addKeyMap(this);
  }

  public void addBaseMap(final Layer layer) {
    if (layer != null) {
      this.baseMapLayers.addLayer(layer);
    }
    if (this.baseMapLayers.getLayerCount() == 1) {
      if (layer.isVisible()) {
        setBaseMapLayer(layer);
      }
    }
  }

  public void addCoordinateSystem(final int srid) {
    this.selectCoordinateSystem.addGeometryFactory(srid);
  }

  public void addMapOverlay(final int zIndex, final MapOverlay overlay) {
    this.layeredPane.add((JComponent)overlay, Integer.valueOf(zIndex));
    if (overlay instanceof PropertyChangeListener) {
      final PropertyChangeListener listener = (PropertyChangeListener)overlay;
      Property.addListener(this, listener);
      Property.addListener(this.project, listener);
      Property.addListener(this.baseMapLayers, listener);
    }
    Property.addListener(overlay, this);
  }

  public void addMapOverlay(final MapOverlay overlay) {
    final int zIndex = 100 * this.overlayIndex++;
    addMapOverlay(zIndex, overlay);
  }

  protected void addMapOverlays() {
    new SelectRecordsOverlay(this);
    new ZoomOverlay(this);
    new EditRecordGeometryOverlay(this);
    this.mouseOverlay = new MouseOverlay(this, this.layeredPane);
    new EditGeoreferencedImageOverlay(this);
    new MeasureOverlay(this);
    new ShortestPathOverlay(this);
    this.toolTipOverlay = new ToolTipOverlay(this);
  }

  public void addOverlayActionOverride(final String overlayAction,
    final String... overrideOverlayActions) {
    for (final String overrideOverlayAction : overrideOverlayActions) {
      Maps.addToSet(this.overlayActionOverrides, overlayAction, overrideOverlayAction);
    }
  }

  private void addPointerLocation(final boolean geographics) {
    final MapPointerLocation location = new MapPointerLocation(this, geographics);
    this.leftStatusBar.add(location);
  }

  public void addUndo(final UndoableEdit edit) {
    this.undoManager.addEdit(edit);
  }

  public void addZoomBookmark() {
    final BoundingBox boundingBox = getBoundingBox();
    if (!boundingBox.isEmpty()) {
      final String name = Dialogs.showInputDialog("Enter bookmark name", "Add Zoom Bookmark",
        JOptionPane.QUESTION_MESSAGE);
      if (Property.hasValue(name)) {
        final Project project = getProject();
        project.addZoomBookmark(name, boundingBox);
      }
    }
  }

  public boolean canOverrideOverlayAction(final String newAction) {
    final String currentAction = getOverlayAction();
    if (newAction == null) {
      return false;
    } else if (currentAction == null) {
      return true;
    } else if (currentAction.equals(newAction)) {
      return true;
    } else {
      final Set<String> overrideActions = this.overlayActionOverrides.get(currentAction);
      if (overrideActions == null) {
        return false;
      } else {
        return overrideActions.contains(newAction);
      }
    }
  }

  public boolean canOverrideOverlayAction(final String newAction, final String currentAction) {
    if (currentAction == null) {
      return true;
    } else {
      final Set<String> overrideActions = this.overlayActionOverrides.get(currentAction);
      if (overrideActions == null) {
        return false;
      } else {
        return overrideActions.contains(newAction);
      }
    }
  }

  public void clearCloseSelected() {
    if (this.closeSelectedRecords != null) {
      this.closeSelectedRecords.clear();
    }
    if (this.closeSelectedLocations != null) {
      this.closeSelectedLocations.clear();
    }
  }

  public void clearMessage(final String overlayAction) {
    this.messageLabel.setVisible(false);
  }

  public boolean clearOverlayAction(final String overlayAction) {
    if (overlayAction == null) {
      return false;
    } else if (isOverlayAction(overlayAction)) {
      this.messageLabel.setVisible(false);
      this.overlayActionCursorStack.pop();
      this.overlayActionStack.pop();
      this.messageStack.pop();
      if (hasOverlayAction()) {
        final Cursor cursor = this.overlayActionCursorStack.peek();
        setViewportCursor(cursor);
        final String previousAction = this.overlayActionStack.peek();
        this.overlayActionLabel.setText(CaseConverter.toCapitalizedWords(previousAction));
        final Pair<String, Color> message = this.messageStack.peek();
        if (message != null) {
          this.messageLabel.setText(message.getValue1());
          this.messageLabel.setForeground(message.getValue2());
          this.messageLabel.setVisible(true);
        }
      } else {
        this.overlayActionLabel.setText("");
        this.overlayActionLabel.setVisible(false);
        this.messageLabel.setVisible(false);
        setViewportCursor(AbstractOverlay.DEFAULT_CURSOR);
      }
      firePropertyChange("overlayAction", overlayAction, null);
      return true;
    } else {
      return false;
    }
  }

  public void clearOverlayActions() {
    while (!this.overlayActionStack.isEmpty()) {
      final String action = this.overlayActionStack.element();
      clearOverlayAction(action);
    }
  }

  public void clearToolTipText() {
    this.toolTipOverlay.clearText();
  }

  public void clearVisibleOverlay(final Component overlay) {
    if (this.visibleOverlay == overlay) {
      try {
        for (final Component component : this.layeredPane.getComponents()) {
          if (component != this.mouseOverlay) {
            component.setVisible(true);
          }
        }
      } finally {
        this.visibleOverlay = null;
      }
    }
  }

  public void clearZoomHistory() {
    this.zoomHistory.clear();
    this.zoomHistoryIndex = -1;
    firePropertyChange("zoomPreviousEnabled", true, false);
    firePropertyChange("zoomNextEnabled", true, false);
  }

  public void destroy() {
    setDropTarget(null);
    Property.removeAllListeners(this);
    setDropTarget(null);
    this.layerOverlay.destroy();
    for (final Component overlay : this.layeredPane.getComponents()) {
      if (overlay instanceof AbstractOverlay) {
        final AbstractOverlay abstractOverlay = (AbstractOverlay)overlay;
        abstractOverlay.destroy();
      }
    }
    removeAll();

    this.layeredPane.removeAll();
    this.statusBarPanel.removeAll();
    this.leftStatusBar = null;
    this.rightStatusBar = null;
    if (this.baseMapLayers != null) {
      this.baseMapLayers.delete();
    }
    this.baseMapLayers = null;
    this.baseMapOverlay = null;
    this.fileDropListener = null;
    this.layerOverlay = null;
    this.progressBar = null;
    this.project.reset();
    this.toolBar.clear();
    this.toolTipOverlay = null;
    this.undoManager.die();
    Property.removeAllListeners(this.viewport.getPropertyChangeSupport());
    this.zoomBookmarkButton = null;
    this.zoomHistory.clear();
  }

  @Override
  protected void finalize() throws Throwable {
    this.layerOverlay.destroy();
    super.finalize();
  }

  public CloseLocation findCloseLocation(final AbstractRecordLayer layer, final LayerRecord record,
    final Geometry geometry) {
    final Point point = MouseOverlay.getEventPoint();
    final double x = point.getX();
    final double y = point.getY();
    final double maxDistance = this.viewport.getHotspotMapUnits();
    final GeometryFactory geometryFactory = this.viewport.getGeometryFactory2dFloating();
    final Geometry convertedGeometry = geometry.convertGeometry(geometryFactory);
    final Pair<GeometryComponent, Double> closestGeometryComponent = convertedGeometry
      .findGeometryComponentWithinDistance(x, y, maxDistance);

    if (!closestGeometryComponent.isEmpty()) {
      final GeometryComponent geometryComponent = closestGeometryComponent.getValue1();
      if (geometryComponent instanceof Vertex) {
        final Vertex convertedVertex = (Vertex)geometryComponent;
        final int[] vertexId = convertedVertex.getVertexId();
        // Get the vertex from the original geometry
        final Vertex vertex = geometry.getVertex(vertexId);
        return new CloseLocation(layer, record, vertex);
      } else if (geometryComponent instanceof Segment) {
        final Segment convertedSegment = (Segment)geometryComponent;
        final int[] segmentId = convertedSegment.getSegmentId();
        // Get the segment from the original geometry
        final Segment segment = geometry.getSegment(segmentId);

        final Point closePoint = convertedSegment.project(x, y);
        final Point sourcePoint = geometry.convertGeometry(closePoint);
        return new CloseLocation(layer, record, segment, closePoint, sourcePoint);
      }
    }
    return null;
  }

  public Layer getBaseMapLayer() {
    return this.baseMapOverlay.getLayer();
  }

  public LayerGroup getBaseMapLayers() {
    return this.baseMapLayers;
  }

  public LayerMapOverlay getBaseMapOverlay() {
    return this.baseMapOverlay;
  }

  public BoundingBox getBoundingBox() {
    return this.viewport.getBoundingBox();
  }

  public LayerRecord getCloseRecord() {
    final BoundingBox boundingBox = getHotspotBoundingBox();
    final double scale = getViewport().getScale();
    return getCloseRecord(this.project, boundingBox, scale);
  }

  private LayerRecord getCloseRecord(final LayerGroup layerGroup, final BoundingBox boundingBox,
    final double scale) {
    for (final Layer layer : layerGroup) {
      if (layer.isVisible(scale)) {
        if (layer instanceof LayerGroup) {
          final LayerGroup childGroup = (LayerGroup)layer;
          final LayerRecord record = getCloseRecord(childGroup, boundingBox, scale);
          if (record != null) {
            return record;
          }
        } else if (layer instanceof AbstractRecordLayer) {
          final AbstractRecordLayer recordLayer = (AbstractRecordLayer)layer;
          for (final LayerRecord record : recordLayer.getRecords(boundingBox)) {
            if (recordLayer.isVisible(record)) {
              final Geometry geometry = record.getGeometry();
              if (geometry.intersects(geometry)) {
                return record;
              }
            }
          }
        }
      }
    }
    return null;
  }

  public List<CloseLocation> getCloseSelectedLocations() {
    return this.closeSelectedLocations;
  }

  public List<LayerRecord> getCloseSelectedRecords() {
    return this.closeSelectedRecords;
  }

  public FileDropTargetListener getFileDropListener() {
    return this.fileDropListener;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.viewport.getGeometryFactory();
  }

  public BoundingBox getHotspotBoundingBox() {
    final Point point = MouseOverlay.getEventPoint();
    final double x = point.getX();
    final double y = point.getY();
    final ComponentViewport2D viewport = this.viewport;
    final double maxDistance = viewport.getHotspotMapUnits();
    final GeometryFactory geometryFactory = viewport.getGeometryFactory2dFloating();
    return geometryFactory.newBoundingBox(x - maxDistance, y - maxDistance, x + maxDistance,
      y + maxDistance);
  }

  public MapOverlay getLayerOverlay() {
    return this.layerOverlay;
  }

  public JPanel getLeftStatusBar() {
    return this.leftStatusBar;
  }

  @SuppressWarnings("unchecked")
  public <T extends JComponent> T getMapOverlay(final Class<T> overlayClass) {
    for (final Component component : this.layeredPane.getComponents()) {
      if (overlayClass.isAssignableFrom(component.getClass())) {
        return (T)component;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <T extends JComponent> List<T> getMapOverlays(final Class<T> overlayClass) {
    final List<T> overlays = new ArrayList<>();
    for (final Component component : this.layeredPane.getComponents()) {
      if (overlayClass.isAssignableFrom(component.getClass())) {
        overlays.add((T)component);
      }
    }
    return overlays;
  }

  public MouseOverlay getMouseOverlay() {
    return this.mouseOverlay;
  }

  public String getOverlayAction() {
    if (this.overlayActionStack == null) {
      return null;
    } else {
      return this.overlayActionStack.peek();
    }
  }

  public Cursor getOverlayActionCursor(final String name) {
    return Maps.get(this.overlayActionCursors, name, AbstractOverlay.DEFAULT_CURSOR);
  }

  public Preferences getPreferences() {
    return this.preferences;
  }

  public SwingWorkerProgressBar getProgressBar() {
    return this.progressBar;
  }

  public Project getProject() {
    return this.project;
  }

  public ProjectFrame getProjectFrame() {
    return this.projectFrame;
  }

  public JPanel getRightStatusBar() {
    return this.rightStatusBar;
  }

  public double getScale() {
    if (this.viewport == null) {
      return Double.NaN;
    } else {
      return this.viewport.getScale();
    }
  }

  public List<Long> getScales() {
    return this.viewport.getScales();
  }

  public ToolBar getToolBar() {
    return this.toolBar;
  }

  public UndoManager getUndoManager() {
    return this.undoManager;
  }

  public double getUnitsPerPixel() {
    if (this.viewport == null) {
      return 1;
    } else {
      return this.viewport.getUnitsPerPixel();
    }
  }

  public ComponentViewport2D getViewport() {
    return this.viewport;
  }

  public boolean hasOverlayAction() {
    return !this.overlayActionStack.isEmpty();
  }

  public boolean hasOverlayAction(final String overlayAction) {
    return this.overlayActionStack.contains(overlayAction);
  }

  public boolean isInitializing() {
    return this.initializing;
  }

  public boolean isMenuVisible() {
    return this.menuVisible;
  }

  public boolean isOverlayAction(final String overlayAction) {
    if (overlayAction == null) {
      return false;
    } else {
      final String oldAction = getOverlayAction();
      if (oldAction == null) {
        return false;
      } else {
        return overlayAction.equals(oldAction);
      }
    }
  }

  public boolean isZoomNextEnabled() {
    return this.zoomHistoryIndex < this.zoomHistory.size() - 1;
  }

  public boolean isZoomPreviousEnabled() {
    return this.zoomHistoryIndex > 0;
  }

  public void mouseExitedCloseSelected(final MouseEvent event) {
    clearCloseSelected();
  }

  public boolean mouseMovedCloseSelected(final MouseEvent event) {
    if (isOverlayAction(SelectRecordsOverlay.ACTION_SELECT_RECORDS)
      || isOverlayAction(ZoomOverlay.ACTION_ZOOM_BOX) || isOverlayAction(ZoomOverlay.ACTION_PAN)
      || this.selectedRecordLayers.isEmpty()) {
      clearCloseSelected();
      return false;
    } else {
      final double scale = getViewport().getScale();

      final BoundingBox boundingBox = getHotspotBoundingBox();
      final List<LayerRecord> closeRecords = new ArrayList<>();
      final List<CloseLocation> closeLocations = new ArrayList<>();
      try {
        for (final AbstractRecordLayer layer : this.selectedRecordLayers) {
          for (final LayerRecord closeRecord : layer.getSelectedRecords(boundingBox)) {
            if (layer.isVisible(scale) && layer.isVisible(closeRecord)) {
              final Geometry geometry = closeRecord.getGeometry();
              final CloseLocation closeLocation = findCloseLocation(layer, closeRecord, geometry);
              if (closeLocation != null) {
                closeRecords.add(closeRecord);
                closeLocations.add(closeLocation);
              }
            }
          }

        }
      } catch (final ConcurrentModificationException e) {
      }
      this.closeSelectedRecords = closeRecords;
      this.closeSelectedLocations = closeLocations;
      repaint();
      return true;
    }
  }

  public void moveToFront(final JComponent overlay) {
    this.layeredPane.moveToFront(overlay);
  }

  protected LayerMapOverlay newLayerOverlay() {
    return new LayerRendererOverlay(this, this.project);
  }

  protected void newStatusBar() {
    this.statusBarPanel = new JPanel(new BorderLayout());
    this.statusBarPanel.setPreferredSize(new Dimension(200, 30));
    add(this.statusBarPanel, BorderLayout.SOUTH);
    this.statusBarPanel.add(this.leftStatusBar, BorderLayout.WEST);
    this.statusBarPanel.add(this.rightStatusBar, BorderLayout.EAST);

    final JToggleButton showAreaBBOXButton = new JToggleButton(Icons.getIcon("area_bbox"));
    showAreaBBOXButton.setToolTipText("Show/Hide Max Extents");
    showAreaBBOXButton.addActionListener(event -> {
      final boolean selected = showAreaBBOXButton.isSelected();
      this.layerOverlay.setShowAreaBoundingBox(selected);
      this.project.setProperty("showAreaBoundingBox", selected);
    });
    showAreaBBOXButton.setBorderPainted(false);
    this.leftStatusBar.add(showAreaBBOXButton);

    addPointerLocation(false);
    addPointerLocation(true);
    final MapPointerElevation elevation = new MapPointerElevation(this);
    this.leftStatusBar.add(elevation);

    this.overlayActionLabel = new JLabel();
    this.overlayActionLabel.setBorder(
      BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),
        BorderFactory.createEmptyBorder(2, 3, 2, 3)));
    this.overlayActionLabel.setVisible(false);
    this.overlayActionLabel.setForeground(WebColors.Green);
    this.leftStatusBar.add(this.overlayActionLabel);

    this.messageLabel = new JLabel();
    this.messageLabel.setBorder(
      BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),
        BorderFactory.createEmptyBorder(2, 3, 2, 3)));
    this.messageLabel.setVisible(false);
    this.leftStatusBar.add(this.messageLabel);

    this.progressBar = new SwingWorkerProgressBar();
    this.rightStatusBar.add(this.progressBar);
  }

  protected void newToolBar() {
    add(this.toolBar, BorderLayout.NORTH);

    newToolBarZoomButtons();

    newToolBarUndoButtons();

    newToolBarLayerControls();
  }

  private void newToolBarLayerControls() {
    this.toolBar.addButtonTitleIcon("layers", "Refresh All Layers", "arrow_refresh", this::refresh);

    this.selectCoordinateSystem = new SelectMapCoordinateSystem(this);
    this.toolBar.addComponent("layers", this.selectCoordinateSystem);

    final LayerGroupListModel baseMapLayersModel = new LayerGroupListModel(this.baseMapLayers,
      true);
    this.baseMapLayerField = baseMapLayersModel.newBaseComboBox("baseMapLayer");
    this.baseMapLayerField.setMaximumSize(new Dimension(200, 22));
    ConsumerSelectedItemListener.addItemListener(this.baseMapLayerField, this::setBaseMapLayer);
    if (this.baseMapLayers.getLayerCount() > 0) {
      this.baseMapLayerField.setSelectedIndex(1);
    }
    this.baseMapLayerField.setToolTipText("Base Map");

    this.toolBar.addComponent("layers", this.baseMapLayerField);
    Property.addListener(this.baseMapOverlay, "layer", this);
    this.baseMapLayerField.setSelectedIndex(0);
    this.toolBar.addButtonTitleIcon("layers", "Refresh Base Map", "map_refresh",
      this.baseMapOverlay::refresh);
  }

  private void newToolBarUndoButtons() {
    final EnableCheck canUndo = new ObjectPropertyEnableCheck(this.undoManager, "canUndo");
    final EnableCheck canRedo = new ObjectPropertyEnableCheck(this.undoManager, "canRedo");

    this.toolBar.addButton("undo", "Undo", "arrow_undo", canUndo, this.undoManager::undo);
    this.toolBar.addButton("undo", "Redo", "arrow_redo", canRedo, this.undoManager::redo);
  }

  private void newToolBarZoomButtons() {
    this.toolBar.addButtonTitleIcon("zoom", "Zoom to World", "magnifier_zoom_world",
      this::zoomToWorld);

    this.toolBar.addButtonTitleIcon("zoom", "Zoom In", "magnifier_zoom_in", this::zoomIn);

    this.toolBar.addButtonTitleIcon("zoom", "Zoom Out", "magnifier_zoom_out", this::zoomOut);

    final JButton zoomPreviousButton = this.toolBar.addButtonTitleIcon("zoom", "Zoom Previous",
      "magnifier_zoom_left", this::zoomPrevious);
    zoomPreviousButton.setEnabled(false);
    Property.addListener(this, "zoomPreviousEnabled",
      new EnableComponentListener(zoomPreviousButton));

    final JButton zoomNextButton = this.toolBar.addButtonTitleIcon("zoom", "Zoom Next",
      "magnifier_zoom_right", this::zoomNext);
    zoomNextButton.setEnabled(false);
    Property.addListener(this, "zoomNextEnabled", new EnableComponentListener(zoomNextButton));

    final JButton zoomSelectedButton = this.toolBar.addButtonTitleIcon("zoom", "Zoom To Selected",
      "magnifier_zoom_selected", this::zoomToSelected);
    zoomSelectedButton.setEnabled(false);
    Property.addListener(this.project, "hasSelectedRecords",
      new EnableComponentListener(zoomSelectedButton));

    this.zoomBookmarkButton = this.toolBar.addButtonTitleIcon("zoom", "Zoom Bookmarks",
      "zoom_bookmark", this::showZoomBookmarkMenu);

    this.toolBar.addComponent("zoom", new SelectMapScale(this));
    this.toolBar.addComponent("zoom", new SelectMapUnitsPerPixel(this));
  }

  public void panToBoundingBox(BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    boundingBox = boundingBox.bboxToCs(geometryFactory);
    final Viewport2D viewport = getViewport();
    if (!RectangleUtil.isEmpty(boundingBox)) {
      final Point centre = boundingBox.getCentre();
      viewport.setCentre(centre);
    }
  }

  public void panToGeometry(final Geometry geometry) {
    if (geometry != null) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Geometry convertedGeometry = geometry.convertGeometry(geometryFactory);
      final BoundingBox boudingBox = convertedGeometry.getBoundingBox();
      panToBoundingBox(boudingBox);
    }
  }

  public void panToRecord(final Record record) {
    if (record != null) {
      final Geometry geometry = record.getGeometry();
      panToGeometry(geometry);
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    final String propertyName = event.getPropertyName();
    final Object oldValue = event.getOldValue();
    final Object newValue = event.getNewValue();
    if ("hasSelectedRecords".equals(propertyName)) {
      if (source instanceof AbstractRecordLayer) {
        final AbstractRecordLayer layer = (AbstractRecordLayer)source;
        synchronized (this.selectedRecordLayers) {
          if (Boolean.TRUE.equals(newValue)) {
            if (!this.selectedRecordLayers.contains(layer)) {
              this.selectedRecordLayers.add(layer);
            }
          } else {
            this.selectedRecordLayers.remove(layer);
          }
        }
      }
    } else if (source == this.project) {
      if ("viewBoundingBox".equals(propertyName)) {
        final BoundingBox boundingBox = (BoundingBox)newValue;
        setBoundingBox(boundingBox);
      } else if ("geometryFactory".equals(propertyName)) {
        final GeometryFactory oldGeometryFactory = (GeometryFactory)oldValue;
        final GeometryFactory newGeometryFactory = (GeometryFactory)newValue;
        setGeometryFactory(oldGeometryFactory, newGeometryFactory);
      }
    } else if (source == this.viewport) {
      if ("geometryFactory".equals(propertyName)) {
        final GeometryFactory oldGeometryFactory = (GeometryFactory)oldValue;
        final GeometryFactory newGeometryFactory = (GeometryFactory)newValue;
        setGeometryFactory(oldGeometryFactory, newGeometryFactory);
      } else if ("boundingBox".equals(propertyName)) {
        final BoundingBox boundingBox = this.viewport.getBoundingBox();
        setBoundingBox(boundingBox);
      } else if ("unitsPerPixel".equals(propertyName)) {
        repaint();
      }
    } else if (source == this.baseMapOverlay) {
      if ("layer".equals(propertyName)) {
        final Layer layer = (Layer)newValue;
        if (this.baseMapLayerField != null) {
          if (layer == null) {
            this.baseMapLayerField.setSelectedItem(0);
          } else {
            this.baseMapLayerField.setSelectedItem(layer);
          }
        }
      }
    } else if (source == this.baseMapLayers) {
      if ("layers".equals(propertyName)) {
        if (this.baseMapOverlay != null) {
          final Layer selectedLayer = this.baseMapOverlay.getLayer();
          final Layer newLayer = (Layer)newValue;
          if (selectedLayer != newLayer) {
            if (selectedLayer != null) {
              final LayerGroup selectedLayerParent = selectedLayer.getLayerGroup();
              if (selectedLayer.isDeleted()
                || selectedLayerParent != null && selectedLayerParent != this.baseMapLayers) {
                this.baseMapOverlay.setLayer(null);
              }
            }
            if (newLayer != null && newLayer.isVisible()) {
              this.baseMapOverlay.setLayer(newLayer);
              this.baseMapLayerField.setSelectedItem(newLayer);
              if (selectedLayer != null) {
                selectedLayer.setVisible(false);
              }
            }
          }
        }
      }
    } else if (source instanceof Layer) {
      final Layer layer = (Layer)source;
      if (layer.getParent() == this.baseMapLayers) {
        if ("visible".equals(propertyName)) {
          final boolean visible = layer.isVisible();
          if (visible) {
            this.baseMapLayerField.setSelectedItem(layer);
          } else if (!this.baseMapLayers.isHasVisibleLayer()) {
            this.baseMapLayerField.setSelectedIndex(0);
          }
        }
      }
    }
    repaint();
  }

  public void refresh() {
    final Project project = getProject();
    if (project != null) {
      project.refresh();
    }
  }

  public synchronized void setBaseMapLayer(final Layer layer) {
    if (layer == NullLayer.INSTANCE || this.baseMapLayers.containsLayer(layer)) {
      final Layer oldValue = getBaseMapLayer();
      this.baseMapOverlay.setLayer(layer);
      firePropertyChange("baseMapLayer", oldValue, layer);
    }
  }

  public synchronized void setBoundingBox(final BoundingBox boundingBox) {
    Invoke.later(() -> {
      if (this.settingBoundingBox.isFalse()) {
        try (
          BaseCloseable settingBoundingBox = this.settingBoundingBox.closeable(true)) {
          final BoundingBox oldBoundingBox = getBoundingBox();
          final double oldUnitsPerPixel = getUnitsPerPixel();

          final boolean zoomPreviousEnabled = isZoomPreviousEnabled();
          final boolean zoomNextEnabled = isZoomNextEnabled();
          final BoundingBox resizedBoundingBox = this.viewport.setBoundingBox(boundingBox);
          if (this.project != null) {
            this.project.setViewBoundingBox(resizedBoundingBox);

            // setScale(this.viewport.getScale());
            synchronized (this.zoomHistory) {
              if (this.updateZoomHistory.isTrue() && !this.viewport.isComponentResizing()) {
                BoundingBox currentBoundingBox = null;
                if (this.zoomHistoryIndex > -1) {
                  currentBoundingBox = this.zoomHistory.get(this.zoomHistoryIndex);
                  if (!currentBoundingBox.equals(resizedBoundingBox)) {
                    while (this.zoomHistory.size() > this.zoomHistoryIndex + 1) {
                      this.zoomHistory.removeLast();
                    }
                    for (int i = this.zoomHistory.size() - 1; i > this.zoomHistoryIndex; i++) {
                      this.zoomHistory.remove(i);
                    }
                    this.zoomHistory.add(resizedBoundingBox);
                    this.zoomHistoryIndex = this.zoomHistory.size() - 1;
                    if (this.zoomHistory.size() > 50) {
                      this.zoomHistory.removeFirst();

                      this.zoomHistoryIndex--;
                    }
                  }
                } else {
                  this.zoomHistory.add(resizedBoundingBox);
                  this.zoomHistoryIndex = 0;
                }
              }
            }
            firePropertyChange("unitsPerPixel", oldUnitsPerPixel, getUnitsPerPixel());
            firePropertyChange("boundingBox", oldBoundingBox, resizedBoundingBox);
            firePropertyChange("zoomPreviousEnabled", zoomPreviousEnabled, isZoomPreviousEnabled());
            firePropertyChange("zoomNextEnabled", zoomNextEnabled, isZoomNextEnabled());

            repaint();
          }
        }
      }
    });
  }

  private void setGeometryFactory(final GeometryFactory oldGeometryFactory,
    final GeometryFactory newGeometryFactory) {
    this.project.setGeometryFactory(newGeometryFactory);
    this.viewport.setGeometryFactory(newGeometryFactory);

    repaint();
    firePropertyChange("geometryFactory", oldGeometryFactory, newGeometryFactory);
  }

  public void setInitializing(final boolean initializing) {
    Invoke.later(() -> {
      if (initializing != this.initializing) {
        this.initializing = initializing;
        if (initializing) {
          this.busyLabel.setDelay(200);
          remove(this.layeredPanel);
          add(this.busyLabel, BorderLayout.CENTER);
        } else {
          remove(this.busyLabel);
          add(this.layeredPanel, BorderLayout.CENTER);
        }
      }
    });
  }

  public void setMapCursor(Cursor cursor) {
    if (cursor == null) {
      final String overlayAction = getOverlayAction();
      cursor = getOverlayActionCursor(overlayAction);
      if (cursor == null) {
        cursor = AbstractOverlay.DEFAULT_CURSOR;
      }
    }
    setViewportCursor(cursor);
    if (!this.overlayActionCursorStack.isEmpty()) {
      this.overlayActionCursorStack.set(0, cursor);
    }
  }

  public void setMapOverlayEnabled(final Class<? extends JComponent> overlayClass,
    final boolean enabled) {
    final JComponent component = getMapOverlay(overlayClass);
    if (component != null) {
      component.setEnabled(enabled);
    }
  }

  public void setMenuVisible(final boolean menuVisible) {
    this.menuVisible = menuVisible;
  }

  public void setMessage(final String overlayAction, final String message, final Color color) {
    if (isOverlayAction(overlayAction)) {
      this.messageStack.pop();
      this.messageStack.push(new Pair<>(message, color));
      this.messageLabel.setForeground(color);
      this.messageLabel.setText(message);
      this.messageLabel.setVisible(true);
    }
  }

  public boolean setOverlayAction(final String overlayAction) {
    final String oldAction = getOverlayAction();
    if (overlayAction == null) {
      firePropertyChange("overlayAction", oldAction, null);
      return false;
    } else if (DataType.equal(oldAction, overlayAction)) {
      return true;
    } else if (canOverrideOverlayAction(overlayAction, oldAction)) {
      final Cursor cursor = getOverlayActionCursor(overlayAction);
      this.overlayActionCursorStack.push(cursor);
      setViewportCursor(cursor);
      this.overlayActionStack.push(overlayAction);
      this.overlayActionLabel.setText(CaseConverter.toCapitalizedWords(overlayAction));
      this.overlayActionLabel.setVisible(true);
      this.messageStack.push(null);
      this.messageLabel.setVisible(false);
      firePropertyChange("overlayAction", oldAction, overlayAction);
      return true;
    } else {
      return false;
    }
  }

  public boolean setOverlayActionClearOthers(final String overlayAction) {
    final String oldAction = getOverlayAction();
    if (overlayAction == null) {
      firePropertyChange("overlayAction", oldAction, null);
      return false;
    } else if (DataType.equal(oldAction, overlayAction)) {
      return true;
    } else {
      clearOverlayActions();
      return setOverlayAction(overlayAction);
    }
  }

  public void setOverlayActionCursor(final String name, final Cursor cursor) {
    this.overlayActionCursors.put(name, cursor);
  }

  public synchronized void setScale(double scale) {
    if (!this.settingScale && this.viewport != null && Double.isFinite(scale) && scale > 0) {
      try {
        this.settingScale = true;
        if (!getGeometryFactory().isGeographic()) {
          scale = Doubles.makePrecise(10.0, scale);
        }
        if (scale >= 0.1) {
          if (scale != this.scale) {
            this.viewport.setScale(scale);
            this.scale = scale;
            repaint();
          }
        }
      } finally {
        this.settingScale = false;
      }
    }
  }

  public void setToolTipText(final int x, final int y, final CharSequence text) {
    Invoke.later(() -> {
      this.toolTipOverlay.setText(x, y, text);
    });
  }

  public void setToolTipText(final Point2D location, final CharSequence text) {
    Invoke.later(() -> {
      this.toolTipOverlay.setText(location, text);
    });
  }

  private void setViewportCursor(final Cursor cursor) {
    this.layeredPane.setCursor(cursor);
  }

  public void setVisibleOverlay(final JComponent overlay) {
    if (this.visibleOverlay == null) {
      this.visibleOverlay = overlay;
      for (final Component component : this.layeredPane.getComponents()) {
        if (component != overlay && component != this.mouseOverlay) {
          component.setVisible(false);
        }
      }
    }
  }

  private void setZoomHistoryIndex(int zoomHistoryIndex) {
    synchronized (this.zoomHistory) {
      try (
        BaseCloseable updateZoomHistory = this.updateZoomHistory.closeable(false)) {
        final boolean zoomPreviousEnabled = isZoomPreviousEnabled();
        final boolean zoomNextEnabled = isZoomNextEnabled();
        final int zoomHistorySize = this.zoomHistory.size();
        if (zoomHistoryIndex < 1) {
          zoomHistoryIndex = 0;
        } else if (zoomHistoryIndex >= zoomHistorySize) {
          zoomHistoryIndex = zoomHistorySize - 2;
        }
        this.zoomHistoryIndex = zoomHistoryIndex;
        final BoundingBox boundingBox = this.zoomHistory.get(zoomHistoryIndex);
        this.viewport.setBoundingBoxAndGeometryFactory(boundingBox);

        this.project.setViewBoundingBoxAndGeometryFactory(boundingBox);
        firePropertyChange("zoomPreviousEnabled", zoomPreviousEnabled, isZoomPreviousEnabled());
        firePropertyChange("zoomNextEnabled", zoomNextEnabled, isZoomNextEnabled());
      }
    }
  }

  public void showZoomBookmarkMenu() {
    final BaseJPopupMenu menu = new BaseJPopupMenu();

    menu.addMenuItem("Add Bookmark", "add", this::addZoomBookmark);
    menu.addSeparator();
    final Project project = getProject();
    for (final Entry<String, BoundingBox> entry : project.getZoomBookmarks().entrySet()) {
      final String name = entry.getKey();
      final BoundingBox boundingBox = entry.getValue();
      menu.addMenuItem("Zoom to " + name, "magnifier", () -> zoomToBoundingBox(boundingBox));
    }
    menu.showMenu(this.zoomBookmarkButton, 0, 20);
  }

  public void toggleMode(final String mode) {
    if (isOverlayAction(mode)) {
      clearOverlayAction(mode);
    } else {
      setOverlayAction(mode);
    }
  }

  public void zoom(final Point mapPoint, final int steps) {
    long newScale;
    if (steps != 0) {
      double newUnitsPerPixel;
      if (this.viewport.isZoomByUnitsPerPixel()) {
        final double oldUnitsPerPixel = getUnitsPerPixel();
        if (steps < 0) {
          newUnitsPerPixel = this.viewport.getZoomInUnitsPerPixel(oldUnitsPerPixel, -steps);
        } else {
          newUnitsPerPixel = this.viewport.getZoomOutUnitsPerPixel(oldUnitsPerPixel, steps);
        }
      } else {
        final double oldScale = getScale();
        if (steps < 0) {
          newScale = this.viewport.getZoomInScale(oldScale, -steps);
        } else {
          newScale = this.viewport.getZoomOutScale(oldScale, steps);
        }
        if (newScale > 0 && Math.abs(newScale - oldScale) > 0.0001) {
          setScale(newScale);
          return;
        } else {
          newUnitsPerPixel = this.viewport.getUnitsPerPixel(newScale);
        }
      }
      final Viewport2D viewport = getViewport();
      final BoundingBox boundingBox = getBoundingBox();
      final double width = boundingBox.getWidth();
      final double height = boundingBox.getHeight();

      final double viewWidthPixels = viewport.getViewWidthPixels();
      final double newWidth = viewWidthPixels * newUnitsPerPixel;

      final double viewHeightPixels = viewport.getViewHeightPixels();
      final double newHeight = viewHeightPixels * newUnitsPerPixel;

      final double x = mapPoint.getX();
      final double x1 = boundingBox.getMinX();
      final double deltaX = x - x1;
      final double percentX = deltaX / width;
      final double newDeltaX = newWidth * percentX;
      final double newX1 = x - newDeltaX;

      final double y = mapPoint.getY();
      final double y1 = boundingBox.getMinY();
      final double deltaY = y - y1;
      final double percentY = deltaY / height;
      final double newDeltaY = newHeight * percentY;
      final double newY1 = y - newDeltaY;

      final GeometryFactory newGeometryFactory = boundingBox.getGeometryFactory();
      final BoundingBox newBoundingBox = newGeometryFactory.newBoundingBox(newX1, newY1,
        newX1 + newWidth, newY1 + newHeight);
      setBoundingBox(newBoundingBox);
    }
  }

  public void zoomIn() {
    this.viewport.zoomIn();
  }

  public void zoomNext() {
    setZoomHistoryIndex(this.zoomHistoryIndex + 1);
  }

  public void zoomOut() {
    this.viewport.zoomOut();
  }

  public void zoomPrevious() {
    setZoomHistoryIndex(this.zoomHistoryIndex - 1);
  }

  /**
   * Zoom to the bounding box with a 5% padding on each side
   *
   * @param boundingBox
   */
  public void zoomToBoundingBox(BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    boundingBox = boundingBox //
      .bboxEditor() //
      .setGeometryFactory(geometryFactory) //
      .expandPercent(0.1) //
      .newBoundingBox() //
    ;
    setBoundingBox(boundingBox);
  }

  public void zoomToGeometry(final Geometry geometry) {
    if (geometry != null) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      final Geometry convertedGeometry = geometry.convertGeometry(geometryFactory);
      final BoundingBox boudingBox = convertedGeometry.getBoundingBox();
      zoomToBoundingBox(boudingBox);
    }
  }

  public void zoomToLayer(final Layer layer) {
    if (layer != null && layer.isExists() && layer.isVisible()) {
      final BoundingBox boundingBox = layer.getBoundingBox(true);
      zoomToBoundingBox(boundingBox);
    }
  }

  public void zoomToRecord(final Record record) {
    if (record != null) {
      final Geometry geometry = record.getGeometry();
      zoomToGeometry(geometry);
    }
  }

  public void zoomToSelected() {
    zoomToSelected(this.project);
  }

  public void zoomToSelected(final Layer layer) {
    final BoundingBox boundingBox = layer.getSelectedBoundingBox();
    if (!boundingBox.isEmpty()) {
      zoomToBoundingBox(boundingBox);
    }
  }

  public void zoomToWorld() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final BoundingBox boundingBox = geometryFactory.getAreaBoundingBox();
    setBoundingBox(boundingBox);
  }
}
