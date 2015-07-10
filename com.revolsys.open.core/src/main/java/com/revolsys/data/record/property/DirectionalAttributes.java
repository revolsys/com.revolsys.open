package com.revolsys.data.record.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.data.equals.RecordEquals;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.Records;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.gis.graph.Edge;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.vertex.Vertex;

public class DirectionalAttributes extends AbstractRecordDefinitionProperty {
  public static final String PROPERTY_NAME = DirectionalAttributes.class.getName()
    + ".propertyName";

  private static final Logger LOG = LoggerFactory.getLogger(DirectionalAttributes.class);

  public static boolean canMergeObjects(final Point point, final Record record1,
    final Record record2) {
    final Set<String> excludes = Collections.emptySet();
    final DirectionalAttributes property = DirectionalAttributes.getProperty(record1);
    return property.canMerge(point, record1, record2, excludes);
  }

  public static boolean canMergeObjects(final Point point, final Record record1,
    final Record record2, final Set<String> equalExcludeAttributes) {
    final DirectionalAttributes property = DirectionalAttributes.getProperty(record1);
    return property.canMerge(point, record1, record2, equalExcludeAttributes);
  }

  public static void edgeSplitAttributes(final LineString line, final Point point,
    final List<Edge<Record>> edges) {
    if (!edges.isEmpty()) {
      final Edge<Record> firstEdge = edges.get(0);
      final Record record = firstEdge.getObject();
      final DirectionalAttributes property = DirectionalAttributes.getProperty(record);
      property.setEdgeSplitAttributes(line, point, edges);
    }
  }

  public static boolean equalsObjects(final Record record1, final Record record2) {
    final Set<String> excludes = Collections.emptySet();
    return equalsObjects(record1, record2, excludes);
  }

  public static boolean equalsObjects(final Record record1, final Record record2,
    final Collection<String> equalExcludeAttributes) {
    final DirectionalAttributes property = DirectionalAttributes.getProperty(record1);
    return property.equals(record1, record2, equalExcludeAttributes);
  }

  public static Set<String> getCantMergeAttributesObjects(final Point point, final Record record1,
    final Record record2, final Set<String> equalExcludeAttributes) {
    final DirectionalAttributes property = DirectionalAttributes.getProperty(record1);
    return property.getCantMergeAttributes(point, record1, record2, equalExcludeAttributes);
  }

  public static DirectionalAttributes getProperty(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    return getProperty(recordDefinition);
  }

  public static DirectionalAttributes getProperty(final RecordDefinition recordDefinition) {
    DirectionalAttributes property = recordDefinition.getProperty(PROPERTY_NAME);
    if (property == null) {
      property = new DirectionalAttributes();
      property.setRecordDefinition(recordDefinition);
    }
    return property;
  }

  public static Record getReverseObject(final Record record) {
    final DirectionalAttributes property = getProperty(record);
    final Record reverse = property.getReverse(record);
    return reverse;
  }

  public static boolean hasProperty(final Record record) {
    final RecordDefinition recordDefinition = record.getRecordDefinition();
    return recordDefinition.getProperty(PROPERTY_NAME) != null;
  }

  public static Record merge(final Point point, final Record record1, final Record record2) {
    final DirectionalAttributes property = DirectionalAttributes.getProperty(record1);
    return property.getMergedRecord(point, record1, record2);
  }

  public static Record merge(final Record record1, final Record record2) {
    final DirectionalAttributes property = DirectionalAttributes.getProperty(record1);
    return property.getMergedRecord(record1, record2);
  }

  public static Record mergeLongest(final Point point, final Record record1, final Record record2) {
    final DirectionalAttributes property = DirectionalAttributes.getProperty(record1);
    return property.getMergedRecordReverseLongest(point, record1, record2);
  }

  public static Record mergeLongest(final Record record1, final Record record2) {
    final DirectionalAttributes property = DirectionalAttributes.getProperty(record1);
    return property.getMergedRecordReverseLongest(record1, record2);
  }

  public static void reverse(final Record record) {
    final DirectionalAttributes property = getProperty(record);
    property.reverseAttributesAndGeometry(record);
  }

  private final Map<String, String> endFieldNamePairs = new HashMap<String, String>();

  private final Map<String, String> sideFieldNamePairs = new HashMap<String, String>();

  private final Map<String, String> reverseFieldNameMap = new HashMap<String, String>();

  private final Set<String> startFieldNames = new HashSet<String>();

  private final Set<String> sideFieldNames = new HashSet<String>();

  private final Set<String> endFieldNames = new HashSet<String>();

  private final Map<String, Map<Object, Object>> directionalAttributeValues = new HashMap<String, Map<Object, Object>>();

  private final List<List<String>> endAndSideFieldNamePairs = new ArrayList<List<String>>();

  public DirectionalAttributes() {
  }

  public void addDirectionalAttributeValues(final String fieldName,
    final Map<? extends Object, ? extends Object> values) {
    final Map<Object, Object> newValues = new LinkedHashMap<Object, Object>();
    for (final Entry<? extends Object, ? extends Object> entry : values.entrySet()) {
      final Object value1 = entry.getKey();
      final Object value2 = entry.getValue();
      addValue(newValues, value1, value2);
      addValue(newValues, value2, value1);
    }
    this.directionalAttributeValues.put(fieldName, newValues);
  }

  public void addEndAndSideAttributePairs(final String startLeftFieldName,
    final String startRightFieldName, final String endLeftFieldName,
    final String endRightFieldName) {
    this.endAndSideFieldNamePairs.add(
      Arrays.asList(startLeftFieldName, startRightFieldName, endLeftFieldName, endRightFieldName));
    addEndAttributePairInternal(startLeftFieldName, endLeftFieldName);
    addEndAttributePairInternal(startRightFieldName, endRightFieldName);
    addFieldNamePair(this.reverseFieldNameMap, startLeftFieldName, endRightFieldName);
    addFieldNamePair(this.reverseFieldNameMap, endLeftFieldName, startRightFieldName);
  }

  public void addEndAttributePair(final String startFieldName, final String endFieldName) {
    addEndAttributePairInternal(startFieldName, endFieldName);
    addFieldNamePair(this.reverseFieldNameMap, startFieldName, endFieldName);
  }

  private void addEndAttributePairInternal(final String startFieldName, final String endFieldName) {
    addFieldNamePair(this.endFieldNamePairs, startFieldName, endFieldName);
    this.startFieldNames.add(startFieldName);
    this.endFieldNames.add(endFieldName);
  }

  /**
   * Add a mapping from the fromFieldName to the toFieldName and an
   * inverse mapping to the namePairs map.
   *
   * @param namePairs The name pair mapping.
   * @param fromFieldName The from attribute name.
   * @param toFieldName The to attribute name.
   */
  private void addFieldNamePair(final Map<String, String> namePairs, final String fromFieldName,
    final String toFieldName) {
    final String fromPair = namePairs.get(fromFieldName);
    if (fromPair == null) {
      final String toPair = namePairs.get(toFieldName);
      if (toPair == null) {
        namePairs.put(fromFieldName, toFieldName);
        namePairs.put(toFieldName, fromFieldName);
      } else if (toPair.equals(fromFieldName)) {
        throw new IllegalArgumentException(
          "Cannot override mapping " + toFieldName + "=" + toPair + " to " + fromFieldName);
      }
    } else if (fromPair.equals(toFieldName)) {
      throw new IllegalArgumentException(
        "Cannot override mapping " + fromFieldName + "=" + fromPair + " to " + toFieldName);
    }
  }

  public void addSideAttributePair(final String leftFieldName, final String rightFieldName) {
    addFieldNamePair(this.sideFieldNamePairs, leftFieldName, rightFieldName);
    this.sideFieldNames.add(leftFieldName);
    this.sideFieldNames.add(rightFieldName);
    addFieldNamePair(this.reverseFieldNameMap, leftFieldName, rightFieldName);
  }

  protected void addValue(final Map<Object, Object> map, final Object key, final Object value) {
    final Object oldValue = map.get(key);
    if (oldValue != null && !oldValue.equals(value)) {
      throw new IllegalArgumentException(
        "Cannot override mapping " + key + "=" + oldValue + " with " + value);
    }
    map.put(key, value);
  }

  public boolean canMerge(final Point point, final Record record1, final Record record2,
    final Collection<String> equalExcludeAttributes) {
    final boolean[] forwardsIndicators = getForwardsIndicators(point, record1, record2);

    if (forwardsIndicators != null) {
      final RecordDefinition recordDefinition = getRecordDefinition();
      final EqualIgnoreAttributes equalIgnore = EqualIgnoreAttributes.getProperty(recordDefinition);
      for (final String fieldName : recordDefinition.getFieldNames()) {
        if (!RecordEquals.isFieldIgnored(recordDefinition, equalExcludeAttributes, fieldName)
          && !equalIgnore.isFieldIgnored(fieldName)) {
          if (!canMerge(fieldName, point, record1, record2, equalExcludeAttributes,
            forwardsIndicators)) {
            return false;
          }
        }
      }
      return true;
    } else {
      return false;
    }
  }

  public boolean canMerge(final String fieldName, final Point point, final Record record1,
    final Record record2, final Collection<String> equalExcludeAttributes,
    final boolean[] forwardsIndicators) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (fieldName.equals(recordDefinition.getGeometryFieldName())) {
      final LineString line1 = record1.getGeometry();
      final LineString line2 = record2.getGeometry();
      return !line1.equals(line2);
    }
    if (forwardsIndicators == null) {
      return false;
    } else {
      final boolean line1Forwards = forwardsIndicators[0];
      final boolean line2Forwards = forwardsIndicators[1];
      if (hasDirectionalAttributeValues(fieldName)) {
        if (line1Forwards != line2Forwards) {
          final Object value1 = record1.getValue(fieldName);
          final Object value2 = getDirectionalAttributeValue(record2, fieldName);
          if (EqualsInstance.INSTANCE.equals(value1, value2, equalExcludeAttributes)) {
            return true;
          } else {
            if (LOG.isDebugEnabled()) {
              LOG.debug("Different values (" + fieldName + "=" + value1 + ") != (" + fieldName
                + " = " + value2 + ")");
              LOG.debug(record1.toString());
              LOG.debug(record2.toString());
            }
            return false;
          }
        }
      } else if (isStartAttribute(fieldName)) {
        return canMergeStartAttribute(fieldName, record1, line1Forwards, record2, line2Forwards,
          equalExcludeAttributes);
      } else if (isEndAttribute(fieldName)) {
        return canMergeEndAttribute(fieldName, record1, line1Forwards, record2, line2Forwards,
          equalExcludeAttributes);
      } else if (isSideAttribute(fieldName)) {
        if (line1Forwards != line2Forwards) {
          final String oppositeFieldName = getSideAttributePair(fieldName);
          if (oppositeFieldName == null) { // only check the pair once
            return true;
          } else {
            return equals(record1, fieldName, record2, oppositeFieldName, equalExcludeAttributes);
          }
        }
      }
      return equals(record1, fieldName, record2, fieldName, equalExcludeAttributes);
    }
  }

  protected boolean canMergeEndAttribute(final String endFieldName, final Record record1,
    final boolean line1Forwards, final Record record2, final boolean line2Forwards,
    final Collection<String> equalExcludeAttributes) {
    final String startFieldName = this.endFieldNamePairs.get(endFieldName);
    if (line1Forwards) {
      if (line2Forwards) {
        // -->*--> true true
        return isNull(record1, endFieldName, record2, startFieldName, equalExcludeAttributes);
      } else {
        // -->*<-- true false
        return isNull(record1, endFieldName, record2, endFieldName, equalExcludeAttributes);
      }
    } else {
      if (line2Forwards) {
        // <--*--> false true
        return true;
      } else {
        // <--*<-- false false
        return isNull(record1, startFieldName, record2, endFieldName, equalExcludeAttributes);
      }
    }
  }

  protected boolean canMergeStartAttribute(final String startFieldName, final Record record1,
    final boolean line1Forwards, final Record record2, final boolean line2Forwards,
    final Collection<String> equalExcludeAttributes) {
    final String endFieldName = this.endFieldNamePairs.get(startFieldName);
    if (line1Forwards) {
      if (line2Forwards) {
        // -->*--> true true
        return isNull(record1, endFieldName, record2, startFieldName, equalExcludeAttributes);
      } else {
        // -->*<-- true false
        return true;
      }
    } else {
      if (line2Forwards) {
        // <--*--> false true
        return isNull(record1, startFieldName, record2, startFieldName, equalExcludeAttributes);
      } else {
        // <--*<-- false false
        return isNull(record1, startFieldName, record2, endFieldName, equalExcludeAttributes);
      }
    }
  }

  public void clearEndAttributes(final Record record) {
    for (final String fieldName : this.endFieldNames) {
      record.setValue(fieldName, null);
    }
  }

  public void clearStartAttributes(final Record record) {
    for (final String fieldName : this.startFieldNames) {
      record.setValue(fieldName, null);
    }
  }

  public boolean equals(final Record record1, final Record record2,
    final Collection<String> equalExcludeAttributes) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final EqualIgnoreAttributes equalIgnore = EqualIgnoreAttributes.getProperty(recordDefinition);
    for (final String fieldName : recordDefinition.getFieldNames()) {
      if (!equalExcludeAttributes.contains(fieldName) && !equalIgnore.isFieldIgnored(fieldName)) {
        if (!equals(fieldName, record1, record2, equalExcludeAttributes)) {
          return false;
        }
      }
    }
    return true;
  }

  protected boolean equals(final Record record1, final String name1, final Record record2,
    final String name2, final Collection<String> equalExcludeAttributes) {
    final Object value1 = record1.getValue(name1);
    final Object value2 = record2.getValue(name2);
    if (EqualsInstance.INSTANCE.equals(value1, value2, equalExcludeAttributes)) {
      return true;
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug(
          "Different values (" + name1 + "=" + value1 + ") != (" + name2 + " = " + value2 + ")");
        LOG.debug(record1.toString());
        LOG.debug(record2.toString());
      }
      return false;
    }
  }

  protected boolean equals(final String fieldName, final Record record1, final Record record2,
    final Collection<String> equalExcludeAttributes) {
    final LineString line1 = record1.getGeometry();
    final LineString line2 = record2.getGeometry();
    final RecordDefinition recordDefinition = getRecordDefinition();
    if (fieldName.equals(recordDefinition.getGeometryFieldName())) {
      return line1.equals(line2);
    }

    boolean reverseEquals;
    if (line1.equalsVertex(2, 0, line2, 0)) {
      if (line1.isClosed()) {
        // TODO handle loops
        throw new IllegalArgumentException("Cannot handle loops");
      }
      reverseEquals = false;
    } else {
      reverseEquals = true;
    }
    if (reverseEquals) {
      return equalsReverse(fieldName, record1, record2, equalExcludeAttributes);
    } else {
      return equals(record1, fieldName, record2, fieldName, equalExcludeAttributes);
    }
  }

  private boolean equalsReverse(final String fieldName, final Record record1, final Record record2,
    final Collection<String> equalExcludeAttributes) {
    if (hasDirectionalAttributeValues(fieldName)) {
      final Object value1 = record1.getValue(fieldName);
      final Object value2 = getDirectionalAttributeValue(record2, fieldName);
      if (EqualsInstance.INSTANCE.equals(value1, value2, equalExcludeAttributes)) {
        return true;
      } else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Different values (" + fieldName + "=" + value1 + ") != (" + fieldName + " = "
            + value2 + ")");
          LOG.debug(record1.toString());
          LOG.debug(record2.toString());
        }
        return false;
      }
    } else {
      final String reverseFieldName = getReverseFieldName(fieldName);
      if (reverseFieldName == null) {
        return equals(record1, fieldName, record2, fieldName, equalExcludeAttributes);
      } else {
        return equals(record1, fieldName, record2, reverseFieldName, equalExcludeAttributes);
      }
    }
  }

  public Set<String> getCantMergeAttributes(final Point point, final Record record1,
    final Record record2, final Collection<String> equalExcludeAttributes) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    final boolean[] forwardsIndicators = getForwardsIndicators(point, record1, record2);
    if (forwardsIndicators != null) {
      final Set<String> fieldNames = new LinkedHashSet<String>();
      final EqualIgnoreAttributes equalIgnore = EqualIgnoreAttributes.getProperty(recordDefinition);
      for (final String fieldName : recordDefinition.getFieldNames()) {
        if (!equalExcludeAttributes.contains(fieldName) && !equalIgnore.isFieldIgnored(fieldName)) {
          if (!canMerge(fieldName, point, record1, record2, equalExcludeAttributes,
            forwardsIndicators)) {
            fieldNames.add(fieldName);
          }
        }
      }
      return fieldNames;
    } else {
      final String geometryFieldName = recordDefinition.getGeometryFieldName();
      return Collections.singleton(geometryFieldName);
    }
  }

  protected Object getDirectionalAttributeValue(final Map<String, ? extends Object> record,
    final String fieldName) {
    final Object value = record.get(fieldName);

    final Map<Object, Object> valueMap = this.directionalAttributeValues.get(fieldName);
    if (valueMap != null) {
      if (valueMap.containsKey(value)) {
        final Object directionalValue = valueMap.get(value);
        return directionalValue;
      }
    }
    return value;
  }

  public Map<String, Map<Object, Object>> getDirectionalAttributeValues() {
    return this.directionalAttributeValues;
  }

  public List<List<String>> getEndAndSideFieldNamePairs() {
    return this.endAndSideFieldNamePairs;
  }

  public Map<String, String> getEndFieldNamePairs() {
    return this.endFieldNamePairs;
  }

  public Set<String> getEndFieldNames() {
    return this.endFieldNames;
  }

  protected boolean[] getForwardsIndicators(final Point point, final Record record1,
    final Record record2) {
    final LineString line1 = record1.getGeometry();
    final LineString line2 = record2.getGeometry();

    final boolean[] forwards = new boolean[2];
    final int vertexCount1 = line1.getVertexCount();
    final int vertexCount2 = line2.getVertexCount();
    final int lastPointIndex1 = vertexCount1 - 1;
    if (line1.equalsVertex(2, 0, line2, 0) && line1.equalsVertex(2, 0, point)) {
      // <--*--> false true
      forwards[0] = false;
      forwards[1] = true;
    } else if (line1.equalsVertex(2, vertexCount1 - 1, line2, vertexCount2 - 1)
      && line1.equalsVertex(2, lastPointIndex1, point)) {
      // -->*<-- true false
      forwards[0] = true;
      forwards[1] = false;
    } else if (line1.equalsVertex(2, vertexCount1 - 1, line2, 0)
      && line1.equalsVertex(2, lastPointIndex1, point)) {
      // -->*--> true true
      forwards[0] = true;
      forwards[1] = true;
    } else
      if (line1.equalsVertex(2, 0, line2, vertexCount2 - 1) && line1.equalsVertex(2, 0, point)) {
      // <--*<-- false false
      forwards[0] = false;
      forwards[1] = false;
    } else {
      return null;
    }
    return forwards;
  }

  public Map<String, Object> getMergedMap(final Point point, final Record record1, Record record2) {
    final LineString line1 = record1.getGeometry();
    LineString line2 = record2.getGeometry();

    Record startObject;
    Record endObject;

    LineString newLine;

    final Vertex line1From = line1.getVertex(0);
    final Vertex line2From = line2.getVertex(0);
    if (line1From.equals(2, line2From) && line1From.equals(2, point)) {
      record2 = getReverse(record2);
      line2 = record2.getGeometry();
      startObject = record2;
      endObject = record1;
      newLine = line1.merge(point, line2);
    } else {
      final Vertex line1To = line1.getVertex(-1);
      final Vertex line2To = line2.getVertex(-1);
      if (line1To.equals(2, line2To) && line1To.equals(2, point)) {
        record2 = getReverse(record2);
        line2 = record2.getGeometry();
        startObject = record1;
        endObject = record2;
        newLine = line1.merge(point, line2);
      } else if (line1To.equals(2, line2From) && line1To.equals(2, point)) {
        startObject = record1;
        endObject = record2;
        newLine = line1.merge(point, line2);
      } else if (line1From.equals(2, line2To) && line1From.equals(2, point)) {
        startObject = record2;
        endObject = record1;
        newLine = line2.merge(point, line1);
      } else {
        throw new IllegalArgumentException("Lines for records don't touch");
      }
    }

    final Map<String, Object> newValues = new LinkedHashMap<String, Object>(record1);
    setStartAttributes(startObject, newValues);
    setEndAttributes(endObject, newValues);
    final RecordDefinition recordDefinition = record1.getRecordDefinition();
    final String geometryFieldName = recordDefinition.getGeometryFieldName();
    newValues.put(geometryFieldName, newLine);
    return newValues;
  }

  /**
   * Get a new record that is the result of merging the two records. The
   * attributes will be taken from the record with the longest length. If one
   * line needs to be reversed then the second record will be reversed.
   *
   * @param record1
   * @param record2
   * @return
   */
  public Record getMergedRecord(final Point point, final Record record1, Record record2) {
    final LineString line1 = record1.getGeometry();
    LineString line2 = record2.getGeometry();

    Record startObject;
    Record endObject;

    final boolean line1Longer = line1.getLength() > line2.getLength();
    LineString newLine;
    final int lastPoint1 = line1.getVertexCount() - 1;
    final int lastPoint2 = line2.getVertexCount() - 1;

    if (line1.equalsVertex(2, 0, line2, 0) && line1.equalsVertex(2, 0, point)) {
      record2 = getReverse(record2);
      line2 = record2.getGeometry();
      startObject = record2;
      endObject = record1;
      newLine = line1.merge(point, line2);
    } else if (line1.equalsVertex(2, lastPoint1, line2, lastPoint2)
      && line1.equalsVertex(2, lastPoint1, point)) {
      record2 = getReverse(record2);
      line2 = record2.getGeometry();
      startObject = record1;
      endObject = record2;
      newLine = line1.merge(point, line2);
    } else
      if (line1.equalsVertex(2, lastPoint1, line2, 0) && line1.equalsVertex(2, lastPoint1, point)) {
      startObject = record1;
      endObject = record2;
      newLine = line1.merge(point, line2);
    } else if (line1.equalsVertex(2, 0, line2, lastPoint2) && line1.equalsVertex(2, 0, point)) {
      startObject = record2;
      endObject = record1;
      newLine = line2.merge(point, line1);
    } else {
      throw new IllegalArgumentException("Lines for records don't touch");
    }

    Record newObject;
    if (line1Longer) {
      newObject = Records.copy(record1, newLine);
    } else {
      newObject = Records.copy(record2, newLine);
    }
    setStartAttributes(startObject, newObject);
    setEndAttributes(endObject, newObject);
    LengthFieldName.setObjectLength(newObject);
    return newObject;
  }

  /**
   * Get a new record that is the result of merging the two records. The
   * attributes will be taken from the record with the longest length. If one
   * line needs to be reversed then the second record will be reversed.
   *
   * @param record1
   * @param record2
   * @return
   */
  public Record getMergedRecord(final Record record1, Record record2) {
    final LineString line1 = record1.getGeometry();
    final int vertexCount1 = line1.getVertexCount();
    LineString line2 = record2.getGeometry();
    final int vertexCount2 = line2.getVertexCount();

    Record startObject;
    Record endObject;

    final boolean line1Longer = line1.getLength() > line2.getLength();
    LineString newLine;

    if (line1.equalsVertex(2, 0, line2, 0)) {
      record2 = getReverse(record2);
      line2 = record2.getGeometry();
      startObject = record2;
      endObject = record1;
      newLine = line1.merge(line2);
    } else if (line1.equalsVertex(2, vertexCount1 - 1, line2, vertexCount2 - 1)) {
      record2 = getReverse(record2);
      line2 = record2.getGeometry();
      startObject = record1;
      endObject = record2;
      newLine = line1.merge(line2);
    } else if (line1.equalsVertex(2, vertexCount1 - 1, line2, 0)) {
      startObject = record1;
      endObject = record2;
      newLine = line1.merge(line2);
    } else if (line1.equalsVertex(2, 0, line2, vertexCount2 - 1)) {
      startObject = record2;
      endObject = record1;
      newLine = line2.merge(line1);
    } else {
      throw new IllegalArgumentException("Lines for records don't touch");
    }

    Record newObject;
    if (line1Longer) {
      newObject = Records.copy(record1, newLine);
    } else {
      newObject = Records.copy(record2, newLine);
    }
    setStartAttributes(startObject, newObject);
    setEndAttributes(endObject, newObject);
    LengthFieldName.setObjectLength(newObject);
    return newObject;
  }

  public Record getMergedRecordReverseLongest(final Point point, final Record record1,
    final Record record2) {
    final LineString line1 = record1.getGeometry();
    final LineString line2 = record2.getGeometry();
    if (line1.getLength() >= line2.getLength()) {
      return getMergedRecord(point, record1, record2);
    } else {
      return getMergedRecord(point, record2, record1);
    }
  }

  /**
   * Get a new record that is the result of merging the two records. The
   * attributes will be taken from the record with the longest length. If one
   * line needs to be reversed then the longest will be reversed.
   *
   * @param record1
   * @param record2
   * @return
   */
  public Record getMergedRecordReverseLongest(final Record record1, final Record record2) {
    final LineString line1 = record1.getGeometry();
    final LineString line2 = record2.getGeometry();
    if (line1.getLength() >= line2.getLength()) {
      return getMergedRecord(record1, record2);
    } else {
      return getMergedRecord(record2, record1);
    }
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public Record getReverse(final Record record) {
    final Record reverse = record.clone();
    reverseAttributesAndGeometry(reverse);
    return reverse;
  }

  public Map<String, Object> getReverseAttributes(final Map<String, Object> record) {
    final Map<String, Object> reverse = new LinkedHashMap<String, Object>(record);
    for (final Entry<String, String> pair : this.reverseFieldNameMap.entrySet()) {
      final String fromFieldName = pair.getKey();
      final String toFieldName = pair.getValue();
      final Object toValue = record.get(toFieldName);
      reverse.put(fromFieldName, toValue);
    }
    for (final String fieldName : this.directionalAttributeValues.keySet()) {
      final Object value = getDirectionalAttributeValue(record, fieldName);
      reverse.put(fieldName, value);
    }
    return reverse;
  }

  public Map<String, Object> getReverseAttributesAndGeometry(final Map<String, Object> record) {
    final Map<String, Object> reverse = getReverseAttributes(record);
    final String geometryFieldName = getRecordDefinition().getGeometryFieldName();
    if (geometryFieldName != null) {
      final Geometry geometry = getReverseLine(record);
      reverse.put(geometryFieldName, geometry);
    }
    return reverse;
  }

  public String getReverseFieldName(final String fieldName) {
    return this.reverseFieldNameMap.get(fieldName);
  }

  public Map<String, Object> getReverseGeometry(final Map<String, Object> record) {
    final Map<String, Object> reverse = new LinkedHashMap<String, Object>(record);
    final String geometryFieldName = getRecordDefinition().getGeometryFieldName();
    if (geometryFieldName != null) {
      final Geometry geometry = getReverseLine(record);
      reverse.put(geometryFieldName, geometry);
    }
    return reverse;
  }

  protected Geometry getReverseLine(final Map<String, Object> record) {
    final String geometryFieldName = getRecordDefinition().getGeometryFieldName();
    final LineString line = (LineString)record.get(geometryFieldName);
    if (line == null) {
      return null;
    } else {
      final LineString reverseLine = line.reverse();
      return reverseLine;
    }
  }

  protected String getSideAttributePair(final String fieldName) {
    return this.sideFieldNamePairs.get(fieldName);
  }

  public Map<String, String> getSideFieldNamePairs() {
    return this.sideFieldNamePairs;
  }

  public Set<String> getStartFieldNames() {
    return this.startFieldNames;
  }

  public boolean hasDirectionalAttributes() {
    return !this.directionalAttributeValues.isEmpty() || !this.reverseFieldNameMap.isEmpty();
  }

  public boolean hasDirectionalAttributeValues(final String fieldName) {
    return this.directionalAttributeValues.containsKey(fieldName);
  }

  public boolean isEndAttribute(final String fieldName) {
    return this.endFieldNames.contains(fieldName);
  }

  protected boolean isNull(final Record record1, final String name1, final Record record2,
    final String name2, final Collection<String> equalExcludeAttributes) {
    final Object value1 = record1.getValue(name1);
    final Object value2 = record2.getValue(name2);
    if (value1 == null && value2 == null) {
      return true;
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Both values not null (" + name1 + "=" + value1 + ") != (" + name2 + " = "
          + value2 + ")");
        LOG.debug(record1.toString());
        LOG.debug(record2.toString());
      }
      return false;
    }
  }

  public boolean isSideAttribute(final String fieldName) {
    return this.sideFieldNames.contains(fieldName);
  }

  public boolean isStartAttribute(final String fieldName) {
    return this.startFieldNames.contains(fieldName);
  }

  public void reverseAttributes(final Map<String, Object> record) {
    final Map<String, Object> reverseAttributes = getReverseAttributes(record);
    record.putAll(reverseAttributes);
  }

  public void reverseAttributesAndGeometry(final Map<String, Object> record) {
    final Map<String, Object> reverseAttributes = getReverseAttributesAndGeometry(record);
    record.putAll(reverseAttributes);
  }

  public void reverseGeometry(final Map<String, Object> record) {
    final Map<String, Object> reverseAttributes = getReverseGeometry(record);
    record.putAll(reverseAttributes);

  }

  public void setDirectionalAttributeValues(
    final Map<String, Map<Object, Object>> directionalAttributeValues) {
    for (final Entry<String, Map<Object, Object>> entry : directionalAttributeValues.entrySet()) {
      final String fieldName = entry.getKey();
      final Map<Object, Object> values = entry.getValue();
      addDirectionalAttributeValues(fieldName, values);
    }
  }

  public void setEdgeSplitAttributes(final LineString line, final Point point,
    final List<Edge<Record>> edges) {
    for (final Edge<Record> edge : edges) {
      final Record record = edge.getObject();
      setSplitAttributes(line, point, record);
    }
  }

  public void setEndAndSideFieldNamePairs(final List<List<String>> endAndSideAttributePairs) {
    for (final List<String> endAndSideAttributePair : endAndSideAttributePairs) {
      final String startLeftFieldName = endAndSideAttributePair.get(0);
      final String startRightFieldName = endAndSideAttributePair.get(1);
      final String endLeftFieldName = endAndSideAttributePair.get(2);
      final String endRightFieldName = endAndSideAttributePair.get(3);
      addEndAndSideAttributePairs(startLeftFieldName, startRightFieldName, endLeftFieldName,
        endRightFieldName);
    }
  }

  public void setEndAttributes(final Record source, final Map<String, Object> newObject) {
    for (final String fieldName : this.endFieldNames) {
      final Object value = source.getValue(fieldName);
      newObject.put(fieldName, value);
    }
  }

  public void setEndFieldNamePairs(final Map<String, String> fieldNamePairs) {
    this.endFieldNamePairs.clear();
    this.endFieldNames.clear();
    this.startFieldNames.clear();
    for (final Entry<String, String> pair : fieldNamePairs.entrySet()) {
      final String from = pair.getKey();
      final String to = pair.getValue();
      addEndAttributePair(from, to);
    }
  }

  public void setSideFieldNamePairs(final Map<String, String> fieldNamePairs) {
    this.sideFieldNamePairs.clear();
    for (final Entry<String, String> pair : fieldNamePairs.entrySet()) {
      final String from = pair.getKey();
      final String to = pair.getValue();
      addSideAttributePair(from, to);
    }
  }

  public void setSplitAttributes(final LineString line, final Point point, final Record record) {
    final LineString newLine = record.getGeometry();
    if (newLine != null) {
      final boolean firstPoint = newLine.equalsVertex(2, 0, point);
      final boolean toPoint = newLine.equalsVertex(2, -1, point);
      if (firstPoint) {
        if (!toPoint) {
          clearStartAttributes(record);
        }
      } else if (toPoint) {
        clearEndAttributes(record);
      }
    }
  }

  public void setStartAttributes(final Record source, final Map<String, Object> newObject) {
    for (final String fieldName : this.startFieldNames) {
      final Object value = source.getValue(fieldName);
      newObject.put(fieldName, value);
    }
  }

  @Override
  public String toString() {
    return "DirectionalAttributes";
  }
}
