package com.revolsys.io.wkt;

import java.io.BufferedWriter;
import java.io.PrintWriter;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.property.AttributeProperties;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;

public class WktDataObjectWriter extends AbstractWriter<Record> {

  private final RecordDefinition metaData;

  private final PrintWriter out;

  private boolean open;

  public WktDataObjectWriter(final RecordDefinition metaData,
    final java.io.Writer out) {
    this.metaData = metaData;
    this.out = new PrintWriter(new BufferedWriter(out));
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    if (geometryAttribute != null) {
      final GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
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
  public void write(final Record object) {
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
