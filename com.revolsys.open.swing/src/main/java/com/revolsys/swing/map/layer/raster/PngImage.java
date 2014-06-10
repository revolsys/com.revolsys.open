package com.revolsys.swing.map.layer.raster;

import org.springframework.core.io.Resource;

public class PngImage extends JaiGeoReferencedImage {

  public PngImage(final Resource imageResource) {
    super(imageResource);
  }

  @Override
  public String getWorldFileExtension() {
    return "pgw";
  }
}
