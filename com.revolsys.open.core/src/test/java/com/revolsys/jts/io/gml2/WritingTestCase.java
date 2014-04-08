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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.PrecisionModel;

/**
 * Test Case framework for GML unit tests.
 * 
 * @author David Zwiers, Vivid Solutions.
 * @author Martin Davis 
 */
public abstract class WritingTestCase extends TestCase {

  protected StringWriter sw = null;

  protected static PrecisionModel precisionModel = new PrecisionModel(1000);

  protected static GeometryFactory geometryFactory = new GeometryFactory(
    precisionModel);

  /**
   * @param arg
   */
  public WritingTestCase(final String arg) {
    super(arg);
  }

  protected void checkRoundTrip(final Geometry g) throws SAXException,
    IOException, ParserConfigurationException {
    final GMLWriter out = new GMLWriter();
    out.setPrefix(null);
    out.setNamespace(true);
    out.setSrsName("foo");
    // this markup is not currently work with GMLReader
    // out.setCustomElements(new String[] { "<test>1</test>" } );
    out.write(g, getWriter());

    System.out.println(this.sw.toString());

    final GMLReader in = new GMLReader();
    final Geometry g2 = in.read(getReader(), geometryFactory);

    // System.out.println((pt==null?"NULL":pt.toString()));
    // System.out.println((pt2==null?"NULL":pt2.toString()));
    assertTrue("The input Geometry is not the same as the output Geometry",
      g.equalsExact(g2));
  }

  protected Reader getReader() throws IOException {
    this.sw.flush();
    this.sw.close();
    final String s = this.sw.toString();

    // System.out.println(s);

    return new StringReader(s);
  }

  protected Writer getWriter() {
    this.sw = new StringWriter();
    this.sw.write("<?xml version='1.0' encoding='UTF-8'?>\n");
    return this.sw;
  }
}
