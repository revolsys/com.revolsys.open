package com.revolsys.raster.io.format.png;

import com.revolsys.raster.JaiGeoreferencedImage;
import com.revolsys.spring.resource.Resource;

public class PngImage extends JaiGeoreferencedImage {

  public PngImage(final Resource imageResource) {
    super(imageResource);
  }

  @Override
  public String getWorldFileExtension() {
    return "pgw";
  }
}
