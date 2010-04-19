package com.revolsys.gis.wkt;

import java.io.BufferedWriter;
import java.io.PrintWriter;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
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
      setProperty(IoConstants.SRID_PROPERTY, srid);
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
