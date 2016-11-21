package com.revolsys.record.io;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.format.csv.AbstractRecordReader;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;

public class GeometryRecordReader extends AbstractRecordReader {
  private final GeometryReader geometryReader;

  private Iterator<Geometry> geometryIterator;

  private final String baseName;

  public GeometryRecordReader(final String baseName, final GeometryReader geometryReader,
    final RecordFactory<? extends Record> recordFactory) {
    super(recordFactory);
    this.baseName = baseName;
    this.geometryReader = geometryReader;
  }

  @Override
  protected Record getNext() throws NoSuchElementException {
    if (this.geometryIterator.hasNext()) {
      final Geometry geometry = this.geometryIterator.next();
      final Record record = newRecord();
      record.setGeometryValue(geometry);
      return record;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  protected void initDo() {
    this.geometryIterator = this.geometryReader.iterator();
    this.geometryIterator.hasNext();
    setGeometryFactory(this.geometryReader.getGeometryFactory());
    final RecordDefinition recordDefinition = new RecordDefinitionBuilder(this.baseName) //
      .addField("GEOMETRY", DataTypes.GEOMETRY) //
      .getRecordDefinition();
    setRecordDefinition(recordDefinition);
  }

}
