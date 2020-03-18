package com.revolsys.swing.map.layer.elevation.tin;

import java.beans.PropertyChangeEvent;
import java.util.Map;

import org.jeometry.common.logging.Logs;

import com.revolsys.elevation.tin.TriangulatedIrregularNetwork;
import com.revolsys.elevation.tin.TriangulatedIrregularNetworkReaderFactory;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.elevation.ElevationModelLayer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.util.Property;

public class TriangulatedIrregularNetworkLayer extends AbstractLayer
  implements ElevationModelLayer {
  public static final String J_TYPE = "triangulatedIrregularNetworkLayer";
  static {
    final MenuFactory menu = MenuFactory.getMenu(TriangulatedIrregularNetworkLayer.class);
    menu.addGroup(0, "table");
    menu.addGroup(2, "edit");

    menu.<TriangulatedIrregularNetworkLayer> addMenuItem("refresh", "Reload from File",
      Icons.getIconWithBadge("page", "refresh"), TriangulatedIrregularNetworkLayer::revertDo, true);
    menu.deleteMenuItem("refresh", "Refresh");
  }

  private TriangulatedIrregularNetwork tin;

  private Resource resource;

  private String url;

  public TriangulatedIrregularNetworkLayer(final Map<String, ? extends Object> properties) {
    super(J_TYPE);
    setProperties(properties);
    setSelectSupported(false);
    setQuerySupported(false);
    setReadOnly(true);
    final TriangulatedIrregularNetworkLayerRenderer renderer = new TriangulatedIrregularNetworkLayerRenderer(
      this);
    setRenderer(renderer);
    setIcon("tin");
  }

  @Override
  public BoundingBox getBoundingBox() {
    final TriangulatedIrregularNetwork elevationModel = getTin();
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
      return getGeometryFactory().bboxEmpty();
    }
  }

  @Override
  public double getElevation(final double x, final double y) {
    return this.tin.getElevation(x, y);
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    if (this.tin == null) {
      return getBoundingBox().getGeometryFactory();
    } else {
      return this.tin.getGeometryFactory();
    }
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
      revertDo();
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
      final TriangulatedIrregularNetworkReaderFactory factory = IoFactory
        .factoryByFileExtension(TriangulatedIrregularNetworkReaderFactory.class, fileNameExtension);
      if (factory != null) {
        SwingUtil.addLabelledReadOnlyTextField(panel, "File Type", factory.getName());
      }
    }
    GroupLayouts.makeColumns(panel, 2, true);
    return panel;
  }

  @Override
  public BufferedGeoreferencedImage newRenderImage() {
    return null;
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

  protected void revertDo() {
    if (this.resource != null) {
      TriangulatedIrregularNetwork tin = null;
      try {
        this.tin = null;
        final Resource resource = Resource.getResource(this.url);
        if (resource.exists()) {
          tin = TriangulatedIrregularNetwork.newTriangulatedIrregularNetwork(resource);
          if (tin == null) {
            Logs.error(TriangulatedIrregularNetworkLayer.class, "Cannot load TIN: " + this.url);
          }
        } else {
          Logs.error(TriangulatedIrregularNetworkLayer.class, "TIN does not exist: " + this.url);
        }
      } catch (final Throwable e) {
        Logs.error(TriangulatedIrregularNetworkLayer.class, "Unable to load TIN: " + this.url, e);
      } finally {
        setTin(tin);
      }
      firePropertyChange("hasChanges", true, false);
    }

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
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    map.remove("querySupported");
    map.remove("selectSupported");
    map.remove("editable");
    map.remove("readOnly");
    map.remove("showOriginalImage");
    map.remove("imageSettings");
    addToMap(map, "url", this.url);
    return map;
  }

  @Override
  public void zoomToLayer() {
    final Project project = getProject();
    final GeometryFactory geometryFactory = project.getGeometryFactory();
    final BoundingBox layerBoundingBox = getBoundingBox();
    BoundingBox boundingBox = layerBoundingBox;
    boundingBox = boundingBox //
      .bboxEditor() //
      .setGeometryFactory(geometryFactory) //
      .expandPercent(0.1) //
      .clipToCoordinateSystem() //
      .newBoundingBox();

    project.setViewBoundingBox(boundingBox);
  }
}
