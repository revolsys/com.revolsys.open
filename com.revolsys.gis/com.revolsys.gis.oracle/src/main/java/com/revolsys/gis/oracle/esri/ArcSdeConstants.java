package com.revolsys.gis.oracle.esri;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import oracle.sql.SQLName;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;

public final class ArcSdeConstants {

  public static final int ST_GEOMETRY_LINESTRING = 4;

  public static final int ST_GEOMETRY_POINT = 1;

  public static final int ST_GEOMETRY_POLYGON = 8;

  public static final SQLName ST_GEOMETRY_SQL_NAME;

  public static final int GEOMETRY = 0;

  public static final int POINT = 1;

  public static final int CURVE = 2;

  public static final int LINESTRING = 3;

  public static final int SURFACE = 4;

  public static final int POLYGON = 5;

  public static final int COLLECTION = 6;

  public static final int MULTI_POINT = 7;

  public static final int MULTI_CURVE = 8;

  public static final int MULTI_LINESTRING = 9;

  public static final int MULT_SURFACE = 10;

  public static final int MULTI_POLYGON = 11;

  public static final Map<Integer, DataType> DATA_TYPE_MAP = new HashMap<Integer, DataType>();

  static {
    try {
      ST_GEOMETRY_SQL_NAME = new SQLName("SDE", "ST_GEOMETRY", null);
    } catch (final SQLException e) {
      throw new RuntimeException(
        "Unable to create SQL name for SDE.ST_GEOMETRY");
    }
    DATA_TYPE_MAP.put(POINT, DataTypes.POINT);
    DATA_TYPE_MAP.put(LINESTRING, DataTypes.LINESTRING);
    DATA_TYPE_MAP.put(POLYGON, DataTypes.POLYGON);
  }

  public static DataType getGeometryDataType(
    int geometryType) {
    final DataType dataType = DATA_TYPE_MAP.get(geometryType);
    if (dataType == null) {
      return DataTypes.GEOMETRY;
    } else {
      return dataType;
    }
  }
}
