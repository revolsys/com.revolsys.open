package com.revolsys.ui.html.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.revolsys.collection.ResultPager;
import com.revolsys.equals.EqualsInstance;
import com.revolsys.identifier.Identifier;
import com.revolsys.io.PathName;
import com.revolsys.io.Reader;
import com.revolsys.record.Record;
import com.revolsys.record.RecordState;
import com.revolsys.record.query.Or;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.ui.html.serializer.key.KeySerializer;
import com.revolsys.ui.html.view.TabElementContainer;
import com.revolsys.ui.web.utils.HttpServletUtils;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public class RecordHtmlUiBuilder extends HtmlUiBuilder<Record> {

  private RecordStore recordStore;

  private PathName tableName;

  public RecordHtmlUiBuilder() {
  }

  public RecordHtmlUiBuilder(final String typePath, final PathName tableName,
    final String idPropertyName, final String title, final String pluralTitle) {
    super(typePath, title, pluralTitle);
    this.tableName = tableName;
    setIdPropertyName(idPropertyName);
  }

  public RecordHtmlUiBuilder(final String typePath, final String title) {
    super(typePath, title);
  }

  public RecordHtmlUiBuilder(final String typePath, final String title, final String pluralTitle) {
    super(typePath, title, pluralTitle);
  }

  public Object createDataTableHandler(final HttpServletRequest request, final String pageName) {
    final Map<String, Object> parameters = Collections.emptyMap();
    return createDataTableHandler(request, pageName, parameters);
  }

  public Object createDataTableHandler(final HttpServletRequest request, final String pageName,
    final Map<String, Object> parameters) {
    if (isDataTableCallback(request)) {
      return createDataTableMap(request, pageName, parameters);
    } else {
      final TabElementContainer tabs = new TabElementContainer();
      addTabDataTable(tabs, this, pageName, parameters);
      return tabs;
    }
  }

  public Object createDataTableHandlerOrRedirect(final HttpServletRequest request,
    final HttpServletResponse response, final String pageName, final Object parentBuilder,
    final String parentPageName, final Map<String, Object> parameters) {
    if (isDataTableCallback(request)) {
      return createDataTableMap(request, pageName, parameters);
    } else {
      return redirectToTab(parentBuilder, parentPageName, pageName);
    }
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> createDataTableMap(final HttpServletRequest request,
    final String pageName, final Map<String, Object> parameters) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    Query query = (Query)parameters.get("query");
    if (query == null) {
      final Map<String, Object> filter = (Map<String, Object>)parameters.get("filter");
      query = Query.and(recordDefinition, filter);
    }
    final String fromClause = (String)parameters.get("fromClause");
    query.setFromClause(fromClause);

    return createDataTableMap(request, pageName, query);
  }

  protected Map<String, Object> createDataTableMap(final HttpServletRequest request,
    final String pageName, final Query query) {
    final String search = request.getParameter("search[value]");
    final List<String> columnNames = new ArrayList<>();
    final List<KeySerializer> serializers = getSerializers(pageName, "list");
    final Or or = new Or();
    for (int i = 0;; i++) {
      final String name = request.getParameter("columns[" + i + "][name]");
      if (Property.hasValue(name)) {
        final KeySerializer serializer = serializers.get(i);
        final String columnName = JavaBeanUtil.getFirstName(serializer.getKey());
        columnNames.add(columnName);
        if (Property.hasValue(search)) {
          if (HttpServletUtils.getBooleanParameter(request, "columns[" + i + "][searchable]")) {
            or.add(Q.iLike("T." + columnName, search));
          }
        }
      } else {
        break;
      }
    }
    if (!or.isEmpty()) {
      query.and(or);
    }
    final Map<String, Boolean> orderBy = getDataTableSortOrder(columnNames, request);
    query.setOrderBy(orderBy);

    return createDataTableMap(request, getRecordStore(), query, pageName);
  }

  public Object createDataTableMap(final String pageName, final Map<String, Object> parameters) {
    final HttpServletRequest request = HttpServletUtils.getRequest();
    return createDataTableMap(request, pageName, parameters);
  }

  @Override
  protected Record createObject() {
    return this.recordStore.newRecord(this.tableName);
  }

  public void deleteObject(final Object id) {
    final Record object = loadObject(id);
    if (object != null) {
      this.recordStore.delete(object);
    }
  }

  @Override
  @PreDestroy
  public void destroy() {
    super.destroy();
    this.recordStore = null;
    this.tableName = null;
  }

  protected RecordDefinition getRecordDefinition() {
    return getRecordStore().getRecordDefinition(getTableName());
  }

  public RecordStore getRecordStore() {
    return this.recordStore;
  }

  public ResultPager<Record> getResultPager(final Query query) {
    return this.recordStore.page(query);
  }

  public PathName getTableName() {
    return this.tableName;
  }

  @Override
  protected void insertObject(final Record object) {
    if (object.getIdentifier() == null) {
      object.setIdValue(this.recordStore.createPrimaryIdValue(this.tableName));
    }
    this.recordStore.insert(object);
  }

  protected boolean isPropertyUnique(final Record object, final String fieldName) {
    final String value = object.getValue(fieldName);
    final RecordStore recordStore = getRecordStore();
    final RecordDefinition recordDefinition = recordStore.getRecordDefinition(this.tableName);
    if (recordDefinition == null) {
      return true;
    } else {
      final Query query = Query.equal(recordDefinition, fieldName, value);
      final Reader<Record> results = recordStore.query(query);
      final List<Record> objects = results.read();
      if (object.getState() == RecordState.New) {
        return objects.isEmpty();
      } else {
        final Identifier id = object.getIdentifier();
        for (final Iterator<Record> iterator = objects.iterator(); iterator.hasNext();) {
          final Record matchedObject = iterator.next();
          final Identifier matchedId = matchedObject.getIdentifier();
          if (EqualsInstance.INSTANCE.equals(id, matchedId)) {
            iterator.remove();
          }
        }
        return objects.isEmpty();
      }
    }
  }

  @Override
  public Record loadObject(final Object id) {
    return loadObject(this.tableName, id);
  }

  public Record loadObject(final PathName typeName, final Object id) {
    final Record object = this.recordStore.load(typeName, id);
    return object;
  }

  public void setRecordStore(final RecordStore recordStore) {
    this.recordStore = recordStore;
  }

  public void setTableName(final PathName tableName) {
    this.tableName = tableName;
  }

  @Override
  protected void updateObject(final Record object) {
    this.recordStore.update(object);
  }
}
