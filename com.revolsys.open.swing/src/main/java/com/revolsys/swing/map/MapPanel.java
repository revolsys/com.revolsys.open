package com.revolsys.swing.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.undo.UndoableEdit;

import org.jdesktop.swingx.JXStatusBar;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.listener.EnableComponentListener;
import com.revolsys.swing.listener.InvokeMethodSelectedItemListener;
import com.revolsys.swing.map.border.FullSizeLayoutManager;
import com.revolsys.swing.map.border.MapRulerBorder;
import com.revolsys.swing.map.component.MapPointerLocation;
import com.revolsys.swing.map.component.SelectMapCoordinateSystem;
import com.revolsys.swing.map.component.SelectMapScale;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.NullLayer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.list.LayerGroupListModel;
import com.revolsys.swing.map.listener.FileDropTargetListener;
import com.revolsys.swing.map.overlay.EditGeoReferencedImageOverlay;
import com.revolsys.swing.map.overlay.EditGeometryOverlay;
import com.revolsys.swing.map.overlay.LayerRendererOverlay;
import com.revolsys.swing.map.overlay.MouseOverlay;
import com.revolsys.swing.map.overlay.ZoomOverlay;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.swing.undo.UndoManager;
import com.vividsolutions.jts.geom.Geometry;

@SuppressWarnings("serial")
public class MapPanel extends JPanel implements PropertyChangeListener {

  public static final BoundingBox BC_ENVELOPE = new BoundingBox(
    GeometryFactory.getFactory(3857, 3, 1000, 1000), -15555252, 6174862,
    -12346993, 8584083);

  public static final String MAP_CONTROLS_WORKING_AREA = "mapControlsCWorkingArea";

  public static final String MAP_PANEL = "mapPanel";

  public static final String MAP_TABLE_WORKING_AREA = "mapTablesCWorkingArea";

  public static MapPanel get(final Layer layer) {
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

  final List<Double> scales = new ArrayList<Double>();

  private final LayerGroup baseMapLayers;

  private final LayerRendererOverlay baseMapOverlay;

  private final JLayeredPane layeredPane;

  private final LayerRendererOverlay layerOverlay;

  private MouseOverlay mouseOverlay;

  private int overlayIndex = 1;

  private Project project;

  private double scale = 0;

  private SelectMapScale selectMapScale;

  private final UndoManager undoManager = new UndoManager();

  private final JXStatusBar statusBar = new JXStatusBar();

  private final ToolBar toolBar = new ToolBar();

  private final Viewport2D viewport;

  private final LinkedList<BoundingBox> zoomHistory = new LinkedList<BoundingBox>();

  private int zoomHistoryIndex = -1;

  private final FileDropTargetListener fileDropListener;

  private boolean updateZoomHistory = true;

  public MapPanel() {
    this(new Project());
  }

  public MapPanel(final Project project) {
    super(new BorderLayout());
    this.project = project;
    this.baseMapLayers = project.addLayerGroup("Base Maps");
    project.setProperty(MAP_PANEL, this);
    layeredPane = new JLayeredPane();
    layeredPane.setOpaque(true);
    layeredPane.setBackground(Color.WHITE);
    layeredPane.setVisible(true);
    layeredPane.setLayout(new FullSizeLayoutManager());

    add(layeredPane, BorderLayout.CENTER);

    this.viewport = new ComponentViewport2D(project, layeredPane);
    viewport.addPropertyChangeListener(this);

    createScales();
    this.viewport.setScales(getScales());

    layeredPane.setBorder(new MapRulerBorder(viewport));

    baseMapOverlay = new LayerRendererOverlay(this);
    layeredPane.add(baseMapOverlay, new Integer(0));
    baseMapOverlay.addPropertyChangeListener("layer", this);

    layerOverlay = new LayerRendererOverlay(this, project);
    layeredPane.add(layerOverlay, new Integer(1));

    project.addPropertyChangeListener("viewBoundingBox",
      new PropertyChangeListener() {

        @Override
        public void propertyChange(final PropertyChangeEvent event) {
          final BoundingBox boundingBox = (BoundingBox)event.getNewValue();
          setBoundingBox(boundingBox);
        }
      });
    baseMapLayers.addPropertyChangeListener(this);
    project.addPropertyChangeListener(this);

    addMapOverlays();

    addToolBar();

    addStatusBar();

    zoomToWorld();

    fileDropListener = new FileDropTargetListener(this);
    undoManager.addKeyMap(this);
  }

  public void addBaseMap(final Layer layer) {
    if (layer != null) {
      baseMapLayers.add(layer);
    }
    if (baseMapLayers.size() == 1) {
      setBaseMapLayer(layer);
    }
  }

  private void addLayerControls() {
    final SelectMapCoordinateSystem selectCoordinateSystem = new SelectMapCoordinateSystem(
      this);
    toolBar.addComponent("layers", selectCoordinateSystem);

    final LayerGroupListModel baseMapLayersModel = new LayerGroupListModel(
      baseMapLayers, true);
    final ComboBox comboBox = new ComboBox(baseMapLayersModel);
    comboBox.setMaximumSize(new Dimension(200, 20));
    comboBox.addItemListener(new InvokeMethodSelectedItemListener(this,
      "setBaseMapLayer"));
    if (baseMapLayers.size() > 0) {
      comboBox.setSelectedIndex(1);
    }
    comboBox.setToolTipText("Select the base layerOverlay layer");
    toolBar.addComponent("layers", comboBox);
    baseMapOverlay.addPropertyChangeListener("layer",
      new PropertyChangeListener() {

        @Override
        public void propertyChange(final PropertyChangeEvent event) {
          final Layer layer = (Layer)event.getNewValue();
          if (layer != null) {
            comboBox.setSelectedItem(layer);
          }
        }
      });
  }

  public void addMapOverlay(final int zIndex, final JComponent overlay) {
    layeredPane.add(overlay, new Integer(zIndex));
    if (overlay instanceof PropertyChangeListener) {
      final PropertyChangeListener listener = (PropertyChangeListener)overlay;
      addPropertyChangeListener(listener);
      project.addPropertyChangeListener(listener);
      baseMapLayers.addPropertyChangeListener(listener);
    }
    overlay.addPropertyChangeListener(this);
  }

  public void addMapOverlay(final JComponent overlay) {
    final int zIndex = 1000 * overlayIndex++;
    addMapOverlay(zIndex, overlay);
  }

  protected void addMapOverlays() {
    new ZoomOverlay(this);
    new EditGeometryOverlay(this);
    this.mouseOverlay = new MouseOverlay(layeredPane);
    new EditGeoReferencedImageOverlay(this);
  }

  private void addPointerLocation(final boolean geographics) {
    final MapPointerLocation location = new MapPointerLocation(this,
      geographics);
    statusBar.add(location);
  }

  protected void addScale(final double metresPerPixel) {
    final double scale = viewport.getScaleForMetresPerPixel(metresPerPixel);
    scales.add(scale);
  }

  protected void addStatusBar() {
    add(statusBar, BorderLayout.SOUTH);

    addPointerLocation(false);
    addPointerLocation(true);
  }

  protected void addToolBar() {
    add(toolBar, BorderLayout.NORTH);

    addZoomButtons();

    addUndoButtons();

    addLayerControls();

  }

  public void addUndo(final UndoableEdit edit) {
    undoManager.addEdit(edit);
  }

  protected void addUndoButtons() {
    final EnableCheck canUndo = new ObjectPropertyEnableCheck(undoManager,
      "canUndo");
    final EnableCheck canRedo = new ObjectPropertyEnableCheck(undoManager,
      "canRedo");

    toolBar.addButton("undo", "Undo", "arrow_undo", canUndo, undoManager,
      "undo");
    toolBar.addButton("undo", "Redo", "arrow_redo", canRedo, undoManager,
      "redo");
  }

  private void addZoomButtons() {
    toolBar.addButtonTitleIcon("zoom", "Zoom to World", "magnifier_zoom_world",
      this, "zoomToWorld");

    toolBar.addButtonTitleIcon("zoom", "Zoom to British Columbia", "zoom_bc",
      this, "setBoundingBox", MapPanel.BC_ENVELOPE);

    toolBar.addButtonTitleIcon("zoom", "Zoom In", "magnifier_zoom_in", this,
      "zoomIn");

    toolBar.addButtonTitleIcon("zoom", "Zoom Out", "magnifier_zoom_out", this,
      "zoomOut");

    final JButton zoomPreviousButton = toolBar.addButtonTitleIcon("zoom",
      "Zoom Previous", "magnifier_zoom_left", this, "zoomPrevious");
    zoomPreviousButton.setEnabled(false);
    addPropertyChangeListener("zoomPreviousEnabled",
      new EnableComponentListener(zoomPreviousButton));

    final JButton zoomNextButton = toolBar.addButtonTitleIcon("zoom",
      "Zoom Next", "magnifier_zoom_right", this, "zoomNext");
    zoomNextButton.setEnabled(false);
    addPropertyChangeListener("zoomNextEnabled", new EnableComponentListener(
      zoomNextButton));

    toolBar.addButtonTitleIcon("zoom", "Zoom To Selected",
      "magnifier_zoom_selected", this, "zoomToSelected");
    // TODO disable if none selected

    selectMapScale = new SelectMapScale(this);
    toolBar.addComponent("zoom", selectMapScale);
  }

  public void clearZoomHistory() {
    zoomHistory.clear();
    zoomHistoryIndex = -1;
  }

  public void createScales() {
    double multiplier = 0.001;
    for (int i = 0; i < 9; i++) {
      addScale(1 * multiplier);
      addScale(2 * multiplier);
      addScale(5 * multiplier);
      multiplier *= 10;
    }
    Collections.reverse(scales);
  }

  public void dispose() {
    layerOverlay.dispose();
  }

  @Override
  protected void finalize() throws Throwable {
    layerOverlay.dispose();
    super.finalize();
  }

  public Layer getBaseMapLayer() {
    return baseMapOverlay.getLayer();
  }

  public LayerGroup getBaseMapLayers() {
    return baseMapLayers;
  }

  public LayerRendererOverlay getBaseMapOverlay() {
    return baseMapOverlay;
  }

  public BoundingBox getBoundingBox() {
    return viewport.getBoundingBox();
  }

  public FileDropTargetListener getFileDropListener() {
    return fileDropListener;
  }

  public GeometryFactory getGeometryFactory() {
    return project.getGeometryFactory();
  }

  public LayerRendererOverlay getLayerOverlay() {
    return layerOverlay;
  }

  public Point getMapMousePosition() {
    final Point mousePosition = layeredPane.getMousePosition();
    mousePosition.x -= layeredPane.getInsets().left;
    mousePosition.y -= layeredPane.getInsets().top;
    return mousePosition;
  }

  @SuppressWarnings("unchecked")
  public <T extends JComponent> T getMapOverlay(final Class<T> overlayClass) {
    for (final Component component : layeredPane.getComponents()) {
      if (overlayClass.isAssignableFrom(component.getClass())) {
        return (T)component;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public <T extends JComponent> List<T> getMapOverlays(
    final Class<T> overlayClass) {
    final List<T> overlays = new ArrayList<T>();
    for (final Component component : layeredPane.getComponents()) {
      if (overlayClass.isAssignableFrom(component.getClass())) {
        overlays.add((T)component);
      }
    }
    return overlays;
  }

  public MouseOverlay getMouseOverlay() {
    return mouseOverlay;
  }

  public LayerGroup getProject() {
    return project;
  }

  public double getScale() {
    return scale;
  }

  public List<Double> getScales() {
    return scales;
  }

  public JXStatusBar getStatusBar() {
    return statusBar;
  }

  public ToolBar getToolBar() {
    return toolBar;
  }

  public UndoManager getUndoManager() {
    return undoManager;
  }

  public Viewport2D getViewport() {
    return viewport;
  }

  public double getZoomInScale(final double scale) {
    final long scaleCeil = (long)Math.floor(scale);
    for (final double nextScale : scales) {
      final long newScale = (long)Math.floor(nextScale);
      if (newScale < scaleCeil) {
        return nextScale;
      }
    }
    return scales.get(scales.size() - 1);
  }

  public double getZoomOutScale(final double scale) {
    final long scaleCeil = (long)Math.floor(scale);
    final List<Double> scales = new ArrayList<Double>(this.scales);
    Collections.reverse(scales);
    for (final double nextScale : scales) {
      final long newScale = (long)Math.floor(nextScale);
      if (newScale > scaleCeil) {
        return nextScale;
      }
    }
    return scales.get(0);
  }

  public boolean isZoomNextEnabled() {
    return zoomHistoryIndex < zoomHistory.size() - 1;
  }

  public boolean isZoomPreviousEnabled() {
    return zoomHistoryIndex > 0;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    final String propertyName = event.getPropertyName();
    if (source == viewport) {
      if ("scale".equals(propertyName)) {
        final double scale = viewport.getScale();
        setScale(scale);
      }
    } else if (source == baseMapLayers) {
      if ("layers".equals(propertyName)) {
        if (baseMapOverlay != null && baseMapOverlay.getLayer() == null) {
          final Layer layer = (Layer)event.getNewValue();
          if (layer != null) {
            baseMapOverlay.setLayer(layer);
          }
        }
      }
    }
    repaint();
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    final LayerGroup project = this.project;
    this.project = null;
    project.delete();
  }

  public synchronized void setBaseMapLayer(final Layer layer) {
    if (layer == NullLayer.INSTANCE || baseMapLayers.contains(layer)) {
      final Layer oldValue = getBaseMapLayer();
      baseMapOverlay.setLayer(layer);
      firePropertyChange("baseMapLayer", oldValue, layer);
    }
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    final BoundingBox oldBoundingBox = getBoundingBox();

    final boolean zoomPreviousEnabled = isZoomPreviousEnabled();
    final boolean zoomNextEnabled = isZoomNextEnabled();
    final BoundingBox resizedBoundingBox = viewport.setBoundingBox(boundingBox);
    project.setViewBoundingBox(resizedBoundingBox);
    setScale(viewport.getScale());
    synchronized (zoomHistory) {
      if (updateZoomHistory) {
        BoundingBox currentBoundingBox = null;
        if (zoomHistoryIndex > -1) {
          currentBoundingBox = zoomHistory.get(zoomHistoryIndex);
          if (!currentBoundingBox.equals(resizedBoundingBox)) {
            while (zoomHistory.size() > zoomHistoryIndex + 1) {
              zoomHistory.removeLast();
            }
            for (int i = zoomHistory.size() - 1; i > zoomHistoryIndex; i++) {
              zoomHistory.remove(i);
            }
            zoomHistory.add(resizedBoundingBox);
            zoomHistoryIndex = zoomHistory.size() - 1;
            if (zoomHistory.size() > 50) {
              zoomHistory.removeFirst();

              zoomHistoryIndex--;
            }
          }
        } else {
          zoomHistory.add(resizedBoundingBox);
          zoomHistoryIndex = 0;
        }
      }
    }
    firePropertyChange("boundingBox", oldBoundingBox, resizedBoundingBox);
    firePropertyChange("zoomPreviousEnabled", zoomPreviousEnabled,
      isZoomPreviousEnabled());
    firePropertyChange("zoomNextEnabled", zoomNextEnabled, isZoomNextEnabled());

    repaint();
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    final GeometryFactory oldValue = getGeometryFactory();
    if (geometryFactory != oldValue) {
      project.setGeometryFactory(geometryFactory);
      firePropertyChange("geometryFactory", oldValue, geometryFactory);
      repaint();
    }
  }

  public void setMapOverlayEnabled(
    final Class<? extends JComponent> overlayClass, final boolean enabled) {
    final JComponent component = getMapOverlay(overlayClass);
    if (component != null) {
      component.setEnabled(enabled);
    }
  }

  public void setScale(final double scale) {
    final double oldValue = this.scale;
    if (scale != oldValue) {
      viewport.setScale(scale);
      this.scale = scale;
      firePropertyChange("scale", oldValue, scale);
      repaint();
    }
  }

  private void setZoomHistoryIndex(int zoomHistoryIndex) {
    synchronized (zoomHistory) {
      updateZoomHistory = false;
      try {
        final boolean zoomPreviousEnabled = isZoomPreviousEnabled();
        final boolean zoomNextEnabled = isZoomNextEnabled();
        final int zoomHistorySize = zoomHistory.size();
        if (zoomHistoryIndex < 1) {
          zoomHistoryIndex = 0;
        } else if (zoomHistoryIndex >= zoomHistorySize) {
          zoomHistoryIndex = zoomHistorySize - 2;
        }
        this.zoomHistoryIndex = zoomHistoryIndex;
        final BoundingBox boundingBox = zoomHistory.get(zoomHistoryIndex);
        viewport.setBoundingBox(boundingBox);

        project.setViewBoundingBox(boundingBox);
        firePropertyChange("zoomPreviousEnabled", zoomPreviousEnabled,
          isZoomPreviousEnabled());
        firePropertyChange("zoomNextEnabled", zoomNextEnabled,
          isZoomNextEnabled());
      } finally {
        updateZoomHistory = true;
      }
    }
  }

  public void zoom(final com.vividsolutions.jts.geom.Point mapPoint,
    final int steps) {
    final BoundingBox extent = getBoundingBox();
    double factor = steps * 2;
    if (factor < 0) {
      factor = 1 / -factor;
    }

    final double x = mapPoint.getX();
    final double x1 = extent.getMinX();
    final double width = extent.getWidth();
    final double newWidth = width * factor;
    final double deltaX = x - x1;
    final double percentX = deltaX / width;
    final double newDeltaX = newWidth * percentX;
    final double newX1 = x - newDeltaX;

    final double y = mapPoint.getY();
    final double y1 = extent.getMinY();
    final double height = extent.getHeight();
    final double newHeight = height * factor;
    final double deltaY = y - y1;
    final double percentY = deltaY / height;
    final double newDeltaY = newHeight * percentY;
    final double newY1 = y - newDeltaY;

    final GeometryFactory newGeometryFactory = extent.getGeometryFactory();
    final BoundingBox newBoundingBox = new BoundingBox(newGeometryFactory,
      newX1, newY1, newX1 + newWidth, newY1 + newHeight);
    setBoundingBox(newBoundingBox);
  }

  public void zoomIn() {
    final double scale = getScale();
    final double newScale = getZoomInScale(scale);
    setScale(newScale);
  }

  public void zoomNext() {
    setZoomHistoryIndex(zoomHistoryIndex + 1);
  }

  public void zoomOut() {
    final double scale = getScale();
    final double newScale = getZoomOutScale(scale);
    setScale(newScale);
  }

  public void zoomPrevious() {
    setZoomHistoryIndex(zoomHistoryIndex - 1);
  }

  /**
   * Zoom to the bounding box with a 5% padding on each side
   * 
   * @param boundingBox
   */
  public void zoomTo(BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    boundingBox = boundingBox.convert(geometryFactory).expandPercent(0.1);
    setBoundingBox(boundingBox);
  }

  public void zoomTo(final DataObject object) {
    if (object != null) {
      final Geometry geometry = object.getGeometryValue();
      zoomTo(geometry);
    }
  }

  public void zoomTo(final Geometry geometry) {
    if (geometry != null) {
      final Geometry convertedGeometry = getGeometryFactory().copy(geometry);
      final BoundingBox boudingBox = BoundingBox.getBoundingBox(convertedGeometry);
      zoomTo(boudingBox);
    }
  }

  public void zoomTo(final Layer layer) {
    if (layer != null && layer.isVisible()) {
      final BoundingBox boundingBox = layer.getBoundingBox(true);
      zoomTo(boundingBox);
    }
  }

  public void zoomToSelected() {
    final BoundingBox boundingBox = project.getSelectedBoundingBox();
    if (!boundingBox.isNull()) {
      zoomTo(boundingBox);
    }
  }

  public void zoomToWorld() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    final BoundingBox boundingBox = coordinateSystem.getAreaBoundingBox();
    setBoundingBox(boundingBox);
  }

}
