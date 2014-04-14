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
package com.revolsys.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.springframework.util.StringUtils;

import com.revolsys.io.xml.XmlWriter;

public final class HtmlUtil {
  public static final String HTML_NS_PREFIX = "";

  public static final String HTML_NS_URI = "http://www.w3.org/1999/xhtml";

  public static final QName A = new QName(HTML_NS_URI, "a", HTML_NS_PREFIX);

  public static final QName ATTR_ACCEPT = new QName("accept");

  public static final QName ATTR_ACTION = new QName("action");

  public static final QName ATTR_ALT = new QName("alt");

  public static final QName ATTR_CELL_PADDING = new QName("cellpadding");

  public static final QName ATTR_CELL_SPACING = new QName("cellspacing");

  public static final QName ATTR_CHECKED = new QName("checked");

  public static final QName ATTR_CLASS = new QName("class");

  public static final QName ATTR_COLS = new QName("cols");

  public static final QName ATTR_ENCTYPE = new QName("enctype");

  public static final QName ATTR_FOR = new QName("for");

  public static final QName ATTR_HEIGHT = new QName("height");

  public static final QName ATTR_HREF = new QName("href");

  public static final QName ATTR_ID = new QName("id");

  public static final QName ATTR_LANG = new QName("xml", "lang");

  public static final QName ATTR_MAX_LENGTH = new QName("maxlength");

  public static final QName ATTR_METHOD = new QName("method");

  public static final QName ATTR_MULTIPLE = new QName("multiple");

  public static final QName ATTR_NAME = new QName("name");

  public static final QName ATTR_ON_CHANGE = new QName("onchange");

  public static final QName ATTR_ON_CLICK = new QName("onclick");

  public static final QName ATTR_ON_MOUSE_OUT = new QName("onmouseout");

  public static final QName ATTR_ON_SUBMIT = new QName("onsubmit");

  public static final QName ATTR_REL = new QName("rel");

  public static final QName ATTR_ROWS = new QName("rows");

  public static final QName ATTR_SELECTED = new QName("selected");

  public static final QName ATTR_SIZE = new QName("size");

  public static final QName ATTR_SRC = new QName("src");

  public static final QName ATTR_STYLE = new QName("style");

  public static final QName ATTR_TITLE = new QName("title");

  public static final QName ATTR_TYPE = new QName("type");

  public static final QName ATTR_VALUE = new QName("value");

  public static final QName ATTR_WIDTH = new QName("width");

  public static final QName B = new QName(HTML_NS_URI, "b", HTML_NS_PREFIX);

  public static final QName BODY = new QName(HTML_NS_URI, "body",
    HTML_NS_PREFIX);

  public static final QName BR = new QName(HTML_NS_URI, "br", HTML_NS_PREFIX);

  public static final QName DD = new QName(HTML_NS_URI, "dd", HTML_NS_PREFIX);

  public static final QName DIV = new QName(HTML_NS_URI, "div", HTML_NS_PREFIX);

  public static final QName DL = new QName(HTML_NS_URI, "dl", HTML_NS_PREFIX);

  public static final QName DT = new QName(HTML_NS_URI, "dt", HTML_NS_PREFIX);

  public static final QName FORM = new QName(HTML_NS_URI, "form",
    HTML_NS_PREFIX);

  public static final QName H1 = new QName(HTML_NS_URI, "h1", HTML_NS_PREFIX);

  public static final QName H2 = new QName(HTML_NS_URI, "h2", HTML_NS_PREFIX);

  public static final QName H3 = new QName(HTML_NS_URI, "h3", HTML_NS_PREFIX);

  public static final QName H4 = new QName(HTML_NS_URI, "h4", HTML_NS_PREFIX);

  public static final QName H5 = new QName(HTML_NS_URI, "h5", HTML_NS_PREFIX);

  public static final QName H6 = new QName(HTML_NS_URI, "h6", HTML_NS_PREFIX);

  public static final QName HEAD = new QName(HTML_NS_URI, "head",
    HTML_NS_PREFIX);

  public static final QName HTML = new QName(HTML_NS_URI, "html",
    HTML_NS_PREFIX);

  public static final QName I = new QName(HTML_NS_URI, "i", HTML_NS_PREFIX);

  public static final QName IMG = new QName(HTML_NS_URI, "img", HTML_NS_PREFIX);

  public static final QName INPUT = new QName(HTML_NS_URI, "input",
    HTML_NS_PREFIX);

  public static final QName LABEL = new QName(HTML_NS_URI, "label",
    HTML_NS_PREFIX);

  public static final QName LI = new QName(HTML_NS_URI, "li", HTML_NS_PREFIX);

  public static final QName LINK = new QName(HTML_NS_URI, "link",
    HTML_NS_PREFIX);

  public static final QName OL = new QName(HTML_NS_URI, "ol", HTML_NS_PREFIX);

  public static final QName OPTION = new QName(HTML_NS_URI, "option",
    HTML_NS_PREFIX);

  public static final QName P = new QName(HTML_NS_URI, "p", HTML_NS_PREFIX);

  public static final QName PRE = new QName(HTML_NS_URI, "pre", HTML_NS_PREFIX);

  public static final QName SCRIPT = new QName(HTML_NS_URI, "script",
    HTML_NS_PREFIX);

  public static final QName SELECT = new QName(HTML_NS_URI, "select",
    HTML_NS_PREFIX);

  public static final QName SPAN = new QName(HTML_NS_URI, "span",
    HTML_NS_PREFIX);

  public static final QName STYLE = new QName(HTML_NS_URI, "style",
    HTML_NS_PREFIX);

  public static final QName TABLE = new QName(HTML_NS_URI, "table",
    HTML_NS_PREFIX);

  public static final QName TBODY = new QName(HTML_NS_URI, "tbody",
    HTML_NS_PREFIX);

  public static final QName TD = new QName(HTML_NS_URI, "td", HTML_NS_PREFIX);

  public static final QName TEXT_AREA = new QName(HTML_NS_URI, "textarea",
    HTML_NS_PREFIX);

  public static final QName TFOOT = new QName(HTML_NS_URI, "tfoot",
    HTML_NS_PREFIX);

  public static final QName TH = new QName(HTML_NS_URI, "th", HTML_NS_PREFIX);

  public static final QName THEAD = new QName(HTML_NS_URI, "thead",
    HTML_NS_PREFIX);

  public static final QName TITLE = new QName(HTML_NS_URI, "title",
    HTML_NS_PREFIX);

  public static final QName TR = new QName(HTML_NS_URI, "tr", HTML_NS_PREFIX);

  public static final QName UL = new QName(HTML_NS_URI, "ul", HTML_NS_PREFIX);

  public static final QName ATTR_TARGET = new QName("target");

  public static final QName HR = new QName(HTML_NS_URI, "hr", HTML_NS_PREFIX);

  public static final QName CODE = new QName(HTML_NS_URI, "code",
    HTML_NS_PREFIX);

  public static final QName META = new QName(HTML_NS_URI, "meta",
    HTML_NS_PREFIX);

  public static final QName ATTR_HTTP_EQUIV = new QName("http-equiv");

  public static final QName ATTR_CONTENT = new QName("content");

  public static void elementWithId(final XmlWriter writer, final QName tag,
    final String id, final Object content) {
    writer.startTag(tag);
    if (StringUtils.hasText(id)) {
      writer.attribute(ATTR_ID, id.replaceAll("[^A-Za-z0-9\\-:.]", "_"));
    }
    writer.text(content);
    writer.endTag(tag);
  }

  public static void serializeA(final XmlWriter out, final String cssClass,
    final Object url, final Object content) {
    if (url != null) {
      out.startTag(A);
      if (cssClass != null) {
        out.attribute(ATTR_CLASS, cssClass);
      }
      out.attribute(ATTR_HREF, url);
    }
    out.text(content);
    if (url != null) {
      out.endTag(A);
    }
  }

  public static void serializeB(final XmlWriter out, final String content) {
    out.startTag(B);
    out.text(content);
    out.endTag(B);
  }

  public static void serializeButtonInput(final XmlWriter out,
    final String value, final String onClick) {
    out.startTag(INPUT);
    out.attribute(ATTR_TYPE, "button");
    out.attribute(ATTR_VALUE, value);
    out.attribute(ATTR_ON_CLICK, onClick);

    out.endTag(INPUT);

  }

  public static void serializeCheckBox(final XmlWriter out, final String name,
    final String value, final boolean selected, final String onClick) {
    out.startTag(INPUT);
    out.attribute(ATTR_ID, name);
    out.attribute(ATTR_NAME, name);
    out.attribute(ATTR_TYPE, "checkbox");
    if (selected) {
      out.attribute(HtmlUtil.ATTR_CHECKED, "checked");
    }
    if (value != null) {
      out.attribute(ATTR_VALUE, value);
    }
    if (onClick != null) {
      out.attribute(ATTR_ON_CLICK, onClick);
    }
    out.endTag(INPUT);
  }

  public static void serializeCss(final XmlWriter out, final String url) {
    out.startTag(LINK);
    out.attribute(ATTR_HREF, url);
    out.attribute(ATTR_REL, "stylesheet");
    out.attribute(ATTR_TYPE, "text/css");
    out.endTag(LINK);
  }

  public static void serializeDiv(final XmlWriter out, final String cssClass,
    final Object content) {
    if (content != null) {
      final String text = content.toString().trim();
      if (text.length() > 0) {
        out.startTag(DIV);
        if (cssClass != null) {
          out.attribute(ATTR_CLASS, cssClass);
        }
        out.text(text);
        out.endTag(DIV);
      }
    }
  }

  public static void serializeFileInput(final XmlWriter out, final String name) {
    out.startTag(INPUT);
    out.attribute(ATTR_NAME, name);
    out.attribute(ATTR_TYPE, "file");

    out.endTag(INPUT);
  }

  public static void serializeFileInput(final XmlWriter out, final String name,
    final Object value) {
    out.startTag(INPUT);
    out.attribute(ATTR_NAME, name);
    out.attribute(ATTR_TYPE, "file");

    if (value != null) {
      out.attribute(ATTR_VALUE, value);
    }

    out.endTag(INPUT);

  }

  public static void serializeHiddenInput(final XmlWriter out,
    final String name, final Object value) {

    String stringValue = null;
    if (value != null) {
      stringValue = value.toString();
    }
    serializeHiddenInput(out, name, stringValue);
  }

  public static void serializeHiddenInput(final XmlWriter out,
    final String name, final String value) {
    out.startTag(INPUT);
    out.attribute(ATTR_NAME, name);
    out.attribute(ATTR_TYPE, "hidden");
    if (value != null) {
      out.attribute(ATTR_VALUE, value);
    }
    out.endTag(INPUT);
  }

  public static void serializePre(final XmlWriter out, final String text) {
    out.startTag(PRE);
    out.text(text);
    out.endTag(PRE);
  }

  public static void serializeScript(final XmlWriter out, final String script) {
    out.startTag(SCRIPT);
    out.attribute(ATTR_TYPE, "text/javascript");
    out.text(script);
    out.endTag(SCRIPT);
  }

  public static void serializeScriptLink(final XmlWriter out, final String url) {
    out.startTag(SCRIPT);
    out.attribute(ATTR_TYPE, "text/javascript");
    out.attribute(ATTR_SRC, url);
    out.text("");
    out.endTag(SCRIPT);
  }

  public static void serializeSelect(final XmlWriter out, final String name,
    final Object selectedValue, final boolean optional,
    final List<? extends Object> values) {
    out.startTag(SELECT);
    out.attribute(ATTR_NAME, name);
    if (optional) {
      out.startTag(OPTION);
      out.attribute(ATTR_VALUE, "");
      out.text("-");
      out.endTag(OPTION);
    }
    if (values != null) {
      for (final Object value : values) {

        out.startTag(OPTION);
        if (selectedValue != null && selectedValue.equals(value)) {
          out.attribute(ATTR_SELECTED, "true");
        }
        out.text(value);

        out.endTag(OPTION);

      }
    }
    out.endTag(SELECT);

  }

  public static void serializeSelect(final XmlWriter out, final String name,
    final Object selectedValue, final boolean optional, final Map<?, ?> values) {
    out.startTag(SELECT);
    out.attribute(ATTR_NAME, name);
    if (optional) {
      out.startTag(OPTION);
      out.attribute(ATTR_VALUE, "");
      out.text("-");
      out.endTag(OPTION);
    }
    if (values != null) {
      for (final Entry<?, ?> entry : values.entrySet()) {
        final Object value = entry.getKey();
        final Object text = entry.getValue();
        out.startTag(OPTION);
        if (selectedValue != null && selectedValue.equals(value)) {
          out.attribute(ATTR_SELECTED, "true");
        }
        out.attribute(ATTR_VALUE, value);
        out.text(text);
        out.endTag(OPTION);

      }
    }
    out.endTag(SELECT);

  }

  public static void serializeSelect(final XmlWriter out, final String name,
    final Object selectedValue, final boolean optional, final Object... values) {
    serializeSelect(out, name, selectedValue, false, Arrays.asList(values));

  }

  public static void serializeSpan(final XmlWriter out, final String cssClass,
    final Object content) {
    if (content != null) {
      final String text = content.toString().trim();
      if (text.length() > 0) {
        out.startTag(SPAN);
        if (cssClass != null) {
          out.attribute(ATTR_CLASS, cssClass);
        }
        out.text(text);
        out.endTag(SPAN);
      }
    }
  }

  public static void serializeSubmitInput(final XmlWriter out,
    final String name, final Object value) {
    out.startTag(INPUT);
    out.attribute(ATTR_NAME, name);
    out.attribute(ATTR_TYPE, "submit");

    if (value != null) {
      out.attribute(ATTR_VALUE, value);
    }
    out.endTag(INPUT);

  }

  public static void serializeTag(final XmlWriter out, final QName tag,
    final String content) {
    out.startTag(tag);
    out.text(content);
    out.endTag(tag);
  }

  public static void serializeTextInput(final XmlWriter out, final String name,
    final Object value, final int size, final int maxLength) {
    out.startTag(INPUT);
    out.attribute(ATTR_NAME, name);
    out.attribute(ATTR_TYPE, "text");
    out.attribute(ATTR_SIZE, size);
    out.attribute(ATTR_MAX_LENGTH, maxLength);
    if (value != null) {
      out.attribute(ATTR_VALUE, value);
    }
    out.endTag(INPUT);

  }

  /**
   * Construct a new HtmlUtil.
   */
  private HtmlUtil() {
  }

  public void addTableRow(final StringBuffer text, final String... cells) {
    text.append("<tr>");
    for (final String string : cells) {

    }
    text.append("</tr>");
  }
}
