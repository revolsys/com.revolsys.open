package com.revolsys.record.io.format.esri.gdb.xml.model.enums;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;

public enum GeometryType {
  esriGeometryAny(DataTypes.GEOMETRY), //
  esriGeometryBag(DataTypes.GEOMETRY_COLLECTION), //
  esriGeometryBezier3Curve(null), //
  esriGeometryCircularArc(null), //
  esriGeometryEllipticArc(null), //
  esriGeometryEnvelope(DataTypes.BOUNDING_BOX), //
  esriGeometryLine(DataTypes.LINE_STRING), //
  esriGeometryMultiPatch(null), //
  esriGeometryMultipoint(DataTypes.MULTI_POINT), //
  esriGeometryNull(null), //
  esriGeometryPath(DataTypes.LINE_STRING), //
  esriGeometryPoint(DataTypes.POINT), //
  esriGeometryPolygon(DataTypes.MULTI_POLYGON), //
  esriGeometryPolyline(DataTypes.MULTI_LINE_STRING), //
  esriGeometryRay(null), //
  esriGeometryRing(DataTypes.LINEAR_RING), //
  esriGeometrySphere(null), //
  esriGeometryTriangleFan(null), //
  esriGeometryTriangles(null), //
  esriGeometryTriangleStrip(null);

  private DataType dataType;

  private GeometryType(final DataType dataType) {
    this.dataType = dataType;
  }

  public DataType getDataType() {
    return this.dataType;
  }
}
