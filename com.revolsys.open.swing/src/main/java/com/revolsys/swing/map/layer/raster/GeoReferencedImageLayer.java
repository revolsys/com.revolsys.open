package com.revolsys.swing.map.layer.raster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.InvokeMethodMapObjectFactory;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.overlay.MappedLocation;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.model.ObjectTreeModel;

public class GeoReferencedImageLayer extends AbstractLayer {

  public static final MapObjectFactory FACTORY = new InvokeMethodMapObjectFactory(
    "geoReferencedImage", "Geo-referenced Image",
    GeoReferencedImageLayer.class, "create");

  static {
    final MenuFactory menu = ObjectTreeModel.getMenu(GeoReferencedImageLayer.class);

    menu.addMenuItem("edit", TreeItemRunnable.createAction("Fit to Screen",
      "arrow_out", "fitToViewport"));
    menu.addMenuItem(
      "edit",
      TreeItemRunnable.createAction("Revert to Saved", "arrow_revert", "revert"));

  }

  public static GeoReferencedImageLayer create(
    final Map<String, Object> properties) {
    final String url = (String)properties.get("url");
    if (StringUtils.hasText(url)) {
      final Resource resource = SpringUtil.getResource(url);
      final GeoReferencedImageLayer layer = new GeoReferencedImageLayer(
        resource);
      layer.setProperties(properties);
      return layer;
    } else {
      throw new IllegalArgumentException(
        "Layer definition does not contain a 'url' property");
    }
  }

  private GeoReferencedImage image;

  private final Resource resource;

  private final String url;

  public GeoReferencedImageLayer(final Resource resource) {
    setType(getType());
    setRenderer(new GeoReferencedImageLayerRenderer(this));
    setSelectSupported(false);
    setQuerySupported(false);
    this.resource = resource;
    this.url = SpringUtil.getUrl(resource).toString();
    setType("geoReferencedImage");
    setName(FileUtil.getBaseName(this.url));
    Invoke.background("Loading file: " + this.url, this, "revert");
  }

  public BoundingBox fitToViewport() {
    final Project project = getProject();
    if (project == null || this.image == null) {
      return new BoundingBox();
    } else {
      final BoundingBox oldValue = this.image.getBoundingBox();
      final BoundingBox viewBoundingBox = project.getViewBoundingBox();
      if (viewBoundingBox.isNull()) {
        return viewBoundingBox;
      } else {
        final double viewRatio = viewBoundingBox.getAspectRatio();
        final double imageRatio = this.image.getImageAspectRatio();
        BoundingBox boundingBox;
        if (viewRatio > imageRatio) {
          boundingBox = viewBoundingBox.expandPercent(-1 + imageRatio
            / viewRatio, 0.0);
        } else if (viewRatio < imageRatio) {
          boundingBox = viewBoundingBox.expandPercent(0.0, -1 + viewRatio
            / imageRatio);
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
    final GeoReferencedImage image = getImage();
    if (image == null) {
      return new BoundingBox();
    } else {
      final BoundingBox boundingBox = image.getBoundingBox();
      if (boundingBox == null || boundingBox.isNull()) {
        return fitToViewport();
      }
      return boundingBox;
    }
  }

  @Override
  public BoundingBox getBoundingBox(final boolean visibleLayersOnly) {
    if (isVisible() || !visibleLayersOnly) {
      return getBoundingBox();
    } else {
      return new BoundingBox(getGeometryFactory());
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return getBoundingBox().getGeometryFactory();
  }

  public GeoReferencedImage getImage() {
    return this.image;
  }

  public void revert() {
    if (this.image == null && this.resource != null) {
      final Resource imageResource = SpringUtil.getResource(this.url);
      if (imageResource.exists()) {
        try {
          this.image = AbstractGeoReferencedImageFactory.loadGeoReferencedImage(imageResource);
          if (this.image == null) {
            LoggerFactory.getLogger(GeoReferencedImageLayer.class).error(
              "Cannot load image:" + this.url);
          } else {
            final Object boundingBoxProperty = getProperty("boundingBox");
            final BoundingBox boundingBox = StringConverterRegistry.toObject(
              BoundingBox.class, boundingBoxProperty);
            if (boundingBox != null && !boundingBox.isEmpty()) {
              image.setBoundingBox(boundingBox);
            }

            final List<?> tiePointsProperty = getProperty("tiePoints");
            final List<MappedLocation> tiePoints = new ArrayList<MappedLocation>();
            if (tiePointsProperty != null) {
              for (final Object tiePointValue : tiePointsProperty) {
                if (tiePointValue instanceof MappedLocation) {
                  tiePoints.add((MappedLocation)tiePointValue);
                } else if (tiePointValue instanceof Map) {
                  final Map<String, Object> map = (Map<String, Object>)tiePointValue;
                  tiePoints.add(new MappedLocation(map));
                }
              }

              image.setTiePoints(tiePoints);
            }

          }
        } catch (final RuntimeException e) {
          LoggerFactory.getLogger(GeoReferencedImageLayer.class).error(
            "Unable to load image" + this.url, e);
        }
      } else {
        LoggerFactory.getLogger(GeoReferencedImageLayer.class).error(
          "Image does not exist:" + this.url);
      }
    } else {
      this.image.revert();
    }
    firePropertyChange("revert", false, true);
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    if (image != null) {
      this.image.setBoundingBox(boundingBox);
    }
  }

  public void setImage(final GeoReferencedImage image) {
    this.image = image;
  }

  @Override
  public void setProperty(final String name, Object value) {
    if ("boundingBox".equals(name)) {
      value = StringConverterRegistry.toObject(BoundingBox.class, value);
    }
    super.setProperty(name, value);
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    map.remove("querySupported");
    map.remove("selectSupported");
    MapSerializerUtil.add(map, "url", this.url);
    final BoundingBox boundingBox = image.getBoundingBox();
    if (boundingBox != null) {
      MapSerializerUtil.add(map, "boundingBox", boundingBox.toString());
    }
    MapSerializerUtil.add(map, "tiePoints", image.getTiePoints(),
      Collections.emptyList());
    return map;
  }
}
