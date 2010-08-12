package com.revolsys.gis.esri.gdb.xml.type;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.InPlaceIterator;
import com.revolsys.xml.XsiConstants;
import com.revolsys.xml.io.XmlWriter;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class XmlGeometryFieldType extends AbstractEsriGeodatabaseXmlFieldType {
  public XmlGeometryFieldType(
    final String esriFieldTypeName,
    final DataType dataType) {
    super(dataType, "xs:" + dataType.getName().getLocalPart(),
      esriFieldTypeName);
  }

  @Override
  public int getFixedLength() {
    return 4;
  }

  @Override
  protected String getType(
    final Object value) {
    if (value instanceof Point) {
      return POINT_N_TYPE;
    } else if (value instanceof LineString) {
      return POLYLINE_N_TYPE;
    } else if (value instanceof Polygon) {
    }
    return null;
  }

  public void write(
    final XmlWriter out,
    final Coordinates coordinates,
    final boolean hasZ) {
    out.element(X, coordinates.getX());
    out.element(Y, coordinates.getY());
    if (hasZ) {
      out.element(Z, coordinates.getZ());
    }
  }

  private void write(
    final XmlWriter out,
    final LineString line) {
    final CoordinatesList points = CoordinatesListUtil.get(line);
    final boolean hasZ = points.getNumAxis() > 2;
    out.element(HAS_ID, false);
    out.element(HAS_Z, hasZ);
    out.element(HAS_M, false);
    out.startTag(PATH_ARRAY);
    out.attribute(XsiConstants.TYPE, PATH_ARRAY_TYPE);

    out.startTag(POINT_ARRAY);
    out.attribute(XsiConstants.TYPE, POINT_ARRAY_TYPE);

    for (final Coordinates point : new InPlaceIterator(points)) {
      out.startTag(POINT);
      out.attribute(XsiConstants.TYPE, POINT_N_TYPE);
      write(out, point, hasZ);
    }

    out.endTag(POINT_ARRAY);

    out.endTag(PATH_ARRAY);
  }

  private void write(
    final XmlWriter out,
    final Point point) {
    final Coordinates coordinates = CoordinatesUtil.get(point);
    final boolean hasZ = coordinates.getNumAxis() > 2;
    write(out, coordinates, hasZ);
  }

  private void write(
    final XmlWriter out,
    final Polygon polygon) {
  }

  @Override
  protected void writeValueText(
    final XmlWriter out,
    final Object value) {
    if (value instanceof Point) {
      final Point point = (Point)value;
      write(out, point);
    } else if (value instanceof LineString) {
      final LineString line = (LineString)value;
      write(out, line);
    } else if (value instanceof Polygon) {
      final Polygon polygon = (Polygon)value;
      write(out, polygon);
    }
  }
}
