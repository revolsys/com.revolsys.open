package com.revolsys.ui.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;

import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.ui.web.controller.PathAliasController;
import com.revolsys.ui.web.utils.HttpServletUtils;
import com.revolsys.util.JexlUtil;
import com.revolsys.util.UrlUtil;

public class Menu extends BaseObjectWithProperties implements Cloneable, BeanNameAware {
  private static final Logger LOG = LoggerFactory.getLogger(Menu.class);

  private String anchor;

  private final Map<String, JexlExpression> dynamicParameters = new HashMap<>();

  private String iconName;

  private String imageSrc;

  private String id;

  private List<Menu> menus = new ArrayList<>();

  private String name;

  private String onClick;

  private Map<String, Object> parameters = new HashMap<>();

  private final Map<String, Object> staticParameters = new HashMap<>();

  private String target;

  private String title;

  private JexlExpression titleExpression;

  private String uri;

  private JexlExpression uriExpression;

  private boolean visible = true;

  public Menu() {
  }

  public Menu(final String title, final String uri) {
    setTitle(title);
    setUri(uri);
  }

  public Menu(final String title, final String uri, final String onClick) {
    setTitle(title);
    setUri(uri);
    this.onClick = onClick;
  }

  public void addAllMenuItems(final Menu menu) {
    addMenuItems(menu.getMenus());
  }

  public void addMenuItem(final int index, final Menu menu) {
    this.menus.add(index, menu);
  }

  public Menu addMenuItem(final Menu menu) {
    this.menus.add(menu);
    return menu;
  }

  public void addMenuItem(final String title, final String uri) {
    final Menu menu = new Menu(title, uri);
    addMenuItem(menu);
  }

  public void addMenuItems(final List<Menu> menus) {
    this.menus.addAll(menus);
  }

  public void addParameter(final Object name, final Object value) {
    if (name != null) {
      if (value != null) {
        addParameter(name.toString(), value);
      } else {
        removeParameter(name.toString());
      }
    }
  }

  public void addParameter(final String name, final Object value) {
    if (value != null) {
      this.parameters.put(name, value);
      JexlExpression expression = null;
      try {
        expression = JexlUtil.newExpression(value.toString());
      } catch (final Exception e) {
        LOG.error("Invalid Jexl Expression '" + value + "': " + e.getMessage(), e);
      }
      if (expression != null) {
        this.dynamicParameters.put(name, expression);
        this.staticParameters.remove(name);
      } else {
        this.dynamicParameters.remove(name);
        this.staticParameters.put(name, value);
      }
    } else {
      removeParameter(name);
    }
  }

  public void addParameters(final Map<String, ? extends Object> parameters) {
    for (final Entry<String, ? extends Object> parameter : parameters.entrySet()) {
      final String name = parameter.getKey();
      final Object value = parameter.getValue();
      addParameter(name, value);
    }
  }

  @Override
  public Menu clone() {
    final Menu menu = new Menu();
    menu.setAnchor(this.anchor);
    menu.addMenuItems(this.menus);
    menu.setName(this.name);
    menu.addParameters(this.parameters);
    menu.setTitle(this.title);
    menu.setUri(this.uri);
    menu.setVisible(this.visible);
    return menu;
  }

  public String getAnchor() {
    return this.anchor;
  }

  public String getCssClass() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getIconName() {
    return this.iconName;
  }

  public String getId() {
    return this.id;
  }

  public String getImageSrc() {
    return this.imageSrc;
  }

  public String getLink() {
    return getLink(null);
  }

  public String getLink(final JexlContext context) {
    String baseUri = this.uri;
    if (this.uriExpression != null) {
      if (context != null) {
        baseUri = (String)JexlUtil.evaluateExpression(context, this.uriExpression);
      } else {
        baseUri = null;
      }
    }
    if (baseUri == null) {
      if (this.anchor != null) {
        return "#" + this.anchor;
      } else {
        return null;
      }
    } else {
      baseUri = HttpServletUtils.getAbsoluteUrl(PathAliasController.getPath(baseUri));

      Map<String, Object> params;
      if (context != null) {
        params = new HashMap<>(this.staticParameters);
        for (final Entry<String, JexlExpression> param : this.dynamicParameters.entrySet()) {
          final String key = param.getKey();
          final JexlExpression expression = param.getValue();
          final Object value = JexlUtil.evaluateExpression(context, expression);
          params.put(key, value);
        }
      } else {
        params = this.staticParameters;
      }
      final String link = UrlUtil.getUrl(baseUri, params);
      if (this.anchor == null) {
        return link;
      } else {
        return link + "#" + this.anchor;
      }
    }
  }

  public String getLinkTitle() {
    return getLinkTitle(null);
  }

  public String getLinkTitle(final JexlContext context) {
    if (this.titleExpression != null) {
      if (context != null) {
        return (String)JexlUtil.evaluateExpression(context, this.titleExpression);
      } else {
        return null;
      }
    } else {
      return this.title;
    }
  }

  /**
   * @return Returns the menus.
   */
  public List<Menu> getMenus() {
    return this.menus;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return this.name;
  }

  public String getOnClick() {
    return this.onClick;
  }

  /**
   * @return Returns the parameters.
   */
  public Map<String, Object> getParameters() {
    return this.parameters;
  }

  public String getTarget() {
    return this.target;
  }

  /**
   * @return Returns the title.
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * @return Returns the uri.
   */
  public String getUri() {
    return this.uri;
  }

  public boolean isVisible() {
    // TODO Auto-generated method stub
    return this.visible;
  }

  public void removeParameter(final String name) {
    this.parameters.remove(name);
    this.dynamicParameters.remove(name);
    this.staticParameters.remove(name);
  }

  public void setAnchor(final String anchor) {
    this.anchor = anchor;
  }

  @Override
  public void setBeanName(final String name) {
    if (this.id == null) {
      this.id = name;
    }
  }

  public void setIconName(final String iconName) {
    this.iconName = iconName;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public void setImageSrc(final String imageSrc) {
    this.imageSrc = imageSrc;
  }

  /**
   * @param menus The menus to set.
   */
  public void setMenus(final List menus) {
    this.menus = menus;
  }

  /**
   * @param name The name to set.
   */
  public void setName(final String name) {
    this.name = name;
  }

  public void setOnClick(final String onClick) {
    this.onClick = onClick;
  }

  /**
   * @param parameters The parameters to set.
   */
  public void setParameters(final Map parameters) {
    for (final Iterator params = parameters.entrySet().iterator(); params.hasNext();) {
      final Map.Entry entry = (Map.Entry)params.next();
      addParameter(entry.getKey(), entry.getValue());
    }
    this.parameters = parameters;
  }

  public void setTarget(final String target) {
    this.target = target;
  }

  /**
   * @param title The title to set.
   */
  public void setTitle(final String title) {
    if (title != null) {
      this.title = title;
      try {
        this.titleExpression = JexlUtil.newExpression(this.title);
      } catch (final Exception e) {
        LOG.error("Error creating expression '" + this.title + "': " + e.getMessage(), e);
        this.titleExpression = null;
      }
    } else {
      this.title = null;
      this.titleExpression = null;
    }
  }

  /**
   * @param uri The uri to set.
   */
  public void setUri(final String uri) {
    if (uri != null) {
      this.uri = uri.replaceAll(" ", "%20");
      try {
        this.uriExpression = JexlUtil.newExpression(this.uri);
      } catch (final Exception e) {
        LOG.error("Error creating expression '" + this.uri + "': " + e.getMessage(), e);
        this.uriExpression = null;
      }
    } else {
      this.uri = null;
      this.uriExpression = null;
    }
  }

  public void setVisible(final boolean visible) {
    this.visible = visible;
  }

  @Override
  public String toString() {
    return this.title + "[" + this.uri + "]";
  }

}
