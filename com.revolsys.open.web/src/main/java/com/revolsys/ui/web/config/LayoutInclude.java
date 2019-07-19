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

public class LayoutInclude {
  private final String area;

  private final Layout layout;

  public LayoutInclude(final String area, final Layout layout) {
    this.area = area;
    this.layout = layout;
  }

  public String getArea() {
    return this.area;
  }

  public Layout getLayout() {
    return this.layout;
  }
}
