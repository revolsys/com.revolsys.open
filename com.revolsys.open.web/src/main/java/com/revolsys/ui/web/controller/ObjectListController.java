package com.revolsys.ui.web.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.revolsys.collection.ResultPager;
import com.revolsys.gis.data.io.DataAccessObject;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.form.Form;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.ui.html.view.FilterableTableView;
import com.revolsys.ui.html.view.MenuElement;
import com.revolsys.ui.html.view.ResultPagerView;
import com.revolsys.ui.model.Menu;

public class ObjectListController extends BaseController {

  private DataAccessObject<?> dataAccessObject;

  private int defaultPageSize = 25;

  private HtmlUiBuilder htmlUiBuilder;

  private int maxPageSize = 100;

  public DataAccessObject<?> getDataAccessObject() {
    return dataAccessObject;
  }

  public int getDefaultPageSize() {
    return defaultPageSize;
  }

  public HtmlUiBuilder getHtmlUiBuilder() {
    return htmlUiBuilder;
  }

  public int getMaxPageSize() {
    return maxPageSize;
  }

  protected void initApplicationContext() throws BeansException {
    Menu actionMenu = getActionMenu();
    int index = 0;
    actionMenu.addMenuItem(index++, new Menu("Search", "#",
      "document.forms['searchForm'].submit(); return false;"));
    actionMenu.addMenuItem(index++,
      new Menu("Clear Search", htmlUiBuilder.getPageUrl("list")));
    if (htmlUiBuilder.hasPageUrl("add")) {
      actionMenu.addMenuItem(index++,
        new Menu("Add", htmlUiBuilder.getPageUrl("add")));
    }
  }

  @SuppressWarnings("unchecked")
  protected ModelAndView handleRequestInternal(
    HttpServletRequest request,
    HttpServletResponse response) throws Exception {
    ElementContainer view = new ElementContainer();

    ElementContainer listContainer = new ElementContainer();

    FilterableTableView listView = htmlUiBuilder.createFilterableTableView(
      "objectList", null, "list", request.getLocale());
    listContainer.add(listView);

    Form searchForm = new Form("searchForm");
    searchForm.setMethod("get");
    searchForm.add(listContainer);
    searchForm.initialize(request);
    view.add(searchForm);

    Map<String, Object> whereClause = new LinkedHashMap<String, Object>();
    if (searchForm.isValid()) {
      Field idField = searchForm.getField("idLink");
      if (idField != null) {
        Object id = searchForm.getValue("idLink");
        Object object = null;
        try {
          object = dataAccessObject.load(Integer.valueOf(id.toString()));
        } catch (Exception e) {
        }
        if (object == null) {

          String title = htmlUiBuilder.getTitle();
          idField.addValidationError(title + " #" + id + " not found");
        } else {
          String idLinkUrl = htmlUiBuilder.getPageUrl("view",
            Collections.singletonMap("id", id));
          RedirectView redirectView = new RedirectView(idLinkUrl, true);
          return new ModelAndView(redirectView);
        }
      }
      for (String propertyName : searchForm.getFieldNames()) {
        if (!"search".equals(propertyName) && !"idLink".equals(propertyName)
          && searchForm.hasValue(propertyName)) {
          Object value = searchForm.getValue(propertyName);
          if (value instanceof String) {
            String string = (String)value;
            whereClause.put(propertyName, "%" + string + "%");
          } else {
            whereClause.put(propertyName, value);
          }
        }
      }
    }
    ResultPager pager = dataAccessObject.page(whereClause,
      Collections.singletonMap("id", true));
    try {
      List<?> results = Collections.emptyList();
      int pageSize;
      try {
        pageSize = Integer.parseInt(request.getParameter("pageSize"));
      } catch (Throwable t) {
        pageSize = defaultPageSize;
      }
      pager.setPageSize(Math.min(maxPageSize, pageSize));
      try {
        String page = request.getParameter("page");
        pager.setPageNumber(Integer.parseInt(page));
      } catch (Throwable t) {
        pager.setPageNumber(1);
      }
      results = pager.getList();

      listView.setRows(results);

      Map<String, Object> model = new HashMap<String, Object>();
      if (pager.getNumResults() > 0) {
        Map parameters = request.getParameterMap();
        ResultPagerView pagerView = new ResultPagerView(pager,
          request.getRequestURI(), parameters);
        listContainer.add(0, pagerView);
        listContainer.add(pagerView);

      }
      model.put("title", htmlUiBuilder.getPluralTitle());
      model.put("uiBuilder", htmlUiBuilder);
      MenuElement actionMenu = getActionMenuElement(request);
      model.put("view", Arrays.asList(view, actionMenu));

      return new ModelAndView("view", model);
    } finally {
      pager.close();
    }
  }

  public void setDataAccessObject(final DataAccessObject<?> dataAccessObject) {
    this.dataAccessObject = dataAccessObject;
  }

  public void setDefaultPageSize(final int defaultPageSize) {
    this.defaultPageSize = defaultPageSize;
  }

  public void setHtmlUiBuilder(final HtmlUiBuilder uiBuilder) {
    this.htmlUiBuilder = uiBuilder;
  }

  public void setMaxPageSize(final int maxPageSize) {
    this.maxPageSize = maxPageSize;
  }

}
