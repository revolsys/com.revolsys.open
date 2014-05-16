package com.revolsys.io.tsv;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractDataObjectIoFactory;
import com.revolsys.gis.data.io.DataObjectIteratorReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
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
    final DataObjectFactory dataObjectFactory) {
    final CsvDataObjectIterator iterator = new CsvDataObjectIterator(resource,
      dataObjectFactory, TsvConstants.FIELD_SEPARATOR);
    return new DataObjectIteratorReader(iterator);
  }

  @Override
  public Writer<DataObject> createDataObjectWriter(final String baseName,
    final DataObjectMetaData metaData, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream,
      charset);

    return new CsvDataObjectWriter(metaData, writer,
      TsvConstants.FIELD_SEPARATOR, true);
  }
}
