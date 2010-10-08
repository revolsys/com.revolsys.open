package com.revolsys.gis.format.xbase.io;

import java.io.IOException;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.gis.data.io.DataObjectIteratorReader;
import com.revolsys.gis.data.io.DataObjectReader;
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
    super("dBase", true);
    addMediaTypeAndFileExtension("application/dbase", "dbf");
    addMediaTypeAndFileExtension("application/dbf", "dbf");
  }

  
  public DataObjectReader createDataObjectReader(
    final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    try {
      XbaseIterator iterator = new XbaseIterator(resource, dataObjectFactory);

      return new DataObjectIteratorReader(iterator);
    } catch (IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }


}
