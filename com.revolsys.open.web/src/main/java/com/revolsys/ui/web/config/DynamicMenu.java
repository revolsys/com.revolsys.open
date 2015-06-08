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
package com.revolsys.ui.web.config;

import java.util.Collection;

public class DynamicMenu extends Menu {

  private final MenuItemLoader loader;

  public DynamicMenu(final String name, final String title, final String uri, final String anchor,
    final String condition, final MenuItemLoader loader) throws Exception {
    super(name, title, uri, anchor, condition);
    this.loader = loader;
  }

  @Override
  public Collection getItems() {
    return this.loader.getItems();
  }

  @Override
  public String getTitle() {
    String title = this.loader.getTitle();
    if (title == null) {
      title = super.getTitle();
    }
    return title;
  }

  @Override
  public String getUri() {
    String uri = this.loader.getUri();
    if (uri == null) {
      uri = super.getUri();
    }
    return uri;
  }
}
