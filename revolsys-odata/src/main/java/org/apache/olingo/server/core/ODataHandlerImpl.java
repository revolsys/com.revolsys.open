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

import java.util.LinkedList;
import java.util.List;

import org.apache.olingo.commons.api.edm.constants.ODataServiceVersion;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataHandler;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ODataServerError;
import org.apache.olingo.server.api.OlingoExtension;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.etag.CustomETagSupport;
import org.apache.olingo.server.api.etag.PreconditionException;
import org.apache.olingo.server.api.processor.DefaultProcessor;
import org.apache.olingo.server.api.processor.ErrorProcessor;
import org.apache.olingo.server.api.processor.Processor;
import org.apache.olingo.server.api.serializer.CustomContentTypeSupport;
import org.apache.olingo.server.api.serializer.RepresentationType;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.FormatOption;
import org.apache.olingo.server.core.uri.parser.Parser;
import org.apache.olingo.server.core.uri.parser.UriParserException;
import org.apache.olingo.server.core.uri.parser.UriParserSemanticException;
import org.apache.olingo.server.core.uri.parser.UriParserSyntaxException;
import org.apache.olingo.server.core.uri.validator.UriValidationException;
import org.apache.olingo.server.core.uri.validator.UriValidator;
import org.jeometry.common.logging.Logs;

public class ODataHandlerImpl implements ODataHandler {

  private final OData odata;

  private final ServiceMetadata serviceMetadata;

  private final List<Processor> processors = new LinkedList<>();

  private CustomContentTypeSupport customContentTypeSupport;

  private CustomETagSupport customETagSupport;

  private Exception lastThrownException;

  public ODataHandlerImpl(final OData odata, final ServiceMetadata serviceMetadata) {
    this.odata = odata;
    this.serviceMetadata = serviceMetadata;

    register(new DefaultRedirectProcessor());
    register(new DefaultProcessor());
  }

  public CustomContentTypeSupport getCustomContentTypeSupport() {
    return this.customContentTypeSupport;
  }

  public CustomETagSupport getCustomETagSupport() {
    return this.customETagSupport;
  }

  public Exception getLastThrownException() {
    return this.lastThrownException;
  }

  public void handleException(final ODataRequest request, final ODataResponse response,
    final ODataServerError serverError, final Exception exception) {
    Logs.error(this, exception);
    this.lastThrownException = exception;
    ErrorProcessor exceptionProcessor;
    try {
      exceptionProcessor = selectProcessor(ErrorProcessor.class);
    } catch (final ODataHandlerException e) {
      // This cannot happen since there is always an ExceptionProcessor
      // registered.
      exceptionProcessor = new DefaultProcessor();
    }
    ContentType requestedContentType;
    try {
      final FormatOption formatOption = request.getFormatOption();
      requestedContentType = ContentNegotiator.doContentNegotiation(formatOption, request,
        getCustomContentTypeSupport(), RepresentationType.ERROR);
    } catch (final AcceptHeaderContentNegotiatorException e) {
      requestedContentType = ContentType.JSON;
    } catch (final ContentNegotiatorException e) {
      requestedContentType = ContentType.JSON;
    }
    exceptionProcessor.processError(request, response, serverError, requestedContentType);
  }

  @Override
  public ODataResponse process(final ODataRequest request) {
    final ODataResponse response = new ODataResponse();
    try {
      processInternal(request, response);
    } catch (final UriValidationException e) {
      final ODataServerError serverError = ODataExceptionHelper.createServerErrorObject(e, null);
      handleException(request, response, serverError, e);
    } catch (final UriParserSemanticException e) {
      final ODataServerError serverError = ODataExceptionHelper.createServerErrorObject(e, null);
      handleException(request, response, serverError, e);
    } catch (final UriParserSyntaxException e) {
      final ODataServerError serverError = ODataExceptionHelper.createServerErrorObject(e, null);
      handleException(request, response, serverError, e);
    } catch (final UriParserException e) {
      final ODataServerError serverError = ODataExceptionHelper.createServerErrorObject(e, null);
      handleException(request, response, serverError, e);
    } catch (final AcceptHeaderContentNegotiatorException e) {
      final ODataServerError serverError = ODataExceptionHelper.createServerErrorObject(e, null);
      handleException(request, response, serverError, e);
    } catch (final ContentNegotiatorException e) {
      final ODataServerError serverError = ODataExceptionHelper.createServerErrorObject(e, null);
      handleException(request, response, serverError, e);
    } catch (final SerializerException e) {
      final ODataServerError serverError = ODataExceptionHelper.createServerErrorObject(e, null);
      handleException(request, response, serverError, e);
    } catch (final DeserializerException e) {
      final ODataServerError serverError = ODataExceptionHelper.createServerErrorObject(e, null);
      handleException(request, response, serverError, e);
    } catch (final PreconditionException e) {
      final ODataServerError serverError = ODataExceptionHelper.createServerErrorObject(e, null);
      handleException(request, response, serverError, e);
    } catch (final ODataHandlerException e) {
      final ODataServerError serverError = ODataExceptionHelper.createServerErrorObject(e, null);
      handleException(request, response, serverError, e);
    } catch (final ODataApplicationException e) {
      final ODataServerError serverError = ODataExceptionHelper.createServerErrorObject(e);
      handleException(request, response, serverError, e);
    } catch (final Exception e) {
      final ODataServerError serverError = ODataExceptionHelper.createServerErrorObject(e);
      handleException(request, response, serverError, e);
    }
    return response;
  }

  private void processInternal(final ODataRequest request, final ODataResponse response)
    throws ODataApplicationException, ODataLibraryException {

    response.setHeader(HttpHeader.ODATA_VERSION, ODataServiceVersion.V40.toString());

    try {
      validateODataVersion(request);
    } catch (final ODataHandlerException e) {
      throw e;
    }

    UriInfo uriInfo;
    try {
      uriInfo = new Parser(this.serviceMetadata.getEdm(), this.odata).parseUri(
        request.getRawODataPath(), request.getRawQueryPath(), null, request.getRawBaseUri());
      request.setUriInfo(uriInfo);
    } catch (final ODataLibraryException e) {
      throw e;
    }
    final HttpMethod method = request.getMethod();
    try {
      new UriValidator().validate(uriInfo, method);
    } catch (final UriValidationException e) {
      throw e;
    }
    new ODataDispatcher(uriInfo, this).dispatch(request, response);
  }

  @Override
  public void register(final OlingoExtension extension) {
    if (extension instanceof CustomContentTypeSupport) {
      this.customContentTypeSupport = (CustomContentTypeSupport)extension;
    } else if (extension instanceof CustomETagSupport) {
      this.customETagSupport = (CustomETagSupport)extension;
    } else {
      throw new ODataRuntimeException(
        "Got not supported exception with class name " + extension.getClass().getSimpleName());
    }
  }

  @Override
  public void register(final Processor processor) {
    this.processors.add(0, processor);
  }

  <T extends Processor> T selectProcessor(final Class<T> cls) throws ODataHandlerException {
    for (final Processor processor : this.processors) {
      if (cls.isAssignableFrom(processor.getClass())) {
        processor.init(this.odata, this.serviceMetadata);
        return cls.cast(processor);
      }
    }
    throw new ODataHandlerException("Processor: " + cls.getSimpleName() + " not registered.",
      ODataHandlerException.MessageKeys.PROCESSOR_NOT_IMPLEMENTED, cls.getSimpleName());
  }

  private void validateODataVersion(final ODataRequest request) throws ODataHandlerException {
    final String odataVersion = request.getHeader(HttpHeader.ODATA_VERSION);
    if (odataVersion != null && !ODataServiceVersion.isValidODataVersion(odataVersion)) {
      throw new ODataHandlerException("ODataVersion not supported: " + odataVersion,
        ODataHandlerException.MessageKeys.ODATA_VERSION_NOT_SUPPORTED, odataVersion);
    }

    final String maxVersion = request.getHeader(HttpHeader.ODATA_MAX_VERSION);
    if (maxVersion != null && !ODataServiceVersion.isValidMaxODataVersion(maxVersion)) {
      throw new ODataHandlerException("ODataVersion not supported: " + maxVersion,
        ODataHandlerException.MessageKeys.ODATA_VERSION_NOT_SUPPORTED, maxVersion);
    }
  }
}
