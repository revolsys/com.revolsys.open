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
package com.revolsys.jts.algorithm;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import test.jts.TestFiles;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTFileReader;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.util.Stopwatch;

public class InteriorPointTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(InteriorPointTest.class);
  }

  WKTReader rdr = new WKTReader();

  public InteriorPointTest(final String name) {
    super(name);
  }

  private void checkInteriorPoint(final Geometry g) {
    final Point ip = g.getInteriorPoint();
    assertTrue(g.contains(ip));
  }

  void checkInteriorPoint(final List geoms) {
    final Stopwatch sw = new Stopwatch();
    for (final Iterator i = geoms.iterator(); i.hasNext();) {
      final Geometry g = (Geometry)i.next();
      checkInteriorPoint(g);
      System.out.print(".");
    }
    System.out.println();
    System.out.println("  " + sw.getTimeString());
  }

  void checkInteriorPointFile(final String file) throws Exception {
    final WKTFileReader fileRdr = new WKTFileReader(new FileReader(file),
      this.rdr);
    checkInteriorPointFile(fileRdr);
  }

  private void checkInteriorPointFile(final WKTFileReader fileRdr)
    throws IOException, ParseException {
    final List polys = fileRdr.read();
    checkInteriorPoint(polys);
  }

  void checkInteriorPointResource(final String resource) throws Exception {
    final InputStream is = this.getClass().getResourceAsStream(resource);
    final WKTFileReader fileRdr = new WKTFileReader(new InputStreamReader(is),
      this.rdr);
    checkInteriorPointFile(fileRdr);
  }

  public void testAll() throws Exception {
    checkInteriorPointFile(TestFiles.DATA_DIR + "world.wkt");
    checkInteriorPointFile(TestFiles.DATA_DIR + "africa.wkt");
    // checkInteriorPointFile("../../../../../data/africa.wkt");
  }

}
