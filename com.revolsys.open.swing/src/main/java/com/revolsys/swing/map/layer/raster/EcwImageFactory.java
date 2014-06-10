package com.revolsys.swing.map.layer.raster;

import it.geosolutions.imageio.plugins.ecw.ECWImageReaderSpi;
import it.geosolutions.imageio.stream.input.spi.FileImageInputStreamExtImplSpi;

import javax.imageio.spi.IIORegistry;

import org.springframework.core.io.Resource;

public class EcwImageFactory extends AbstractGeoReferencedImageFactory {
  static {
    final IIORegistry registry = IIORegistry.getDefaultInstance();
    registry.registerServiceProvider(new ECWImageReaderSpi());
    registry.registerServiceProvider(new FileImageInputStreamExtImplSpi());
  }

  public EcwImageFactory() {
    super("ECW");
    addMediaTypeAndFileExtension("image/ecw", "ecw");
  }

  @Override
  public GeoReferencedImage loadImage(final Resource resource) {
    return new EcwImage(resource);
  }

}
