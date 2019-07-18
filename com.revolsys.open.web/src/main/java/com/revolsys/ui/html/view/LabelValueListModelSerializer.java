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
import com.revolsys.ui.html.serializer.LabelValueListSerializer;
import com.revolsys.ui.model.LabelValueListModel;

/**
 * @author paustin
 * @version 1.0
 */
public class LabelValueListModelSerializer implements LabelValueListSerializer {
  private String labelCss;

  private final LabelValueListModel model;

  private String valueCss;

  public LabelValueListModelSerializer(final LabelValueListModel model) {
    this.model = model;
  }

  public LabelValueListModelSerializer(final LabelValueListModel model, final String labelCss,
    final String valueCss) {
    this(model);
    this.labelCss = labelCss;
    this.valueCss = valueCss;
  }

  @Override
  public String getLabelCss(final int index) {
    return this.labelCss;
  }

  @Override
  public int getSize() {
    return this.model.getSize();
  }

  @Override
  public String getValueCss(final int index) {
    return this.valueCss;
  }

  @Override
  public void serializeLabel(final XmlWriter out, final int index) {
    final String label = this.model.getLabel(index);
    if (label != null) {
      out.text(label);
    } else {
      out.entityRef("nbsp");
    }
  }

  @Override
  public void serializeValue(final XmlWriter out, final int index) {
    final String value = this.model.getValue(index);
    if (value != null) {
      out.text(value);
    } else {
      out.entityRef("nbsp");
    }
  }
}
