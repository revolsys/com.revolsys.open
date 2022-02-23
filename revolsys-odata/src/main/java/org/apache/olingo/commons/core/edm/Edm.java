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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.EdmAction;
import org.apache.olingo.commons.api.edm.EdmAnnotations;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntityContainer;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmException;
import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmParameter;
import org.apache.olingo.commons.api.edm.EdmSchema;
import org.apache.olingo.commons.api.edm.EdmTerm;
import org.apache.olingo.commons.api.edm.EdmTypeDefinition;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlAliasInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotations;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlOperation;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edm.provider.CsdlStructuralType;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlTypeDefinition;
import org.apache.olingo.commons.api.ex.ODataException;

/**
 * Entity Data Model (EDM)
 * <br/>
 * Interface representing a Entity Data Model as described in the Conceptual Schema Definition.
 */
public class Edm {

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

  private final CsdlEdmProvider provider;

  private final Map<FullQualifiedName, List<CsdlAction>> actionsMap = Collections
    .synchronizedMap(new HashMap<FullQualifiedName, List<CsdlAction>>());

  private final Map<FullQualifiedName, List<CsdlFunction>> functionsMap = Collections
    .synchronizedMap(new HashMap<FullQualifiedName, List<CsdlFunction>>());

  private List<CsdlSchema> termSchemaDefinition = new ArrayList<>();

  public Edm(final CsdlEdmProvider provider) {
    this.provider = provider;
  }

  public Edm(final CsdlEdmProvider provider, final List<CsdlSchema> termSchemaDefinition) {
    this.provider = provider;
    this.termSchemaDefinition = termSchemaDefinition;
    populateAnnotationMap();
  }

  /**
   * @param csdlEntityContainer
   * @param annotations
   */
  private void addAnnotationsOnEntityContainer(final CsdlEntityContainer csdlEntityContainer,
    final List<CsdlAnnotation> annotations) {
    if (null != annotations) {
      for (final CsdlAnnotation annotation : annotations) {
        if (!compareAnnotations(csdlEntityContainer.getAnnotations(), annotation)) {
          csdlEntityContainer.getAnnotations().add(annotation);
        }
      }
    }
  }

  /**
   * @param enumType
   * @param annotations
   */
  private void addAnnotationsOnEnumTypes(final CsdlEnumType enumType,
    final List<CsdlAnnotation> annotations) {
    if (null != annotations) {
      for (final CsdlAnnotation annotation : annotations) {
        if (!compareAnnotations(enumType.getAnnotations(), annotation)) {
          enumType.getAnnotations().add(annotation);
        }
      }
    }
  }

  /**
   * Adds annotations to navigation properties of entity and complex types
   * @param structuralType
   * @param navProperty
   * @param navPropAnnotations
   */
  private void addAnnotationsOnNavProperties(final CsdlStructuralType structuralType,
    final CsdlNavigationProperty navProperty, final List<CsdlAnnotation> navPropAnnotations) {
    if (null != navPropAnnotations && !navPropAnnotations.isEmpty()) {
      for (final CsdlAnnotation annotation : navPropAnnotations) {
        if (!compareAnnotations(
          structuralType.getNavigationProperty(navProperty.getName()).getAnnotations(),
          annotation)) {
          structuralType.getNavigationProperty(navProperty.getName())
            .getAnnotations()
            .add(annotation);
        }
      }
    }
  }

  /**
   * Adds annotations to properties of entity type and complex type
   * @param structuralType
   * @param property
   * @param propAnnotations
   */
  private void addAnnotationsOnPropertiesOfStructuralType(final CsdlStructuralType structuralType,
    final CsdlProperty property, final List<CsdlAnnotation> propAnnotations) {
    if (null != propAnnotations && !propAnnotations.isEmpty()) {
      for (final CsdlAnnotation annotation : propAnnotations) {
        if (!compareAnnotations(structuralType.getProperty(property.getName()).getAnnotations(),
          annotation)) {
          structuralType.getProperty(property.getName()).getAnnotations().add(annotation);
        }
      }
    }
  }

  /**
   * Add annoations to entity types and complex types
   * @param entityType
   * @param annotations
   */
  private void addAnnotationsOnStructuralType(final CsdlStructuralType structuralType,
    final List<CsdlAnnotation> annotations) {
    if (null != annotations && !annotations.isEmpty()) {
      for (final CsdlAnnotation annotation : annotations) {
        if (!compareAnnotations(structuralType.getAnnotations(), annotation)) {
          structuralType.getAnnotations().add(annotation);
        }
      }
    }
  }

  /**
   * @param typeDefinition
   * @param annotations
   */
  private void addAnnotationsOnTypeDefinitions(final CsdlTypeDefinition typeDefinition,
    final List<CsdlAnnotation> annotations) {
    if (null != annotations) {
      for (final CsdlAnnotation annotation : annotations) {
        if (!compareAnnotations(typeDefinition.getAnnotations(), annotation)) {
          typeDefinition.getAnnotations().add(annotation);
        }
      }
    }
  }

  /** Adds annotations to action
   * @param operation
   * @param annotationsOnAlias
   */
  private void addAnnotationsToOperations(final CsdlOperation operation,
    final List<CsdlAnnotation> annotations) {
    for (final CsdlAnnotation annotation : annotations) {
      if (!compareAnnotations(operation.getAnnotations(), annotation)) {
        operation.getAnnotations().add(annotation);
      }
    }
  }

  /** Adds annotations to action parameters
   * @param operation
   * @param actionName
   * @param annotations
   */
  private void addAnnotationsToParamsOfOperations(final CsdlOperation operation,
    final FullQualifiedName actionName) {
    final List<CsdlParameter> parameters = operation.getParameters();
    for (final CsdlParameter parameter : parameters) {
      final List<CsdlAnnotation> annotsToParams = getAnnotationsMap()
        .get(actionName.getFullQualifiedNameAsString() + "/" + parameter.getName());
      if (null != annotsToParams && !annotsToParams.isEmpty()) {
        for (final CsdlAnnotation annotation : annotsToParams) {
          if (!compareAnnotations(operation.getParameter(parameter.getName()).getAnnotations(),
            annotation)) {
            operation.getParameter(parameter.getName()).getAnnotations().add(annotation);
          }
        }
      }
      final String aliasName = getAliasInfo(actionName.getNamespace());
      final List<CsdlAnnotation> annotsToParamsOnAlias = getAnnotationsMap()
        .get(aliasName + "." + actionName.getName() + "/" + parameter.getName());
      if (null != annotsToParamsOnAlias && !annotsToParamsOnAlias.isEmpty()) {
        for (final CsdlAnnotation annotation : annotsToParamsOnAlias) {
          if (!compareAnnotations(operation.getParameter(parameter.getName()).getAnnotations(),
            annotation)) {
            operation.getParameter(parameter.getName()).getAnnotations().add(annotation);
          }
        }
      }
    }
  }

  public void addEntityContainerAnnotations(final CsdlEntityContainer csdlEntityContainer,
    final FullQualifiedName containerName) {
    final String aliasName = getAliasInfo(containerName.getNamespace());
    final List<CsdlAnnotation> annotations = getAnnotationsMap()
      .get(containerName.getFullQualifiedNameAsString());
    final List<CsdlAnnotation> annotationsOnAlias = getAnnotationsMap()
      .get(aliasName + "." + containerName.getName());
    addAnnotationsOnEntityContainer(csdlEntityContainer, annotations);
    addAnnotationsOnEntityContainer(csdlEntityContainer, annotationsOnAlias);
  }

  public void addEnumTypeAnnotations(final CsdlEnumType enumType,
    final FullQualifiedName enumName) {
    final String aliasName = getAliasInfo(enumName.getNamespace());
    final List<CsdlAnnotation> annotations = getAnnotationsMap()
      .get(enumName.getFullQualifiedNameAsString());
    final List<CsdlAnnotation> annotationsOnAlias = getAnnotationsMap()
      .get(aliasName + "." + enumName.getName());
    addAnnotationsOnEnumTypes(enumType, annotations);
    addAnnotationsOnEnumTypes(enumType, annotationsOnAlias);
  }

  public void addOperationsAnnotations(final CsdlOperation operation,
    final FullQualifiedName actionName) {
    final String aliasName = getAliasInfo(actionName.getNamespace());
    final List<CsdlAnnotation> annotations = getAnnotationsMap()
      .get(actionName.getFullQualifiedNameAsString());
    final List<CsdlAnnotation> annotationsOnAlias = getAnnotationsMap()
      .get(aliasName + "." + actionName.getName());
    if (null != annotations) {
      addAnnotationsToOperations(operation, annotations);
    }
    if (null != annotationsOnAlias) {
      addAnnotationsToOperations(operation, annotationsOnAlias);
    }
    addAnnotationsToParamsOfOperations(operation, actionName);
  }

  /**
   * Add the annotations defined in an external file to the property/
   * navigation property and the entity
   * @param structuralType
   * @param typeName
   * @param csdlEntityContainer
   */
  public void addStructuralTypeAnnotations(final CsdlStructuralType structuralType,
    final FullQualifiedName typeName, final CsdlEntityContainer csdlEntityContainer) {
    updateAnnotationsOnStructuralProperties(structuralType, typeName, csdlEntityContainer);
    updateAnnotationsOnStructuralNavProperties(structuralType, typeName, csdlEntityContainer);
  }

  public void addTypeDefnAnnotations(final CsdlTypeDefinition typeDefinition,
    final FullQualifiedName typeDefinitionName) {
    final String aliasName = getAliasInfo(typeDefinitionName.getNamespace());
    final List<CsdlAnnotation> annotations = getAnnotationsMap()
      .get(typeDefinitionName.getFullQualifiedNameAsString());
    final List<CsdlAnnotation> annotationsOnAlias = getAnnotationsMap()
      .get(aliasName + "." + typeDefinitionName.getName());
    addAnnotationsOnTypeDefinitions(typeDefinition, annotations);
    addAnnotationsOnTypeDefinitions(typeDefinition, annotationsOnAlias);
  }

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

  private boolean compareAnnotations(final List<CsdlAnnotation> annotations,
    final CsdlAnnotation annotation) {
    for (final CsdlAnnotation annot : annotations) {
      if (annot.equals(annotation)) {
        return true;
      }
    }
    return false;
  }

  protected Map<String, String> createAliasToNamespaceInfo() {
    final Map<String, String> aliasToNamespaceInfos = new HashMap<>();
    try {
      final List<CsdlAliasInfo> aliasInfos = this.provider.getAliasInfos();
      if (aliasInfos != null) {
        for (final CsdlAliasInfo info : aliasInfos) {
          aliasToNamespaceInfos.put(info.getAlias(), info.getNamespace());
        }
      }
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
    return aliasToNamespaceInfos;
  }

  protected EdmAnnotations createAnnotationGroup(final FullQualifiedName targetName,
    final String qualifier) {
    try {
      CsdlAnnotations providerGroup = this.provider.getAnnotationsGroup(targetName, qualifier);
      if (null == providerGroup) {
        for (final CsdlSchema schema : this.termSchemaDefinition) {
          providerGroup = schema.getAnnotationGroup(targetName.getFullQualifiedNameAsString(),
            qualifier);
          break;
        }
      }
      if (providerGroup != null) {
        return new EdmAnnotationsImpl(this, providerGroup);
      }
      return null;
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
  }

  public EdmAction createBoundAction(final FullQualifiedName actionName,
    final FullQualifiedName bindingParameterTypeName, final Boolean isBindingParameterCollection) {

    try {
      List<CsdlAction> actions = this.actionsMap.get(actionName);
      if (actions == null) {
        actions = this.provider.getActions(actionName);
        if (actions == null) {
          return null;
        } else {
          this.actionsMap.put(actionName, actions);
        }
      }
      // Search for bound action where binding parameter matches
      for (final CsdlAction action : actions) {
        if (action.isBound()) {
          final List<CsdlParameter> parameters = action.getParameters();
          final CsdlParameter parameter = parameters.get(0);
          if ((bindingParameterTypeName.equals(parameter.getTypeFQN())
            || isEntityPreviousTypeCompatibleToBindingParam(bindingParameterTypeName, parameter)
            || isComplexPreviousTypeCompatibleToBindingParam(bindingParameterTypeName, parameter,
              isBindingParameterCollection))
            && isBindingParameterCollection.booleanValue() == parameter.isCollection()) {
            addOperationsAnnotations(action, actionName);
            return new EdmActionImpl(this, actionName, action);
          }

        }
      }
      return null;
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
  }

  public EdmFunction createBoundFunction(final FullQualifiedName functionName,
    final FullQualifiedName bindingParameterTypeName, final Boolean isBindingParameterCollection,
    final List<String> parameterNames) {

    try {
      List<CsdlFunction> functions = this.functionsMap.get(functionName);
      if (functions == null) {
        functions = this.provider.getFunctions(functionName);
        if (functions == null) {
          return null;
        } else {
          this.functionsMap.put(functionName, functions);
        }
      }
      final List<String> parameterNamesCopy = parameterNames == null
        ? Collections.<String> emptyList()
        : parameterNames;
      for (final CsdlFunction function : functions) {
        if (function.isBound()) {
          final List<CsdlParameter> providerParameters = function.getParameters();
          if (providerParameters == null || providerParameters.isEmpty()) {
            throw new EdmException("No parameter specified for bound function: " + functionName);
          }
          final CsdlParameter bindingParameter = providerParameters.get(0);
          if ((bindingParameterTypeName.equals(bindingParameter.getTypeFQN())
            || isEntityPreviousTypeCompatibleToBindingParam(bindingParameterTypeName,
              bindingParameter)
            || isComplexPreviousTypeCompatibleToBindingParam(bindingParameterTypeName,
              bindingParameter, isBindingParameterCollection))
            && isBindingParameterCollection.booleanValue() == bindingParameter.isCollection()
            && parameterNamesCopy.size() == providerParameters.size() - 1) {

            final List<String> providerParameterNames = new ArrayList<>();
            for (int i = 1; i < providerParameters.size(); i++) {
              providerParameterNames.add(providerParameters.get(i).getName());
            }
            if (parameterNamesCopy.containsAll(providerParameterNames)) {
              addOperationsAnnotations(function, functionName);
              return new EdmFunctionImpl(this, functionName, function);
            }
          }
        }
      }
      return null;
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
  }

  public EdmComplexType createComplexType(final FullQualifiedName complexTypeName) {
    try {
      final CsdlComplexType complexType = this.provider.getComplexType(complexTypeName);
      if (complexType != null) {
        final List<CsdlAnnotation> annotations = getAnnotationsMap()
          .get(complexTypeName.getFullQualifiedNameAsString());
        if (null != annotations && !annotations.isEmpty()) {
          addAnnotationsOnStructuralType(complexType, annotations);
        }
        final String aliasName = getAliasInfo(complexTypeName.getNamespace());
        final List<CsdlAnnotation> annotationsOnAlias = getAnnotationsMap()
          .get(aliasName + "." + complexTypeName.getName());
        if (null != annotationsOnAlias && !annotationsOnAlias.isEmpty()) {
          addAnnotationsOnStructuralType(complexType, annotationsOnAlias);
        }

        if (!isComplexDerivedFromES()) {
          addStructuralTypeAnnotations(complexType, complexTypeName,
            this.provider.getEntityContainer());
        }
        return new EdmComplexTypeImpl(this, complexTypeName, complexType);
      }
      return null;
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
  }

  public EdmEntityContainer createEntityContainer(final FullQualifiedName containerName) {
    try {
      final CsdlEntityContainerInfo entityContainerInfo = this.provider
        .getEntityContainerInfo(containerName);
      if (entityContainerInfo != null) {
        final CsdlEntityContainer entityContainer = this.provider.getEntityContainer();
        addEntityContainerAnnotations(entityContainer, entityContainerInfo.getContainerName());
        return new EdmEntityContainerImpl(this, this.provider,
          entityContainerInfo.getContainerName(), entityContainer);
      }
      return null;
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
  }

  public EdmEntityType createEntityType(final FullQualifiedName entityTypeName) {
    try {
      final CsdlEntityType entityType = this.provider.getEntityType(entityTypeName);
      if (entityType != null) {
        final List<CsdlAnnotation> annotations = getAnnotationsMap()
          .get(entityTypeName.getFullQualifiedNameAsString());
        final String aliasName = getAliasInfo(entityTypeName.getNamespace());
        final List<CsdlAnnotation> annotationsOnAlias = getAnnotationsMap()
          .get(aliasName + "." + entityTypeName.getName());
        addAnnotationsOnStructuralType(entityType, annotations);
        addAnnotationsOnStructuralType(entityType, annotationsOnAlias);

        if (!isEntityDerivedFromES()) {
          addStructuralTypeAnnotations(entityType, entityTypeName,
            this.provider.getEntityContainer());
        }
        return new EdmEntityTypeImpl(this, entityTypeName, entityType);
      }
      return null;
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
  }

  public EdmEnumType createEnumType(final FullQualifiedName enumName) {
    try {
      final CsdlEnumType enumType = this.provider.getEnumType(enumName);
      if (enumType != null) {
        addEnumTypeAnnotations(enumType, enumName);
        return new EdmEnumTypeImpl(this, enumName, enumType);
      }
      return null;
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
  }

  protected Map<String, EdmSchema> createSchemas() {
    try {
      final Map<String, EdmSchema> providerSchemas = new LinkedHashMap<>();
      final List<CsdlSchema> localSchemas = this.provider.getSchemas();
      if (localSchemas != null) {
        for (final CsdlSchema schema : localSchemas) {
          providerSchemas.put(schema.getNamespace(),
            new EdmSchemaImpl(this, this.provider, schema));
        }
      }
      for (final CsdlSchema termSchemaDefn : this.termSchemaDefinition) {
        providerSchemas.put(termSchemaDefn.getNamespace(),
          new EdmSchemaImpl(this, this.provider, termSchemaDefn));
      }
      return providerSchemas;
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
  }

  protected EdmTerm createTerm(final FullQualifiedName termName) {
    try {
      final CsdlTerm providerTerm = this.provider.getTerm(termName);
      if (providerTerm != null) {
        return new EdmTermImpl(this, termName.getNamespace(), providerTerm);
      } else {
        for (final CsdlSchema schema : this.termSchemaDefinition) {
          if (schema.getNamespace().equalsIgnoreCase(termName.getNamespace())
            || null != schema.getAlias()
              && schema.getAlias().equalsIgnoreCase(termName.getNamespace())) {
            final List<CsdlTerm> terms = schema.getTerms();
            for (final CsdlTerm term : terms) {
              if (term.getName().equals(termName.getName())) {
                return new EdmTermImpl(this, termName.getNamespace(), term);
              }
            }
          }
        }
      }
      return null;
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
  }

  public EdmTypeDefinition createTypeDefinition(final FullQualifiedName typeDefinitionName) {
    try {
      final CsdlTypeDefinition typeDefinition = this.provider.getTypeDefinition(typeDefinitionName);
      if (typeDefinition != null) {
        addTypeDefnAnnotations(typeDefinition, typeDefinitionName);
        return new EdmTypeDefinitionImpl(this, typeDefinitionName, typeDefinition);
      }
      return null;
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
  }

  protected EdmAction createUnboundAction(final FullQualifiedName actionName) {
    try {
      List<CsdlAction> actions = this.actionsMap.get(actionName);
      if (actions == null) {
        actions = this.provider.getActions(actionName);
        if (actions == null) {
          return null;
        } else {
          this.actionsMap.put(actionName, actions);
        }
      }
      // Search for first unbound action
      for (final CsdlAction action : actions) {
        if (!action.isBound()) {
          addOperationsAnnotations(action, actionName);
          return new EdmActionImpl(this, actionName, action);
        }
      }
      return null;
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
  }

  protected EdmFunction createUnboundFunction(final FullQualifiedName functionName,
    final List<String> parameterNames) {
    try {
      List<CsdlFunction> functions = this.functionsMap.get(functionName);
      if (functions == null) {
        functions = this.provider.getFunctions(functionName);
        if (functions == null) {
          return null;
        } else {
          this.functionsMap.put(functionName, functions);
        }
      }

      final List<String> parameterNamesCopy = parameterNames == null
        ? Collections.<String> emptyList()
        : parameterNames;
      for (final CsdlFunction function : functions) {
        if (!function.isBound()) {
          List<CsdlParameter> providerParameters = function.getParameters();
          if (providerParameters == null) {
            providerParameters = Collections.emptyList();
          }
          if (parameterNamesCopy.size() == providerParameters.size()) {
            final List<String> functionParameterNames = new ArrayList<>();
            for (final CsdlParameter parameter : providerParameters) {
              functionParameterNames.add(parameter.getName());
            }

            if (parameterNamesCopy.containsAll(functionParameterNames)) {
              addOperationsAnnotations(function, functionName);
              addAnnotationsToParamsOfOperations(function, functionName);
              return new EdmFunctionImpl(this, functionName, function);
            }
          }
        }
      }
      return null;
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
  }

  protected List<EdmFunction> createUnboundFunctions(final FullQualifiedName functionName) {
    final List<EdmFunction> result = new ArrayList<>();

    try {
      List<CsdlFunction> functions = this.functionsMap.get(functionName);
      if (functions == null) {
        functions = this.provider.getFunctions(functionName);
        if (functions != null) {
          this.functionsMap.put(functionName, functions);
        }
      }
      if (functions != null) {
        for (final CsdlFunction function : functions) {
          if (!function.isBound()) {
            addOperationsAnnotations(function, functionName);
            result.add(new EdmFunctionImpl(this, functionName, function));
          }
        }
      }
    } catch (final ODataException e) {
      throw new EdmException(e);
    }

    return result;
  }

  /**
   * @param schema
   */
  private void fetchAnnotationsInMetadataAndExternalFile(final CsdlSchema schema) {
    final List<CsdlAnnotations> annotationGrps = schema.getAnnotationGroups();
    for (final CsdlAnnotations annotationGrp : annotationGrps) {
      if (!getAnnotationsMap().containsKey(annotationGrp.getTarget())) {
        getAnnotationsMap().put(annotationGrp.getTarget(), annotationGrp.getAnnotations());
      } else {
        final List<CsdlAnnotation> annotations = getAnnotationsMap().get(annotationGrp.getTarget());
        final List<CsdlAnnotation> newAnnotations = new ArrayList<>();
        for (final CsdlAnnotation annotation : annotationGrp.getAnnotations()) {
          if (!compareAnnotations(annotations, annotation)) {
            newAnnotations.add(annotation);
          }
        }
        if (!newAnnotations.isEmpty()) {
          getAnnotationsMap().get(annotationGrp.getTarget()).addAll(newAnnotations);
        }
      }
    }
  }

  /**
   * Get alias name given the namespace from the alias info
   * @param namespace
   * @return
   */
  private String getAliasInfo(final String namespace) {
    try {
      if (null != this.provider.getAliasInfos()) {
        for (final CsdlAliasInfo aliasInfo : this.provider.getAliasInfos()) {
          if (null != aliasInfo.getNamespace()
            && aliasInfo.getNamespace().equalsIgnoreCase(namespace)) {
            return aliasInfo.getAlias();
          }
        }
      }
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
    return null;
  }

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

  public EdmEntityContainer getEntityContainer() {
    return getEntityContainer(null);
  }

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

  public List<EdmSchema> getSchemas() {
    if (this.schemaList == null) {
      initSchemas();
    }
    return this.schemaList;
  }

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

  public List<CsdlSchema> getTermSchemaDefinitions() {
    return this.termSchemaDefinition;
  }

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

  /**
   * @param bindingParameterTypeName
   * @param parameter
   * @param isBindingParameterCollection
   * @return
   * @throws ODataException
   */
  private boolean isComplexPreviousTypeCompatibleToBindingParam(
    final FullQualifiedName bindingParameterTypeName, final CsdlParameter parameter,
    final Boolean isBindingParameterCollection) throws ODataException {
    final CsdlComplexType complexType = this.provider.getComplexType(bindingParameterTypeName);
    if (this.provider.getEntityType(parameter.getTypeFQN()) == null) {
      return false;
    }
    final List<CsdlProperty> properties = this.provider.getEntityType(parameter.getTypeFQN())
      .getProperties();
    for (final CsdlProperty property : properties) {
      final String paramPropertyTypeName = property.getTypeName().getFullQualifiedNameAsString();
      if (complexType != null && complexType.getBaseType() != null
        && complexType.getBaseTypeFQN().getFullQualifiedNameAsString().equals(paramPropertyTypeName)
        || paramPropertyTypeName.equals(bindingParameterTypeName.getFullQualifiedNameAsString())
          && isBindingParameterCollection.booleanValue() == property.isDataTypeCollection()) {
        return true;
      }
    }
    return false;
  }

  protected boolean isEntityDerivedFromES() {
    return this.isEntityDerivedFromES;
  }

  /**
   * @param bindingParameterTypeName
   * @param parameter
   * @return
   * @throws ODataException
   */
  private boolean isEntityPreviousTypeCompatibleToBindingParam(
    final FullQualifiedName bindingParameterTypeName, final CsdlParameter parameter)
    throws ODataException {
    return this.provider.getEntityType(bindingParameterTypeName) != null
      && this.provider.getEntityType(bindingParameterTypeName).getBaseTypeFQN() != null
      && this.provider.getEntityType(bindingParameterTypeName)
        .getBaseTypeFQN()
        .equals(parameter.getTypeFQN());
  }

  protected boolean isPreviousES() {
    return this.isPreviousES;
  }

  private void loadAliasToNamespaceInfo() {
    final Map<String, String> localAliasToNamespaceInfo = createAliasToNamespaceInfo();
    this.aliasToNamespaceInfo = Collections.synchronizedMap(localAliasToNamespaceInfo);
  }

  /**
   * Populates a map of String (annotation target) and List of CsdlAnnotations
   * Reads both term definition schema (external schema) and
   * provider schema (actual metadata file)
   */
  private void populateAnnotationMap() {
    for (final CsdlSchema schema : this.termSchemaDefinition) {
      fetchAnnotationsInMetadataAndExternalFile(schema);
    }
    try {
      if (null != this.provider.getSchemas()) {
        for (final CsdlSchema schema : this.provider.getSchemas()) {
          fetchAnnotationsInMetadataAndExternalFile(schema);
        }
      }
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
  }

  /**
   * Remove the annotations added to navigation properties
   * of a complex type loaded via entity set path
   * @param structuralType
   * @param typeName
   * @param csdlEntityContainer
   * @param navProperties
   * @param entitySets
   */
  private void removeAnnotationsAddedToCTNavPropFromES(final CsdlStructuralType structuralType,
    final FullQualifiedName typeName, final CsdlEntityContainer csdlEntityContainer,
    final List<CsdlNavigationProperty> navProperties, final List<CsdlEntitySet> entitySets) {
    String containerName;
    String schemaName;
    String complexPropName;
    for (final CsdlEntitySet entitySet : entitySets) {
      try {
        final CsdlEntityType entType = this.provider.getEntityType(entitySet.getTypeFQN());
        final List<CsdlProperty> entTypeProperties = null != entType ? entType.getProperties()
          : new ArrayList<>();
        for (final CsdlProperty entTypeProperty : entTypeProperties) {
          if (entTypeProperty.equalsType(typeName)) {
            complexPropName = entTypeProperty.getName();
            containerName = csdlEntityContainer.getName();
            schemaName = typeName.getNamespace();
            for (final CsdlNavigationProperty navProperty : navProperties) {
              final List<CsdlAnnotation> annotPropDerivedFromES = getAnnotationsMap()
                .get(schemaName + "." + containerName + "/" + entitySet.getName() + "/"
                  + complexPropName + "/" + navProperty.getName());
              removeAnnotationsOnNavPropDerivedFromEntitySet(structuralType, navProperty,
                annotPropDerivedFromES);
              String aliasName = getAliasInfo(schemaName);
              final List<CsdlAnnotation> annotPropDerivedFromESOnAlias = getAnnotationsMap()
                .get(aliasName + "." + containerName + "/" + entitySet.getName() + "/"
                  + complexPropName + "/" + navProperty.getName());
              removeAnnotationsOnNavPropDerivedFromEntitySet(structuralType, navProperty,
                annotPropDerivedFromESOnAlias);

              final List<CsdlAnnotation> propAnnotations = getAnnotationsMap()
                .get(typeName.getFullQualifiedNameAsString() + "/" + navProperty.getName());
              addAnnotationsOnNavProperties(structuralType, navProperty, propAnnotations);
              aliasName = getAliasInfo(typeName.getNamespace());
              final List<CsdlAnnotation> propAnnotationsOnAlias = getAnnotationsMap()
                .get(aliasName + "." + typeName.getName() + "/" + navProperty.getName());
              addAnnotationsOnNavProperties(structuralType, navProperty, propAnnotationsOnAlias);
            }
          }
        }
      } catch (final ODataException e) {
        throw new EdmException(e);
      }
    }
    for (final CsdlNavigationProperty navProperty : structuralType.getNavigationProperties()) {
      final List<CsdlAnnotation> propAnnotations = getAnnotationsMap()
        .get(typeName.getFullQualifiedNameAsString() + "/" + navProperty.getName());
      addAnnotationsOnNavProperties(structuralType, navProperty, propAnnotations);
      final String aliasName = getAliasInfo(typeName.getNamespace());
      final List<CsdlAnnotation> propAnnotationsOnAlias = getAnnotationsMap()
        .get(aliasName + "." + typeName.getName() + "/" + navProperty.getName());
      addAnnotationsOnNavProperties(structuralType, navProperty, propAnnotationsOnAlias);
    }
  }

  /**
   * Removes the annotation added on complex type property via Entity Set
   * @param structuralType
   * @param typeName
   * @param csdlEntityContainer
   * @param properties
   * @param entitySets
   */
  private void removeAnnotationsAddedToCTTypePropFromES(final CsdlStructuralType structuralType,
    final FullQualifiedName typeName, final CsdlEntityContainer csdlEntityContainer,
    final List<CsdlProperty> properties, final List<CsdlEntitySet> entitySets) {
    String containerName;
    String schemaName;
    String complexPropName;
    for (final CsdlEntitySet entitySet : entitySets) {
      try {
        final CsdlEntityType entType = this.provider.getEntityType(entitySet.getTypeFQN());
        final List<CsdlProperty> entTypeProperties = null != entType ? entType.getProperties()
          : new ArrayList<>();
        for (final CsdlProperty entTypeProperty : entTypeProperties) {
          if (null != entTypeProperty.getType()
            && entTypeProperty.getType().endsWith("." + structuralType.getName())) {
            complexPropName = entTypeProperty.getName();
            containerName = csdlEntityContainer.getName();
            schemaName = typeName.getNamespace();
            for (final CsdlProperty property : properties) {
              final List<CsdlAnnotation> annotPropDerivedFromES = getAnnotationsMap()
                .get(schemaName + "." + containerName + "/" + entitySet.getName() + "/"
                  + complexPropName + "/" + property.getName());
              removeAnnotationsOnPropDerivedFromEntitySet(structuralType, property,
                annotPropDerivedFromES);
              String aliasName = getAliasInfo(schemaName);
              final List<CsdlAnnotation> annotPropDerivedFromESOnAlias = getAnnotationsMap()
                .get(aliasName + "." + containerName + "/" + entitySet.getName() + "/"
                  + complexPropName + "/" + property.getName());
              removeAnnotationsOnPropDerivedFromEntitySet(structuralType, property,
                annotPropDerivedFromESOnAlias);

              final List<CsdlAnnotation> propAnnotations = getAnnotationsMap()
                .get(typeName.getFullQualifiedNameAsString() + "/" + property.getName());
              addAnnotationsOnPropertiesOfStructuralType(structuralType, property, propAnnotations);
              aliasName = getAliasInfo(typeName.getNamespace());
              final List<CsdlAnnotation> propAnnotationsOnAlias = getAnnotationsMap()
                .get(typeName.getName() + "/" + property.getName());
              addAnnotationsOnPropertiesOfStructuralType(structuralType, property,
                propAnnotationsOnAlias);
            }
          }
        }
      } catch (final ODataException e) {
        throw new EdmException(e);
      }
    }
  }

  /**
   * Removes the annotations added to properties of structural types
   * if annotation was added before via EntitySet path
   * @param structuralType
   * @param navProperty
   * @param annotPropDerivedFromES
   */
  private void removeAnnotationsOnNavPropDerivedFromEntitySet(
    final CsdlStructuralType structuralType, final CsdlNavigationProperty navProperty,
    final List<CsdlAnnotation> annotPropDerivedFromES) {
    if (null != annotPropDerivedFromES && !annotPropDerivedFromES.isEmpty()) {
      for (final CsdlAnnotation annotation : annotPropDerivedFromES) {
        final List<CsdlAnnotation> propAnnot = structuralType
          .getNavigationProperty(navProperty.getName())
          .getAnnotations();
        if (propAnnot.contains(annotation)) {
          propAnnot.remove(annotation);
        }
      }
    }
  }

  /**
   * Removes the annotations added to properties of entity type when added via entity set
   * @param structuralType
   * @param property
   * @param annotPropDerivedFromESOnAlias
   */
  private void removeAnnotationsOnPropDerivedFromEntitySet(final CsdlStructuralType structuralType,
    final CsdlProperty property, final List<CsdlAnnotation> annotPropDerivedFromES) {
    if (null != annotPropDerivedFromES && !annotPropDerivedFromES.isEmpty()) {
      for (final CsdlAnnotation annotation : annotPropDerivedFromES) {
        final List<CsdlAnnotation> propAnnot = structuralType.getProperty(property.getName())
          .getAnnotations();
        if (propAnnot.contains(annotation)) {
          propAnnot.remove(annotation);
        }
      }
    }
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

  /** Check if annotations are added on navigation properties of a structural type
   * @param structuralType
   * @param typeName
   * @param csdlEntityContainer
   * @param isNavPropAnnotationsCleared
   * @param annotationGrp
   */
  private void updateAnnotationsOnStructuralNavProperties(final CsdlStructuralType structuralType,
    final FullQualifiedName typeName, final CsdlEntityContainer csdlEntityContainer) {
    final List<CsdlNavigationProperty> navProperties = structuralType.getNavigationProperties();
    String containerName = null;
    String schemaName = null;
    String entitySetName = null;
    final List<CsdlEntitySet> entitySets = csdlEntityContainer != null
      ? csdlEntityContainer.getEntitySets()
      : new ArrayList<>();
    if (structuralType instanceof CsdlComplexType) {
      removeAnnotationsAddedToCTNavPropFromES(structuralType, typeName, csdlEntityContainer,
        navProperties, entitySets);
    } else {
      for (final CsdlEntitySet entitySet : entitySets) {
        entitySetName = entitySet.getName();
        final String entityTypeName = entitySet.getTypeFQN().getFullQualifiedNameAsString();
        if (null != entityTypeName
          && entityTypeName.equalsIgnoreCase(typeName.getFullQualifiedNameAsString())) {
          containerName = csdlEntityContainer.getName();
          schemaName = typeName.getNamespace();
          break;
        }
      }
      for (final CsdlNavigationProperty navProperty : navProperties) {
        final List<CsdlAnnotation> annotPropDerivedFromES = getAnnotationsMap().get(
          schemaName + "." + containerName + "/" + entitySetName + "/" + navProperty.getName());
        removeAnnotationsOnNavPropDerivedFromEntitySet(structuralType, navProperty,
          annotPropDerivedFromES);
        String aliasName = getAliasInfo(schemaName);
        final List<CsdlAnnotation> annotPropDerivedFromESOnAlias = getAnnotationsMap()
          .get(aliasName + "." + containerName + "/" + entitySetName + "/" + navProperty.getName());
        removeAnnotationsOnNavPropDerivedFromEntitySet(structuralType, navProperty,
          annotPropDerivedFromESOnAlias);

        final List<CsdlAnnotation> navPropAnnotations = getAnnotationsMap()
          .get(typeName + "/" + navProperty.getName());
        addAnnotationsOnNavProperties(structuralType, navProperty, navPropAnnotations);
        aliasName = getAliasInfo(typeName.getNamespace());
        final List<CsdlAnnotation> navPropAnnotationsOnAlias = getAnnotationsMap()
          .get(aliasName + "." + typeName.getName() + "/" + navProperty.getName());
        addAnnotationsOnNavProperties(structuralType, navProperty, navPropAnnotationsOnAlias);
      }
    }
  }

  /** Check if annotations are added on properties of a structural type
   * @param structuralType
   * @param typeName
   * @param csdlEntityContainer
   */
  private void updateAnnotationsOnStructuralProperties(final CsdlStructuralType structuralType,
    final FullQualifiedName typeName, final CsdlEntityContainer csdlEntityContainer) {
    final List<CsdlProperty> properties = structuralType.getProperties();
    String containerName = null;
    String schemaName = null;
    String entitySetName = null;
    final List<CsdlEntitySet> entitySets = null != csdlEntityContainer
      ? csdlEntityContainer.getEntitySets()
      : new ArrayList<>();
    if (structuralType instanceof CsdlComplexType) {
      removeAnnotationsAddedToCTTypePropFromES(structuralType, typeName, csdlEntityContainer,
        properties, entitySets);
    } else {
      for (final CsdlEntitySet entitySet : entitySets) {
        entitySetName = entitySet.getName();
        final String entityTypeName = entitySet.getTypeFQN().getFullQualifiedNameAsString();
        if (null != entityTypeName
          && entityTypeName.equalsIgnoreCase(typeName.getFullQualifiedNameAsString())) {
          containerName = csdlEntityContainer.getName();
          schemaName = typeName.getNamespace();
          break;
        }
      }
      for (final CsdlProperty property : properties) {
        final List<CsdlAnnotation> annotPropDerivedFromES = getAnnotationsMap()
          .get(schemaName + "." + containerName + "/" + entitySetName + "/" + property.getName());
        removeAnnotationsOnPropDerivedFromEntitySet(structuralType, property,
          annotPropDerivedFromES);
        String aliasName = getAliasInfo(schemaName);
        final List<CsdlAnnotation> annotPropDerivedFromESOnAlias = getAnnotationsMap()
          .get(aliasName + "." + containerName + "/" + entitySetName + "/" + property.getName());
        removeAnnotationsOnPropDerivedFromEntitySet(structuralType, property,
          annotPropDerivedFromESOnAlias);
        final List<CsdlAnnotation> propAnnotations = getAnnotationsMap()
          .get(typeName.getFullQualifiedNameAsString() + "/" + property.getName());
        addAnnotationsOnPropertiesOfStructuralType(structuralType, property, propAnnotations);
        aliasName = getAliasInfo(typeName.getNamespace());
        final List<CsdlAnnotation> propAnnotationsOnAlias = getAnnotationsMap()
          .get(aliasName + "." + typeName.getName() + "/" + property.getName());
        addAnnotationsOnPropertiesOfStructuralType(structuralType, property,
          propAnnotationsOnAlias);
      }
    }
  }
}
