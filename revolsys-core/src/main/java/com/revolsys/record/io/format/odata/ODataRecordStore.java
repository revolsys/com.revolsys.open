package com.revolsys.record.io.format.odata;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import javax.swing.SwingUtilities;

import org.apache.http.NameValuePair;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.http.ApacheHttpRequestBuilder;
import com.revolsys.http.ApacheHttpRequestBuilderFactory;
import com.revolsys.http.ConfigurableRequestBuilderFactory;
import com.revolsys.net.http.SimpleNameValuePair;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.query.AcceptAllCondition;
import com.revolsys.record.query.Add;
import com.revolsys.record.query.And;
import com.revolsys.record.query.CollectionValue;
import com.revolsys.record.query.Column;
import com.revolsys.record.query.ColumnAlias;
import com.revolsys.record.query.ColumnReference;
import com.revolsys.record.query.Divide;
import com.revolsys.record.query.Equal;
import com.revolsys.record.query.GreaterThan;
import com.revolsys.record.query.GreaterThanEqual;
import com.revolsys.record.query.ILike;
import com.revolsys.record.query.In;
import com.revolsys.record.query.IsNotNull;
import com.revolsys.record.query.IsNull;
import com.revolsys.record.query.LessThan;
import com.revolsys.record.query.LessThanEqual;
import com.revolsys.record.query.Like;
import com.revolsys.record.query.Mod;
import com.revolsys.record.query.Multiply;
import com.revolsys.record.query.NotEqual;
import com.revolsys.record.query.Or;
import com.revolsys.record.query.OrderBy;
import com.revolsys.record.query.Query;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.Subtract;
import com.revolsys.record.query.Value;
import com.revolsys.record.query.functions.EnvelopeIntersects;
import com.revolsys.record.query.functions.Lower;
import com.revolsys.record.query.functions.Upper;
import com.revolsys.record.query.functions.WithinDistance;
import com.revolsys.record.schema.AbstractRecordStore;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.record.schema.RecordStoreSchemaElement;
import com.revolsys.util.Debug;
import com.revolsys.util.UriBuilder;
import com.revolsys.util.UrlUtil;

public class ODataRecordStore extends AbstractRecordStore {
  public static final NameValuePair FORMAT_JSON = new SimpleNameValuePair("$format", "json");

  private static final Map<Class<? extends QueryValue>, BiConsumer<StringBuilder, QueryValue>> HANDLERS = new HashMap<>();

  static {
    addMultipleCondition(And.class, " and ");
    addMultipleCondition(Or.class, " or ");
    addBinaryCondition(Equal.class, " eq ");
    addBinaryCondition(NotEqual.class, " ne ");
    addBinaryCondition(GreaterThan.class, " gt ");
    addBinaryCondition(GreaterThanEqual.class, " ge ");
    addBinaryCondition(LessThan.class, " lt ");
    addBinaryCondition(LessThanEqual.class, " le ");
    addBinaryCondition(Add.class, " add ");
    addBinaryCondition(Subtract.class, " sub ");
    addBinaryCondition(Multiply.class, " mul ");
    addBinaryCondition(Divide.class, " div ");
    addBinaryCondition(Mod.class, " mod ");
    addBinaryCondition(In.class, " in ");
    // TODO has

    addUnaryCondition(IsNull.class, " eq null");
    addUnaryCondition(IsNotNull.class, " ne null");
    addUnaryFunction("tolower", Lower.class);
    addUnaryFunction("toupper", Upper.class);
    HANDLERS.put(Like.class, ODataRecordStore::addLike);
    HANDLERS.put(ILike.class, ODataRecordStore::addILike);
    HANDLERS.put(Value.class, ODataRecordStore::addValue);
    HANDLERS.put(CollectionValue.class, ODataRecordStore::addCollectionValue);
    HANDLERS.put(Column.class, ODataRecordStore::addColumn);
    HANDLERS.put(ColumnAlias.class, ODataRecordStore::addColumn);
    HANDLERS.put(FieldDefinition.class, ODataRecordStore::addColumn);
    HANDLERS.put(AcceptAllCondition.class, (filter, value) -> {
    });
    HANDLERS.put(EnvelopeIntersects.class, (filter, value) -> {
      final EnvelopeIntersects i = (EnvelopeIntersects)value;
      final Value v = i.getRight();
      final BoundingBox boundingBox = (BoundingBox)v.getValue();
      final Polygon polygon = boundingBox
        .toPolygon(boundingBox.getGeometryFactory().convertAxisCount(2), 1, 1);
      final String geometryPrefix = getGeometryPrefix(boundingBox);

      filter.append("geo.intersects(");
      appendQueryValue(filter, i.getLeft());
      filter.append(", ").append(geometryPrefix).append('\'').append(polygon.toEwkt()).append("')");
    });
    HANDLERS.put(WithinDistance.class, (filter, value) -> {
      final WithinDistance wd = (WithinDistance)value;
      final QueryValue g1 = wd.getGeometry1Value();
      final QueryValue g2 = wd.getGeometry2Value();
      filter.append("geo.distance(");
      appendQueryValue(filter, g1);
      filter.append(", ");
      appendQueryValue(filter, g2);
      filter.append(") le ");
      appendQueryValue(filter, wd.getDistanceValue());
    });
  }

  public static void addBinaryCondition(final Class<? extends QueryValue> clazz,
    final String operator) {
    HANDLERS.put(clazz, (filter, condition) -> addBinaryCondition(filter, condition, operator));
  }

  private static void addBinaryCondition(final StringBuilder filter, final QueryValue value,
    final String operator) {
    final List<QueryValue> values = value.getQueryValues();
    filter.append('(');
    appendQueryValue(filter, values.get(0));
    filter.append(operator);
    appendQueryValue(filter, values.get(1));
    filter.append(')');
  }

  private static void addCollectionValue(final StringBuilder filter, final QueryValue condition) {
    filter.append('(');
    boolean first = true;
    for (final QueryValue value : condition.getQueryValues()) {
      if (first) {
        first = false;
      } else {
        filter.append(',');
      }
      appendQueryValue(filter, value);
    }
    filter.append(')');
  }

  private static void addColumn(final StringBuilder filter, final QueryValue condition) {
    final ColumnReference column = (ColumnReference)condition;
    filter.append(column.getName());
  }

  private static void addILike(final StringBuilder filter, final QueryValue queryValue) {
    final List<QueryValue> values = queryValue.getQueryValues();
    final QueryValue left = values.get(0);
    final QueryValue right = values.get(1);

    if (left instanceof ColumnReference && right instanceof Value) {
      final ColumnReference column = (ColumnReference)left;
      final Value value = (Value)right;
      filter.append("contains(toupper(");
      filter.append(column.getName());
      filter.append("),toupper('");
      String text = value.getValue().toString();
      if (text.startsWith("%")) {
        text = text.substring(1);
      }
      if (text.endsWith("%")) {
        text = text.substring(0, text.length() - 1);
      }
      filter.append(text);
      filter.append("'))");
    } else {
      throw new IllegalArgumentException("iLike only supports column and value: " + queryValue);
    }
  }

  private static void addLike(final StringBuilder filter, final QueryValue queryValue) {
    final List<QueryValue> values = queryValue.getQueryValues();
    final QueryValue left = values.get(0);

    final QueryValue right = values.get(1);
    if (left instanceof ColumnReference && right instanceof Value) {
      final ColumnReference column = (ColumnReference)left;
      final Value value = (Value)right;
      filter.append("contains(");
      filter.append(column.getName());
      filter.append(',');
      String text = value.getValue().toString();
      if (text.startsWith("%")) {
        text = text.substring(1);
      }
      if (text.endsWith("%")) {
        text = text.substring(1);
      }
      filter.append(column.getName());
      filter.append(')');
    }
    throw new IllegalArgumentException("Like only supports column and value: " + queryValue);
  }

  public static void addMultipleCondition(final Class<? extends QueryValue> clazz,
    final String operator) {
    HANDLERS.put(clazz, (filter, condition) -> addMultipleCondition(filter, condition, operator));
  }

  private static void addMultipleCondition(final StringBuilder filter, final QueryValue value,
    final String operator) {
    final List<QueryValue> values = value.getQueryValues();
    filter.append('(');
    boolean first = true;
    for (final QueryValue arg : values) {
      if (first) {
        first = false;
      } else {
        filter.append(operator);
      }
      appendQueryValue(filter, arg);
    }
    filter.append(')');
  }

  private static void addUnaryCondition(final Class<? extends QueryValue> clazz,
    final String operator) {
    HANDLERS.put(clazz, (filter, condition) -> addUnaryCondition(filter, condition, operator));
  }

  private static void addUnaryCondition(final StringBuilder filter, final QueryValue value,
    final String operator) {
    final List<QueryValue> childValues = value.getQueryValues();
    filter.append('(');
    final QueryValue childValue = childValues.get(0);
    appendQueryValue(filter, childValue);
    filter.append(operator);
    filter.append(')');
  }

  private static void addUnaryFunction(final String operator,
    final Class<? extends QueryValue> clazz) {
    HANDLERS.put(clazz, (filter, condition) -> addUnaryFunction(filter, condition, operator));
  }

  private static void addUnaryFunction(final StringBuilder filter, final QueryValue value,
    final String functionName) {
    final List<QueryValue> childValues = value.getQueryValues();
    filter.append(functionName);
    filter.append('(');
    final QueryValue childValue = childValues.get(0);
    appendQueryValue(filter, childValue);
    filter.append(')');
  }

  private static void addValue(final StringBuilder filter, final QueryValue condition) {
    final Value value = (Value)condition;
    final Object v = value.getValue();
    if (v instanceof Number || v instanceof Boolean) {
      filter.append(v);
    } else if (v instanceof UUID) {
      final UUID uuid = (UUID)v;
      filter.append(uuid.toString().toUpperCase());
    } else if (v instanceof Geometry) {
      final Geometry geometry = (Geometry)v;
      final String geometryPrefix = getGeometryPrefix(geometry);
      filter.append(geometryPrefix)
        .append('\'')
        .append(geometry.convertAxisCount(2).toEwkt())
        .append('\'');
    } else {
      filter.append('\'').append(v.toString().replace("'", "''")).append('\'');
    }
  }

  private static void appendQueryValue(final StringBuilder filter, final QueryValue value) {
    final Class<? extends QueryValue> valueClass = value.getClass();

    for (Class<?> clazz = valueClass; clazz != Object.class; clazz = clazz.getSuperclass()) {
      final BiConsumer<StringBuilder, QueryValue> handler = HANDLERS.get(clazz);
      if (handler != null) {
        handler.accept(filter, value);
        return;
      }
    }
    throw new IllegalArgumentException(
      "Query clause not supported: " + value + ": " + value.getClass());
  }

  private static String getGeometryPrefix(final GeometryFactoryProxy spatial) {
    if (spatial.getGeometryFactory().isGeographic()) {
      return "geography";
    } else {
      return "geometry";
    }
  }

  private final ApacheHttpRequestBuilderFactory requestBuilderFactory;

  private final URI uri;

  public ODataRecordStore(final ApacheHttpRequestBuilderFactory requestFactory, final URI uri) {
    this.requestBuilderFactory = requestFactory;
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
    ApacheHttpRequestBuilderFactory requestBuilderFactory = connectionProperties
      .getValue("requestBuilderFactory");
    if (requestBuilderFactory == null) {
      final String apiKey = connectionProperties.getString("apiKey");
      if (apiKey == null) {
        throw new IllegalArgumentException("No login config");
      } else {
        requestBuilderFactory = new ConfigurableRequestBuilderFactory().addHeader("ApiKey", apiKey);
      }
    }
    this.requestBuilderFactory = requestBuilderFactory;
  }

  @Override
  public boolean deleteRecord(final PathName typePath, final Identifier identifier) {
    final String name = typePath.getName();
    final URI baseUri = getUri();
    final Object idValue = identifier.getValue(0);
    String idString;
    if (idValue instanceof Number) {
      idString = idValue.toString();
    } else {
      idString = "'" + idValue.toString().replace("'", "''") + "'";
    }
    final URI uri = new UriBuilder(baseUri).appendPathSegments(name + "(" + idString + ")").build();

    if (SwingUtilities.isEventDispatchThread()) {
      Debug.noOp();
    }
    final ApacheHttpRequestBuilder request = this.requestBuilderFactory.delete(uri)
      .setParameter(ODataRecordStore.FORMAT_JSON);
    final JsonObject result = request.getJson();
    return result.getBoolean("deleted", false);
  }

  @Override
  public boolean deleteRecord(final Record record) {
    if (record == null) {
      return false;
    } else {
      final PathName typeName = record.getPathName();
      final Identifier identifier = record.getIdentifier();
      return deleteRecord(typeName, identifier);
    }
  }

  JsonObject getJson(final URI uri) {
    if (SwingUtilities.isEventDispatchThread()) {
      Debug.noOp();
    }
    return this.requestBuilderFactory.get(uri).setParameter(FORMAT_JSON).getJson();
  }

  @Override
  public Record getRecord(final PathName typePath, final Identifier id) {
    if (id == null) {
      return null;
    } else {
      final Object idValue = id.getValue(0);
      return getRecordDo(typePath, idValue);
    }
  }

  @Override
  public Record getRecord(final PathName typePath, final Object... id) {
    return getRecordDo(typePath, id[0]);
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

  private Record getRecordDo(final PathName typePath, final Object idValue) {
    if (SwingUtilities.isEventDispatchThread()) {
      Debug.noOp();
    }
    final RecordDefinition recordDefinition = getRecordDefinition(typePath);
    if (recordDefinition == null) {
      return null;
    }

    final String name = typePath.getName();
    final URI baseUri = getUri();
    String idString;
    if (idValue instanceof Number) {
      idString = idValue.toString();
    } else {
      idString = "'" + idValue.toString().replace("'", "''") + "'";
    }
    final URI uri = new UriBuilder(baseUri).appendPathSegments(name + "(" + idString + ")").build();
    final ApacheHttpRequestBuilder request = this.requestBuilderFactory.get(uri)
      .setParameter(ODataRecordStore.FORMAT_JSON);

    final JsonObject result = request.getJson();
    return recordDefinition.newRecord(result);
  }

  @Override
  public RecordReader getRecords(final Query query) {
    return newIterator(query, null);
  }

  @Override
  public String getRecordStoreType() {
    return "OData";
  }

  public URI getUri() {
    return this.uri;
  }

  @Override
  public void insertRecord(final Record record) {
    if (SwingUtilities.isEventDispatchThread()) {
      Debug.noOp();
    }
    final String name = record.getPathName().getName();
    final URI baseUri = getUri();
    final URI uri = new UriBuilder(baseUri).appendPathSegments(name).build();

    final JsonObject json = record.toJson();
    final ApacheHttpRequestBuilder request = this.requestBuilderFactory.post(uri)
      .setParameter(ODataRecordStore.FORMAT_JSON)
      .setJsonEntity(json);
    final JsonObject result = request.getJson();
    record.setValues(result);
  }

  @Override
  public ODataQueryIterator newIterator(final Query query, final Map<String, Object> properties) {
    if (SwingUtilities.isEventDispatchThread()) {
      Debug.noOp();
    }
    return new ODataQueryIterator(this, this.requestBuilderFactory, query, properties);
  }

  @Override
  public RecordWriter newRecordWriter(final boolean throwExceptions) {
    throw new UnsupportedOperationException("Writing records not supported");
  }

  public ApacheHttpRequestBuilder newRequest(final Query query) {
    if (SwingUtilities.isEventDispatchThread()) {
      Debug.noOp();
    }
    final String name = query.getTablePath().getName();
    final URI baseUri = getUri();
    final URI uri = new UriBuilder(baseUri).appendPathSegments(name).build();
    final ApacheHttpRequestBuilder request = this.requestBuilderFactory.get(uri)
      .setParameter(ODataRecordStore.FORMAT_JSON);
    newRequestSelect(query, request);
    newRequestFilter(query, request);
    newRequestOrderBy(request, query);
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

  private void newRequestFilter(final Query query, final ApacheHttpRequestBuilder request) {
    final QueryValue condition = query.getWhereCondition();
    if (condition != null) {
      final StringBuilder filter = new StringBuilder();
      appendQueryValue(filter, condition);
      if (filter.length() > 0) {
        request.setParameter("$filter", filter);
      }
    }
  }

  private void newRequestOrderBy(final ApacheHttpRequestBuilder request, final Query query) {
    final List<OrderBy> orderBys = query.getOrderBy();
    if (!orderBys.isEmpty()) {
      final StringBuilder order = new StringBuilder();
      boolean first = true;
      for (final OrderBy orderBy : orderBys) {
        if (first) {
          first = false;
        } else {
          order.append(", ");
        }
        final QueryValue orderField = orderBy.getField();
        if (orderField instanceof ColumnReference) {
          final ColumnReference column = (ColumnReference)orderField;
          order.append(column.getName());
        }
        if (!orderBy.isAscending()) {
          order.append(" desc");
        }
      }
      request.setParameter("$orderby", order);
    }
  }

  private void newRequestSelect(final Query query, final ApacheHttpRequestBuilder request) {
    final List<QueryValue> selectValues = query.getSelect();
    if (!selectValues.isEmpty()) {
      final StringBuilder select = new StringBuilder();
      for (final QueryValue selectItem : selectValues) {
        if (selectItem instanceof ColumnReference) {
          final ColumnReference column = (ColumnReference)selectItem;
          if (select.length() > 0) {
            select.append(',');
          }
          select.append(column.getName());
        } else {
          throw new IllegalArgumentException("Not supported:" + selectItem);
        }
      }
      request.setParameter("$select", select);
    }
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
          }
        }
      }
    }
  }

  @Override
  public String toString() {
    return this.uri.toString();
  }
}
