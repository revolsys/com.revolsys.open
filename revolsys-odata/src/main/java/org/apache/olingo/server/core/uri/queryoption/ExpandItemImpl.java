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

import java.util.List;

import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.ApplyOption;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.LevelsExpandOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;

public class ExpandItemImpl implements ExpandItem {
  private LevelsExpandOption levelsExpandOption;

  private FilterOption filterOption;

  private SearchOption searchOption;

  private OrderByOption orderByOption;

  private SkipOption skipOption;

  private TopOption topOption;

  private CountOption inlineCountOption;

  private SelectOption selectOption;

  private ExpandOption expandOption;

  private ApplyOption applyOption;

  private UriInfoResource resourceInfo;

  private boolean isStar;

  private boolean isRef;

  private boolean hasCountPath;

  private EdmType startTypeFilter;

  @Override
  public ApplyOption getApplyOption() {
    return this.applyOption;
  }

  @Override
  public CountOption getCountOption() {
    return this.inlineCountOption;
  }

  @Override
  public ExpandOption getExpandOption() {
    return this.expandOption;
  }

  @Override
  public FilterOption getFilterOption() {
    return this.filterOption;
  }

  @Override
  public LevelsExpandOption getLevelsOption() {
    return this.levelsExpandOption;
  }

  @Override
  public OrderByOption getOrderByOption() {
    return this.orderByOption;
  }

  @Override
  public UriInfoResource getResourcePath() {

    return this.resourceInfo;
  }

  @Override
  public SearchOption getSearchOption() {
    return this.searchOption;
  }

  @Override
  public SelectOption getSelectOption() {

    return this.selectOption;
  }

  @Override
  public SkipOption getSkipOption() {
    return this.skipOption;
  }

  @Override
  public EdmType getStartTypeFilter() {
    return this.startTypeFilter;
  }

  @Override
  public TopOption getTopOption() {
    return this.topOption;
  }

  @Override
  public boolean hasCountPath() {
    return this.hasCountPath;
  }

  @Override
  public boolean isRef() {
    return this.isRef;
  }

  @Override
  public boolean isStar() {
    return this.isStar;
  }

  public void setCountPath(final boolean value) {
    this.hasCountPath = value;
  }

  public ExpandItemImpl setIsRef(final boolean isRef) {
    this.isRef = isRef;
    return this;
  }

  public ExpandItemImpl setIsStar(final boolean isStar) {
    this.isStar = isStar;
    return this;
  }

  public ExpandItemImpl setResourcePath(final UriInfoResource resourceInfo) {
    this.resourceInfo = resourceInfo;
    return this;
  }

  public ExpandItemImpl setSystemQueryOption(final SystemQueryOption sysItem) {

    if (sysItem instanceof ApplyOption) {
      validateDoubleSystemQueryOption(this.applyOption, sysItem);
      this.applyOption = (ApplyOption)sysItem;
    } else if (sysItem instanceof ExpandOption) {
      validateDoubleSystemQueryOption(this.expandOption, sysItem);
      this.expandOption = (ExpandOption)sysItem;
    } else if (sysItem instanceof FilterOption) {
      validateDoubleSystemQueryOption(this.filterOption, sysItem);
      this.filterOption = (FilterOption)sysItem;
    } else if (sysItem instanceof CountOption) {
      validateDoubleSystemQueryOption(this.inlineCountOption, sysItem);
      this.inlineCountOption = (CountOption)sysItem;
    } else if (sysItem instanceof OrderByOption) {
      validateDoubleSystemQueryOption(this.orderByOption, sysItem);
      this.orderByOption = (OrderByOption)sysItem;
    } else if (sysItem instanceof SearchOption) {
      validateDoubleSystemQueryOption(this.searchOption, sysItem);
      this.searchOption = (SearchOption)sysItem;
    } else if (sysItem instanceof SelectOption) {
      validateDoubleSystemQueryOption(this.selectOption, sysItem);
      this.selectOption = (SelectOption)sysItem;
    } else if (sysItem instanceof SkipOption) {
      validateDoubleSystemQueryOption(this.skipOption, sysItem);
      this.skipOption = (SkipOption)sysItem;
    } else if (sysItem instanceof TopOption) {
      validateDoubleSystemQueryOption(this.topOption, sysItem);
      this.topOption = (TopOption)sysItem;
    } else if (sysItem instanceof LevelsExpandOption) {
      if (this.levelsExpandOption != null) {
        throw new ODataRuntimeException("$levels");
      }
      this.levelsExpandOption = (LevelsExpandOption)sysItem;
    }
    return this;
  }

  public ExpandItemImpl setSystemQueryOptions(final List<SystemQueryOption> list) {
    for (final SystemQueryOption item : list) {
      setSystemQueryOption(item);
    }
    return this;
  }

  public ExpandItemImpl setTypeFilter(final EdmType startTypeFilter) {
    this.startTypeFilter = startTypeFilter;
    return this;
  }

  private void validateDoubleSystemQueryOption(final SystemQueryOption oldOption,
    final SystemQueryOption newOption) {
    if (oldOption != null) {
      throw new ODataRuntimeException(newOption.getName());
    }
  }
}
