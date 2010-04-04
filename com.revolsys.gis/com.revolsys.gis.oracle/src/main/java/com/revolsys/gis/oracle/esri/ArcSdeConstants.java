package com.revolsys.gis.oracle.esri;

import java.sql.SQLException;

import oracle.sql.SQLName;

public final class ArcSdeConstants {

  public static final int ST_GEOMETRY_LINESTRING = 4;

  public static final int ST_GEOMETRY_POINT = 1;

  public static final int ST_GEOMETRY_POLYGON = 8;

  public static final SQLName ST_GEOMETRY_SQL_NAME;

  static {
    try {
      ST_GEOMETRY_SQL_NAME = new SQLName("SDE", "ST_GEOMETRY", null);
    } catch (final SQLException e) {
      throw new RuntimeException(
        "Unable to create SQL name for SDE.ST_GEOMETRY");
    }
  }
}
