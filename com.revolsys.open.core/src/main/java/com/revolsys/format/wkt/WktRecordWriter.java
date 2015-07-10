package com.revolsys.format.wkt;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.property.FieldProperties;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.WrappedException;

public class WktRecordWriter extends AbstractRecordWriter {

  private final RecordDefinition recordDefinition;

  private final Writer out;

  private boolean open;

  public WktRecordWriter(final RecordDefinition recordDefinition, final Writer out) {
    this.recordDefinition = recordDefinition;
    this.out = new BufferedWriter(out);
    final FieldDefinition geometryField = recordDefinition.getGeometryField();
    if (geometryField != null) {
      final GeometryFactory geometryFactory = geometryField
        .getProperty(FieldProperties.GEOMETRY_FACTORY);
      setProperty(IoConstants.GEOMETRY_FACTORY, geometryFactory);
    }

  }

  @Override
  public void close() {
    FileUtil.closeSilent(this.out);
  }

  @Override
  public void flush() {
    try {
      this.out.flush();
    } catch (final IOException e) {
    }
  }

  @Override
  public String toString() {
    return this.recordDefinition.getPath().toString();
  }

  @Override
  public void write(final Record object) {
    try {
      if (!this.open) {
        this.open = true;
      }
      final Geometry geometry = object.getGeometry();
      final int srid = geometry.getSrid();
      if (srid > 0) {
        this.out.write("SRID=");
        this.out.write(Integer.toString(srid));
        this.out.write(';');
      }
      EWktWriter.write(this.out, geometry);
      this.out.write('\n');
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

}
