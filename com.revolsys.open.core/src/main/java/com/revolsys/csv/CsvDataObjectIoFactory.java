package com.revolsys.csv;

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

public class CsvDataObjectIoFactory extends AbstractDataObjectIoFactory {
  /** The factory instance. */
  public static final CsvDataObjectIoFactory INSTANCE = new CsvDataObjectIoFactory();

  public CsvDataObjectIoFactory() {
    super(CsvConstants.DESCRIPTION, false);
    addMediaTypeAndFileExtension(CsvConstants.MEDIA_TYPE,
      CsvConstants.FILE_EXTENSION);
  }

  public Writer<DataObject> createDataObjectWriter(String baseName,
    DataObjectMetaData metaData, OutputStream outputStream, Charset charset) {
    return new CsvDataObjectWriter(metaData, new OutputStreamWriter(
      outputStream, charset));
  }

  public DataObjectReader createDataObjectReader(final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    final CsvDataObjectIterator iterator = new CsvDataObjectIterator(resource,
      dataObjectFactory);
    return new DataObjectIteratorReader(iterator);
  }
}
