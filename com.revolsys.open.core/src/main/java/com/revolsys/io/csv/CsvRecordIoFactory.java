package com.revolsys.io.csv;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractRecordIoFactory;
import com.revolsys.data.io.RecordIteratorReader;
import com.revolsys.data.io.RecordReader;
import com.revolsys.data.io.RecordStore;
import com.revolsys.data.io.RecordStoreFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.DirectoryRecordStore;
import com.revolsys.io.Writer;
import com.revolsys.spring.SpringUtil;

public class CsvRecordIoFactory extends AbstractRecordIoFactory
  implements RecordStoreFactory {
  public CsvRecordIoFactory() {
    super(CsvConstants.DESCRIPTION, false, true, true);
    addMediaTypeAndFileExtension(CsvConstants.MEDIA_TYPE,
      CsvConstants.FILE_EXTENSION);
  }

  @Override
  public RecordReader createRecordReader(final Resource resource,
    final RecordFactory dataObjectFactory) {
    final CsvRecordIterator iterator = new CsvRecordIterator(resource,
      dataObjectFactory);
    return new RecordIteratorReader(iterator);
  }

  @Override
  public RecordStore createRecordStore(
    final Map<String, ? extends Object> connectionProperties) {
    final String url = (String)connectionProperties.get("url");
    final Resource resource = SpringUtil.getResource(url);
    final File directory = SpringUtil.getFile(resource);
    return new DirectoryRecordStore(directory, "csv");
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition metaData, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream,
      charset);

    return new CsvRecordWriter(metaData, writer);
  }

  @Override
  public Class<? extends RecordStore> getRecordStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return RecordStore.class;
  }

  @Override
  public List<String> getUrlPatterns() {
    // TODO Auto-generated method stub
    return null;
  }
}
