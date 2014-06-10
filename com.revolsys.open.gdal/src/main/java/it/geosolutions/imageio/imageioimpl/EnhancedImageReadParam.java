/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *        http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.imageioimpl;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.imageio.ImageReadParam;

public class EnhancedImageReadParam extends ImageReadParam implements Cloneable {

  protected Rectangle destinationRegion;

  @Override
  public Object clone() throws CloneNotSupportedException {
    final EnhancedImageReadParam param = new EnhancedImageReadParam();
    return narrowClone(param);
  }

  public Rectangle getDestinationRegion() {
    return destinationRegion;
  }

  /**
     * Performs a narrow clone of this {@link EnhancedImageReadParam}.
     * 
     * @param param
     *                the {@link EnhancedImageReadParam} instance containing the clone.
     * @return the narrow clone of this {@link ImageReadParam}.
     */
  protected Object narrowClone(final EnhancedImageReadParam param) {
    param.setDestination(this.getDestination());
    final int[] destBands = this.getDestinationBands();
    if (destBands != null) {
      param.setDestinationBands(destBands.clone());
    }
    final Point p = this.getDestinationOffset();
    if (p != null) {
      param.setDestinationOffset((Point)p.clone());
    }

    if (this.getDestinationType() != null) {
      param.setDestinationType(this.getDestinationType());
    }
    final int[] srcBands = this.getSourceBands();
    if (srcBands != null) {
      param.setSourceBands(srcBands.clone());
    }

    param.setSourceProgressivePasses(this.getSourceMinProgressivePass(),
      this.getSourceNumProgressivePasses());
    final Rectangle srcRegion = this.getSourceRegion();
    if (srcRegion != null) {
      param.setSourceRegion((Rectangle)srcRegion.clone());
    }

    param.setSourceSubsampling(this.getSourceXSubsampling(),
      this.getSourceYSubsampling(), this.getSubsamplingXOffset(),
      this.getSubsamplingYOffset());
    param.setController(this.getController());
    final Dimension d = this.getSourceRenderSize();
    if (d != null) {
      param.setSourceRenderSize((Dimension)d.clone());
    }

    final Rectangle destinationRegion = this.getDestinationRegion();
    if (destinationRegion != null) {
      param.setDestinationRegion((Rectangle)destinationRegion.clone());
    }
    return param;
  }

  public void setDestinationRegion(final Rectangle destinationRegion) {
    this.destinationRegion = (Rectangle)destinationRegion.clone();
  }
}
