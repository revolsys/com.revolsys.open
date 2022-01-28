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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.apply.AggregateExpression;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;

/**
 * Represents an aggregate expression.
 */
public class AggregateExpressionImpl implements AggregateExpression {

  private UriInfo path;

  private Expression expression;

  private StandardMethod standardMethod;

  private FullQualifiedName customMethod;

  private String alias;

  private AggregateExpression inlineAggregateExpression;

  private final List<AggregateExpression> from = new ArrayList<>();

  private final Set<String> dynamicProperties = new HashSet<>();

  @Override
  public <T> T accept(final ExpressionVisitor<T> visitor)
    throws ExpressionVisitException, ODataApplicationException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addDynamicProperty(final String name) {
    this.dynamicProperties.add(name);
  }

  public AggregateExpressionImpl addFrom(final AggregateExpression from) {
    this.from.add(from);
    return this;
  }

  @Override
  public String getAlias() {
    return this.alias;
  }

  @Override
  public FullQualifiedName getCustomMethod() {
    return this.customMethod;
  }

  @Override
  public Set<String> getDynamicProperties() {
    return Collections.unmodifiableSet(this.dynamicProperties);
  }

  @Override
  public Expression getExpression() {
    return this.expression;
  }

  @Override
  public List<AggregateExpression> getFrom() {
    return Collections.unmodifiableList(this.from);
  }

  @Override
  public AggregateExpression getInlineAggregateExpression() {
    return this.inlineAggregateExpression;
  }

  @Override
  public List<UriResource> getPath() {
    return this.path == null ? Collections.<UriResource> emptyList()
      : this.path.getUriResourceParts();
  }

  @Override
  public StandardMethod getStandardMethod() {
    return this.standardMethod;
  }

  public AggregateExpressionImpl setAlias(final String alias) {
    this.alias = alias;
    return this;
  }

  public AggregateExpressionImpl setCustomMethod(final FullQualifiedName customMethod) {
    this.customMethod = customMethod;
    return this;
  }

  public AggregateExpressionImpl setExpression(final Expression expression) {
    this.expression = expression;
    return this;
  }

  public AggregateExpressionImpl setInlineAggregateExpression(
    final AggregateExpression aggregateExpression) {
    this.inlineAggregateExpression = aggregateExpression;
    return this;
  }

  public AggregateExpressionImpl setPath(final UriInfo uriInfo) {
    this.path = uriInfo;
    return this;
  }

  public AggregateExpressionImpl setStandardMethod(final StandardMethod standardMethod) {
    this.standardMethod = standardMethod;
    return this;
  }
}
