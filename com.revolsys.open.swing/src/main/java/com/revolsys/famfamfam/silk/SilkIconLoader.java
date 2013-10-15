package com.revolsys.famfamfam.silk;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.revolsys.util.OS;

public class SilkIconLoader {
  private static final String RESOURCE_FOLDER = "/"
    + SilkIconLoader.class.getPackage().getName().replace(".", "/") + "/icons/";

  public static Cursor getCursor(final String imageName) {
    return getCursor(imageName, 0, 0);

  }

  public static Cursor getCursor(final String imageName, final int delta) {
    return getCursor(imageName, delta, delta);
  }

  public static Cursor getCursor(final String imageName, final int dx,
    final int dy) {
    Image image = getImage(imageName);
    if (image == null) {
      return null;
    } else {
      final Toolkit toolkit = Toolkit.getDefaultToolkit();
      if (OS.isWindows()) {
        final BufferedImage newImage = new BufferedImage(32, 32,
          BufferedImage.TYPE_INT_ARGB);
        final Graphics graphics = newImage.getGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        image = newImage;
      }
      return toolkit.createCustomCursor(image, new Point(dx, dy), imageName);
    }
  }

  public static ImageIcon getIcon(final String imageName) {
    final Image image = getImage(imageName);
    if (image == null) {
      return null;
    } else {
      final ImageIcon icon = new ImageIcon(image);
      return icon;
    }
  }

  protected static BufferedImage getImage(final InputStream in) {
    if (in != null) {
      try {
        final BufferedImage image = ImageIO.read(in);
        final BufferedImage convertedImg = new BufferedImage(image.getWidth(),
          image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final Graphics graphics = convertedImg.getGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return convertedImg;
      } catch (final IOException e) {
      }
    }
    return null;

  }

  public static BufferedImage getImage(final String imageName) {
    final Class<?> clazz = SilkIconLoader.class;
    final String resourceName = RESOURCE_FOLDER + imageName + ".png";
    InputStream in = clazz.getResourceAsStream(resourceName);
    if (in == null) {
      in = Thread.currentThread()
        .getContextClassLoader()
        .getResourceAsStream("images/" + imageName + ".png");
    }
    return getImage(in);
  }

}
