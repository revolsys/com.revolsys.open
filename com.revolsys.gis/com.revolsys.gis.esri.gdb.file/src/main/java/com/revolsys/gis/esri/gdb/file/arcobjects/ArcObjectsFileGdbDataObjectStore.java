package com.revolsys.gis.esri.gdb.file.arcobjects;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
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

import com.esri.arcgis.datasourcesGDB.FileGDBWorkspaceFactory;
import com.esri.arcgis.geodatabase.CodedValueDomain;
import com.esri.arcgis.geodatabase.FeatureClass;
import com.esri.arcgis.geodatabase.FeatureDataset;
import com.esri.arcgis.geodatabase.IDataset;
import com.esri.arcgis.geodatabase.IDomain;
import com.esri.arcgis.geodatabase.IEnumDataset;
import com.esri.arcgis.geodatabase.IEnumDomain;
import com.esri.arcgis.geodatabase.IEnumFeatureClass;
import com.esri.arcgis.geodatabase.IEnumNameMapping;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IField;
import com.esri.arcgis.geodatabase.IFields;
import com.esri.arcgis.geodatabase.IIndex;
import com.esri.arcgis.geodatabase.IIndexes;
import com.esri.arcgis.geodatabase.ITable;
import com.esri.arcgis.geodatabase.IWorkspace;
import com.esri.arcgis.geodatabase.IWorkspaceName;
import com.esri.arcgis.geodatabase.IWorkspaceProxy;
import com.esri.arcgis.geodatabase.Table;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geodatabase.esriDatasetType;
import com.esri.arcgis.geodatabase.esriFieldType;
import com.esri.arcgis.geodatabasedistributed.GdbImporter;
import com.esri.arcgis.geodatabasedistributed.IGdbXmlImport;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.AoInitialize;
import com.esri.arcgis.system.EngineInitializer;
import com.esri.arcgis.system.IName;
import com.esri.arcgis.system.esriLicenseProductCode;
import com.esri.arcgis.system.esriLicenseStatus;
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
import com.revolsys.gis.esri.gdb.file.FileGdbDataObjectStore;
import com.revolsys.gis.esri.gdb.file.arcobjects.type.BinaryAttribute;
import com.revolsys.gis.esri.gdb.file.arcobjects.type.DateAttribute;
import com.revolsys.gis.esri.gdb.file.arcobjects.type.DoubleAttribute;
import com.revolsys.gis.esri.gdb.file.arcobjects.type.FloatAttribute;
import com.revolsys.gis.esri.gdb.file.arcobjects.type.GeometryAttribute;
import com.revolsys.gis.esri.gdb.file.arcobjects.type.GlobalIdAttribute;
import com.revolsys.gis.esri.gdb.file.arcobjects.type.GuidAttribute;
import com.revolsys.gis.esri.gdb.file.arcobjects.type.IntegerAttribute;
import com.revolsys.gis.esri.gdb.file.arcobjects.type.OidAttribute;
import com.revolsys.gis.esri.gdb.file.arcobjects.type.ShortAttribute;
import com.revolsys.gis.esri.gdb.file.arcobjects.type.StringAttribute;
import com.revolsys.gis.esri.gdb.file.arcobjects.type.XmlAttribute;
import com.revolsys.gis.esri.gdb.file.capi.swig.EsriFileGdb;
import com.revolsys.gis.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.gis.esri.gdb.xml.model.DEFeatureClass;
import com.revolsys.gis.esri.gdb.xml.model.SpatialReference;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.util.JavaBeanUtil;
import com.vividsolutions.jts.geom.Geometry;

public class ArcObjectsFileGdbDataObjectStore extends AbstractDataObjectStore
  implements FileGdbDataObjectStore {

  private static final String CATALOG_PATH_PROPERTY = ArcObjectsFileGdbDataObjectStore.class
    + ".CatalogPath";

  private static final Map<Integer, Constructor<? extends Attribute>> ESRI_FIELD_TYPE_ATTRIBUTE_MAP = new HashMap<Integer, Constructor<? extends Attribute>>();

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\?");

  static {
    EngineInitializer.initializeEngine();
    try {
      final AoInitialize aoInit = new AoInitialize();
      if (aoInit.isProductCodeAvailable(esriLicenseProductCode.esriLicenseProductCodeEngine) == esriLicenseStatus.esriLicenseAvailable) {
        aoInit.initialize(esriLicenseProductCode.esriLicenseProductCodeEngine);
      } else if (aoInit.isProductCodeAvailable(esriLicenseProductCode.esriLicenseProductCodeArcEditor) == esriLicenseStatus.esriLicenseAvailable) {
        aoInit.initialize(esriLicenseProductCode.esriLicenseProductCodeArcEditor);
      } else {
        throw new RuntimeException("Unable to get ArcGIS engine linence");
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to get ArcGIS engine linence", e);
    }
    addFieldTypeAttributeConstructor(esriFieldType.esriFieldTypeInteger,
      IntegerAttribute.class);
    addFieldTypeAttributeConstructor(esriFieldType.esriFieldTypeSmallInteger,
      ShortAttribute.class);
    addFieldTypeAttributeConstructor(esriFieldType.esriFieldTypeDouble,
      DoubleAttribute.class);
    addFieldTypeAttributeConstructor(esriFieldType.esriFieldTypeSingle,
      FloatAttribute.class);
    addFieldTypeAttributeConstructor(esriFieldType.esriFieldTypeString,
      StringAttribute.class);
    addFieldTypeAttributeConstructor(esriFieldType.esriFieldTypeDate,
      DateAttribute.class);
    addFieldTypeAttributeConstructor(esriFieldType.esriFieldTypeGeometry,
      GeometryAttribute.class);
    addFieldTypeAttributeConstructor(esriFieldType.esriFieldTypeOID,
      OidAttribute.class);
    addFieldTypeAttributeConstructor(esriFieldType.esriFieldTypeBlob,
      BinaryAttribute.class);
    addFieldTypeAttributeConstructor(esriFieldType.esriFieldTypeGlobalID,
      GlobalIdAttribute.class);
    addFieldTypeAttributeConstructor(esriFieldType.esriFieldTypeGUID,
      GuidAttribute.class);
    addFieldTypeAttributeConstructor(esriFieldType.esriFieldTypeXML,
      XmlAttribute.class);

  }

  private static void addFieldTypeAttributeConstructor(final int fieldType,
    final Class<? extends Attribute> attributeClass) {
    try {
      final Constructor<? extends Attribute> constructor = attributeClass.getConstructor(IField.class);
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

  // private Geodatabase geodatabase;

  private String defaultSchema;

  private static final Logger LOG = LoggerFactory.getLogger(ArcObjectsFileGdbDataObjectStore.class);

  private String fileName;

  private boolean createMissingGeodatabase = false;

  private boolean createMissingTables;

  private Resource template;

  private Workspace workspace;

  private final Map<String, IDataset> dataSetMap = new HashMap<String, IDataset>();

  private final Map<QName, ITable> tableMap = new HashMap<QName, ITable>();

  public ArcObjectsFileGdbDataObjectStore() {
  }

  public ArcObjectsFileGdbDataObjectStore(final File file) {
    this.fileName = file.getAbsolutePath();
  }

  public ArcObjectsFileGdbDataObjectStore(final String fileName) {
    this.fileName = fileName;
  }

  public void addChildSchema(final String path) {
    try {
      final IEnumDataset datasets = workspace.getDatasets(esriDatasetType.esriDTFeatureDataset);
      for (IDataset dataset = datasets.next(); dataset != null; dataset = datasets.next()) {
        final String childPath = dataset.getFullName().getNameString();
        addFeatureDatasetSchema(childPath);
        dataSetMap.put(childPath, dataset);
      }
    } catch (final Exception e) {
      throw new RuntimeException("Unable to get feature datasets for path "
        + path);
    }
  }

  private void addFeatureDatasetSchema(final String path) {
    final String schemaName = path.substring(1);
    final DataObjectStoreSchema schema = new DataObjectStoreSchema(this,
      schemaName);
    addSchema(schema);
    addChildSchema(path);
  }

  private void addTableMetaData(final String schemaName, final IDataset dataset)
    throws AutomationException, IOException {
    final DataObjectMetaData metaData = getMetaData(schemaName, dataset);
    tableMap.put(metaData.getName(), getITable(dataset));
    addMetaData(metaData);
  }

  @Override
  @PreDestroy
  public void close() {
    try {
      if (workspace != null) {
        workspace.release();
      }
    } finally {
      workspace = null;
    }
  }

  public FileGdbReader createReader() {
    return new FileGdbReader(this);
  }

  private DataObjectMetaData createTable(final DataObjectMetaData objectMetaData) {
    // TODO final GeometryFactory geometryFactory =
    // objectMetaData.getGeometryFactory();
    // final SpatialReference spatialReference =
    // getSpatialReference(geometryFactory);
    //
    // final DETable deTable = EsriXmlDataObjectMetaDataUtil.getDETable(
    // objectMetaData, spatialReference);
    // return createTable(deTable);
    return null;
  }

  public void createWorkspace() {
    try {
      final File file = new File(fileName);
      final FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
      final IWorkspaceName iWorkspaceName = factory.create(file.getParent(),
        file.getName(), null, 0);
      final IWorkspace iWorkspace = new IWorkspaceProxy(
        ((IName)iWorkspaceName).open());
      workspace = new com.esri.arcgis.geodatabase.Workspace(iWorkspace);
    } catch (final Exception e) {
      throw new RuntimeException("Unable to open geodatabase " + fileName, e);
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
      try {
        workspace.delete();
      } catch (final Exception e) {
        throw new RuntimeException("Unable to delete geodatabase " + fileName,
          e);
      }
    }
  }

  public ITable getITable(final QName typeName) {
    return tableMap.get(typeName);
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

  @Override
  public DataObjectMetaData getMetaData(final DataObjectMetaData objectMetaData) {
    DataObjectMetaData metaData = super.getMetaData(objectMetaData);
    if (createMissingTables && metaData == null) {
      metaData = createTable(objectMetaData);
    }
    return metaData;
  }

  private DataObjectMetaData getMetaData(final String schemaName,
    final IDataset dataset) {
    try {
      ITable table = getITable(dataset);
      final String tableName = ((IDataset)table).getName();
      final QName typeName = new QName(schemaName, tableName);
      final DataObjectStoreSchema schema = getSchema(schemaName);
      final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(this,
        schema, typeName);
      final IFields fields = table.getFields();
      for (int i = 0; i < fields.getFieldCount(); i++) {
        final IField field = fields.getField(i);
        final String fieldName = field.getName();
        final int type = field.getType();
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
      final String oidFieldName = table.getOIDFieldName();
      metaData.setProperty(
        EsriGeodatabaseXmlConstants.ESRI_OBJECT_ID_FIELD_NAME, oidFieldName);
      if (table instanceof DEFeatureClass) {
        final DEFeatureClass featureClass = (DEFeatureClass)table;
        final String shapeFieldName = featureClass.getShapeFieldName();
        metaData.setGeometryAttributeName(shapeFieldName);
      }
      final IIndexes indexes = table.getIndexes();
      for (int i = 0; i < indexes.getIndexCount(); i++) {
        final IIndex index = indexes.getIndex(i);
        if (index.getName().endsWith("_PK")) {
          final IFields indexFields = index.getFields();
          for (int j = 0; i < indexFields.getFieldCount(); j++) {
            final IField field = indexFields.getField(j);
            final String fieldName = field.getName();
            metaData.setIdAttributeName(fieldName);
          }
        }
      }
      addMetaDataProperties(metaData);
      return metaData;
    } catch (final Exception e) {
      throw new RuntimeException("Unable to get metadata", e);
    }
  }

  public ITable getITable(final IDataset dataset) {
    try {
      ITable table;
      if (dataset.getType() == esriDatasetType.esriDTTable) {
        table = new Table(dataset);
      } else {
        table = new FeatureClass(dataset);
      }
      return table;
    } catch (Exception e) {
      throw new RuntimeException("Unable to get table " + dataset);
    }
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
        openWorkspace();
      } else {
        throw new IllegalArgumentException(
          "ESRI File Geodatabase must be a directory");
      }
    } else if (createMissingGeodatabase) {
      if (template == null) {
        createWorkspace();
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
            openWorkspace();
          }
        }
        if (workspace == null) {
          createFromXmlTemplate();
        }
      } else {
        throw new IllegalArgumentException("Template does not exist "
          + template);
      }
    } else {
      throw new IllegalArgumentException("ESRI file geodatbase not found "
        + fileName);
    }
    try {
      final IEnumDomain domains = workspace.getDomains();
      for (IDomain domain = domains.next(); domain != null; domain = domains.next()) {
        loadDomain(domain);
      }
    } catch (final Exception e) {
      throw new RuntimeException("Unable to load domain");
    }
  }

  public void createFromXmlTemplate() {
    try {
      createWorkspace();

      File file = FileUtil.getFile(template);
      String fileName = file.getAbsolutePath();

      IGdbXmlImport importer = new GdbImporter();
      IEnumNameMapping[] enumNameMapping = new IEnumNameMapping[1];
      importer.generateNameMapping(fileName, workspace, enumNameMapping);
      importer.importWorkspace(fileName, enumNameMapping[0], workspace, true);
      file.delete();
    } catch (Exception e) {
      throw new RuntimeException("Unable to create workspace from template "
        + template, e);
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
      final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this,
        typeName, metaData.getIdAttributeName() + " = " + id);
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

  protected void loadDomain(final IDomain domain) {
    if (domain instanceof CodedValueDomain) {
      final CodedValueDomain codedValueDomain = (CodedValueDomain)domain;
      final FileGdbDomainCodeTable codeTable = new FileGdbDomainCodeTable(
        workspace, codedValueDomain);
      addCodeTable(codeTable);
      final List<String> columnNames = domainColumNames.get(codeTable.getName());
      if (columnNames != null) {
        for (final String columnName : columnNames) {
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
        esriDatasetType.esriDTFeatureClass);
      loadSchemaDataObjectMetaData(metaDataMap, schemaName, "\\",
        esriDatasetType.esriDTTable);
    }
    final String path = "\\" + schemaName;
    loadSchemaDataObjectMetaData(metaDataMap, schemaName, path,
      esriDatasetType.esriDTFeatureClass);
    loadSchemaDataObjectMetaData(metaDataMap, schemaName, path,
      esriDatasetType.esriDTTable);
  }

  public void loadSchemaDataObjectMetaData(
    final Map<QName, DataObjectMetaData> metaDataMap,
    final FeatureDataset dataset, final String schemaName, final String path,
    final int datasetType) {
    try {
      final IEnumFeatureClass datasets = dataset.getClasses();
      for (IFeatureClass feature = datasets.next(); feature != null; feature = datasets.next()) {
        addTableMetaData(schemaName, (FeatureClass)feature);
      }
    } catch (final Exception e) {
      throw new RuntimeException("Unable to load metadata ", e);
    }
  }

  public void loadSchemaDataObjectMetaData(
    final Map<QName, DataObjectMetaData> metaDataMap, final String schemaName,
    final String path, final int datasetType) {
    try {
      final IEnumDataset datasets = workspace.getDatasets(datasetType);
      for (IDataset dataset = datasets.next(); dataset != null; dataset = datasets.next()) {
        addTableMetaData(schemaName, dataset);
      }
    } catch (final Exception e) {
      throw new RuntimeException("Unable to load metadata ", e);
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

  public void openWorkspace() {
    try {
      final FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
      final IWorkspace iWorkspace = factory.openFromFile(fileName, 0);
      workspace = new com.esri.arcgis.geodatabase.Workspace(iWorkspace);
    } catch (final Exception e) {
      throw new RuntimeException("Unable to open geodatabase " + fileName, e);
    }
  }

  public Reader<DataObject> query(final QName typeName) {
    final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this,
      typeName);
    final IteratorReader<DataObject> reader = new IteratorReader<DataObject>(
      iterator);
    return reader;
  }

  public Reader<DataObject> query(final QName typeName,
    final BoundingBox boundingBox) {
    final Iterator<DataObject> iterator = new FileGdbFeatureClassQueryIterator(
      this, typeName, boundingBox);
    final IteratorReader<DataObject> reader = new IteratorReader<DataObject>(
      iterator);
    return reader;
  }

  public Reader<DataObject> query(final QName typeName, final Geometry geometry) {
    final BoundingBox boundingBox = new BoundingBox(geometry);
    return query(typeName, boundingBox);
  }

  public Reader<DataObject> query(final QName typeName, final String where,
    final Object... arguments) {
    final DataObjectMetaData metaData = getMetaData(typeName);
    if (metaData == null) {
      throw new IllegalArgumentException("Type name does not exist " + typeName);
    } else {
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

      final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this,
        typeName, whereClause.toString());
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
