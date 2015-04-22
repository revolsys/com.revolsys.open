package com.revolsys.format.gif;

import org.springframework.core.io.Resource;

import com.revolsys.raster.JaiGeoReferencedImage;

public class GifImage extends JaiGeoReferencedImage {

  public GifImage(final Resource imageResource) {
    super(imageResource);
  }

  @Override
  public String getWorldFileExtension() {
    return "gfw";
  }
}
