package com.revolsys.raster;

import org.springframework.core.io.Resource;

import com.revolsys.io.IoFactory;

public interface GeoreferencedImageFactory extends IoFactory {

  GeoreferencedImage loadImage(Resource resource);
}
