package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.algorithm.index.LineSegmentIndex;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;

public class SplitGeometryProcess extends
  BaseInOutProcess<DataObject, DataObject> {
  /** The statistics to record the number new observations created. */
  private Statistics createdStatistics;

  private PrecisionModel elevationPrecisionModel;

  private Geometry geometry;

  private LineSegmentIndex index = new LineSegmentIndex();

  /** The statistics to record the number of observations ignored. */
  private Statistics notWrittenStatistics;

  private double tolerance = 2.0;

  private GeometryFactory geometryFactory;

  protected DataObject createSplitObject(
    final DataObject object,
    final LineString newLine) {
    return DataObjectUtil.copy(object, newLine);
  }

  /**
   * Get the statistics to record the number new observations created.
   * 
   * @return The statistics to record the number new observations created.
   */
  public Statistics getCreatedStatistics() {
    if (createdStatistics == null) {
      createdStatistics = new Statistics("Created");
    }
    return createdStatistics;
  }

  public PrecisionModel getElevationPrecisionModel() {
    return elevationPrecisionModel;
  }

  public Geometry getGeometry() {
    return geometry;
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public Statistics getNotWrittenStatistics() {
    if (notWrittenStatistics == null) {
      notWrittenStatistics = new Statistics("Discarded");
    }
    return notWrittenStatistics;
  }

  public double getTolerance() {
    return tolerance;
  }

  @Override
  protected void postRun(
    final Channel<DataObject> in,
    final Channel<DataObject> out) {
    if (createdStatistics != null) {
      createdStatistics.disconnect();
    }
    if (notWrittenStatistics != null) {
      notWrittenStatistics.disconnect();
    }
  }

  @Override
  protected void preRun(
    final Channel<DataObject> in,
    final Channel<DataObject> out) {
    if (createdStatistics != null) {
      createdStatistics.connect();
    }
    if (notWrittenStatistics != null) {
      notWrittenStatistics.connect();
    }
  }

  @Override
  protected void process(
    final Channel<DataObject> in,
    final Channel<DataObject> out,
    final DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      if (line.isWithinDistance(this.geometry, 0)) {
        final List<DataObject> newObjects = split(object, line);
        for (final DataObject newObject : newObjects) {
          out.write(newObject);
        }
        if (newObjects.size() > 1) {
          if (notWrittenStatistics != null) {
            notWrittenStatistics.add(object);
          }
          if (createdStatistics != null) {
            createdStatistics.add(object, newObjects.size());
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

  public void setElevationPrecisionModel(
    final PrecisionModel elevationPrecisionModel) {
    this.elevationPrecisionModel = elevationPrecisionModel;
  }

  public void setGeometry(final Geometry geometry) {
    this.geometry = geometry;
    index = new LineSegmentIndex();
    index.insert(geometry);
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

  protected List<DataObject> split(
    final DataObject object,
    final LineString line) {
    final List<DataObject> newObjects = new ArrayList<DataObject>();
    final List<LineString> newLines = LineStringUtil.split(geometryFactory,
      line, index, tolerance);
    if (newLines.size() == 1) {
      final LineString newLine = newLines.get(0);
      if (newLine == line) {
        newObjects.add(object);
      } else {
        final DataObject newObject = createSplitObject(object, newLine);
        newObjects.add(newObject);
      }
    } else {
      for (final LineString newLine : newLines) {
        final DataObject newObject = createSplitObject(object, newLine);
        newObjects.add(newObject);
      }
    }
    return newObjects;
  }

}
