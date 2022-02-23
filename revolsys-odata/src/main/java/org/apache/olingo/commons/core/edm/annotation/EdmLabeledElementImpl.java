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

import org.apache.olingo.commons.api.edm.EdmException;
import org.apache.olingo.commons.api.edm.annotation.EdmExpression;
import org.apache.olingo.commons.api.edm.annotation.EdmLabeledElement;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlLabeledElement;
import org.apache.olingo.commons.core.edm.Edm;

public class EdmLabeledElementImpl extends AbstractEdmAnnotatableDynamicExpression
  implements EdmLabeledElement {

  private EdmExpression value;

  private final CsdlLabeledElement csdlLableledElement;

  public EdmLabeledElementImpl(final Edm edm, final CsdlLabeledElement csdlExp) {
    super(edm, "LabeledElement", csdlExp);
    this.csdlLableledElement = csdlExp;
  }

  @Override
  public EdmExpressionType getExpressionType() {
    return EdmExpressionType.LabeledElement;
  }

  @Override
  public String getName() {
    if (this.csdlLableledElement.getName() == null) {
      throw new EdmException("The LabeledElement expression must have a name attribute.");
    }
    return this.csdlLableledElement.getName();
  }

  @Override
  public EdmExpression getValue() {
    if (this.value == null) {
      if (this.csdlLableledElement.getValue() == null) {
        throw new EdmException("The LabeledElement expression must have a child expression");
      }
      this.value = getExpression(this.edm, this.csdlLableledElement.getValue());
    }
    return this.value;
  }
}
