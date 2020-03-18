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
package com.revolsys.ui.html.view;

import java.io.OutputStream;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.io.FileUtil;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.decorator.Decorator;
import com.revolsys.ui.html.form.Form;

public class Element implements Cloneable {
  private ElementContainer container;

  private Decorator decorator;

  @Override
  public Element clone() {
    try {
      return (Element)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  public ElementContainer getContainer() {
    return this.container;
  }

  public Decorator getDecorator() {
    return this.decorator;
  }

  public Form getForm() {
    if (this.container == null) {
      return null;
    } else {
      return this.container.getForm();
    }
  }

  public void initialize(final HttpServletRequest request) {
  }

  public void serialize(final OutputStream outputStream) {
    serialize(FileUtil.newUtf8Writer(outputStream));
  }

  public final void serialize(final Writer out) {
    serialize(out, false);
  }

  public final void serialize(final Writer out, final boolean useNamespaces) {

    final XmlWriter xmlOut = new XmlWriter(out, useNamespaces);
    xmlOut.flush();
    serialize(xmlOut);

    xmlOut.flush();
  }

  public final void serialize(final XmlWriter out) {
    if (this.decorator != null) {
      this.decorator.serialize(out, this);
    } else {
      serializeElement(out);
    }
  }

  public void serializeElement(final XmlWriter out) {
  }

  public void setContainer(final ElementContainer container) {
    this.container = container;
  }

  public void setDecorator(final Decorator decorator) {
    this.decorator = decorator;
  }
}
