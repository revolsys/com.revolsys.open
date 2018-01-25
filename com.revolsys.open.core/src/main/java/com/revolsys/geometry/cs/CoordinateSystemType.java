package com.revolsys.geometry.cs;

import java.util.Arrays;
import java.util.List;

public class CoordinateSystemType {
  public static final List<String> TYPE_NAMES = Arrays.asList("spherical", "ellipsoidal",
    "Cartesian", "vertical");

  private final int id;

  private final int type;

  private final boolean deprecated;

  public CoordinateSystemType(final int id, final int type, final boolean deprecated) {
    this.id = id;
    this.type = type;
    this.deprecated = deprecated;
  }

  public int getId() {
    return this.id;
  }

  public int getType() {
    return this.type;
  }

  public String getTypeName() {
    return TYPE_NAMES.get(this.type);
  }

  public boolean isDeprecated() {
    return this.deprecated;
  }

  @Override
  public String toString() {
    return getTypeName();
  }
}
