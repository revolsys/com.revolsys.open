package com.revolsys.swing.map.layer.elevation.gridded;

import java.beans.PropertyChangeEvent;
import java.util.Map;
import java.util.function.Predicate;

import javax.swing.JOptionPane;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.logging.Logs;
import com.revolsys.raster.GeoreferencedImageReadFactory;
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

    menu.deleteMenuItem("refresh", "Refresh");
  }

  public static void mapObjectFactoryInit() {
    MapObjectFactoryRegistry.newFactory(J_TYPE, "Gridded Elevation Model Layer",
      GriddedElevationModelLayer::new);
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
    final GriddedElevationModelLayerRenderer renderer = new GriddedElevationModelLayerRenderer(
      this);
    setRenderer(renderer);
    final int opacity = Maps.getInteger(properties, "opacity", 255);
    setOpacity(opacity);
    setIcon(Icons.getIcon("picture"));
  }

  public void cancelChanges() {
    if (this.elevationModel == null && this.resource != null) {
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
  }

  @Override
  public BoundingBox getBoundingBox() {
    final GriddedElevationModel elevationModel = getElevationModel();
    if (elevationModel == null) {
      return BoundingBox.EMPTY;
    } else {
      return elevationModel.getBoundingBox();
    }
  }

  @Override
  public BoundingBox getBoundingBox(final boolean visibleLayersOnly) {
    if (isExists() && (isVisible() || !visibleLayersOnly)) {
      return getBoundingBox();
    } else {
      return new BoundingBoxDoubleGf(getGeometryFactory());
    }
  }

  public double getElevation(final Point point) {
    return getElevationModel().getElevationDouble(point);
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
      cancelChanges();
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
      final GeoreferencedImageReadFactory factory = IoFactory
        .factoryByFileExtension(GeoreferencedImageReadFactory.class, fileNameExtension);
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

  @Override
  public void setEditable(final boolean editable) {
    Invoke.background("Set Editable " + this, () -> {
      synchronized (getSync()) {
        if (!editable) {
          firePropertyChange("preEditable", false, true);
          if (isHasChanges()) {
            final Integer result = Invoke.andWait(() -> {
              return JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(),
                "The layer has unsaved changes. Click Yes to save changes. Click No to discard changes. Click Cancel to continue editing.",
                "Save Changes", JOptionPane.YES_NO_CANCEL_OPTION);
            });

            if (result == JOptionPane.YES_OPTION) {
              if (!saveChanges()) {
                setVisible(true);
                return;
              }
            } else if (result == JOptionPane.NO_OPTION) {
              cancelChanges();
            } else {
              setVisible(true);
              // Don't allow state change if cancelled
              return;
            }

          }
        }
        super.setEditable(editable);
      }
    });
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
  public void setVisible(final boolean visible) {
    super.setVisible(visible);
    if (!visible) {
      setEditable(false);
    }
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    map.remove("querySupported");
    map.remove("selectSupported");
    map.remove("editable");
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
    BoundingBox boundingBox = layerBoundingBox;
    boundingBox = boundingBox.convert(geometryFactory).expandPercent(0.1).clipToCoordinateSystem();

    project.setViewBoundingBox(boundingBox);
  }
}
