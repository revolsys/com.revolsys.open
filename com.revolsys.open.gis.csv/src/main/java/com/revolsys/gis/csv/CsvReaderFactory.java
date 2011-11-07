package com.revolsys.gis.csv;

import java.io.IOException;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryReaderFactory;
import com.revolsys.gis.data.io.DataObjectIteratorReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObjectFactory;

public class CsvReaderFactory extends AbstractDataObjectAndGeometryReaderFactory {

  /** The factory instance. */
  public static final CsvReaderFactory INSTANCE = new CsvReaderFactory();

  /**
   * Get the factory instance.
   * 
   * @return The instance.
   */
  public static CsvReaderFactory get() {
    return INSTANCE;
  }

  public CsvReaderFactory() {
    super(CsvConstants.DESCRIPTION, false);
    addMediaTypeAndFileExtension(com.revolsys.csv.CsvConstants.MEDIA_TYPE,
      com.revolsys.csv.CsvConstants.FILE_EXTENSION);
  }

  public DataObjectReader createDataObjectReader(
    final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    try {
      final CsvIterator iterator = new CsvIterator(resource, dataObjectFactory);
      return new DataObjectIteratorReader(iterator);
    } catch (IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }
}
