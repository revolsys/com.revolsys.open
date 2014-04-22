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

import java.io.File;
import java.util.Iterator;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Lineal;
import com.revolsys.jts.geom.Polygonal;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.geom.Puntal;
import com.revolsys.jts.io.WKBWriter;
import com.revolsys.jts.util.Assert;
import com.revolsys.jtstest.test.TestCase;
import com.revolsys.jtstest.test.TestCaseList;
import com.revolsys.jtstest.test.Testable;
import com.revolsys.jtstest.testrunner.StringUtil;

/**
 * @version 1.7
 */
public class XMLTestWriter {
  public static String getRunDescription(final TestCaseList l) {
    for (final Iterator i = l.getList().iterator(); i.hasNext();) {
      final TestCaseEdit tce = (TestCaseEdit)i.next();
      if (tce.getTestable() instanceof TestRunnerTestCaseAdapter) {
        final TestRunnerTestCaseAdapter a = (TestRunnerTestCaseAdapter)tce.getTestable();
        final String description = a.getTestRunnerTestCase()
          .getTestRun()
          .getDescription();
        if (description != null && description.length() > 0) {
          return "  <desc>" + StringUtil.escapeHTML(description) + "</desc>"
            + StringUtil.newLine;
        }
        return "";
      }
    }
    return "";
  }

  public static String getRunWorkspace(final TestCaseList l) {
    for (final Iterator i = l.getList().iterator(); i.hasNext();) {
      final TestCaseEdit tce = (TestCaseEdit)i.next();
      if (tce.getTestable() instanceof TestRunnerTestCaseAdapter) {
        final TestRunnerTestCaseAdapter a = (TestRunnerTestCaseAdapter)tce.getTestable();
        final File workspace = a.getTestRunnerTestCase()
          .getTestRun()
          .getWorkspace();
        if (workspace != null) {
          return "  <workspace file=\""
            + StringUtil.escapeHTML(workspace.toString()) + "\"/>"
            + StringUtil.newLine;
        }
        return "";
      }
    }
    return "";
  }

  public static String getRunXml(final TestCaseList tcList,
    final GeometryFactory geometryFactory) {
    String runXML = "<run>" + StringUtil.newLine;
    runXML += getRunDescription(tcList);
    runXML += getRunWorkspace(tcList);
    runXML += toXML(geometryFactory.getPrecisionModel()) + StringUtil.newLine;
    runXML += (new XMLTestWriter()).getTestXML(tcList) + "</run>";
    return runXML;
  }

  public static String toXML(final PrecisionModel precisionModel) {
    if (precisionModel.isFloating()) {
      return "<precisionModel type=\"FLOATING\"/>";
    }
    return "<precisionModel type=\"FIXED\" scale=\""
      + precisionModel.getScale() + "\"/>";
  }

  private final WKBWriter wkbWriter = new WKBWriter();

  public XMLTestWriter() {
  }

  public String getDescriptionForXml(final TestCase testCase) {
    if (isGdbcTestCase(testCase)) {
      return getDescriptionForXmlFromGdbcTestCase(testCase);
    }
    if (testCase.getDescription() != null
      && testCase.getDescription().length() > 0) {
      return "<desc>" + StringUtil.escapeHTML(testCase.getDescription())
        + "</desc>\n";
    }
    if (testCase.getName() != null && testCase.getName().length() > 0) {
      return "<desc>" + StringUtil.escapeHTML(testCase.getName()) + "</desc>\n";
    }
    return "<desc> " + getGeometryArgPairCode(testCase.getGeometries())
      + " </desc>\n";
  }

  public String getDescriptionForXmlFromGdbcTestCase(final TestCase testCase) {
    final int descriptionColonIndex = testCase.getDescription().indexOf(":");
    return "<desc>"
      + StringUtil.escapeHTML(testCase.getName() + " ["
        + testCase.getDescription().substring(1 + descriptionColonIndex).trim()
        + "]") + "</desc>\n";
  }

  private String getGeometryArgPairCode(final Geometry[] geom) {
    return getGeometryCode(geom[0]) + "/" + getGeometryCode(geom[1]);
  }

  private String getGeometryCode(final Geometry geom) {
    String dimCode = "";
    if (geom instanceof Puntal) {
      dimCode = "P";
    }
    if (geom instanceof Lineal) {
      dimCode = "L";
    }
    if (geom instanceof Polygonal) {
      dimCode = "L";
    }

    if (geom instanceof GeometryCollection) {
      return "m" + dimCode;
    }

    return dimCode;
  }

  public String getTestXML(final Geometry geometry, final String opName,
    final String[] arguments) {
    String xml = "  <test>\n";
    xml += "    <op name=\"" + opName + "\" arg1=\"A\"";
    int j = 2;
    for (int i = 0; i < arguments.length; i++) {
      final String argument = arguments[i];
      Assert.isTrue(argument != null);
      xml += " arg" + j + "=\"" + argument + "\"";
      j++;
    }
    xml += ">\n";
    xml += StringUtil.indent(geometry.toWkt() + "\n", 6);
    xml += "    </op>\n";
    xml += "  </test>\n";
    return xml;
  }

  public String getTestXML(final Testable testCase) {
    return getTestXML(testCase, true);
  }

  public String getTestXML(final Testable testable, final boolean useWKT) {
    if (testable instanceof TestCase) {
      return getTestXML((TestCase)testable, useWKT);
    }
    if (testable instanceof TestCaseEdit) {
      return getTestXML(((TestCaseEdit)testable).getTestable(), useWKT);
    }
    if (testable instanceof TestRunnerTestCaseAdapter) {
      return getTestXML(testable, useWKT);
    }
    Assert.shouldNeverReachHere();
    return null;
  }

  private String getTestXML(final TestCase testCase, final boolean useWKT) {
    final Geometry[] geom = testCase.getGeometries();
    final StringBuffer xml = new StringBuffer();
    xml.append("<case>\n");
    xml.append(getDescriptionForXml(testCase));
    if (geom[0] != null) {
      final String wkt0 = getWKTorWKB(geom[0], useWKT);
      xml.append("  <a>\n" + wkt0 + "\n    </a>\n");
    }
    if (geom[1] != null) {
      final String wkt1 = getWKTorWKB(geom[1], useWKT);
      xml.append("  <b>\n" + wkt1 + "\n    </b>\n");
    }
    if (testCase.getExpectedIntersectionMatrix() != null) {
      xml.append("  <test>\n");
      xml.append("    <op name=\"relate\" arg1=\"A\" arg2=\"B\" arg3=\""
        + testCase.getExpectedIntersectionMatrix() + "\">true</op>\n");
      xml.append("  </test>\n");
    }
    if (testCase.getExpectedBoundary() != null) {
      xml.append(getTestXML(testCase.getExpectedBoundary(), "getboundary",
        new String[] {}));
    }
    if (testCase.getExpectedConvexHull() != null) {
      xml.append(getTestXML(testCase.getExpectedConvexHull(), "convexhull",
        new String[] {}));
    }
    if (testCase.getExpectedIntersection() != null) {
      xml.append(getTestXML(testCase.getExpectedIntersection(), "intersection",
        new String[] {
          "B"
        }));
    }
    if (testCase.getExpectedUnion() != null) {
      xml.append(getTestXML(testCase.getExpectedUnion(), "union", new String[] {
        "B"
      }));
    }
    if (testCase.getExpectedDifference() != null) {
      xml.append(getTestXML(testCase.getExpectedDifference(), "difference",
        new String[] {
          "B"
        }));
    }
    if (testCase.getExpectedSymDifference() != null) {
      xml.append(getTestXML(testCase.getExpectedSymDifference(),
        "symdifference", new String[] {
          "B"
        }));
    }
    xml.append("</case>\n");
    return xml.toString();
  }

  public String getTestXML(final TestCaseList tcList) {
    final StringBuffer xml = new StringBuffer();
    for (int i = 0; i < tcList.getList().size(); i++) {
      xml.append("\n");
      xml.append(getTestXML((Testable)tcList.getList().get(i)));
    }
    xml.append("\n");
    return xml.toString();
  }

  public String getTestXML(final TestRunnerTestCaseAdapter adapter) {
    return adapter.getTestRunnerTestCase().toXml();
  }

  private String getWKTorWKB(final Geometry g, final boolean useWKT) {
    if (useWKT) {
      return g.toWkt();
    }
    return WKBWriter.toHex(wkbWriter.write(g));
  }

  private boolean isGdbcTestCase(final TestCase testCase) {
    if (testCase.getName() == null || testCase.getDescription() == null) {
      return false;
    }
    if (testCase.getName().equalsIgnoreCase(testCase.getDescription())) {
      return false;
    }
    final int nameColonIndex = testCase.getName().indexOf(":");
    final int descriptionColonIndex = testCase.getDescription().indexOf(":");
    if (nameColonIndex == -1 || descriptionColonIndex == -1) {
      return false;
    }
    if (nameColonIndex != descriptionColonIndex) {
      return false;
    }
    return true;
  }

}
