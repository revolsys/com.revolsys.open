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
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.Maps;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.identifier.SingleIdentifier;
import com.revolsys.data.query.AbstractMultiCondition;
import com.revolsys.data.query.BinaryCondition;
import com.revolsys.data.query.CollectionValue;
import com.revolsys.data.query.Column;
import com.revolsys.data.query.Condition;
import com.revolsys.data.query.ILike;
import com.revolsys.data.query.In;
import com.revolsys.data.query.LeftUnaryCondition;
import com.revolsys.data.query.Like;
import com.revolsys.data.query.Query;
import com.revolsys.data.query.QueryValue;
import com.revolsys.data.query.RightUnaryCondition;
import com.revolsys.data.query.SqlCondition;
import com.revolsys.data.query.Value;
import com.revolsys.data.query.functions.EnvelopeIntersects;
import com.revolsys.data.query.functions.WithinDistance;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.AbstractRecordStore;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.data.record.schema.RecordStoreSchemaElement;
import com.revolsys.data.types.DataType;
import com.revolsys.format.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.format.esri.gdb.xml.model.CodedValueDomain;
import com.revolsys.format.esri.gdb.xml.model.DEFeatureClass;
import com.revolsys.format.esri.gdb.xml.model.DEFeatureDataset;
import com.revolsys.format.esri.gdb.xml.model.DETable;
import com.revolsys.format.esri.gdb.xml.model.Domain;
import com.revolsys.format.esri.gdb.xml.model.EsriGdbXmlParser;
import com.revolsys.format.esri.gdb.xml.model.EsriGdbXmlSerializer;
import com.revolsys.format.esri.gdb.xml.model.EsriXmlRecordDefinitionUtil;
import com.revolsys.format.esri.gdb.xml.model.Field;
import com.revolsys.format.esri.gdb.xml.model.Index;
import com.revolsys.format.esri.gdb.xml.model.SpatialReference;
import com.revolsys.format.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.format.xml.XmlProcessor;
import com.revolsys.gis.esri.gdb.file.capi.FileGdbDomainCodeTable;
import com.revolsys.gis.esri.gdb.file.capi.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.capi.swig.Envelope;
import com.revolsys.gis.esri.gdb.file.capi.swig.EsriFileGdb;
import com.revolsys.gis.esri.gdb.file.capi.swig.Geodatabase;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.gis.esri.gdb.file.capi.swig.Table;
import com.revolsys.gis.esri.gdb.file.capi.swig.VectorOfWString;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.BinaryFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.DateFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.DoubleFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.FloatFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.GeometryFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.GlobalIdFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.GuidFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.IntegerFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.OidFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.ShortFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.StringFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.XmlFieldDefinition;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Path;
import com.revolsys.io.Writer;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.DateUtil;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public class FileGdbRecordStore extends AbstractRecordStore {
  static final Object API_SYNC = new Object();

  private static final Map<FieldType, Constructor<? extends AbstractFileGdbFieldDefinition>> ESRI_FIELD_TYPE_ATTRIBUTE_MAP = new HashMap<FieldType, Constructor<? extends AbstractFileGdbFieldDefinition>>();

  private static final Logger LOG = LoggerFactory.getLogger(FileGdbRecordStore.class);

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\?");

  static {
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeInteger,
      IntegerFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeSmallInteger,
      ShortFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeDouble,
      DoubleFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeSingle,
      FloatFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeString,
      StringFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeDate,
      DateFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeGeometry,
      GeometryFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeOID,
      OidFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeBlob,
      BinaryFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeGlobalID,
      GlobalIdFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeGUID,
      GuidFieldDefinition.class);
    addFieldTypeAttributeConstructor(FieldType.esriFieldTypeXML,
      XmlFieldDefinition.class);

  }

  private static void addFieldTypeAttributeConstructor(
    final FieldType fieldType,
    final Class<? extends AbstractFileGdbFieldDefinition> fieldClass) {
    try {
      final Constructor<? extends AbstractFileGdbFieldDefinition> constructor = fieldClass.getConstructor(Field.class);
      ESRI_FIELD_TYPE_ATTRIBUTE_MAP.put(fieldType, constructor);
    } catch (final SecurityException e) {
      LOG.error("No public constructor for ESRI type " + fieldType, e);
    } catch (final NoSuchMethodException e) {
      LOG.error("No public constructor for ESRI type " + fieldType, e);
    }

  }

  public static SpatialReference getSpatialReference(
    final GeometryFactory geometryFactory) {
    if (geometryFactory == null || geometryFactory.getSrid() == 0) {
      return null;
    } else {
      final String wkt;
      synchronized (API_SYNC) {
        wkt = EsriFileGdb.getSpatialReferenceWkt(geometryFactory.getSrid());
      }
      final SpatialReference spatialReference = SpatialReference.get(
        geometryFactory, wkt);
      return spatialReference;
    }
  }

  private final Object apiSync = new Object();

  private final Map<String, String> catalogPathByPath = new HashMap<>();

  private boolean closed = false;

  private boolean createMissingRecordStore = true;

  private boolean createMissingTables = true;

  private String defaultSchemaPath = "/";

  private Map<String, List<String>> domainFieldNames = new HashMap<>();

  private final Set<EnumRows> enumRowsToClose = new HashSet<>();

  private boolean exists = false;

  private String fileName;

  private Geodatabase geodatabase;

  private int geodatabaseReferenceCount;

  private final Map<String, AtomicLong> idGenerators = new HashMap<>();

  private boolean initialized;

  private final Map<String, Integer> tableReferenceCountsByTypePath = new HashMap<>();

  private final Map<String, Table> tablesToClose = new HashMap<>();

  private final Map<String, Integer> writeLockCountsByTypePath = new HashMap<>();

  protected FileGdbRecordStore(final File file) {
    fileName = file.getAbsolutePath();
    setConnectionProperties(Collections.singletonMap("url",
      FileUtil.toUrl(file).toString()));
    catalogPathByPath.put("/", "\\");
  }

  @Override
  public void addCodeTable(final CodeTable codeTable) {
    super.addCodeTable(codeTable);
    synchronized (apiSync) {
      synchronized (API_SYNC) {
        if (codeTable instanceof Domain) {
          final Domain domain = (Domain)codeTable;
          createDomain(domain);
        }
      }
    }
  }

  public void alterDomain(final CodedValueDomain domain) {
    final String domainDefinition = EsriGdbXmlSerializer.toString(domain);
    synchronized (apiSync) {
      synchronized (API_SYNC) {
        final Geodatabase geodatabase = getGeodatabase();
        if (geodatabase != null) {
          try {
            geodatabase.alterDomain(domainDefinition);
          } finally {
            releaseGeodatabase();
          }
        }
      }
    }
  }

  @Override
  public void appendQueryValue(final Query query, final StringBuilder buffer,
    final QueryValue condition) {
    if (condition instanceof Like || condition instanceof ILike) {
      final BinaryCondition like = (BinaryCondition)condition;
      final QueryValue left = like.getLeft();
      final QueryValue right = like.getRight();
      buffer.append("UPPER(CAST(");
      appendQueryValue(query, buffer, left);
      buffer.append(" AS VARCHAR(4000))) LIKE ");
      if (right instanceof Value) {
        final Value valueCondition = (Value)right;
        final Object value = valueCondition.getValue();
        buffer.append("'");
        if (value != null) {
          final String string = StringConverterRegistry.toString(value);
          buffer.append(string.toUpperCase().replaceAll("'", "''"));
        }
        buffer.append("'");
      } else {
        appendQueryValue(query, buffer, right);
      }
    } else if (condition instanceof LeftUnaryCondition) {
      final LeftUnaryCondition unaryCondition = (LeftUnaryCondition)condition;
      final String operator = unaryCondition.getOperator();
      final QueryValue right = unaryCondition.getQueryValue();
      buffer.append(operator);
      buffer.append(" ");
      appendQueryValue(query, buffer, right);
    } else if (condition instanceof RightUnaryCondition) {
      final RightUnaryCondition unaryCondition = (RightUnaryCondition)condition;
      final QueryValue left = unaryCondition.getValue();
      final String operator = unaryCondition.getOperator();
      appendQueryValue(query, buffer, left);
      buffer.append(" ");
      buffer.append(operator);
    } else if (condition instanceof BinaryCondition) {
      final BinaryCondition binaryCondition = (BinaryCondition)condition;
      final QueryValue left = binaryCondition.getLeft();
      final String operator = binaryCondition.getOperator();
      final QueryValue right = binaryCondition.getRight();
      appendQueryValue(query, buffer, left);
      buffer.append(" ");
      buffer.append(operator);
      buffer.append(" ");
      appendQueryValue(query, buffer, right);
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
        appendQueryValue(query, buffer, subCondition);
      }
      buffer.append(")");
    } else if (condition instanceof In) {
      final In in = (In)condition;
      if (in.isEmpty()) {
        buffer.append("1==1");
      } else {
        final QueryValue left = in.getLeft();
        appendQueryValue(query, buffer, left);
        buffer.append(" IN (");
        appendQueryValue(query, buffer, in.getValues());
        buffer.append(")");
      }
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
          final StringBuffer replacement = new StringBuffer();
          matcher.appendReplacement(replacement,
            StringConverterRegistry.toString(argument));
          buffer.append(replacement);
          appendValue(buffer, argument);
          i++;
        }
        final StringBuffer tail = new StringBuffer();
        matcher.appendTail(tail);
        buffer.append(tail);
      }
    } else if (condition instanceof EnvelopeIntersects) {
      buffer.append("1 = 1");
    } else if (condition instanceof WithinDistance) {
      buffer.append("1 = 1");
    } else {
      condition.appendDefaultSql(query, this, buffer);
    }
  }

  public void appendValue(final StringBuilder buffer, Object value) {
    if (value instanceof SingleIdentifier) {
      final SingleIdentifier identifier = (SingleIdentifier)value;
      value = identifier.getValue(0);
    }
    if (value == null) {
      buffer.append("''");
    } else if (value instanceof Number) {
      buffer.append(value);
    } else if (value instanceof java.util.Date) {
      final String stringValue = DateUtil.format("yyyy-MM-dd",
        (java.util.Date)value);
      buffer.append("DATE '" + stringValue + "'");
    } else {
      final String stringValue = StringConverterRegistry.toString(value);
      buffer.append("'");
      buffer.append(stringValue.replaceAll("'", "''"));
      buffer.append("'");
    }
  }

  @Override
  @PreDestroy
  public void close() {
    if (!FileGdbRecordStoreFactory.release(fileName)) {
      doClose();
    }
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

  private void closeGeodatabase(final Geodatabase geodatabase) {
    if (geodatabase != null) {
      synchronized (API_SYNC) {
        EsriFileGdb.CloseGeodatabase(geodatabase);
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

  public boolean closeTable(final String typePath) {
    synchronized (apiSync) {
      int count = Maps.getInteger(tableReferenceCountsByTypePath, typePath, 0);
      count--;
      if (count <= 0) {
        tableReferenceCountsByTypePath.remove(typePath);
        final Table table = tablesToClose.remove(typePath);
        closeTable(typePath, table);
        return true;
      } else {
        tableReferenceCountsByTypePath.put(typePath, count);
        return false;
      }
    }
  }

  private void closeTable(final String typePath, final Table table) {
    synchronized (API_SYNC) {
      if (table != null) {
        try {
          if (!isClosed()) {
            getGeodatabase().closeTable(table);
          }
        } catch (final Throwable e) {
          LoggerFactory.getLogger(getClass()).error(
            "Cannot close Table " + typePath, e);
        } finally {
          tablesToClose.remove(typePath);
          tableReferenceCountsByTypePath.remove(typePath);
          try {
            table.delete();
          } catch (final Throwable t) {
          }
        }
      }
      if (tablesToClose.isEmpty() && geodatabase != null) {
        try {
          EsriFileGdb.CloseGeodatabase(geodatabase);
        } finally {
          geodatabase = null;
        }
      }
    }
  }

  private void closeTables() {
    synchronized (apiSync) {
      if (!tablesToClose.isEmpty()) {
        final Geodatabase geodatabase = getGeodatabase();
        if (geodatabase != null) {
          try {
            for (final Entry<String, Table> entry : tablesToClose.entrySet()) {
              final Table table = entry.getValue();
              try {
                table.setLoadOnlyMode(false);
                table.freeWriteLock();
                geodatabase.closeTable(table);
              } catch (final Throwable e) {
              } finally {
                try {
                  table.delete();
                } catch (final Throwable t) {
                }
              }
            }
            tablesToClose.clear();
            tableReferenceCountsByTypePath.clear();
            writeLockCountsByTypePath.clear();
          } finally {
            releaseGeodatabase();
          }
        }
      }
    }
  }

  public synchronized void createDomain(final Domain domain) {
    synchronized (apiSync) {
      final Geodatabase geodatabase = getGeodatabase();
      if (geodatabase != null) {
        try {
          final String domainName = domain.getDomainName();
          if (!domainFieldNames.containsKey(domainName)) {
            synchronized (API_SYNC) {
              final String domainDef = EsriGdbXmlSerializer.toString(domain);
              try {
                getGeodatabase().createDomain(domainDef);
              } catch (final Exception e) {
                LOG.debug(domainDef);
                LOG.error("Unable to create domain", e);
              }
              loadDomain(geodatabase, domain.getDomainName());
            }
          }
        } finally {
          releaseGeodatabase();
        }
      }
    }
  }

  private RecordStoreSchema createFeatureDatasetSchema(
    final RecordStoreSchema parentSchema, final String catalogPath) {
    final String schemaPath = toPath(catalogPath);
    final RecordStoreSchema schema = new RecordStoreSchema(parentSchema,
      schemaPath);
    catalogPathByPath.put(schemaPath, catalogPath.replaceAll("/", "\\\\"));
    return schema;
  }

  private Geodatabase createGeodatabase() {
    Geodatabase geodatabase;
    synchronized (API_SYNC) {
      geodatabase = EsriFileGdb.createGeodatabase(fileName);
    }
    return geodatabase;
  }

  @Override
  public AbstractIterator<Record> createIterator(final Query query,
    final Map<String, Object> properties) {
    String typePath = query.getTypeName();
    RecordDefinition recordDefinition = query.getRecordDefinition();
    if (recordDefinition == null) {
      typePath = query.getTypeName();
      recordDefinition = getRecordDefinition(typePath);
      if (recordDefinition == null) {
        throw new IllegalArgumentException("Type name does not exist "
          + typePath);
      }
    } else {
      typePath = recordDefinition.getPath();
    }
    final BoundingBox boundingBox = QueryValue.getBoundingBox(query);
    final Map<String, Boolean> orderBy = query.getOrderBy();
    final StringBuilder whereClause = getWhereClause(query);
    StringBuilder sql = new StringBuilder();
    if (orderBy.isEmpty() || boundingBox != null) {
      if (!orderBy.isEmpty()) {
        LoggerFactory.getLogger(getClass()).error(
          "Unable to sort on " + recordDefinition.getPath() + " "
            + orderBy.keySet()
            + " as the ESRI library can't sort with a bounding box query");
      }
      sql = whereClause;
    } else {
      sql.append("SELECT ");

      final List<String> fieldNames = query.getFieldNames();
      if (fieldNames.isEmpty()) {
        CollectionUtil.append(sql, recordDefinition.getFieldNames());
      } else {
        CollectionUtil.append(sql, fieldNames);
      }
      sql.append(" FROM ");
      sql.append(JdbcUtils.getTableName(typePath));
      if (whereClause.length() > 0) {
        sql.append(" WHERE ");
        sql.append(whereClause);
      }
      boolean first = true;
      for (final Entry<String, Boolean> entry : orderBy.entrySet()) {
        final String column = entry.getKey();
        final DataType dataType = recordDefinition.getFieldType(column);
        if (dataType != null
          && !Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
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
          LoggerFactory.getLogger(getClass()).error(
            "Unable to sort on " + recordDefinition.getPath() + "." + column
              + " as the ESRI library can't sort on " + dataType + " columns");
        }
      }
    }

    final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this,
      typePath, sql.toString(), boundingBox, query, query.getOffset(),
      query.getLimit());
    iterator.setStatistics(query.getStatistics());
    return iterator;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T createPrimaryIdValue(final String typePath) {
    synchronized (apiSync) {
      final RecordDefinition recordDefinition = getRecordDefinition(typePath);
      if (recordDefinition == null) {
        return null;
      } else {
        final String idFieldName = recordDefinition.getIdFieldName();
        if (idFieldName == null) {
          return null;
        } else if (!idFieldName.equals("OBJECTID")) {
          AtomicLong idGenerator = idGenerators.get(typePath);
          if (idGenerator == null) {
            long maxId = 0;
            for (final Record object : query(typePath)) {
              final Identifier id = object.getIdentifier();
              final Object firstId = id.getValue(0);
              if (firstId instanceof Number) {
                final Number number = (Number)firstId;
                if (number.longValue() > maxId) {
                  maxId = number.longValue();
                }
              }
            }
            idGenerator = new AtomicLong(maxId);
            idGenerators.put(typePath, idGenerator);
          }
          return (T)(Object)idGenerator.incrementAndGet();
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

  private RecordStoreSchema createSchema(final String schemaCatalogPath,
    final SpatialReference spatialReference) {
    synchronized (apiSync) {
      synchronized (API_SYNC) {
        final Geodatabase geodatabase = getGeodatabase();
        if (geodatabase == null) {
          return null;
        } else {
          try {
            final String schemaPath = toPath(schemaCatalogPath);
            String parentCatalogPath = "\\";
            RecordStoreSchema schema = getRootSchema();
            for (final String childSchemaPath : Path.getPaths(schemaPath)) {
              if (childSchemaPath.length() > 1) {
                RecordStoreSchema childSchema = schema.getSchema(childSchemaPath);
                final String childCatalogPath = childSchemaPath.replaceAll("/",
                  "\\\\");
                if (!hasChildDataset(getGeodatabase(), parentCatalogPath,
                  "Feature Dataset", childCatalogPath)) {
                  if (spatialReference != null) {
                    final DEFeatureDataset dataset = EsriXmlRecordDefinitionUtil.createDEFeatureDataset(
                      childCatalogPath, spatialReference);
                    final String datasetDefinition = EsriGdbXmlSerializer.toString(dataset);
                    try {
                      geodatabase.createFeatureDataset(datasetDefinition);
                    } catch (final Throwable t) {
                      if (LOG.isDebugEnabled()) {
                        LOG.debug(datasetDefinition);
                      }
                      throw new RuntimeException(
                        "Unable to create feature dataset " + childCatalogPath,
                        t);
                    }
                  }
                }
                if (childSchema == null) {
                  childSchema = createFeatureDatasetSchema(schema,
                    childCatalogPath);
                  schema.addElement(childSchema);
                }
                schema = childSchema;
                parentCatalogPath = childCatalogPath;
              }
            }
            return schema;
          } finally {
            releaseGeodatabase();
          }
        }
      }
    }
  }

  private RecordDefinitionImpl createTable(final DETable deTable) {
    synchronized (apiSync) {
      synchronized (API_SYNC) {
        final Geodatabase geodatabase = getGeodatabase();
        if (geodatabase == null) {
          return null;
        } else {
          try {
            String schemaCatalogPath = deTable.getParentCatalogPath();
            SpatialReference spatialReference;
            if (deTable instanceof DEFeatureClass) {
              final DEFeatureClass featureClass = (DEFeatureClass)deTable;
              spatialReference = featureClass.getSpatialReference();
            } else {
              spatialReference = null;
            }
            String schemaPath = toPath(schemaCatalogPath);
            final RecordStoreSchema schema = createSchema(schemaCatalogPath,
              spatialReference);

            if (schemaPath.equals(defaultSchemaPath)) {
              if (!(deTable instanceof DEFeatureClass)) {
                schemaCatalogPath = "\\";
                deTable.setCatalogPath("\\" + deTable.getName());
              }
            } else if (schemaPath.equals("")) {
              schemaPath = defaultSchemaPath;
            }
            for (final Field field : deTable.getFields()) {
              final String fieldName = field.getName();
              final CodeTable codeTable = getCodeTableByFieldName(fieldName);
              if (codeTable instanceof FileGdbDomainCodeTable) {
                final FileGdbDomainCodeTable domainCodeTable = (FileGdbDomainCodeTable)codeTable;
                field.setDomain(domainCodeTable.getDomain());
              }
            }
            final String tableDefinition = EsriGdbXmlSerializer.toString(deTable);
            try {
              final Table table = geodatabase.createTable(tableDefinition,
                schemaCatalogPath);
              geodatabase.closeTable(table);
              table.delete();
              final RecordDefinitionImpl recordDefinition = getRecordDefinition(
                schemaPath, schemaCatalogPath, tableDefinition);
              addRecordDefinition(recordDefinition);
              schema.addElement(recordDefinition);
              return recordDefinition;
            } catch (final Throwable t) {
              throw new RuntimeException("Unable to create table "
                + deTable.getCatalogPath(), t);
            }
          } finally {
            releaseGeodatabase();
          }
        }
      }
    }
  }

  private RecordDefinition createTable(final RecordDefinition recordDefinition) {
    synchronized (apiSync) {
      synchronized (API_SYNC) {
        final GeometryFactory geometryFactory = recordDefinition.getGeometryFactory();
        final SpatialReference spatialReference = getSpatialReference(geometryFactory);

        final DETable deTable = EsriXmlRecordDefinitionUtil.getDETable(
          recordDefinition, spatialReference);
        final RecordDefinitionImpl tableRecordDefinition = createTable(deTable);
        final String idFieldName = recordDefinition.getIdFieldName();
        if (idFieldName != null) {
          tableRecordDefinition.setIdFieldName(idFieldName);
        }
        return tableRecordDefinition;
      }
    }
  }

  @Override
  public FileGdbWriter createWriter() {
    return new FileGdbWriter(this);
  }

  @Override
  public void delete(final Record object) {
    // Don't synchronize to avoid deadlock as that is done lower down in the
    // methods
    if (object.getState() == RecordState.Persisted
      || object.getState() == RecordState.Modified) {
      object.setState(RecordState.Deleted);
      final Writer<Record> writer = getWriter();
      writer.write(object);
    }
  }

  public void deleteGeodatabase() {
    synchronized (apiSync) {
      final String fileName = this.fileName;
      try {
        doClose();
      } finally {
        if (new File(fileName).exists()) {
          synchronized (API_SYNC) {
            EsriFileGdb.DeleteGeodatabase(fileName);
          }
        }
      }
    }
  }

  protected void deleteRow(final String typePath, final Table table,
    final Row row) {
    synchronized (apiSync) {
      if (isOpen(table)) {
        final boolean loadOnly = writeLockCountsByTypePath.containsKey(typePath);
        if (loadOnly) {
          table.setLoadOnlyMode(false);
        }
        table.deleteRow(row);
        if (loadOnly) {
          table.setLoadOnlyMode(true);
        }
      }
    }
  }

  public void doClose() {
    exists = false;
    synchronized (apiSync) {
      synchronized (API_SYNC) {
        try {
          if (!isClosed()) {
            if (geodatabase != null) {
              final Writer<Record> writer = getSharedAttribute("writer");
              if (writer != null) {
                writer.close();
                setSharedAttribute("writer", null);
              }
              closeEnumRows();
              closeTables();
              try {
                if (geodatabase != null) {
                  closeGeodatabase(geodatabase);
                }
              } finally {
                geodatabase = null;
              }
            }
          }
        } finally {
          closed = true;
          super.close();
        }
      }
    }
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
  }

  protected void freeWriteLock(final String typePath, final Table table) {
    synchronized (apiSync) {
      if (isOpen(table)) {
        if (Maps.decrementCount(writeLockCountsByTypePath, typePath) == 0) {
          table.setLoadOnlyMode(false);
          table.freeWriteLock();
        }
      }
    }
  }

  public Object getApiSync() {
    return apiSync;
  }

  protected String getCatalogPath(final RecordStoreSchemaElement element) {
    final String path = element.getPath();
    return getCatalogPath(path);
  }

  protected String getCatalogPath(final String path) {
    final String catalogPath = catalogPathByPath.get(path);
    if (Property.hasValue(catalogPath)) {
      return catalogPath;
    } else {
      return path.replaceAll("/", "\\\\");
    }
  }

  private VectorOfWString getChildDatasets(final Geodatabase geodatabase,
    final String catalogPath, final String datasetType) {
    final boolean pathExists = isPathExists(geodatabase, catalogPath);
    if (pathExists) {
      return geodatabase.getChildDatasets(catalogPath, datasetType);
    } else {
      return null;
    }
  }

  public String getDefaultSchema() {
    return defaultSchemaPath;
  }

  public Map<String, List<String>> getDomainFieldNames() {
    return domainFieldNames;
  }

  public String getFileName() {
    return fileName;
  }

  private Geodatabase getGeodatabase() {
    synchronized (apiSync) {
      if (isExists()) {
        geodatabaseReferenceCount++;
        if (geodatabase == null) {
          geodatabase = openGeodatabase();
        }
        return geodatabase;
      } else {
        return null;
      }
    }
  }

  @Override
  public RecordDefinition getRecordDefinition(
    final RecordDefinition sourceRecordDefinition) {
    synchronized (apiSync) {
      if (getGeometryFactory() == null) {
        setGeometryFactory(sourceRecordDefinition.getGeometryFactory());
      }
      RecordDefinition recordDefinition = super.getRecordDefinition(sourceRecordDefinition);
      if (createMissingTables && recordDefinition == null) {
        recordDefinition = createTable(sourceRecordDefinition);
      }
      return recordDefinition;
    }
  }

  public RecordDefinitionImpl getRecordDefinition(final String schemaName,
    final String path, final String tableDefinition) {
    synchronized (apiSync) {
      synchronized (API_SYNC) {
        try {
          final XmlProcessor parser = new EsriGdbXmlParser();
          final DETable deTable = parser.process(tableDefinition);
          final String tableName = deTable.getName();
          final String typePath = Path.toPath(schemaName, tableName);
          final RecordStoreSchema schema = getSchema(schemaName);
          final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(
            schema, typePath);
          for (final Field field : deTable.getFields()) {
            final String fieldName = field.getName();
            final FieldType type = field.getType();
            final Constructor<? extends AbstractFileGdbFieldDefinition> attributeConstructor = ESRI_FIELD_TYPE_ATTRIBUTE_MAP.get(type);
            if (attributeConstructor != null) {
              try {
                final AbstractFileGdbFieldDefinition attribute = JavaBeanUtil.invokeConstructor(
                  attributeConstructor, field);
                attribute.setRecordStore(this);
                recordDefinition.addField(attribute);
                if (attribute instanceof GlobalIdFieldDefinition) {
                  recordDefinition.setIdFieldName(fieldName);
                }
              } catch (final Throwable e) {
                LOG.error(tableDefinition);
                throw new RuntimeException("Error creating attribute for "
                  + typePath + "." + field.getName() + " : " + field.getType(),
                  e);
              }
            } else {
              LOG.error("Unsupported field type " + fieldName + ":" + type);
            }
          }
          final String oidFieldName = deTable.getOIDFieldName();
          recordDefinition.setProperty(
            EsriGeodatabaseXmlConstants.ESRI_OBJECT_ID_FIELD_NAME, oidFieldName);
          if (deTable instanceof DEFeatureClass) {
            final DEFeatureClass featureClass = (DEFeatureClass)deTable;
            final String shapeFieldName = featureClass.getShapeFieldName();
            recordDefinition.setGeometryFieldName(shapeFieldName);
          }
          for (final Index index : deTable.getIndexes()) {
            if (index.getName().endsWith("_PK")) {
              for (final Field field : index.getFields()) {
                final String fieldName = field.getName();
                recordDefinition.setIdFieldName(fieldName);
              }
            }
          }
          addRecordDefinitionProperties(recordDefinition);
          if (recordDefinition.getIdFieldIndex() == -1) {
            recordDefinition.setIdFieldName(deTable.getOIDFieldName());
          }
          catalogPathByPath.put(typePath, deTable.getCatalogPath());
          return recordDefinition;
        } catch (final RuntimeException e) {
          if (LOG.isDebugEnabled()) {
            LOG.debug(tableDefinition);
          }
          throw e;
        }
      }
    }
  }

  @Override
  public int getRowCount(final Query query) {
    if (query == null) {
      return 0;
    } else {
      synchronized (apiSync) {
        final Geodatabase geodatabase = getGeodatabase();
        if (geodatabase == null) {
          return 0;
        } else {
          try {
            String typePath = query.getTypeName();
            RecordDefinition recordDefinition = query.getRecordDefinition();
            if (recordDefinition == null) {
              typePath = query.getTypeName();
              recordDefinition = getRecordDefinition(typePath);
              if (recordDefinition == null) {
                return 0;
              }
            } else {
              typePath = recordDefinition.getPath();
            }
            final StringBuilder whereClause = getWhereClause(query);
            final BoundingBox boundingBox = QueryValue.getBoundingBox(query);

            if (boundingBox == null) {
              final StringBuilder sql = new StringBuilder();
              sql.append("SELECT OBJECTID FROM ");
              sql.append(JdbcUtils.getTableName(typePath));
              if (whereClause.length() > 0) {
                sql.append(" WHERE ");
                sql.append(whereClause);
              }

              final EnumRows rows = query(sql.toString(), false);
              if (rows == null) {
                return 0;
              } else {
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
              }
            } else {
              final GeometryFieldDefinition geometryField = (GeometryFieldDefinition)recordDefinition.getGeometryField();
              if (geometryField == null || boundingBox.isEmpty()) {
                return 0;
              } else {
                final StringBuilder sql = new StringBuilder();
                sql.append("SELECT " + geometryField.getName() + " FROM ");
                sql.append(JdbcUtils.getTableName(typePath));
                if (whereClause.length() > 0) {
                  sql.append(" WHERE ");
                  sql.append(whereClause);
                }

                final EnumRows rows = query(sql.toString(), false);
                try {
                  int count = 0;
                  for (Row row = rows.next(); row != null; row = rows.next()) {
                    final Geometry geometry = (Geometry)geometryField.getValue(row);
                    if (geometry != null) {
                      final BoundingBox geometryBoundingBox = geometry.getBoundingBox();
                      if (geometryBoundingBox.intersects(boundingBox)) {
                        count++;
                      }
                    }
                    row.delete();
                  }
                  return count;
                } finally {
                  closeEnumRows(rows);
                }
              }
            }
          } finally {
            releaseGeodatabase();
          }
        }
      }
    }
  }

  protected Table getTable(final String typePath) {
    synchronized (apiSync) {
      synchronized (API_SYNC) {
        if (!isExists() || getRecordDefinition(typePath) == null) {
          return null;
        } else {
          final String path = typePath.replaceAll("/", "\\\\");
          try {
            final Geodatabase geodatabase = getGeodatabase();
            if (geodatabase == null) {
              return null;
            } else {
              try {
                Table table = tablesToClose.get(typePath);
                if (table == null) {
                  table = this.geodatabase.openTable(path);
                  if (table != null) {
                    if (tablesToClose.isEmpty()) {
                      geodatabaseReferenceCount++;
                    }
                    tablesToClose.put(typePath, table);
                  }
                }
                return table;
              } catch (final RuntimeException e) {
                throw new RuntimeException("Unable to open table " + typePath,
                  e);
              }
            }
          } finally {
            releaseGeodatabase();
          }
        }
      }
    }
  }

  protected Table getTableWithWriteLock(final String typePath) {
    synchronized (apiSync) {
      final Table table = getTable(typePath);
      if (table != null) {
        if (Maps.addCount(writeLockCountsByTypePath, typePath) == 1) {
          table.setWriteLock();
          table.setLoadOnlyMode(true);
        }
      }
      return table;
    }
  }

  protected StringBuilder getWhereClause(final Query query) {
    final StringBuilder whereClause = new StringBuilder();
    final Condition whereCondition = query.getWhereCondition();
    if (whereCondition != null) {
      appendQueryValue(query, whereClause, whereCondition);
    }
    return whereClause;
  }

  @Override
  public FileGdbWriter getWriter() {
    synchronized (apiSync) {
      FileGdbWriter writer = getSharedAttribute("writer");
      if (writer == null) {
        writer = createWriter();
        setSharedAttribute("writer", writer);
      }
      return writer;
    }
  }

  protected boolean hasCatalogPath(final String path) {
    final String catalogPath = catalogPathByPath.get(path);
    return catalogPath != null;
  }

  private boolean hasChildDataset(final Geodatabase geodatabase,
    final String parentCatalogPath, final String datasetType,
    final String childCatalogPath) {
    try {
      final VectorOfWString childDatasets = geodatabase.getChildDatasets(
        parentCatalogPath, datasetType);
      for (int i = 0; i < childDatasets.size(); i++) {
        final String catalogPath = childDatasets.get(i);
        if (catalogPath.equals(childCatalogPath)) {
          return true;
        }
      }
      return false;
    } catch (final RuntimeException e) {
      if ("-2147211775\tThe item was not found.".equals(e.getMessage())) {
        return false;
      } else {
        throw e;
      }
    }
  }

  @Override
  @PostConstruct
  public void initialize() {
    synchronized (apiSync) {
      synchronized (API_SYNC) {
        if (!initialized) {
          Geodatabase geodatabase = null;
          initialized = true;
          try {
            super.initialize();
            final File file = new File(fileName);
            if (file.exists()) {
              if (file.isDirectory()) {
                if (!new File(fileName, "gdb").exists()) {
                  throw new IllegalArgumentException(
                    FileUtil.getCanonicalPath(file)
                      + " is not a valid ESRI File Geodatabase");
                }
                geodatabase = openGeodatabase();
              } else {
                throw new IllegalArgumentException(
                  FileUtil.getCanonicalPath(file)
                    + " ESRI File Geodatabase must be a directory");
              }
            } else if (createMissingRecordStore) {
              geodatabase = createGeodatabase();
            } else {
              throw new IllegalArgumentException(
                "ESRI file geodatabase not found " + fileName);
            }
            final VectorOfWString domainNames = geodatabase.getDomains();
            for (int i = 0; i < domainNames.size(); i++) {
              final String domainName = domainNames.get(i);
              loadDomain(geodatabase, domainName);
            }
            exists = true;
          } catch (final Throwable e) {
            closed = true;
            ExceptionUtil.throwUncheckedException(e);
          } finally {
            if (geodatabase != null) {
              closeGeodatabase(geodatabase);
            }
          }
        }
      }
    }
  }

  @Override
  public void insert(final Record object) {
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

  public boolean isClosed() {
    return closed;
  }

  public boolean isCreateMissingRecordStore() {
    return createMissingRecordStore;
  }

  public boolean isCreateMissingTables() {
    return createMissingTables;
  }

  public boolean isExists() {
    return exists && !isClosed();
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

  private boolean isPathExists(final Geodatabase geodatabase, String path) {
    if (path == null) {
      return false;
    } else if ("\\".equals(path)) {
      return true;
    } else {
      final boolean pathExists = true;

      path = path.replaceAll("[\\/]+", "\\");
      path = path.replaceAll("\\$", "");
      int index = 0;
      while (index != -1) {
        final String parentPath = path.substring(0, index + 1);
        final int nextIndex = path.indexOf(index + 1, '\\');
        String currentPath;
        if (nextIndex == -1) {
          currentPath = path;
        } else {
          currentPath = path.substring(0, nextIndex);
        }
        boolean found = false;
        final VectorOfWString children = geodatabase.getChildDatasets(
          parentPath, "Feature Dataset");
        for (int i = 0; i < children.size(); i++) {
          final String childPath = children.get(i);
          if (childPath.equals(currentPath)) {
            found = true;
          }
        }
        if (!found) {
          return false;
        }
        index = nextIndex;
      }
      return pathExists;
    }
  }

  @Override
  public Record load(final String typePath, final Object... id) {
    synchronized (apiSync) {
      final RecordDefinition recordDefinition = getRecordDefinition(typePath);
      if (recordDefinition == null) {
        throw new IllegalArgumentException("Unknown type " + typePath);
      } else {
        final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this,
          typePath, recordDefinition.getIdFieldName() + " = " + id[0]);
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

  protected void loadDomain(final Geodatabase geodatabase,
    final String domainName) {
    synchronized (apiSync) {
      synchronized (API_SYNC) {
        final String domainDef = geodatabase.getDomainDefinition(domainName);
        final Domain domain = EsriGdbXmlParser.parse(domainDef);
        if (domain instanceof CodedValueDomain) {
          final CodedValueDomain codedValueDomain = (CodedValueDomain)domain;
          final FileGdbDomainCodeTable codeTable = new FileGdbDomainCodeTable(
            this, codedValueDomain);
          super.addCodeTable(codeTable);
          final List<String> columnNames = domainFieldNames.get(domainName);
          if (columnNames != null) {
            for (final String columnName : columnNames) {
              addCodeTable(columnName, codeTable);
            }
          }
        }
      }
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

  @Override
  protected void obtainConnected() {
    getGeodatabase();
  }

  private Geodatabase openGeodatabase() {
    synchronized (API_SYNC) {
      return EsriFileGdb.openGeodatabase(fileName);
    }
  }

  public EnumRows query(final String sql, final boolean recycling) {
    synchronized (apiSync) {
      final Geodatabase geodatabase = getGeodatabase();
      if (geodatabase == null) {
        return null;
      } else {
        try {
          final EnumRows enumRows = geodatabase.query(sql, recycling);
          enumRowsToClose.add(enumRows);
          return enumRows;
        } catch (final Throwable t) {
          throw new RuntimeException("Error running sql: " + sql, t);
        } finally {
          releaseGeodatabase();
        }
      }
    }
  }

  @Override
  protected Map<String, ? extends RecordStoreSchemaElement> refreshSchemaElements(
    final RecordStoreSchema schema) {
    synchronized (apiSync) {
      synchronized (API_SYNC) {
        final Map<String, RecordStoreSchemaElement> elementsByPath = new TreeMap<>();
        final Geodatabase geodatabase = getGeodatabase();
        if (geodatabase != null) {
          try {
            final String schemaPath = schema.getPath();
            final String schemaCatalogPath = getCatalogPath(schema);
            final VectorOfWString childDatasets = getChildDatasets(geodatabase,
              schemaCatalogPath, "Feature Dataset");
            if (childDatasets != null) {
              for (int i = 0; i < childDatasets.size(); i++) {
                final String childCatalogPath = childDatasets.get(i);
                final String childPath = toPath(childCatalogPath);
                RecordStoreSchema childSchema = schema.getSchema(childPath);
                if (childSchema == null) {
                  childSchema = createFeatureDatasetSchema(schema, childPath);
                } else {
                  if (childSchema.isInitialized()) {
                    childSchema.refresh();
                  }
                }
                elementsByPath.put(childPath.toUpperCase(), childSchema);
              }
            }
            if (Path.isParent(schemaPath, defaultSchemaPath)
              && !elementsByPath.containsKey(defaultSchemaPath)) {
              final SpatialReference spatialReference = getSpatialReference(getGeometryFactory());
              final RecordStoreSchema childSchema = createSchema(
                defaultSchemaPath, spatialReference);
              elementsByPath.put(defaultSchemaPath.toUpperCase(), childSchema);
            }

            if (schema.equalPath(defaultSchemaPath)) {
              refreshSchemaRecordDefinitions(elementsByPath, schemaPath, "\\",
                "Feature Class");
              refreshSchemaRecordDefinitions(elementsByPath, schemaPath, "\\",
                "Table");
            }
            refreshSchemaRecordDefinitions(elementsByPath, schemaPath,
              schemaCatalogPath, "Feature Class");
            refreshSchemaRecordDefinitions(elementsByPath, schemaPath,
              schemaCatalogPath, "Table");
          } finally {
            releaseGeodatabase();
          }
        }
        return elementsByPath;
      }
    }
  }

  private void refreshSchemaRecordDefinitions(
    final Map<String, RecordStoreSchemaElement> elementsByPath,
    final String schemaPath, final String catalogPath, final String datasetType) {
    synchronized (apiSync) {
      synchronized (API_SYNC) {
        final Geodatabase geodatabase = getGeodatabase();
        if (geodatabase != null) {
          try {
            final boolean pathExists = isPathExists(geodatabase, catalogPath);
            if (pathExists) {
              final VectorOfWString childFeatureClasses = getChildDatasets(
                geodatabase, catalogPath, datasetType);
              if (childFeatureClasses != null) {
                for (int i = 0; i < childFeatureClasses.size(); i++) {
                  final String childCatalogPath = childFeatureClasses.get(i);
                  final String tableDefinition = geodatabase.getTableDefinition(childCatalogPath);
                  final RecordDefinition recordDefinition = getRecordDefinition(
                    schemaPath, childCatalogPath, tableDefinition);
                  addRecordDefinition(recordDefinition);
                  final String upperChildPath = recordDefinition.getPath()
                    .toUpperCase();
                  elementsByPath.put(upperChildPath, recordDefinition);
                }
              }
            }
          } finally {
            releaseGeodatabase();
          }
        }
      }
    }
  }

  @Override
  protected void releaseConnected() {
    releaseGeodatabase();
  }

  private void releaseGeodatabase() {
    synchronized (apiSync) {
      if (geodatabase != null) {
        geodatabaseReferenceCount--;
        if (geodatabaseReferenceCount <= 0) {
          geodatabaseReferenceCount = 0;
          try {
            closeGeodatabase(geodatabase);
          } finally {
            geodatabase = null;
          }
        }
      }
    }
  }

  protected void releaseTable(final String typePath) {
    synchronized (apiSync) {
      final Geodatabase geodatabase = getGeodatabase();
      if (geodatabase != null) {
        try {
          final Table table = tablesToClose.get(typePath);
          if (table != null) {
            if (Maps.decrementCount(tableReferenceCountsByTypePath, typePath) == 0) {
              try {
                tablesToClose.remove(typePath);
                writeLockCountsByTypePath.remove(typePath);
                geodatabase.closeTable(table);
              } catch (final Exception e) {
                LoggerFactory.getLogger(getClass()).error(
                  "Unable to close table: " + typePath, e);
              } finally {
                if (tablesToClose.isEmpty()) {
                  geodatabaseReferenceCount--;
                }
                table.delete();
              }
            }
          }
        } finally {
          releaseGeodatabase();
        }
      }
    }
  }

  protected void releaseTableAndWriteLock(final String typePath) {
    synchronized (apiSync) {
      final Geodatabase geodatabase = getGeodatabase();
      if (geodatabase != null) {
        try {
          final Table table = tablesToClose.get(typePath);
          if (table != null) {
            if (Maps.decrementCount(writeLockCountsByTypePath, typePath) == 0) {
              try {
                table.setLoadOnlyMode(false);
                table.freeWriteLock();
              } catch (final Exception e) {
                LoggerFactory.getLogger(getClass()).error(
                  "Unable to free write lock for table: " + typePath, e);
              }
            }
          }
          releaseTable(typePath);
        } finally {
          releaseGeodatabase();
        }
      }
    }
  }

  public EnumRows search(final Object typePath, final Table table,
    final String fields, final String whereClause, final boolean recycling) {
    synchronized (apiSync) {
      if (isOpen(table)) {
        try {
          final EnumRows rows = table.search(fields, whereClause, recycling);
          enumRowsToClose.add(rows);
          return rows;
        } catch (final Throwable t) {
          LoggerFactory.getLogger(getClass()).error(
            "Unable to execute query " + fields + " FROM " + typePath
              + " WHERE " + whereClause, t);

        }
      }
      return null;
    }
  }

  public EnumRows search(final Object typePath, final Table table,
    final String fields, final String whereClause, final Envelope boundingBox,
    final boolean recycling) {
    synchronized (apiSync) {
      if (isOpen(table)) {
        try {
          final EnumRows rows = table.search(fields, whereClause, boundingBox,
            recycling);
          enumRowsToClose.add(rows);
          return rows;
        } catch (final Exception e) {
          LOG.error("ERROR executing query SELECT " + fields + " FROM "
            + typePath + " WHERE " + whereClause + " AND " + boundingBox, e);
        }
      }
      return null;
    }
  }

  public void setCreateMissingRecordStore(final boolean createMissingRecordStore) {
    this.createMissingRecordStore = createMissingRecordStore;
  }

  public void setCreateMissingTables(final boolean createMissingTables) {
    this.createMissingTables = createMissingTables;
  }

  public void setDefaultSchema(final String defaultSchema) {
    synchronized (apiSync) {
      if (Property.hasValue(defaultSchema)) {
        defaultSchemaPath = Path.clean(defaultSchema);
      } else {
        defaultSchemaPath = "/";
      }
    }
  }

  public void setDomainFieldNames(
    final Map<String, List<String>> domainColumNames) {
    domainFieldNames = domainColumNames;
  }

  public void setFileName(final String fileName) {
    this.fileName = fileName;
  }

  public void setNull(final Row row, final String name) {
    synchronized (apiSync) {
      row.setNull(name);
    }
  }

  protected String toPath(final String catalogPath) {
    return Path.clean(catalogPath);
  }

  @Override
  public String toString() {
    return fileName;
  }

  @Override
  public void update(final Record object) {
    // Don't synchronize to avoid deadlock as that is done lower down in the
    // methods
    getWriter().write(object);
  }

  protected void updateRow(final String typePath, final Table table,
    final Row row) {
    synchronized (apiSync) {
      if (isOpen(table)) {
        final boolean loadOnly = writeLockCountsByTypePath.containsKey(typePath);
        if (loadOnly) {
          table.setLoadOnlyMode(false);
        }
        table.updateRow(row);
        if (loadOnly) {
          table.setLoadOnlyMode(true);
        }
      }
    }
  }
}
