package com.revolsys.elevation.cloud.las.zip;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.revolsys.collection.map.IntHashMap;
import com.revolsys.elevation.cloud.las.zip.v1.LasZipItemCodecGpsTime11V1;
import com.revolsys.elevation.cloud.las.zip.v1.LasZipItemCodecPoint10V1;
import com.revolsys.elevation.cloud.las.zip.v1.LasZipItemCodecRgb12V1;
import com.revolsys.elevation.cloud.las.zip.v2.LasZipItemCodecGpsTime11V2;
import com.revolsys.elevation.cloud.las.zip.v2.LasZipItemCodecPoint10V2;
import com.revolsys.elevation.cloud.las.zip.v2.LasZipItemCodecRgb12V2;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingCodec;

public enum LasZipItemType {
  BYTE(0, -1), // Only used for non-standard record sizes. Not supported
  SHORT(1, -1), // Not used
  INT(2, -1), // Not used
  LONG(3, -1), // Not used
  FLOAT(4, -1), // Not used
  DOUBLE(5, -1), // Not used
  POINT10(6, 20), //
  GPSTIME11(7, 8), //
  RGB12(8, 6), //
  WAVEPACKET13(9, 29), //
  POINT14(10, 30), //
  RGB14(11, 6), //
  RGBNIR14(12, 8), //
  WAVEPACKET14(13, 29), //
  BYTE14(14, -1) //
  ;

  private static final Map<Integer, LasZipItemType> TYPES = new HashMap<>();

  static {
    for (final LasZipItemType type : values()) {
      TYPES.put(type.id, type);
    }

    POINT10.addCodec(1, LasZipItemCodecPoint10V1::new);
    GPSTIME11.addCodec(1, LasZipItemCodecGpsTime11V1::new);
    RGB12.addCodec(1, LasZipItemCodecRgb12V1::new);

    POINT10.addCodec(2, LasZipItemCodecPoint10V2::new);
    GPSTIME11.addCodec(2, LasZipItemCodecGpsTime11V2::new);
    RGB12.addCodec(2, LasZipItemCodecRgb12V2::new);
  }

  public static LasZipItemType fromId(final int i) {
    return TYPES.get(i);
  }

  private int id;

  private int size;

  private final IntHashMap<Function<ArithmeticCodingCodec, LasZipItemCodec>> codecByVersion = new IntHashMap<>();

  private LasZipItemType(final int id, final int size) {
    this.id = id;
    this.size = size;
  }

  private void addCodec(final int version,
    final Function<ArithmeticCodingCodec, LasZipItemCodec> codecConstructor) {
    this.codecByVersion.put(version, codecConstructor);
  }

  public int getId() {
    return this.id;
  }

  public int getSize() {
    return this.size;
  }

  public LasZipItemCodec newCodec(final int version, final ArithmeticCodingCodec codec) {
    final Function<ArithmeticCodingCodec, LasZipItemCodec> codecConstructor = this.codecByVersion
      .get(version);
    if (codecConstructor == null) {
      throw new IllegalArgumentException(
        "LasZip item type " + name() + " version " + version + " not currently supported");
    } else {
      return codecConstructor.apply(codec);
    }
  }
}