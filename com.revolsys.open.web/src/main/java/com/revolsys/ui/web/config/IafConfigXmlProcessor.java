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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.record.io.format.xml.StaxReader;
import com.revolsys.record.io.format.xml.XmlProcessor;
import com.revolsys.record.io.format.xml.XmlProcessorContext;
import com.revolsys.ui.html.view.Script;
import com.revolsys.ui.html.view.Style;
import com.revolsys.ui.web.exception.PageNotFoundException;

public class IafConfigXmlProcessor extends XmlProcessor {
  private static final Logger log = LoggerFactory.getLogger(IafConfigXmlProcessor.class);

  private static final Map standardTypes = new HashMap();

  static {
    standardTypes.put("int", Integer.class);
    standardTypes.put("long", Long.class);
    standardTypes.put("boolean", Boolean.class);
    standardTypes.put("string", String.class);
  }

  private boolean child = false;

  private Config config;

  private final LinkedList pageStack = new LinkedList();

  private final ServletContext servletContext;

  public IafConfigXmlProcessor(final IafConfigXmlProcessor parent) {
    super("urn:x-revolsys-com:iaf:core:config");
    final XmlProcessorContext context = parent.getContext();
    setContext(context);
    this.servletContext = parent.servletContext;
    this.config = parent.config;
    this.child = true;
  }

  public IafConfigXmlProcessor(final XmlProcessorContext processorContext) {
    super("urn:x-revolsys-com:iaf:core:config");
    setContext(processorContext);
    this.servletContext = (ServletContext)processorContext
      .getAttribute("javax.servlet.ServletContext");
  }

  private boolean getBooleanAttribute(final StaxReader parser, final String name) {
    final String value = parser.getAttributeValue(null, name);
    if (value != null && (value.equals("yes") || value.equals("true"))) {
      return true;
    }
    return false;
  }

  private Page getCurrentPage() {
    if (this.pageStack.isEmpty()) {
      return null;
    } else {
      return (Page)this.pageStack.getLast();
    }
  }

  private Page getPageByRef(final StaxReader parser, final String pageRef) {
    Page page = null;
    if (!pageRef.equals("")) {
      try {
        Page currentPage = getCurrentPage();
        while (currentPage != null) {
          page = currentPage.getPage(pageRef);
          if (page != null) {
            currentPage = null;
          } else {
            currentPage = currentPage.getParent();
          }
        }
        if (page == null) {
          page = this.config.getPage(this.config.getBasePath() + pageRef);
        }
        return page;
      } catch (final PageNotFoundException e) {
        getContext().addError(e.getMessage(), e, parser.getLocation());
      }
    }
    return page;
  }

  public ActionConfig processAction(final StaxReader parser)
    throws XMLStreamException, IOException {
    final String type = parser.getAttributeValue(null, "type");
    final String file = parser.getAttributeValue(null, "file");
    final ActionConfig action = new ActionConfig(this.config, type);
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(parser);
      if (object instanceof Parameter) {
        action.addParameter((Parameter)object);
      }
    }
    return action;
  }

  public Area processArea(final StaxReader parser) throws XMLStreamException, IOException {
    final String name = parser.getAttributeValue(null, "name");
    final String component = parser.getAttributeValue(null, "component");
    final Area area = new Area(name, component);
    parser.skipSubTree();
    return area;
  }

  public Argument processArgument(final StaxReader parser) throws XMLStreamException, IOException {
    final String name = parser.getAttributeValue(null, "name");
    final String typePath = parser.getAttributeValue(null, "type");
    final String defaultValue = parser.getAttributeValue(null, "default");
    Class type = null;
    if (typePath != null) {
      type = (Class)standardTypes.get(typePath);
      if (type == null) {
        try {
          type = Class.forName(typePath);
        } catch (final ClassNotFoundException e) {
          log.error(e.getMessage(), e);
          throw new RuntimeException("Type " + typePath + "could not be found", e);
        }
      }
    }
    final boolean required = getBooleanAttribute(parser, "required");
    final boolean inheritable = getBooleanAttribute(parser, "inheritable");
    final Argument argument = new Argument(name, type, defaultValue, required, inheritable);
    parser.skipSubTree();
    return argument;
  }

  public Attribute processAttribute(final StaxReader parser)
    throws XMLStreamException, IOException {
    final String name = parser.getAttributeValue(null, "name");
    final String typePath = parser.getAttributeValue(null, "type");
    final String value = parser.getAttributeValue(null, "value");
    Class type = null;
    if (typePath != null) {
      type = (Class)standardTypes.get(typePath);
      if (type == null) {
        try {
          type = Class.forName(typePath);
        } catch (final ClassNotFoundException e) {
          log.error(e.getMessage(), e);
          throw new RuntimeException("Type " + typePath + "could not be found", e);
        }
      }
    }
    final String loaderName = parser.getAttributeValue(null, "loader");
    Class loader = null;
    if (loaderName != null) {
      try {
        loader = Class.forName(loaderName);
      } catch (final ClassNotFoundException e) {
        throw new RuntimeException("Loader " + loaderName + "could not be found", e);
      }
    }
    final String inheritableAttr = parser.getAttributeValue(null, "inheritable");
    boolean inheritable = false;
    if (inheritableAttr != null
      && (inheritableAttr.equals("yes") || inheritableAttr.equals("true"))) {
      inheritable = true;
    }
    final Attribute attribute = new Attribute(this.config, name, type, value, inheritable, loader);
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(parser);
      if (object instanceof Parameter) {
        attribute.addParameter((Parameter)object);
      }
    }
    attribute.init();
    return attribute;
  }

  public Component processComponent(final StaxReader parser)
    throws XMLStreamException, IOException {
    final String area = parser.getAttributeValue(null, "area");
    final String name = parser.getAttributeValue(null, "name");
    final String file = parser.getAttributeValue(null, "file");
    final Component component = new Component(area, name, file);
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(parser);
      if (object instanceof ActionConfig) {
        component.addAction((ActionConfig)object);
      } else if (object instanceof Style) {
        component.addStyle((Style)object);
      } else if (object instanceof Script) {
        component.addScript((Script)object);
      } else if (object instanceof OnLoad) {
        component.addOnLoad((OnLoad)object);
      } else if (object instanceof Field) {
        component.addField((Field)object);
      }
    }
    return component;
  }

  public ComponentInclude processComponentInclude(final StaxReader parser)
    throws XMLStreamException, IOException {
    final String name = parser.getAttributeValue(null, "name");
    final String area = parser.getAttributeValue(null, "area");
    final Component component = (Component)this.config.getComponent(name).clone();
    final ComponentInclude componentInclude = new ComponentInclude(area, component);
    parser.skipSubTree();
    return componentInclude;
  }

  public Menu processDynamicMenu(final StaxReader parser) throws XMLStreamException, IOException {
    final String name = parser.getAttributeValue(null, "name");
    String pageRef = parser.getAttributeValue(null, "pageRef");
    String title = parser.getAttributeValue(null, "title");
    final String anchor = parser.getAttributeValue(null, "anchor");
    String url = parser.getAttributeValue(null, "uri");
    final String condition = parser.getAttributeValue(null, "condition");
    final String loaderClassName = parser.getAttributeValue(null, "class");
    if (pageRef != null) {
      pageRef = pageRef.trim();
      if (!pageRef.equals("")) {
        try {
          final Page page = this.config.getPageByName(pageRef);
          url = page.getPath();
          if (title == null || title.trim().equals("")) {
            title = page.getTitle();
          }
        } catch (final PageNotFoundException e) {
          log.error(e.getMessage(), e);
          getContext().addError(e.getMessage(), e, parser.getLocation());
        }
      }
    }
    try {
      final Class loaderClass = Class.forName(loaderClassName);
      final Constructor loaderCons = loaderClass.getConstructor(new Class[] {
        Config.class
      });
      final MenuItemLoader loader = (MenuItemLoader)loaderCons.newInstance(new Object[] {
        this.config
      });
      final DynamicMenu menu = new DynamicMenu(name, title, url, anchor, condition, loader);
      while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
        final Object object = process(parser);
        if (object instanceof WebProperty) {
          final WebProperty webProperty = (WebProperty)object;
          loader.setProperty(webProperty.getName(), webProperty.getValue());
        }
      }
      return menu;
    } catch (final InvocationTargetException e) {
      final Throwable t = e.getCause();
      log.error(t.getMessage(), t);
      getContext().addError(t.getMessage(), t, parser.getLocation());
    } catch (final Exception e) {
      log.error(e.getMessage(), e);
      getContext().addError(e.getMessage(), e, parser.getLocation());
    }
    return null;
  }

  public ElementComponent processElementComponent(final StaxReader parser)
    throws XMLStreamException, IOException {
    final String area = parser.getAttributeValue(null, "area");
    final String name = parser.getAttributeValue(null, "name");
    final String attribute = parser.getAttributeValue(null, "attribute");
    final ElementComponent component = new ElementComponent(area, name, attribute);
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(parser);
      if (object instanceof ActionConfig) {
        component.addAction((ActionConfig)object);
      } else if (object instanceof Style) {
        component.addStyle((Style)object);
      } else if (object instanceof Script) {
        component.addScript((Script)object);
      } else if (object instanceof OnLoad) {
        component.addOnLoad((OnLoad)object);
      } else if (object instanceof Field) {
        component.addField((Field)object);
      }
    }
    return component;
  }

  public Field processField(final StaxReader parser) throws XMLStreamException, IOException {
    final String script = parser.getAttributeValue(null, "script");
    final Field field = new Field(script);
    parser.skipSubTree();
    return field;
  }

  public Config processIafConfig(final StaxReader parser) throws XMLStreamException, IOException {
    final String basePath = parser.getAttributeValue(null, "basePath");
    if (!this.child) {
      this.config = new Config(this.servletContext, basePath);
      WebUiContext.set(new WebUiContext(this.config, null, null, null, null));
      getContext().setAttribute("com.revolsys.iaf.core.Config", this.config);
    }
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(parser);
      if (object instanceof Page) {
        this.config.addPage((Page)object);
      } else if (object instanceof Layout) {
        this.config.addLayout((Layout)object);
      } else if (object instanceof Component) {
        this.config.addComponent((Component)object);
      } else if (object instanceof Menu) {
        this.config.addMenu((Menu)object);
      }
    }
    return this.config;
  }

  public Config processImport(final StaxReader parser) throws IOException {
    final String resource = parser.getAttributeValue(null, "resource");
    if (resource != null) {
      final URL resourceUrl = getClass().getResource(resource);
      if (resourceUrl != null) {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setXMLReporter(getContext());
        final StaxReader importParser = StaxReader.newXmlReader(factory, resourceUrl.openStream());
        importParser.skipToStartElement();
        if (importParser.getEventType() == XMLStreamConstants.START_ELEMENT) {
          new IafConfigXmlProcessor(this).process(importParser);
        }
      }
    }
    parser.skipSubTree();
    return null;
  }

  public JavaComponent processJavaComponent(final StaxReader parser)
    throws XMLStreamException, IOException {
    final String area = parser.getAttributeValue(null, "area");
    final String name = parser.getAttributeValue(null, "name");
    final String className = parser.getAttributeValue(null, "class");
    final JavaComponent component = new JavaComponent(area, name, className);
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(parser);
      if (object instanceof ActionConfig) {
        component.addAction((ActionConfig)object);
      } else if (object instanceof Style) {
        component.addStyle((Style)object);
      } else if (object instanceof Script) {
        component.addScript((Script)object);
      } else if (object instanceof OnLoad) {
        component.addOnLoad((OnLoad)object);
      } else if (object instanceof Field) {
        component.addField((Field)object);
      } else if (object instanceof WebProperty) {
        final WebProperty webProperty = (WebProperty)object;
        component.setProperty(webProperty.getName(), webProperty.getValue());
      }
    }
    return component;
  }

  public Layout processLayout(final StaxReader parser) throws XMLStreamException, IOException {
    final String areaName = parser.getAttributeValue(null, "area");
    final String name = parser.getAttributeValue(null, "name");
    final String file = parser.getAttributeValue(null, "file");
    boolean page = false;
    final String pageAttr = parser.getAttributeValue(null, "page");
    if (pageAttr != null && (pageAttr.equals("yes") || pageAttr.equals("true"))) {
      page = true;
    }
    final Layout layout = new Layout(areaName, name, file, page);
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(parser);
      if (object instanceof ActionConfig) {
        layout.addAction((ActionConfig)object);
      } else if (object instanceof Style) {
        layout.addStyle((Style)object);
      } else if (object instanceof Script) {
        layout.addScript((Script)object);
      } else if (object instanceof OnLoad) {
        layout.addOnLoad((OnLoad)object);
      } else if (object instanceof Field) {
        layout.addField((Field)object);
      } else if (object instanceof Area) {
        final Area area = (Area)object;
        layout.addArea(area);
        final String componentName = area.getComponentName();
        if (componentName != null && componentName.length() > 0) {
          final Component component = this.config.getComponent(componentName);
          layout.setComponent(area.getName(), (Component)component.clone());
        }
      }
    }
    return layout;
  }

  public LayoutInclude processLayoutInclude(final StaxReader parser)
    throws XMLStreamException, IOException {
    final String name = parser.getAttributeValue(null, "name");
    final String area = parser.getAttributeValue(null, "area");
    final Layout layout = (Layout)this.config.getLayout(name).clone();
    final LayoutInclude layoutInclude = new LayoutInclude(area, layout);
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(parser);
      if (object instanceof LayoutInclude) {
        final LayoutInclude childInclude = (LayoutInclude)object;
        layout.setComponent(childInclude.getArea(), childInclude.getLayout());
      } else if (object instanceof ComponentInclude) {
        final ComponentInclude componentInclude = (ComponentInclude)object;
        layout.setComponent(componentInclude.getArea(), componentInclude.getComponent());
      } else if (object instanceof Component) {
        final Component component = (Component)object;
        layout.setComponent(component.getArea(), component);
      }
    }
    return layoutInclude;
  }

  public Menu processMenu(final StaxReader parser) throws XMLStreamException, IOException {
    final String name = parser.getAttributeValue(null, "name");
    String pageRef = parser.getAttributeValue(null, "pageRef");
    String title = parser.getAttributeValue(null, "title");
    final String anchor = parser.getAttributeValue(null, "anchor");
    String url = parser.getAttributeValue(null, "uri");
    final String condition = parser.getAttributeValue(null, "condition");
    if (pageRef != null) {
      pageRef = pageRef.trim();
      final Page page = getPageByRef(parser, pageRef.trim());
      if (page != null) {
        url = page.getFullPath();
        if (title == null || title.trim().equals("")) {
          title = page.getTitle();
        }
      }
    }
    Menu menu = null;
    try {
      menu = new Menu(name, title, url, anchor, condition);
    } catch (final Exception e) {
      getContext().addError("Error creating menu item: " + e.getMessage(), e, parser.getLocation());
    }
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(parser);
      if (object instanceof MenuItem) {
        menu.addMenuItem((MenuItem)object);
      } else if (object instanceof Parameter) {
        try {
          menu.addParameter((Parameter)object);
        } catch (final Exception e) {
          getContext().addError("Error adding menu parameter: " + e.getMessage(), e,
            parser.getLocation());
        }
      } else if (object instanceof WebProperty) {
        menu.addProperty((WebProperty)object);
      }
    }
    return menu;
  }

  public Menu processMenuInclude(final StaxReader parser) throws XMLStreamException, IOException {
    final String refname = parser.getAttributeValue(null, "refname");
    final String name = parser.getAttributeValue(null, "name");
    Menu menu = this.config.getMenu(refname);
    if (name != null) {
      menu = (Menu)menu.clone();
      menu.setName(name);
    }

    parser.skipSubTree();
    return menu;
  }

  public MenuItem processMenuItem(final StaxReader parser) throws XMLStreamException, IOException {
    final String name = parser.getAttributeValue(null, "name");
    final String pageRef = parser.getAttributeValue(null, "pageRef");
    final String title = parser.getAttributeValue(null, "title");
    final String anchor = parser.getAttributeValue(null, "anchor");
    final String url = parser.getAttributeValue(null, "uri");
    final String condition = parser.getAttributeValue(null, "condition");
    MenuItem menuItem = null;
    try {
      if (pageRef != null) {
        menuItem = new PageRefMenuItem(name, title, pageRef.trim(), anchor, condition);
      } else {
        menuItem = new MenuItem(name, title, url, anchor, condition);
      }
    } catch (final Exception e) {
      getContext().addError("Error creating menu item: " + e.getMessage(), e, parser.getLocation());
      return null;
    }
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(parser);
      if (object instanceof Parameter) {
        try {
          menuItem.addParameter((Parameter)object);
        } catch (final Exception e) {
          getContext().addError("Error creating menu item parameter: " + e.getMessage(), e,
            parser.getLocation());
        }
      } else if (object instanceof WebProperty) {
        menuItem.addProperty((WebProperty)object);
      }
    }
    return menuItem;
  }

  public OnLoad processOnLoad(final StaxReader parser) throws XMLStreamException, IOException {
    final String script = parser.getAttributeValue(null, "script");
    final OnLoad onLoad = new OnLoad(script);
    parser.skipSubTree();
    return onLoad;
  }

  public Page processPage(final StaxReader parser) throws XMLStreamException, IOException {
    final String name = parser.getAttributeValue(null, "name");
    final String title = parser.getAttributeValue(null, "title");
    final String path = parser.getAttributeValue(null, "path");
    final boolean secure = getBooleanAttribute(parser, "secure");

    final Page page = new Page(name, title, path, secure);
    final Page parentPage = getCurrentPage();
    if (parentPage != null) {
      page.setParent(parentPage);
      this.config.addPage(page);
    }
    this.pageStack.add(page);
    while (parser.nextTag() == XMLStreamConstants.START_ELEMENT) {
      final Object object = process(parser);
      if (object instanceof ActionConfig) {
        page.addAction((ActionConfig)object);
      } else if (object instanceof Style) {
        page.addStyle((Style)object);
      } else if (object instanceof Script) {
        page.addScript((Script)object);
      } else if (object instanceof OnLoad) {
        page.addOnLoad((OnLoad)object);
      } else if (object instanceof LayoutInclude) {
        final LayoutInclude layoutInclude = (LayoutInclude)object;
        page.setLayout(layoutInclude.getLayout());
      } else if (object instanceof Page) {
        page.addPage((Page)object);
      } else if (object instanceof Menu) {
        page.addMenu((Menu)object);
      } else if (object instanceof Argument) {
        page.addArgument((Argument)object);
      } else if (object instanceof Attribute) {
        page.addField((Attribute)object);
      }
    }
    this.pageStack.removeLast();
    return page;
  }

  public Parameter processParameter(final StaxReader parser)
    throws XMLStreamException, IOException {
    final String name = parser.getAttributeValue(null, "name");
    final String value = parser.getAttributeValue(null, "value");
    final Parameter parameter = new Parameter(name, value);
    parser.skipSubTree();
    return parameter;
  }

  public WebProperty processProperty(final StaxReader parser)
    throws XMLStreamException, IOException {
    final String name = parser.getAttributeValue(null, "name");
    final String value = parser.getAttributeValue(null, "value");
    final WebProperty webProperty = new WebProperty(name, value);
    parser.skipSubTree();
    return webProperty;
  }

  public Script processScript(final StaxReader parser) throws XMLStreamException, IOException {
    final String file = parser.getAttributeValue(null, "file");
    final Script script = new Script(file);
    parser.skipSubTree();
    return script;
  }

  public Style processStyle(final StaxReader parser) throws XMLStreamException, IOException {
    final String file = parser.getAttributeValue(null, "file");
    final Style style = new Style(file);
    parser.skipSubTree();
    return style;
  }
}
