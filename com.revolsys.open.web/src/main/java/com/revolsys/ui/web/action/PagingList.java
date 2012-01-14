package com.revolsys.ui.web.action;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.revolsys.collection.ResultPager;
import com.revolsys.gis.data.io.DataAccessObject;
import com.revolsys.orm.core.SpringDaoFactory;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderFactory;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ResultPagerView;
import com.revolsys.ui.web.config.ActionConfig;
import com.revolsys.ui.web.exception.ActionException;
import com.revolsys.ui.web.exception.ActionInitException;

public class PagingList extends SpringFrameworkAction {
  private static final Logger log = Logger.getLogger(PagingList.class);

  /** The default number of orders in a page. */
  private static final int DEFAULT_PAGE_SIZE = 10;

  /** The maximum number of orders that can be displayed. */
  private static final int MAX_PAGE_SIZE = 100;

  private DataAccessObject dao;

  private HtmlUiBuilder builder;

  private Method pageMethod;

  private List queryArgs = new ArrayList();

  private String keyListName;

  public void init(final ActionConfig config) throws ActionInitException {
    super.init(config);
    String objectClassName = (String)config.getParameter("objectClassName");
    keyListName = (String)config.getParameter("keyList");
    if (keyListName == null) {
      keyListName = "list";
    }
    String queryName = (String)config.getParameter("queryName");
    if (queryName == null) {
      queryName = "All";
    }
    String queryArgNames = (String)config.getParameter("queryArgs");
    if (queryArgNames != null) {
      String[] argNames = queryArgNames.split(",");
      for (int i = 0; i < argNames.length; i++) {
        String argName = argNames[i];
        queryArgs.add(argName);
      }
    }

    try {
      Class objectClass = Class.forName(objectClassName);
      dao = SpringDaoFactory.get(getApplicationContext(), objectClassName);
      builder = HtmlUiBuilderFactory.get(getApplicationContext(),
        objectClassName);
      Method[] methods = dao.getClass().getMethods();
      String pageMethodName = "page" + queryName;
      for (int i = 0; pageMethod == null && i < methods.length; i++) {
        Method method = methods[i];
        if (method.getName().equals(pageMethodName)) {
          pageMethod = method;
        }
      }
      if (pageMethod == null) {
        throw new ActionInitException("Cannot find method " + pageMethodName
          + " on class " + objectClassName);
      }
    } catch (Exception e) {
      throw new ActionInitException(e.getMessage(), e);
    }
  }

  public void process(final HttpServletRequest request,
    final HttpServletResponse response) throws ActionException, IOException {
    try {
      Object[] args = new Object[queryArgs.size()];
      int i = 0;
      for (Iterator argNames = queryArgs.iterator(); argNames.hasNext();) {
        String name = (String)argNames.next();
        ActionConfig config = getConfig();
        Object value = config.getParameter(name);
        if (value == null) {
          value = request.getAttribute(name);
        }
        args[i] = value;
        i++;
      }
      ResultPager pager = (ResultPager)pageMethod.invoke(dao, args);
      int pageSize;
      try {
        pageSize = Integer.parseInt(request.getParameter("pageSize"));
      } catch (Throwable t) {
        pageSize = DEFAULT_PAGE_SIZE;
      }
      pager.setPageSize(Math.min(MAX_PAGE_SIZE, pageSize));
      try {
        String page = request.getParameter("page");
        pager.setPageNumber(Integer.parseInt(page));
      } catch (Throwable t) {
        pager.setPageNumber(1);
      }
      List results = pager.getList();
      if (pager.getNumResults() > 0) {
        final Map parameterMap = request.getParameterMap();
        ResultPagerView pagerView = new ResultPagerView(pager,
          request.getRequestURI(), parameterMap);
        request.setAttribute("pager", pagerView);
      }
      Element listView = builder.createTableView(results, "objectList", null,
        keyListName, request.getLocale());
      request.setAttribute("list", listView);
    } catch (IllegalAccessException e) {
      throw new ActionException(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      throw new ActionException(cause.getMessage(), cause);
    }

  }

}
