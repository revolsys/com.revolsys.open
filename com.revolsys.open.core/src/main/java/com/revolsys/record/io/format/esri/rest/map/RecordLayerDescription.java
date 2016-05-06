package com.revolsys.record.io.format.esri.rest.map;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.datatype.DataType;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.io.PathName;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.logging.Logs;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.code.SimpleCodeTable;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.record.io.format.esri.gdb.xml.model.enums.GeometryType;
import com.revolsys.record.io.format.esri.rest.ArcGisRestCatalog;
import com.revolsys.record.io.format.esri.rest.ArcGisRestServerFeatureIterator;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.query.Condition;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;
import com.revolsys.util.UrlUtil;

public class RecordLayerDescription extends LayerDescription
  implements RecordDefinitionProxy, GeometryFactoryProxy {
  public static RecordLayerDescription getRecordLayerDescription(final String serverUrl,
    final PathName pathName) {
    final ArcGisRestCatalog catalog = new ArcGisRestCatalog(serverUrl);
    return catalog.getCatalogElement(pathName, RecordLayerDescription.class);
  }

  public static RecordLayerDescription getRecordLayerDescription(final String serverUrl,
    final String path) {
    final PathName pathName = PathName.newPathName(path);
    return getRecordLayerDescription(serverUrl, pathName);
  }

  private RecordDefinition recordDefinition;

  private BoundingBox boundingBox;

  public RecordLayerDescription() {
  }

  public RecordLayerDescription(final ArcGisRestMapServer mapServer, final Integer id,
    final String name) {
    super(mapServer, id, name);
  }

  private void addDefaultRecordQueryParameters(final Map<String, Object> parameters) {
    parameters.put("returnZ", "true");
    parameters.put("outFields", "*");
  }

  private void addField(final RecordDefinitionImpl recordDefinition, final String geometryType,
    final MapEx field) {
    final String fieldName = field.getString("name");
    final String fieldTitle = field.getString("string");
    final String fieldType = field.getString("type");
    final FieldType esriFieldType = FieldType.valueOf(fieldType);
    final DataType dataType;
    if (esriFieldType == FieldType.esriFieldTypeGeometry) {
      final DataType geometryDataType = getGeometryDataType(recordDefinition, geometryType);

      if (geometryDataType == null) {
        throw new IllegalArgumentException("No geometryType specified for " + getResourceUrl());
      }
      dataType = geometryDataType;
    } else {
      dataType = esriFieldType.getDataType();
    }
    if (dataType == null) {
      throw new IllegalArgumentException(
        "Unsupported field=" + fieldName + " type=" + dataType + " for " + getResourceUrl());
    }
    final int length = field.getInteger("length", 0);
    final FieldDefinition fieldDefinition = recordDefinition.addField(fieldName, dataType, length,
      false);
    fieldDefinition.setTitle(fieldTitle);
    setCodeTable(fieldDefinition, field);
  }

  public BoundingBox getBoundingBox() {
    refreshIfNeeded();
    return this.boundingBox;
  }

  private DataType getGeometryDataType(final RecordDefinitionImpl recordDefinition,
    final String geometryType) {
    DataType geometryDataType = null;
    if (Property.hasValue(geometryType)) {
      final GeometryType esriGeometryType = GeometryType.valueOf(geometryType);
      geometryDataType = esriGeometryType.getDataType();
      if (geometryDataType == null) {
        throw new IllegalArgumentException(
          "Unsupported geometryType=" + geometryType + " for " + getResourceUrl());
      }
    }
    return geometryDataType;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    if (this.recordDefinition == null) {
      return GeometryFactoryProxy.super.getGeometryFactory();
    } else {
      return this.recordDefinition.getGeometryFactory();
    }
  }

  public int getRecordCount(final BoundingBox boundingBox) {
    final Map<String, Object> parameters = newQueryParameters(boundingBox);
    if (parameters != null) {
      parameters.put("returnCountOnly", "true");
      final String resourceUrl = getResourceUrl() + "/query";
      final String queryUrl = UrlUtil.getUrl(resourceUrl, parameters);
      final Resource resource = Resource.getResource(queryUrl);
      try {
        final MapEx response = Json.toMap(resource);
        return response.getInteger("count", 0);

      } catch (final Throwable e) {
        Logs.debug(this, "Unable to get count for: " + boundingBox + "\n" + queryUrl);
      }
    }
    return 0;
  }

  public int getRecordCount(final Query query) {
    final Map<String, Object> parameters = newQueryParameters(query);
    parameters.put("returnCountOnly", "true");
    if (query != null) {
      // OFFSET & LIMIT
      final int offset = query.getOffset();
      parameters.put("resultOffset", offset);
      final int limit = query.getLimit();
      if (limit != Integer.MAX_VALUE) {
        parameters.put("resultRecordCount", limit);
      }
    }
    final String resourceUrl = getResourceUrl() + "/query";
    final String queryUrl = UrlUtil.getUrl(resourceUrl, parameters);
    final Resource resource = Resource.getResource(queryUrl);
    try {
      final MapEx response = Json.toMap(resource);
      return response.getInteger("count", 0);

    } catch (final Throwable e) {
      Logs.debug(this, "Unable to get count for: " + query + "\n" + queryUrl);
    }
    return 0;
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    refreshIfNeeded();
    return this.recordDefinition;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public <V extends Record> List<V> getRecords(final RecordFactory<V> recordFactory,
    final BoundingBox boundingBox) {
    final Map<String, Object> parameters = newQueryParameters(boundingBox);
    if (parameters != null) {
      final String queryUrl = getResourceUrl() + "/query";
      try (
        RecordReader reader = new ArcGisRestServerFeatureIterator(this, queryUrl, parameters, 0,
          Integer.MAX_VALUE, recordFactory)) {
        return (List)reader.toList();
      }
    }
    return Collections.emptyList();
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public <V extends Record> List<V> getRecords(final RecordFactory<V> recordFactory,
    final Query query) {
    try (
      RecordReader reader = newRecordReader(recordFactory, query)) {
      return (List)reader.toList();
    }
  }

  @Override
  protected void initialize(final MapEx properties) {
    this.boundingBox = newBoundingBox(properties, "extent");
    final PathName pathName = getPathName();
    final List<MapEx> fields = properties.getValue("fields");
    if (fields != null) {
      final RecordDefinitionImpl newRecordDefinition = new RecordDefinitionImpl(pathName);
      final String description = properties.getString("description");
      newRecordDefinition.setDescription(description);

      final String geometryType = properties.getString("geometryType");

      for (final MapEx field : fields) {
        addField(newRecordDefinition, geometryType, field);
      }

      if (this.boundingBox != null) {
        final GeometryFactory geometryFactory = this.boundingBox.getGeometryFactory();
        newRecordDefinition.setGeometryFactory(geometryFactory);
      }
      final FieldDefinition objectIdField = newRecordDefinition.getField("OBJECTID");
      if (newRecordDefinition.getIdField() == null && objectIdField != null) {
        final int fieldIndex = objectIdField.getIndex();
        newRecordDefinition.setIdFieldIndex(fieldIndex);
        objectIdField.setRequired(true);
      }
      this.recordDefinition = newRecordDefinition;
    }
    super.initialize(properties);
  }

  public Map<String, Object> newQueryParameters(BoundingBox boundingBox) {
    refreshIfNeeded();
    boundingBox = convertBoundingBox(boundingBox);
    if (Property.hasValue(boundingBox)) {
      final Map<String, Object> parameters = new LinkedHashMap<>();
      parameters.put("f", "json");
      parameters.put("geometryType", "esriGeometryEnvelope");
      final double minX = boundingBox.getMinX();
      final double minY = boundingBox.getMinY();
      final double maxX = boundingBox.getMaxX();
      final double maxY = boundingBox.getMaxY();
      final String boundingBoxText = minX + "," + minY + "," + maxX + "," + maxY;
      parameters.put("geometry", boundingBoxText);
      addDefaultRecordQueryParameters(parameters);
      return parameters;
    } else {
      return null;
    }
  }

  public Map<String, Object> newQueryParameters(final Query query) {
    final Map<String, Object> parameters = new LinkedHashMap<>();
    parameters.put("f", "json");
    parameters.put("where", "1=1");
    if (query != null) {
      // WHERE
      final Condition whereCondition = query.getWhereCondition();
      if (whereCondition != Condition.ALL) {
        final String where = whereCondition.toString();
        parameters.put("where", where);
      }

      // ORDER BY
      final Map<String, Boolean> orderBy = query.getOrderBy();
      if (Property.hasValue(orderBy)) {
        final String orderByFields = JdbcUtils.appendOrderByFields(new StringBuilder(), orderBy)
          .toString();
        parameters.put("orderByFields", orderByFields);
      }
    }
    return parameters;
  }

  public <V extends Record> RecordReader newRecordReader(final RecordFactory<V> recordFactory,
    final Query query) {
    refreshIfNeeded();
    final String queryUrl = getResourceUrl() + "/query";
    final Map<String, Object> parameters = newQueryParameters(query);
    addDefaultRecordQueryParameters(parameters);
    int offset = 0;
    int limit = Integer.MAX_VALUE;
    if (query != null) {
      offset = query.getOffset();
      limit = query.getLimit();
    }
    return new ArcGisRestServerFeatureIterator(this, queryUrl, parameters, offset, limit,
      recordFactory);
  }

  @SuppressWarnings("unchecked")
  private void setCodeTable(final FieldDefinition fieldDefinition,
    final Map<String, Object> field) {
    final Map<String, Object> domain = (Map<String, Object>)field.get("domain");
    if (domain != null) {
      final String domainType = (String)domain.get("type");
      final String domainName = (String)field.get("name");
      final List<Map<String, String>> codedValues = (List<Map<String, String>>)field
        .get("codedValues");
      if ("codedValue".equals(domainType) && Property.hasValuesAll(domainName, codedValues)) {
        final SimpleCodeTable codeTable = new SimpleCodeTable(domainName);
        for (final Map<String, String> codedValue : codedValues) {
          final String code = codedValue.get("code");
          final String description = codedValue.get("name");
          codeTable.addValue(code, description);
        }
        fieldDefinition.setCodeTable(codeTable);
      }
    }
  }
}