package com.revolsys.io.xbase;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractDataObjectIoFactory;
import com.revolsys.data.io.DataObjectIteratorReader;
import com.revolsys.data.io.DataObjectReader;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.Writer;
import com.revolsys.spring.OutputStreamResource;

public class XBaseDataObjectIoFactory extends AbstractDataObjectIoFactory {
  public XBaseDataObjectIoFactory() {
    super("D-Base", true, false, true);
    addMediaTypeAndFileExtension("application/dbase", "dbf");
    addMediaTypeAndFileExtension("application/dbf", "dbf");
  }

  @Override
  public DataObjectReader createDataObjectReader(final Resource resource,
    final RecordFactory dataObjectFactory) {
    try {
      final XbaseIterator iterator = new XbaseIterator(resource,
        dataObjectFactory);

      return new DataObjectIteratorReader(iterator);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

  @Override
  public Writer<Record> createDataObjectWriter(
    final RecordDefinition metaData, final Resource resource) {
    return new XbaseDataObjectWriter(metaData, resource);
  }

  @Override
  public Writer<Record> createDataObjectWriter(final String baseName,
    final RecordDefinition metaData, final OutputStream outputStream,
    final Charset charset) {
    return createDataObjectWriter(metaData, new OutputStreamResource(baseName,
      outputStream));
  }

}
