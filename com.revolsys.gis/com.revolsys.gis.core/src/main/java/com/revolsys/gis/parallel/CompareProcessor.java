package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import com.revolsys.filter.AndFilter;
import com.revolsys.filter.Factory;
import com.revolsys.filter.Filter;
import com.revolsys.filter.FilterUtil;
import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.algorithm.index.PointDataObjectMap;
import com.revolsys.gis.algorithm.linematch.LineMatchGraph;
import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.data.model.filter.GeometryFilter;
import com.revolsys.gis.data.model.filter.LineIntersectsFilter;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.jts.filter.LineEqualFilter;
import com.revolsys.gis.jts.filter.LineEqualIgnoreDirectionFilter;
import com.revolsys.gis.util.NoOp;
import com.revolsys.parallel.channel.Channel;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

public class CompareProcessor extends AbstractMergeProcess {

  private static final DataObjectMetaData POINT_LOG_METADATA;

  private static final DataObjectMetaData LINE_LOG_METADATA;
  static {
    DataObjectMetaDataImpl pointLogMetaData = new DataObjectMetaDataImpl(
      new QName("CompareLogPoint"));
    pointLogMetaData.addAttribute(new Attribute("type", DataTypes.STRING, 50,
      true));
    pointLogMetaData.addAttribute(new Attribute("message", DataTypes.STRING,
      254, true));
    pointLogMetaData.addAttribute(new Attribute("geometry",
      DataTypes.MULTI_POINT, true));
    POINT_LOG_METADATA = pointLogMetaData;

    DataObjectMetaDataImpl lineLogMetaData = new DataObjectMetaDataImpl(
      new QName("CompareLogLine"));
    lineLogMetaData.addAttribute(new Attribute("type", DataTypes.STRING, 50,
      true));
    lineLogMetaData.addAttribute(new Attribute("message", DataTypes.STRING,
      254, true));
    lineLogMetaData.addAttribute(new Attribute("geometry",
      DataTypes.LINESTRING, true));
    LINE_LOG_METADATA = lineLogMetaData;
  }

  private Factory<Filter<DataObject>, DataObject> equalFilterFactory;

  private Filter<DataObject> excludeFilter;

  public Filter<DataObject> getExcludeFilter() {
    return excludeFilter;
  }

  public void setExcludeFilter(
    Filter<DataObject> excludeFilter) {
    this.excludeFilter = excludeFilter;
  }

  private Statistics equalStatistics = new Statistics("Equal");

  private Statistics duplicateSourceStatistics = new Statistics(
    "Duplicate Source");

  private Statistics duplicateOtherStatistics = new Statistics(
    "Duplicate Other");

  private String label;

  private Channel<DataObject> logOut;

  private Statistics notEqualStatistics = new Statistics("Not Equal");

  private DataObjectQuadTree otherIndex = new DataObjectQuadTree();

  private PointDataObjectMap otherPointMap = new PointDataObjectMap();

  private PointDataObjectMap sourcePointMap = new PointDataObjectMap();

  private Set<DataObject> sourceObjects = new LinkedHashSet<DataObject>();

  private boolean cleanDuplicatePoints = true;

  @Override
  protected void addOtherObject(
    final DataObject object) {
    if (excludeFilter != null && !excludeFilter.accept(object)) {
      final Geometry geometry = object.getGeometryValue();
      if (geometry instanceof Point) {
        boolean add = true;
        if (cleanDuplicatePoints) {
          final List<DataObject> objects = otherPointMap.getObjects(object);
          if (!objects.isEmpty()) {
            final Filter<DataObject> filter = equalFilterFactory.create(object);
            add = !FilterUtil.matches(objects, filter);
          }
          if (add) {
            otherPointMap.add(object);
          } else {
            duplicateOtherStatistics.add(object);
          }
        }
      } else if (geometry instanceof LineString) {
        otherIndex.insert(object);
      }
    }
  }

  @Override
  protected void addSourceObject(
    final DataObject object) {
    if (excludeFilter != null && !excludeFilter.accept(object)) {
      final Geometry geometry = object.getGeometryValue();
      if (geometry instanceof Point) {
        boolean add = true;
        if (cleanDuplicatePoints) {
          final List<DataObject> objects = sourcePointMap.getObjects(object);
          if (!objects.isEmpty()) {
            final Filter<DataObject> filter = equalFilterFactory.create(object);
            add = !FilterUtil.matches(objects, filter);
          }
        }
        if (add) {
          sourcePointMap.add(object);
        } else {
          duplicateSourceStatistics.add(object);
        }
      } else if (geometry instanceof LineString) {
        sourceObjects.add(object);
      }
    }
  }

  public Factory<Filter<DataObject>, DataObject> getEqualFilterFactory() {
    return equalFilterFactory;
  }

  public Statistics getEqualStatistics() {
    return equalStatistics;
  }

  public String getLabel() {
    return label;
  }

  /**
   * @return the logOut
   */
  public Channel<DataObject> getLogOut() {
    return logOut;
  }

  public Statistics getNotEqualStatistics() {
    return notEqualStatistics;
  }

  private void logError(
    final DataObject object,
    final String message) {
    notEqualStatistics.add(object);
    Geometry geometry = object.getGeometryValue();
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      Geometry subGeometry = geometry.getGeometryN(i);
      if (subGeometry == null) {
        System.err.println("Null geometry");
      } else {
        DataObject logObject;
        if (subGeometry instanceof LineString) {
          logObject = new ArrayDataObject(LINE_LOG_METADATA);
        } else if (subGeometry instanceof Point) {
          logObject = new ArrayDataObject(POINT_LOG_METADATA);
        } else if (subGeometry instanceof MultiPoint) {
          logObject = new ArrayDataObject(POINT_LOG_METADATA);
        } else {
          logObject = null;
        }
        logObject.setValue("type", object.getMetaData().getName());
        logObject.setValue("message", message);
        logObject.setGeometryValue(subGeometry);
        logOut.write(logObject);
      }
    }

  }

  private void processExactPointMatch(
    final DataObject sourceObject) {
    final Filter<DataObject> equalFilter = equalFilterFactory.create(sourceObject);
    final DataObject otherObject = otherPointMap.getFirstMatch(sourceObject,
      equalFilter);
    if (otherObject != null) {
      equalStatistics.add(sourceObject);
      removeObject(sourceObject);
      removeOtherObject(otherObject);
    }
  }

  private void processExactPointMatches() {
    for (final DataObject object : new ArrayList<DataObject>(
      sourcePointMap.getAll())) {
      processExactPointMatch(object);
    }
  }

  private void processExactLineMatch(
    final DataObject sourceObject) {
    LineString sourceLine = sourceObject.getGeometryValue();
    final LineEqualIgnoreDirectionFilter lineEqualFilter = new LineEqualIgnoreDirectionFilter(sourceLine,3);
    final Filter<DataObject> geometryFilter = new GeometryFilter<LineString>(
      lineEqualFilter);
    final Filter<DataObject> equalFilter = equalFilterFactory.create(sourceObject);
    final Filter<DataObject> filter = new AndFilter<DataObject>(equalFilter,
      geometryFilter);

    final DataObject otherObject = otherIndex.queryFirst(sourceObject, filter);
    if (otherObject != null) {
      equalStatistics.add(sourceObject);
      removeObject(sourceObject);
      removeOtherObject(otherObject);
    }
  }

  private void processExactLineMatches() {
    for (final DataObject object : new ArrayList<DataObject>(sourceObjects)) {
      processExactLineMatch(object);
    }
  }

  @Override
  protected void processObjects(
    final DataObjectMetaData metaData,
    final Channel<DataObject> out) {
    System.out.println(metaData.getName());
    if (otherIndex.size() + otherPointMap.size() == 0) {
      for (final DataObject object : sourceObjects) {
        logError(object, "Source missing in Other");
      }
    } else {
      processExactPointMatches();
      processExactLineMatches();
      processPartialMatches();
    }
    for (final DataObject object : otherIndex.queryAll()) {
      logError(object, "Other missing in Source");
    }
    for (final DataObject object : otherPointMap.getAll()) {
      logError(object, "Other missing in Source");
    }
    for (final DataObject object : sourceObjects) {
      logError(object, "Source missing in Other");
    }
    sourceObjects.clear();
    otherIndex = new DataObjectQuadTree();
    otherPointMap.clear();
  }

  private void processPartialMatch(
    final DataObject sourceObject) {
    final Geometry sourceGeometry = sourceObject.getGeometryValue();
    if (sourceGeometry instanceof LineString) {
      final LineString sourceLine = (LineString)sourceGeometry;

      final LineIntersectsFilter intersectsFilter = new LineIntersectsFilter(
        sourceLine);
      final Filter<DataObject> geometryFilter = new GeometryFilter<LineString>(
        intersectsFilter);
      final Filter<DataObject> equalFilter = equalFilterFactory.create(sourceObject);
      final Filter<DataObject> filter = new AndFilter<DataObject>(equalFilter,
        geometryFilter);
      final List<DataObject> otherObjects = otherIndex.queryList(
        sourceGeometry, filter);
      if (!otherObjects.isEmpty()) {
        final LineMatchGraph<DataObject> graph = new LineMatchGraph<DataObject>(
          sourceObject, sourceLine);
        for (final DataObject otherObject : otherObjects) {
          final LineString otherLine = otherObject.getGeometryValue();
          graph.add(otherLine);
        }
        final MultiLineString nonMatchedLines = graph.getNonMatchedLines(0);
        if (nonMatchedLines.isEmpty()) {
          removeObject(sourceObject);

        } else {
          removeObject(sourceObject);
          if (nonMatchedLines.getNumGeometries() == 1
            && nonMatchedLines.getGeometryN(0).getLength() == 1) {
          } else {
            for (int j = 0; j < nonMatchedLines.getNumGeometries(); j++) {
              final Geometry newGeometry = nonMatchedLines.getGeometryN(j);
              final DataObject newObject = DataObjectUtil.copy(sourceObject,
                newGeometry);
              addSourceObject(newObject);
            }
          }
        }
        for (int i = 0; i < otherObjects.size(); i++) {
          final DataObject otherObject = otherObjects.get(i);
          final MultiLineString otherNonMatched = graph.getNonMatchedLines(
            i + 1, 0);
          for (int j = 0; j < otherNonMatched.getNumGeometries(); j++) {
            final Geometry newGeometry = otherNonMatched.getGeometryN(j);
            final DataObject newOtherObject = DataObjectUtil.copy(otherObject,
              newGeometry);
            addOtherObject(newOtherObject);
          }
          removeOtherObject(otherObject);
        }
      }
    }
  }

  private void processPartialMatches() {
    for (final DataObject object : new ArrayList<DataObject>(sourceObjects)) {
      processPartialMatch(object);
    }
  }

  private void removeObject(
    final DataObject object) {
    sourceObjects.remove(object);
  }

  private void removeOtherObject(
    final DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry instanceof Point) {
      otherPointMap.remove(object);
    } else {
      int size = otherIndex.size();
      otherIndex.remove(object);
      NoOp.equals(size, otherIndex.size());
    }
  }

  public void setEqualFilterFactory(
    final Factory<Filter<DataObject>, DataObject> equalFilterFactory) {
    this.equalFilterFactory = equalFilterFactory;
  }

  public void setEqualStatistics(
    final Statistics equalStatistics) {
    this.equalStatistics = equalStatistics;
  }

  public void setLabel(
    final String label) {
    this.label = label;
  }

  /**
   * @param logOut the logOut to set
   */
  public void setLogOut(
    final Channel<DataObject> logOut) {
    this.logOut = logOut;
    logOut.writeConnect();
  }

  public void setNotEqualStatistics(
    final Statistics notEqualStatistics) {
    this.notEqualStatistics = notEqualStatistics;
  }

  @Override
  protected void setUp() {
    if (equalStatistics != null) {
      equalStatistics.connect();
    }
    if (notEqualStatistics != null) {
      notEqualStatistics.connect();
    }
    duplicateSourceStatistics.connect();
    duplicateOtherStatistics.connect();
  }

  @Override
  protected void tearDown() {
    logOut.writeDisconnect();
    sourceObjects = null;
    sourcePointMap.clear();
    otherPointMap = null;
    otherIndex = null;
    equalStatistics.disconnect();
    notEqualStatistics.disconnect();
    duplicateSourceStatistics.disconnect();
    duplicateOtherStatistics.disconnect();
  }
}
