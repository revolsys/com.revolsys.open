package com.revolsys.gis.data.io;

import java.util.Map;

import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.Geometry;

public class DataObjectWriterGeometryWriter extends AbstractWriter<Geometry> {
  private final Writer<DataObject> writer;

  public DataObjectWriterGeometryWriter(final Writer<DataObject> writer) {
    this.writer = writer;
  }

  @Override
  public void close() {
    writer.close();
  }

  @Override
  public void flush() {
    writer.flush();
  }

  @Override
  public Map<String, Object> getProperties() {
    return writer.getProperties();
  }

  @Override
  public <V> V getProperty(final String name) {
    return (V)writer.getProperty(name);
  }

  @Override
  public void setProperty(final String name, final Object value) {
    writer.setProperty(name, value);
  }

  @Override
  public void write(final Geometry geometry) {
    DataObjectMetaData metaData = DataObjectUtil.createGeometryMetaData();
    final DataObject object = new ArrayDataObject(
      metaData);
    object.setGeometryValue(geometry);
    writer.write(object);
  }

}
