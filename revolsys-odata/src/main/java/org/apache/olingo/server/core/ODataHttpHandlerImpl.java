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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataContent;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ODataServerError;
import org.apache.olingo.server.api.OlingoExtension;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerException;
import org.apache.olingo.server.api.etag.CustomETagSupport;
import org.apache.olingo.server.api.processor.Processor;
import org.apache.olingo.server.api.serializer.CustomContentTypeSupport;

public class ODataHttpHandlerImpl implements ODataHttpHandler {

  public static final int COPY_BUFFER_SIZE = 8192;

  private static final String REQUESTMAPPING = "requestMapping";

  private static void closeStream(final Channel closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (final IOException e) {
        // ignore
      }
    }
  }

  static void convertToHttp(final HttpServletResponse response, final ODataResponse odResponse) {
    response.setStatus(odResponse.getStatusCode());

    for (final Entry<String, List<String>> entry : odResponse.getAllHeaders().entrySet()) {
      for (final String headerValue : entry.getValue()) {
        response.addHeader(entry.getKey(), headerValue);
      }
    }

    if (odResponse.getContent() != null) {
      copyContent(odResponse.getContent(), response);
    } else if (odResponse.getODataContent() != null) {
      writeContent(odResponse, response);
    }
  }

  static void copyContent(final InputStream inputStream,
    final HttpServletResponse servletResponse) {
    copyContent(Channels.newChannel(inputStream), servletResponse);
  }

  static void copyContent(final ReadableByteChannel input,
    final HttpServletResponse servletResponse) {
    try (
      WritableByteChannel output = Channels.newChannel(servletResponse.getOutputStream());) {
      final ByteBuffer inBuffer = ByteBuffer.allocate(COPY_BUFFER_SIZE);
      while (input.read(inBuffer) > 0) {
        inBuffer.flip();
        output.write(inBuffer);
        inBuffer.clear();
      }
    } catch (final IOException e) {
      throw new ODataRuntimeException("Error on reading request content", e);
    } finally {
      closeStream(input);
    }
  }

  static void copyHeaders(final ODataRequest odRequest, final HttpServletRequest req) {
    for (final Enumeration<?> headerNames = req.getHeaderNames(); headerNames.hasMoreElements();) {
      final String headerName = (String)headerNames.nextElement();
      @SuppressWarnings("unchecked")
      // getHeaders() says it returns an Enumeration of String.
      final List<String> headerValues = Collections.list(req.getHeaders(headerName));
      odRequest.addHeader(headerName, headerValues);
    }
  }

  static HttpMethod extractMethod(final HttpServletRequest httpRequest)
    throws ODataLibraryException {
    final HttpMethod httpRequestMethod;
    try {
      httpRequestMethod = HttpMethod.valueOf(httpRequest.getMethod());
    } catch (final IllegalArgumentException e) {
      throw new ODataHandlerException("HTTP method not allowed" + httpRequest.getMethod(), e,
        ODataHandlerException.MessageKeys.HTTP_METHOD_NOT_ALLOWED, httpRequest.getMethod());
    }
    try {
      if (httpRequestMethod == HttpMethod.POST) {
        final String xHttpMethod = httpRequest.getHeader(HttpHeader.X_HTTP_METHOD);
        final String xHttpMethodOverride = httpRequest.getHeader(HttpHeader.X_HTTP_METHOD_OVERRIDE);

        if (xHttpMethod == null && xHttpMethodOverride == null) {
          return httpRequestMethod;
        } else if (xHttpMethod == null) {
          return HttpMethod.valueOf(xHttpMethodOverride);
        } else if (xHttpMethodOverride == null) {
          return HttpMethod.valueOf(xHttpMethod);
        } else {
          if (!xHttpMethod.equalsIgnoreCase(xHttpMethodOverride)) {
            throw new ODataHandlerException("Ambiguous X-HTTP-Methods",
              ODataHandlerException.MessageKeys.AMBIGUOUS_XHTTP_METHOD, xHttpMethod,
              xHttpMethodOverride);
          }
          return HttpMethod.valueOf(xHttpMethod);
        }
      } else {
        return httpRequestMethod;
      }
    } catch (final IllegalArgumentException e) {
      throw new ODataHandlerException("Invalid HTTP method" + httpRequest.getMethod(), e,
        ODataHandlerException.MessageKeys.INVALID_HTTP_METHOD, httpRequest.getMethod());
    }
  }

  static void fillUriInformation(final ODataRequest odRequest, final HttpServletRequest httpRequest,
    final int split) {
    final String rawRequestUri = httpRequest.getRequestURL().toString();

    String rawServiceResolutionUri = null;
    String rawODataPath;
    // Application need to set the request mapping attribute if the request is
    // coming from a spring based application
    if (httpRequest.getAttribute(REQUESTMAPPING) != null) {
      final String requestMapping = httpRequest.getAttribute(REQUESTMAPPING).toString();
      rawServiceResolutionUri = requestMapping;
      final int beginIndex = rawRequestUri.indexOf(requestMapping) + requestMapping.length();
      rawODataPath = rawRequestUri.substring(beginIndex);
    } else if (!"".equals(httpRequest.getServletPath())) {
      final int beginIndex = rawRequestUri.indexOf(httpRequest.getServletPath())
        + httpRequest.getServletPath().length();
      rawODataPath = rawRequestUri.substring(beginIndex);
    } else if (!"".equals(httpRequest.getContextPath())) {
      final int beginIndex = rawRequestUri.indexOf(httpRequest.getContextPath())
        + httpRequest.getContextPath().length();
      rawODataPath = rawRequestUri.substring(beginIndex);
    } else {
      rawODataPath = httpRequest.getRequestURI();
    }

    if (split > 0) {
      rawServiceResolutionUri = rawODataPath;
      for (int i = 0; i < split; i++) {
        final int index = rawODataPath.indexOf('/', 1);
        if (-1 == index) {
          rawODataPath = "";
          break;
        } else {
          rawODataPath = rawODataPath.substring(index);
        }
      }
      final int end = rawServiceResolutionUri.length() - rawODataPath.length();
      rawServiceResolutionUri = rawServiceResolutionUri.substring(0, end);
    }

    final String rawBaseUri = rawRequestUri.substring(0,
      rawRequestUri.length() - rawODataPath.length());

    odRequest.setRawQueryPath(httpRequest.getQueryString());
    odRequest.setRawRequestUri(rawRequestUri
      + (httpRequest.getQueryString() == null ? "" : "?" + httpRequest.getQueryString()));
    odRequest.setRawODataPath(rawODataPath);
    odRequest.setRawBaseUri(rawBaseUri);
    odRequest.setRawServiceResolutionUri(rawServiceResolutionUri);
  }

  static void writeContent(final ODataResponse odataResponse,
    final HttpServletResponse servletResponse) {
    try (
      ServletOutputStream out = servletResponse.getOutputStream()) {
      final ODataContent res = odataResponse.getODataContent();
      res.write(out);
    } catch (final IOException e) {
      throw new ODataRuntimeException("Error on reading request content", e);
    }
  }

  private final ODataHandlerImpl handler;

  private int split = 0;

  public ODataHttpHandlerImpl(final OData odata, final ServiceMetadata serviceMetadata) {
    this.handler = new ODataHandlerImpl(odata, serviceMetadata);
  }

  private ODataRequest fillODataRequest(final ODataRequest odRequest,
    final HttpServletRequest httpRequest, final int split) throws ODataLibraryException {
    try {
      odRequest.setBody(httpRequest.getInputStream());
      odRequest.setProtocol(httpRequest.getProtocol());
      odRequest.setMethod(extractMethod(httpRequest));
      copyHeaders(odRequest, httpRequest);
      fillUriInformation(odRequest, httpRequest, split);
      return odRequest;
    } catch (final IOException e) {
      throw new DeserializerException("An I/O exception occurred.", e,
        DeserializerException.MessageKeys.IO_EXCEPTION);
    } finally {
    }
  }

  private ODataResponse handleException(final ODataRequest odRequest, final Exception e) {
    final ODataResponse resp = new ODataResponse();
    ODataServerError serverError;
    if (e instanceof ODataHandlerException) {
      serverError = ODataExceptionHelper.createServerErrorObject((ODataHandlerException)e, null);
    } else if (e instanceof ODataLibraryException) {
      serverError = ODataExceptionHelper.createServerErrorObject((ODataLibraryException)e, null);
    } else {
      serverError = ODataExceptionHelper.createServerErrorObject(e);
    }
    this.handler.handleException(odRequest, resp, serverError, e);
    return resp;
  }

  @Override
  public void process(final HttpServletRequest request, final HttpServletResponse response) {
    final ODataRequest odRequest = new ODataRequest();
    ODataResponse odResponse;
    try {
      fillODataRequest(odRequest, request, this.split);

      odResponse = process(odRequest);
      // ALL future methods after process must not throw exceptions!
    } catch (final Exception e) {
      odResponse = handleException(odRequest, e);
    }

    convertToHttp(response, odResponse);
  }

  @Override
  public ODataResponse process(final ODataRequest request) {
    return this.handler.process(request);
  }

  @Override
  public void register(final CustomContentTypeSupport customContentTypeSupport) {
    this.handler.register(customContentTypeSupport);
  }

  @Override
  public void register(final CustomETagSupport customConcurrencyControlSupport) {
    this.handler.register(customConcurrencyControlSupport);
  }

  @Override
  public void register(final OlingoExtension extension) {
    this.handler.register(extension);
  }

  @Override
  public void register(final Processor processor) {
    this.handler.register(processor);
  }

  @Override
  public void setSplit(final int split) {
    this.split = split;
  }
}
