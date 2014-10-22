package com.revolsys.ui.html.builder;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.revolsys.collection.ResultPager;
import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.query.Or;
import com.revolsys.data.query.Q;
import com.revolsys.data.query.Query;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordState;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.io.Reader;
import com.revolsys.ui.html.serializer.key.KeySerializer;
import com.revolsys.ui.html.view.TabElementContainer;
import com.revolsys.ui.web.utils.HttpServletUtils;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public class RecordHtmlUiBuilder extends HtmlUiBuilder<Record> {

  private RecordStore recordStore;

  private String tableName;

  public RecordHtmlUiBuilder() {
  }

  public RecordHtmlUiBuilder(final String typePath, final String title) {
    super(typePath, title);
  }

  public RecordHtmlUiBuilder(final String typePath, final String title,
    final String pluralTitle) {
    super(typePath, title, pluralTitle);
  }

  public RecordHtmlUiBuilder(final String typePath, final String tableName,
    final String idPropertyName, final String title, final String pluralTitle) {
    super(typePath, title, pluralTitle);
    this.tableName = tableName;
    setIdPropertyName(idPropertyName);
  }

  public Object createDataTableHandler(final HttpServletRequest request,
    final String pageName) {
    final Map<String, Object> parameters = Collections.emptyMap();
    return createDataTableHandler(request, pageName, parameters);
  }

  public Object createDataTableHandler(final HttpServletRequest request,
    final String pageName, final Map<String, Object> parameters) {
    if (isDataTableCallback(request)) {
      return createDataTableMap(request, pageName, parameters);
    } else {
      final TabElementContainer tabs = new TabElementContainer();
      addTabDataTable(tabs, this, pageName, parameters);
      return tabs;
    }
  }

  public Object createDataTableHandlerOrRedirect(
    final HttpServletRequest request, final HttpServletResponse response,
    final String pageName, final Object parentBuilder,
    final String parentPageName, final Map<String, Object> parameters) {
    if (isDataTableCallback(request)) {
      return createDataTableMap(request, pageName, parameters);
    } else {
      return redirectToTab(parentBuilder, parentPageName, pageName);
    }
  }

  @SuppressWarnings("unchecked")
  public Map<String, Object> createDataTableMap(
    final HttpServletRequest request, final String pageName,
    final Map<String, Object> parameters) {
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

  protected Map<String, Object> createDataTableMap(
    final HttpServletRequest request, final String pageName, final Query query) {
    final String search = request.getParameter("sSearch");
    if (Property.hasValue(search)) {
      final List<KeySerializer> serializers = getSerializers(pageName, "list");
      final Or or = new Or();
      final int numSortColumns = HttpServletUtils.getIntegerParameter(request,
          "iColumns");
      for (int i = 0; i < numSortColumns; i++) {
        if (HttpServletUtils.getBooleanParameter(request, "bSearchable_" + i)) {
          final KeySerializer serializer = serializers.get(i);
          final String columnName = JavaBeanUtil.getFirstName(serializer.getKey());
          or.add(Q.iLike("T." + columnName, search));
        }
      }
      if (!or.isEmpty()) {
        query.and(or);
      }
    }
    final Map<String, Boolean> orderBy = getDataTableSortOrder(request);
    query.setOrderBy(orderBy);

    try (
        final ResultPager<Record> pager = getResultPager(query)) {
      return createDataTableMap(request, pager, pageName);
    }
  }

  public Object createDataTableMap(final String pageName,
    final Map<String, Object> parameters) {
    final HttpServletRequest request = HttpServletUtils.getRequest();
    return createDataTableMap(request, pageName, parameters);
  }

  @Override
  protected Record createObject() {
    return this.recordStore.create(this.tableName);
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

  public String getTableName() {
    return this.tableName;
  }

  @Override
  protected void insertObject(final Record object) {
    if (object.getIdentifier() == null) {
      object.setIdValue(this.recordStore.createPrimaryIdValue(this.tableName));
    }
    this.recordStore.insert(object);
  }

  protected boolean isPropertyUnique(final Record object,
    final String fieldName) {
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

  public Record loadObject(final String typeName, final Object id) {
    final Record object = this.recordStore.load(typeName, id);
    return object;
  }

  public void setRecordStore(final RecordStore recordStore) {
    this.recordStore = recordStore;
  }

  public void setTableName(final String tableName) {
    this.tableName = tableName;
  }

  @Override
  protected void updateObject(final Record object) {
    this.recordStore.update(object);
  }
}
