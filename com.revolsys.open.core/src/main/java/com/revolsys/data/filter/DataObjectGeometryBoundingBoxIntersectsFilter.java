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
package com.revolsys.data.filter;

import java.util.Collection;
import java.util.List;

import com.revolsys.data.record.Record;
import com.revolsys.filter.Filter;
import com.revolsys.filter.FilterUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;

public class DataObjectGeometryBoundingBoxIntersectsFilter implements
  Filter<Record> {
  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public static <D extends Record> List<D> filter(
    final Collection<D> collection, final BoundingBox boundingBox) {
    final Filter filter = new DataObjectGeometryBoundingBoxIntersectsFilter(
      boundingBox);
    return FilterUtil.filter(collection, filter);
  }

  private final BoundingBox boundingBox;

  public DataObjectGeometryBoundingBoxIntersectsFilter(
    final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  @Override
  public boolean accept(final Record object) {
    try {
      final Geometry geometry = object.getGeometryValue();
      if (geometry != null && geometry.intersects(this.boundingBox)) {
        return true;
      } else {
        return false;
      }
    } catch (final Throwable t) {
      t.printStackTrace();
      return false;
    }
  }
}
