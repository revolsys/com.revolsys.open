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

public class EdmProviderImpl extends AbstractEdm {

  private final CsdlEdmProvider provider;

  private final Map<FullQualifiedName, List<CsdlAction>> actionsMap = Collections
    .synchronizedMap(new HashMap<FullQualifiedName, List<CsdlAction>>());

  private final Map<FullQualifiedName, List<CsdlFunction>> functionsMap = Collections
    .synchronizedMap(new HashMap<FullQualifiedName, List<CsdlFunction>>());

  private List<CsdlSchema> termSchemaDefinition = new ArrayList<>();

  public EdmProviderImpl(final CsdlEdmProvider provider) {
    this.provider = provider;
  }

  public EdmProviderImpl(final CsdlEdmProvider provider,
    final List<CsdlSchema> termSchemaDefinition) {
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

  private boolean compareAnnotations(final List<CsdlAnnotation> annotations,
    final CsdlAnnotation annotation) {
    for (final CsdlAnnotation annot : annotations) {
      if (annot.equals(annotation)) {
        return true;
      }
    }
    return false;
  }

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  public List<CsdlSchema> getTermSchemaDefinitions() {
    return this.termSchemaDefinition;
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
      final String paramPropertyTypeName = property.getTypeName()
        .getFullQualifiedNameAsString();
      if (complexType != null && complexType.getBaseType() != null
        && complexType.getBaseTypeFQN().getFullQualifiedNameAsString().equals(paramPropertyTypeName)
        || paramPropertyTypeName.equals(bindingParameterTypeName.getFullQualifiedNameAsString())
          && isBindingParameterCollection.booleanValue() == property.isDataTypeCollection()) {
        return true;
      }
    }
    return false;
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
