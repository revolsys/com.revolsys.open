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


import com.revolsys.io.xml.io.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;

public class PasswordField extends TextField {

  /**
   * @param name
   * @param required
   */
  public PasswordField(final String name, final boolean required) {
    super(name, required);
  }

  public PasswordField(final String name, final int minLength,
    final int maxLength, final boolean required) {
    super(name, maxLength, minLength, maxLength, "", required);
  }

  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.INPUT);
    out.attribute(HtmlUtil.ATTR_ID, getName());
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    out.attribute(HtmlUtil.ATTR_TYPE, "password");
    out.attribute(HtmlUtil.ATTR_SIZE, Integer.toString(getSize()));
    if (getMaxLength() < Integer.MAX_VALUE) {
      out.attribute(HtmlUtil.ATTR_MAX_LENGTH, Integer.toString(getMaxLength()));
    }
    out.endTag(HtmlUtil.INPUT);
  }
}
