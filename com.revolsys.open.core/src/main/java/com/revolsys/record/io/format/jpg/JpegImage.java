package com.revolsys.record.io.format.jpg;

import com.revolsys.raster.JaiGeoreferencedImage;
import com.revolsys.spring.resource.Resource;

public class JpegImage extends JaiGeoreferencedImage {

  public JpegImage(final Resource imageResource) {
    super(imageResource);
  }

  @Override
  public String getWorldFileExtension() {
    return "jgw";
  }
}
