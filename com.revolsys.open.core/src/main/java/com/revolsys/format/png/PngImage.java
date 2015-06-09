package com.revolsys.format.png;

import org.springframework.core.io.Resource;

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
