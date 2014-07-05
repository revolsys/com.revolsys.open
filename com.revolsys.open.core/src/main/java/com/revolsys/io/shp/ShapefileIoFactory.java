package com.revolsys.io.shp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractRecordAndGeometryIoFactory;
import com.revolsys.data.io.RecordIteratorReader;
import com.revolsys.data.io.RecordReader;
import com.revolsys.data.io.RecordStore;
import com.revolsys.data.io.RecordStoreFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.DirectoryRecordStore;
import com.revolsys.io.Writer;
import com.revolsys.spring.OutputStreamResource;
import com.revolsys.spring.SpringUtil;

public class ShapefileIoFactory extends AbstractRecordAndGeometryIoFactory
  implements RecordStoreFactory {
  public ShapefileIoFactory() {
    super(ShapefileConstants.DESCRIPTION, true, true);
    addMediaTypeAndFileExtension(ShapefileConstants.MIME_TYPE,
      ShapefileConstants.FILE_EXTENSION);
    setSingleFile(false);
  }

  @Override
  public RecordReader createRecordReader(final Resource resource,
    final RecordFactory recordFactory) {
    try {
      final ShapefileIterator iterator = new ShapefileIterator(resource,
        recordFactory);
      return new RecordIteratorReader(iterator);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

  @Override
  public RecordStore createRecordStore(
    final Map<String, ? extends Object> connectionProperties) {
    final String url = (String)connectionProperties.get("url");
    final Resource resource = SpringUtil.getResource(url);
    final File directory = SpringUtil.getFile(resource);
    return new DirectoryRecordStore(directory,
      ShapefileConstants.FILE_EXTENSION);
  }

  @Override
  public Writer<Record> createRecordWriter(
    final RecordDefinition recordDefinition, final Resource resource) {
    return new ShapefileRecordWriter(recordDefinition, resource);
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    return createRecordWriter(recordDefinition, new OutputStreamResource(baseName,
      outputStream));
  }

  @Override
  public Class<? extends RecordStore> getRecordStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return RecordStore.class;
  }

  @Override
  public List<String> getUrlPatterns() {
    return null;
  }
}
