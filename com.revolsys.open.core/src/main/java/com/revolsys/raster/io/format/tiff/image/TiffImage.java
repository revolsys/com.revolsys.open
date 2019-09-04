package com.revolsys.raster.io.format.tiff.image;

import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.io.format.tiff.TiffDirectory;

public interface TiffImage extends GeoreferencedImage {
  TiffDirectory getTiffDirectory();
}
