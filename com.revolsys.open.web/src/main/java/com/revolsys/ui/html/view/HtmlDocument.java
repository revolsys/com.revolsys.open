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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.jeometry.common.logging.Logs;

import com.revolsys.io.FileUtil;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

/**
 * @author paustin
 * @version 1.0
 */
public class HtmlDocument extends ElementContainer {

  private final List<BufferedReader> styles = new ArrayList<>();

  public void addStyle(final InputStream styleIn) {
    this.styles.add(new BufferedReader(FileUtil.newUtf8Reader(styleIn)));
  }

  public void addStyle(final Reader styleIn) {
    this.styles.add(new BufferedReader(styleIn));
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlElem.HTML);

    out.startTag(HtmlElem.HEAD);
    serializeStyles(out);
    out.endTag(HtmlElem.HEAD);

    out.startTag(HtmlElem.BODY);

    out.startTag(HtmlElem.DIV);
    out.attribute(HtmlAttr.CLASS, "bodyContent");
    super.serializeElement(out);
    out.endTag(HtmlElem.DIV);

    out.endTag(HtmlElem.BODY);
    out.endTag(HtmlElem.HTML);
  }

  private void serializeStyles(final XmlWriter out) {
    for (final BufferedReader reader : this.styles) {
      out.startTag(HtmlElem.STYLE);
      try {
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          out.text(line);
          out.write('\n');
        }
      } catch (final IOException e) {
        Logs.error(this, "Cannot read style", e);
      }
      out.endTag(HtmlElem.STYLE);
    }
  }
}
