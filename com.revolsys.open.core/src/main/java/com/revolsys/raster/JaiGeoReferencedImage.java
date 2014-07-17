package com.revolsys.raster;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.springframework.core.io.Resource;

public class JaiGeoReferencedImage extends AbstractGeoReferencedImage {
  protected JaiGeoReferencedImage() {
  }

  public JaiGeoReferencedImage(final Resource imageResource) {
    setImageResource(imageResource);

    final PlanarImage jaiImage = JAI.create("fileload",
      getFile().getAbsolutePath());
    setRenderedImage(jaiImage);

    loadImageMetaData();
    postConstruct();
  }
}
