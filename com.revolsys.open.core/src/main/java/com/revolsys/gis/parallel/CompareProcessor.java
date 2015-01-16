package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.revolsys.data.filter.RecordGeometryFilter;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordLog;
import com.revolsys.data.record.RecordUtil;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.filter.AndFilter;
import com.revolsys.filter.Factory;
import com.revolsys.filter.Filter;
import com.revolsys.filter.FilterUtil;
import com.revolsys.gis.algorithm.index.PointRecordMap;
import com.revolsys.gis.algorithm.index.RecordQuadTree;
import com.revolsys.gis.algorithm.linematch.LineMatchGraph;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.jts.filter.LineEqualIgnoreDirectionFilter;
import com.revolsys.gis.jts.filter.LineIntersectsFilter;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.parallel.channel.Channel;

public class CompareProcessor extends AbstractMergeProcess {

  private final boolean cleanDuplicatePoints = true;

  private Statistics duplicateOtherStatistics = new Statistics(
      "Duplicate Other");

  private Statistics duplicateSourceStatistics = new Statistics(
      "Duplicate Source");

  private Factory<Filter<Record>, Record> equalFilterFactory;

  private Statistics equalStatistics = new Statistics("Equal");

  private Filter<Record> excludeFilter;

  private Statistics excludeNotEqualOtherStatistics = new Statistics(
      "Exclude Not Equal Other");

  private Statistics excludeNotEqualSourceStatistics = new Statistics(
      "Exclude Not Equal Source");

  private String label;

  private Statistics notEqualOtherStatistics = new Statistics("Not Equal Other");

  private Statistics notEqualSourceStatistics = new Statistics(
      "Not Equal Source");

  private RecordQuadTree otherIndex = new RecordQuadTree();

  private PointRecordMap otherPointMap = new PointRecordMap();

  private Set<Record> sourceObjects = new LinkedHashSet<Record>();

  private final PointRecordMap sourcePointMap = new PointRecordMap();

  private boolean logNotEqualSource = true;

  @Override
  protected void addOtherObject(final Record object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry instanceof Point) {
      boolean add = true;
      if (this.cleanDuplicatePoints) {
        final List<Record> objects = this.otherPointMap.getRecords(object);
        if (!objects.isEmpty()) {
          final Filter<Record> filter = this.equalFilterFactory.create(object);
          add = !FilterUtil.matches(objects, filter);
        }
        if (add) {
          this.otherPointMap.add(object);
        } else {
          this.duplicateOtherStatistics.add(object);
        }
      }
    } else if (geometry instanceof LineString) {
      this.otherIndex.insert(object);
    }
  }

  @Override
  protected void addSourceObject(final Record object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry instanceof Point) {
      boolean add = true;
      if (this.cleanDuplicatePoints) {
        final List<Record> objects = this.sourcePointMap.getRecords(object);
        if (!objects.isEmpty()) {
          final Filter<Record> filter = this.equalFilterFactory.create(object);
          add = !FilterUtil.matches(objects, filter);
        }
      }
      if (add) {
        this.sourcePointMap.add(object);
      } else {
        this.duplicateSourceStatistics.add(object);
      }
    } else if (geometry instanceof LineString) {
      this.sourceObjects.add(object);
    }
  }

  public Statistics getDuplicateOtherStatistics() {
    return this.duplicateOtherStatistics;
  }

  public Statistics getDuplicateSourceStatistics() {
    return this.duplicateSourceStatistics;
  }

  public Factory<Filter<Record>, Record> getEqualFilterFactory() {
    return this.equalFilterFactory;
  }

  public Statistics getEqualStatistics() {
    return this.equalStatistics;
  }

  public Filter<Record> getExcludeFilter() {
    return this.excludeFilter;
  }

  public Statistics getExcludeNotEqualOtherStatistics() {
    return this.excludeNotEqualOtherStatistics;
  }

  public Statistics getExcludeNotEqualSourceStatistics() {
    return this.excludeNotEqualSourceStatistics;
  }

  public String getLabel() {
    return this.label;
  }

  public Statistics getNotEqualOtherStatistics() {
    return this.notEqualOtherStatistics;
  }

  public Statistics getNotEqualSourceStatistics() {
    return this.notEqualSourceStatistics;
  }

  public boolean isLogNotEqualSource() {
    return this.logNotEqualSource;
  }

  private void logError(final Record object, final String message,
    final boolean source) {
    if (this.excludeFilter == null || !this.excludeFilter.accept(object)) {
      if (source) {
        this.notEqualSourceStatistics.add(object);
      } else {
        this.notEqualOtherStatistics.add(object);
      }
      RecordLog.error(getClass(), message, object);
    } else {
      if (source) {
        this.excludeNotEqualSourceStatistics.add(object);
      } else {
        this.excludeNotEqualOtherStatistics.add(object);
      }
    }
  }

  private void processExactLineMatch(final Record sourceObject) {
    final LineString sourceLine = sourceObject.getGeometryValue();
    final LineEqualIgnoreDirectionFilter lineEqualFilter = new LineEqualIgnoreDirectionFilter(
      sourceLine, 3);
    final Filter<Record> geometryFilter = new RecordGeometryFilter<LineString>(
        lineEqualFilter);
    final Filter<Record> equalFilter = this.equalFilterFactory.create(sourceObject);
    final Filter<Record> filter = new AndFilter<Record>(equalFilter,
        geometryFilter);

    final Record otherObject = this.otherIndex.queryFirst(sourceObject, filter);
    if (otherObject != null) {
      this.equalStatistics.add(sourceObject);
      removeObject(sourceObject);
      removeOtherObject(otherObject);
    }
  }

  private void processExactLineMatches() {
    for (final Record object : new ArrayList<Record>(this.sourceObjects)) {
      processExactLineMatch(object);
    }
  }

  private void processExactPointMatch(final Record sourceObject) {
    final Filter<Record> equalFilter = this.equalFilterFactory.create(sourceObject);
    final Record otherObject = this.otherPointMap.getFirstMatch(sourceObject,
      equalFilter);
    if (otherObject != null) {
      final Point sourcePoint = sourceObject.getGeometryValue();
      final double sourceZ = sourcePoint.getZ();

      final Point otherPoint = otherObject.getGeometryValue();
      final double otherZ = otherPoint.getZ();

      if (sourceZ == otherZ || Double.isNaN(sourceZ) && Double.isNaN(otherZ)) {
        this.equalStatistics.add(sourceObject);
        removeObject(sourceObject);
        removeOtherObject(otherObject);
      }
    }
  }

  private void processExactPointMatches() {
    for (final Record object : new ArrayList<Record>(
        this.sourcePointMap.getAll())) {
      processExactPointMatch(object);
    }
  }

  @Override
  protected void processObjects(final RecordDefinition recordDefinition,
    final Channel<Record> out) {
    if (this.otherIndex.size() + this.otherPointMap.size() == 0) {
      if (this.logNotEqualSource) {
        for (final Record object : this.sourceObjects) {
          logError(object, "Source missing in Other", true);
        }
      }
    } else {
      processExactPointMatches();
      processExactLineMatches();
      processPartialMatches();
    }
    for (final Record object : this.otherIndex.getAll()) {
      logError(object, "Other missing in Source", false);
    }
    for (final Record object : this.otherPointMap.getAll()) {
      logError(object, "Other missing in Source", false);
    }
    if (this.logNotEqualSource) {
      for (final Record object : this.sourceObjects) {
        logError(object, "Source missing in Other", true);
      }
    }
    this.sourceObjects.clear();
    this.otherIndex = new RecordQuadTree();
    this.otherPointMap.clear();
  }

  private void processPartialMatch(final Record sourceObject) {
    final Geometry sourceGeometry = sourceObject.getGeometryValue();
    if (sourceGeometry instanceof LineString) {
      final LineString sourceLine = (LineString)sourceGeometry;

      final LineIntersectsFilter intersectsFilter = new LineIntersectsFilter(
        sourceLine);
      final Filter<Record> geometryFilter = new RecordGeometryFilter<LineString>(
          intersectsFilter);
      final Filter<Record> equalFilter = this.equalFilterFactory.create(sourceObject);
      final Filter<Record> filter = new AndFilter<Record>(equalFilter,
          geometryFilter);
      final List<Record> otherObjects = this.otherIndex.queryList(
        sourceGeometry, filter);
      if (!otherObjects.isEmpty()) {
        final LineMatchGraph<Record> graph = new LineMatchGraph<Record>(
            sourceObject, sourceLine);
        for (final Record otherObject : otherObjects) {
          final LineString otherLine = otherObject.getGeometryValue();
          graph.add(otherLine);
        }
        final MultiLineString nonMatchedLines = graph.getNonMatchedLines(0);
        if (nonMatchedLines.isEmpty()) {
          removeObject(sourceObject);

        } else {
          removeObject(sourceObject);
          if (nonMatchedLines.getGeometryCount() == 1
              && nonMatchedLines.getGeometry(0).getLength() == 1) {
          } else {
            for (int j = 0; j < nonMatchedLines.getGeometryCount(); j++) {
              final Geometry newGeometry = nonMatchedLines.getGeometry(j);
              final Record newObject = RecordUtil.copy(sourceObject,
                newGeometry);
              addSourceObject(newObject);
            }
          }
        }
        for (int i = 0; i < otherObjects.size(); i++) {
          final Record otherObject = otherObjects.get(i);
          final MultiLineString otherNonMatched = graph.getNonMatchedLines(
            i + 1, 0);
          for (int j = 0; j < otherNonMatched.getGeometryCount(); j++) {
            final Geometry newGeometry = otherNonMatched.getGeometry(j);
            final Record newOtherObject = RecordUtil.copy(otherObject,
              newGeometry);
            addOtherObject(newOtherObject);
          }
          removeOtherObject(otherObject);
        }
      }
    }
  }

  private void processPartialMatches() {
    for (final Record object : new ArrayList<Record>(this.sourceObjects)) {
      processPartialMatch(object);
    }
  }

  private void removeObject(final Record object) {
    this.sourceObjects.remove(object);
  }

  private void removeOtherObject(final Record object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry instanceof Point) {
      this.otherPointMap.remove(object);
    } else {
      this.otherIndex.remove(object);
    }
  }

  public void setEqualFilterFactory(
    final Factory<Filter<Record>, Record> equalFilterFactory) {
    this.equalFilterFactory = equalFilterFactory;
  }

  public void setExcludeFilter(final Filter<Record> excludeFilter) {
    this.excludeFilter = excludeFilter;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public void setLogNotEqualSource(final boolean logNotEqualSource) {
    this.logNotEqualSource = logNotEqualSource;
  }

  @Override
  protected void setUp() {
    this.equalStatistics.connect();
    this.notEqualSourceStatistics.connect();
    this.notEqualOtherStatistics.connect();
    this.duplicateSourceStatistics.connect();
    this.duplicateOtherStatistics.connect();
    this.excludeNotEqualSourceStatistics.connect();
    this.excludeNotEqualOtherStatistics.connect();
  }

  @Override
  protected void tearDown() {
    this.sourceObjects = null;
    this.sourcePointMap.clear();
    this.otherPointMap = null;
    this.otherIndex = null;
    this.equalStatistics.disconnect();
    this.notEqualSourceStatistics.disconnect();
    this.notEqualOtherStatistics.disconnect();
    this.duplicateSourceStatistics.disconnect();
    this.duplicateOtherStatistics.disconnect();
    this.excludeNotEqualSourceStatistics.disconnect();
    this.excludeNotEqualOtherStatistics.disconnect();
    this.equalStatistics = null;
    this.notEqualSourceStatistics = null;
    this.notEqualOtherStatistics = null;
    this.duplicateSourceStatistics = null;
    this.duplicateOtherStatistics = null;
    this.excludeNotEqualSourceStatistics = null;
    this.excludeNotEqualOtherStatistics = null;
  }
}
