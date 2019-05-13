package com.revolsys.raster.io.format.jpg;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageReadFactory;
import com.revolsys.raster.GeoreferencedImageWriter;
import com.revolsys.raster.GeoreferencedImageWriterFactory;
import com.revolsys.raster.ImageIoGeoreferencedImageWriter;
import com.revolsys.spring.resource.Resource;

public class JpegImageFactory extends AbstractIoFactory
  implements GeoreferencedImageReadFactory, GeoreferencedImageWriterFactory {

  public JpegImageFactory() {
    super("JPEG");
    addMediaTypeAndFileExtension("image/jpeg", "jpg");
    addMediaTypeAndFileExtension("image/jpeg", "jpeg");
  }

  @Override
  public GeoreferencedImage loadImage(final Resource resource) {
    return new JpegImage(resource);
  }

  @Override
  public GeoreferencedImageWriter newGeoreferencedImageWriter(final Resource resource) {
    return new ImageIoGeoreferencedImageWriter(resource, "JPEG");
  }

}
