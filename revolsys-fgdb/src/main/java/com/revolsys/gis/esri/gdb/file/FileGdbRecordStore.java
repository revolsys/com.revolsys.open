package com.revolsys.gis.esri.gdb.file;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.identifier.SingleIdentifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.date.Dates;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.collection.map.IntHashMap;
import com.revolsys.esri.filegdb.jni.EsriFileGdb;
import com.revolsys.esri.filegdb.jni.Geodatabase;
import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.esri.filegdb.jni.Table;
import com.revolsys.esri.filegdb.jni.VectorOfWString;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.esri.gdb.file.capi.FileGdbDomainCodeTable;
import com.revolsys.gis.esri.gdb.file.capi.type.GeometryFieldDefinition;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.parallel.SingleThreadExecutor;
import com.revolsys.record.Record;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.io.format.esri.gdb.xml.model.DEFeatureClass;
import com.revolsys.record.io.format.esri.gdb.xml.model.DEFeatureDataset;
import com.revolsys.record.io.format.esri.gdb.xml.model.DETable;
import com.revolsys.record.io.format.esri.gdb.xml.model.Domain;
import com.revolsys.record.io.format.esri.gdb.xml.model.EsriGdbXmlParser;
import com.revolsys.record.io.format.esri.gdb.xml.model.EsriGdbXmlSerializer;
import com.revolsys.record.io.format.esri.gdb.xml.model.EsriXmlRecordDefinitionUtil;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.record.io.format.esri.gdb.xml.model.SpatialReference;
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
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.record.schema.RecordStoreSchemaElement;
import com.revolsys.util.StringBuilders;

public class FileGdbRecordStore extends AbstractRecordStore {

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\?");

  private static IntHashMap<String> WKT_BY_ID = new IntHashMap<>();

  private static final SingleThreadExecutor TASK_EXECUTOR;
  static {
    TASK_EXECUTOR = new SingleThreadExecutor("ESRI FGDB Create Thread", new FgdbApiInit());
    TASK_EXECUTOR.waitForRunning();
  }

  public static SpatialReference getSpatialReference(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      final int horizontalCoordinateSystemId = geometryFactory.getHorizontalCoordinateSystemId();
      if (horizontalCoordinateSystemId > 0) {
        String wkt;
        synchronized (WKT_BY_ID) {
          wkt = WKT_BY_ID.get(horizontalCoordinateSystemId);
          if (wkt == null) {
            wkt = EsriFileGdb.getSpatialReferenceWkt(horizontalCoordinateSystemId);
            WKT_BY_ID.put(horizontalCoordinateSystemId, wkt);
          }
        }
        final SpatialReference spatialReference = SpatialReference.get(geometryFactory, wkt);
        return spatialReference;
      }
    }
    return null;
  }

  public static String toCatalogPath(final PathName path) {
    return path.getPath().replaceAll("/", "\\\\");
  }

  private PathName defaultSchemaPath = PathName.ROOT;

  private Map<String, List<String>> domainFieldNames = new HashMap<>();

  private boolean exists = false;

  private String fileName;

  private final GeodatabaseReference geodatabase = new GeodatabaseReference();

  private final Map<PathName, AtomicLong> idGenerators = new HashMap<>();

  private final Map<String, TableReference> tableByCatalogPath = new HashMap<>();

  private boolean createLengthField = false;

  private boolean createAreaField = false;

  FileGdbRecordStore(final File file) {
    this.fileName = FileUtil.getCanonicalPath(file);
    setConnectionProperties(Collections.singletonMap("url", FileUtil.toUrl(file).toString()));
    setCreateMissingRecordStore(true);
    setCreateMissingTables(true);
  }

  @Override
  public void addCodeTable(final CodeTable codeTable) {
    super.addCodeTable(codeTable);
    if (codeTable instanceof Domain) {
      final Domain domain = (Domain)codeTable;
      newDomainCodeTable(domain);
    }
  }

  @Override
  protected void addRecordDefinitionProperties(final RecordDefinitionImpl recordDefinition) {
    super.addRecordDefinitionProperties(recordDefinition);
  }

  public void alterDomain(final Domain domain) {
    this.geodatabase.alterDomain(domain);
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
      final QueryValue right = unaryCondition.getValue();
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
    synchronized (this.geodatabase) {
      try {
        if (!isClosed()) {
          final Writer<Record> writer = getThreadProperty("writer");
          if (writer != null) {
            setThreadProperty("writer", null);
            writer.close();
          }
          for (final TableReference table : this.tableByCatalogPath.values()) {
            table.close();
          }
          this.tableByCatalogPath.clear();

          this.geodatabase.close();
        }
      } finally {
        super.close();
      }
    }
  }

  private void closeGeodatabase(final Geodatabase geodatabase) {
    if (geodatabase != null) {
      final int closeResult = EsriFileGdb.CloseGeodatabase(geodatabase);
      if (closeResult != 0) {
        Logs.error(this, "Error closing: " + this.fileName + " ESRI Error=" + closeResult);
      }
    }
  }

  public void compactGeodatabase() {
    this.geodatabase.compactGeodatabase();
  }

  public void deleteGeodatabase() {
    setCreateMissingRecordStore(false);
    setCreateMissingTables(false);
    final String fileName = this.fileName;
    try {
      closeDo();
    } finally {
      if (new File(fileName).exists()) {
        final Integer deleteResult = this.geodatabase.deleteGeodatabase();
        if (deleteResult != null && deleteResult != 0) {
          Logs.error(this, "Error deleting: " + fileName + " ESRI Error=" + deleteResult);
        }
      }
    }
  }

  @Override
  public boolean deleteRecord(final Record record) {
    try (
      TableWrapper tableWrapper = getTableWrapper(record)) {
      if (tableWrapper != null) {
        return tableWrapper.deleteRecord(record);
      }
    }
    return false;
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

  @Override
  public Record getRecord(final PathName typePath, final Object... id) {
    final FileGdbRecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      throw new IllegalArgumentException("Unknown type " + typePath);
    } else {
      final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this, recordDefinition,
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

  @Override
  public int getRecordCount(final Query query) {
    if (query == null) {
      return 0;
    } else {
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

      final TableReference table = getTableReference(recordDefinition);
      if (boundingBox == null) {
        if (whereClause.length() == 0) {
          return table.getRecordCount();
        } else {
          final StringBuilder sql = new StringBuilder();
          sql.append("SELECT OBJECTID FROM ");
          sql.append(JdbcUtils.getTableName(typePath));
          if (whereClause.length() > 0) {
            sql.append(" WHERE ");
            sql.append(whereClause);
          }

          try (
            TableWrapper tableWrapper = table.connect();
            final FileGdbEnumRowsIterator rows = tableWrapper.query(sql.toString(), true)) {
            int count = 0;
            for (@SuppressWarnings("unused")
            final Row row : rows) {
              count++;
            }
            return count;
          }
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
            TableWrapper tableWrapper = table.connect();
            final FileGdbEnumRowsIterator rows = tableWrapper.query(sql.toString(), false)) {
            int count = 0;
            for (final Row row : rows) {
              final Geometry geometry = (Geometry)geometryField.getValue(row);
              if (geometry != null) {
                final BoundingBox geometryBoundingBox = geometry.getBoundingBox();
                if (geometryBoundingBox.bboxIntersects(boundingBox)) {
                  count++;
                }
              }
            }
            return count;
          }
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <RD extends RecordDefinition> RD getRecordDefinition(
    final RecordDefinition sourceRecordDefinition) {
    if (sourceRecordDefinition == null) {
      return null;
    } else if (sourceRecordDefinition.equalsRecordStore(this)) {
      return (RD)sourceRecordDefinition;
    } else {
      synchronized (this.geodatabase) {
        final PathName pathName = sourceRecordDefinition.getPathName();
        RecordDefinition recordDefinition = getRecordDefinition(pathName);
        if (recordDefinition == null) {
          if (!sourceRecordDefinition.hasGeometryField()) {
            final PathName name = pathName.getNamePath();
            recordDefinition = getRecordDefinition(name);
          }
          if (isCreateMissingTables() && recordDefinition == null) {
            if (getGeometryFactory() == null) {
              setGeometryFactory(sourceRecordDefinition.getGeometryFactory());
            }
            final GeometryFactory geometryFactory = sourceRecordDefinition.getGeometryFactory();
            final SpatialReference spatialReference = getSpatialReference(geometryFactory);

            final DETable deTable = EsriXmlRecordDefinitionUtil.getDETable(sourceRecordDefinition,
              spatialReference, this.createLengthField, this.createAreaField);
            final RecordDefinitionImpl tableRecordDefinition = newTableRecordDefinition(deTable);
            final String idFieldName = sourceRecordDefinition.getIdFieldName();
            if (idFieldName != null) {
              tableRecordDefinition.setIdFieldName(idFieldName);
            }
            recordDefinition = tableRecordDefinition;
          }
        }
        return (RD)recordDefinition;
      }
    }
  }

  @Override
  public String getRecordStoreType() {
    return FileGdbRecordStoreFactory.DESCRIPTION;
  }

  public TableWrapper getTable(final PathName path) {
    final TableReference table = getTableReference(path);
    if (table == null) {
      return null;
    } else {
      return table.connect();
    }
  }

  public TableWrapper getTableLocked(final PathName path) {
    final TableReference table = getTableReference(path);
    if (table == null) {
      return null;
    } else {
      return table.writeLock(false);
    }
  }

  public TableWrapper getTableLocked(final RecordDefinition recordDefinition,
    final boolean loadOnlyMode) {
    final TableReference table = getTableReference(recordDefinition);
    if (table == null) {
      return null;
    } else {
      return table.writeLock(loadOnlyMode);
    }
  }

  TableReference getTableReference(final FileGdbRecordDefinition recordDefinition,
    final PathName pathName, final String catalogPath) {
    synchronized (this.geodatabase) {
      TableReference tableReference = this.tableByCatalogPath.get(catalogPath);
      if (tableReference == null) {
        tableReference = new TableReference(this, recordDefinition, this.geodatabase, pathName,
          catalogPath);
        this.tableByCatalogPath.put(catalogPath, tableReference);
      }
      return tableReference;
    }
  }

  private TableReference getTableReference(final PathName path) {
    final FileGdbRecordDefinition recordDefinition = getRecordDefinition(path);
    if (recordDefinition != null) {
      return recordDefinition.getTableReference();
    }
    return null;
  }

  protected TableReference getTableReference(final RecordDefinition recordDefinition) {
    final FileGdbRecordDefinition fileGdbRecordDefinition = getRecordDefinition(recordDefinition);
    if (fileGdbRecordDefinition == null) {
      return null;
    } else {
      return fileGdbRecordDefinition.getTableReference();
    }
  }

  protected TableReference getTableReference(final RecordDefinitionProxy recordDefinition) {
    if (recordDefinition != null) {
      final RecordDefinition rd = recordDefinition.getRecordDefinition();
      return getTableReference(rd);
    }
    return null;
  }

  private TableWrapper getTableWrapper(final Record record) {
    final TableReference tableReference = getTableReference(record);
    if (tableReference != null) {
      return tableReference.connect();
    }
    return null;
  }

  protected StringBuilder getWhereClause(final Query query) {
    final StringBuilder whereClause = new StringBuilder();
    final Condition whereCondition = query.getWhereCondition();
    if (!whereCondition.isEmpty()) {
      appendQueryValue(query, whereClause, whereCondition);
    }
    return whereClause;
  }

  @Override
  public void initializeDo() {
    boolean exists = false;
    synchronized (this.geodatabase) {
      Geodatabase geodatabase = null;
      try {
        super.initializeDo();
        final File file = new File(this.fileName);
        if (file.exists()) {
          if (file.isDirectory()) {
            if (!new File(this.fileName, "gdb").exists()) {
              throw new IllegalArgumentException(
                FileUtil.getCanonicalPath(file) + " is not a valid ESRI File Geodatabase");
            }
            geodatabase = EsriFileGdb.openGeodatabase(this.fileName);
          } else {
            throw new IllegalArgumentException(
              FileUtil.getCanonicalPath(file) + " ESRI File Geodatabase must be a directory");
          }
        } else if (isCreateMissingRecordStore()) {
          geodatabase = EsriFileGdb.createGeodatabase(this.fileName);
          final FileGdbRecordStoreSchema rootSchema = getRootSchema();
          rootSchema.setInitialized(true);
        } else {
          throw new IllegalArgumentException("ESRI file geodatabase not found " + this.fileName);
        }
        if (geodatabase == null) {
          throw new IllegalArgumentException(
            "Unable to open ESRI file geodatabase not found " + this.fileName);
        }
        final VectorOfWString domainNames = geodatabase.getDomains();
        for (int i = 0; i < domainNames.size(); i++) {
          final String domainName = domainNames.get(i);
          final String domainDef = this.geodatabase.getDomainDefinition(domainName);
          loadDomain(domainName, domainDef);
        }
        exists = true;
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
    this.exists = exists;
    this.geodatabase.setFileName(this.fileName);
  }

  @Override
  public void insertRecord(final Record record) {
    try (
      TableWrapper tableWrapper = getTableWrapper(record)) {
      if (tableWrapper != null) {
        tableWrapper.insertRecord(record);
      }
    }
  }

  public boolean isCreateAreaField() {
    return this.createAreaField;
  }

  public boolean isCreateLengthField() {
    return this.createLengthField;
  }

  public boolean isExists() {
    return this.exists && !isClosed();
  }

  private FileGdbDomainCodeTable loadDomain(final String domainName, final String domainDef) {
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
    return null;
  }

  void lockTable(final Table table) {
    synchronized (this.geodatabase) {
      table.setWriteLock();
    }
  }

  public CodeTable newDomainCodeTable(final Domain domain) {
    final String domainName = domain.getDomainName();
    if (!this.domainFieldNames.containsKey(domainName)) {
      final String domainDef = EsriGdbXmlSerializer.toString(domain);
      try {
        this.geodatabase.createDomain(domainDef);
        return loadDomain(domainName, domainDef);
      } catch (final Exception e) {
        Logs.debug(this, domainDef);
        Logs.error(this, "Unable to create domain", e);
      }
    }
    return null;
  }

  @Override
  public AbstractIterator<Record> newIterator(final Query query,
    final Map<String, Object> properties) {
    PathName pathName = query.getTypePath();
    final RecordDefinition recordDefinition = query.getRecordDefinition();
    if (recordDefinition != null) {
      pathName = recordDefinition.getPathName();
    }
    final FileGdbRecordDefinition fileGdbRecordDefinition = getRecordDefinition(pathName);
    if (fileGdbRecordDefinition == null) {
      throw new IllegalArgumentException("Type name does not exist " + pathName);
    }
    final String catalogPath = fileGdbRecordDefinition.getCatalogPath();
    final BoundingBox boundingBox = QueryValue.getBoundingBox(query);
    final Map<? extends CharSequence, Boolean> orderBy = query.getOrderBy();
    final StringBuilder whereClause = getWhereClause(query);
    StringBuilder sql = new StringBuilder();
    if (orderBy.isEmpty() || boundingBox != null) {
      if (!orderBy.isEmpty()) {
        Logs.error(this, "Unable to sort on " + fileGdbRecordDefinition.getPathName() + " "
          + orderBy.keySet() + " as the ESRI library can't sort with a bounding box query");
      }
      sql = whereClause;
    } else {
      sql.append("SELECT ");

      final List<String> fieldNames = query.getFieldNames();
      if (fieldNames.isEmpty()) {
        StringBuilders.append(sql, fileGdbRecordDefinition.getFieldNames());
      } else {
        StringBuilders.append(sql, fieldNames);
      }
      sql.append(" FROM ");
      sql.append(JdbcUtils.getTableName(catalogPath));
      if (whereClause.length() > 0) {
        sql.append(" WHERE ");
        sql.append(whereClause);
      }
      boolean useOrderBy = true;
      if (orderBy.size() == 1) {
        final Entry<? extends CharSequence, Boolean> entry = orderBy.entrySet().iterator().next();
        final CharSequence fieldName = entry.getKey();
        if (entry.getValue() == Boolean.TRUE && fieldName.toString().equals("OBJECTID")) {
          useOrderBy = false;
        }
      }
      if (useOrderBy) {
        boolean first = true;
        for (final Entry<? extends CharSequence, Boolean> entry : orderBy.entrySet()) {
          final CharSequence fieldName = entry.getKey();

          final DataType dataType = fileGdbRecordDefinition.getFieldType(fieldName);
          if (dataType != null && !Geometry.class.isAssignableFrom(dataType.getJavaClass())) {
            if (first) {
              sql.append(" ORDER BY ");
              first = false;
            } else {
              sql.append(", ");
            }
            if (fieldName instanceof FieldDefinition) {
              final FieldDefinition field = (FieldDefinition)fieldName;
              field.appendColumnName(sql);
            } else {
              sql.append(fieldName);
            }
            final Boolean ascending = entry.getValue();
            if (!ascending) {
              sql.append(" DESC");
            }

          } else {
            Logs.error(this, "Unable to sort on " + fileGdbRecordDefinition.getPath() + "."
              + fieldName + " as the ESRI library can't sort on " + dataType + " columns");
          }
        }
      }
    }

    final FileGdbQueryIterator iterator = new FileGdbQueryIterator(this, fileGdbRecordDefinition,
      sql.toString(), boundingBox, query, query.getOffset(), query.getLimit());
    iterator.setStatistics(query.getStatistics());
    return iterator;
  }

  @Override
  public Identifier newPrimaryIdentifier(final PathName typePath) {
    synchronized (this.idGenerators) {
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
    return newRecordWriter(false);
  }

  @Override
  public FileGdbWriter newRecordWriter(final boolean throwExceptions) {
    FileGdbWriter writer = getThreadProperty("writer");
    if (writer == null || writer.isClosed()) {
      writer = new FileGdbWriter(this);
      setThreadProperty("writer", writer);
    }
    return writer;
  }

  @Override
  public FileGdbWriter newRecordWriter(final RecordDefinitionProxy recordDefinition) {
    return newRecordWriter(recordDefinition, false);
  }

  public FileGdbWriter newRecordWriter(final RecordDefinitionProxy recordDefinition,
    final boolean loadOnlyMode) {
    synchronized (this.geodatabase) {
      final GeodatabaseReference geodatabase = this.geodatabase;
      if (geodatabase != null) {
        synchronized (geodatabase) {
          try (
            BaseCloseable connection = geodatabase.connect()) {
            final FileGdbRecordDefinition fileGdbRecordDefinition = getRecordDefinition(
              recordDefinition);
            if (fileGdbRecordDefinition != null) {
              return new FileGdbWriter(this, recordDefinition, fileGdbRecordDefinition,
                loadOnlyMode);
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  protected RecordStoreSchema newRootSchema() {
    return new FileGdbRecordStoreSchema(this);
  }

  private FileGdbRecordStoreSchema newSchema(final PathName schemaPath,
    final SpatialReference spatialReference) {
    synchronized (this.geodatabase) {
      try (
        BaseCloseable geodatabaseClosable = this.geodatabase.connect()) {
        String parentCatalogPath = "\\";
        FileGdbRecordStoreSchema schema = getRootSchema();
        for (final PathName childSchemaPath : schemaPath.getPaths()) {
          if (childSchemaPath.length() > 1) {
            FileGdbRecordStoreSchema childSchema = schema.getSchema(childSchemaPath);
            final String childCatalogPath = toCatalogPath(childSchemaPath);
            if (!this.geodatabase.hasChildDataset(parentCatalogPath, "Feature Dataset",
              childCatalogPath)) {
              if (spatialReference != null) {
                final DEFeatureDataset dataset = EsriXmlRecordDefinitionUtil
                  .newDEFeatureDataset(childCatalogPath, spatialReference);
                final String datasetDefinition = EsriGdbXmlSerializer.toString(dataset);
                try {
                  this.geodatabase.createFeatureDataset(datasetDefinition);
                } catch (final Throwable t) {
                  Logs.debug(this, datasetDefinition);
                  throw Exceptions.wrap("Unable to create feature dataset " + childCatalogPath, t);
                }
              }
            }
            if (childSchema == null) {
              childSchema = new FileGdbRecordStoreSchema(schema, childSchemaPath);
              childSchema.setInitialized(true);
              schema.addElement(childSchema);
            }
            schema = childSchema;
            parentCatalogPath = childCatalogPath;
          }
        }
        return schema;
      }
    }
  }

  private FileGdbRecordDefinition newTableRecordDefinition(final DETable deTable) {
    String schemaCatalogPath = deTable.getParentCatalogPath();
    SpatialReference spatialReference;
    if (deTable instanceof DEFeatureClass) {
      final DEFeatureClass featureClass = (DEFeatureClass)deTable;
      spatialReference = featureClass.getSpatialReference();
    } else {
      spatialReference = null;
    }
    PathName schemaPath = toPath(schemaCatalogPath);
    FileGdbRecordStoreSchema schema = newSchema(schemaPath, spatialReference);

    if (schemaPath.equals(this.defaultSchemaPath)) {
      if (!(deTable instanceof DEFeatureClass)) {
        schemaCatalogPath = "\\";
        deTable.setCatalogPath("\\" + deTable.getName());
        schema = getRootSchema();
        schemaPath = schema.getPathName();
      }
    } else if (schemaPath.length() <= 1) {
      if (deTable instanceof DEFeatureClass) {
        schemaPath = this.defaultSchemaPath;
      }
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
      this.geodatabase.createTable(tableDefinition, schemaCatalogPath);

      final FileGdbRecordDefinition recordDefinition = new FileGdbRecordDefinition(this,
        PathName.newPathName(schemaPath), tableDefinition);
      initRecordDefinition(recordDefinition);
      schema.addElement(recordDefinition);
      return recordDefinition;
    } catch (final Throwable t) {
      throw new RuntimeException("Unable to create table " + deTable.getCatalogPath(), t);
    }
  }

  public BaseCloseable openTable(final PathName path) {
    final TableReference table = getTableReference(path);
    if (table == null) {
      return null;
    } else {
      return table.connect().wrap();
    }
  }

  @Override
  protected Map<PathName, ? extends RecordStoreSchemaElement> refreshSchemaElements(
    final RecordStoreSchema schema) {
    synchronized (schema) {
      final Map<PathName, RecordStoreSchemaElement> elementsByPath = new TreeMap<>();
      synchronized (this.geodatabase) {
        try (
          BaseCloseable geodatabaseConnection = this.geodatabase.connect()) {
          final PathName schemaPath = schema.getPathName();
          final FileGdbRecordStoreSchema fileGdbSchema = (FileGdbRecordStoreSchema)schema;
          final String schemaCatalogPath = fileGdbSchema.getCatalogPath();
          final VectorOfWString childDatasets = this.geodatabase.getChildDatasets(schemaCatalogPath,
            "Feature Dataset");
          if (childDatasets != null) {
            for (int i = 0; i < childDatasets.size(); i++) {
              final String childCatalogPath = childDatasets.get(i);
              final PathName childPath = toPath(childCatalogPath);
              FileGdbRecordStoreSchema childSchema = schema.getSchema(childPath);
              if (childSchema == null) {
                childSchema = new FileGdbRecordStoreSchema(fileGdbSchema, childPath);
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
            final FileGdbRecordStoreSchema childSchema = newSchema(this.defaultSchemaPath,
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
        }
      }

      return elementsByPath;
    }
  }

  private void refreshSchemaRecordDefinitions(
    final Map<PathName, RecordStoreSchemaElement> elementsByPath, final PathName schemaPath,
    final String catalogPath, final String datasetType) {
    final boolean pathExists = this.geodatabase.isPathExists(catalogPath);
    if (pathExists) {
      final VectorOfWString childFeatureClasses = this.geodatabase.getChildDatasets(catalogPath,
        datasetType);
      if (childFeatureClasses != null) {
        for (int i = 0; i < childFeatureClasses.size(); i++) {
          final String childCatalogPath = childFeatureClasses.get(i);
          final String tableDefinition = this.geodatabase.getTableDefinition(childCatalogPath);
          final FileGdbRecordDefinition recordDefinition = new FileGdbRecordDefinition(this,
            schemaPath, tableDefinition);
          initRecordDefinition(recordDefinition);
          final PathName childPath = recordDefinition.getPathName();
          elementsByPath.put(childPath, recordDefinition);
        }
      }
    }
  }

  public void setCreateAreaField(final boolean createAreaField) {
    this.createAreaField = createAreaField;
  }

  public void setCreateLengthField(final boolean createLengthField) {
    this.createLengthField = createLengthField;
  }

  public void setDefaultSchema(final PathName defaultSchema) {
    if (defaultSchema != null) {
      this.defaultSchemaPath = defaultSchema;
    } else {
      this.defaultSchemaPath = PathName.ROOT;
    }
  }

  public void setDefaultSchema(final String defaultSchema) {
    setDefaultSchema(PathName.newPathName(defaultSchema));
  }

  public void setDomainFieldNames(final Map<String, List<String>> domainFieldNames) {
    this.domainFieldNames = domainFieldNames;
  }

  public void setFileName(final String fileName) {
    this.fileName = fileName;
  }

  protected PathName toPath(final String catalogPath) {
    return PathName.newPathName(catalogPath);
  }

  @Override
  public String toString() {
    return this.fileName;
  }

  void unlockTable(final Table table) {
    synchronized (this.geodatabase) {
      table.freeWriteLock();
    }
  }

  @Override
  public void updateRecord(final Record record) {
    try (
      TableWrapper tableWrapper = getTableWrapper(record)) {
      if (tableWrapper != null) {
        tableWrapper.updateRecord(record);
      }
    }
  }

  public BaseCloseable writeLock(final PathName path) {
    final TableReference table = getTableReference(path);
    if (table == null) {
      return null;
    } else {
      return table.writeLock(false);
    }
  }

}
