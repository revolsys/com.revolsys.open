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
package com.revolsys.ui.html.fields;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.HtmlUtil;

public class Button extends Field {

  public static Button submit(final String name, final String title) {
    return new Button(name, false, "submit", null, title, "btn btn-primary btn-sm");
  }

  private String cssClass;

  private String text;

  private String type;

  public Button(final String name) {
    this(name, false, "submit", name, name, null);
  }

  public Button(final String name, final boolean required, final String type, final Object value,
    final String text, final String cssClass) {
    super(name, required);
    setValue(value);
    this.type = type;
    this.text = text;
    this.cssClass = cssClass;
  }

  public Button(final String name, final Object value) {
    super(name, false);
    setValue(value);
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    final String name = getName();
    final Object value = getValue();
    HtmlUtil.serializeButton(out, name, this.type, value, this.text, this.cssClass);
  }

}
