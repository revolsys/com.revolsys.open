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

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.ui.html.fields.Field;
import com.revolsys.util.JavaBeanUtil;

public class ObjectElementContainer extends ElementContainer {
  private Object object;

  public Object getObject() {
    return object;
  }

  public void setObject(final Object object) {
    this.object = object;
  }

  public Object getInitialValue(final Field field, HttpServletRequest request) {
    if (object != null) {
      String propertyName = field.getName();
      return JavaBeanUtil.getProperty(object, propertyName);
    }
    return null;
  }

  public boolean validate() {
    boolean valid = true;
    if (object != null) {
      for (Iterator fields = getFields().values().iterator(); fields.hasNext();) {
        Field field = (Field)fields.next();
        if (!field.hasValidationErrors()) {
          String propertyName = field.getName();
          Object value = field.getValue();
          try {
            JavaBeanUtil.executeSetMethod(object, propertyName, value);
          } catch (IllegalArgumentException e) {
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
