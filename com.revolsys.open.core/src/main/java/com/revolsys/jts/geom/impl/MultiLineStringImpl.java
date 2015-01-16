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
package com.revolsys.jts.geom.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.prep.PreparedMultiLineString;

/**
 * Models a collection of (@link LineString}s.
 * <p>
 * Any collection of LineStrings is a valid MultiLineString.
 *
 *@version 1.7
 */
public class MultiLineStringImpl extends AbstractMultiLineString implements
MultiLineString {

  private static final long serialVersionUID = 8166665132445433741L;

  private final GeometryFactory geometryFactory;

  private LineString[] lines;

  public MultiLineStringImpl(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public MultiLineStringImpl(final GeometryFactory geometryFactory,
    final LineString[] lines) {
    this.geometryFactory = geometryFactory;
    if (lines == null || lines.length == 0) {
      this.lines = null;
    } else if (hasNullElements(lines)) {
      throw new IllegalArgumentException(
          "geometries must not contain null elements");
    } else {
      this.lines = new LineString[lines.length];
      for (int i = 0; i < lines.length; i++) {
        final LineString line = lines[i];
        this.lines[i] = geometryFactory.lineString(line);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> List<V> getGeometries() {
    if (this.lines == null) {
      return new ArrayList<V>();
    } else {
      return (List<V>)new ArrayList<>(Arrays.asList(this.lines));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V getGeometry(final int n) {
    if (this.lines == null) {
      return null;
    } else {
      return (V)this.lines[n];
    }
  }

  @Override
  public int getGeometryCount() {
    if (this.lines == null) {
      return 0;
    } else {
      return this.lines.length;
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public boolean isEmpty() {
    return this.lines == null;
  }

  @Override
  public MultiLineString prepare() {
    return new PreparedMultiLineString(this);
  }

}
