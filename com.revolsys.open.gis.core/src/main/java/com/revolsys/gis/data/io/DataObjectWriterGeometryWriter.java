package com.revolsys.gis.data.io;

import java.util.Map;


import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.Writer;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectWriterGeometryWriter extends AbstractWriter<Geometry> {
  private Writer<DataObject> writer;

  public DataObjectWriterGeometryWriter(
    Writer<DataObject> writer) {
    this.writer = writer;
  }

  public void close() {
    writer.close();
  }

  public void flush() {
    writer.flush();
  }

  @Override
  public Map<String, Object> getProperties() {
    return writer.getProperties();
  }

  public <V> V getProperty(
    String name) {
    return (V)writer.getProperty(name);
  }

  public void setProperty(
    String name,
    Object value) {
    writer.setProperty(name, value);
  }

  public void write(
    Geometry geometry) {
    DataObject object = new ArrayDataObject(DataObjectUtil.GEOMETRY_META_DATA);
    object.setGeometryValue(geometry);
    writer.write(object);
  }

}
