package com.revolsys.gis.data.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;

import org.slf4j.LoggerFactory;

import com.revolsys.collection.WeakCache;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.PathUtil;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.JavaBeanUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.AssertionFailedException;

public class DataObjectMetaDataImpl extends AbstractObjectWithProperties
  implements DataObjectMetaData, Cloneable {
  private static final AtomicInteger INSTANCE_IDS = new AtomicInteger(0);

  private static final Map<Integer, DataObjectMetaDataImpl> METADATA_CACHE = new WeakCache<Integer, DataObjectMetaDataImpl>();

  public static void destroy(final DataObjectMetaDataImpl... metaDataList) {
    for (final DataObjectMetaDataImpl metaData : metaDataList) {
      metaData.destroy();
    }
  }

  public static DataObjectMetaData getMetaData(final int instanceId) {
    return METADATA_CACHE.get(instanceId);
  }

  private Map<String, Integer> attributeIdMap = new HashMap<String, Integer>();

  private Map<String, Attribute> attributeMap = new HashMap<String, Attribute>();

  private List<String> attributeNames = new ArrayList<String>();

  private List<Attribute> attributes = new ArrayList<Attribute>();

  private Map<String, CodeTable> codeTableByColumnMap = new HashMap<String, CodeTable>();

  private DataObjectFactory dataObjectFactory = new ArrayDataObjectFactory();

  private DataObjectMetaDataFactory dataObjectMetaDataFactory;

  private DataObjectStore dataObjectStore;

  private Map<String, Object> defaultValues = new HashMap<String, Object>();

  /** The index of the primary geometry attribute. */
  private int geometryAttributeIndex = -1;

  private List<Integer> geometryAttributeIndexes = new ArrayList<Integer>();

  private List<String> geometryAttributeNames = new ArrayList<String>();

  /** The index of the ID attribute. */
  private int idAttributeIndex = -1;

  private final Integer instanceId = INSTANCE_IDS.getAndIncrement();

  /** The path of the data type. */
  private String path;

  private Map<String, Collection<Object>> restrictions = new HashMap<String, Collection<Object>>();

  protected DataObjectStoreSchema schema;

  private List<DataObjectMetaData> superClasses = new ArrayList<DataObjectMetaData>();

  public DataObjectMetaDataImpl() {
  }

  public DataObjectMetaDataImpl(final DataObjectMetaData metaData) {
    this(metaData.getPath(), metaData.getProperties(), metaData.getAttributes());
    setIdAttributeIndex(metaData.getIdAttributeIndex());
    METADATA_CACHE.put(instanceId, this);
  }

  public DataObjectMetaDataImpl(final DataObjectStore dataObjectStore,
    final DataObjectStoreSchema schema, final DataObjectMetaData metaData) {
    this(metaData);
    this.dataObjectStore = dataObjectStore;
    this.dataObjectFactory = dataObjectStore.getDataObjectFactory();
    this.schema = schema;
    METADATA_CACHE.put(instanceId, this);
  }

  public DataObjectMetaDataImpl(final DataObjectStore dataObjectStore,
    final DataObjectStoreSchema schema, final String typePath) {
    this(typePath);
    this.dataObjectStore = dataObjectStore;
    this.dataObjectFactory = dataObjectStore.getDataObjectFactory();
    this.schema = schema;
    METADATA_CACHE.put(instanceId, this);
  }

  public DataObjectMetaDataImpl(final String name) {
    this.path = name;
    METADATA_CACHE.put(instanceId, this);
  }

  public DataObjectMetaDataImpl(final String name,
    final Attribute... attributes) {
    this(name, null, attributes);
  }

  public DataObjectMetaDataImpl(final String name,
    final List<Attribute> attributes) {
    this(name, null, attributes);
  }

  public DataObjectMetaDataImpl(final String name,
    final Map<String, Object> properties, final Attribute... attributes) {
    this(name, properties, Arrays.asList(attributes));
  }

  public DataObjectMetaDataImpl(final String name,
    final Map<String, Object> properties, final List<Attribute> attributes) {
    this.path = name;
    for (final Attribute attribute : attributes) {
      addAttribute(attribute.clone());
    }
    cloneProperties(properties);
    METADATA_CACHE.put(instanceId, this);
  }

  public void addAttribute(final Attribute attribute) {
    final int index = attributeNames.size();
    final String name = attribute.getName();
    String lowerName;
    if (name == null) {
      lowerName = null;
    } else {
      lowerName = name.toLowerCase();

    }
    attributeNames.add(name);
    attributes.add(attribute);
    attributeMap.put(lowerName, attribute);
    attributeIdMap.put(lowerName, attributeIdMap.size());
    final DataType dataType = attribute.getType();
    if (dataType == null) {
      LoggerFactory.getLogger(getClass()).debug(attribute.toString());
    } else {
      final Class<?> dataClass = dataType.getJavaClass();
      if (Geometry.class.isAssignableFrom(dataClass)) {
        geometryAttributeIndexes.add(index);
        geometryAttributeNames.add(name);
        if (geometryAttributeIndex == -1) {
          geometryAttributeIndex = index;
        }
      }
    }
    attribute.setIndex(index);
    attribute.setMetaData(this);
  }

  /**
   * Adds an attribute with the given case-sensitive name.
   * 
   * @throws AssertionFailedException if a second Geometry is being added
   */
  public Attribute addAttribute(final String attributeName, final DataType type) {
    return addAttribute(attributeName, type, false);
  }

  public Attribute addAttribute(final String name, final DataType type,
    final boolean required) {
    final Attribute attribute = new Attribute(name, type, required);
    addAttribute(attribute);
    return attribute;
  }

  public Attribute addAttribute(final String name, final DataType type,
    final int length, final boolean required) {
    final Attribute attribute = new Attribute(name, type, length, required);
    addAttribute(attribute);
    return attribute;
  }

  public Attribute addAttribute(final String name, final DataType type,
    final int length, final int scale, final boolean required) {
    final Attribute attribute = new Attribute(name, type, length, scale,
      required);
    addAttribute(attribute);
    return attribute;
  }

  public void addColumnCodeTable(final String column, final CodeTable codeTable) {
    codeTableByColumnMap.put(column, codeTable);
  }

  @Override
  public void addDefaultValue(final String attributeName,
    final Object defaultValue) {
    defaultValues.put(attributeName, defaultValue);
  }

  public void addRestriction(final String attributePath,
    final Collection<Object> values) {
    restrictions.put(attributePath, values);
  }

  public void addSuperClass(final DataObjectMetaData superClass) {
    if (!superClasses.contains(superClass)) {
      superClasses.add(superClass);
    }
  }

  @Override
  public DataObjectMetaDataImpl clone() {
    final DataObjectMetaDataImpl clone = new DataObjectMetaDataImpl(path,
      getProperties(), attributes);
    clone.setIdAttributeIndex(idAttributeIndex);
    clone.setProperties(getProperties());
    return clone;
  }

  public void cloneProperties(final Map<String, Object> properties) {
    if (properties != null) {
      for (final Entry<String, Object> property : properties.entrySet()) {
        final String propertyName = property.getKey();
        if (property instanceof DataObjectMetaDataProperty) {
          DataObjectMetaDataProperty metaDataProperty = (DataObjectMetaDataProperty)property;
          metaDataProperty = metaDataProperty.clone();
          metaDataProperty.setMetaData(this);
          setProperty(propertyName, metaDataProperty);
        } else {
          setProperty(propertyName, property);
        }
      }
    }
  }

  @Override
  public int compareTo(final DataObjectMetaData other) {
    final String otherPath = other.getPath();
    if (otherPath == path) {
      return 0;
    } else if (path == null) {
      return 1;
    } else if (otherPath == null) {
      return -1;
    } else {
      return path.compareTo(otherPath);
    }
  }

  @Override
  public DataObject createDataObject() {
    return dataObjectFactory.createDataObject(this);
  }

  @Override
  public void delete(final DataObject dataObject) {
    if (dataObjectStore == null) {
      throw new UnsupportedOperationException();
    } else {
      dataObjectStore.delete(dataObject);
    }
  }

  @Override
  @PreDestroy
  public void destroy() {
    super.close();
    METADATA_CACHE.remove(instanceId);
    attributeIdMap = null;
    attributeMap = null;
    attributeNames = null;
    attributes = null;
    dataObjectFactory = null;
    dataObjectMetaDataFactory = null;
    dataObjectStore = null;
    defaultValues = null;
    geometryAttributeIndexes = null;
    geometryAttributeNames = null;
    path = null;
    restrictions = null;
    schema = null;
    superClasses = null;
  }

  @Override
  public boolean equals(final Object other) {
    return other == this;
  }

  @Override
  public Attribute getAttribute(final CharSequence name) {
    if (name == null) {
      return null;
    } else {
      final String lowerName = name.toString().toLowerCase();
      return attributeMap.get(lowerName);
    }
  }

  @Override
  public Attribute getAttribute(final int i) {
    return attributes.get(i);
  }

  @Override
  public Class<?> getAttributeClass(final CharSequence name) {
    final DataType dataType = getAttributeType(name);
    if (dataType == null) {
      return Object.class;
    } else {
      return dataType.getJavaClass();
    }
  }

  @Override
  public Class<?> getAttributeClass(final int i) {
    final DataType dataType = getAttributeType(i);
    if (dataType == null) {
      return Object.class;
    } else {
      return dataType.getJavaClass();
    }
  }

  @Override
  public int getAttributeCount() {
    return attributes.size();
  }

  @Override
  public int getAttributeIndex(final CharSequence name) {
    if (name == null) {
      return -1;
    } else {
      final String lowerName = name.toString().toLowerCase();
      final Integer attributeId = attributeIdMap.get(lowerName);
      if (attributeId == null) {
        return -1;
      } else {
        return attributeId;
      }
    }
  }

  @Override
  public int getAttributeLength(final int i) {
    try {
      final Attribute attribute = attributes.get(i);
      return attribute.getLength();
    } catch (final ArrayIndexOutOfBoundsException e) {
      throw e;
    }
  }

  @Override
  public String getAttributeName(final int i) {
    try {
      if (i == -1) {
        return null;
      } else {
        final Attribute attribute = attributes.get(i);
        return attribute.getName();
      }
    } catch (final ArrayIndexOutOfBoundsException e) {
      throw e;
    }
  }

  @Override
  public List<String> getAttributeNames() {
    return new ArrayList<String>(attributeNames);
  }

  @Override
  public List<Attribute> getAttributes() {
    return new ArrayList<Attribute>(attributes);
  }

  @Override
  public int getAttributeScale(final int i) {
    final Attribute attribute = attributes.get(i);
    return attribute.getScale();
  }

  @Override
  public String getAttributeTitle(final String fieldName) {
    final Attribute attribute = getAttribute(fieldName);
    if (attribute == null) {
      return CaseConverter.toCapitalizedWords(fieldName);
    } else {
      return attribute.getTitle();
    }
  }

  @Override
  public List<String> getAttributeTitles() {
    final List<String> titles = new ArrayList<String>();
    for (final Attribute attribute : getAttributes()) {
      titles.add(attribute.getTitle());
    }
    return titles;
  }

  @Override
  public DataType getAttributeType(final CharSequence name) {
    final int index = getAttributeIndex(name);
    if (index == -1) {
      return null;
    } else {
      return getAttributeType(index);
    }
  }

  @Override
  public DataType getAttributeType(final int i) {
    final Attribute attribute = attributes.get(i);
    return attribute.getType();
  }

  @Override
  public CodeTable getCodeTableByColumn(final String column) {
    CodeTable codeTable = codeTableByColumnMap.get(column);
    if (codeTable == null && dataObjectStore != null) {
      codeTable = dataObjectStore.getCodeTableByColumn(column);
    }
    return codeTable;
  }

  @Override
  public DataObjectFactory getDataObjectFactory() {
    return dataObjectFactory;
  }

  @Override
  public DataObjectMetaDataFactory getDataObjectMetaDataFactory() {
    if (dataObjectMetaDataFactory == null) {
      return dataObjectStore;
    } else {
      return dataObjectMetaDataFactory;
    }
  }

  @Override
  public DataObjectStore getDataObjectStore() {
    return dataObjectStore;
  }

  @Override
  public Object getDefaultValue(final String attributeName) {
    return defaultValues.get(attributeName);
  }

  @Override
  public Map<String, Object> getDefaultValues() {
    return defaultValues;
  }

  @Override
  public Attribute getGeometryAttribute() {
    if (geometryAttributeIndex == -1) {
      return null;
    } else {
      return attributes.get(geometryAttributeIndex);
    }
  }

  @Override
  public int getGeometryAttributeIndex() {
    return geometryAttributeIndex;
  }

  @Override
  public List<Integer> getGeometryAttributeIndexes() {
    return geometryAttributeIndexes;
  }

  @Override
  public String getGeometryAttributeName() {
    return getAttributeName(geometryAttributeIndex);
  }

  @Override
  public List<String> getGeometryAttributeNames() {
    return geometryAttributeNames;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    final Attribute geometryAttribute = getGeometryAttribute();
    if (geometryAttribute == null) {
      return null;
    } else {
      final GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
      return geometryFactory;
    }
  }

  @Override
  public Attribute getIdAttribute() {
    if (idAttributeIndex >= 0) {
      return attributes.get(idAttributeIndex);
    } else {
      return null;
    }
  }

  @Override
  public int getIdAttributeIndex() {
    return idAttributeIndex;
  }

  @Override
  public String getIdAttributeName() {
    return getAttributeName(idAttributeIndex);
  }

  @Override
  public int getInstanceId() {
    return instanceId;
  }

  @Override
  public String getPath() {
    return path;
  }

  public Map<String, Collection<Object>> getRestrictions() {
    return restrictions;
  }

  public DataObjectStoreSchema getSchema() {
    return schema;
  }

  @Override
  public String getTypeName() {
    return PathUtil.getName(path);
  }

  @Override
  public boolean hasAttribute(final CharSequence name) {
    final String lowerName = name.toString().toLowerCase();
    return attributeMap.containsKey(lowerName);
  }

  @Override
  public int hashCode() {
    if (path == null) {
      return super.hashCode();
    } else {
      return path.hashCode();
    }
  }

  @Override
  public boolean isAttributeRequired(final CharSequence name) {
    final Attribute attribute = getAttribute(name);
    return attribute.isRequired();
  }

  @Override
  public boolean isAttributeRequired(final int i) {
    final Attribute attribute = getAttribute(i);
    return attribute.isRequired();
  }

  @Override
  public boolean isInstanceOf(final DataObjectMetaData classDefinition) {
    if (classDefinition == null) {
      return false;
    }
    if (equals(classDefinition)) {
      return true;
    }
    for (final DataObjectMetaData superClass : superClasses) {
      if (superClass.isInstanceOf(classDefinition)) {
        return true;
      }
    }
    return false;
  }

  private void readObject(final ObjectInputStream ois)
    throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
    METADATA_CACHE.put(instanceId, this);
  }

  public void replaceAttribute(final Attribute attribute,
    final Attribute newAttribute) {
    final String name = attribute.getName();
    final String lowerName = name.toLowerCase();
    final String newName = newAttribute.getName();
    if (attributes.contains(attribute) && name.equals(newName)) {
      final int index = attribute.getIndex();
      attributes.set(index, newAttribute);
      attributeMap.put(lowerName, newAttribute);
      newAttribute.setIndex(index);
    } else {
      addAttribute(newAttribute);
    }
  }

  public void setCodeTableByColumnMap(
    final Map<String, CodeTable> codeTableByColumnMap) {
    this.codeTableByColumnMap = codeTableByColumnMap;
  }

  public void setDataObjectMetaDataFactory(
    final DataObjectMetaDataFactory dataObjectMetaDataFactory) {
    this.dataObjectMetaDataFactory = dataObjectMetaDataFactory;
  }

  @Override
  public void setDefaultValues(final Map<String, ? extends Object> defaultValues) {
    this.defaultValues = new HashMap<String, Object>(defaultValues);
  }

  /**
   * @param geometryAttributeIndex the geometryAttributeIndex to set
   */
  public void setGeometryAttributeIndex(final int geometryAttributeIndex) {
    this.geometryAttributeIndex = geometryAttributeIndex;
  }

  public void setGeometryAttributeName(final String name) {
    final int id = getAttributeIndex(name);
    setGeometryAttributeIndex(id);
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    final Attribute geometryAttribute = getGeometryAttribute();
    if (geometryAttribute != null) {
      geometryAttribute.setProperty(AttributeProperties.GEOMETRY_FACTORY,
        geometryFactory);
    }
  }

  /**
   * @param idAttributeIndex the idAttributeIndex to set
   */
  public void setIdAttributeIndex(final int idAttributeIndex) {
    this.idAttributeIndex = idAttributeIndex;
  }

  public void setIdAttributeName(final String name) {
    final int id = getAttributeIndex(name);
    setIdAttributeIndex(id);
  }

  @Override
  public void setName(final String path) {
    this.path = path;
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    if (properties != null) {
      for (final Entry<String, ? extends Object> entry : properties.entrySet()) {
        final String key = entry.getKey();
        final Object value = entry.getValue();
        if (value instanceof ValueMetaDataProperty) {
          final ValueMetaDataProperty valueProperty = (ValueMetaDataProperty)value;
          final String propertyName = valueProperty.getPropertyName();
          final Object propertyValue = valueProperty.getValue();
          JavaBeanUtil.setProperty(this, propertyName, propertyValue);
        }
        if (value instanceof DataObjectMetaDataProperty) {
          final DataObjectMetaDataProperty property = (DataObjectMetaDataProperty)value;
          final DataObjectMetaDataProperty clonedProperty = property.clone();
          clonedProperty.setMetaData(this);
        } else {
          setProperty(key, value);
        }
      }
    }

  }

  @Override
  public String toString() {
    return path.toString();
  }
}
