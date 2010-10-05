package com.revolsys.gis.wkt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;

import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectIterator;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class WktIterator extends AbstractObjectWithProperties implements
  DataObjectIterator {

  private DataObject currentObject;

  private final DataObjectFactory factory;

  private boolean hasNext = true;

  private final BufferedReader in;

  private boolean initialized;

  private WKTReader wktReader;

  public WktIterator(
    final DataObjectFactory factory,
    final Resource resource)
    throws IOException {
    this.factory = factory;
    this.in = new BufferedReader(new InputStreamReader(
      resource.getInputStream()));
  }

  public void close() {
    hasNext = false;
    FileUtil.closeSilent(in);
  }

  @Override
  protected void finalize()
    throws Throwable {

    close();
  }

  public DataObjectMetaData getMetaData() {
    return WktConstants.META_DATA;
  }

  public boolean hasNext() {
    if (!initialized) {
      init();
    }
    return hasNext;
  }

  private void init() {
    initialized = true;
    GeometryFactory geometryFactory = getProperty(IoConstants.GEOMETRY_FACTORY);
    if (geometryFactory == null) {
      geometryFactory = new GeometryFactory();
    }
    wktReader = new WKTReader(geometryFactory);
    readNext();
  }

  public DataObject next() {
    if (!hasNext) {
      throw new NoSuchElementException("No more elements");
    } else {
      final DataObject object = currentObject;
      readNext();
      return object;
    }
  }

  private void readNext() {
    try {
      final Geometry geometry = wktReader.read(in);
      if (geometry == null) {
        close();
      } else {
        currentObject = factory.createDataObject(getMetaData());
        currentObject.setGeometryValue(geometry);
      }
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      hasNext = false;
      close();
    }

  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
}
