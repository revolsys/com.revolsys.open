/*
 * $URL: https://secure.revolsys.com/svn/open.revolsys.com/rs-iaf/trunk/src/main/java/com/revolsys/ui/html/view/HtmlUiBuilderKeyView.java $
 * 
 * $Date: 2006-12-06 08:54:31 -0800 (Wed, 06 Dec 2006) $
 * $Revision: 188 $
 *
 * Copyright 2004- Revolution Systems Inc.
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
import com.revolsys.ui.html.builder.HtmlUiBuilder;

/**
 * The HtmlUiBuilderKeyView is an {@Element} for serializing the
 * specified key on the object using a {@link HtmlUiBuilder}.
 *
 * @author Paul Austin
 */
public class HtmlUiBuilderKeyView extends Element implements SetObject {
  /** The HTML UI builder. */
  private HtmlUiBuilder builder;

  /** The key to serialize on the builder. */
  private String key;

  /** The object to serialize. */
  private Object object;

  /**
   * Constuct a new HtmlUiBuilderKeyView.
   *
   * @param builder The HTML UI builder.
   * @param object The object to serialize.
   * @param key The key to serialize on the builder.
   */
  public HtmlUiBuilderKeyView(final HtmlUiBuilder builder, final Object object, final String key) {
    this.builder = builder;
    this.object = object;
    this.key = key;
  }

  /**
   * Constuct a new HtmlUiBuilderKeyView.
   *
   * @param builder The HTML UI builder.
   * @param key The key to serialize on the builder.
   */
  public HtmlUiBuilderKeyView(final HtmlUiBuilder builder, final String key) {
    this(builder, null, key);
  }

  /**
   * Get the HTML UI builder.
   *
   * @return The HTML UI builder.
   */
  public HtmlUiBuilder getBuilder() {
    return this.builder;
  }

  /**
   * Get the key to serialize on the builder.
   *
   * @return T
   */
  public String getKey() {
    return this.key;
  }

  /**
   * Get the object to serialize.
   *
   * @return The object to serialize.
   */
  public Object getObject() {
    return this.object;
  }

  /**
   * Serialize the key on the object using the builder.
   *
   * @param out The XML writer to serialize to.
   */
  @Override
  public void serializeElement(final XmlWriter out) {
    if (this.object != null) {
      this.builder.serialize(out, this.object, this.key);
    }
  }

  /**
   * Set the HTML UI builder.
   *
   * @param builder The HTML UI builder.
   */
  public void setBuilder(final HtmlUiBuilder builder) {
    this.builder = builder;
  }

  /**
   * Set the key to serialize on the builder.
   *
   * @param key The key to serialize on the builder.
   */
  public void setKey(final String key) {
    this.key = key;
  }

  /**
   * Set the object to serialize.
   *
   * @param object The object to serialize.
   */
  @Override
  public void setObject(final Object object) {
    this.object = object;
  }
}
