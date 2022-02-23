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

import org.apache.olingo.commons.api.edm.annotation.EdmCollection;
import org.apache.olingo.commons.api.edm.annotation.EdmExpression;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlCollection;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlExpression;
import org.apache.olingo.commons.core.edm.Edm;

public class EdmCollectionImpl extends AbstractEdmDynamicExpression implements EdmCollection {

  private List<EdmExpression> items;

  private final CsdlCollection csdlCollection;

  public EdmCollectionImpl(final Edm edm, final CsdlCollection csdlExp) {
    super(edm, "Collection");
    this.csdlCollection = csdlExp;
  }

  @Override
  public EdmExpressionType getExpressionType() {
    return EdmExpressionType.Collection;
  }

  @Override
  public List<EdmExpression> getItems() {
    if (this.items == null) {
      final List<EdmExpression> localItems = new ArrayList<>();
      if (this.csdlCollection.getItems() != null) {
        for (final CsdlExpression item : this.csdlCollection.getItems()) {
          localItems.add(getExpression(this.edm, item));
        }
      }
      this.items = Collections.unmodifiableList(localItems);
    }
    return this.items;
  }
}
