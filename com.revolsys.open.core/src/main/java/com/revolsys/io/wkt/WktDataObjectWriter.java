package com.revolsys.io.wkt;

import java.io.BufferedWriter;
import java.io.PrintWriter;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.jts.geom.Geometry;

public class WktDataObjectWriter extends AbstractWriter<DataObject> {

  private final DataObjectMetaData metaData;

  private final PrintWriter out;

  private boolean open;

  public WktDataObjectWriter(final DataObjectMetaData metaData,
    final java.io.Writer out) {
    this.metaData = metaData;
    this.out = new PrintWriter(new BufferedWriter(out));
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    if (geometryAttribute != null) {
      final com.revolsys.jts.geom.GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
      setProperty(IoConstants.GEOMETRY_FACTORY, geometryFactory);
    }

  }

  @Override
  public void close() {
    out.close();
  }

  @Override
  public void flush() {
    out.flush();
  }

  @Override
  public String toString() {
    return metaData.getPath().toString();
  }

  @Override
  public void write(final DataObject object) {
    if (!open) {
      open = true;
    }
    final Geometry geometry = object.getGeometryValue();
    final int srid = geometry.getSrid();
    if (srid > 0) {
      out.print("SRID=");
      out.print(srid);
      out.print(';');
    }
    WktWriter.write(out, geometry);
    out.println();
  }

}
