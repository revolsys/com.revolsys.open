package com.revolsys.geometry.cs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import com.revolsys.collection.map.IntHashMap;
import com.revolsys.geometry.cs.epsg.EpsgAuthority;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.cs.projection.AlbersConicEqualArea;
import com.revolsys.geometry.cs.projection.CoordinatesProjection;
import com.revolsys.geometry.cs.projection.LambertConicConformal1SP;
import com.revolsys.geometry.cs.projection.Mercator1SP;
import com.revolsys.geometry.cs.projection.Mercator2SP;
import com.revolsys.geometry.cs.projection.ProjectionFactory;
import com.revolsys.geometry.cs.projection.TransverseMercator;
import com.revolsys.geometry.cs.projection.TransverseMercatorSouthOriented;
import com.revolsys.geometry.cs.projection.WebMercator;
import com.revolsys.geometry.cs.unit.LinearUnit;
import com.revolsys.util.Debug;
import com.revolsys.util.Equals;

public class CoordinateOperationMethod
  implements Serializable, Comparable<CoordinateOperationMethod> {

  public static final String LAMBERT_CONIC_CONFORMAL_2SP_BELGIUM = "Lambert_Conic_Conformal_(2SP_Belgium)";

  public static final String MERCATOR_1SP_SPHERICAL = "Mercator_(1SP)_(Spherical)";

  private static final Map<String, String> PROJECTION_ALIASES = new TreeMap<>();

  private static final IntHashMap<CoordinateOperationMethod> METHOD_BY_ID = new IntHashMap<>();

  private static Map<String, CoordinateOperationMethod> METHOD_BY_NAME = new TreeMap<>();

  private static final long serialVersionUID = 6199958151692874551L;

  static {
    addMethod(9822, "Albers_Equal_Area", true, false, Arrays.asList(//
      new MultiParameterName(ParameterNames.STANDARD_PARALLEL_1,
        ParameterNames.LATITUDE_OF_1ST_STANDARD_PARALLEL), //
      new MultiParameterName(ParameterNames.STANDARD_PARALLEL_2,
        ParameterNames.LATITUDE_OF_2ND_STANDARD_PARALLEL), //
      new MultiParameterName(ParameterNames.LATITUDE_OF_ORIGIN, ParameterNames.LATITUDE_OF_CENTRE,
        ParameterNames.LATITUDE_OF_FALSE_ORIGIN), //
      new MultiParameterName(ParameterNames.CENTRAL_MERIDIAN, ParameterNames.LONGITUDE_OF_CENTRE,
        ParameterNames.LONGITUDE_OF_FALSE_ORIGIN), //
      new MultiParameterName(ParameterNames.FALSE_EASTING, ParameterNames.EASTING_AT_FALSE_ORIGIN), //
      new MultiParameterName(ParameterNames.FALSE_NORTHING, ParameterNames.NORTHING_AT_FALSE_ORIGIN) //
    ), AlbersConicEqualArea::new, "Albers", "Albers_Equal_Area_Conic", "Albers_Conic_Equal_Area");

    addMethod(9806, "CassiniSoldner", true, false, Arrays.asList(//
      new MultiParameterName(ParameterNames.LATITUDE_OF_ORIGIN,
        ParameterNames.LATITUDE_OF_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.CENTRAL_MERIDIAN,
        ParameterNames.LONGITUDE_OF_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.FALSE_EASTING), //
      new MultiParameterName(ParameterNames.FALSE_NORTHING), //
      new MultiParameterName(1.0, ParameterNames.SCALE_FACTOR) //
    ), null, "Cassini");

    addMethod(9819, "Krovak", true, false, Arrays.asList(//
      new MultiParameterName(ParameterNames.LATITUDE_OF_CENTRE,
        ParameterNames.LATITUDE_OF_PROJECTION_CENTRE), //
      new MultiParameterName(ParameterNames.LONGITUDE_OF_CENTRE,
        ParameterNames.LONGITUDE_OF_ORIGIN), //
      new MultiParameterName(ParameterNames.AZIMUTH, ParameterNames.COLATITUDE_OF_CONE_AXIS), //
      new MultiParameterName(ParameterNames.PSEUDO_STANDARD_PARALLEL_1,
        ParameterNames.LATITUDE_OF_PSEUDO_STANDARD_PARALLEL), //
      new MultiParameterName(ParameterNames.SCALE_FACTOR,
        ParameterNames.SCALE_FACTOR_ON_PSEUDO_STANDARD_PARALLEL), //
      new MultiParameterName(ParameterNames.FALSE_EASTING), //
      new MultiParameterName(ParameterNames.FALSE_NORTHING) //
    ), null);

    addMethod(9820, "Lambert_Azimuthal_Equal_Area", true, false, Arrays.asList(//
      new MultiParameterName(ParameterNames.LATITUDE_OF_ORIGIN, ParameterNames.LATITUDE_OF_CENTRE,
        ParameterNames.LATITUDE_OF_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.CENTRAL_MERIDIAN, ParameterNames.LONGITUDE_OF_CENTRE,
        ParameterNames.LONGITUDE_OF_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.FALSE_EASTING), //
      new MultiParameterName(ParameterNames.FALSE_NORTHING) //
    ), null);

    addMethod(1027, "Lambert_Azimuthal_Equal_Area_Spherical", true, false, Arrays.asList(//
      new MultiParameterName(ParameterNames.LATITUDE_OF_ORIGIN, ParameterNames.LATITUDE_OF_CENTRE,
        ParameterNames.LATITUDE_OF_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.CENTRAL_MERIDIAN, ParameterNames.LONGITUDE_OF_CENTRE,
        ParameterNames.LONGITUDE_OF_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.FALSE_EASTING), //
      new MultiParameterName(ParameterNames.FALSE_NORTHING) //
    ), null);

    addMethod(9801, "Lambert_Conic_Conformal_1SP", true, false, Arrays.asList(//
      new MultiParameterName(ParameterNames.LATITUDE_OF_ORIGIN,
        ParameterNames.LATITUDE_OF_NATURAL_ORIGIN, ParameterNames.STANDARD_PARALLEL_1), //
      new MultiParameterName(ParameterNames.CENTRAL_MERIDIAN,
        ParameterNames.LONGITUDE_OF_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.SCALE_FACTOR,
        ParameterNames.SCALE_FACTOR_AT_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.FALSE_EASTING), //
      new MultiParameterName(ParameterNames.FALSE_NORTHING) //
    ), LambertConicConformal1SP::new);

    addMethod(9802, "Lambert_Conic_Conformal_2SP", true, false, Arrays.asList(//
      new MultiParameterName(ParameterNames.LATITUDE_OF_ORIGIN,
        ParameterNames.LATITUDE_OF_FALSE_ORIGIN), //
      new MultiParameterName(ParameterNames.CENTRAL_MERIDIAN,
        ParameterNames.LONGITUDE_OF_FALSE_ORIGIN), //
      new MultiParameterName(ParameterNames.STANDARD_PARALLEL_1,
        ParameterNames.LATITUDE_OF_1ST_STANDARD_PARALLEL), //
      new MultiParameterName(ParameterNames.STANDARD_PARALLEL_2,
        ParameterNames.LATITUDE_OF_2ND_STANDARD_PARALLEL), //
      new MultiParameterName(ParameterNames.FALSE_EASTING, ParameterNames.EASTING_AT_FALSE_ORIGIN), //
      new MultiParameterName(ParameterNames.FALSE_NORTHING, ParameterNames.NORTHING_AT_FALSE_ORIGIN) //
    ), LambertConicConformal1SP::new);

    addMethod(9826, "Lambert_Conic_Conformal_West_Orientated", true, false, Arrays.asList(//
      new MultiParameterName(ParameterNames.LATITUDE_OF_ORIGIN,
        ParameterNames.LATITUDE_OF_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.CENTRAL_MERIDIAN,
        ParameterNames.LONGITUDE_OF_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.SCALE_FACTOR,
        ParameterNames.SCALE_FACTOR_AT_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.FALSE_EASTING), //
      new MultiParameterName(ParameterNames.FALSE_NORTHING) //
    ), null);

    addMethod(9804, "Mercator_1SP", true, false, Arrays.asList(//
      new MultiParameterName(0.0, ParameterNames.LATITUDE_OF_ORIGIN,
        ParameterNames.LATITUDE_OF_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.CENTRAL_MERIDIAN,
        ParameterNames.LONGITUDE_OF_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.SCALE_FACTOR,
        ParameterNames.SCALE_FACTOR_AT_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.FALSE_EASTING), //
      new MultiParameterName(ParameterNames.FALSE_NORTHING) //
    ), Mercator1SP::new, "Mercator_variant_A");

    addMethod(9805, "Mercator_2SP", true, false, Arrays.asList(//
      new MultiParameterName(ParameterNames.STANDARD_PARALLEL_1,
        ParameterNames.LATITUDE_OF_1ST_STANDARD_PARALLEL), //
      new MultiParameterName(ParameterNames.CENTRAL_MERIDIAN,
        ParameterNames.LONGITUDE_OF_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.FALSE_EASTING), //
      new MultiParameterName(ParameterNames.FALSE_NORTHING) //
    ), Mercator2SP::new, "Mercator_variant_B");

    addMethod(1024, "Mercator_Auxiliary_Sphere", true, false, Arrays.asList(//
      new MultiParameterName(ParameterNames.STANDARD_PARALLEL_1,
        ParameterNames.LATITUDE_OF_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.CENTRAL_MERIDIAN,
        ParameterNames.LONGITUDE_OF_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.FALSE_EASTING), //
      new MultiParameterName(ParameterNames.FALSE_NORTHING), //
      new MultiParameterName(0.0, new SingleParameterName("auxiliary_sphere_type")) //
    ), WebMercator::new, "Popular_Visualisation_Pseudo_Mercator");

    addMethod(9809, "Stereographic_North_Pole", true, false, Arrays.asList(//
      new MultiParameterName(ParameterNames.LATITUDE_OF_ORIGIN, ParameterNames.STANDARD_PARALLEL_1,
        ParameterNames.LATITUDE_OF_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.CENTRAL_MERIDIAN,
        ParameterNames.LONGITUDE_OF_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.SCALE_FACTOR,
        ParameterNames.SCALE_FACTOR_AT_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.FALSE_EASTING), //
      new MultiParameterName(ParameterNames.FALSE_NORTHING) //
    ), null, "Oblique_Stereographic", "Double_Stereographic");

    addMethod(9829, "Stereographic_South_Pole", true, false, Arrays.asList(//
      new MultiParameterName(ParameterNames.STANDARD_PARALLEL_1,
        ParameterNames.LATITUDE_OF_STANDARD_PARALLEL), //
      new MultiParameterName(ParameterNames.CENTRAL_MERIDIAN, ParameterNames.LONGITUDE_OF_ORIGIN),
      new MultiParameterName(1.0, ParameterNames.SCALE_FACTOR), //
      new MultiParameterName(ParameterNames.FALSE_EASTING), //
      new MultiParameterName(ParameterNames.FALSE_NORTHING) //
    ), null, "Polar_Stereographic_variant_B");

    final List<ParameterName> transverseMercatorParameterNames = Arrays.asList(//
      new MultiParameterName(ParameterNames.LATITUDE_OF_ORIGIN,
        ParameterNames.LATITUDE_OF_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.CENTRAL_MERIDIAN,
        ParameterNames.LONGITUDE_OF_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.SCALE_FACTOR,
        ParameterNames.SCALE_FACTOR_AT_NATURAL_ORIGIN), //
      new MultiParameterName(ParameterNames.FALSE_EASTING), //
      new MultiParameterName(ParameterNames.FALSE_NORTHING) //
    );
    addMethod(9807, "Transverse_Mercator", true, false, transverseMercatorParameterNames,
      TransverseMercator::new);
    addMethod(9808, "Transverse_Mercator_South_Orientated", true, false,
      transverseMercatorParameterNames, TransverseMercatorSouthOriented::new);

    addMethod(9812, "Hotine_Oblique_Mercator_variant_A", true, false, Arrays.asList(//
      new MultiParameterName(ParameterNames.LATITUDE_OF_CENTRE,
        ParameterNames.LATITUDE_OF_PROJECTION_CENTRE), //
      new MultiParameterName(ParameterNames.LONGITUDE_OF_CENTRE,
        ParameterNames.LONGITUDE_OF_PROJECTION_CENTRE), //
      new MultiParameterName(ParameterNames.AZIMUTH, ParameterNames.AZIMUTH_OF_INITIAL_LINE), //
      new MultiParameterName(ParameterNames.RECTIFIED_GRID_ANGLE,
        ParameterNames.ANGLE_FROM_RECTIFIED_TO_SKEW_GRID), //
      new MultiParameterName(ParameterNames.SCALE_FACTOR,
        ParameterNames.SCALE_FACTOR_ON_INITIAL_LINE), //
      new MultiParameterName(ParameterNames.FALSE_EASTING), //
      new MultiParameterName(ParameterNames.FALSE_NORTHING) //
    ), null);
    // "Hotine_Oblique_Mercator_Azimuth_Natural_Origin");

    addMethod(9815, "Hotine_Oblique_Mercator_variant_B", true, false, Arrays.asList(//
      new MultiParameterName(ParameterNames.LATITUDE_OF_CENTRE,
        ParameterNames.LATITUDE_OF_PROJECTION_CENTRE), //
      new MultiParameterName(ParameterNames.LONGITUDE_OF_CENTRE,
        ParameterNames.LONGITUDE_OF_PROJECTION_CENTRE), //
      new MultiParameterName(ParameterNames.AZIMUTH, ParameterNames.AZIMUTH_OF_INITIAL_LINE), //
      new MultiParameterName(ParameterNames.RECTIFIED_GRID_ANGLE,
        ParameterNames.ANGLE_FROM_RECTIFIED_TO_SKEW_GRID), //
      new MultiParameterName(ParameterNames.SCALE_FACTOR,
        ParameterNames.SCALE_FACTOR_ON_INITIAL_LINE), //
      new MultiParameterName(ParameterNames.FALSE_EASTING,
        ParameterNames.EASTING_AT_PROJECTION_CENTRE), //
      new MultiParameterName(ParameterNames.FALSE_NORTHING,
        ParameterNames.NORTHING_AT_PROJECTION_CENTRE) //
    ), null, "Hotine_Oblique_Mercator_Azimuth_Center",
      "Hotine_Oblique_Mercator_Azimuth_Natural_Origin");

    addMethod(9816, "Tunisia_Mining_Grid", true, false, Arrays.asList(//
      new MultiParameterName(ParameterNames.LATITUDE_OF_ORIGIN,
        ParameterNames.LATITUDE_OF_FALSE_ORIGIN), //
      new MultiParameterName(ParameterNames.CENTRAL_MERIDIAN,
        ParameterNames.LONGITUDE_OF_FALSE_ORIGIN), //
      new MultiParameterName(ParameterNames.FALSE_EASTING, ParameterNames.EASTING_AT_FALSE_ORIGIN), //
      new MultiParameterName(ParameterNames.FALSE_NORTHING, ParameterNames.NORTHING_AT_FALSE_ORIGIN) //
    ), null);

    addAlias(LAMBERT_CONIC_CONFORMAL_2SP_BELGIUM, LAMBERT_CONIC_CONFORMAL_2SP_BELGIUM);

    addAlias(MERCATOR_1SP_SPHERICAL, MERCATOR_1SP_SPHERICAL);
  }

  public static void addAlias(String name, final String normalizedName) {
    name = name.toLowerCase().replaceAll("[^a-z0-9]", "");
    PROJECTION_ALIASES.put(name, normalizedName);
  }

  public static CoordinateOperationMethod addMethod(final int id, final String name,
    final boolean reverse, final boolean deprecated, final List<ParameterName> parameterNames) {
    CoordinateOperationMethod method = METHOD_BY_ID.get(id);
    if (method == null) {
      method = new CoordinateOperationMethod(id, name, reverse, deprecated, parameterNames);
      METHOD_BY_ID.put(id, method);
      if (!deprecated) {
        METHOD_BY_NAME.put(method.getName(), method);
      }
    } else {
      Debug.noOp();
    }
    return method;
  }

  private static void addMethod(final int id, final String name, final boolean reverse,
    final boolean deprecated, final List<ParameterName> parameterNames,
    final Function<ProjectedCoordinateSystem, CoordinatesProjection> coordinatesProjectionFactory,
    final String... aliases) {
    final CoordinateOperationMethod method = new CoordinateOperationMethod(id, name, reverse,
      deprecated, parameterNames, coordinatesProjectionFactory);
    METHOD_BY_ID.put(id, method);
    METHOD_BY_NAME.put(name, method);
    for (final String alias : aliases) {
      METHOD_BY_NAME.put(alias, method);
    }
  }

  public static synchronized CoordinateOperationMethod getMethod(final String name) {
    EpsgCoordinateSystems.initialize();
    CoordinateOperationMethod coordinateOperationMethod = METHOD_BY_NAME.get(name);
    if (coordinateOperationMethod == null) {
      coordinateOperationMethod = new CoordinateOperationMethod(name);
      METHOD_BY_NAME.put(name, coordinateOperationMethod);
    }
    return coordinateOperationMethod;
  }

  public static CoordinateOperationMethod getMethod(String methodName,
    final Map<ParameterName, ?> parameters) {
    if (methodName == null) {
      return null;
    } else {
      if ("Lambert_Conformal_Conic".equals(methodName)) {
        if (parameters.containsKey(ParameterNames.STANDARD_PARALLEL_2)) {
          methodName = "Lambert_Conic_Conformal_2SP";
        } else {
          methodName = "Lambert_Conic_Conformal_1SP";
        }
      } else if ("Mercator".equals(methodName)) {
        if (parameters.containsKey(ParameterNames.STANDARD_PARALLEL_1)) {
          if (parameters.containsKey(ParameterNames.LATITUDE_OF_ORIGIN)) {
            methodName = "Mercator_2SP";
          } else {
            methodName = "Mercator_2SP";
          }
        } else {
          methodName = "Mercator_1SP";
        }
      }
      return getMethod(methodName);
    }
  }

  public static IntHashMap<CoordinateOperationMethod> getMethodById() {
    return METHOD_BY_ID;
  }

  public static String getNormalizedName(final String name) {
    if (name == null) {
      return null;
    } else {
      final String searchName = name.toLowerCase().replaceAll("[^a-z0-9]", "");
      final String normalizedName = PROJECTION_ALIASES.get(searchName);
      if (normalizedName == null) {
        return name;
      } else {
        return normalizedName;
      }
    }
  }

  public static Map<ParameterName, ParameterValue> getParameters(
    final CoordinateOperationMethod method, final Map<ParameterName, Double> values,
    final LinearUnit linearUnit) {
    if (method == null) {
      final Map<ParameterName, ParameterValue> parameters = new LinkedHashMap<>();
      for (final ParameterName parameterName : values.keySet()) {
        final Object value = values.get(parameterName);
        if (value != null) {
          final ParameterValue parameterValue = parameterName.newParameterValue(linearUnit,
            (Double)value);
          parameters.put(parameterName, parameterValue);
        }
      }
      return parameters;
    } else {
      return method.getParameters(values, linearUnit);
    }
  }

  public static String normalizeName(final String name) {
    return name.replaceAll(" ", "_").replaceAll("[^a-zA-Z0-9_]", "");
  }

  private Authority authority;

  private final String name;

  private final String normalizedName;

  private boolean reverse;

  private boolean deprecated;

  private List<ParameterName> parameterNames = new ArrayList<>();

  private Function<ProjectedCoordinateSystem, CoordinatesProjection> coordinatesProjectionFactory;

  public CoordinateOperationMethod(final int id, final String name, final boolean reverse,
    final boolean deprecated, final List<ParameterName> parameterNames) {
    this(normalizeName(name));
    this.authority = new EpsgAuthority(id);
    this.reverse = reverse;
    this.deprecated = deprecated;
    this.parameterNames = parameterNames;
  }

  public CoordinateOperationMethod(final int id, final String name, final boolean reverse,
    final boolean deprecated, final List<ParameterName> parameterNames,
    final Function<ProjectedCoordinateSystem, CoordinatesProjection> coordinatesProjectionFactory) {
    this(id, name, reverse, deprecated, parameterNames);
    this.coordinatesProjectionFactory = coordinatesProjectionFactory;
  }

  public CoordinateOperationMethod(final String name) {
    this.name = name;
    this.normalizedName = getNormalizedName(name);
  }

  @Override
  public int compareTo(final CoordinateOperationMethod coordinateOperationMethod) {
    return getNormalizedName().compareTo(coordinateOperationMethod.getNormalizedName());
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof CoordinateOperationMethod) {
      final CoordinateOperationMethod coordinateOperationMethod = (CoordinateOperationMethod)obj;
      if (Equals.equals(this.authority, coordinateOperationMethod.authority)) {
        return true;
      } else {
        return getNormalizedName().equals(coordinateOperationMethod.getNormalizedName());
      }
    }
    return false;
  }

  public Authority getAuthority() {
    return this.authority;
  }

  public String getName() {
    return this.name;
  }

  public String getNormalizedName() {
    return this.normalizedName;
  }

  public List<ParameterName> getParameterNames() {
    return this.parameterNames;
  }

  public Map<ParameterName, ParameterValue> getParameters(final Map<ParameterName, Double> values,
    final LinearUnit linearUnit) {
    final Map<ParameterName, ParameterValue> parameters = new LinkedHashMap<>();
    final Set<ParameterName> usedNames = new HashSet<>();
    for (final ParameterName parameterName : this.parameterNames) {
      ParameterValue value = parameterName.getValue(linearUnit, values);
      if (value == null) {
        value = parameterName.getDefaultValue();
      } else {
        parameterName.addNames(usedNames);
      }
      parameters.put(parameterName, value);
    }
    for (final ParameterName parameterName : values.keySet()) {
      if (!usedNames.contains(parameterName)) {
        final Object value = values.get(parameterName);
        if (value != null) {
          final ParameterValue parameterValue = parameterName.newParameterValue(linearUnit,
            (Double)value);
          parameters.put(parameterName, parameterValue);
        }
      }
    }
    return parameters;
  }

  @Override
  public int hashCode() {
    return getNormalizedName().hashCode();
  }

  public boolean isDeprecated() {
    return this.deprecated;
  }

  public boolean isReverse() {
    return this.reverse;
  }

  public synchronized CoordinatesProjection newCoordinatesProjection(
    final ProjectedCoordinateSystem coordinateSystem) {
    if (this.coordinatesProjectionFactory == null) {
      return ProjectionFactory.newCoordinatesProjection(coordinateSystem);
    } else {
      return this.coordinatesProjectionFactory.apply(coordinateSystem);
    }
  }

  public boolean setParameter(final Map<ParameterName, ParameterValue> parameterValues,
    final ParameterName parameterName, final ParameterValue parameterValue) {
    if (parameterValues.containsKey(parameterName)) {
      parameterValues.put(parameterName, parameterValue);
      return true;
    } else {
      for (final ParameterName methodParameterName : this.parameterNames) {
        if (methodParameterName.equals(parameterName)) {
          parameterValues.put(methodParameterName, parameterValue);
          return true;
        }
      }
      return false;
    }
  }

  @Override
  public String toString() {
    return getNormalizedName();
  }
}
