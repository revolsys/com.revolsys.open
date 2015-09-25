package com.revolsys.record.io.format.gif;

import com.revolsys.raster.JaiGeoreferencedImage;
import com.revolsys.spring.resource.Resource;

public class GifImage extends JaiGeoreferencedImage {

  public GifImage(final Resource imageResource) {
    super(imageResource);
  }

  @Override
  public String getWorldFileExtension() {
    return "gfw";
  }
}
