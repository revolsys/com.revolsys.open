package com.revolsys.gis.csv;

import java.io.IOException;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.AbstractReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.FileUtil;

public class CsvReader extends AbstractReader<DataObject> implements
  DataObjectReader {
  public static DataObjectMetaData getMetaData(
    final Resource resource) {
    final CsvReader reader = (CsvReader)CsvReaderFactory.get()
      .createDataObjectReader(resource);
    final DataObjectMetaData metaData = reader.getMetaData();
    reader.close();
    return metaData;
  }

  public static DataObjectMetaData getMetaData(
    final String resourceName) {
    final ClassPathResource resource = new ClassPathResource(resourceName,
      CsvReader.class);
    return getMetaData(resource);
  }

  private DataObjectFactory dataObjectFactory;

  private CsvIterator iterator;

  private Resource resource;

  public CsvReader() {
    dataObjectFactory = new ArrayDataObjectFactory();
  }

  public CsvReader(
    final Resource resource) {
    this(resource, new ArrayDataObjectFactory());
  }

  public CsvReader(
    final Resource resource,
    final DataObjectFactory dataObjectFactory) {
    this.resource = resource;
    this.dataObjectFactory = dataObjectFactory;
  }

  public void close() {
    iterator.close();
  }

  public DataObjectFactory getDataObjectFactory() {
    return dataObjectFactory;
  }

  public DataObjectMetaData getMetaData() {
    return iterator().getMetaData();
  }

  @Override
  public Map<String, Object> getProperties() {
    return iterator().getProperties();
  }

  public CsvIterator iterator() {
    if (iterator == null) {
      try {
        iterator = new CsvIterator(resource, dataObjectFactory);
      } catch (final IOException e) {
        throw new IllegalArgumentException("Unable to create Iterator:"
          + e.getMessage(), e);
      }
    }
    return iterator;
  }

  public void setDataObjectFactory(
    final DataObjectFactory dataObjectFactory) {
    this.dataObjectFactory = dataObjectFactory;
  }

  public void setResource(
    final Resource resource) {
    this.resource = resource;
  }

}
