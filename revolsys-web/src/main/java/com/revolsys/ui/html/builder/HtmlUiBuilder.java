package com.revolsys.ui.html.builder;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.logging.Logs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import com.revolsys.collection.ResultPager;
import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.Maps;
import com.revolsys.io.Reader;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.transaction.Transaction;
import com.revolsys.ui.html.decorator.Decorator;
import com.revolsys.ui.html.decorator.FieldLabelDecorator;
import com.revolsys.ui.html.decorator.FormGroupDecorator;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.fields.LongField;
import com.revolsys.ui.html.fields.TextField;
import com.revolsys.ui.html.form.Form;
import com.revolsys.ui.html.form.HtmlUiBuilderObjectForm;
import com.revolsys.ui.html.form.UiBuilderObjectForm;
import com.revolsys.ui.html.serializer.BuilderMethodSerializer;
import com.revolsys.ui.html.serializer.BuilderSerializer;
import com.revolsys.ui.html.serializer.KeySerializerDetailSerializer;
import com.revolsys.ui.html.serializer.KeySerializerTableSerializer;
import com.revolsys.ui.html.serializer.RowsTableSerializer;
import com.revolsys.ui.html.serializer.key.KeySerializer;
import com.revolsys.ui.html.serializer.type.BooleanSerializer;
import com.revolsys.ui.html.serializer.type.DateSerializer;
import com.revolsys.ui.html.serializer.type.DateTimeSerializer;
import com.revolsys.ui.html.serializer.type.TimestampSerializer;
import com.revolsys.ui.html.serializer.type.TypeSerializer;
import com.revolsys.ui.html.view.ButtonsToolbarElement;
import com.revolsys.ui.html.view.DetailView;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.ui.html.view.ElementLabel;
import com.revolsys.ui.html.view.RawContent;
import com.revolsys.ui.html.view.Script;
import com.revolsys.ui.html.view.TabElementContainer;
import com.revolsys.ui.html.view.TableView;
import com.revolsys.ui.model.Menu;
import com.revolsys.ui.web.annotation.ColumnSortOrder;
import com.revolsys.ui.web.annotation.RequestMapping;
import com.revolsys.ui.web.config.Page;
import com.revolsys.ui.web.config.WebUiContext;
import com.revolsys.ui.web.exception.PageNotFoundException;
import com.revolsys.ui.web.rest.interceptor.MediaTypeUtil;
import com.revolsys.ui.web.utils.HttpServletUtils;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.JexlUtil;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;
import com.revolsys.util.UrlUtil;

@ResponseStatus(reason = "Access Denied", value = HttpStatus.FORBIDDEN)
public class HtmlUiBuilder<T> implements BeanFactoryAware, ServletContextAware {

  private static final Pattern LINK_KEY_PATTERN = Pattern.compile("link\\(([\\w/]+),([\\w.]+)\\)");

  private static final Pattern SUB_KEY_PATTERN = Pattern.compile("^([\\w]+)(?:\\.(.+))?");

  public static HttpServletRequest getRequest() {
    final ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder
      .getRequestAttributes();
    final HttpServletRequest request = requestAttributes.getRequest();
    return request;
  }

  protected static String getUriTemplateVariable(final String name) {
    final Map<String, String> parameters = getUriTemplateVariables();
    return parameters.get(name);
  }

  public static Map<String, String> getUriTemplateVariables() {
    final HttpServletRequest request = getRequest();
    @SuppressWarnings("unchecked")
    final Map<String, String> uriTemplateVariables = (Map<String, String>)request
      .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    if (uriTemplateVariables == null) {
      return Collections.emptyMap();
    } else {
      return uriTemplateVariables;
    }
  }

  public static boolean isDataTableCallback() {
    return HttpServletUtils.getParameter("_") != null
      && HttpServletUtils.getParameter("callback") == null;
  }

  public static boolean isDataTableCallback(final HttpServletRequest request) {
    return request.getParameter("_") != null;
  }

  public static boolean isHtmlPage(final HttpServletRequest request) {
    return MediaTypeUtil.isPreferedMediaType(request, MediaType.TEXT_HTML);
  }

  private BeanFactory beanFactory;

  private HtmlUiBuilderFactory builderFactory;

  private Map<Class<?>, TypeSerializer> classSerializers = new HashMap<>();

  private int defaultPageSize = 25;

  private Map<String, String> fieldInstructions = new HashMap<>();

  private Map<String, Decorator> fieldLabels = new HashMap<>();

  private Map<String, Element> fields = new HashMap<>();

  private String idParameterName;

  private String idPropertyName = "id";

  /** The map of key lists for list viewSerializers. */
  private Map<String, List<String>> keyLists = new HashMap<>();

  private Map<String, KeySerializer> keySerializers = new HashMap<>();

  private Map<String, String> labels = new HashMap<>();

  private final Map<String, List<List<Object>>> listSortOrder = new HashMap<>();

  private Logger log = LoggerFactory.getLogger(getClass());

  private int maxPageSize = 100;

  private Map<String, String> messages;

  private Map<String, String> nullLabels = new HashMap<>();

  private Map<String, Page> pagesByName = new HashMap<>();

  private Map<String, String> pageUrls = new HashMap<>();

  private String pluralTitle;

  private String title;

  private String typeName;

  private boolean usePathVariables = true;

  private Map<String, List<KeySerializer>> viewSerializers = new HashMap<>();

  public HtmlUiBuilder() {
    initLabels();
    initSerializers();
    initFields();
    initPages();
  }

  public HtmlUiBuilder(final String typeName, final String title) {
    this(typeName, title, title);
  }

  public HtmlUiBuilder(final String typeName, final String title, final String pluralTitle) {
    this();
    setTypeName(typeName);
    this.title = title;
    this.pluralTitle = pluralTitle;
  }

  public void addField(final Field field) {
    final String name = field.getName();
    this.fields.put(name, field);
  }

  public void addKeySerializer(final KeySerializer keySerializer) {
    this.keySerializers.put(keySerializer.getName(), keySerializer);
  }

  public void addLabel(final String key, final String label) {
    this.labels.put(key, label);
  }

  protected void addListMenuItems(final Menu menu, final String prefix) {
  }

  public void addMenuElement(final ElementContainer container, final Menu menu) {
    if (menu.getMenus().size() > 0) {
      final ButtonsToolbarElement buttonsToolbar = new ButtonsToolbarElement(menu);
      container.add(buttonsToolbar);
    }
  }

  public Menu addMenuItem(final Menu menu, final String prefix, final String pageName,
    final String linkTitle) {
    final Map<String, Object> parameters = Collections.<String, Object> emptyMap();
    return addMenuItem(menu, prefix, pageName, linkTitle, parameters);
  }

  public Menu addMenuItem(final Menu menu, final String prefix, final String pageName,
    final String linkTitle, final Map<String, Object> parameters) {
    return addMenuItem(menu, prefix, pageName, linkTitle, null, parameters);
  }

  public Menu addMenuItem(final Menu menu, final String prefix, final String pageName,
    final String linkTitle, final String target) {
    final Map<String, Object> parameters = Collections.<String, Object> emptyMap();
    return addMenuItem(menu, prefix, pageName, linkTitle, target, parameters);
  }

  public Menu addMenuItem(final Menu menu, final String prefix, final String pageName,
    final String linkTitle, final String target, final Map<String, Object> parameters) {
    final Page page = getPage(prefix, pageName);
    if (page != null) {
      final String url = page.getFullUrl(parameters);
      if (url != null) {
        final Menu menuItem = new Menu(linkTitle, url);
        menuItem.setTarget(target);
        menu.addMenuItem(menuItem);
        return menuItem;
      }
    }
    return null;
  }

  public void addMessageView(final ElementContainer view, final String messageName,
    final Map<String, Object> variables) {
    final String message = getMessage(messageName, variables);
    if (message != null) {
      view.add(new RawContent(message));
    }
  }

  /**
   * @param key
   * @param label
   */
  public void addNullLabel(final String key, final String label) {
    this.nullLabels.put(key, label);
  }

  public void addObjectViewPage(final TabElementContainer tabs, final Object object,
    final String prefix) {
    final HttpServletRequest request = getRequest();
    if (object == null) {
      throw new PageNotFoundException();
    } else {
      final String pageName = getName(prefix, "view");

      final Page page = getPage(pageName);
      if (page == null) {
        throw new PageNotFoundException("Page not found " + pageName);
      } else {
        final List<KeySerializer> serializers = getSerializers(pageName, "view");
        final Element detailView = newDetailView(object, serializers);

        setPageTitle(request, pageName);

        final Menu actionMenu = new Menu();
        final Menu editMenu = addMenuItem(actionMenu, prefix, "edit", "Edit", "_top");
        if (editMenu != null) {
          editMenu.addProperty("buttonClass", "btn-primary");
        }

        final ElementContainer view = new ElementContainer(detailView);
        addMenuElement(view, actionMenu);
        final String tabId = getTypeName() + "_" + pageName;
        final String title = getPageTitle(pageName);
        tabs.add(tabId, title, view);
      }
    }
  }

  public void addPage(final Page page) {
    final String pageName = page.getName();
    this.pagesByName.put(pageName, page);
  }

  public void addTabDataTable(final TabElementContainer container, final Object builderName,
    final String pageName, Map<String, Object> parameters) {
    final HtmlUiBuilder<?> builder = getBuilder(builderName);
    if (builder != null) {
      parameters = new HashMap<>(parameters);
      parameters.put("deferLoading", 0);
      parameters.put("tabbed", true);
      parameters.put("scrollYPercent", 1);

      final HttpServletRequest request = getRequest();
      final ElementContainer element = builder.newDataTable(request, pageName, parameters);
      if (element != null) {
        final String tabId = builder.getTypeName() + "_" + pageName;
        final String title = builder.getPageTitle(pageName);
        container.add(tabId, title, element);
      }
    }
  }

  protected Record convertRecord(final Record record) {
    return record;
  }

  @PreDestroy
  public void destroy() {
    this.beanFactory = null;
    this.builderFactory = null;
    this.classSerializers = null;
    this.fieldInstructions = null;
    this.fieldLabels = null;
    this.fields = null;
    this.idParameterName = null;
    this.idPropertyName = null;
    this.keyLists = null;
    this.keySerializers = null;
    this.labels = null;
    this.log = null;
    this.messages = null;
    this.nullLabels = null;
    this.pagesByName = null;
    this.pageUrls = null;
    this.pluralTitle = null;
    this.title = null;
    this.typeName = null;
    this.viewSerializers = null;
  }

  /**
   * Construct a new new field (or element) for the named key. The parameters from the
   * HttpRequest can be used to customise the look of the field.
   *
   * @param request The servlet request.
   * @param key The field key.
   * @return The generated field element.
   */
  public Element getAttribute(final HttpServletRequest request, final String key) {
    if (key.equals("id")) {
      return new LongField("id", false);
    } else if (this.fields.containsKey(key)) {
      final Element field = this.fields.get(key);
      return field.clone();
    } else {
      final TextField field = new TextField(key, false);
      return field;
    }
  }

  public Decorator getAttributeFormGroupLabel(final String key, final Element element) {
    final String label = getLabel(key, element);
    final String instructions = getAttributeInstruction(key);
    return new FormGroupDecorator(label, instructions);
  }

  public String getAttributeInstruction(final String key) {
    return this.fieldInstructions.get(key);
  }

  public Map<String, String> getAttributeInstructions() {
    return this.fieldInstructions;
  }

  public Decorator getAttributeLabel(final String key, final Element element) {
    final Map<String, Decorator> fieldLabels = getAttributeLabels();
    Decorator fieldLabel = fieldLabels.get(key);
    if (fieldLabel == null) {
      final String label = getLabel(key, element);
      final String instructions = getAttributeInstruction(key);
      if (element instanceof Field) {
        fieldLabel = new FieldLabelDecorator(label, instructions);
      } else {
        fieldLabel = new ElementLabel(label, instructions);
      }
      fieldLabels.put(key, fieldLabel);
    }
    return fieldLabel;
  }

  public Map<String, Decorator> getAttributeLabels() {
    return this.fieldLabels;
  }

  public Map<String, Element> getAttributes() {
    return this.fields;
  }

  /**
   * @return Returns the beanFactory.
   */
  protected final BeanFactory getBeanFactory() {
    return this.beanFactory;
  }

  /**
   * Get the HTML UI Builder for the object's class.
   *
   * @param objectClass<?> The object class.
   * @return The builder.
   */
  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public <H extends HtmlUiBuilder<?>> H getBuilder(final Class<?> objectClass) {
    if (this.builderFactory != null) {
      return this.builderFactory.get(objectClass);
    } else {
      final HtmlUiBuilder htmlUiBuilder = HtmlUiBuilderFactory.get(this.beanFactory, objectClass);
      return (H)htmlUiBuilder;
    }
  }

  /**
   * Get the HTML UI Builder for the object's class.
   *
   * @param object The object.
   * @return The builder.
   */
  @SuppressWarnings("unchecked")
  public <H extends HtmlUiBuilder<?>> H getBuilder(final Object object) {
    if (object != null) {
      if (object instanceof HtmlUiBuilder) {
        return (H)object;
      } else if (object instanceof CharSequence) {
        final String typeName = object.toString();
        return (H)getBuilder(typeName);
      } else if (object instanceof Class) {
        final Class<H> class1 = (Class<H>)object;
        @SuppressWarnings("rawtypes")
        final HtmlUiBuilder builder = getBuilder(class1);
        return (H)builder;
      } else {
        final Class<H> class1 = (Class<H>)object.getClass();
        @SuppressWarnings("rawtypes")
        final HtmlUiBuilder builder = getBuilder(class1);
        return (H)builder;
      }
    } else {
      return null;
    }
  }

  /**
   * Get the HTML UI Builder for the class.
   *
   * @param className The name of the class.
   * @return The builder.
   */
  @SuppressWarnings("unchecked")
  public <H extends HtmlUiBuilder<?>> H getBuilder(final String typeName) {
    @SuppressWarnings("rawtypes")
    final HtmlUiBuilder htmlUiBuilder = HtmlUiBuilderFactory.get(this.beanFactory, typeName);
    return (H)htmlUiBuilder;
  }

  /**
   * Get the factory used to get related HTML UI builders,
   *
   * @return The factory.
   */
  public HtmlUiBuilderFactory getBuilderFactory() {
    return this.builderFactory;
  }

  public Map<String, Boolean> getDataTableSortOrder(final List<String> columnNames,
    final HttpServletRequest request) {
    final Map<String, Boolean> sortOrder = new LinkedHashMap<>();
    for (int i = 0;; i++) {
      final String columnIndex = request.getParameter("order[" + i + "][column]");
      final String direction = request.getParameter("order[" + i + "][dir]");
      if (Property.hasValue(columnIndex) && Property.hasValue(direction)) {
        final String columnName = columnNames.get(Integer.valueOf(columnIndex));
        final Boolean sortDir = "asc".equalsIgnoreCase(direction);
        sortOrder.put(columnName, sortDir);
      } else {
        return sortOrder;
      }
    }
  }

  public int getDefaultPageSize() {
    return this.defaultPageSize;
  }

  /**
   * @return Returns the idParameterNames.
   */
  public String getIdParameterName() {
    return this.idParameterName;
  }

  public String getIdPropertyName() {
    return this.idPropertyName;
  }

  public Object getIdValue(final Object object) {
    return Property.get(object, this.idPropertyName);
  }

  /**
   * Get the key list with the specified name, or the default if not defined.
   *
   * @param name The name of the key list.
   * @return The key list.
   */
  public List<String> getKeyList(final String name) {
    return getKeyListOrDefault(this.keyLists, name, "default");
  }

  /**
   * Get the key list with the specified name, or the list for defaultName if
   * not defined.
   *
   * @param name The name of the key list.
   * @param defaultName The name of the default key list to use.
   * @return The key list.
   */
  public List<String> getKeyList(final String name, final String defaultName) {
    return getKeyListOrDefault(this.keyLists, name, defaultName);
  }

  /**
   * Get the key list with the specified name, or the default if not defined.
   *
   * @param keyLists The map of key lists.
   * @param name The name of the key list.
   * @param defaultName The name of the default key List
   * @return The key list.
   */
  private List<String> getKeyListOrDefault(final Map<String, List<String>> keyLists,
    final String name, final String defaultName) {
    List<String> keyList = keyLists.get(name);
    if (keyList == null) {
      keyList = keyLists.get(defaultName);
      if (keyList == null) {
        return Collections.emptyList();
      }
    }
    return keyList;
  }

  /**
   * Get the map of key lists.
   *
   * @return The map of key lists.
   */
  public Map<String, List<String>> getKeyLists() {
    return this.keyLists;
  }

  public KeySerializer getKeySerializer(final String name) {
    return this.keySerializers.get(name);
  }

  /**
   * @return Returns the keySerializers.
   */
  public Map<String, KeySerializer> getKeySerializers() {
    return this.keySerializers;
  }

  /**
   * <p>
   * Get the label for the key. The following process is used (in sequence) to
   * get the label for the key.
   * </p>
   * <ol>
   * <li>An explict label defined in {@link #setLabels(Map)}</li>
   * <li>The label for the propetry name portion of a sub key (e.g. For the key
   * "organization.name" the property name portion is "organization", so the
   * label would be the label for the key "organization").</li>
   * <li>The label for the link text key of a link key (e.g. For the key
   * "Link(view, id)" the link text key is "id", so the label would be the label
   * for the key "id"</li>
   * <li>The key converted to Upper Case Words.</li>
   * </ol>
   * <p>
   * After the first call for a particular key the calculated labels are cached.
   * </p>
   *
   * @param key The key.
   * @return The label.
   */
  public String getLabel(final String key) {
    final Map<String, String> labels = getLabels();
    String label = labels.get(key);
    if (label == null) {
      final Matcher linkKeyMatcher = LINK_KEY_PATTERN.matcher(key);
      if (linkKeyMatcher.find()) {
        label = getLabel(linkKeyMatcher.group(2));
      } else {
        final Matcher subKeyMatcher = SUB_KEY_PATTERN.matcher(key);
        if (subKeyMatcher.find() && subKeyMatcher.group(2) != null) {
          label = getLabel(subKeyMatcher.group(1));
        } else {
          label = CaseConverter.toCapitalizedWords(key);
        }
      }

      addLabel(key, label);
    }
    return label;
  }

  public String getLabel(final String key, final Element element) {
    final Map<String, String> labels = getLabels();
    String label = labels.get(key);
    if (label == null) {
      if (element instanceof Field) {
        final Field field = (Field)element;
        label = field.getLabel();
      }
      if (label == null) {
        return getLabel(key);
      } else {
        labels.put(key, label);
      }
    }
    return label;
  }

  public Map<String, String> getLabels() {
    return this.labels;
  }

  public Map<String, List<List<Object>>> getListSortOrder() {
    return this.listSortOrder;
  }

  public List<List<Object>> getListSortOrder(final String pageName) {
    final List<List<Object>> sortOrder = this.listSortOrder.get(pageName);
    if (sortOrder == null) {
      if (pageName.equals("list")) {
        return Collections.emptyList();
      } else {
        return getListSortOrder("list");
      }
    } else {
      return sortOrder;
    }
  }

  public int getMaxPageSize() {
    return this.maxPageSize;
  }

  public String getMessage(final String messageName) {
    return this.messages.get(messageName);
  }

  public String getMessage(final String messageName, final Map<String, Object> variables) {
    final String message = getMessage(messageName);
    if (message != null) {
      try {
        final JexlExpression expression = JexlUtil.newExpression(message);
        if (expression != null) {
          final JexlContext context = new MapContext(variables);
          return (String)expression.evaluate(context);
        }
      } catch (final Throwable e) {
        this.log.error(e.getMessage(), e);
      }
    }
    return message;
  }

  private String getName(final String prefix, final String keyListName) {
    if (Property.hasValue(prefix)) {
      return prefix + CaseConverter.toUpperFirstChar(keyListName);
    } else {
      return keyListName;
    }
  }

  private String getNullLabel(final String key) {
    return this.nullLabels.get(key);
  }

  /**
   * @return Returns the nullLabels.
   */
  public Map<String, String> getNullLabels() {
    return this.nullLabels;
  }

  public Page getPage(final String path) {
    Page linkPage = this.pagesByName.get(path);
    if (linkPage == null) {
      final WebUiContext webUiContext = WebUiContext.get();
      if (webUiContext != null) {
        final Page page = webUiContext.getPage();
        if (page != null) {
          linkPage = page.getPage(path);
        }
      }
      if (linkPage == null) {
        final String pageByName = this.pageUrls.get(path);
        if (pageByName != null) {
          linkPage = new Page(null, getPluralTitle(), pageByName, false);
        }
      }
    }
    return linkPage;
  }

  protected Page getPage(final String prefix, final String name) {
    final String pageName = getName(prefix, name);
    Page viewPage = getPage(pageName);
    if (viewPage == null) {
      viewPage = getPage(name);
    }
    return viewPage;
  }

  public Map<String, Page> getPagesByName() {
    return this.pagesByName;
  }

  public String getPageTitle(final String pageName) {
    final Page page = getPage(pageName);
    if (page == null) {
      return null;
    } else {
      final String title = page.getExpandedTitle();
      return title;
    }
  }

  public String getPageUrl(final String name) {
    final Map<String, Object> parameters = Collections.emptyMap();
    return getPageUrl(name, parameters);
  }

  public String getPageUrl(final String name, final Map<String, ? extends Object> parameters) {
    final Page page = getPage(name);
    if (page == null) {
      return null;
    } else {
      final String url = page.getFullUrl(parameters);
      return url;
    }
  }

  public String getPageUrlOld(final String name) {
    final String url = this.pageUrls.get(name);
    return url;
  }

  public Map<String, String> getPageUrls() {
    return this.pageUrls;
  }

  public String getPluralTitle() {
    return this.pluralTitle;
  }

  public Object getProperty(final Object object, final String keyName) {
    try {
      return Property.get(object, keyName);
    } catch (final Throwable e) {
      this.log.error("Unable to get property " + keyName + " for:\n" + object, e);
      return "ERROR";
    }
  }

  public ResultPager<T> getResultPager(final Map<String, Object> filter) {
    throw new UnsupportedOperationException();
  }

  protected List<KeySerializer> getSerializers(final String viewName) {
    final List<KeySerializer> serializers = this.viewSerializers.get(viewName);
    if (Property.isEmpty(serializers)) {
      final List<String> elements = getKeyList(viewName);
      if (!Property.isEmpty(elements)) {
        newView(viewName, elements);
      }
    }
    return serializers;
  }

  protected List<KeySerializer> getSerializers(final String viewName,
    final String defaultViewName) {
    List<KeySerializer> serializers = getSerializers(viewName);
    if (serializers == null) {
      serializers = getSerializers(defaultViewName);
      if (serializers == null) {
        serializers = Collections.emptyList();
      } else {
        this.viewSerializers.put(viewName, serializers);
      }
    }
    return serializers;
  }

  public String getTitle() {
    return this.title;
  }

  public String getTypeName() {
    return this.typeName;
  }

  public boolean hasPageUrl(final String pageName) {
    return this.pageUrls.containsKey(pageName);
  }

  protected void initFields() {
  }

  public void initializeForm(final HtmlUiBuilderObjectForm form, final HttpServletRequest request) {
  }

  public void initializeForm(final UiBuilderObjectForm form, final HttpServletRequest request) {
  }

  protected void initLabels() {
  }

  protected void initPages() {
    final Class<?> clazz = getClass();
    final Method[] methods = clazz.getMethods();
    for (final Method method : methods) {
      final String name = method.getName();
      final RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
      if (requestMapping != null) {
        String pageName = requestMapping.name();
        if (Property.isEmpty(pageName)) {
          pageName = name;
        }
        String title = requestMapping.title();
        if (Property.isEmpty(title)) {
          title = CaseConverter.toCapitalizedWords(pageName);
        }
        final boolean secure = requestMapping.secure();

        String path = null;
        final String[] paths = requestMapping.value();
        if (paths.length > 0) {
          path = paths[0];
        }

        final String[] fieldNames = requestMapping.fieldNames();
        if (fieldNames.length > 0) {
          newView(pageName, Arrays.asList(fieldNames));
          newKeyList(pageName, Arrays.asList(fieldNames));

          final ColumnSortOrder[] columnSortOrder = requestMapping.columnSortOrder();
          final List<List<Object>> tableSortOrder = new ArrayList<>();
          for (final ColumnSortOrder sortOrder : columnSortOrder) {
            int fieldIndex = -1;
            final String sortFieldName = sortOrder.value();
            for (int i = 0; i < fieldNames.length; i++) {
              final String fieldName = fieldNames[i];
              if (sortFieldName.equals(fieldName)) {
                fieldIndex = i;
              }
            }
            final boolean ascending = sortOrder.ascending();
            String direction;
            if (ascending) {
              direction = "asc";
            } else {
              direction = "desc";
            }
            tableSortOrder.add(Arrays.asList(fieldIndex, direction));
          }
          if (!tableSortOrder.isEmpty()) {
            setListSortOrder(pageName, tableSortOrder);
          }
        }
        final String permission = requestMapping.permission();
        final Page page = new Page(pageName, title, path, secure);
        page.setPermission(permission);
        addPage(page);
      }
    }
  }

  protected void initSerializers() {
    final Class<?> clazz = getClass();
    final Method[] methods = clazz.getMethods();
    for (final Method method : methods) {
      final String name = method.getName();
      final Class<?>[] parameterTypes = method.getParameterTypes();
      if (parameterTypes.length == 2) {
        if (parameterTypes[0] == XmlWriter.class) {
          if (parameterTypes[1] == Object.class) {
            final BuilderMethodSerializer keySerializer = new BuilderMethodSerializer(name, this,
              method);
            final String title = this.labels.get(name);
            if (title != null) {
              keySerializer.setLabel(title);
            }
            addKeySerializer(keySerializer);
          }
        }
      }
    }
    this.classSerializers.put(Date.class, new DateTimeSerializer());
    this.classSerializers.put(java.sql.Date.class, new DateSerializer());
    this.classSerializers.put(Timestamp.class, new TimestampSerializer());
    this.classSerializers.put(Boolean.class, new BooleanSerializer());
    setListSortOrder(0, true);
  }

  protected void insertObject(final T object) {
  }

  public boolean isUsePathVariables() {
    return this.usePathVariables;
  }

  public T loadObject(final Object id) {
    throw new UnsupportedOperationException();
  }

  public ElementContainer newDataTable(final HttpServletRequest request, final String pageName,
    final Map<String, ? extends Object> parameters) {
    final String pageUrl = getPageUrl(pageName);
    if (Property.hasValue(pageUrl)) {
      final Map<String, Object> params = new HashMap<>();
      params.putAll(parameters);
      params.put("ajaxSource", pageUrl.replaceAll("/+$", ".json"));
      final List<T> rows = Collections.emptyList();
      final ElementContainer table = newDataTable(request, pageName, params, rows);

      return table;
    } else {
      return null;
    }
  }

  public ElementContainer newDataTable(final HttpServletRequest request, final String pageName,
    Map<String, ? extends Object> parameters, final Collection<? extends Object> rows) {
    parameters = new HashMap<>(parameters);
    final List<KeySerializer> serializers = getSerializers(pageName, "list");
    final RowsTableSerializer model = new KeySerializerTableSerializer(serializers, rows);
    final String typeName = getTypeName();
    final TableView tableView = new TableView(model, typeName + " dataTable no-footer");
    tableView.setWidth("100%");
    final String tableId = typeName + "_" + pageName + "_table";
    tableView.setId(tableId);
    tableView.setNoRecordsMessgae(null);

    final Map<String, Object> tableParams = new LinkedHashMap<>();
    tableParams.put("stateSave", true);
    tableParams.put("autoWidth", true);
    tableParams.put("dom", Maps.getString(parameters, "dom", "frtiS"));
    final Boolean ordering = Maps.getBoolean(parameters, "ordering");
    if (ordering != null) {
      tableParams.put("ordering", ordering);
    }
    final String scrollY = (String)parameters.get("scrollY");
    if (scrollY == null) {
      tableParams.put("scrollY", "300px");
    } else {
      tableParams.put("scrollY", scrollY);
    }
    final String scrollX = (String)parameters.get("scrollX");
    if (scrollX == null) {
      tableParams.put("scrollX", "true");
    } else {
      tableParams.put("scrollX", scrollX);
    }
    tableParams.put("order", getListSortOrder(pageName));

    Boolean serverSide = (Boolean)parameters.get("serverSide");
    final String ajaxSource = (String)parameters.get("ajaxSource");
    if (Property.hasValue(ajaxSource)) {
      if (serverSide == null) {
        serverSide = true;
      }

      tableParams.put("serverSide", serverSide);
      tableParams.put("ajax", ajaxSource);
    } else if (serverSide == null) {
      serverSide = false;
    }

    tableParams.put("scroller", Collections.singletonMap("loadingIndicator", true));
    final List<Map<String, Object>> columnDefs = new ArrayList<>();
    int i = 0;
    for (final KeySerializer serializer : serializers) {
      final Map<String, Object> columnDef = new LinkedHashMap<>();
      columnDef.put("targets", Arrays.asList(i));
      columnDef.put("name", serializer.getKey());
      columnDef.put("className", serializer.getKey().replaceAll("[^A-Za-z0-9]", "_"));
      columnDef.put("title", serializer.getLabel());

      final Boolean sortable = serializer.getProperty("sortable");
      if (sortable != null) {
        columnDef.put("orderable", sortable);
      }

      final Boolean searchable = serializer.getProperty("searchable");
      if (searchable != null) {
        columnDef.put("searchable", searchable);
      }

      final Boolean visible = serializer.getProperty("visible");
      if (visible != null) {
        columnDef.put("visible", visible);
      }
      final String width = serializer.getWidth();
      if (width != null) {
        columnDef.put("width", width);
      }

      columnDefs.add(columnDef);
      i++;
    }
    if (!columnDefs.isEmpty()) {
      tableParams.put("columnDefs", columnDefs);
    }
    Number scrollYPercent = (Number)parameters.get("scrollYPercent");
    if (scrollYPercent == null) {
      if (scrollY == null) {
        scrollYPercent = 0;
      } else {
        scrollYPercent = 0.98;
      }
    }
    final Script script = new Script();
    String jsonMap = Json.toString(tableParams);
    jsonMap = jsonMap.substring(0, jsonMap.length() - 1)
      + ",\"createdRow\": function( row, data, dataIndex ) {refreshButtons(row);}"
      + ",\"initComplete\": function() {$(this).DataTable().columns.adjust();}";
    jsonMap += "}";
    final StringBuffer scriptBody = new StringBuffer();
    scriptBody.append("$(document).ready(function() {\n");
    scriptBody.append("  var tableDiv = $('#");
    scriptBody.append(tableId);
    scriptBody.append(" table');\n");
    scriptBody.append("  var table = tableDiv.dataTable(");
    scriptBody.append(jsonMap);
    scriptBody.append("\n );\n");
    scriptBody.append("  tableShowEvents(table,");
    scriptBody.append(scrollYPercent);
    scriptBody.append(");\n");
    scriptBody
      .append("$(window).bind('resize', function () {$(table).DataTable().columns.adjust();} );");
    scriptBody.append("});");
    script.setContent(scriptBody.toString());
    final ElementContainer container = new ElementContainer(tableView, script);

    final String prefix = pageName.replaceAll("[lL]ist$", "");
    final Menu actionMenu = new Menu();
    final Menu addMenuItem = addMenuItem(actionMenu, prefix, "add", "Add", "_top");
    if (addMenuItem != null) {
      addMenuItem.addProperty("buttonClass", "btn-primary");
    }
    addListMenuItems(actionMenu, prefix);
    addMenuElement(container, actionMenu);

    return container;
  }

  public Object newDataTableHandler(final HttpServletRequest request, final String pageName,
    final Callable<Collection<? extends Object>> rowsCallable) {
    final Map<String, Object> parameters = new HashMap<>();
    return newDataTableHandler(request, pageName, parameters, rowsCallable);
  }

  public Object newDataTableHandler(final HttpServletRequest request, final String pageName,
    final Collection<? extends Object> rows) {
    final Map<String, Object> parameters = new HashMap<>();
    return newDataTableHandler(request, pageName, parameters, rows);
  }

  public Object newDataTableHandler(final HttpServletRequest request, final String pageName,
    final Map<String, Object> parameters,
    final Callable<Collection<? extends Object>> rowsCallable) {
    parameters.put("serverSide", false);
    if (isDataTableCallback(request)) {
      try {
        final Collection<? extends Object> rows = rowsCallable.call();
        return newDataTableMap(request, rows, pageName);
      } catch (final Exception e) {
        throw new RuntimeException("Unable to get rows", e);
      }
    } else {
      final TabElementContainer tabs = new TabElementContainer();
      addTabDataTable(tabs, this, pageName, parameters);
      return tabs;
    }
  }

  public Object newDataTableHandler(final HttpServletRequest request, final String pageName,
    final Map<String, Object> parameters, final Collection<? extends Object> rows) {
    if (isDataTableCallback(request)) {
      return newDataTableMap(request, rows, pageName);
    } else {
      parameters.put("serverSide", false);
      final TabElementContainer tabs = new TabElementContainer();
      addTabDataTable(tabs, this, pageName, parameters);
      return tabs;
    }
  }

  public Object newDataTableHandler(final String pageName,
    final Collection<? extends Object> rows) {
    final HttpServletRequest request = HttpServletUtils.getRequest();
    return newDataTableHandler(request, pageName, rows);
  }

  public Object newDataTableHandlerOrRedirect(final HttpServletRequest request,
    final HttpServletResponse response, final String pageName,
    final Collection<? extends Object> rows, final Object parentBuilder,
    final String parentPageName) {
    if (isDataTableCallback(request)) {
      try {
        return newDataTableMap(request, rows, pageName);
      } catch (final Exception e) {
        throw new RuntimeException("Unable to get rows", e);
      }
    } else {
      return redirectToTab(parentBuilder, parentPageName, pageName);
    }
  }

  public Object newDataTableHandlerOrRedirect(final HttpServletRequest request,
    final String pageName, final Callable<Collection<? extends Object>> rowsCallable,
    final Object parentBuilder, final String parentPageName) {
    if (isDataTableCallback(request)) {
      try {
        final Collection<? extends Object> rows = rowsCallable.call();
        return newDataTableMap(request, rows, pageName);
      } catch (final Exception e) {
        return Exceptions.throwUncheckedException(e);
      }
    } else {
      return redirectToTab(parentBuilder, parentPageName, pageName);
    }
  }

  public Map<String, Object> newDataTableMap(final Collection<? extends Object> records,
    final String pageName) {
    final HttpServletRequest request = HttpServletUtils.getRequest();
    return newDataTableMap(request, records, pageName);
  }

  public Map<String, Object> newDataTableMap(final HttpServletRequest request,
    final Collection<? extends Object> records, final String pageName) {
    final List<KeySerializer> serializers = getSerializers(pageName, "list");

    final List<List<String>> rows = new ArrayList<>();
    for (final Object object : records) {
      final List<String> row = new ArrayList<>();
      for (final KeySerializer serializer : serializers) {
        final String html = serializer.toString(object);
        row.add(html);
      }
      rows.add(row);
    }

    final Map<String, Object> response = new LinkedHashMap<>();
    final int recordCount = rows.size();
    response.put("draw", HttpServletUtils.getIntegerParameter(request, "draw"));
    response.put("recordsTotal", recordCount);
    response.put("recordsFiltered", recordCount);
    response.put("data", rows);
    return response;
  }

  public Map<String, Object> newDataTableMap(final HttpServletRequest request,
    final RecordStore recordStore, Query query, final String pageName) {
    final Map<String, Object> response = new LinkedHashMap<>();
    try {
      final int recordCount = recordStore.getRecordCount(query);
      final List<List<String>> rows = new ArrayList<>();
      if (recordCount > 0) {
        int recordCountMax = 50;
        final String lengthString = request.getParameter("length");
        if (Property.hasValue(lengthString)) {
          if (!"NAN".equalsIgnoreCase(lengthString)) {
            try {
              recordCountMax = Integer.valueOf(lengthString);
            } catch (final Throwable e) {
            }
          }
        }

        final int offset = HttpServletUtils.getIntegerParameter(request, "start");
        query = query.clone();
        query.setOffset(offset);
        query.setLimit(recordCountMax);

        final List<KeySerializer> serializers = getSerializers(pageName, "list");

        try (
          Reader<Record> reader = recordStore.getRecords(query)) {
          for (Record record : reader) {
            record = convertRecord(record);
            final List<String> row = new ArrayList<>();
            for (final KeySerializer serializer : serializers) {
              final String html = serializer.toString(record);
              row.add(html);
            }
            rows.add(row);
          }
        }
      }
      response.put("draw", HttpServletUtils.getIntegerParameter(request, "draw"));
      response.put("recordsTotal", recordCount);
      response.put("recordsFiltered", recordCount);
      response.put("data", rows);
    } catch (final Throwable e) {
      Logs.error(this, "Error executing query: " + query, e);
      response.put("error", "Error executing query");
    }
    return response;
  }

  public ElementContainer newDetailView(final Object object,
    final List<KeySerializer> serializers) {
    final KeySerializerDetailSerializer model = new KeySerializerDetailSerializer(serializers);
    model.setObject(object);
    final DetailView detailView = new DetailView(model, this.typeName);
    return new ElementContainer(detailView);
  }

  /**
   * Construct a new form for the object using the specified list of fields keys. The
   * form is created without the form title.
   *
   * @param <T> The type of form to return.
   * @param object The object to create the form for.
   * @param keyListName The name of the list of keys for the fields to include
   * on the form.
   * @return The generated form.
   */
  @SuppressWarnings("unchecked")
  public <F extends Form> F newForm(final Object object, final String keyListName) {
    final HtmlUiBuilderObjectForm form = newForm(object, getTypeName(), getKeyList(keyListName));
    return (F)form;
  }

  @SuppressWarnings("unchecked")
  public <F extends Form> F newForm(final Object object, final String formName,
    final List<String> keys) {
    final HtmlUiBuilderObjectForm form = new HtmlUiBuilderObjectForm(object, this, formName, keys);
    return (F)form;
  }

  @SuppressWarnings("unchecked")
  public <F extends Form> F newForm(final Object object, final String formName,
    final String keyList) {
    final List<String> keys = getKeyList(keyList);

    return (F)newForm(object, formName, keys);
  }

  protected void newKeyList(final String name, final List<String> fieldNames) {
    this.keyLists.put(name, Lists.toArray(fieldNames));
  }

  protected T newObject() {
    return null;
  }

  public Element newObjectAddPage(final Map<String, Object> defaultValues, final String prefix,
    final String preInsertMethod) throws IOException, ServletException {
    final T object = newObject();
    final HttpServletRequest request = HttpServletUtils.getRequest();
    Property.set(object, defaultValues);

    // if (!canAddObject(request)) {
    // response.sendError(HttpServletResponse.SC_FORBIDDEN,
    // "No permission to edit " + getTypeName() + " #" + getId());
    // return null;
    // }
    final Map<String, Object> parameters = new HashMap<>();

    final String pageName = getName(prefix, "add");
    final Set<String> parameterNamesToSave = new HashSet<>();

    final Form form = newTableForm(object, pageName);
    for (final String param : parameterNamesToSave) {
      form.addSavedParameter(param, request.getParameter(param));
    }
    form.initialize(request);

    if (form.isPosted() && form.isMainFormTask()) {
      if (form.isValid()) {
        if ((Boolean)JavaBeanUtil.method(this, preInsertMethod, form, object)) {
          insertObject(object);
          parameters.put("message", "Saved");
          final Object id = Property.get(object, getIdPropertyName());
          parameters.put(getIdParameterName(), id);

          postInsert(object);
          final String viewName = getName(prefix, "view");
          final String url = getPageUrl(viewName, parameters);
          redirectAfterCommit(url);
          return new TabElementContainer();
        }
      }
    }

    final Page page = getPage(pageName);
    final String title = page.getExpandedTitle();
    request.setAttribute("title", title);

    final Menu actionMenu = new Menu();
    addMenuItem(actionMenu, prefix, "list", "Cancel", "_top").addProperty("buttonClass",
      "btn-danger");
    addMenuItem(actionMenu, prefix, "add", "Clear Fields").addProperty("buttonClass",
      "btn-warning");
    final String name = form.getName();
    final Menu saveMenu = new Menu("Save", "javascript:$('#" + name + "').submit()");
    saveMenu.addProperty("buttonClass", "btn-primary");
    actionMenu.addMenuItem(saveMenu);

    final ButtonsToolbarElement buttonsToolbar = new ButtonsToolbarElement(actionMenu);
    final ElementContainer view = new ElementContainer(form, buttonsToolbar);
    final TabElementContainer tabs = new TabElementContainer();
    tabs.add(title, view);
    return tabs;
  }

  public Element newObjectEditPage(final T object, final String prefix)
    throws IOException, ServletException {
    if (object == null) {
      throw new PageNotFoundException();
    } else {
      final HttpServletRequest request = HttpServletUtils.getRequest();
      final Set<String> parameterNamesToSave = new HashSet<>();
      parameterNamesToSave.add(getIdParameterName());

      final String pageName = getName(prefix, "edit");
      final Form form = newTableForm(object, pageName);
      for (final String param : parameterNamesToSave) {
        form.addSavedParameter(param, request.getParameter(param));
      }
      form.initialize(request);

      if (form.isPosted() && form.isMainFormTask()) {
        if (form.isValid() && preUpdate(form, object)) {
          updateObject(object);
          postUpdate(object);

          final Map<String, Object> parameters = new HashMap<>();
          // Get after object has changed
          final Object id = Property.get(object, getIdPropertyName());
          parameters.put(getIdParameterName(), id);

          final Page viewPage = getPage(prefix, "view");
          final String url = viewPage.getFullUrl(parameters);
          redirectAfterCommit(url);
          return new TabElementContainer();
        } else {
          setRollbackOnly(object);
        }
      } else {
        setRollbackOnly(object);
      }

      final Page page = getPage(prefix, "edit");
      final String title = page.getExpandedTitle();
      request.setAttribute("title", title);

      final Menu actionMenu = new Menu();
      addMenuItem(actionMenu, prefix, "view", "Cancel", "_top").addProperty("buttonClass",
        "btn-danger");
      addMenuItem(actionMenu, prefix, "edit", "Revert to Saved", "_top").addProperty("buttonClass",
        "btn-warning");
      final String name = form.getName();
      final Menu saveItem = new Menu("Save", "javascript:$('#" + name + "').submit()");
      saveItem.addProperty("buttonClass", "btn-primary");
      actionMenu.addMenuItem(saveItem);

      final ButtonsToolbarElement buttonsToolbar = new ButtonsToolbarElement(actionMenu);
      final ElementContainer view = new ElementContainer(form, buttonsToolbar);
      final TabElementContainer tabs = new TabElementContainer();
      tabs.add(title, view);
      return tabs;
    }
  }

  @SuppressWarnings("unchecked")
  public <F extends Form> F newTableForm(final Object object, final String keyListName) {
    final List<String> keyList = getKeyList(keyListName);
    final UiBuilderObjectForm form = new UiBuilderObjectForm(object, this, getTypeName(), keyList);
    return (F)form;
  }

  protected void newView(final String name, final List<?> elements) {
    final List<KeySerializer> serializers = new ArrayList<>();
    this.viewSerializers.put(name, serializers);
    for (final Object element : elements) {
      if (element != null) {
        KeySerializer serializer = null;
        if (element instanceof KeySerializer) {
          serializer = (KeySerializer)element;
        } else {
          final String key = element.toString();
          serializer = this.keySerializers.get(key);
          if (serializer == null) {
            final String title = getLabel(key);
            serializer = new BuilderSerializer(key, title, this);
          }
        }
        if (serializer instanceof HtmlUiBuilderAware) {
          @SuppressWarnings("unchecked")
          final HtmlUiBuilderAware<HtmlUiBuilder<?>> builderAware = (HtmlUiBuilderAware<HtmlUiBuilder<?>>)serializer;
          builderAware.setHtmlUiBuilder(this);
        }
        serializers.add(serializer);
      }
    }
  }

  protected void newView(final String name, final Object... elements) {
    newView(name, Arrays.asList(elements));
  }

  protected void notFound(final HttpServletResponse response, final String message)
    throws IOException {
    response.sendError(HttpServletResponse.SC_NOT_FOUND, message);
  }

  public void postInsert(final T object) {
  }

  public void postUpdate(final T object) {
  }

  public boolean preInsert(final Form form, final T object) {
    return true;
  }

  public boolean preUpdate(final Form form, final T object) {
    return true;
  }

  public void redirectAfterCommit(final String url) {
    final Map<String, Object> parameters = new HashMap<>();
    final HttpServletRequest request = HttpServletUtils.getRequest();
    for (final String parameterName : Arrays.asList("plain", "htmlCss")) {
      final String value = request.getParameter(parameterName);
      if (Property.hasValue(value)) {
        parameters.put(parameterName, value);
      }
    }
    final String urlWithParameters = UrlUtil.getUrl(url, parameters);
    Transaction.afterCommit(() -> HttpServletUtils.redirect(urlWithParameters));
  }

  public Void redirectPage(final String pageName) {
    String url = getPageUrl(pageName);
    if (url == null) {
      url = "..";
    }
    redirectAfterCommit(url);
    return null;
  }

  public Object redirectToTab(final Object parentBuilder, final String parentPageName,
    final String tabName) {
    final HtmlUiBuilder<?> builder = getBuilder(parentBuilder);
    if (builder != null) {
      final Page parentPage = builder.getPage(parentPageName);
      if (parentPage != null) {
        String url = parentPage.getFullUrl();
        if (url != null) {
          url += "#" + getTypeName() + "_" + tabName;
          redirectAfterCommit(url);
          return null;
        }
      }
    }
    throw new RuntimeException(
      "Unable to get page " + parentPageName + " from builder " + parentBuilder);
  }

  public void referrerRedirect(final HttpServletRequest request) {
    final String url = request.getHeader("Referer");
    redirectAfterCommit(url);
  }

  /**
   * Serialize the value represented by the key from the object.
   *
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   * @param key The key to serialize.
   * @throws IOException If there was an I/O error serializing the value.
   */
  public void serialize(final XmlWriter out, final Object object, final String key) {

    if (object == null) {
      serializeNullLabel(out, key);
    } else {
      final Object serializer = this.keySerializers.get(key);
      if (serializer == null) {
        String path = null;
        String valueKey = key;
        final Matcher linkMatcher = LINK_KEY_PATTERN.matcher(key);

        if (linkMatcher.matches()) {
          path = linkMatcher.group(1);
          valueKey = linkMatcher.group(2);
        }

        HtmlUiBuilder<? extends Object> uiBuilder = this;
        final String[] parts = valueKey.split("\\.");
        Object currentObject = object;
        for (int i = 0; i < parts.length - 1; i++) {
          final String keyName = parts[i];
          try {
            currentObject = getProperty(currentObject, keyName);
            if (currentObject == null) {
              serializeNullLabel(out, keyName);
              return;
            }

            uiBuilder = getBuilder(currentObject);
          } catch (final IllegalArgumentException e) {
            final String message = currentObject.getClass().getName() + " does not have a property "
              + keyName;
            this.log.error(e.getMessage(), e);
            out.element(HtmlElem.B, message);
            return;
          }
        }
        final String lastKey = parts[parts.length - 1];
        if (path == null) {
          if (uiBuilder == this) {
            try {
              final Object value = getProperty(currentObject, lastKey);
              if (value == null) {
                serializeNullLabel(out, lastKey);
                return;
              } else {
                final TypeSerializer typeSerializer = this.classSerializers.get(value.getClass());
                if (typeSerializer == null) {
                  final String stringValue;
                  if (value instanceof Collection) {
                    final Collection<?> collection = (Collection<?>)value;
                    stringValue = Strings.toString(", ", collection);
                  } else {
                    stringValue = value.toString();
                  }
                  if (stringValue.length() > 0) {
                    out.text(stringValue);
                    return;
                  }
                } else {
                  typeSerializer.serialize(out, value);
                  return;
                }
              }
            } catch (final IllegalArgumentException e) {
              final String message = currentObject.getClass().getName()
                + " does not have a property " + key;
              this.log.error(e.getMessage(), e);
              out.element(HtmlElem.B, message);
              return;
            }
          } else {
            uiBuilder.serialize(out, currentObject, lastKey);

          }
        } else {
          uiBuilder.serializeLink(out, currentObject, lastKey, path);
        }
      } else {
        if (serializer instanceof TypeSerializer) {
          final TypeSerializer typeSerializer = (TypeSerializer)serializer;
          typeSerializer.serialize(out, object);
          return;
        } else if (serializer instanceof KeySerializer) {
          final KeySerializer keySerializer = (KeySerializer)serializer;
          keySerializer.serialize(out, object);
          return;
        }

      }
    }
  }

  public void serializeLink(final XmlWriter out, final Object object, final String key,
    final String pageName) {
    final Map<String, Object> parameters = new HashMap<>();
    final Object id = getIdValue(object);
    parameters.put(this.idParameterName, id);
    parameters.put(key, getProperty(object, key));
    final String url = getPageUrl(pageName, parameters);
    if (url == null) {
      serializeNullLabel(out, pageName);
    } else {
      out.startTag(HtmlElem.A);
      out.attribute(HtmlAttr.HREF, url);
      out.attribute(HtmlAttr.TARGET, "_top");
      serialize(out, object, key);
      out.endTag(HtmlElem.A);
    }
  }

  public void serializeLink(final XmlWriter out, final Object object, final String key,
    final String pageName, final Map<String, String> parameterKeys) {
    final Map<String, Object> parameters = new HashMap<>();
    if (parameterKeys.isEmpty()) {
      final Object id = getIdValue(object);
      parameters.put(this.idParameterName, id);
    } else {
      for (final Entry<String, String> parameterKey : parameterKeys.entrySet()) {
        final String parameterName = parameterKey.getKey();
        final String keyName = parameterKey.getValue();
        final Object value = getProperty(object, keyName);
        if (value != null) {
          parameters.put(parameterName, value);
        }
      }
    }
    final String url = getPageUrl(pageName, parameters);
    if (url == null) {
      serialize(out, object, key);
    } else {
      out.startTag(HtmlElem.A);
      out.attribute(HtmlAttr.HREF, url);
      out.attribute(HtmlAttr.TARGET, "_top");
      serialize(out, object, key);
      out.endTag(HtmlElem.A);
    }
  }

  /**
   * Serialize the message where a key has no value. The default is the
   * character '-'.
   *
   * @param out The XML writer to serialize to.
   * @param key The key to serialize the no value message for.
   * @throws IOException If there was an I/O error serializing the value.
   */
  public void serializeNullLabel(final XmlWriter out, final String key) {
    final String nullLabel = getNullLabel(key);
    if (nullLabel == null) {
      final int dotIndex = key.lastIndexOf('.');
      if (dotIndex == -1) {
        out.text("-");
      } else {
        serializeNullLabel(out, key.substring(0, dotIndex));
      }
    } else {
      out.text(nullLabel);
    }
  }

  @Override
  public void setBeanFactory(final BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  /**
   * Set the factory used to get related HTML UI builders,
   *
   * @param builderFactory The factory.
   */
  public void setBuilderFactory(final HtmlUiBuilderFactory builderFactory) {
    this.builderFactory = builderFactory;
  }

  public void setDefaultPageSize(final int defaultPageSize) {
    this.defaultPageSize = defaultPageSize;
  }

  /**
   * @param fieldInstructions The fieldInstructions to set.
   */
  public void setFieldInstructions(final Map<String, String> fieldInstructions) {
    this.fieldInstructions = fieldInstructions;
  }

  public void setFields(final Map<String, Element> fields) {
    this.fields = fields;
  }

  /**
   * @param idParameterName The idParameterName to set.
   */
  public void setIdParameterName(final String idParameterName) {
    this.idParameterName = idParameterName;
  }

  public void setIdPropertyName(final String idPropertyName) {
    this.idPropertyName = idPropertyName;
  }

  /**
   * Set the key list with the specified name.
   *
   * @param name The name of the key list.
   * @param keyList<String> The key list.
   */
  public void setKeyList(final String name, final List<String> keyList) {
    this.keyLists.put(name, keyList);
  }

  /**
   * Set the map of key lists.
   *
   * @param keyLists The map of key lists.
   */
  public void setKeyLists(final Map<String, List<String>> keyLists) {
    this.keyLists = keyLists;
    if (!keyLists.containsKey("list")) {
      setKeyList("list", getKeyList("listView"));
    }
    if (!keyLists.containsKey("detail")) {
      setKeyList("detail", getKeyList("detailView"));
    }
    if (!keyLists.containsKey("form")) {
      setKeyList("form", getKeyList("formView"));
    }
  }

  /**
   * @param labels The labels to set.
   */
  public void setLabels(final Map<String, String> labels) {
    this.labels.putAll(labels);
  }

  public void setListSortOrder(final int columnIndex, final boolean ascending) {
    String direction;
    if (ascending) {
      direction = "asc";
    } else {
      direction = "desc";
    }
    final List<Object> columnSortOrder = Arrays.asList(columnIndex, direction);
    final List<List<Object>> sortOrder = Collections.singletonList(columnSortOrder);
    this.listSortOrder.put("list", sortOrder);
  }

  public void setListSortOrder(final Map<String, List<List<Object>>> listSortOrder) {
    this.listSortOrder.putAll(listSortOrder);
  }

  protected void setListSortOrder(final String pageName, final List<List<Object>> listSortOrder) {
    this.listSortOrder.put(pageName, listSortOrder);
  }

  public void setMaxPageSize(final int maxPageSize) {
    this.maxPageSize = maxPageSize;
  }

  public void setMessages(final Map<String, String> messages) {
    this.messages = messages;
  }

  /**
   * @param nullLabels The nullLabels to set.
   */
  public void setNullLabels(final Map<String, String> nullLabels) {
    this.nullLabels = nullLabels;
  }

  public void setPages(final Collection<Page> pages) {
    for (final Page page : pages) {
      addPage(page);
    }
  }

  public void setPagesByName(final Map<String, Page> pagesByName) {
    this.pagesByName = pagesByName;
  }

  public String setPageTitle(final HttpServletRequest request, final String pageName) {
    final String title = getPageTitle(pageName);
    if (request.getAttribute("pageTitle") == null) {
      request.setAttribute("title", title);
      request.setAttribute("pageTitle", title);
    }
    return title;
  }

  public void setPageTitleAttribute(final HttpServletRequest request, final String pageName) {
    final Page page = getPage(pageName);
    if (page != null) {
      final String title = page.getExpandedTitle();
      request.setAttribute("title", title);
      request.setAttribute("pageHeading", title);
    }
  }

  public void setPageUrls(final Map<String, String> pageUrls) {
    this.pageUrls = pageUrls;
  }

  public void setPluralTitle(final String pluralTitle) {
    this.pluralTitle = pluralTitle;
  }

  public void setRollbackOnly(final T object) {
  }

  public void setSerializers(final Collection<KeySerializer> keySerializers) {
    for (final KeySerializer serializer : keySerializers) {
      addKeySerializer(serializer);
    }
  }

  @Override
  public void setServletContext(final ServletContext servletContext) {
  }

  public void setTitle(final String typeLabel) {
    this.title = typeLabel;
  }

  public void setTypeName(final String typeName) {
    this.typeName = typeName;
    if (this.idParameterName == null) {
      this.idParameterName = typeName + "Id";
    }
  }

  public void setUsePathVariables(final boolean usePathVariables) {
    this.usePathVariables = usePathVariables;
  }

  public void setValue(final Object object, final String key, final Object value) {
    if (object instanceof Record) {
      final Record record = (Record)object;
      record.setValueByPath(key, value);
    } else if (object instanceof Map) {
      @SuppressWarnings("unchecked")
      final Map<String, Object> map = (Map<String, Object>)object;
      map.put(key, value);
    } else {
      Property.setSimple(object, key, value);
    }
  }

  public void setViews(final Map<String, List<?>> views) {
    for (final Entry<String, List<?>> view : views.entrySet()) {
      final String name = view.getKey();
      final List<?> elements = view.getValue();
      newView(name, elements);
    }
  }

  protected void updateObject(final T object) {
  }

  public boolean validateForm(final HtmlUiBuilderObjectForm form) {
    return true;
  }

  public boolean validateForm(final UiBuilderObjectForm uiBuilderObjectForm) {
    return true;
  }

}
