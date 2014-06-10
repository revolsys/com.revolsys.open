/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
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
package it.geosolutions.imageio.stream.input.spi;

import it.geosolutions.imageio.utilities.ImageIOUtilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.stream.FileCacheImageInputStream;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of an {@link ImageInputStreamSpi} for instantiating an
 * {@link ImageInputStream} capable of connecting to a {@link URL}.
 * 
 * <p>
 * I basically rely on the existence of something to read from a {@link File} in
 * case this {@link URL} points to a {@link File}, otherwise I try to open up
 * an {@link InputStream} and I ask the
 * {@link ImageIO#createImageInputStream(Object)} to create an
 * {@link ImageInputStream} for it.
 * 
 * 
 * @see ImageInputStream
 * @see ImageInputStreamSpi
 * @see ImageIO#createImageInputStream(Object)
 * 
 * @author Simone Giannecchini, GeoSolutions
 */
public class URLImageInputStreamSpi extends ImageInputStreamSpi {
  /** Logger. */
  private final static Logger LOGGER = LoggerFactory.getLogger("it.geosolutions.imageio.stream.input");

  private static final String vendorName = "GeoSolutions";

  private static final String version = "1.0";

  private static final Class<?> inputClass = URL.class;

  /**
   * Default constructor for a {@link URLImageInputStreamSpi};
   */
  public URLImageInputStreamSpi() {
    super(vendorName, version, inputClass);
  }

  /**
   * 
   * @see javax.imageio.spi.ImageInputStreamSpi#createInputStreamInstance(java.lang.Object,
   *      boolean, java.io.File)
   */
  @Override
  public ImageInputStream createInputStreamInstance(final Object input,
    final boolean useCache, final File cacheDir) {
    // is it a URL?
    if (!(input instanceof URL)) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("The provided input is not a valid URL.");
      }
      return null;
    }

    try {
      // URL that points to a file?
      final URL sourceURL = ((URL)input);
      final File tempFile = ImageIOUtilities.urlToFile(sourceURL);
      if (tempFile.exists() && tempFile.isFile() && tempFile.canRead()) {
        return new FileImageInputStream(tempFile);
      }

      // URL that does NOT points to a file, let's open up a stream
      if (useCache) {
        return new MemoryCacheImageInputStream(sourceURL.openStream());
      } else {
        return new FileCacheImageInputStream(sourceURL.openStream(), cacheDir);
      }

    } catch (final IOException e) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(e.getLocalizedMessage(), e);
      }
      return null;
    }
  }

  /**
   * @see ImageInputStreamSpi#getDescription(Locale).
   */
  @Override
  public String getDescription(final Locale locale) {
    return "Service provider that helps connecting to the object pointed by a URL";
  }
}
