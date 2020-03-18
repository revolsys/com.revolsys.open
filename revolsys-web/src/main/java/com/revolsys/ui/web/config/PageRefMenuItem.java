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
package com.revolsys.ui.web.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.jexl3.JexlExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.ui.web.exception.PageNotFoundException;

public class PageRefMenuItem extends MenuItem {
  private static final Logger log = LoggerFactory.getLogger(PageRefMenuItem.class);

  private final String pageRef;

  public PageRefMenuItem(final String title, final String pageRef) throws Exception {
    this(title, title, pageRef);
  }

  public PageRefMenuItem(final String name, final String title, final String pageRef)
    throws Exception {
    this(name, title, pageRef, null, null);
  }

  public PageRefMenuItem(final String name, final String title, final String pageRef,
    final String anchor, final String condition) throws Exception {
    super(name, title, null, anchor, condition);
    this.pageRef = pageRef;
  }

  private Page getReferencedPage() {
    final WebUiContext context = WebUiContext.get();
    final Page currentPage = context.getPage();
    if (currentPage != null) {
      return currentPage.getPage(this.pageRef);
    } else {
      try {
        return context.getConfig().getPage(this.pageRef);
      } catch (final PageNotFoundException e) {
        return null;
      }

    }

  }

  @Override
  public String getTitle() {
    final String title = super.getTitle();
    if (title != null) {
      return title;
    } else {
      final Page referencedPage = getReferencedPage();
      if (referencedPage != null) {
        return referencedPage.getTitle();
      } else {
        return "";
      }
    }
  }

  @Override
  public String getUri() {
    final WebUiContext context = WebUiContext.get();
    final Page referencedPage = getReferencedPage();

    if (referencedPage != null) {
      final Map uriParams = new HashMap();
      if (getStaticParameters() != null) {
        uriParams.putAll(getStaticParameters());
      }
      for (final Iterator params = getParameters().entrySet().iterator(); params.hasNext();) {
        final Map.Entry param = (Map.Entry)params.next();
        final Object key = param.getKey();
        if (!uriParams.containsKey(key)) {
          final Object value = context.evaluateExpression((JexlExpression)param.getValue());
          uriParams.put(key, value);
        }
      }
      if (getAnchor() == null) {
        return referencedPage.getFullUrl(uriParams);
      } else {
        return referencedPage.getFullUrl(uriParams) + "#" + getAnchor();
      }
    } else {
      return null;
    }

  }

  @Override
  public boolean isVisible() {
    // TODO Auto-generated method stub
    return super.isVisible();
  }
}
