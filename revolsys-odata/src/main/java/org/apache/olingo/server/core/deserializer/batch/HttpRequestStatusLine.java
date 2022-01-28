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
package org.apache.olingo.server.core.deserializer.batch;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.deserializer.batch.BatchDeserializerException;
import org.apache.olingo.server.api.deserializer.batch.BatchDeserializerException.MessageKeys;

public class HttpRequestStatusLine {
  private static final Pattern PATTERN_RELATIVE_URI = Pattern.compile("([^/][^?]*)(?:\\?(.*))?");

  private static final Set<HttpMethod> HTTP_CHANGE_SET_METHODS = new HashSet<>(
    Arrays.asList(new HttpMethod[] {
      HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.PATCH
    }));

  private static final String HTTP_VERSION = "HTTP/1.1";

  final private Line statusLine;

  final private String requestBaseUri;

  private HttpMethod method;

  private String httpVersion;

  private final String rawServiceResolutionUri;

  private String rawQueryPath;

  private String rawODataPath;

  private String rawBaseUri;

  private String rawRequestUri;

  public HttpRequestStatusLine(final Line httpStatusLine, final String baseUri,
    final String serviceResolutionUri) throws BatchDeserializerException {
    this.statusLine = httpStatusLine;
    this.requestBaseUri = baseUri;
    this.rawServiceResolutionUri = serviceResolutionUri;

    parse();
  }

  private void buildUri(final String oDataPath, final String queryOptions)
    throws BatchDeserializerException {
    this.rawBaseUri = this.requestBaseUri;
    this.rawODataPath = "/" + oDataPath;
    this.rawRequestUri = this.requestBaseUri + this.rawODataPath;

    if (queryOptions != null) {
      this.rawRequestUri += "?" + queryOptions;
      this.rawQueryPath = queryOptions;
    } else {
      this.rawQueryPath = "";
    }
  }

  public String getHttpVersion() {
    return this.httpVersion;
  }

  public int getLineNumber() {
    return this.statusLine.getLineNumber();
  }

  public HttpMethod getMethod() {
    return this.method;
  }

  public String getRawBaseUri() {
    return this.rawBaseUri;
  }

  public String getRawODataPath() {
    return this.rawODataPath;
  }

  public String getRawQueryPath() {
    return this.rawQueryPath;
  }

  public String getRawRequestUri() {
    return this.rawRequestUri;
  }

  public String getRawServiceResolutionUri() {
    return this.rawServiceResolutionUri;
  }

  public String getRequestBaseUri() {
    return this.requestBaseUri;
  }

  private void parse() throws BatchDeserializerException {
    final String[] parts = this.statusLine.toString().split(" ");

    // Status line consists of 3 parts: Method, URI and HTTP Version
    if (parts.length == 3) {
      this.method = parseMethod(parts[0]);
      parseUri(parts[1], this.requestBaseUri);
      this.httpVersion = parseHttpVersion(parts[2]);
    } else {
      throw new BatchDeserializerException("Invalid status line", MessageKeys.INVALID_STATUS_LINE,
        Integer.toString(this.statusLine.getLineNumber()));
    }
  }

  private void parseAbsoluteUri(final String rawUri, final String baseUri)
    throws BatchDeserializerException {
    if (rawUri.startsWith(baseUri)) {
      final String relativeUri = removeLeadingSlash(rawUri.substring(baseUri.length()));
      parseRelativeUri(relativeUri);
    } else {
      throw new BatchDeserializerException("Base uri does not match", MessageKeys.INVALID_BASE_URI,
        Integer.toString(this.statusLine.getLineNumber()));
    }
  }

  private String parseHttpVersion(final String httpVersion) throws BatchDeserializerException {
    if (!HTTP_VERSION.equals(httpVersion.trim())) {
      throw new BatchDeserializerException("Invalid http version", MessageKeys.INVALID_HTTP_VERSION,
        Integer.toString(this.statusLine.getLineNumber()));
    } else {
      return HTTP_VERSION;
    }
  }

  private HttpMethod parseMethod(final String method) throws BatchDeserializerException {
    try {
      return HttpMethod.valueOf(method.trim());
    } catch (final IllegalArgumentException e) {
      throw new BatchDeserializerException("Illegal http method", e, MessageKeys.INVALID_METHOD,
        Integer.toString(this.statusLine.getLineNumber()));
    }
  }

  private void parseRelativeUri(final String rawUri) throws BatchDeserializerException {
    final Matcher relativeUriMatcher = PATTERN_RELATIVE_URI.matcher(rawUri);

    if (relativeUriMatcher.matches()) {
      buildUri(relativeUriMatcher.group(1), relativeUriMatcher.group(2));
    } else {
      throw new BatchDeserializerException("Malformed uri", MessageKeys.INVALID_URI,
        Integer.toString(this.statusLine.getLineNumber()));
    }
  }

  private void parseUri(final String rawUri, final String baseUri)
    throws BatchDeserializerException {
    try {
      final URI uri = new URI(rawUri);

      if (uri.isAbsolute()) {
        parseAbsoluteUri(rawUri, baseUri);
      } else {
        final URI base = URI.create(baseUri);
        if (rawUri.startsWith(base.getRawPath())) {
          parseRelativeUri(removeLeadingSlash(rawUri.substring(base.getRawPath().length())));
        } else {
          parseRelativeUri(rawUri);
        }
      }
    } catch (final URISyntaxException e) {
      throw new BatchDeserializerException("Malformed uri", e, MessageKeys.INVALID_URI,
        Integer.toString(this.statusLine.getLineNumber()));
    }
  }

  private String removeLeadingSlash(final String value) {
    return value.length() > 0 && value.charAt(0) == '/' ? value.substring(1) : value;
  }

  public void validateHttpMethod(final boolean isChangeSet) throws BatchDeserializerException {
    if (isChangeSet && !HTTP_CHANGE_SET_METHODS.contains(getMethod())) {
      throw new BatchDeserializerException("Invalid change set method",
        MessageKeys.INVALID_CHANGESET_METHOD, Integer.toString(this.statusLine.getLineNumber()));
    }
  }
}
