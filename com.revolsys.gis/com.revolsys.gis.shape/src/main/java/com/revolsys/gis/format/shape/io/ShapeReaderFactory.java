package com.revolsys.gis.format.shape.io;

import java.io.File;

import com.revolsys.gis.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;

public class ShapeReaderFactory extends AbstractDataObjectReaderFactory {

  /** The factory instance. */
  public static final ShapeReaderFactory INSTANCE = new ShapeReaderFactory();

  /**
   * Get the factory instance.
   * 
   * @return The instance.
   */
  public static ShapeReaderFactory get() {
    return INSTANCE;
  }

  public ShapeReaderFactory() {
    super("ESRI Shapefile");
    addMediaTypeAndFileExtension("application/x-shp", "shp");
  }

  @Override
  public Reader<DataObject> createDataObjectReader(
    final File file,
    final DataObjectFactory dataObjectFactory) {
    return new ShapeReader(file, dataObjectFactory);
  }

  public Reader<DataObject> createDataObjectReader(
    java.io.Reader in,
    DataObjectFactory factory) {
    throw new UnsupportedOperationException();
  }
}
