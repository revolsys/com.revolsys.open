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
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.Property;

public class DetailView extends Element {
  private String cssClass = "panel panel-info table-responsive";

  private final int numColumns;

  private final LabelValueListSerializer serializer;

  private String title;

  public DetailView(final LabelValueListSerializer serializer) {
    this(serializer, null, null);
  }

  public DetailView(final LabelValueListSerializer serializer, final String cssClass) {
    this(serializer, cssClass, null);
  }

  public DetailView(final LabelValueListSerializer serializer, final String cssClass,
    final String title) {
    this(serializer, cssClass, title, 1);
  }

  public DetailView(final LabelValueListSerializer serializer, final String cssClass,
    final String title, final int numColumns) {
    this.serializer = serializer;
    if (cssClass != null && cssClass.trim().length() > 0) {
      this.cssClass += " " + cssClass;
    }
    if (title != null) {
      this.title = title.trim();
    }
    this.title = title;
    this.numColumns = numColumns;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    final int size = this.serializer.getSize();
    if (size > 0) {
      out.startTag(HtmlElem.DIV);
      out.attribute(HtmlAttr.CLASS, this.cssClass);

      if (this.title != null && this.title.length() > 0) {
        out.startTag(HtmlElem.DIV);
        out.attribute(HtmlAttr.CLASS, "panel-heading");
        {
          out.startTag(HtmlElem.DIV);
          out.attribute(HtmlAttr.CLASS, "panel-title");
          out.text(this.title);
          out.endTag(HtmlElem.DIV);
        }
        out.endTag(HtmlElem.DIV);
      }

      out.startTag(HtmlElem.TABLE);
      out.attribute(HtmlAttr.CELL_SPACING, "0");
      out.attribute(HtmlAttr.CELL_PADDING, "0");
      out.attribute(HtmlAttr.CLASS, "table table-striped table-condensed");
      for (int i = 0; i < size; i++) {
        final boolean firstCol = i % this.numColumns == 0;
        final boolean lastCol = (i + 1) % this.numColumns == 0 || i == size - 1;
        if (firstCol) {
          out.startTag(HtmlElem.TR);
        }
        out.startTag(HtmlElem.TH);
        final String labelCss = this.serializer.getLabelCss(i);
        if (Property.hasValue(labelCss)) {
          out.attribute(HtmlAttr.CLASS, labelCss);
        }
        this.serializer.serializeLabel(out, i);
        out.endTag(HtmlElem.TH);
        out.startTag(HtmlElem.TD);
        final String valueCss = this.serializer.getValueCss(i);
        if (Property.hasValue(valueCss)) {
          out.attribute(HtmlAttr.CLASS, valueCss);
        }
        this.serializer.serializeValue(out, i);
        out.endTag(HtmlElem.TD);
        if (lastCol) {
          out.endTag(HtmlElem.TR);
        }
      }
      out.endTag(HtmlElem.TABLE);
      out.endTag(HtmlElem.DIV);
    }
  }
}
