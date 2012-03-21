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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriTemplate;
import org.springframework.web.util.UrlPathHelper;

import com.revolsys.spring.StringTemplate;
import com.revolsys.spring.security.SpringExpressionUtil;
import com.revolsys.ui.web.controller.PathAliasController;
import com.revolsys.ui.web.exception.PageNotFoundException;
import com.revolsys.ui.web.utils.HttpRequestUtils;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.JexlUtil;
import com.revolsys.util.UrlUtil;

public class Page extends Component {
  private static final SpelExpressionParser PARSER = new SpelExpressionParser();

  private static final Logger LOG = Logger.getLogger(Page.class);

  private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

  public static String getAbsoluteUrl(final String url) {
    if (url.startsWith("/")) {
      final HttpServletRequest request = HttpRequestUtils.getHttpServletRequest();
      final String serverUrl = HttpRequestUtils.getServerUrl(request);
      final String contextPath = URL_PATH_HELPER.getOriginatingContextPath(request);
      return serverUrl + contextPath + url;
    } else {
      return url;
    }
  }

  private final List<Argument> arguments = new ArrayList<Argument>();

  private final Map<String, Argument> argumentsMap = new HashMap<String, Argument>();

  private final List<Attribute> attributes = new ArrayList<Attribute>();

  private final Map<String, Attribute> attributesMap = new HashMap<String, Attribute>();

  private Layout layout;

  private long menuId;

  private final Map<String, Menu> menus = new HashMap<String, Menu>();

  private final Map<String, Page> pages = new HashMap<String, Page>();

  private Page parent;

  private String path = "";

  private Map<String, Page> pathMap = new HashMap<String, Page>();

  private final Map<String, String> properties = new HashMap<String, String>();

  private boolean secure;

  private String title = "";

  private org.apache.commons.jexl.Expression titleExpression;

  private UriTemplate uriTemplate;

  private StringTemplate titleTemplate;

  private String permission;

  private Expression permissionExpression;

  public Page() {
  }

  public Page(final Page page) {
    super(page);
    menuId = page.menuId;
    setPath(page.path);
    setTitle(page.title);
    properties.putAll(page.properties);
    arguments.addAll(page.arguments);
    argumentsMap.putAll(page.argumentsMap);
    attributes.addAll(page.attributes);
    attributesMap.putAll(page.attributesMap);
  }

  public Page(final String name, final String title, final String path,
    final boolean secure) {
    super(name);
    setTitle(title);
    setPath(path);
    this.secure = secure;
  }

  public void addArgument(final Argument argument) {
    if (!hasArgument(argument.getName())) {
      arguments.add(argument);
      argumentsMap.put(argument.getName(), argument);
    }
    if (argument.isInheritable()) {
      for (final Page page : this.pages.values()) {
        page.addArgument(argument);
      }
    }
  }

  public void addAttribute(final Attribute attribute) {
    if (!hasArgument(attribute.getName())) {
      attributes.add(attribute);
      attributesMap.put(attribute.getName(), attribute);
    }
    if (attribute.isInheritable()) {
      for (final Page page : pages.values()) {
        page.addAttribute(attribute);
      }
    }
  }

  public void addMenu(final Menu menu) {
    menus.put(menu.getName(), menu);
  }

  public void addPage(final Page page) {
    pages.put(page.getName(), page);
    pathMap.put(page.getPath(), page);
    page.setParent(this);
    for (final Entry<String, Page> entry : page.getPathMap().entrySet()) {
      final String path = page.getPath() + entry.getKey();
      final Page childPage = entry.getValue();
      pathMap.put(path, childPage);
    }

    for (final Argument argument : arguments) {
      if (argument.isInheritable()) {
        page.addArgument(argument);
      }
    }
    for (final Attribute attribute : attributes) {
      if (attribute.isInheritable()) {
        page.addAttribute(attribute);
      }
    }
  }

  public void addProperty(final String name, final String value) {
    properties.put(name, value);
  }

  public boolean canAccess(Map<String, ? extends Object> parameters) {
    if (permissionExpression == null) {
      return true;
    } else {
      try {
        final EvaluationContext securityEvaluationContext = SpringExpressionUtil.createSecurityEvaluationContext();
        for (Entry<String, ? extends Object> entry : parameters.entrySet()) {
          String name = entry.getKey();
          Object value = entry.getValue();
          securityEvaluationContext.setVariable(name, value);
        }
        return ExpressionUtils.evaluateAsBoolean(permissionExpression,
          securityEvaluationContext);
      } catch (Throwable t) {
        LOG.error("Unable to evaluate " + permission, t);
        return false;
      }
    }
  }

  @Override
  public Object clone() {
    return new Page(this);
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof Page) {
      final Page p = (Page)o;
      if (super.equals(o)
        && p.menuId == menuId
        && p.path.equals(path)
        && (p.title == title || p.title != null && title != null
          && p.title.equals(title)) && p.properties.equals(properties)) {
        return true;
      }
    }
    return false;
  }

  public String getAbsolutePath() {
    if (parent != null) {
      return parent.getAbsolutePath() + path;
    } else {
      final WebUiContext uiContext = WebUiContext.get();
      if (uiContext != null) {
        final Config config = uiContext.getConfig();
        if (config != null) {
          final String basePath = config.getBasePath();
          if (basePath != null) {
            return basePath + path;
          }
        }
      }
    }
    return path;
  }

  public List getArguments() {
    return arguments;
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public String getExpandedTitle() {
    final Map<String, Object> parameters = Collections.<String, Object> emptyMap();
    return getTitle(parameters);
  }

  public String getFullPath() {
    if (secure) {
      return getAbsolutePath() + ".wps";
    } else {
      return getAbsolutePath();
    }
  }

  public String getFullUrl() {
    final Map<String, Object> parameters = Collections.emptyMap();
    return getFullUrl(parameters);
  }

  public String getFullUrl(final Map<String, ? extends Object> parameters) {
    final Map<String, Object> uriParameters = new HashMap<String, Object>(
      parameters);
    final HttpServletRequest request = HttpRequestUtils.getHttpServletRequest();
    if (request != null) {
      for (final Argument argument : arguments) {
        final String name = argument.getName();
        if (!uriParameters.containsKey(name)) {
          final String value = request.getParameter(name);
          if (value != null) {
            uriParameters.put(name, value);
          }
        }
      }
    }
    if (canAccess(uriParameters)) {
      final Map<String, Object> uriTemplateVariables = getUriTemplateVariables(uriParameters);
      final URI path = uriTemplate.expand(uriTemplateVariables);
      final String url = UrlUtil.getUrl(path, uriParameters);
      return getAbsoluteUrl(PathAliasController.getPath(url));
    } else {
      return null;
    }
  }

  public Layout getLayout() {
    return layout;
  }

  public Menu getMenu(final String name) {
    return menus.get(name);
  }

  public long getMenuId() {
    return menuId;
  }

  public Page getPage(final String name) {
    if (name == null) {
      return null;
    }
    try {
      if (name.startsWith("/")) {
        return WebUiContext.get().getConfig().getPage(name);
      }
      Page page = pages.get(name);
      if (page == null) {
        final Page parent = getParent();
        if (parent != null) {
          page = parent.getPage(name);
        } else {
          page = WebUiContext.get().getConfig().getPage("/" + name);
        }
      }
      return page;
    } catch (final PageNotFoundException e) {
      return null;
    }
  }

  public Page getParent() {
    return parent;
  }

  public String getPath() {
    return path;
  }

  public Map<String, Page> getPathMap() {
    return pathMap;
  }

  public String getPermission() {
    return permission;
  }

  public Expression getPermissionExpression() {
    return permissionExpression;
  }

  public String getProperty(final String name) {
    return properties.get(name);
  }

  public String getTitle() {
    if (titleExpression != null) {
      final WebUiContext context = WebUiContext.get();
      return (String)context.evaluateExpression(titleExpression);
    } else {
      return title;
    }
  }

  public String getTitle(final Map<String, Object> parameters) {
    if (titleTemplate == null) {
      return title;
    } else {
      final Map<String, Object> uriTemplateVariables = getUriTemplateVariables(parameters);
      return titleTemplate.expand(uriTemplateVariables);
    }
  }

  public Map<String, Object> getUriTemplateVariables(
    final Map<String, Object> parameters) {
    final HttpServletRequest request = HttpRequestUtils.getHttpServletRequest();
    final Map<String, String> pathVariables = HttpRequestUtils.getPathVariables();
    final Map<String, Object> uriTemplateVariables = new HashMap<String, Object>();

    for (final String name : uriTemplate.getVariableNames()) {
      Object value = parameters.remove(name);
      if (value == null) {
        if (pathVariables != null) {
          value = pathVariables.get(name);
        }
        if (value == null) {
          value = request.getParameter(name);
        }
      }
      if (value != null) {
        uriTemplateVariables.put(name, value);
      }
    }
    return uriTemplateVariables;
  }

  public boolean hasArgument(final String name) {
    return argumentsMap.containsKey(name);
  }

  public boolean hasAttribute(final String name) {
    return attributesMap.containsKey(name);
  }

  /**
   * Generate the hash code for the object.
   * 
   * @return The hashCode.
   */
  @Override
  public int hashCode() {
    return super.hashCode() + (path.hashCode() << 2);
  }

  public final boolean isSecure() {
    return secure;
  }

  public void setLayout(final Layout layout) {
    this.layout = layout;
    layout.setPage(this);
  }

  public void setMenuId(final long menuId) {
    this.menuId = menuId;
  }

  public void setParent(final Page parent) {
    this.parent = parent;
    if (parent.isSecure()) {
      secure = true;
    }
  }

  public void setPath(final String path) {
    if (path != null) {
      this.path = path;
    } else {
      this.path = "/" + CaseConverter.toLowerCamelCase(getName());
    }
    uriTemplate = new UriTemplate(this.path);
  }

  public void setPathMap(final Map<String, Page> pathMap) {
    this.pathMap = pathMap;
  }

  public void setPermission(final String permission) {
    this.permission = permission;
    if (StringUtils.hasText(permission)) {
      this.permissionExpression = PARSER.parseExpression(permission);
    } else {
      this.permissionExpression = null;
    }
  }

  public final void setSecure(final boolean secure) {
    this.secure = secure;
  }

  public void setTitle(final String title) {
    if (title != null) {
      this.title = title;
      titleTemplate = new StringTemplate(title);
      if (titleTemplate.getVariableNames().isEmpty()) {
        titleTemplate = null;
      }
      try {
        titleExpression = JexlUtil.createExpression(title);
      } catch (final Exception e) {
        LOG.error(e.getMessage(), e);
      }
    } else {
      this.title = CaseConverter.toCapitalizedWords(getName());
    }
  }
}
