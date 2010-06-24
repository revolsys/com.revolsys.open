/*
 * $URL:https://secure.revolsys.com/svn/open.revolsys.com/InternetApplicationFramework/trunk/src/java/com/revolsys/ui/html/builder/HtmlUiBuilder.java $
 * $Author:paul.austin@revolsys.com $
 * $Date:2006-12-06 16:54:31Z $
 * $Revision:188 $
 *
 * Copyright 2004- Revolution Systems Inc.
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
package com.revolsys.ui.html.builder;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.context.HashMapContext;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.web.context.ServletContextAware;

import com.revolsys.orm.core.DataAccessObject;
import com.revolsys.orm.core.SpringDaoFactory;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.decorator.Decorator;
import com.revolsys.ui.html.decorator.FieldLabelDecorator;
import com.revolsys.ui.html.fields.LongField;
import com.revolsys.ui.html.fields.TextField;
import com.revolsys.ui.html.form.Form;
import com.revolsys.ui.html.form.HtmlUiBuilderObjectForm;
import com.revolsys.ui.html.serializer.BuilderMethodLocaleSerializer;
import com.revolsys.ui.html.serializer.BuilderMethodSerializer;
import com.revolsys.ui.html.serializer.HtmlUiBuilderCollectionTableSerializer;
import com.revolsys.ui.html.serializer.HtmlUiBuilderDetailSerializer;
import com.revolsys.ui.html.serializer.key.KeySerializer;
import com.revolsys.ui.html.serializer.type.BooleanSerializer;
import com.revolsys.ui.html.serializer.type.DateSerializer;
import com.revolsys.ui.html.serializer.type.DateTimeSerializer;
import com.revolsys.ui.html.serializer.type.TimestampSerializer;
import com.revolsys.ui.html.serializer.type.TypeSerializer;
import com.revolsys.ui.html.view.DetailView;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.ui.html.view.ElementLabel;
import com.revolsys.ui.html.view.FilterableTableView;
import com.revolsys.ui.html.view.RawContent;
import com.revolsys.ui.html.view.TableView;
import com.revolsys.ui.web.config.HttpServletRequestJexlContext;
import com.revolsys.ui.web.config.Page;
import com.revolsys.ui.web.config.WebUiContext;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.JexlUtil;
import com.revolsys.xml.io.XmlWriter;

public class HtmlUiBuilder implements BeanFactoryAware, ServletContextAware {

  private static final Pattern LINK_KEY_PATTERN = Pattern.compile("link\\(([\\w/]+),([\\w.]+)\\)");

  private static final Pattern SUB_KEY_PATTERN = Pattern.compile("^([\\w]+)(?:\\.(.+))?");

  private BeanFactory beanFactory;

  private HtmlUiBuilderFactory builderFactory;

  protected Map<Class<?>, TypeSerializer> classSerializers = new HashMap<Class<?>, TypeSerializer>();

  private HttpServletRequestJexlContext context;

  private Map<String, String> fieldInstructions = new HashMap<String, String>();

  private final Map<String, Decorator> fieldLabels = new HashMap<String, Decorator>();

  private Map<String, Element> fields = new HashMap<String, Element>();

  protected String idParameterName;

  protected String idPropertyName = "id";

  /** The map of key lists for list views. */
  private Map<String, List<String>> keyLists = new HashMap<String, List<String>>();

  protected Map<String, KeySerializer> keySerializers = new HashMap<String, KeySerializer>();

  private Map<String, String> labels = new HashMap<String, String>();

  private final Logger log = Logger.getLogger(getClass());

  private Map<String, String> messages;

  protected Map<String, String> nullLabels = new HashMap<String, String>();

  private Map<String, String> pageUrls = new HashMap<String, String>();

  private String pluralTitle;

  protected String title;

  protected String typeName;

  public HtmlUiBuilder() {
    final Class<?> clazz = getClass();
    final Method[] methods = clazz.getMethods();
    for (int i = 0; i < methods.length; i++) {
      final Method method = methods[i];
      final String name = method.getName();

      final Class<?>[] parameterTypes = method.getParameterTypes();
      if (parameterTypes.length == 2) {
        if (parameterTypes[0] == XmlWriter.class) {
          if (parameterTypes[1] == Object.class) {
            keySerializers.put(name, new BuilderMethodSerializer(this, method));
          }
        }
      } else if (parameterTypes.length == 3) {
        if (parameterTypes[0] == XmlWriter.class) {
          if (parameterTypes[1] == Object.class) {
            if (parameterTypes[2] == Locale.class) {
              keySerializers.put(name, new BuilderMethodLocaleSerializer(this,
                method));
            }
          }
        }
      }
    }
    classSerializers.put(Date.class, new DateTimeSerializer());
    classSerializers.put(java.sql.Date.class, new DateSerializer());
    classSerializers.put(Timestamp.class, new TimestampSerializer());
    classSerializers.put(Boolean.class, new BooleanSerializer());
  }

  public HtmlUiBuilder(
    final String typeName,
    final String title) {
    this(typeName, title, title);
  }

  public HtmlUiBuilder(
    final String typeName,
    final String title,
    final String pluralTitle) {
    this();
    setTypeName(typeName);
    this.title = title;
    this.pluralTitle = pluralTitle;
  }

  public void addKeySerializer(
    final String key,
    final KeySerializer keySerializer) {
    keySerializers.put(key, keySerializer);
  }

  public void addMessageView(
    final ElementContainer view,
    final String messageName,
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
  public void addNullLabel(
    final String key,
    final String label) {
    nullLabels.put(key, label);
  }

  public Element createDetailView(
    final Object object,
    final String cssClass,
    final List<String> keys,
    final Locale locale) {
    final HtmlUiBuilderDetailSerializer model = new HtmlUiBuilderDetailSerializer(
      this, keys, locale);
    model.setObject(object);
    return new DetailView(model, cssClass);
  }

  public Element createDetailView(
    final Object object,
    final String cssClass,
    final String keyList,
    final Locale locale) {
    return createDetailView(object, cssClass, getKeyList(keyList), locale);
  }

  public FilterableTableView createFilterableTableView(
    final String cssClass,
    final String title,
    final List<String> keys,
    final Locale locale) {

    final HtmlUiBuilderCollectionTableSerializer model = new HtmlUiBuilderCollectionTableSerializer(
      this, keys, locale);
    return new FilterableTableView(this, model, cssClass + " " + typeName,
      title, keys);

  }

  public FilterableTableView createFilterableTableView(
    final String cssClass,
    final String title,
    final String keyList,
    final Locale locale) {
    final List<String> keyListkeyList = getKeyList(keyList);
    return createFilterableTableView(cssClass, title, keyListkeyList, locale);
  }

  /**
   * Create a form for the object using the specified list of fields keys. The
   * form is created without the form title.
   * 
   * @param <T> The type of form to return.
   * @param object The object to create the form for.
   * @param fieldKeys The list of keys for the fields to include on the form.
   * @param locale The locale.
   * @return The generated form.
   */
  @SuppressWarnings("unchecked")
  public <T extends Form> T createForm(
    final Object object,
    final List<String> fieldKeys,
    final Locale locale) {
    final HtmlUiBuilderObjectForm form = createForm(object, getTypeName(),
      fieldKeys, locale);
    return (T)form;
  }

  public <F extends Form> F createForm(
    final Object object,
    final String formName,
    final List<String> keys,
    final Locale locale) {
    final HtmlUiBuilderObjectForm form = new HtmlUiBuilderObjectForm(object,
      this, formName, keys);
    return (F)form;
  }

  /**
   * Create a form for the object using the specified list of fields keys. The
   * form is created without the form title.
   * 
   * @param <T> The type of form to return.
   * @param object The object to create the form for.
   * @param keyListName The name of the list of keys for the fields to include
   *          on the form.
   * @param locale The locale.
   * @return The generated form.
   */
  @SuppressWarnings("unchecked")
  public <F extends Form> F createForm(
    final Object object,
    final String keyListName,
    final Locale locale) {
    final HtmlUiBuilderObjectForm form = createForm(object, getTypeName(),
      getKeyList(keyListName), locale);
    return (F)form;
  }

  public <F extends Form> F createForm(
    final Object object,
    final String formName,
    final String keyList,
    final Locale locale) {
    final List<String> keys = getKeyList(keyList);

    return (F)createForm(object, formName, keys, locale);
  }

  public TableView createTableView(
    final Collection<?> rows,
    final String cssClass,
    final String title,
    final List<String> keys,
    final Locale locale) {
    final HtmlUiBuilderCollectionTableSerializer model = new HtmlUiBuilderCollectionTableSerializer(
      this, keys, locale);
    model.setRows(rows);
    return new TableView(model, cssClass + " " + typeName, title);

  }

  public TableView createTableView(
    final Collection<?> rows,
    final String cssClass,
    final String title,
    final String keyListName,
    final Locale locale) {
    final List<String> keys = getKeyList(keyListName);
    return createTableView(rows, cssClass, title, keys, locale);
  }

  public <T> TableView createTableView(
    final Collection<T> results,
    final String keyListName,
    final Locale locale) {
    return createTableView(results, null, null, keyListName, locale);
  }

  /**
   * @return Returns the beanFactory.
   */
  protected final BeanFactory getBeanFactory() {
    return beanFactory;
  }

  /**
   * Get the HTML UI Builder for the object's class.
   * 
   * @param objectClass<?> The object class.
   * @return The builder.
   */
  public HtmlUiBuilder getBuilder(
    final Class<?> objectClass) {
    if (builderFactory != null) {
      return builderFactory.get(objectClass);
    } else {
      return HtmlUiBuilderFactory.get(beanFactory, objectClass);
    }
  }

  /**
   * Get the HTML UI Builder for the object's class.
   * 
   * @param object The object.
   * @return The builder.
   */
  public HtmlUiBuilder getBuilder(
    final Object object) {
    if (object != null) {
      return getBuilder(object.getClass());
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
  public HtmlUiBuilder getBuilder(
    final String className) {
    try {
      final Class<?> clazz = Class.forName(className);
      return getBuilder(clazz);
    } catch (final ClassNotFoundException e) {
      return null;
    }
  }

  /**
   * Get the factory used to get related HTML UI builders,
   * 
   * @return The factory.
   */
  public HtmlUiBuilderFactory getBuilderFactory() {
    return builderFactory;
  }

  /**
   * Get the Data Access Object for the object's class.
   * 
   * @param objectClass<?> The object class.
   * @return The builder.
   */
  public DataAccessObject getDao(
    final Class<?> objectClass) {
    if (beanFactory != null) {
      return SpringDaoFactory.get(beanFactory, objectClass);
    } else {
      return null;
    }
  }

  /**
   * Get the Data Access Object for the object's class name.
   * 
   * @param objectClass<?>Name The object class name.
   * @return The builder.
   */
  public DataAccessObject getDao(
    final String objectClassName) {
    if (beanFactory != null) {
      return SpringDaoFactory.get(beanFactory, objectClassName);
    } else {
      return null;
    }
  }

  /**
   * Create a new field (or element) for the named key. The parameters from the
   * HttpRequest can be used to customise the look of the field.
   * 
   * @param request The servlet request.
   * @param key The field key.
   * @return The generated field element.
   */
  public Element getField(
    final HttpServletRequest request,
    final String key) {
    if (key.equals("id")) {
      return new LongField("id", false);
    } else if (fields.containsKey(key)) {
      final Element field = fields.get(key);
      return field.clone();
    } else {
      final TextField field = new TextField(key, false);
      return field;
    }
  }

  public String getFieldInstruction(
    final String key) {
    return fieldInstructions.get(key);
  }

  public Map<String, String> getFieldInstructions() {
    return fieldInstructions;
  }

  public Decorator getFieldLabel(
    final String key,
    final boolean field) {
    final Map<String, Decorator> fieldLabels = getFieldLabels();
    Decorator fieldLabel = fieldLabels.get(key);
    if (fieldLabel == null) {
      final String label = getLabel(key);
      final String instructions = getFieldInstruction(key);
      if (field) {
        fieldLabel = new FieldLabelDecorator(label, instructions);
      } else {
        fieldLabel = new ElementLabel(label, instructions);
      }
      fieldLabels.put(key, fieldLabel);
    }
    return fieldLabel;
  }

  public Map<String, Decorator> getFieldLabels() {
    return fieldLabels;
  }

  public Map<String, Element> getFields() {
    return fields;
  }

  /**
   * @return Returns the idParameterName.
   */
  public String getIdParameterName() {
    return idParameterName;
  }

  public String getIdPropertyName() {
    return idPropertyName;
  }

  /**
   * Get the key list with the specified name, or the default if not defined.
   * 
   * @param name The name of the key list.
   * @return The key list.
   */
  public List<String> getKeyList(
    final String name) {
    return getKeyListOrDefault(keyLists, name, "default");
  }

  /**
   * Get the key list with the specified name, or the list for defaultName if
   * not defined.
   * 
   * @param name The name of the key list.
   * @param defaultName The name of the default key list to use.
   * @return The key list.
   */
  public List<String> getKeyList(
    final String name,
    final String defaultName) {
    return getKeyListOrDefault(keyLists, name, defaultName);
  }

  /**
   * Get the key list with the specified name, or the default if not defined.
   * 
   * @param keyLists The map of key lists.
   * @param name The name of the key list.
   * @param defaultName The name of the default key List
   * @return The key list.
   */
  private List<String> getKeyListOrDefault(
    final Map<String, List<String>> keyLists,
    final String name,
    final String defaultName) {
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
    return keyLists;
  }

  /**
   * @return Returns the keySerializers.
   */
  public Map<String, KeySerializer> getKeySerializers() {
    return keySerializers;
  }

  /**
   * <p>
   * Get the label for the key in the default {@link Locale}.
   * </p>
   * 
   * @param key The key.
   * @return The label.
   * @see #getLabel(String, Locale)
   */
  public String getLabel(
    final String key) {
    return getLabel(key, Locale.getDefault());
  }

  /**
   * <p>
   * Get the label for the key in the specified {@link Locale}. The following
   * process is used (in sequence) to get the label for the key.
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
   * @param locale The locale.
   * @return The label.
   */
  public String getLabel(
    final String key,
    final Locale locale) {
    String label = getLabels().get(key);
    if (label == null) {
      final Matcher subKeyMatcher = SUB_KEY_PATTERN.matcher(key);
      if (subKeyMatcher.find() && subKeyMatcher.group(2) != null) {
        label = getLabel(subKeyMatcher.group(1));
      } else {
        final Matcher linkbKeyMatcher = LINK_KEY_PATTERN.matcher(key);
        if (linkbKeyMatcher.find()) {
          label = getLabel(linkbKeyMatcher.group(2));
        } else {
          label = CaseConverter.toCapitalizedWords(key);
        }
      }
      labels.put(key, label);
    }
    return label;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  public String getMessage(
    final String messageName) {
    return messages.get(messageName);
  }

  public String getMessage(
    final String messageName,
    final Map<String, Object> variables) {
    final String message = getMessage(messageName);
    if (message != null) {
      try {
        final Expression expression = JexlUtil.createExpression(message);
        if (expression != null) {
          final JexlContext context = new HashMapContext();
          context.setVars(variables);
          return (String)expression.evaluate(context);
        }
      } catch (final Throwable e) {
        log.error(e.getMessage(), e);
      }
    }
    return message;
  }

  private String getNullLabel(
    final String key) {
    return nullLabels.get(key);
  }

  /**
   * @return Returns the nullLabels.
   */
  public Map<String, String> getNullLabels() {
    return nullLabels;
  }

  public String getPageUrl(
    final String name) {
    final String url = pageUrls.get(name);
    return url;
  }

  public String getPageUrl(
    final String name,
    final Map<String, ? extends Object> parameters) {
    final String url = pageUrls.get(name);
    if (url == null) {
      return null;
    } else {
      return getUrl(url, parameters);
    }
  }

  public Map<String, String> getPageUrls() {
    return pageUrls;
  }

  public String getPluralTitle() {
    return pluralTitle;
  }

  public String getTitle() {
    return title;
  }

  public String getTypeName() {
    return typeName;
  }

  private String getUrl(
    final String url,
    final Map<String, ? extends Object> parameters) {
    try {
      final Expression expression = JexlUtil.createExpression(url);
      if (expression != null) {
        context.setAttributes(parameters);
        return (String)JexlUtil.evaluateExpression(context, expression);
      }
    } catch (final Exception e) {
    } finally {
      context.clearAttributes();
    }
    return url;
  }

  public boolean hasPageUrl(
    final String pageName) {
    return pageUrls.containsKey(pageName);
  }

  public void idLink(
    final XmlWriter out,
    final Object object) {
    final Object id = JavaBeanUtil.getProperty(object, idPropertyName);
    if (id != null) {
      final Map<String, Object> parameters = Collections.singletonMap(
        idParameterName, id);
      final String url = getPageUrl("view", parameters);
      HtmlUtil.serializeA(out, null, url, id);
    }
  }

  public void initializeForm(
    final HtmlUiBuilderObjectForm form,
    final HttpServletRequest request) {
  }

  /**
   * Serialize the value represented by the key from the object. The locale can
   * be used to customize the display for that locale (e.g. different date
   * formats).
   * 
   * @param out The XML writer to serialize to.
   * @param object The object to get the value from.
   * @param key The key to serialize.
   * @param locale The locale.
   * @throws IOException If there was an I/O error serializing the value.
   */
  public void serialize(
    final XmlWriter out,
    final Object object,
    final String key,
    final Locale locale) {

    if (object != null) {

      final int dotIndex = key.indexOf('.');
      if (dotIndex != -1) {
        final String keyName = key.substring(0, dotIndex);
        final String subKey = key.substring(dotIndex + 1);

        try {
          final Object subObject = JavaBeanUtil.getProperty(object, keyName);
          if (subObject != null) {
            final HtmlUiBuilder subBuilder = getBuilder(subObject);
            if (subBuilder != null) {
              subBuilder.serialize(out, subObject, subKey, locale);
              return;
            }
          }
        } catch (final IllegalArgumentException e) {
          final String message = object.getClass().getName()
            + " does not have a property " + keyName;
          log.error(e.getMessage(), e);
          out.element(HtmlUtil.B, message);
        }
      } else {
        final Object serializer = keySerializers.get(key);
        if (serializer == null) {
          final Matcher linkMatcher = LINK_KEY_PATTERN.matcher(key);
          if (linkMatcher.matches()) {
            final String path = linkMatcher.group(1);
            final String textKey = linkMatcher.group(2);

            final Object id = JavaBeanUtil.getProperty(object, idPropertyName);
            Page linkPage = getPage(path);
            if (linkPage != null) {
              out.startTag(HtmlUtil.A);
              final Map<String, Object> parameters = Collections.singletonMap(
                idParameterName, id);
              final String url = linkPage.getFullUrl(parameters);
              out.attribute(HtmlUtil.ATTR_HREF, url);
              serialize(out, object, textKey, locale);
              out.endTag(HtmlUtil.A);
            }
            return;
          } else {
            try {
              final Object value = JavaBeanUtil.getProperty(object, key);
              if (value != null) {
                final TypeSerializer typeSerializer = classSerializers.get(value.getClass());
                if (typeSerializer == null) {
                  final String stringValue = value.toString();
                  if (stringValue.length() > 0) {
                    out.text(stringValue);
                    return;
                  }
                } else {
                  typeSerializer.serialize(out, value, locale);
                  return;
                }
              }
            } catch (final IllegalArgumentException e) {
              final String message = object.getClass().getName()
                + " does not have a property " + key;
              log.error(e.getMessage(), e);
              out.element(HtmlUtil.B, message);
              return;
            }
          }
        } else {
          if (serializer instanceof TypeSerializer) {
            final TypeSerializer typeSerializer = (TypeSerializer)serializer;
            typeSerializer.serialize(out, object, locale);
            return;
          } else if (serializer instanceof KeySerializer) {
            final KeySerializer keySerializer = (KeySerializer)serializer;
            keySerializer.serialize(out, object, key, locale);
            return;
          }
        }
      }
    }
    serializeNullLabel(out, key, locale);
  }

  public void serializeLink(
    final XmlWriter out,
    final String path,
    final Long id) {
    Page page = getPage(path);
    if (page != null) {
      final Map<String, Long> parameters = Collections.singletonMap(
        idParameterName, id);
      final String url = page.getFullUrl(parameters);
      HtmlUtil.serializeA(out, null, url, id);
    }
  }

  private Page getPage(
    final String path) {
    Page linkPage = null;
    final WebUiContext webUiContext = WebUiContext.get();
    if (webUiContext != null) {
      final Page page = webUiContext.getPage();
      linkPage = page.getPage(path);
    }
    if (linkPage == null) {
      final String pageByName = pageUrls.get(path);
      if (pageByName != null) {
        linkPage = new Page(null, null, pageByName, false);
      } else {
        linkPage = new Page(null, null, path, false);
      }
    }
    return linkPage;
  }

  public void serializeLink(
    final XmlWriter out,
    final String pageName,
    final Object object) {
    final Object id = JavaBeanUtil.getProperty(object, idPropertyName);
    Page page = getPage(pageName);
    if (page != null) {
      final Map<String, Object> parameters = Collections.singletonMap(
        idParameterName, id);
      final String url = page.getFullUrl(parameters);
      HtmlUtil.serializeA(out, null, url, pageName);
    }
  }

  /**
   * Serialize the message where a key has no value. The default is the
   * character '-'.
   * 
   * @param out The XML writer to serialize to.
   * @param key The key to serialize the no value message for.
   * @param locale The locale.
   * @throws IOException If there was an I/O error serializing the value.
   */
  public void serializeNullLabel(
    final XmlWriter out,
    final String key,
    final Locale locale) {
    final String nullLabel = getNullLabel(key);
    if (nullLabel == null) {
      final int dotIndex = key.lastIndexOf('.');
      if (dotIndex == -1) {
        out.text("-");
      } else {
        serializeNullLabel(out, key.substring(0, dotIndex), locale);
      }
    } else {
      out.text(nullLabel);
    }
  }

  public void setBeanFactory(
    final BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  /**
   * Set the factory used to get related HTML UI builders,
   * 
   * @param builderFactory The factory.
   */
  public void setBuilderFactory(
    final HtmlUiBuilderFactory builderFactory) {
    this.builderFactory = builderFactory;
  }

  /**
   * @param fieldInstructions The fieldInstructions to set.
   */
  public void setFieldInstructions(
    final Map<String, String> fieldInstructions) {
    this.fieldInstructions = fieldInstructions;
  }

  public void setFields(
    final Map<String, Element> fields) {
    this.fields = fields;
  }

  /**
   * @param idParameterName The idParameterName to set.
   */
  public void setIdParameterName(
    final String idParameterName) {
    this.idParameterName = idParameterName;
  }

  public void setIdPropertyName(
    final String idPropertyName) {
    this.idPropertyName = idPropertyName;
  }

  /**
   * Set the key list with the specified name.
   * 
   * @param name The name of the key list.
   * @param keyList<String> The key list.
   */
  public void setKeyList(
    final String name,
    final List<String> keyList) {
    keyLists.put(name, keyList);
  }

  /**
   * Set the map of key lists.
   * 
   * @param keyLists The map of key lists.
   */
  public void setKeyLists(
    final Map<String, List<String>> keyLists) {
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
   * @param keySerializers The keySerializers to set.
   */
  public void setKeySerializers(
    final Map<String, KeySerializer> keySerializers) {
    this.keySerializers = keySerializers;
  }

  /**
   * @param labels The labels to set.
   */
  public void setLabels(
    final Map<String, String> labels) {
    this.labels = labels;
  }

  public void setMessages(
    final Map<String, String> messages) {
    this.messages = messages;
  }

  /**
   * @param nullLabels The nullLabels to set.
   */
  public void setNullLabels(
    final Map<String, String> nullLabels) {
    this.nullLabels = nullLabels;
  }

  public void setPageUrls(
    final Map<String, String> pageUrls) {
    this.pageUrls = pageUrls;
  }

  public void setPluralTitle(
    final String pluralTitle) {
    this.pluralTitle = pluralTitle;
  }

  public void setServletContext(
    final ServletContext servletContext) {
    context = new HttpServletRequestJexlContext(servletContext);
  }

  public void setTitle(
    final String typeLabel) {
    this.title = typeLabel;
  }

  public void setTypeName(
    final String typeName) {
    this.typeName = typeName;
    if (idParameterName == null) {
      this.idParameterName = typeName + "Id";
    }
  }

  public boolean validateForm(
    final HtmlUiBuilderObjectForm form) {
    return true;
  }
}
