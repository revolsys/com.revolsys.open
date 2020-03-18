package com.revolsys.swing.map.layer.elevation.gridded;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jeometry.common.logging.Logs;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReader;
import com.revolsys.elevation.gridded.GriddedElevationModelReaderFactory;
import com.revolsys.elevation.gridded.GriddedElevationModelWriterFactory;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.file.Paths;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.Borders;
import com.revolsys.swing.RsSwingServiceInitializer;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.io.SwingIo;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.elevation.ElevationModelLayer;
import com.revolsys.swing.map.layer.elevation.gridded.renderer.AbstractGriddedElevationModelLayerRenderer;
import com.revolsys.swing.map.layer.elevation.gridded.renderer.MultipleGriddedElevationModelLayerRenderer;
import com.revolsys.swing.map.layer.elevation.gridded.renderer.RasterizerGriddedElevationModelLayerRenderer;
import com.revolsys.swing.map.layer.elevation.gridded.renderer.TiledMultipleGriddedElevationModelLayerRenderer;
import com.revolsys.swing.map.layer.record.style.panel.LayerStylePanel;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.TreeNodes;
import com.revolsys.swing.tree.node.file.PathTreeNode;
import com.revolsys.util.Property;

public class GriddedElevationModelLayer extends AbstractLayer implements ElevationModelLayer {
  public static final String J_TYPE = "griddedElevationModelLayer";

  static {
    final MenuFactory menu = MenuFactory.getMenu(GriddedElevationModelLayer.class);
    menu.addGroup(0, "table");
    menu.addGroup(2, "edit");

    final Predicate<GriddedElevationModelLayer> notReadOnly = ((Predicate<GriddedElevationModelLayer>)GriddedElevationModelLayer::isReadOnly)
      .negate();
    final Predicate<GriddedElevationModelLayer> editable = GriddedElevationModelLayer::isEditable;

    menu.<GriddedElevationModelLayer> addCheckboxMenuItem("edit", "Editable", "pencil", notReadOnly,
      GriddedElevationModelLayer::toggleEditable, editable, true);

    menu.<GriddedElevationModelLayer> addMenuItem("edit", "Save As...", "disk",
      GriddedElevationModelLayer::saveAs, true);

    menu.<GriddedElevationModelLayer> addMenuItem("refresh", "Reload from File", "page:refresh",
      GriddedElevationModelLayer::revertDo, true);
  }

  private static void actionExport(final PathTreeNode node) {
    final Path path = node.getPath();
    SwingIo.exportToFile("Gridded Elevation Model", "com.revolsys.swing.io.gridded_dem.export",
      GriddedElevationModelWriterFactory.class, "asc", path, targetFile -> {
        try {
          final GriddedElevationModel elevationModel = GriddedElevationModel
            .newGriddedElevationModel(path);
          elevationModel.writeGriddedElevationModel(targetFile);
        } catch (final Exception e) {
          Logs.error(RsSwingServiceInitializer.class,
            "Error exporting gridded elevation:\n" + path + "\n" + targetFile, e);
        }
      });
  }

  private static void actionZoomTo(final PathTreeNode node) {
    final Path file = node.getPath();
    final String baseName = Paths.getBaseName(file);
    Invoke.background("Zoom to Gridded Elevation Model: " + baseName, () -> {
      try (
        GriddedElevationModelReader reader = GriddedElevationModelReader
          .newGriddedElevationModelReader(file)) {
        MapPanel.zoomToBoundingBox(baseName, reader);
      }
    });
  }

  public static void factoryInit() {
    // Renderers
    MapObjectFactoryRegistry.newFactory("rasterizerGriddedElevationModelLayerRenderer",
      RasterizerGriddedElevationModelLayerRenderer::new);
    MapObjectFactoryRegistry.newFactory("multipleGriddedElevationModelLayerRenderer",
      MultipleGriddedElevationModelLayerRenderer::new);
    MapObjectFactoryRegistry.newFactory("tiledMultipleGriddedElevationModelLayerRenderer",
      TiledMultipleGriddedElevationModelLayerRenderer::new);

    // Layers
    MapObjectFactoryRegistry.newFactory("griddedElevationModelLayer",
      "Gridded Elevation Model Layer", GriddedElevationModelLayer::new);

    MapObjectFactoryRegistry.newFactory("tiledGriddedElevationModelLayer",
      "Tiled Gridded Elevation Model Layer", TiledGriddedElevationModelLayer::new);

    // Menus
    final EnableCheck enableCheck = RsSwingServiceInitializer
      .enableCheck(GriddedElevationModelReaderFactory.class);
    menuItemPathAddLayer("gridded_dem", "Add Gridded Elevation Model Layer", "gridded_dem",
      GriddedElevationModelReaderFactory.class);

    TreeNodes
      .addMenuItem(PathTreeNode.MENU, "gridded_dem", "Export Gridded Elevation Model",
        (final PathTreeNode node) -> actionExport(node)) //
      .setVisibleCheck(enableCheck) //
      .setIconName("gridded_dem", "save");

    TreeNodes
      .addMenuItem(PathTreeNode.MENU, "gridded_dem", "Zoom to Gridded Elevation Model",
        (final PathTreeNode node) -> actionZoomTo(node))
      .setVisibleCheck(enableCheck) //
      .setIconName("gridded_dem", "magnifier");
  }

  private GriddedElevationModel elevationModel;

  private Resource resource;

  private String url;

  public GriddedElevationModelLayer(final Map<String, ? extends Object> properties) {
    super(J_TYPE);
    setProperties(properties);
    setSelectSupported(false);
    setQuerySupported(false);
    setReadOnly(true);
    if (getRenderer() == null) {
      final MultipleGriddedElevationModelLayerRenderer renderer = new MultipleGriddedElevationModelLayerRenderer(
        this);
      setRenderer(renderer);
    }
    setIcon("gridded_dem");
  }

  @Override
  public BoundingBox getBoundingBox(final boolean visibleLayersOnly) {
    if (isExists() && (isVisible() || !visibleLayersOnly)) {
      return getBoundingBox();
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.bboxEmpty();
    }
  }

  @Override
  public double getElevation(final double x, final double y) {
    if (this.elevationModel == null) {
      return Double.NaN;
    } else {
      return this.elevationModel.getValue(x, y);
    }
  }

  public GriddedElevationModel getElevationModel() {
    return this.elevationModel;
  }

  @Override
  protected boolean initializeDo() {
    final String url = getProperty("url");
    if (Property.hasValue(url)) {
      this.url = url;
      this.resource = Resource.getResource(url);
      revertDo();
      return this.elevationModel != null;
    } else {
      Logs.error(this, "Layer definition does not contain a 'url' property");
      return false;
    }
  }

  @Override
  public boolean isVisible() {
    return super.isVisible() || isEditable();
  }

  @Override
  public TabbedValuePanel newPropertiesPanel() {
    final TabbedValuePanel propertiesPanel = super.newPropertiesPanel();
    newPropertiesPanelStyle(propertiesPanel);
    return propertiesPanel;
  }

  protected void newPropertiesPanelStyle(final TabbedValuePanel propertiesPanel) {
    if (getRenderer() != null) {
      final LayerStylePanel stylePanel = new LayerStylePanel(this);
      propertiesPanel.addTab("Style", "palette", stylePanel);
    }
  }

  @Override
  protected BasePanel newPropertiesTabGeneral(final TabbedValuePanel tabPanel) {
    final BasePanel generalPanel = super.newPropertiesTabGeneral(tabPanel);
    newPropertiesTabGeneralPanelMetaData(generalPanel);
    return generalPanel;
  }

  private ValueField newPropertiesTabGeneralPanelMetaData(final BasePanel parent) {
    final ValueField panel = new ValueField(this);
    Borders.titled(panel, "Grid");
    if (this.elevationModel != null && isExists()) {
      parent.add(panel);
      SwingUtil.addLabelledReadOnlyTextField(panel, "Grid Width",
        this.elevationModel.getGridWidth());
      SwingUtil.addLabelledReadOnlyTextField(panel, "Grid Height",
        this.elevationModel.getGridHeight());
      SwingUtil.addLabelledReadOnlyTextField(panel, "Grid Cell Width",
        this.elevationModel.getGridCellWidth());
      SwingUtil.addLabelledReadOnlyTextField(panel, "Grid Cell Height",
        this.elevationModel.getGridCellHeight());
      GroupLayouts.makeColumns(panel, 2, true);
    }
    return panel;
  }

  @Override
  protected ValueField newPropertiesTabGeneralPanelSource(final BasePanel parent) {
    final ValueField panel = super.newPropertiesTabGeneralPanelSource(parent);

    if (this.url.startsWith("file:")) {
      final String fileName = this.url.replaceFirst("file:(//)?", "");
      SwingUtil.addLabelledReadOnlyTextField(panel, "File", fileName);
    } else {
      SwingUtil.addLabelledReadOnlyTextField(panel, "URL", this.url);
    }
    final String fileExtension = FileUtil.getFileNameExtension(this.url);
    if (Property.hasValue(fileExtension)) {
      SwingUtil.addLabelledReadOnlyTextField(panel, "File Extension", fileExtension);
      final GriddedElevationModelReaderFactory factory = IoFactory
        .factoryByFileExtension(GriddedElevationModelReaderFactory.class, fileExtension);
      if (factory != null) {
        SwingUtil.addLabelledReadOnlyTextField(panel, "File Type", factory.getName());
      }
    }
    GroupLayouts.makeColumns(panel, 2, true);
    return panel;
  }

  @Override
  public BufferedGeoreferencedImage newRenderImage() {
    final int width = this.elevationModel.getGridWidth();
    final int height = this.elevationModel.getGridHeight();
    final BoundingBox boundingBox = getBoundingBox();
    final BufferedGeoreferencedImage image = new BufferedGeoreferencedImage(boundingBox, width,
      height);
    image.setRenderedImage(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
    return image;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    final String propertyName = event.getPropertyName();
    if ("hasChanges".equals(propertyName)) {
      final GriddedElevationModel image = getElevationModel();
      if (event.getSource() == image) {
        image.writeGriddedElevationModel();
      }
    }
  }

  @Override
  protected void refreshDo() {
    final AbstractGriddedElevationModelLayerRenderer renderer = getRenderer();
    renderer.refresh();
    redraw();
  }

  protected void revertDo() {
    if (this.resource != null) {
      GriddedElevationModel elevationModel = null;
      if (this.resource.exists()) {
        try {
          elevationModel = GriddedElevationModel.newGriddedElevationModel(this.resource);
          if (elevationModel == null) {
            Logs.error(GriddedElevationModelLayer.class,
              "Cannot load elevation model: " + this.url);
          }
        } catch (final RuntimeException e) {
          Logs.error(GriddedElevationModelLayer.class, "Unable to elevation model: " + this.url, e);
        }
      } else {
        Logs.error(GriddedElevationModelLayer.class, "Elevation model does not exist: " + this.url);
      }
      setElevationModel(elevationModel);
    } else {
      if (this.elevationModel != null) {
        this.elevationModel.cancelChanges();
      }
    }
    firePropertyChange("hasChanges", true, false);
    firePropertyChange("refresh", false, true);
  }

  public void saveAs() {
    final String baseName = this.resource.getBaseName();
    final Consumer<File> action = file -> this.elevationModel.writeGriddedElevationModel(file);
    SwingIo.exportToFile("Gridded Elevation Model", "com.revolsys.swing.io.gridded_dem.export",
      GriddedElevationModelWriterFactory.class, "asc", baseName, action);
  }

  protected void saveImageChanges() {
    if (this.elevationModel != null) {
      this.elevationModel.writeGriddedElevationModel();
    }
  }

  @Override
  public void setBoundingBox(final BoundingBox boundingBox) {
    super.setBoundingBox(boundingBox);
    if (this.elevationModel != null) {
      this.elevationModel.setBoundingBox(boundingBox);
    }
  }

  public void setElevationModel(final GriddedElevationModel elevationModel) {
    final GriddedElevationModel old = this.elevationModel;
    Property.removeListener(this.elevationModel, this);
    this.elevationModel = elevationModel;
    if (elevationModel == null) {
      setExists(false);
    } else {
      final GeometryFactory geometryFactory = elevationModel.getGeometryFactory();
      setGeometryFactoryPrompt(geometryFactory);
      final BoundingBox boundingBox = elevationModel.getBoundingBox();
      super.setBoundingBox(boundingBox);
      setExists(true);
      Property.addListener(elevationModel, this);
    }
    final AbstractGriddedElevationModelLayerRenderer renderer = getRenderer();
    if (renderer != null) {
      renderer.setElevationModel(elevationModel);
    }
    firePropertyChange("elevationModel", old, this.elevationModel);
  }

  @SuppressWarnings("unchecked")
  public void setStyle(Object style) {
    if (style instanceof Map) {
      final Map<String, Object> map = (Map<String, Object>)style;
      style = MapObjectFactory.toObject(map);
    }
    if (style instanceof MultipleGriddedElevationModelLayerRenderer) {
      final MultipleGriddedElevationModelLayerRenderer renderer = (MultipleGriddedElevationModelLayerRenderer)style;
      setRenderer(renderer);
    } else if (style instanceof RasterizerGriddedElevationModelLayerRenderer) {
      final MultipleGriddedElevationModelLayerRenderer renderer = new MultipleGriddedElevationModelLayerRenderer(
        this, (RasterizerGriddedElevationModelLayerRenderer)style);
      setRenderer(renderer);
    } else {
      Logs.error(this, "Cannot create renderer for: " + style);
    }
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    map.remove("querySupported");
    map.remove("selectSupported");
    map.remove("editable");
    map.remove("readOnly");
    addToMap(map, "url", this.url);
    return map;
  }

  @Override
  public void zoomToLayer() {
    final Project project = getProject();
    final GeometryFactory geometryFactory = project.getGeometryFactory();
    final BoundingBox layerBoundingBox = getBoundingBox();
    final BoundingBox boundingBox = layerBoundingBox.bboxEditor() //
      .setGeometryFactory(geometryFactory) //
      .expandPercent(0.1)//
      .clipToCoordinateSystem() //
      .newBoundingBox();

    project.setViewBoundingBox(boundingBox);
  }

}
