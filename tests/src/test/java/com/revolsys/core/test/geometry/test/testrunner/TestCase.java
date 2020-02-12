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
package com.revolsys.core.test.geometry.test.testrunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.springframework.util.StringUtils;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.util.Property;

import junit.framework.Test;

/**
 *  A set of tests for two Geometry's.
 *
 *@author     jaquino
 *@created    June 22, 2001
 *
 * @version 1.7
 */
public class TestCase extends junit.framework.TestSuite implements MapSerializer {
  private Geometry a;

  private Geometry b;

  private final int caseIndex;

  private GeometryFactory geometryFactory;

  private final boolean isRun = false;

  private String testDescription;

  private final TestFile testFile;

  /**
   *  Creates a TestCase with the given description. The tests will be applied
   *  to a and b.
   */
  public TestCase(final String description, final Geometry a, final Geometry b, final File aWktFile,
    final File bWktFile, final TestFile testFile, final int caseIndex, final int lineNumber) {
    this.testFile = testFile;
    this.caseIndex = caseIndex;
    if (Property.hasValue(description)) {
      this.testDescription = description.replaceAll("\\s+", " ")
        .replaceAll("[^A-Za-z0-9\\-_ ]", " ");
      setName(getId() + "." + this.testDescription);
    } else {
      setName(getId() + ".");

    }
    this.a = a;
    this.b = b;
  }

  /**
   *  Adds a Test to the TestCase.
   */
  public void add(final GeometryOperationTest test) {
    addTest(test);
  }

  public int getCaseIndex() {
    return this.caseIndex;
  }

  public Geometry getGeometryA() {
    return this.a;
  }

  public Geometry getGeometryB() {
    return this.b;
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public String getId() {
    return this.testFile.getId() + "." + this.caseIndex;
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
    return this.testDescription;
  }

  public TestFile getTestRun() {
    return this.testFile;
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
    return this.isRun;
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
  public JsonObject toMap() {
    final JsonObject map = JsonObject.hash();
    map.put("type", "test");

    if (Property.hasValue(this.testDescription)) {
      map.put("description", this.testDescription);
    }
    addToMap(map, "geometryFactory", this.geometryFactory);
    final JsonObject properties = JsonObject.hash();
    if (this.testFile != null) {
      addAllToMap(properties, this.testFile.getProperties());
    }
    addToMap(properties, "a", this.a);
    addToMap(properties, "b", this.b);

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
    if (this.testDescription != null && this.testDescription.length() > 0) {
      xml += "  <desc>" + StringUtil.escapeHTML(this.testDescription) + "</desc>"
        + StringUtil.newLine;
    }
    xml += xml("a", this.a) + StringUtil.newLine;
    xml += xml("b", this.b);
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
    xml += StringUtil.indent(g.toEwkt(), 4) + StringUtil.newLine;
    xml += "  </" + id + ">" + StringUtil.newLine;
    return xml;
  }
}
