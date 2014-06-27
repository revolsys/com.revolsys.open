package com.revolsys.ui.html.builder;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;

import com.revolsys.collection.ResultPager;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.gis.data.model.RecordIdentifier;
import com.revolsys.gis.data.query.Or;
import com.revolsys.gis.data.query.Q;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.model.data.equals.EqualsInstance;
import com.revolsys.io.Reader;
import com.revolsys.ui.html.serializer.key.KeySerializer;
import com.revolsys.ui.html.view.TabElementContainer;
import com.revolsys.ui.web.utils.HttpServletUtils;
import com.revolsys.util.JavaBeanUtil;

public class DataObjectHtmlUiBuilder extends HtmlUiBuilder<DataObject> {

  private DataObjectStore dataStore;

  private String tableName;

  public DataObjectHtmlUiBuilder() {
  }

  public DataObjectHtmlUiBuilder(final String typePath, final String title) {
    super(typePath, title);
  }

  public DataObjectHtmlUiBuilder(final String typePath, final String title,
    final String pluralTitle) {
    super(typePath, title, pluralTitle);
  }

  public DataObjectHtmlUiBuilder(final String typePath, final String tableName,
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
    final DataObjectMetaData metaData = getMetaData();
    Query query = (Query)parameters.get("query");
    if (query == null) {
      final Map<String, Object> filter = (Map<String, Object>)parameters.get("filter");
      query = Query.and(metaData, filter);
    }
    final String fromClause = (String)parameters.get("fromClause");
    query.setFromClause(fromClause);

    return createDataTableMap(request, pageName, query);
  }

  protected Map<String, Object> createDataTableMap(
    final HttpServletRequest request, final String pageName, final Query query) {
    final String search = request.getParameter("sSearch");
    if (StringUtils.hasText(search)) {
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
      final ResultPager<DataObject> pager = getResultPager(query)) {
      return createDataTableMap(request, pager, pageName);
    }
  }

  public Object createDataTableMap(final String pageName,
    final Map<String, Object> parameters) {
    final HttpServletRequest request = HttpServletUtils.getRequest();
    return createDataTableMap(request, pageName, parameters);
  }

  @Override
  protected DataObject createObject() {
    return this.dataStore.create(this.tableName);
  }

  public void deleteObject(final Object id) {
    final DataObject object = loadObject(id);
    if (object != null) {
      this.dataStore.delete(object);
    }
  }

  @Override
  @PreDestroy
  public void destroy() {
    super.destroy();
    this.dataStore = null;
    this.tableName = null;
  }

  public DataObjectStore getDataStore() {
    return this.dataStore;
  }

  protected DataObjectMetaData getMetaData() {
    return getDataStore().getMetaData(getTableName());
  }

  public ResultPager<DataObject> getResultPager(final Query query) {
    return this.dataStore.page(query);
  }

  public String getTableName() {
    return this.tableName;
  }

  @Override
  protected void insertObject(final DataObject object) {
    if (object.getIdentifier() == null) {
      object.setIdValue(this.dataStore.createPrimaryIdValue(this.tableName));
    }
    this.dataStore.insert(object);
  }

  protected boolean isPropertyUnique(final DataObject object,
    final String attributeName) {
    final String value = object.getValue(attributeName);
    final DataObjectStore dataStore = getDataStore();
    final DataObjectMetaData metaData = dataStore.getMetaData(this.tableName);
    if (metaData == null) {
      return true;
    } else {
      final Query query = Query.equal(metaData, attributeName, value);
      final Reader<DataObject> results = dataStore.query(query);
      final List<DataObject> objects = results.read();
      if (object.getState() == DataObjectState.New) {
        return objects.isEmpty();
      } else {
        final RecordIdentifier id = object.getIdentifier();
        for (final Iterator<DataObject> iterator = objects.iterator(); iterator.hasNext();) {
          final DataObject matchedObject = iterator.next();
          final RecordIdentifier matchedId = matchedObject.getIdentifier();
          if (EqualsInstance.INSTANCE.equals(id, matchedId)) {
            iterator.remove();
          }
        }
        return objects.isEmpty();
      }
    }
  }

  @Override
  public DataObject loadObject(final Object id) {
    return loadObject(this.tableName, id);
  }

  public DataObject loadObject(final String typeName, final Object id) {
    final DataObject object = this.dataStore.load(typeName, id);
    return object;
  }

  public void setDataStore(final DataObjectStore dataStore) {
    this.dataStore = dataStore;
  }

  public void setTableName(final String tableName) {
    this.tableName = tableName;
  }

  @Override
  protected void updateObject(final DataObject object) {
    this.dataStore.update(object);
  }
}
