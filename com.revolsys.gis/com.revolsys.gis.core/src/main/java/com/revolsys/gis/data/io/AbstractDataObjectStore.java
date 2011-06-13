package com.revolsys.gis.data.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.io.AbstractObjectWithProperties;
import com.vividsolutions.jts.geom.Envelope;

public abstract class AbstractDataObjectStore extends
  AbstractObjectWithProperties implements DataObjectStore {

  private DataObjectFactory dataObjectFactory;

  private Map<String, DataObjectStoreSchema> schemaMap = new TreeMap<String, DataObjectStoreSchema>();

  public AbstractDataObjectStore() {
    this(new ArrayDataObjectFactory());
  }

  public AbstractDataObjectStore(final DataObjectFactory dataObjectFactory) {
    this.dataObjectFactory = dataObjectFactory;
  }

  @PreDestroy
  public void close() {
  }

  public DataObject create(final QName typeName) {
    final DataObjectMetaData metaData = getMetaData(typeName);
    if (metaData == null) {
      return null;
    } else {
      return dataObjectFactory.createDataObject(metaData);
    }
  }

  public void delete(final DataObject object) {
    throw new UnsupportedOperationException("Delete not supported");
  }

  public void deleteAll(final Collection<DataObject> objects) {
    for (final DataObject object : objects) {
      delete(object);
    }
  }

  protected DataObjectMetaData findMetaData(final QName typeName) {
    final String schemaName = typeName.getNamespaceURI();
    final DataObjectStoreSchema schema = getSchema(schemaName);
    if (schema == null) {
      return null;
    } else {
      return schema.findMetaData(typeName);
    }
  }

  public CodeTable getCodeTable(final QName typeName) {
    return null;
  }

  public CodeTable getCodeTableByColumn(final String columnName) {
    return null;
  }

  public DataObjectFactory getDataObjectFactory() {
    return this.dataObjectFactory;
  }

  public DataObjectMetaData getMetaData(final QName typeName) {
    final String schemaName = typeName.getNamespaceURI();
    final DataObjectStoreSchema schema = getSchema(schemaName);
    if (schema == null) {
      return null;
    } else {
      return schema.getMetaData(typeName);
    }
  }

  public DataObjectStoreSchema getSchema(final String schemaName) {
    synchronized (schemaMap) {
      if (schemaMap.isEmpty()) {
        loadSchemas(schemaMap);
      }
      return schemaMap.get(schemaName);
    }
  }

  public Map<String, DataObjectStoreSchema> getSchemaMap() {
    return schemaMap;
  }

  public List<DataObjectStoreSchema> getSchemas() {
    synchronized (schemaMap) {
      if (schemaMap.isEmpty()) {
        loadSchemas(schemaMap);
      }
      return new ArrayList<DataObjectStoreSchema>(schemaMap.values());
    }
  }

  public List<QName> getTypeNames(final String schemaName) {
    final DataObjectStoreSchema schema = getSchema(schemaName);
    return schema.getTypeNames();
  }

  public List<DataObjectMetaData> getTypes(final String namespace) {
    final List<DataObjectMetaData> types = new ArrayList<DataObjectMetaData>();
    for (final QName typeName : getTypeNames(namespace)) {
      types.add(getMetaData(typeName));
    }
    return types;
  }

  public void insert(final DataObject dataObject) {
    throw new UnsupportedOperationException("Insert not supported");
  }

  public void insertAll(final Collection<DataObject> objects) {
    for (final DataObject object : objects) {
      insert(object);
    }
  }

  public boolean isEditable(final QName typeName) {
    return false;
  }

  public DataObject load(final QName typeName, final Object id) {
    throw new UnsupportedOperationException("Load not supported");
  }

  protected abstract void loadSchemaDataObjectMetaData(
    DataObjectStoreSchema schema, Map<QName, DataObjectMetaData> metaDataMap);

  protected abstract void loadSchemas(
    Map<String, DataObjectStoreSchema> schemaMap);

  public Reader<DataObject> query(final QName typeName,
    final BoundingBox boundingBox) {
    final DataObjectMetaData metaData = getMetaData(typeName);
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    final GeometryFactory geometryFactory = geometryAttribute.getProperty(AttributeProperties.GEOMETRY_FACTORY);
    final Envelope envelope = boundingBox.convert(geometryFactory);
    return query(typeName, envelope);
  }

  protected void refreshMetaData(final String schemaName) {
    final DataObjectStoreSchema schema = getSchema(schemaName);
    if (schema != null) {
      schema.refreshMetaData();
    }
  }

  public void setDataObjectFactory(final DataObjectFactory dataObjectFactory) {
    this.dataObjectFactory = dataObjectFactory;
  }

  public void setSchemaMap(final Map<String, DataObjectStoreSchema> schemaMap) {
    this.schemaMap = new DataObjectStoreSchemaMapProxy(this, schemaMap);
  }

  public void update(final DataObject object) {
    throw new UnsupportedOperationException("Update not supported");
  }

  public void updateAll(final Collection<DataObject> objects) {
    for (final DataObject object : objects) {
      update(object);
    }
  }
}
