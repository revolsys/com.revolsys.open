package com.revolsys.gis.csv;

import com.revolsys.gis.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;

public class CsvReaderFactory extends AbstractDataObjectReaderFactory {

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
    super(CsvConstants.DESCRIPTION);
    addMediaTypeAndFileExtension(com.revolsys.csv.CsvConstants.MEDIA_TYPE,
      com.revolsys.csv.CsvConstants.FILE_EXTENSION);
  }

  public Reader<DataObject> createDataObjectReader(
    final java.io.Reader in,
    final DataObjectFactory dataObjectFactory) {
    return new CsvReader(in, dataObjectFactory);

  }

}
