package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.jts.CoordinateDistanceComparator;
import com.revolsys.gis.jts.CoordinateSequenceUtil;
import com.revolsys.gis.jts.LineSegment3D;
import com.revolsys.gis.jts.LineSegmentIndex;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;

public class SplitGeometryProcess extends BaseInOutProcess<DataObject> {
  /** The statistics to record the number new observations created. */
  private Statistics createdStatistics;

  private PrecisionModel elevationPrecisionModel;

  private Geometry geometry;

  private LineSegmentIndex index = new LineSegmentIndex();

  /** The statistics to record the number of observations ignored. */
  private Statistics notWrittenStatistics;

  private double tolerance = 2.0;

  private GeometryFactory geometryFactory;

  private void createLineString(final LineString line,
    final CoordinateSequence coordinates, final Coordinate startCoordinate,
    final int startIndex, final int endIndex, final Coordinate endCoordinate,
    final List<LineString> lines) {
    final CoordinateSequence newCoordinates = CoordinateSequenceUtil.subSequence(
      coordinates, startCoordinate, startIndex, endIndex - startIndex + 1,
      endCoordinate);
    if (newCoordinates.size() > 1) {
      final LineString newLine = geometryFactory.createLineString(newCoordinates);
      lines.add(newLine);
    }
  }

  protected DataObject createSplitObject(final DataObject object,
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
  protected void postRun(final Channel<DataObject> in,
    final Channel<DataObject> out) {
    if (createdStatistics != null) {
      createdStatistics.disconnect();
    }
    if (notWrittenStatistics != null) {
      notWrittenStatistics.disconnect();
    }
  }

  @Override
  protected void preRun(final Channel<DataObject> in,
    final Channel<DataObject> out) {
    if (createdStatistics != null) {
      createdStatistics.connect();
    }
    if (notWrittenStatistics != null) {
      notWrittenStatistics.connect();
    }
  }

  @Override
  protected void process(final Channel<DataObject> in,
    final Channel<DataObject> out, final DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      if (line.isWithinDistance(this.geometry, 0)) {
        split(out, object, line);
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

  private void split(final Channel<DataObject> out, final DataObject object,
    final LineString line) {
    final PrecisionModel precisionModel = geometryFactory.getPrecisionModel();
    final CoordinatesList coordinates = CoordinatesListUtil.get(line);
    final Coordinate firstCoordinate = coordinates.getCoordinate(0);
    final int lastIndex = coordinates.size() - 1;
    final Coordinate lastCoordinate = coordinates.getCoordinate(lastIndex);
    int startIndex = 0;
    final List<LineString> newLines = new ArrayList<LineString>();
    Coordinate startCoordinate = null;
    Coordinate c0 = coordinates.getCoordinate(0);
    for (int i = 1; i < coordinates.size(); i++) {
      final Coordinate c1 = coordinates.getCoordinate(i);

      final List<Coordinate> intersections = index.queryIntersections(c0, c1);
      if (!intersections.isEmpty()) {
        if (intersections.size() > 1) {
          Collections.sort(intersections, new CoordinateDistanceComparator(c0));
        }
        int j = 0;
        for (final Coordinate intersection : intersections) {
          if (!isWithinDistanceOfBoundary(c0)
            && !isWithinDistanceOfBoundary(c1)) {
            if (i == 1 && intersection.distance(firstCoordinate) < tolerance) {
            } else if (i == lastIndex
              && intersection.distance(lastCoordinate) < tolerance) {
            } else {
              final double d0 = intersection.distance(c0);
              final double d1 = intersection.distance(c1);
              if (d0 <= tolerance) {
                if (d1 > tolerance) {
                  createLineString(line, coordinates, startCoordinate,
                    startIndex, i - 1, null, newLines);
                  startIndex = i - 1;
                  startCoordinate = null;
                } else {
                  precisionModel.makePrecise(intersection);
                  if (elevationPrecisionModel != null) {
                    intersection.z = elevationPrecisionModel.makePrecise(intersection.z);
                  }
                  createLineString(line, coordinates, startCoordinate,
                    startIndex, i - 1, intersection, newLines);
                  startIndex = i + 1;
                  startCoordinate = intersection;
                  c0 = intersection;
                }
              } else if (d1 <= tolerance) {
                createLineString(line, coordinates, startCoordinate,
                  startIndex, i, null, newLines);
                startIndex = i;
                startCoordinate = null;
              } else {
                precisionModel.makePrecise(intersection);
                if (elevationPrecisionModel != null) {
                  intersection.z = elevationPrecisionModel.makePrecise(intersection.z);
                }
                createLineString(line, coordinates, startCoordinate,
                  startIndex, i - 1, intersection, newLines);
                startIndex = i;
                startCoordinate = intersection;
                c0 = intersection;
              }
            }
          }
          j++;
        }
      }
      c0 = c1;
    }
    if (newLines.isEmpty()) {
      out.write(object);
    } else {
      createLineString(line, coordinates, startCoordinate, startIndex,
        lastIndex, null, newLines);
      for (final LineString newLine : newLines) {
        if (newLine.getLength() > 0) {
          final DataObject newObject = createSplitObject(object, newLine);
          out.write(newObject);
        }
      }
      if (notWrittenStatistics != null) {
        notWrittenStatistics.add(object);
      }
      if (createdStatistics != null) {
        createdStatistics.add(object, newLines.size());
      }
    }
  }

  private boolean isWithinDistanceOfBoundary(Coordinate nextCoordinate) {
    Envelope envelope = new Envelope(nextCoordinate);
    envelope.expandBy(1);
    List<LineSegment3D> lines = index.query(envelope);
    for (LineSegment3D line : lines) {
      if (line.distance(nextCoordinate) <= 1) {
        return true;
      }
    }

    return false;
  }
}
