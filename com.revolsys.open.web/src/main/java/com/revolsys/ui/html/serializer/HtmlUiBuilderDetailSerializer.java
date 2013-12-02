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

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.builder.HtmlUiBuilder;

public class HtmlUiBuilderDetailSerializer implements LabelValueListSerializer {
  private final List keys;

  private final HtmlUiBuilder builder;

  private int size;

  private Object object;

  public HtmlUiBuilderDetailSerializer(final HtmlUiBuilder builder,
    final List keys) {
    this.builder = builder;
    this.keys = keys;
    if (keys != null) {
      this.size = keys.size();
    }
  }

  public String getLabelCss(final int index) {
    return "detailTitle";
  }

  public Object getObject() {
    return object;
  }

  public int getSize() {
    return size;
  }

  public String getValueCss(final int index) {
    return "detailValue";
  }

  public void serializeLabel(final XmlWriter out, final int index) {
    if (index < size) {
      final String key = (String)keys.get(index);
      out.text(builder.getLabel(key));
    } else {
      out.entityRef("nbsp");
    }
  }

  public void serializeValue(final XmlWriter out, final int index) {
    if (index < size) {
      builder.serialize(out, object, (String)keys.get(index));
    } else {
      out.entityRef("nbsp");
    }
  }

  public void setObject(final Object object) {
    this.object = object;
  }

}
