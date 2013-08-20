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
package com.revolsys.gis.data.model.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectGeometryFilter<G extends Geometry> implements
  Filter<DataObject> {
  private Filter<G> filter;

  public DataObjectGeometryFilter() {
  }

  public DataObjectGeometryFilter(final Filter<G> filter) {
    this.filter = filter;
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean accept(final DataObject object) {
    final G geometry = (G)object.getGeometryValue();
    if (filter.accept(geometry)) {
      return true;
    } else {
      return false;
    }
  }

  public Filter<G> getFilter() {
    return filter;
  }

  public void setFilter(final Filter<G> filter) {
    this.filter = filter;
  }

}
