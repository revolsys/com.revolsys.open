package com.revolsys.elevation.cloud.las;

import java.util.Map;
import java.util.TreeMap;

public enum LasClassification {
  DEFAULT((byte)0, "Created, never classified"), //
  UNCLASSIFIED((byte)1, "Unclassified"), //
  GROUND((byte)2, "Ground"), //
  LOW_VEGITATION((byte)3, "Low Vegitation"), //
  MEDIUM_VEGITATION((byte)4, "Medium Vegitation"), //
  HIGH_VEGITATION((byte)5, "High Vegitation"), //
  BUILDING((byte)6, "Building"), //
  LOW_POINT((byte)7, "Low Point (noise)"), //
  MODEL_KEY_POINT((byte)8, "Model Key-point (mass point)"), //
  WATER((byte)9, "Water"), //
  RESERVED_10((byte)10, ""), //
  RESERVED_11((byte)11, ""), //
  OVERLAP_POINTS((byte)12, "Overlap Points"), //
  RESERVED_13((byte)13, ""), //
  RESERVED_14((byte)14, ""), //
  RESERVED_15((byte)15, ""), //
  RESERVED_16((byte)16, ""), //
  RESERVED_17((byte)17, ""), //
  RESERVED_18((byte)18, ""), //
  RESERVED_19((byte)19, ""), //
  RESERVED_20((byte)20, ""), //
  RESERVED_21((byte)21, ""), //
  RESERVED_22((byte)22, ""), //
  RESERVED_23((byte)23, ""), //
  RESERVED_24((byte)24, ""), //
  RESERVED_25((byte)25, ""), //
  RESERVED_26((byte)26, ""), //
  RESERVED_27((byte)27, ""), //
  RESERVED_28((byte)28, ""), //
  RESERVED_29((byte)29, ""), //
  RESERVED_30((byte)30, ""), //
  RESERVED_31((byte)31, "");

  private static final Map<Byte, LasClassification> ENUM_BY_CODE = new TreeMap<>();

  static {
    for (final LasClassification classification : values()) {
      final byte code = classification.code;

      ENUM_BY_CODE.put(code, classification);
    }
  }

  public static LasClassification enumByCode(final byte code) {
    return ENUM_BY_CODE.get(code);
  }

  private byte code;

  private String description;

  private LasClassification(final byte code, final String description) {
    this.code = code;
    this.description = description;
  }

  public boolean equals(final byte code) {
    return this.code == code;
  }

  public byte getCode() {
    return this.code;
  }

  public String getDescription() {
    return this.description;
  }
}
