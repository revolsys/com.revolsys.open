package com.revolsys.gis.parallel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectLog;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.data.equals.EqualsInstance;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.MultiInputSelector;
import com.revolsys.parallel.channel.store.Buffer;
import com.revolsys.parallel.process.AbstractInProcess;
import com.revolsys.util.CollectionUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.PrecisionModel;

public class OrderedEqualCompareProcessor extends AbstractInProcess<DataObject> {

  private Channel<DataObject> otherIn;

  private int otherInBufferSize = 0;

  private String attributeName;

  private boolean running;

  private String sourceName = "Source";

  private String otherName = "Other";

  private List<String> equalExclude = new ArrayList<String>();

  private final PrecisionModel precisionModel = new PrecisionModel(1000);

  private boolean equals(final Geometry geometry1, final Geometry geometry2) {
    if (geometry1 == null) {
      return geometry2 == null;
    } else if (geometry2 == null) {
      return false;
    } else if (geometry1.getClass() == geometry2.getClass()) {
      if (geometry1 instanceof GeometryCollection) {
        if (geometry1.getNumGeometries() == geometry2.getNumGeometries()) {
          for (int i = 0; i < geometry1.getNumGeometries(); i++) {
            final Geometry subGeometry1 = geometry1.getGeometryN(i);
            final Geometry subGeometry2 = geometry2.getGeometryN(i);
            if (!equals(subGeometry1, subGeometry2)) {
              return false;
            }
          }
          return true;
        } else {
          return false;
        }
      } else {
        final List<CoordinatesList> parts1 = CoordinatesListUtil.getAll(geometry1);
        final List<CoordinatesList> parts2 = CoordinatesListUtil.getAll(geometry2);
        if (parts1.size() == parts2.size()) {
          for (int i = 0; i < parts1.size(); i++) {
            final CoordinatesList points1 = parts1.get(i);
            final CoordinatesList points2 = parts2.get(i);
            if (points1.size() == points2.size()
              && points1.getNumAxis() == points2.getNumAxis()) {
              for (int j = 0; j < points1.size(); j++) {
                for (int k = 0; k < points1.getNumAxis(); k++) {
                  double value1 = points1.getValue(j, k);
                  double value2 = points2.getValue(j, k);
                  value1 = precisionModel.makePrecise(value1);
                  value2 = precisionModel.makePrecise(value2);
                  if (Double.compare(value1, value2) != 0) {
                    return false;
                  }
                }
              }
            } else {
              return false;
            }
          }
          return true;
        } else {
          return false;
        }

      }
    } else {
      return false;
    }
  }

  protected boolean geometryEquals(final DataObject object1,
    final DataObject object2) {
    final Geometry geometry1 = object1.getGeometryValue();
    final Geometry geometry2 = object2.getGeometryValue();

    return equals(geometry1, geometry2);
  }

  public String getAttributeName() {
    return attributeName;
  }

  public List<String> getEqualExclude() {
    return equalExclude;
  }

  protected Set<String> getNotEqualAttributeNames(final DataObject object1,
    final DataObject object2) {
    final DataObjectMetaData metaData = object1.getMetaData();
    final Set<String> notEqualAttributeNames = new LinkedHashSet<String>();
    final String geometryAttributeName = metaData.getGeometryAttributeName();
    for (final String attributeName : metaData.getAttributeNames()) {
      if (!equalExclude.contains(attributeName)
        && !attributeName.equals(geometryAttributeName)) {
        final Object value1 = object1.getValue(attributeName);
        final Object value2 = object2.getValue(attributeName);
        if (!valueEquals(value1, value2)) {
          notEqualAttributeNames.add(attributeName);
        }
      }
    }
    return notEqualAttributeNames;
  }

  protected boolean valueEquals(final Object value1, final Object value2) {
    return EqualsInstance.INSTANCE.equals(value1, value2);
  }

  /**
   * @return the in
   */
  public Channel<DataObject> getOtherIn() {
    if (otherIn == null) {
      if (otherInBufferSize < 1) {
        setOtherIn(new Channel<DataObject>());
      } else {
        final Buffer<DataObject> buffer = new Buffer<DataObject>(
          otherInBufferSize);
        setOtherIn(new Channel<DataObject>(buffer));
      }
    }
    return otherIn;
  }

  public int getOtherInBufferSize() {
    return otherInBufferSize;
  }

  public String getOtherName() {
    return otherName;
  }

  public String getSourceName() {
    return sourceName;
  }

  protected void logNoMatch(final DataObject object, final boolean other) {
    if (other) {
      DataObjectLog.error(getClass(), otherName + " has no match in "
        + sourceName, object);
    } else {
      DataObjectLog.error(getClass(), sourceName + " has no match in "
        + otherName, object);
    }
  }

  private void logNoMatch(final DataObject[] objects,
    final Channel<DataObject> channel, final boolean other) {
    if (objects[0] != null) {
      logNoMatch(objects[0], false);
    }
    if (objects[1] != null) {
      logNoMatch(objects[1], true);
    }
    while (running) {
      final DataObject object = readObject(channel);
      logNoMatch(object, other);
    }
  }

  protected void logNotEqual(final DataObject sourceObject,
    final DataObject otherObject, final Set<String> notEqualAttributeNames,
    final boolean geometryEquals) {
    final String attributeNames = CollectionUtil.toString(",",
      notEqualAttributeNames);
    DataObjectLog.error(getClass(), sourceName + " " + attributeNames,
      sourceObject);
    DataObjectLog.error(getClass(), otherName + " " + attributeNames,
      otherObject);
  }

  protected DataObject readObject(final Channel<DataObject> channel) {
    return channel.read();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void run(final Channel<DataObject> in) {
    running = true;
    final Channel<DataObject>[] channels = new Channel[] {
      in, otherIn
    };
    DataObject previousEqualObject = null;

    final DataObject[] objects = new DataObject[2];
    final boolean[] guard = new boolean[] {
      true, true
    };
    final MultiInputSelector alt = new MultiInputSelector();
    while (running) {
      final int index = alt.select(channels, guard);
      if (index == -1) {
        if (in.isClosed()) {
          logNoMatch(objects, otherIn, true);
          return;
        } else if (otherIn.isClosed()) {
          logNoMatch(objects, in, false);
          return;
        } else {
        }
      } else {
        final Channel<DataObject> channel = channels[index];
        final DataObject readObject = readObject(channel);
        if (readObject != null) {
          if (previousEqualObject != null
            && EqualsInstance.INSTANCE.equals(previousEqualObject, readObject)) {
            if (index == 0) {
              DataObjectLog.error(getClass(), "Duplicate in " + sourceName,
                readObject);
            } else {
              DataObjectLog.error(getClass(), "Duplicate in " + otherName,
                readObject);
            }
          } else {
            DataObject sourceObject;
            DataObject otherObject;
            final int oppositeIndex = (index + 1) % 2;
            if (index == 0) {
              sourceObject = readObject;
              otherObject = objects[oppositeIndex];
            } else {
              sourceObject = objects[oppositeIndex];
              otherObject = readObject;
            }
            final Object value = readObject.getValue(attributeName);
            if (value == null) {
              DataObjectLog.error(getClass(), "Missing key value for "
                + attributeName, readObject);
            } else if (objects[oppositeIndex] == null) {
              objects[index] = readObject;
              guard[index] = false;
              guard[oppositeIndex] = true;
            } else {
              final Comparable<Object> sourceComparator = sourceObject.getValue(attributeName);
              final Object otherValue = otherObject.getValue(attributeName);
              // TODO duplicates
              final int compare = sourceComparator.compareTo(otherValue);
              if (compare == 0) {
                final Set<String> notEqualAttributeNames = getNotEqualAttributeNames(
                  sourceObject, otherObject);

                final boolean geometryEquals = geometryEquals(sourceObject,
                  otherObject);
                if (!geometryEquals) {
                  final String geometryAttributeName = sourceObject.getMetaData()
                    .getGeometryAttributeName();
                  notEqualAttributeNames.add(geometryAttributeName);
                }
                if (!notEqualAttributeNames.isEmpty()) {
                  logNotEqual(sourceObject, otherObject,
                    notEqualAttributeNames, geometryEquals);
                }
                objects[0] = null;
                objects[1] = null;
                guard[0] = true;
                guard[1] = true;
                previousEqualObject = sourceObject;
              } else if (compare < 0) { // other object is bigger, keep other
                                        // object
                logNoMatch(sourceObject, false);
                objects[0] = null;
                objects[1] = otherObject;
                guard[0] = true;
                guard[1] = false;

              } else { // source is bigger, keep source object
                logNoMatch(otherObject, true);
                objects[0] = sourceObject;
                objects[1] = null;
                guard[0] = false;
                guard[1] = true;
              }
            }
          }
        }
      }
    }
  }

  public void setAttributeName(final String attributeName) {
    this.attributeName = attributeName;
  }

  public void setEqualExclude(final List<String> equalExclude) {
    this.equalExclude = equalExclude;
  }

  /**
   * @param in the in to set
   */
  public void setOtherIn(final Channel<DataObject> in) {
    this.otherIn = in;
    in.readConnect();
  }

  public void setOtherInBufferSize(final int otherInBufferSize) {
    this.otherInBufferSize = otherInBufferSize;
  }

  public void setOtherName(final String otherName) {
    this.otherName = otherName;
  }

  public void setSourceName(final String sourceName) {
    this.sourceName = sourceName;
  }
}
