package com.revolsys.gis.wkt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class WktDataObjectIterator extends AbstractIterator<DataObject>
  implements DataObjectIterator {

  private final DataObjectFactory factory;

  private final BufferedReader in;

  private WktParser wktParser;

  private DataObjectMetaData metaData;

  public WktDataObjectIterator(
    final DataObjectFactory factory,
    final Resource resource)
    throws IOException {
    this.factory = factory;
    this.in = new BufferedReader(new InputStreamReader(
      resource.getInputStream()));
    metaData = DataObjectUtil.GEOMETRY_META_DATA.clone();
  }

  @Override
  protected void doClose() {
    FileUtil.closeSilent(in);
  }

  @Override
  protected void doInit() {
    GeometryFactory geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
    if (geometryFactory == null) {
      geometryFactory = new GeometryFactory();
    }
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    if (geometryAttribute != null) {
      geometryAttribute.setProperty(AttributeProperties.GEOMETRY_FACTORY,
        geometryFactory);
    }
    wktParser = new WktParser(geometryFactory);
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  @Override
  protected DataObject getNext() {
    try {
      String wkt = in.readLine();
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
