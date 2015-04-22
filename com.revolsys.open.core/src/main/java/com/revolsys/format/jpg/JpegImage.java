package com.revolsys.format.jpg;

import org.springframework.core.io.Resource;

import com.revolsys.raster.JaiGeoReferencedImage;

public class JpegImage extends JaiGeoReferencedImage {

  public JpegImage(final Resource imageResource) {
    super(imageResource);
  }

  @Override
  public String getWorldFileExtension() {
    return "jgw";
  }
}
