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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoAll;
import org.apache.olingo.server.api.uri.UriInfoBatch;
import org.apache.olingo.server.api.uri.UriInfoCrossjoin;
import org.apache.olingo.server.api.uri.UriInfoEntityId;
import org.apache.olingo.server.api.uri.UriInfoKind;
import org.apache.olingo.server.api.uri.UriInfoMetadata;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriInfoService;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.AliasQueryOption;
import org.apache.olingo.server.api.uri.queryoption.ApplyOption;
import org.apache.olingo.server.api.uri.queryoption.CountOption;
import org.apache.olingo.server.api.uri.queryoption.CustomQueryOption;
import org.apache.olingo.server.api.uri.queryoption.DeltaTokenOption;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.FilterOption;
import org.apache.olingo.server.api.uri.queryoption.FormatOption;
import org.apache.olingo.server.api.uri.queryoption.IdOption;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.QueryOption;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SkipTokenOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;
import org.apache.olingo.server.api.uri.queryoption.TopOption;

public class UriInfoImpl implements UriInfo {

  private UriInfoKind kind;

  // for $entity
  private final List<String> entitySetNames = new ArrayList<>();

  // for $entity
  private EdmEntityType entityTypeCast;

  private UriResource lastResourcePart;

  private final List<UriResource> pathParts = new ArrayList<>();

  private final Map<SystemQueryOptionKind, SystemQueryOption> systemQueryOptions = new EnumMap<>(
    SystemQueryOptionKind.class);

  private final Map<String, AliasQueryOption> aliases = new HashMap<>();

  private final List<CustomQueryOption> customQueryOptions = new ArrayList<>();

  private String fragment;

  public UriInfoImpl addAlias(final AliasQueryOption alias) {
    if (this.aliases.containsKey(alias.getName())) {
      throw new ODataRuntimeException("Alias " + alias.getName() + " is already there.");
    } else {
      this.aliases.put(alias.getName(), alias);
    }
    return this;
  }

  public UriInfoImpl addCustomQueryOption(final CustomQueryOption option) {
    if (option.getName() != null && !option.getName().isEmpty()) {
      this.customQueryOptions.add(option);
    }
    return this;
  }

  public UriInfoImpl addEntitySetName(final String entitySet) {
    this.entitySetNames.add(entitySet);
    return this;
  }

  public UriInfoImpl addResourcePart(final UriResource uriPathInfo) {
    this.pathParts.add(uriPathInfo);
    this.lastResourcePart = uriPathInfo;
    return this;
  }

  @Override
  public UriInfoAll asUriInfoAll() {
    return this;
  }

  @Override
  public UriInfoBatch asUriInfoBatch() {
    return this;
  }

  @Override
  public UriInfoCrossjoin asUriInfoCrossjoin() {
    return this;
  }

  @Override
  public UriInfoEntityId asUriInfoEntityId() {
    return this;
  }

  @Override
  public UriInfoMetadata asUriInfoMetadata() {
    return this;
  }

  @Override
  public UriInfoResource asUriInfoResource() {
    return this;
  }

  @Override
  public UriInfoService asUriInfoService() {
    return this;
  }

  @Override
  public List<AliasQueryOption> getAliases() {
    return Collections.unmodifiableList(new ArrayList<>(this.aliases.values()));
  }

  public Map<String, AliasQueryOption> getAliasMap() {
    return Collections.unmodifiableMap(this.aliases);
  }

  @Override
  public ApplyOption getApplyOption() {
    return (ApplyOption)this.systemQueryOptions.get(SystemQueryOptionKind.APPLY);
  }

  @Override
  public CountOption getCountOption() {
    return (CountOption)this.systemQueryOptions.get(SystemQueryOptionKind.COUNT);
  }

  @Override
  public List<CustomQueryOption> getCustomQueryOptions() {
    return Collections.unmodifiableList(this.customQueryOptions);
  }

  @Override
  public DeltaTokenOption getDeltaTokenOption() {
    return (DeltaTokenOption)this.systemQueryOptions.get(SystemQueryOptionKind.DELTATOKEN);
  }

  @Override
  public List<String> getEntitySetNames() {
    return Collections.unmodifiableList(this.entitySetNames);
  }

  @Override
  public EdmEntityType getEntityTypeCast() {
    return this.entityTypeCast;
  }

  @Override
  public ExpandOption getExpandOption() {
    return (ExpandOption)this.systemQueryOptions.get(SystemQueryOptionKind.EXPAND);
  }

  @Override
  public FilterOption getFilterOption() {
    return (FilterOption)this.systemQueryOptions.get(SystemQueryOptionKind.FILTER);
  }

  @Override
  public FormatOption getFormatOption() {
    return (FormatOption)this.systemQueryOptions.get(SystemQueryOptionKind.FORMAT);
  }

  @Override
  public String getFragment() {
    return this.fragment;
  }

  @Override
  public IdOption getIdOption() {
    return (IdOption)this.systemQueryOptions.get(SystemQueryOptionKind.ID);
  }

  @Override
  public UriInfoKind getKind() {
    return this.kind;
  }

  public UriResource getLastResourcePart() {
    return this.lastResourcePart;
  }

  @Override
  public OrderByOption getOrderByOption() {
    return (OrderByOption)this.systemQueryOptions.get(SystemQueryOptionKind.ORDERBY);
  }

  @Override
  public SearchOption getSearchOption() {
    return (SearchOption)this.systemQueryOptions.get(SystemQueryOptionKind.SEARCH);
  }

  @Override
  public SelectOption getSelectOption() {
    return (SelectOption)this.systemQueryOptions.get(SystemQueryOptionKind.SELECT);
  }

  @Override
  public SkipOption getSkipOption() {
    return (SkipOption)this.systemQueryOptions.get(SystemQueryOptionKind.SKIP);
  }

  @Override
  public SkipTokenOption getSkipTokenOption() {
    return (SkipTokenOption)this.systemQueryOptions.get(SystemQueryOptionKind.SKIPTOKEN);
  }

  @Override
  public List<SystemQueryOption> getSystemQueryOptions() {
    return Collections.unmodifiableList(new ArrayList<>(this.systemQueryOptions.values()));
  }

  @Override
  public TopOption getTopOption() {
    return (TopOption)this.systemQueryOptions.get(SystemQueryOptionKind.TOP);
  }

  @Override
  public List<UriResource> getUriResourceParts() {
    return Collections.unmodifiableList(this.pathParts);
  }

  @Override
  public String getValueForAlias(final String alias) {
    final AliasQueryOption aliasQueryOption = this.aliases.get(alias);
    return aliasQueryOption == null ? null : aliasQueryOption.getText();
  }

  public UriInfoImpl setEntityTypeCast(final EdmEntityType type) {
    this.entityTypeCast = type;
    return this;
  }

  public UriInfoImpl setFragment(final String fragment) {
    this.fragment = fragment;
    return this;
  }

  public UriInfoImpl setKind(final UriInfoKind kind) {
    this.kind = kind;
    return this;
  }

  public UriInfoImpl setQueryOption(final QueryOption option) {
    if (option instanceof SystemQueryOption) {
      setSystemQueryOption((SystemQueryOption)option);
    } else if (option instanceof AliasQueryOption) {
      addAlias((AliasQueryOption)option);
    } else if (option instanceof CustomQueryOption) {
      addCustomQueryOption((CustomQueryOption)option);
    }
    return this;
  }

  /**
   * Adds system query option.
   * @param systemOption the option to be added
   * @return this object for method chaining
   * @throws ODataRuntimeException if an unsupported option is provided
   * or an option of this kind has been added before
   */
  public UriInfoImpl setSystemQueryOption(final SystemQueryOption systemOption) {
    final SystemQueryOptionKind systemQueryOptionKind = systemOption.getKind();
    if (this.systemQueryOptions.containsKey(systemQueryOptionKind)) {
      throw new ODataRuntimeException("Double System Query Option: " + systemOption.getName());
    }

    switch (systemQueryOptionKind) {
      case EXPAND:
      case FILTER:
      case FORMAT:
      case ID:
      case COUNT:
      case ORDERBY:
      case SEARCH:
      case SELECT:
      case SKIP:
      case SKIPTOKEN:
      case DELTATOKEN:
      case TOP:
      case LEVELS:
      case APPLY:
        this.systemQueryOptions.put(systemQueryOptionKind, systemOption);
      break;
      default:
        throw new ODataRuntimeException(
          "Unsupported System Query Option: " + systemOption.getName());
    }
    return this;
  }
}
