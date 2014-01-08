package com.revolsys.gis.data.io;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataFactory;
import com.revolsys.gis.io.Statistics;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Reader;

public class DataObjectDirectoryReader extends
  AbstractDirectoryReader<DataObject> implements DataObjectMetaDataFactory {

  private final Map<String, DataObjectMetaData> typePathMetaDataMap = new HashMap<String, DataObjectMetaData>();

  private final Statistics statistics = new Statistics();

  public DataObjectDirectoryReader() {
  }

  protected void addMetaData(final DataObjectReader reader) {
    final DataObjectMetaData metaData = reader.getMetaData();
    if (metaData != null) {
      final String path = metaData.getPath();
      typePathMetaDataMap.put(path, metaData);
    }
  }

  @Override
  protected Reader<DataObject> createReader(final Resource resource) {
    final IoFactoryRegistry registry = IoFactoryRegistry.getInstance();
    final String filename = resource.getFilename();
    final String extension = FileUtil.getFileNameExtension(filename);
    final DataObjectReaderFactory factory = registry.getFactoryByFileExtension(
      DataObjectReaderFactory.class, extension);
    final DataObjectReader reader = factory.createDataObjectReader(resource);
    addMetaData(reader);
    return reader;
  }

  @Override
  public DataObjectMetaData getMetaData(final String path) {
    final DataObjectMetaData metaData = typePathMetaDataMap.get(path);
    return metaData;
  }

  public Statistics getStatistics() {
    return statistics;
  }

  /**
   * Get the next data object read by this reader.
   * 
   * @return The next DataObject.
   * @exception NoSuchElementException If the reader has no more data objects.
   */
  @Override
  public DataObject next() {
    final DataObject record = super.next();
    statistics.add(record);
    return record;
  }

}
