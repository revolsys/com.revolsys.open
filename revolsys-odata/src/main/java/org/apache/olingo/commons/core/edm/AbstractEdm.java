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
package org.apache.olingo.commons.core.edm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmAction;
import org.apache.olingo.commons.api.edm.EdmAnnotations;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntityContainer;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmParameter;
import org.apache.olingo.commons.api.edm.EdmSchema;
import org.apache.olingo.commons.api.edm.EdmTerm;
import org.apache.olingo.commons.api.edm.EdmTypeDefinition;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;

public abstract class AbstractEdm implements Edm {

  protected Map<String, EdmSchema> schemas;

  protected List<EdmSchema> schemaList;

  private boolean isEntityDerivedFromES;

  private boolean isComplexDerivedFromES;

  private boolean isPreviousES;

  private final Map<FullQualifiedName, EdmEntityContainer> entityContainers = Collections
    .synchronizedMap(new HashMap<FullQualifiedName, EdmEntityContainer>());

  private final Map<FullQualifiedName, EdmEnumType> enumTypes = Collections
    .synchronizedMap(new HashMap<FullQualifiedName, EdmEnumType>());

  private final Map<FullQualifiedName, EdmTypeDefinition> typeDefinitions = Collections
    .synchronizedMap(new HashMap<FullQualifiedName, EdmTypeDefinition>());

  private final Map<FullQualifiedName, EdmEntityType> entityTypes = Collections
    .synchronizedMap(new HashMap<FullQualifiedName, EdmEntityType>());

  private final Map<FullQualifiedName, EdmComplexType> complexTypes = Collections
    .synchronizedMap(new HashMap<FullQualifiedName, EdmComplexType>());

  private final Map<FullQualifiedName, EdmAction> unboundActions = Collections
    .synchronizedMap(new HashMap<FullQualifiedName, EdmAction>());

  private final Map<FullQualifiedName, List<EdmFunction>> unboundFunctionsByName = Collections
    .synchronizedMap(new HashMap<FullQualifiedName, List<EdmFunction>>());

  private final Map<FunctionMapKey, EdmFunction> unboundFunctionsByKey = Collections
    .synchronizedMap(new HashMap<FunctionMapKey, EdmFunction>());

  private final Map<ActionMapKey, EdmAction> boundActions = Collections
    .synchronizedMap(new HashMap<ActionMapKey, EdmAction>());

  private final Map<FunctionMapKey, EdmFunction> boundFunctions = Collections
    .synchronizedMap(new HashMap<FunctionMapKey, EdmFunction>());

  private final Map<FullQualifiedName, EdmTerm> terms = Collections
    .synchronizedMap(new HashMap<FullQualifiedName, EdmTerm>());

  private final Map<TargetQualifierMapKey, EdmAnnotations> annotationGroups = Collections
    .synchronizedMap(new HashMap<TargetQualifierMapKey, EdmAnnotations>());

  private Map<String, String> aliasToNamespaceInfo = null;

  private final Map<FullQualifiedName, EdmEntityType> entityTypesWithAnnotations = Collections
    .synchronizedMap(new HashMap<FullQualifiedName, EdmEntityType>());

  private final Map<FullQualifiedName, EdmEntityType> entityTypesDerivedFromES = Collections
    .synchronizedMap(new HashMap<FullQualifiedName, EdmEntityType>());

  private final Map<FullQualifiedName, EdmComplexType> complexTypesWithAnnotations = Collections
    .synchronizedMap(new HashMap<FullQualifiedName, EdmComplexType>());

  private final Map<FullQualifiedName, EdmComplexType> complexTypesDerivedFromES = Collections
    .synchronizedMap(new HashMap<FullQualifiedName, EdmComplexType>());

  private final Map<String, List<CsdlAnnotation>> annotationMap = new HashMap<>();

  public void cacheAction(final FullQualifiedName actionName, final EdmAction action) {
    if (action.isBound()) {
      final ActionMapKey key = new ActionMapKey(actionName, action.getBindingParameterTypeFqn(),
        action.isBindingParameterTypeCollection());
      this.boundActions.put(key, action);
    } else {
      this.unboundActions.put(actionName, action);
    }
  }

  public void cacheAliasNamespaceInfo(final String alias, final String namespace) {
    this.aliasToNamespaceInfo.put(alias, namespace);
  }

  public void cacheAnnotationGroup(final FullQualifiedName targetName,
    final EdmAnnotations annotationsGroup) {
    final TargetQualifierMapKey key = new TargetQualifierMapKey(targetName,
      annotationsGroup.getQualifier());
    this.annotationGroups.put(key, annotationsGroup);
  }

  public void cacheComplexType(final FullQualifiedName compelxTypeName,
    final EdmComplexType complexType) {
    this.complexTypes.put(compelxTypeName, complexType);
  }

  public void cacheEntityContainer(final FullQualifiedName containerFQN,
    final EdmEntityContainer container) {
    this.entityContainers.put(containerFQN, container);
  }

  public void cacheEntityType(final FullQualifiedName entityTypeName,
    final EdmEntityType entityType) {
    this.entityTypes.put(entityTypeName, entityType);
  }

  public void cacheEnumType(final FullQualifiedName enumName, final EdmEnumType enumType) {
    this.enumTypes.put(enumName, enumType);
  }

  public void cacheFunction(final FullQualifiedName functionName, final EdmFunction function) {
    final FunctionMapKey key = new FunctionMapKey(functionName,
      function.getBindingParameterTypeFqn(), function.isBindingParameterTypeCollection(),
      function.getParameterNames());

    if (function.isBound()) {
      this.boundFunctions.put(key, function);
    } else {
      if (!this.unboundFunctionsByName.containsKey(functionName)) {
        this.unboundFunctionsByName.put(functionName, new ArrayList<EdmFunction>());
      }
      this.unboundFunctionsByName.get(functionName).add(function);

      this.unboundFunctionsByKey.put(key, function);
    }
  }

  public void cacheTerm(final FullQualifiedName termName, final EdmTerm term) {
    this.terms.put(termName, term);
  }

  public void cacheTypeDefinition(final FullQualifiedName typeDefName,
    final EdmTypeDefinition typeDef) {
    this.typeDefinitions.put(typeDefName, typeDef);
  }

  protected abstract Map<String, String> createAliasToNamespaceInfo();

  protected abstract EdmAnnotations createAnnotationGroup(FullQualifiedName targetName,
    String qualifier);

  protected abstract EdmAction createBoundAction(FullQualifiedName actionName,
    FullQualifiedName bindingParameterTypeName, Boolean isBindingParameterCollection);

  protected abstract EdmFunction createBoundFunction(FullQualifiedName functionName,
    FullQualifiedName bindingParameterTypeName, Boolean isBindingParameterCollection,
    List<String> parameterNames);

  protected abstract EdmComplexType createComplexType(FullQualifiedName complexTypeName);

  protected abstract EdmEntityContainer createEntityContainer(FullQualifiedName containerName);

  protected abstract EdmEntityType createEntityType(FullQualifiedName entityTypeName);

  protected abstract EdmEnumType createEnumType(FullQualifiedName enumName);

  protected abstract Map<String, EdmSchema> createSchemas();

  protected abstract EdmTerm createTerm(FullQualifiedName termName);

  protected abstract EdmTypeDefinition createTypeDefinition(FullQualifiedName typeDefinitionName);

  protected abstract EdmAction createUnboundAction(FullQualifiedName actionName);

  protected abstract EdmFunction createUnboundFunction(FullQualifiedName functionName,
    List<String> parameterNames);

  protected abstract List<EdmFunction> createUnboundFunctions(FullQualifiedName functionName);

  @Override
  public EdmAnnotations getAnnotationGroup(final FullQualifiedName targetName,
    final String qualifier) {
    final FullQualifiedName fqn = resolvePossibleAlias(targetName);
    final TargetQualifierMapKey key = new TargetQualifierMapKey(fqn, qualifier);
    EdmAnnotations _annotations = this.annotationGroups.get(key);
    if (_annotations == null) {
      _annotations = createAnnotationGroup(fqn, qualifier);
      if (_annotations != null) {
        this.annotationGroups.put(key, _annotations);
      }
    }
    return _annotations;
  }

  protected Map<String, List<CsdlAnnotation>> getAnnotationsMap() {
    return this.annotationMap;
  }

  @Override
  public EdmAction getBoundAction(final FullQualifiedName actionName,
    final FullQualifiedName bindingParameterTypeName, final Boolean isBindingParameterCollection) {

    final FullQualifiedName actionFqn = resolvePossibleAlias(actionName);
    final FullQualifiedName bindingParameterTypeFqn = resolvePossibleAlias(
      bindingParameterTypeName);
    final ActionMapKey key = new ActionMapKey(actionFqn, bindingParameterTypeFqn,
      isBindingParameterCollection);
    EdmAction action = this.boundActions.get(key);
    if (action == null) {
      action = createBoundAction(actionFqn, bindingParameterTypeFqn, isBindingParameterCollection);
      if (action != null) {
        this.boundActions.put(key, action);
      }
    }

    return action;
  }

  @Override
  public EdmAction getBoundActionWithBindingType(final FullQualifiedName bindingParameterTypeName,
    final Boolean isBindingParameterCollection) {
    for (final EdmSchema schema : getSchemas()) {
      for (final EdmAction action : schema.getActions()) {
        if (action.isBound()) {
          final EdmParameter bindingParameter = action
            .getParameter(action.getParameterNames().get(0));
          if (bindingParameter.getType().getFullQualifiedName().equals(bindingParameterTypeName)
            && bindingParameter.isCollection() == isBindingParameterCollection) {
            return action;
          }
        }
      }
    }
    return null;
  }

  @Override
  public EdmFunction getBoundFunction(final FullQualifiedName functionName,
    final FullQualifiedName bindingParameterTypeName, final Boolean isBindingParameterCollection,
    final List<String> parameterNames) {

    final FullQualifiedName functionFqn = resolvePossibleAlias(functionName);
    final FullQualifiedName bindingParameterTypeFqn = resolvePossibleAlias(
      bindingParameterTypeName);
    final FunctionMapKey key = new FunctionMapKey(functionFqn, bindingParameterTypeFqn,
      isBindingParameterCollection, parameterNames);
    EdmFunction function = this.boundFunctions.get(key);
    if (function == null) {
      function = createBoundFunction(functionFqn, bindingParameterTypeFqn,
        isBindingParameterCollection, parameterNames);
      if (function != null) {
        this.boundFunctions.put(key, function);
      }
    }

    return function;
  }

  @Override
  public List<EdmFunction> getBoundFunctionsWithBindingType(
    final FullQualifiedName bindingParameterTypeName, final Boolean isBindingParameterCollection) {
    final List<EdmFunction> functions = new ArrayList<>();
    for (final EdmSchema schema : getSchemas()) {
      for (final EdmFunction function : schema.getFunctions()) {
        if (function.isBound()) {
          final EdmParameter bindingParameter = function
            .getParameter(function.getParameterNames().get(0));
          if (bindingParameter.getType().getFullQualifiedName().equals(bindingParameterTypeName)
            && bindingParameter.isCollection() == isBindingParameterCollection) {
            functions.add(function);
          }
        }
      }
    }
    return functions;
  }

  @Override
  public EdmComplexType getComplexType(final FullQualifiedName namespaceOrAliasFQN) {
    final FullQualifiedName fqn = resolvePossibleAlias(namespaceOrAliasFQN);
    EdmComplexType complexType = this.complexTypes.get(fqn);
    if (complexType == null) {
      complexType = createComplexType(fqn);
      if (complexType != null) {
        this.complexTypes.put(fqn, complexType);
      }
    }
    return complexType;
  }

  @Override
  public EdmComplexType getComplexTypeWithAnnotations(final FullQualifiedName namespaceOrAliasFQN) {
    final FullQualifiedName fqn = resolvePossibleAlias(namespaceOrAliasFQN);
    EdmComplexType complexType = this.complexTypesWithAnnotations.get(fqn);
    if (complexType == null) {
      complexType = createComplexType(fqn);
      if (complexType != null) {
        this.complexTypesWithAnnotations.put(fqn, complexType);
      }
    }
    setIsPreviousES(false);
    return complexType;
  }

  protected EdmComplexType getComplexTypeWithAnnotations(
    final FullQualifiedName namespaceOrAliasFQN, final boolean isComplexDerivedFromES) {
    this.isComplexDerivedFromES = isComplexDerivedFromES;
    final FullQualifiedName fqn = resolvePossibleAlias(namespaceOrAliasFQN);
    if (!isPreviousES() && getEntityContainer() != null) {
      getEntityContainer().getEntitySetsWithAnnotations();
    }
    EdmComplexType complexType = this.complexTypesDerivedFromES.get(fqn);
    if (complexType == null) {
      complexType = createComplexType(fqn);
      if (complexType != null) {
        this.complexTypesDerivedFromES.put(fqn, complexType);
      }
    }
    this.isComplexDerivedFromES = false;
    return complexType;
  }

  @Override
  public EdmEntityContainer getEntityContainer() {
    return getEntityContainer(null);
  }

  @Override
  public EdmEntityContainer getEntityContainer(final FullQualifiedName namespaceOrAliasFQN) {
    final FullQualifiedName fqn = resolvePossibleAlias(namespaceOrAliasFQN);
    EdmEntityContainer container = this.entityContainers.get(fqn);
    if (container == null) {
      container = createEntityContainer(fqn);
      if (container != null) {
        this.entityContainers.put(fqn, container);
        if (fqn == null) {
          this.entityContainers
            .put(new FullQualifiedName(container.getNamespace(), container.getName()), container);
        }
      }
    }
    return container;
  }

  @Override
  public EdmEntityType getEntityType(final FullQualifiedName namespaceOrAliasFQN) {
    final FullQualifiedName fqn = resolvePossibleAlias(namespaceOrAliasFQN);
    EdmEntityType entityType = this.entityTypes.get(fqn);
    if (entityType == null) {
      entityType = createEntityType(fqn);
      if (entityType != null) {
        this.entityTypes.put(fqn, entityType);
      }
    }
    return entityType;
  }

  @Override
  public EdmEntityType getEntityTypeWithAnnotations(final FullQualifiedName namespaceOrAliasFQN) {
    final FullQualifiedName fqn = resolvePossibleAlias(namespaceOrAliasFQN);
    EdmEntityType entityType = this.entityTypesWithAnnotations.get(fqn);
    if (entityType == null) {
      entityType = createEntityType(fqn);
      if (entityType != null) {
        this.entityTypesWithAnnotations.put(fqn, entityType);
      }
    }
    setIsPreviousES(false);
    return entityType;
  }

  protected EdmEntityType getEntityTypeWithAnnotations(final FullQualifiedName namespaceOrAliasFQN,
    final boolean isEntityDerivedFromES) {
    this.isEntityDerivedFromES = isEntityDerivedFromES;
    final FullQualifiedName fqn = resolvePossibleAlias(namespaceOrAliasFQN);
    if (!isPreviousES() && getEntityContainer() != null) {
      getEntityContainer().getEntitySetsWithAnnotations();
    }
    EdmEntityType entityType = this.entityTypesDerivedFromES.get(fqn);
    if (entityType == null) {
      entityType = createEntityType(fqn);
      if (entityType != null) {
        this.entityTypesDerivedFromES.put(fqn, entityType);
      }
    }
    this.isEntityDerivedFromES = false;
    return entityType;
  }

  @Override
  public EdmEnumType getEnumType(final FullQualifiedName namespaceOrAliasFQN) {
    final FullQualifiedName fqn = resolvePossibleAlias(namespaceOrAliasFQN);
    EdmEnumType enumType = this.enumTypes.get(fqn);
    if (enumType == null) {
      enumType = createEnumType(fqn);
      if (enumType != null) {
        this.enumTypes.put(fqn, enumType);
      }
    }
    return enumType;
  }

  @Override
  public EdmSchema getSchema(final String namespace) {
    if (this.schemas == null) {
      initSchemas();
    }

    EdmSchema schema = this.schemas.get(namespace);
    if (schema == null) {
      schema = this.schemas.get(this.aliasToNamespaceInfo.get(namespace));
    }
    return schema;
  }

  @Override
  public List<EdmSchema> getSchemas() {
    if (this.schemaList == null) {
      initSchemas();
    }
    return this.schemaList;
  }

  @Override
  public EdmTerm getTerm(final FullQualifiedName termName) {
    final FullQualifiedName fqn = resolvePossibleAlias(termName);
    EdmTerm term = this.terms.get(fqn);
    if (term == null) {
      term = createTerm(fqn);
      if (term != null) {
        this.terms.put(fqn, term);
      }
    }
    return term;
  }

  @Override
  public EdmTypeDefinition getTypeDefinition(final FullQualifiedName namespaceOrAliasFQN) {
    final FullQualifiedName fqn = resolvePossibleAlias(namespaceOrAliasFQN);
    EdmTypeDefinition typeDefinition = this.typeDefinitions.get(fqn);
    if (typeDefinition == null) {
      typeDefinition = createTypeDefinition(fqn);
      if (typeDefinition != null) {
        this.typeDefinitions.put(fqn, typeDefinition);
      }
    }
    return typeDefinition;
  }

  @Override
  public EdmAction getUnboundAction(final FullQualifiedName actionName) {
    final FullQualifiedName fqn = resolvePossibleAlias(actionName);
    EdmAction action = this.unboundActions.get(fqn);
    if (action == null) {
      action = createUnboundAction(fqn);
      if (action != null) {
        this.unboundActions.put(actionName, action);
      }
    }

    return action;
  }

  @Override
  public EdmFunction getUnboundFunction(final FullQualifiedName functionName,
    final List<String> parameterNames) {
    final FullQualifiedName functionFqn = resolvePossibleAlias(functionName);

    final FunctionMapKey key = new FunctionMapKey(functionFqn, null, null, parameterNames);
    EdmFunction function = this.unboundFunctionsByKey.get(key);
    if (function == null) {
      function = createUnboundFunction(functionFqn, parameterNames);
      if (function != null) {
        this.unboundFunctionsByKey.put(key, function);
      }
    }

    return function;
  }

  @Override
  public List<EdmFunction> getUnboundFunctions(final FullQualifiedName functionName) {
    final FullQualifiedName functionFqn = resolvePossibleAlias(functionName);

    List<EdmFunction> functions = this.unboundFunctionsByName.get(functionFqn);
    if (functions == null) {
      functions = createUnboundFunctions(functionFqn);
      if (functions != null) {
        this.unboundFunctionsByName.put(functionFqn, functions);

        for (final EdmFunction unbound : functions) {
          final FunctionMapKey key = new FunctionMapKey(
            new FullQualifiedName(unbound.getNamespace(), unbound.getName()),
            unbound.getBindingParameterTypeFqn(), unbound.isBindingParameterTypeCollection(),
            unbound.getParameterNames());
          this.unboundFunctionsByKey.put(key, unbound);
        }
      }
    }

    return functions;
  }

  private void initSchemas() {
    loadAliasToNamespaceInfo();
    final Map<String, EdmSchema> localSchemas = createSchemas();
    this.schemas = Collections.synchronizedMap(localSchemas);

    this.schemaList = Collections.unmodifiableList(new ArrayList<>(this.schemas.values()));
  }

  protected boolean isComplexDerivedFromES() {
    return this.isComplexDerivedFromES;
  }

  protected boolean isEntityDerivedFromES() {
    return this.isEntityDerivedFromES;
  }

  protected boolean isPreviousES() {
    return this.isPreviousES;
  }

  private void loadAliasToNamespaceInfo() {
    final Map<String, String> localAliasToNamespaceInfo = createAliasToNamespaceInfo();
    this.aliasToNamespaceInfo = Collections.synchronizedMap(localAliasToNamespaceInfo);
  }

  private FullQualifiedName resolvePossibleAlias(final FullQualifiedName namespaceOrAliasFQN) {
    if (this.aliasToNamespaceInfo == null) {
      loadAliasToNamespaceInfo();
    }
    FullQualifiedName finalFQN = null;
    if (namespaceOrAliasFQN != null) {
      final String namespace = this.aliasToNamespaceInfo.get(namespaceOrAliasFQN.getNamespace());
      // If not contained in info it must be a namespace
      if (namespace == null) {
        finalFQN = namespaceOrAliasFQN;
      } else {
        finalFQN = new FullQualifiedName(namespace, namespaceOrAliasFQN.getName());
      }
    }
    return finalFQN;
  }

  protected void setIsPreviousES(final boolean isPreviousES) {
    this.isPreviousES = isPreviousES;
  }
}
