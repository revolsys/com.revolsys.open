package com.revolsys.swing.map.layer.elevation.tin;

import java.beans.PropertyChangeEvent;
import java.util.Map;
import java.util.function.Predicate;

import javax.swing.JOptionPane;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
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

public class TriangulatedIrregularNetworkLayer extends AbstractLayer {
  public static final String J_TYPE = "triangulatedIrregularNetworkLayer";
  static {
    final MenuFactory menu = MenuFactory.getMenu(TriangulatedIrregularNetworkLayer.class);
    menu.addGroup(0, "table");
    menu.addGroup(2, "edit");

    final Predicate<TriangulatedIrregularNetworkLayer> notReadOnly = ((Predicate<TriangulatedIrregularNetworkLayer>)TriangulatedIrregularNetworkLayer::isReadOnly)
      .negate();
    final Predicate<TriangulatedIrregularNetworkLayer> editable = TriangulatedIrregularNetworkLayer::isEditable;

    Menus.<TriangulatedIrregularNetworkLayer> addCheckboxMenuItem(menu, "edit", "Editable",
      "pencil", notReadOnly, TriangulatedIrregularNetworkLayer::toggleEditable, editable, true);

    menu.deleteMenuItem("refresh", "Refresh");
  }

  public static void mapObjectFactoryInit() {
    MapObjectFactoryRegistry.newFactory(J_TYPE, "Triangulated Irregular Network Layer",
      TriangulatedIrregularNetworkLayer::new);
  }

  private TriangulatedIrregularNetwork tin;

  private int opacity = 255;

  private Resource resource;

  private String url;

  public TriangulatedIrregularNetworkLayer(final Map<String, ? extends Object> properties) {
    super(J_TYPE);
    setProperties(properties);
    setSelectSupported(false);
    setQuerySupported(false);
    final TriangulatedIrregularNetworkLayerRenderer renderer = new TriangulatedIrregularNetworkLayerRenderer(
      this);
    setRenderer(renderer);
    final int opacity = Maps.getInteger(properties, "opacity", 255);
    setOpacity(opacity);
    setIcon(Icons.getIcon("picture"));
  }

  public void cancelChanges() {
    if (this.tin == null && this.resource != null) {
      TriangulatedIrregularNetwork tin = null;
      final Resource resource = Resource.getResource(this.url);
      if (resource.exists()) {
        try {
          tin = TriangulatedIrregularNetwork.newTriangulatedIrregularNetwork(resource);
          if (tin == null) {
            Logs.error(TriangulatedIrregularNetworkLayer.class,
              "Cannot load elevation model: " + this.url);
          }
        } catch (final RuntimeException e) {
          Logs.error(TriangulatedIrregularNetworkLayer.class,
            "Unable to elevation model: " + this.url, e);
        }
      } else {
        Logs.error(TriangulatedIrregularNetworkLayer.class,
          "Elevation model does not exist: " + this.url);
      }
      setTin(tin);
    } else {
      if (this.tin != null) {
        this.tin.cancelChanges();
      }
    }
    firePropertyChange("hasChanges", true, false);
  }

  @Override
  public BoundingBox getBoundingBox() {
    final TriangulatedIrregularNetwork elevationModel = getTin();
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

  @Override
  public GeometryFactory getGeometryFactory() {
    if (this.tin == null) {
      return getBoundingBox().getGeometryFactory();
    } else {
      return this.tin.getGeometryFactory();
    }
  }

  public int getOpacity() {
    return this.opacity;
  }

  public TriangulatedIrregularNetwork getTin() {
    return this.tin;
  }

  @Override
  protected boolean initializeDo() {
    final String url = getProperty("url");
    if (Property.hasValue(url)) {
      this.url = url;
      this.resource = Resource.getResource(url);
      cancelChanges();
      return this.tin != null;
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
      final TriangulatedIrregularNetwork image = getTin();
      if (event.getSource() == image) {
        image.writeTriangulatedIrregularNetwork();
      }
    }
  }

  protected void saveImageChanges() {
    if (this.tin != null) {
      this.tin.writeTriangulatedIrregularNetwork();
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

  public void setTin(final TriangulatedIrregularNetwork elevationModel) {
    final TriangulatedIrregularNetwork old = this.tin;
    Property.removeListener(this.tin, this);
    this.tin = elevationModel;
    if (elevationModel == null) {
      setExists(false);
    } else {
      setExists(true);
      Property.addListener(elevationModel, this);
    }
    firePropertyChange("elevationModel", old, this.tin);
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
