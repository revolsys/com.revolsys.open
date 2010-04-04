package com.revolsys.gis.gpx.io;

import com.revolsys.gis.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObject;
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
    super("GPS Exchange Format");
    addMediaTypeAndFileExtension(GpxConstants.MEDIA_TYPE, GpxConstants.FILE_EXTENSION);
  }

  /**
   * Create a reader for the file using the specified data object factory.
   * 
   * @param inputStream The file to read.
   * @param factory The factory used to create data objects.
   * @return The reader for the file.
   */
  public Reader<DataObject> createDataObjectReader(
    final java.io.Reader reader,
    final DataObjectFactory dataObjectFactory) {
    return new GpxReader(reader, dataObjectFactory);
  }

}
