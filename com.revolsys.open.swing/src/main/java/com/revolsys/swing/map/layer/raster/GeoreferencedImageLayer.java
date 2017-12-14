package com.revolsys.swing.map.layer.raster;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import java.util.function.Predicate;

import javax.swing.JOptionPane;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.logging.Logs;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageReadFactory;
import com.revolsys.raster.MappedLocation;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.Borders;
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
import com.revolsys.util.number.Doubles;

public class GeoreferencedImageLayer extends AbstractLayer {

  static {
    MenuFactory.addMenuInitializer(GeoreferencedImageLayer.class, (menu) -> {
      menu.addGroup(0, "table");
      menu.addGroup(2, "edit");

      final Predicate<GeoreferencedImageLayer> notReadOnly = ((Predicate<GeoreferencedImageLayer>)GeoreferencedImageLayer::isReadOnly)
        .negate();
      final Predicate<GeoreferencedImageLayer> editable = GeoreferencedImageLayer::isEditable;

      Menus.<GeoreferencedImageLayer> addMenuItem(menu, "table", "View Tie-Points", "table_go",
        GeoreferencedImageLayer::showTableView, false);

      Menus.<GeoreferencedImageLayer> addCheckboxMenuItem(menu, "edit", "Editable", "pencil",
        notReadOnly, GeoreferencedImageLayer::toggleEditable, editable, true);

      Menus.<GeoreferencedImageLayer> addCheckboxMenuItem(menu, "edit", "Show Original Image",
        (String)null, editable.and(GeoreferencedImageLayer::isHasTransform),
        GeoreferencedImageLayer::toggleShowOriginalImage,
        GeoreferencedImageLayer::isShowOriginalImage, true);

      Menus.<GeoreferencedImageLayer> addMenuItem(menu, "edit", "Fit to Screen", "arrow_out",
        editable, GeoreferencedImageLayer::fitToViewport, true);

      menu.deleteMenuItem("refresh", "Refresh");
    });
  }

  public static GeoreferencedImageLayer newLayer(final Map<String, ? extends Object> config) {
    return new GeoreferencedImageLayer(config);
  }

  private GeoreferencedImage image;

  private int opacity = 255;

  private Resource resource;

  private boolean showOriginalImage = false;

  private String url;

  public GeoreferencedImageLayer(final Map<String, ? extends Object> config) {
    super("geoReferencedImageLayer");
    setProperties(config);
    setSelectSupported(false);
    setQuerySupported(false);
    setRenderer(new GeoreferencedImageLayerRenderer(this));
    final int opacity = Maps.getInteger(config, "opacity", 255);
    setOpacity(opacity);
    setIcon("picture");
  }

  public void cancelChanges() {
    if (this.image == null && this.resource != null) {
      GeoreferencedImage image = null;
      final Resource imageResource = Resource.getResource(this.url);
      if (imageResource.exists()) {
        try {
          image = GeoreferencedImageReadFactory.loadGeoreferencedImage(imageResource);
          if (image == null) {
            Logs.error(GeoreferencedImageLayer.class, "Cannot load image: " + this.url);
          }
        } catch (final RuntimeException e) {
          Logs.error(GeoreferencedImageLayer.class, "Unable to load image: " + this.url, e);
        }
      } else {
        Logs.error(GeoreferencedImageLayer.class, "Image does not exist: " + this.url);
      }
      setImage(image);
    } else {
      this.image.cancelChanges();
    }
    firePropertyChange("hasChanges", true, false);
  }

  public void deleteTiePoint(final MappedLocation tiePoint) {
    if (isEditable()) {
      this.image.deleteTiePoint(tiePoint);
    } else {
      Logs.error(this, "Cannot delete tie-point. Layer " + getPath() + " is not editable");
    }
  }

  public synchronized BoundingBox fitToViewport() {
    final Project project = getProject();
    if (project == null || this.image == null || !isInitialized()) {
      return BoundingBox.empty();
    } else {
      final BoundingBox oldValue = this.image.getBoundingBox();
      final BoundingBox viewBoundingBox = project.getViewBoundingBox();
      if (viewBoundingBox.isEmpty()) {
        return viewBoundingBox;
      } else {
        final double viewRatio = viewBoundingBox.getAspectRatio();
        final double imageRatio = this.image.getImageAspectRatio();
        BoundingBox boundingBox;
        if (viewRatio > imageRatio) {
          boundingBox = viewBoundingBox.expandPercent(-1 + imageRatio / viewRatio, 0.0);
        } else if (viewRatio < imageRatio) {
          boundingBox = viewBoundingBox.expandPercent(0.0, -1 + viewRatio / imageRatio);
        } else {
          boundingBox = viewBoundingBox;
        }
        this.image.setBoundingBox(boundingBox);
        firePropertyChange("boundingBox", oldValue, boundingBox);
        return boundingBox;
      }
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    final GeoreferencedImage image = getImage();
    if (image == null) {
      return BoundingBox.empty();
    } else {
      BoundingBox boundingBox = image.getBoundingBox();
      if (boundingBox.isEmpty()) {
        final boolean hasChanges = isHasChanges();
        boundingBox = fitToViewport();
        if (hasChanges) {
          saveChanges();
        } else {
          cancelChanges();
        }
      }
      return boundingBox;
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

  @Override
  public GeometryFactory getGeometryFactory() {
    if (this.image == null) {
      return getBoundingBox().getGeometryFactory();
    } else {
      return this.image.getGeometryFactory();
    }
  }

  public GeoreferencedImage getImage() {
    return this.image;
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
      return true;
    } else {
      Logs.error(this, "Layer definition does not contain a 'url' property");
      return false;
    }
  }

  public boolean isHasTransform() {
    if (this.image == null) {
      return false;
    } else {
      return this.image.isHasTransform();
    }
  }

  public boolean isShowOriginalImage() {
    return this.image.isHasTransform() && this.showOriginalImage;
  }

  @Override
  public boolean isVisible() {
    return super.isVisible() || isEditable();
  }

  @Override
  public TabbedValuePanel newPropertiesPanel() {
    final TabbedValuePanel propertiesPanel = super.newPropertiesPanel();
    final TiePointsPanel tiePointsPanel = newTableViewComponent(null);
    if (tiePointsPanel != null) {
      Borders.titled(tiePointsPanel, "Tie Points");

      propertiesPanel.addTab("Geo-Referencing", tiePointsPanel);
    }
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
  protected TiePointsPanel newTableViewComponent(final Map<String, Object> config) {
    if (getImage() == null) {
      return null;
    } else {
      return new TiePointsPanel(this);
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    super.propertyChange(event);
    final String propertyName = event.getPropertyName();
    if ("hasChanges".equals(propertyName)) {
      final GeoreferencedImage image = getImage();
      if (event.getSource() == image) {
        image.saveChanges();
      }
    }
  }

  protected void saveImageChanges() {
    if (this.image != null) {
      this.image.saveChanges();
    }
  }

  @Override
  public void setBoundingBox(final BoundingBox boundingBox) {
    if (this.image != null) {
      this.image.setBoundingBox(boundingBox);
    }
  }

  @Override
  public void setEditable(final boolean editable) {
    Invoke.background("Set Editable " + this, () -> {
      synchronized (getSync()) {
        if (editable) {
          setShowOriginalImage(true);
        } else {
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
        if (editable == false) {
          setShowOriginalImage(false);
        }
      }
    });
  }

  public void setImage(final GeoreferencedImage image) {
    final GeoreferencedImage old = this.image;
    Property.removeListener(this.image, this);
    this.image = image;
    if (image == null) {
      setExists(false);
    } else {
      setExists(true);
      Property.addListener(image, this);
    }
    firePropertyChange("image", old, this.image);
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

  public void setShowOriginalImage(final boolean showOriginalImage) {
    final Object oldValue = this.showOriginalImage;
    this.showOriginalImage = showOriginalImage;
    firePropertyChange("showOriginalImage", oldValue, showOriginalImage);
  }

  @Override
  public void setVisible(final boolean visible) {
    super.setVisible(visible);
    if (!visible) {
      setEditable(false);
    }
  }

  public Point sourcePixelToTargetPoint(final BoundingBox boundingBox, final boolean useTransform,
    final double... coordinates) {
    if (useTransform) {
      final AffineTransform transform = this.image.getAffineTransformation(boundingBox);
      transform.transform(coordinates, 0, coordinates, 0, 1);
    }
    final double imageX = coordinates[0];
    final double imageY = coordinates[1];

    final GeoreferencedImage image = getImage();
    final double xPercent = imageX / image.getImageWidth();
    final double yPercent = imageY / image.getImageHeight();

    final double modelWidth = boundingBox.getWidth();
    final double modelHeight = boundingBox.getHeight();

    final double modelX = boundingBox.getMinX() + modelWidth * xPercent;
    final double modelY = boundingBox.getMinY() + modelHeight * yPercent;
    final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
    final Point imagePoint = geometryFactory.point(modelX, modelY);
    return imagePoint;
  }

  public Point sourcePixelToTargetPoint(final MappedLocation tiePoint) {
    final Point sourcePixel = tiePoint.getSourcePixel();
    return sourcePixelToTargetPoint(sourcePixel);
  }

  public Point sourcePixelToTargetPoint(final Point sourcePixel) {
    final BoundingBox boundingBox = getBoundingBox();
    final double x = sourcePixel.getX();
    final double y = sourcePixel.getY();
    final boolean useTransform = !isShowOriginalImage();
    return sourcePixelToTargetPoint(boundingBox, useTransform, x, y);
  }

  public Point targetPointToSourcePixel(Point targetPoint) {
    final GeoreferencedImage image = getImage();
    final BoundingBox boundingBox = getBoundingBox();
    targetPoint = targetPoint.convertPoint2d(boundingBox.getGeometryFactory());
    final double modelX = targetPoint.getX();
    final double modelY = targetPoint.getY();
    final double modelDeltaX = modelX - boundingBox.getMinX();
    final double modelDeltaY = modelY - boundingBox.getMinY();

    final double modelWidth = boundingBox.getWidth();
    final double modelHeight = boundingBox.getHeight();

    final double xRatio = modelDeltaX / modelWidth;
    final double yRatio = modelDeltaY / modelHeight;

    final double imageX = image.getImageWidth() * xRatio;
    final double imageY = image.getImageHeight() * yRatio;
    final double[] coordinates = new double[] {
      imageX, imageY
    };
    if (!isShowOriginalImage()) {
      try {
        final AffineTransform transform = image.getAffineTransformation(boundingBox)
          .createInverse();
        transform.transform(coordinates, 0, coordinates, 0, 1);
      } catch (final NoninvertibleTransformException e) {
      }
    }
    return new PointDoubleXY(Doubles.makePrecise(1, coordinates[0]),
      Doubles.makePrecise(1, coordinates[1]));
  }

  public void toggleShowOriginalImage() {
    final boolean showOriginalImage = isShowOriginalImage();
    setShowOriginalImage(!showOriginalImage);
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
    final AffineTransform transform = this.image.getAffineTransformation(layerBoundingBox);
    if (!transform.isIdentity()) {
      final GeoreferencedImage image = getImage();

      final double width = image.getImageWidth() - 1;
      final double height = image.getImageHeight() - 1;
      final double[] targetCoordinates = MappedLocation.toModelCoordinates(image, layerBoundingBox,
        true, 0, height, width, height, width, 0, 0, 0, 0, height);
      final LineString line = layerBoundingBox.getGeometryFactory().lineString(2,
        targetCoordinates);
      boundingBox = boundingBox.expandToInclude(line);
    }
    boundingBox = boundingBox.convert(geometryFactory).expandPercent(0.1).clipToCoordinateSystem();

    project.setViewBoundingBox(boundingBox);
  }
}
