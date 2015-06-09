package com.revolsys.format.pdf;

import org.springframework.core.io.Resource;

import com.revolsys.raster.AbstractGeoreferencedImageFactory;
import com.revolsys.raster.GeoreferencedImage;

public class PdfImageFactory extends AbstractGeoreferencedImageFactory {

  public PdfImageFactory() {
    super("PDF");
    addMediaTypeAndFileExtension("application/pdf", "pdf");
  }

  @Override
  public GeoreferencedImage loadImage(final Resource resource) {
    return new PdfImage(resource);
  }

}
