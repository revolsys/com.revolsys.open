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
package com.revolsys.ui.html.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.UrlPathHelper;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.util.UrlUtil;

public class Form extends ElementContainer {
  public static final String FORM_TASK_PARAM = "rs_form_task";

  public static final String GET_METHOD = "get";

  public static final String POST_METHOD = "post";

  private String action = "";

  private String cssClass = "form";

  private String defaultFormTask;

  private String formTask;

  private String method = POST_METHOD;

  private String name;

  private final List<String> onSubmit = new ArrayList<String>();

  private boolean posted = false;

  private final Map<String, Object> savedParameters = new HashMap<String, Object>();

  private String title;

  private static final UrlPathHelper URL_HELPER = new UrlPathHelper();

  public Form() {
  }

  /**
   * @param name
   */
  public Form(final String name) {
    this(name, null, null);
  }

  public Form(final String name, final String action) {
    this(name, action, null);
  }

  public Form(final String name, final String action, final String title) {
    this.name = name;
    if (action != null) {
      this.action = action;
    }
    this.title = title;
    defaultFormTask = name;
    addCssClass(name);
  }

  public void addCssClass(final String cssClass) {
    this.cssClass += " " + cssClass;
  }

  public void addOnSubmit(final String script) {
    onSubmit.add(script);
  }

  public void addSavedParameter(final String name, final Object value) {
    this.savedParameters.put(name, value);
  }

  /**
   * @return Returns the action.
   */
  public String getAction() {
    return action;
  }

  protected Map getActionParameters(final HttpServletRequest request) {
    final Set fieldNames = getFields().keySet();
    final Map parameters = new HashMap();
    for (final Enumeration paramNames = request.getParameterNames(); paramNames.hasMoreElements();) {
      final String fieldName = (String)paramNames.nextElement();
      if (!fieldNames.contains(fieldName) && !FORM_TASK_PARAM.equals(fieldName)) {
        parameters.put(fieldName, request.getParameterValues(fieldName));
      }
    }
    return parameters;
  }

  @Override
  public Form getForm() {
    return this;
  }

  @Override
  public Object getInitialValue(
    final Field field,
    final HttpServletRequest request) {
    return null;
  }

  public String getMethod() {
    return method;
  }

  public String getName() {
    return name;
  }

  public String getTask() {
    return formTask;
  }

  public String getTitle() {
    return title;
  }

  public <T> T getValue(final String fieldName) {
    final Field field = getField(fieldName);
    if (field != null) {
      return (T)field.getValue();
    } else {
      return null;
    }
  }

  public boolean hasTask() {
    return formTask != null;
  }

  public boolean hasValue(final String fieldName) {
    final Field field = getField(fieldName);
    if (field != null) {
      return field.hasValue();
    } else {
      return false;
    }
  }

  @Override
  public void initialize(final HttpServletRequest request) {
    if (action == null || action.trim().length() == 0) {
      action = URL_HELPER.getOriginatingRequestUri(request);
    }
    // ensure the formTaskField is initialized first so that it can be used
    // by the other fields
    formTask = request.getParameter(FORM_TASK_PARAM);

    final String method = request.getMethod();
    posted = method.equalsIgnoreCase(POST_METHOD);
    preInit(request);
    super.initialize(request);

    final Map<String, Field> fieldMap = getFields();
    final Collection<Field> fields = fieldMap.values();
    for (final Field field : fields) {
      field.initialize(this, request);
    }
    for (final Field field : fields) {
      field.postInit(request);
    }
    final Map parameters = getActionParameters(request);
    action = UrlUtil.getUrl(action, parameters);
  }

  public boolean isMainFormTask() {
    return hasTask() && formTask.equals(name);
  }

  public boolean isPosted() {
    return posted;
  }

  public boolean isValid() {
    boolean success = true;
    for (final Field field : getFields().values()) {
      success &= field.isValid();
    }
    success &= validate();
    return success;
  }

  protected void preInit(final HttpServletRequest request) {
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    serializeStartTag(out);
    if (defaultFormTask != null) {
      HtmlUtil.serializeHiddenInput(out, FORM_TASK_PARAM, defaultFormTask);
    }
    for (final Entry<String, Object> savedParam : savedParameters.entrySet()) {
      final Object value = savedParam.getValue();
      if (value != null) {
        HtmlUtil.serializeHiddenInput(out, savedParam.getKey(), value);
      }
    }
    super.serializeElement(out);
    serializeEndTag(out);
  }

  /**
   * @param out
   * @throws IOException
   */
  public void serializeEndTag(final XmlWriter out) {
    out.endTag(HtmlUtil.DIV);
    out.endTag(HtmlUtil.FORM);
    out.endTag(HtmlUtil.DIV);
  }

  /**
   * @param out
   * @throws IOException
   */
  public void serializeStartTag(final XmlWriter out) {
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, cssClass);
    final String title = getTitle();
    if (title != null) {
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_CLASS, "title");
      out.text(title);
      out.endTag(HtmlUtil.DIV);
    }
    out.startTag(HtmlUtil.FORM);
    if (onSubmit.size() > 0) {
      final StringBuffer submitScripts = new StringBuffer();
      for (final String script : onSubmit) {
        submitScripts.append(script).append(';');
      }
      out.attribute(HtmlUtil.ATTR_ON_SUBMIT, submitScripts.toString());
    }
    out.attribute(HtmlUtil.ATTR_ID, getName());
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    out.attribute(HtmlUtil.ATTR_ACTION, getAction());
    out.attribute(HtmlUtil.ATTR_METHOD, getMethod());
    out.startTag(HtmlUtil.DIV);
  }

  /**
   * @param action The action to set.
   */
  public void setAction(final String action) {
    this.action = action;
  }

  /**
   * @param method The method to set.
   */
  public void setMethod(final String method) {
    this.method = method;
  }

  public void setName(final String name) {
    this.name = name;
  }

  /**
   * @param title The title to set.
   */
  public void setTitle(final String title) {
    this.title = title;
  }

  public void setValue(final String fieldName, final Object value) {
    final Field field = getField(fieldName);
    if (field != null) {
      field.setValue(value);
    } else {
      throw new IllegalArgumentException("No Such Field " + fieldName);
    }
  }

}
