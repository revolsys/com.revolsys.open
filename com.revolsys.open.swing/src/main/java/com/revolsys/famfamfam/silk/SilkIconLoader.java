package com.revolsys.famfamfam.silk;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.WeakCache;
import com.revolsys.util.OS;

public class SilkIconLoader {
  public static void addIcon(final List<Icon> icons, Icon icon,
    final boolean enabled) {
    if (icon != null) {
      if (!enabled) {
        icon = SilkIconLoader.getDisabledIcon(icon);
      }
      icons.add(icon);
    }
  }

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

  public static Icon getDisabledIcon(final Icon icon) {
    ImageIcon disabledIcon = null;
    final Reference<ImageIcon> iconReference = DISABLED_ICON_BY_ICON.get(icon);
    if (iconReference != null) {
      disabledIcon = iconReference.get();
    }
    if (disabledIcon == null) {
      disabledIcon = new ImageIcon(
        grayscale((BufferedImage)((ImageIcon)icon).getImage()));
      DISABLED_ICON_BY_ICON.put(icon, new WeakReference<>(disabledIcon));
    }
    return disabledIcon;
  }

  public static ImageIcon getDisabledIcon(final String imageName) {
    ImageIcon icon = null;
    Reference<ImageIcon> iconReference = DISABLED_ICON_CACHE.get(imageName);
    if (iconReference != null) {
      icon = iconReference.get();
    }
    if (icon == null) {
      final Image image = getDisabledImage(imageName);
      if (image == null) {
        return null;
      } else {
        icon = new ImageIcon(image);
        iconReference = new WeakReference<>(icon);
        DISABLED_ICON_CACHE.put(imageName, iconReference);
        DISABLED_ICON_BY_ICON.put(getIcon(imageName), iconReference);

      }
    }
    return icon;
  }

  public static Image getDisabledImage(final String imageName) {
    BufferedImage image = null;
    final Reference<BufferedImage> imageReference = DISABLED_IMAGE_CACHE.get(imageName);
    if (imageReference != null) {
      image = imageReference.get();
    }
    if (image == null) {
      image = getImage(imageName);
      image = grayscale(image);
      DISABLED_IMAGE_CACHE.put(imageName, new WeakReference<>(image));
    }
    return image;
  }

  public static ImageIcon getIcon(final String imageName) {
    ImageIcon icon = null;
    final Reference<ImageIcon> iconReference = ICON_CACHE.get(imageName);
    if (iconReference != null) {
      icon = iconReference.get();
    }
    if (icon == null) {
      final Image image = getImage(imageName);
      if (image == null) {
        return null;
      } else {
        icon = new ImageIcon(image);
        ICON_CACHE.put(imageName, new WeakReference<>(icon));
      }
    }
    return icon;
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
    BufferedImage image = null;
    final Reference<BufferedImage> imageReference = IMAGE_CACHE.get(imageName);
    if (imageReference != null) {
      image = imageReference.get();
    }
    if (image == null) {
      final String fileExtension = "png";
      image = getImage(imageName, fileExtension);
      IMAGE_CACHE.put(imageName, new WeakReference<>(image));
    }
    return image;
  }

  public static BufferedImage getImage(final String imageName,
    final String fileExtension) {
    BufferedImage image;
    final Class<?> clazz = SilkIconLoader.class;
    final String resourceName = RESOURCE_FOLDER + imageName + "."
        + fileExtension;
    InputStream in = clazz.getResourceAsStream(resourceName);
    if (in == null) {
      in = Thread.currentThread()
          .getContextClassLoader()
          .getResourceAsStream("images/" + imageName + "." + fileExtension);
    }
    image = getImage(in);
    return image;
  }

  public static BufferedImage grayscale(final BufferedImage original) {
    final int width = original.getWidth();
    final int height = original.getHeight();
    final int type = original.getType();
    final BufferedImage newImage = new BufferedImage(width, height, type);

    final int[] avgLUT = new int[766];
    for (int i = 0; i < avgLUT.length; i++) {
      avgLUT[i] = i / 3;
    }

    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        final int rgb = original.getRGB(i, j);
        final int alpha = rgb >> 24 & 0xff;
      final int red = rgb >> 16 & 0xff;
    final int green = rgb >> 8 & 0xff;
    final int blue = rgb & 0xff;

    int newRgb = red + green + blue;
    newRgb = avgLUT[newRgb];
    newRgb = WebColors.colorToRGB(alpha, newRgb, newRgb, newRgb);

    newImage.setRGB(i, j, newRgb);
      }
    }
    return newImage;
  }

  public static Icon merge(final List<Icon> icons, final int space) {
    int maxWidth = 0;
    int maxHeight = 0;
    int i = 0;
    for (final Icon icon : icons) {
      if (icon != null) {
        maxWidth += icon.getIconWidth();
        maxHeight = Math.max(maxHeight, icon.getIconHeight());
        i++;
      }
    }
    maxWidth += (i - 1) * space;

    if (maxWidth == 0) {
      return null;
    }
    if (maxHeight == 0) {
      return null;
    }

    final BufferedImage newImage = new BufferedImage(maxWidth, maxHeight,
      BufferedImage.TYPE_INT_ARGB);

    final Graphics g = newImage.createGraphics();
    int x = 0;
    for (final Icon icon : icons) {
      if (icon != null) {
        final Image image = ((ImageIcon)icon).getImage();
        final int iconWidth = icon.getIconWidth();
        final int iconHeight = icon.getIconHeight();
        g.drawImage(image, x, 0, iconWidth, iconHeight, null);
        x += iconWidth;
        x += space;
      }
    }

    return new ImageIcon(newImage);
  }

  public static final String RESOURCE_FOLDER = "/"
      + SilkIconLoader.class.getPackage().getName().replace(".", "/") + "/icons/";

  private static final Map<String, Reference<BufferedImage>> IMAGE_CACHE = new HashMap<>();

  private static final Map<String, Reference<BufferedImage>> DISABLED_IMAGE_CACHE = new HashMap<>();

  private static final Map<String, Reference<ImageIcon>> ICON_CACHE = new HashMap<>();

  private static final Map<String, Reference<ImageIcon>> DISABLED_ICON_CACHE = new HashMap<>();

  private static final Map<Icon, Reference<ImageIcon>> DISABLED_ICON_BY_ICON = new WeakCache<>();

}
