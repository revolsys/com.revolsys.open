package com.revolsys.gis.esri.gdb.file.arcobjects;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.esri.arcgis.datasourcesGDB.FileGDBWorkspaceFactory;
import com.esri.arcgis.geodatabase.CodedValueDomain;
import com.esri.arcgis.geodatabase.FeatureClass;
import com.esri.arcgis.geodatabase.FeatureDataset;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IDataset;
import com.esri.arcgis.geodatabase.IDomain;
import com.esri.arcgis.geodatabase.IEnumDataset;
import com.esri.arcgis.geodatabase.IEnumDomain;
import com.esri.arcgis.geodatabase.IEnumFeatureClass;
import com.esri.arcgis.geodatabase.IEnumNameMapping;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureBuffer;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geodatabase.IField;
import com.esri.arcgis.geodatabase.IFields;
import com.esri.arcgis.geodatabase.IIndex;
import com.esri.arcgis.geodatabase.IIndexes;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geodatabase.IRowBuffer;
import com.esri.arcgis.geodatabase.ITable;
import com.esri.arcgis.geodatabase.IWorkspace;
import com.esri.arcgis.geodatabase.QueryFilter;
import com.esri.arcgis.geodatabase.SpatialFilter;
import com.esri.arcgis.geodatabase.Table;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geodatabase.esriDatasetType;
import com.esri.arcgis.geodatabase.esriFieldType;
import com.esri.arcgis.geodatabasedistributed.GdbImporter;
import com.esri.arcgis.geodatabasedistributed.IGdbXmlImport;
import com.esri.arcgis.geometry.Envelope;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.AoInitialize;
import com.esri.arcgis.system.Cleaner;
import com.esri.arcgis.system.EngineInitializer;
import com.esri.arcgis.system.esriLicenseProductCode;
import com.esri.arcgis.system.esriLicenseStatus;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.esri.gdb.file.arcobjects.type.AbstractFileGdbAttribute;
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
import com.revolsys.gis.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.gis.esri.gdb.xml.model.DEFeatureClass;
import com.revolsys.io.FileUtil;
import com.revolsys.util.JavaBeanUtil;

public class ArcObjectsUtil {

  private static final Map<Integer, Constructor<? extends Attribute>> ESRI_FIELD_TYPE_ATTRIBUTE_MAP = new HashMap<Integer, Constructor<? extends Attribute>>();

  private static final Logger LOG = LoggerFactory.getLogger(ArcObjectsUtil.class);

  private static final String TABLE = ArcObjectsUtil.class.getName() + ".Table";

  private static AoInitialize aoInit;

  static {
    initLicence();
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

  public static void initLicence() {
    System.setProperty("ARCGIS_OPTIMIZED_CASTING", "");
    EngineInitializer.initializeEngine();
    try {
      aoInit = new AoInitialize();
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
  }

  public static void releaseLicence() {
    try {
      aoInit.shutdown();
      aoInit = null;
    } catch (final IOException e) {
      throw new RuntimeException("Unable to shutdown ArcGIS engine linence", e);
    }
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

  public static void close(Workspace workspace) {
    if (workspace != null) {
      workspace.release();
    }
  }

  public static void release(Object object) {
    if (object != null) {
      Cleaner.release(object);
    }
  }

  public static void flush(ICursor cursor) {
    try {
      cursor.flush();
    } catch (final Exception e) {
      throw new RuntimeException("Unable to flush cursor", e);
    }
  }

  public static void flush(IFeatureCursor cursor) {
    try {
      cursor.flush();
    } catch (final Exception e) {
      throw new RuntimeException("Unable to flush cursor", e);
    }
  }

  public static void delete(Workspace workspace) {
    try {
      workspace.delete();
    } catch (final Exception e) {
      throw new RuntimeException("Unable to delete geodatabase", e);
    }
  }

  public static List<CodeTable> getCodeTables(Workspace workspace) {
    List<CodeTable> codeTables = new ArrayList<CodeTable>();
    try {
      final IEnumDomain domains = workspace.getDomains();
      for (IDomain domain = domains.next(); domain != null; domain = domains.next()) {
        CodeTable codeTable = getCodeTable(workspace, domain);
        if (codeTable != null) {
          codeTables.add(codeTable);
        }
      }
    } catch (final Exception e) {
      throw new RuntimeException("Unable to load domain");
    }
    return codeTables;
  }

  public static ITable getITable(final IDataset dataset) {
    try {
      ITable table;
      if (dataset.getType() == esriDatasetType.esriDTTable) {
        table = new Table(dataset);
      } else {
        table = new FeatureClass(dataset);
      }
      return table;
    } catch (final Exception e) {
      throw new RuntimeException("Unable to get table " + dataset);
    }
  }

  public static IDataset getDataset(final Workspace workspace,
    final int datasetType, final String tableName) {
    try {
      final IEnumDataset datasets = workspace.getDatasets(datasetType);
      for (IDataset dataset = datasets.next(); dataset != null; dataset = datasets.next()) {
        final String name = dataset.getName();
        if (name.equals(tableName)) {
          return dataset;
        }
      }
      return null;
    } catch (Exception e) {
      throw new RuntimeException("Unable to get dataset " + tableName, e);
    }
  }

  public static DataObject getNext(IFeatureCursor rows,
    DataObjectMetaData metaData, DataObjectFactory dataObjectFactory) {
    try {
      final IRow row = rows.nextFeature();
      if (row == null) {
        throw new NoSuchElementException();
      } else {
        final DataObject object = dataObjectFactory.createDataObject(metaData);
        for (final Attribute attribute : metaData.getAttributes()) {
          final String name = attribute.getName();
          final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
          final Object value = esriAttribute.getValue(row);
          object.setValue(name, value);
        }
        object.setState(DataObjectState.Persisted);
        return object;
      }
    } catch (final NoSuchElementException e) {
      throw e;
    } catch (final Exception e) {
      throw new RuntimeException("Unable to get next row", e);
    }
  }

  public static DataObject getNext(ICursor rows, DataObjectMetaData metaData,
    DataObjectFactory dataObjectFactory) {
    try {
      final IRow row = rows.nextRow();
      if (row == null) {
        throw new NoSuchElementException();
      } else {
        final DataObject object = dataObjectFactory.createDataObject(metaData);
        for (final Attribute attribute : metaData.getAttributes()) {
          final String name = attribute.getName();
          final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
          final Object value = esriAttribute.getValue(row);
          object.setValue(name, value);
        }
        object.setState(DataObjectState.Persisted);
        return object;
      }
    } catch (final NoSuchElementException e) {
      throw e;
    } catch (final Exception e) {
      throw new RuntimeException("Unable to get next row", e);
    }
  }

  public static IFeatureCursor search(DataObjectMetaData metaData,
    String fields, String whereClause, BoundingBox boundingBox) {
    try {
      final Envelope envelope = new Envelope();

      final double x1 = boundingBox.getMinX();
      final double y1 = boundingBox.getMinY();
      final double x2 = boundingBox.getMaxX();
      final double y2 = boundingBox.getMaxY();
      envelope.setXMin(x1);
      envelope.setYMin(y1);
      envelope.setXMax(x2);
      envelope.setYMax(y2);
      final SpatialFilter query = new SpatialFilter();
      query.setGeometryByRef(envelope);
      query.setSubFields(fields);
      query.setWhereClause(whereClause);
      FeatureClass featureClass = (FeatureClass)getTable(metaData);
      return featureClass.search(query, true);
    } catch (Exception e) {
      throw new RuntimeException("Unable to perform search", e);
    }
  }

  public static void startEditing(final Workspace workspace) {
    try {
      if (!workspace.isBeingEdited()) {
        workspace.startEditing(false);
      }
    } catch (Exception e) {
      throw new RuntimeException("Unable to start editing", e);
    }
  }

  public static void stopEditing(final Workspace workspace) {
    try {
      if (workspace.isBeingEdited()) {
        workspace.stopEditing(true);
      }
    } catch (Exception e) {
      throw new RuntimeException("Unable to stop editing", e);
    }
  }

  public static Object createInsertCursor(final DataObjectMetaData metaData) {
    try {
      final ITable table = getTable(metaData);
      if (table instanceof FeatureClass) {
        FeatureClass featureClass = (FeatureClass)table;
        featureClass.setLoadOnlyMode(true);
        IFeatureCursor cursor = featureClass.IFeatureClass_insert(false);
        return cursor;
      } else {
        ICursor cursor = table.insert(false);
        return cursor;
      }
    } catch (Exception e) {
      throw new RuntimeException("Unable to create insert cursor "
        + metaData.getName(), e);
    }
  }

  public static void setLoadOnlyMode(final DataObjectMetaData metaData,
    boolean loadOnlyMode) throws IOException, AutomationException {
    final ITable table = getTable(metaData);
    setLoadOnlyMode(table, loadOnlyMode);
  }

  public static void setLoadOnlyMode(final ITable table, boolean loadOnlyMode)
    throws IOException, AutomationException {
    if (table instanceof FeatureClass) {
      FeatureClass featureClass = (FeatureClass)table;
      featureClass.setLoadOnlyMode(loadOnlyMode);
    }
  }

  public static Object insert(ICursor cursor, DataObjectMetaData metaData,
    DataObject object) {
    final ITable table = getTable(metaData);
    try {
      IRowBuffer row = table.createRowBuffer();
      try {
        for (final Attribute attribute : metaData.getAttributes()) {
          final String name = attribute.getName();
          final Object value = object.getValue(name);
          final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
          esriAttribute.setInsertValue(row, value);
        }
        Object id;
        id = cursor.insertRow(row);

        object.setValue("OBJECTID", id);
        for (final Attribute attribute : metaData.getAttributes()) {
          final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
          esriAttribute.setPostInsertValue(object, row);
        }

        return id;
      } finally {
        Cleaner.release(row);
      }
    } catch (AutomationException e) {
      throw new RuntimeException("Unable to insert row", e);
    } catch (IOException e) {
      throw new RuntimeException("Unable to insert row", e);
    }
  }

  public static Object insert(IFeatureCursor cursor,
    DataObjectMetaData metaData, DataObject object) {
    final FeatureClass feature = (FeatureClass)getTable(metaData);
    try {
      IFeatureBuffer row = feature.createFeatureBuffer();
      try {
        for (final Attribute attribute : metaData.getAttributes()) {
          final String name = attribute.getName();
          final Object value = object.getValue(name);
          final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
          esriAttribute.setInsertValue(row, value);
        }
        Object id;
        id = cursor.insertFeature(row);

        object.setValue("OBJECTID", id);
        for (final Attribute attribute : metaData.getAttributes()) {
          final AbstractFileGdbAttribute esriAttribute = (AbstractFileGdbAttribute)attribute;
          esriAttribute.setPostInsertValue(object, row);
        }
        return id;
      } finally {
        Cleaner.release(row);
      }
    } catch (AutomationException e) {
      throw new RuntimeException("Unable to insert row", e);
    } catch (IOException e) {
      throw new RuntimeException("Unable to insert row", e);
    }
  }

  public static IRowBuffer createRowBuffer(final DataObjectMetaData metaData) {
    try {
      final ITable table = getTable(metaData);
      if (table instanceof FeatureClass) {
        FeatureClass featureClass = (FeatureClass)table;
        return featureClass.createFeatureBuffer();
      } else {
        return table.createRowBuffer();
      }
    } catch (Exception e) {
      throw new RuntimeException("Unable to create insert cursor "
        + metaData.getName(), e);
    }
  }

  public static Object createDomainValue(Workspace workspace,
    CodedValueDomain domain, Object id, String value) {
    try {
      if (domain.getFieldType() == esriFieldType.esriFieldTypeInteger) {
        id = ((Number)id).intValue();
      } else if (domain.getFieldType() == esriFieldType.esriFieldTypeSmallInteger) {
        id = ((Number)id).shortValue();
      }
      domain.addCode(id, value);
      workspace.alterDomain(domain);
      return id;
    } catch (final Exception e) {
      throw new RuntimeException("Unable to create value " + value, e);
    }
  }

  public static ICursor search(DataObjectMetaData metaData, String fields,
    String whereClause) {
    try {
      final QueryFilter query = new QueryFilter();
      query.setSubFields(fields);
      query.setWhereClause(whereClause);
      ITable table = getTable(metaData);
      return table.ITable_search(query, true);
    } catch (Exception e) {
      throw new RuntimeException("Unable to perform search", e);
    }
  }

  public static List<String> getFeatureDatasetNames(final Workspace workspace) {
    try {
      final IEnumDataset datasets = workspace.getDatasets(esriDatasetType.esriDTFeatureDataset);
      List<String> names = new ArrayList<String>();
      for (IDataset dataset = datasets.next(); dataset != null; dataset = datasets.next()) {
        final String name = dataset.getName();
        names.add(name);
      }
      return names;
    } catch (Exception e) {
      throw new RuntimeException("Unable to get dataset names", e);
    }
  }

  public static FeatureDataset getFeatureDataset(final Workspace workspace,
    final String tableName) {
    final IDataset dataset = getDataset(workspace,
      esriDatasetType.esriDTFeatureDataset, tableName);
    if (dataset == null) {
      return null;
    } else {
      try {
        return new FeatureDataset(dataset);
      } catch (IOException e) {
        throw new RuntimeException("Unable to create feature dataset "
          + tableName, e);
      }
    }
  }

  public static List<DataObjectMetaData> getFeatureClassMetaData(
    final FeatureDataset dataset, final DataObjectStoreSchema schema) {
    List<DataObjectMetaData> metaDataList = new ArrayList<DataObjectMetaData>();
    if (dataset != null) {
      try {
        final IEnumFeatureClass datasets = dataset.getClasses();
        for (IFeatureClass featureClass = datasets.next(); featureClass != null; featureClass = datasets.next()) {
          DataObjectMetaData metaData = getMetaData(schema,
            (IDataset)featureClass);
          metaDataList.add(metaData);
        }
      } catch (final Exception e) {
        throw new RuntimeException("Unable to load metadata ", e);
      }
    }
    return metaDataList;

  }

  public static List<DataObjectMetaData> getDataObjectMetaData(
    final Workspace workspace, final DataObjectStoreSchema schema,
    final int datasetType) {
    List<DataObjectMetaData> metaDataList = new ArrayList<DataObjectMetaData>();
    try {
      final IEnumDataset datasets = workspace.getDatasets(datasetType);
      for (IDataset dataset = datasets.next(); dataset != null; dataset = datasets.next()) {
        DataObjectMetaData metaData = getMetaData(schema, dataset);
        metaDataList.add(metaData);
      }
    } catch (final Exception e) {
      throw new RuntimeException("Unable to load metadata ", e);
    }
    return metaDataList;
  }

  public static List<DataObjectMetaData> getSchemaDataObjectMetaData(
    Workspace workspace, final DataObjectStoreSchema schema,
    String defaultSchema) {
    List<DataObjectMetaData> metaDataList = new ArrayList<DataObjectMetaData>();
    final String schemaName = schema.getName();
    if (schemaName.equals(defaultSchema)) {
      List<DataObjectMetaData> featureClassMetaData = getDataObjectMetaData(
        workspace, schema, esriDatasetType.esriDTFeatureClass);
      metaDataList.addAll(featureClassMetaData);

      List<DataObjectMetaData> tableMetaData = getDataObjectMetaData(workspace,
        schema, esriDatasetType.esriDTTable);
      metaDataList.addAll(tableMetaData);
    }
    FeatureDataset featureDataset = getFeatureDataset(workspace, schemaName);
    List<DataObjectMetaData> featureClassMetaData = getFeatureClassMetaData(
      featureDataset, schema);
    metaDataList.addAll(featureClassMetaData);
    return metaDataList;
  }

  public static DataObjectMetaData getMetaData(
    final DataObjectStoreSchema schema, final IDataset dataset) {
    try {
      final ITable table = getITable(dataset);
      final String tableName = ((IDataset)table).getName();
      String schemaName = schema.getName();
      final QName typeName = new QName(schemaName, tableName);
      DataObjectStore dataStore = schema.getDataObjectStore();
      final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(
        dataStore, schema, typeName);
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
      metaData.setProperty(TABLE, table);
      schema.addMetaData(metaData);
      return metaData;
    } catch (final Exception e) {
      throw new RuntimeException("Unable to get metadata", e);
    }
  }

  public static CodeTable getCodeTable(Workspace workspace,
    final IDomain domain) {
    if (domain instanceof CodedValueDomain) {
      final CodedValueDomain codedValueDomain = (CodedValueDomain)domain;
      final FileGdbDomainCodeTable codeTable = new FileGdbDomainCodeTable(
        workspace, codedValueDomain);
      return codeTable;
    }
    return null;
  }

  public static Workspace createWorkspace(String fileName) {
    try {
      final File file = new File(fileName);
      final FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
      String directoryName = file.getParent();
      String workspaceName = file.getName();
      factory.create(directoryName, workspaceName, null, 0);
      return openWorkspace(fileName);
    } catch (final Exception e) {
      throw new RuntimeException("Unable to open geodatabase " + fileName, e);
    }
  }

  public static Workspace createFromXmlTemplate(String fileName,
    Resource template) {
    try {
      Workspace workspace = createWorkspace(fileName);

      final File file = FileUtil.getFile(template);
      final String templateFileName = file.getAbsolutePath();

      final IGdbXmlImport importer = new GdbImporter();
      final IEnumNameMapping[] enumNameMapping = new IEnumNameMapping[1];
      importer.generateNameMapping(templateFileName, workspace, enumNameMapping);
      importer.importWorkspace(templateFileName, enumNameMapping[0], workspace,
        true);
      file.delete();
      return workspace;
    } catch (final Exception e) {
      throw new RuntimeException("Unable to create workspace from template "
        + template, e);
    }
  }

  public static Workspace openWorkspace(String fileName) {
    try {
      final FileGDBWorkspaceFactory factory = new FileGDBWorkspaceFactory();
      final IWorkspace iWorkspace = factory.openFromFile(fileName, 0);
      final Workspace workspace = new com.esri.arcgis.geodatabase.Workspace(
        iWorkspace);
      workspace.emptyCache();
      return workspace;
    } catch (final Exception e) {
      throw new RuntimeException("Unable to open geodatabase " + fileName, e);
    }
  }

  public static ITable getTable(DataObjectMetaData metaData) {
    return metaData.getProperty(TABLE);
  }
}
