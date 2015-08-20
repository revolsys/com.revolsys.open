package com.revolsys.format.jpg;

import com.revolsys.spring.resource.Resource;

import com.revolsys.raster.JaiGeoreferencedImage;

public class JpegImage extends JaiGeoreferencedImage {

  public JpegImage(final Resource imageResource) {
    super(imageResource);
  }

  @Override
  public String getWorldFileExtension() {
    return "jgw";
  }
}
