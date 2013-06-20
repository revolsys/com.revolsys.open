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
    final Image image = getImage(imageName);
    if (image == null) {
      return null;
    } else {
      final Toolkit toolkit = Toolkit.getDefaultToolkit();
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

  public static ImageIcon getIconWidthBadge(final String imageName,
    final String smallImageName) {
    final Image image = getImageWidthBadge(imageName, smallImageName);
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
        return ImageIO.read(in);
      } catch (final IOException e) {
      }
    }
    return null;

  }

  public static BufferedImage getImage(final String imageName) {
    return getImageWithPrefix("", imageName);
  }

  public static Image getImageWidthBadge(final String imageName,
    final String smallImageName) {
    final BufferedImage image = getImage(imageName);
    final BufferedImage smallImage = getSmallImage(smallImageName);
    if (image == null) {
      return smallImage;
    } else if (smallImage == null) {
      return image;
    } else {
      final int w = Math.max(image.getWidth(), smallImage.getWidth());
      final int h = Math.max(image.getHeight(), smallImage.getHeight());
      final BufferedImage combined = new BufferedImage(w, h,
        BufferedImage.TYPE_INT_ARGB);

      final Graphics g = combined.getGraphics();
      g.drawImage(image, 0, 0, null);
      g.drawImage(smallImage, 0, 0, null);
      return combined;
    }
  }

  private static BufferedImage getImageWithPrefix(final String prefix,
    final String imageName) {
    final Class<?> clazz = SilkIconLoader.class;
    final String resourceName = RESOURCE_FOLDER + prefix + imageName + ".png";
    InputStream in = clazz.getResourceAsStream(resourceName);
    if (in == null) {
      in = Thread.currentThread()
        .getContextClassLoader()
        .getResourceAsStream("images/" + imageName + ".png");
    }
    return getImage(in);
  }

  public static ImageIcon getSmallIcon(final String imageName) {
    final Image image = getSmallImage(imageName);
    if (image == null) {
      return null;
    } else {
      final ImageIcon icon = new ImageIcon(image);
      return icon;
    }
  }

  public static BufferedImage getSmallImage(final String imageName) {
    return getImageWithPrefix("small/", imageName);
  }
}
