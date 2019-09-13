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

import org.apache.commons.jexl3.JexlExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.ui.html.view.Script;
import com.revolsys.ui.html.view.Style;
import com.revolsys.ui.web.exception.ActionException;
import com.revolsys.ui.web.exception.PageNotFoundException;
import com.revolsys.util.JexlUtil;
import com.revolsys.util.UrlUtil;

public class PageController implements SiteNodeController {

  private static final Logger log = LoggerFactory.getLogger(PageController.class);

  /** The list of actions to perform on this page. */
  private Collection actions = new ArrayList();

  protected List arguments = new ArrayList();

  protected Map argumentsMap = new HashMap();

  protected List attributes = new ArrayList();

  protected HashMap attributesMap = new HashMap();

  protected Config config;

  private Layout layout;

  protected long menuId;

  private Map menus = new HashMap();

  /** The site node for this page. */
  private SiteNode node = new SiteNode(this);

  protected Map properties = new HashMap();

  /** The list of client side scripts for the page. */
  private final Collection scripts = new ArrayList();

  protected boolean secure;

  /** The list of style sheets for the page. */
  private final Collection styles = new ArrayList();

  protected String title = "";

  private JexlExpression titleExpression;

  public PageController() {
  }

  public PageController(final Config config, final String name) {
    this.config = config;
  }

  public PageController(final PageController page) {
    this.node.setNodes(page.getNodes());
    // TODO clone
    this.actions.addAll(page.getActions());
    setPath(page.getPath());

    // TODO do a deep clone
    this.menus.putAll(page.getMenus());
    this.styles.addAll(page.getStyles());
    this.scripts.addAll(page.getScripts());
    this.menuId = page.menuId;
    this.title = page.title;
    this.properties.putAll(page.properties);
    this.arguments.addAll(page.arguments);
    this.argumentsMap.putAll(page.argumentsMap);
    this.attributes.addAll(page.attributes);
    this.attributesMap.putAll(page.attributesMap);
  }

  public void addArgument(final Argument argument) {
    if (!hasArgument(argument.getName())) {
      this.arguments.add(argument);
      this.argumentsMap.put(argument.getName(), argument);
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

  public void addField(final Attribute attribute) {
    if (!hasArgument(attribute.getName())) {
      this.attributes.add(attribute);
      this.attributesMap.put(attribute.getName(), attribute);
    }
    if (attribute.isInheritable()) {
      // TODO deal with inheritance
    }
  }

  public void addMenu(final com.revolsys.ui.model.Menu menu) {
    this.menus.put(menu.getName(), menu);
  }

  public void addProperty(final String name, final String value) {
    this.properties.put(name, value);
  }

  @Override
  public Object clone() {
    return new PageController(this);
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof PageController) {
      final PageController p = (PageController)o;
      if (super.equals(o) && p.menuId == this.menuId && p.getPath().equals(getPath())
        && (p.title == this.title
          || p.title != null && this.title != null && p.title.equals(this.title))
        && p.properties.equals(this.properties)) {
        return true;
      }
    }
    return false;
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
  public void forward(final ServletContext servletContext, final HttpServletRequest request,
    final HttpServletResponse response, final String path) throws ServletException, IOException {
    if (!response.isCommitted()) {
      servletContext.getRequestDispatcher(path).forward(request, response);
    }
  }

  public String getAbsolutePath() {
    return WebUiContext.get().getConfig().getBasePath() + this.node.getFullPath();
  }

  /**
   * @return Returns the actions.
   */
  public Collection getActions() {
    return this.actions;
  }

  public List getArguments() {
    return this.arguments;
  }

  /**
   * @return Returns the config.
   */
  public Config getConfig() {
    return this.config;
  }

  public List getFields() {
    return this.attributes;
  }

  public String getFullPath() {
    if (this.secure) {
      return getAbsolutePath() + ".wps";
    } else {
      return getAbsolutePath() + ".wp";
    }
  }

  public String getFullUrl() {
    return getFullUrl(Collections.EMPTY_MAP);
  }

  public String getFullUrl(final Map parameters) {
    final WebUiContext iafContext = WebUiContext.get();
    final Map uriParameters = new HashMap(parameters);
    if (iafContext != null) {
      final HttpServletRequest request = iafContext.getRequest();
      if (request != null) {
        for (final Iterator arguments = this.arguments.iterator(); arguments.hasNext();) {
          final Argument argument = (Argument)arguments.next();
          final String name = argument.getName();
          if (!uriParameters.containsKey(name)) {
            final String value = request.getParameter(name);
            if (value != null) {
              uriParameters.put(name, value);
            }
          }
        }
      }
    }
    return UrlUtil.getUrl(getFullPath(), uriParameters);
  }

  public Layout getLayout() {
    return this.layout;
  }

  public Menu getMenu(final String name) {
    return (Menu)this.menus.get(name);
  }

  public long getMenuId() {
    return this.menuId;
  }

  public Collection getMenuList() {
    return this.menus.values();
  }

  /**
   * @return Returns the menus.
   */
  public Map getMenus() {
    return this.menus;
  }

  /**
   * @return Returns the node.
   */
  @Override
  public SiteNode getNode() {
    return this.node;
  }

  public Collection getNodes() {
    return this.node.getNodes();
  }

  @Override
  public String getPath() {
    return this.node.getPath();
  }

  public String getProperty(final String name) {
    return (String)this.properties.get(name);
  }

  /**
   * @return Returns the scripts.
   */
  public Collection getScripts() {
    return this.scripts;
  }

  /**
   * @return Returns the styles.
   */
  public Collection getStyles() {
    return this.styles;
  }

  public String getTitle() {
    if (this.titleExpression != null) {
      final WebUiContext context = WebUiContext.get();
      return (String)context.evaluateExpression(this.titleExpression);
    } else {
      return this.title;
    }
  }

  // TODO deal with inheritable arguments and attributes

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
    return super.hashCode() + (getPath().hashCode() << 2);
  }

  public void invokeActions(final ServletContext servletContext, final HttpServletRequest request,
    final HttpServletResponse response) throws IOException, ServletException {
    final Iterator actions = getActions().iterator();
    while (actions.hasNext()) {
      final Action action = (Action)actions.next();
      action.process(request, response);
    }
  }

  public final boolean isSecure() {
    return this.secure;
  }

  @Override
  public void process(final ServletContext servletContext, final HttpServletRequest request,
    final HttpServletResponse response) throws IOException, ServletException {
    // WebUiContext.set(new WebUiContext(config, request.getContextPath(), this,
    // request, response, null));
    if (isSecure() && !this.secure) {
      response.sendRedirect(getFullUrl());
      return;
    }
    processArguments(request);
    processAttributes(request);
    request.setAttribute("niceConfig", this.config);

    final String menuName = request.getParameter("menuName");
    request.setAttribute("menuSelected", menuName);
    request.setAttribute("title", getTitle());
    invokeActions(servletContext, request, response);

    final Layout layout = getLayout();
    if (layout != null) {
      final String file = layout.getFile();
      if (file != null && file.length() > 0) {
        forward(servletContext, request, response, file);
      }
    }
  }

  /**
   * @param page
   * @param request
   * @throws PageNotFoundException
   */
  private void processArguments(final HttpServletRequest request) throws ActionException {
    for (final Iterator arguments = getArguments().iterator(); arguments.hasNext();) {
      final Argument argument = (Argument)arguments.next();
      final String name = argument.getName();
      Object value = null;
      String stringValue = request.getParameter(name);
      if (stringValue == null) {
        stringValue = argument.getDefault();
      }
      if (stringValue != null) {
        final Class argumentType = argument.getType();
        try {
          value = argument.valueOf(stringValue);
        } catch (final NumberFormatException e) {
          throw new PageNotFoundException("Page argument is not a valid number: " + name);
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
  private void processAttributes(final HttpServletRequest request) throws ActionException {
    for (final Iterator attributes = getFields().iterator(); attributes.hasNext();) {
      final Attribute attribute = (Attribute)attributes.next();
      final String name = attribute.getName();
      final AttributeLoader loader = attribute.getLoader();
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
   * @param actions The actions to set.
   */
  public void setActions(final Collection actions) {
    this.actions = actions;
  }

  /**
   * @param config The config to set.
   */
  public void setConfig(final Config config) {
    this.config = config;
  }

  public void setLayout(final Layout layout) {
    this.layout = layout;
    setStyles(layout.getStyles());
    setScripts(layout.getScripts());
    // layout.setPage((Page)this);
  }

  public void setMenuId(final long menuId) {
    this.menuId = menuId;
  }

  public void setMenuList(final Collection menus) {
    for (final Iterator menuIter = menus.iterator(); menuIter.hasNext();) {
      final com.revolsys.ui.model.Menu menu = (com.revolsys.ui.model.Menu)menuIter.next();
      addMenu(menu);
    }
    log.debug(this + ":" + getMenus());
  }

  /**
   * @param menus The menus to set.
   */
  public void setMenus(final Map menus) {
    this.menus = menus;
  }

  /**
   * @param node The node to set.
   */
  @Override
  public void setNode(final SiteNode node) {
    this.node = node;
  }

  public void setNodes(final Collection nodes) {
    this.node.setNodes(nodes);
  }

  public void setPath(final String path) {
    this.node.setPath(path);
  }

  /**
   * @param scripts The scripts to set.
   */
  public void setScripts(final Collection scripts) {
    for (final Iterator scriptItet = scripts.iterator(); scriptItet.hasNext();) {
      final Object element = scriptItet.next();
      if (element instanceof Script) {
        final Script script = (Script)element;
        this.scripts.add(script);
      } else if (element instanceof String) {
        final String scriptUrl = (String)element;
        this.scripts.add(new Script(scriptUrl));
      }
    }
  }

  public final void setSecure(final boolean secure) {
    this.secure = secure;
  }

  /**
   * @param styles The styles to set.
   */
  public void setStyles(final Collection styles) {
    for (final Iterator styleIter = styles.iterator(); styleIter.hasNext();) {
      final Object element = styleIter.next();
      if (element instanceof Style) {
        final Style style = (Style)element;
        this.styles.add(style);
      } else if (element instanceof String) {
        final String styleUrl = (String)element;
        this.styles.add(new Style(styleUrl));
      }
    }
  }

  public void setTitle(final String title) {
    if (title != null) {
      this.title = title;
      try {
        this.titleExpression = JexlUtil.newExpression(title);
      } catch (final Exception e) {
        log.error(e.getMessage(), e);
      }
    } else {
      // this.title = CaseConverter.toCapitalizedWords(getName());
    }
  }

  // public String toString() {
  // return title;
  // }
}
