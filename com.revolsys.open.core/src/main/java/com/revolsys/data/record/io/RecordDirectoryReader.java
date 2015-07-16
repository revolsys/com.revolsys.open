package com.revolsys.data.record.io;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractDirectoryReader;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionFactory;
import com.revolsys.gis.io.Statistics;
import com.revolsys.io.Reader;

public class RecordDirectoryReader extends AbstractDirectoryReader<Record>
  implements RecordDefinitionFactory {

  private final Map<String, RecordDefinition> typePathRecordDefinitionMap = new HashMap<String, RecordDefinition>();

  private Statistics statistics = new Statistics();

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
  protected Reader<Record> createReader(final Resource resource) {
    final RecordReader reader = RecordReader.create(resource);
    addRecordDefinition(reader);
    return reader;
  }

  @Override
  public RecordDefinition getRecordDefinition(final String path) {
    final RecordDefinition recordDefinition = this.typePathRecordDefinitionMap.get(path);
    return recordDefinition;
  }

  public Statistics getStatistics() {
    return this.statistics;
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
