package com.revolsys.swing.map.layer.raster;

import java.io.File;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.springframework.core.io.Resource;

import com.revolsys.spring.SpringUtil;

public class GeoJpegImage extends GeoReferencedImage {

  public GeoJpegImage(final Resource imageResource) {
    final File file = SpringUtil.getOrDownloadFile(imageResource);
    final RenderedOp image = JAI.create("fileload", file.getAbsolutePath());
    setImage(image.getAsBufferedImage());
    loadImageMetaData(imageResource, image);
  }

  protected void loadImageMetaData(final Resource resource,
    final RenderedOp image) {
    loadProjectionFile(resource);
    loadWorldFile(resource, "jgw");
  }
}
