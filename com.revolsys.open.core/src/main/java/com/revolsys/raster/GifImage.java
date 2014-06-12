package com.revolsys.raster;

import org.springframework.core.io.Resource;

public class GifImage extends JaiGeoReferencedImage {

  public GifImage(final Resource imageResource) {
    super(imageResource);
  }

  @Override
  public String getWorldFileExtension() {
    return "gfw";
  }
}
