package com.revolsys.io.gpx;

import java.io.IOException;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryReaderFactory;
import com.revolsys.gis.data.io.DataObjectIterator;
import com.revolsys.gis.data.io.DataObjectIteratorReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;

public class GpxReaderFactory extends
  AbstractDataObjectAndGeometryReaderFactory {
  public GpxReaderFactory() {
    super("GPS Exchange Format", false);
    addMediaTypeAndFileExtension(GpxConstants.MEDIA_TYPE,
      GpxConstants.FILE_EXTENSION);
    setCustomAttributionSupported(false);
  }

  public DataObjectReader createDataObjectReader(
    final DataObjectMetaData metaData,
    final Resource resource,
    final DataObjectFactory factory) {
    throw new UnsupportedOperationException();
  }

  /**
   * Create a reader for the file using the specified data object factory.
   * 
   * @param inputStream The file to read.
   * @param factory The factory used to create data objects.
   * @return The reader for the file.
   */
  public DataObjectReader createDataObjectReader(
    final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    try {
      final DataObjectIterator iterator = new GpxIterator(resource,
        dataObjectFactory, null);
      return new DataObjectIteratorReader(iterator);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to open resource " + resource,
        e);
    }
  }
}
