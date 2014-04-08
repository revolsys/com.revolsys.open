package com.revolsys.io.wkt;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectIterator;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.jts.geom.Geometry;

public class WktDataObjectIterator extends AbstractIterator<DataObject>
  implements DataObjectIterator {

  private DataObjectFactory factory;

  private BufferedReader in;

  private WktParser wktParser;

  private DataObjectMetaData metaData;

  public WktDataObjectIterator(final DataObjectFactory factory,
    final Resource resource) throws IOException {
    this.factory = factory;
    this.in = new BufferedReader(
      FileUtil.createUtf8Reader(resource.getInputStream()));
    this.metaData = DataObjectUtil.createGeometryMetaData();
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
      geometryFactory = GeometryFactory.getFactory();
    } else {
      geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
      if (geometryFactory == null) {
        geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
        if (geometryFactory == null) {
          geometryFactory = GeometryFactory.getFactory();
        }
        geometryAttribute.setProperty(AttributeProperties.GEOMETRY_FACTORY,
          geometryFactory);
      }
    }
    wktParser = new WktParser(geometryFactory);
  }

  @Override
  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  @Override
  protected DataObject getNext() {
    try {
      final String wkt = in.readLine();
      final Geometry geometry = wktParser.parseGeometry(wkt);
      if (geometry == null) {
        throw new NoSuchElementException();
      } else {
        final DataObject object = factory.createDataObject(getMetaData());
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
