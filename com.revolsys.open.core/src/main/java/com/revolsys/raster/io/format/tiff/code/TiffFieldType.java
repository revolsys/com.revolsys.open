package com.revolsys.raster.io.format.tiff.code;

import java.util.HashMap;
import java.util.Map;

import org.jeometry.common.function.Function3;

import com.revolsys.raster.io.format.tiff.TiffDirectory;
import com.revolsys.raster.io.format.tiff.TiffDirectoryEntry;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryAscii;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryDouble;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryDoubleArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryFloat;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryFloatArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntrySignedByte;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntrySignedByteArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntrySignedInt;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntrySignedIntArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntrySignedLong;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntrySignedLongArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntrySignedRational;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntrySignedRationalArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntrySignedShort;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntrySignedShortArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUnsignedByte;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUnsignedByteArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUnsignedInt;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUnsignedIntArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUnsignedLong;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUnsignedLongArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUnsignedRational;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUnsignedRationalArray;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUnsignedShort;
import com.revolsys.raster.io.format.tiff.directory.entry.TiffDirectoryEntryUnsignedShortArray;

public enum TiffFieldType {
  // 8-bit unsigned integer
  BYTE(//
    1, //
    TiffDirectoryEntryUnsignedByte::new, //
    TiffDirectoryEntryUnsignedByteArray::newEntry//
  ),
  // 8-bit byte that contains a 7-bit ASCII code; the last
  // byte must be NUL (binary zero)
  ASCII(//
    2, //
    TiffDirectoryEntryAscii::new//
  ),
  // 16-bit (2-byte) unsigned integer
  SHORT(//
    3, //
    TiffDirectoryEntryUnsignedShort::new, //
    TiffDirectoryEntryUnsignedShortArray::newEntry//
  ),
  // 32-bit (4-byte) unsigned integer
  LONG(//
    4, //
    TiffDirectoryEntryUnsignedInt::new, //
    TiffDirectoryEntryUnsignedIntArray::newEntry//
  ),
  // Two LONGs: the first represents the
  // numerator of a fraction; the second, the
  // denominator
  RATIONAL(//
    5, //
    TiffDirectoryEntryUnsignedRational::new, //
    TiffDirectoryEntryUnsignedRationalArray::new//
  ),
  // 8-bit signed (twos-complement) integer.
  SBYTE(//
    6, //
    TiffDirectoryEntrySignedByte::new, //
    TiffDirectoryEntrySignedByteArray::newEntry//
  ),
  // 8-bit byte that may contain anything,
  // depending on the definition of the field
  UNDEFINED(//
    7, //
    TiffDirectoryEntryUnsignedByte::new, //
    TiffDirectoryEntrySignedByteArray::newEntry//
  ),
  // 16-bit (2-byte) signed (twos-complement) integer
  SSHORT(//
    8, //
    TiffDirectoryEntrySignedShort::new, //
    TiffDirectoryEntrySignedShortArray::newEntry//
  ),
  // 32-bit (4-byte) signed (twos-complement) integer
  SLONG(//
    9, //
    TiffDirectoryEntrySignedInt::new, //
    TiffDirectoryEntrySignedIntArray::newEntry//
  ),
  // Two SLONG’s: the first represents the
  // numerator of a fraction, the second the
  // denominator
  SRATIONAL(//
    10, //
    TiffDirectoryEntrySignedRational::new, //
    TiffDirectoryEntrySignedRationalArray::new//
  ),
  // Single precision (4-byte) IEEE format
  FLOAT(//
    11, //
    TiffDirectoryEntryFloat::new, //
    TiffDirectoryEntryFloatArray::newEntry//
  ),
  // Double precision (8-byte) IEEE format
  DOUBLE(//
    12, //
    TiffDirectoryEntryDouble::new, //
    TiffDirectoryEntryDoubleArray::newEntry//
  ),
  // 64-bit (8-byte) unsigned (twos-complement) integer
  LONG8(//
    16, //
    TiffDirectoryEntryUnsignedLong::new, //
    TiffDirectoryEntryUnsignedLongArray::newEntry//
  ),
  // 64-bit (8-byte) signed (twos-complement) integer
  SLONG8(//
    17, //
    TiffDirectoryEntrySignedLong::new, //
    TiffDirectoryEntrySignedLongArray::newEntry//
  );

  private static Map<Integer, TiffFieldType> ENUM_BY_TYPE = new HashMap<>();

  static {
    for (final TiffFieldType fieldType : TiffFieldType.values()) {
      ENUM_BY_TYPE.put(fieldType.type, fieldType);
    }
  }

  public static TiffFieldType valueByType(final int type) {
    return ENUM_BY_TYPE.get(type);
  }

  private final int type;

  private final Function3<TiffFieldType, TiffTag, TiffDirectory, TiffDirectoryEntry> newDirectoryEntryFunction;

  private final Function3<TiffFieldType, TiffTag, TiffDirectory, TiffDirectoryEntry> newDirectoryEntryArrayFunction;

  private TiffFieldType(final int type,
    final Function3<TiffFieldType, TiffTag, TiffDirectory, TiffDirectoryEntry> newDirectoryEntryFunction) {
    this(type, newDirectoryEntryFunction, null);
  }

  private TiffFieldType(final int type,
    final Function3<TiffFieldType, TiffTag, TiffDirectory, TiffDirectoryEntry> newDirectoryEntryFunction,
    final Function3<TiffFieldType, TiffTag, TiffDirectory, TiffDirectoryEntry> newDirectoryEntryArrayFunction) {
    this.type = type;
    this.newDirectoryEntryFunction = newDirectoryEntryFunction;
    this.newDirectoryEntryArrayFunction = newDirectoryEntryArrayFunction;
  }

  public int getType() {
    return this.type;
  }

  public TiffDirectoryEntry newDirectoryEntry(final TiffTag tag, final TiffDirectory directory) {
    return this.newDirectoryEntryFunction.apply(this, tag, directory);
  }

  public TiffDirectoryEntry newDirectoryEntryArray(final TiffTag tag,
    final TiffDirectory directory) {
    if (this.newDirectoryEntryArrayFunction == null) {
      throw new IllegalArgumentException("Array of " + this + " not supported");
    } else {
      return this.newDirectoryEntryArrayFunction.apply(this, tag, directory);
    }
  }
}
