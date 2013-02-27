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

import org.slf4j.LoggerFactory;

import com.revolsys.gis.bing.BingClient.ImagerySet;
import com.revolsys.gis.cs.BoundingBox;
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
import com.revolsys.swing.map.layer.bing.BingLayer;
import com.revolsys.swing.map.list.LayerGroupListModel;
import com.revolsys.swing.map.overlay.EditGeometryOverlay;
import com.revolsys.swing.map.overlay.LayerRendererOverlay;
import com.revolsys.swing.map.overlay.MouseOverlay;
import com.revolsys.swing.map.overlay.SelectedOverlay;
import com.revolsys.swing.map.overlay.ZoomOverlay;
import com.revolsys.swing.toolbar.ToolBar;
import com.vividsolutions.jts.geom.Geometry;

@SuppressWarnings("serial")
public class MapPanel extends JPanel implements PropertyChangeListener {

  public static final BoundingBox BC_ENVELOPE = //
  new BoundingBox(GeometryFactory.getFactory(3857, 3, 1000, 1000), -15555252,
    6174862, -12346993, 8584083);

  // Coordinates for oliver area below, used for debugging
  // new Envelope(-13315415, 6300724, -13309149, 6305430);

  private final LayerGroup baseMapLayers = new LayerGroup("Base Maps");

  private final String bingMapsKey = "ArIUlgTPb9o1ZxL_dqrMpjv3FYzgBdgVKua-8czp3OVpMjGjOIzwoZevIk_gSk4i";

  private final LayerRendererOverlay map;

  private final LayerRendererOverlay baseMapOverlay;

  private final Project project = new Project();

  private final JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

  private final ToolBar toolBar;

  private final LinkedList<BoundingBox> zoomHistory = new LinkedList<BoundingBox>();

  private final LinkedList<BoundingBox> initialExtents = new LinkedList<BoundingBox>();

  private int zoomHistoryIndex = -1;

  private double scale = 0;

  // private final GeometryEditOverlay geometryEditOverlay;

  private SelectMapScale selectMapScale;

  private final Viewport2D viewport;

  private boolean initialized;

  private final JLayeredPane layeredPane;

  private MouseOverlay mouseOverlay;

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if (event.getSource() == viewport) {
      if ("scale".equals(event.getPropertyName())) {
        double scale = viewport.getScale();
        setScale(scale);
      }
    }

  }

  public MapPanel() {
    super(new BorderLayout());
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

    baseMapOverlay = new LayerRendererOverlay(viewport);
    layeredPane.add(baseMapOverlay, new Integer(0));

    map = new LayerRendererOverlay(viewport, project);
    layeredPane.add(map, new Integer(1));

    project.addPropertyChangeListener("viewBoundingBox",
      new PropertyChangeListener() {

        @Override
        public void propertyChange(final PropertyChangeEvent event) {
          final BoundingBox boundingBox = (BoundingBox)event.getNewValue();
          setBoundingBox(boundingBox);
        }
      });

    initBaseMapLayers();

    addStandardButtons();

    setBoundingBox(BC_ENVELOPE);

    addMapOverlays();

    addStatusBar();
  }

  protected void addStatusBar() {
    add(statusBar, BorderLayout.SOUTH);

    addPointerLocation("Albers", 3005, 1000.0);
    addPointerLocation("Lat/Lon", 4269, 10000000.0);
    statusBar.add(new MapScale(viewport));
  }

  protected void addMapOverlays() {
    new ZoomOverlay(this);
    new SelectedOverlay(this);
    new EditGeometryOverlay(this);
    this.mouseOverlay = new MouseOverlay(layeredPane);
    // new EditOverlay(this);
    // geometryEditOverlay = new GeometryEditOverlay(this);
  }

  @SuppressWarnings("unchecked")
  public <T extends JComponent> T getMapOverlay(Class<T> overlayClass) {
    for (Component component : layeredPane.getComponents()) {
      if (overlayClass.isAssignableFrom(component.getClass())) {
        return (T)component;
      }
    }
    return null;
  }

  public void setMapOverlayEnabled(Class<? extends JComponent> overlayClass,
    boolean enabled) {
    JComponent component = getMapOverlay(overlayClass);
    if (component != null) {
      component.setEnabled(enabled);
    }
  }

  public void addBaseMap(final Layer layer) {
    if (layer != null) {
      baseMapLayers.add(layer);
    }
    if (baseMapLayers.size() == 1) {
      setBaseMapLayer(layer);
    }
  }

  public BingLayer addBingBaseMap(final ImagerySet imagerySet) {
    try {
      final BingLayer layer = new BingLayer(bingMapsKey, imagerySet);
      addBaseMap(layer);
      return layer;
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error(
        "Unable to create BING layer " + imagerySet, e);
      return null;
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
  }

  public void addMapOverlay(final JComponent overlay) {
    int zIndex = 1000 * overlayIndex++;
    addMapOverlay(zIndex, overlay);
  }

  public void addMapOverlay(int zIndex, final JComponent overlay) {
    layeredPane.add(overlay, new Integer(zIndex));
  }

  private int overlayIndex = 1;

  private void addPointerLocation(final String title, final int srid,
    final double scaleFactor) {
    final MapPointerLocation location = new MapPointerLocation(viewport, title,
      GeometryFactory.getFactory(srid, 2, scaleFactor, scaleFactor));
    mouseOverlay.addMouseMotionListener(location);
    statusBar.add(location);
  }

  public void addArcGisRestBaseMap(final String name,
    final String url) {
//    try {
//      final TiledMapServiceLayer layer = new TiledMapServiceLayer(name, url);
//      addBaseMap(layer);
//      return layer;
//    } catch (final Throwable e) {
//      LoggerFactory.getLogger(getClass()).error(
//        "Unable to create Tile layer " + name + "=" + url, e);
//      return null;
//    }
  }

  private void addStandardButtons() {
    addZoomButtons();

    addLayerControls();
  }

  private void addZoomButtons() {
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

    selectMapScale = new SelectMapScale(this);
    toolBar.addComponent("zoom", selectMapScale);
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

  private void initBaseMapLayers() {
    addBingBaseMap(ImagerySet.Road);
    addBingBaseMap(ImagerySet.AerialWithLabels);
    addBingBaseMap(ImagerySet.Aerial);
    addArcGisRestBaseMap(
      "ESRI Open Street Map",
      "http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer");

    addArcGisRestBaseMap(
      "GeoBC Hillshade",
      "http://maps.gov.bc.ca/arcserver/rest/services/Province/web_mercator_cache/MapServer");

    addArcGisRestBaseMap(
      "ESRI World Topographic",
      "http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer");

    addArcGisRestBaseMap(
      "ESRI World Terrain",
      "http://services.arcgisonline.com/ArcGIS/rest/services/World_Terrain_Base/MapServer");
  }

  public boolean isInitialized() {
    synchronized (zoomHistory) {
      return initialized && initialExtents.isEmpty();
    }
  }

  // public void removeMapOverlay(final MapOverlay overlay) {
  // map.removeMapOverlay(overlay);
  // }

  public boolean isZoomNextEnabled() {
    return zoomHistoryIndex < zoomHistory.size() - 1;
  }

  public boolean isZoomPreviousEnabled() {
    return zoomHistoryIndex > 0;
  }

  // public void setEditingEnabled(final boolean enabled) {
  // geometryEditOverlay.setActive(enabled);
  // }

  public synchronized void setBaseMapLayer(final Layer layer) {
    if (layer == NullLayer.INSTANCE || baseMapLayers.contains(layer)) {
      final Layer oldValue = getBaseMapLayer();
      baseMapOverlay.setLayer(layer);
      firePropertyChange("baseMapLayer", oldValue, layer);
    }
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    viewport.setBoundingBox(boundingBox);
    setScale(viewport.getScale());
    repaint();
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

  // private void updateZoomHistory() {
  // synchronized (zoomHistory) {
  // final BoundingBox boundingBox = viewport.getBoundingBox();
  // if (this.extent == null || !this.extent.equals(boundingBox)) {
  // this.extent = boundingBox;
  // viewport.setBoundingBox(boundingBox);
  // final boolean zoomPreviousEnabled = isZoomPreviousEnabled();
  // final boolean zoomNextEnabled = isZoomNextEnabled();
  // while (zoomHistory.size() > zoomHistoryIndex + 1) {
  // zoomHistory.removeLast();
  // }
  // for (int i = zoomHistory.size() - 1; i > zoomHistoryIndex; i++) {
  // zoomHistory.remove(i);
  // }
  // zoomHistory.add(boundingBox);
  // zoomHistoryIndex = zoomHistory.size() - 1;
  // if (zoomHistory.size() > 50) {
  // zoomHistory.removeFirst();
  //
  // zoomHistoryIndex--;
  // }
  // firePropertyChange("zoomPreviousEnabled", zoomPreviousEnabled,
  // isZoomPreviousEnabled());
  // firePropertyChange("zoomNextEnabled", zoomNextEnabled,
  // isZoomNextEnabled());
  // }
  // }
  // }

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
    final BoundingBox boundingBox = getBoundingBox().clone();
    boundingBox.expandPercent(-0.5);
    setBoundingBox(boundingBox);
  }

  public void zoomNext() {
    setZoomHistoryIndex(zoomHistoryIndex + 1);
  }

  public void zoomOut() {
    final BoundingBox boundingBox = getBoundingBox().clone();
    boundingBox.expandPercent(1);
    setBoundingBox(boundingBox);
  }

  public void zoomPrevious() {
    setZoomHistoryIndex(zoomHistoryIndex - 1);
  }

  public void zoomTo(final DataObject object) {
    if (object != null) {
      final Geometry geometry = object.getGeometryValue();
      zoomTo(geometry);
    }
  }

  public void zoomTo(final Geometry geometry) {
    if (geometry != null) {
      Geometry convertedGeometry = getGeometryFactory().copy(geometry);
      final BoundingBox boundingBox = BoundingBox.getBoundingBox(
        convertedGeometry).clone();
      boundingBox.expandBy(boundingBox.getWidth() * 0.05,
        boundingBox.getHeight() * 0.05);
      setBoundingBox(boundingBox);
    }
  }

  public void zoomTo(final Layer layer) {
    if (layer != null && layer.isVisible()) {
      final BoundingBox boundingBox = layer.getBoundingBox(true)
        .clone()
        .convert(getGeometryFactory());
      final double width = boundingBox.getWidth();
      final double height = boundingBox.getHeight();
      boundingBox.expandBy(width * 0.05, height * 0.05);
      setBoundingBox(boundingBox);
    }
  }

}
