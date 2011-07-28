package com.revolsys.gis.esri.gdb.file;

import java.io.File;
import java.io.IOException;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.AbstractDataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.io.IteratorReader;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.esri.gdb.file.swig.EsriFileGdb;
import com.revolsys.gis.esri.gdb.file.swig.Geodatabase;
import com.revolsys.gis.esri.gdb.file.swig.Table;
import com.revolsys.gis.esri.gdb.file.swig.VectorOfWString;
import com.revolsys.gis.esri.gdb.file.type.BinaryAttribute;
import com.revolsys.gis.esri.gdb.file.type.DateAttribute;
import com.revolsys.gis.esri.gdb.file.type.DoubleAttribute;
import com.revolsys.gis.esri.gdb.file.type.FloatAttribute;
import com.revolsys.gis.esri.gdb.file.type.GeometryAttribute;
import com.revolsys.gis.esri.gdb.file.type.GlobalIdAttribute;
import com.revolsys.gis.esri.gdb.file.type.GuidAttribute;
import com.revolsys.gis.esri.gdb.file.type.IntegerAttribute;
import com.revolsys.gis.esri.gdb.file.type.OidAttribute;
import com.revolsys.gis.esri.gdb.file.type.ShortAttribute;
import com.revolsys.gis.esri.gdb.file.type.StringAttribute;
import com.revolsys.gis.esri.gdb.file.type.XmlAttribute;
import com.revolsys.gis.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.gis.esri.gdb.xml.model.CodedValueDomain;
import com.revolsys.gis.esri.gdb.xml.model.DEFeatureClass;
import com.revolsys.gis.esri.gdb.xml.model.DEFeatureDataset;
import com.revolsys.gis.esri.gdb.xml.model.DETable;
import com.revolsys.gis.esri.gdb.xml.model.DataElement;
import com.revolsys.gis.esri.gdb.xml.model.Domain;
import com.revolsys.gis.esri.gdb.xml.model.EsriGdbXmlParser;
import com.revolsys.gis.esri.gdb.xml.model.EsriGdbXmlSerializer;
import com.revolsys.gis.esri.gdb.xml.model.EsriXmlDataObjectMetaDataUtil;
import com.revolsys.gis.esri.gdb.xml.model.Field;
import com.revolsys.gis.esri.gdb.xml.model.Index;
import com.revolsys.gis.esri.gdb.xml.model.SpatialReference;
import com.revolsys.gis.esri.gdb.xml.model.Workspace;
import com.revolsys.gis.esri.gdb.xml.model.WorkspaceDefinition;
import com.revolsys.gis.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.xml.io.XmlProcessor;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public class FileGdbDataObjectStore extends AbstractDataObjectStore {
  private static final String CATALOG_PATH_PROPERTY = FileGdbDataObjectStore.class
    + ".CatalogPath";

  private static void addFieldTypeAttributeConstructor(
    final FieldType fieldType, final Class<? extends Attribute> attributeClass) {
    try {
      final Constructor<? extends Attribute> constructor = attributeClass.getConstructor(Field.class);
      ESRI_FIELD_TYPE_ATTRIBUTE_MAP.put(fieldType, constructor);
    } catch (final SecurityException e) {
      LOG.error("No public constructor for ESRI type " + fieldType, e);
    } catch (final NoSuchMethodException e) {
      LOG.error("No public constructor for ESRI type " + fieldType, e);
    }

  }

  public static SpatialReference getSpatialReference(
    final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      return null;
    } else {
      final String wkt = EsriFileGdb.getSpatialReferenceWkt(geometryFactory.getSRID());
      final SpatialReference spatialReference = SpatialReference.get(
        geometryFactory, wkt);
      return spatialReference;
    }
  }

  private Map<String, List<String>> domainColumNames = new HashMap<String, List<String>>();

  private String defaultSchema;

  private static final Logger LOG = LoggerFactory.getLogger(FileGdbDataObjectStore.class);

  private Geodatabase geodatabase;

  private String fileName;

  private boolean createMissingGeodatabase = false;

  private boolean createMissingTables;

  private static final Map<FieldType, Constructor<? extends Attribute>> ESRI_FIELD_TYPE_ATTRIBUTE_MAP = new HashMap<FieldType, Constructor<? extends Attribute>>();

  static {
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeInteger,
      IntegerAttribute.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeSmallInteger,
      ShortAttribute.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeDouble,
      DoubleAttribute.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeSingle,
      FloatAttribute.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeString,
      StringAttribute.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeDate,
      DateAttribute.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeGeometry,
      GeometryAttribute.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeOID,
      OidAttribute.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeBlob,
      BinaryAttribute.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeGlobalID,
      GlobalIdAttribute.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeGUID,
      GuidAttribute.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeXML,
      XmlAttribute.class);

  }

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\?");

  private Resource template;

  public FileGdbDataObjectStore() {
  }

  public FileGdbDataObjectStore(final File file) {
    this.fileName = file.getAbsolutePath();
  }

  public FileGdbDataObjectStore(final String fileName) {
    this.fileName = fileName;
  }

  public void addChildSchema(final String path) {
    final VectorOfWString childDatasets = geodatabase.getChildDatasets(path,
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

  private void addTableMetaData(final String schemaName, final String path) {
    final Table table = geodatabase.openTable(path);
    try {
      final DataObjectMetaData metaData = getMetaData(schemaName, path, table);
      addMetaData(metaData);
    } finally {
      geodatabase.CloseTable(table);
    }
  }

  @Override
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

  protected void closeTable(final Table table) {
    try {
      geodatabase.CloseTable(table);
    } catch (final Throwable e) {
      LOG.error("Unable to close table", e);
    }
  }

  public void createDomain(final Domain domain) {
    final String domainDef = EsriGdbXmlSerializer.toString(domain);
    try {
      geodatabase.createDomain(domainDef);
    } catch (final Exception e) {
      LOG.debug(domainDef);
      LOG.error("Unable to create domain", e);
    }
    loadDomain(domain.getDomainName());
  }

  private void createSchema(final DETable table) {
    final List<DEFeatureDataset> datasets = EsriXmlDataObjectMetaDataUtil.createDEFeatureDatasets(table);
    for (final DEFeatureDataset dataset : datasets) {
      final String path = dataset.getCatalogPath();
      final String datasetDefinition = EsriGdbXmlSerializer.toString(dataset);
      try {
        geodatabase.createFeatureDataset(datasetDefinition);
        addFeatureDatasetSchema(path);
      } catch (final Throwable t) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(datasetDefinition);
        }
        throw new RuntimeException("Unable to create feature dataset " + path,
          t);
      }
    }
  }

  public void createSchema(final String schemaName,
    final GeometryFactory geometryFactory) {
    final SpatialReference spatialReference = getSpatialReference(geometryFactory);
    final List<DEFeatureDataset> datasets = EsriXmlDataObjectMetaDataUtil.createDEFeatureDatasets(
      schemaName, spatialReference);
    for (final DEFeatureDataset dataset : datasets) {
      final String path = dataset.getCatalogPath();
      final String datasetDefinition = EsriGdbXmlSerializer.toString(dataset);
      try {
        geodatabase.createFeatureDataset(datasetDefinition);
        addFeatureDatasetSchema(path);
      } catch (final Throwable t) {
        if (LOG.isDebugEnabled()) {
          LOG.debug(datasetDefinition);
        }
        throw new RuntimeException("Unable to create feature dataset " + path,
          t);
      }
    }
  }

  private DataObjectMetaData createTable(final DataObjectMetaData objectMetaData) {
    final GeometryFactory geometryFactory = objectMetaData.getGeometryFactory();
    final SpatialReference spatialReference = getSpatialReference(geometryFactory);

    final DETable deTable = EsriXmlDataObjectMetaDataUtil.getDETable(
      objectMetaData, spatialReference);
    return createTable(deTable);
  }

  public DataObjectMetaData createTable(final DETable deTable) {
    String schemaPath = deTable.getParentCatalogPath();
    String schemaName = schemaPath.substring(1);
    final DataObjectStoreSchema schema = getSchema(schemaName);
    if (schema == null) {
      if (schemaName.equals(defaultSchema)) {
        addSchema(new DataObjectStoreSchema(this, schemaName));
      } else {
        createSchema(deTable);
      }
    }
    if (schemaName.equals(defaultSchema)) {
      schemaPath = "\\";
      // @TODO clone
      deTable.setCatalogPath("\\" + deTable.getName());
    } else if (schemaName.equals("")) {
      schemaName = defaultSchema;
    }
    for (final Field field : deTable.getFields()) {
      final String fieldName = field.getName();
      final CodeTable<?> codeTable = getCodeTableByColumn(fieldName);
      if (codeTable instanceof FileGdbDomainCodeTable) {
        final FileGdbDomainCodeTable domainCodeTable = (FileGdbDomainCodeTable)codeTable;
        field.setDomain(domainCodeTable.getDomain());
      }
    }
    final String tableDefinition = EsriGdbXmlSerializer.toString(deTable);
    final Table table;
    try {
      table = geodatabase.createTable(tableDefinition, schemaPath);
    } catch (final Throwable t) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(tableDefinition);
      }
      throw new RuntimeException("Unable to create table "
        + deTable.getCatalogPath(), t);
    }
    try {
      final DataObjectMetaData metaData = getMetaData(schemaName, schemaPath,
        table);
      addMetaData(metaData);
      return metaData;
    } finally {
      geodatabase.CloseTable(table);
    }
  }

  public Writer<DataObject> createWriter() {
    return new FileGdbWriter(this);
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

  public void deleteGeodatabase() {
    close();
    if (new File(fileName).exists()) {
      EsriFileGdb.DeleteGeodatabase(fileName);
    }
  }

  public String getDefaultSchema() {
    return defaultSchema;
  }

  public Map<String, List<String>> getDomainColumNames() {
    return domainColumNames;
  }

  public String getFileName() {
    return fileName;
  }

  protected Geodatabase getGeodatabase() {
    return geodatabase;
  }

  @Override
  public DataObjectMetaData getMetaData(final DataObjectMetaData objectMetaData) {
    synchronized (geodatabase) {
      DataObjectMetaData metaData = super.getMetaData(objectMetaData);
      if (createMissingTables && metaData == null) {
        metaData = createTable(objectMetaData);
      }
      return metaData;
    }
  }

  private DataObjectMetaData getMetaData(final String schemaName,
    final String path, final Table table) {
    final String tableDefinition = table.getDefinition();
    try {
      final XmlProcessor parser = new EsriGdbXmlParser();
      final DETable deTable = parser.process(tableDefinition);
      final String tableName = deTable.getName();
      final QName typeName = new QName(schemaName, tableName);
      final DataObjectStoreSchema schema = getSchema(schemaName);
      final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(this,
        schema, typeName);
      for (final Field field : deTable.getFields()) {
        final String fieldName = field.getName();
        final FieldType type = field.getType();
        final Constructor<? extends Attribute> attributeConstructor = ESRI_FIELD_TYPE_ATTRIBUTE_MAP.get(type);
        if (attributeConstructor != null) {
          try {
            final Attribute attribute = JavaBeanUtil.invokeConstructor(
              attributeConstructor, field);
            metaData.addAttribute(attribute);
            if (attribute instanceof GlobalIdAttribute) {
              metaData.setIdAttributeName(fieldName);
            }
          } catch (final Throwable e) {
            LOG.error(tableDefinition);
            throw new RuntimeException("Error creating attribute for "
              + typeName + "." + field.getName() + " : " + field.getType(), e);
          }
          if (fieldName.equals(tableName + "_ID")) {
            metaData.setIdAttributeName(fieldName);
          }
        } else {
          LOG.error("Unsupported field type " + fieldName + ":" + type);
        }
      }
      final String oidFieldName = deTable.getOIDFieldName();
      metaData.setProperty(
        EsriGeodatabaseXmlConstants.ESRI_OBJECT_ID_FIELD_NAME, oidFieldName);
      if (deTable instanceof DEFeatureClass) {
        final DEFeatureClass featureClass = (DEFeatureClass)deTable;
        final String shapeFieldName = featureClass.getShapeFieldName();
        metaData.setGeometryAttributeName(shapeFieldName);
      }
      metaData.setProperty(CATALOG_PATH_PROPERTY, path);
      for (final Index index : deTable.getIndexes()) {
        if (index.getName().endsWith("_PK")) {
          for (final Field field : index.getFields()) {
            final String fieldName = field.getName();
            metaData.setIdAttributeName(fieldName);
          }
        }
      }
      addMetaDataProperties(metaData);
      return metaData;
    } catch (final RuntimeException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(tableDefinition);
      }
      throw e;
    }
  }

  protected Table getTable(final QName typeName) {
    final String schemaName = typeName.getNamespaceURI();
    final String path = "\\\\" + schemaName + "\\" + typeName.getLocalPart();
    return geodatabase.openTable(path);
  }

  public Resource getTemplate() {
    return template;
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
    final File file = new File(fileName);
    if (file.exists() && new File(fileName, "gdb").exists()) {
      if (file.isDirectory()) {
        geodatabase = EsriFileGdb.openGeodatabase(fileName);
      } else {
        throw new IllegalArgumentException(
          "ESRI File Geodatabase must be a directory");
      }
    } else if (createMissingGeodatabase) {
      if (template == null) {
        geodatabase = EsriFileGdb.createGeodatabase(fileName);
      } else if (template.exists()) {
        if (template instanceof FileSystemResource) {
          final FileSystemResource fileResource = (FileSystemResource)template;
          final File templateFile = fileResource.getFile();
          if (templateFile.isDirectory()) {
            try {
              FileUtil.copy(templateFile, file);
            } catch (final IOException e) {
              throw new IllegalArgumentException(
                "Unable to copy template ESRI geodatabase " + template, e);
            }
            geodatabase = EsriFileGdb.openGeodatabase(fileName);
          }
        }
        if (geodatabase == null) {
          geodatabase = EsriFileGdb.createGeodatabase(fileName);
          final Workspace workspace = EsriGdbXmlParser.parse(template);
          final WorkspaceDefinition workspaceDefinition = workspace.getWorkspaceDefinition();
          for (final Domain domain : workspaceDefinition.getDomains()) {
            createDomain(domain);
          }
          for (final DataElement dataElement : workspaceDefinition.getDatasetDefinitions()) {
            if (dataElement instanceof DETable) {
              final DETable table = (DETable)dataElement;
              createTable(table);
            }
          }
        }
      } else {
        throw new IllegalArgumentException("Template does not exist "
          + template);
      }
    } else {
      throw new IllegalArgumentException("ESRI file geodatbase not found "
        + fileName);
    }
    final VectorOfWString domainNames = geodatabase.getDomains();
    for (int i = 0; i < domainNames.size(); i++) {
      final String domainName = domainNames.get(i);
      loadDomain(domainName);
    }
  }

  @Override
  public void insert(final DataObject object) {
    getWriter().write(object);
  }

  public boolean isCreateMissingGeodatabase() {
    return createMissingGeodatabase;
  }

  public boolean isCreateMissingTables() {
    return createMissingTables;
  }

  @Override
  public DataObject load(final QName typeName, final Object id) {
    final DataObjectMetaData metaData = getMetaData(typeName);
    if (metaData == null) {
      throw new IllegalArgumentException("Unknown type " + typeName);
    } else {
      final Table table = getTable(typeName);
      final FileGdbQueryIterator iterator = new FileGdbQueryIterator(metaData,
        this, table, metaData.getIdAttributeName() + " = " + id);
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

  protected void loadDomain(final String domainName) {
    final String domainDef = geodatabase.getDomainDefinition(domainName);
    final Domain domain = EsriGdbXmlParser.parse(domainDef);
    if (domain instanceof CodedValueDomain) {
      final CodedValueDomain codedValueDomain = (CodedValueDomain)domain;
      final FileGdbDomainCodeTable codeTable = new FileGdbDomainCodeTable(
        geodatabase, codedValueDomain);
      addCodeTable(codeTable);
      List<String> columnNames = domainColumNames.get(domainName);
      if (columnNames != null) {
        for (String columnName : columnNames) {
          addCodeTable(columnName, codeTable);
        }
      }
    }
  }

  @Override
  protected void loadSchemaDataObjectMetaData(
    final DataObjectStoreSchema schema,
    final Map<QName, DataObjectMetaData> metaDataMap) {
    final String schemaName = schema.getName();
    if (schemaName.equals(defaultSchema)) {
      loadSchemaDataObjectMetaData(metaDataMap, schemaName, "\\",
        "Feature Class");
      loadSchemaDataObjectMetaData(metaDataMap, schemaName, "\\", "Table");
    }
    final String path = "\\" + schemaName;
    loadSchemaDataObjectMetaData(metaDataMap, schemaName, path, "Feature Class");
    loadSchemaDataObjectMetaData(metaDataMap, schemaName, path, "Table");
  }

  public void loadSchemaDataObjectMetaData(
    final Map<QName, DataObjectMetaData> metaDataMap, final String schemaName,
    final String path, final String datasetType) {
    try {
      final VectorOfWString childFeatureClasses = geodatabase.getChildDatasets(
        path, datasetType);
      for (int i = 0; i < childFeatureClasses.size(); i++) {
        final String childPath = childFeatureClasses.get(i);
        addTableMetaData(schemaName, childPath);
      }
    } catch (final RuntimeException e) {
      final String message = e.getMessage();
      if (message == null
        || !message.equals("-2147211775\tThe item was not found.")) {
        throw e;
      }
    }
  }

  @Override
  protected void loadSchemas(final Map<String, DataObjectStoreSchema> schemaMap) {
    if (defaultSchema != null) {
      addSchema(new DataObjectStoreSchema(this, defaultSchema));
    } else {
      addSchema(new DataObjectStoreSchema(this, ""));
    }
    addChildSchema("\\");
  }

  public Reader<DataObject> query(final QName typeName) {
    final Table table = getTable(typeName);
    final DataObjectMetaData metaData = getMetaData(typeName);
    if (metaData == null) {
      throw new IllegalArgumentException("Type name does not exist " + typeName);
    } else {
      final FileGdbQueryIterator iterator = new FileGdbQueryIterator(metaData,
        this, table);
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
      final FileGdbQueryIterator iterator = new FileGdbQueryIterator(metaData,
        this, table, envelope);
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
          if (argument instanceof Number) {
            whereClause.append(argument);
          } else {
            whereClause.append("'");
            whereClause.append(argument);
            whereClause.append("'");
          }
          i++;
        }
        matcher.appendTail(whereClause);
      }

      final FileGdbQueryIterator iterator = new FileGdbQueryIterator(metaData,
        this, table, whereClause.toString());
      final IteratorReader<DataObject> reader = new IteratorReader<DataObject>(
        iterator);
      return reader;
    }
  }

  public void setCreateMissingGeodatabase(final boolean createMissingGeodatabase) {
    this.createMissingGeodatabase = createMissingGeodatabase;
  }

  public void setCreateMissingTables(final boolean createMissingTables) {
    this.createMissingTables = createMissingTables;
  }

  public void setDefaultSchema(final String defaultSchema) {
    this.defaultSchema = defaultSchema;
  }

  public void setDomainColumNames(
    final Map<String, List<String>> domainColumNames) {
    this.domainColumNames = domainColumNames;
  }

  public void setFileName(final String fileName) {
    this.fileName = fileName;
  }

  public void setTemplate(final Resource template) {
    this.template = template;
  }

  @Override
  public void update(final DataObject object) {
    getWriter().write(object);
  }
}
