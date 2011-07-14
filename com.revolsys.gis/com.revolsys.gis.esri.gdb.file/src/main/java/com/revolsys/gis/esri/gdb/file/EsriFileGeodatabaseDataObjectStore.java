package com.revolsys.gis.esri.gdb.file;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.data.io.AbstractDataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.io.IteratorReader;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.esri.gdb.file.swig.EsriFileGdb;
import com.revolsys.gis.esri.gdb.file.swig.Geodatabase;
import com.revolsys.gis.esri.gdb.file.swig.Table;
import com.revolsys.gis.esri.gdb.file.swig.VectorOfWString;
import com.revolsys.gis.esri.gdb.file.type.BinaryAttribute;
import com.revolsys.gis.esri.gdb.file.type.DateAttribute;
import com.revolsys.gis.esri.gdb.file.type.DoubleAttribute;
import com.revolsys.gis.esri.gdb.file.type.FloatAttribute;
import com.revolsys.gis.esri.gdb.file.type.GeometryAttribute;
import com.revolsys.gis.esri.gdb.file.type.GuidAttribute;
import com.revolsys.gis.esri.gdb.file.type.IntegerAttribute;
import com.revolsys.gis.esri.gdb.file.type.OidAttribute;
import com.revolsys.gis.esri.gdb.file.type.ShortAttribute;
import com.revolsys.gis.esri.gdb.file.type.StringAttribute;
import com.revolsys.gis.esri.gdb.file.type.XmlAttribute;
import com.revolsys.gis.esri.gdb.xml.model.DEFeatureClass;
import com.revolsys.gis.esri.gdb.xml.model.DEFeatureDataset;
import com.revolsys.gis.esri.gdb.xml.model.DETable;
import com.revolsys.gis.esri.gdb.xml.model.EsriGdbXmlParser;
import com.revolsys.gis.esri.gdb.xml.model.EsriXmlDataObjectMetaDataUtil;
import com.revolsys.gis.esri.gdb.xml.model.Field;
import com.revolsys.gis.esri.gdb.xml.model.Serializer;
import com.revolsys.io.Writer;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.xml.io.XmlProcessor;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class EsriFileGeodatabaseDataObjectStore extends AbstractDataObjectStore {

  private static final Logger LOG = LoggerFactory.getLogger(EsriFileGeodatabaseDataObjectStore.class);

  private Geodatabase geodatabase;

  private String fileName;

  private boolean createMissingGeodatabase = false;

  private boolean createMissingTables;

  public boolean isCreateMissingGeodatabase() {
    return createMissingGeodatabase;
  }

  public void setCreateMissingGeodatabase(boolean createMissingGeodatabase) {
    this.createMissingGeodatabase = createMissingGeodatabase;
  }

  private static final Map<String, Constructor<? extends Attribute>> ESRI_FIELD_TYPE_ATTRIBUTE_MAP = new HashMap<String, Constructor<? extends Attribute>>();

  static {
    addFieldTypeAttributeConstructor("esriFieldTypeInteger",
      IntegerAttribute.class);
    addFieldTypeAttributeConstructor("esriFieldTypeSmallInteger",
      ShortAttribute.class);
    addFieldTypeAttributeConstructor("esriFieldTypeDouble",
      DoubleAttribute.class);
    addFieldTypeAttributeConstructor("esriFieldTypeSingle",
      FloatAttribute.class);
    addFieldTypeAttributeConstructor("esriFieldTypeString",
      StringAttribute.class);
    addFieldTypeAttributeConstructor("esriFieldTypeDate", DateAttribute.class);
    addFieldTypeAttributeConstructor("esriFieldTypeGeometry",
      GeometryAttribute.class);
    addFieldTypeAttributeConstructor("esriFieldTypeOID", OidAttribute.class);
    addFieldTypeAttributeConstructor("esriFieldTypeBlob", BinaryAttribute.class);
    addFieldTypeAttributeConstructor("esriFieldTypeGlobalID",
      GuidAttribute.class);
    addFieldTypeAttributeConstructor("esriFieldTypeGUID", GuidAttribute.class);
    addFieldTypeAttributeConstructor("esriFieldTypeXML", XmlAttribute.class);

  }

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\?");

  private static void addFieldTypeAttributeConstructor(
    final String esriTypeName, final Class<? extends Attribute> attributeClass) {
    try {
      final Constructor<? extends Attribute> constructor = attributeClass.getConstructor(Field.class);
      ESRI_FIELD_TYPE_ATTRIBUTE_MAP.put(esriTypeName, constructor);
    } catch (final SecurityException e) {
      LOG.error("No public constructor for ESRI type " + esriTypeName, e);
    } catch (final NoSuchMethodException e) {
      LOG.error("No public constructor for ESRI type " + esriTypeName, e);
    }

  }

  public EsriFileGeodatabaseDataObjectStore() {
  }

  public EsriFileGeodatabaseDataObjectStore(final String fileName) {
    this.fileName = fileName;
  }

  public void addChildSchema(final String path) {
    VectorOfWString childDatasets = geodatabase.getChildDatasets(path,
      "Feature Dataset");
    for (int i = 0; i < childDatasets.size(); i++) {
      final String childPath = childDatasets.get(i);
      addFeatureDatasetSchema(childPath);
    }
  }

  private void addFeatureDatasetSchema(final String path) {
    final String schemaName = path.substring(1);
    final DataObjectStoreSchema schema = new DataObjectStoreSchema(this,
      schemaName);
    addSchema(schema);
    addChildSchema(path);
  }

  protected Geodatabase getGeodatabase() {
    return geodatabase;
  }

  protected void closeTable(Table table) {
    try {
      geodatabase.CloseTable(table);
    } catch (Throwable e) {
      LOG.error("Unable to close table", e);
    }
  }

  public Writer<DataObject> createWriter() {
    return new EsriFileGeodatabaseWriter(this);
  }

  @Override
  public void delete(final DataObject object) {
    if (object.getState() == DataObjectState.Persisted
      || object.getState() == DataObjectState.Modified) {
      object.setState(DataObjectState.Deleted);
      final Writer<DataObject> writer = getWriter();
      writer.write(object);
    }
  }

  @PreDestroy
  public void close() {
    try {
      if (geodatabase != null) {
        EsriFileGdb.CloseGeodatabase(geodatabase);
      }
    } finally {
      geodatabase = null;
    }
  }

  public String getFileName() {
    return fileName;
  }

  protected Table getTable(final QName typeName) {
    final String path = "\\\\" + typeName.getNamespaceURI() + "\\"
      + typeName.getLocalPart();
    return geodatabase.openTable(path);
  }

  public synchronized Writer<DataObject> getWriter() {
    Writer<DataObject> writer = getSharedAttribute("writer");
    if (writer == null) {
      writer = createWriter();
      setSharedAttribute("writer", writer);
    }
    return writer;
  }

  @PostConstruct
  public void initialize() {
    System.loadLibrary("EsriFileGdb");
    final File file = new File(fileName);
    if (file.exists()) {
      if (file.isDirectory()) {
        geodatabase = EsriFileGdb.openGeodatabase(fileName);
      } else {
        throw new IllegalArgumentException(
          "ESRI File Geodatabase must be a directory");
      }
    } else if (createMissingGeodatabase) {
      geodatabase = EsriFileGdb.createGeodatabase(fileName);
    } else {
      throw new IllegalArgumentException("ESRI file geodatbase not found "
        + fileName);
    }
  }

  @Override
  public void insert(final DataObject object) {
    getWriter().write(object);
  }

  public void deleteGeodatabase() {
    close();
    if (new File(fileName).exists()) {
      EsriFileGdb.DeleteGeodatabase(fileName);
    }
  }

  @Override
  public DataObject load(final QName typeName, final Object id) {
    final DataObjectMetaData metaData = getMetaData(typeName);
    if (metaData == null) {
      throw new IllegalArgumentException("Unknown type " + typeName);
    } else {
      final Table table = getTable(typeName);
      final EsriFileGeodatabaseQueryIterator iterator = new EsriFileGeodatabaseQueryIterator(
        metaData, this, table, metaData.getIdAttributeName() + " = " + id);
      try {
        if (iterator.hasNext()) {
          return iterator.next();
        } else {
          return null;
        }
      } finally {
        iterator.close();
      }
    }
  }

  @Override
  protected void loadSchemaDataObjectMetaData(
    final DataObjectStoreSchema schema,
    final Map<QName, DataObjectMetaData> metaDataMap) {
    final String schemaName = schema.getName();
    final String path = "\\" + schemaName;
    loadSchemaDataObjectMetaData(metaDataMap, schemaName, path, "Feature Class");
    loadSchemaDataObjectMetaData(metaDataMap, schemaName, path, "Table");
  }

  public void loadSchemaDataObjectMetaData(
    final Map<QName, DataObjectMetaData> metaDataMap, final String schemaName,
    final String path, final String datasetType) {
    VectorOfWString childFeatureClasses = geodatabase.getChildDatasets(path,
      datasetType);
    for (int i = 0; i < childFeatureClasses.size(); i++) {
      final String childPath = childFeatureClasses.get(i);
      addTableMetaData(childPath);
    }
  }

  private void addTableMetaData(final String path) {
    final Table table = geodatabase.openTable(path);
    try {
      DataObjectMetaData metaData = getMetaData(table);
      addMetaData(metaData);
    } finally {
      geodatabase.CloseTable(table);
    }
  }

  private DataObjectMetaData getMetaData(final Table table) {
    final String tableDefinition = table.getDefinition();
    try {
      final XmlProcessor parser = new EsriGdbXmlParser();
      final DETable deTable = parser.process(tableDefinition);
      final QName typeName = deTable.getTypeName();
      final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(
        typeName);
      for (final Field field : deTable.getFields()) {
        final String fieldName = field.getName();
        final String type = field.getType();
        final Constructor<? extends Attribute> attributeConstructor = ESRI_FIELD_TYPE_ATTRIBUTE_MAP.get(type);
        if (attributeConstructor != null) {
          try {
            final Attribute attribute = JavaBeanUtil.invokeConstructor(
              attributeConstructor, field);
            metaData.addAttribute(attribute);
            if (type.equals("esriFieldTypeOID")) {
              metaData.setIdAttributeName(fieldName);
            }
          } catch (final Throwable e) {
            LOG.error(tableDefinition);
            throw new RuntimeException("Error creating attribute for "
              + typeName + "." + field.getName() + " : " + field.getType(), e);
          }
        } else {
          LOG.error("Unsupported field type " + fieldName + ":" + type);
        }
      }
      if (metaData.getIdAttributeIndex() == -1) {
        final String oidFieldName = deTable.getOIDFieldName();
        metaData.setIdAttributeName(oidFieldName);
      }
      if (deTable instanceof DEFeatureClass) {
        DEFeatureClass featureClass = (DEFeatureClass)deTable;
        final String shapeFieldName = featureClass.getShapeFieldName();
        metaData.setGeometryAttributeName(shapeFieldName);
      }
      return metaData;
    } catch (RuntimeException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(tableDefinition);
      }
      throw e;
    }
  }

  @Override
  protected void loadSchemas(final Map<String, DataObjectStoreSchema> schemaMap) {
    addSchema(new DataObjectStoreSchema(this, ""));

    addChildSchema("\\");
  }

  public Reader<DataObject> query(final QName typeName) {
    final Table table = getTable(typeName);
    final DataObjectMetaData metaData = getMetaData(typeName);
    if (metaData == null) {
      throw new IllegalArgumentException("Type name does not exist " + typeName);
    } else {
      final EsriFileGeodatabaseQueryIterator iterator = new EsriFileGeodatabaseQueryIterator(
        metaData, this, table);
      final IteratorReader<DataObject> reader = new IteratorReader<DataObject>(
        iterator);
      return reader;
    }
  }

  public Reader<DataObject> query(final QName typeName, final Envelope envelope) {
    final DataObjectMetaData metaData = getMetaData(typeName);
    if (metaData == null) {
      throw new IllegalArgumentException("Type name does not exist " + typeName);
    } else {
      final Table table = getTable(typeName);
      final EsriFileGeodatabaseQueryIterator iterator = new EsriFileGeodatabaseQueryIterator(
        metaData, this, table, envelope);
      final IteratorReader<DataObject> reader = new IteratorReader<DataObject>(
        iterator);
      return reader;
    }
  }

  public Reader<DataObject> query(final QName typeName, final Geometry geometry) {
    final Envelope envelope = geometry.getEnvelopeInternal();
    return query(typeName, envelope);
  }

  public Reader<DataObject> query(final QName typeName, final String where,
    final Object... arguments) {
    final DataObjectMetaData metaData = getMetaData(typeName);
    if (metaData == null) {
      throw new IllegalArgumentException("Type name does not exist " + typeName);
    } else {
      final Table table = getTable(typeName);
      final StringBuffer whereClause = new StringBuffer();
      if (arguments.length == 0) {
        if (where.indexOf('?') > -1) {
          throw new IllegalArgumentException(
            "No arguments specified for a where clause with placeholders: "
              + where);
        } else {
          whereClause.append(where);
        }
      } else {
        final Matcher matcher = PLACEHOLDER_PATTERN.matcher(where);
        int i = 0;
        while (matcher.find()) {
          if (i >= arguments.length) {
            throw new IllegalArgumentException(
              "Not enough arguments for where clause with placeholders: "
                + where);
          }
          final Object argument = arguments[i];
          matcher.appendReplacement(whereClause, "");
          whereClause.append(argument);
          i++;
        }
        matcher.appendTail(whereClause);
      }

      final EsriFileGeodatabaseQueryIterator iterator = new EsriFileGeodatabaseQueryIterator(
        metaData, this, table, whereClause.toString());
      final IteratorReader<DataObject> reader = new IteratorReader<DataObject>(
        iterator);
      return reader;
    }
  }

  public void setFileName(final String fileName) {
    this.fileName = fileName;
  }

  @Override
  public void update(final DataObject object) {
    getWriter().write(object);
  }

  public boolean isCreateMissingTables() {
    return createMissingTables;
  }

  public void setCreateMissingTables(boolean createMissingTables) {
    this.createMissingTables = createMissingTables;
  }

  public DataObjectMetaData getMetaData(DataObjectMetaData objectMetaData) {
    synchronized (geodatabase) {
      DataObjectMetaData metaData = super.getMetaData(objectMetaData);
      if (createMissingTables && metaData == null) {
        final DataObjectStoreSchema schema = getSchema(objectMetaData);
        if (schema == null) {
          createSchema(objectMetaData);
        }

        metaData = createTable(objectMetaData);
      }
      return metaData;
    }
  }

  private void createSchema(DataObjectMetaData objectMetaData) {
    final List<DEFeatureDataset> datasets = EsriXmlDataObjectMetaDataUtil.getDEFeatureDatasets(objectMetaData);
    for (DEFeatureDataset dataset : datasets) {
      String path = dataset.getCatalogPath();
      final String datasetDefinition = Serializer.toString(dataset);
      try {
        geodatabase.createFeatureDataset(datasetDefinition);
        addFeatureDatasetSchema(path);
      } catch (Throwable t) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(datasetDefinition);
        }
        throw new RuntimeException("Unable to create feature dataset " + path,
          t);
      }
    }
  }

  private DataObjectStoreSchema getSchema(DataObjectMetaData objectMetaData) {
    final QName typeName = objectMetaData.getName();
    final String schemaName = typeName.getNamespaceURI();
    final DataObjectStoreSchema schema = getSchema(schemaName);
    return schema;
  }

  private DataObjectMetaData createTable(DataObjectMetaData objectMetaData) {
    final QName typeName = objectMetaData.getName();
    final String schemaName = typeName.getNamespaceURI();
    final DETable deTable = EsriXmlDataObjectMetaDataUtil.getDETable(objectMetaData);
    String tableDefinition = Serializer.toString(deTable);
    try {
      Table table = geodatabase.createTable(tableDefinition, "\\" + schemaName);
      try {
        DataObjectMetaData metaData = getMetaData(table);
        addMetaData(metaData);
        return metaData;
      } finally {
        geodatabase.CloseTable(table);
      }
    } catch (Throwable t) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(tableDefinition);
      }
      throw new RuntimeException("Unable to create table " + typeName, t);
    }
  }
}
