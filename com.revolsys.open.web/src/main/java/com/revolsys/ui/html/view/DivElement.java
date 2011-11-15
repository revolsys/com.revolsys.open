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


import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;

/**
 * @author paustin
 * @version 1.0
 */
public class DivElement extends Element {
  private String cssClass;

  private String content;

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

  /*
   * (non-Javadoc)
   * 
   * @see com.revolsys.ui.model.Element#serializeElement(com.revolsys.io.xml.XmlWriter)
   */
  public void serializeElement(final XmlWriter out) {
    HtmlUtil.serializeDiv(out, cssClass, content);
  }
}
