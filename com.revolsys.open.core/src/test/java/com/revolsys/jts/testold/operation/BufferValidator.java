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
package com.revolsys.jts.testold.operation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;

/**
 * @version 1.7
 */
public class BufferValidator {

  private static abstract class Test implements Comparable<Test> {
    private final String name;

    private final int priority;

    public Test(final String name) {
      this(name, 2);
    }

    public Test(final String name, final int priority) {
      this.name = name;
      this.priority = priority;
    }

    @Override
    public int compareTo(final Test test) {
      return this.priority - test.priority;
    }

    public String getName() {
      return this.name;
    }

    public abstract void test() throws Exception;

    @Override
    public String toString() {
      return getName();
    }
  }

  public static void main(final String[] args) throws Exception {
    final Geometry g = new WKTReader().read("MULTILINESTRING (( 635074.5418406526 6184832.4888257105, 635074.5681951842 6184832.571842485, 635074.6472587794 6184832.575795664 ), ( 635074.6657069515 6184832.53889932, 635074.6933792098 6184832.451929366, 635074.5642420045 6184832.474330718 ))");
    // System.out.println(g);
    // System.out.println(g.buffer(0.01, 100));
    // System.out.println("END");
  }

  private Geometry original;

  private final double bufferDistance;

  private final Map<String, Test> nameToTestMap = new HashMap<String, Test>();

  private Geometry buffer;

  private static final int QUADRANT_SEGMENTS_1 = 100;

  private static final int QUADRANT_SEGMENTS_2 = 50;

  private final String wkt;

  private WKTReader wktReader;

  public BufferValidator(final double bufferDistance, final String wkt)
    throws ParseException {
    this(bufferDistance, wkt, true);
  }

  public BufferValidator(final double bufferDistance, final String wkt,
    final boolean addContainsTest) throws ParseException {
    // SRID = 888 is to test that SRID is preserved in computed buffers
    setFactory(new PrecisionModel(), 888);
    this.bufferDistance = bufferDistance;
    this.wkt = wkt;
    if (addContainsTest) {
      addContainsTest();
      // addBufferResultValidatorTest();
    }
  }

  private void addContainsTest() {
    addTest(new Test("Contains Test") {
      private boolean contains(final Geometry a, final Geometry b) {
        // JTS doesn't currently handle empty geometries correctly [Jon Aquino
        // 10/29/2003]
        if (b.isEmpty()) {
          return true;
        }
        final boolean isContained = a.contains(b);
        return isContained;
      }

      @Override
      public void test() throws Exception {
        final Geometry original = getOriginal();
        if (original.getClass() == GeometryCollection.class) {
          return;
        }
        final boolean valid = original.isValid();
        com.revolsys.jts.util.Assert.isTrue(valid);
        final Geometry buffer = getBuffer();
        if (BufferValidator.this.bufferDistance > 0) {
          Assert.assertTrue(supplement("Expected buffer to contain original"),
            contains(buffer, original));
        } else {
          Assert.assertTrue(supplement("Expected original to contain buffer"),
            contains(original, buffer));
        }
      }
    });
  }

  private BufferValidator addTest(final Test test) {
    this.nameToTestMap.put(test.getName(), test);
    return this;
  }

  private Geometry getBuffer() throws ParseException {
    if (this.buffer == null) {
      final Geometry original = getOriginal();
      this.buffer = original.buffer(this.bufferDistance, QUADRANT_SEGMENTS_1);
      if (buffer.getClass() == GeometryCollection.class && buffer.isEmpty()) {
        try {
          // #contains doesn't work with GeometryCollections [Jon Aquino
          // 10/29/2003]
          this.buffer = this.wktReader.read("POINT EMPTY");
        } catch (final ParseException e) {
          com.revolsys.jts.util.Assert.shouldNeverReachHere();
        }
      }
    }
    return this.buffer;
  }

  private Geometry getOriginal() throws ParseException {
    if (this.original == null) {
      this.original = this.wktReader.read(this.wkt);
    }
    return this.original;
  }

  public BufferValidator setBufferHolesExpected(
    final boolean bufferHolesExpected) {
    return addTest(new Test("Buffer Holes Test") {
      private boolean hasHoles(final Geometry buffer) {
        if (buffer.isEmpty()) {
          return false;
        }
        if (buffer instanceof Polygon) {
          return ((Polygon)buffer).getNumInteriorRing() > 0;
        }
        final MultiPolygon multiPolygon = (MultiPolygon)buffer;
        for (int i = 0; i < multiPolygon.getGeometryCount(); i++) {
          if (hasHoles(multiPolygon.getGeometry(i))) {
            return true;
          }
        }
        return false;
      }

      @Override
      public void test() throws Exception {
        Assert.assertTrue(supplement("Expected buffer "
          + (bufferHolesExpected ? "" : "not ") + "to have holes"),
          hasHoles(getBuffer()) == bufferHolesExpected);
      }
    });
  }

  public BufferValidator setEmptyBufferExpected(
    final boolean emptyBufferExpected) {
    return addTest(new Test("Empty Buffer Test", 1) {
      @Override
      public void test() throws Exception {
        Assert.assertTrue(supplement("Expected buffer "
          + (emptyBufferExpected ? "" : "not ") + "to be empty"),
          emptyBufferExpected == getBuffer().isEmpty());
      }
    });
  }

  public BufferValidator setExpectedArea(final double expectedArea) {
    return addTest(new Test("Area Test") {
      @Override
      public void test() throws Exception {
        final double tolerance = Math.abs(getBuffer().getArea()
          - getOriginal().buffer(BufferValidator.this.bufferDistance,
            QUADRANT_SEGMENTS_1 - QUADRANT_SEGMENTS_2).getArea());
        Assert.assertEquals(getName(), expectedArea, getBuffer().getArea(),
          tolerance);
      }
    });
  }

  public BufferValidator setFactory(final PrecisionModel precisionModel,
    final int srid) {
    this.wktReader = new WKTReader(GeometryFactory.getFactory(0,
      precisionModel.getScale()));
    return this;
  }

  public BufferValidator setPrecisionModel(final PrecisionModel precisionModel) {
    this.wktReader = new WKTReader(GeometryFactory.getFactory(0,
      precisionModel.getScale()));
    return this;
  }

  private String supplement(final String message) throws ParseException {
    String newMessage = "\n" + message + "\n";
    newMessage += "Original: " + getOriginal() + "\n";
    newMessage += "Buffer Distance: " + this.bufferDistance + "\n";
    newMessage += "Buffer: " + getBuffer() + "\n";
    return newMessage.substring(0, newMessage.length() - 1);
  }

  public void test() throws Exception {
    try {
      final Collection<Test> tests = this.nameToTestMap.values();
      for (final Test test : tests) {
        test.test();
      }
    } catch (final Exception e) {
      throw new Exception(supplement(e.toString()), e);
    }
  }

}
