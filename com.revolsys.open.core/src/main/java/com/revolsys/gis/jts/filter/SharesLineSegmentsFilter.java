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

import java.util.function.Predicate;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.segment.Segment;

public class SharesLineSegmentsFilter implements Predicate<LineString> {
  private final LineString line;

  public SharesLineSegmentsFilter(final LineString line) {
    this.line = line;
  }

  @Override
  public boolean test(final LineString line) {
    for (final Segment segment1 : this.line.segments()) {
      for (final Segment segment2 : line.segments()) {
        if (segment1.equals(2, segment2)) {
          return true;
        }
      }
    }
    return false;
  }
}
