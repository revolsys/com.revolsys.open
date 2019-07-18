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

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.builder.HtmlUiBuilder;

public class HtmlUiBuilderDetailSerializer implements LabelValueListSerializer {
  private final HtmlUiBuilder builder;

  private final List keys;

  private Object object;

  private int size;

  public HtmlUiBuilderDetailSerializer(final HtmlUiBuilder builder, final List keys) {
    this.builder = builder;
    this.keys = keys;
    if (keys != null) {
      this.size = keys.size();
    }
  }

  @Override
  public String getLabelCss(final int index) {
    return null;
  }

  public Object getObject() {
    return this.object;
  }

  @Override
  public int getSize() {
    return this.size;
  }

  @Override
  public String getValueCss(final int index) {
    return null;
  }

  @Override
  public void serializeLabel(final XmlWriter out, final int index) {
    if (index < this.size) {
      final String key = (String)this.keys.get(index);
      out.text(this.builder.getLabel(key));
    } else {
      out.entityRef("nbsp");
    }
  }

  @Override
  public void serializeValue(final XmlWriter out, final int index) {
    if (index < this.size) {
      this.builder.serialize(out, this.object, (String)this.keys.get(index));
    } else {
      out.entityRef("nbsp");
    }
  }

  public void setObject(final Object object) {
    this.object = object;
  }

}
