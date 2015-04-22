package com.revolsys.format.wkt;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.data.io.RecordIterator;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.RecordUtil;
import com.revolsys.data.record.property.FieldProperties;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;

public class WktRecordIterator extends AbstractIterator<Record>
implements RecordIterator {

  private RecordFactory factory;

  private BufferedReader in;

  private WktParser wktParser;

  private RecordDefinition recordDefinition;

  public WktRecordIterator(final RecordFactory factory,
    final Resource resource) throws IOException {
    this.factory = factory;
    this.in = new BufferedReader(
      FileUtil.createUtf8Reader(resource.getInputStream()));
    this.recordDefinition = RecordUtil.createGeometryRecordDefinition();
  }

  @Override
  protected void doClose() {
    FileUtil.closeSilent(this.in);
    this.factory = null;
    this.in = null;
    this.wktParser = null;
    this.recordDefinition = null;
  }

  @Override
  protected void doInit() {
    GeometryFactory geometryFactory;
    final FieldDefinition geometryField = this.recordDefinition.getGeometryField();
    if (geometryField == null) {
      geometryFactory = GeometryFactory.floating3();
    } else {
      geometryFactory = geometryField.getProperty(FieldProperties.GEOMETRY_FACTORY);
      if (geometryFactory == null) {
        geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
        if (geometryFactory == null) {
          geometryFactory = GeometryFactory.floating3();
        }
        geometryField.setProperty(FieldProperties.GEOMETRY_FACTORY,
          geometryFactory);
      }
    }
    this.wktParser = new WktParser(geometryFactory);
  }

  @Override
  protected Record getNext() {
    try {
      final String wkt = this.in.readLine();
      final Geometry geometry = this.wktParser.parseGeometry(wkt);
      if (geometry == null) {
        throw new NoSuchElementException();
      } else {
        final Record object = this.factory.createRecord(getRecordDefinition());
        object.setGeometryValue(geometry);
        return object;
      }
    } catch (final IOException e) {
      throw new RuntimeException("Error reading geometry ", e);
    }

  }

  @Override
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
