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

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

/**
 * @author paustin
 * @version 1.0
 */
public class DivElement extends Element {
  private final String content;

  private final String cssClass;

  private String role;

  public DivElement(final String content) {
    this(null, content);
  }

  /**
   * @param object
   * @param content
   */
  public DivElement(final String cssClass, final String content) {
    this.cssClass = cssClass;
    this.content = content;
  }

  public String getRole() {
    return this.role;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    if (this.content != null) {
      final String text = this.content.toString().trim();
      if (text.length() > 0) {
        out.startTag(HtmlElem.DIV);
        if (this.cssClass != null) {
          out.attribute(HtmlAttr.CLASS, this.cssClass);
        }
        if (this.role != null) {
          out.attribute(HtmlAttr.ROLE, this.role);
        }
        out.text(text);
        out.endTag(HtmlElem.DIV);
      }
    }
  }

  public DivElement setRole(final String role) {
    this.role = role;
    return this;
  }
}
