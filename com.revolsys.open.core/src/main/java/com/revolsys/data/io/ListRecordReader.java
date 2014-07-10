package com.revolsys.data.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.AbstractReader;

public class ListRecordReader extends AbstractReader<Record> implements
  RecordReader {
  private RecordDefinition recordDefinition;

  private List<Record> objects = new ArrayList<Record>();

  public ListRecordReader(final RecordDefinition recordDefinition,
    final Collection<? extends Record> objects) {
    this.recordDefinition = recordDefinition;
    this.objects = new ArrayList<Record>(objects);
  }

  public ListRecordReader(final RecordDefinition recordDefinition,
    final Record... objects) {
    this(recordDefinition, Arrays.asList(objects));
  }

  @Override
  public void close() {
    recordDefinition = null;
    objects = null;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return recordDefinition;
  }

  @Override
  public Iterator<Record> iterator() {
    return objects.iterator();
  }

  @Override
  public void open() {
  }
}