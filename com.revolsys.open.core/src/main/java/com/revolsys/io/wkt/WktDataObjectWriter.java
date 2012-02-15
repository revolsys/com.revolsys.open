package com.revolsys.io.wkt;

import java.io.BufferedWriter;
import java.io.PrintWriter;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.IoConstants;
import com.vividsolutions.jts.geom.Geometry;

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
    return metaData.getName().toString();
  }

  public void write(final DataObject object) {
    if (!open) {
      open = true;
    }
    final Geometry geometry = object.getGeometryValue();
    WktWriter.write(out, geometry);
    out.println();
  }

}
