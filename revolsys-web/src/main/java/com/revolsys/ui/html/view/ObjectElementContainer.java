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

import javax.servlet.http.HttpServletRequest;

import com.revolsys.ui.html.fields.Field;
import com.revolsys.util.Property;

public class ObjectElementContainer extends ElementContainer {
  private Object object;

  @Override
  public Object getInitialValue(final Field field, final HttpServletRequest request) {
    if (this.object != null) {
      final String propertyName = field.getName();
      final Object object1 = this.object;
      return Property.get(object1, propertyName);
    }
    return null;
  }

  public Object getObject() {
    return this.object;
  }

  public void setObject(final Object object) {
    this.object = object;
  }

  @Override
  public boolean validate() {
    boolean valid = true;
    if (this.object != null) {
      for (final Object element : getFields().values()) {
        final Field field = (Field)element;
        if (!field.hasValidationErrors()) {
          final String propertyName = field.getName();
          final Object value = field.getValue();
          try {
            Property.setSimple(this.object, propertyName, value);
          } catch (final IllegalArgumentException e) {
            field.addValidationError(e.getMessage());
            valid = false;
          }
        }
      }
    }
    valid &= super.validate();
    return valid;
  }

}
