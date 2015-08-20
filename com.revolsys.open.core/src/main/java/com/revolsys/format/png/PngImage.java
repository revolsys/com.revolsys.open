package com.revolsys.format.png;

import com.revolsys.spring.resource.Resource;

import com.revolsys.raster.JaiGeoreferencedImage;

public class PngImage extends JaiGeoreferencedImage {

  public PngImage(final Resource imageResource) {
    super(imageResource);
  }

  @Override
  public String getWorldFileExtension() {
    return "pgw";
  }
}
