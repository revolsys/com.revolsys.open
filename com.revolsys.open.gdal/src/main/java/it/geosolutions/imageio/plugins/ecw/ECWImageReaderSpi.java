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
package it.geosolutions.imageio.plugins.ecw;

import it.geosolutions.imageio.gdalframework.GDALImageReaderSpi;
import it.geosolutions.imageio.gdalframework.GDALUtilities;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.gdal.gdal.Dataset;
import org.gdal.gdalconst.gdalconst;

import com.revolsys.util.ManifestUtil;

/**
 * Service provider interface for the ECW Image
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @author Daniele Romagnoli, GeoSolutions.
 */
public class ECWImageReaderSpi extends GDALImageReaderSpi {

  private static final String DESCRIPTION = "ECW Image Reader";

  private static final String[] extraImageMetadataFormatClassNames = {
    null
  };

  private static final String[] extraImageMetadataFormatNames = {
    null
  };

  private static final String[] extraStreamMetadataFormatClassNames = {
    null
  };

  private static final String[] extraStreamMetadataFormatNames = {
    null
  };

  private static final String[] formatNames = {
    "ECW", "ECWP", "ecw", "ecwp"
  };

  private static final String[] MIMETypes = {
    "image/ecw"
  };

  private static final String READER_CLASS_NAME = "it.geosolutions.imageio.plugins.ecw.ECWImageReader";

  private static final String[] suffixes = {
    "ecw"
  };

  // ImageMetadataFormatNames and ImageMetadataFormatClassNames
  private static final boolean supportsStandardImageMetadataFormat = false;

  // StreamMetadataFormatNames and StreamMetadataFormatClassNames
  private static final boolean supportsStandardStreamMetadataFormat = false;

  private static final String vendorName = "GeoSolutions";

  private static String VERSION = ManifestUtil.getImplementationVersion("RS GDAL");

  // writerSpiNames
  private static final String[] wSN = {/* "javax.imageio.plugins.ecw.ECWImageWriterSpi" */
    null
  };

  public ECWImageReaderSpi() {
    super(vendorName, VERSION, formatNames,
      suffixes,
      MIMETypes,
      READER_CLASS_NAME,
      new Class[] {
        ImageInputStream.class, File.class, ECWPImageInputStream.class
      },
      wSN, // writer Spi Names
      supportsStandardStreamMetadataFormat, null, null,
      extraStreamMetadataFormatNames, extraStreamMetadataFormatClassNames,
      supportsStandardImageMetadataFormat, null, null,
      extraImageMetadataFormatNames, extraImageMetadataFormatClassNames,
      Collections.singletonList("ECW"));

  }

  /**
   * This method checks if the provided input can be decoded from this SPI
   */
  @Override
  public boolean canDecodeInput(final Object input) throws IOException {
    if (input instanceof ECWPImageInputStream) {
      final String ecwp = ((ECWPImageInputStream)input).getECWPLink();
      boolean isDecodeable = false;
      if (ecwp != null) {
        final Dataset ds = GDALUtilities.acquireDataSet(ecwp,
          gdalconst.GA_ReadOnly);
        if (ds != null) {
          isDecodeable = isDecodable(ds);
        }
      }
      return isDecodeable;
    } else {
      return super.canDecodeInput(input);
    }

  }

  /**
   * Returns an instance of the ECWImageReader
   * 
   * @see javax.imageio.spi.ImageReaderSpi#createReaderInstance(java.lang.Object)
   */
  @Override
  public ImageReader createReaderInstance(final Object source)
    throws IOException {
    return new ECWImageReader(this);
  }

  /**
   * @see javax.imageio.spi.IIOServiceProvider#getDescription(java.util.Locale)
   */
  @Override
  public String getDescription(final Locale locale) {
    return DESCRIPTION;
  }

}
