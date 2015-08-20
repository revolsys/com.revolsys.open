package com.revolsys.raster;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import com.revolsys.spring.resource.Resource;

public class JaiGeoreferencedImage extends AbstractGeoreferencedImage {
  protected JaiGeoreferencedImage() {
  }

  public JaiGeoreferencedImage(final Resource imageResource) {
    setImageResource(imageResource);

    final PlanarImage jaiImage = JAI.create("fileload", getFile().getAbsolutePath());
    setRenderedImage(jaiImage);

    loadImageMetaData();
    postConstruct();
  }

  @Override
  public void cancelChanges() {
    if (getImageResource() != null) {
      loadImageMetaData();
      setHasChanges(false);
    }
  }
}
