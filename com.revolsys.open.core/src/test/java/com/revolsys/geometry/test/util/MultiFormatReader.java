/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.test.util;

import java.io.IOException;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.wkb.ParseException;
import com.revolsys.geometry.wkb.WKBReader;
import com.revolsys.geometry.wkb.WKTReader;

/**
 * Reads a {@link Geometry} from a string which is in either WKT, WKBHex
 * or GML format
 *
 * @author Martin Davis
 * @version 1.7
 */
public class MultiFormatReader {
  public static final int FORMAT_GML = 3;

  public static final int FORMAT_UNKNOWN = 0;

  public static final int FORMAT_WKB = 2;

  public static final int FORMAT_WKT = 1;

  private static final int MAX_CHARS_TO_CHECK = 6;

  public static int format(final String s) {
    if (isWKB(s)) {
      return FORMAT_WKB;
    }
    if (isGML(s)) {
      return FORMAT_GML;
    }
    if (isWKT(s)) {
      return FORMAT_WKT;
    }
    return FORMAT_UNKNOWN;
  }

  public static boolean isGML(final String str) {
    return str.indexOf("<") >= 0;
  }

  private static boolean isHex(final String str, final int maxCharsToTest) {
    for (int i = 0; i < maxCharsToTest && i < str.length(); i++) {
      final char ch = str.charAt(i);
      if (!isHexDigit(ch)) {
        return false;
      }
    }
    return true;
  }

  private static boolean isHexDigit(final char ch) {
    if (Character.isDigit(ch)) {
      return true;
    }
    final char chLow = Character.toLowerCase(ch);
    if (chLow >= 'a' && chLow <= 'f') {
      return true;
    }
    return false;
  }

  public static boolean isWKB(final String str) {
    return isHex(str, MAX_CHARS_TO_CHECK);
  }

  public static boolean isWKT(final String s) {
    return !isWKB(s) && !isGML(s);
  }

  private final GeometryFactory geomFactory;

  private final WKBReader wkbReader;

  private final WKTReader wktReader;

  public MultiFormatReader() {
    this(GeometryFactory.DEFAULT);
  }

  public MultiFormatReader(final GeometryFactory geomFactory) {
    this.geomFactory = geomFactory;
    this.wktReader = new WKTReader(geomFactory);
    this.wkbReader = new WKBReader(geomFactory);
  }

  public Geometry read(final String geomStr) throws ParseException, IOException {
    final String trimStr = geomStr.trim();
    if (isWKB(trimStr)) {
      return IOUtil.readGeometriesFromWKBHexString(trimStr, this.geomFactory);
    }
    if (isGML(trimStr)) {
      return readGML(trimStr);
    }

    return IOUtil.readGeometriesFromWKTString(trimStr, this.geomFactory);
  }

  private Geometry readGML(final String str) throws ParseException {
    return null;
    // try {
    // return (new GMLReader()).read(str, geomFactory);
    // }
    // catch (Exception ex) {
    // throw new ParseException(ex.getMessage());
    // // ex.printStackTrace();
    // }
  }
}
