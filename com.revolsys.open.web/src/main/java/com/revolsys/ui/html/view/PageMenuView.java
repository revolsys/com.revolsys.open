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

import com.revolsys.ui.web.config.WebUiContext;

public class PageMenuView extends MenuView {
  @Override
  public void processProperty(final String name, final Object value) {
    final String stringValue = (String)value;
    if (name.equals("menuName")) {
      final WebUiContext context = WebUiContext.get();
      setObject(context.getPage().getMenu(stringValue));
    } else {
      super.processProperty(name, value);
    }
  }
}
