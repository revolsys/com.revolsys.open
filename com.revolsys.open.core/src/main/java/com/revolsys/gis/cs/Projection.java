package com.revolsys.gis.cs;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.data.equals.Equals;

public class Projection implements Serializable, Comparable<Projection> {
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

  public Projection(final String name) {
    this.name = name;
    this.normalizedName = getNormalizedName(name);
  }

  public Projection(final String name, final Authority authority) {
    this(name);
    this.authority = authority;
  }

  @Override
  public int compareTo(final Projection projection) {
    return getNormalizedName().compareTo(projection.getNormalizedName());
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Projection) {
      final Projection projection = (Projection)obj;
      if (Equals.equal(this.authority, projection.authority)) {
        return true;
      } else {
        return getNormalizedName().equals(projection.getNormalizedName());
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

  @Override
  public String toString() {
    return getNormalizedName();
  }
}
