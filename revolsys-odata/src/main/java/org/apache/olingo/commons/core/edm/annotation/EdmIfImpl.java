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
import org.apache.olingo.commons.api.edm.annotation.EdmIf;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlIf;
import org.apache.olingo.commons.core.edm.Edm;

public class EdmIfImpl extends AbstractEdmAnnotatableDynamicExpression implements EdmIf {

  private EdmExpression guard;

  private EdmExpression _then;

  private EdmExpression _else;

  private final CsdlIf csdlExp;

  public EdmIfImpl(final Edm edm, final CsdlIf csdlExp) {
    super(edm, "If", csdlExp);
    this.csdlExp = csdlExp;
  }

  @Override
  public EdmExpression getElse() {
    // The else clause might be null in certain conditions so we can`t evaluate
    // this here.
    if (this._else == null && this.csdlExp.getElse() != null) {
      this._else = getExpression(this.edm, this.csdlExp.getElse());
    }
    return this._else;
  }

  @Override
  public EdmExpressionType getExpressionType() {
    return EdmExpressionType.If;
  }

  @Override
  public EdmExpression getGuard() {
    if (this.guard == null) {
      if (this.csdlExp.getGuard() == null) {
        throw new EdmException("Guard clause of an if expression must not be null");
      }
      this.guard = getExpression(this.edm, this.csdlExp.getGuard());
    }
    return this.guard;
  }

  @Override
  public EdmExpression getThen() {
    if (this._then == null) {
      if (this.csdlExp.getThen() == null) {
        throw new EdmException("Then clause of an if expression must not be null");
      }
      this._then = getExpression(this.edm, this.csdlExp.getThen());
    }
    return this._then;
  }
}
