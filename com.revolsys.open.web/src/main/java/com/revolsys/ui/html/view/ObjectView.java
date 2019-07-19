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

import java.util.HashMap;
import java.util.Map;

public class ObjectView extends Element {
  private Object object;

  private final Map properties = new HashMap();

  public Object getObject() {
    return this.object;
  }

  public Object getProperty(final String name) {
    return this.properties.get(name);
  }

  protected void processProperty(final String name, final Object value) {
  }

  public void setObject(final Object object) {
    this.object = object;
  }

  public void setProperty(final String name, final Object value) {
    this.properties.put(name, value);
    processProperty(name, value);
  }
}
