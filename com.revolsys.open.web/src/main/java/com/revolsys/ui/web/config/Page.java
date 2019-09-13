package com.revolsys.ui.web.config;

import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.jexl3.JexlExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.util.UriTemplate;

import com.revolsys.spring.StringTemplate;
import com.revolsys.spring.security.SpringExpressionUtil;
import com.revolsys.ui.web.exception.PageNotFoundException;
import com.revolsys.ui.web.utils.HttpServletUtils;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.JexlUtil;
import com.revolsys.util.UrlUtil;

public class Page extends Component {
  private static final Logger LOG = LoggerFactory.getLogger(Page.class);

  private final List<Argument> arguments = new ArrayList<>();

  private final Map<String, Argument> argumentsMap = new HashMap<>();

  private final List<Attribute> attributes = new ArrayList<>();

  private final Map<String, Attribute> attributesMap = new HashMap<>();

  private Layout layout;

  private long menuId;

  private final Map<String, Menu> menus = new HashMap<>();

  private final Map<String, Page> pages = new HashMap<>();

  private Page parent;

  private String path = "";

  private Map<String, Page> pathMap = new HashMap<>();

  private String permission;

  private Expression permissionExpression;

  private final Map<String, String> properties = new HashMap<>();

  private boolean secure;

  private String title = "";

  private JexlExpression titleExpression;

  private StringTemplate titleTemplate;

  private UriTemplate uriTemplate;

  public Page() {
  }

  public Page(final Page page) {
    super(page);
    this.menuId = page.menuId;
    setPath(page.path);
    setTitle(page.title);
    this.properties.putAll(page.properties);
    this.arguments.addAll(page.arguments);
    this.argumentsMap.putAll(page.argumentsMap);
    this.attributes.addAll(page.attributes);
    this.attributesMap.putAll(page.attributesMap);
  }

  public Page(final String name, final String path) {
    super(name);
    setPath(path);
  }

  public Page(final String name, final String title, final String path) {
    this(name, path);
    setTitle(title);
  }

  public Page(final String name, final String title, final String path, final boolean secure) {
    this(name, title, path);
    this.secure = secure;
  }

  public void addArgument(final Argument argument) {
    if (!hasArgument(argument.getName())) {
      this.arguments.add(argument);
      this.argumentsMap.put(argument.getName(), argument);
    }
    if (argument.isInheritable()) {
      for (final Page page : this.pages.values()) {
        page.addArgument(argument);
      }
    }
  }

  public void addField(final Attribute attribute) {
    if (!hasArgument(attribute.getName())) {
      this.attributes.add(attribute);
      this.attributesMap.put(attribute.getName(), attribute);
    }
    if (attribute.isInheritable()) {
      for (final Page page : this.pages.values()) {
        page.addField(attribute);
      }
    }
  }

  public void addMenu(final Menu menu) {
    this.menus.put(menu.getName(), menu);
  }

  public void addPage(final Page page) {
    this.pages.put(page.getName(), page);
    this.pathMap.put(page.getPath(), page);
    page.setParent(this);
    for (final Entry<String, Page> entry : page.getPathMap().entrySet()) {
      final String path = page.getPath() + entry.getKey();
      final Page childPage = entry.getValue();
      this.pathMap.put(path, childPage);
    }

    for (final Argument argument : this.arguments) {
      if (argument.isInheritable()) {
        page.addArgument(argument);
      }
    }
    for (final Attribute attribute : this.attributes) {
      if (attribute.isInheritable()) {
        page.addField(attribute);
      }
    }
  }

  public void addProperty(final String name, final String value) {
    this.properties.put(name, value);
  }

  public boolean canAccess(final Map<String, ? extends Object> parameters) {
    if (this.permissionExpression == null) {
      return true;
    } else {
      try {
        final EvaluationContext securityEvaluationContext = SpringExpressionUtil
          .newSecurityEvaluationContext();
        for (final Entry<String, ? extends Object> entry : parameters.entrySet()) {
          final String name = entry.getKey();
          final Object value = entry.getValue();
          securityEvaluationContext.setVariable(name, value);
        }
        return ExpressionUtils.evaluateAsBoolean(this.permissionExpression,
          securityEvaluationContext);
      } catch (final Throwable t) {
        LOG.error("Unable to evaluate " + this.permission, t);
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
      if (super.equals(o) && p.menuId == this.menuId && p.path.equals(this.path)
        && (p.title == this.title
          || p.title != null && this.title != null && p.title.equals(this.title))
        && p.properties.equals(this.properties)) {
        return true;
      }
    }
    return false;
  }

  public String getAbsolutePath() {
    if (this.parent != null) {
      return this.parent.getAbsolutePath() + this.path;
    } else {
      final WebUiContext uiContext = WebUiContext.get();
      if (uiContext != null) {
        final Config config = uiContext.getConfig();
        if (config != null) {
          final String basePath = config.getBasePath();
          if (basePath != null) {
            return basePath + this.path;
          }
        }
      }
    }
    return this.path;
  }

  public List<Argument> getArguments() {
    return this.arguments;
  }

  public String getExpandedTitle() {
    final Map<String, Object> parameters = Collections.<String, Object> emptyMap();
    return getTitle(parameters);
  }

  public List<Attribute> getFields() {
    return this.attributes;
  }

  public String getFullPath() {
    if (this.secure) {
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
    final Map<String, Object> uriParameters = new HashMap<>(parameters);
    final HttpServletRequest request = HttpServletUtils.getRequest();
    if (request != null) {

      for (final Argument argument : this.arguments) {
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
      try {
        final Map<String, Object> uriTemplateVariables = getUriTemplateVariables(uriParameters);
        final SecurityContext securityContext = SecurityContextHolder.getContext();
        final Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
          uriTemplateVariables.put("remoteUser", authentication.getName());
        } else if (request != null) {
          final Principal userPrincipal = request.getUserPrincipal();
          if (userPrincipal != null) {
            uriTemplateVariables.put("remoteUser", userPrincipal.getName());
          }
        }
        final URI path = this.uriTemplate.expand(uriTemplateVariables);
        final String url = UrlUtil.getUrl(path, uriParameters);
        return HttpServletUtils.getFullUrl(url);
      } catch (final IllegalArgumentException e) {
        LOG.debug("Unable to expand variables for " + this.uriTemplate, e);
        return null;
      }
    } else {
      return null;
    }
  }

  public Layout getLayout() {
    return this.layout;
  }

  public Menu getMenu(final String name) {
    return this.menus.get(name);
  }

  public long getMenuId() {
    return this.menuId;
  }

  public Page getPage(final String name) {
    if (name == null) {
      return null;
    }
    try {
      if (name.startsWith("/")) {
        return WebUiContext.get().getConfig().getPage(name);
      }
      Page page = this.pages.get(name);
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
    return this.parent;
  }

  public String getPath() {
    return this.path;
  }

  public Map<String, Page> getPathMap() {
    return this.pathMap;
  }

  public String getPermission() {
    return this.permission;
  }

  public Expression getPermissionExpression() {
    return this.permissionExpression;
  }

  public String getProperty(final String name) {
    return this.properties.get(name);
  }

  public String getTitle() {
    if (this.titleExpression != null) {
      final WebUiContext context = WebUiContext.get();
      return (String)context.evaluateExpression(this.titleExpression);
    } else {
      return this.title;
    }
  }

  public String getTitle(final Map<String, Object> parameters) {
    if (this.titleTemplate == null) {
      return this.title;
    } else {
      final Map<String, Object> uriTemplateVariables = getUriTemplateVariables(parameters);
      return this.titleTemplate.expand(uriTemplateVariables);
    }
  }

  public Map<String, Object> getUriTemplateVariables(final Map<String, Object> parameters) {
    final HttpServletRequest request = HttpServletUtils.getRequest();
    final Map<String, String> pathVariables = HttpServletUtils.getPathVariables();
    final Map<String, Object> uriTemplateVariables = new HashMap<>();

    for (final String name : this.uriTemplate.getVariableNames()) {
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
    return this.argumentsMap.containsKey(name);
  }

  public boolean hasAttribute(final String name) {
    return this.attributesMap.containsKey(name);
  }

  /**
   * Generate the hash code for the object.
   *
   * @return The hashCode.
   */
  @Override
  public int hashCode() {
    return super.hashCode() + (this.path.hashCode() << 2);
  }

  public final boolean isSecure() {
    return this.secure;
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
      this.secure = true;
    }
  }

  public void setPath(final String path) {
    if (path != null) {
      this.path = path;
    } else {
      this.path = "/" + CaseConverter.toLowerCamelCase(getName());
    }
    this.uriTemplate = new UriTemplate(this.path);
  }

  public void setPathMap(final Map<String, Page> pathMap) {
    this.pathMap = pathMap;
  }

  public void setPermission(final String permission) {
    this.permission = permission;
    if (com.revolsys.util.Property.hasValue(permission)) {
      final SpelExpressionParser parser = new SpelExpressionParser();
      this.permissionExpression = parser.parseExpression(permission);
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
      this.titleTemplate = new StringTemplate(title);
      if (this.titleTemplate.getVariableNames().isEmpty()) {
        this.titleTemplate = null;
      }
      try {
        this.titleExpression = JexlUtil.newExpression(title);
      } catch (final Exception e) {
        LOG.error(e.getMessage(), e);
      }
    } else {
      this.title = CaseConverter.toCapitalizedWords(getName());
    }
  }

  @Override
  public String toString() {
    return getName();
  }
}
