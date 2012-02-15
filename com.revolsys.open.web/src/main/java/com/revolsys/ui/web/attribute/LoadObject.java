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
package com.revolsys.ui.web.attribute;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.revolsys.gis.data.io.DataAccessObject;
import com.revolsys.orm.core.SpringDaoFactory;
import com.revolsys.ui.web.config.Attribute;
import com.revolsys.ui.web.exception.ActionException;
import com.revolsys.ui.web.exception.PageNotFoundException;

public class LoadObject extends SpringFrameworkAttributeLoader {
  private static final Logger log = Logger.getLogger(LoadObject.class);

  private DataAccessObject<Object> dao;

  private Class<?> objectClass;

  private String idArgument;

  public Object getValue(final HttpServletRequest request)
    throws ActionException {
    final Long id = (Long)request.getAttribute(idArgument);
    if (id == null) {
      throw new PageNotFoundException(idArgument + " parameter not specified");
    } else {
      final Object object = dao.load(id);
      if (object == null) {
        throw new PageNotFoundException("Record not found with id " + id);
      } else {
        return object;
      }
    }
  }

  @Override
  public void init(final Attribute attribute) {
    super.init(attribute);
    idArgument = (String)attribute.getParameter("idArgument");
    final String objectClassName = (String)attribute.getParameter("objectClassName");
    dao = SpringDaoFactory.get(getApplicationContext(), objectClassName);
  }
}
