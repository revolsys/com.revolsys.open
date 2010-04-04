package com.revolsys.gis.data.io;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.Writer;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectWriterGeometryWriter extends AbstractWriter<Geometry> {
  public static final DataObjectMetaData META_DATA;
  static {
    final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(
      new QName("geometry"));
    metaData.addAttribute("geometry", DataTypes.GEOMETRY, true);
    META_DATA = metaData;
  }

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

  public <V> V getProperty(
    QName name) {
    return (V)writer.getProperty(name);
  }

  public void setProperty(
    QName name,
    Object value) {
    writer.setProperty(name, value);
  }

  public void write(
    Geometry geometry) {
    DataObject object = new ArrayDataObject(META_DATA);
    object.setGeometryValue(geometry);
    writer.write(object);
  }

}
