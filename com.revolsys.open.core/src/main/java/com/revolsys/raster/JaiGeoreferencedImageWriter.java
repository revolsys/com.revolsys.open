package com.revolsys.raster;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.media.jai.JAI;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.spring.resource.Resource;

public class JaiGeoreferencedImageWriter implements GeoreferencedImageWriter {

  private final Resource resource;

  private final String formatName;

  public JaiGeoreferencedImageWriter(final Resource resource, final String formatName) {
    this.resource = resource;
    this.formatName = formatName;
  }

  @Override
  public void write(final GeoreferencedImage image) {
    try (
      OutputStream out = this.resource.newBufferedOutputStream()) {
      final RenderedImage renderedImage = image.getRenderedImage();
      // ImageIO.write(renderedImage, this.formatName, out);
      final String fileName = this.resource.getFile().getAbsolutePath();
      JAI.create("filestore", renderedImage, fileName, this.formatName);
    } catch (final IOException e) {
      throw Exceptions.wrap("Unable to write: " + this.resource, e);
    }
  }
}
