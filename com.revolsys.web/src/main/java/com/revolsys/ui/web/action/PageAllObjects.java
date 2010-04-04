/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.ui.web.action;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.revolsys.orm.core.DataAccessObject;
import com.revolsys.orm.core.ResultPager;
import com.revolsys.orm.core.SpringDaoFactory;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderFactory;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ResultPagerView;
import com.revolsys.ui.web.config.ActionConfig;
import com.revolsys.ui.web.exception.ActionException;
import com.revolsys.ui.web.exception.ActionInitException;

/**
 * @author paustin
 */
public class PageAllObjects extends SpringFrameworkAction {
  /** The default number of orders in a page. */
  private static final int DEFAULT_PAGE_SIZE = 10;

  /** The maximum number of orders that can be displayed. */
  private static final int MAX_PAGE_SIZE = 100;

  private static final Logger log = Logger.getLogger(PageAllObjects.class);

  private HtmlUiBuilder builder;

  private DataAccessObject dao;

  private String objectClassName;

  private Class objectClass;

  private Method pageMethod;

  public void init(final ActionConfig config) throws ActionInitException {
    super.init(config);
    objectClassName = (String)config.getParameter("objectClassName");
    try {
      objectClass = Class.forName(objectClassName);
    } catch (ClassNotFoundException e) {
      throw new ActionInitException(e);
    }
    dao = SpringDaoFactory.get(getApplicationContext(), objectClassName);
    builder = HtmlUiBuilderFactory.get(getApplicationContext(), objectClassName);
    try {
      pageMethod = dao.getClass().getMethod("pageAll", new Class[] {});
    } catch (Exception e) {
      throw new ActionInitException(e.getMessage(), e);
    }
  }

  public void process(final HttpServletRequest request,
    final HttpServletResponse response) throws ActionException, IOException {
    try {
      ResultPager pager = (ResultPager)pageMethod.invoke(dao, new Object[] {});
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
        ResultPagerView pagerView = new ResultPagerView(pager,
          request.getRequestURI(), request.getParameterMap());
        request.setAttribute("pager", pagerView);
      }
      Element listView = builder.createTableView(results, "objectList", null,
        "list", request.getLocale());
      request.setAttribute("list", listView);
    } catch (IllegalAccessException e) {
      throw new ActionException(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      throw new ActionException(cause.getMessage(), cause);
    }

  }
}
