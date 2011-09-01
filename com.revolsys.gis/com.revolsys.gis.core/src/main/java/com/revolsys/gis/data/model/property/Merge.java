package com.revolsys.gis.data.model.property;

import java.util.Collection;

import com.revolsys.gis.data.model.AbstractDataObjectMetaDataProperty;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.vividsolutions.jts.geom.LineString;

public class Merge extends AbstractDataObjectMetaDataProperty {
  public static final String PROPERTY_NAME = Merge.class.getName()
    + ".propertyName";

  public static Merge getProperty(final DataObject object) {
    final DataObjectMetaData metaData = object.getMetaData();
    return getProperty(metaData);
  }

  protected static Merge getProperty(final DataObjectMetaData metaData) {
    Merge property = metaData.getProperty(PROPERTY_NAME);
    if (property == null) {
      property = new Merge();
      property.setMetaData(metaData);
    }
    return property;
  }

  public Merge() {
  }

  public boolean canMerge(final DataObject object1, final DataObject object2,
    final Collection<String> equalExcludeAttributes) {
    final DataObjectMetaData metaData = getMetaData();
    final LineAttributePairs attributePairs = LineAttributePairs.getProperty(metaData);
    final EqualIgnoreAttributes equalIgnore = EqualIgnoreAttributes.getProperty(metaData);
    for (final String attributeName : metaData.getAttributeNames()) {
      if (!equalExcludeAttributes.contains(attributeName)
        && !equalIgnore.isAttributeIgnored(attributeName)) {
        if (!attributePairs.canMerge(attributeName, object1, object2,
          equalExcludeAttributes)) {
          return false;
        }
      }
    }
    return true;
  }

  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  @Override
  public String toString() {
    return "Merge";
  }

  public DataObject getMergedObject(DataObject object1, DataObject object2) {
    final LineAttributePairs attributePairs = LineAttributePairs.getProperty(object1);
    final LineString line1 = object1.getGeometryValue();
    final LineString line2 = object2.getGeometryValue();
    final CoordinatesList points1 = CoordinatesListUtil.get(line1);
    final CoordinatesList points2 = CoordinatesListUtil.get(line2);

    boolean object1First = true;

    final boolean line1Longer = line1.getLength() > line2.getLength();

    if (points1.equal(0, points2, 0)) {
      if (line1Longer) {
        object2 = attributePairs.getReverse(object2);
        object1First = false;
      } else {
        object1 = attributePairs.getReverse(object1);
        object1First = true;
      }
    } else if (points1.equal(points1.size() - 1, points2, points2.size() - 1)) {
      if (line1Longer) {
        object2 = attributePairs.getReverse(object2);
        object1First = true;
      } else {
        object1 = attributePairs.getReverse(object1);
        object1First = false;
      }
    } else if (points1.equal(points1.size() - 1, points2, 0)) {
      object1First = true;
    } else {
      object1First = false;
    }

    DataObject newObject;
    if (line1Longer) {
      newObject = object1.clone();
    } else {
      newObject = object2.clone();
    }
    LineString newLine;
    if (object1First) {
      newLine = LineStringUtil.merge(line1, line2);
      attributePairs.setStartAttributes(object1, newObject);
      attributePairs.setEndAttributes(object2, newObject);
    } else {
      newLine = LineStringUtil.merge(line2, line1);
      attributePairs.setStartAttributes(object2, newObject);
      attributePairs.setEndAttributes(object1, newObject);
    }
    newObject.setGeometryValue(newLine);
    LengthAttributeName.setObjectLength(newObject);
    return newObject;
  }
}
