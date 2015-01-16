/*
 * $URL:$
 * $Author:$
 * $Date:$
 * $Revision:$

 * Copyright 2004-2007 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.gis.jts.filter;

import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.LineString;

public class LineEqualExactFilter implements Filter<LineString> {
  private final LineString line;

  int axisCount = -1;

  public LineEqualExactFilter(final LineString line) {
    this.line = line;
  }

  public LineEqualExactFilter(final LineString line, final int axisCount) {
    this.line = line;
    this.axisCount = axisCount;
  }

  @Override
  public boolean accept(final LineString line) {
    int axisCount = this.axisCount;
    if (axisCount == -1) {
      axisCount = Math.max(this.line.getAxisCount(), line.getAxisCount());
    }
    final boolean equal = this.line.equals(axisCount, line);
    if (equal) {
      return true;
    } else {
      return false;
    }
  }

}
