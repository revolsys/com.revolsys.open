package com.revolsys.raster;

import org.springframework.core.io.Resource;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.IoFactoryRegistry;

public abstract class AbstractGeoreferencedImageFactory extends AbstractIoFactory implements
  GeoreferencedImageFactory {

  public static GeoreferencedImageFactory getGeoreferencedImageFactory(final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final GeoreferencedImageFactory factory = ioFactoryRegistry.getFactoryByResource(
      GeoreferencedImageFactory.class, resource);
    return factory;
  }

  public static GeoreferencedImageFactory getGeoreferencedImageFactory(final String fileName) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final GeoreferencedImageFactory factory = ioFactoryRegistry.getFactoryByFileName(
      GeoreferencedImageFactory.class, fileName);
    return factory;
  }

  public static boolean hasGeoreferencedImageFactory(final Resource resource) {
    return getGeoreferencedImageFactory(resource) != null;
  }

  public static boolean hasGeoreferencedImageFactory(final String fileName) {
    return getGeoreferencedImageFactory(fileName) != null;
  }

  public static GeoreferencedImage loadGeoreferencedImage(final Resource resource) {
    final GeoreferencedImageFactory factory = getGeoreferencedImageFactory(resource);
    if (factory == null) {
      return null;
    } else {
      final GeoreferencedImage reader = factory.loadImage(resource);
      return reader;
    }
  }

  public AbstractGeoreferencedImageFactory(final String name) {
    super(name);
  }

}
