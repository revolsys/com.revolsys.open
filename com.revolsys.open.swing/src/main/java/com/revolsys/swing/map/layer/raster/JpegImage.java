package com.revolsys.swing.map.layer.raster;

import org.springframework.core.io.Resource;

public class JpegImage extends JaiGeoReferencedImage {

  public JpegImage(final Resource imageResource) {
    super(imageResource);
  }

  @Override
  public String getWorldFileExtension() {
    return "jgw";
  }
}
