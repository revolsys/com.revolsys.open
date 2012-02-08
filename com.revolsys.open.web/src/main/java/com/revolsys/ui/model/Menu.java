package com.revolsys.ui.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.JexlContext;
import org.apache.log4j.Logger;

import com.revolsys.util.JexlUtil;
import com.revolsys.util.UrlUtil;

public class Menu implements Cloneable {
  private static final Logger LOG = Logger.getLogger(Menu.class);

  private String anchor;

  private Map<String, Expression> dynamicParameters = new HashMap<String, Expression>();

  private List<Menu> menus = new ArrayList<Menu>();

  private String name;

  private String onClick;

  private Map<String, Object> parameters = new HashMap<String, Object>();

  private Map<String, Object> staticParameters = new HashMap<String, Object>();

  private String title;

  private Expression titleExpression;

  private String uri;

  private Expression uriExpression;

  private boolean visible = true;
  
  private String target;

  public Menu() {
  }

  public Menu(final String title, final String uri) {
    setTitle(title);
    setUri(uri);
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public Menu(String title, String uri, String onClick) {
    setTitle(title);
    setUri(uri);
    this.onClick = onClick;
  }

  public void addAllMenuItems(final Menu menu) {
    addMenuItems(menu.getMenus());
  }

  public void addMenuItem(final Menu menu) {
    menus.add(menu);
  }

  public void addMenuItem(final int index, final Menu menu) {
    menus.add(index, menu);
  }

  public void addMenuItem(final String title, final String uri) {
    Menu menu = new Menu(title, uri);
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
      parameters.put(name, value);
      Expression expression = null;
      try {
        expression = JexlUtil.createExpression(value.toString());
      } catch (Exception e) {
        LOG.error("Invalid Jexl Expression '" + value + "': " + e.getMessage(),
          e);
      }
      if (expression != null) {
        dynamicParameters.put(name, expression);
        staticParameters.remove(name);
      } else {
        dynamicParameters.remove(name);
        staticParameters.put(name, value);
      }
    } else {
      removeParameter(name);
    }
  }

  public void addParameters(final Map<String, ? extends Object> parameters) {
    for (Entry<String, ? extends Object> parameter : parameters.entrySet()) {
      String name = parameter.getKey();
      Object value = parameter.getValue();
      addParameter(name, value);
    }
  }

  public Menu clone() {
    Menu menu = new Menu();
    menu.setAnchor(anchor);
    menu.addMenuItems(menus);
    menu.setName(name);
    menu.addParameters(parameters);
    menu.setTitle(title);
    menu.setUri(uri);
    menu.setVisible(visible);
    return menu;
  }

  public String getCssClass() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getLink() {
    return getLink(null);
  }

  public String getAnchor() {
    return anchor;
  }

  public String getLink(final JexlContext context) {
    String baseUri = uri;
    if (uriExpression != null) {
      if (context != null) {
        baseUri = (String)JexlUtil.evaluateExpression(context, uriExpression);
      } else {
        baseUri = null;
      }
    }

    if (baseUri == null) {
      if (anchor != null) {
        return "#" + anchor;
      } else {
        return null;
      }
    } else {
      Map params;
      if (context != null) {
        params = new HashMap(staticParameters);
        for (Iterator paramIter = dynamicParameters.entrySet().iterator(); paramIter.hasNext();) {
          Map.Entry param = (Map.Entry)paramIter.next();
          Object key = param.getKey();
          Expression expression = (Expression)param.getValue();
          Object value = JexlUtil.evaluateExpression(context, expression);
          params.put(key, value);
        }
      } else {
        params = staticParameters;
      }
      String link = UrlUtil.getUrl(baseUri, params);
      if (anchor == null) {
        return link;
      } else {
        return link + "#" + anchor;
      }
    }
  }

  public String getLinkTitle() {
    return getLinkTitle(null);
  }

  public String getLinkTitle(final JexlContext context) {
    if (titleExpression != null) {
      if (context != null) {
        return (String)JexlUtil.evaluateExpression(context, titleExpression);
      } else {
        return null;
      }
    } else {
      return title;
    }
  }

  /**
   * @return Returns the menus.
   */
  public List<Menu> getMenus() {
    return menus;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }

  public String getOnClick() {
    return onClick;
  }

  /**
   * @return Returns the parameters.
   */
  public Map<String, Object> getParameters() {
    return parameters;
  }

  /**
   * @return Returns the title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * @return Returns the uri.
   */
  public String getUri() {
    return uri;
  }

  public boolean isVisible() {
    // TODO Auto-generated method stub
    return visible;
  }

  public void removeParameter(final String name) {
    parameters.remove(name);
    dynamicParameters.remove(name);
    staticParameters.remove(name);
  }

  public void setAnchor(final String anchor) {
    this.anchor = anchor;
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

  public void setOnClick(String onClick) {
    this.onClick = onClick;
  }

  /**
   * @param parameters The parameters to set.
   */
  public void setParameters(final Map parameters) {
    for (Iterator params = parameters.entrySet().iterator(); params.hasNext();) {
      Map.Entry entry = (Map.Entry)params.next();
      addParameter(entry.getKey(), entry.getValue());
    }
    this.parameters = parameters;
  }

  /**
   * @param title The title to set.
   */
  public void setTitle(final String title) {
    if (title != null) {
      this.title = title;
      try {
        titleExpression = JexlUtil.createExpression(this.title);
      } catch (Exception e) {
        LOG.error(
          "Error creating expression '" + this.title + "': " + e.getMessage(),
          e);
        titleExpression = null;
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
        uriExpression = JexlUtil.createExpression(this.uri);
      } catch (Exception e) {
        LOG.error(
          "Error creating expression '" + this.uri + "': " + e.getMessage(), e);
        uriExpression = null;
      }
    } else {
      this.uri = null;
      this.uriExpression = null;
    }
  }

  public void setVisible(final boolean visible) {
    this.visible = visible;
  }

  public String toString() {
    return title + "[" + uri + "]";
  }

}
