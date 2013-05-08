package com.revolsys.swing.map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.listener.EnableComponentListener;
import com.revolsys.swing.listener.InvokeMethodSelectedItemListener;
import com.revolsys.swing.map.border.FullSizeLayoutManager;
import com.revolsys.swing.map.border.MapRulerBorder;
import com.revolsys.swing.map.component.MapPointerLocation;
import com.revolsys.swing.map.component.MapScale;
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
import com.revolsys.swing.map.overlay.SelectFeaturesOverlay;
import com.revolsys.swing.map.overlay.ZoomOverlay;
import com.revolsys.swing.toolbar.ToolBar;
import com.vividsolutions.jts.geom.Geometry;

@SuppressWarnings("serial")
public class MapPanel extends JPanel implements PropertyChangeListener {

  public static final BoundingBox BC_ENVELOPE = //
  new BoundingBox(GeometryFactory.getFactory(3857, 3, 1000, 1000), -15555252,
    6174862, -12346993, 8584083);

  public static final String MAP_CONTROLS_WORKING_AREA = "mapControlsCWorkingArea";

  public static final String MAP_PANEL = "mapPanel";

  public static final String MAP_TABLE_WORKING_AREA = "mapTablesCWorkingArea";

  public static MapPanel get(Layer layer) {
    if (layer == null) {
      return null;
    } else {
      Project project = layer.getProject();
      if (project == null) {
        return null;
      } else {
        return project.getProperty(MAP_PANEL);
      }
    }
  }

  private final LayerGroup baseMapLayers;

  private final LayerRendererOverlay baseMapOverlay;

  private final JLayeredPane layeredPane;

  private final LayerRendererOverlay map;

  private MouseOverlay mouseOverlay;

  private int overlayIndex = 1;

  private final Project project = new Project();

  private double scale = 0;

  private SelectMapScale selectMapScale;

  private final JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

  private final ToolBar toolBar;

  private final Viewport2D viewport;

  private final LinkedList<BoundingBox> zoomHistory = new LinkedList<BoundingBox>();

  private int zoomHistoryIndex = -1;

  private FileDropTargetListener fileDropListener;

  public MapPanel() {
    super(new BorderLayout());
    this.baseMapLayers = project.addLayerGroup("Base Maps");
    project.setProperty(MAP_PANEL, this);

    toolBar = new ToolBar();
    add(toolBar, BorderLayout.NORTH);

    layeredPane = new JLayeredPane();
    layeredPane.setOpaque(true);
    layeredPane.setBackground(Color.WHITE);
    layeredPane.setVisible(true);
    layeredPane.setLayout(new FullSizeLayoutManager());

    add(layeredPane, BorderLayout.CENTER);

    this.viewport = new ComponentViewport2D(project, layeredPane);
    viewport.addPropertyChangeListener(this);

    layeredPane.setBorder(new MapRulerBorder(viewport));

    baseMapOverlay = new LayerRendererOverlay(this);
    layeredPane.add(baseMapOverlay, new Integer(0));
    baseMapOverlay.addPropertyChangeListener("layer", this);

    map = new LayerRendererOverlay(this, project);
    layeredPane.add(map, new Integer(1));

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

    addStandardButtons();

    addMapOverlays();

    addStatusBar();

    zoomToWorld();

    fileDropListener = new FileDropTargetListener(this);
  }

  public FileDropTargetListener getFileDropListener() {
    return fileDropListener;
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
    final LayerGroupListModel baseMapLayersModel = new LayerGroupListModel(
      baseMapLayers, true);
    final JComboBox comboBox = new JComboBox(baseMapLayersModel);
    comboBox.setEditable(false);
    comboBox.setMaximumSize(new Dimension(200, 20));
    comboBox.addItemListener(new InvokeMethodSelectedItemListener(this,
      "setBaseMapLayer"));
    if (baseMapLayers.size() > 0) {
      comboBox.setSelectedIndex(1);
    }
    comboBox.setToolTipText("Select the base map layer");
    toolBar.addComponent("layers", comboBox);
    baseMapOverlay.addPropertyChangeListener("layer",
      new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent event) {
          Layer layer = (Layer)event.getNewValue();
          if (layer != null) {
            comboBox.setSelectedItem(layer);
          }
        }
      });
  }

  public void addMapOverlay(final int zIndex, final JComponent overlay) {
    layeredPane.add(overlay, new Integer(zIndex));
    if (overlay instanceof PropertyChangeListener) {
      PropertyChangeListener listener = (PropertyChangeListener)overlay;
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
    new SelectFeaturesOverlay(this);
    new EditGeometryOverlay(this);
    this.mouseOverlay = new MouseOverlay(layeredPane);
    new EditGeoReferencedImageOverlay(this);
  }

  private void addPointerLocation(final String title, final int srid,
    final double scaleFactor) {
    final MapPointerLocation location = new MapPointerLocation(viewport, title,
      GeometryFactory.getFactory(srid, 2, scaleFactor, scaleFactor));
    mouseOverlay.addMouseMotionListener(location);
    statusBar.add(location);
  }

  private void addStandardButtons() {
    addZoomButtons();

    addLayerControls();
  }

  protected void addStatusBar() {
    add(statusBar, BorderLayout.SOUTH);

    addPointerLocation("Albers", 3005, 1000.0);
    addPointerLocation("Lat/Lon", 4269, 10000000.0);
    statusBar.add(new MapScale(viewport));
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

  public void dispose() {
    map.dispose();
  }

  @Override
  protected void finalize() throws Throwable {
    map.dispose();
    super.finalize();
  }

  public Layer getBaseMapLayer() {
    return baseMapOverlay.getLayer();
  }

  public LayerGroup getBaseMapLayers() {
    return baseMapLayers;
  }

  public BoundingBox getBoundingBox() {
    return viewport.getBoundingBox();
  }

  public GeometryFactory getGeometryFactory() {
    return project.getGeometryFactory();
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

  public Project getProject() {
    return project;
  }

  public double getScale() {
    return scale;
  }

  public JPanel getStatusBar() {
    return statusBar;
  }

  public ToolBar getToolBar() {
    return toolBar;
  }

  public Viewport2D getViewport() {
    return viewport;
  }

  public boolean isZoomNextEnabled() {
    return zoomHistoryIndex < zoomHistory.size() - 1;
  }

  public boolean isZoomPreviousEnabled() {
    return zoomHistoryIndex > 0;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    Object source = event.getSource();
    if (source == viewport) {
      if ("scale".equals(event.getPropertyName())) {
        final double scale = viewport.getScale();
        setScale(scale);
      }
    } else if (source == baseMapLayers) {
      if (baseMapOverlay != null && baseMapOverlay.getLayer() == null) {
        final Layer layer = (Layer)event.getNewValue();
        if (layer != null) {
          baseMapOverlay.setLayer(layer);
        }
      }
    }
    repaint();
  }

  public synchronized void setBaseMapLayer(final Layer layer) {
    if (layer == NullLayer.INSTANCE || baseMapLayers.contains(layer)) {
      final Layer oldValue = getBaseMapLayer();
      baseMapOverlay.setLayer(layer);
      firePropertyChange("baseMapLayer", oldValue, layer);
    }
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    final boolean zoomPreviousEnabled = isZoomPreviousEnabled();
    final boolean zoomNextEnabled = isZoomNextEnabled();
    viewport.setBoundingBox(boundingBox);
    setScale(viewport.getScale());
    synchronized (zoomHistory) {
      BoundingBox currentBoundingBox = null;
      if (zoomHistoryIndex > -1) {
        currentBoundingBox = zoomHistory.get(zoomHistoryIndex);
        if (!currentBoundingBox.equals(boundingBox)) {
          while (zoomHistory.size() > zoomHistoryIndex + 1) {
            zoomHistory.removeLast();
          }
          for (int i = zoomHistory.size() - 1; i > zoomHistoryIndex; i++) {
            zoomHistory.remove(i);
          }
          zoomHistory.add(boundingBox);
          zoomHistoryIndex = zoomHistory.size() - 1;
          if (zoomHistory.size() > 50) {
            zoomHistory.removeFirst();

            zoomHistoryIndex--;
          }
        }
      } else {
        zoomHistory.add(boundingBox);
        zoomHistoryIndex = 0;
      }

    }
    firePropertyChange("zoomPreviousEnabled", zoomPreviousEnabled,
      isZoomPreviousEnabled());
    firePropertyChange("zoomNextEnabled", zoomNextEnabled, isZoomNextEnabled());

    repaint();
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
    BoundingBox boundingBox = getBoundingBox();
    boundingBox = boundingBox.expandPercent(-0.5);
    setBoundingBox(boundingBox);
  }

  public void zoomNext() {
    setZoomHistoryIndex(zoomHistoryIndex + 1);
  }

  public void zoomOut() {
    BoundingBox boundingBox = getBoundingBox();
    boundingBox = boundingBox.expandPercent(1);
    setBoundingBox(boundingBox);
  }

  public void zoomPrevious() {
    setZoomHistoryIndex(zoomHistoryIndex - 1);
  }

  /**
   * Zoom to the bounding box with a 5% padding on each side
   * 
   * @param boudingBox
   */
  public void zoomTo(BoundingBox boudingBox) {
    GeometryFactory geometryFactory = getGeometryFactory();
    final BoundingBox boundingBox = boudingBox.convert(geometryFactory)
      .expandPercent(0.1);
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
      BoundingBox boudingBox = BoundingBox.getBoundingBox(convertedGeometry);
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
    BoundingBox boundingBox = project.getSelectedBoundingBox();
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
