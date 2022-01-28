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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmActionImport;
import org.apache.olingo.commons.api.edm.EdmEntityContainer;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmException;
import org.apache.olingo.commons.api.edm.EdmFunctionImport;
import org.apache.olingo.commons.api.edm.EdmSingleton;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlActionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlAliasInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlComplexType;
import org.apache.olingo.commons.api.edm.provider.CsdlEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlFunctionImport;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlOperationImport;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlSingleton;
import org.apache.olingo.commons.api.ex.ODataException;

public class EdmEntityContainerImpl extends AbstractEdmNamed implements EdmEntityContainer {

  private final CsdlEdmProvider provider;

  private CsdlEntityContainer container;

  private final FullQualifiedName entityContainerName;

  private final FullQualifiedName parentContainerName;

  private List<EdmSingleton> singletons;

  private final Map<String, EdmSingleton> singletonCache = Collections
    .synchronizedMap(new LinkedHashMap<String, EdmSingleton>());

  private List<EdmEntitySet> entitySets;

  private final Map<String, EdmEntitySet> entitySetCache = Collections
    .synchronizedMap(new LinkedHashMap<String, EdmEntitySet>());

  private List<EdmActionImport> actionImports;

  private final Map<String, EdmActionImport> actionImportCache = Collections
    .synchronizedMap(new LinkedHashMap<String, EdmActionImport>());

  private List<EdmFunctionImport> functionImports;

  private final Map<String, EdmFunctionImport> functionImportCache = Collections
    .synchronizedMap(new LinkedHashMap<String, EdmFunctionImport>());

  private boolean isAnnotationsIncluded = false;

  private final Map<String, EdmEntitySet> entitySetWithAnnotationsCache = Collections
    .synchronizedMap(new LinkedHashMap<String, EdmEntitySet>());

  private final Map<String, EdmSingleton> singletonWithAnnotationsCache = Collections
    .synchronizedMap(new LinkedHashMap<String, EdmSingleton>());

  private boolean isSingletonAnnotationsIncluded = false;

  private final String SLASH = "/";

  private final String DOT = ".";

  public EdmEntityContainerImpl(final Edm edm, final CsdlEdmProvider provider,
    final CsdlEntityContainerInfo entityContainerInfo) {
    super(edm, entityContainerInfo.getContainerName().getName(), null);
    this.provider = provider;
    this.entityContainerName = entityContainerInfo.getContainerName();
    this.parentContainerName = entityContainerInfo.getExtendsContainer();
  }

  public EdmEntityContainerImpl(final Edm edm, final CsdlEdmProvider provider,
    final FullQualifiedName containerFQN, final CsdlEntityContainer entityContainer) {
    super(edm, containerFQN.getName(), entityContainer);
    this.provider = provider;
    this.container = entityContainer;
    this.entityContainerName = containerFQN;
    this.parentContainerName = entityContainer == null ? null
      : entityContainer.getExtendsContainerFQN();
  }

  /**
   * Adds annotations on complex type navigation properties
   * @param complexType
   * @param complexNavProperty
   * @param annotations
   */
  private void addAnnotationsOnComplexTypeNavProperties(final CsdlComplexType complexType,
    final CsdlNavigationProperty complexNavProperty, final List<CsdlAnnotation> annotations) {
    if (null != annotations && !annotations.isEmpty()) {
      this.isAnnotationsIncluded = true;
      for (final CsdlAnnotation annotation : annotations) {
        if (!compareAnnotations(
          complexType.getNavigationProperty(complexNavProperty.getName()).getAnnotations(),
          annotation)) {
          complexType.getNavigationProperty(complexNavProperty.getName())
            .getAnnotations()
            .add(annotation);
        }
      }
    }
  }

  /**
   * Adds annotations on complex type properties
   * @param complexType
   * @param complexProperty
   * @param annotations
   */
  private void addAnnotationsOnComplexTypeProperties(final CsdlComplexType complexType,
    final CsdlProperty complexProperty, final List<CsdlAnnotation> annotations) {
    if (null != annotations && !annotations.isEmpty()) {
      this.isAnnotationsIncluded = true;
      for (final CsdlAnnotation annotation : annotations) {
        if (!compareAnnotations(complexType.getProperty(complexProperty.getName()).getAnnotations(),
          annotation)) {
          complexType.getProperty(complexProperty.getName()).getAnnotations().add(annotation);
        }
      }
    }
  }

  /**
   * Adds annotations on entity sets
   * @param entitySet
   * @param annotations
   */
  private void addAnnotationsOnEntitySet(final CsdlEntitySet entitySet,
    final List<CsdlAnnotation> annotations) {
    if (null != annotations && !annotations.isEmpty()) {
      this.isAnnotationsIncluded = true;
      for (final CsdlAnnotation annotation : annotations) {
        if (!compareAnnotations(entitySet.getAnnotations(), annotation)) {
          entitySet.getAnnotations().add(annotation);
        }
      }
    }
  }

  /**
   * @param entityType
   * @param navProperty
   * @param annotations
   */
  private void addAnnotationsOnETNavProperties(final CsdlEntityType entityType,
    final CsdlNavigationProperty navProperty, final List<CsdlAnnotation> annotations) {
    if (null != annotations && !annotations.isEmpty()) {
      this.isAnnotationsIncluded = true;
      for (final CsdlAnnotation annotation : annotations) {
        if (!compareAnnotations(
          entityType.getNavigationProperty(navProperty.getName()).getAnnotations(), annotation)) {
          entityType.getNavigationProperty(navProperty.getName()).getAnnotations().add(annotation);
        }
      }
    }
  }

  /**
   * Adds annotations to Entity type Properties derived from entity set
   * @param entityType
   * @param property
   * @param annotations
   */
  private void addAnnotationsOnETProperties(final CsdlEntityType entityType,
    final CsdlProperty property, final List<CsdlAnnotation> annotations) {
    if (null != annotations && !annotations.isEmpty()) {
      this.isAnnotationsIncluded = true;
      for (final CsdlAnnotation annotation : annotations) {
        if (!compareAnnotations(entityType.getProperty(property.getName()).getAnnotations(),
          annotation)) {
          entityType.getProperty(property.getName()).getAnnotations().add(annotation);
        }
      }
    }
  }

  /**
   * Adds annotations on action import
   * @param operationImport
   * @param annotations
   */
  private void addAnnotationsOnOperationImport(final CsdlOperationImport operationImport,
    final List<CsdlAnnotation> annotations) {
    if (null != annotations && !annotations.isEmpty()) {
      for (final CsdlAnnotation annotation : annotations) {
        if (!compareAnnotations(operationImport.getAnnotations(), annotation)) {
          operationImport.getAnnotations().add(annotation);
        }
      }
    }
  }

  /**
   * Adds annotations on singleton
   * @param singleton
   * @param annotations
   */
  private void addAnnotationsOnSingleton(final CsdlSingleton singleton,
    final List<CsdlAnnotation> annotations) {
    if (null != annotations && !annotations.isEmpty()) {
      this.isSingletonAnnotationsIncluded = true;
      for (final CsdlAnnotation annotation : annotations) {
        if (!compareAnnotations(singleton.getAnnotations(), annotation)) {
          singleton.getAnnotations().add(annotation);
        }
      }
    }
  }

  /**
   * @param entitySet
   * @param entityContainerName
   * @param complexProperty
   * @param complexType
   * @return
   */
  private void addAnnotationsToComplexTypeIncludedFromES(final CsdlEntitySet entitySet,
    final FullQualifiedName entityContainerName, final CsdlProperty complexProperty,
    final CsdlComplexType complexType) {
    final String aliasName = getAliasInfo(entityContainerName.getNamespace());
    for (final CsdlProperty complexPropertyName : complexType.getProperties()) {
      removeAnnotationAddedToPropertiesOfComplexType(complexType, complexPropertyName,
        entityContainerName);

      final List<CsdlAnnotation> annotations = ((EdmProviderImpl)this.edm).getAnnotationsMap()
        .get(entityContainerName + this.SLASH + entitySet.getName() + this.SLASH
          + complexProperty.getName() + this.SLASH + complexPropertyName.getName());
      addAnnotationsOnComplexTypeProperties(complexType, complexPropertyName, annotations);

      final List<CsdlAnnotation> annotationsOnAlias = ((EdmProviderImpl)this.edm)
        .getAnnotationsMap()
        .get(aliasName + this.DOT + entityContainerName.getName() + this.SLASH + entitySet.getName()
          + this.SLASH + complexProperty.getName() + this.SLASH + complexPropertyName.getName());
      addAnnotationsOnComplexTypeProperties(complexType, complexPropertyName, annotationsOnAlias);
    }
    for (final CsdlNavigationProperty complexNavProperty : complexType.getNavigationProperties()) {
      checkAnnotationAddedToNavPropertiesOfComplexType(complexType, complexNavProperty,
        entityContainerName);

      final List<CsdlAnnotation> annotations = ((EdmProviderImpl)this.edm).getAnnotationsMap()
        .get(entityContainerName + this.SLASH + entitySet.getName() + this.SLASH
          + complexProperty.getName() + this.SLASH + complexNavProperty.getName());
      addAnnotationsOnComplexTypeNavProperties(complexType, complexNavProperty, annotations);

      final List<CsdlAnnotation> annotationsOnAlias = ((EdmProviderImpl)this.edm)
        .getAnnotationsMap()
        .get(aliasName + this.DOT + entityContainerName.getName() + this.SLASH + entitySet.getName()
          + this.SLASH + complexProperty.getName() + this.SLASH + complexNavProperty.getName());
      addAnnotationsOnComplexTypeNavProperties(complexType, complexNavProperty, annotationsOnAlias);
    }
  }

  /**
   *
   * @param singleton
   * @param entityContainerName2
   * @param annotationGrp
   * @param propertyName
   * @param isComplexNavPropAnnotationsCleared
   * @param complexType
   */
  private void addAnnotationsToComplexTypeIncludedFromSingleton(final CsdlSingleton singleton,
    final CsdlProperty propertyName, final CsdlComplexType complexType) {
    final String aliasName = getAliasInfo(this.entityContainerName.getNamespace());
    for (final CsdlProperty complexPropertyName : complexType.getProperties()) {
      removeAnnotationAddedToPropertiesOfComplexType(complexType, complexPropertyName,
        this.entityContainerName);

      final List<CsdlAnnotation> annotations = ((EdmProviderImpl)this.edm).getAnnotationsMap()
        .get(this.entityContainerName + this.SLASH + singleton.getName() + this.SLASH
          + propertyName.getName() + this.SLASH + complexPropertyName.getName());
      addAnnotationsOnComplexTypeProperties(complexType, complexPropertyName, annotations);
      final List<CsdlAnnotation> annotationsOnAlias = ((EdmProviderImpl)this.edm)
        .getAnnotationsMap()
        .get(aliasName + this.DOT + this.entityContainerName.getName() + this.SLASH
          + singleton.getName() + this.SLASH + propertyName.getName() + this.SLASH
          + complexPropertyName.getName());
      addAnnotationsOnComplexTypeProperties(complexType, complexPropertyName, annotationsOnAlias);
    }
    for (final CsdlNavigationProperty complexNavPropertyName : complexType
      .getNavigationProperties()) {
      checkAnnotationAddedToNavPropertiesOfComplexType(complexType, complexNavPropertyName,
        this.entityContainerName);

      final List<CsdlAnnotation> annotations = ((EdmProviderImpl)this.edm).getAnnotationsMap()
        .get(this.entityContainerName + this.SLASH + singleton.getName() + this.SLASH
          + propertyName.getName() + this.SLASH + complexNavPropertyName.getName());
      addAnnotationsOnComplexTypeNavProperties(complexType, complexNavPropertyName, annotations);
      final List<CsdlAnnotation> annotationsOnAlias = ((EdmProviderImpl)this.edm)
        .getAnnotationsMap()
        .get(aliasName + this.DOT + this.entityContainerName.getName() + this.SLASH
          + singleton.getName() + this.SLASH + propertyName.getName() + this.SLASH
          + complexNavPropertyName.getName());
      addAnnotationsOnComplexTypeNavProperties(complexType, complexNavPropertyName,
        annotationsOnAlias);
    }
  }

  /**
   * Adds annotations to Entity type Navigation Properties derived from entity set
   * @param entitySet
   * @param entityContainerName
   * @param entityType
   * @param navProperty
   */
  private void addAnnotationsToETNavProperties(final CsdlEntitySet entitySet,
    final FullQualifiedName entityContainerName, final CsdlEntityType entityType,
    final CsdlNavigationProperty navProperty) {
    final List<CsdlAnnotation> annotations = ((EdmProviderImpl)this.edm).getAnnotationsMap()
      .get(entityContainerName + this.SLASH + entitySet.getName() + this.SLASH
        + navProperty.getName());
    addAnnotationsOnETNavProperties(entityType, navProperty, annotations);

    final String aliasName = getAliasInfo(entityContainerName.getNamespace());
    final List<CsdlAnnotation> annotationsOnAlias = ((EdmProviderImpl)this.edm).getAnnotationsMap()
      .get(aliasName + this.DOT + entityContainerName.getName() + this.SLASH + entitySet.getName()
        + this.SLASH + navProperty.getName());
    addAnnotationsOnETNavProperties(entityType, navProperty, annotationsOnAlias);
  }

  /**
   * @param entitySet
   * @param entityContainerName
   * @param entityType
   * @param property
   */
  private void addAnnotationsToETProperties(final CsdlEntitySet entitySet,
    final FullQualifiedName entityContainerName, final CsdlEntityType entityType,
    final CsdlProperty property) {
    final List<CsdlAnnotation> annotations = ((EdmProviderImpl)this.edm).getAnnotationsMap()
      .get(
        entityContainerName + this.SLASH + entitySet.getName() + this.SLASH + property.getName());
    addAnnotationsOnETProperties(entityType, property, annotations);

    final String aliasName = getAliasInfo(entityContainerName.getNamespace());
    final List<CsdlAnnotation> annotationsOnAlias = ((EdmProviderImpl)this.edm).getAnnotationsMap()
      .get(aliasName + this.DOT + entityContainerName.getName() + this.SLASH + entitySet.getName()
        + this.SLASH + property.getName());
    addAnnotationsOnETProperties(entityType, property, annotationsOnAlias);
  }

  /** adds annotations to entity type properties derived from singleton
   * E.g of target paths
   * MySchema.MyEntityContainer/MySingleton/MyComplexProperty/MyNavigationProperty
   * @param singleton
   * @param isPropAnnotationsCleared
   * @param isNavPropAnnotationsCleared
   * @param entityType
   * @param entityContainerName
   * @param annotationGrp
   */
  private void addAnnotationsToPropertiesDerivedFromSingleton(final CsdlSingleton singleton,
    final CsdlEntityType entityType, final FullQualifiedName entityContainerName) {
    String entitySetName = null;
    String schemaName = null;
    String containerName = null;
    try {
      final List<CsdlEntitySet> entitySets = this.provider.getEntityContainer() != null
        ? this.provider.getEntityContainer().getEntitySets()
        : new ArrayList<>();
      for (final CsdlEntitySet entitySet : entitySets) {
        entitySetName = entitySet.getName();
        final String entityTypeName = entitySet.getTypeFQN().getFullQualifiedNameAsString();
        if (null != entityTypeName && entityTypeName.equalsIgnoreCase(
          entitySet.getTypeFQN().getNamespace() + this.DOT + entityType.getName())) {
          containerName = this.provider.getEntityContainer().getName();
          schemaName = entitySet.getTypeFQN().getNamespace();
          for (final CsdlProperty property : entityType.getProperties()) {
            if (isPropertyComplex(property)) {
              final CsdlComplexType complexType = getComplexTypeFromProperty(property);
              addAnnotationsToComplexTypeIncludedFromSingleton(singleton, property, complexType);
            }
            removeAnnotationsAddedToPropertiesOfEntityType(entityType, property,
              entityContainerName);
            removeAnnotationsAddedToPropertiesViaEntitySet(entityType, property, schemaName,
              containerName, entitySetName);
          }
        }
      }
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
  }

  /** Adds annotations to Entity type Properties derived from entity set
   * E.g of target paths
   * MySchema.MyEntityContainer/MyEntitySet/MyProperty
   * MySchema.MyEntityContainer/MyEntitySet/MyNavigationProperty
   * MySchema.MyEntityContainer/MyEntitySet/MyComplexProperty/MyProperty
   * MySchema.MyEntityContainer/MyEntitySet/MyComplexProperty/MyNavigationProperty
   * @param entitySet
   * @param entityContainerName
   * @param entityType
   * @return
   */
  private void addAnnotationsToPropertiesIncludedFromES(final CsdlEntitySet entitySet,
    final FullQualifiedName entityContainerName, final CsdlEntityType entityType) {
    for (final CsdlProperty property : entityType.getProperties()) {
      removeAnnotationsAddedToPropertiesOfEntityType(entityType, property, entityContainerName);
      if (isPropertyComplex(property)) {
        final CsdlComplexType complexType = getComplexTypeFromProperty(property);
        addAnnotationsToComplexTypeIncludedFromES(entitySet, entityContainerName, property,
          complexType);
      } else {
        addAnnotationsToETProperties(entitySet, entityContainerName, entityType, property);
      }
    }
    for (final CsdlNavigationProperty navProperty : entityType.getNavigationProperties()) {
      removeAnnotationAddedToNavProperties(entityType, navProperty, entityContainerName);
      addAnnotationsToETNavProperties(entitySet, entityContainerName, entityType, navProperty);
    }
  }

  private void addEntitySetAnnotations(final CsdlEntitySet entitySet,
    final FullQualifiedName entityContainerName) {
    final CsdlEntityType entityType = getCsdlEntityTypeFromEntitySet(entitySet);
    if (entityType == null) {
      return;
    }

    final List<CsdlAnnotation> annotations = ((EdmProviderImpl)this.edm).getAnnotationsMap()
      .get(entityContainerName + this.SLASH + entitySet.getName());
    addAnnotationsOnEntitySet(entitySet, annotations);
    final String aliasName = getAliasInfo(entityContainerName.getNamespace());
    final List<CsdlAnnotation> annotationsOnAlias = ((EdmProviderImpl)this.edm).getAnnotationsMap()
      .get(aliasName + this.DOT + entityContainerName.getName() + this.SLASH + entitySet.getName());
    addAnnotationsOnEntitySet(entitySet, annotationsOnAlias);
    addAnnotationsToPropertiesIncludedFromES(entitySet, entityContainerName, entityType);
  }

  private void addOperationImportAnnotations(final CsdlOperationImport operationImport,
    final FullQualifiedName entityContainerName) {
    final List<CsdlAnnotation> annotations = ((EdmProviderImpl)this.edm).getAnnotationsMap()
      .get(entityContainerName + this.SLASH + operationImport.getName());
    addAnnotationsOnOperationImport(operationImport, annotations);

    final String aliasName = getAliasInfo(entityContainerName.getNamespace());
    final List<CsdlAnnotation> annotationsOnAlias = ((EdmProviderImpl)this.edm).getAnnotationsMap()
      .get(aliasName + this.DOT + entityContainerName.getName() + this.SLASH
        + operationImport.getName());
    addAnnotationsOnOperationImport(operationImport, annotationsOnAlias);
  }

  private void addSingletonAnnotations(final CsdlSingleton singleton,
    final FullQualifiedName entityContainerName) {
    final CsdlEntityType entityType = fetchEntityTypeFromSingleton(singleton);
    if (entityType == null) {
      return;
    }
    final List<CsdlAnnotation> annotations = ((EdmProviderImpl)this.edm).getAnnotationsMap()
      .get(entityContainerName + this.SLASH + singleton.getName());
    addAnnotationsOnSingleton(singleton, annotations);
    final String aliasName = getAliasInfo(entityContainerName.getNamespace());
    final List<CsdlAnnotation> annotationsOnAlias = ((EdmProviderImpl)this.edm).getAnnotationsMap()
      .get(aliasName + this.DOT + entityContainerName.getName() + this.SLASH + singleton.getName());
    addAnnotationsOnSingleton(singleton, annotationsOnAlias);
    addAnnotationsToPropertiesDerivedFromSingleton(singleton, entityType, entityContainerName);
  }

  private void checkAnnotationAddedToNavPropertiesOfComplexType(final CsdlComplexType complexType,
    final CsdlNavigationProperty complexNavProperty, final FullQualifiedName entityContainerName) {
    final List<CsdlAnnotation> annotations = ((EdmProviderImpl)this.edm).getAnnotationsMap()
      .get(entityContainerName.getNamespace() + this.DOT + complexType.getName() + this.SLASH
        + complexNavProperty.getName());
    removeAnnotationsOnNavProperties(complexNavProperty, annotations);

    final String aliasName = getAliasInfo(entityContainerName.getNamespace());
    final List<CsdlAnnotation> annotationsOnAlias = ((EdmProviderImpl)this.edm).getAnnotationsMap()
      .get(
        aliasName + this.DOT + complexType.getName() + this.SLASH + complexNavProperty.getName());
    removeAnnotationsOnNavProperties(complexNavProperty, annotationsOnAlias);
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

  protected EdmActionImport createActionImport(final String actionImportName) {
    EdmActionImport actionImport = null;

    try {
      final CsdlActionImport providerImport = this.provider
        .getActionImport(this.entityContainerName, actionImportName);
      if (providerImport != null) {
        addOperationImportAnnotations(providerImport, this.entityContainerName);
        actionImport = new EdmActionImportImpl(this.edm, this, providerImport);
      }
    } catch (final ODataException e) {
      throw new EdmException(e);
    }

    return actionImport;
  }

  protected EdmEntitySet createEntitySet(final String entitySetName) {
    EdmEntitySet entitySet = null;

    try {
      final CsdlEntitySet providerEntitySet = this.provider.getEntitySet(this.entityContainerName,
        entitySetName);
      if (providerEntitySet != null) {
        addEntitySetAnnotations(providerEntitySet, this.entityContainerName);
        entitySet = new EdmEntitySetImpl(this.edm, this, providerEntitySet);
      }
    } catch (final ODataException e) {
      throw new EdmException(e);
    }

    return entitySet;
  }

  protected EdmFunctionImport createFunctionImport(final String functionImportName) {
    EdmFunctionImport functionImport = null;

    try {
      final CsdlFunctionImport providerImport = this.provider
        .getFunctionImport(this.entityContainerName, functionImportName);
      if (providerImport != null) {
        addOperationImportAnnotations(providerImport, this.entityContainerName);
        functionImport = new EdmFunctionImportImpl(this.edm, this, providerImport);
      }
    } catch (final ODataException e) {
      throw new EdmException(e);
    }

    return functionImport;
  }

  protected EdmSingleton createSingleton(final String singletonName) {
    EdmSingleton singleton = null;

    try {
      final CsdlSingleton providerSingleton = this.provider.getSingleton(this.entityContainerName,
        singletonName);
      if (providerSingleton != null) {
        addSingletonAnnotations(providerSingleton, this.entityContainerName);
        singleton = new EdmSingletonImpl(this.edm, this, providerSingleton);
      }
    } catch (final ODataException e) {
      throw new EdmException(e);
    }

    return singleton;
  }

  /**
   * @param singleton
   * @return
   */
  private CsdlEntityType fetchEntityTypeFromSingleton(final CsdlSingleton singleton) {
    CsdlEntityType entityType;
    try {
      entityType = singleton.getTypeFQN() != null ? this.provider.getEntityType(
        new FullQualifiedName(singleton.getTypeFQN().getFullQualifiedNameAsString())) : null;
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
    return entityType;
  }

  @Override
  public EdmActionImport getActionImport(final String actionImportName) {
    EdmActionImport actionImport = this.actionImportCache.get(actionImportName);
    if (actionImport == null) {
      actionImport = createActionImport(actionImportName);
      if (actionImport != null) {
        this.actionImportCache.put(actionImportName, actionImport);
      }
    }
    return actionImport;
  }

  @Override
  public List<EdmActionImport> getActionImports() {
    if (this.actionImports == null) {
      loadAllActionImports();
    }
    return Collections.unmodifiableList(this.actionImports);
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

  /**
   * @param propertyName
   * @return
   */
  private CsdlComplexType getComplexTypeFromProperty(final CsdlProperty propertyName) {
    CsdlComplexType complexType;
    try {
      complexType = this.provider.getComplexType(propertyName.getTypeName());
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
    return complexType;
  }

  /**
   * @param entitySet
   * @return
   */
  private CsdlEntityType getCsdlEntityTypeFromEntitySet(final CsdlEntitySet entitySet) {
    CsdlEntityType entityType;
    try {
      entityType = entitySet.getTypeFQN() != null ? this.provider.getEntityType(
        new FullQualifiedName(entitySet.getTypeFQN().getFullQualifiedNameAsString())) : null;
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
    return entityType;
  }

  @Override
  public EdmEntitySet getEntitySet(final String entitySetName) {
    EdmEntitySet entitySet = this.entitySetWithAnnotationsCache.get(entitySetName);
    if (entitySet == null) {
      entitySet = this.entitySetCache.get(entitySetName);
      if (entitySet == null) {
        entitySet = createEntitySet(entitySetName);
        if (entitySet != null) {
          if (this.isAnnotationsIncluded) {
            this.entitySetWithAnnotationsCache.put(entitySetName, entitySet);
          } else {
            this.entitySetCache.put(entitySetName, entitySet);
          }
        }
      }
    }
    ((EdmProviderImpl)this.edm).setIsPreviousES(true);
    return entitySet;
  }

  @Override
  public List<EdmEntitySet> getEntitySets() {
    if (this.entitySets == null) {
      loadAllEntitySets();
    }
    return Collections.unmodifiableList(this.entitySets);
  }

  @Override
  public List<EdmEntitySet> getEntitySetsWithAnnotations() {
    loadAllEntitySets();
    return Collections.unmodifiableList(this.entitySets);
  }

  @Override
  public FullQualifiedName getFullQualifiedName() {
    return this.entityContainerName;
  }

  @Override
  public EdmFunctionImport getFunctionImport(final String functionImportName) {
    EdmFunctionImport functionImport = this.functionImportCache.get(functionImportName);
    if (functionImport == null) {
      functionImport = createFunctionImport(functionImportName);
      if (functionImport != null) {
        this.functionImportCache.put(functionImportName, functionImport);
      }
    }
    return functionImport;
  }

  @Override
  public List<EdmFunctionImport> getFunctionImports() {
    if (this.functionImports == null) {
      loadAllFunctionImports();
    }
    return Collections.unmodifiableList(this.functionImports);
  }

  @Override
  public String getNamespace() {
    return this.entityContainerName.getNamespace();
  }

  @Override
  public FullQualifiedName getParentContainerName() {
    return this.parentContainerName;
  }

  @Override
  public EdmSingleton getSingleton(final String singletonName) {
    EdmSingleton singleton = this.singletonWithAnnotationsCache.get(singletonName);
    if (singleton == null) {
      singleton = this.singletonCache.get(singletonName);
      if (singleton == null) {
        singleton = createSingleton(singletonName);
        if (singleton != null) {
          if (this.isSingletonAnnotationsIncluded) {
            this.singletonWithAnnotationsCache.put(singletonName, singleton);
          } else {
            this.singletonCache.put(singletonName, singleton);
          }
        }
      }
    }
    return singleton;
  }

  @Override
  public List<EdmSingleton> getSingletons() {
    if (this.singletons == null) {
      loadAllSingletons();
    }
    return Collections.unmodifiableList(this.singletons);
  }

  private boolean isPropertyComplex(final CsdlProperty propertyName) {
    try {
      return this.provider.getComplexType(propertyName.getTypeName()) != null ? true : false;
    } catch (final ODataException e) {
      throw new EdmException(e);
    }
  }

  protected void loadAllActionImports() {
    loadContainer();
    final List<CsdlActionImport> providerActionImports = this.container.getActionImports();
    final List<EdmActionImport> actionImportsLocal = new ArrayList<>();

    if (providerActionImports != null) {
      for (final CsdlActionImport actionImport : providerActionImports) {
        addOperationImportAnnotations(actionImport, this.entityContainerName);
        final EdmActionImportImpl impl = new EdmActionImportImpl(this.edm, this, actionImport);
        this.actionImportCache.put(actionImport.getName(), impl);
        actionImportsLocal.add(impl);
      }
      this.actionImports = actionImportsLocal;
    }

  }

  protected void loadAllEntitySets() {
    loadContainer();
    final List<CsdlEntitySet> providerEntitySets = this.container.getEntitySets();
    final List<EdmEntitySet> entitySetsLocal = new ArrayList<>();

    if (providerEntitySets != null) {
      for (final CsdlEntitySet entitySet : providerEntitySets) {
        addEntitySetAnnotations(entitySet, this.entityContainerName);
        final EdmEntitySetImpl impl = new EdmEntitySetImpl(this.edm, this, entitySet);
        if (this.isAnnotationsIncluded) {
          this.entitySetWithAnnotationsCache.put(impl.getName(), impl);
        } else {
          this.entitySetCache.put(impl.getName(), impl);
        }
        entitySetsLocal.add(impl);
      }
      this.entitySets = entitySetsLocal;
      ((EdmProviderImpl)this.edm).setIsPreviousES(true);
    }
  }

  protected void loadAllFunctionImports() {
    loadContainer();
    final List<CsdlFunctionImport> providerFunctionImports = this.container.getFunctionImports();
    final ArrayList<EdmFunctionImport> functionImportsLocal = new ArrayList<>();

    if (providerFunctionImports != null) {
      for (final CsdlFunctionImport functionImport : providerFunctionImports) {
        addOperationImportAnnotations(functionImport, this.entityContainerName);
        final EdmFunctionImportImpl impl = new EdmFunctionImportImpl(this.edm, this,
          functionImport);
        this.functionImportCache.put(impl.getName(), impl);
        functionImportsLocal.add(impl);
      }
      this.functionImports = functionImportsLocal;
    }
  }

  protected void loadAllSingletons() {
    loadContainer();
    final List<CsdlSingleton> providerSingletons = this.container.getSingletons();
    final List<EdmSingleton> singletonsLocal = new ArrayList<>();

    if (providerSingletons != null) {
      for (final CsdlSingleton singleton : providerSingletons) {
        addSingletonAnnotations(singleton, this.entityContainerName);
        final EdmSingletonImpl impl = new EdmSingletonImpl(this.edm, this, singleton);
        this.singletonCache.put(singleton.getName(), impl);
        singletonsLocal.add(impl);
      }
      this.singletons = singletonsLocal;
    }
  }

  private void loadContainer() {
    if (this.container == null) {
      try {
        CsdlEntityContainer containerLocal = this.provider.getEntityContainer();
        if (containerLocal == null) {
          containerLocal = new CsdlEntityContainer().setName(getName());
        }
        ((EdmProviderImpl)this.edm).addEntityContainerAnnotations(containerLocal,
          this.entityContainerName);
        this.container = containerLocal;
      } catch (final ODataException e) {
        throw new EdmException(e);
      }
    }
  }

  private void removeAnnotationAddedToNavProperties(final CsdlEntityType entityType,
    final CsdlNavigationProperty navProperty, final FullQualifiedName entityContainerName) {
    final List<CsdlAnnotation> annotations = ((EdmProviderImpl)this.edm).getAnnotationsMap()
      .get(entityContainerName.getNamespace() + this.DOT + entityType.getName() + this.SLASH
        + navProperty.getName());
    removeAnnotationsOnNavProperties(navProperty, annotations);

    final String aliasName = getAliasInfo(entityContainerName.getNamespace());
    final List<CsdlAnnotation> annotationsOnAlias = ((EdmProviderImpl)this.edm).getAnnotationsMap()
      .get(aliasName + this.DOT + entityContainerName.getName() + this.DOT + entityType.getName()
        + this.SLASH + navProperty.getName());
    removeAnnotationsOnNavProperties(navProperty, annotationsOnAlias);
  }

  private void removeAnnotationAddedToPropertiesOfComplexType(final CsdlComplexType complexType,
    final CsdlProperty complexPropertyName, final FullQualifiedName entityContainerName) {
    final List<CsdlAnnotation> annotations = ((EdmProviderImpl)this.edm).getAnnotationsMap()
      .get(entityContainerName.getNamespace() + this.DOT + complexType.getName() + this.SLASH
        + complexPropertyName.getName());
    removeAnnotationsOnETProperties(complexPropertyName, annotations);

    final String aliasName = getAliasInfo(entityContainerName.getNamespace());
    final List<CsdlAnnotation> annotationsOnAlias = ((EdmProviderImpl)this.edm).getAnnotationsMap()
      .get(aliasName + this.DOT + entityContainerName.getName() + this.DOT + complexType.getName()
        + this.SLASH + complexPropertyName.getName());
    removeAnnotationsOnETProperties(complexPropertyName, annotationsOnAlias);
  }

  /**
   * If annotations are added to properties via entity type path, then remove it
   * @param type
   * @param property
   * @param entityContainerName
   */
  private void removeAnnotationsAddedToPropertiesOfEntityType(final CsdlEntityType type,
    final CsdlProperty property, final FullQualifiedName entityContainerName) {
    final List<CsdlAnnotation> annotations = ((EdmProviderImpl)this.edm).getAnnotationsMap()
      .get(entityContainerName.getNamespace() + this.DOT + type.getName() + this.SLASH
        + property.getName());
    removeAnnotationsOnETProperties(property, annotations);

    final String aliasName = getAliasInfo(entityContainerName.getNamespace());
    final List<CsdlAnnotation> annotationsOnAlias = ((EdmProviderImpl)this.edm).getAnnotationsMap()
      .get(aliasName + this.DOT + entityContainerName.getName() + this.DOT + type.getName()
        + this.SLASH + property.getName());
    removeAnnotationsOnETProperties(property, annotationsOnAlias);
  }

  /**
   * If annotations are added to properties via Entity set then remove them
   * @param entityType
   * @param property
   * @param schemaName
   * @param containerName
   * @param entitySetName
   */
  private void removeAnnotationsAddedToPropertiesViaEntitySet(final CsdlEntityType entityType,
    final CsdlProperty property, final String schemaName, final String containerName,
    final String entitySetName) {
    final List<CsdlAnnotation> annotPropDerivedFromES = ((EdmProviderImpl)this.edm)
      .getAnnotationsMap()
      .get(schemaName + this.DOT + containerName + this.SLASH + entitySetName + this.SLASH
        + property.getName());
    removeAnnotationsOnPropertiesDerivedFromES(entityType, property, annotPropDerivedFromES);
    final String aliasName = getAliasInfo(schemaName);
    final List<CsdlAnnotation> annotPropDerivedFromESOnAlias = ((EdmProviderImpl)this.edm)
      .getAnnotationsMap()
      .get(aliasName + this.DOT + containerName + this.SLASH + entitySetName + this.SLASH
        + property.getName());
    removeAnnotationsOnPropertiesDerivedFromES(entityType, property, annotPropDerivedFromESOnAlias);
  }

  /**
   * Removes the annotations added on Entity type
   * properties when there is a target path on entity type
   * @param property
   * @param annotations
   */
  private void removeAnnotationsOnETProperties(final CsdlProperty property,
    final List<CsdlAnnotation> annotations) {
    if (null != annotations && !annotations.isEmpty()) {
      for (final CsdlAnnotation annotation : annotations) {
        property.getAnnotations().remove(annotation);
      }
    }
  }

  /**
   * Removes the annotations added on Entity type
   * navigation properties when there is a target path on entity type
   * @param property
   * @param annotations
   */
  private void removeAnnotationsOnNavProperties(final CsdlNavigationProperty property,
    final List<CsdlAnnotation> annotations) {
    if (null != annotations && !annotations.isEmpty()) {
      for (final CsdlAnnotation annotation : annotations) {
        property.getAnnotations().remove(annotation);
      }
    }
  }

  /**
   * Removes the annotations added on properties via Entity Set in case of singleton flow
   * @param entityType
   * @param property
   * @param annotPropDerivedFromES
   */
  private void removeAnnotationsOnPropertiesDerivedFromES(final CsdlEntityType entityType,
    final CsdlProperty property, final List<CsdlAnnotation> annotPropDerivedFromES) {
    if (null != annotPropDerivedFromES && !annotPropDerivedFromES.isEmpty()) {
      for (final CsdlAnnotation annotation : annotPropDerivedFromES) {
        entityType.getProperty(property.getName()).getAnnotations().remove(annotation);
      }
    }
  }
}
