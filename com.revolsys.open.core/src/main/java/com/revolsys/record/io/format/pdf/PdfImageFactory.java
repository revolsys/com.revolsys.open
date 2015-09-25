package com.revolsys.record.io.format.pdf;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageFactory;
import com.revolsys.spring.resource.Resource;

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
