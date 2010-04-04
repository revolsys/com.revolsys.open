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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.ui.html.decorator.Decorator;
import com.revolsys.ui.html.form.Form;
import com.revolsys.xml.io.XmlWriter;

public class Element implements Cloneable {
  private ElementContainer container;

  private Decorator decorator;

  public ElementContainer getContainer() {
    return container;
  }

  public void setContainer(final ElementContainer container) {
    this.container = container;
  }

  public Form getForm() {
    return container.getForm();
  }

  public Decorator getDecorator() {
    return decorator;
  }

  public final void serialize(final Writer out) throws IOException {
    serialize(out, true);
  }

  public void initialize(final HttpServletRequest request) {
  }

  public final void serialize(final Writer out, final boolean useNamespaces)
    throws IOException {
    out.flush();

    XmlWriter xmlOut = new XmlWriter(out, useNamespaces);
    serialize(xmlOut);

    xmlOut.flush();
  }

  public final void serialize(final XmlWriter out) throws IOException {
    if (decorator != null) {
      decorator.serialize(out, this);
    } else {
      serializeElement(out);
    }
  }

  public void serializeElement(final XmlWriter out) throws IOException {
  }

  public void setDecorator(final Decorator decorator) {
    this.decorator = decorator;
  }

  public void serialize(OutputStream outputStream) throws IOException {
    serialize(new OutputStreamWriter(outputStream, Charset.forName("UTF-8")));
  }

  public Element clone() {
    try {
      return (Element)super.clone();
    } catch (CloneNotSupportedException e) {
      return null;
    }
  }
}
