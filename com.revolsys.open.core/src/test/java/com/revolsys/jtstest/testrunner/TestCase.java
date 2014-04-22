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
package com.revolsys.jtstest.testrunner;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.springframework.util.StringUtils;

import com.revolsys.io.map.InvokeMethodMapObjectFactory;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;

/**
 *  A set of tests for two Geometry's.
 *
 *@author     jaquino
 *@created    June 22, 2001
 *
 * @version 1.7
 */
public class TestCase implements Runnable, MapSerializer {
  private String description;

  public static final MapObjectFactory FACTORY = new InvokeMethodMapObjectFactory(
    "testCase", "Test Case", TestCase.class, "create");

  public static TestCase create(final Map<String, Object> map) {
    return new TestCase(map);
  }

  private String aWkt;

  private String bWkt;

  private Geometry a;

  private Geometry b;

  private final Vector<Test> tests = new Vector<Test>();

  private TestRun testRun;

  private int caseIndex;

  private boolean isRun = false;

  private GeometryFactory geometryFactory;

  public TestCase(final Map<String, Object> map) {
    this.description = (String)map.get("description");
    this.aWkt = (String)map.get("a");
    this.bWkt = (String)map.get("b");
  }

  /**
   *  Creates a TestCase with the given description. The tests will be applied
   *  to a and b.
   */
  public TestCase(final String description, final Geometry a, final Geometry b,
    final File aWktFile, final File bWktFile, final TestRun testRun,
    final int caseIndex, final int lineNumber) {
    if (description != null) {
      this.description = description.replaceAll("\\s+", " ");
    }
    this.a = a;
    this.b = b;
    this.testRun = testRun;
    this.caseIndex = caseIndex;
  }

  public TestCase(final TestRun testRun, final int index,
    final Map<String, Object> map) {
    this.testRun = testRun;
    this.caseIndex = index;
    this.description = (String)map.get("description");
    this.aWkt = (String)map.get("a");
    this.bWkt = (String)map.get("b");
  }

  /**
   *  Adds a Test to the TestCase.
   */
  public void add(final Test test) {
    tests.add(test);
  }

  public int getCaseIndex() {
    return caseIndex;
  }

  public String getDescription() {
    return description;
  }

  public Geometry getGeometryA() {
    return a;
  }

  public Geometry getGeometryB() {
    return b;
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public int getLineNumber() {
    return 0;
  }

  /**
   *  Returns the number of tests.
   *
   *@return    The testCount value
   */
  public int getTestCount() {
    return tests.size();
  }

  public TestRun getTestRun() {
    return testRun;
  }

  public List<Test> getTests() {
    return Collections.unmodifiableList(tests);
  }

  public boolean isRun() {
    return isRun;
  }

  public void remove(final Test test) {
    tests.remove(test);
  }

  @Override
  public void run() {
    isRun = true;
    for (final Test test : tests) {
      test.run();
    }
  }

  public void setDescription(final String description) {
    this.description = StringUtils.trimWhitespace(description);
  }

  public void setGeometryA(final Geometry a) {
    this.a = a;
  }

  public void setGeometryB(final Geometry b) {
    this.b = b;
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "test");

    if (StringUtils.hasText(description)) {
      map.put("description", description);
    }
    MapSerializerUtil.add(map, "geometryFactory", geometryFactory);
    final Map<String, Object> properties = new LinkedHashMap<String, Object>();
    if (testRun != null) {
      MapSerializerUtil.addAll(properties, testRun.getProperties());
    }
    MapSerializerUtil.add(properties, "a", a);
    MapSerializerUtil.add(properties, "b", b);

    if (!properties.isEmpty()) {
      map.put("properties", properties);
    }

    MapSerializerUtil.add(map, "tests", tests);
    return map;
  }

  @Override
  public String toString() {
    return toMap().toString();
  }

  public String toXml() {
    String xml = "";
    xml += "<case>" + StringUtil.newLine;
    if (description != null && description.length() > 0) {
      xml += "  <desc>" + StringUtil.escapeHTML(description) + "</desc>"
        + StringUtil.newLine;
    }
    xml += xml("a", a) + StringUtil.newLine;
    xml += xml("b", b);
    for (final Iterator i = tests.iterator(); i.hasNext();) {
      final Test test = (Test)i.next();
      xml += test.toXml();
    }
    xml += "</case>" + StringUtil.newLine;
    return xml;
  }

  private String xml(final String id, final Geometry g) {
    if (g == null) {
      return "";
    }
    String xml = "";
    xml += "  <" + id + ">" + StringUtil.newLine;
    xml += StringUtil.indent(g.toWkt(), 4) + StringUtil.newLine;
    xml += "  </" + id + ">" + StringUtil.newLine;
    return xml;
  }
}
