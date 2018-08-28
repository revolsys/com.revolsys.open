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
package com.revolsys.core.test.geometry.test.util;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.wkb.ParseException;
import com.revolsys.geometry.wkb.WKBReader;

/**
 * Reads a {@link Geometry} from a string which is in either WKT or WKBHex format
 *
 * @author Martin Davis
 * @version 1.7
 */
public class WKTOrWKBReader {
  private static final int MAX_CHARS_TO_CHECK = 6;

  public static boolean isHex(final String geometryText) {
    for (int i = 0; i < MAX_CHARS_TO_CHECK && i < geometryText.length(); i++) {
      final char ch = geometryText.charAt(i);
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

  private final WKBReader wkbReader;

  private final GeometryFactory geometryFactory;

  public WKTOrWKBReader(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    this.wkbReader = new WKBReader(geometryFactory);
  }

  public Geometry read(final String geomStr) throws ParseException {
    final String trimStr = geomStr.trim();
    if (isHex(trimStr)) {
      return this.wkbReader.read(WKBReader.hexToBytes(trimStr));
    } else {
      return this.geometryFactory.geometry(trimStr);
    }
  }
}
