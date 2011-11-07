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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.revolsys.orm.core.DataAccessObject;
import com.revolsys.orm.core.SpringDaoFactory;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderFactory;
import com.revolsys.ui.html.form.Form;
import com.revolsys.ui.web.config.ActionConfig;
import com.revolsys.ui.web.config.Page;
import com.revolsys.ui.web.config.WebUiContext;
import com.revolsys.ui.web.exception.ActionException;
import com.revolsys.ui.web.exception.ActionInitException;
import com.revolsys.ui.web.exception.RedirectException;
import com.revolsys.util.JavaBeanUtil;

public class EditObject extends SpringFrameworkAction {
  private static final Logger log = Logger.getLogger(EditObject.class);

  private HtmlUiBuilder builder;

  private DataAccessObject<Object> dao;

  private Class objectClass;

  private Map extraObjectAttributes = new HashMap();

  private String nextPagePath;

  public void init(final ActionConfig config) throws ActionInitException {
    super.init(config);
    String objectClassName = (String)config.getParameter("objectClassName");
    try {
      objectClass = Class.forName(objectClassName);
    } catch (ClassNotFoundException e) {
      throw new ActionInitException(e);
    }
    dao = SpringDaoFactory.get(getApplicationContext(), objectClassName);
    builder = HtmlUiBuilderFactory.get(getApplicationContext(), objectClassName);
    nextPagePath = (String)config.getParameter("nextPage");
    String attributeNames = (String)config.getParameter("extraObjectAttributes");
    if (attributeNames != null) {
      String[] names = attributeNames.split(",");
      for (int i = 0; i < names.length; i++) {
        String name = names[i];
        String[] nameParts = name.split("=");
        if (nameParts.length == 1) {
          extraObjectAttributes.put(name, name);
        } else {
          extraObjectAttributes.put(nameParts[0], nameParts[1]);
        }
      }
    }
  }

  public void process(final HttpServletRequest request,
    final HttpServletResponse response) throws ActionException, IOException {
    Object object = request.getAttribute(builder.getTypeName());
    boolean addMode = object == null;
    String keyListName = "edit";
    if (addMode) {
      try {
        object = objectClass.newInstance();
      } catch (InstantiationException e) {
        throw new ActionException("Unable to instantiate " + objectClass + ":"
          + e.getMessage(), e);
      } catch (IllegalAccessException e) {
        throw new ActionException("Unable to instantiate " + objectClass + ":"
          + e.getMessage(), e);
      }
      keyListName = "add";
    }
    Form form = builder.createForm(object, "objectEdit", builder.getKeyList(
      keyListName, "form"), request.getLocale());
    form.initialize(request);
    request.setAttribute("form", form);

    if (form.isPosted()) {
      if (form.isMainFormTask() && form.isValid()) {
        for (Iterator attributes = extraObjectAttributes.entrySet().iterator(); attributes.hasNext();) {
          Map.Entry entry = (Entry)attributes.next();
          String attrubuteName = (String)entry.getValue();
          String propertyName = (String)entry.getKey();
          JavaBeanUtil.setProperty(object, propertyName,
            request.getAttribute(attrubuteName));
        }
        Long id = (Long)JavaBeanUtil.getProperty(object, "id");
        if (id == null) {
          dao.persist(object);
        } else {
          dao.merge(object);
        }
        Page currentPage = (Page)WebUiContext.get().getPage();
        Page nextPage = null;
        if (nextPagePath != null) {
          nextPage = currentPage.getPage(nextPagePath);
        }
        if (nextPage == null) {
          nextPage = currentPage.getParent();
        }
        throw new RedirectException(
          nextPage.getFullUrl(Collections.singletonMap(builder.getTypeName()
            + "Id", JavaBeanUtil.getProperty(object, "id"))));
      }
    }
  }
}
