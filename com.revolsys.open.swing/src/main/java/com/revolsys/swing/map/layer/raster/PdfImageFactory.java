package com.revolsys.swing.map.layer.raster;

import org.springframework.core.io.Resource;

public class PdfImageFactory extends AbstractGeoReferencedImageFactory {

  public PdfImageFactory() {
    super("PDF");
    addMediaTypeAndFileExtension("application/pdf", "pdf");
  }

  @Override
  public GeoReferencedImage loadImage(final Resource resource) {
    return new PdfImage(resource);
  }

}
