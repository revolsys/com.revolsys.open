package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.index.LineSegmentIndex;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.util.LineStringUtil;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.revolsys.record.Record;
import com.revolsys.record.Records;
import com.revolsys.util.count.LabelCountMap;

public class SplitGeometryProcess extends BaseInOutProcess<Record, Record> {
  /** The statistics to record the number new observations created. */
  private LabelCountMap createdStatistics;

  private Geometry geometry;

  private GeometryFactory geometryFactory;

  private LineSegmentIndex index = new LineSegmentIndex();

  /** The statistics to record the number of observations ignored. */
  private LabelCountMap notWrittenStatistics;

  private double tolerance = 2.0;

  /**
   * Get the statistics to record the number new observations created.
   *
   * @return The statistics to record the number new observations created.
   */
  public LabelCountMap getCreatedStatistics() {
    if (this.createdStatistics == null) {
      this.createdStatistics = new LabelCountMap("Created");
    }
    return this.createdStatistics;
  }

  public Geometry getGeometry() {
    return this.geometry;
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public LabelCountMap getNotWrittenStatistics() {
    if (this.notWrittenStatistics == null) {
      this.notWrittenStatistics = new LabelCountMap("Discarded");
    }
    return this.notWrittenStatistics;
  }

  public double getTolerance() {
    return this.tolerance;
  }

  protected Record newSplitRecord(final Record object, final LineString newLine) {
    return Records.copy(object, newLine);
  }

  @Override
  protected void postRun(final Channel<Record> in, final Channel<Record> out) {
    if (this.createdStatistics != null) {
      this.createdStatistics.disconnect();
    }
    if (this.notWrittenStatistics != null) {
      this.notWrittenStatistics.disconnect();
    }
  }

  @Override
  protected void preRun(final Channel<Record> in, final Channel<Record> out) {
    if (this.createdStatistics != null) {
      this.createdStatistics.connect();
    }
    if (this.notWrittenStatistics != null) {
      this.notWrittenStatistics.connect();
    }
  }

  @Override
  protected void process(final Channel<Record> in, final Channel<Record> out, final Record object) {
    final Geometry geometry = object.getGeometry();
    if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      if (line.isWithinDistance(this.geometry, 0)) {
        final List<Record> newObjects = split(object, line);
        for (final Record newObject : newObjects) {
          out.write(newObject);
        }
        if (newObjects.size() > 1) {
          if (this.notWrittenStatistics != null) {
            this.notWrittenStatistics.addCount(object);
          }
          if (this.createdStatistics != null) {
            this.createdStatistics.addCount(object, newObjects.size());
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
  public void setCreatedStatistics(final LabelCountMap createdStatistics) {
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

  public void setNotWrittenStatistics(final LabelCountMap notWrittenStatistics) {
    this.notWrittenStatistics = notWrittenStatistics;
  }

  public void setTolerance(final double tolerance) {
    this.tolerance = tolerance;
  }

  protected List<Record> split(final Record object, final LineString line) {
    final List<Record> newObjects = new ArrayList<>();
    final List<LineString> newLines = LineStringUtil.split(this.geometryFactory, line, this.index,
      this.tolerance);
    if (newLines.size() == 1) {
      final LineString newLine = newLines.get(0);
      if (newLine == line) {
        newObjects.add(object);
      } else {
        final Record newObject = newSplitRecord(object, newLine);
        newObjects.add(newObject);
      }
    } else {
      for (final LineString newLine : newLines) {
        final Record newObject = newSplitRecord(object, newLine);
        newObjects.add(newObject);
      }
    }
    return newObjects;
  }

}
