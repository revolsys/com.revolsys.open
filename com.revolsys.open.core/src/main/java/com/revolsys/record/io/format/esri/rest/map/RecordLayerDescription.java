package com.revolsys.record.io.format.esri.rest.map;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
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

  private void addField(final RecordDefinitionImpl recordDefinition,
    final Map<String, Object> field) {
    final String fieldName = (String)field.get("name");
    final String fieldTitle = (String)field.get("string");
    final String fieldType = (String)field.get("type");
    final FieldType esriFieldType = FieldType.valueOf(fieldType);
    final DataType dataType;
    if (esriFieldType == FieldType.esriFieldTypeGeometry) {
      final DataType geometryDataType = getGeometryDataType(recordDefinition);

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
    final int length = Maps.getInteger(field, "length", 0);
    final FieldDefinition fieldDefinition = recordDefinition.addField(fieldName, dataType, length,
      false);
    fieldDefinition.setTitle(fieldTitle);
    setCodeTable(fieldDefinition, field);
  }

  public BoundingBox getBoundingBox() {
    if (this.boundingBox == null) {
      this.boundingBox = getBoundingBox("extent");
    }
    return this.boundingBox;
  }

  private DataType getGeometryDataType(final RecordDefinitionImpl recordDefinition) {
    final String geometryType = getValue("geometryType");
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

  public int getRecordCount(final Query query) {
    final Map<String, Object> parameters = newQueryParameters(query);
    parameters.put("returnCountOnly", "true");

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
    if (this.recordDefinition == null) {
      final PathName pathName = getPathName();
      final List<Map<String, Object>> fields = getValue("fields");
      if (fields != null) {
        final RecordDefinitionImpl newRecordDefinition = new RecordDefinitionImpl(pathName);
        final String description = getValue("description");
        newRecordDefinition.setDescription(description);

        for (final Map<String, Object> field : fields) {
          addField(newRecordDefinition, field);
        }

        final BoundingBox boundingBox = getBoundingBox();
        if (boundingBox != null) {
          final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
          newRecordDefinition.setGeometryFactory(geometryFactory);
        }
        this.recordDefinition = newRecordDefinition;
      }
    }
    return this.recordDefinition;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public <V extends Record> List<V> getRecords(final RecordFactory<V> recordFactory,
    BoundingBox boundingBox) {
    boundingBox = convertBoundingBox(boundingBox);
    if (Property.hasValue(boundingBox)) {
      final Map<String, Object> parameters = new LinkedHashMap<>();
      parameters.put("f", "json");
      parameters.put("geometryType", "esriGeometryEnvelope");
      final String boundingBoxText = boundingBox.getMinX() + "," + boundingBox.getMinY() + ","
        + boundingBox.getMaxX() + "," + boundingBox.getMaxY();
      parameters.put("geometry", boundingBoxText);
      addDefaultRecordQueryParameters(parameters);
      final String resourceUrl = getResourceUrl() + "/query";
      final String queryUrl = UrlUtil.getUrl(resourceUrl, parameters);
      final Resource resource = Resource.getResource(queryUrl);
      final RecordDefinition recordDefinition = getRecordDefinition();
      try (
        RecordReader reader = new ArcGisRestServerFeatureIterator(recordDefinition, resource,
          recordFactory)) {
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
    final Map<String, Object> parameters = newQueryParameters(query);
    addDefaultRecordQueryParameters(parameters);
    final String resourceUrl = getResourceUrl() + "/query";
    final String queryUrl = UrlUtil.getUrl(resourceUrl, parameters);
    final Resource resource = Resource.getResource(queryUrl);
    final RecordDefinition recordDefinition = getRecordDefinition();
    try (
      RecordReader reader = new ArcGisRestServerFeatureIterator(recordDefinition, resource,
        recordFactory)) {
      return (List)reader.toList();
    }
  }

  public Map<String, Object> newQueryParameters(final Query query) {
    final Map<String, Object> parameters = new LinkedHashMap<>();
    parameters.put("f", "json");

    // WHERE
    final Condition whereCondition = query.getWhereCondition();
    if (whereCondition == Condition.ALL) {
      parameters.put("where", "1=1");
    } else {
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

    // OFFSET & LIMIT
    final int offset = query.getOffset();
    parameters.put("resultOffset", offset);
    final int limit = query.getLimit();
    if (limit != Integer.MAX_VALUE) {
      parameters.put("resultRecordCount", limit);
    }
    return parameters;
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
