package com.revolsys.data.io;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionFactory;
import com.revolsys.gis.io.Statistics;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Reader;

public class RecordDirectoryReader extends
  AbstractDirectoryReader<Record> implements RecordDefinitionFactory {

  private final Map<String, RecordDefinition> typePathMetaDataMap = new HashMap<String, RecordDefinition>();

  private Statistics statistics = new Statistics();

  public RecordDirectoryReader() {
  }

  protected void addMetaData(final RecordReader reader) {
    final RecordDefinition recordDefinition = reader.getRecordDefinition();
    if (recordDefinition != null) {
      final String path = recordDefinition.getPath();
      typePathMetaDataMap.put(path, recordDefinition);
    }
  }

  @Override
  protected Reader<Record> createReader(final Resource resource) {
    final IoFactoryRegistry registry = IoFactoryRegistry.getInstance();
    final String filename = resource.getFilename();
    final String extension = FileUtil.getFileNameExtension(filename);
    final RecordReaderFactory factory = registry.getFactoryByFileExtension(
      RecordReaderFactory.class, extension);
    final RecordReader reader = factory.createRecordReader(resource);
    addMetaData(reader);
    return reader;
  }

  @Override
  public RecordDefinition getRecordDefinition(final String path) {
    final RecordDefinition recordDefinition = typePathMetaDataMap.get(path);
    return recordDefinition;
  }

  public Statistics getStatistics() {
    return statistics;
  }

  /**
   * Get the next data object read by this reader.
   * 
   * @return The next Record.
   * @exception NoSuchElementException If the reader has no more data objects.
   */
  @Override
  public Record next() {
    final Record record = super.next();
    statistics.add(record);
    return record;
  }

  public void setStatistics(final Statistics statistics) {
    if (this.statistics != statistics) {
      this.statistics = statistics;
      statistics.connect();
    }
  }

}
