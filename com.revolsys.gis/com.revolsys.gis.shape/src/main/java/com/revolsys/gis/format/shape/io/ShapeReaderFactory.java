package com.revolsys.gis.format.shape.io;

import org.springframework.core.io.Resource;

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

  public Reader<DataObject> createDataObjectReader(
    final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    return new ShapeReader(resource, dataObjectFactory);
  }
}
