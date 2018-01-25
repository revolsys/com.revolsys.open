package com.revolsys.geometry.cs.epsg;

import com.revolsys.geometry.cs.Area;
import com.revolsys.geometry.cs.Projection;

public class CoordinateOperation {
  private final int id;

  private final Projection method;

  private final String name;

  private final byte type;

  private final int sourceCrsCode;

  private final int targetCrsCode;

  private final String transformationVersion;

  private final int variant;

  private final Area area;

  private final double accuracy;

  private final boolean deprecated;

  public CoordinateOperation(final int id, final Projection method, final String name,
    final byte type, final int sourceCrsCode, final int targetCrsCode,
    final String transformationVersion, final int variant, final Area area, final double accuracy,
    final boolean deprecated) {
    this.id = id;
    this.method = method;
    this.name = name;
    this.type = type;
    this.sourceCrsCode = sourceCrsCode;
    this.targetCrsCode = targetCrsCode;
    this.transformationVersion = transformationVersion;
    this.variant = variant;
    this.area = area;
    this.accuracy = accuracy;
    this.deprecated = deprecated;
  }

  public double getAccuracy() {
    return this.accuracy;
  }

  public Area getArea() {
    return this.area;
  }

  public int getId() {
    return this.id;
  }

  public Projection getMethod() {
    return this.method;
  }

  public String getName() {
    return this.name;
  }

  public int getSourceCrsCode() {
    return this.sourceCrsCode;
  }

  public int getTargetCrsCode() {
    return this.targetCrsCode;
  }

  public String getTransformationVersion() {
    return this.transformationVersion;
  }

  public byte getType() {
    return this.type;
  }

  public int getVariant() {
    return this.variant;
  }

  public boolean isDeprecated() {
    return this.deprecated;
  }
}
