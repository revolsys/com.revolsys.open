package com.revolsys.record.io;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.io.Reader;
import com.revolsys.record.Record;

public class GeometryRecordReader implements Iterator<Geometry>, GeometryReader {
  private Reader<Record> reader;

  private Iterator<Record> iterator;

  public GeometryRecordReader(final Reader<Record> reader) {
    this.reader = reader;
    this.iterator = reader.iterator();
  }

  @Override
  public void close() {
    this.reader = null;
    this.iterator = null;
  }

  @Override
  public final MapEx getProperties() {
    return this.reader.getProperties();
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public Iterator<Geometry> iterator() {
    return this;
  }

  @Override
  public Geometry next() {
    if (this.iterator.hasNext()) {
      final Record record = this.iterator.next();
      return record.getGeometry();
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    this.iterator.remove();
  }
}
