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
package org.apache.olingo.server.core.deserializer;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Parameter;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;

public class DeserializerResultImpl implements DeserializerResult {
  public static class DeserializerResultBuilder {
    private Entity entity;

    private EntityCollection entitySet;

    private ExpandOption expandOption;

    private Property property;

    private Map<String, Parameter> actionParameters;

    private List<URI> entityReferences;

    public DeserializerResultBuilder actionParameters(
      final Map<String, Parameter> actionParameters) {
      this.actionParameters = actionParameters;
      return this;
    }

    public DeserializerResult build() {
      final DeserializerResultImpl result = new DeserializerResultImpl();
      result.entity = this.entity;
      result.entitySet = this.entitySet;
      result.expandOption = this.expandOption;
      result.property = this.property;
      result.entityReferences = this.entityReferences == null ? new ArrayList<>()
        : this.entityReferences;
      result.actionParameters = this.actionParameters == null ? new LinkedHashMap<>()
        : this.actionParameters;

      return result;
    }

    public DeserializerResultBuilder entity(final Entity entity) {
      this.entity = entity;
      return this;
    }

    public DeserializerResultBuilder entityCollection(final EntityCollection entitySet) {
      this.entitySet = entitySet;
      return this;
    }

    public DeserializerResultBuilder entityReferences(final List<URI> entityReferences) {
      this.entityReferences = entityReferences;
      return this;
    }

    public DeserializerResultBuilder expandOption(final ExpandOption expandOption) {
      this.expandOption = expandOption;
      return this;
    }

    public DeserializerResultBuilder property(final Property property) {
      this.property = property;
      return this;
    }
  }

  public static DeserializerResultBuilder with() {
    return new DeserializerResultBuilder();
  }

  private Entity entity;

  private EntityCollection entitySet;

  private ExpandOption expandOption;

  private Property property;

  private Map<String, Parameter> actionParameters;

  private List<URI> entityReferences;

  private DeserializerResultImpl() {
  }

  @Override
  public Map<String, Parameter> getActionParameters() {
    return this.actionParameters;
  }

  @Override
  public Entity getEntity() {
    return this.entity;
  }

  @Override
  public EntityCollection getEntityCollection() {
    return this.entitySet;
  }

  @Override
  public List<URI> getEntityReferences() {
    return this.entityReferences;
  }

  @Override
  public ExpandOption getExpandTree() {
    return this.expandOption;
  }

  @Override
  public Property getProperty() {
    return this.property;
  }
}
