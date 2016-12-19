package com.revolsys.gis.esri.gdb.file;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.revolsys.beans.ObjectException;
import com.revolsys.beans.ObjectPropertyException;
import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.Maps;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.ClockDirection;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.esri.gdb.file.capi.FileGdbDomainCodeTable;
import com.revolsys.gis.esri.gdb.file.capi.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.capi.swig.Envelope;
import com.revolsys.gis.esri.gdb.file.capi.swig.EsriFileGdb;
import com.revolsys.gis.esri.gdb.file.capi.swig.Geodatabase;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.gis.esri.gdb.file.capi.swig.Table;
import com.revolsys.gis.esri.gdb.file.capi.swig.VectorOfWString;
import com.revolsys.gis.esri.gdb.file.capi.type.AbstractFileGdbFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.AreaFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.BinaryFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.DateFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.DoubleFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.FloatFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.GeometryFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.GlobalIdFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.GuidFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.IntegerFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.LengthFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.OidFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.ShortFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.StringFieldDefinition;
import com.revolsys.gis.esri.gdb.file.capi.type.XmlFieldDefinition;
import com.revolsys.identifier.Identifier;
import com.revolsys.identifier.SingleIdentifier;
import com.revolsys.io.FileUtil;
import com.revolsys.io.PathName;
import com.revolsys.io.PathUtil;
import com.revolsys.io.Writer;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.logging.Logs;
import com.revolsys.parallel.SingleThreadExecutor;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.io.format.esri.gdb.xml.EsriGeodatabaseXmlConstants;
import com.revolsys.record.io.format.esri.gdb.xml.model.DEFeatureClass;
import com.revolsys.record.io.format.esri.gdb.xml.model.DEFeatureDataset;
import com.revolsys.record.io.format.esri.gdb.xml.model.DETable;
import com.revolsys.record.io.format.esri.gdb.xml.model.Domain;
import com.revolsys.record.io.format.esri.gdb.xml.model.EsriGdbXmlParser;
import com.revolsys.record.io.format.esri.gdb.xml.model.EsriGdbXmlSerializer;
import com.revolsys.record.io.format.esri.gdb.xml.model.EsriXmlRecordDefinitionUtil;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.record.io.format.esri.gdb.xml.model.Index;
import com.revolsys.record.io.format.esri.gdb.xml.model.SpatialReference;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.record.io.format.xml.XmlProcessor;
import com.revolsys.record.property.LengthFieldName;
import com.revolsys.record.query.AbstractMultiCondition;
import com.revolsys.record.query.BinaryCondition;
import com.revolsys.record.query.CollectionValue;
import com.revolsys.record.query.Column;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.ILike;
import com.revolsys.record.query.In;
import com.revolsys.record.query.LeftUnaryCondition;
import com.revolsys.record.query.Like;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.RightUnaryCondition;
import com.revolsys.record.query.SqlCondition;
import com.revolsys.record.query.Value;
import com.revolsys.record.query.functions.EnvelopeIntersects;
import com.revolsys.record.query.functions.WithinDistance;
import com.revolsys.record.schema.AbstractRecordStore;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.record.schema.RecordStoreSchemaElement;
import com.revolsys.util.Dates;
import com.revolsys.util.Exceptions;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;
import com.revolsys.util.StringBuilders;

public class FileGdbRecordStore extends AbstractRecordStore {
  private static final Object API_SYNC = new Object();

  private static final Map<FieldType, Constructor<? extends AbstractFileGdbFieldDefinition>> ESRI_FIELD_TYPE_FIELD_DEFINITION_MAP = new HashMap<>();

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\?");

  private static final SingleThreadExecutor TASK_EXECUTOR = new SingleThreadExecutor(
    "ESRI FGDB Create Thread");

  static {
    addFieldTypeConstructor(FieldType.esriFieldTypeInteger, IntegerFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeSmallInteger, ShortFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeDouble, DoubleFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeSingle, FloatFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeString, StringFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeDate, DateFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeGeometry, GeometryFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeOID, OidFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeBlob, BinaryFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeGlobalID, GlobalIdFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeGUID, GuidFieldDefinition.class);
    addFieldTypeConstructor(FieldType.esriFieldTypeXML, XmlFieldDefinition.class);
  }

  private static void addFieldTypeConstructor(final FieldType fieldType,
    final Class<? extends AbstractFileGdbFieldDefinition> fieldClass) {
    try {
      final Constructor<? extends AbstractFileGdbFieldDefinition> constructor = fieldClass
        .getConstructor(Field.class);
      ESRI_FIELD_TYPE_FIELD_DEFINITION_MAP.put(fieldType, constructor);
    } catch (final SecurityException e) {
      Logs.error(FileGdbRecordStore.class, "No public constructor for ESRI type " + fieldType, e);
    } catch (final NoSuchMethodException e) {
      Logs.error(FileGdbRecordStore.class, "No public constructor for ESRI type " + fieldType, e);
    }

  }

  private static <V> V getSingleThreadResult(final Callable<V> callable) {
    synchronized (API_SYNC) {
      return TASK_EXECUTOR.call(callable);
    }
  }

  public static SpatialReference getSpatialReference(final GeometryFactory geometryFactory) {
    if (geometryFactory == null || geometryFactory.getCoordinateSystemId() == 0) {
      return null;
    } else {
      final String wkt = getSingleThreadResult(() -> {
        return EsriFileGdb.getSpatialReferenceWkt(geometryFactory.getCoordinateSystemId());
      });
      final SpatialReference spatialReference = SpatialReference.get(geometryFactory, wkt);
      return spatialReference;
    }
  }

  private final Object apiSync = new Object();

  private final Map<PathName, String> catalogPathByPath = new HashMap<>();

  private boolean createMissingRecordStore = true;

  private boolean createMissingTables = true;

  private PathName defaultSchemaPath = PathName.ROOT;

  private Map<String, List<String>> domainFieldNames = new HashMap<>();

  private boolean exists = false;

  private String fileName;

  private Geodatabase geodatabase;

  private int geodatabaseReferenceCount;

  private final Map<PathName, AtomicLong> idGenerators = new HashMap<>();

  private boolean initialized;

  private final Map<String, Table> tableByCatalogPath = new HashMap<>();

  private final Map<String, Integer> tableReferenceCountsByCatalogPath = new HashMap<>();

  private final Map<String, Integer> tableWriteLockCountsByCatalogPath = new HashMap<>();

  private boolean createLengthField = false;

  private boolean createAreaField = false;

  FileGdbRecordStore(final File file) {
    this.fileName = FileUtil.getCanonicalPath(file);
    setConnectionProperties(Collections.singletonMap("url", FileUtil.toUrl(file).toString()));
    this.catalogPathByPath.put(PathName.ROOT, "\\");
  }

  @Override
  public void addCodeTable(final CodeTable codeTable) {
    super.addCodeTable(codeTable);
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        if (codeTable instanceof Domain) {
          final Domain domain = (Domain)codeTable;
          newDomainCodeTable(domain);
        }
      }
    }
  }

  public void alterDomain(final Domain domain) {
    final String domainDefinition = EsriGdbXmlSerializer.toString(domain);
    synchronized (this.apiSync) {
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
          final String string = DataTypes.toString(value);
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
        buffer.append("1==0");
      } else {
        final QueryValue left = in.getLeft();
        appendQueryValue(query, buffer, left);
        buffer.append(" IN (");
        appendQueryValue(query, buffer, in.getValues());
        buffer.append(")");
      }
    } else if (condition instanceof Value) {
      final Value valueCondition = (Value)condition;
      Object value = valueCondition.getValue();
      if (value instanceof Identifier) {
        final Identifier identifier = (Identifier)value;
        value = identifier.getValue(0);
      }
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
            "No arguments specified for a where clause with placeholders: " + where);
        } else {
          buffer.append(where);
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
          final StringBuffer replacement = new StringBuffer();
          matcher.appendReplacement(replacement, DataTypes.toString(argument));
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
      final String stringValue = Dates.format("yyyy-MM-dd", (java.util.Date)value);
      buffer.append("DATE '" + stringValue + "'");
    } else {
      final Object value1 = value;
      final String stringValue = DataTypes.toString(value1);
      buffer.append("'");
      buffer.append(stringValue.replaceAll("'", "''"));
      buffer.append("'");
    }
  }

  @Override
  @PreDestroy
  public void close() {
    if (FileGdbRecordStoreFactory.release(this)) {
      closeDo();
    }
  }

  public void closeDo() {
    this.exists = false;
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        try {
          if (!isClosed()) {
            if (this.geodatabase != null) {
              final Writer<Record> writer = getThreadProperty("writer");
              if (writer != null) {
                writer.close();
                setThreadProperty("writer", null);
              }
              closeTables();
              try {
                if (this.geodatabase != null) {
                  closeGeodatabase(this.geodatabase);
                }
              } finally {
                this.geodatabase = null;
              }
            }
          }
        } finally {
          super.close();
        }
      }
    }
  }

  private void closeGeodatabase(final Geodatabase geodatabase) {
    if (geodatabase != null) {
      final int closeResult = getSingleThreadResult(() -> {
        return EsriFileGdb.CloseGeodatabase(geodatabase);
      });
      if (closeResult != 0) {
        Logs.error(this, "Error closing: " + this.fileName + " ESRI Error=" + closeResult);
      }
    }
  }

  public boolean closeTable(final PathName typePath) {
    synchronized (this.apiSync) {
      final String path = getCatalogPath(typePath);
      int count = Maps.getInteger(this.tableReferenceCountsByCatalogPath, path, 0);
      count--;
      if (count <= 0) {
        this.tableReferenceCountsByCatalogPath.remove(path);
        final Table table = this.tableByCatalogPath.remove(path);
        synchronized (API_SYNC) {
          if (table != null) {
            try {
              final Geodatabase geodatabase = getGeodatabase();
              if (geodatabase != null) {
                try {
                  geodatabase.closeTable(table);
                } finally {
                  releaseGeodatabase();
                }
              }
            } catch (final Throwable e) {
              Logs.error(this, "Cannot close Table " + typePath, e);
            } finally {
              try {
                table.delete();
              } catch (final Throwable t) {
              }
            }
          }
        }
        return true;
      } else {
        this.tableReferenceCountsByCatalogPath.put(path, count);
        return false;
      }
    }
  }

  private void closeTables() {
    synchronized (this.apiSync) {
      if (!this.tableByCatalogPath.isEmpty()) {
        final Geodatabase geodatabase = getGeodatabase();
        if (geodatabase != null) {
          try {
            for (final Table table : this.tableByCatalogPath.values()) {
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
            this.tableByCatalogPath.clear();
            this.tableReferenceCountsByCatalogPath.clear();
            this.tableWriteLockCountsByCatalogPath.clear();
          } finally {
            releaseGeodatabase();
          }
        }
      }
    }
  }

  public void deleteGeodatabase() {
    synchronized (this.apiSync) {
      this.createMissingRecordStore = false;
      this.createMissingTables = false;
      final String fileName = this.fileName;
      try {
        closeDo();
      } finally {
        if (new File(fileName).exists()) {
          final int deleteResult = getSingleThreadResult(() -> {
            return EsriFileGdb.DeleteGeodatabase(fileName);
          });
          if (deleteResult != 0) {
            Logs.error(this, "Error deleting: " + fileName + " ESRI Error=" + deleteResult);
          }
        }
      }
    }
  }

  @Override
  public boolean deleteRecord(final Record record) {
    if (record == null) {
      return false;
    } else {
      final RecordDefinition recordDefinition = record.getRecordDefinition();
      final Table table = getTableWithWriteLock(recordDefinition);
      try {
        return deleteRecord(table, record);
      } finally {
        releaseTableAndWriteLock(recordDefinition);
      }
    }
  }

  boolean deleteRecord(final Table table, final Record record) {
    final Integer objectId = record.getInteger("OBJECTID");
    final PathName typePath = record.getPathName();
    if (objectId != null && table != null) {
      synchronized (table) {
        final String whereClause = "OBJECTID=" + objectId;
        try (
          final FileGdbEnumRowsIterator rows = search(typePath, table, "OBJECTID", whereClause,
            false)) {
          for (final Row row : rows) {
            synchronized (this.apiSync) {
              final boolean loadOnly = isTableLocked(typePath);
              if (loadOnly) {
                table.setLoadOnlyMode(false);
              }
              table.deleteRow(row);
              if (loadOnly) {
                table.setLoadOnlyMode(true);
              }
            }
            record.setState(RecordState.DELETED);
            addStatistic("Delete", record);
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
  }

  public Object getApiSync() {
    return this.apiSync;
  }

  protected String getCatalogPath(final PathName path) {
    final String catalogPath = this.catalogPathByPath.get(path);
    if (Property.hasValue(catalogPath)) {
      return catalogPath;
    } else {
      return toCatalogPath(path);
    }
  }

  protected String getCatalogPath(final RecordStoreSchemaElement element) {
    final PathName path = element.getPathName();
    return getCatalogPath(path);
  }

  private VectorOfWString getChildDatasets(final Geodatabase geodatabase, final String catalogPath,
    final String datasetType) {
    final boolean pathExists = isPathExists(geodatabase, catalogPath);
    if (pathExists) {
      return geodatabase.getChildDatasets(catalogPath, datasetType);
    } else {
      return null;
    }
  }

  public PathName getDefaultSchemaPath() {
    return this.defaultSchemaPath;
  }

  public Map<String, List<String>> getDomainFieldNames() {
    return this.domainFieldNames;
  }

  public final String getFileName() {
    return this.fileName;
  }

  private Geodatabase getGeodatabase() {
    synchronized (this.apiSync) {
      if (isExists()) {
        this.geodatabaseReferenceCount++;
        if (this.geodatabase == null) {
          this.geodatabase = openGeodatabase();
        }
        return this.geodatabase;
      } else {
        return null;
      }
    }
  }

  @Override
  public Record getRecord(final PathName typePath, final Object... id) {
    synchronized (this.apiSync) {
      final RecordDefinition recordDefinition = getRecordDefinition(typePath);
      if (recordDefinition == null) {
        throw new IllegalArgumentException("Unknown type " + typePath);
      } else {
        final String catalogPath = getCatalogPath(typePath);
        final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this, catalogPath,
          recordDefinition.getIdFieldName() + " = " + id[0]);
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

  @Override
  public int getRecordCount(final Query query) {
    if (query == null) {
      return 0;
    } else {
      synchronized (this.apiSync) {
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

              try (
                final FileGdbEnumRowsIterator rows = query(sql.toString(), false)) {
                int count = 0;
                for (@SuppressWarnings("unused")
                final Row row : rows) {
                  count++;
                }
                return count;
              }
            } else {
              final GeometryFieldDefinition geometryField = (GeometryFieldDefinition)recordDefinition
                .getGeometryField();
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

                try (
                  final FileGdbEnumRowsIterator rows = query(sql.toString(), false)) {
                  int count = 0;
                  for (final Row row : rows) {
                    final Geometry geometry = (Geometry)geometryField.getValue(row);
                    if (geometry != null) {
                      final BoundingBox geometryBoundingBox = geometry.getBoundingBox();
                      if (geometryBoundingBox.intersects(boundingBox)) {
                        count++;
                      }
                    }
                  }
                  return count;
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

  public RecordDefinitionImpl getRecordDefinition(final PathName schemaName, final String path,
    final String tableDefinition) {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        try {
          final XmlProcessor parser = new EsriGdbXmlParser();
          final DETable deTable = parser.process(tableDefinition);
          final String tableName = deTable.getName();
          final PathName typePath = PathName.newPathName(schemaName.newChild(tableName));
          final RecordStoreSchema schema = getSchema(schemaName);
          final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(schema, typePath);
          recordDefinition.setPolygonRingDirection(ClockDirection.NONE);
          String lengthFieldName = null;
          String areaFieldName = null;
          if (deTable instanceof DEFeatureClass) {
            final DEFeatureClass featureClass = (DEFeatureClass)deTable;

            lengthFieldName = featureClass.getLengthFieldName();
            final LengthFieldName lengthFieldNameProperty = new LengthFieldName(lengthFieldName);
            lengthFieldNameProperty.setRecordDefinition(recordDefinition);

            areaFieldName = featureClass.getAreaFieldName();
            final LengthFieldName areaFieldNameProperty = new LengthFieldName(areaFieldName);
            areaFieldNameProperty.setRecordDefinition(recordDefinition);

          }
          for (final Field field : deTable.getFields()) {
            final String fieldName = field.getName();
            AbstractFileGdbFieldDefinition fieldDefinition = null;
            if (fieldName.equals(lengthFieldName)) {
              fieldDefinition = new LengthFieldDefinition(field);
            } else if (fieldName.equals(areaFieldName)) {
              fieldDefinition = new AreaFieldDefinition(field);
            } else {
              final FieldType type = field.getType();
              final Constructor<? extends AbstractFileGdbFieldDefinition> fieldConstructor = ESRI_FIELD_TYPE_FIELD_DEFINITION_MAP
                .get(type);
              if (fieldConstructor != null) {
                try {
                  fieldDefinition = JavaBeanUtil.invokeConstructor(fieldConstructor, field);
                } catch (final Throwable e) {
                  Logs.error(this, tableDefinition);
                  throw new RuntimeException("Error creating field for " + typePath + "."
                    + field.getName() + " : " + field.getType(), e);
                }
              } else {
                Logs.error(this, "Unsupported field type " + fieldName + ":" + type);
              }
            }
            if (fieldDefinition != null) {
              final Domain domain = field.getDomain();
              if (domain != null) {
                CodeTable codeTable = getCodeTable(domain.getDomainName() + "_ID");
                if (codeTable == null) {
                  codeTable = new FileGdbDomainCodeTable(this, domain);
                  addCodeTable(codeTable);
                }
                fieldDefinition.setCodeTable(codeTable);
              }
              fieldDefinition.setRecordStore(this);
              recordDefinition.addField(fieldDefinition);
              if (fieldDefinition instanceof GlobalIdFieldDefinition) {
                recordDefinition.setIdFieldName(fieldName);
              }
            }
          }
          final String oidFieldName = deTable.getOIDFieldName();
          recordDefinition.setProperty(EsriGeodatabaseXmlConstants.ESRI_OBJECT_ID_FIELD_NAME,
            oidFieldName);
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
          this.catalogPathByPath.put(typePath, deTable.getCatalogPath());
          return recordDefinition;
        } catch (final RuntimeException e) {
          Logs.debug(this, tableDefinition);
          throw e;
        }
      }
    }
  }

  @Override
  public RecordDefinition getRecordDefinition(final RecordDefinition sourceRecordDefinition) {
    synchronized (this.apiSync) {
      if (getGeometryFactory() == null) {
        setGeometryFactory(sourceRecordDefinition.getGeometryFactory());
      }
      final String typePath = sourceRecordDefinition.getPath();
      RecordDefinition recordDefinition = getRecordDefinition(typePath);
      if (recordDefinition == null) {
        if (!sourceRecordDefinition.hasGeometryField()) {
          recordDefinition = getRecordDefinition(PathUtil.getName(typePath));
        }
        if (this.createMissingTables && recordDefinition == null) {
          recordDefinition = newTableRecordDefinition(sourceRecordDefinition);
        }
      }
      return recordDefinition;
    }
  }

  @Override
  public String getRecordStoreType() {
    return FileGdbRecordStoreFactory.DESCRIPTION;
  }

  protected Table getTable(final RecordDefinition recordDefinition) {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        final RecordDefinition fgdbRecordDefinition = getRecordDefinition(recordDefinition);
        if (!isExists() || fgdbRecordDefinition == null) {
          return null;
        } else {
          try {
            final Geodatabase geodatabase = getGeodatabase();
            if (geodatabase == null) {
              return null;
            } else {
              final String catalogPath = getCatalogPath(fgdbRecordDefinition);
              try {
                Table table = this.tableByCatalogPath.get(catalogPath);
                if (table == null) {
                  table = this.geodatabase.openTable(catalogPath);
                  if (table != null) {
                    if (this.tableByCatalogPath.isEmpty()) {
                      this.geodatabaseReferenceCount++;
                    }
                    Maps.addCount(this.tableReferenceCountsByCatalogPath, catalogPath);
                    this.tableByCatalogPath.put(catalogPath, table);
                  }
                } else {
                  Maps.addCount(this.tableReferenceCountsByCatalogPath, catalogPath);
                }
                return table;
              } catch (final RuntimeException e) {
                throw new RuntimeException("Unable to open table " + catalogPath, e);
              }
            }
          } finally {
            releaseGeodatabase();
          }
        }
      }
    }
  }

  protected Table getTableWithWriteLock(final RecordDefinition recordDefinition) {
    synchronized (this.apiSync) {
      final Table table = getTable(recordDefinition);
      if (table != null) {
        final String catalogPath = getCatalogPath(recordDefinition);

        final Integer count = Maps.addCount(this.tableWriteLockCountsByCatalogPath, catalogPath);
        if (count == 1) {
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
    if (!whereCondition.isEmpty()) {
      appendQueryValue(query, whereClause, whereCondition);
    }
    return whereClause;
  }

  protected boolean hasCatalogPath(final String path) {
    final String catalogPath = this.catalogPathByPath.get(path);
    return catalogPath != null;
  }

  private boolean hasChildDataset(final Geodatabase geodatabase, final String parentCatalogPath,
    final String datasetType, final String childCatalogPath) {
    try {
      final VectorOfWString childDatasets = geodatabase.getChildDatasets(parentCatalogPath,
        datasetType);
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
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        if (!this.initialized) {
          Geodatabase geodatabase = null;
          this.initialized = true;
          try {
            super.initialize();
            final File file = new File(this.fileName);
            if (file.exists()) {
              if (file.isDirectory()) {
                if (!new File(this.fileName, "gdb").exists()) {
                  throw new IllegalArgumentException(
                    FileUtil.getCanonicalPath(file) + " is not a valid ESRI File Geodatabase");
                }
                geodatabase = getSingleThreadResult(() -> {
                  return EsriFileGdb.openGeodatabase(this.fileName);
                });
              } else {
                throw new IllegalArgumentException(
                  FileUtil.getCanonicalPath(file) + " ESRI File Geodatabase must be a directory");
              }
            } else if (this.createMissingRecordStore) {
              geodatabase = newGeodatabase();
            } else {
              throw new IllegalArgumentException(
                "ESRI file geodatabase not found " + this.fileName);
            }
            final VectorOfWString domainNames = geodatabase.getDomains();
            for (int i = 0; i < domainNames.size(); i++) {
              final String domainName = domainNames.get(i);
              loadDomain(geodatabase, domainName);
            }
            this.exists = true;
          } catch (final Throwable e) {
            try {
              closeDo();
            } finally {
              Exceptions.throwUncheckedException(e);
            }
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
  public void insertRecord(final Record record) {
    if (record == null) {
    } else {
      final RecordDefinition recordDefinition = record.getRecordDefinition();
      final Table table = getTableWithWriteLock(recordDefinition);
      try {
        insertRecord(table, record);
      } finally {
        releaseTableAndWriteLock(recordDefinition);
      }
    }
  }

  void insertRecord(final Table table, final Record record) {
    final RecordDefinition sourceRecordDefinition = record.getRecordDefinition();
    final RecordDefinition recordDefinition = getRecordDefinition(sourceRecordDefinition);

    validateRequired(record, recordDefinition);

    final PathName typePath = recordDefinition.getPathName();
    if (table == null) {
      throw new ObjectException(record, "Cannot find table: " + typePath);
    } else {
      try {
        final Row row = newRowObject(table);

        try {
          for (final FieldDefinition field : recordDefinition.getFields()) {
            final String name = field.getName();
            try {
              final Object value = record.getValue(name);
              final AbstractFileGdbFieldDefinition esriField = (AbstractFileGdbFieldDefinition)field;
              esriField.setInsertValue(record, row, value);
            } catch (final Throwable e) {
              throw new ObjectPropertyException(record, name, e);
            }
          }
          insertRow(table, row);
          if (sourceRecordDefinition == recordDefinition) {
            for (final FieldDefinition field : recordDefinition.getFields()) {
              final AbstractFileGdbFieldDefinition esriField = (AbstractFileGdbFieldDefinition)field;
              try {
                esriField.setPostInsertValue(record, row);
              } catch (final Throwable e) {
                throw new ObjectPropertyException(record, field.getName(), e);
              }
            }
            record.setState(RecordState.PERSISTED);
          }
        } finally {
          row.delete();
          addStatistic("Insert", record);
        }
      } catch (final ObjectException e) {
        if (e.getObject() == record) {
          throw e;
        } else {
          throw new ObjectException(record, e);
        }
      } catch (final Throwable e) {
        throw new ObjectException(record, e);
      }
    }
  }

  protected void insertRow(final Table table, final Row row) {
    synchronized (this.apiSync) {
      if (isOpen(table)) {
        table.insertRow(row);
      }
    }
  }

  public boolean isCreateAreaField() {
    return this.createAreaField;
  }

  public boolean isCreateLengthField() {
    return this.createLengthField;
  }

  public boolean isCreateMissingRecordStore() {
    return this.createMissingRecordStore;
  }

  public boolean isCreateMissingTables() {
    return this.createMissingTables;
  }

  public boolean isExists() {
    return this.exists && !isClosed();
  }

  public boolean isNull(final Row row, final String name) {
    synchronized (this.apiSync) {
      return row.isNull(name);
    }
  }

  public boolean isOpen(final Table table) {
    synchronized (this.apiSync) {
      if (table == null) {
        return false;
      } else {
        final boolean open = this.tableByCatalogPath.containsValue(table);
        return open;
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
        final VectorOfWString children = geodatabase.getChildDatasets(parentPath,
          "Feature Dataset");
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

  private boolean isTableLocked(final PathName typePath) {
    final String path = getCatalogPath(typePath);
    return Maps.getCount(this.tableWriteLockCountsByCatalogPath, path) > 0;
  }

  protected FileGdbDomainCodeTable loadDomain(final Geodatabase geodatabase,
    final String domainName) {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        final String domainDef = geodatabase.getDomainDefinition(domainName);
        final Domain domain = EsriGdbXmlParser.parse(domainDef);
        if (domain != null) {
          final FileGdbDomainCodeTable codeTable = new FileGdbDomainCodeTable(this, domain);
          super.addCodeTable(codeTable);
          final List<String> fieldNames = this.domainFieldNames.get(domainName);
          if (fieldNames != null) {
            for (final String fieldName : fieldNames) {
              addCodeTable(fieldName, codeTable);
            }
          }
          return codeTable;
        }
      }
    }
    return null;
  }

  public synchronized CodeTable newDomainCodeTable(final Domain domain) {
    synchronized (this.apiSync) {
      final Geodatabase geodatabase = getGeodatabase();
      if (geodatabase != null) {
        try {
          final String domainName = domain.getDomainName();
          if (!this.domainFieldNames.containsKey(domainName)) {
            synchronized (API_SYNC) {
              final String domainDef = EsriGdbXmlSerializer.toString(domain);
              try {
                geodatabase.createDomain(domainDef);
              } catch (final Exception e) {
                Logs.debug(this, domainDef);
                Logs.error(this, "Unable to create domain", e);
              }
              return loadDomain(geodatabase, domain.getDomainName());
            }
          }
        } finally {
          releaseGeodatabase();
        }
      }
    }
    return null;
  }

  private RecordStoreSchema newFeatureDatasetSchema(final RecordStoreSchema parentSchema,
    final PathName schemaPath) {

    final PathName childSchemaPath = schemaPath;
    final RecordStoreSchema schema = new RecordStoreSchema(parentSchema, childSchemaPath);
    this.catalogPathByPath.put(childSchemaPath, toCatalogPath(schemaPath));
    return schema;
  }

  private Geodatabase newGeodatabase() {
    return getSingleThreadResult(() -> {
      return EsriFileGdb.createGeodatabase(this.fileName);
    });
  }

  @Override
  public AbstractIterator<Record> newIterator(final Query query,
    final Map<String, Object> properties) {
    PathName typePath = query.getTypePath();
    RecordDefinition recordDefinition = query.getRecordDefinition();
    if (recordDefinition == null) {
      recordDefinition = getRecordDefinition(typePath);
      if (recordDefinition == null) {
        throw new IllegalArgumentException("Type name does not exist " + typePath);
      }
    } else {
      typePath = recordDefinition.getPathName();
    }
    final String catalogPath = getCatalogPath(typePath);
    final BoundingBox boundingBox = QueryValue.getBoundingBox(query);
    final Map<String, Boolean> orderBy = query.getOrderBy();
    final StringBuilder whereClause = getWhereClause(query);
    StringBuilder sql = new StringBuilder();
    if (orderBy.isEmpty() || boundingBox != null) {
      if (!orderBy.isEmpty()) {
        Logs.error(this, "Unable to sort on " + catalogPath + " " + orderBy.keySet()
          + " as the ESRI library can't sort with a bounding box query");
      }
      sql = whereClause;
    } else {
      sql.append("SELECT ");

      final List<String> fieldNames = query.getFieldNames();
      if (fieldNames.isEmpty()) {
        StringBuilders.append(sql, recordDefinition.getFieldNames());
      } else {
        StringBuilders.append(sql, fieldNames);
      }
      sql.append(" FROM ");
      sql.append(JdbcUtils.getTableName(catalogPath));
      if (whereClause.length() > 0) {
        sql.append(" WHERE ");
        sql.append(whereClause);
      }
      boolean first = true;
      for (final Entry<String, Boolean> entry : orderBy.entrySet()) {
        final String column = entry.getKey();
        final DataType dataType = recordDefinition.getFieldType(column);
        if (dataType != null && !Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
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
          Logs.error(this, "Unable to sort on " + recordDefinition.getPath() + "." + column
            + " as the ESRI library can't sort on " + dataType + " columns");
        }
      }
    }

    final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this, catalogPath,
      sql.toString(), boundingBox, query, query.getOffset(), query.getLimit());
    iterator.setStatistics(query.getStatistics());
    return iterator;
  }

  @Override
  public Identifier newPrimaryIdentifier(final PathName typePath) {
    synchronized (this.apiSync) {
      final RecordDefinition recordDefinition = getRecordDefinition(typePath);
      if (recordDefinition == null) {
        return null;
      } else {
        final String idFieldName = recordDefinition.getIdFieldName();
        if (idFieldName == null) {
          return null;
        } else if (!idFieldName.equals("OBJECTID")) {
          AtomicLong idGenerator = this.idGenerators.get(typePath);
          if (idGenerator == null) {
            long maxId = 0;
            for (final Record record : getRecords(typePath)) {
              final Identifier id = record.getIdentifier();
              final Object firstId = id.getValue(0);
              if (firstId instanceof Number) {
                final Number number = (Number)firstId;
                if (number.longValue() > maxId) {
                  maxId = number.longValue();
                }
              }
            }
            idGenerator = new AtomicLong(maxId);
            this.idGenerators.put(typePath, idGenerator);
          }
          return Identifier.newIdentifier(idGenerator.incrementAndGet());
        } else {
          return null;
        }
      }
    }
  }

  @Override
  public FileGdbWriter newRecordWriter() {
    synchronized (this.apiSync) {
      FileGdbWriter writer = getThreadProperty("writer");
      if (writer == null || writer.isClosed()) {
        writer = new FileGdbWriter(this);
        setThreadProperty("writer", writer);
      }
      return writer;
    }
  }

  protected Row newRowObject(final Table table) {
    synchronized (this.apiSync) {
      if (isOpen(table)) {
        return table.createRowObject();
      } else {
        return null;
      }
    }
  }

  private RecordStoreSchema newSchema(final PathName schemaPath,
    final SpatialReference spatialReference) {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        final Geodatabase geodatabase = getGeodatabase();
        if (geodatabase == null) {
          return null;
        } else {
          try {
            String parentCatalogPath = "\\";
            RecordStoreSchema schema = getRootSchema();
            for (final PathName childSchemaPath : schemaPath.getPaths()) {
              if (childSchemaPath.length() > 1) {
                RecordStoreSchema childSchema = schema.getSchema(childSchemaPath);
                final String childCatalogPath = toCatalogPath(childSchemaPath);
                if (!hasChildDataset(getGeodatabase(), parentCatalogPath, "Feature Dataset",
                  childCatalogPath)) {
                  if (spatialReference != null) {
                    final DEFeatureDataset dataset = EsriXmlRecordDefinitionUtil
                      .newDEFeatureDataset(childCatalogPath, spatialReference);
                    final String datasetDefinition = EsriGdbXmlSerializer.toString(dataset);
                    try {
                      geodatabase.createFeatureDataset(datasetDefinition);
                    } catch (final Throwable t) {
                      Logs.debug(this, datasetDefinition);
                      throw new RuntimeException(
                        "Unable to create feature dataset " + childCatalogPath, t);
                    }
                  }
                }
                if (childSchema == null) {
                  childSchema = newFeatureDatasetSchema(schema, childSchemaPath);
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

  private RecordDefinitionImpl newTableRecordDefinition(final DETable deTable) {
    synchronized (this.apiSync) {
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
            PathName schemaPath = toPath(schemaCatalogPath);
            final RecordStoreSchema schema = newSchema(schemaPath, spatialReference);

            if (schemaPath.equals(this.defaultSchemaPath)) {
              if (!(deTable instanceof DEFeatureClass)) {
                schemaCatalogPath = "\\";
                deTable.setCatalogPath("\\" + deTable.getName());
              }
            } else if (schemaPath.equals("")) {
              schemaPath = this.defaultSchemaPath;
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
              final Table table = geodatabase.createTable(tableDefinition, schemaCatalogPath);
              geodatabase.closeTable(table);
              table.delete();
              final RecordDefinitionImpl recordDefinition = getRecordDefinition(
                PathName.newPathName(schemaPath), schemaCatalogPath, tableDefinition);
              initRecordDefinition(recordDefinition);
              schema.addElement(recordDefinition);
              return recordDefinition;
            } catch (final Throwable t) {
              throw new RuntimeException("Unable to create table " + deTable.getCatalogPath(), t);
            }
          } finally {
            releaseGeodatabase();
          }
        }
      }
    }
  }

  private RecordDefinition newTableRecordDefinition(final RecordDefinition recordDefinition) {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        final GeometryFactory geometryFactory = recordDefinition.getGeometryFactory();
        final SpatialReference spatialReference = getSpatialReference(geometryFactory);

        final DETable deTable = EsriXmlRecordDefinitionUtil.getDETable(recordDefinition,
          spatialReference, this.createLengthField, this.createAreaField);
        final RecordDefinitionImpl tableRecordDefinition = newTableRecordDefinition(deTable);
        final String idFieldName = recordDefinition.getIdFieldName();
        if (idFieldName != null) {
          tableRecordDefinition.setIdFieldName(idFieldName);
        }
        return tableRecordDefinition;
      }
    }
  }

  @Override
  protected void obtainConnected() {
    getGeodatabase();
  }

  private Geodatabase openGeodatabase() {
    return getSingleThreadResult(() -> {
      try {
        return EsriFileGdb.openGeodatabase(this.fileName);
      } catch (final FileGdbException e) {
        final String message = e.getMessage();
        if ("The system cannot find the path specified. (-2147024893)".equals(message)) {
          return null;
        } else {
          throw e;
        }
      }
    });
  }

  public FileGdbEnumRowsIterator query(final String sql, final boolean recycling) {
    EnumRows rows = null;
    synchronized (this.apiSync) {
      final Geodatabase geodatabase = getGeodatabase();
      if (geodatabase == null) {
        return null;
      } else {
        try {
          rows = geodatabase.query(sql, recycling);
        } catch (final Throwable t) {
          throw new RuntimeException("Error running sql: " + sql, t);
        } finally {
          releaseGeodatabase();
        }
      }
    }
    return new FileGdbEnumRowsIterator(rows);
  }

  @Override
  protected Map<PathName, ? extends RecordStoreSchemaElement> refreshSchemaElements(
    final RecordStoreSchema schema) {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        final Map<PathName, RecordStoreSchemaElement> elementsByPath = new TreeMap<>();
        final Geodatabase geodatabase = getGeodatabase();
        if (geodatabase != null) {
          try {
            final PathName schemaPath = schema.getPathName();
            final String schemaCatalogPath = getCatalogPath(schema);
            final VectorOfWString childDatasets = getChildDatasets(geodatabase, schemaCatalogPath,
              "Feature Dataset");
            if (childDatasets != null) {
              for (int i = 0; i < childDatasets.size(); i++) {
                final String childCatalogPath = childDatasets.get(i);
                final PathName childPath = toPath(childCatalogPath);
                RecordStoreSchema childSchema = schema.getSchema(childPath);
                if (childSchema == null) {
                  childSchema = newFeatureDatasetSchema(schema, childPath);
                } else {
                  if (childSchema.isInitialized()) {
                    childSchema.refresh();
                  }
                }
                elementsByPath.put(childPath, childSchema);
              }
            }
            if (schemaPath.isParentOf(this.defaultSchemaPath)
              && !elementsByPath.containsKey(this.defaultSchemaPath)) {
              final SpatialReference spatialReference = getSpatialReference(getGeometryFactory());
              final RecordStoreSchema childSchema = newSchema(this.defaultSchemaPath,
                spatialReference);
              elementsByPath.put(this.defaultSchemaPath, childSchema);
            }

            if (schema.equalPath(this.defaultSchemaPath)) {
              refreshSchemaRecordDefinitions(elementsByPath, schemaPath, "\\", "Feature Class");
              refreshSchemaRecordDefinitions(elementsByPath, schemaPath, "\\", "Table");
            }
            refreshSchemaRecordDefinitions(elementsByPath, schemaPath, schemaCatalogPath,
              "Feature Class");
            refreshSchemaRecordDefinitions(elementsByPath, schemaPath, schemaCatalogPath, "Table");
          } finally {
            releaseGeodatabase();
          }
        }
        return elementsByPath;
      }
    }
  }

  private void refreshSchemaRecordDefinitions(
    final Map<PathName, RecordStoreSchemaElement> elementsByPath, final PathName schemaPath,
    final String catalogPath, final String datasetType) {
    synchronized (this.apiSync) {
      synchronized (API_SYNC) {
        final Geodatabase geodatabase = getGeodatabase();
        if (geodatabase != null) {
          try {
            final boolean pathExists = isPathExists(geodatabase, catalogPath);
            if (pathExists) {
              final VectorOfWString childFeatureClasses = getChildDatasets(geodatabase, catalogPath,
                datasetType);
              if (childFeatureClasses != null) {
                for (int i = 0; i < childFeatureClasses.size(); i++) {
                  final String childCatalogPath = childFeatureClasses.get(i);
                  final String tableDefinition = geodatabase.getTableDefinition(childCatalogPath);
                  final RecordDefinition recordDefinition = getRecordDefinition(schemaPath,
                    childCatalogPath, tableDefinition);
                  initRecordDefinition(recordDefinition);
                  final PathName childPath = recordDefinition.getPathName();
                  elementsByPath.put(childPath, recordDefinition);
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
    synchronized (this.apiSync) {
      if (this.geodatabase != null) {
        this.geodatabaseReferenceCount--;
        if (this.geodatabaseReferenceCount <= 0) {
          this.geodatabaseReferenceCount = 0;
          try {
            closeGeodatabase(this.geodatabase);
          } finally {
            this.geodatabase = null;
          }
        }
      }
    }
  }

  protected void releaseTable(final String catalogPath) {
    synchronized (this.apiSync) {
      final Geodatabase geodatabase = getGeodatabase();
      if (geodatabase != null) {
        try {
          final Table table = this.tableByCatalogPath.get(catalogPath);
          if (table != null) {
            final Integer count = Maps.decrementCount(this.tableReferenceCountsByCatalogPath,
              catalogPath);
            if (count == 0) {
              try {
                this.tableByCatalogPath.remove(catalogPath);
                this.tableWriteLockCountsByCatalogPath.remove(catalogPath);
                geodatabase.closeTable(table);
              } catch (final Exception e) {
                Logs.error(this, "Unable to close table: " + catalogPath, e);
              } finally {
                if (this.tableByCatalogPath.isEmpty()) {
                  this.geodatabaseReferenceCount--;
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

  protected void releaseTableAndWriteLock(final RecordDefinition recordDefinition) {
    final String catalogPath = getCatalogPath(recordDefinition);
    releaseTableAndWriteLock(catalogPath);
  }

  protected void releaseTableAndWriteLock(final String catalogPath) {
    synchronized (this.apiSync) {
      final Geodatabase geodatabase = getGeodatabase();
      if (geodatabase != null) {
        try {
          final Table table = this.tableByCatalogPath.get(catalogPath);
          if (table != null) {
            final Integer count = Maps.decrementCount(this.tableWriteLockCountsByCatalogPath,
              catalogPath);
            if (count == 0) {
              try {
                table.setLoadOnlyMode(false);
                table.freeWriteLock();
              } catch (final Exception e) {
                Logs.error(this, "Unable to free write lock for table: " + catalogPath, e);
              }
            }
          }
          releaseTable(catalogPath);
        } finally {
          releaseGeodatabase();
        }
      }
    }
  }

  public FileGdbEnumRowsIterator search(final Object typePath, final Table table,
    final String fields, final String whereClause, final boolean recycling) {
    EnumRows rows = null;
    synchronized (this.apiSync) {
      if (isOpen(table)) {
        try {
          rows = table.search(fields, whereClause, recycling);
        } catch (final Throwable t) {
          if (!isClosed()) {
            Logs.error(this,
              "Unable to execute query " + fields + " FROM " + typePath + " WHERE " + whereClause,
              t);
          }

        }
      }
      return new FileGdbEnumRowsIterator(rows);
    }
  }

  public FileGdbEnumRowsIterator search(final Object typePath, final Table table,
    final String fields, final String whereClause, final Envelope boundingBox,
    final boolean recycling) {
    EnumRows rows = null;
    if (!boundingBox.IsEmpty()) {
      synchronized (this.apiSync) {
        if (isOpen(table)) {
          try {
            rows = table.search(fields, whereClause, boundingBox, recycling);
          } catch (final Exception e) {
            if (!isClosed()) {
              Logs.error(this,
                "ERROR executing query SELECT " + fields + " FROM " + typePath + " WHERE "
                  + whereClause + " AND GEOMETRY intersects BBOX(" + boundingBox.getXMin() + " "
                  + boundingBox.getYMin() + "," + boundingBox.getXMax() + " "
                  + boundingBox.getYMax() + ")",
                e);
            }
          }
        }
      }
    }
    return new FileGdbEnumRowsIterator(rows);
  }

  public void setCreateAreaField(final boolean createAreaField) {
    this.createAreaField = createAreaField;
  }

  public void setCreateLengthField(final boolean createLengthField) {
    this.createLengthField = createLengthField;
  }

  public void setCreateMissingRecordStore(final boolean createMissingRecordStore) {
    this.createMissingRecordStore = createMissingRecordStore;
  }

  public void setCreateMissingTables(final boolean createMissingTables) {
    this.createMissingTables = createMissingTables;
  }

  public void setDefaultSchema(final PathName defaultSchema) {
    synchronized (this.apiSync) {
      if (Property.hasValue(defaultSchema)) {
        this.defaultSchemaPath = defaultSchema;
      } else {
        this.defaultSchemaPath = PathName.ROOT;
      }
    }
  }

  public void setDefaultSchema(final String defaultSchema) {
    synchronized (this.apiSync) {
      if (Property.hasValue(defaultSchema)) {
        this.defaultSchemaPath = PathName.newPathName(defaultSchema);
      } else {
        this.defaultSchemaPath = PathName.ROOT;
      }
    }
  }

  public void setDomainFieldNames(final Map<String, List<String>> domainFieldNames) {
    this.domainFieldNames = domainFieldNames;
  }

  public void setFileName(final String fileName) {
    this.fileName = fileName;
  }

  public void setNull(final Row row, final String name) {
    synchronized (this.apiSync) {
      row.setNull(name);
    }
  }

  public String toCatalogPath(final PathName path) {
    return path.getPath().replaceAll("/", "\\\\");
  }

  protected PathName toPath(final String catalogPath) {
    return PathName.newPathName(catalogPath);
  }

  @Override
  public String toString() {
    return this.fileName;
  }

  @Override
  public void updateRecord(final Record record) {
    if (record == null) {
    } else {
      final RecordDefinition recordDefinition = record.getRecordDefinition();
      final Table table = getTableWithWriteLock(recordDefinition);
      try {
        updateRecord(table, record);
      } finally {
        releaseTableAndWriteLock(recordDefinition);
      }
    }
  }

  void updateRecord(final Table table, final Record record) {
    final Object objectId = record.getValue("OBJECTID");
    if (objectId == null) {
      insertRecord(table, record);
    } else {
      final RecordDefinition sourceRecordDefinition = record.getRecordDefinition();
      final RecordDefinition recordDefinition = getRecordDefinition(sourceRecordDefinition);

      validateRequired(record, recordDefinition);

      final PathName typePath = sourceRecordDefinition.getPathName();
      final String whereClause = "OBJECTID=" + objectId;
      try (
        final FileGdbEnumRowsIterator rows = search(typePath, table, "*", whereClause, false)) {
        for (final Row row : rows) {
          try {
            for (final FieldDefinition field : recordDefinition.getFields()) {
              final String name = field.getName();
              try {
                final Object value = record.getValue(name);
                final AbstractFileGdbFieldDefinition esriField = (AbstractFileGdbFieldDefinition)field;
                esriField.setUpdateValue(record, row, value);
              } catch (final Throwable e) {
                throw new ObjectPropertyException(record, name, e);
              }
            }
            updateRow(typePath, table, row);
            record.setState(RecordState.PERSISTED);
            addStatistic("Update", record);
          } catch (final ObjectException e) {
            if (e.getObject() == record) {
              throw e;
            } else {
              throw new ObjectException(record, e);
            }
          } catch (final Throwable e) {
            throw new ObjectException(record, e);
          }
        }
      }
    }
  }

  protected void updateRow(final PathName typePath, final Table table, final Row row) {
    synchronized (this.apiSync) {
      if (isOpen(table)) {
        final boolean loadOnly = isTableLocked(typePath);
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

  private void validateRequired(final Record record, final RecordDefinition recordDefinition) {
    for (final FieldDefinition field : recordDefinition.getFields()) {
      final String name = field.getName();
      if (field.isRequired()) {
        final Object value = record.getValue(name);
        if (value == null && !((AbstractFileGdbFieldDefinition)field).isAutoCalculated()) {
          throw new ObjectPropertyException(record, name, "Value required");
        }
      }
    }
  }

}
