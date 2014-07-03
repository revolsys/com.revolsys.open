package com.revolsys.io.tsv;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractDataObjectIoFactory;
import com.revolsys.data.io.DataObjectIteratorReader;
import com.revolsys.data.io.DataObjectReader;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.Writer;
import com.revolsys.io.csv.CsvDataObjectIterator;
import com.revolsys.io.csv.CsvDataObjectWriter;

public class TsvDataObjectIoFactory extends AbstractDataObjectIoFactory {
  public TsvDataObjectIoFactory() {
    super(TsvConstants.DESCRIPTION, false, true, true);
    addMediaTypeAndFileExtension(TsvConstants.MEDIA_TYPE,
      TsvConstants.FILE_EXTENSION);
  }

  @Override
  public DataObjectReader createDataObjectReader(final Resource resource,
    final RecordFactory dataObjectFactory) {
    final CsvDataObjectIterator iterator = new CsvDataObjectIterator(resource,
      dataObjectFactory, TsvConstants.FIELD_SEPARATOR);
    return new DataObjectIteratorReader(iterator);
  }

  @Override
  public Writer<Record> createDataObjectWriter(final String baseName,
    final RecordDefinition metaData, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream,
      charset);

    return new CsvDataObjectWriter(metaData, writer,
      TsvConstants.FIELD_SEPARATOR, true);
  }
}
