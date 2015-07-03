package com.revolsys.raster;

import org.springframework.core.io.Resource;

import com.revolsys.io.IoFactory;

public interface GeoreferencedImageFactory extends IoFactory {
  static GeoreferencedImage loadGeoreferencedImage(final Resource resource) {
    final GeoreferencedImageFactory factory = IoFactory.factory(GeoreferencedImageFactory.class,
      resource);
    if (factory == null) {
      return null;
    } else {
      final GeoreferencedImage reader = factory.loadImage(resource);
      return reader;
    }
  }

  GeoreferencedImage loadImage(Resource resource);
}
