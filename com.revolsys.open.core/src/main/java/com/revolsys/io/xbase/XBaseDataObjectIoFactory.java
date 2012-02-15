package com.revolsys.io.xbase;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectIoFactory;
import com.revolsys.gis.data.io.DataObjectIteratorReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.Writer;
import com.revolsys.spring.OutputStreamResource;

public class XBaseDataObjectIoFactory extends AbstractDataObjectIoFactory {
  public XBaseDataObjectIoFactory() {
    super("D-Base", true, false, true);
    addMediaTypeAndFileExtension("application/dbase", "dbf");
    addMediaTypeAndFileExtension("application/dbf", "dbf");
  }

  public DataObjectReader createDataObjectReader(
    final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    try {
      final XbaseIterator iterator = new XbaseIterator(resource,
        dataObjectFactory);

      return new DataObjectIteratorReader(iterator);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

  @Override
  public Writer<DataObject> createDataObjectWriter(
    final DataObjectMetaData metaData,
    final Resource resource) {
    return new XbaseDataObjectWriter(metaData, resource);
  }

  public Writer<DataObject> createDataObjectWriter(
    final String baseName,
    final DataObjectMetaData metaData,
    final OutputStream outputStream,
    final Charset charset) {
    return createDataObjectWriter(metaData, new OutputStreamResource(baseName,
      outputStream));
  }

}
