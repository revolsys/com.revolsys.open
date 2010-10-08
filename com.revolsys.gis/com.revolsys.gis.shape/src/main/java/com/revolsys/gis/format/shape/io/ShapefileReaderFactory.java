package com.revolsys.gis.format.shape.io;

import java.io.IOException;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.gis.data.io.DataObjectIteratorReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObjectFactory;

public class ShapefileReaderFactory extends AbstractDataObjectReaderFactory {
  public ShapefileReaderFactory() {
    super(ShapefileConstants.DESCRIPTION, true);
    addMediaTypeAndFileExtension(ShapefileConstants.MIME_TYPE,
      ShapefileConstants.FILE_EXTENSION);
  }

  public DataObjectReader createDataObjectReader(
    final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    try {
      ShapefileIterator iterator = new ShapefileIterator(resource,
        dataObjectFactory);
      return new DataObjectIteratorReader(iterator);
    } catch (IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }
}
