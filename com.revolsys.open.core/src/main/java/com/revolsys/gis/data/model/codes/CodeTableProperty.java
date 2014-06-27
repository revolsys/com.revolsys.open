package com.revolsys.gis.data.model.codes;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataProperty;
import com.revolsys.gis.data.model.ListRecordIdentifier;
import com.revolsys.gis.data.model.RecordIdentifier;
import com.revolsys.gis.data.model.SingleRecordIdentifier;
import com.revolsys.gis.data.model.comparator.DataObjectAttributeComparator;
import com.revolsys.gis.data.query.And;
import com.revolsys.gis.data.query.Q;
import com.revolsys.gis.data.query.Query;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;
import com.revolsys.util.Property;

public class CodeTableProperty extends AbstractCodeTable implements
DataObjectMetaDataProperty {

  public static final CodeTableProperty getProperty(
    final DataObjectMetaData metaData) {
    final CodeTableProperty property = metaData.getProperty(PROPERTY_NAME);
    return property;
  }

  private static final ArrayList<String> DEFAULT_ATTRIBUTE_NAMES = new ArrayList<String>(
      Arrays.asList("VALUE"));

  public static final String PROPERTY_NAME = CodeTableProperty.class.getName();

  private String creationTimestampAttributeName;

  private String modificationTimestampAttributeName;

  private List<String> attributeAliases = new ArrayList<String>();

  private DataObjectStore dataStore;

  private boolean loadAll = true;

  private DataObjectMetaData metaData;

  private List<String> valueAttributeNames = DEFAULT_ATTRIBUTE_NAMES;

  private List<String> orderBy = DEFAULT_ATTRIBUTE_NAMES;

  private String typePath;

  private String idAttributeName;

  private boolean createMissingCodes = true;

  private boolean loading = false;

  private boolean loadMissingCodes = true;

  private final ThreadLocal<Boolean> threadLoading = new ThreadLocal<Boolean>();

  public CodeTableProperty() {
  }

  public void addAttributeAlias(final String columnName) {
    this.attributeAliases.add(columnName);
  }

  public void addValue(final DataObject code) {
    final RecordIdentifier id = code.getIdentifier(getIdAttributeName());
    final List<Object> values = new ArrayList<Object>();
    for (final String attributeName : this.valueAttributeNames) {
      final Object value = code.getValue(attributeName);
      values.add(value);
    }
    addValue(id, values);
  }

  protected void addValues(final Iterable<DataObject> allCodes) {
    for (final DataObject code : allCodes) {
      addValue(code);
    }
  }

  @Override
  public CodeTableProperty clone() {
    final CodeTableProperty clone = (CodeTableProperty)super.clone();
    clone.metaData = null;
    clone.attributeAliases = new ArrayList<String>(this.attributeAliases);
    clone.valueAttributeNames = new ArrayList<String>(this.valueAttributeNames);
    return clone;
  }

  protected synchronized RecordIdentifier createId(final List<Object> values) {
    if (this.createMissingCodes) {
      // TODO prevent duplicates from other threads/processes
      final DataObject code = this.dataStore.create(this.typePath);
      final DataObjectMetaData metaData = code.getMetaData();
      Object id = this.dataStore.createPrimaryIdValue(this.typePath);
      if (id == null) {
        final Attribute idAttribute = metaData.getIdAttribute();
        if (idAttribute != null) {
          if (Number.class.isAssignableFrom(idAttribute.getType()
            .getJavaClass())) {
            id = getNextId();
          } else {
            id = UUID.randomUUID().toString();
          }
        }
      }
      code.setIdValue(id);
      for (int i = 0; i < this.valueAttributeNames.size(); i++) {
        final String name = this.valueAttributeNames.get(i);
        final Object value = values.get(i);
        code.setValue(name, value);
      }

      final Timestamp now = new Timestamp(System.currentTimeMillis());
      if (this.creationTimestampAttributeName != null) {
        code.setValue(this.creationTimestampAttributeName, now);
      }
      if (this.modificationTimestampAttributeName != null) {
        code.setValue(this.modificationTimestampAttributeName, now);
      }

      this.dataStore.insert(code);
      return code.getIdentifier();
    } else {
      return null;
    }
  }

  @Override
  public List<String> getAttributeAliases() {
    return this.attributeAliases;
  }

  @Override
  public Map<RecordIdentifier, List<Object>> getCodes() {
    final Map<RecordIdentifier, List<Object>> codes = super.getCodes();
    if (codes.isEmpty() && isLoadAll()) {
      loadAll();
      return super.getCodes();
    } else {
      return codes;
    }
  }

  public String getCreationTimestampAttributeName() {
    return this.creationTimestampAttributeName;
  }

  public DataObjectStore getDataStore() {
    return this.dataStore;
  }

  @Override
  public String getIdAttributeName() {
    if (StringUtils.hasText(this.idAttributeName)) {
      return this.idAttributeName;
    } else if (this.metaData == null) {
      return "";
    } else {
      final String idAttributeName = this.metaData.getIdAttributeName();
      if (StringUtils.hasText(idAttributeName)) {
        return idAttributeName;
      } else {
        return this.metaData.getAttributeName(0);
      }
    }
  }

  @Override
  public Map<String, ? extends Object> getMap(final RecordIdentifier id) {
    final List<Object> values = getValues(id);
    if (values == null) {
      return Collections.emptyMap();
    } else {
      final Map<String, Object> map = new HashMap<String, Object>();
      for (int i = 0; i < values.size(); i++) {
        final String name = this.valueAttributeNames.get(i);
        final Object value = values.get(i);
        map.put(name, value);
      }
      return map;
    }
  }

  @Override
  public DataObjectMetaData getMetaData() {
    return this.metaData;
  }

  public String getModificationTimestampAttributeName() {
    return this.modificationTimestampAttributeName;
  }

  @Override
  public String getPropertyName() {
    return PROPERTY_NAME;
  }

  public String getTypeName() {
    return this.typePath;
  }

  @Override
  public <V> V getValue(final Object id) {
    if (id instanceof RecordIdentifier) {
      return getValue((RecordIdentifier)id);
    } else if (id instanceof List) {
      final List list = (List)id;
      return getValue(new ListRecordIdentifier(list));
    } else {
      return getValue(SingleRecordIdentifier.create(id));
    }
  }

  @Override
  public List<String> getValueAttributeNames() {
    return this.valueAttributeNames;
  }

  public boolean isCreateMissingCodes() {
    return this.createMissingCodes;
  }

  public boolean isLoadAll() {
    return this.loadAll;
  }

  protected synchronized void loadAll() {
    if (this.threadLoading.get() != Boolean.TRUE) {
      if (this.loading) {
        while (this.loading) {
          try {
            wait(1000);
          } catch (final InterruptedException e) {
          }
        }
        return;
      } else {
        this.threadLoading.set(Boolean.TRUE);
        this.loading = true;
        try {
          final DataObjectMetaData metaData = this.dataStore.getMetaData(this.typePath);
          final Query query = new Query(this.typePath);
          query.setAttributeNames(metaData.getAttributeNames());
          for (final String order : this.orderBy) {
            query.addOrderBy(order, true);
          }
          try (
              Reader<DataObject> reader = this.dataStore.query(query)) {
            final List<DataObject> codes = reader.read();
            this.dataStore.getStatistics()
            .getStatistics("query")
            .add(this.typePath, -codes.size());
            Collections.sort(codes, new DataObjectAttributeComparator(
              this.orderBy));
            addValues(codes);
          }
          Property.firePropertyChange(this, "valuesChanged", false, true);
        } finally {
          this.loading = false;
          this.threadLoading.set(null);
        }
      }
    }
  }

  @Override
  protected synchronized RecordIdentifier loadId(final List<Object> values,
    final boolean createId) {
    if (this.loadAll && !this.loadMissingCodes && !isEmpty()) {
      return null;
    }
    RecordIdentifier id = null;
    if (createId && this.loadAll) {
      loadAll();
      id = getId(values, false);
    } else {
      final Query query = new Query(this.typePath);
      final And and = new And();
      if (!values.isEmpty()) {
        int i = 0;
        for (final String attributeName : this.valueAttributeNames) {
          final Object value = values.get(i);
          if (value == null) {
            and.add(Q.isNull(attributeName));
          } else {
            final Attribute attribute = this.metaData.getAttribute(attributeName);
            and.add(Q.equal(attribute, value));
          }
          i++;
        }
      }
      query.setWhereCondition(and);
      final Reader<DataObject> reader = this.dataStore.query(query);
      try {
        final List<DataObject> codes = reader.read();
        this.dataStore.getStatistics()
        .getStatistics("query")
        .add(this.typePath, -codes.size());
        addValues(codes);
        id = getIdByValue(values);
        Property.firePropertyChange(this, "valuesChanged", false, true);
      } finally {
        reader.close();
      }
    }
    if (createId && id == null) {
      return createId(values);
    } else {
      return id;
    }
  }

  @Override
  protected List<Object> loadValues(final Object id) {
    List<Object> values = null;
    if (this.loadAll) {
      loadAll();
      values = getValueById(id);
    } else {
      final DataObject code = this.dataStore.load(this.typePath, id);
      if (code != null) {
        addValue(code);
        values = getValueById(id);
      }
    }
    return values;
  }

  @Override
  public synchronized void refresh() {
    super.refresh();
    if (isLoadAll()) {
      loadAll();
    }
  }

  public void setAttributeAliases(final List<String> columnAliases) {
    this.attributeAliases = columnAliases;
  }

  public void setCreateMissingCodes(final boolean createMissingCodes) {
    this.createMissingCodes = createMissingCodes;
  }

  public void setCreationTimestampAttributeName(
    final String creationTimestampAttributeName) {
    this.creationTimestampAttributeName = creationTimestampAttributeName;
  }

  public void setIdAttributeName(final String idAttributeName) {
    this.idAttributeName = idAttributeName;
  }

  public void setLoadAll(final boolean loadAll) {
    this.loadAll = loadAll;
  }

  public void setLoadMissingCodes(final boolean loadMissingCodes) {
    this.loadMissingCodes = loadMissingCodes;
  }

  @Override
  public void setMetaData(final DataObjectMetaData metaData) {
    if (this.metaData != metaData) {
      if (this.metaData != null) {
        this.metaData.setProperty(getPropertyName(), null);
      }
      this.metaData = metaData;
      if (metaData == null) {
        this.dataStore = null;
        this.typePath = null;
      } else {
        this.typePath = metaData.getPath();
        setName(PathUtil.getName(this.typePath));
        this.dataStore = this.metaData.getDataStore();
        metaData.setProperty(getPropertyName(), this);
        this.dataStore.addCodeTable(this);
      }
    }
  }

  public void setModificationTimestampAttributeName(
    final String modificationTimestampAttributeName) {
    this.modificationTimestampAttributeName = modificationTimestampAttributeName;
  }

  public void setOrderBy(final List<String> orderBy) {
    this.orderBy = new ArrayList<String>(orderBy);
  }

  public void setValueAttributeName(final String valueColumns) {
    setValueAttributeNames(valueColumns);
  }

  public void setValueAttributeNames(final List<String> valueColumns) {
    this.valueAttributeNames = new ArrayList<String>(valueColumns);
    if (this.orderBy == DEFAULT_ATTRIBUTE_NAMES) {
      setOrderBy(valueColumns);
    }
  }

  public void setValueAttributeNames(final String... valueColumns) {
    setValueAttributeNames(Arrays.asList(valueColumns));
  }

  @Override
  public String toString() {
    return this.typePath + " " + getIdAttributeName() + " "
      + this.valueAttributeNames;

  }

  public String toString(final List<String> values) {
    final StringBuffer string = new StringBuffer(values.get(0));
    for (int i = 1; i < values.size(); i++) {
      final String value = values.get(i);
      string.append(",");
      string.append(value);
    }
    return string.toString();
  }
}
