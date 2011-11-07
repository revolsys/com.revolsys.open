package com.revolsys.ui.web.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.jexl.Expression;
import org.apache.log4j.Logger;

import com.revolsys.ui.html.view.Script;
import com.revolsys.ui.html.view.Style;
import com.revolsys.ui.web.exception.ActionException;
import com.revolsys.ui.web.exception.PageNotFoundException;
import com.revolsys.util.JexlUtil;
import com.revolsys.util.UrlUtil;

public class PageController implements SiteNodeController {

  private static final Logger log = Logger.getLogger(PageController.class);

  protected List arguments = new ArrayList();

  protected Config config;

  protected long menuId;

  protected String title = "";

  protected Map properties = new HashMap();

  private Map menus = new HashMap();

  private Layout layout;

  protected Map argumentsMap = new HashMap();

  private Expression titleExpression;

  protected HashMap attributesMap = new HashMap();

  protected List attributes = new ArrayList();

  protected boolean secure;

  /** The list of actions to perform on this page. */
  private Collection actions = new ArrayList();

  /** The site node for this page. */
  private SiteNode node = new SiteNode(this);

  /** The list of client side scripts for the page. */
  private Collection scripts = new ArrayList();

  /** The list of style sheets for the page. */
  private Collection styles = new ArrayList();

  public PageController() {
  }

  public PageController(final Config config, final String name) {
    this.config = config;
  }

  public PageController(final PageController page) {
    node.setNodes(page.getNodes());
    // TODO clone
    actions.addAll(page.getActions());
    setPath(page.getPath());

    // TODO do a deep clone
    menus.putAll(page.getMenus());
    styles.addAll(page.getStyles());
    scripts.addAll(page.getScripts());
    menuId = page.menuId;
    title = page.title;
    properties.putAll(page.properties);
    arguments.addAll(page.arguments);
    argumentsMap.putAll(page.argumentsMap);
    attributes.addAll(page.attributes);
    attributesMap.putAll(page.attributesMap);
  }

  public void process(final ServletContext servletContext,
    final HttpServletRequest request, final HttpServletResponse response)
    throws IOException, ServletException {
    // WebUiContext.set(new WebUiContext(config, request.getContextPath(), this,
    // request, response, null));
    if (isSecure() && !secure) {
      response.sendRedirect(getFullUrl());
      return;
    }
    processArguments(request);
    processAttributes(request);
    request.setAttribute("niceConfig", config);

    String menuName = request.getParameter("menuName");
    request.setAttribute("menuSelected", menuName);
    request.setAttribute("title", getTitle());
    invokeActions(servletContext, request, response);

    Layout layout = getLayout();
    if (layout != null) {
      String file = layout.getFile();
      if (file != null && file.length() > 0) {
        forward(servletContext, request, response, file);
      }
    }
  }

  public void invokeActions(final ServletContext servletContext,
    final HttpServletRequest request, final HttpServletResponse response)
    throws IOException, ServletException {
    Iterator actions = getActions().iterator();
    while (actions.hasNext()) {
      Action action = (Action)actions.next();
      action.process(request, response);
    }
  }

  /**
   * @param page
   * @param request
   * @throws PageNotFoundException
   */
  private void processArguments(final HttpServletRequest request)
    throws ActionException {
    for (Iterator arguments = getArguments().iterator(); arguments.hasNext();) {
      Argument argument = (Argument)arguments.next();
      String name = argument.getName();
      Object value = null;
      String stringValue = request.getParameter(name);
      if (stringValue == null) {
        stringValue = argument.getDefault();
      }
      if (stringValue != null) {
        Class argumentType = argument.getType();
        try {
          value = argument.valueOf(stringValue);
        } catch (NumberFormatException e) {
          throw new PageNotFoundException(
            "Page argument is not a valid number: " + name);
        }
      }
      if (value != null) {
        request.setAttribute(name, value);
      } else if (argument.isRequired()) {
        throw new PageNotFoundException("Missing page argument: " + name);
      }
    }
  }

  /**
   * @param page
   * @param request
   * @throws PageNotFoundException
   */
  private void processAttributes(final HttpServletRequest request)
    throws ActionException {
    for (Iterator attributes = getAttributes().iterator(); attributes.hasNext();) {
      Attribute attribute = (Attribute)attributes.next();
      String name = attribute.getName();
      AttributeLoader loader = attribute.getLoader();
      Object value = null;
      if (loader != null) {
        value = loader.getValue(request);
      } else {
        value = attribute.getValue();
      }
      if (value != null) {
        request.setAttribute(name, value);
      }
    }
  }

  /**
   * Forward the request to the specified resource.
   * 
   * @param servletContext The servlet context.
   * @param request the parameters of the client request
   * @param response the response sent back to the client
   * @param path the path to the resource to forward to
   * @exception ServletException if there was a problem handling the request
   * @exception IOException if an input output error occurs when handling the
   *              request
   */
  public void forward(final ServletContext servletContext,
    final HttpServletRequest request, final HttpServletResponse response,
    final String path) throws ServletException, IOException {
    if (!response.isCommitted()) {
      servletContext.getRequestDispatcher(path).forward(request, response);
    }
  }

  public void addArgument(final Argument argument) {
    if (!hasArgument(argument.getName())) {
      arguments.add(argument);
      argumentsMap.put(argument.getName(), argument);
    }
    if (argument.isInheritable()) {
      // TODOhow to add this to sub pages
      // for (Iterator pages = this.pages.values().iterator(); pages.hasNext();)
      // {
      // Page page = (Page)pages.next();
      // addArgument(argument);
      // }
    }
  }

  public boolean hasArgument(final String name) {
    return argumentsMap.containsKey(name);
  }

  public void setLayout(final Layout layout) {
    this.layout = layout;
    setStyles(layout.getStyles());
    setScripts(layout.getScripts());
    // layout.setPage((Page)this);
  }

  public List getArguments() {
    return arguments;
  }

  public Layout getLayout() {
    return layout;
  }

  public void setMenuId(final long menuId) {
    this.menuId = menuId;
  }

  public long getMenuId() {
    return menuId;
  }

  public void setPath(final String path) {
    node.setPath(path);
  }

  public String getPath() {
    return node.getPath();
  }

  public String getAbsolutePath() {
    return WebUiContext.get().getConfig().getBasePath() + node.getFullPath();
  }

  public String getFullPath() {
    if (secure) {
      return getAbsolutePath() + ".wps";
    } else {
      return getAbsolutePath() + ".wp";
    }
  }

  public String getFullUrl() {
    return getFullUrl(Collections.EMPTY_MAP);
  }

  public String getFullUrl(final Map parameters) {
    WebUiContext iafContext = WebUiContext.get();
    Map uriParameters = new HashMap(parameters);
    if (iafContext != null) {
      HttpServletRequest request = iafContext.getRequest();
      if (request != null) {
        for (Iterator arguments = this.arguments.iterator(); arguments.hasNext();) {
          Argument argument = (Argument)arguments.next();
          String name = argument.getName();
          if (!uriParameters.containsKey(name)) {
            String value = request.getParameter(name);
            if (value != null) {
              uriParameters.put(name, value);
            }
          }
        }
      }
    }
    return UrlUtil.getUrl(getFullPath(), uriParameters);
  }

  public final boolean isSecure() {
    return secure;
  }

  public final void setSecure(final boolean secure) {
    this.secure = secure;
  }

  public void setTitle(final String title) {
    if (title != null) {
      this.title = title;
      try {
        titleExpression = JexlUtil.createExpression(title);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    } else {
      // this.title = CaseConverter.toCapitalizedWords(getName());
    }
  }

  public String getTitle() {
    if (titleExpression != null) {
      WebUiContext context = WebUiContext.get();
      return (String)context.evaluateExpression(titleExpression);
    } else {
      return title;
    }
  }

  public void addProperty(final String name, final String value) {
    properties.put(name, value);
  }

  public String getProperty(final String name) {
    return (String)properties.get(name);
  }

  public Object clone() {
    return new PageController(this);
  }

  public void addMenu(final com.revolsys.ui.model.Menu menu) {
    menus.put(menu.getName(), menu);
  }

  public Menu getMenu(final String name) {
    return (Menu)menus.get(name);
  }

  // TODO deal with inheritable arguments and attributes

  /**
   * @return Returns the menus.
   */
  public Map getMenus() {
    return menus;
  }

  /**
   * @param menus The menus to set.
   */
  public void setMenus(final Map menus) {
    this.menus = menus;
  }

  public Collection getMenuList() {
    return menus.values();
  }

  public void setMenuList(final Collection menus) {
    for (Iterator menuIter = menus.iterator(); menuIter.hasNext();) {
      com.revolsys.ui.model.Menu menu = (com.revolsys.ui.model.Menu)menuIter.next();
      addMenu(menu);
    }
    log.debug(this + ":" + getMenus());
  }

  public boolean equals(final Object o) {
    if (o instanceof PageController) {
      PageController p = (PageController)o;
      if (super.equals(o)
        && p.menuId == menuId
        && p.getPath().equals(getPath())
        && (p.title == title || p.title != null && title != null
          && p.title.equals(title)) && p.properties.equals(properties)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Generate the hash code for the object.
   * 
   * @return The hashCode.
   */
  public int hashCode() {
    return super.hashCode() + (getPath().hashCode() << 2);
  }

  public void addAttribute(final Attribute attribute) {
    if (!hasArgument(attribute.getName())) {
      attributes.add(attribute);
      attributesMap.put(attribute.getName(), attribute);
    }
    if (attribute.isInheritable()) {
      // TODO deal with inheritance
    }
  }

  public List getAttributes() {
    return attributes;
  }

  public boolean hasAttribute(final String name) {
    return attributesMap.containsKey(name);
  }

  /**
   * @return Returns the config.
   */
  public Config getConfig() {
    return config;
  }

  /**
   * @param config The config to set.
   */
  public void setConfig(Config config) {
    this.config = config;
  }

  /**
   * @return Returns the node.
   */
  public SiteNode getNode() {
    return node;
  }

  /**
   * @param node The node to set.
   */
  public void setNode(final SiteNode node) {
    this.node = node;
  }

  public Collection getNodes() {
    return node.getNodes();
  }

  public void setNodes(final Collection nodes) {
    node.setNodes(nodes);
  }

  /**
   * @return Returns the actions.
   */
  public Collection getActions() {
    return actions;
  }

  /**
   * @param actions The actions to set.
   */
  public void setActions(final Collection actions) {
    this.actions = actions;
  }

  /**
   * @return Returns the styles.
   */
  public Collection getStyles() {
    return styles;
  }

  /**
   * @param styles The styles to set.
   */
  public void setStyles(final Collection styles) {
    for (Iterator styleIter = styles.iterator(); styleIter.hasNext();) {
      Object element = styleIter.next();
      if (element instanceof Style) {
        Style style = (Style)element;
        this.styles.add(style);
      } else if (element instanceof String) {
        String styleUrl = (String)element;
        this.styles.add(new Style(styleUrl));
      }
    }
    log.debug(styles);
  }

  /**
   * @return Returns the scripts.
   */
  public Collection getScripts() {
    return scripts;
  }

  /**
   * @param scripts The scripts to set.
   */
  public void setScripts(final Collection scripts) {
    for (Iterator scriptItet = scripts.iterator(); scriptItet.hasNext();) {
      Object element = scriptItet.next();
      if (element instanceof Script) {
        Script script = (Script)element;
        this.scripts.add(script);
      } else if (element instanceof String) {
        String scriptUrl = (String)element;
        this.scripts.add(new Script(scriptUrl));
      }
    }
  }

//  public String toString() {
//    return title;
//  }
}
