package com.revolsys.swing.map.layer.elevation.gridded;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.JFileChooser;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.elevation.gridded.GriddedElevationModelReadFactory;
import com.revolsys.elevation.gridded.GriddedElevationModelWriterFactory;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.file.FileNameExtensionFilter;
import com.revolsys.logging.Logs;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.Menus;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.PreferencesUtil;
import com.revolsys.util.Property;

public class GriddedElevationModelLayer extends AbstractLayer {
  public static final String J_TYPE = "griddedElevationModelLayer";

  static {
    final MenuFactory menu = MenuFactory.getMenu(GriddedElevationModelLayer.class);
    menu.addGroup(0, "table");
    menu.addGroup(2, "edit");

    final Predicate<GriddedElevationModelLayer> notReadOnly = ((Predicate<GriddedElevationModelLayer>)GriddedElevationModelLayer::isReadOnly)
      .negate();
    final Predicate<GriddedElevationModelLayer> editable = GriddedElevationModelLayer::isEditable;

    Menus.<GriddedElevationModelLayer> addCheckboxMenuItem(menu, "edit", "Editable", "pencil",
      notReadOnly, GriddedElevationModelLayer::toggleEditable, editable, true);

    Menus.<GriddedElevationModelLayer> addMenuItem(menu, "edit", "Save As...", "disk",
      GriddedElevationModelLayer::saveAs, true);

    Menus.<GriddedElevationModelLayer> addMenuItem(menu, "refresh", "Reload from File",
      "page:refresh", GriddedElevationModelLayer::revertDo, true);
  }

  public static void saveAs(final String title, final Consumer<File> exportAction) {
    Invoke.later(() -> {
      final JFileChooser fileChooser = SwingUtil.newFileChooser("Save As",
        "com.revolsys.swing.map.layer.elevation.gridded.save", "directory");
      final String defaultFileExtension = PreferencesUtil.getUserString(
        "com.revolsys.swing.map.layer.elevation.gridded.save", "fileExtension", "demcb");

      final List<FileNameExtensionFilter> fileFilters = new ArrayList<>();
      for (final GriddedElevationModelWriterFactory factory : IoFactory
        .factories(GriddedElevationModelWriterFactory.class)) {
        factory.addFileFilters(fileFilters);
      }
      IoFactory.sortFilters(fileFilters);

      fileChooser.setAcceptAllFileFilterUsed(false);
      fileChooser.setSelectedFile(new File(fileChooser.getCurrentDirectory(), title));
      for (final FileNameExtensionFilter fileFilter : fileFilters) {
        fileChooser.addChoosableFileFilter(fileFilter);
        if (Arrays.asList(fileFilter.getExtensions()).contains(defaultFileExtension)) {
          fileChooser.setFileFilter(fileFilter);
        }
      }

      fileChooser.setMultiSelectionEnabled(false);
      final int returnVal = fileChooser.showSaveDialog(SwingUtil.getActiveWindow());
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        final FileNameExtensionFilter fileFilter = (FileNameExtensionFilter)fileChooser
          .getFileFilter();
        File file = fileChooser.getSelectedFile();
        if (file != null) {
          final String fileExtension = FileUtil.getFileNameExtension(file);
          final String expectedExtension = fileFilter.getExtensions().get(0);
          if (!fileExtension.equals(expectedExtension)) {
            file = FileUtil.getFileWithExtension(file, expectedExtension);
          }
          final File targetFile = file;
          PreferencesUtil.setUserString("com.revolsys.swing.map.layer.elevation.gridded.save",
            "fileExtension", expectedExtension);
          PreferencesUtil.setUserString("com.revolsys.swing.map.layer.elevation.gridded.save",
            "directory", file.getParent());
          final String description = "Save " + title + " as " + targetFile.getAbsolutePath();
          Invoke.background(description, () -> {
            exportAction.accept(targetFile);
          });
        }
      }
    });
  }

  private GriddedElevationModel elevationModel;

  private int opacity = 255;

  private Resource resource;

  private String url;

  public GriddedElevationModelLayer(final Map<String, ? extends Object> properties) {
    super(J_TYPE);
    setProperties(properties);
    setSelectSupported(false);
    setQuerySupported(false);
    setReadOnly(true);
    final GriddedElevationModelLayerRenderer renderer = new GriddedElevationModelLayerRenderer(
      this);
    setRenderer(renderer);
    final int opacity = Maps.getInteger(properties, "opacity", 255);
    setOpacity(opacity);
    setIcon(Icons.getIcon("picture"));
  }

  @Override
  public BoundingBox getBoundingBox() {
    final GriddedElevationModel elevationModel = getElevationModel();
    if (elevationModel == null) {
      return BoundingBox.empty();
    } else {
      return elevationModel.getBoundingBox();
    }
  }

  @Override
  public BoundingBox getBoundingBox(final boolean visibleLayersOnly) {
    if (isExists() && (isVisible() || !visibleLayersOnly)) {
      return getBoundingBox();
    } else {
      return getGeometryFactory().newBoundingBoxEmpty();
    }
  }

  public double getElevation(final Point point) {
    return getElevationModel().getElevation(point);
  }

  public GriddedElevationModel getElevationModel() {
    return this.elevationModel;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    if (this.elevationModel == null) {
      return getBoundingBox().getGeometryFactory();
    } else {
      return this.elevationModel.getGeometryFactory();
    }
  }

  public int getOpacity() {
    return this.opacity;
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

    return propertiesPanel;
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
    final String fileNameExtension = FileUtil.getFileNameExtension(this.url);
    if (Property.hasValue(fileNameExtension)) {
      SwingUtil.addLabelledReadOnlyTextField(panel, "File Extension", fileNameExtension);
      final GriddedElevationModelReadFactory factory = IoFactory
        .factoryByFileExtension(GriddedElevationModelReadFactory.class, fileNameExtension);
      if (factory != null) {
        SwingUtil.addLabelledReadOnlyTextField(panel, "File Type", factory.getName());
      }
    }
    GroupLayouts.makeColumns(panel, 2, true);
    return panel;
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
    final GriddedElevationModelLayerRenderer renderer = getRenderer();
    renderer.refresh();
  }

  protected void revertDo() {
    if (this.resource != null) {
      GriddedElevationModel elevationModel = null;
      final Resource resource = Resource.getResource(this.url);
      if (resource.exists()) {
        try {
          elevationModel = GriddedElevationModel.newGriddedElevationModel(resource);
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
    saveAs(this.resource.getBaseName(), (file) -> {
      this.elevationModel.writeGriddedElevationModel(file);
    });
  }

  protected void saveImageChanges() {
    if (this.elevationModel != null) {
      this.elevationModel.writeGriddedElevationModel();
    }
  }

  @Override
  public void setBoundingBox(final BoundingBox boundingBox) {
    if (this.elevationModel != null) {
      System.out.println(boundingBox);
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
      setExists(true);
      Property.addListener(elevationModel, this);
    }
    firePropertyChange("elevationModel", old, this.elevationModel);
  }

  public void setOpacity(int opacity) {
    final int oldValue = this.opacity;
    if (opacity < 0) {
      opacity = 0;
    } else if (opacity > 255) {
      opacity = 255;
    }
    this.opacity = opacity;
    firePropertyChange("opacity", oldValue, opacity);
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    map.remove("querySupported");
    map.remove("selectSupported");
    map.remove("editable");
    map.remove("readOnly");
    map.remove("showOriginalImage");
    map.remove("imageSettings");
    addToMap(map, "url", this.url);
    addToMap(map, "opacity", this.opacity, 1);
    return map;
  }

  @Override
  public void zoomToLayer() {
    final Project project = getProject();
    final GeometryFactory geometryFactory = project.getGeometryFactory();
    final BoundingBox layerBoundingBox = getBoundingBox();
    final BoundingBox boundingBox = layerBoundingBox.convert(geometryFactory)//
      .expandPercent(0.1)//
      .clipToCoordinateSystem();

    project.setViewBoundingBox(boundingBox);
  }
}
