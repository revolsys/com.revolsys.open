package com.revolsys.raster.io.format.tiff;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jeometry.coordinatesystem.model.ParameterName;
import org.jeometry.coordinatesystem.model.ParameterNames;
import org.jeometry.coordinatesystem.model.ParameterValue;

import com.revolsys.collection.map.IntHashMap;

public enum TiffProjectionParameterName {
  ProjStdParallel1GeoKey(3078, ParameterNames.STANDARD_PARALLEL_1), //
  ProjStdParallel2GeoKey(3079, ParameterNames.STANDARD_PARALLEL_2), //
  ProjNatOriginLongGeoKey(3080, ParameterNames.STANDARD_PARALLEL_1), //
  ProjNatOriginLatGeoKey(3081, ParameterNames.LATITUDE_OF_NATURAL_ORIGIN), //
  ProjFalseEastingGeoKey(3082, ParameterNames.FALSE_EASTING), //
  ProjFalseNorthingGeoKey(3083, ParameterNames.FALSE_NORTHING), //
  ProjFalseOriginLongGeoKey(3084, ParameterNames.LONGITUDE_OF_FALSE_ORIGIN), //
  ProjFalseOriginLatGeoKey(3085, ParameterNames.LATITUDE_OF_FALSE_ORIGIN), //
  ProjFalseOriginEastingGeoKey(3086, ParameterNames.EASTING_AT_FALSE_ORIGIN), //
  ProjFalseOriginNorthingGeoKey(3087, ParameterNames.NORTHING_AT_FALSE_ORIGIN), //
  ProjCenterLongGeoKey(3088, ParameterNames.LONGITUDE_OF_CENTER), //
  ProjCenterLatGeoKey(3089, ParameterNames.LATITUDE_OF_CENTER), //
  ProjCenterEastingGeoKey(3090, ParameterNames.EASTING_AT_PROJECTION_CENTRE), //
  ProjCenterNorthingGeoKey(3091, ParameterNames.NORTHING_AT_PROJECTION_CENTRE), //
  ProjScaleAtNatOriginGeoKey(3092, ParameterNames.SCALE_FACTOR_AT_NATURAL_ORIGIN), //
  ProjScaleAtCenterGeoKey(3093, ParameterNames.SCALE_FACTOR), //
  ProjAzimuthAngleGeoKey(3094, ParameterNames.AZIMUTH); //
  // ProjStraightVertPoleLongGeoKey(3095, ParameterNames.);

  private static IntHashMap<TiffProjectionParameterName> valueByCode = new IntHashMap<>();

  private static Map<ParameterName, TiffProjectionParameterName> valueByParameterName = new HashMap<>();

  static {
    for (final TiffProjectionParameterName value : TiffProjectionParameterName.values()) {
      final int code = value.getCode();
      valueByCode.put(code, value);
      final ParameterName parameterName = value.getParameterName();
      valueByParameterName.put(parameterName, value);
    }
  }

  public static TiffProjectionParameterName getCode(final int code) {
    return valueByCode.get(code);
  }

  public static TiffProjectionParameterName getCode(final ParameterName parameterName) {
    return valueByParameterName.get(parameterName);
  }

  public static Map<ParameterName, ParameterValue> getProjectionParameters(
    final Map<Integer, Object> geoKeys) {
    final Map<ParameterName, ParameterValue> parameters = new LinkedHashMap<>();
    for (final TiffProjectionParameterName tiffParam : values()) {
      final int code = tiffParam.getCode();
      final ParameterName parameterName = tiffParam.getParameterName();
      TiffImageFactory.addDoubleParameter(parameters, parameterName, geoKeys, code);
    }
    return parameters;
  }

  private int code;

  private ParameterName parameterName;

  private TiffProjectionParameterName(final int code, final ParameterName parameterName) {
    this.code = code;
    this.parameterName = parameterName;
  }

  public int getCode() {
    return this.code;
  }

  public ParameterName getParameterName() {
    return this.parameterName;
  }
}
