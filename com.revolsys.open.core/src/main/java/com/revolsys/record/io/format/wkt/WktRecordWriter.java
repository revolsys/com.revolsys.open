package com.revolsys.record.io.format.wkt;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.record.Record;
import com.revolsys.record.property.FieldProperties;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.Exceptions;

public class WktRecordWriter extends AbstractRecordWriter {

  private boolean open;

  private final Writer out;

  private final RecordDefinition recordDefinition;

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
  public ClockDirection getPolygonRingDirection() {
    return ClockDirection.COUNTER_CLOCKWISE;
  }

  @Override
  public String toString() {
    return this.recordDefinition.getPath().toString();
  }

  @Override
  public void write(final Record record) {
    try {
      if (!this.open) {
        this.open = true;
      }
      final Geometry geometry = record.getGeometry();
      final int srid = geometry.getHorizontalCoordinateSystemId();
      if (srid > 0) {
        this.out.write("SRID=");
        this.out.write(Integer.toString(srid));
        this.out.write(';');
      }
      EWktWriter.writeCCW(this.out, geometry);
      this.out.write('\n');
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

}
