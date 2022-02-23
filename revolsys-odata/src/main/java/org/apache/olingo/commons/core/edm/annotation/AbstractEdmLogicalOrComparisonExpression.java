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
import org.apache.olingo.commons.api.edm.annotation.EdmLogicalOrComparisonExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlLogicalOrComparisonExpression;
//CHECKSTYLE:OFF
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlLogicalOrComparisonExpression.LogicalOrComparisonExpressionType;
import org.apache.olingo.commons.core.edm.Edm;

//CHECKSTYLE:ON

public abstract class AbstractEdmLogicalOrComparisonExpression
  extends AbstractEdmAnnotatableDynamicExpression implements EdmLogicalOrComparisonExpression {

  private EdmExpression left;

  private EdmExpression right;

  private final CsdlLogicalOrComparisonExpression csdlExp;

  public AbstractEdmLogicalOrComparisonExpression(final Edm edm,
    final CsdlLogicalOrComparisonExpression csdlExp) {
    super(edm, csdlExp.getType().toString(), csdlExp);
    this.csdlExp = csdlExp;
  }

  @Override
  public EdmExpressionType getExpressionType() {
    switch (this.csdlExp.getType()) {
      case And:
        return EdmExpressionType.And;
      case Or:
        return EdmExpressionType.Or;
      case Not:
        return EdmExpressionType.Not;
      case Eq:
        return EdmExpressionType.Eq;
      case Ne:
        return EdmExpressionType.Ne;
      case Gt:
        return EdmExpressionType.Gt;
      case Ge:
        return EdmExpressionType.Ge;
      case Lt:
        return EdmExpressionType.Lt;
      case Le:
        return EdmExpressionType.Le;
      default:
        throw new EdmException(
          "Invalid Expressiontype for logical or comparison expression: " + this.csdlExp.getType());
    }
  }

  @Override
  public EdmExpression getLeftExpression() {
    if (this.left == null) {
      if (this.csdlExp.getLeft() == null) {
        throw new EdmException(
          "Comparison Or Logical expression MUST have a left and right expression.");
      }
      this.left = AbstractEdmExpression.getExpression(this.edm, this.csdlExp.getLeft());
      if (this.csdlExp.getType() == LogicalOrComparisonExpressionType.Not) {
        this.right = this.left;
      }
    }
    return this.left;
  }

  @Override
  public EdmExpression getRightExpression() {
    if (this.right == null) {
      if (this.csdlExp.getRight() == null) {
        throw new EdmException(
          "Comparison Or Logical expression MUST have a left and right expression.");
      }
      this.right = AbstractEdmExpression.getExpression(this.edm, this.csdlExp.getRight());
      if (this.csdlExp.getType() == LogicalOrComparisonExpressionType.Not) {
        this.left = this.right;
      }
    }
    return this.right;
  }
}
