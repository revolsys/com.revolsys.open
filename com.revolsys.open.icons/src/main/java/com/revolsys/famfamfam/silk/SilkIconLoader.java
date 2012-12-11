package com.revolsys.famfamfam.silk;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class SilkIconLoader {
  private static final String RESOURCE_FOLDER = "/"
    + SilkIconLoader.class.getPackage().getName().replace(".", "/") + "/icons/";

  public static ImageIcon getIcon(final String imageName) {
    Image image = getImage(imageName);
    if (image == null) {
      return null;
    } else {
      ImageIcon icon = new ImageIcon(image);
      return icon;
    }
  }

  public static Cursor getCursor(final String imageName) {
    Image image = getImage(imageName);
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    return toolkit.createCustomCursor(image, new Point(16, 16), imageName);
  }

  public static BufferedImage getImage(final String imageName) {
    Class<?> clazz = SilkIconLoader.class;
    String resourceName = RESOURCE_FOLDER + imageName + ".png";
    InputStream in = clazz.getResourceAsStream(resourceName);
    if (in == null) {
      return null;
    } else {
      try {
        return ImageIO.read(in);
      } catch (IOException e) {
        return null;
      }
    }
  }
}
