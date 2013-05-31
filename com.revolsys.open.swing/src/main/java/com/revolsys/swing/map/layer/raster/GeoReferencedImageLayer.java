package com.revolsys.swing.map.layer.raster;

import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.InvokeMethodLayerFactory;
import com.revolsys.swing.map.layer.LayerFactory;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.model.ObjectTreeModel;

public class GeoReferencedImageLayer extends AbstractLayer {

  public static final LayerFactory<GeoReferencedImageLayer> FACTORY = new InvokeMethodLayerFactory<GeoReferencedImageLayer>(
    "geoReferencedImage", "Geo-referenced Image",
    GeoReferencedImageLayer.class, "create");

  static {
    final MenuFactory menu = ObjectTreeModel.getMenu(GeoReferencedImageLayer.class);

    menu.addMenuItem("edit", TreeItemRunnable.createAction("Fit to Screen",
      "arrow_out", null, "fitToViewport"));
    menu.addMenuItem("edit", TreeItemRunnable.createAction("Revert to Saved",
      "arrow_undo", null, "revert"));

  }

  public static GeoReferencedImageLayer create(
    final Map<String, Object> properties) {
    final String url = (String)properties.get("url");
    if (url == null) {
      throw new IllegalArgumentException(
        "A geo referenced image layer requires a url.");
    } else {
      final Resource imageResource = SpringUtil.getResource(url);
      if (imageResource.exists()) {
        final GeoReferencedImage image = AbstractGeoReferencedImageFactory.loadGeoReferencedImage(imageResource);
        if (image == null) {
          LoggerFactory.getLogger(GeoReferencedImageLayer.class).error(
            "Cannot load image:" + imageResource);
          return null;
        } else {
          final GeoReferencedImageLayer layer = new GeoReferencedImageLayer(
            image);
          layer.setProperties(properties);
          return layer;
        }
      } else {
        LoggerFactory.getLogger(GeoReferencedImageLayer.class).error(
          "Image does not exist:" + imageResource);
        return null;
      }
    }
  }

  private GeoReferencedImage image;

  public GeoReferencedImageLayer(final GeoReferencedImage image) {
    setRenderer(new GeoReferencedImageLayerRenderer(this));
    setSelectSupported(false);
    setQuerySupported(false);
    setImage(image);
    setGeometryFactory(image.getGeometryFactory());
  }

  public GeoReferencedImageLayer(final String name,
    final GeoReferencedImage image) {
    this(image);
    setName(name);
  }

  public BoundingBox fitToViewport() {
    final BoundingBox oldValue = image.getBoundingBox();
    final Project project = getProject();
    if (project == null) {
      return new BoundingBox();
    } else {
      final BoundingBox viewBoundingBox = project.getViewBoundingBox();
      if (viewBoundingBox.isNull()) {
        return viewBoundingBox;
      } else {
        final double viewRatio = viewBoundingBox.getAspectRatio();
        final double imageRatio = image.getImageAspectRatio();
        BoundingBox boundingBox;
        if (viewRatio > imageRatio) {
          boundingBox = viewBoundingBox.expandPercent(-1
            + (imageRatio / viewRatio), 0.0);
        } else if (viewRatio < imageRatio) {
          boundingBox = viewBoundingBox.expandPercent(0.0, -1
            + (viewRatio / imageRatio));
        } else {
          boundingBox = viewBoundingBox;
        }
        image.setBoundingBox(boundingBox);
        getPropertyChangeSupport().firePropertyChange("boundingBox", oldValue,
          boundingBox);
        return boundingBox;
      }
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    final BoundingBox boundingBox = getImage().getBoundingBox();
    if (boundingBox == null || boundingBox.isNull()) {
      return fitToViewport();
    }
    return boundingBox;
  }

  @Override
  public BoundingBox getBoundingBox(final boolean visibleLayersOnly) {
    if (isVisible() || !visibleLayersOnly) {
      return getBoundingBox();
    } else {
      return new BoundingBox(getGeometryFactory());
    }
  }

  public GeoReferencedImage getImage() {
    return image;
  }

  public void revert() {
    final GeoReferencedImage image = getImage();
    if (image != null) {
      image.revert();
      getPropertyChangeSupport().firePropertyChange("revert", false, true);
    }
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    image.setBoundingBox(boundingBox);
  }

  public void setImage(final GeoReferencedImage image) {
    this.image = image;
  }
}
