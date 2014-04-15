package com.revolsys.jtstest.testbuilder.model;

import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.io.WKTWriter;
import com.revolsys.jts.util.Assert;
import com.revolsys.jtstest.test.TestCaseList;
import com.revolsys.jtstest.test.Testable;
import com.revolsys.jtstest.testbuilder.AppConstants;
import com.revolsys.jtstest.testbuilder.ui.SwingUtil;
import com.revolsys.jtstest.testbuilder.ui.style.BasicStyle;
import com.revolsys.jtstest.testrunner.StringUtil;
import com.revolsys.jtstest.testrunner.TestCase;
import com.revolsys.jtstest.testrunner.TestReader;
import com.revolsys.jtstest.testrunner.TestRun;
import com.revolsys.jtstest.util.IOUtil;
import com.revolsys.jtstest.util.MultiFormatReader;

public class TestBuilderModel {
  private class IteratorWrapper implements ListIterator {
    ListIterator i;

    public IteratorWrapper(final ListIterator i) {
      this.i = i;
    }

    @Override
    public void add(final Object o) {
      checkStop();
      i.add(o);
    }

    private void checkStop() {
      final int a = 5;
    }

    @Override
    public boolean hasNext() {
      return i.hasNext();
    }

    @Override
    public boolean hasPrevious() {
      return i.hasPrevious();
    }

    @Override
    public Object next() {
      checkStop();
      return i.next();
    }

    @Override
    public int nextIndex() {
      return i.nextIndex();
    }

    @Override
    public Object previous() {
      checkStop();
      return i.previous();
    }

    @Override
    public int previousIndex() {
      return i.previousIndex();
    }

    @Override
    public void remove() {
      checkStop();
      i.remove();
    }

    @Override
    public void set(final Object o) {
      checkStop();
      i.set(o);
    }
  }

  public static int MAX_DISPLAY_POINTS = 2000;

  protected static boolean showingGrid = true;

  protected static boolean showingStructure = false;

  protected static boolean showingOrientation = false;

  protected static boolean showingVertices = true;

  protected static boolean showingCoordinates = true;

  protected static boolean isMagnifyingTopology = false;

  protected static double topologyStretchSize = AppConstants.TOPO_STRETCH_VIEW_DIST;

  public static boolean isShowingGrid() {
    return showingGrid;
  }

  public static boolean isShowingOrientation() {
    return showingOrientation;
  }

  public static boolean isShowingStructure() {
    return showingStructure;
  }

  public static boolean isShowingVertices() {
    return showingVertices;
  }

  public static void setShowingGrid(final boolean show) {
    showingGrid = show;
  }

  public static void setShowingOrientation(final boolean show) {
    showingOrientation = show;
  }

  public static void setShowingStructure(final boolean show) {
    showingStructure = show;
  }

  public static void setShowingVertices(final boolean show) {
    showingVertices = show;
  }

  private GeometryFactory geometryFactory = GeometryFactory.getFactory();

  private final GeometryEditModel geomEditModel;

  private final LayerList layerList = new LayerList();

  private final WKTWriter writer = new WKTWriter();

  private Object currResult = null;

  private String opName = "";

  private TestCaseList tcList = new TestCaseList();

  private ListIterator tcListi;

  private List parseErrors = null;

  /**
   *  The current test case (if any). Invariant: currTestCase = tcListi.prev()
   */
  private TestCaseEdit currTestCase;

  private final ArrayList wktABeforePMChange = new ArrayList();

  private final ArrayList wktBBeforePMChange = new ArrayList();

  public TestBuilderModel() {
    geomEditModel = new GeometryEditModel();
    initLayers();
    initTestCaseList();
  }

  public void addCase(final Geometry[] geoms) {
    addCase(geoms, null);
  }

  public void addCase(final Geometry[] geoms, final String name) {
    TestCaseEdit copy = null;
    copy = new TestCaseEdit(geoms, name);
    tcListi.add(copy);
    currTestCase = copy;
  }

  public void changePrecisionModel(final PrecisionModel precisionModel)
    throws ParseException {
    saveWKTBeforePMChange();
    // setPrecisionModel(precisionModel);
    loadWKTAfterPMChange();
  }

  public void copyCase() {
    TestCaseEdit copy = null;
    copy = new TestCaseEdit(currTestCase);
    tcListi.add(copy);
    currTestCase = copy;
  }

  public void copyResult(final boolean isFormatted) {
    SwingUtil.copyToClipboard(currResult, isFormatted);
  }

  public void createNew() {
    // move to end of list
    while (tcListi.hasNext()) {
      tcListi.next();
    }
    currTestCase = new TestCaseEdit(geometryFactory);
    tcListi.add(currTestCase);
  }

  private TestCaseList createTestCaseList(final File xmlTestFile) {
    final TestReader testReader = new TestReader();
    final TestRun testRun = testReader.createTestRun(xmlTestFile, 1);
    parseErrors = testReader.getParsingProblems();

    final TestCaseList tcl = new TestCaseList();
    if (hasParseErrors()) {
      return tcl;
    }
    for (final Iterator i = testRun.getTestCases().iterator(); i.hasNext();) {
      final TestCase testCase = (TestCase)i.next();
      tcl.add(new TestRunnerTestCaseAdapter(testCase));
    }
    return tcl;
  }

  private TestCaseList createTestCaseList(final File[] filesAndDirectories) {
    final TestCaseList testCaseList = new TestCaseList();
    for (int i = 0; i < filesAndDirectories.length; i++) {
      final File fileOrDirectory = filesAndDirectories[i];
      if (fileOrDirectory.isFile()) {
        testCaseList.add(createTestCaseList(fileOrDirectory));
      } else if (fileOrDirectory.isDirectory()) {
        testCaseList.add(createTestCaseListFromDirectory(fileOrDirectory));
      }
    }
    return testCaseList;
  }

  private TestCaseList createTestCaseListFromDirectory(final File directory) {
    Assert.isTrue(directory.isDirectory());
    final TestCaseList testCaseList = new TestCaseList();
    final List files = Arrays.asList(directory.listFiles());
    for (final Iterator i = files.iterator(); i.hasNext();) {
      final File file = (File)i.next();
      testCaseList.add(createTestCaseList(file));
    }
    return testCaseList;
  }

  public void deleteCase() {
    // corner case - handle case where list has only one element
    if (tcList.getList().size() == 1) {
      tcListi.previous();
    }
    tcListi.remove();

    if (tcListi.hasNext()) {
      currTestCase = (TestCaseEdit)tcListi.next();
    } else if (tcListi.hasPrevious()) {
      currTestCase = (TestCaseEdit)tcListi.previous();
    } else {
      createNew();
    }
  }

  public Testable getCurrentTestable() {
    return currTestCase;
  }

  /*
   * public Geometry readMultipleGeometriesFromFile(String filename) throws
   * Exception, IOException { String ext = FileUtil.extension(filename); if
   * (ext.equalsIgnoreCase("shp")) return
   * readMultipleGeometriesFromShapefile(filename); return
   * readMultipleGeometryFromWKT(filename); } private Geometry
   * readMultipleGeometriesFromShapefile(String filename) throws Exception {
   * Shapefile shpfile = new Shapefile(new FileInputStream(filename));
   * GeometryFactory geomFact = getGeometryFactory();
   * shpfile.readStream(geomFact); List geomList = new ArrayList(); do {
   * Geometry geom = shpfile.next(); if (geom == null) break;
   * geomList.add(geom); } while (true); return
   * geomFact.createGeometryCollection
   * (GeometryFactory.toGeometryArray(geomList)); } private Geometry
   * readMultipleGeometryFromWKT(String filename) throws ParseException,
   * IOException { return
   * readMultipleGeometryFromWKTString(FileUtil.readText(filename)); } private
   * Geometry readMultipleGeometryFromWKTString(String geoms) throws
   * ParseException, IOException { GeometryFactory geomFact =
   * getGeometryFactory(); WKTReader reader = new WKTReader(geomFact);
   * WKTFileReader fileReader = new WKTFileReader(new StringReader(geoms),
   * reader); List geomList = fileReader.read(); if (geomList.size() == 1)
   * return (Geometry) geomList.get(0); // TODO: turn polygons into a GC return
   * geomFact.buildGeometry(geomList); }
   */

  // =============================================================

  public TestCaseEdit getCurrentTestCaseEdit() {
    return currTestCase;
  }

  public int getCurrentTestIndex() {
    return tcListi.previousIndex();
  }

  public GeometryEditModel getGeometryEditModel() {
    return geomEditModel;
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public LayerList getLayers() {
    return layerList;
  }

  public String getOpName() {
    return opName;
  }

  /**
   * 
   * @return empy list if no errors
   */
  public List getParsingProblems() {
    return parseErrors;
  }

  public Object getResult() {
    return currResult;
  }

  public String getResultDisplayString(final Geometry g) {
    if (g == null) {
      return "";
    }
    if (g.getVertexCount() > MAX_DISPLAY_POINTS) {
      return GeometryEditModel.toStringVeryLarge(g);
    }
    return writer.writeFormatted(g);
  }

  public TestCaseList getTestCaseList() {
    return tcList;
  }

  public java.util.List getTestCases() {
    return Collections.unmodifiableList(tcList.getList());
  }

  public TestCaseList getTestList() {
    return tcList;
  }

  public int getTestListSize() {
    return tcList.getList().size();
  }

  public double getTopologyStretchSize() {
    return topologyStretchSize;
  }

  public boolean hasParseErrors() {
    if (parseErrors == null) {
      return false;
    }
    return parseErrors.size() > 0;
  }

  private void initLayers() {
    /*
     * GeometryStretcherView stretcher = new
     * GeometryStretcherView(geomEditModel); GeometryContainer geomCont0 =
     * stretcher.getContainer(0); GeometryContainer geomCont1 =
     * stretcher.getContainer(1);
     */

    final GeometryContainer geomCont0 = new IndexedGeometryContainer(
      geomEditModel, 0);
    final GeometryContainer geomCont1 = new IndexedGeometryContainer(
      geomEditModel, 1);

    layerList.getLayer(LayerList.LYR_A).setSource(geomCont0);
    layerList.getLayer(LayerList.LYR_B).setSource(geomCont1);

    if (geomEditModel != null) {
      layerList.getLayer(LayerList.LYR_RESULT).setSource(
        new ResultGeometryContainer(geomEditModel));
    }

    final Layer lyrA = layerList.getLayer(LayerList.LYR_A);
    lyrA.setStyle(new BasicStyle(GeometryDepiction.GEOM_A_LINE_CLR,
      GeometryDepiction.GEOM_A_FILL_CLR));

    final Layer lyrB = layerList.getLayer(LayerList.LYR_B);
    lyrB.setStyle(new BasicStyle(GeometryDepiction.GEOM_B_LINE_CLR,
      GeometryDepiction.GEOM_B_FILL_CLR));

    final Layer lyrR = layerList.getLayer(LayerList.LYR_RESULT);
    lyrR.setStyle(new BasicStyle(GeometryDepiction.GEOM_RESULT_LINE_CLR,
      GeometryDepiction.GEOM_RESULT_FILL_CLR));
  }

  public void initList(final TestCaseList tcl) {
    tcList = tcl;
    tcListi = new IteratorWrapper(tcList.getList().listIterator());
    // ensure that there is always a valid TestCase in the list
    if (tcListi.hasNext()) {
      currTestCase = (TestCaseEdit)tcListi.next();
    } else {
      createNew();
    }
  }

  public void initTestCaseList() {
    tcList = new TestCaseList();
    tcListi = new IteratorWrapper(tcList.getList().listIterator());
    // ensure that there is always a valid TestCase in the list
    createNew();
  }

  public boolean isMagnifyingTopology() {
    return isMagnifyingTopology;
  }

  public void loadEditList(final TestCaseList tcl) throws ParseException {
    final TestCaseList newTcl = new TestCaseList();
    for (final Iterator i = tcl.getList().iterator(); i.hasNext();) {
      final Testable tc = (Testable)i.next();

      if (tc instanceof TestCaseEdit) {
        newTcl.add(tc);
      } else {
        newTcl.add(new TestCaseEdit(tc));
      }

    }
    initList(newTcl);
  }

  public void loadGeometryText(final String wktA, final String wktB)
    throws ParseException, IOException {
    final MultiFormatReader reader = new MultiFormatReader(getGeometryFactory());

    // read geom A
    Geometry g0 = null;
    if (wktA.length() > 0) {
      g0 = reader.read(wktA);
    }

    // read geom B
    Geometry g1 = null;
    if (wktB.length() > 0) {
      g1 = reader.read(wktB);
    }
    /*
     * if (moveToOrigin) { Coordinates offset = pickOffset(g0, g1); if (offset
     * == null) { return; } if (g0 != null) { g0 =
     * reader.read(offset(getGeometryTextA(), offset)); } if (g1 != null) { g1 =
     * reader.read(offset(getGeometryTextB(), offset)); } }
     */

    final TestCaseEdit testCaseEdit = getCurrentTestCaseEdit();
    testCaseEdit.setGeometry(0, g0);
    testCaseEdit.setGeometry(1, g1);
    getGeometryEditModel().setTestCase(testCaseEdit);
  }

  public void loadMultipleGeometriesFromFile(final int geomIndex,
    final String filename) throws Exception {
    final Geometry g = IOUtil.readGeometriesFromFile(filename,
      getGeometryFactory());
    final TestCaseEdit testCaseEdit = getCurrentTestCaseEdit();
    testCaseEdit.setGeometry(geomIndex, g);
    testCaseEdit.setName(filename);
    getGeometryEditModel().setTestCase(testCaseEdit);
  }

  void loadTestCaseList(final TestCaseList tcl,
    final GeometryFactory geometryFactory) throws Exception {
    setGeometryFactory(geometryFactory);
    if (tcl != null) {
      loadEditList(tcl);
    }
  }

  private void loadWKTAfterPMChange() throws ParseException {
    final WKTReader reader = new WKTReader(getGeometryFactory());
    for (int i = 0; i < getTestCaseList().getList().size(); i++) {
      final Testable testable = (Testable)getTestCaseList().getList().get(i);
      final String wktA = (String)wktABeforePMChange.get(i);
      final String wktB = (String)wktBBeforePMChange.get(i);
      testable.setGeometry(0, wktA != null ? reader.read(wktA) : null);
      testable.setGeometry(1, wktB != null ? reader.read(wktB) : null);
    }
  }

  public void nextCase() {
    // don't move past the last one
    if (tcListi.nextIndex() >= tcList.getList().size()) {
      return;
    }
    if (tcListi.hasNext()) {
      currTestCase = (TestCaseEdit)tcListi.next();
    }
  }

  private String offset(final String wellKnownText, final Coordinates offset)
    throws IOException {
    String offsetWellKnownText = "";
    final StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(
      wellKnownText));
    boolean xValue = false;
    int type = tokenizer.nextToken();
    while (type != StreamTokenizer.TT_EOF) {
      offsetWellKnownText += " ";
      switch (type) {
        case StreamTokenizer.TT_EOL:
        break;
        case StreamTokenizer.TT_NUMBER:
          xValue = !xValue;
          offsetWellKnownText += offsetNumber(tokenizer.nval, offset, xValue);
        break;
        case StreamTokenizer.TT_WORD:
          offsetWellKnownText += tokenizer.sval;
        break;
        case '(':
          offsetWellKnownText += "(";
        break;
        case ')':
          offsetWellKnownText += ")";
        break;
        case ',':
          offsetWellKnownText += ",";
        break;
        default:
          Assert.shouldNeverReachHere();
      }
      type = tokenizer.nextToken();
    }
    return offsetWellKnownText;
  }

  private double offsetNumber(final double number, final Coordinates offset,
    final boolean xValue) {
    return number - (xValue ? offset.getX() : offset.getY());
  }

  public void openXmlFilesAndDirectories(final File[] files) throws Exception {
    final TestCaseList testCaseList = createTestCaseList(files);
    GeometryFactory geometryFactory = GeometryFactory.getFactory();
    if (!testCaseList.getList().isEmpty()) {
      final TestRunnerTestCaseAdapter a = (TestRunnerTestCaseAdapter)testCaseList.getList()
        .get(0);
      geometryFactory = a.getTestRunnerTestCase()
        .getTestRun()
        .getGeometryFactory();
    }
    if (tcList.getList().size() == 1
      && ((Testable)tcList.getList().get(0)).getGeometry(0) == null
      && ((Testable)tcList.getList().get(0)).getGeometry(1) == null) {
      loadTestCaseList(testCaseList, geometryFactory);
    } else {
      final TestCaseList newList = new TestCaseList();
      newList.add(tcList);
      newList.add(testCaseList);
      loadTestCaseList(newList, geometryFactory);
    }
  }

  public void pasteGeometry(final int geomIndex) throws ParseException,
    IOException {
    final Object obj = SwingUtil.getFromClipboard();
    Geometry g = null;
    if (obj instanceof String) {
      g = readGeometryText((String)obj);
    } else {
      g = (Geometry)obj;
    }

    final TestCaseEdit testCaseEdit = getCurrentTestCaseEdit();
    testCaseEdit.setGeometry(geomIndex, g);
    getGeometryEditModel().setTestCase(testCaseEdit);
  }

  private Coordinates pickOffset(final Geometry a, final Geometry b) {
    if (a != null && !a.isEmpty()) {
      return a.getCoordinateArray()[0];
    }
    if (b != null && !b.isEmpty()) {
      return b.getCoordinateArray()[0];
    }
    return null;
  }

  public void prevCase() {
    // since current test case = tcListi.prev, to
    // display the case *before* the current one must move back twice
    if (tcListi.hasPrevious()) {
      tcListi.previous();
    }
    if (tcListi.hasPrevious()) {
      tcListi.previous();
    }
    currTestCase = (TestCaseEdit)tcListi.next();
  }

  public Geometry readGeometryText(final String geomStr) throws ParseException,
    IOException {
    final MultiFormatReader reader = new MultiFormatReader(getGeometryFactory());

    Geometry g = null;
    if (geomStr.length() > 0) {
      g = reader.read(geomStr);
    }
    return g;
  }

  private void saveWKTBeforePMChange() {
    wktABeforePMChange.clear();
    wktBBeforePMChange.clear();
    for (final Iterator i = getTestCaseList().getList().iterator(); i.hasNext();) {
      final Testable testable = (Testable)i.next();
      final Geometry a = testable.getGeometry(0);
      final Geometry b = testable.getGeometry(1);
      wktABeforePMChange.add(a != null ? a.toWkt() : null);
      wktBBeforePMChange.add(b != null ? b.toWkt() : null);
    }
  }

  public void setCurrentTestCase(final TestCaseEdit testCase) {
    while (tcListi.hasPrevious()) {
      tcListi.previous();
    }
    while (tcListi.hasNext()) {
      if (tcListi.next() == testCase) {
        currTestCase = testCase;
        return;
      }
    }
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setMagnifyingTopology(final boolean show) {
    isMagnifyingTopology = show;
  }

  public void setOpName(final String opName) {
    if (opName == null) {
      this.opName = "";
    } else {
      this.opName = StringUtil.capitalize(opName);
    }
  }

  public void setResult(final Object result) {
    currResult = result;
    if (result == null || result instanceof Geometry) {
      getCurrentTestCaseEdit().setResult((Geometry)result);
    }
  }

  public void setTopologyStretchSize(final double pixels) {
    topologyStretchSize = pixels;
  }

}
