package com.revolsys.gis.cs;

import java.util.Map;
import java.util.TreeMap;

public class ProjectionParameterNames {

  private static final Map<String, String> ALIASES = new TreeMap<>();

  private static final String ANGLE_FROM_RECTIFIED_TO_SKEW_GRID = "angle_from_rectified_to_skew_grid";

  public static final String AZIMUTH = "azimuth";

  private static final String AZIMUTH_OF_INITIAL_LINE = "azimuth_of_initial_line";

  public static final String CENTRAL_MERIDIAN = "central_meridian";

  private static final String CO_LATITUDE_OF_CONE_AXIS = "co-latitude_of_cone_axis";

  private static final String EASTING_AT_FALSE_ORIGIN = "easting_at_false_origin";

  private static final String EASTING_AT_PROJECTION_CENTRE = "easting_at_projection_centre";

  public static final String FALSE_EASTING = "false_easting";

  public static final String FALSE_NORTHING = "false_northing";

  private static final String INITIAL_LONGITUDE = "initial_longitude";

  private static final String LATITUDE_OF_1ST_STANDARD_PARALLEL = "latitude_of_1st_standard_parallel";

  private static final String LATITUDE_OF_2ND_STANDARD_PARALLEL = "latitude_of_2nd_standard_parallel";

  public static final String LATITUDE_OF_CENTER = "latitude_of_center";

  private static final String LATITUDE_OF_FALSE_ORIGIN = "latitude_of_false_origin";

  private static final String LATITUDE_OF_NATURAL_ORIGIN = "latitude_of_natural_origin";

  private static final String LATITUDE_OF_ORIGIN = "latitude_of_origin";

  private static final String LATITUDE_OF_PROJECTION_CENTRE = "latitude_of_projection_centre";

  private static final String LATITUDE_OF_PSEUDO_STANDARD_PARALLEL = "latitude_of_pseudo_standard_parallel";

  private static final String LATITUDE_OF_STANDARD_PARALLEL = "latitude_of_standard_parallel";

  public static final String LONGITUDE_OF_CENTER = "longitude_of_center";

  private static final String LONGITUDE_OF_FALSE_ORIGIN = "longitude_of_false_origin";

  private static final String LONGITUDE_OF_NATURAL_ORIGIN = "longitude_of_natural_origin";

  private static final String LONGITUDE_OF_ORIGIN = "longitude_of_origin";

  private static final String LONGITUDE_OF_PROJECTION_CENTRE = "longitude_of_projection_centre";

  private static final String NORTHING_AT_FALSE_ORIGIN = "northing_at_false_origin";

  private static final String NORTHING_AT_PROJECTION_CENTRE = "northing_at_projection_centre";

  private static final String PSEUDO_STANDARD_PARALLEL_1 = "pseudo_standard_parallel_1";

  private static final String RECTIFIED_GRID_ANGLE = "rectified_grid_angle";

  public static final String SCALE_FACTOR = "scale_factor";

  private static final String SCALE_FACTOR_AT_NATURAL_ORIGIN = "scale_factor_at_natural_origin";

  private static final String SCALE_FACTOR_ON_INITIAL_LINE = "scale_factor_on_initial_line";

  private static final String SCALE_FACTOR_ON_PSEUDO_STANDARD_PARALLEL = "scale_factor_on_pseudo_standard_parallel";

  public static final String STANDARD_PARALLEL_1 = "standard_parallel_1";

  public static final String STANDARD_PARALLEL_2 = "standard_parallel_2";

  public static final String ZONE_WIDTH = "zone_width";

  static {
    ALIASES.put(FALSE_EASTING, FALSE_EASTING);
    ALIASES.put(EASTING_AT_FALSE_ORIGIN, FALSE_EASTING);
    ALIASES.put(EASTING_AT_PROJECTION_CENTRE, FALSE_NORTHING);

    ALIASES.put(FALSE_NORTHING, FALSE_NORTHING);
    ALIASES.put(NORTHING_AT_FALSE_ORIGIN, FALSE_NORTHING);
    ALIASES.put(NORTHING_AT_PROJECTION_CENTRE, FALSE_NORTHING);

    ALIASES.put(LONGITUDE_OF_CENTER, LONGITUDE_OF_CENTER);
    ALIASES.put(LONGITUDE_OF_FALSE_ORIGIN, LONGITUDE_OF_CENTER);
    ALIASES.put(LONGITUDE_OF_ORIGIN, LONGITUDE_OF_CENTER);
    ALIASES.put(LONGITUDE_OF_NATURAL_ORIGIN, LONGITUDE_OF_CENTER);
    ALIASES.put(CENTRAL_MERIDIAN, LONGITUDE_OF_CENTER);
    ALIASES.put(LONGITUDE_OF_PROJECTION_CENTRE, LONGITUDE_OF_CENTER);
    ALIASES.put(INITIAL_LONGITUDE, LONGITUDE_OF_CENTER);

    ALIASES.put(LATITUDE_OF_CENTER, LATITUDE_OF_CENTER);
    ALIASES.put(LATITUDE_OF_FALSE_ORIGIN, LATITUDE_OF_CENTER);
    ALIASES.put(LATITUDE_OF_ORIGIN, LATITUDE_OF_CENTER);
    ALIASES.put(LATITUDE_OF_NATURAL_ORIGIN, LATITUDE_OF_CENTER);
    ALIASES.put(LATITUDE_OF_PROJECTION_CENTRE, LATITUDE_OF_CENTER);
    ALIASES.put(LATITUDE_OF_STANDARD_PARALLEL, LATITUDE_OF_CENTER);

    ALIASES.put(STANDARD_PARALLEL_1, STANDARD_PARALLEL_1);
    ALIASES.put(LATITUDE_OF_1ST_STANDARD_PARALLEL, STANDARD_PARALLEL_1);
    ALIASES.put(LATITUDE_OF_PSEUDO_STANDARD_PARALLEL, STANDARD_PARALLEL_1);
    ALIASES.put(PSEUDO_STANDARD_PARALLEL_1, STANDARD_PARALLEL_1);

    ALIASES.put(STANDARD_PARALLEL_2, STANDARD_PARALLEL_2);
    ALIASES.put(LATITUDE_OF_2ND_STANDARD_PARALLEL, STANDARD_PARALLEL_2);

    ALIASES.put(SCALE_FACTOR, SCALE_FACTOR);
    ALIASES.put(SCALE_FACTOR_AT_NATURAL_ORIGIN, SCALE_FACTOR);
    ALIASES.put(SCALE_FACTOR_ON_INITIAL_LINE, SCALE_FACTOR);
    ALIASES.put(SCALE_FACTOR_ON_PSEUDO_STANDARD_PARALLEL, SCALE_FACTOR);

    ALIASES.put(AZIMUTH, AZIMUTH);
    ALIASES.put(AZIMUTH_OF_INITIAL_LINE, AZIMUTH);
    ALIASES.put(CO_LATITUDE_OF_CONE_AXIS, AZIMUTH);

    ALIASES.put(RECTIFIED_GRID_ANGLE, RECTIFIED_GRID_ANGLE);
    ALIASES.put(ANGLE_FROM_RECTIFIED_TO_SKEW_GRID, RECTIFIED_GRID_ANGLE);

    ALIASES.put(ZONE_WIDTH, ZONE_WIDTH);
  }

  public static String getParameterName(String name) {
    name = name.toLowerCase().replaceAll(" ", "_");
    String alias = ALIASES.get(name);
    if (alias == null) {
      alias = name.intern();
      ALIASES.put(alias, alias);
      // System.out.println(alias);
      return alias;
    } else {
      return alias;
    }
  }
}
