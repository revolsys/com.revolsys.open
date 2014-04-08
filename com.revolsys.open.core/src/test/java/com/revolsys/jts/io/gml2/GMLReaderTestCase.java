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
package com.revolsys.jts.io.gml2;

import java.io.FileReader;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.PrecisionModel;

public class GMLReaderTestCase extends TestCase {

  private static final String TEST_DIR = "bin/data/";

  protected static PrecisionModel precisionModel = new PrecisionModel(1000);

  protected static GeometryFactory geometryFactory = new GeometryFactory(
    precisionModel);

  public GMLReaderTestCase(final String arg0) {
    super(arg0);
    // TODO Auto-generated constructor stub
  }

  public void testLineStringRead() throws SAXException, IOException,
    ParserConfigurationException {
    final FileReader fr = new FileReader(TEST_DIR + "linestrings.xml");

    final GMLReader gr = new GMLReader();
    final Geometry g = gr.read(fr, geometryFactory);

    final GeometryCollection gc = (GeometryCollection)g;
    assertTrue(gc.getNumGeometries() == 25);

    for (int i = 0; i < 25; i++) {
      final LineString ls = (LineString)gc.getGeometryN(i);
      assertNotNull(ls);
    }
  }

  public void testMultiLineStringRead() throws SAXException, IOException,
    ParserConfigurationException {
    final FileReader fr = new FileReader(TEST_DIR + "multilinestrings.xml");

    final GMLReader gr = new GMLReader();
    final Geometry g = gr.read(fr, geometryFactory);

    final GeometryCollection gc = (GeometryCollection)g;
    assertTrue(gc.getNumGeometries() == 25);

    for (int i = 0; i < 25; i++) {
      final MultiLineString ls = (MultiLineString)gc.getGeometryN(i);
      assertNotNull(ls);
    }
  }

  public void testMultiPointRead() throws SAXException, IOException,
    ParserConfigurationException {
    final FileReader fr = new FileReader(TEST_DIR + "multipoints.xml");

    final GMLReader gr = new GMLReader();
    final Geometry g = gr.read(fr, geometryFactory);

    final GeometryCollection gc = (GeometryCollection)g;
    assertTrue(gc.getNumGeometries() == 25);

    for (int i = 0; i < 25; i++) {
      final MultiPoint p = (MultiPoint)gc.getGeometryN(i);
      assertNotNull(p);
    }
  }

  public void testMultiPolygonRead() throws SAXException, IOException,
    ParserConfigurationException {
    final FileReader fr = new FileReader(TEST_DIR + "multipolygons.xml");

    final GMLReader gr = new GMLReader();
    final Geometry g = gr.read(fr, geometryFactory);

    final GeometryCollection gc = (GeometryCollection)g;
    assertTrue(gc.getNumGeometries() == 25);

    for (int i = 0; i < 25; i++) {
      final MultiPolygon p = (MultiPolygon)gc.getGeometryN(i);
      assertNotNull(p);
    }
  }

  public void testPointRead() throws SAXException, IOException,
    ParserConfigurationException {
    final FileReader fr = new FileReader(TEST_DIR + "points.xml");

    final GMLReader gr = new GMLReader();
    final Geometry g = gr.read(fr, geometryFactory);

    final GeometryCollection gc = (GeometryCollection)g;
    assertTrue(gc.getNumGeometries() == 25);

    for (int i = 0; i < 25; i++) {
      final Point p = (Point)gc.getGeometryN(i);
      assertNotNull(p);
    }
  }

  public void testPolygonRead() throws SAXException, IOException,
    ParserConfigurationException {
    final FileReader fr = new FileReader(TEST_DIR + "polygons.xml");

    final GMLReader gr = new GMLReader();
    final Geometry g = gr.read(fr, geometryFactory);

    final GeometryCollection gc = (GeometryCollection)g;
    assertTrue(gc.getNumGeometries() == 25);

    for (int i = 0; i < 25; i++) {
      final Polygon p = (Polygon)gc.getGeometryN(i);
      assertNotNull(p);
    }
  }

}
