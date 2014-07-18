package com.revolsys.io.wkt;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.Writer;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.property.AttributeProperties;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.IoConstants;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;

public class WktRecordWriter extends AbstractRecordWriter {

  private final RecordDefinition recordDefinition;

  private final PrintWriter out;

  private boolean open;

  public WktRecordWriter(final RecordDefinition recordDefinition,
    final Writer out) {
    this.recordDefinition = recordDefinition;
    this.out = new PrintWriter(new BufferedWriter(out));
    final Attribute geometryAttribute = recordDefinition.getGeometryAttribute();
    if (geometryAttribute != null) {
      final GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
      setProperty(IoConstants.GEOMETRY_FACTORY, geometryFactory);
    }

  }

  @Override
  public void close() {
    this.out.close();
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  @Override
  public String toString() {
    return this.recordDefinition.getPath().toString();
  }

  @Override
  public void write(final Record object) {
    if (!this.open) {
      this.open = true;
    }
    final Geometry geometry = object.getGeometryValue();
    final int srid = geometry.getSrid();
    if (srid > 0) {
      this.out.print("SRID=");
      this.out.print(srid);
      this.out.print(';');
    }
    WktWriter.write(this.out, geometry);
    this.out.println();
  }

}
