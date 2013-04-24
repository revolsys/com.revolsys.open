package com.revolsys.swing.map.layer.raster;

import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.InvokeMethodLayerFactory;
import com.revolsys.swing.map.layer.LayerFactory;

public class GeoReferencedImageLayer extends AbstractLayer {

  public static final LayerFactory<GeoReferencedImageLayer> FACTORY = new InvokeMethodLayerFactory<GeoReferencedImageLayer>(
    "geoReferencedImage", "Geo-referenced Image", GeoReferencedImageLayer.class, "create");

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

  private GeoReferencedImage image;

  public GeoReferencedImageLayer(GeoReferencedImage image) {
    setRenderer(new GeoReferencedImageLayerRenderer(this));
    setReadOnly(true);
    setSelectSupported(false);
    setQuerySupported(false);
    setImage(image);
    setGeometryFactory(image.getGeometryFactory());
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

  public void setImage(GeoReferencedImage image) {
    this.image = image;
  }

  public GeoReferencedImage getImage() {
    return image;
  }
}
