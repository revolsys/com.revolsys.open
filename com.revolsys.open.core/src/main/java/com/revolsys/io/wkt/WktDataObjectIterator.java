package com.revolsys.io.wkt;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.data.io.DataObjectIterator;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.RecordUtil;
import com.revolsys.data.record.property.AttributeProperties;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;

public class WktDataObjectIterator extends AbstractIterator<Record>
  implements DataObjectIterator {

  private RecordFactory factory;

  private BufferedReader in;

  private WktParser wktParser;

  private RecordDefinition metaData;

  public WktDataObjectIterator(final RecordFactory factory,
    final Resource resource) throws IOException {
    this.factory = factory;
    this.in = new BufferedReader(
      FileUtil.createUtf8Reader(resource.getInputStream()));
    this.metaData = RecordUtil.createGeometryMetaData();
  }

  @Override
  protected void doClose() {
    FileUtil.closeSilent(in);
    factory = null;
    in = null;
    wktParser = null;
    metaData = null;
  }

  @Override
  protected void doInit() {
    GeometryFactory geometryFactory;
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    if (geometryAttribute == null) {
      geometryFactory = GeometryFactory.floating3();
    } else {
      geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
      if (geometryFactory == null) {
        geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
        if (geometryFactory == null) {
          geometryFactory = GeometryFactory.floating3();
        }
        geometryAttribute.setProperty(AttributeProperties.GEOMETRY_FACTORY,
          geometryFactory);
      }
    }
    wktParser = new WktParser(geometryFactory);
  }

  @Override
  public RecordDefinition getMetaData() {
    return metaData;
  }

  @Override
  protected Record getNext() {
    try {
      final String wkt = in.readLine();
      final Geometry geometry = wktParser.parseGeometry(wkt);
      if (geometry == null) {
        throw new NoSuchElementException();
      } else {
        final Record object = factory.createRecord(getMetaData());
        object.setGeometryValue(geometry);
        return object;
      }
    } catch (final IOException e) {
      throw new RuntimeException("Error reading geometry ", e);
    }

  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
