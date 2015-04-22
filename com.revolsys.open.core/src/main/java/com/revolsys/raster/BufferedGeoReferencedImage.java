package com.revolsys.raster;

import java.awt.image.BufferedImage;

import com.revolsys.jts.geom.BoundingBox;

public class BufferedGeoReferencedImage extends AbstractGeoReferencedImage {

  protected BufferedGeoReferencedImage() {

  }

  public BufferedGeoReferencedImage(final BoundingBox boundingBox, final BufferedImage image) {
    this(boundingBox, image.getWidth(), image.getHeight());
    setRenderedImage(image);
    postConstruct();
  }

  public BufferedGeoReferencedImage(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight) {
    setBoundingBox(boundingBox);
    setImageWidth(imageWidth);
    setImageHeight(imageHeight);
    postConstruct();
  }

  @Override
  public String toString() {
    return "BufferedImage " + getImageWidth() + "x" + getImageHeight();
  }

}
