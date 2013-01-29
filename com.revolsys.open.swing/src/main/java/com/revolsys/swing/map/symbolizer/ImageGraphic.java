package com.revolsys.swing.map.symbolizer;

import java.awt.image.BufferedImage;

public class ImageGraphic extends AbstractGraphicSymbol {
  private BufferedImage image;

  public ImageGraphic() {
  }

  public ImageGraphic(final BufferedImage image) {
    this.image = image;
  }

  public BufferedImage getImage() {
    return image;
  }

  public void setImage(final BufferedImage image) {
    this.image = image;
  }

}
