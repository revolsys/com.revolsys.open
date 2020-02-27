package com.revolsys.gdal.record;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Driver;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.GeomFieldDefn;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.gdal.ogr.ogrConstants;
import org.gdal.osr.SpatialReference;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.date.Dates;
import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.gdal.Gdal;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.query.AbstractMultiCondition;
import com.revolsys.record.query.BinaryCondition;
import com.revolsys.record.query.CollectionValue;
import com.revolsys.record.query.Column;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.ILike;
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
import com.revolsys.util.Property;
import com.revolsys.util.StringBuilders;

public class OgrRecordStore extends AbstractRecordStore {

  public static final String GEO_PAKCAGE = "GPKG";

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\?");

  public static final String ROWID = "ROWID";

  public static final String SQLITE = "SQLite";

  private DataSource dataSource;

  private String driverName;

  private final File file;

  private final Map<String, String> idFieldNames = new HashMap<>();

  private final Map<String, PathName> layerNameToPathMap = new HashMap<>();

  private final Set<Layer> layersToClose = new HashSet<>();

  private final Map<PathName, String> pathToLayerNameMap = new HashMap<>();

  protected OgrRecordStore(final String driverName, final File file) {
    this.driverName = driverName;
    this.file = file;
    setCreateMissingTables(true);
  }

  synchronized void addLayerToClose(final Layer layer) {
    this.layersToClose.add(layer);
  }

  @Override
  public void appendQueryValue(final Query query, final StringBuilder sql,
    final QueryValue condition) {
    if (condition instanceof Like || condition instanceof ILike) {
      final BinaryCondition like = (BinaryCondition)condition;
      final QueryValue left = like.getLeft();
      final QueryValue right = like.getRight();
      sql.append("UPPER(");
      appendQueryValue(query, sql, left);
      sql.append(") LIKE ");
      if (right instanceof Value) {
        final Value valueCondition = (Value)right;
        final Object value = valueCondition.getValue();
        sql.append("'");
        if (value != null) {
          final String string = DataTypes.toString(value);
          sql.append(string.toUpperCase());
        }
        sql.append("'");
      } else {
        appendQueryValue(query, sql, right);
      }
    } else if (condition instanceof LeftUnaryCondition) {
      final LeftUnaryCondition unaryCondition = (LeftUnaryCondition)condition;
      final String operator = unaryCondition.getOperator();
      final QueryValue right = unaryCondition.getValue();
      sql.append(operator);
      sql.append(" ");
      appendQueryValue(query, sql, right);
    } else if (condition instanceof RightUnaryCondition) {
      final RightUnaryCondition unaryCondition = (RightUnaryCondition)condition;
      final QueryValue left = unaryCondition.getValue();
      final String operator = unaryCondition.getOperator();
      appendQueryValue(query, sql, left);
      sql.append(" ");
      sql.append(operator);
    } else if (condition instanceof BinaryCondition) {
      final BinaryCondition binaryCondition = (BinaryCondition)condition;
      final QueryValue left = binaryCondition.getLeft();
      final String operator = binaryCondition.getOperator();
      final QueryValue right = binaryCondition.getRight();
      appendQueryValue(query, sql, left);
      sql.append(" ");
      sql.append(operator);
      sql.append(" ");
      appendQueryValue(query, sql, right);
    } else if (condition instanceof AbstractMultiCondition) {
      final AbstractMultiCondition multipleCondition = (AbstractMultiCondition)condition;
      sql.append("(");
      boolean first = true;
      final String operator = multipleCondition.getOperator();
      for (final QueryValue subCondition : multipleCondition.getQueryValues()) {
        if (first) {
          first = false;
        } else {
          sql.append(" ");
          sql.append(operator);
          sql.append(" ");
        }
        appendQueryValue(query, sql, subCondition);
      }
      sql.append(")");
    } else if (condition instanceof Value) {
      final Value valueCondition = (Value)condition;
      final Object value = valueCondition.getValue();
      appendValue(sql, value);
    } else if (condition instanceof CollectionValue) {
      final CollectionValue collectionValue = (CollectionValue)condition;
      final List<Object> values = collectionValue.getValues();
      boolean first = true;
      for (final Object value : values) {
        if (first) {
          first = false;
        } else {
          sql.append(", ");
        }
        appendValue(sql, value);
      }
    } else if (condition instanceof Column) {
      final Column column = (Column)condition;
      final Object name = column.getName();
      sql.append(name);
    } else if (condition instanceof SqlCondition) {
      final SqlCondition sqlCondition = (SqlCondition)condition;
      final String where = sqlCondition.getSql();
      final List<Object> parameters = sqlCondition.getParameterValues();
      if (parameters.isEmpty()) {
        if (where.indexOf('?') > -1) {
          throw new IllegalArgumentException(
            "No arguments specified for a where clause with placeholders: " + where);
        } else {
          sql.append(where);
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
          sql.append(replacement);
          appendValue(sql, argument);
          i++;
        }
        final StringBuffer tail = new StringBuffer();
        matcher.appendTail(tail);
        sql.append(tail);
      }
    } else if (condition instanceof EnvelopeIntersects) {
      final EnvelopeIntersects envelopeIntersects = (EnvelopeIntersects)condition;
      final QueryValue boundingBox1Value = envelopeIntersects.getBoundingBox1Value();
      final QueryValue boundingBox2Value = envelopeIntersects.getBoundingBox2Value();
      if (boundingBox1Value == null || boundingBox2Value == null) {
        sql.append("1 = 0");
      } else {
        sql.append("Intersects(");
        boundingBox1Value.appendSql(query, this, sql);
        sql.append(",");
        boundingBox2Value.appendSql(query, this, sql);
        sql.append(")");
      }
    } else if (condition instanceof WithinDistance) {
      final WithinDistance withinDistance = (WithinDistance)condition;
      final QueryValue geometry1Value = withinDistance.getGeometry1Value();
      final QueryValue geometry2Value = withinDistance.getGeometry2Value();
      final QueryValue distanceValue = withinDistance.getDistanceValue();
      if (geometry1Value == null || geometry2Value == null || distanceValue == null) {
        sql.append("1 = 0");
      } else {
        sql.append("Distance(");
        geometry1Value.appendSql(query, this, sql);
        sql.append(", ");
        geometry2Value.appendSql(query, this, sql);
        sql.append(") <= ");
        distanceValue.appendSql(query, this, sql);
        sql.append(")");
      }
    } else {
      condition.appendDefaultSql(query, this, sql);
    }
  }

  public void appendValue(final StringBuilder sql, final Object value) {
    if (value == null) {
      sql.append("''");
    } else if (value instanceof Number) {
      sql.append(value);
    } else if (value instanceof java.sql.Date) {
      final String stringValue = Dates.format("yyyy-MM-dd", (java.util.Date)value);
      sql.append("CAST('" + stringValue + "' AS DATE)");
    } else if (value instanceof java.util.Date) {
      final String stringValue = Dates.format("yyyy-MM-dd", (java.util.Date)value);
      sql.append("CAST('" + stringValue + "' AS TIMESTAMP)");
    } else if (value instanceof BoundingBox) {
      final BoundingBox boundingBox = (BoundingBox)value;
      sql.append("BuildMbr(");
      sql.append(boundingBox.getMinX());
      sql.append(",");
      sql.append(boundingBox.getMinY());
      sql.append(",");
      sql.append(boundingBox.getMaxX());
      sql.append(",");
      sql.append(boundingBox.getMaxY());
      sql.append(")");
    } else {
      final String stringValue = DataTypes.toString(value);
      sql.append("'");
      sql.append(stringValue.replaceAll("'", "''"));
      sql.append("'");
    }
  }

  @Override
  @PreDestroy
  public void close() {
    if (!OgrRecordStoreFactory.release(this.file)) {
      closeDo();
    }
  }

  public void closeDo() {
    synchronized (this) {
      if (!isClosed()) {
        if (this.dataSource != null) {
          try {
            for (final Layer layer : this.layersToClose) {
              this.dataSource.ReleaseResultSet(layer);
            }
            this.layersToClose.clear();
            this.dataSource.delete();
          } finally {
            this.dataSource = null;
            super.close();
          }
        }
      }
    }
  }

  protected DataSource getDataSource() {
    if (isClosed()) {
      return null;
    } else {
      if (this.dataSource == null) {
        this.dataSource = newDataSource(false);
        this.driverName = this.dataSource.GetDriver().getName();
      }
      return this.dataSource;
    }
  }

  public String getDriverName() {
    return this.driverName;
  }

  private int getGeometryFieldType(final GeometryFactory geometryFactory,
    final FieldDefinition field) {
    int type;
    final DataType dataType = field.getDataType();

    if (GeometryDataTypes.POINT.equals(dataType)) {
      type = 1;
    } else if (GeometryDataTypes.LINE_STRING.equals(dataType)) {
      type = 2;
    } else if (GeometryDataTypes.POLYGON.equals(dataType)) {
      type = 3;
    } else if (GeometryDataTypes.MULTI_POINT.equals(dataType)) {
      type = 4;
    } else if (GeometryDataTypes.MULTI_LINE_STRING.equals(dataType)) {
      type = 5;
    } else if (GeometryDataTypes.MULTI_POINT.equals(dataType)) {
      type = 6;
    } else if (GeometryDataTypes.GEOMETRY_COLLECTION.equals(dataType)) {
      type = 7;
    } else if (GeometryDataTypes.LINEAR_RING.equals(dataType)) {
      type = 101;
    } else {
      throw new IllegalArgumentException("Unsupported geometry type " + dataType + " for " + field);
    }

    if (geometryFactory.getAxisCount() > 2) {
      type += 0x80000000;
    }
    return type;
  }

  public String getIdFieldName(final RecordDefinition recordDefinition) {
    String path;
    if (recordDefinition == null) {
      path = null;
    } else {
      path = recordDefinition.getPath();
    }

    return getIdFieldName(path);
  }

  public String getIdFieldName(final String typePath) {
    if (typePath != null) {
      final String idFieldName = this.idFieldNames.get(typePath.toUpperCase());
      if (idFieldName != null) {
        return idFieldName;
      }
    }
    return ROWID;
  }

  protected Layer getLayer(final String typePath) {
    final DataSource dataSource = getDataSource();
    if (dataSource == null) {
      return null;
    } else {
      final String layerName = getLayerName(typePath);
      if (layerName == null) {
        return null;
      } else {
        return dataSource.GetLayer(layerName);
      }
    }
  }

  protected String getLayerName(final String typePath) {
    if (typePath == null) {
      return null;
    } else {
      final String layerName = this.pathToLayerNameMap.get(typePath.toUpperCase());
      if (layerName == null) {
        return typePath;
      } else {
        return layerName;
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

      final StringBuilder sql = new StringBuilder();
      sql.append("SELECT COUNT(*) FROM ");
      final String layerName = getLayerName(typePath);
      sql.append(layerName);
      if (whereClause.length() > 0) {
        sql.append(" WHERE ");
        sql.append(whereClause);
      }
      final DataSource dataSource = getDataSource();
      if (dataSource != null) {
        final Layer result = dataSource.ExecuteSQL(sql.toString());
        if (result != null) {

          addLayerToClose(result);
          try {
            final Feature feature = result.GetNextFeature();
            if (feature != null) {
              try {
                return feature.GetFieldAsInteger(0);
              } finally {
                feature.delete();
              }
            }
          } finally {
            releaseLayerToClose(result);
          }
        }
      }
    }
    return 0;
  }

  @Override
  public RecordDefinition getRecordDefinition(final RecordDefinition sourceRecordDefinition) {
    final DataSource dataSource = getDataSource();
    synchronized (dataSource) {
      if (getGeometryFactory() == null) {
        setGeometryFactory(sourceRecordDefinition.getGeometryFactory());
      }
      RecordDefinition recordDefinition = super.getRecordDefinition(sourceRecordDefinition);
      if (isCreateMissingTables() && recordDefinition == null) {
        recordDefinition = newLayerRecordDefinition(dataSource, sourceRecordDefinition);
      }
      return recordDefinition;
    }
  }

  @Override
  public String getRecordStoreType() {
    return this.driverName;
  }

  protected String getSql(final Query query) {
    final RecordDefinition recordDefinition = query.getRecordDefinition();
    final String typePath = recordDefinition.getPath();
    final Map<? extends CharSequence, Boolean> orderBy = query.getOrderBy();
    final StringBuilder sql = new StringBuilder();
    sql.append("SELECT ");

    List<String> fieldNames = query.getFieldNames();
    if (fieldNames.isEmpty()) {
      fieldNames = recordDefinition.getFieldNames();
    }
    fieldNames.remove("ROWID");
    StringBuilders.append(sql, fieldNames);
    sql.append(" FROM ");
    final String layerName = getLayerName(typePath);
    sql.append(layerName);
    final StringBuilder whereClause = getWhereClause(query);
    if (whereClause.length() > 0) {
      sql.append(" WHERE ");
      sql.append(whereClause);
    }
    boolean first = true;
    for (final Entry<? extends CharSequence, Boolean> entry : orderBy.entrySet()) {
      final CharSequence fieldName = entry.getKey();
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
    }
    return sql.toString();
  }

  protected StringBuilder getWhereClause(final Query query) {
    final StringBuilder whereClause = new StringBuilder();
    final Condition whereCondition = query.getWhereCondition();
    if (!whereCondition.isEmpty()) {
      appendQueryValue(query, whereClause, whereCondition);
    }
    return whereClause;
  }

  protected DataSource newDataSource(final boolean update) {
    final String path = FileUtil.getCanonicalPath(this.file);
    DataSource dataSource;
    if (this.file.exists()) {
      dataSource = ogr.Open(path, update);
    } else {
      final Driver driver = ogr.GetDriverByName(this.driverName);
      dataSource = driver.CreateDataSource(path);
    }
    return dataSource;
  }

  @Override
  public AbstractIterator<Record> newIterator(final Query query,
    final Map<String, Object> properties) {
    String typePath = query.getTypeName();
    RecordDefinition recordDefinition = query.getRecordDefinition();
    if (recordDefinition == null) {
      typePath = query.getTypeName();
      recordDefinition = getRecordDefinition(typePath);
      if (recordDefinition == null) {
        throw new IllegalArgumentException("Type name does not exist " + typePath);
      } else {
        query.setRecordDefinition(recordDefinition);
      }
    } else {
      typePath = recordDefinition.getPath();
    }

    final OgrQueryIterator iterator = new OgrQueryIterator(this, query);
    return iterator;
  }

  private RecordDefinition newLayerRecordDefinition(final DataSource dataSource,
    final RecordDefinition sourceRecordDefinition) {
    final PathName typePath = sourceRecordDefinition.getPathName();
    final String name = typePath.getName();
    final PathName parentPath = typePath.getParent();
    final RecordStoreSchema schema = getSchema(parentPath);
    Layer layer;
    if (sourceRecordDefinition.hasGeometryField()) {
      final GeometryFactory geometryFactory = sourceRecordDefinition.getGeometryFactory();
      final FieldDefinition geometryField = sourceRecordDefinition.getGeometryField();
      final int geometryFieldType = getGeometryFieldType(geometryFactory, geometryField);
      final SpatialReference spatialReference = Gdal.getSpatialReference(geometryFactory);
      layer = dataSource.CreateLayer(typePath.getPath(), spatialReference, geometryFieldType);
    } else {
      layer = dataSource.CreateLayer(name);
    }
    if (dataSource.TestCapability(ogrConstants.ODsCCreateLayer) == false) {
      System.err.println("CreateLayer not supported by driver.");
    }
    return newLayerRecordDefinition(schema, layer);
  }

  protected RecordDefinitionImpl newLayerRecordDefinition(final RecordStoreSchema schema,
    final Layer layer) {
    final String layerName = layer.GetName();
    final PathName typePath = PathName.newPathName(layerName);

    /** This primes the layer so that the fidColumn is loaded correctly. */
    layer.GetNextFeature();

    final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(schema, typePath);
    String idFieldName = layer.GetFIDColumn();
    if (!Property.hasValue(idFieldName)) {
      idFieldName = "rowid";
    }
    this.idFieldNames.put(typePath.getUpperPath(), idFieldName);
    final FeatureDefn layerDefinition = layer.GetLayerDefn();
    if (SQLITE.equals(this.driverName) || GEO_PAKCAGE.equals(this.driverName)) {
      recordDefinition.addField(idFieldName, DataTypes.LONG, true);
      recordDefinition.setIdFieldName(idFieldName);
    }
    for (int fieldIndex = 0; fieldIndex < layerDefinition.GetFieldCount(); fieldIndex++) {
      final FieldDefn fieldDefinition = layerDefinition.GetFieldDefn(fieldIndex);
      final String fieldName = fieldDefinition.GetName();
      final int fieldType = fieldDefinition.GetFieldType();
      final int fieldWidth = fieldDefinition.GetWidth();
      final int fieldPrecision = fieldDefinition.GetPrecision();
      DataType fieldDataType;
      switch (fieldType) {
        case 0:
          fieldDataType = DataTypes.INT;
        break;
        case 2:
          fieldDataType = DataTypes.DOUBLE;
        break;
        case 4:
        case 6:
          fieldDataType = DataTypes.STRING;
        break;
        case 9:
          fieldDataType = DataTypes.DATE;
        break;
        case 11:
          fieldDataType = DataTypes.DATE_TIME;
        break;

        default:
          fieldDataType = DataTypes.STRING;
          final String fieldTypeName = fieldDefinition.GetFieldTypeName(fieldType);
          Logs.error(this,
            "Unsupported field type " + this.file + " " + fieldName + ": " + fieldTypeName);
        break;
      }
      final FieldDefinition field = new FieldDefinition(fieldName, fieldDataType, fieldWidth,
        fieldPrecision, false);
      recordDefinition.addField(field);
    }
    for (int fieldIndex = 0; fieldIndex < layerDefinition.GetGeomFieldCount(); fieldIndex++) {
      final GeomFieldDefn fieldDefinition = layerDefinition.GetGeomFieldDefn(fieldIndex);
      final String fieldName = fieldDefinition.GetName();
      final int geometryFieldType = fieldDefinition.GetFieldType();
      DataType geometryFieldDataType;
      int axisCount = 2;
      switch (geometryFieldType) {
        case 1:
          geometryFieldDataType = GeometryDataTypes.POINT;
        break;
        case 2:
          geometryFieldDataType = GeometryDataTypes.LINE_STRING;
        break;
        case 3:
          geometryFieldDataType = GeometryDataTypes.POLYGON;
        break;
        case 4:
          geometryFieldDataType = GeometryDataTypes.MULTI_POINT;
        break;
        case 5:
          geometryFieldDataType = GeometryDataTypes.MULTI_LINE_STRING;
        break;
        case 6:
          geometryFieldDataType = GeometryDataTypes.MULTI_POLYGON;
        break;
        case 7:
          geometryFieldDataType = GeometryDataTypes.GEOMETRY_COLLECTION;
        break;
        case 101:
          geometryFieldDataType = GeometryDataTypes.LINEAR_RING;
        break;
        case 0x80000000 + 1:
          geometryFieldDataType = GeometryDataTypes.POINT;
          axisCount = 3;
        break;
        case 0x80000000 + 2:
          geometryFieldDataType = GeometryDataTypes.LINE_STRING;
          axisCount = 3;
        break;
        case 0x80000000 + 3:
          geometryFieldDataType = GeometryDataTypes.POLYGON;
          axisCount = 3;
        break;
        case 0x80000000 + 4:
          geometryFieldDataType = GeometryDataTypes.MULTI_POINT;
          axisCount = 3;
        break;
        case 0x80000000 + 5:
          geometryFieldDataType = GeometryDataTypes.MULTI_LINE_STRING;
          axisCount = 3;
        break;
        case 0x80000000 + 6:
          geometryFieldDataType = GeometryDataTypes.MULTI_POLYGON;
          axisCount = 3;
        break;
        case 0x80000000 + 7:
          geometryFieldDataType = GeometryDataTypes.GEOMETRY_COLLECTION;
          axisCount = 3;
        break;

        default:
          geometryFieldDataType = GeometryDataTypes.GEOMETRY;
        break;
      }
      final SpatialReference spatialReference = fieldDefinition.GetSpatialRef();
      final GeometryFactory geometryFactory = Gdal.getGeometryFactory(spatialReference, axisCount);
      final FieldDefinition field = new FieldDefinition(fieldName, geometryFieldDataType, false);
      field.setGeometryFactory(geometryFactory);
      recordDefinition.addField(field);
    }
    return recordDefinition;
  }

  @Override
  public RecordWriter newRecordWriter(final boolean throwExceptions) {
    return new OgrRecordWriter(this);
  }

  @Override
  protected synchronized Map<PathName, ? extends RecordStoreSchemaElement> refreshSchemaElements(
    final RecordStoreSchema schema) {
    final Map<PathName, RecordStoreSchemaElement> elementsByPath = new TreeMap<>();
    if (!isClosed()) {
      final DataSource dataSource = getDataSource();
      if (dataSource != null) {
        for (int layerIndex = 0; layerIndex < dataSource.GetLayerCount(); layerIndex++) {
          final Layer layer = dataSource.GetLayer(layerIndex);
          if (layer != null) {
            try {
              final RecordDefinitionImpl recordDefinition = newLayerRecordDefinition(schema, layer);
              final PathName typePath = recordDefinition.getPathName();
              final String layerName = layer.GetName();
              this.layerNameToPathMap.put(layerName.toUpperCase(), typePath);
              this.pathToLayerNameMap.put(typePath, layerName);
              elementsByPath.put(typePath, recordDefinition);
            } finally {
              layer.delete();
            }
          }
        }
      }
    }
    return elementsByPath;
  }

  synchronized void releaseLayerToClose(final Layer layer) {
    if (layer != null) {
      try {
        if (this.dataSource != null) {
          this.dataSource.ReleaseResultSet(layer);
        }
      } catch (final Throwable e) {
        Logs.error(this, "Cannot close Table " + layer.GetName(), e);
      } finally {
        this.layersToClose.remove(layer);
        layer.delete();
      }
    }
  }

  @Override
  public String toString() {
    return this.file.toString();
  }
}
