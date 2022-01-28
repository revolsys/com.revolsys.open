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
package org.apache.olingo.server.core;

import java.io.IOException;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmAction;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.EdmReturnType;
import org.apache.olingo.commons.api.edm.EdmSingleton;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.core.edm.primitivetype.EdmPrimitiveTypeFactory;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.etag.CustomETagSupport;
import org.apache.olingo.server.api.etag.PreconditionException;
import org.apache.olingo.server.api.processor.ActionComplexCollectionProcessor;
import org.apache.olingo.server.api.processor.ActionComplexProcessor;
import org.apache.olingo.server.api.processor.ActionEntityCollectionProcessor;
import org.apache.olingo.server.api.processor.ActionEntityProcessor;
import org.apache.olingo.server.api.processor.ActionPrimitiveCollectionProcessor;
import org.apache.olingo.server.api.processor.ActionPrimitiveProcessor;
import org.apache.olingo.server.api.processor.ActionVoidProcessor;
import org.apache.olingo.server.api.processor.BatchProcessor;
import org.apache.olingo.server.api.processor.ComplexCollectionProcessor;
import org.apache.olingo.server.api.processor.ComplexProcessor;
import org.apache.olingo.server.api.processor.CountComplexCollectionProcessor;
import org.apache.olingo.server.api.processor.CountEntityCollectionProcessor;
import org.apache.olingo.server.api.processor.CountPrimitiveCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.MediaEntityProcessor;
import org.apache.olingo.server.api.processor.MetadataProcessor;
import org.apache.olingo.server.api.processor.PrimitiveCollectionProcessor;
import org.apache.olingo.server.api.processor.PrimitiveProcessor;
import org.apache.olingo.server.api.processor.PrimitiveValueProcessor;
import org.apache.olingo.server.api.processor.ReferenceCollectionProcessor;
import org.apache.olingo.server.api.processor.ReferenceProcessor;
import org.apache.olingo.server.api.processor.ServiceDocumentProcessor;
import org.apache.olingo.server.api.serializer.RepresentationType;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceAction;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.UriResourceSingleton;
import org.apache.olingo.server.core.batchhandler.BatchHandler;
import org.apache.olingo.server.core.etag.PreconditionsValidator;

public class ODataDispatcher {

  private static final String NOT_IMPLEMENTED_MESSAGE = "not implemented";

  private static final String RETURN_MINIMAL = "return=minimal";

  private static final String RETURN_REPRESENTATION = "return=representation";

  private static final String EDMSTREAM = "Edm.Stream";

  private final UriInfo uriInfo;

  private final ODataHandlerImpl handler;

  public ODataDispatcher(final UriInfo uriInfo, final ODataHandlerImpl handler) {
    this.uriInfo = uriInfo;
    this.handler = handler;
  }

  private void checkMethod(final HttpMethod requestMethod, final HttpMethod allowedMethod)
    throws ODataHandlerException {
    if (requestMethod != allowedMethod) {
      throwMethodNotAllowed(requestMethod);
    }
  }

  private void checkMethods(final HttpMethod requestMethod, final HttpMethod... allowedMethods)
    throws ODataHandlerException {
    // Check if the request method is one of the allowed ones
    for (final HttpMethod allowedMethod : allowedMethods) {
      if (requestMethod == allowedMethod) {
        return;
      }
    }
    // request method does not match any allowed method
    throwMethodNotAllowed(requestMethod);
  }

  public void dispatch(final ODataRequest request, final ODataResponse response)
    throws ODataApplicationException, ODataLibraryException {
    switch (this.uriInfo.getKind()) {
      case metadata:
        checkMethods(request.getMethod(), HttpMethod.GET, HttpMethod.HEAD);
        final ContentType requestedContentType = ContentNegotiator.doContentNegotiation(
          this.uriInfo.getFormatOption(), request, this.handler.getCustomContentTypeSupport(),
          RepresentationType.METADATA);
        this.handler.selectProcessor(MetadataProcessor.class)
          .readMetadata(request, response, this.uriInfo, requestedContentType);
      break;

      case service:
        checkMethods(request.getMethod(), HttpMethod.GET, HttpMethod.HEAD);
        if ("".equals(request.getRawODataPath())) {
          this.handler.selectProcessor(RedirectProcessor.class).redirect(request, response);
        } else {
          final ContentType serviceContentType = ContentNegotiator.doContentNegotiation(
            this.uriInfo.getFormatOption(), request, this.handler.getCustomContentTypeSupport(),
            RepresentationType.SERVICE);
          this.handler.selectProcessor(ServiceDocumentProcessor.class)
            .readServiceDocument(request, response, this.uriInfo, serviceContentType);
        }
      break;

      case resource:
      case entityId:
        handleResourceDispatching(request, response);
      break;

      case batch:
        checkMethod(request.getMethod(), HttpMethod.POST);
        new BatchHandler(this.handler, this.handler.selectProcessor(BatchProcessor.class))
          .process(request, response, true);
      break;

      default:
        throw new ODataHandlerException(NOT_IMPLEMENTED_MESSAGE,
          ODataHandlerException.MessageKeys.FUNCTIONALITY_NOT_IMPLEMENTED);
    }
  }

  private ContentType getSupportedContentType(final String contentTypeHeader,
    final RepresentationType representationType, final boolean mustNotBeNull)
    throws ODataHandlerException, ContentNegotiatorException {
    if (contentTypeHeader == null) {
      if (mustNotBeNull) {
        throw new ODataHandlerException("ContentTypeHeader parameter is null",
          ODataHandlerException.MessageKeys.MISSING_CONTENT_TYPE);
      }
      return ContentType.APPLICATION_JSON;
    }
    ContentType contentType;
    try {
      contentType = ContentType.create(contentTypeHeader);
    } catch (final IllegalArgumentException e) {
      throw new ODataHandlerException("Illegal content type.", e,
        ODataHandlerException.MessageKeys.INVALID_CONTENT_TYPE, contentTypeHeader);
    }
    ContentNegotiator.checkSupport(contentType, this.handler.getCustomContentTypeSupport(),
      representationType);
    return contentType;
  }

  private void handleActionDispatching(final ODataRequest request, final ODataResponse response,
    final UriResourceAction uriResourceAction)
    throws ODataApplicationException, ODataLibraryException {
    final EdmAction action = uriResourceAction.getAction();
    if (action.isBound()) {
      // Only bound actions can have ETag control for the binding parameter.
      validatePreconditions(request, false);
    }
    final ContentType requestFormat = getSupportedContentType(
      request.getHeader(HttpHeader.CONTENT_TYPE), RepresentationType.ACTION_PARAMETERS, false);
    final EdmReturnType returnType = action.getReturnType();
    if (returnType == null) {
      this.handler.selectProcessor(ActionVoidProcessor.class)
        .processActionVoid(request, response, this.uriInfo, requestFormat);
    } else {
      final boolean isCollection = returnType.isCollection();
      ContentType responseFormat;
      switch (returnType.getType().getKind()) {
        case ENTITY:
          responseFormat = ContentNegotiator.doContentNegotiation(this.uriInfo.getFormatOption(),
            request, this.handler.getCustomContentTypeSupport(),
            isCollection ? RepresentationType.COLLECTION_ENTITY : RepresentationType.ENTITY);
          if (isCollection) {
            this.handler.selectProcessor(ActionEntityCollectionProcessor.class)
              .processActionEntityCollection(request, response, this.uriInfo, requestFormat,
                responseFormat);
          } else {
            this.handler.selectProcessor(ActionEntityProcessor.class)
              .processActionEntity(request, response, this.uriInfo, requestFormat, responseFormat);
          }
        break;

        case PRIMITIVE:
          responseFormat = ContentNegotiator.doContentNegotiation(this.uriInfo.getFormatOption(),
            request, this.handler.getCustomContentTypeSupport(),
            isCollection ? RepresentationType.COLLECTION_PRIMITIVE : RepresentationType.PRIMITIVE);
          if (isCollection) {
            this.handler.selectProcessor(ActionPrimitiveCollectionProcessor.class)
              .processActionPrimitiveCollection(request, response, this.uriInfo, requestFormat,
                responseFormat);
          } else {
            this.handler.selectProcessor(ActionPrimitiveProcessor.class)
              .processActionPrimitive(request, response, this.uriInfo, requestFormat,
                responseFormat);
          }
        break;

        case COMPLEX:
          responseFormat = ContentNegotiator.doContentNegotiation(this.uriInfo.getFormatOption(),
            request, this.handler.getCustomContentTypeSupport(),
            isCollection ? RepresentationType.COLLECTION_COMPLEX : RepresentationType.COMPLEX);
          if (isCollection) {
            this.handler.selectProcessor(ActionComplexCollectionProcessor.class)
              .processActionComplexCollection(request, response, this.uriInfo, requestFormat,
                responseFormat);
          } else {
            this.handler.selectProcessor(ActionComplexProcessor.class)
              .processActionComplex(request, response, this.uriInfo, requestFormat, responseFormat);
          }
        break;

        default:
          throw new ODataHandlerException(NOT_IMPLEMENTED_MESSAGE,
            ODataHandlerException.MessageKeys.FUNCTIONALITY_NOT_IMPLEMENTED);
      }
    }
  }

  private void handleComplexDispatching(final ODataRequest request, final ODataResponse response,
    final boolean isCollection) throws ODataApplicationException, ODataLibraryException {
    final HttpMethod method = request.getMethod();
    final RepresentationType complexRepresentationType = isCollection
      ? RepresentationType.COLLECTION_COMPLEX
      : RepresentationType.COMPLEX;
    if (method == HttpMethod.GET) {
      validatePreferHeader(request);
      final ContentType requestedContentType = ContentNegotiator.doContentNegotiation(
        this.uriInfo.getFormatOption(), request, this.handler.getCustomContentTypeSupport(),
        complexRepresentationType);
      if (isCollection) {
        this.handler.selectProcessor(ComplexCollectionProcessor.class)
          .readComplexCollection(request, response, this.uriInfo, requestedContentType);
      } else {
        this.handler.selectProcessor(ComplexProcessor.class)
          .readComplex(request, response, this.uriInfo, requestedContentType);
      }
    } else if (method == HttpMethod.PUT || method == HttpMethod.PATCH
      || method == HttpMethod.POST && isCollection) {
      validatePreconditions(request, false);
      final ContentType requestFormat = getSupportedContentType(
        request.getHeader(HttpHeader.CONTENT_TYPE), complexRepresentationType, true);
      final ContentType responseFormat = ContentNegotiator.doContentNegotiation(
        this.uriInfo.getFormatOption(), request, this.handler.getCustomContentTypeSupport(),
        complexRepresentationType);
      if (isCollection) {
        this.handler.selectProcessor(ComplexCollectionProcessor.class)
          .updateComplexCollection(request, response, this.uriInfo, requestFormat, responseFormat);
      } else {
        this.handler.selectProcessor(ComplexProcessor.class)
          .updateComplex(request, response, this.uriInfo, requestFormat, responseFormat);
      }
    } else if (method == HttpMethod.DELETE) {
      validatePreferHeader(request);
      validatePreconditions(request, false);
      if (isCollection) {
        this.handler.selectProcessor(ComplexCollectionProcessor.class)
          .deleteComplexCollection(request, response, this.uriInfo);
      } else {
        this.handler.selectProcessor(ComplexProcessor.class)
          .deleteComplex(request, response, this.uriInfo);
      }
    } else {
      throwMethodNotAllowed(method);
    }
  }

  private void handleCountDispatching(final ODataRequest request, final ODataResponse response,
    final int lastPathSegmentIndex) throws ODataApplicationException, ODataLibraryException {
    validatePreferHeader(request);
    final UriResource resource = this.uriInfo.getUriResourceParts().get(lastPathSegmentIndex - 1);
    if (resource instanceof UriResourceEntitySet || resource instanceof UriResourceNavigation
      || resource instanceof UriResourceFunction
        && ((UriResourceFunction)resource).getType().getKind() == EdmTypeKind.ENTITY) {
      this.handler.selectProcessor(CountEntityCollectionProcessor.class)
        .countEntityCollection(request, response, this.uriInfo);
    } else if (resource instanceof UriResourcePrimitiveProperty
      || resource instanceof UriResourceFunction
        && ((UriResourceFunction)resource).getType().getKind() == EdmTypeKind.PRIMITIVE) {
      this.handler.selectProcessor(CountPrimitiveCollectionProcessor.class)
        .countPrimitiveCollection(request, response, this.uriInfo);
    } else {
      this.handler.selectProcessor(CountComplexCollectionProcessor.class)
        .countComplexCollection(request, response, this.uriInfo);
    }
  }

  private void handleEntityCollectionDispatching(final ODataRequest request,
    final ODataResponse response, final boolean isMedia) throws ContentNegotiatorException,
    ODataApplicationException, ODataLibraryException, ODataHandlerException {
    final HttpMethod method = request.getMethod();
    if (method == HttpMethod.GET) {
      validatePreferHeader(request);
      final ContentType requestedContentType = ContentNegotiator.doContentNegotiation(
        this.uriInfo.getFormatOption(), request, this.handler.getCustomContentTypeSupport(),
        RepresentationType.COLLECTION_ENTITY);
      this.handler.selectProcessor(EntityCollectionProcessor.class)
        .readEntityCollection(request, response, this.uriInfo, requestedContentType);
    } else if (method == HttpMethod.POST) {
      final ContentType responseFormat = ContentNegotiator.doContentNegotiation(
        this.uriInfo.getFormatOption(), request, this.handler.getCustomContentTypeSupport(),
        RepresentationType.ENTITY);
      if (isMedia) {
        validatePreferHeader(request);
        final ContentType requestFormat = ContentType
          .parse(request.getHeader(HttpHeader.CONTENT_TYPE));
        this.handler.selectProcessor(MediaEntityProcessor.class)
          .createMediaEntity(request, response, this.uriInfo, requestFormat, responseFormat);
      } else {
        try {
          final ContentType requestFormat = request.getHeader(HttpHeader.CONTENT_TYPE) == null
            && (request.getBody() == null || request.getBody().available() == 0)
              ? getSupportedContentType(request.getHeader(HttpHeader.CONTENT_TYPE),
                RepresentationType.ENTITY, false)
              : getSupportedContentType(request.getHeader(HttpHeader.CONTENT_TYPE),
                RepresentationType.ENTITY, true);
          this.handler.selectProcessor(EntityProcessor.class)
            .createEntity(request, response, this.uriInfo, requestFormat, responseFormat);
        } catch (final IOException e) {
          throw new ODataHandlerException("There is problem in the payload.",
            ODataHandlerException.MessageKeys.INVALID_PAYLOAD);
        }
      }
    } else if (method == HttpMethod.PUT && this.uriInfo.getUriResourceParts().size() == 2) {
      if (isMedia) {
        validatePreferHeader(request);
      }
      validatePreconditions(request, false);
      final ContentType requestFormat = getSupportedContentType(
        request.getHeader(HttpHeader.CONTENT_TYPE), RepresentationType.ENTITY, true);
      final ContentType responseFormat = ContentNegotiator.doContentNegotiation(
        this.uriInfo.getFormatOption(), request, this.handler.getCustomContentTypeSupport(),
        RepresentationType.ENTITY);
      this.handler.selectProcessor(EntityProcessor.class)
        .updateEntity(request, response, this.uriInfo, requestFormat, responseFormat);
    } else {
      throwMethodNotAllowed(method);
    }
  }

  private void handleEntityDispatching(final ODataRequest request, final ODataResponse response,
    final boolean isCollection, final boolean isMedia)
    throws ODataApplicationException, ODataLibraryException {
    if (isCollection) {
      handleEntityCollectionDispatching(request, response, isMedia);
    } else {
      handleSingleEntityDispatching(request, response, isMedia, false);
    }
  }

  private void handleFunctionDispatching(final ODataRequest request, final ODataResponse response,
    final UriResourceFunction uriResourceFunction)
    throws ODataApplicationException, ODataLibraryException {
    EdmFunction function = uriResourceFunction.getFunction();
    if (function == null) {
      function = uriResourceFunction.getFunctionImport().getUnboundFunctions().get(0);
    }
    final EdmReturnType returnType = function.getReturnType();
    switch (returnType.getType().getKind()) {
      case ENTITY:
        handleEntityDispatching(request, response,
          returnType.isCollection() && uriResourceFunction.getKeyPredicates().isEmpty(), false);
      break;
      case PRIMITIVE:
        handlePrimitiveDispatching(request, response, returnType.isCollection());
      break;
      case COMPLEX:
        handleComplexDispatching(request, response, returnType.isCollection());
      break;
      default:
        throw new ODataHandlerException(NOT_IMPLEMENTED_MESSAGE,
          ODataHandlerException.MessageKeys.FUNCTIONALITY_NOT_IMPLEMENTED);
    }
  }

  private void handleMediaValueDispatching(final ODataRequest request, final ODataResponse response,
    final UriResource resource) throws ContentNegotiatorException, ODataApplicationException,
    ODataLibraryException, ODataHandlerException, PreconditionException {
    final HttpMethod method = request.getMethod();
    validatePreferHeader(request);
    if (method == HttpMethod.GET) {
      // This can be a GET on an EntitySet, Navigation or Function
      final ContentType requestedContentType = ContentNegotiator.doContentNegotiation(
        this.uriInfo.getFormatOption(), request, this.handler.getCustomContentTypeSupport(),
        RepresentationType.MEDIA);
      this.handler.selectProcessor(MediaEntityProcessor.class)
        .readMediaEntity(request, response, this.uriInfo, requestedContentType);
      // PUT and DELETE can only be called on EntitySets or Navigation
      // properties which are media resources
    } else if (method == HttpMethod.PUT
      && (isEntityOrNavigationMedia(resource) || isSingletonMedia(resource))) {
      validatePreconditions(request, true);
      final ContentType requestFormat = ContentType
        .parse(request.getHeader(HttpHeader.CONTENT_TYPE));
      final ContentType responseFormat = ContentNegotiator.doContentNegotiation(
        this.uriInfo.getFormatOption(), request, this.handler.getCustomContentTypeSupport(),
        RepresentationType.ENTITY);
      this.handler.selectProcessor(MediaEntityProcessor.class)
        .updateMediaEntity(request, response, this.uriInfo, requestFormat, responseFormat);
    } else if (method == HttpMethod.DELETE && isEntityOrNavigationMedia(resource)) {
      validatePreconditions(request, true);
      this.handler.selectProcessor(MediaEntityProcessor.class)
        .deleteMediaEntity(request, response, this.uriInfo);
    } else {
      throwMethodNotAllowed(method);
    }
  }

  private void handlePrimitiveDispatching(final ODataRequest request, final ODataResponse response,
    final boolean isCollection) throws ODataApplicationException, ODataLibraryException {
    final HttpMethod method = request.getMethod();
    final RepresentationType representationType = isCollection
      ? RepresentationType.COLLECTION_PRIMITIVE
      : RepresentationType.PRIMITIVE;
    if (method == HttpMethod.GET) {
      validatePreferHeader(request);
      final ContentType requestedContentType = ContentNegotiator.doContentNegotiation(
        this.uriInfo.getFormatOption(), request, this.handler.getCustomContentTypeSupport(),
        representationType);
      if (isCollection) {
        this.handler.selectProcessor(PrimitiveCollectionProcessor.class)
          .readPrimitiveCollection(request, response, this.uriInfo, requestedContentType);
      } else {
        this.handler.selectProcessor(PrimitiveProcessor.class)
          .readPrimitive(request, response, this.uriInfo, requestedContentType);
      }
    } else if (method == HttpMethod.PUT || method == HttpMethod.PATCH
      || method == HttpMethod.POST && isCollection) {
      validatePreconditions(request, false);
      ContentType requestFormat = null;
      final List<UriResource> uriResources = this.uriInfo.getUriResourceParts();
      final UriResource uriResource = uriResources.get(uriResources.size() - 1);
      if (uriResource instanceof UriResourcePrimitiveProperty
        && ((UriResourcePrimitiveProperty)uriResource).getType()
          .getFullQualifiedName()
          .getFullQualifiedNameAsString()
          .equalsIgnoreCase(EDMSTREAM)) {
        requestFormat = ContentType.parse(request.getHeader(HttpHeader.CONTENT_TYPE));
      } else {
        requestFormat = getSupportedContentType(request.getHeader(HttpHeader.CONTENT_TYPE),
          representationType, true);
      }
      final ContentType responseFormat = ContentNegotiator.doContentNegotiation(
        this.uriInfo.getFormatOption(), request, this.handler.getCustomContentTypeSupport(),
        representationType);
      if (isCollection) {
        this.handler.selectProcessor(PrimitiveCollectionProcessor.class)
          .updatePrimitiveCollection(request, response, this.uriInfo, requestFormat,
            responseFormat);
      } else {
        this.handler.selectProcessor(PrimitiveProcessor.class)
          .updatePrimitive(request, response, this.uriInfo, requestFormat, responseFormat);
      }
    } else if (method == HttpMethod.DELETE) {
      validatePreferHeader(request);
      validatePreconditions(request, false);
      if (isCollection) {
        this.handler.selectProcessor(PrimitiveCollectionProcessor.class)
          .deletePrimitiveCollection(request, response, this.uriInfo);
      } else {
        this.handler.selectProcessor(PrimitiveProcessor.class)
          .deletePrimitive(request, response, this.uriInfo);
      }
    } else {
      throwMethodNotAllowed(method);
    }
  }

  private void handlePrimitiveValueDispatching(final ODataRequest request,
    final ODataResponse response, final UriResource resource) throws ContentNegotiatorException,
    ODataApplicationException, ODataLibraryException, ODataHandlerException, PreconditionException {
    final HttpMethod method = request.getMethod();
    final EdmType type = resource instanceof UriResourceProperty
      ? ((UriResourceProperty)resource).getType()
      : ((UriResourceFunction)resource).getType();
    final RepresentationType valueRepresentationType = type == EdmPrimitiveTypeFactory
      .getInstance(EdmPrimitiveTypeKind.Binary) ? RepresentationType.BINARY
        : RepresentationType.VALUE;
    if (method == HttpMethod.GET) {
      validatePreferHeader(request);
      final ContentType requestedContentType = ContentNegotiator.doContentNegotiation(
        this.uriInfo.getFormatOption(), request, this.handler.getCustomContentTypeSupport(),
        valueRepresentationType);

      this.handler.selectProcessor(PrimitiveValueProcessor.class)
        .readPrimitiveValue(request, response, this.uriInfo, requestedContentType);
    } else if (method == HttpMethod.PUT && resource instanceof UriResourceProperty) {
      validatePreconditions(request, false);
      final ContentType requestFormat = getSupportedContentType(
        request.getHeader(HttpHeader.CONTENT_TYPE), valueRepresentationType, true);
      final ContentType responseFormat = ContentNegotiator.doContentNegotiation(
        this.uriInfo.getFormatOption(), request, this.handler.getCustomContentTypeSupport(),
        valueRepresentationType);
      this.handler.selectProcessor(PrimitiveValueProcessor.class)
        .updatePrimitiveValue(request, response, this.uriInfo, requestFormat, responseFormat);
    } else if (method == HttpMethod.DELETE && resource instanceof UriResourceProperty) {
      validatePreferHeader(request);
      validatePreconditions(request, false);
      this.handler.selectProcessor(PrimitiveValueProcessor.class)
        .deletePrimitiveValue(request, response, this.uriInfo);
    } else {
      throwMethodNotAllowed(method);
    }
  }

  private void handleReferenceDispatching(final ODataRequest request, final ODataResponse response,
    final int lastPathSegmentIndex) throws ODataApplicationException, ODataLibraryException {
    final HttpMethod httpMethod = request.getMethod();
    final boolean isCollection = ((UriResourcePartTyped)this.uriInfo.getUriResourceParts()
      .get(lastPathSegmentIndex - 1)).isCollection();

    if (isCollection && httpMethod == HttpMethod.GET) {
      validatePreferHeader(request);
      final ContentType responseFormat = ContentNegotiator.doContentNegotiation(
        this.uriInfo.getFormatOption(), request, this.handler.getCustomContentTypeSupport(),
        RepresentationType.COLLECTION_REFERENCE);
      this.handler.selectProcessor(ReferenceCollectionProcessor.class)
        .readReferenceCollection(request, response, this.uriInfo, responseFormat);

    } else if (isCollection && httpMethod == HttpMethod.POST) {
      final ContentType requestFormat = getSupportedContentType(
        request.getHeader(HttpHeader.CONTENT_TYPE), RepresentationType.REFERENCE, true);
      this.handler.selectProcessor(ReferenceProcessor.class)
        .createReference(request, response, this.uriInfo, requestFormat);

    } else if (!isCollection && httpMethod == HttpMethod.GET) {
      validatePreferHeader(request);
      final ContentType responseFormat = ContentNegotiator.doContentNegotiation(
        this.uriInfo.getFormatOption(), request, this.handler.getCustomContentTypeSupport(),
        RepresentationType.REFERENCE);
      this.handler.selectProcessor(ReferenceProcessor.class)
        .readReference(request, response, this.uriInfo, responseFormat);

    } else if (!isCollection && (httpMethod == HttpMethod.PUT || httpMethod == HttpMethod.PATCH)) {
      final ContentType requestFormat = getSupportedContentType(
        request.getHeader(HttpHeader.CONTENT_TYPE), RepresentationType.REFERENCE, true);
      this.handler.selectProcessor(ReferenceProcessor.class)
        .updateReference(request, response, this.uriInfo, requestFormat);

    } else if (httpMethod == HttpMethod.DELETE) {
      validatePreferHeader(request);
      this.handler.selectProcessor(ReferenceProcessor.class)
        .deleteReference(request, response, this.uriInfo);

    } else {
      throwMethodNotAllowed(httpMethod);
    }
  }

  private void handleResourceDispatching(final ODataRequest request, final ODataResponse response)
    throws ODataApplicationException, ODataLibraryException {

    final int lastPathSegmentIndex = this.uriInfo.getUriResourceParts().size() - 1;
    final UriResource lastPathSegment = this.uriInfo.getUriResourceParts()
      .get(lastPathSegmentIndex);

    switch (lastPathSegment.getKind()) {
      case action:
        checkMethod(request.getMethod(), HttpMethod.POST);
        handleActionDispatching(request, response, (UriResourceAction)lastPathSegment);
      break;

      case function:
        checkMethod(request.getMethod(), HttpMethod.GET);
        handleFunctionDispatching(request, response, (UriResourceFunction)lastPathSegment);
      break;

      case entitySet:
      case navigationProperty:
        handleEntityDispatching(request, response,
          ((UriResourcePartTyped)lastPathSegment).isCollection(),
          isEntityOrNavigationMedia(lastPathSegment));
      break;

      case singleton:
        handleSingleEntityDispatching(request, response, isSingletonMedia(lastPathSegment), true);
      break;

      case count:
        checkMethod(request.getMethod(), HttpMethod.GET);
        handleCountDispatching(request, response, lastPathSegmentIndex);
      break;

      case primitiveProperty:
        handlePrimitiveDispatching(request, response,
          ((UriResourceProperty)lastPathSegment).isCollection());
      break;

      case complexProperty:
        handleComplexDispatching(request, response,
          ((UriResourceProperty)lastPathSegment).isCollection());
      break;

      case value:
        handleValueDispatching(request, response, lastPathSegmentIndex);
      break;

      case ref:
        handleReferenceDispatching(request, response, lastPathSegmentIndex);
      break;

      default:
        throw new ODataHandlerException(NOT_IMPLEMENTED_MESSAGE,
          ODataHandlerException.MessageKeys.FUNCTIONALITY_NOT_IMPLEMENTED);
    }
  }

  private void handleSingleEntityDispatching(final ODataRequest request,
    final ODataResponse response, final boolean isMedia, final boolean isSingleton)
    throws ContentNegotiatorException, ODataApplicationException, ODataLibraryException,
    ODataHandlerException, PreconditionException {
    final HttpMethod method = request.getMethod();
    if (method == HttpMethod.GET) {
      validatePreferHeader(request);
      final ContentType requestedContentType = ContentNegotiator.doContentNegotiation(
        this.uriInfo.getFormatOption(), request, this.handler.getCustomContentTypeSupport(),
        RepresentationType.ENTITY);
      this.handler.selectProcessor(EntityProcessor.class)
        .readEntity(request, response, this.uriInfo, requestedContentType);
    } else if (method == HttpMethod.PUT || method == HttpMethod.PATCH) {
      if (isMedia) {
        validatePreferHeader(request);
      }
      validatePreconditions(request, false);
      final ContentType requestFormat = getSupportedContentType(
        request.getHeader(HttpHeader.CONTENT_TYPE), RepresentationType.ENTITY, true);
      final ContentType responseFormat = ContentNegotiator.doContentNegotiation(
        this.uriInfo.getFormatOption(), request, this.handler.getCustomContentTypeSupport(),
        RepresentationType.ENTITY);
      this.handler.selectProcessor(EntityProcessor.class)
        .updateEntity(request, response, this.uriInfo, requestFormat, responseFormat);
    } else if (method == HttpMethod.DELETE && !isSingleton) {
      validateIsSingleton(method);
      validatePreconditions(request, false);
      validatePreferHeader(request);
      if (isMedia) {
        this.handler.selectProcessor(MediaEntityProcessor.class)
          .deleteEntity(request, response, this.uriInfo);
      } else {
        this.handler.selectProcessor(EntityProcessor.class)
          .deleteEntity(request, response, this.uriInfo);
      }
    } else {
      throwMethodNotAllowed(method);
    }
  }

  private void handleValueDispatching(final ODataRequest request, final ODataResponse response,
    final int lastPathSegmentIndex) throws ODataApplicationException, ODataLibraryException {
    // The URI Parser already checked if $value is allowed here so we only have
    // to dispatch to the correct processor
    final UriResource resource = this.uriInfo.getUriResourceParts().get(lastPathSegmentIndex - 1);
    if (resource instanceof UriResourceProperty || resource instanceof UriResourceFunction
      && ((UriResourceFunction)resource).getType().getKind() == EdmTypeKind.PRIMITIVE) {
      handlePrimitiveValueDispatching(request, response, resource);
    } else {
      handleMediaValueDispatching(request, response, resource);
    }
  }

  private boolean isEntityOrNavigationMedia(final UriResource pathSegment) {
    // This method MUST NOT check if the resource is of type function since
    // these are handled differently
    return pathSegment instanceof UriResourceEntitySet
      && ((UriResourceEntitySet)pathSegment).getEntityType().hasStream()
      || pathSegment instanceof UriResourceNavigation
        && ((EdmEntityType)((UriResourceNavigation)pathSegment).getType()).hasStream();
  }

  private boolean isSingletonMedia(final UriResource pathSegment) {
    return pathSegment instanceof UriResourceSingleton
      && ((UriResourceSingleton)pathSegment).getEntityType().hasStream();
  }

  private void throwMethodNotAllowed(final HttpMethod httpMethod) throws ODataHandlerException {
    throw new ODataHandlerException("HTTP method " + httpMethod + " is not allowed.",
      ODataHandlerException.MessageKeys.HTTP_METHOD_NOT_ALLOWED, httpMethod.toString());
  }

  /* Delete method is not allowed for Entities navigating to Singleton */
  private void validateIsSingleton(final HttpMethod method) throws ODataHandlerException {
    final int lastPathSegmentIndex = this.uriInfo.getUriResourceParts().size() - 1;
    final UriResource pathSegment = this.uriInfo.getUriResourceParts().get(lastPathSegmentIndex);
    if (pathSegment instanceof UriResourceNavigation
      && this.uriInfo.getUriResourceParts()
        .get(lastPathSegmentIndex - 1) instanceof UriResourceEntitySet
      && ((UriResourceEntitySet)this.uriInfo.getUriResourceParts().get(lastPathSegmentIndex - 1))
        .getEntitySet()
        .getRelatedBindingTarget(pathSegment.getSegmentValue()) instanceof EdmSingleton) {
      throwMethodNotAllowed(method);
    }
  }

  private void validatePreconditions(final ODataRequest request, final boolean isMediaValue)
    throws PreconditionException {
    // If needed perform preconditions validation.
    final CustomETagSupport eTagSupport = this.handler.getCustomETagSupport();
    if (eTagSupport != null
      && new PreconditionsValidator(this.uriInfo).mustValidatePreconditions(eTagSupport,
        isMediaValue)
      && request.getHeader(HttpHeader.IF_MATCH) == null
      && request.getHeader(HttpHeader.IF_NONE_MATCH) == null) {
      throw new PreconditionException("Expected an if-match or if-none-match header.",
        PreconditionException.MessageKeys.MISSING_HEADER);
    }
  }

  /**Checks if Prefer header is set with return=minimal or
   * return=representation for GET and DELETE requests
   * @param request
   * @throws ODataHandlerException
   */
  private void validatePreferHeader(final ODataRequest request) throws ODataHandlerException {
    final List<String> returnPreference = request.getHeaders(HttpHeader.PREFER);
    if (null != returnPreference) {
      for (final String preference : returnPreference) {
        if (preference.equals(RETURN_MINIMAL) || preference.equals(RETURN_REPRESENTATION)) {
          throw new ODataHandlerException("Prefer Header not supported: " + preference,
            ODataHandlerException.MessageKeys.INVALID_PREFER_HEADER, preference);
        }
      }
    }
  }

}
