package com.revolsys.odata.model;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import org.apache.http.NameValuePair;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.http.ApacheHttpRequestBuilder;
import com.revolsys.http.ApacheHttpRequestBuilderFactory;
import com.revolsys.http.ConfigurableRequestBuilderFactory;
import com.revolsys.net.http.SimpleNameValuePair;
import com.revolsys.record.io.RecordIterator;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.AcceptAllCondition;
import com.revolsys.record.query.And;
import com.revolsys.record.query.Column;
import com.revolsys.record.query.ColumnAlias;
import com.revolsys.record.query.ColumnReference;
import com.revolsys.record.query.Equal;
import com.revolsys.record.query.GreaterThan;
import com.revolsys.record.query.GreaterThanEqual;
import com.revolsys.record.query.IsNotNull;
import com.revolsys.record.query.IsNull;
import com.revolsys.record.query.LessThan;
import com.revolsys.record.query.LessThanEqual;
import com.revolsys.record.query.NotEqual;
import com.revolsys.record.query.Or;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.Value;
import com.revolsys.record.query.functions.EnvelopeIntersects;
import com.revolsys.record.schema.AbstractRecordStore;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.record.schema.RecordStoreSchemaElement;
import com.revolsys.util.Debug;
import com.revolsys.util.UriBuilder;
import com.revolsys.util.UrlUtil;

public class ODataRecordStore extends AbstractRecordStore {
  public static final NameValuePair FORMAT_JSON = new SimpleNameValuePair("$format", "json");

  private static final Map<Class<? extends QueryValue>, BiFunction<StringBuilder, QueryValue, Boolean>> HANDLERS = new HashMap<>();

  static {
    addBinaryCondition(Equal.class, " eq ");
    addBinaryCondition(NotEqual.class, " ne ");
    addBinaryCondition(GreaterThan.class, " gt ");
    addBinaryCondition(GreaterThanEqual.class, " ge ");
    addBinaryCondition(LessThan.class, " lt ");
    addBinaryCondition(LessThanEqual.class, " le ");
    addBinaryCondition(And.class, " and ");
    addBinaryCondition(Or.class, " or ");
    addUnaryCondition(IsNull.class, " eq null");
    addUnaryCondition(IsNotNull.class, " ne null");
    HANDLERS.put(Value.class, ODataRecordStore::addValue);
    HANDLERS.put(Column.class, ODataRecordStore::addColumn);
    HANDLERS.put(ColumnAlias.class, ODataRecordStore::addColumn);
    HANDLERS.put(FieldDefinition.class, ODataRecordStore::addColumn);
    HANDLERS.put(AcceptAllCondition.class, (filter, value) -> false);
    HANDLERS.put(EnvelopeIntersects.class, (filter, value) -> {
      final EnvelopeIntersects i = (EnvelopeIntersects)value;
      final Value v = i.getRight();
      final BoundingBox boundingBox = (BoundingBox)v.getValue();
      final Polygon polygon = boundingBox
        .toPolygon(boundingBox.getGeometryFactory().convertAxisCount(2), 1, 1);

      filter.append("geo.intersects(");
      appendQueryValue(filter, i.getLeft());
      filter.append(", geometry'");
      filter.append(polygon.toEwkt());
      filter.append("')");
      return true;
    });
  }

  public static void addBinaryCondition(final Class<? extends QueryValue> clazz,
    final String operator) {
    HANDLERS.put(clazz, (filter, condition) -> addBinaryCondition(filter, condition, operator));
  }

  public static boolean addBinaryCondition(final StringBuilder filter, final QueryValue value,
    final String operator) {
    final List<QueryValue> values = value.getQueryValues();
    if (values.size() > 0) {
      filter.append('(');
      for (final QueryValue childValue : values) {
        if (appendQueryValue(filter, childValue)) {
          filter.append(operator);
        }
      }
      filter.setLength(filter.length() - operator.length());
      filter.append(')');
      return true;
    }
    return false;
  }

  public static boolean addColumn(final StringBuilder filter, final QueryValue condition) {
    final ColumnReference column = (ColumnReference)condition;
    filter.append(column.getName());
    return true;
  }

  public static void addUnaryCondition(final Class<? extends QueryValue> clazz,
    final String operator) {
    HANDLERS.put(clazz, (filter, condition) -> addUnaryCondition(filter, condition, operator));
  }

  public static boolean addUnaryCondition(final StringBuilder filter, final QueryValue value,
    final String operator) {
    final List<QueryValue> childValues = value.getQueryValues();
    if (childValues != null) {
      filter.append('(');
      final QueryValue childValue = childValues.get(0);
      if (appendQueryValue(filter, childValue)) {
        filter.append(operator);
      }
      filter.append(')');

      return true;
    }
    return false;
  }

  public static boolean addValue(final StringBuilder filter, final QueryValue condition) {
    final Value value = (Value)condition;
    final Object v = value.getValue();
    if (v instanceof Number || v instanceof Boolean) {
      filter.append(v);
    } else if (v instanceof UUID) {
      final UUID uuid = (UUID)v;
      filter.append(uuid.toString().toUpperCase());
    } else {
      filter.append('\'').append(v.toString().replace("'", "''")).append('\'');
    }
    return true;
  }

  public static boolean appendQueryValue(final StringBuilder filter, final QueryValue value) {
    final Class<? extends QueryValue> valueClass = value.getClass();
    final BiFunction<StringBuilder, QueryValue, Boolean> handler = HANDLERS.get(valueClass);
    if (handler != null) {
      return handler.apply(filter, value);
    }

    return false;
  }

  private final ApacheHttpRequestBuilderFactory requestFactory;

  private final URI uri;

  public ODataRecordStore(final ApacheHttpRequestBuilderFactory requestFactory, final URI uri) {
    this.requestFactory = requestFactory;
    this.uri = uri;
  }

  ODataRecordStore(final OData databaseFactory, final MapEx connectionProperties) {
    final URI uri = connectionProperties.getValue("url", DataTypes.ANY_URI);
    if (uri.getScheme().equals("odata")) {
      final String serviceUrl = uri.getSchemeSpecificPart();
      this.uri = URI.create(serviceUrl);
    } else {
      this.uri = uri;
    }
    final String apiKey = connectionProperties.getString("apiKey");
    if (apiKey == null) {
      throw new IllegalArgumentException("No login config");
    } else {
      this.requestFactory = new ConfigurableRequestBuilderFactory().addHeader("ApiKey", apiKey);
    }
  }

  JsonObject getJson(final URI uri) {
    return this.requestFactory.get(uri).setParameter(FORMAT_JSON).getJson();
  }

  @Override
  public int getRecordCount(Query query) {
    query = query.clone();
    query.setLimit(1);
    query.clearOrderBy();
    final ApacheHttpRequestBuilder request = newRequest(query);
    request.setParameter("$count", "true");
    final JsonObject result = request.getJson();
    return result.getInteger("@odata.count", 0);
  }

  @Override
  public String getRecordStoreType() {
    return "OData";
  }

  public URI getUri() {
    return this.uri;
  }

  @Override
  public RecordIterator newIterator(final Query query, final Map<String, Object> properties) {
    return new ODataQueryIterator(this, this.requestFactory, query, properties);
  }

  @Override
  public RecordWriter newRecordWriter(final boolean throwExceptions) {
    // TODO Auto-generated method stub
    return null;
  }

  public ApacheHttpRequestBuilder newRequest(final Query query) {
    final String name = query.getTablePath().getName();
    final URI baseUri = getUri();
    final URI uri = new UriBuilder(baseUri).appendPathSegments(name).build();
    final ApacheHttpRequestBuilder request = this.requestFactory.get(uri)
      .setParameter(ODataRecordStore.FORMAT_JSON);

    final QueryValue condition = query.getWhereCondition();
    if (condition != null) {
      final StringBuilder filter = new StringBuilder();
      if (ODataRecordStore.appendQueryValue(filter, condition)) {
        request.setParameter("$filter", filter);
      }
    }
    final int offset = query.getOffset();
    if (offset > 0) {
      request.setParameter("$skip", offset);
    }
    final int limit = query.getLimit();
    if (limit > 0 && limit < Integer.MAX_VALUE) {
      request.setParameter("$top", limit);
    }
    return request;
  }

  @Override
  protected RecordStoreSchema newRootSchema() {
    return new ODataRecordStoreSchema(this);
  }

  @Override
  protected Map<PathName, ? extends RecordStoreSchemaElement> refreshSchemaElements(
    final RecordStoreSchema schema) {
    final Map<PathName, RecordStoreSchemaElement> elements = new LinkedHashMap<>();
    final PathName pathName = schema.getPathName();
    final String schemaName = pathName.toString().substring(1).replace('/', '.');
    final URI uri = UrlUtil.appendPath(this.uri, "$metadata");
    final JsonObject json = getJson(uri);
    int startIndex;
    if (PathName.ROOT.equals(pathName)) {
      startIndex = 0;
    } else {
      startIndex = schemaName.length() + 1;
    }
    for (final String name : json.keySet()) {
      if (name.equals(schemaName)) {
        refreshSchemaRecordDefinitions((ODataRecordStoreSchema)schema, elements, pathName, json,
          name);
      } else if (!name.startsWith("$") && startIndex < name.length()) {
        String childName = name.substring(startIndex);
        final int endIndex = childName.indexOf('.');
        if (endIndex != -1) {
          childName = childName.substring(0, endIndex);
        }
        final PathName childPath = pathName.newChild(name);
        final ODataRecordStoreSchema childSchema = new ODataRecordStoreSchema(
          (ODataRecordStoreSchema)schema, childPath);
        elements.put(childPath, childSchema);
      }
    }

    return elements;
  }

  private void refreshSchemaRecordDefinitions(final ODataRecordStoreSchema schema,
    final Map<PathName, RecordStoreSchemaElement> elements, final PathName pathName,
    final JsonObject metadata, final String name) {
    final JsonObject schemaMap = metadata.getJsonObject(name);
    final JsonObject containerMap = schemaMap.getJsonObject("Container");
    if (containerMap.equalValue("$Kind", "EntityContainer")) {

      for (final String entitySetName : containerMap.keySet()) {
        if (!entitySetName.startsWith("$")) {
          final JsonObject entitySet = containerMap.getJsonObject(entitySetName);
          if (entitySet.equalValue("$Kind", "EntitySet")) {
            final String entityType = entitySet.getString("$Type");
            final PathName childName = pathName.newChild(entitySetName);
            final ODataRecordDefinition recordDefinition = new ODataRecordDefinition(schema,
              childName, metadata, entityType);
            elements.put(childName, recordDefinition);
          } else {
            Debug.noOp();
          }
        }
      }
    } else {
      Debug.noOp();
    }
  }

  @Override
  public String toString() {
    return this.uri.toString();
  }
}
