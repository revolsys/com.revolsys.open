package com.revolsys.gdal.record;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.GeomFieldDefn;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.gdal.osr.SpatialReference;
import org.slf4j.LoggerFactory;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.query.AbstractMultiCondition;
import com.revolsys.data.query.BinaryCondition;
import com.revolsys.data.query.CollectionValue;
import com.revolsys.data.query.Column;
import com.revolsys.data.query.Condition;
import com.revolsys.data.query.ILike;
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
import com.revolsys.data.record.property.AttributeProperties;
import com.revolsys.data.record.schema.AbstractRecordStore;
import com.revolsys.data.record.schema.Attribute;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.data.record.schema.RecordStoreSchemaElement;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gdal.Gdal;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Path;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.DateUtil;

public class OgrRecordStore extends AbstractRecordStore {
  static {
    Gdal.init();
  }

  private final File file;

  private DataSource dataSource;

  private boolean closed;

  private final Map<String, String> layerNameToPathMap = new HashMap<>();

  private final Map<String, String> pathToLayerNameMap = new HashMap<>();

  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\?");

  protected OgrRecordStore(final File file) {
    this.file = file;
  }

  @Override
  public void appendQueryValue(final Query query, final StringBuilder buffer,
    final QueryValue condition) {
    if (condition instanceof Like || condition instanceof ILike) {
      final BinaryCondition like = (BinaryCondition)condition;
      final QueryValue left = like.getLeft();
      final QueryValue right = like.getRight();
      appendQueryValue(query, buffer, left);
      buffer.append(" ILIKE ");
      if (right instanceof Value) {
        final Value valueCondition = (Value)right;
        final Object value = valueCondition.getValue();
        buffer.append("'");
        if (value != null) {
          final String string = StringConverterRegistry.toString(value);
          buffer.append(string.toUpperCase());
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

  public void appendValue(final StringBuilder buffer, final Object value) {
    if (value == null) {
      buffer.append("''");
    } else if (value instanceof Number) {
      buffer.append(value);
    } else if (value instanceof java.sql.Date) {
      final String stringValue = DateUtil.format("yyyy-MM-dd",
        (java.util.Date)value);
      buffer.append("CAST('" + stringValue + "' AS DATE)");
    } else if (value instanceof java.util.Date) {
      final String stringValue = DateUtil.format("yyyy-MM-dd",
        (java.util.Date)value);
      buffer.append("CAST('" + stringValue + "' AS TIMESTAMP)");
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
    if (!OgrRecordStoreFactory.release(this.file)) {
      doClose();
    }
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

    final OgrQueryIterator iterator = new OgrQueryIterator(this, query);
    return iterator;
  }

  protected RecordDefinitionImpl createRecordDefinition(
    final RecordStoreSchema schema, final Layer layer) {
    final String layerName = layer.GetName();
    final String typePath = Path.clean(layerName);

    final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(
      schema, typePath);
    final FeatureDefn layerDefinition = layer.GetLayerDefn();

    for (int fieldIndex = 0; fieldIndex < layerDefinition.GetFieldCount(); fieldIndex++) {
      final FieldDefn fieldDefinition = layerDefinition.GetFieldDefn(fieldIndex);
      final String fieldName = fieldDefinition.GetName();
      final String fieldTypeName = fieldDefinition.GetFieldTypeName(fieldDefinition.GetFieldType());
      final int fieldWidth = fieldDefinition.GetWidth();
      final int fieldPrecision = fieldDefinition.GetPrecision();
      DataType fieldDataType = DataTypes.STRING;
      if ("String".equals(fieldTypeName)) {
        fieldDataType = DataTypes.STRING;
      } else if ("Integer".equals(fieldTypeName)) {
        fieldDataType = DataTypes.INT;
      } else if ("Real".equals(fieldTypeName)) {
        fieldDataType = DataTypes.DOUBLE;
      } else if ("Date".equals(fieldTypeName)) {
        fieldDataType = DataTypes.DATE;
      } else if ("DateTime".equals(fieldTypeName)) {
        fieldDataType = DataTypes.DATE_TIME;
      } else {
        LoggerFactory.getLogger(getClass()).error(
          "Unsupported field type " + this.file + " " + fieldName + ": "
              + fieldTypeName);
      }
      final Attribute field = new Attribute(fieldName, fieldDataType,
        fieldWidth, fieldPrecision, false);
      recordDefinition.addAttribute(field);
    }
    for (int fieldIndex = 0; fieldIndex < layerDefinition.GetGeomFieldCount(); fieldIndex++) {
      final GeomFieldDefn fieldDefinition = layerDefinition.GetGeomFieldDefn(fieldIndex);
      final String fieldName = fieldDefinition.GetName();
      final int fieldType = fieldDefinition.GetFieldType();
      DataType fieldDataType;
      int axisCount = 2;
      switch (fieldType) {
        case 1:
          fieldDataType = DataTypes.POINT;
          break;
        case 2:
          fieldDataType = DataTypes.LINE_STRING;
          break;
        case 3:
          fieldDataType = DataTypes.POLYGON;
          break;
        case 4:
          fieldDataType = DataTypes.MULTI_POINT;
          break;
        case 5:
          fieldDataType = DataTypes.MULTI_LINE_STRING;
          break;
        case 6:
          fieldDataType = DataTypes.MULTI_POLYGON;
          break;
        case 7:
          fieldDataType = DataTypes.GEOMETRY_COLLECTION;
          break;
        case 101:
          fieldDataType = DataTypes.LINEAR_RING;
          break;
        case 0x80000000 + 1:
          fieldDataType = DataTypes.POINT;
        axisCount = 3;
        break;
        case 0x80000000 + 2:
          fieldDataType = DataTypes.LINE_STRING;
        axisCount = 3;
        break;
        case 0x80000000 + 3:
          fieldDataType = DataTypes.POLYGON;
        axisCount = 3;
        break;
        case 0x80000000 + 4:
          fieldDataType = DataTypes.MULTI_POINT;
        axisCount = 3;
        break;
        case 0x80000000 + 5:
          fieldDataType = DataTypes.MULTI_LINE_STRING;
        axisCount = 3;
        break;
        case 0x80000000 + 6:
          fieldDataType = DataTypes.MULTI_POLYGON;
        axisCount = 3;
        break;
        case 0x80000000 + 7:
          fieldDataType = DataTypes.GEOMETRY_COLLECTION;
        axisCount = 3;
        break;

        default:
          fieldDataType = DataTypes.GEOMETRY;
          break;
      }
      final SpatialReference spatialReference = fieldDefinition.GetSpatialRef();
      final CoordinateSystem coordinateSystem = Gdal.getCoordinateSystem(spatialReference);
      final GeometryFactory geometryFactory = GeometryFactory.floating(
        coordinateSystem, axisCount);
      final Attribute field = new Attribute(fieldName, fieldDataType, false);
      field.setProperty(AttributeProperties.GEOMETRY_FACTORY, geometryFactory);
      recordDefinition.addAttribute(field);
    }
    return recordDefinition;
  }

  @Override
  public Writer<Record> createWriter() {
    // TODO Auto-generated method stub
    return null;
  }

  public void doClose() {
    synchronized (this) {

      if (!isClosed()) {
        if (this.dataSource != null) {
          try {
            this.dataSource.delete();
          } finally {
            this.dataSource = null;
            this.closed = true;
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
        this.dataSource = ogr.Open(FileUtil.getCanonicalPath(this.file), true);
      }
      return this.dataSource;
    }
  }

  protected String getLayerName(final String typePath) {
    return this.pathToLayerNameMap.get(typePath.toUpperCase());
  }

  @Override
  public int getRowCount(final Query query) {
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
      // final BoundingBox boundingBox = QueryValue.getBoundingBox(query);

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
          final Feature feature = result.GetNextFeature();
          if (feature != null) {
            try {
              return feature.GetFieldAsInteger(0);
            } finally {
              feature.delete();
            }
          }
        }
      }
    }
    return 0;
  }

  protected String getSql(final Query query) {
    final RecordDefinition recordDefinition = query.getRecordDefinition();
    final String typePath = recordDefinition.getPath();
    final BoundingBox boundingBox = QueryValue.getBoundingBox(query);
    final Map<String, Boolean> orderBy = query.getOrderBy();
    final StringBuilder sql = new StringBuilder();
    sql.append("SELECT ");

    final List<String> attributeNames = query.getAttributeNames();
    if (attributeNames.isEmpty()) {
      CollectionUtil.append(sql, recordDefinition.getAttributeNames());
    } else {
      CollectionUtil.append(sql, attributeNames);
    }
    sql.append(" FROM ");
    final String layerName = getLayerName(typePath);
    sql.append(layerName);
    final StringBuilder whereClause = getWhereClause(query);
    if (whereClause.length() > 0) {
      sql.append(" WHERE ");
      sql.append(whereClause);
    }
    boolean first = true;
    for (final Iterator<Entry<String, Boolean>> iterator = orderBy.entrySet()
        .iterator(); iterator.hasNext();) {
      final Entry<String, Boolean> entry = iterator.next();
      final String column = entry.getKey();
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
    }
    return sql.toString();
  }

  protected StringBuilder getWhereClause(final Query query) {
    final StringBuilder whereClause = new StringBuilder();
    final Condition whereCondition = query.getWhereCondition();
    if (whereCondition != null) {
      appendQueryValue(query, whereClause, whereCondition);
    }
    return whereClause;
  }

  public boolean isClosed() {
    return this.closed;
  }

  @Override
  protected Map<String, ? extends RecordStoreSchemaElement> refreshSchemaElements(
    final RecordStoreSchema schema) {
    final Map<String, RecordStoreSchemaElement> elementsByPath = new TreeMap<>();
    if (!isClosed()) {
      final DataSource dataSource = getDataSource();
      if (dataSource != null) {
        for (int layerIndex = 0; layerIndex < dataSource.GetLayerCount(); layerIndex++) {
          final Layer layer = dataSource.GetLayer(layerIndex);
          final RecordDefinitionImpl recordDefinition = createRecordDefinition(
            schema, layer);
          final String typePath = recordDefinition.getPath().toUpperCase();
          final String layerName = layer.GetName();
          this.layerNameToPathMap.put(layerName.toUpperCase(), typePath);
          this.pathToLayerNameMap.put(typePath, layerName);
          elementsByPath.put(typePath, recordDefinition);
        }
      }
    }
    return elementsByPath;
  }
}
