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
 * Represents a edm:If expression
 */
public class CsdlIf extends CsdlDynamicExpression implements CsdlAnnotatable {

  private CsdlExpression guard;

  private CsdlExpression _then;

  private CsdlExpression _else;

  private List<CsdlAnnotation> annotations = new ArrayList<>();

  private boolean checkAnnotations(final List<CsdlAnnotation> csdlIfAnnotations) {
    if (csdlIfAnnotations == null) {
      return false;
    }
    if (this.getAnnotations().size() == csdlIfAnnotations.size()) {
      for (int i = 0; i < this.getAnnotations().size(); i++) {
        if (!this.getAnnotations().get(i).equals(csdlIfAnnotations.get(i))) {
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
    if (obj == null || !(obj instanceof CsdlIf)) {
      return false;
    }
    final CsdlIf csdlIf = (CsdlIf)obj;
    return (this.getGuard() == null ? csdlIf.getGuard() == null
      : this.getGuard().equals(csdlIf.getGuard()))
      && (this.getThen() == null ? csdlIf.getThen() == null
        : this.getThen().equals(csdlIf.getThen()))
      && (this.getElse() == null ? csdlIf.getElse() == null
        : this.getElse().equals(csdlIf.getElse()))
      && (this.getAnnotations() == null ? csdlIf.getAnnotations() == null
        : checkAnnotations(csdlIf.getAnnotations()));
  }

  @Override
  public List<CsdlAnnotation> getAnnotations() {
    return this.annotations;
  }

  /**
   * Return the third expression of the edm:If expression.
   * If the condition of the condition is evaluated to false,
   * this expression as to be executed.
   *
   * @return Third Expression of the edm:If expression
   */
  public CsdlExpression getElse() {
    return this._else;
  }

  /**
   * Returns the first expression of the edm:If expression.
   * This expression represents the condition of the if expression
   *
   * @return First expression of the if expression
   */
  public CsdlExpression getGuard() {
    return this.guard;
  }

  /**
   * Return the second expression of the edm:If expression.
   * If the condition of the condition is evaluated to true,
   * this expression as to be executed.
   *
   * @return Second Expression of the edm:If expression
   */
  public CsdlExpression getThen() {
    return this._then;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (this.guard == null ? 0 : this.guard.hashCode());
    result = prime * result + (this._then == null ? 0 : this._then.hashCode());
    result = prime * result + (this._else == null ? 0 : this._else.hashCode());
    result = prime * result + (this.annotations == null ? 0 : this.annotations.hashCode());
    return result;
  }

  public CsdlIf setAnnotations(final List<CsdlAnnotation> annotations) {
    this.annotations = annotations;
    return this;
  }

  public CsdlIf setElse(final CsdlExpression _else) {
    this._else = _else;
    return this;
  }

  public CsdlIf setGuard(final CsdlExpression guard) {
    this.guard = guard;
    return this;
  }

  public CsdlIf setThen(final CsdlExpression _then) {
    this._then = _then;
    return this;
  }
}
