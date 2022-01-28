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

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotatable;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;

/**
 * The edm:LabeledElement expression assigns a name to a child expression. The value of the child expression can
 * then be reused elsewhere with an edm:LabeledElementReference (See {@link CsdlLabeledElementReference}) expression.
 */
public class CsdlLabeledElement extends CsdlDynamicExpression implements CsdlAnnotatable {

  private String name;

  private CsdlExpression value;

  private List<CsdlAnnotation> annotations = new ArrayList<>();

  private boolean checkAnnotations(final List<CsdlAnnotation> csdlLabelledEleAnnotations) {
    if (csdlLabelledEleAnnotations == null) {
      return false;
    }
    if (this.getAnnotations().size() == csdlLabelledEleAnnotations.size()) {
      for (int i = 0; i < this.getAnnotations().size(); i++) {
        if (!this.getAnnotations().get(i).equals(csdlLabelledEleAnnotations.get(i))) {
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
    if (obj == null || !(obj instanceof CsdlLabeledElement)) {
      return false;
    }
    final CsdlLabeledElement csdlLabelledEle = (CsdlLabeledElement)obj;
    return (this.getName() == null ? csdlLabelledEle.getName() == null
      : this.getName().equals(csdlLabelledEle.getName()))
      && (this.getValue() == null ? csdlLabelledEle.getValue() == null
        : this.getValue().equals(csdlLabelledEle.getValue()))
      && (this.getAnnotations() == null ? csdlLabelledEle.getAnnotations() == null
        : checkAnnotations(csdlLabelledEle.getAnnotations()));
  }

  @Override
  public List<CsdlAnnotation> getAnnotations() {
    return this.annotations;
  }

  /**
   * Returns the assigned name
   * @return assigned name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns the child expression
   *
   * @return child expression
   */
  public CsdlExpression getValue() {
    return this.value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (this.name == null ? 0 : this.name.hashCode());
    result = prime * result + (this.value == null ? 0 : this.value.hashCode());
    result = prime * result + (this.annotations == null ? 0 : this.annotations.hashCode());
    return result;
  }

  public CsdlLabeledElement setAnnotations(final List<CsdlAnnotation> annotations) {
    this.annotations = annotations;
    return this;
  }

  public CsdlLabeledElement setName(final String name) {
    this.name = name;
    return this;
  }

  public CsdlLabeledElement setValue(final CsdlExpression value) {
    this.value = value;
    return this;
  }
}
