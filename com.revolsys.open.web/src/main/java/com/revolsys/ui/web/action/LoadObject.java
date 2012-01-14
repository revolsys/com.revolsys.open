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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.revolsys.gis.data.io.DataAccessObject;
import com.revolsys.orm.core.SpringDaoFactory;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.builder.HtmlUiBuilderFactory;
import com.revolsys.ui.web.config.ActionConfig;
import com.revolsys.ui.web.exception.ActionException;
import com.revolsys.ui.web.exception.ActionInitException;
import com.revolsys.ui.web.exception.PageNotFoundException;

public class LoadObject extends SpringFrameworkAction {
  private static final Logger log = Logger.getLogger(LoadObject.class);

  private HtmlUiBuilder builder;

  private DataAccessObject dao;

  private String nextUrl;

  private String objectClassName;

  private Class objectClass;

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
  }

  public void process(final HttpServletRequest request,
    final HttpServletResponse response) throws ActionException, IOException {
    String idName = builder.getTypeName() + "Id";
    Long id = (Long)request.getAttribute(idName);
    if (id == null) {
      throw new PageNotFoundException(idName + " parameter not specified");
    } else {

      Object object = dao.load(id);
      if (object == null) {
        throw new PageNotFoundException(builder.getTitle()
          + " cannot be found with id " + id);
      } else {
        request.setAttribute(builder.getTypeName(), object);
      }

    }
  }
}
