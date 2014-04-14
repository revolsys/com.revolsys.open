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
package com.revolsys.jtstest.testbuilder.model;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.io.WKTWriter;
import com.revolsys.jtstest.test.TestCaseList;
import com.revolsys.jtstest.test.Testable;
import com.revolsys.jtstest.testrunner.StringUtil;

/**
 * @version 1.7
 */
public class JavaTestWriter {
  public static String getRunJava(final String className,
    final TestBuilderModel tbModel) {
    return "package com.revolsys.jtstest.testsuite;" + StringUtil.newLine + ""
      + StringUtil.newLine + "import com.revolsys.jtstest.test.*;"
      + StringUtil.newLine + "" + StringUtil.newLine + "public class "
      + className + " extends TestCaseList {" + StringUtil.newLine
      + "  public static void main(String[] args) {" + StringUtil.newLine
      + "    " + className + " test = new " + className + "();"
      + StringUtil.newLine + "    test.run();" + StringUtil.newLine + "  }"
      + StringUtil.newLine + "" + StringUtil.newLine + "  public " + className
      + "() {" + StringUtil.newLine + getTestJava(tbModel.getTestCaseList())
      + "  }" + StringUtil.newLine + "}";
  }

  public static String getTestJava(final TestCaseList tcList) {
    final StringBuffer java = new StringBuffer();
    for (int i = 0; i < tcList.getList().size(); i++) {
      java.append((new JavaTestWriter()).write((Testable)tcList.getList()
        .get(i)));
    }
    return java.toString();
  }

  private final WKTWriter writer = new WKTWriter();

  public JavaTestWriter() {
  }

  private String write(final Geometry geometry) {
    if (geometry == null) {
      return "null";
    }
    return "\"" + writer.write(geometry) + "\"";
  }

  public String write(final Testable testable) {
    final StringBuffer text = new StringBuffer();
    text.append("    add(new TestCase(\n");
    final String name = testable.getName() == null ? "" : testable.getName();
    final String description = testable.getDescription() == null ? ""
      : testable.getDescription();
    final String a = testable.getGeometry(0) == null ? null
      : writer.write(testable.getGeometry(0));
    final String b = testable.getGeometry(1) == null ? null
      : writer.write(testable.getGeometry(1));
    final String im = testable.getExpectedIntersectionMatrix() != null ? testable.getExpectedIntersectionMatrix()
      .toString()
      : null;
    text.append("          \"" + name + "\",\n");
    text.append("          \"" + description + "\",\n");
    text.append("          " + (a == null ? "null" : "\"" + a + "\"") + ",\n");
    text.append("          " + (b == null ? "null" : "\"" + b + "\"") + ",\n");
    text.append("          " + (im == null ? "null" : "\"" + im + "\"") + ",\n");
    text.append("          " + write(testable.getExpectedConvexHull()) + ",\n");
    text.append("          " + write(testable.getExpectedIntersection())
      + ",\n");
    text.append("          " + write(testable.getExpectedUnion()) + ",\n");
    text.append("          " + write(testable.getExpectedDifference()) + ",\n");
    text.append("          " + write(testable.getExpectedSymDifference())
      + ",\n");
    text.append("          " + write(testable.getExpectedBoundary()) + "));\n");
    return text.toString();
  }
}
