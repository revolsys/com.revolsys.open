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
package com.revolsys.jtstest.util;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKBReader;
import com.revolsys.jts.io.WKTReader;

/**
 * Reads a {@link Geometry} from a string which is in either WKT or WKBHex format
 *
 * @author Martin Davis
 * @version 1.7
 */
public class WKTOrWKBReader {
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

  private static final int MAX_CHARS_TO_CHECK = 6;

  private final WKTReader wktReader;

  private final WKBReader wkbReader;

  public WKTOrWKBReader(final GeometryFactory geomFactory) {
    this.wktReader = new WKTReader(geomFactory);
    this.wkbReader = new WKBReader(geomFactory);
  }

  public Geometry read(final String geomStr) throws ParseException {
    final String trimStr = geomStr.trim();
    if (isHex(trimStr, MAX_CHARS_TO_CHECK)) {
      return this.wkbReader.read(WKBReader.hexToBytes(trimStr));
    }
    return this.wktReader.read(trimStr);
  }
}
