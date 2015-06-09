package com.revolsys.format.gif;

import org.springframework.core.io.Resource;

import com.revolsys.raster.JaiGeoreferencedImage;

public class GifImage extends JaiGeoreferencedImage {

  public GifImage(final Resource imageResource) {
    super(imageResource);
  }

  @Override
  public String getWorldFileExtension() {
    return "gfw";
  }
}
