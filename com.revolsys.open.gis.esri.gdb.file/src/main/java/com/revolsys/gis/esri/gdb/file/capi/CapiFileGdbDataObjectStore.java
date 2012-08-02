package com.revolsys.gis.esri.gdb.file.capi;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.gis.cs.BoundingBox;
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
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.esri.gdb.file.FileGdbDataObjectStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.capi.swig.Envelope;
import com.revolsys.gis.esri.gdb.file.capi.swig.EsriFileGdb;
import com.revolsys.gis.esri.gdb.file.capi.swig.Geodatabase;
import com.revolsys.gis.esri.gdb.file.capi.swig.Table;
import com.revolsys.gis.esri.gdb.file.capi.swig.VectorOfWString;
import com.revolsys.gis.esri.gdb.file.capi.type.BinaryAttribute;
import com.revolsys.gis.esri.gdb.file.capi.type.DateAttribute;
import com.revolsys.gis.esri.gdb.file.capi.type.DoubleAttribute;
import com.revolsys.gis.esri.gdb.file.capi.type.FloatAttribute;
import com.revolsys.gis.esri.gdb.file.capi.type.GeometryAttribute;
import com.revolsys.gis.esri.gdb.file.capi.type.GlobalIdAttribute;
import com.revolsys.gis.esri.gdb.file.capi.type.GuidAttribute;
import com.revolsys.gis.esri.gdb.file.capi.type.IntegerAttribute;
import com.revolsys.gis.esri.gdb.file.capi.type.OidAttribute;
import com.revolsys.gis.esri.gdb.file.capi.type.ShortAttribute;
import com.revolsys.gis.esri.gdb.file.capi.type.StringAttribute;
import com.revolsys.gis.esri.gdb.file.capi.type.XmlAttribute;
import com.revolsys.io.FileUtil;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.io.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.io.esri.gdb.xml.model.CodedValueDomain;
import com.revolsys.io.esri.gdb.xml.model.DEFeatureClass;
import com.revolsys.io.esri.gdb.xml.model.DEFeatureDataset;
import com.revolsys.io.esri.gdb.xml.model.DETable;
import com.revolsys.io.esri.gdb.xml.model.DataElement;
import com.revolsys.io.esri.gdb.xml.model.Domain;
import com.revolsys.io.esri.gdb.xml.model.EsriGdbXmlParser;
import com.revolsys.io.esri.gdb.xml.model.EsriGdbXmlSerializer;
import com.revolsys.io.esri.gdb.xml.model.EsriXmlDataObjectMetaDataUtil;
import com.revolsys.io.esri.gdb.xml.model.Field;
import com.revolsys.io.esri.gdb.xml.model.Index;
import com.revolsys.io.esri.gdb.xml.model.SpatialReference;
import com.revolsys.io.esri.gdb.xml.model.Workspace;
import com.revolsys.io.esri.gdb.xml.model.WorkspaceDefinition;
import com.revolsys.io.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.io.xml.XmlProcessor;
import com.revolsys.util.JavaBeanUtil;
import com.vividsolutions.jts.geom.Geometry;

public class CapiFileGdbDataObjectStore extends AbstractDataObjectStore
  implements FileGdbDataObjectStore {
  private static final String CATALOG_PATH_PROPERTY = CapiFileGdbDataObjectStore.class
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
    if (geometryFactory == null || geometryFactory.getSRID() == 0) {
      return null;
    } else {
      final String wkt = EsriFileGdb.getSpatialReferenceWkt(geometryFactory.getSRID());
      final SpatialReference spatialReference = SpatialReference.get(
        geometryFactory, wkt);
      return spatialReference;
    }
  }

  private final Map<String, AtomicLong> idGenerators = new HashMap<String, AtomicLong>();

  private Map<String, List<String>> domainColumNames = new HashMap<String, List<String>>();

  private String defaultSchema = "/";

  private static final Logger LOG = LoggerFactory.getLogger(CapiFileGdbDataObjectStore.class);

  private Geodatabase geodatabase;

  private String fileName;

  private boolean createMissingDataStore = true;

  private boolean createMissingTables = true;

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

  private final Set<Table> tablesToClose = new HashSet<Table>();

  private final Set<EnumRows> enumRowsToClose = new HashSet<EnumRows>();

  private boolean initialized;

  public CapiFileGdbDataObjectStore() {
  }

  public CapiFileGdbDataObjectStore(final File file) {
    this.fileName = file.getAbsolutePath();
  }

  public CapiFileGdbDataObjectStore(final String fileName) {
    this.fileName = fileName;
  }

  public synchronized void addChildSchema(final String path) {
    final VectorOfWString childDatasets = geodatabase.getChildDatasets(path,
      "Feature Dataset");
    for (int i = 0; i < childDatasets.size(); i++) {
      final String childPath = childDatasets.get(i);
      addFeatureDatasetSchema(childPath);
    }
  }

  @Override
  public synchronized void addCodeTable(final CodeTable codeTable) {
    super.addCodeTable(codeTable);
    if (codeTable instanceof Domain) {
      final Domain domain = (Domain)codeTable;
      createDomain(domain);
    }
  }

  private DataObjectStoreSchema addFeatureDatasetSchema(final String path) {
    final String schemaName = path.replaceAll("\\\\", "/");
    final DataObjectStoreSchema schema = new DataObjectStoreSchema(this,
      schemaName);
    schema.setProperty(CATALOG_PATH_PROPERTY, path);
    addSchema(schema);
    addChildSchema(path);
    return schema;
  }

  private void addTableMetaData(final String schemaName, final String path) {
    final String tableDefinition = geodatabase.getTableDefinition(path);
    final DataObjectMetaData metaData = getMetaData(schemaName, path,
      tableDefinition);
    addMetaData(metaData);
  }

  @Override
  @PreDestroy
  public synchronized void close() {
    for (final EnumRows rows : new ArrayList<EnumRows>(enumRowsToClose)) {
      closeEnumRows(rows);
    }
    for (final Table table : new ArrayList<Table>(tablesToClose)) {
      closeTable(table);
    }
    if (geodatabase != null) {
      geodatabase.delete();
      geodatabase = null;
    }
    super.close();
  }

  // @Override
  // protected AbstractIterator<DataObject> createIterator(final Query query,
  // final Map<String, Object> properties) {
  // return new JdbcQueryIterator(this, query, properties);
  // }
  //
  //
  // public FileGdbReader createReader() {
  // return new FileGdbReader(this);
  // }

  public void closeEnumRows(EnumRows rows) {
    if (rows != null) {
      if (enumRowsToClose.remove(rows)) {
        try {
          rows.Close();
        } catch (final NullPointerException e) {
        } finally {
          rows.delete();
          rows = null;
        }
      }
    }
  }

  protected synchronized void closeTable(final Table table) {
    try {
      tablesToClose.remove(table);
      if (geodatabase != null) {
        geodatabase.closeTable(table);
        table.delete();
      }
    } catch (final Throwable e) {
      LOG.error("Unable to close table", e);
    }
  }

  public synchronized void createDomain(final Domain domain) {
    final String domainName = domain.getDomainName();
    if (!domainColumNames.containsKey(domainName)) {
      final String domainDef = EsriGdbXmlSerializer.toString(domain);
      try {
        geodatabase.createDomain(domainDef);
      } catch (final Exception e) {
        LOG.debug(domainDef);
        LOG.error("Unable to create domain", e);
      }
      loadDomain(domain.getDomainName());
    }
  }

  // TODO add bounding box
  @Override
  protected synchronized AbstractIterator<DataObject> createIterator(
    final Query query, final Map<String, Object> properties) {
    String typePath = query.getTypeName();
    DataObjectMetaData metaData = query.getMetaData();
    if (metaData == null) {
      typePath = query.getTypeName();
      metaData = getMetaData(typePath);
      if (metaData == null) {
        throw new IllegalArgumentException("Type name does not exist "
          + typePath);
      }
    } else {
      typePath = metaData.getPath();
    }
    String where = query.getWhereClause();
    if (where == null) {
      where = "";
    }
    final List<Object> parameters = query.getParameters();
    final StringBuffer whereClause = new StringBuffer();
    if (parameters.isEmpty()) {
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
        if (i >= parameters.size()) {
          throw new IllegalArgumentException(
            "Not enough arguments for where clause with placeholders: " + where);
        }
        final Object argument = parameters.get(i);
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
    final BoundingBox boundingBox = query.getBoundingBox();
    final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this,
      typePath, whereClause.toString(), boundingBox);
    return iterator;
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized <T> T createPrimaryIdValue(final String typePath) {
    final DataObjectMetaData metaData = getMetaData(typePath);
    if (metaData == null) {
      return null;
    } else {
      final String idAttributeName = metaData.getIdAttributeName();
      if (idAttributeName == null) {
        return null;
      } else if (!idAttributeName.equals("OBJECTID")) {
        AtomicLong idGenerator = idGenerators.get(typePath);
        if (idGenerator == null) {
          long maxId = 0;
          for (final DataObject object : query(typePath)) {
            final Object id = object.getIdValue();
            if (id instanceof Number) {
              final Number number = (Number)id;
              if (number.longValue() > maxId) {
                maxId = number.longValue();
              }
            }
          }
          idGenerator = new AtomicLong(maxId);
          idGenerators.put(typePath, idGenerator);
        }
        return (T)((Object)(idGenerator.incrementAndGet()));
      } else {
        return null;
      }
    }
  }

  private DataObjectStoreSchema createSchema(final DETable table) {
    final String catalogPath = table.getParentCatalogPath();
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
    return getSchema(catalogPath.replaceAll("\\\\", "/"));
  }

  public synchronized void createSchema(final String schemaName,
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
    final DataObjectMetaDataImpl tableMetaData = createTable(deTable);
    final String idAttributeName = objectMetaData.getIdAttributeName();
    if (idAttributeName != null) {
      tableMetaData.setIdAttributeName(idAttributeName);
    }
    return tableMetaData;
  }

  protected synchronized DataObjectMetaDataImpl createTable(
    final DETable deTable) {
    String schemaPath = deTable.getParentCatalogPath();
    String schemaName = schemaPath.replaceAll("\\\\", "/");
    DataObjectStoreSchema schema = getSchema(schemaName);
    if (schema == null) {
      if (schemaName.length() > 1 && deTable instanceof DEFeatureClass) {
        schema = createSchema(deTable);
      } else {
        schema = new DataObjectStoreSchema(this, schemaName);
        addSchema(schema);
      }
    } else if (schema.getProperty(CATALOG_PATH_PROPERTY) == null) {
      if (schemaName.length() > 1 && deTable instanceof DEFeatureClass) {
        createSchema(deTable);
      }
    }
    if (schemaName.equals(defaultSchema)) {
      if (!(deTable instanceof DEFeatureClass)) {
        schemaPath = "\\";
        // @TODO clone
        deTable.setCatalogPath("\\" + deTable.getName());

      }
    } else if (schemaName.equals("")) {
      schemaName = defaultSchema;
    }
    for (final Field field : deTable.getFields()) {
      final String fieldName = field.getName();
      final CodeTable codeTable = getCodeTableByColumn(fieldName);
      if (codeTable instanceof FileGdbDomainCodeTable) {
        final FileGdbDomainCodeTable domainCodeTable = (FileGdbDomainCodeTable)codeTable;
        field.setDomain(domainCodeTable.getDomain());
      }
    }
    final String tableDefinition = EsriGdbXmlSerializer.toString(deTable);
    final Table table;
    try {
      table = geodatabase.createTable(tableDefinition, schemaPath);
      tablesToClose.add(table);
    } catch (final Throwable t) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(tableDefinition);
      }
      throw new RuntimeException("Unable to create table "
        + deTable.getCatalogPath(), t);
    }
    try {
      final DataObjectMetaDataImpl metaData = getMetaData(schemaName,
        schemaPath, tableDefinition);
      addMetaData(metaData);
      return metaData;
    } finally {
      closeTable(table);
    }
  }

  @Override
  public synchronized Writer<DataObject> createWriter() {
    return new FileGdbWriter(this);
  }

  @Override
  public synchronized void delete(final DataObject object) {
    if (object.getState() == DataObjectState.Persisted
      || object.getState() == DataObjectState.Modified) {
      object.setState(DataObjectState.Deleted);
      final Writer<DataObject> writer = getWriter();
      writer.write(object);
    }
  }

  @Override
  public synchronized void deleteGeodatabase() {
    close();
    if (new File(fileName).exists()) {
      EsriFileGdb.DeleteGeodatabase(fileName);
    }
  }

  public synchronized String getDefaultSchema() {
    return defaultSchema;
  }

  public synchronized Map<String, List<String>> getDomainColumNames() {
    return domainColumNames;
  }

  public synchronized String getFileName() {
    return fileName;
  }

  protected synchronized Geodatabase getGeodatabase() {
    return geodatabase;
  }

  @Override
  public synchronized DataObjectMetaData getMetaData(
    final DataObjectMetaData objectMetaData) {
    synchronized (geodatabase) {
      DataObjectMetaData metaData = super.getMetaData(objectMetaData);
      if (createMissingTables && metaData == null) {
        metaData = createTable(objectMetaData);
      }
      return metaData;
    }
  }

  public synchronized DataObjectMetaDataImpl getMetaData(
    final String schemaName, final String path, final String tableDefinition) {
    try {
      final XmlProcessor parser = new EsriGdbXmlParser();
      final DETable deTable = parser.process(tableDefinition);
      final String tableName = deTable.getName();
      final String typePath = PathUtil.toPath(schemaName, tableName);
      final DataObjectStoreSchema schema = getSchema(schemaName);
      final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(this,
        schema, typePath);
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
              + typePath + "." + field.getName() + " : " + field.getType(), e);
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
      if (metaData.getIdAttributeIndex() == -1) {
        metaData.setIdAttributeName(deTable.getOIDFieldName());
      }

      return metaData;
    } catch (final RuntimeException e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug(tableDefinition);
      }
      throw e;
    }
  }

  protected synchronized Table getTable(final String typePath) {
    if (getMetaData(typePath) == null) {
      return null;
    } else {
      final String path = typePath.replaceAll("/", "\\\\");
      try {
        final Table table = geodatabase.openTable(path);
        tablesToClose.add(table);
        return table;
      } catch (final RuntimeException e) {
        throw new RuntimeException("Unable to open table " + typePath, e);
      }
    }
  }

  public synchronized Resource getTemplate() {
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

  @Override
  @PostConstruct
  public synchronized void initialize() {
    if (!initialized) {
      initialized = true;
      super.initialize();
      final File file = new File(fileName);
      if (file.exists() && new File(fileName, "gdb").exists()) {
        if (file.isDirectory()) {
          geodatabase = EsriFileGdb.openGeodatabase(fileName);
        } else {
          throw new IllegalArgumentException(
            "ESRI File Geodatabase must be a directory");
        }
      } else if (createMissingDataStore) {
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
  }

  @Override
  public synchronized void insert(final DataObject object) {
    getWriter().write(object);
  }

  public synchronized boolean isCreateMissingDataStore() {
    return createMissingDataStore;
  }

  public synchronized boolean isCreateMissingTables() {
    return createMissingTables;
  }

  @Override
  public synchronized DataObject load(final String typePath, final Object id) {
    final DataObjectMetaData metaData = getMetaData(typePath);
    if (metaData == null) {
      throw new IllegalArgumentException("Unknown type " + typePath);
    } else {
      final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this,
        typePath, metaData.getIdAttributeName() + " = " + id);
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

  protected synchronized void loadDomain(final String domainName) {
    final String domainDef = geodatabase.getDomainDefinition(domainName);
    final Domain domain = EsriGdbXmlParser.parse(domainDef);
    if (domain instanceof CodedValueDomain) {
      final CodedValueDomain codedValueDomain = (CodedValueDomain)domain;
      final FileGdbDomainCodeTable codeTable = new FileGdbDomainCodeTable(this,
        codedValueDomain);
      super.addCodeTable(codeTable);
      final List<String> columnNames = domainColumNames.get(domainName);
      if (columnNames != null) {
        for (final String columnName : columnNames) {
          addCodeTable(columnName, codeTable);
        }
      }
    }
  }

  @Override
  protected synchronized void loadSchemaDataObjectMetaData(
    final DataObjectStoreSchema schema,
    final Map<String, DataObjectMetaData> metaDataMap) {
    final String schemaName = schema.getPath();
    if (schemaName.equals(defaultSchema)) {
      loadSchemaDataObjectMetaData(metaDataMap, schemaName, "\\",
        "Feature Class");
      loadSchemaDataObjectMetaData(metaDataMap, schemaName, "\\", "Table");
    }
    final String path = schemaName.replaceAll("/", "\\\\");
    loadSchemaDataObjectMetaData(metaDataMap, schemaName, path, "Feature Class");
    loadSchemaDataObjectMetaData(metaDataMap, schemaName, path, "Table");
  }

  public synchronized void loadSchemaDataObjectMetaData(
    final Map<String, DataObjectMetaData> metaDataMap, final String schemaName,
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
  protected synchronized void loadSchemas(
    final Map<String, DataObjectStoreSchema> schemaMap) {
    addSchema(new DataObjectStoreSchema(this, defaultSchema));
    addChildSchema("\\");
  }

  // @Override
  // public synchronized Reader<DataObject> query(final String typePath) {
  // final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this,
  // typePath);
  // final IteratorReader<DataObject> reader = new IteratorReader<DataObject>(
  // iterator);
  // return reader;
  // }

  @Override
  public synchronized Reader<DataObject> query(final String typePath,
    final BoundingBox boundingBox) {
    final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this,
      typePath, boundingBox);
    final IteratorReader<DataObject> reader = new IteratorReader<DataObject>(
      iterator);
    return reader;
  }

  @Override
  public synchronized Reader<DataObject> query(final String typePath,
    final Geometry geometry) {
    final BoundingBox boundingBox = new BoundingBox(geometry);
    return query(typePath, boundingBox);
  }

  public EnumRows search(final Table table, final String fields,
    final String whereClause, final boolean recycling) {
    final EnumRows rows = table.search(fields, whereClause, recycling);
    enumRowsToClose.add(rows);
    return rows;
  }

  public EnumRows search(final Table table, final String fields,
    final String whereClause, final Envelope boundingBox,
    final boolean recycling) {
    final EnumRows rows = table.search(fields, whereClause, boundingBox,
      recycling);
    enumRowsToClose.add(rows);
    return rows;
  }

  @Override
  public synchronized void setCreateMissingDataStore(
    final boolean createMissingDataStore) {
    this.createMissingDataStore = createMissingDataStore;
  }

  @Override
  public synchronized void setCreateMissingTables(
    final boolean createMissingTables) {
    this.createMissingTables = createMissingTables;
  }

  @Override
  public synchronized void setDefaultSchema(final String defaultSchema) {
    if (StringUtils.hasText(defaultSchema)) {
      if (!defaultSchema.startsWith("/")) {
        this.defaultSchema = "/" + defaultSchema;
      } else {
        this.defaultSchema = defaultSchema;
      }
    } else {
      this.defaultSchema = "/";
    }
    refreshSchema();
  }

  public synchronized void setDomainColumNames(
    final Map<String, List<String>> domainColumNames) {
    this.domainColumNames = domainColumNames;
  }

  public synchronized void setFileName(final String fileName) {
    this.fileName = fileName;
  }

  public synchronized void setTemplate(final Resource template) {
    this.template = template;
  }

  @Override
  public synchronized void update(final DataObject object) {
    getWriter().write(object);
  }
}
