package com.revolsys.format.png;

import org.springframework.core.io.Resource;

import com.revolsys.raster.JaiGeoReferencedImage;

public class PngImage extends JaiGeoReferencedImage {

  public PngImage(final Resource imageResource) {
    super(imageResource);
  }

  @Override
  public String getWorldFileExtension() {
    return "pgw";
  }
}
