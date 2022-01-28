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
package org.apache.olingo.server.core.uri.queryoption;

import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.SelectItem;

public class SelectItemImpl implements SelectItem {

  private UriInfoResource path;

  private boolean isStar;

  private FullQualifiedName addOperationsInSchemaNameSpace;

  private EdmType startTypeFilter;

  public void addAllOperationsInSchema(final FullQualifiedName addOperationsInSchemaNameSpace) {
    this.addOperationsInSchemaNameSpace = addOperationsInSchemaNameSpace;
  }

  @Override
  public FullQualifiedName getAllOperationsInSchemaNameSpace() {
    return this.addOperationsInSchemaNameSpace;
  }

  @Override
  public UriInfoResource getResourcePath() {

    return this.path;
  }

  @Override
  public EdmType getStartTypeFilter() {
    return this.startTypeFilter;
  }

  @Override
  public boolean isAllOperationsInSchema() {
    return this.addOperationsInSchemaNameSpace != null;
  }

  @Override
  public boolean isStar() {
    return this.isStar;
  }

  public SelectItemImpl setResourcePath(final UriInfoResource path) {
    this.path = path;
    return this;
  }

  public SelectItemImpl setStar(final boolean isStar) {
    this.isStar = isStar;
    return this;
  }

  public SelectItemImpl setTypeFilter(final EdmType startTypeFilter) {
    this.startTypeFilter = startTypeFilter;
    return this;
  }

}
