package com.revolsys.gis.format.xbase.io;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;

public class XBaseReaderFactory extends AbstractDataObjectReaderFactory {

  /** The factory instance. */
  public static final XBaseReaderFactory INSTANCE = new XBaseReaderFactory();

  /**
   * Get the factory instance.
   * 
   * @return The instance.
   */
  public static XBaseReaderFactory get() {
    return INSTANCE;
  }

  public XBaseReaderFactory() {
    super("dBase");
    addMediaTypeAndFileExtension("application/dbase", "dbf");
    addMediaTypeAndFileExtension("application/dbf", "dbf");
  }

  public Reader<DataObject> createDataObjectReader(
    java.io.Reader in,
    DataObjectFactory factory) {
    return null;
  }
  
  public Reader<DataObject> createDataObjectReader(
    final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    return new XBaseReader(resource, dataObjectFactory);
  }


}
