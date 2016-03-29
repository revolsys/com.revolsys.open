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

public class Script extends Element {
  private String content;

  private String file;

  private String type = "text/javascript";

  public Script() {
  }

  public Script(final String file) {
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
    out.startTag(HtmlElem.SCRIPT);
    out.attribute(HtmlAttr.TYPE, this.type);
    if (this.content != null) {
      out.write(this.content);
    } else {
      out.attribute(HtmlAttr.SRC, this.file);
      out.write('\n');
    }
    out.endTag(HtmlElem.SCRIPT);
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
}
