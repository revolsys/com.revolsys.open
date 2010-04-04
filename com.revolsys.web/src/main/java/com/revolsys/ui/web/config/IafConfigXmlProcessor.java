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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;

import com.revolsys.ui.html.view.Script;
import com.revolsys.ui.html.view.Style;
import com.revolsys.ui.web.exception.PageNotFoundException;
import com.revolsys.xml.io.StaxUtils;
import com.revolsys.xml.io.XmlProcessor;
import com.revolsys.xml.io.XmlProcessorContext;

public class IafConfigXmlProcessor extends XmlProcessor {
  private static final Logger log = Logger.getLogger(IafConfigXmlProcessor.class);

  private static final Class[] PROCESS_METHOD_ARGS = new Class[] {
    XMLStreamReader.class
  };

  private ServletContext servletContext;

  private static final Map standardTypes = new HashMap();

  private boolean child = false;

  private Config config;

  private LinkedList pageStack = new LinkedList();

  public IafConfigXmlProcessor(final XmlProcessorContext processorContext) {
    super("urn:x-revolsys-com:iaf:core:config");
    setContext(processorContext);
    this.servletContext = (ServletContext)processorContext.getAttribute("javax.servlet.ServletContext");
  }

  public IafConfigXmlProcessor(final IafConfigXmlProcessor parent) {
    super("urn:x-revolsys-com:iaf:core:config");
    XmlProcessorContext context = parent.getContext();
    setContext(context);
    this.servletContext = parent.servletContext;
    this.config = parent.config;
    child = true;
  }

  static {
    standardTypes.put("int", Integer.class);
    standardTypes.put("long", Long.class);
    standardTypes.put("boolean", Boolean.class);
    standardTypes.put("string", String.class);
  }

  public Config processIafConfig(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String basePath = parser.getAttributeValue(null, "basePath");
    if (!child) {
      config = new Config(servletContext, basePath);
      WebUiContext.set(new WebUiContext(config, null, null, null, null));
      getContext().setAttribute("com.revolsys.iaf.core.Config", config);
    }
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      Object object = process(parser);
      if (object instanceof Page) {
        config.addPage((Page)object);
      } else if (object instanceof Layout) {
        config.addLayout((Layout)object);
      } else if (object instanceof Component) {
        config.addComponent((Component)object);
      } else if (object instanceof Menu) {
        config.addMenu((Menu)object);
      }
    }
    return config;
  }

  public Config processImport(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String resource = parser.getAttributeValue(null, "resource");
    if (resource != null) {
      URL resourceUrl = getClass().getResource(resource);
      if (resourceUrl != null) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setXMLReporter(getContext());
        XMLStreamReader importParser = factory.createXMLStreamReader(resourceUrl.openStream());
        StaxUtils.skipToStartElement(importParser);
        if (importParser.getEventType() == XMLStreamReader.START_ELEMENT) {
          new IafConfigXmlProcessor(this).process(importParser);
        }
      }
    }
    StaxUtils.skipSubTree(parser);
    return null;
  }

  public ActionConfig processAction(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String type = parser.getAttributeValue(null, "type");
    String file = parser.getAttributeValue(null, "file");
    ActionConfig action = new ActionConfig(config, type);
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      Object object = process(parser);
      if (object instanceof Parameter) {
        action.addParameter((Parameter)object);
      }
    }
    return action;
  }

  public Area processArea(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String name = parser.getAttributeValue(null, "name");
    String component = parser.getAttributeValue(null, "component");
    Area area = new Area(name, component);
    StaxUtils.skipSubTree(parser);
    return area;
  }

  public Argument processArgument(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String name = parser.getAttributeValue(null, "name");
    String typeName = parser.getAttributeValue(null, "type");
    String defaultValue = parser.getAttributeValue(null, "default");
    Class type = null;
    if (typeName != null) {
      type = (Class)standardTypes.get(typeName);
      if (type == null) {
        try {
          type = Class.forName(typeName);
        } catch (ClassNotFoundException e) {
          log.error(e);
          throw new RuntimeException("Type " + typeName + "could not be found",
            e);
        }
      }
    }
    boolean required = getBooleanAttribute(parser, "required");
    boolean inheritable = getBooleanAttribute(parser, "inheritable");
    Argument argument = new Argument(name, type, defaultValue, required,
      inheritable);
    StaxUtils.skipSubTree(parser);
    return argument;
  }

  private boolean getBooleanAttribute(final XMLStreamReader parser,
    final String name) {
    String value = parser.getAttributeValue(null, name);
    if (value != null && (value.equals("yes") || value.equals("true"))) {
      return true;
    }
    return false;
  }

  public Attribute processAttribute(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String name = parser.getAttributeValue(null, "name");
    String typeName = parser.getAttributeValue(null, "type");
    String value = parser.getAttributeValue(null, "value");
    Class type = null;
    if (typeName != null) {
      type = (Class)standardTypes.get(typeName);
      if (type == null) {
        try {
          type = Class.forName(typeName);
        } catch (ClassNotFoundException e) {
          log.error(e);
          throw new RuntimeException("Type " + typeName + "could not be found",
            e);
        }
      }
    }
    String loaderName = parser.getAttributeValue(null, "loader");
    Class loader = null;
    if (loaderName != null) {
      try {
        loader = Class.forName(loaderName);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("Loader " + loaderName
          + "could not be found", e);
      }
    }
    String inheritableAttr = parser.getAttributeValue(null, "inheritable");
    boolean inheritable = false;
    if (inheritableAttr != null
      && (inheritableAttr.equals("yes") || inheritableAttr.equals("true"))) {
      inheritable = true;
    }
    Attribute attribute = new Attribute(config, name, type, value, inheritable,
      loader);
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      Object object = process(parser);
      if (object instanceof Parameter) {
        attribute.addParameter((Parameter)object);
      }
    }
    attribute.init();
    return attribute;
  }

  public Component processComponent(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String area = parser.getAttributeValue(null, "area");
    String name = parser.getAttributeValue(null, "name");
    String file = parser.getAttributeValue(null, "file");
    Component component = new Component(area, name, file);
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      Object object = process(parser);
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

  public ComponentInclude processComponentInclude(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String name = parser.getAttributeValue(null, "name");
    String area = parser.getAttributeValue(null, "area");
    Component component = (Component)config.getComponent(name).clone();
    ComponentInclude componentInclude = new ComponentInclude(area, component);
    StaxUtils.skipSubTree(parser);
    return componentInclude;
  }

  public Menu processDynamicMenu(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String name = parser.getAttributeValue(null, "name");
    String pageRef = parser.getAttributeValue(null, "pageRef");
    String title = parser.getAttributeValue(null, "title");
    String anchor = parser.getAttributeValue(null, "anchor");
    String url = parser.getAttributeValue(null, "uri");
    String condition = parser.getAttributeValue(null, "condition");
    String loaderClassName = parser.getAttributeValue(null, "class");
    if (pageRef != null) {
      pageRef = pageRef.trim();
      if (!pageRef.equals("")) {
        try {
          Page page = config.getPageByName(pageRef);
          url = page.getPath();
          if (title == null || title.trim().equals("")) {
            title = page.getTitle();
          }
        } catch (PageNotFoundException e) {
          log.error(e.getMessage(), e);
          getContext().addError(e.getMessage(), e, parser.getLocation());
        }
      }
    }
    try {
      Class loaderClass = Class.forName(loaderClassName);
      Constructor loaderCons = loaderClass.getConstructor(new Class[] {
        Config.class
      });
      MenuItemLoader loader = (MenuItemLoader)loaderCons.newInstance(new Object[] {
        config
      });
      DynamicMenu menu = new DynamicMenu(name, title, url, anchor, condition,
        loader);
      while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
        Object object = process(parser);
        if (object instanceof Property) {
          Property property = (Property)object;
          loader.setProperty(property.getName(), property.getValue());
        }
      }
      return menu;
    } catch (InvocationTargetException e) {
      Throwable t = e.getCause();
      log.error(t.getMessage(), t);
      getContext().addError(t.getMessage(), t, parser.getLocation());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      getContext().addError(e.getMessage(), e, parser.getLocation());
    }
    return null;
  }

  public Field processField(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String script = parser.getAttributeValue(null, "script");
    Field field = new Field(script);
    StaxUtils.skipSubTree(parser);
    return field;
  }

  public JavaComponent processJavaComponent(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String area = parser.getAttributeValue(null, "area");
    String name = parser.getAttributeValue(null, "name");
    String className = parser.getAttributeValue(null, "class");
    JavaComponent component = new JavaComponent(area, name, className);
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      Object object = process(parser);
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
      } else if (object instanceof Property) {
        Property property = (Property)object;
        component.setProperty(property.getName(), property.getValue());
      }
    }
    return component;
  }

  public ElementComponent processElementComponent(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String area = parser.getAttributeValue(null, "area");
    String name = parser.getAttributeValue(null, "name");
    String attribute = parser.getAttributeValue(null, "attribute");
    ElementComponent component = new ElementComponent(area, name, attribute);
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      Object object = process(parser);
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

  public Layout processLayout(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String areaName = parser.getAttributeValue(null, "area");
    String name = parser.getAttributeValue(null, "name");
    String file = parser.getAttributeValue(null, "file");
    boolean page = false;
    String pageAttr = parser.getAttributeValue(null, "page");
    if (pageAttr != null && (pageAttr.equals("yes") || pageAttr.equals("true"))) {
      page = true;
    }
    Layout layout = new Layout(areaName, name, file, page);
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      Object object = process(parser);
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
        Area area = (Area)object;
        layout.addArea(area);
        String componentName = area.getComponentName();
        if (componentName != null && componentName.length() > 0) {
          Component component = config.getComponent(componentName);
          layout.setComponent(area.getName(), (Component)component.clone());
        }
      }
    }
    return layout;
  }

  public LayoutInclude processLayoutInclude(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String name = parser.getAttributeValue(null, "name");
    String area = parser.getAttributeValue(null, "area");
    Layout layout = (Layout)config.getLayout(name).clone();
    LayoutInclude layoutInclude = new LayoutInclude(area, layout);
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      Object object = process(parser);
      if (object instanceof LayoutInclude) {
        LayoutInclude childInclude = (LayoutInclude)object;
        layout.setComponent(childInclude.getArea(), childInclude.getLayout());
      } else if (object instanceof ComponentInclude) {
        ComponentInclude componentInclude = (ComponentInclude)object;
        layout.setComponent(componentInclude.getArea(),
          componentInclude.getComponent());
      } else if (object instanceof Component) {
        Component component = (Component)object;
        layout.setComponent(component.getArea(), component);
      }
    }
    return layoutInclude;
  }

  public Menu processMenu(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String name = parser.getAttributeValue(null, "name");
    String pageRef = parser.getAttributeValue(null, "pageRef");
    String title = parser.getAttributeValue(null, "title");
    String anchor = parser.getAttributeValue(null, "anchor");
    String url = parser.getAttributeValue(null, "uri");
    String condition = parser.getAttributeValue(null, "condition");
    if (pageRef != null) {
      pageRef = pageRef.trim();
      Page page = getPageByRef(parser, pageRef.trim());
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
    } catch (Exception e) {
      getContext().addError("Error creating menu item: " + e.getMessage(), e,
        parser.getLocation());
    }
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      Object object = process(parser);
      if (object instanceof MenuItem) {
        menu.addMenuItem((MenuItem)object);
      } else if (object instanceof Parameter) {
        try {
          menu.addParameter((Parameter)object);
        } catch (Exception e) {
          getContext().addError(
            "Error adding menu parameter: " + e.getMessage(), e,
            parser.getLocation());
        }
      } else if (object instanceof Property) {
        menu.addProperty((Property)object);
      }
    }
    return menu;
  }

  private Page getPageByRef(final XMLStreamReader parser, final String pageRef) {
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
          page = config.getPage(config.getBasePath() + pageRef);
        }
        return page;
      } catch (PageNotFoundException e) {
        getContext().addError(e.getMessage(), e, parser.getLocation());
      }
    }
    return page;
  }

  public MenuItem processMenuItem(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String name = parser.getAttributeValue(null, "name");
    String pageRef = parser.getAttributeValue(null, "pageRef");
    String title = parser.getAttributeValue(null, "title");
    String anchor = parser.getAttributeValue(null, "anchor");
    String url = parser.getAttributeValue(null, "uri");
    String condition = parser.getAttributeValue(null, "condition");
    MenuItem menuItem = null;
    try {
      if (pageRef != null) {
        menuItem = new PageRefMenuItem(name, title, pageRef.trim(), anchor,
          condition);
      } else {
        menuItem = new MenuItem(name, title, url, anchor, condition);
      }
    } catch (Exception e) {
      getContext().addError("Error creating menu item: " + e.getMessage(), e,
        parser.getLocation());
      return null;
    }
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      Object object = process(parser);
      if (object instanceof Parameter) {
        try {
          menuItem.addParameter((Parameter)object);
        } catch (Exception e) {
          getContext().addError(
            "Error creating menu item parameter: " + e.getMessage(), e,
            parser.getLocation());
        }
      } else if (object instanceof Property) {
        menuItem.addProperty((Property)object);
      }
    }
    return menuItem;
  }

  private Page getCurrentPage() {
    if (pageStack.isEmpty()) {
      return null;
    } else {
      return (Page)pageStack.getLast();
    }
  }

  public Menu processMenuInclude(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String refname = parser.getAttributeValue(null, "refname");
    String name = parser.getAttributeValue(null, "name");
    Menu menu = (Menu)config.getMenu(refname);
    if (name != null) {
      menu = (Menu)menu.clone();
      menu.setName(name);
    }

    StaxUtils.skipSubTree(parser);
    return menu;
  }

  public ObjectDetailComponent processObjectDetailComponent(
    final XMLStreamReader parser) throws XMLStreamException, IOException {
    String area = parser.getAttributeValue(null, "area");
    String name = parser.getAttributeValue(null, "name");
    String attribute = parser.getAttributeValue(null, "attribute");
    ObjectDetailComponent component = new ObjectDetailComponent(area, name,
      attribute);
    component.setCssClass(parser.getAttributeValue(null, "cssClass"));
    component.setKeyList(parser.getAttributeValue(null, "keyList"));

    StaxUtils.skipSubTree(parser);
    return component;
  }

  public OnLoad processOnLoad(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String script = parser.getAttributeValue(null, "script");
    OnLoad onLoad = new OnLoad(script);
    StaxUtils.skipSubTree(parser);
    return onLoad;
  }

  public Page processPage(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String name = parser.getAttributeValue(null, "name");
    String title = parser.getAttributeValue(null, "title");
    String path = parser.getAttributeValue(null, "path");
    boolean secure = getBooleanAttribute(parser, "secure");

    Page page = new Page(name, title, path, secure);
    Page parentPage = getCurrentPage();
    if (parentPage != null) {
      page.setParent(parentPage);
      config.addPage(page);
    }
    pageStack.add(page);
    while (parser.nextTag() == XMLStreamReader.START_ELEMENT) {
      Object object = process(parser);
      if (object instanceof ActionConfig) {
        page.addAction((ActionConfig)object);
      } else if (object instanceof Style) {
        page.addStyle((Style)object);
      } else if (object instanceof Script) {
        page.addScript((Script)object);
      } else if (object instanceof OnLoad) {
        page.addOnLoad((OnLoad)object);
      } else if (object instanceof LayoutInclude) {
        LayoutInclude layoutInclude = (LayoutInclude)object;
        page.setLayout(layoutInclude.getLayout());
      } else if (object instanceof Page) {
        page.addPage((Page)object);
      } else if (object instanceof Menu) {
        page.addMenu((Menu)object);
      } else if (object instanceof Argument) {
        page.addArgument((Argument)object);
      } else if (object instanceof Attribute) {
        page.addAttribute((Attribute)object);
      }
    }
    pageStack.removeLast();
    return page;
  }

  public Parameter processParameter(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String name = parser.getAttributeValue(null, "name");
    String value = parser.getAttributeValue(null, "value");
    Parameter parameter = new Parameter(name, value);
    StaxUtils.skipSubTree(parser);
    return parameter;
  }

  public Property processProperty(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String name = parser.getAttributeValue(null, "name");
    String value = parser.getAttributeValue(null, "value");
    Property property = new Property(name, value);
    StaxUtils.skipSubTree(parser);
    return property;
  }

  public Script processScript(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String file = parser.getAttributeValue(null, "file");
    Script script = new Script(file);
    StaxUtils.skipSubTree(parser);
    return script;
  }

  public Style processStyle(final XMLStreamReader parser)
    throws XMLStreamException, IOException {
    String file = parser.getAttributeValue(null, "file");
    Style style = new Style(file);
    StaxUtils.skipSubTree(parser);
    return style;
  }
}
