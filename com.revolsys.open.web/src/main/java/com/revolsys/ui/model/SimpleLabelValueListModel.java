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
package com.revolsys.ui.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author paustin
 * @version 1.0
 */
public class SimpleLabelValueListModel implements LabelValueListModel {
  private final List labels = new ArrayList();

  private final Map values = new HashMap();

  public void add(final String label, final Object value) {
    if (value != null) {
      this.labels.add(label);
      this.values.put(label, value.toString());
    } else {
      this.labels.add(label);
      this.values.put(label, value);
    }
  }

  public void add(final String label, final String value) {
    this.labels.add(label);
    this.values.put(label, value);
  }

  @Override
  public String getLabel(final int index) {
    return (String)this.labels.get(index);
  }

  @Override
  public int getSize() {
    return this.labels.size();
  }

  @Override
  public String getValue(final int index) {
    final String label = getLabel(index);
    return (String)this.values.get(label);
  }
}
