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
import com.revolsys.ui.html.serializer.LabelValueListSerializer;

public class DetailView extends Element {
  private String cssClass = "detail";

  private final LabelValueListSerializer serializer;

  private String title;

  private final int numColumns;

  public DetailView(final LabelValueListSerializer serializer) {
    this(serializer, null, null);
  }

  public DetailView(final LabelValueListSerializer serializer,
    final String cssClass) {
    this(serializer, cssClass, null);
  }

  public DetailView(final LabelValueListSerializer serializer,
    final String cssClass, final String title) {
    this(serializer, cssClass, title, 1);
  }

  public DetailView(final LabelValueListSerializer serializer,
    final String cssClass, final String title, final int numColumns) {
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
    final int size = serializer.getSize();
    if (size > 0) {
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_CLASS, cssClass);

      if (title != null && title.length() > 0) {
        out.startTag(HtmlUtil.DIV);
        out.attribute(HtmlUtil.ATTR_CLASS, "title");
        out.text(title);
        out.endTag(HtmlUtil.DIV);
      }

      out.startTag(HtmlUtil.TABLE);
      out.attribute(HtmlUtil.ATTR_CELL_SPACING, "0");
      out.attribute(HtmlUtil.ATTR_CELL_PADDING, "0");
      out.attribute(HtmlUtil.ATTR_CLASS, "data");
      for (int i = 0; i < size; i++) {
        final boolean firstCol = i % numColumns == 0;
        final boolean lastCol = (i + 1) % numColumns == 0 || i == size - 1;
        String labelCss = "";
        String valueCss = "";
        if (firstCol) {
          out.startTag(HtmlUtil.TR);
          labelCss = " firstCol";
          String rowCss = "";
          if (i == 0) {
            rowCss += " firstRow";
          }
          if (i / numColumns == (size - 1) / numColumns) {
            rowCss += " lastRow";
          }
          if (i % 2 == 1) {
            rowCss += " even";
          }
          if (rowCss.length() > 0) {
            out.attribute(HtmlUtil.ATTR_CLASS, rowCss);
          }
        }
        if (lastCol) {
          valueCss = " lastCol";
        }
        out.startTag(HtmlUtil.TH);
        final String newLabelCss = serializer.getLabelCss(i);
        if (newLabelCss != null) {
          labelCss = newLabelCss + labelCss;
        }
        if (labelCss.length() > 0) {
          out.attribute(HtmlUtil.ATTR_CLASS, labelCss);
        }
        serializer.serializeLabel(out, i);
        out.endTag(HtmlUtil.TH);
        out.startTag(HtmlUtil.TD);
        final String newValueCss = serializer.getValueCss(i);
        if (newValueCss != null) {
          valueCss = newValueCss + valueCss;
        }
        if (labelCss.length() > 0) {
          out.attribute(HtmlUtil.ATTR_CLASS, valueCss);
        }
        serializer.serializeValue(out, i);
        out.endTag(HtmlUtil.TD);
        if (lastCol) {
          out.endTag(HtmlUtil.TR);
        }
      }
      out.endTag(HtmlUtil.TABLE);
      out.endTag(HtmlUtil.DIV);
    }
  }
}
