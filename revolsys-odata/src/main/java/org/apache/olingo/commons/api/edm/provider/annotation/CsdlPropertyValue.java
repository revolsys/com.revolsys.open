/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.commons.api.edm.provider.annotation;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmItem;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotatable;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;

/**
 * The edm:PropertyValue element supplies a value to a property on the type instantiated by an
 * edm:Record expression (See {@link org.apache.olingo.commons.api.edm.annotation.EdmRecord}).
 * The value is obtained by evaluating an expression.
 */
public class CsdlPropertyValue implements CsdlAbstractEdmItem, CsdlAnnotatable {

  private String property;

  private CsdlExpression value;

  private List<CsdlAnnotation> annotations = new ArrayList<>();

  private boolean checkAnnotations(final List<CsdlAnnotation> csdlPropertyValueAnnot) {
    if (csdlPropertyValueAnnot == null) {
      return false;
    }
    if (this.getAnnotations().size() == csdlPropertyValueAnnot.size()) {
      for (int i = 0; i < this.getAnnotations().size(); i++) {
        if (!this.getAnnotations().get(i).equals(csdlPropertyValueAnnot.get(i))) {
          return false;
        }
      }
    } else {
      return false;
    }
    return true;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null || !(obj instanceof CsdlPropertyValue)) {
      return false;
    }
    final CsdlPropertyValue csdlPropertyValue = (CsdlPropertyValue)obj;

    return (this.getProperty() == null ? csdlPropertyValue.getProperty() == null
      : this.getProperty().equalsIgnoreCase(csdlPropertyValue.getProperty()))
      && (this.getValue() == null ? csdlPropertyValue.getValue() == null
        : this.getValue().equals(csdlPropertyValue.getValue()))
      && (this.getAnnotations() == null ? csdlPropertyValue.getAnnotations() == null
        : checkAnnotations(csdlPropertyValue.getAnnotations()));
  }

  @Override
  public List<CsdlAnnotation> getAnnotations() {
    return this.annotations;
  }

  /**
   * Property name
   * @return Property name
   */
  public String getProperty() {
    return this.property;
  }

  /**
   * Evaluated value of the expression (property value)
   * @return evaluated value of the expression
   */
  public CsdlExpression getValue() {
    return this.value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (this.property == null ? 0 : this.property.hashCode());
    result = prime * result + (this.value == null ? 0 : this.value.hashCode());
    result = prime * result + (this.annotations == null ? 0 : this.annotations.hashCode());
    return result;
  }

  public CsdlPropertyValue setAnnotations(final List<CsdlAnnotation> annotations) {
    this.annotations = annotations;
    return this;
  }

  public CsdlPropertyValue setProperty(final String property) {
    this.property = property;
    return this;
  }

  public CsdlPropertyValue setValue(final CsdlExpression value) {
    this.value = value;
    return this;
  }
}
