package com.revolsys.record.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.revolsys.io.AbstractReader;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;

public class ListRecordReader extends AbstractReader<Record> implements RecordReader {
  private List<Record> records = new ArrayList<>();

  private RecordDefinition recordDefinition;

  public ListRecordReader(final RecordDefinition recordDefinition,
    final Collection<? extends Record> records) {
    this.recordDefinition = recordDefinition;
    this.records = new ArrayList<>(records);
  }

  public ListRecordReader(final RecordDefinition recordDefinition, final Record... records) {
    this(recordDefinition, Arrays.asList(records));
  }

  @Override
  public void close() {
    this.recordDefinition = null;
    this.records = Collections.emptyList();
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  @Override
  public Iterator<Record> iterator() {
    return this.records.iterator();
  }

  @Override
  public void open() {
  }
}
