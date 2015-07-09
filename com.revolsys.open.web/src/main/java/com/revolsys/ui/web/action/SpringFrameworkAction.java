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

import javax.servlet.ServletContext;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.revolsys.ui.web.config.ActionConfig;
import com.revolsys.ui.web.config.IafAction;
import com.revolsys.ui.web.exception.ActionInitException;

public abstract class SpringFrameworkAction extends IafAction {
  private WebApplicationContext applicationContext;

  public WebApplicationContext getApplicationContext() {
    return this.applicationContext;
  }

  @Override
  public void init(final ActionConfig actionConfig) throws ActionInitException {
    super.init(actionConfig);
    final ServletContext servletContext = actionConfig.getConfig().getServletContext();
    this.applicationContext = WebApplicationContextUtils
      .getRequiredWebApplicationContext(servletContext);
  }
}
