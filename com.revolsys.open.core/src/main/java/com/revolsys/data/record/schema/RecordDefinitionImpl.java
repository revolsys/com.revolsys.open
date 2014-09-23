package com.revolsys.data.record.schema;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;

import org.slf4j.LoggerFactory;

import com.revolsys.collection.WeakCache;
import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.property.AttributeProperties;
import com.revolsys.data.record.property.RecordDefinitionProperty;
import com.revolsys.data.record.property.ValueRecordDefinitionProperty;
import com.revolsys.data.types.DataType;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.JavaBeanUtil;

public class RecordDefinitionImpl extends AbstractRecordStoreSchemaElement
  implements RecordDefinition {
  public static RecordDefinitionImpl create(final Map<String, Object> properties) {
    return new RecordDefinitionImpl(properties);
  }

  public static void destroy(final RecordDefinitionImpl... recordDefinitionList) {
    for (final RecordDefinitionImpl recordDefinition : recordDefinitionList) {
      recordDefinition.destroy();
    }
  }

  public static RecordDefinition getRecordDefinition(final int instanceId) {
    return RECORD_DEFINITION_CACHE.get(instanceId);
  }

  private static final AtomicInteger INSTANCE_IDS = new AtomicInteger(0);

  private static final Map<Integer, RecordDefinitionImpl> RECORD_DEFINITION_CACHE = new WeakCache<Integer, RecordDefinitionImpl>();

  private final Map<String, Integer> attributeIdMap = new HashMap<String, Integer>();

  private final Map<String, Attribute> attributeMap = new HashMap<String, Attribute>();

  private final List<String> attributeNames = new ArrayList<String>();

  private final List<Attribute> attributes = new ArrayList<Attribute>();

  private Map<String, CodeTable> codeTableByColumnMap = new HashMap<String, CodeTable>();

  private RecordFactory recordFactory = new ArrayRecordFactory();

  private RecordDefinitionFactory recordDefinitionFactory;

  private Map<String, Object> defaultValues = new HashMap<String, Object>();

  /** The index of the primary geometry attribute. */
  private int geometryAttributeIndex = -1;

  private final List<Integer> geometryAttributeIndexes = new ArrayList<>();

  private final List<String> geometryAttributeNames = new ArrayList<>();

  private final List<Integer> idAttributeIndexes = new ArrayList<>();

  private final List<String> idAttributeNames = new ArrayList<>();

  private final List<Attribute> idAttributes = new ArrayList<>();

  /** The index of the ID attribute. */
  private int idAttributeIndex = -1;

  private final Integer instanceId = INSTANCE_IDS.getAndIncrement();

  private final Map<String, Collection<Object>> restrictions = new HashMap<String, Collection<Object>>();

  private final List<RecordDefinition> superClasses = new ArrayList<RecordDefinition>();

  private String description;

  public RecordDefinitionImpl() {
  }

  @SuppressWarnings("unchecked")
  public RecordDefinitionImpl(final Map<String, Object> properties) {
    this(CollectionUtil.getString(properties, "path"));
    final List<Object> fields = (List<Object>)properties.get("fields");
    for (final Object object : fields) {
      if (object instanceof Attribute) {
        final Attribute field = (Attribute)object;
        addAttribute(field.clone());
      } else if (object instanceof Map) {
        final Map<String, Object> fieldProperties = (Map<String, Object>)object;
        final Attribute field = Attribute.create(fieldProperties);
        addAttribute(field);
      }
    }
    final Map<String, Object> geometryFactoryDef = (Map<String, Object>)properties.get("geometryFactory");
    if (geometryFactoryDef != null) {
      final GeometryFactory geometryFactory = MapObjectFactoryRegistry.toObject(geometryFactoryDef);
      setGeometryFactory(geometryFactory);
    }
  }

  public RecordDefinitionImpl(final RecordDefinition recordDefinition) {
    this(recordDefinition.getPath(), recordDefinition.getProperties(),
      recordDefinition.getAttributes());
    setIdAttributeIndex(recordDefinition.getIdAttributeIndex());
    RECORD_DEFINITION_CACHE.put(this.instanceId, this);
  }

  public RecordDefinitionImpl(final RecordStoreSchema schema,
    final RecordDefinition recordDefinition) {
    this(schema, recordDefinition.getPath());
    for (final Attribute attribute : recordDefinition.getAttributes()) {
      addAttribute(attribute.clone());
    }
    cloneProperties(recordDefinition.getProperties());
  }

  public RecordDefinitionImpl(final RecordStoreSchema schema, final String path) {
    super(schema, path);
    final RecordStore recordStore = getRecordStore();
    if (recordStore != null) {
      this.recordFactory = recordStore.getRecordFactory();
    }
    RECORD_DEFINITION_CACHE.put(this.instanceId, this);
  }

  public RecordDefinitionImpl(final RecordStoreSchema schema,
    final String path, final Map<String, Object> properties,
    final List<Attribute> attributes) {
    this(schema, path);
    for (final Attribute attribute : attributes) {
      addAttribute(attribute.clone());
    }
    cloneProperties(properties);
  }

  public RecordDefinitionImpl(final String path) {
    super(path);
    RECORD_DEFINITION_CACHE.put(this.instanceId, this);
  }

  public RecordDefinitionImpl(final String path, final Attribute... attributes) {
    this(path, null, attributes);
  }

  public RecordDefinitionImpl(final String path,
    final List<Attribute> attributes) {
    this(path, null, attributes);
  }

  public RecordDefinitionImpl(final String path,
    final Map<String, Object> properties, final Attribute... attributes) {
    this(path, properties, Arrays.asList(attributes));
  }

  public RecordDefinitionImpl(final String path,
    final Map<String, Object> properties, final List<Attribute> attributes) {
    super(path);
    for (final Attribute attribute : attributes) {
      addAttribute(attribute.clone());
    }
    cloneProperties(properties);
    RECORD_DEFINITION_CACHE.put(this.instanceId, this);
  }

  public void addAttribute(final Attribute attribute) {
    final int index = this.attributeNames.size();
    final String name = attribute.getName();
    String lowerName;
    if (name == null) {
      lowerName = null;
    } else {
      lowerName = name.toLowerCase();

    }
    this.attributeNames.add(name);
    this.attributes.add(attribute);
    this.attributeMap.put(lowerName, attribute);
    this.attributeIdMap.put(lowerName, this.attributeIdMap.size());
    final DataType dataType = attribute.getType();
    if (dataType == null) {
      LoggerFactory.getLogger(getClass()).debug(attribute.toString());
    } else {
      final Class<?> dataClass = dataType.getJavaClass();
      if (Geometry.class.isAssignableFrom(dataClass)) {
        this.geometryAttributeIndexes.add(index);
        this.geometryAttributeNames.add(name);
        if (this.geometryAttributeIndex == -1) {
          this.geometryAttributeIndex = index;
        }
      }
    }
    attribute.setIndex(index);
    attribute.setRecordDefinition(this);
  }

  /**
   * Adds an attribute with the given case-sensitive name.
   *
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
    this.codeTableByColumnMap.put(column, codeTable);
  }

  @Override
  public void addDefaultValue(final String attributeName,
    final Object defaultValue) {
    this.defaultValues.put(attributeName, defaultValue);
  }

  public void addRestriction(final String attributePath,
    final Collection<Object> values) {
    this.restrictions.put(attributePath, values);
  }

  public void addSuperClass(final RecordDefinition superClass) {
    if (!this.superClasses.contains(superClass)) {
      this.superClasses.add(superClass);
    }
  }

  public void cloneProperties(final Map<String, Object> properties) {
    if (properties != null) {
      for (final Entry<String, Object> property : properties.entrySet()) {
        final String propertyName = property.getKey();
        if (property instanceof RecordDefinitionProperty) {
          RecordDefinitionProperty recordDefinitionProperty = (RecordDefinitionProperty)property;
          recordDefinitionProperty = recordDefinitionProperty.clone();
          recordDefinitionProperty.setRecordDefinition(this);
          setProperty(propertyName, recordDefinitionProperty);
        } else {
          setProperty(propertyName, property);
        }
      }
    }
  }

  @Override
  public Record createRecord() {
    final RecordFactory recordFactory = this.recordFactory;
    if (recordFactory == null) {
      return null;
    } else {
      return recordFactory.createRecord(this);
    }
  }

  @Override
  public void delete(final Record record) {
    final RecordStore recordStore = getRecordStore();
    if (recordStore == null) {
      throw new UnsupportedOperationException();
    } else {
      recordStore.delete(record);
    }
  }

  @Override
  @PreDestroy
  public void destroy() {
    super.close();
    RECORD_DEFINITION_CACHE.remove(this.instanceId);
    this.attributeIdMap.clear();
    this.attributeMap.clear();
    this.attributeNames.clear();
    this.attributes.clear();
    this.codeTableByColumnMap.clear();
    this.recordFactory = null;
    this.recordDefinitionFactory = new RecordDefinitionFactoryImpl();
    this.defaultValues.clear();
    this.description = "";
    this.geometryAttributeIndex = -1;
    this.geometryAttributeIndexes.clear();
    this.geometryAttributeNames.clear();
    this.restrictions.clear();
    this.superClasses.clear();
  }

  @Override
  public Attribute getAttribute(final CharSequence name) {
    if (name == null) {
      return null;
    } else {
      final String lowerName = name.toString().toLowerCase();
      return this.attributeMap.get(lowerName);
    }
  }

  @Override
  public Attribute getAttribute(final int i) {
    return this.attributes.get(i);
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
    return this.attributes.size();
  }

  @Override
  public int getAttributeIndex(final CharSequence name) {
    if (name == null) {
      return -1;
    } else {
      final String lowerName = name.toString().toLowerCase();
      final Integer attributeId = this.attributeIdMap.get(lowerName);
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
      final Attribute attribute = this.attributes.get(i);
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
      } else if (this.attributes == null) {
        return null;
      } else {
        final Attribute attribute = this.attributes.get(i);
        return attribute.getName();
      }
    } catch (final ArrayIndexOutOfBoundsException e) {
      throw e;
    }
  }

  @Override
  public List<String> getAttributeNames() {
    return new ArrayList<String>(this.attributeNames);
  }

  @Override
  public List<Attribute> getAttributes() {
    return new ArrayList<Attribute>(this.attributes);
  }

  @Override
  public int getAttributeScale(final int i) {
    final Attribute attribute = this.attributes.get(i);
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
    final Attribute attribute = this.attributes.get(i);
    return attribute.getType();
  }

  @Override
  public CodeTable getCodeTableByColumn(final String column) {
    final RecordStore recordStore = getRecordStore();
    if (recordStore == null) {
      return null;
    } else {
      CodeTable codeTable = this.codeTableByColumnMap.get(column);
      if (codeTable == null && recordStore != null) {
        codeTable = recordStore.getCodeTableByColumn(column);
      }
      return codeTable;
    }
  }

  @Override
  public Object getDefaultValue(final String attributeName) {
    return this.defaultValues.get(attributeName);
  }

  @Override
  public Map<String, Object> getDefaultValues() {
    return this.defaultValues;
  }

  public String getDescription() {
    return this.description;
  }

  @Override
  public Attribute getGeometryAttribute() {
    if (this.geometryAttributeIndex == -1) {
      return null;
    } else {
      return this.attributes.get(this.geometryAttributeIndex);
    }
  }

  @Override
  public int getGeometryAttributeIndex() {
    return this.geometryAttributeIndex;
  }

  @Override
  public List<Integer> getGeometryAttributeIndexes() {
    return Collections.unmodifiableList(this.geometryAttributeIndexes);
  }

  @Override
  public String getGeometryAttributeName() {
    return getAttributeName(this.geometryAttributeIndex);
  }

  @Override
  public List<String> getGeometryAttributeNames() {
    return Collections.unmodifiableList(this.geometryAttributeNames);
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
    if (this.idAttributeIndex >= 0) {
      return this.attributes.get(this.idAttributeIndex);
    } else {
      return null;
    }
  }

  @Override
  public int getIdAttributeIndex() {
    return this.idAttributeIndex;
  }

  @Override
  public List<Integer> getIdAttributeIndexes() {
    return Collections.unmodifiableList(this.idAttributeIndexes);
  }

  @Override
  public String getIdAttributeName() {
    return getAttributeName(this.idAttributeIndex);
  }

  @Override
  public List<String> getIdAttributeNames() {
    return Collections.unmodifiableList(this.idAttributeNames);
  }

  @Override
  public List<Attribute> getIdAttributes() {
    return Collections.unmodifiableList(this.idAttributes);
  }

  @Override
  public int getInstanceId() {
    return this.instanceId;
  }

  @Override
  public RecordDefinitionFactory getRecordDefinitionFactory() {
    if (this.recordDefinitionFactory == null) {
      final RecordStore recordStore = getRecordStore();
      return recordStore;
    } else {
      return this.recordDefinitionFactory;
    }
  }

  @Override
  public RecordFactory getRecordFactory() {
    return this.recordFactory;
  }

  public Map<String, Collection<Object>> getRestrictions() {
    return this.restrictions;
  }

  @Override
  public boolean hasAttribute(final CharSequence name) {
    final String lowerName = name.toString().toLowerCase();
    return this.attributeMap.containsKey(lowerName);
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
  public boolean isInstanceOf(final RecordDefinition classDefinition) {
    if (classDefinition == null) {
      return false;
    }
    if (equals(classDefinition)) {
      return true;
    }
    for (final RecordDefinition superClass : this.superClasses) {
      if (superClass.isInstanceOf(classDefinition)) {
        return true;
      }
    }
    return false;
  }

  private void readObject(final ObjectInputStream ois)
    throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
    RECORD_DEFINITION_CACHE.put(this.instanceId, this);
  }

  public RecordDefinitionImpl rename(final String path) {
    final RecordDefinitionImpl clone = new RecordDefinitionImpl(path,
      getProperties(), this.attributes);
    clone.setIdAttributeIndex(this.idAttributeIndex);
    clone.setProperties(getProperties());
    return clone;
  }

  public void replaceAttribute(final Attribute attribute,
    final Attribute newAttribute) {
    final String name = attribute.getName();
    final String lowerName = name.toLowerCase();
    final String newName = newAttribute.getName();
    if (this.attributes.contains(attribute) && name.equals(newName)) {
      final int index = attribute.getIndex();
      this.attributes.set(index, newAttribute);
      this.attributeMap.put(lowerName, newAttribute);
      newAttribute.setIndex(index);
    } else {
      addAttribute(newAttribute);
    }
  }

  public void setCodeTableByColumnMap(
    final Map<String, CodeTable> codeTableByColumnMap) {
    this.codeTableByColumnMap = codeTableByColumnMap;
  }

  @Override
  public void setDefaultValues(final Map<String, ? extends Object> defaultValues) {
    if (defaultValues == null) {
      this.defaultValues = new HashMap<>();
    } else {
      this.defaultValues = new HashMap<>(defaultValues);
    }
  }

  public void setDescription(final String description) {
    this.description = description;
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
    this.idAttributeIndexes.clear();
    this.idAttributeIndexes.add(idAttributeIndex);
    this.idAttributeNames.clear();
    this.idAttributeNames.add(getIdAttributeName());
    this.idAttributes.clear();
    this.idAttributes.add(getIdAttribute());
  }

  public void setIdAttributeName(final String name) {
    final int id = getAttributeIndex(name);
    setIdAttributeIndex(id);
  }

  public void setIdAttributeNames(final Collection<String> names) {
    if (names != null) {
      if (names.size() == 1) {
        final String name = CollectionUtil.get(names, 0);
        setIdAttributeName(name);
      } else {
        for (final String name : names) {
          final int index = getAttributeIndex(name);
          if (index == -1) {
            LoggerFactory.getLogger(getClass()).error(
              "Cannot set ID " + getPath() + "." + name + " does not exist");
          } else {
            this.idAttributeIndexes.add(index);
            this.idAttributeNames.add(name);
            this.idAttributes.add(getAttribute(index));
          }
        }
      }
    }
  }

  public void setIdAttributeNames(final String... names) {
    setIdAttributeNames(Arrays.asList(names));
  }

  @Override
  public void setProperties(final Map<String, ? extends Object> properties) {
    if (properties != null) {
      for (final Entry<String, ? extends Object> entry : properties.entrySet()) {
        final String key = entry.getKey();
        final Object value = entry.getValue();
        if (value instanceof ValueRecordDefinitionProperty) {
          final ValueRecordDefinitionProperty valueProperty = (ValueRecordDefinitionProperty)value;
          final String propertyName = valueProperty.getPropertyName();
          final Object propertyValue = valueProperty.getValue();
          JavaBeanUtil.setProperty(this, propertyName, propertyValue);
        }
        if (value instanceof RecordDefinitionProperty) {
          final RecordDefinitionProperty property = (RecordDefinitionProperty)value;
          final RecordDefinitionProperty clonedProperty = property.clone();
          clonedProperty.setRecordDefinition(this);
        } else {
          setProperty(key, value);
        }
      }
    }

  }

  public void setRecordDefinitionFactory(
    final RecordDefinitionFactory recordDefinitionFactory) {
    this.recordDefinitionFactory = recordDefinitionFactory;
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "dataRecordDefinition");
    final String path = getPath();
    map.put("path", path);
    final GeometryFactory geometryFactory = getGeometryFactory();
    MapSerializerUtil.add(map, "geometryFactory", geometryFactory, null);
    final List<Attribute> attributes = getAttributes();
    MapSerializerUtil.add(map, "fields", attributes);
    return map;
  }

}
