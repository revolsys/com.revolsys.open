package com.revolsys.ui.html.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.io.PathName;

import com.revolsys.collection.ResultPager;
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

  public void deleteObject(final Object id) {
    final Record record = loadObject(id);
    if (record != null) {
      this.recordStore.deleteRecord(record);
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
      final Identifier identifier = this.recordStore.newPrimaryIdentifier(this.tableName);
      object.setIdentifier(identifier);
    }
    this.recordStore.insertRecord(object);
  }

  protected boolean isPropertyUnique(final Record object, final String fieldName) {
    final String value = object.getValue(fieldName);
    final RecordStore recordStore = getRecordStore();
    final RecordDefinition recordDefinition = recordStore.getRecordDefinition(this.tableName);
    if (recordDefinition == null) {
      return true;
    } else {
      final Query query = Query.equal(recordDefinition, fieldName, value);
      final Reader<Record> results = recordStore.getRecords(query);
      final List<Record> objects = results.toList();
      if (object.getState() == RecordState.NEW) {
        return objects.isEmpty();
      } else {
        final Identifier id = object.getIdentifier();
        for (final Iterator<Record> iterator = objects.iterator(); iterator.hasNext();) {
          final Record matchedObject = iterator.next();
          final Identifier matchedId = matchedObject.getIdentifier();
          if (DataType.equal(id, matchedId)) {
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
    final Record object = this.recordStore.getRecord(typeName, id);
    return object;
  }

  public Object newDataTableHandler(final HttpServletRequest request, final String pageName) {
    final Map<String, Object> parameters = Collections.emptyMap();
    return newDataTableHandler(request, pageName, parameters);
  }

  public Object newDataTableHandler(final HttpServletRequest request, final String pageName,
    final Map<String, Object> parameters) {
    if (isDataTableCallback(request)) {
      return newDataTableMap(request, pageName, parameters);
    } else {
      final TabElementContainer tabs = new TabElementContainer();
      addTabDataTable(tabs, this, pageName, parameters);
      return tabs;
    }
  }

  public Object newDataTableHandlerOrRedirect(final HttpServletRequest request,
    final HttpServletResponse response, final String pageName, final Object parentBuilder,
    final String parentPageName, final Map<String, Object> parameters) {
    if (isDataTableCallback(request)) {
      return newDataTableMap(request, pageName, parameters);
    } else {
      return redirectToTab(parentBuilder, parentPageName, pageName);
    }
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> newDataTableMap(final HttpServletRequest request,
    final String pageName, final Map<String, Object> parameters) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    Query query = (Query)parameters.get("query");
    if (query == null) {
      final Map<String, Object> filter = (Map<String, Object>)parameters.get("filter");
      query = Query.and(recordDefinition, filter);
    }
    final String fromClause = (String)parameters.get("fromClause");
    if (Property.hasValue(fromClause)) {
      query.setFromClause(fromClause);
    }
    return newDataTableMap(request, pageName, query);
  }

  protected Map<String, Object> newDataTableMap(final HttpServletRequest request,
    final String pageName, final Query query) {
    final String search = request.getParameter("search[value]");
    final List<String> fieldNames = new ArrayList<>();
    final List<KeySerializer> serializers = getSerializers(pageName, "list");
    final Or or = new Or();
    for (int i = 0;; i++) {
      final String name = request.getParameter("columns[" + i + "][name]");
      if (Property.hasValue(name)) {
        final KeySerializer serializer = serializers.get(i);
        final String fieldName = serializer.getSortFieldName();
        fieldNames.add(fieldName);
        if (Property.hasValue(search)) {
          if (HttpServletUtils.getBooleanParameter(request, "columns[" + i + "][searchable]")) {
            or.or(Q.iLike("T." + fieldName, search));
          }
        }
      } else {
        break;
      }
    }
    if (!or.isEmpty()) {
      query.and(or);
    }
    final Map<String, Boolean> orderBy = getDataTableSortOrder(fieldNames, request);
    query.setOrderBy(orderBy);

    final RecordStore recordStore = getRecordStore();
    return newDataTableMap(request, recordStore, query, pageName);
  }

  public Object newDataTableMap(final String pageName, final Map<String, Object> parameters) {
    final HttpServletRequest request = HttpServletUtils.getRequest();
    return newDataTableMap(request, pageName, parameters);
  }

  @Override
  protected Record newObject() {
    return this.recordStore.newRecord(this.tableName);
  }

  public void setRecordStore(final RecordStore recordStore) {
    this.recordStore = recordStore;
  }

  public void setTableName(final PathName tableName) {
    this.tableName = tableName;
  }

  @Override
  protected void updateObject(final Record object) {
    this.recordStore.updateRecord(object);
  }
}
