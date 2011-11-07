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
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.xml.io.XmlWriter;

/**
 * @author paustin
 * @version 1.0
 */
public class HtmlDocument extends ElementContainer {

  private List<BufferedReader> styles = new ArrayList<BufferedReader>();

  public void addStyle(final InputStream styleIn) {
    styles.add(new BufferedReader(new InputStreamReader(styleIn)));
  }

  public void addStyle(final Reader styleIn) {
    styles.add(new BufferedReader(styleIn));
  }

  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.HTML);

    out.startTag(HtmlUtil.HEAD);
    serializeStyles(out);
    out.endTag(HtmlUtil.HEAD);

    out.startTag(HtmlUtil.BODY);

    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, "bodyContent");
    super.serializeElement(out);
    out.endTag(HtmlUtil.DIV);

    out.endTag(HtmlUtil.BODY);
    out.endTag(HtmlUtil.HTML);
  }

  private void serializeStyles(final XmlWriter out) {
    for (BufferedReader reader : this.styles) {
      out.startTag(HtmlUtil.STYLE);
      try {
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
          out.text(line);
          out.write('\n');
        }
      } catch (IOException e) {
        LoggerFactory.getLogger(getClass()).error("Cannot read style", out);
      }
      out.endTag(HtmlUtil.STYLE);
    }
  }
}
