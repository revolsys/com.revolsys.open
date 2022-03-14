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
package org.apache.olingo.commons.core.edm.annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmException;
import org.apache.olingo.commons.api.edm.annotation.EdmApply;
import org.apache.olingo.commons.api.edm.annotation.EdmExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlApply;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlExpression;
import org.apache.olingo.commons.core.edm.Edm;

public class EdmApplyImpl extends AbstractEdmAnnotatableDynamicExpression implements EdmApply {

  private final CsdlApply csdlExp;

  private String function;

  private List<EdmExpression> parameters;

  public EdmApplyImpl(final Edm edm, final CsdlApply csdlExp) {
    super(edm, "Apply", csdlExp);
    this.csdlExp = csdlExp;
  }

  @Override
  public EdmExpressionType getExpressionType() {
    return EdmExpressionType.Apply;
  }

  @Override
  public String getFunction() {
    if (this.function == null) {
      if (this.csdlExp.getFunction() == null) {
        throw new EdmException("An Apply expression must specify a function.");
      }
      this.function = this.csdlExp.getFunction();
    }
    return this.function;
  }

  @Override
  public List<EdmExpression> getParameters() {
    if (this.parameters == null) {
      final List<EdmExpression> localParameters = new ArrayList<>();
      if (this.csdlExp.getParameters() != null) {
        for (final CsdlExpression param : this.csdlExp.getParameters()) {
          localParameters.add(getExpression(this.edm, param));
        }
      }
      this.parameters = Collections.unmodifiableList(localParameters);
    }
    return this.parameters;
  }
}
