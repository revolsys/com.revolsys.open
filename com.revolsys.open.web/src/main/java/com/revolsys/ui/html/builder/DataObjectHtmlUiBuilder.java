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
import com.revolsys.gis.data.query.Conditions;
import com.revolsys.gis.data.query.MultipleCondition;
import com.revolsys.gis.data.query.Query;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
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
    final Map<String, Object> filter = (Map<String, Object>)parameters.get("filter");
    final Query query = Query.and(metaData, filter);

    final String fromClause = (String)parameters.get("fromClause");
    query.setFromClause(fromClause);

    return createDataTableMap(request, pageName, query);
  }

  protected Map<String, Object> createDataTableMap(
    final HttpServletRequest request, final String pageName, final Query query) {
    final String search = request.getParameter("sSearch");
    if (StringUtils.hasText(search)) {
      final List<KeySerializer> serializers = getSerializers(pageName, "list");
      final MultipleCondition or = Conditions.or();
      final int numSortColumns = HttpServletUtils.getIntegerParameter(request,
        "iColumns");
      for (int i = 0; i < numSortColumns; i++) {
        if (HttpServletUtils.getBooleanParameter(request, "bSearchable_" + i)) {
          final KeySerializer serializer = serializers.get(i);
          final String columnName = JavaBeanUtil.getFirstName(serializer.getKey());
          or.add(Conditions.likeUpper("T." + columnName, search));
        }
      }
      if (!or.isEmpty()) {
        query.and(or);
      }
    }
    final Map<String, Boolean> orderBy = getDataTableSortOrder(request);
    query.setOrderBy(orderBy);
    final ResultPager<DataObject> pager = getResultPager(query);
    try {
      return createDataTableMap(request, pager, pageName);
    } finally {
      pager.close();
    }
  }

  public Object createDataTableMap(final String pageName,
    final Map<String, Object> parameters) {
    final HttpServletRequest request = HttpServletUtils.getRequest();
    return createDataTableMap(request, pageName, parameters);
  }

  @Override
  protected DataObject createObject() {
    return dataStore.create(tableName);
  }

  public void deleteObject(final Object id) {
    final DataObject object = loadObject(id);
    if (object != null) {
      dataStore.delete(object);
    }
  }

  @Override
  @PreDestroy
  public void destroy() {
    super.destroy();
    dataStore = null;
    tableName = null;
  }

  public DataObjectStore getDataStore() {
    return dataStore;
  }

  protected DataObjectMetaData getMetaData() {
    return getDataStore().getMetaData(getTableName());
  }

  public ResultPager<DataObject> getResultPager(final Query query) {
    return dataStore.page(query);
  }

  public String getTableName() {
    return tableName;
  }

  @Override
  protected void insertObject(final DataObject object) {
    if (object.getIdValue() == null) {
      object.setIdValue(dataStore.createPrimaryIdValue(tableName));
    }
    dataStore.insert(object);
  }

  protected boolean isPropertyUnique(final DataObject object,
    final String attributeName) {
    final String value = object.getValue(attributeName);
    final DataObjectStore dataStore = getDataStore();
    final DataObjectMetaData metaData = dataStore.getMetaData(tableName);
    if (metaData == null) {
      return true;
    } else {
      final Query query = Query.equal(metaData, attributeName, value);
      final Reader<DataObject> results = dataStore.query(query);
      final List<DataObject> objects = results.read();
      if (object.getState() == DataObjectState.New) {
        return objects.isEmpty();
      } else {
        final Object id = object.getIdValue();
        for (final Iterator<DataObject> iterator = objects.iterator(); iterator.hasNext();) {
          final DataObject matchedObject = iterator.next();
          final Object matchedId = matchedObject.getIdValue();
          if (EqualsRegistry.INSTANCE.equals(id, matchedId)) {
            iterator.remove();
          }
        }
        return objects.isEmpty();
      }
    }
  }

  @Override
  public DataObject loadObject(final Object id) {
    return loadObject(tableName, id);
  }

  public DataObject loadObject(final String typeName, final Object id) {
    final DataObject object = dataStore.load(typeName, id);
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
    dataStore.update(object);
  }
}
