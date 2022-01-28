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

public class CsdlApply extends CsdlDynamicExpression implements CsdlAnnotatable {

  private String function;

  private List<CsdlExpression> parameters = new ArrayList<>();

  private List<CsdlAnnotation> annotations = new ArrayList<>();

  private boolean checkAnnotations(final List<CsdlAnnotation> annotApplyannotations) {
    if (annotApplyannotations == null) {
      return false;
    }
    if (this.getAnnotations().size() == annotApplyannotations.size()) {
      for (int i = 0; i < this.getAnnotations().size(); i++) {
        if (!this.getAnnotations().get(i).equals(annotApplyannotations.get(i))) {
          return false;
        }
      }
    } else {
      return false;
    }
    return true;
  }

  private boolean checkParamaters(final List<CsdlExpression> annotApplyParams) {
    if (annotApplyParams == null) {
      return false;
    }
    if (this.getParameters().size() == annotApplyParams.size()) {
      for (int i = 0; i < this.getParameters().size(); i++) {
        if (!this.getParameters().get(i).equals(annotApplyParams.get(i))) {
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
    if (obj == null || !(obj instanceof CsdlApply)) {
      return false;
    }
    final CsdlApply annotApply = (CsdlApply)obj;
    return (this.getFunction() == null ? annotApply.getFunction() == null
      : this.getFunction().equals(annotApply.getFunction()))
      && (this.getParameters() == null ? annotApply.getParameters() == null
        : checkParamaters(annotApply.getParameters()))
      && (this.getAnnotations() == null ? annotApply.getAnnotations() == null
        : checkAnnotations(annotApply.getAnnotations()));
  }

  @Override
  public List<CsdlAnnotation> getAnnotations() {
    return this.annotations;
  }

  /**
   * A QualifiedName specifying the name of the client-side function to apply.
   * <br/>
   * OData defines three canonical functions. Services MAY support additional functions that MUST be qualified with a
   * namespace or alias other than odata. Function names qualified with odata are reserved for this specification and
   * its future versions.
   *
   * @return function full qualified name
   * @see org.apache.olingo.commons.api.Constants#CANONICAL_FUNCTION_CONCAT
   * @see org.apache.olingo.commons.api.Constants#CANONICAL_FUNCTION_FILLURITEMPLATE
   * @see org.apache.olingo.commons.api.Constants#CANONICAL_FUNCTION_URIENCODE
   */
  public String getFunction() {
    return this.function;
  }

  /**
   * Returns the expressions applied to the parameters of the function
   * @return List of expression
   */
  public List<CsdlExpression> getParameters() {
    return this.parameters;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (this.function == null ? 0 : this.function.hashCode());
    result = prime * result + (this.parameters == null ? 0 : this.parameters.hashCode());
    result = prime * result + (this.annotations == null ? 0 : this.annotations.hashCode());
    return result;
  }

  public CsdlApply setAnnotations(final List<CsdlAnnotation> annotations) {
    this.annotations = annotations;
    return this;
  }

  public CsdlApply setFunction(final String function) {
    this.function = function;
    return this;
  }

  public CsdlApply setParameters(final List<CsdlExpression> parameters) {
    this.parameters = parameters;
    return this;
  }
}
