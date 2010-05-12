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
package com.revolsys.ui.html.serializer;

import java.util.List;
import java.util.Locale;

import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.xml.io.XmlWriter;

public class HtmlUiBuilderDetailSerializer implements LabelValueListSerializer {
  private List keys;

  private HtmlUiBuilder builder;

  private int size;

  private Object object;

  private Locale locale;

  public HtmlUiBuilderDetailSerializer(final HtmlUiBuilder builder,
    final List keys, final Locale locale) {
    this.builder = builder;
    this.keys = keys;
    this.locale = locale;
    if (keys != null) {
      this.size = keys.size();
    }
  }

  public int getSize() {
    return size;
  }

  public String getLabelCss(final int index) {
    return "detailTitle";
  }

  public String getValueCss(final int index) {
    return "detailValue";
  }

  public void serializeLabel(final XmlWriter out, final int index)
    {
    if (index < size) {
      String key = (String)keys.get(index);
      out.text(builder.getLabel(key));
    } else {
      out.entityRef("nbsp");
    }
  }

  public void serializeValue(final XmlWriter out, final int index)
    {
    if (index < size) {
      builder.serialize(out, object, (String)keys.get(index), locale);
    } else {
      out.entityRef("nbsp");
    }
  }

  public Object getObject() {
    return object;
  }

  public void setObject(final Object object) {
    this.object = object;
  }

  public Locale getLocale() {
    return locale;
  }

}
