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
package org.apache.olingo.server.core.uri.queryoption.apply;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.apply.GroupByItem;

/**
 * Represents a grouping property.
 */
public class GroupByItemImpl implements GroupByItem {

  private UriInfo path;

  private boolean isRollupAll;

  private final List<GroupByItem> rollup = new ArrayList<>();

  public GroupByItemImpl addRollupItem(final GroupByItem groupByItem) {
    this.rollup.add(groupByItem);
    return this;
  }

  @Override
  public List<UriResource> getPath() {
    return this.path == null ? Collections.<UriResource> emptyList()
      : this.path.getUriResourceParts();
  }

  @Override
  public List<GroupByItem> getRollup() {
    return this.rollup;
  }

  @Override
  public boolean isRollupAll() {
    return this.isRollupAll;
  }

  public GroupByItemImpl setIsRollupAll() {
    this.isRollupAll = true;
    return this;
  }

  public GroupByItemImpl setPath(final UriInfo uriInfo) {
    this.path = uriInfo;
    return this;
  }
}
