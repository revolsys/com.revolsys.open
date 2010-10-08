package com.revolsys.gis.gpx.io;

import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.gis.data.io.DataObjectIterator;
import com.revolsys.gis.data.io.DataObjectIteratorReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObjectFactory;

public class GpxReaderFactory extends AbstractDataObjectReaderFactory {
  /** The factory instance. */
  public static final GpxReaderFactory INSTANCE = new GpxReaderFactory();

  /**
   * Get the factory instance.
   * 
   * @return The instance.
   */
  public static GpxReaderFactory get() {
    return INSTANCE;
  }

  public GpxReaderFactory() {
    super("GPS Exchange Format", false);
    addMediaTypeAndFileExtension(GpxConstants.MEDIA_TYPE,
      GpxConstants.FILE_EXTENSION);
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
      DataObjectIterator iterator = new GpxIterator(new InputStreamReader(resource.getInputStream()), dataObjectFactory, null);
      return new DataObjectIteratorReader(iterator);
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to open resource " + resource,
        e);
    }
  }
}
