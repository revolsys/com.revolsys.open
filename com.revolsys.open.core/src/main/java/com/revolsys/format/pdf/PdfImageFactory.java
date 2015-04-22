package com.revolsys.format.pdf;

import org.springframework.core.io.Resource;

import com.revolsys.raster.AbstractGeoReferencedImageFactory;
import com.revolsys.raster.GeoReferencedImage;

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
