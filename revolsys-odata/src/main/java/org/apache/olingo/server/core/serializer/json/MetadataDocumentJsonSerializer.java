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
package org.apache.olingo.server.core.serializer.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.olingo.commons.api.edm.EdmAction;
import org.apache.olingo.commons.api.edm.EdmActionImport;
import org.apache.olingo.commons.api.edm.EdmAnnotatable;
import org.apache.olingo.commons.api.edm.EdmAnnotation;
import org.apache.olingo.commons.api.edm.EdmAnnotations;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntityContainer;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmException;
import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmFunctionImport;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.EdmMember;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmNavigationPropertyBinding;
import org.apache.olingo.commons.api.edm.EdmOperation;
import org.apache.olingo.commons.api.edm.EdmParameter;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmReferentialConstraint;
import org.apache.olingo.commons.api.edm.EdmReturnType;
import org.apache.olingo.commons.api.edm.EdmSchema;
import org.apache.olingo.commons.api.edm.EdmSingleton;
import org.apache.olingo.commons.api.edm.EdmStructuredType;
import org.apache.olingo.commons.api.edm.EdmTerm;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.EdmTypeDefinition;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.TargetType;
import org.apache.olingo.commons.api.edm.annotation.EdmApply;
import org.apache.olingo.commons.api.edm.annotation.EdmCast;
import org.apache.olingo.commons.api.edm.annotation.EdmConstantExpression;
import org.apache.olingo.commons.api.edm.annotation.EdmDynamicExpression;
import org.apache.olingo.commons.api.edm.annotation.EdmExpression;
import org.apache.olingo.commons.api.edm.annotation.EdmIf;
import org.apache.olingo.commons.api.edm.annotation.EdmIsOf;
import org.apache.olingo.commons.api.edm.annotation.EdmLabeledElement;
import org.apache.olingo.commons.api.edm.annotation.EdmLabeledElementReference;
import org.apache.olingo.commons.api.edm.annotation.EdmLogicalOrComparisonExpression;
import org.apache.olingo.commons.api.edm.annotation.EdmNavigationPropertyPath;
import org.apache.olingo.commons.api.edm.annotation.EdmNot;
import org.apache.olingo.commons.api.edm.annotation.EdmNull;
import org.apache.olingo.commons.api.edm.annotation.EdmPath;
import org.apache.olingo.commons.api.edm.annotation.EdmPropertyPath;
import org.apache.olingo.commons.api.edm.annotation.EdmPropertyValue;
import org.apache.olingo.commons.api.edm.annotation.EdmRecord;
import org.apache.olingo.commons.api.edm.annotation.EdmUrlRef;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.commons.api.edmx.EdmxReferenceInclude;
import org.apache.olingo.commons.api.edmx.EdmxReferenceIncludeAnnotation;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.Kind;
import org.apache.olingo.server.api.serializer.SerializerException;

import com.revolsys.record.io.format.json.JsonWriter;

public class MetadataDocumentJsonSerializer {

  private static final String DOLLAR = "$";

  private static final String VERSION = DOLLAR + "Version";

  private static final String REFERENCES = DOLLAR + "Reference";

  private static final String INCLUDE = DOLLAR + "Include";

  private static final String NAMESPACE = DOLLAR + "Namespace";

  private static final String ALIAS = DOLLAR + "Alias";

  private static final String INCLUDE_ANNOTATIONS = DOLLAR + "IncludeAnnotations";

  private static final String TERM_NAMESPACE = DOLLAR + "TermNamespace";

  private static final String TARGET_NAMESPACE = DOLLAR + "TargetNamespace";

  private static final String QUALIFIER = DOLLAR + "Qualifier";

  private static final String IS_FLAGS = DOLLAR + "IsFlags";

  private static final String UNDERLYING_TYPE = DOLLAR + "UnderlyingType";

  private static final String KIND = DOLLAR + "Kind";

  private static final String MAX_LENGTH = DOLLAR + "MaxLength";

  private static final String PRECISION = DOLLAR + "Precision";

  private static final String SCALE = DOLLAR + "Scale";

  private static final String SRID = DOLLAR + "SRID";

  private static final String COLLECTION = DOLLAR + "Collection";

  private static final String BASE_TYPE = DOLLAR + "BaseType";

  private static final String HAS_STREAM = DOLLAR + "HasStream";

  private static final String KEY = DOLLAR + "Key";

  private static final String ABSTRACT = DOLLAR + "Abstract";

  private static final String TYPE = DOLLAR + "Type";

  private static final String NULLABLE = DOLLAR + "Nullable";

  private static final String UNICODE = DOLLAR + "Unicode";

  private static final String DEFAULT_VALUE = DOLLAR + "DefaultValue";

  private static final String PARTNER = DOLLAR + "Partner";

  private static final String CONTAINS_TARGET = DOLLAR + "ContainsTarget";

  private static final String REFERENTIAL_CONSTRAINT = DOLLAR + "ReferentialConstraint";

  private static final String ISBOUND = DOLLAR + "IsBound";

  private static final String ENTITY_SET_PATH = DOLLAR + "EntitySetPath";

  private static final String PARAMETER = DOLLAR + "Parameter";

  private static final String RETURN_TYPE = DOLLAR + "ReturnType";

  private static final String ISCOMPOSABLE = DOLLAR + "IsComposable";

  private static final String PARAMETER_NAME = DOLLAR + "Name";

  private static final String BASE_TERM = DOLLAR + "BaseTerm";

  private static final String APPLIES_TO = DOLLAR + "AppliesTo";

  private static final String NAVIGATION_PROPERTY_BINDING = DOLLAR + "NavigationPropertyBinding";

  private static final String EXTENDS = DOLLAR + "Extends";

  private static final String INCLUDE_IN_SERV_DOC = DOLLAR + "IncludeInServiceDocument";

  private static final String ANNOTATION = DOLLAR + "Annotations";

  private static final String ANNOTATION_PATH = DOLLAR + "Path";

  private static final String NAME = DOLLAR + "Name";

  private static final String ON_DELETE = "OnDelete";

  private static final String ON_DELETE_PROPERTY = "Action";

  private final ServiceMetadata serviceMetadata;

  private final Map<String, String> namespaceToAlias = new HashMap<>();

  public MetadataDocumentJsonSerializer(final ServiceMetadata serviceMetadata)
    throws SerializerException {
    if (serviceMetadata == null || serviceMetadata.getEdm() == null) {
      throw new SerializerException("Service Metadata and EDM must not be null for a service.",
        SerializerException.MessageKeys.NULL_METADATA_OR_EDM);
    }
    this.serviceMetadata = serviceMetadata;
  }

  private void appendActionImports(final JsonWriter json, final List<EdmActionImport> actionImports,
    final String containerNamespace) throws SerializerException, IOException {
    for (final EdmActionImport actionImport : actionImports) {
      json.label(actionImport.getName());
      json.startObject();
      json.labelValue(KIND, Kind.ActionImport.name());
      json.labelValue(DOLLAR + Kind.Action.name(),
        getAliasedFullQualifiedName(actionImport.getUnboundAction()));
      if (actionImport.getReturnedEntitySet() != null) {
        json.labelValue(DOLLAR + Kind.EntitySet.name(),
          containerNamespace + "." + actionImport.getReturnedEntitySet().getName());
      }
      appendAnnotations(json, actionImport, null);
      json.endObject();
    }

  }

  private void appendActions(final JsonWriter json, final List<EdmAction> actions)
    throws SerializerException, IOException {
    final Map<String, List<EdmAction>> actionsMap = new HashMap<>();
    for (final EdmAction action : actions) {
      if (actionsMap.containsKey(action.getName())) {
        final List<EdmAction> actionsWithSpecificActionName = actionsMap.get(action.getName());
        actionsWithSpecificActionName.add(action);
        actionsMap.put(action.getName(), actionsWithSpecificActionName);
      } else {
        final List<EdmAction> actionList = new ArrayList<>();
        actionList.add(action);
        actionsMap.put(action.getName(), actionList);
      }
    }
    for (final Entry<String, List<EdmAction>> actionsMapEntry : actionsMap.entrySet()) {
      json.label(actionsMapEntry.getKey());
      json.startList();
      final List<EdmAction> actionEntry = actionsMapEntry.getValue();
      for (final EdmAction action : actionEntry) {
        json.startObject();
        json.labelValue(KIND, Kind.Action.name());
        if (action.getEntitySetPath() != null) {
          json.labelValue(ENTITY_SET_PATH, action.getEntitySetPath());
        }
        json.labelValue(ISBOUND, action.isBound());

        appendOperationParameters(json, action);

        appendOperationReturnType(json, action);

        appendAnnotations(json, action, null);

        json.endObject();
      }
      json.endList();
    }
  }

  private void appendAnnotationGroup(final JsonWriter json, final EdmAnnotations annotationGroup)
    throws SerializerException, IOException {
    final String targetPath = annotationGroup.getTargetPath();
    if (annotationGroup.getQualifier() != null) {
      json.label(targetPath + "#" + annotationGroup.getQualifier());
    } else {
      json.label(targetPath);
    }
    json.startObject();
    appendAnnotations(json, annotationGroup, null);
    json.endObject();
  }

  private void appendAnnotationGroups(final JsonWriter json,
    final List<EdmAnnotations> annotationGroups) throws SerializerException, IOException {
    if (!annotationGroups.isEmpty()) {
      json.label(ANNOTATION);
      json.startObject();
      for (final EdmAnnotations annotationGroup : annotationGroups) {
        appendAnnotationGroup(json, annotationGroup);
      }
      json.endObject();
    }
  }

  private void appendAnnotations(final JsonWriter json, final EdmAnnotatable annotatable,
    final String memberName) throws SerializerException, IOException {
    final List<EdmAnnotation> annotations = annotatable.getAnnotations();
    if (annotations != null && !annotations.isEmpty()) {
      for (final EdmAnnotation annotation : annotations) {
        String termName = memberName != null ? memberName : "";
        if (annotation.getTerm() != null) {
          termName += "@"
            + getAliasedFullQualifiedName(annotation.getTerm().getFullQualifiedName());
        }
        if (annotation.getQualifier() != null) {
          termName += "#" + annotation.getQualifier();
        }
        if (annotation.getExpression() == null && termName.length() > 0) {
          json.labelValue(termName, true);
        } else {
          appendExpression(json, annotation.getExpression(), termName);
        }
        appendAnnotations(json, annotation, termName);
      }
    }
  }

  private void appendComplexTypes(final JsonWriter json, final List<EdmComplexType> complexTypes)
    throws SerializerException, IOException {
    for (final EdmComplexType complexType : complexTypes) {
      json.label(complexType.getName());
      json.startObject();

      json.labelValue(KIND, Kind.ComplexType.name());
      if (complexType.getBaseType() != null) {
        json.labelValue(BASE_TYPE, getAliasedFullQualifiedName(complexType.getBaseType()));
      }

      if (complexType.isAbstract()) {
        json.labelValue(ABSTRACT, complexType.isAbstract());
      }

      appendProperties(json, complexType);

      appendNavigationProperties(json, complexType);

      appendAnnotations(json, complexType, null);

      json.endObject();
    }
  }

  private void appendConstantExpression(final JsonWriter json, final EdmConstantExpression constExp,
    final String termName) throws SerializerException, IOException {
    switch (constExp.getExpressionType()) {
      case Binary:
        json.label(termName);
        json.startObject();
        json.labelValue(DOLLAR + constExp.getExpressionName(), constExp.getValueAsString());
        json.endObject();
      break;
      case Date:
        json.label(termName);
        json.startObject();
        json.labelValue(DOLLAR + constExp.getExpressionName(), constExp.getValueAsString());
        json.endObject();
      break;
      case DateTimeOffset:
        json.label(termName);
        json.startObject();
        json.labelValue(DOLLAR + constExp.getExpressionName(), constExp.getValueAsString());
        json.endObject();
      break;
      case Decimal:
        json.label(termName);
        json.startObject();
        json.labelValue(DOLLAR + constExp.getExpressionName(), constExp.getValueAsString());
        json.endObject();
      break;
      case Float:
        json.label(termName);
        json.startObject();
        json.labelValue(DOLLAR + constExp.getExpressionName(), constExp.getValueAsString());
        json.endObject();
      break;
      case Int:
        json.label(termName);
        json.startObject();
        json.labelValue(DOLLAR + constExp.getExpressionName(), constExp.getValueAsString());
        json.endObject();
      break;
      case Duration:
        json.label(termName);
        json.startObject();
        json.labelValue(DOLLAR + constExp.getExpressionName(), constExp.getValueAsString());
        json.endObject();
      break;
      case EnumMember:
        json.label(termName);
        json.startObject();
        json.labelValue(DOLLAR + constExp.getExpressionName(), constExp.getValueAsString());
        json.endObject();
      break;
      case Guid:
        json.label(termName);
        json.startObject();
        json.labelValue("$" + constExp.getExpressionName(), constExp.getValueAsString());
        json.endObject();
      break;
      case TimeOfDay:
        json.label(termName);
        json.startObject();
        json.labelValue(DOLLAR + constExp.getExpressionName(), constExp.getValueAsString());
        json.endObject();
      break;
      case Bool:
        if (termName != null && termName.length() > 0) {
          json.labelValue(termName, Boolean.valueOf(constExp.getValueAsString()));
        } else {
          json.value(Boolean.valueOf(constExp.getValueAsString()));
        }
      break;
      case String:
        if (termName != null && termName.length() > 0) {
          json.labelValue(termName, constExp.getValueAsString());
        } else {
          json.value(constExp.getValueAsString());
        }
      break;
      default:
        throw new IllegalArgumentException(
          "Unkown ExpressionType " + "for constant expression: " + constExp.getExpressionType());
    }
  }

  private void appendDataServices(final JsonWriter json) throws SerializerException, IOException {
    for (final EdmSchema schema : this.serviceMetadata.getEdm().getSchemas()) {
      appendSchema(json, schema);
    }
  }

  private void appendDynamicExpression(final JsonWriter json, final EdmDynamicExpression dynExp,
    final String termName) throws SerializerException, IOException {
    if (termName != null) {
      json.label(termName);
    }
    switch (dynExp.getExpressionType()) {
      // Logical
      case And:
        appendLogicalOrComparisonExpression(json, dynExp.asAnd());
      break;
      case Or:
        appendLogicalOrComparisonExpression(json, dynExp.asOr());
      break;
      case Not:
        appendNotExpression(json, dynExp.asNot());
      break;
      // Comparison
      case Eq:
        appendLogicalOrComparisonExpression(json, dynExp.asEq());
      break;
      case Ne:
        appendLogicalOrComparisonExpression(json, dynExp.asNe());
      break;
      case Gt:
        appendLogicalOrComparisonExpression(json, dynExp.asGt());
      break;
      case Ge:
        appendLogicalOrComparisonExpression(json, dynExp.asGe());
      break;
      case Lt:
        appendLogicalOrComparisonExpression(json, dynExp.asLt());
      break;
      case Le:
        appendLogicalOrComparisonExpression(json, dynExp.asLe());
      break;
      case AnnotationPath:
        json.startObject();
        json.labelValue(ANNOTATION_PATH, dynExp.asAnnotationPath().getValue());
        json.endObject();
      break;
      case Apply:
        final EdmApply asApply = dynExp.asApply();
        json.startObject();
        json.label(DOLLAR + asApply.getExpressionName());
        json.startList();
        for (final EdmExpression parameter : asApply.getParameters()) {
          appendExpression(json, parameter, null);
        }
        json.endList();
        json.labelValue(DOLLAR + Kind.Function.name(), asApply.getFunction());

        appendAnnotations(json, asApply, null);
        json.endObject();
      break;
      case Cast:
        final EdmCast asCast = dynExp.asCast();
        json.startObject();
        appendExpression(json, asCast.getValue(), DOLLAR + asCast.getExpressionName());
        json.labelValue(TYPE, getAliasedFullQualifiedName(asCast.getType()));

        if (asCast.getMaxLength() != null) {
          json.labelValue(MAX_LENGTH, asCast.getMaxLength());
        }

        if (asCast.getPrecision() != null) {
          json.labelValue(PRECISION, asCast.getPrecision());
        }

        if (asCast.getScale() != null) {
          json.labelValue(SCALE, asCast.getScale());
        }
        appendAnnotations(json, asCast, null);
        json.endObject();
      break;
      case Collection:
        json.startList();
        for (final EdmExpression item : dynExp.asCollection().getItems()) {
          appendExpression(json, item, null);
        }
        json.endList();
      break;
      case If:
        final EdmIf asIf = dynExp.asIf();
        json.startObject();
        json.label(DOLLAR + asIf.getExpressionName());
        json.startList();
        appendExpression(json, asIf.getGuard(), null);
        appendExpression(json, asIf.getThen(), null);
        appendExpression(json, asIf.getElse(), null);
        json.endList();
        appendAnnotations(json, asIf, null);
        json.endObject();
      break;
      case IsOf:
        final EdmIsOf asIsOf = dynExp.asIsOf();
        json.startObject();
        appendExpression(json, asIsOf.getValue(), DOLLAR + asIsOf.getExpressionName());

        json.labelValue(TYPE, getAliasedFullQualifiedName(asIsOf.getType()));

        if (asIsOf.getMaxLength() != null) {
          json.labelValue(MAX_LENGTH, asIsOf.getMaxLength());
        }

        if (asIsOf.getPrecision() != null) {
          json.labelValue(PRECISION, asIsOf.getPrecision());
        }

        if (asIsOf.getScale() != null) {
          json.labelValue(SCALE, asIsOf.getScale());
        }
        appendAnnotations(json, asIsOf, null);
        json.endObject();
      break;
      case LabeledElement:
        final EdmLabeledElement asLabeledElement = dynExp.asLabeledElement();
        json.startObject();
        appendExpression(json, asLabeledElement.getValue(),
          DOLLAR + asLabeledElement.getExpressionName());
        json.labelValue(NAME, asLabeledElement.getName());
        appendAnnotations(json, asLabeledElement, null);
        json.endObject();
      break;
      case LabeledElementReference:
        final EdmLabeledElementReference asLabeledElementReference = dynExp
          .asLabeledElementReference();
        json.startObject();
        json.labelValue(DOLLAR + asLabeledElementReference.getExpressionName(),
          asLabeledElementReference.getValue());
        json.endObject();
      break;
      case Null:
        final EdmNull asNull = dynExp.asNull();
        json.startObject();
        json.labelValue(DOLLAR + asNull.getExpressionName(), null);
        appendAnnotations(json, dynExp.asNull(), null);
        json.endObject();
      break;
      case NavigationPropertyPath:
        final EdmNavigationPropertyPath asNavigationPropertyPath = dynExp
          .asNavigationPropertyPath();
        json.startObject();
        json.labelValue(DOLLAR + asNavigationPropertyPath.getExpressionName(),
          asNavigationPropertyPath.getValue());
        json.endObject();
      break;
      case Path:
        final EdmPath asPath = dynExp.asPath();
        json.startObject();
        json.labelValue(DOLLAR + asPath.getExpressionName(), asPath.getValue());
        json.endObject();
      break;
      case PropertyPath:
        final EdmPropertyPath asPropertyPath = dynExp.asPropertyPath();
        json.startObject();
        json.labelValue(DOLLAR + asPropertyPath.getExpressionName(), asPropertyPath.getValue());
        json.endObject();
      break;
      case Record:
        final EdmRecord asRecord = dynExp.asRecord();
        json.startObject();
        try {
          final EdmStructuredType structuredType = asRecord.getType();
          if (structuredType != null) {
            json.labelValue(TYPE, getAliasedFullQualifiedName(structuredType));
          }
        } catch (final EdmException e) {
          final FullQualifiedName type = asRecord.getTypeFQN();
          if (type != null) {
            json.labelValue(TYPE, getAliasedFullQualifiedName(type));
          }
        }
        for (final EdmPropertyValue propValue : asRecord.getPropertyValues()) {
          appendExpression(json, propValue.getValue(), propValue.getProperty());
          appendAnnotations(json, propValue, propValue.getProperty());
        }
        appendAnnotations(json, asRecord, null);
        json.endObject();
      break;
      case UrlRef:
        final EdmUrlRef asUrlRef = dynExp.asUrlRef();
        json.startObject();
        appendExpression(json, asUrlRef.getValue(), DOLLAR + asUrlRef.getExpressionName());
        appendAnnotations(json, asUrlRef, null);
        json.endObject();
      break;
      default:
        throw new IllegalArgumentException(
          "Unkown ExpressionType for dynamic expression: " + dynExp.getExpressionType());
    }
  }

  private void appendEntityContainer(final JsonWriter json, final EdmEntityContainer container)
    throws SerializerException, IOException {
    if (container != null) {
      json.label(container.getName());
      json.startObject();
      json.labelValue(KIND, Kind.EntityContainer.name());
      final FullQualifiedName parentContainerName = container.getParentContainerName();
      if (parentContainerName != null) {
        String parentContainerNameString;
        if (this.namespaceToAlias.get(parentContainerName.getNamespace()) != null) {
          parentContainerNameString = this.namespaceToAlias.get(parentContainerName.getNamespace())
            + "." + parentContainerName.getName();
        } else {
          parentContainerNameString = parentContainerName.getFullQualifiedNameAsString();
        }
        json.label(Kind.Extending.name());
        json.startObject();
        json.labelValue(KIND, Kind.EntityContainer.name());
        json.labelValue(EXTENDS, parentContainerNameString);
        json.endObject();
      }

      // EntitySets
      appendEntitySets(json, container.getEntitySets());

      String containerNamespace;
      if (this.namespaceToAlias.get(container.getNamespace()) != null) {
        containerNamespace = this.namespaceToAlias.get(container.getNamespace());
      } else {
        containerNamespace = container.getNamespace();
      }
      // ActionImports
      appendActionImports(json, container.getActionImports(), containerNamespace);

      // FunctionImports
      appendFunctionImports(json, container.getFunctionImports(), containerNamespace);

      // Singletons
      appendSingletons(json, container.getSingletons());

      // Annotations
      appendAnnotations(json, container, null);

      json.endObject();
    }

  }

  private void appendEntitySets(final JsonWriter json, final List<EdmEntitySet> entitySets)
    throws SerializerException, IOException {
    for (final EdmEntitySet entitySet : entitySets) {
      json.label(entitySet.getName());
      json.startObject();
      json.labelValue(KIND, Kind.EntitySet.name());
      json.labelValue(TYPE, getAliasedFullQualifiedName(entitySet.getEntityType()));
      if (!entitySet.isIncludeInServiceDocument()) {
        json.labelValue(INCLUDE_IN_SERV_DOC, entitySet.isIncludeInServiceDocument());
      }

      appendNavigationPropertyBindings(json, entitySet);
      appendAnnotations(json, entitySet, null);
      json.endObject();
    }
  }

  private void appendEntityTypes(final JsonWriter json, final List<EdmEntityType> entityTypes)
    throws SerializerException, IOException {
    for (final EdmEntityType entityType : entityTypes) {
      json.label(entityType.getName());
      json.startObject();
      json.labelValue(KIND, Kind.EntityType.name());
      if (entityType.hasStream()) {
        json.labelValue(HAS_STREAM, entityType.hasStream());
      }

      if (entityType.getBaseType() != null) {
        json.labelValue(BASE_TYPE, getAliasedFullQualifiedName(entityType.getBaseType()));
      }

      if (entityType.isAbstract()) {
        json.labelValue(ABSTRACT, entityType.isAbstract());
      }

      appendKey(json, entityType);

      appendProperties(json, entityType);

      appendNavigationProperties(json, entityType);

      appendAnnotations(json, entityType, null);

      json.endObject();
    }
  }

  private void appendEnumTypes(final JsonWriter json, final List<EdmEnumType> enumTypes)
    throws SerializerException, IOException {
    for (final EdmEnumType enumType : enumTypes) {
      json.label(enumType.getName());
      json.startObject();
      json.labelValue(KIND, Kind.EnumType.name());
      json.labelValue(IS_FLAGS, enumType.isFlags());
      json.labelValue(UNDERLYING_TYPE, getFullQualifiedName(enumType.getUnderlyingType()));

      for (final String memberName : enumType.getMemberNames()) {

        final EdmMember member = enumType.getMember(memberName);
        if (member.getValue() != null) {
          json.labelValue(memberName, member.getValue());
        }

        appendAnnotations(json, member, memberName);
      }
      json.endObject();
    }
  }

  private void appendExpression(final JsonWriter json, final EdmExpression expression,
    final String termName) throws SerializerException, IOException {
    if (expression == null) {
      return;
    }
    if (expression.isConstant()) {
      appendConstantExpression(json, expression.asConstant(), termName);
    } else if (expression.isDynamic()) {
      appendDynamicExpression(json, expression.asDynamic(), termName);
    } else {
      throw new IllegalArgumentException("Unkown expressiontype in metadata");
    }
  }

  private void appendFunctionImports(final JsonWriter json,
    final List<EdmFunctionImport> functionImports, final String containerNamespace)
    throws SerializerException, IOException {
    for (final EdmFunctionImport functionImport : functionImports) {
      json.label(functionImport.getName());
      json.startObject();

      json.labelValue(KIND, Kind.FunctionImport.name());
      String functionFQNString;
      final FullQualifiedName functionFqn = functionImport.getFunctionFqn();
      if (this.namespaceToAlias.get(functionFqn.getNamespace()) != null) {
        functionFQNString = this.namespaceToAlias.get(functionFqn.getNamespace()) + "."
          + functionFqn.getName();
      } else {
        functionFQNString = functionFqn.getFullQualifiedNameAsString();
      }
      json.labelValue(DOLLAR + Kind.Function.name(), functionFQNString);

      final EdmEntitySet returnedEntitySet = functionImport.getReturnedEntitySet();
      if (returnedEntitySet != null) {
        json.labelValue(DOLLAR + Kind.EntitySet.name(),
          containerNamespace + "." + returnedEntitySet.getName());
      }
      // Default is false and we do not write the default
      if (functionImport.isIncludeInServiceDocument()) {
        json.labelValue(INCLUDE_IN_SERV_DOC, functionImport.isIncludeInServiceDocument());
      }
      appendAnnotations(json, functionImport, null);
      json.endObject();
    }
  }

  private void appendFunctions(final JsonWriter json, final List<EdmFunction> functions)
    throws SerializerException, IOException {
    final Map<String, List<EdmFunction>> functionsMap = new HashMap<>();
    for (final EdmFunction function : functions) {
      if (functionsMap.containsKey(function.getName())) {
        final List<EdmFunction> actionsWithSpecificActionName = functionsMap
          .get(function.getName());
        actionsWithSpecificActionName.add(function);
        functionsMap.put(function.getName(), actionsWithSpecificActionName);
      } else {
        final List<EdmFunction> functionList = new ArrayList<>();
        functionList.add(function);
        functionsMap.put(function.getName(), functionList);
      }
    }

    for (final Entry<String, List<EdmFunction>> functionsMapEntry : functionsMap.entrySet()) {
      json.label(functionsMapEntry.getKey());
      json.startList();
      final List<EdmFunction> functionEntry = functionsMapEntry.getValue();
      for (final EdmFunction function : functionEntry) {
        json.startObject();
        json.labelValue(KIND, Kind.Function.name());
        if (function.getEntitySetPath() != null) {
          json.labelValue(ENTITY_SET_PATH, function.getEntitySetPath());
        }
        if (function.isBound()) {
          json.labelValue(ISBOUND, function.isBound());
        }

        if (function.isComposable()) {
          json.labelValue(ISCOMPOSABLE, function.isComposable());
        }

        appendOperationParameters(json, function);

        appendOperationReturnType(json, function);

        appendAnnotations(json, function, null);

        json.endObject();
      }
      json.endList();
    }
  }

  private void appendIncludeAnnotations(final JsonWriter json,
    final List<EdmxReferenceIncludeAnnotation> includeAnnotations)
    throws SerializerException, IOException {
    json.label(INCLUDE_ANNOTATIONS);
    json.startList();
    for (final EdmxReferenceIncludeAnnotation includeAnnotation : includeAnnotations) {
      json.startObject();
      json.labelValue(TERM_NAMESPACE, includeAnnotation.getTermNamespace());
      if (includeAnnotation.getQualifier() != null) {
        json.labelValue(QUALIFIER, includeAnnotation.getQualifier());
      }
      if (includeAnnotation.getTargetNamespace() != null) {
        json.labelValue(TARGET_NAMESPACE, includeAnnotation.getTargetNamespace());
      }
      json.endObject();
    }
    json.endList();
  }

  private void appendIncludes(final JsonWriter json, final List<EdmxReferenceInclude> includes)
    throws SerializerException, IOException {
    json.label(INCLUDE);
    json.startList();
    for (final EdmxReferenceInclude include : includes) {
      json.startObject();
      json.labelValue(NAMESPACE, include.getNamespace());
      if (include.getAlias() != null) {
        this.namespaceToAlias.put(include.getNamespace(), include.getAlias());
        // Reference Aliases are ignored for now since they are not V2
        // compatible
        json.labelValue(ALIAS, include.getAlias());
      }
      json.endObject();
    }
    json.endList();
  }

  private void appendKey(final JsonWriter json, final EdmEntityType entityType)
    throws SerializerException, IOException {
    final List<EdmKeyPropertyRef> keyPropertyRefs = entityType.getKeyPropertyRefs();
    if (keyPropertyRefs != null && !keyPropertyRefs.isEmpty()) {
      // Resolve Base Type key as it is shown in derived type
      final EdmEntityType baseType = entityType.getBaseType();
      if (baseType != null && baseType.getKeyPropertyRefs() != null
        && !baseType.getKeyPropertyRefs().isEmpty()) {
        return;
      }
      json.label(KEY);
      json.startList();
      for (final EdmKeyPropertyRef keyRef : keyPropertyRefs) {

        if (keyRef.getAlias() != null) {
          json.startObject();
          json.labelValue(keyRef.getAlias(), keyRef.getName());
          json.endObject();
        } else {
          json.value(keyRef.getName());
        }
      }
      json.endList();
    }
  }

  private void appendLogicalOrComparisonExpression(final JsonWriter json,
    final EdmLogicalOrComparisonExpression exp) throws SerializerException, IOException {
    json.startObject();
    json.label(DOLLAR + exp.getExpressionName());
    json.startList();
    appendExpression(json, exp.getLeftExpression(), null);
    appendExpression(json, exp.getRightExpression(), null);
    json.endList();
    appendAnnotations(json, exp, null);
    json.endObject();
  }

  private void appendNavigationProperties(final JsonWriter json, final EdmStructuredType type)
    throws SerializerException, IOException {
    final List<String> navigationPropertyNames = new ArrayList<>(type.getNavigationPropertyNames());
    if (type.getBaseType() != null) {
      navigationPropertyNames.removeAll(type.getBaseType().getNavigationPropertyNames());
    }
    for (final String navigationPropertyName : navigationPropertyNames) {
      final EdmNavigationProperty navigationProperty = type
        .getNavigationProperty(navigationPropertyName);
      json.label(navigationPropertyName);
      json.startObject();
      json.labelValue(KIND, Kind.NavigationProperty.name());

      json.labelValue(TYPE, getAliasedFullQualifiedName(navigationProperty.getType()));
      if (navigationProperty.isCollection()) {
        json.labelValue(COLLECTION, navigationProperty.isCollection());
      }

      if (!navigationProperty.isNullable()) {
        json.labelValue(NULLABLE, navigationProperty.isNullable());
      }

      if (navigationProperty.getPartner() != null) {
        final EdmNavigationProperty partner = navigationProperty.getPartner();
        json.labelValue(PARTNER, partner.getName());
      }

      if (navigationProperty.containsTarget()) {
        json.labelValue(CONTAINS_TARGET, navigationProperty.containsTarget());
      }

      if (navigationProperty.getReferentialConstraints() != null) {
        for (final EdmReferentialConstraint constraint : navigationProperty
          .getReferentialConstraints()) {
          json.startObject();
          json.label(REFERENTIAL_CONSTRAINT);
          json.labelValue(constraint.getPropertyName(), constraint.getReferencedPropertyName());
          for (final EdmAnnotation annotation : constraint.getAnnotations()) {
            appendAnnotations(json, annotation, null);
          }
          json.endObject();
        }
      }

      if (navigationProperty.getOnDelete() != null) {
        json.label(ON_DELETE);
        json.startObject();
        json.labelValue(ON_DELETE_PROPERTY, navigationProperty.getOnDelete().getAction());
        appendAnnotations(json, navigationProperty.getOnDelete(), null);
        json.endObject();
      }

      appendAnnotations(json, navigationProperty, null);

      json.endObject();
    }
  }

  private void appendNavigationPropertyBindings(final JsonWriter json,
    final EdmBindingTarget bindingTarget) throws SerializerException, IOException {
    if (bindingTarget.getNavigationPropertyBindings() != null
      && !bindingTarget.getNavigationPropertyBindings().isEmpty()) {
      json.label(NAVIGATION_PROPERTY_BINDING);
      json.startObject();
      for (final EdmNavigationPropertyBinding binding : bindingTarget
        .getNavigationPropertyBindings()) {
        json.labelValue(binding.getPath(), binding.getTarget());
      }
      json.endObject();
    }
  }

  private void appendNotExpression(final JsonWriter json, final EdmNot exp)
    throws SerializerException, IOException {
    json.startObject();
    appendExpression(json, exp.getLeftExpression(), DOLLAR + exp.getExpressionName());
    appendAnnotations(json, exp, null);
    json.endObject();
  }

  private void appendOperationParameters(final JsonWriter json, final EdmOperation operation)
    throws SerializerException, IOException {
    if (!operation.getParameterNames().isEmpty()) {
      json.label(PARAMETER);
      json.startList();
    }
    for (final String parameterName : operation.getParameterNames()) {
      final EdmParameter parameter = operation.getParameter(parameterName);
      json.startObject();
      json.labelValue(PARAMETER_NAME, parameterName);
      String typeFqnString;
      if (EdmTypeKind.PRIMITIVE.equals(parameter.getType().getKind())) {
        typeFqnString = getFullQualifiedName(parameter.getType());
      } else {
        typeFqnString = getAliasedFullQualifiedName(parameter.getType());
      }
      json.labelValue(TYPE, typeFqnString);
      if (parameter.isCollection()) {
        json.labelValue(COLLECTION, parameter.isCollection());
      }

      appendParameterFacets(json, parameter);

      appendAnnotations(json, parameter, null);
      json.endObject();
    }
    if (!operation.getParameterNames().isEmpty()) {
      json.endList();
    }
  }

  private void appendOperationReturnType(final JsonWriter json, final EdmOperation operation)
    throws SerializerException, IOException {
    final EdmReturnType returnType = operation.getReturnType();
    if (returnType != null) {
      json.label(RETURN_TYPE);
      json.startObject();
      String returnTypeFqnString;
      if (EdmTypeKind.PRIMITIVE.equals(returnType.getType().getKind())) {
        returnTypeFqnString = getFullQualifiedName(returnType.getType());
      } else {
        returnTypeFqnString = getAliasedFullQualifiedName(returnType.getType());
      }
      json.labelValue(TYPE, returnTypeFqnString);
      if (returnType.isCollection()) {
        json.labelValue(COLLECTION, returnType.isCollection());
      }

      appendReturnTypeFacets(json, returnType);
      json.endObject();
    }
  }

  private void appendParameterFacets(final JsonWriter json, final EdmParameter parameter)
    throws SerializerException, IOException {
    if (!parameter.isNullable()) {
      json.labelValue(NULLABLE, parameter.isNullable());
    }
    if (parameter.getMaxLength() != null) {
      json.labelValue(MAX_LENGTH, parameter.getMaxLength());
    }
    if (parameter.getPrecision() != null) {
      json.labelValue(PRECISION, parameter.getPrecision());
    }
    if (parameter.getScale() != null) {
      json.labelValue(SCALE, parameter.getScale());
    }
  }

  private void appendProperties(final JsonWriter json, final EdmStructuredType type)
    throws SerializerException, IOException {
    final List<String> propertyNames = new ArrayList<>(type.getPropertyNames());
    if (type.getBaseType() != null) {
      propertyNames.removeAll(type.getBaseType().getPropertyNames());
    }
    for (final String propertyName : propertyNames) {
      final EdmProperty property = type.getStructuralProperty(propertyName);
      json.label(propertyName);
      json.startObject();
      String fqnString;
      if (property.isPrimitive()) {
        fqnString = getFullQualifiedName(property.getType());
      } else {
        fqnString = getAliasedFullQualifiedName(property.getType());
      }
      json.labelValue(TYPE, fqnString);
      if (property.isCollection()) {
        json.labelValue(COLLECTION, property.isCollection());
      }

      // Facets
      if (!property.isNullable()) {
        json.labelValue(NULLABLE, property.isNullable());
      }

      if (!property.isUnicode()) {
        json.labelValue(UNICODE, property.isUnicode());
      }

      if (property.getDefaultValue() != null) {
        json.labelValue(DEFAULT_VALUE, property.getDefaultValue());
      }

      if (property.getMaxLength() != null) {
        json.labelValue(MAX_LENGTH, property.getMaxLength());
      }

      if (property.getPrecision() != null) {
        json.labelValue(PRECISION, property.getPrecision());
      }

      if (property.getScale() != null) {
        json.labelValue(SCALE, property.getScale());
      }

      if (property.getSrid() != null) {
        json.labelValue(SRID, "" + property.getSrid());
      }

      appendAnnotations(json, property, null);
      json.endObject();
    }
  }

  private void appendReference(final JsonWriter json) throws SerializerException, IOException {
    json.label(REFERENCES);
    json.startObject();
    for (final EdmxReference reference : this.serviceMetadata.getReferences()) {
      json.label(reference.getUri().toASCIIString());
      json.startObject();

      final List<EdmxReferenceInclude> includes = reference.getIncludes();
      if (!includes.isEmpty()) {
        appendIncludes(json, includes);
      }

      final List<EdmxReferenceIncludeAnnotation> includeAnnotations = reference
        .getIncludeAnnotations();
      if (!includeAnnotations.isEmpty()) {
        appendIncludeAnnotations(json, includeAnnotations);
      }
      json.endObject();
    }
    json.endObject();
  }

  private void appendReturnTypeFacets(final JsonWriter json, final EdmReturnType returnType)
    throws SerializerException, IOException {
    if (!returnType.isNullable()) {
      json.labelValue(NULLABLE, returnType.isNullable());
    }
    if (returnType.getMaxLength() != null) {
      json.labelValue(MAX_LENGTH, returnType.getMaxLength());
    }
    if (returnType.getPrecision() != null) {
      json.labelValue(PRECISION, returnType.getPrecision());
    }
    if (returnType.getScale() != null) {
      json.labelValue(SCALE, returnType.getScale());
    }
  }

  private void appendSchema(final JsonWriter json, final EdmSchema schema)
    throws SerializerException, IOException {
    json.label(schema.getNamespace());
    json.startObject();
    if (schema.getAlias() != null) {
      json.labelValue(ALIAS, schema.getAlias());
      this.namespaceToAlias.put(schema.getNamespace(), schema.getAlias());
    }
    // EnumTypes
    appendEnumTypes(json, schema.getEnumTypes());

    // TypeDefinitions
    appendTypeDefinitions(json, schema.getTypeDefinitions());

    // EntityTypes
    appendEntityTypes(json, schema.getEntityTypes());

    // ComplexTypes
    appendComplexTypes(json, schema.getComplexTypes());

    // Actions
    appendActions(json, schema.getActions());

    // Functions
    appendFunctions(json, schema.getFunctions());

    // Terms
    appendTerms(json, schema.getTerms());

    // EntityContainer
    appendEntityContainer(json, schema.getEntityContainer());

    // AnnotationGroups
    appendAnnotationGroups(json, schema.getAnnotationGroups());

    appendAnnotations(json, schema, null);

    json.endObject();
  }

  private void appendSingletons(final JsonWriter json, final List<EdmSingleton> singletons)
    throws SerializerException, IOException {
    for (final EdmSingleton singleton : singletons) {
      json.label(singleton.getName());
      json.startObject();
      json.labelValue(KIND, Kind.Singleton.name());
      json.labelValue(TYPE, getAliasedFullQualifiedName(singleton.getEntityType()));

      appendNavigationPropertyBindings(json, singleton);
      appendAnnotations(json, singleton, null);
      json.endObject();
    }
  }

  private void appendTerms(final JsonWriter json, final List<EdmTerm> terms)
    throws SerializerException, IOException {
    for (final EdmTerm term : terms) {
      json.label(term.getName());
      json.startObject();
      json.labelValue(KIND, Kind.Term.name());

      json.labelValue(TYPE, getAliasedFullQualifiedName(term.getType()));

      if (term.getBaseTerm() != null) {
        json.labelValue(BASE_TERM,
          getAliasedFullQualifiedName(term.getBaseTerm().getFullQualifiedName()));
      }

      if (term.getAppliesTo() != null && !term.getAppliesTo().isEmpty()) {
        String appliesToString = "";
        boolean first = true;
        for (final TargetType target : term.getAppliesTo()) {
          if (first) {
            first = false;
            appliesToString = target.toString();
          } else {
            appliesToString = appliesToString + " " + target.toString();
          }
        }
        json.labelValue(APPLIES_TO, appliesToString);
      }

      // Facets
      if (!term.isNullable()) {
        json.labelValue(NULLABLE, term.isNullable());
      }

      if (term.getDefaultValue() != null) {
        json.labelValue(DEFAULT_VALUE, term.getDefaultValue());
      }

      if (term.getMaxLength() != null) {
        json.labelValue(MAX_LENGTH, term.getMaxLength());
      }

      if (term.getPrecision() != null) {
        json.labelValue(PRECISION, term.getPrecision());
      }

      if (term.getScale() != null) {
        json.labelValue(SCALE, term.getScale());
      }

      appendAnnotations(json, term, null);
      json.endObject();
    }

  }

  private void appendTypeDefinitions(final JsonWriter json,
    final List<EdmTypeDefinition> typeDefinitions) throws SerializerException, IOException {
    for (final EdmTypeDefinition definition : typeDefinitions) {
      json.label(definition.getName());
      json.startObject();
      json.labelValue(KIND, definition.getKind().name());
      json.labelValue(UNDERLYING_TYPE, getFullQualifiedName(definition.getUnderlyingType()));

      // Facets
      if (definition.getMaxLength() != null) {
        json.labelValue(MAX_LENGTH, "" + definition.getMaxLength());
      }

      if (definition.getPrecision() != null) {
        json.labelValue(PRECISION, "" + definition.getPrecision());
      }

      if (definition.getScale() != null) {
        json.labelValue(SCALE, "" + definition.getScale());
      }

      if (definition.getSrid() != null) {
        json.labelValue(SRID, "" + definition.getSrid());
      }

      appendAnnotations(json, definition, null);
      json.endObject();
    }
  }

  private String getAliasedFullQualifiedName(final EdmType type) {
    final FullQualifiedName fqn = type.getFullQualifiedName();
    return getAliasedFullQualifiedName(fqn);
  }

  private String getAliasedFullQualifiedName(final FullQualifiedName fqn) {
    final String name;
    if (this.namespaceToAlias.get(fqn.getNamespace()) != null) {
      name = this.namespaceToAlias.get(fqn.getNamespace()) + "." + fqn.getName();
    } else {
      name = fqn.getFullQualifiedNameAsString();
    }

    return name;
  }

  private String getFullQualifiedName(final EdmType type) {
    return type.getFullQualifiedName().getFullQualifiedNameAsString();
  }

  public void writeMetadataDocument(final JsonWriter json) throws SerializerException, IOException {
    json.startObject();
    json.labelValue(VERSION, "4.01");
    if (!this.serviceMetadata.getReferences().isEmpty()) {
      appendReference(json);
    }
    appendDataServices(json);
    json.endObject();
  }
}
