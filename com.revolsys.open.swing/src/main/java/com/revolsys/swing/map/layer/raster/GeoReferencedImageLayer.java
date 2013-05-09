package com.revolsys.swing.map.layer.raster;

import java.util.Map;

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
    MenuFactory menu = ObjectTreeModel.getMenu(GeoReferencedImageLayer.class);

    menu.addMenuItem("edit", TreeItemRunnable.createAction("Fit to Screen",
      "arrow_out", null, "fitToViewport"));

  }

  public static GeoReferencedImageLayer create(
    final Map<String, Object> properties) {
    final String url = (String)properties.get("url");
    if (url == null) {
      throw new IllegalArgumentException(
        "A geo referenced image layer requires a url.");
    } else {
      Resource imageResource = SpringUtil.getResource(url);
      GeoReferencedImage image = new GeoTiffImage(imageResource);

      final GeoReferencedImageLayer layer = new GeoReferencedImageLayer(image);
      layer.setProperties(properties);
      return layer;
    }
  }

  public void fitToViewport() {
    BoundingBox oldValue = image.getBoundingBox();
    Project project = getProject();
    if (project != null) {
      BoundingBox viewBoundingBox = project.getViewBoundingBox();
      if (!viewBoundingBox.isNull()) {
        double viewRatio = viewBoundingBox.getAspectRatio();
        double imageRatio = image.getImageAspectRatio();
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
      }
    }
  }

  private GeoReferencedImage image;

  public GeoReferencedImageLayer(GeoReferencedImage image) {
    setRenderer(new GeoReferencedImageLayerRenderer(this));
    setSelectSupported(false);
    setQuerySupported(false);
    setImage(image);
    setGeometryFactory(image.getGeometryFactory());
  }

  public GeoReferencedImageLayer(String name, GeoReferencedImage image) {
    this(image);
    setName(name);
  }

  @Override
  public BoundingBox getBoundingBox() {
    return getImage().getBoundingBox();
  }

  @Override
  public BoundingBox getBoundingBox(final boolean visibleLayersOnly) {
    if (isVisible() || !visibleLayersOnly) {
      return getBoundingBox();
    } else {
      return new BoundingBox(getGeometryFactory());
    }
  }

  public void setBoundingBox(BoundingBox boundingBox) {
    image.setBoundingBox(boundingBox);
  }

  public void setImage(GeoReferencedImage image) {
    this.image = image;
  }

  public GeoReferencedImage getImage() {
    return image;
  }
}
