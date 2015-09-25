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
import com.revolsys.util.HtmlUtil;

public class Style extends Element {
  private String content;

  private String file;

  private String type = "text/css";

  public Style() {
  }

  public Style(final String file) {
    super();
    this.file = file;
  }

  /**
   * @return Returns the content.
   */
  public String getContent() {
    return this.content;
  }

  public String getFile() {
    return this.file;
  }

  /**
   * @return Returns the type.
   */
  public String getType() {
    return this.type;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    if (this.content != null) {
      out.startTag(HtmlUtil.STYLE);
      out.attribute(HtmlUtil.ATTR_TYPE, this.type);
      out.write(this.content);
      out.endTag(HtmlUtil.STYLE);
    } else {
      out.startTag(HtmlUtil.LINK);
      out.attribute(HtmlUtil.ATTR_REL, "stylesheet");
      out.attribute(HtmlUtil.ATTR_HREF, this.file);
      out.attribute(HtmlUtil.ATTR_TYPE, this.type);
      out.endTag(HtmlUtil.LINK);
    }
  }

  /**
   * @param content The content to set.
   */
  public void setContent(final String content) {
    this.content = content;
  }

  /**
   * @param file The file to set.
   */
  public void setFile(final String file) {
    this.file = file;
  }

  /**
   * @param type The type to set.
   */
  public void setType(final String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return this.file;
  }
}
