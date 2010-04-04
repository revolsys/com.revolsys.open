package com.revolsys.gis.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import javax.xml.namespace.QName;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractReader;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.FileUtil;

public class CsvReader extends AbstractReader<DataObject> {
  public static DataObjectMetaData getMetaData(
    final InputStream in) {
    final CsvReader reader = (CsvReader)CsvReaderFactory.get().createDataObjectReader(in);
    final DataObjectMetaData metaData = reader.getMetaData();
    reader.close();
    return metaData;
  }

  public static DataObjectMetaData getMetaData(
    final String resourceName) {
    final InputStream in = CsvReader.class.getResourceAsStream(resourceName);
    return getMetaData(in);
  }

  private DataObjectFactory dataObjectFactory;

  private Reader in;

  private CsvIterator iterator;

  public CsvReader() {
    dataObjectFactory = new ArrayDataObjectFactory();
  }

  public CsvReader(
    final InputStream in,
    final DataObjectFactory dataObjectFactory) {
    this.in = new InputStreamReader(in, CsvConstants.CHARACTER_SET);
    this.dataObjectFactory = dataObjectFactory;
  }

  public void setResource(
    Resource resource)
    throws IOException {
    this.in = new InputStreamReader(resource.getInputStream(),
      CsvConstants.CHARACTER_SET);
  }

  public CsvReader(
    final Reader in,
    final DataObjectFactory dataObjectFactory) {
    this.in = in;
    this.dataObjectFactory = dataObjectFactory;
  }

  public void close() {
    FileUtil.closeSilent(in);
  }

  public DataObjectFactory getDataObjectFactory() {
    return dataObjectFactory;
  }

  public DataObjectMetaData getMetaData() {
    return iterator().getMetaData();
  }

  @Override
  public Map<QName, Object> getProperties() {
    return iterator().getProperties();
  }

  public CsvIterator iterator() {
    if (iterator == null) {
      try {
        iterator = new CsvIterator(in, dataObjectFactory);
      } catch (final IOException e) {
        throw new IllegalArgumentException("Unable to create Iterator:"
          + e.getMessage(), e);
      }
    }
    return iterator;
  }

}
