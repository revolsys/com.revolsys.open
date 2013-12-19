package com.revolsys.gis.esri.gdb.file;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.AbstractDataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.query.AbstractMultiCondition;
import com.revolsys.gis.data.query.BinaryCondition;
import com.revolsys.gis.data.query.CollectionValue;
import com.revolsys.gis.data.query.Column;
import com.revolsys.gis.data.query.Condition;
import com.revolsys.gis.data.query.LeftUnaryCondition;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.data.query.QueryValue;
import com.revolsys.gis.data.query.RightUnaryCondition;
import com.revolsys.gis.data.query.SqlCondition;
import com.revolsys.gis.data.query.Value;
import com.revolsys.gis.esri.gdb.file.capi.FileGdbDomainCodeTable;
import com.revolsys.gis.esri.gdb.file.capi.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.capi.swig.Envelope;
import com.revolsys.gis.esri.gdb.file.capi.swig.EsriFileGdb;
import com.revolsys.gis.esri.gdb.file.capi.swig.Geodatabase;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.gis.esri.gdb.file.capi.swig.Table;
import com.revolsys.gis.esri.gdb.file.capi.swig.VectorOfWString;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbAttribute;
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
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.JavaBeanUtil;
import com.vividsolutions.jts.geom.Geometry;

public class CapiFileGdbDataObjectStore extends AbstractDataObjectStore
  implements FileGdbDataObjectStore {
  private static final String CATALOG_PATH_PROPERTY = CapiFileGdbDataObjectStore.class
    + ".CatalogPath";

  private static final Object API_SYNC = new Object();

  private static void addFieldTypeAttributeConstructor(
    final FieldType fieldType,
    final Class<? extends AbstractFileGdbAttribute> attributeClass) {
    try {
      final Constructor<? extends AbstractFileGdbAttribute> constructor = attributeClass.getConstructor(Field.class);
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
      final String wkt;
      synchronized (API_SYNC) {
        wkt = EsriFileGdb.getSpatialReferenceWkt(geometryFactory.getSRID());
      }
      final SpatialReference spatialReference = SpatialReference.get(
        geometryFactory, wkt);
      return spatialReference;
    }
  }

  private final Set<String> loadOnlyByPath = new HashSet<String>();

  private final Map<String, AtomicLong> idGenerators = new HashMap<String, AtomicLong>();

  private Map<String, List<String>> domainColumNames = new HashMap<String, List<String>>();

  private String defaultSchema = "/";

  private static final Logger LOG = LoggerFactory.getLogger(CapiFileGdbDataObjectStore.class);

  private Geodatabase geodatabase;

  private String fileName;

  private boolean createMissingDataStore = true;

  private boolean createMissingTables = true;

  private static final Map<FieldType, Constructor<? extends AbstractFileGdbAttribute>> ESRI_FIELD_TYPE_ATTRIBUTE_MAP = new HashMap<FieldType, Constructor<? extends AbstractFileGdbAttribute>>();

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

  private final Object apiSync = new Object();

  private Resource template;

  private final Map<String, Table> tablesToClose = new HashMap<String, Table>();

  private final Set<EnumRows> enumRowsToClose = new HashSet<EnumRows>();

  private boolean initialized;

  private boolean loadOnly;

  protected CapiFileGdbDataObjectStore(final File file) {
    this.fileName = file.getAbsolutePath();
    setConnectionProperties(Collections.singletonMap("url",
      FileUtil.toUrl(file).toString()));
  }

  public void addChildSchema(final String path) {
    synchronized (apiSync) {
      if (geodatabase != null) {
        final VectorOfWString childDatasets = geodatabase.getChildDatasets(
          path, "Feature Dataset");
        for (int i = 0; i < childDatasets.size(); i++) {
          final String childPath = childDatasets.get(i);
          addFeatureDatasetSchema(childPath);
        }
      }
    }
  }

  @Override
  public void addCodeTable(final CodeTable codeTable) {
    super.addCodeTable(codeTable);
    synchronized (apiSync) {
      if (codeTable instanceof Domain) {
        final Domain domain = (Domain)codeTable;
        createDomain(domain);
      }
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
    synchronized (apiSync) {
      if (geodatabase != null) {
        final String tableDefinition = geodatabase.getTableDefinition(path);
        final DataObjectMetaData metaData = getMetaData(schemaName, path,
          tableDefinition);
        addMetaData(metaData);
      }
    }
  }

  public void alterDomain(final CodedValueDomain domain) {
    final String domainDefinition = EsriGdbXmlSerializer.toString(domain);
    synchronized (apiSync) {
      if (geodatabase != null) {
        geodatabase.alterDomain(domainDefinition);
      }
    }
  }

  private void appendCondition(final StringBuffer buffer,
    final QueryValue condition) {
    if (condition instanceof LeftUnaryCondition) {
      final LeftUnaryCondition unaryCondition = (LeftUnaryCondition)condition;
      final String operator = unaryCondition.getOperator();
      final Condition right = unaryCondition.getQueryValue();
      buffer.append(operator);
      buffer.append(" ");
      appendCondition(buffer, right);
    } else if (condition instanceof RightUnaryCondition) {
      final RightUnaryCondition unaryCondition = (RightUnaryCondition)condition;
      final QueryValue left = unaryCondition.getValue();
      final String operator = unaryCondition.getOperator();
      appendCondition(buffer, left);
      buffer.append(" ");
      buffer.append(operator);
    } else if (condition instanceof BinaryCondition) {
      final BinaryCondition binaryCondition = (BinaryCondition)condition;
      final QueryValue left = binaryCondition.getLeft();
      final String operator = binaryCondition.getOperator();
      final QueryValue right = binaryCondition.getRight();
      appendCondition(buffer, left);
      buffer.append(" ");
      buffer.append(operator);
      buffer.append(" ");
      appendCondition(buffer, right);
    } else if (condition instanceof AbstractMultiCondition) {
      final AbstractMultiCondition multipleCondition = (AbstractMultiCondition)condition;
      buffer.append("(");
      boolean first = true;
      final String operator = multipleCondition.getOperator();
      for (final QueryValue subCondition : multipleCondition.getQueryValues()) {
        if (first) {
          first = false;
        } else {
          buffer.append(" ");
          buffer.append(operator);
          buffer.append(" ");
        }
        appendCondition(buffer, subCondition);
      }
      buffer.append(")");
    } else if (condition instanceof Value) {
      final Value valueCondition = (Value)condition;
      final Object value = valueCondition.getValue();
      appendValue(buffer, value);
    } else if (condition instanceof CollectionValue) {
      final CollectionValue collectionValue = (CollectionValue)condition;
      final List<Object> values = collectionValue.getValues();
      boolean first = true;
      for (final Object value : values) {
        if (first) {
          first = false;
        } else {
          buffer.append(", ");
        }
        appendValue(buffer, value);
      }
    } else if (condition instanceof Column) {
      final Column column = (Column)condition;
      final Object name = column.getName();
      buffer.append(name);
    } else if (condition instanceof SqlCondition) {
      final SqlCondition sqlCondition = (SqlCondition)condition;
      final String where = sqlCondition.getSql();
      final List<Object> parameters = sqlCondition.getParameterValues();
      if (parameters.isEmpty()) {
        if (where.indexOf('?') > -1) {
          throw new IllegalArgumentException(
            "No arguments specified for a where clause with placeholders: "
              + where);
        } else {
          buffer.append(where);
        }
      } else {
        final Matcher matcher = PLACEHOLDER_PATTERN.matcher(where);
        int i = 0;
        while (matcher.find()) {
          if (i >= parameters.size()) {
            throw new IllegalArgumentException(
              "Not enough arguments for where clause with placeholders: "
                + where);
          }
          final Object argument = parameters.get(i);
          matcher.appendReplacement(buffer,
            StringConverterRegistry.toString(argument));
          appendValue(buffer, argument);
          i++;
        }
        matcher.appendTail(buffer);
      }

    } else {
      condition.appendSql(buffer);
    }
  }

  public void appendValue(final StringBuffer buffer, final Object value) {
    if (value instanceof Number) {
      buffer.append(value);
    } else if (value == null) {
      buffer.append("''");
    } else {
      buffer.append("'");
      buffer.append(StringConverterRegistry.toString(value).replaceAll("'",
        "''"));
      buffer.append("'");
    }
  }

  @Override
  @PreDestroy
  public void close() {
    FileGdbDataObjectStoreFactory.release(fileName);
  }

  protected void closeEnumRows() {
    synchronized (apiSync) {
      for (final Iterator<EnumRows> iterator = enumRowsToClose.iterator(); iterator.hasNext();) {
        final EnumRows rows = iterator.next();
        try {
          rows.Close();
        } catch (final Throwable e) {
        } finally {
          rows.delete();
        }
        iterator.remove();
      }
      enumRowsToClose.clear();
    }
  }

  public void closeEnumRows(final EnumRows rows) {
    synchronized (apiSync) {
      if (isOpen(rows)) {
        try {
          rows.Close();
        } catch (final Throwable e) {
        } finally {
          try {
            rows.delete();
          } catch (final Throwable t) {
          }
        }
        enumRowsToClose.remove(rows);
      }
    }
  }

  protected void closeRow(final Row row) {
    if (row != null) {
      synchronized (apiSync) {
        row.delete();
      }
    }
  }

  protected void closeTables() {
    synchronized (apiSync) {
      for (final Iterator<Entry<String, Table>> iterator = tablesToClose.entrySet()
        .iterator(); iterator.hasNext();) {
        final Entry<String, Table> entry = iterator.next();
        final Table table = entry.getValue();
        try {
          if (geodatabase != null) {
            geodatabase.closeTable(table);
          }
        } catch (final Throwable e) {
        } finally {
          try {
            table.delete();
          } catch (final Throwable t) {
          }
        }
        iterator.remove();
      }
      tablesToClose.clear();
    }
  }

  public synchronized void createDomain(final Domain domain) {
    if (geodatabase != null) {
      final String domainName = domain.getDomainName();
      if (!domainColumNames.containsKey(domainName)) {
        synchronized (apiSync) {
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
    }
  }

  // TODO add bounding box
  @Override
  public AbstractIterator<DataObject> createIterator(final Query query,
    final Map<String, Object> properties) {
    synchronized (apiSync) {
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
      final BoundingBox boundingBox = query.getBoundingBox();
      final Map<String, Boolean> orderBy = query.getOrderBy();
      final StringBuffer whereClause = getWhereClause(query);
      StringBuffer sql = new StringBuffer();
      if (orderBy.isEmpty() || boundingBox != null) {
        if (!orderBy.isEmpty()) {
          LoggerFactory.getLogger(getClass()).error(
            "Unable to sort on " + metaData.getPath() + " " + orderBy.keySet()
              + " as the ESRI library can't sort with a bounding box query");
        }
        sql = whereClause;
      } else {
        sql.append("SELECT ");

        final List<String> attributeNames = query.getAttributeNames();
        if (attributeNames.isEmpty()) {
          CollectionUtil.append(sql, metaData.getAttributeNames());
        } else {
          CollectionUtil.append(sql, attributeNames);
        }
        sql.append(" FROM ");
        sql.append(JdbcUtils.getTableName(typePath));
        if (whereClause.length() > 0) {
          sql.append(" WHERE ");
          sql.append(whereClause);
        }
        boolean first = true;
        for (final Iterator<Entry<String, Boolean>> iterator = orderBy.entrySet()
          .iterator(); iterator.hasNext();) {
          final Entry<String, Boolean> entry = iterator.next();
          final String column = entry.getKey();
          final DataType dataType = metaData.getAttributeType(column);
          // TODO at the moment only numbers are supported
          if (dataType != null
            && Number.class.isAssignableFrom(dataType.getJavaClass())) {
            if (first) {
              sql.append(" ORDER BY ");
              first = false;
            } else {
              sql.append(", ");
            }
            sql.append(column);
            final Boolean ascending = entry.getValue();
            if (!ascending) {
              sql.append(" DESC");
            }

          } else {
            LoggerFactory.getLogger(getClass())
              .error(
                "Unable to sort on " + metaData.getPath() + "." + column
                  + " as the ESRI library can't sort on " + dataType
                  + " columns");
          }
        }
      }

      final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this,
        typePath, sql.toString(), boundingBox, query, query.getOffset(),
        query.getLimit());
      return iterator;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T createPrimaryIdValue(final String typePath) {
    synchronized (apiSync) {
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
  }

  protected Row createRowObject(final Table table) {
    synchronized (apiSync) {
      if (isOpen(table)) {
        return table.createRowObject();
      } else {
        return null;
      }
    }
  }

  private DataObjectStoreSchema createSchema(final DETable table) {
    synchronized (apiSync) {
      if (geodatabase == null) {
        return null;
      } else {
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
            throw new RuntimeException("Unable to create feature dataset "
              + path, t);
          }
        }
        return getSchema(catalogPath.replaceAll("\\\\", "/"));
      }
    }
  }

  public void createSchema(final String schemaName,
    final GeometryFactory geometryFactory) {
    synchronized (apiSync) {
      if (geodatabase != null) {
        final SpatialReference spatialReference = getSpatialReference(geometryFactory);
        final List<DEFeatureDataset> datasets = EsriXmlDataObjectMetaDataUtil.createDEFeatureDatasets(
          schemaName.replaceAll("/", ""), spatialReference);
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
            throw new RuntimeException("Unable to create feature dataset "
              + path, t);
          }
        }
      }
    }
  }

  private DataObjectMetaData createTable(final DataObjectMetaData objectMetaData) {
    synchronized (apiSync) {
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
  }

  protected DataObjectMetaDataImpl createTable(final DETable deTable) {
    synchronized (apiSync) {
      if (geodatabase == null) {
        return null;
      } else {
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
        try {
          final Table table = geodatabase.createTable(tableDefinition,
            schemaPath);
          final DataObjectMetaDataImpl metaData = getMetaData(schemaName,
            schemaPath, tableDefinition);
          addMetaData(metaData);
          if (loadOnly) {
            table.setWriteLock();
            table.setLoadOnlyMode(loadOnly);
          }
          tablesToClose.put(metaData.getPath(), table);
          return metaData;

        } catch (final Throwable t) {
          if (LOG.isDebugEnabled()) {
            LOG.debug(tableDefinition);
          }
          throw new RuntimeException("Unable to create table "
            + deTable.getCatalogPath(), t);
        }
      }
    }
  }

  @Override
  public Writer<DataObject> createWriter() {
    synchronized (apiSync) {
      return new FileGdbWriter(this);
    }
  }

  @Override
  public void delete(final DataObject object) {
    // Don't synchronize to avoid deadlock as that is done lower down in the
    // methods
    if (object.getState() == DataObjectState.Persisted
      || object.getState() == DataObjectState.Modified) {
      object.setState(DataObjectState.Deleted);
      final Writer<DataObject> writer = getWriter();
      writer.write(object);
    }
  }

  protected void deletedRow(final Table table, final Row row) {
    synchronized (apiSync) {
      if (isOpen(table)) {
        table.deleteRow(row);
      }
    }
  }

  @Override
  public void deleteGeodatabase() {
    synchronized (apiSync) {
      synchronized (API_SYNC) {
        final String fileName = this.fileName;
        try {
          doClose();
        } finally {
          if (new File(fileName).exists()) {
            EsriFileGdb.DeleteGeodatabase(fileName);
          }
        }
      }
    }
  }

  void doClose() {
    try {
      synchronized (apiSync) {
        synchronized (API_SYNC) {
          if (geodatabase != null) {
            closeEnumRows();
            closeTables();
            try {
              EsriFileGdb.CloseGeodatabase(this.geodatabase);
            } finally {
              this.geodatabase = null;
            }
          }
        }
      }
    } finally {
      super.close();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
  }

  protected void freeWriteLock(final Table table) {
    synchronized (apiSync) {
      if (isOpen(table)) {
        table.freeWriteLock();
      }
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

  @Override
  public DataObjectMetaData getMetaData(final DataObjectMetaData objectMetaData) {
    synchronized (apiSync) {
      DataObjectMetaData metaData = super.getMetaData(objectMetaData);
      if (createMissingTables && metaData == null) {
        metaData = createTable(objectMetaData);
      }
      return metaData;
    }
  }

  public DataObjectMetaDataImpl getMetaData(final String schemaName,
    final String path, final String tableDefinition) {
    synchronized (apiSync) {
      try {
        final XmlProcessor parser = new EsriGdbXmlParser();
        final DETable deTable = parser.process(tableDefinition);
        final String tableName = deTable.getName();
        final String typePath = PathUtil.toPath(schemaName, tableName);
        final DataObjectStoreSchema schema = getSchema(schemaName);
        final DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(
          this, schema, typePath);
        for (final Field field : deTable.getFields()) {
          final String fieldName = field.getName();
          final FieldType type = field.getType();
          final Constructor<? extends AbstractFileGdbAttribute> attributeConstructor = ESRI_FIELD_TYPE_ATTRIBUTE_MAP.get(type);
          if (attributeConstructor != null) {
            try {
              final AbstractFileGdbAttribute attribute = JavaBeanUtil.invokeConstructor(
                attributeConstructor, field);
              attribute.setDataStore(this);
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
  }

  @Override
  public int getRowCount(final Query query) {
    if (query == null) {
      return 0;
    } else {
      synchronized (apiSync) {
        String typePath = query.getTypeName();
        DataObjectMetaData metaData = query.getMetaData();
        if (metaData == null) {
          typePath = query.getTypeName();
          metaData = getMetaData(typePath);
          if (metaData == null) {
            return 0;
          }
        } else {
          typePath = metaData.getPath();
        }
        final StringBuffer whereClause = getWhereClause(query);
        final BoundingBox boundingBox = query.getBoundingBox();

        if (boundingBox == null) {
          final StringBuffer sql = new StringBuffer();
          sql.append("SELECT OBJECTID FROM ");
          sql.append(JdbcUtils.getTableName(typePath));
          if (whereClause.length() > 0) {
            sql.append(" WHERE ");
            sql.append(whereClause);
          }

          final EnumRows rows = query(sql.toString(), false);
          try {
            int count = 0;
            for (Row row = rows.next(); row != null; row = rows.next()) {
              count++;
              row.delete();
            }
            return count;
          } finally {
            closeEnumRows(rows);
          }
        } else {
          final GeometryAttribute geometryAttribute = (GeometryAttribute)metaData.getGeometryAttribute();
          if (geometryAttribute == null || boundingBox.isEmpty()) {
            return 0;
          } else {
            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT " + geometryAttribute.getName() + " FROM ");
            sql.append(JdbcUtils.getTableName(typePath));
            if (whereClause.length() > 0) {
              sql.append(" WHERE ");
              sql.append(whereClause);
            }

            final EnumRows rows = query(sql.toString(), false);
            try {
              int count = 0;
              for (Row row = rows.next(); row != null; row = rows.next()) {
                final Geometry geometry = (Geometry)geometryAttribute.getValue(row);
                final BoundingBox geometryBoundingBox = BoundingBox.getBoundingBox(geometry);
                if (geometryBoundingBox.intersects(boundingBox)) {
                  count++;
                }
                row.delete();
              }
              return count;
            } finally {
              closeEnumRows(rows);
            }
          }
        }
      }
    }
  }

  protected Table getTable(final String typePath) {
    synchronized (apiSync) {
      if (geodatabase == null || getMetaData(typePath) == null) {
        return null;
      } else {
        final String path = typePath.replaceAll("/", "\\\\");
        try {

          Table table = tablesToClose.get(typePath);
          if (table == null) {
            table = geodatabase.openTable(path);
            table.setLoadOnlyMode(loadOnly);
            tablesToClose.put(typePath, table);
          }
          return table;
        } catch (final RuntimeException e) {
          throw new RuntimeException("Unable to open table " + typePath, e);
        }
      }
    }
  }

  public Resource getTemplate() {
    return template;
  }

  protected StringBuffer getWhereClause(final Query query) {
    final StringBuffer whereClause = new StringBuffer();
    final Condition whereCondition = query.getWhereCondition();
    if (whereCondition != null) {
      appendCondition(whereClause, whereCondition);
    }
    return whereClause;
  }

  @Override
  public Writer<DataObject> getWriter() {
    synchronized (apiSync) {
      Writer<DataObject> writer = getSharedAttribute("writer");
      if (writer == null) {
        writer = createWriter();
        setSharedAttribute("writer", writer);
      }
      return writer;
    }
  }

  @Override
  @PostConstruct
  public void initialize() {
    synchronized (apiSync) {
      synchronized (API_SYNC) {
        if (!initialized) {
          initialized = true;
          try {
            super.initialize();
            final File file = new File(fileName);
            if (file.exists()) {
              if (file.isDirectory()) {
                if (new File(fileName, "gdb").exists()) {
                  geodatabase = EsriFileGdb.openGeodatabase(fileName);
                } else {
                  throw new IllegalArgumentException(
                    FileUtil.getCanonicalPath(file)
                      + " is not a valid ESRI File Geodatabase");
                }
              } else {
                throw new IllegalArgumentException(
                  FileUtil.getCanonicalPath(file)
                    + " ESRI File Geodatabase must be a directory");
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
                    } catch (final Throwable e) {
                      throw new IllegalArgumentException(
                        "Unable to copy template ESRI geodatabase " + template,
                        e);
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
              throw new IllegalArgumentException(
                "ESRI file geodatabase not found " + fileName);
            }
            final VectorOfWString domainNames = geodatabase.getDomains();
            for (int i = 0; i < domainNames.size(); i++) {
              final String domainName = domainNames.get(i);
              loadDomain(domainName);
            }
          } catch (final Throwable e) {
            geodatabase = null;
            ExceptionUtil.throwUncheckedException(e);
          }
        }
      }
    }
  }

  @Override
  public void insert(final DataObject object) {
    // Don't synchronize to avoid deadlock as that is done lower down in the
    // methods
    getWriter().write(object);
  }

  protected void insertRow(final Table table, final Row row) {
    synchronized (apiSync) {
      if (isOpen(table)) {
        table.insertRow(row);
      }
    }
  }

  public boolean isCreateMissingDataStore() {
    return createMissingDataStore;
  }

  public boolean isCreateMissingTables() {
    return createMissingTables;
  }

  public boolean isLoadOnly(final String typePath) {
    return loadOnlyByPath.contains(typePath);
  }

  public boolean isNull(final Row row, final String name) {
    synchronized (apiSync) {
      return row.isNull(name);
    }
  }

  public boolean isOpen(final EnumRows enumRows) {
    synchronized (apiSync) {
      if (enumRows == null) {
        return false;
      } else {
        return enumRowsToClose.contains(enumRows);
      }
    }
  }

  public boolean isOpen(final Table table) {
    synchronized (apiSync) {
      if (table == null) {
        return false;
      } else {
        return tablesToClose.containsValue(table);
      }
    }
  }

  @Override
  public DataObject load(final String typePath, final Object id) {
    synchronized (apiSync) {
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
  }

  protected void loadDomain(final String domainName) {
    synchronized (apiSync) {
      if (geodatabase != null) {
        final String domainDef = geodatabase.getDomainDefinition(domainName);
        final Domain domain = EsriGdbXmlParser.parse(domainDef);
        if (domain instanceof CodedValueDomain) {
          final CodedValueDomain codedValueDomain = (CodedValueDomain)domain;
          final FileGdbDomainCodeTable codeTable = new FileGdbDomainCodeTable(
            this, codedValueDomain);
          super.addCodeTable(codeTable);
          final List<String> columnNames = domainColumNames.get(domainName);
          if (columnNames != null) {
            for (final String columnName : columnNames) {
              addCodeTable(columnName, codeTable);
            }
          }
        }
      }
    }
  }

  @Override
  protected void loadSchemaDataObjectMetaData(
    final DataObjectStoreSchema schema,
    final Map<String, DataObjectMetaData> metaDataMap) {
    synchronized (apiSync) {
      final String schemaName = schema.getPath();
      if (schemaName.equals(defaultSchema)) {
        loadSchemaDataObjectMetaData(metaDataMap, schemaName, "\\",
          "Feature Class");
        loadSchemaDataObjectMetaData(metaDataMap, schemaName, "\\", "Table");
      }
      final String path = schemaName.replaceAll("/", "\\\\");
      loadSchemaDataObjectMetaData(metaDataMap, schemaName, path,
        "Feature Class");
      loadSchemaDataObjectMetaData(metaDataMap, schemaName, path, "Table");
    }
  }

  public void loadSchemaDataObjectMetaData(
    final Map<String, DataObjectMetaData> metaDataMap, final String schemaName,
    final String path, final String datasetType) {
    synchronized (apiSync) {
      if (geodatabase != null) {
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
    }
  }

  @Override
  protected void loadSchemas(final Map<String, DataObjectStoreSchema> schemaMap) {
    synchronized (apiSync) {
      addSchema(new DataObjectStoreSchema(this, defaultSchema));
      addChildSchema("\\");
    }
  }

  protected Row nextRow(final EnumRows rows) {
    synchronized (apiSync) {
      if (isOpen(rows)) {
        return rows.next();
      } else {
        return null;
      }
    }
  }

  public EnumRows query(final String sql, final boolean recycling) {
    synchronized (apiSync) {
      final Geodatabase geodatabase = this.geodatabase;
      if (geodatabase == null) {
        return null;
      } else {
        try {
          final EnumRows enumRows = geodatabase.query(sql, recycling);
          enumRowsToClose.add(enumRows);
          return enumRows;
        } catch (final Throwable t) {
          throw new RuntimeException("Error running sql: " + sql, t);
        }
      }
    }
  }

  public EnumRows search(final Table table, final String fields,
    final String whereClause, final boolean recycling) {
    synchronized (apiSync) {
      if (isOpen(table)) {
        try {
          final EnumRows rows = table.search(fields, whereClause, recycling);
          enumRowsToClose.add(rows);
          return rows;
        } catch (final Throwable t) {
          LoggerFactory.getLogger(getClass()).error(
            "Unable to execute query " + fields + " WHERE " + whereClause, t);

        }
      }
      return null;
    }
  }

  public EnumRows search(final Table table, final String fields,
    final String whereClause, final Envelope boundingBox,
    final boolean recycling) {
    synchronized (apiSync) {
      if (isOpen(table)) {
        try {
          final EnumRows rows = table.search(fields, whereClause, boundingBox,
            recycling);
          enumRowsToClose.add(rows);
          return rows;
        } catch (final Exception e) {
          LOG.error("ERROR executing query SELECT " + fields + " WHERE "
            + whereClause + " AND " + boundingBox, e);
        }
      }
      return null;
    }
  }

  @Override
  public void setCreateMissingDataStore(final boolean createMissingDataStore) {
    this.createMissingDataStore = createMissingDataStore;
  }

  @Override
  public void setCreateMissingTables(final boolean createMissingTables) {
    this.createMissingTables = createMissingTables;
  }

  @Override
  public void setDefaultSchema(final String defaultSchema) {
    synchronized (apiSync) {
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
  }

  public void setDomainColumNames(
    final Map<String, List<String>> domainColumNames) {
    this.domainColumNames = domainColumNames;
  }

  public void setFileName(final String fileName) {
    this.fileName = fileName;
  }

  public void setLoadOnly(final boolean loadOnly) {
    synchronized (apiSync) {
      this.loadOnly = loadOnly;
      for (final Table table : tablesToClose.values()) {
        table.setLoadOnlyMode(loadOnly);
      }
    }
  }

  public void setLoadOnly(final String typePath, final boolean loadOnly) {
    synchronized (apiSync) {
      final boolean oldLoadOnly = isLoadOnly(typePath);
      if (oldLoadOnly != loadOnly) {
        getTable(typePath).setLoadOnlyMode(loadOnly);
      }
      if (loadOnly) {
        if (!oldLoadOnly) {
          loadOnlyByPath.add(typePath);
        }
      } else {
        if (oldLoadOnly) {
          loadOnlyByPath.remove(typePath);
        }
      }
    }
  }

  public void setNull(final Row row, final String name) {
    synchronized (apiSync) {
      row.setNull(name);
    }
  }

  public void setTemplate(final Resource template) {
    this.template = template;
  }

  protected void setWriteLock(final Table table) {
    synchronized (apiSync) {
      if (isOpen(table)) {
        table.setWriteLock();
      }
    }
  }

  @Override
  public String toString() {
    return fileName;
  }

  @Override
  public void update(final DataObject object) {
    // Don't synchronize to avoid deadlock as that is done lower down in the
    // methods
    getWriter().write(object);
  }

  protected void updateRow(final Table table, final Row row) {
    synchronized (apiSync) {
      if (isOpen(table)) {
        table.updateRow(row);
      }
    }
  }
}
