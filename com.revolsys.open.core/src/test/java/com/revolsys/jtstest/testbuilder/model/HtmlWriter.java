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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.util.Assert;
import com.revolsys.jtstest.test.TestCaseList;
import com.revolsys.jtstest.test.Testable;
import com.revolsys.jtstest.testbuilder.AppStrings;
import com.revolsys.jtstest.testbuilder.BusyDialog;
import com.revolsys.jtstest.testbuilder.GeometryEditPanel;
import com.revolsys.jtstest.testrunner.BooleanResult;
import com.revolsys.jtstest.testrunner.StringUtil;
import com.revolsys.jtstest.testrunner.GeometryOperationTest;
import com.revolsys.jtstest.testrunner.TestCase;
import com.revolsys.jtstest.util.FileUtil;

/**
 *  An object that creates an .html file describing the test cases. .gif files
 *  are also created.
 *
 * @version 1.7
 */
public class HtmlWriter {
  private static class MapAndList {
    public Map map;

    public List list;
  }

  private final static int IMAGE_WIDTH = 200;

  private final static int IMAGE_HEIGHT = 200;

  private final static int STACK_TRACE_DEPTH = 1;

  private boolean showingABwithSpatialFunction = true;

  private final GeometryEditPanel geometryEditPanel = new GeometryEditPanel();

  private final JFrame frame = new JFrame();

  private File outputDirectory;

  private BusyDialog busyDialog = null;

  public HtmlWriter() {
    geometryEditPanel.setSize(IMAGE_WIDTH, IMAGE_HEIGHT);
    geometryEditPanel.setGridEnabled(false);
    geometryEditPanel.setBorder(BorderFactory.createEmptyBorder());
    frame.getContentPane().add(geometryEditPanel);
  }

  private Object actualResult(final TestCaseEdit testCaseEdit,
    final String opName, final String first, final String second)
    throws Exception {
    try {
      Assert.isTrue((first.equalsIgnoreCase("A"))
        || (first.equalsIgnoreCase("B")));
      final Class geometryClass = Class.forName("com.revolsys.jts.geom.Geometry");
      final Geometry source = testCaseEdit.getGeometry(first.equalsIgnoreCase("A") ? 0
        : 1);
      Object[] target;
      Class[] targetClasses;
      if (second == null) {
        target = new Object[] {};
        targetClasses = new Class[] {};
      } else {
        target = new Object[] {
          testCaseEdit.getGeometry(second.equalsIgnoreCase("A") ? 0 : 1)
        };
        targetClasses = new Class[] {
          geometryClass
        };
      }
      final Method op = geometryClass.getMethod(opName, targetClasses);
      return op.invoke(source, target);
    } catch (final InvocationTargetException e) {
      throw (Exception)e.getTargetException();
    }
  }

  private void addToListMapAndList(final String key, final Object valueItem,
    final Map stringToList, final List keyList) {
    if (stringToList.containsKey(key)) {
      final List value = (List)stringToList.get(key);
      value.add(valueItem);
    } else {
      final List value = new ArrayList();
      value.add(valueItem);
      stringToList.put(key, value);
      keyList.add(key);
    }
  }

  private void createGifFile(final String filenameNoPath, final Geometry a,
    final Geometry b, final Geometry spatialFunction, final boolean showingAB,
    final int imageWidth, final int imageHeight) throws FileNotFoundException,
    IOException {
    createGifFile(filenameNoPath, a, b, spatialFunction, showingAB, imageWidth,
      imageHeight, false);
  }

  private void createGifFile(final String filenameNoPath, final Geometry a,
    final Geometry b, final Geometry result, final boolean showingAB,
    final int imageWidth, final int imageHeight, final boolean zoomToFullExtent)
    throws FileNotFoundException, IOException {
    final TestBuilderModel tbModel = new TestBuilderModel();
    final TestCaseEdit tc = new TestCaseEdit(new Geometry[] {
      a, b
    });
    tc.setResult(result);
    tbModel.getGeometryEditModel().setTestCase(tc);
    geometryEditPanel.setModel(tbModel);
    if (zoomToFullExtent) {
      geometryEditPanel.zoomToFullExtent();
    }
    geometryEditPanel.setShowingResult(result != null);
    geometryEditPanel.setShowingGeometryA(a != null && showingAB);
    geometryEditPanel.setShowingGeometryB(b != null && showingAB);
    final String filenameWithPath = outputDirectory.getPath() + "\\"
      + filenameNoPath;
    final Image image = new BufferedImage(imageWidth, imageHeight,
      BufferedImage.TYPE_4BYTE_ABGR);
    geometryEditPanel.paint(image.getGraphics());
    /*
     * // disabled - should be replaced with PNG output FileOutputStream
     * outputStream = new FileOutputStream(filenameWithPath, false); GifEncoder
     * gifEncoder = new GifEncoder(image, outputStream);
     * gifEncoder.setDimensions(imageWidth, imageHeight); gifEncoder.encode();
     * outputStream.flush(); outputStream.close();
     */
  }

  private void createHtmlFile(final String filename, final String html)
    throws IOException {
    final String pathname = outputDirectory.getPath() + "\\" + filename;
    FileUtil.setContents(pathname, html);
  }

  private String deleteFirstTag(final String html) {
    if (html.lastIndexOf(">") == -1) {
      return html;
    }
    return html.substring(html.indexOf(">") + 1);
  }

  private String deleteLastTag(final String html) {
    if (html.lastIndexOf("<") == -1) {
      return html;
    }
    return html.substring(0, html.lastIndexOf("<"));
  }

  private BooleanResult expectedPredicateResult(
    final TestCaseEdit testCaseEdit, final String opName, final String first,
    final String second) {
    if (!(testCaseEdit.getTestable() instanceof TestRunnerTestCaseAdapter)) {
      return null;
    }
    final TestRunnerTestCaseAdapter adapter = (TestRunnerTestCaseAdapter)testCaseEdit.getTestable();
    final TestCase trTestCase = adapter.getTestRunnerTestCase();
    for (final Iterator i = trTestCase.getTests().iterator(); i.hasNext();) {
      final GeometryOperationTest test = (GeometryOperationTest)i.next();
      if (test.getOperation().equalsIgnoreCase(opName)
        && test.getGeometryIndex().equalsIgnoreCase(first)
        && (test.getArgumentCount() == 0 || ((test.getArgument(0) != null && test.getArgument(
          0)
          .equalsIgnoreCase(second)) || (test.getArgument(0) == null && second.equalsIgnoreCase("null"))))) {
        return (BooleanResult)test.getExpectedResult();
      }
    }
    return null;
  }

  private String html(final Testable testable, final int runSkey,
    final int caseSkey) throws IOException {
    final TestCaseEdit testCaseEdit = (TestCaseEdit)testable;
    String html = "<HTML>" + StringUtil.newLine + "<HEAD>" + StringUtil.newLine
      + "<TITLE>" + StringUtil.escapeHTML(testName(testCaseEdit, caseSkey))
      + "</TITLE>" + StringUtil.newLine
      + "<link REL='STYLESHEET' HREF='../jts.css' TYPE='Text/css'>"
      + StringUtil.newLine + "</HEAD>" + StringUtil.newLine + "<BODY>"
      + StringUtil.newLine + "<div class='testTitle'>"
      + StringUtil.escapeHTML(testName(testCaseEdit, caseSkey)) + "</div>"
      + StringUtil.newLine + "<P>" + StringUtil.newLine;
    html += htmlForAB(testCaseEdit, runSkey, caseSkey);
    html += htmlForTests(testCaseEdit, runSkey, caseSkey);
    html += "</BODY>" + StringUtil.newLine + "</HTML>";
    return html;
  }

  private String htmlForAB(final TestCaseEdit testCaseEdit, final int runSkey,
    final int caseSkey) throws IOException {
    final String wktHtml = "<span class=wktA>"
      + (testCaseEdit.getGeometry(0) == null ? " "
        : testCaseEdit.getGeometry(0).toWkt())
      + "</span>"
      + "<P>"
      + "<span class=wktB>"
      + (testCaseEdit.getGeometry(1) == null ? " "
        : testCaseEdit.getGeometry(1).toWkt()) + "</span>";
    final String html = StringUtil.newLine
      + "<TABLE BORDER=0>"
      + StringUtil.newLine
      + "  <TR>"
      + StringUtil.newLine
      + htmlImageHtmlTextTable("Run" + runSkey + AppStrings.LABEL_TEST_CASE
        + caseSkey + ".gif", wktHtml, 0) + "  </TR>" + StringUtil.newLine
      + "</TABLE>" + StringUtil.newLine;
    createGifFile("Run" + runSkey + AppStrings.LABEL_TEST_CASE + caseSkey
      + ".gif", testCaseEdit.getGeometry(0), testCaseEdit.getGeometry(1), null,
      true, IMAGE_WIDTH, IMAGE_HEIGHT, true);
    return html;
  }

  private String htmlForBinaryPredicates(final TestCaseEdit testCaseEdit,
    final int caseSkey) {
    String html = "";
    if (testCaseEdit.getGeometry(1) != null) {
      html += htmlForRelateTest(testCaseEdit, caseSkey);
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "equals", "A", "B");
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "disjoint", "A", "B");
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "intersects", "A",
        "B");
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "touches", "A", "B");
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "crosses", "A", "B");
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "within", "A", "B");
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "contains", "A", "B");
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "overlaps", "A", "B");

      html = "<h2>Binary Predicates</h2>" + StringUtil.newLine
        + "<TABLE WIDTH=50% BORDER=1>" + StringUtil.newLine + html + "</TABLE>"
        + StringUtil.newLine;
    }
    return html;
  }

  private String htmlForPredicateTest(final TestCaseEdit testCaseEdit,
    final int caseSkey, final String opName, final String first,
    final String second) {
    String actualResultString;
    try {
      actualResultString = actualResult(testCaseEdit, opName, first, second).toString();
    } catch (final Exception e) {
      actualResultString = StringUtil.replace(
        StringUtil.getStackTrace(e, STACK_TRACE_DEPTH), "\n", "<BR>", true);
      e.printStackTrace(System.out);
    }
    final String html = "  <TR>" + StringUtil.newLine
      + "    <TD class=methodTitle>" + opName + "</TD>" + StringUtil.newLine
      + "    <TD class=resultFalse>" + actualResultString + "</TD>"
      + StringUtil.newLine + "  </TR>" + StringUtil.newLine;
    return html;
  }

  private String htmlForRelateTest(final TestCaseEdit testCaseEdit,
    final int caseSkey) {
    String actualValue;
    try {
      actualValue = insertParagraphs(testCaseEdit.getGeometry(0)
        .relate(testCaseEdit.getGeometry(1))
        .toString());
    } catch (final Exception e) {
      actualValue = StringUtil.replace(
        StringUtil.getStackTrace(e, STACK_TRACE_DEPTH), "\n", "<BR>", true);
      e.printStackTrace(System.out);
    }
    final String html = "  <TR>" + StringUtil.newLine
      + "    <TD class=methodTitle rowspan=9>relate</TD>" + StringUtil.newLine
      + "    <TD rowspan=9>" + actualValue + "</TD>" + StringUtil.newLine
      + "  </TR>" + StringUtil.newLine;
    return html;
  }

  private String htmlForSpatialFunctions(final TestCaseEdit testCaseEdit,
    final int runSkey, final int caseSkey) {
    if (testCaseEdit.getExpectedConvexHull() == null
      && testCaseEdit.getExpectedIntersection() == null
      && testCaseEdit.getExpectedUnion() == null
      && testCaseEdit.getExpectedDifference() == null
      && testCaseEdit.getExpectedSymDifference() == null) {
      return "";
    }
    String html = "";
    html += htmlForSpatialFunctionTest(testCaseEdit, runSkey, caseSkey,
      "convexHull", "A", null);
    if (testCaseEdit.getGeometry(1) != null) {
      html += htmlForSpatialFunctionTest(testCaseEdit, runSkey, caseSkey,
        "intersection", "A", "B");
      html += htmlForSpatialFunctionTest(testCaseEdit, runSkey, caseSkey,
        "union", "A", "B");
      html += htmlForSpatialFunctionTest(testCaseEdit, runSkey, caseSkey,
        "difference", "A", "B");
      html += htmlForSpatialFunctionTest(testCaseEdit, runSkey, caseSkey,
        "symDifference", "A", "B");
    }

    html = "<h2>Spatial Analysis Methods</h2>" + StringUtil.newLine
      + "<TABLE BORDER=1>" + StringUtil.newLine + html + "</TABLE>"
      + StringUtil.newLine;
    return html;
  }

  private String htmlForSpatialFunctionTest(final TestCaseEdit testCaseEdit,
    final int runSkey, final int caseSkey, final String geometryOpName,
    final String first, final String second) {
    String actualResultString = "&nbsp;";
    try {
      final Geometry actualResult = (Geometry)actualResult(testCaseEdit,
        geometryOpName, first, second);
      String filenameNoPath = "Run" + runSkey + AppStrings.LABEL_TEST_CASE
        + caseSkey + geometryOpName + "Actual";
      if (first != null) {
        filenameNoPath += first;
      }
      if (second != null) {
        filenameNoPath += second;
      }
      filenameNoPath += ".gif";
      actualResultString = htmlImageHtmlTextTable(filenameNoPath,
        "<SPAN class=wktR>" + actualResult.toWkt() + "</SPAN>", 0);
      createGifFile(filenameNoPath, testCaseEdit.getGeometry(0),
        testCaseEdit.getGeometry(1), actualResult,
        showingABwithSpatialFunction, IMAGE_WIDTH, IMAGE_HEIGHT);
    } catch (final Exception e) {
      actualResultString = "<TD>"
        + StringUtil.replace(StringUtil.getStackTrace(e, STACK_TRACE_DEPTH),
          "\n", "<BR>", true) + "</TD>";
      e.printStackTrace(System.out);
    }
    final String html = "  <TR>" + StringUtil.newLine
      + "    <TD class=methodTitle>" + geometryOpName + "</TD>"
      + StringUtil.newLine + actualResultString + StringUtil.newLine
      + "  </TR>" + StringUtil.newLine;
    return html;
  }

  private String htmlForTests(final TestCaseEdit testCaseEdit,
    final int runSkey, final int caseSkey) throws IOException {
    String html = htmlForBinaryPredicates(testCaseEdit, caseSkey);
    html += htmlForSpatialFunctions(testCaseEdit, runSkey, caseSkey);
    html += htmlForTopologyMethods(testCaseEdit, runSkey, caseSkey);
    return html;
  }

  private String htmlForTopologyMethods(final TestCaseEdit testCaseEdit,
    final int runSkey, final int caseSkey) {
    final boolean isSimpleSpecified = expectedPredicateResult(testCaseEdit,
      "isSimple", "A", null) != null;
    final boolean getBoundarySpecified = testCaseEdit.getExpectedBoundary() != null;
    final boolean isValidSpecified = expectedPredicateResult(testCaseEdit,
      "isValid", "A", null) != null;
    if (!isSimpleSpecified && !getBoundarySpecified && !isValidSpecified) {
      return "";
    }

    String html = "";
    if (isSimpleSpecified) {
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "isSimple", "A",
        null);
    }
    if (isValidSpecified) {
      html += htmlForPredicateTest(testCaseEdit, caseSkey, "isValid", "A", null);
    }
    if (getBoundarySpecified) {
      html += htmlForSpatialFunctionTest(testCaseEdit, runSkey, caseSkey,
        "getBoundary", "A", null);
    }

    html = "<h2>Topology Methods (on A)</h2>" + StringUtil.newLine
      + "<TABLE BORDER=1>" + StringUtil.newLine + html + "</TABLE>"
      + StringUtil.newLine;
    return html;
  }

  private String htmlImageHtmlTextTable(final String imageFilename,
    final String html, final int border) {
    return "    <TD>" + StringUtil.newLine + "      <IMG BORDER=\"1\" SRC=\""
      + imageFilename + "\" WIDTH=" + IMAGE_WIDTH + " HEIGHT=" + IMAGE_HEIGHT
      + ">" + StringUtil.newLine + "    </TD>" + StringUtil.newLine
      + "    <TD>" + StringUtil.newLine + html + StringUtil.newLine
      + "    </TD>" + StringUtil.newLine;
  }

  private String htmlImageTextTable(final String imageFilename,
    final String text, final int border) {
    return htmlImageHtmlTextTable(imageFilename, StringUtil.escapeHTML(text),
      border);
  }

  private String htmlTitle(final PrecisionModel precisionModel) {
    String html = "Precision Model: scale=" + precisionModel.getScale()
      + StringUtil.newLine;
    html = "<div class='precisionModel'>" + html + "</div>";
    return html;
  }

  /*
   * private void createGifFile(String filenameNoPath, Geometry a, Geometry b,
   * Geometry spatialFunction, boolean showingAB, int imageWidth, int
   * imageHeight, boolean zoomToFullExtent) throws FileNotFoundException,
   * IOException { GeometryBuilder builderA = a != null ?
   * GeometryBuilder.create(a) : null; GeometryBuilder builderB = b != null ?
   * GeometryBuilder.create(b) : null; GeometryBuilder builderSpatialFunction =
   * spatialFunction != null ? GeometryBuilder.create(spatialFunction) : null;
   * createGifFile(filenameNoPath, builderA, builderB, builderSpatialFunction,
   * showingAB, imageWidth, imageHeight, zoomToFullExtent); }
   */

  private String indexHtml(final List runs, final Map runMap,
    final PrecisionModel precisionModel) {
    String html = "<HTML>"
      + StringUtil.newLine
      + "<HEAD>"
      + StringUtil.newLine
      + "<TITLE>JTS Test Suite Index</TITLE>"
      + StringUtil.newLine
      + "<link REL='STYLESHEET' HREF='../jts.css' TYPE='Text/css'>"
      + StringUtil.newLine
      + "<script LANGUAGE=\"JavaScript\">"
      + StringUtil.newLine
      + "  function LoadDetailFrame() {"
      + StringUtil.newLine
      + "        testNumber = document.main_form.test_combo.selectedIndex;"
      + StringUtil.newLine
      + "        testHtmlFile = document.main_form.test_combo.options[testNumber].value;"
      + StringUtil.newLine
      + "        parent.detail.location.href=testHtmlFile;"
      + StringUtil.newLine
      + "        document.main_form.test_combo.blur();"
      + StringUtil.newLine
      + "  }"
      + StringUtil.newLine
      + "  function onRunChange() {"
      + StringUtil.newLine
      + "        selectedIndex = document.main_form.run_combo.selectedIndex;"
      + StringUtil.newLine
      + "        selectedCode  = document.main_form.run_combo.options[selectedIndex].value;"
      + StringUtil.newLine;
    int runSkey = 0;
    for (final Iterator i = runs.iterator(); i.hasNext();) {
      final String runDescription = (String)i.next();
      runSkey++;
      html += "        if (selectedCode == 'Run" + runSkey + "') {"
        + StringUtil.newLine;
      final List testables = (List)runMap.get(runDescription);
      int caseSkey = 0;
      for (final Iterator m = testables.iterator(); m.hasNext();) {
        final Testable testable = (Testable)m.next();
        caseSkey++;
        html += "              document.main_form.test_combo.length = "
          + caseSkey + ";" + StringUtil.newLine;
        html += "              document.main_form.test_combo.options["
          + (caseSkey - 1) + "].text  = \""
          + StringUtil.escapeHTML(testName(testable, caseSkey)) + "\";"
          + StringUtil.newLine;
        html += "              document.main_form.test_combo.options["
          + (caseSkey - 1) + "].value  = 'Run" + runSkey + "Case" + caseSkey
          + ".html';" + StringUtil.newLine;
      }
      html += "        LoadDetailFrame();";
      html += "  }";
    }
    html += "  }" + StringUtil.newLine + "</script>" + StringUtil.newLine
      + "</HEAD>" + StringUtil.newLine + "<BODY>" + StringUtil.newLine
      + "<h1>JTS Validation Suite</h1>" + StringUtil.newLine
      + htmlTitle(precisionModel) + "<p>" + StringUtil.newLine
      + "<FORM id=\"main_form\" name=\"main_form\">" + StringUtil.newLine;

    html += "<select id=run_combo name=run_combo size='1' style='width:30%' onChange='onRunChange()'>"
      + StringUtil.newLine;
    runSkey = 0;
    for (final Iterator j = runs.iterator(); j.hasNext();) {
      final String runDescription = (String)j.next();
      runSkey++;
      html += "<OPTION VALUE='Run" + runSkey + "'>"
        + StringUtil.escapeHTML(runName(runDescription, runSkey)) + "</OPTION>"
        + StringUtil.newLine;
    }
    html += "</select>" + StringUtil.newLine;

    html += "<select id=test_combo name=test_combo size='1' style='width:60%' onChange='LoadDetailFrame()'>"
      + StringUtil.newLine;
    final String runDescription = (String)runs.iterator().next();
    final List testables = (List)runMap.get(runDescription);
    int caseSkey = 0;
    for (final Iterator m = testables.iterator(); m.hasNext();) {
      final Testable testable = (Testable)m.next();
      caseSkey++;
      html += "<OPTION VALUE='Run1Case" + caseSkey + ".html'>"
        + StringUtil.escapeHTML(testName(testable, caseSkey)) + "</OPTION>"
        + StringUtil.newLine;
    }
    html += "</select>" + StringUtil.newLine;

    html += "</FORM>" + StringUtil.newLine + "</BODY>" + StringUtil.newLine
      + "</HTML>" + StringUtil.newLine + "" + StringUtil.newLine;
    return html;
  }

  private String insertParagraphs(final String intersectionMatrix) {
    final StringBuffer buffer = new StringBuffer(intersectionMatrix);
    buffer.insert(6, "<BR>");
    buffer.insert(3, "<BR>");
    return buffer.toString();
  }

  private MapAndList runMapAndRuns(final TestCaseList testCaseList) {
    final Map runMap = new TreeMap();
    final List runs = new ArrayList();
    for (final Iterator i = testCaseList.getList().iterator(); i.hasNext();) {
      final TestCaseEdit testCaseEdit = (TestCaseEdit)i.next();
      final Testable testable = testCaseEdit.getTestable();
      if (testable instanceof TestRunnerTestCaseAdapter) {
        final TestCase testRunnerTestCase = ((TestRunnerTestCaseAdapter)testable).getTestRunnerTestCase();
        String runDescription = testRunnerTestCase.getTestRun()
          .getFile()
          .getName();
        runDescription = runDescription.indexOf(".") > -1 ? runDescription.substring(
          0, runDescription.indexOf(".")) : runDescription;
        addToListMapAndList(runDescription, testCaseEdit, runMap, runs);
      } else {
        addToListMapAndList("Other", testCaseEdit, runMap, runs);
      }
    }
    final MapAndList runMapAndRuns = new MapAndList();
    runMapAndRuns.map = runMap;
    runMapAndRuns.list = runs;
    return runMapAndRuns;
  }

  private String runName(final String runDescription, final int runSkey) {
    return "Run " + runSkey + ": " + runDescription;
  }

  public void setBusyDialog(final BusyDialog busyDialog) {
    this.busyDialog = busyDialog;
  }

  public void setShowingABwithSpatialFunction(
    final boolean showingABwithSpatialFunction) {
    this.showingABwithSpatialFunction = showingABwithSpatialFunction;
  }

  private String testName(final Testable testable, final int caseSkey) {
    String name = testable.getName();
    if ((name == null || name.length() == 0)
      && testable instanceof TestCaseEdit) {
      name = ((TestCaseEdit)testable).getDescription();
    }
    final String testTag = AppStrings.LABEL_TEST_CASE + " ";
    if (name == null || name.length() == 0) {
      name = testTag + caseSkey;
    } else {
      name = testTag + caseSkey + ": " + name;
    }
    return name;
  }

  private String testTopHtml() {
    return "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Frameset//EN\"\"http://www.w3.org/TR/REC-html40/frameset.dtd\">"
      + StringUtil.newLine
      + "<HTML>"
      + StringUtil.newLine
      + "<HEAD>"
      + StringUtil.newLine
      + "<TITLE>"
      + StringUtil.newLine
      + "JTS Validation Suite"
      + StringUtil.newLine
      + "</TITLE>"
      + StringUtil.newLine
      + "</HEAD>"
      + StringUtil.newLine
      + "<FRAMESET rows=\"120px,*\" framespacing=0 frameborder=0>"
      + StringUtil.newLine
      + "<FRAME id=contents name=\"contents\"  FRAMEBORDER=0 src=\"contents-frame.html\" scrolling=no>"
      + StringUtil.newLine
      + "<FRAME id=detail name=\"detail\"	FRAMEBORDER=0 src=\"Run1Case1.html\" >"
      + StringUtil.newLine
      + "</FRAMESET>"
      + StringUtil.newLine
      + "<NOFRAMES>"
      + StringUtil.newLine
      + "<H2>"
      + StringUtil.newLine
      + "Frame Alert</H2>"
      + StringUtil.newLine
      + ""
      + StringUtil.newLine
      + "<P>"
      + StringUtil.newLine
      + "This site is designed to be viewed using frames. "
      + StringUtil.newLine
      + "If you see this message, you are using a non-frame-capable web client."
      + StringUtil.newLine + "</HTML>" + StringUtil.newLine;
  }

  public void write(final File outputDirectory,
    final TestCaseList testCaseList, final PrecisionModel precisionModel)
    throws IOException {
    if (busyDialog != null) {
      busyDialog.setDescription("Saving .html and .gif files");
    }
    Assert.isTrue(outputDirectory.isDirectory());
    this.outputDirectory = outputDirectory;
    final MapAndList runMapAndRuns = runMapAndRuns(testCaseList);
    final Map runMap = runMapAndRuns.map;
    final List runs = runMapAndRuns.list;
    createHtmlFile("contents-frame.html",
      indexHtml(runs, runMap, precisionModel));
    createHtmlFile("index.html", testTopHtml());
    int runSkey = 0;
    for (final Iterator i = runs.iterator(); i.hasNext();) {
      final String runDescription = (String)i.next();
      runSkey++;
      final List testables = (List)runMap.get(runDescription);
      int caseSkey = 0;
      for (final Iterator m = testables.iterator(); m.hasNext();) {
        final Testable testable = (Testable)m.next();
        caseSkey++;
        if (busyDialog != null) {
          busyDialog.setDescription("Saving .html and .gif files: " + caseSkey
            + " of " + testCaseList.getList().size() + " tests");
        }
        createHtmlFile("Run" + runSkey + AppStrings.LABEL_TEST_CASE + caseSkey
          + ".html", html(testable, runSkey, caseSkey));
      }
    }
  }
}
