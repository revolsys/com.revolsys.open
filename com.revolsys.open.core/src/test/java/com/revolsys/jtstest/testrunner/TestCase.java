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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.springframework.util.StringUtils;

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
public class TestCase extends junit.framework.TestSuite implements
  MapSerializer {
  private String testDescription;

  private Geometry a;

  private Geometry b;

  private final TestFile testRun;

  private final int caseIndex;

  private final boolean isRun = false;

  private GeometryFactory geometryFactory;

  /**
   *  Creates a TestCase with the given description. The tests will be applied
   *  to a and b.
   */
  public TestCase(final String description, final Geometry a, final Geometry b,
    final File aWktFile, final File bWktFile, final TestFile testRun,
    final int caseIndex, final int lineNumber) {
    if (StringUtils.hasText(description)) {
      this.testDescription = description.replaceAll("\\s+", " ");
      setName(testDescription);
    } else {
      setName(String.valueOf(caseIndex));
    }
    this.a = a;
    this.b = b;
    this.testRun = testRun;
    this.caseIndex = caseIndex;
  }

  /**
   *  Adds a Test to the TestCase.
   */
  public void add(final GeometryOperationTest test) {
    addTest(test);
  }

  public int getCaseIndex() {
    return caseIndex;
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
    return testCount();
  }

  public String getTestDescription() {
    return testDescription;
  }

  public TestFile getTestRun() {
    return testRun;
  }

  public List<GeometryOperationTest> getTests() {
    final List<GeometryOperationTest> testList = new ArrayList<>();
    final Enumeration<Test> tests = tests();
    while (tests.hasMoreElements()) {
      testList.add((GeometryOperationTest)tests.nextElement());
    }
    return testList;
  }

  public boolean isRun() {
    return isRun;
  }

  public void setGeometryA(final Geometry a) {
    this.a = a;
  }

  public void setGeometryB(final Geometry b) {
    this.b = b;
  }

  public void setTestDescription(final String description) {
    this.testDescription = StringUtils.trimWhitespace(description);
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "test");

    if (StringUtils.hasText(testDescription)) {
      map.put("description", testDescription);
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

    // MapSerializerUtil.add(map, "tests", tests);
    return map;
  }

  @Override
  public String toString() {
    return getName();
  }

  public String toXml() {
    String xml = "";
    xml += "<case>" + StringUtil.newLine;
    if (testDescription != null && testDescription.length() > 0) {
      xml += "  <desc>" + StringUtil.escapeHTML(testDescription) + "</desc>"
        + StringUtil.newLine;
    }
    xml += xml("a", a) + StringUtil.newLine;
    xml += xml("b", b);
    for (final GeometryOperationTest test : getTests()) {
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
