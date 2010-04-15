package com.revolsys.gis.wkt;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.AbstractWriter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

public class WktDataObjectWriter extends AbstractWriter<DataObject> {

  private final WKTWriter geometryWriter = new WKTWriter(3);

  private final DataObjectMetaData metaData;

  private final PrintWriter out;

  private boolean open;

  public WktDataObjectWriter(
    final DataObjectMetaData metaData,
    final java.io.Writer out) {
    this.metaData = metaData;
    this.out = new PrintWriter(new BufferedWriter(out));
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    if (geometryAttribute != null) {
      final Integer srid = geometryAttribute.getProperty(AttributeProperties.SRID);
      setProperty("srid", srid);
    }

  }

  public void flush() {
    out.flush();
  }

  public void close() {
    out.close();
  }

  public String toString() {
    return metaData.getName().toString();
  }

  public void write(
    final DataObject object) {
    if (!open) {
      open = true;
    }
    final Geometry geometry = object.getGeometryValue();
    WktWriter.write(out, geometry);
    out.println();
  }

}
