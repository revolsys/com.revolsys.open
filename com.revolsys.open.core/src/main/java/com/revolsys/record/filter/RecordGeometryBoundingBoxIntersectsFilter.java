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
package com.revolsys.record.filter;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.predicate.Predicates;
import com.revolsys.record.Record;

public class RecordGeometryBoundingBoxIntersectsFilter implements Predicate<Record> {
  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public static <D extends Record> List<D> predicate(final Collection<D> collection,
    final BoundingBox boundingBox) {
    final Predicate predicate = new RecordGeometryBoundingBoxIntersectsFilter(boundingBox);
    return Predicates.filter(collection, predicate);
  }

  private final BoundingBox boundingBox;

  public RecordGeometryBoundingBoxIntersectsFilter(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  @Override
  public boolean test(final Record object) {
    try {
      final Geometry geometry = object.getGeometry();
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
