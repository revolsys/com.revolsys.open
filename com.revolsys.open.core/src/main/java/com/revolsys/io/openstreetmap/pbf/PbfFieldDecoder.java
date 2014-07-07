// This software is released into the Public Domain.  See copying.txt for details.
package com.revolsys.io.openstreetmap.pbf;

import java.util.Date;

import com.revolsys.io.openstreetmap.pbf.Osmformat.PrimitiveBlock;
import com.revolsys.io.openstreetmap.pbf.Osmformat.StringTable;

/**
 * Manages decoding of the lower level PBF data structures.
 *
 * @author Brett Henderson
 *
 */
public class PbfFieldDecoder {
  private static final double COORDINATE_SCALING_FACTOR = 0.000000001;

  private final String[] strings;

  private final int coordGranularity;

  private final long coordLatitudeOffset;

  private final long coordLongitudeOffset;

  private final int dateGranularity;

  /**
   * Creates a new instance.
   * 
   * @param primitiveBlock
   *            The primitive block containing the fields to be decoded.
   */
  public PbfFieldDecoder(final PrimitiveBlock primitiveBlock) {
    this.coordGranularity = primitiveBlock.getGranularity();
    this.coordLatitudeOffset = primitiveBlock.getLatOffset();
    this.coordLongitudeOffset = primitiveBlock.getLonOffset();
    this.dateGranularity = primitiveBlock.getDateGranularity();

    final StringTable stringTable = primitiveBlock.getStringtable();
    this.strings = new String[stringTable.getSCount()];
    for (int i = 0; i < this.strings.length; i++) {
      this.strings[i] = stringTable.getS(i).toStringUtf8();
    }
  }

  /**
   * Decodes a raw latitude value into degrees.
   * 
   * @param rawLatitude
   *            The PBF encoded value.
   * @return The latitude in degrees.
   */
  public double decodeLatitude(final long rawLatitude) {
    return COORDINATE_SCALING_FACTOR
      * (this.coordLatitudeOffset + this.coordGranularity * rawLatitude);
  }

  /**
   * Decodes a raw longitude value into degrees.
   * 
   * @param rawLongitude
   *            The PBF encoded value.
   * @return The longitude in degrees.
   */
  public double decodeLongitude(final long rawLongitude) {
    return COORDINATE_SCALING_FACTOR
      * (this.coordLongitudeOffset + this.coordGranularity * rawLongitude);
  }

  /**
   * Decodes a raw string into a String.
   * 
   * @param rawString
   *            The PBF encoding string.
   * @return The string as a String.
   */
  public String decodeString(final int rawString) {
    return this.strings[rawString];
  }

  /**
   * Decodes a raw timestamp value into a Date.
   * 
   * @param rawTimestamp
   *            The PBF encoded timestamp.
   * @return The timestamp as a Date.
   */
  public Date decodeTimestamp(final long rawTimestamp) {
    return new Date(this.dateGranularity * rawTimestamp);
  }
}
