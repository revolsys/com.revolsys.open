package com.revolsys.gis.esri.gdb.file.type;

import java.io.IOException;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.convert.ByteArrayEndianInput;
import com.revolsys.gis.esri.gdb.file.swig.Row;
import com.revolsys.gis.esri.gdb.file.swig.ShapeBuffer;
import com.revolsys.gis.esri.gdb.file.swig.ShapeType;
import com.revolsys.gis.format.shape.io.geometry.ShapefileGeometryUtil;
import com.revolsys.io.EndianInput;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryAttribute extends AbstractEsriFileGeodatabaseAttribute {
  private GeometryFactory geometryFactory = new GeometryFactory();

  public GeometryAttribute(String name, int length, boolean required) {
    super(name, DataTypes.GEOMETRY, length, required);
  }

  @Override
  public Object getValue(Row row) {
    final String name = getName();
    if (row.isNull(name)) {
      return null;
    } else {
      ShapeBuffer shapeBuffer = row.getGeometry();
      EndianInput in = new ByteArrayEndianInput(shapeBuffer.getShapeBuffer());
      final ShapeType shapeType = shapeBuffer.getShapeType();
      try {
        final int type = in.readLEInt();
        Geometry geometry = null;
        switch (shapeType) {
          case shapeNull:
            geometry = null;
          break;
          case shapePoint:
            geometry = ShapefileGeometryUtil.readPoint(geometryFactory, in);
          break;
          case shapePointZ:
            geometry = ShapefileGeometryUtil.readPointZ(geometryFactory, in);
          break;
          case shapePointZM:
            geometry = ShapefileGeometryUtil.readPointZM(geometryFactory, in);
          break;
          case shapePointM:
            geometry = ShapefileGeometryUtil.readPointM(geometryFactory, in);
          break;
          case shapeMultipoint:
            geometry = ShapefileGeometryUtil.readMultiPoint(geometryFactory, in);
          break;
          case shapeMultipointZ:
            geometry = ShapefileGeometryUtil.readMultiPointZ(geometryFactory,
              in);
          break;
          case shapeMultipointZM:
            geometry = ShapefileGeometryUtil.readMultiPointZM(geometryFactory,
              in);
          break;
          case shapeMultipointM:
            geometry = ShapefileGeometryUtil.readMultiPointM(geometryFactory,
              in);
          break;
          case shapePolyline:
            geometry = ShapefileGeometryUtil.readPolyline(geometryFactory, in);
          break;
          case shapePolylineZ:
            geometry = ShapefileGeometryUtil.readPolylineZ(geometryFactory, in);
          break;
          case shapePolylineZM:
            geometry = ShapefileGeometryUtil.readPolylineZM(geometryFactory, in);
          break;
          case shapePolylineM:
            geometry = ShapefileGeometryUtil.readPolylineM(geometryFactory, in);
          break;
          case shapePolygon:
            geometry = ShapefileGeometryUtil.readPolygon(geometryFactory, in);
          break;
          case shapePolygonZ:
            geometry = ShapefileGeometryUtil.readPolygonZ(geometryFactory, in);
          break;
          case shapePolygonZM:
            geometry = ShapefileGeometryUtil.readPolygonZM(geometryFactory, in);
          break;
          case shapePolygonM:
            geometry = ShapefileGeometryUtil.readPolygonM(geometryFactory, in);
          break;
          // TODO general types and multipatch
          default:
          break;
        }
        return geometry;
      } catch (IOException e) {
      }
      return null;
    }
  }

  public void setValue(Row row, Object object) {
  }
}
