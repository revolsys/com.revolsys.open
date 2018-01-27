package com.revolsys.geometry.cs;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.util.Equals;

public class CoordinateOperationMethod implements Serializable, Comparable<CoordinateOperationMethod> {
  public static final String ALBERS_EQUAL_AREA = "Albers_Equal_Area";

  public static final String LAMBERT_CONIC_CONFORMAL_1SP = "Lambert_Conic_Conformal_(1SP)";

  public static final String LAMBERT_CONIC_CONFORMAL_2SP = "Lambert_Conic_Conformal_(2SP)";

  public static final String LAMBERT_CONIC_CONFORMAL_2SP_BELGIUM = "Lambert_Conic_Conformal_(2SP_Belgium)";

  public static final String MERCATOR = "Mercator";

  public static final String MERCATOR_1SP = "Mercator_(1SP)";

  public static final String MERCATOR_1SP_SPHERICAL = "Mercator_(1SP)_(Spherical)";

  public static final String MERCATOR_2SP = "Mercator_(2SP)";

  public static final String POPULAR_VISUALISATION_PSEUDO_MERCATOR = "Popular_Visualisation_Pseudo_Mercator";

  private static final Map<String, String> PROJECTION_ALIASES = new TreeMap<>();

  /**
   *
   */
  private static final long serialVersionUID = 6199958151692874551L;

  public static final String TRANSVERSE_MERCATOR = "Transverse_Mercator";

  static {
    for (final String alias : Arrays.asList(ALBERS_EQUAL_AREA, "Albers", "Albers_Equal_Area_Conic",
      "Albers_Conic_Equal_Area")) {
      addAlias(alias, ALBERS_EQUAL_AREA);
    }

    addAlias(LAMBERT_CONIC_CONFORMAL_1SP, LAMBERT_CONIC_CONFORMAL_1SP);

    addAlias(LAMBERT_CONIC_CONFORMAL_2SP, LAMBERT_CONIC_CONFORMAL_2SP);

    addAlias(LAMBERT_CONIC_CONFORMAL_2SP_BELGIUM, LAMBERT_CONIC_CONFORMAL_2SP_BELGIUM);

    addAlias(MERCATOR, MERCATOR);

    addAlias(MERCATOR_1SP, MERCATOR_1SP);

    addAlias(MERCATOR_1SP_SPHERICAL, MERCATOR_1SP_SPHERICAL);

    addAlias(MERCATOR_2SP, MERCATOR_2SP);

    addAlias(POPULAR_VISUALISATION_PSEUDO_MERCATOR, POPULAR_VISUALISATION_PSEUDO_MERCATOR);
  }

  public static void addAlias(String name, final String normalizedName) {
    name = name.toLowerCase().replaceAll("[^a-z0-9]", "");
    PROJECTION_ALIASES.put(name, normalizedName);
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

  private Authority authority;

  private final String name;

  private final String normalizedName;

  private boolean reverse;

  private boolean deprecated;

  public CoordinateOperationMethod(final Authority authority, final String name, final boolean reverse,
    final boolean deprecated) {
    this(name);
    this.authority = authority;
    this.reverse = reverse;
    this.deprecated = deprecated;
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

  @Override
  public String toString() {
    return getNormalizedName();
  }
}
