package com.revolsys.swing;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;

import com.revolsys.spring.resource.ClassPathResource;

public class Fonts {
  private static boolean initialized = false;

  private static Object SYNC = new Object();

  private static void init() {
    synchronized (SYNC) {
      if (!initialized) {
        try {
          final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
          final ClassPathResource resource = new ClassPathResource(
            "com/revolsys/fonts/fontawesome-webfont.ttf");
          final InputStream inputStream = resource.newInputStream();
          final Font font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
          environment.registerFont(font);
        } catch (IOException | FontFormatException e) {
        }
        initialized = true;
      }
    }
  }

  public static Font newFont(final String name, final int style, final int size) {
    init();
    return new Font(name, style, size);
  }

}
