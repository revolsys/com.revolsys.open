package com.revolsys.raster;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Writer;
import com.revolsys.spring.resource.Resource;

public interface GeoreferencedImageWriter extends Writer<GeoreferencedImage> {
  static boolean isWritable(final Object target) {
    return IoFactory.isAvailable(GeoreferencedImageWriterFactory.class, target);
  }

  static GeoreferencedImageWriter newGeoreferencedImageWriter(final Object target,
    final MapEx properties) {
    final GeoreferencedImageWriterFactory factory = IoFactory
      .factory(GeoreferencedImageWriterFactory.class, target);
    if (factory == null) {
      return null;
    } else {
      final Resource resource = Resource.getResource(target);
      final GeoreferencedImageWriter writer = factory.newGeoreferencedImageWriter(resource);
      writer.setProperties(properties);
      return writer;
    }
  }
}
