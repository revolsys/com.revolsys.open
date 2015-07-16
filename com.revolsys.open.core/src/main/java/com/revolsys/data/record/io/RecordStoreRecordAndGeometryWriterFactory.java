package com.revolsys.data.record.io;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.spring.SpringUtil;

public class RecordStoreRecordAndGeometryWriterFactory extends AbstractIoFactoryWithCoordinateSystem
  implements RecordWriterFactory {

  public RecordStoreRecordAndGeometryWriterFactory(final String name, final String mediaType,
    final boolean geometrySupported, final boolean customAttributionSupported,
    final Iterable<String> fileExtensions) {
    super(name);
    for (final String fileExtension : fileExtensions) {
      addMediaTypeAndFileExtension(mediaType, fileExtension);
    }
  }

  public RecordStoreRecordAndGeometryWriterFactory(final String name, final String mediaType,
    final boolean geometrySupported, final boolean customAttributionSupported,
    final String... fileExtensions) {
    this(name, mediaType, geometrySupported, customAttributionSupported,
      Arrays.asList(fileExtensions));
  }

  @Override
  public RecordWriter createRecordWriter(final RecordDefinition recordDefinition,
    final Resource resource) {
    final File file = SpringUtil.getFile(resource);
    final RecordStore recordStore = RecordStoreFactoryRegistry.createRecordStore(file);
    if (recordStore == null) {
      return null;
    } else {
      recordStore.initialize();
      return new RecordStoreRecordWriter(recordStore);
    }
  }

  @Override
  public RecordWriter createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    throw new UnsupportedOperationException("Writing to a stream not currently supported");
  }
}
