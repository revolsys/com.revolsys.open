package com.revolsys.format.pdf;

import org.springframework.core.io.Resource;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageFactory;

public class PdfImageFactory extends AbstractIoFactory implements GeoreferencedImageFactory {

  public PdfImageFactory() {
    super("PDF");
    addMediaTypeAndFileExtension("application/pdf", "pdf");
  }

  @Override
  public GeoreferencedImage loadImage(final Resource resource) {
    return new PdfImage(resource);
  }

}
