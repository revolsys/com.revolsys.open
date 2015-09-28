package com.revolsys.record.io;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.revolsys.gis.io.Statistics;
import com.revolsys.io.AbstractDirectoryReader;
import com.revolsys.io.Reader;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionFactory;
import com.revolsys.spring.resource.Resource;

public class RecordDirectoryReader extends AbstractDirectoryReader<Record>
  implements RecordDefinitionFactory {

  private Statistics statistics = new Statistics();

  private final Map<String, RecordDefinition> typePathRecordDefinitionMap = new HashMap<String, RecordDefinition>();

  public RecordDirectoryReader() {
  }

  protected void addRecordDefinition(final RecordReader reader) {
    final RecordDefinition recordDefinition = reader.getRecordDefinition();
    if (recordDefinition != null) {
      final String path = recordDefinition.getPath();
      this.typePathRecordDefinitionMap.put(path, recordDefinition);
    }
  }

  @Override
  public RecordDefinition getRecordDefinition(final String path) {
    final RecordDefinition recordDefinition = this.typePathRecordDefinitionMap.get(path);
    return recordDefinition;
  }

  public Statistics getStatistics() {
    return this.statistics;
  }

  @Override
  protected Reader<Record> newReader(final Resource resource) {
    final RecordReader reader = RecordReader.newRecordReader(resource);
    addRecordDefinition(reader);
    return reader;
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
    this.statistics.add(record);
    return record;
  }

  public void setStatistics(final Statistics statistics) {
    if (this.statistics != statistics) {
      this.statistics = statistics;
      statistics.connect();
    }
  }

}
