package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.Records;
import com.revolsys.gis.algorithm.index.LineSegmentIndex;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class SplitGeometryProcess extends
BaseInOutProcess<Record, Record> {
  /** The statistics to record the number new observations created. */
  private Statistics createdStatistics;

  private Geometry geometry;

  private LineSegmentIndex index = new LineSegmentIndex();

  /** The statistics to record the number of observations ignored. */
  private Statistics notWrittenStatistics;

  private double tolerance = 2.0;

  private GeometryFactory geometryFactory;

  protected Record createSplitObject(final Record object,
    final LineString newLine) {
    return Records.copy(object, newLine);
  }

  /**
   * Get the statistics to record the number new observations created.
   *
   * @return The statistics to record the number new observations created.
   */
  public Statistics getCreatedStatistics() {
    if (this.createdStatistics == null) {
      this.createdStatistics = new Statistics("Created");
    }
    return this.createdStatistics;
  }

  public Geometry getGeometry() {
    return this.geometry;
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public Statistics getNotWrittenStatistics() {
    if (this.notWrittenStatistics == null) {
      this.notWrittenStatistics = new Statistics("Discarded");
    }
    return this.notWrittenStatistics;
  }

  public double getTolerance() {
    return this.tolerance;
  }

  @Override
  protected void postRun(final Channel<Record> in,
    final Channel<Record> out) {
    if (this.createdStatistics != null) {
      this.createdStatistics.disconnect();
    }
    if (this.notWrittenStatistics != null) {
      this.notWrittenStatistics.disconnect();
    }
  }

  @Override
  protected void preRun(final Channel<Record> in,
    final Channel<Record> out) {
    if (this.createdStatistics != null) {
      this.createdStatistics.connect();
    }
    if (this.notWrittenStatistics != null) {
      this.notWrittenStatistics.connect();
    }
  }

  @Override
  protected void process(final Channel<Record> in,
    final Channel<Record> out, final Record object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      if (line.isWithinDistance(this.geometry, 0)) {
        final List<Record> newObjects = split(object, line);
        for (final Record newObject : newObjects) {
          out.write(newObject);
        }
        if (newObjects.size() > 1) {
          if (this.notWrittenStatistics != null) {
            this.notWrittenStatistics.add(object);
          }
          if (this.createdStatistics != null) {
            this.createdStatistics.add(object, newObjects.size());
          }
        }

      } else {
        out.write(object);
      }
    } else {
      out.write(object);
    }
  }

  /**
   * Set the statistics to record the number new observations created.
   *
   * @param createdStatistics The statistics to record the number new
   *          observations created.
   */
  public void setCreatedStatistics(final Statistics createdStatistics) {
    this.createdStatistics = createdStatistics;
  }

  public void setGeometry(final Geometry geometry) {
    this.geometry = geometry;
    this.index = new LineSegmentIndex();
    this.index.insert(geometry);
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setNotWrittenStatistics(final Statistics notWrittenStatistics) {
    this.notWrittenStatistics = notWrittenStatistics;
  }

  public void setTolerance(final double tolerance) {
    this.tolerance = tolerance;
  }

  protected List<Record> split(final Record object,
    final LineString line) {
    final List<Record> newObjects = new ArrayList<Record>();
    final List<LineString> newLines = LineStringUtil.split(this.geometryFactory,
      line, this.index, this.tolerance);
    if (newLines.size() == 1) {
      final LineString newLine = newLines.get(0);
      if (newLine == line) {
        newObjects.add(object);
      } else {
        final Record newObject = createSplitObject(object, newLine);
        newObjects.add(newObject);
      }
    } else {
      for (final LineString newLine : newLines) {
        final Record newObject = createSplitObject(object, newLine);
        newObjects.add(newObject);
      }
    }
    return newObjects;
  }

}
