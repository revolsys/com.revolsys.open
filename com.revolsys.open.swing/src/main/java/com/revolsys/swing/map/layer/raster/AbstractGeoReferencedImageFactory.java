package com.revolsys.swing.map.layer.raster;

import org.springframework.core.io.Resource;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.IoFactoryRegistry;

public abstract class AbstractGeoReferencedImageFactory extends
  AbstractIoFactory implements GeoReferencedImageFactory {
  
  public static GeoReferencedImage loadGeoReferencedImage(final Resource resource) {
    final GeoReferencedImageFactory factory = getGeoReferencedImageFactory(resource);
    if (factory == null) {
      return null;
    } else {
      final GeoReferencedImage reader = factory.loadImage(resource);
      return reader;
    }
  }


  protected static GeoReferencedImageFactory getGeoReferencedImageFactory(
    final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final GeoReferencedImageFactory factory = ioFactoryRegistry.getFactoryByResource(
      GeoReferencedImageFactory.class, resource);
    return factory;
  }
  
  public AbstractGeoReferencedImageFactory(String name) {
    super(name);
  }

}
