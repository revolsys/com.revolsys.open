package com.revolsys.io.shp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractDataObjectAndGeometryIoFactory;
import com.revolsys.data.io.DataObjectIteratorReader;
import com.revolsys.data.io.DataObjectReader;
import com.revolsys.data.io.DataObjectStore;
import com.revolsys.data.io.DataObjectStoreFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.DirectoryDataObjectStore;
import com.revolsys.io.Writer;
import com.revolsys.spring.OutputStreamResource;
import com.revolsys.spring.SpringUtil;

public class ShapefileIoFactory extends AbstractDataObjectAndGeometryIoFactory
  implements DataObjectStoreFactory {
  public ShapefileIoFactory() {
    super(ShapefileConstants.DESCRIPTION, true, true);
    addMediaTypeAndFileExtension(ShapefileConstants.MIME_TYPE,
      ShapefileConstants.FILE_EXTENSION);
    setSingleFile(false);
  }

  @Override
  public DataObjectReader createDataObjectReader(final Resource resource,
    final RecordFactory dataObjectFactory) {
    try {
      final ShapefileIterator iterator = new ShapefileIterator(resource,
        dataObjectFactory);
      return new DataObjectIteratorReader(iterator);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

  @Override
  public DataObjectStore createDataObjectStore(
    final Map<String, ? extends Object> connectionProperties) {
    final String url = (String)connectionProperties.get("url");
    final Resource resource = SpringUtil.getResource(url);
    final File directory = SpringUtil.getFile(resource);
    return new DirectoryDataObjectStore(directory,
      ShapefileConstants.FILE_EXTENSION);
  }

  @Override
  public Writer<Record> createDataObjectWriter(
    final RecordDefinition metaData, final Resource resource) {
    return new ShapefileDataObjectWriter(metaData, resource);
  }

  @Override
  public Writer<Record> createDataObjectWriter(final String baseName,
    final RecordDefinition metaData, final OutputStream outputStream,
    final Charset charset) {
    return createDataObjectWriter(metaData, new OutputStreamResource(baseName,
      outputStream));
  }

  @Override
  public Class<? extends DataObjectStore> getDataObjectStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return DataObjectStore.class;
  }

  @Override
  public List<String> getUrlPatterns() {
    return null;
  }
}
