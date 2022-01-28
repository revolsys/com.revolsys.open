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
package org.apache.olingo.server.core.uri;

import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;

public abstract class UriResourceWithKeysImpl extends UriResourceImpl
  implements UriResourcePartTyped {

  private EdmType collectionTypeFilter = null;

  protected List<UriParameter> keyPredicates = null;

  private EdmType entryTypeFilter = null;

  public UriResourceWithKeysImpl(final UriResourceKind kind) {
    super(kind);
  }

  private String getFQN(final EdmType type) {
    return type.getFullQualifiedName().getFullQualifiedNameAsString();
  }

  public List<UriParameter> getKeyPredicates() {
    return this.keyPredicates == null ? Collections.<UriParameter> emptyList()
      : Collections.unmodifiableList(this.keyPredicates);
  }

  @Override
  public String getSegmentValue(final boolean includeFilters) {
    if (includeFilters) {
      final StringBuilder tmp = new StringBuilder();
      if (this.collectionTypeFilter != null) {
        tmp.append(getFQN(this.collectionTypeFilter));
      }

      if (this.entryTypeFilter != null) {
        if (tmp.length() == 0) {
          tmp.append(getFQN(this.entryTypeFilter));
        } else {
          tmp.append("/()").append(getFQN(this.entryTypeFilter));
        }
      }

      if (tmp.length() != 0) {
        return getSegmentValue() + "/" + tmp.toString();
      }
    }

    return getSegmentValue();
  }

  public EdmType getTypeFilterOnCollection() {
    return this.collectionTypeFilter;
  }

  public EdmType getTypeFilterOnEntry() {
    return this.entryTypeFilter;
  }

  public UriResourceWithKeysImpl setCollectionTypeFilter(final EdmType collectionTypeFilter) {
    this.collectionTypeFilter = collectionTypeFilter;
    return this;
  }

  public UriResourceWithKeysImpl setEntryTypeFilter(final EdmType entryTypeFilter) {
    this.entryTypeFilter = entryTypeFilter;
    return this;
  }

  public UriResourceWithKeysImpl setKeyPredicates(final List<UriParameter> list) {
    this.keyPredicates = list;
    return this;
  }

  @Override
  public String toString(final boolean includeFilters) {
    return getSegmentValue(includeFilters);
  }

}
