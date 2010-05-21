package com.revolsys.gis.ecsv.io;

import java.io.IOException;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectAndGeometryReaderFactory;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;

public class EcsvDataObjectReaderFactory extends
  AbstractDataObjectAndGeometryReaderFactory {

  /** The factory instance. */
  public static final EcsvDataObjectReaderFactory INSTANCE = new EcsvDataObjectReaderFactory();

  public EcsvDataObjectReaderFactory() {
    super(EcsvConstants.DESCRIPTION);
    addMediaTypeAndFileExtension(EcsvConstants.MEDIA_TYPE,
      EcsvConstants.FILE_EXTENSION);
  }

  /**
   * Create a reader for the file using the specified data object factory.
   * 
   * @param file The file to read.
   * @param factory The factory used to create data objects.
   * @return The reader for the file.
   */
  public Reader<DataObject> createDataObjectReader(
    Resource resource,
    DataObjectFactory dataObjectFactory) {
    try {
      return new EcsvReader(resource, dataObjectFactory);
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to open resource " + resource,
        e);
    }
  }
}
