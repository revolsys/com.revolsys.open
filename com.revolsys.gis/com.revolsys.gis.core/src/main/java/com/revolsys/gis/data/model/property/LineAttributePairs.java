package com.revolsys.gis.data.model.property;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.data.model.AbstractDataObjectMetaDataProperty;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.vividsolutions.jts.geom.LineString;

public class LineAttributePairs extends AbstractDataObjectMetaDataProperty {
  public static final String PROPERTY_NAME = LineAttributePairs.class.getName()
    + ".propertyName";

  private static final Logger LOG = LoggerFactory.getLogger(LineAttributePairs.class);

  public static LineAttributePairs getProperty(final DataObject object) {
    final DataObjectMetaData metaData = object.getMetaData();
    return getProperty(metaData);
  }

  public static LineAttributePairs getProperty(final DataObjectMetaData metaData) {
    LineAttributePairs property = metaData.getProperty(PROPERTY_NAME);
    if (property == null) {
      property = new LineAttributePairs();
      property.setMetaData(metaData);
    }
    return property;
  }

  private final Map<String, String> allAttributeNamePairs = new HashMap<String, String>();

  private final Map<String, String> endAttributeNamePairs = new HashMap<String, String>();

  private final Map<String, String> sideAttributeNamePairs = new HashMap<String, String>();

  private final Set<String> attributeNames = new HashSet<String>();

  private final Set<String> startAttributeNames = new HashSet<String>();

  private final Set<String> sideAttributeNames = new HashSet<String>();

  private final Set<String> endAttributeNames = new HashSet<String>();

  private final Map<String, Map<Object, Object>> directionalAttributeValues = new HashMap<String, Map<Object, Object>>();

  public LineAttributePairs() {

  }

  private void addAttributeNamePair(final Map<String, String> namePairs,
    final String from, final String to) {
    final String fromPair = namePairs.get(from);
    if (fromPair == null) {
      final String toPair = namePairs.get(to);
      if (toPair == null) {
        namePairs.put(from, to);
        namePairs.put(to, from);
        attributeNames.add(from);
        attributeNames.add(to);
      } else if (toPair.equals(from)) {
        throw new IllegalArgumentException("Cannot override mapping " + to
          + "=" + toPair + " to " + from);
      }
    } else if (fromPair.equals(to)) {
      throw new IllegalArgumentException("Cannot override mapping " + from
        + "=" + fromPair + " to " + to);
    }
  }

  public void addDirectionalAttributeValues(final String attributeName,
    final Map<? extends Object, ? extends Object> values) {
    final Map<Object, Object> newValues = new LinkedHashMap<Object, Object>();
    for (final Entry<? extends Object, ? extends Object> entry : values.entrySet()) {
      final Object value1 = entry.getKey();
      final Object value2 = entry.getValue();
      addValue(newValues, value1, value2);
      addValue(newValues, value2, value1);
    }
    directionalAttributeValues.put(attributeName, newValues);
    attributeNames.add(attributeName);
  }

  public void addEndAttributeNamePair(final String from, final String to) {
    addAttributeNamePair(endAttributeNamePairs, from, to);
    addAttributeNamePair(allAttributeNamePairs, from, to);
    startAttributeNames.add(from);
    endAttributeNames.add(to);
  }

  public void addSideAttributeNamePair(final String from, final String to) {
    addAttributeNamePair(sideAttributeNamePairs, from, to);
    addAttributeNamePair(allAttributeNamePairs, from, to);
    sideAttributeNames.add(from);
    sideAttributeNames.add(to);
  }

  protected void addValue(final Map<Object, Object> map, final Object key,
    final Object value) {
    final Object oldValue = map.get(key);
    if (oldValue != null && !oldValue.equals(value)) {
      throw new IllegalArgumentException("Cannot override mapping " + key + "="
        + oldValue + " with " + value);
    }
    map.put(key, value);
  }

  public boolean canMerge(final String attributeName, final DataObject object1,
    final DataObject object2, final Collection<String> equalExcludeAttributes) {
    if (isPairedAttribute(attributeName)) {
      final LineString line1 = object1.getGeometryValue();
      final LineString line2 = object2.getGeometryValue();
      final CoordinatesList points1 = CoordinatesListUtil.get(line1);
      final CoordinatesList points2 = CoordinatesListUtil.get(line2);

      boolean line1Forwards;
      boolean line2Forwards;
      if (points1.equal(0, points2, 0)) {
        // <--*--> false true
        line1Forwards = false;
        line2Forwards = true;
      } else if (points1.equal(points1.size() - 1, points2, points2.size() - 1)) {
        // -->*<-- true false
        line1Forwards = true;
        line2Forwards = false;
      } else if (points1.equal(points1.size() - 1, points2, 0)) {
        // -->*--> true true
        line1Forwards = true;
        line2Forwards = true;
      } else {
        // <--*<-- false false
        line1Forwards = false;
        line2Forwards = false;
      }
      if (hasDirectionalAttributeValues(attributeName)) {
        if (line1Forwards != line2Forwards) {
          final Object value1 = object1.getValue(attributeName);
          final Object value2 = getDirectionalAttributeValue(object2,
            attributeName);
          if (EqualsRegistry.INSTANCE.equals(value1, value2,
            equalExcludeAttributes)) {
            return true;
          } else {
            LOG.error("Different values (" + attributeName + "=" + value1
              + ") != (" + attributeName + " = " + value2 + ")");
            LOG.error(object1.toString());
            LOG.error(object2.toString());
            return false;
          }
        }
      } else if (isStartAttribute(attributeName)) {
        return canMergeStartAttribute(attributeName, object1, line1Forwards,
          object2, line2Forwards, equalExcludeAttributes);
      } else if (isEndAttribute(attributeName)) {
        return canMergeEndAttribute(attributeName, object1, line1Forwards,
          object2, line2Forwards, equalExcludeAttributes);
      } else if (isSideAttribute(attributeName)) {
        if (line1Forwards != line2Forwards) {
          final String oppositeAttributeName = sideAttributeNamePairs.get(attributeName);
          if (oppositeAttributeName == null) { // only check the pair once
            return true;
          } else {
            return equals(object1, attributeName, object2,
              oppositeAttributeName, equalExcludeAttributes);
          }
        }
      }
    }
    return equals(object1, attributeName, object2, attributeName,
      equalExcludeAttributes);
  }

  protected boolean canMergeEndAttribute(final String endAttributeName,
    final DataObject object1, final boolean line1Forwards,
    final DataObject object2, final boolean line2Forwards,
    final Collection<String> equalExcludeAttributes) {
    final String startAttributeName = endAttributeNamePairs.get(endAttributeName);
    if (line1Forwards) {
      if (line2Forwards) {
        // -->*--> true true
        return isNull(object1, endAttributeName, object2, startAttributeName,
          equalExcludeAttributes);
      } else {
        // -->*<-- true false
        return isNull(object1, endAttributeName, object2, endAttributeName,
          equalExcludeAttributes);
      }
    } else {
      if (line2Forwards) {
        // <--*--> false true
        return true;
      } else {
        // <--*<-- false false
        return isNull(object1, startAttributeName, object2, endAttributeName,
          equalExcludeAttributes);
      }
    }
  }

  protected boolean canMergeStartAttribute(final String startAttributeName,
    final DataObject object1, final boolean line1Forwards,
    final DataObject object2, final boolean line2Forwards,
    final Collection<String> equalExcludeAttributes) {
    final String endAttributeName = endAttributeNamePairs.get(startAttributeName);
    if (line1Forwards) {
      if (line2Forwards) {
        // -->*--> true true
        return isNull(object1, endAttributeName, object2, startAttributeName,
          equalExcludeAttributes);
      } else {
        // -->*<-- true false
        return true;
      }
    } else {
      if (line2Forwards) {
        // <--*--> false true
        return isNull(object1, startAttributeName, object2, startAttributeName,
          equalExcludeAttributes);
      } else {
        // <--*<-- false false
        return isNull(object1, startAttributeName, object2, endAttributeName,
          equalExcludeAttributes);
      }
    }
  }

  protected boolean equals(final DataObject object1, final String name1,
    final DataObject object2, final String name2,
    final Collection<String> equalExcludeAttributes) {
    final Object value1 = object1.getValue(name1);
    final Object value2 = object2.getValue(name2);
    if (EqualsRegistry.INSTANCE.equals(value1, value2, equalExcludeAttributes)) {
      return true;
    } else {
      LOG.error("Different values (" + name1 + "=" + value1 + ") != (" + name2
        + " = " + value2 + ")");
      LOG.error(object1.toString());
      LOG.error(object2.toString());
      return false;
    }
  }

  public boolean equals(final String attributeName, final DataObject object1,
    final DataObject object2) {
    final Object value1 = object1.getValue(attributeName);
    String pairedAttributeName = allAttributeNamePairs.get(attributeName);
    if (pairedAttributeName == null) {
      pairedAttributeName = attributeName;
    }
    final Object value2 = object1.getValue(pairedAttributeName);
    if (EqualsRegistry.INSTANCE.equals(value1, value2)) {
      return true;
    } else {
      LOG.error("Different values for (" + attributeName + "=" + value1
        + ") != (" + pairedAttributeName + " = " + value2 + ")");
      LOG.error(object1.toString());
      LOG.error(object2.toString());
      return false;
    }
  }

  public Set<String> getAttributeNames() {
    return attributeNames;
  }

  protected Object getDirectionalAttributeValue(final DataObject object,
    final String attributeName) {
    final Object value = object.getValue(attributeName);

    final Map<Object, Object> valueMap = directionalAttributeValues.get(attributeName);
    if (valueMap != null) {
      if (valueMap.containsKey(value)) {
        final Object directionalValue = valueMap.get(value);
        return directionalValue;
      }
    }
    return value;
  }

  public Map<String, Map<Object, Object>> getDirectionalAttributeValues() {
    return directionalAttributeValues;
  }

  public Set<String> getEndAttributeNames() {
    return endAttributeNames;
  }

  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public DataObject getReverse(final DataObject object) {
    final DataObject reverse = object.clone();

    final LineString line = object.getGeometryValue();
    final LineString reverseLine = LineStringUtil.reverse(line);
    reverse.setGeometryValue(reverseLine);

    for (final Entry<String, String> pair : allAttributeNamePairs.entrySet()) {
      final String fromAttributeName = pair.getKey();
      final Object fromValue = object.get(fromAttributeName);
      final String toAttributeName = pair.getValue();
      final Object toValue = object.get(toAttributeName);
      reverse.setValue(toAttributeName, fromValue);
      reverse.setValue(fromAttributeName, toValue);
    }
    for (final String attributeName : directionalAttributeValues.keySet()) {
      final Object value = getDirectionalAttributeValue(object, attributeName);
      reverse.setValue(attributeName, value);
    }

    return reverse;
  }

  public Set<String> getStartAttributeNames() {
    return startAttributeNames;
  }

  public boolean hasDirectionalAttributeValues(final String attributeName) {
    return directionalAttributeValues.containsKey(attributeName);
  }

  public boolean isEndAttribute(final String attributeName) {
    return endAttributeNames.contains(attributeName);
  }

  protected boolean isNull(final DataObject object1, final String name1,
    final DataObject object2, final String name2,
    final Collection<String> equalExcludeAttributes) {
    final Object value1 = object1.getValue(name1);
    final Object value2 = object2.getValue(name2);
    if (value1 == null && value2 == null) {
      return true;
    } else {
      LOG.error("Both values not null (" + name1 + "=" + value1 + ") != ("
        + name2 + " = " + value2 + ")");
      LOG.error(object1.toString());
      LOG.error(object2.toString());
      return false;
    }
  }

  public boolean isPairedAttribute(final String attributeName) {
    return attributeNames.contains(attributeName);
  }

  public boolean isSideAttribute(final String attributeName) {
    return sideAttributeNames.contains(attributeName);
  }

  public boolean isStartAttribute(final String attributeName) {
    return startAttributeNames.contains(attributeName);
  }

  public void setDirectionalAttributeValues(
    final Map<String, Map<Object, Object>> directionalAttributeValues) {
    for (final Entry<String, Map<Object, Object>> entry : directionalAttributeValues.entrySet()) {
      final String attributeName = entry.getKey();
      final Map<Object, Object> values = entry.getValue();
      addDirectionalAttributeValues(attributeName, values);
    }
  }

  public void setEndAttributeNamePairs(
    final Map<String, String> attributeNamePairs) {
    this.endAttributeNamePairs.clear();
    this.endAttributeNames.clear();
    this.startAttributeNames.clear();
    for (final Entry<String, String> pair : attributeNamePairs.entrySet()) {
      final String from = pair.getKey();
      final String to = pair.getValue();
      addEndAttributeNamePair(from, to);
    }
  }

  public void setEndAttributes(final DataObject source, final DataObject target) {
    for (final String attributeName : endAttributeNames) {
      final Object value = source.getValue(attributeName);
      target.setValue(attributeName, value);
    }
  }

  public void setSideAttributeNamePairs(
    final Map<String, String> attributeNamePairs) {
    this.sideAttributeNamePairs.clear();
    for (final Entry<String, String> pair : attributeNamePairs.entrySet()) {
      final String from = pair.getKey();
      final String to = pair.getValue();
      addSideAttributeNamePair(from, to);
    }
  }

  public void setStartAttributes(final DataObject source,
    final DataObject target) {
    for (final String attributeName : startAttributeNames) {
      final Object value = source.getValue(attributeName);
      target.setValue(attributeName, value);
    }
  }

  @Override
  public String toString() {
    return "Reverse " + allAttributeNamePairs;
  }
}
