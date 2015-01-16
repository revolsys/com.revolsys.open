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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jtstest.function.GeometryFunctionRegistry;
import com.revolsys.jtstest.function.TestCaseGeometryFunctions;
import com.revolsys.jtstest.geomop.GeometryFunctionOperation;
import com.revolsys.jtstest.geomop.GeometryOperation;
import com.revolsys.jtstest.util.FileUtil;
import com.revolsys.jtstest.util.LineNumberElement;
import com.revolsys.jtstest.util.LineNumberSAXBuilder;
import com.revolsys.jtstest.util.WKTOrWKBReader;

/**
 * @version 1.7
 */
public class TestReader {
  public static final EqualityResultMatcher EQUALITY_RESULT_MATCHER = new EqualityResultMatcher();

  public static final GeometryFunctionRegistry GEOMETRY_FUNCTION_REGISTRY = new GeometryFunctionRegistry(
    TestCaseGeometryFunctions.class);

  public static final GeometryFunctionOperation GEOMETRY_FUNCTION_OPERATION = new GeometryFunctionOperation(
    GEOMETRY_FUNCTION_REGISTRY);

  private GeometryFactory geometryFactory;

  private WKTOrWKBReader wktorbReader;

  private double tolerance = 0.0;

  private GeometryOperation geomOp = GEOMETRY_FUNCTION_OPERATION;

  private ResultMatcher resultMatcher = EQUALITY_RESULT_MATCHER;

  public TestReader() {
  }

  private File absoluteWktFile(final File wktFile, final TestFile testRun) {
    if (wktFile == null) {
      return null;
    }
    File absoluteWktFile = wktFile;
    if (!absoluteWktFile.isAbsolute()) {
      final File directory = testRun.getWorkspace() != null ? testRun.getWorkspace()
        : testRun.getFile().getParentFile();
      absoluteWktFile = new File(directory + File.separator
        + absoluteWktFile.getName());
    }
    return absoluteWktFile;
  }

  private double createPrecisionModel(final Element precisionModelElement)
      throws TestParseException {
    final Attribute scaleAttribute = precisionModelElement.getAttribute("scale");
    if (scaleAttribute == null) {
      throw new TestParseException(
          "Missing scale attribute in <precisionModel>");
    }
    double scale;
    try {
      scale = scaleAttribute.getDoubleValue();
    } catch (final DataConversionException e) {
      throw new TestParseException(
        "Could not convert scale attribute to double: "
            + scaleAttribute.getValue());
    }
    return scale;
  }

  public TestFile createTestRun(final TestDirectory parent,
    final File testFile, final int runIndex) throws Throwable {
    try {
      final SAXBuilder builder = new LineNumberSAXBuilder();
      final Document document = builder.build(new FileInputStream(testFile));
      final Element runElement = document.getRootElement();
      if (!runElement.getName().equalsIgnoreCase("run")) {
        throw new TestParseException("Expected <run> but encountered <"
            + runElement.getName() + ">");
      }
      return parseTestRun(parent, runElement, testFile, runIndex);
    } catch (final IllegalArgumentException e) {
      throw e;
    } catch (final Exception e) {
      throw new IllegalArgumentException("Error parsing " + testFile, e);
    }
  }

  public GeometryOperation getGeometryOperation() {
    return this.geomOp;
  }

  /**
   * Gets an instance of a class with the given name,
   * and ensures that the class is assignable to a specified baseClass.
   *
   * @return an instance of the class, if it is assignment-compatible, or
   *  null if the requested class is not assigment-compatible
   */
  private Object getInstance(final String classname, final Class<?> baseClass) {
    Object o = null;
    try {
      final Class<?> goClass = Class.forName(classname);
      if (!baseClass.isAssignableFrom(goClass)) {
        return null;
      }
      o = goClass.newInstance();
    } catch (final Exception ex) {
      return null;
    }
    return o;
  }

  public boolean isBooleanFunction(final String name) {
    return getGeometryOperation().getReturnType(name) == boolean.class;
  }

  public boolean isDoubleFunction(final String name) {
    return getGeometryOperation().getReturnType(name) == double.class;
  }

  public boolean isGeometryFunction(final String name) {
    final Class<?> returnType = getGeometryOperation().getReturnType(name);
    if (returnType == null) {
      return false;
    }
    return Geometry.class.isAssignableFrom(returnType);
  }

  public boolean isIntegerFunction(final String name) {
    return getGeometryOperation().getReturnType(name) == int.class;
  }

  /**
   * Parses an optional <tt>geometryOperation</tt> element.
   * The default is to leave this unspecified .
   *
   * @param runElement
   * @return an instance of the GeometryOperation class, if specified, or
   * null if no geometry operation was specified
   * @throws TestParseException if a parsing error was encountered
   */
  private GeometryOperation parseGeometryOperation(final Element runElement)
      throws TestParseException {
    final Element goElement = runElement.getChild("geometryOperation");
    if (goElement == null) {
      return GEOMETRY_FUNCTION_OPERATION;
    }
    final String goClass = goElement.getTextTrim();
    final GeometryOperation geomOp = (GeometryOperation)getInstance(goClass,
      GeometryOperation.class);
    if (geomOp == null) {
      throw new TestParseException(
        "Could not create instance of GeometryOperation from class " + goClass);
    }
    return geomOp;
  }

  /**
   * Parses an optional <tt>precisionModel</tt> element.
   * The default is to use a FLOATING model.
   *
   * @param runElement
   * @return a PrecisionModel instance (default if not specified)
   * @throws TestParseException
   */
  private double parsePrecisionModel(final Element runElement)
      throws TestParseException {
    final Element precisionModelElement = runElement.getChild("precisionModel");
    if (precisionModelElement == null) {
      return 0;
    }
    final Attribute typeAttribute = precisionModelElement.getAttribute("type");
    final Attribute scaleAttribute = precisionModelElement.getAttribute("scale");
    if (typeAttribute == null && scaleAttribute == null) {
      throw new TestParseException("Missing type attribute in <precisionModel>");
    }
    if (scaleAttribute != null || typeAttribute != null
        && typeAttribute.getValue().trim().equalsIgnoreCase("FIXED")) {
      if (typeAttribute != null
          && typeAttribute.getValue().trim().equalsIgnoreCase("FLOATING")) {
        throw new TestParseException(
            "scale attribute not allowed in floating <precisionModel>");
      }
      return createPrecisionModel(precisionModelElement);
    }
    return 0;
  }

  /**
   * Parses an optional <tt>resultMatcher</tt> element.
   * The default is to leave this unspecified .
   *
   * @param runElement
   * @return an instance of the ResultMatcher class, if specified, or
   *  null if no result matcher was specified
   * @throws TestParseException if a parsing error was encountered
   */
  private ResultMatcher parseResultMatcher(final Element runElement)
      throws TestParseException {
    final Element goElement = runElement.getChild("resultMatcher");
    if (goElement == null) {
      return EQUALITY_RESULT_MATCHER;
    }
    final String goClass = goElement.getTextTrim();
    final ResultMatcher resultMatcher = (ResultMatcher)getInstance(goClass,
      ResultMatcher.class);
    if (resultMatcher == null) {
      throw new TestParseException(
        "Could not create instance of ResultMatcher from class " + goClass);
    }
    return resultMatcher;
  }

  /**
   *  Creates a List of TestCase's from the given <case> Element's.
   */
  private List<TestCase> parseTestCases(final List caseElements,
    final File testFile, final TestFile testRun, final double tolerance)
        throws Throwable {
    this.wktorbReader = new WKTOrWKBReader(this.geometryFactory);
    final Vector<TestCase> testCases = new Vector<>();
    int caseIndex = 0;
    for (final Iterator i = caseElements.iterator(); i.hasNext();) {
      final Element caseElement = (Element)i.next();
      // System.out.println("Line: " +
      // ((LineNumberElement)caseElement).getStartLine());
      caseIndex++;
      try {
        final Element descElement = caseElement.getChild("desc");
        final Element aElement = caseElement.getChild("a");
        final Element bElement = caseElement.getChild("b");
        final File aWktFile = wktFile(aElement, testRun);
        final File bWktFile = wktFile(bElement, testRun);
        final Geometry a = readGeometry(aElement,
          absoluteWktFile(aWktFile, testRun));
        final Geometry b = readGeometry(bElement,
          absoluteWktFile(bWktFile, testRun));
        final TestCase testCase = new TestCase(
          descElement != null ? descElement.getTextTrim() : "", a, b, aWktFile,
            bWktFile, testRun, caseIndex,
            ((LineNumberElement)caseElement).getStartLine());
        final List testElements = caseElement.getChildren("test");
        // if (testElements.size() == 0) {
        // throw new TestParseException("Missing <test> in <case>");
        // }
        final List<GeometryOperationTest> tests = parseTests(testElements,
          caseIndex, testFile, testCase, tolerance);
        for (final GeometryOperationTest test : tests) {
          testCase.add(test);
        }
        testCases.add(testCase);
      } catch (final Exception e) {
        throw new IllegalArgumentException(
          "An exception occurred while parsing <case> " + caseIndex + " in "
              + testFile, e);
      }
    }
    return testCases;
  }

  /**
   *  Creates a TestRun from the <run> Element.
   * @param parent
   */
  private TestFile parseTestRun(final TestDirectory parent,
    final Element runElement, final File testFile, final int runIndex)
        throws Throwable {

    // ----------- <workspace> (optional) ------------------
    File workspace = null;
    if (runElement.getChild("workspace") != null) {
      if (runElement.getChild("workspace").getAttribute("dir") == null) {
        throw new TestParseException("Missing <dir> in <workspace>");
      }
      workspace = new File(runElement.getChild("workspace")
        .getAttribute("dir")
        .getValue()
        .trim());
      if (!workspace.exists()) {
        throw new TestParseException("<workspace> does not exist: " + workspace);
      }
      if (!workspace.isDirectory()) {
        throw new TestParseException("<workspace> is not a directory: "
            + workspace);
      }
    }

    // ----------- <tolerance> (optional) ------------------
    this.tolerance = parseTolerance(runElement);

    final Element descElement = runElement.getChild("desc");

    // ----------- <geometryOperation> (optional) ------------------
    this.geomOp = parseGeometryOperation(runElement);

    // ----------- <geometryMatcher> (optional) ------------------
    this.resultMatcher = parseResultMatcher(runElement);

    // ----------- <precisionModel> (optional) ----------------
    final double scale = parsePrecisionModel(runElement);
    this.geometryFactory = GeometryFactory.fixed(0, scale);

    // --------------- build TestRun ---------------------
    final String description = descElement != null ? descElement.getTextTrim()
      : "";
    final TestFile testRun = new TestFile(parent, description, runIndex,
      this.geometryFactory, this.geomOp, this.resultMatcher, testFile);
    testRun.setWorkspace(workspace);
    final List caseElements = runElement.getChildren("case");
    if (caseElements.size() == 0) {
      throw new TestParseException("Missing <case> in <run>");
    }
    for (final TestCase testCase : parseTestCases(caseElements, testFile,
      testRun, this.tolerance)) {
      testRun.addTest(testCase);
    }
    return testRun;
  }

  /**
   *  Creates a List of Test's from the given <test> Element's.
   */
  private List<GeometryOperationTest> parseTests(final List testElements,
    final int caseIndex, final File testFile, final TestCase testCase,
    final double tolerance) throws Throwable {
    final List<GeometryOperationTest> tests = new ArrayList<>();
    int testIndex = 0;
    for (final Iterator i = testElements.iterator(); i.hasNext();) {
      final Element testElement = (Element)i.next();
      testIndex++;
      try {
        final Element descElement = testElement.getChild("desc");
        if (testElement.getChildren("op").size() > 1) {
          throw new TestParseException("Multiple <op>s in <test>");
        }
        final Element opElement = testElement.getChild("op");
        if (opElement == null) {
          throw new TestParseException("Missing <op> in <test>");
        }
        final Attribute nameAttribute = opElement.getAttribute("name");
        if (nameAttribute == null) {
          throw new TestParseException("Missing name attribute in <op>");
        }
        final String arg1 = opElement.getAttribute("arg1") == null ? "A"
          : opElement.getAttribute("arg1").getValue().trim();
        final String arg2 = opElement.getAttribute("arg2") == null ? null
          : opElement.getAttribute("arg2").getValue().trim();
        String arg3 = opElement.getAttribute("arg3") == null ? null
          : opElement.getAttribute("arg3").getValue().trim();
        if (arg3 == null
            && nameAttribute.getValue().trim().equalsIgnoreCase("relate")) {
          arg3 = opElement.getAttribute("pattern") == null ? null
            : opElement.getAttribute("pattern").getValue().trim();
        }
        final ArrayList arguments = new ArrayList();
        if (arg2 != null) {
          arguments.add(arg2);
        }
        if (arg3 != null) {
          arguments.add(arg3);
        }
        final Result result = toResult(opElement.getTextTrim(),
          nameAttribute.getValue().trim(), testCase.getTestRun());
        final GeometryOperationTest test = new GeometryOperationTest(testCase,
          testIndex, descElement != null ? descElement.getTextTrim() : "",
            nameAttribute.getValue().trim(), arg1, arguments, result, tolerance);

        tests.add(test);
      } catch (final Exception e) {
        throw new IllegalArgumentException(
          "An exception occurred while parsing <test> " + testIndex
          + " in <case> " + caseIndex + " in " + testFile, e);
      }
    }
    return tests;
  }

  private double parseTolerance(final Element runElement)
      throws TestParseException {
    double tolerance = 0.0;
    // Note: the tolerance element applies to the coordinate-by-coordinate
    // comparisons of spatial functions. It does not apply to binary predicates.
    // [Jon Aquino]
    final Element toleranceElement = runElement.getChild("tolerance");
    if (toleranceElement != null) {
      try {
        tolerance = Double.parseDouble(toleranceElement.getTextTrim());
      } catch (final NumberFormatException e) {
        throw new TestParseException("Could not parse tolerance from string: "
            + toleranceElement.getTextTrim());
      }
    }
    return tolerance;
  }

  private Geometry readGeometry(final Element geometryElement,
    final File wktFile) throws FileNotFoundException,
    com.revolsys.jts.io.ParseException, IOException {
    String geomText = null;
    if (wktFile != null) {
      final List wktList = FileUtil.getContents(wktFile.getPath());
      geomText = toString(wktList);
    } else {
      if (geometryElement == null) {
        return null;
      }
      geomText = geometryElement.getTextTrim();
    }
    return this.wktorbReader.read(geomText);
    /*
     * if (isHex(geomText, 6)) return
     * wkbReader.read(WKBReader.hexToBytes(geomText)); reurn
     * wktReader.read(geomText);
     */
  }

  private BooleanResult toBooleanResult(final String value)
      throws TestParseException {
    if (value.equalsIgnoreCase("true")) {
      return new BooleanResult(true);
    } else if (value.equalsIgnoreCase("false")) {
      return new BooleanResult(false);
    } else {
      throw new TestParseException(
        "Expected 'true' or 'false' but encountered '" + value + "'");
    }
  }

  private DoubleResult toDoubleResult(final String value)
      throws TestParseException {
    try {
      return new DoubleResult(Double.valueOf(value));
    } catch (final NumberFormatException e) {
      throw new TestParseException("Expected double but encountered '" + value
        + "'");
    }
  }

  /*
   * private GeometryOperation getGeometryOperationInstance(String classname) {
   * GeometryOperation op = null; try { Class goClass =
   * Class.forName(classname); if
   * (!(GeometryOperation.class.isAssignableFrom(goClass))) return null; op =
   * (GeometryOperation) goClass.newInstance(); } catch (Exception ex) { return
   * null; } return op; }
   */

  private GeometryResult toGeometryResult(final String value,
    final TestFile testRun) throws com.revolsys.jts.io.ParseException {
    final GeometryFactory geometryFactory = GeometryFactory.floating(0, 2);
    final WKTOrWKBReader wktorbReader = new WKTOrWKBReader(geometryFactory);
    return new GeometryResult(wktorbReader.read(value));
  }

  private IntegerResult toIntegerResult(final String value)
      throws TestParseException {
    try {
      return new IntegerResult(Integer.valueOf(value));
    } catch (final NumberFormatException e) {
      throw new TestParseException("Expected integer but encountered '" + value
        + "'");
    }
  }

  private Result toResult(final String value, final String name,
    final TestFile testRun) throws TestParseException,
    com.revolsys.jts.io.ParseException {
    if (isBooleanFunction(name)) {
      return toBooleanResult(value);
    }
    if (isIntegerFunction(name)) {
      return toIntegerResult(value);
    }
    if (isDoubleFunction(name)) {
      return toDoubleResult(value);
    }
    if (isGeometryFunction(name)) {
      return toGeometryResult(value, testRun);
    }
    throw new TestParseException("Unknown operation name '" + name + "'");
    // return null;
  }

  private String toString(final List<String> stringList) {
    String string = "";
    for (final String line : stringList) {
      string += line + "\n";
    }
    return string;
  }

  private File wktFile(final Element geometryElement, final TestFile testRun)
      throws TestParseException {
    if (geometryElement == null) {
      return null;
    }
    if (geometryElement.getAttribute("file") == null) {
      return null;
    }
    if (!geometryElement.getTextTrim().equals("")) {
      throw new TestParseException(
          "WKT specified both in-line and in external file");
    }

    final File wktFile = new File(geometryElement.getAttribute("file")
      .getValue()
      .trim());
    final File absoluteWktFile = absoluteWktFile(wktFile, testRun);

    if (!absoluteWktFile.exists()) {
      throw new TestParseException("WKT file does not exist: "
          + absoluteWktFile);
    }
    if (absoluteWktFile.isDirectory()) {
      throw new TestParseException("WKT file is a directory: "
          + absoluteWktFile);
    }

    return wktFile;
  }
}
