package com.revolsys.http;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpUriRequest;
import org.jeometry.common.exception.Exceptions;

public class AzureSharedKeyLiteRequestBuilder extends ApacheHttpRequestBuilder {

  private static final char NEWLINE = '\n';

  private static final char COLON = ':';

  private static final char SLASH = '/';

  private static final char COMMA = '/';

  private static final List<String> STANDARD_HEADERS = Arrays.asList("Content-MD5", "Content-Type",
    "Date");

  public AzureSharedKeyLiteRequestBuilder(final ApacheHttpRequestBuilderFactory factory) {
    super(factory);
  }

  @Override
  public HttpUriRequest build() {
    final String accountName = getFactory().getAccountName();
    try {
      final Instant now = Instant.now();
      final String date = AzureSharedKeyRequestBuilder.DATE_FORMAT.withZone(ZoneOffset.UTC)
        .format(now);
      setHeader("Date", date);

      final StringBuilder data = new StringBuilder();
      final String method = getMethod();
      data.append(method);
      data.append(NEWLINE);

      for (final String name : STANDARD_HEADERS) {
        Header header = getFirstHeader(name);
        if (header == null) {
          if ("Content-Type".equals(name)) {
            final HttpEntity entity = getEntity();
            if (entity != null) {
              header = entity.getContentType();
            }
          }
        }
        if (header != null) {
          final String value = header.getValue();
          if (value != null) {
            data.append(value);
          }
        }
        data.append(NEWLINE);
      }
      for (final String name : getHeaderNames()) {
        if (name.startsWith("x-ms-")) {
          data.append(name);
          data.append(COLON);
          final Header header = getFirstHeader(name);
          data.append(header.getValue());
          data.append(NEWLINE);
        }
      }
      data.append(SLASH);
      data.append(accountName);
      final String path = getUri().getRawPath();
      data.append(path);
      final Map<String, Set<String>> parameters = new TreeMap<>();
      for (final NameValuePair parameter : getParameters()) {
        final String name = parameter.getName().toLowerCase();
        final String value = parameter.getValue();
        Set<String> values = parameters.get(name);
        if (values == null) {
          values = new TreeSet<>();
          parameters.put(name, values);
        }
        values.add(value);
      }
      for (final String name : parameters.keySet()) {
        data.append(NEWLINE);
        data.append(name);
        data.append(COLON);
        final Set<String> values = parameters.get(name);
        boolean first = true;
        for (final String value : values) {
          if (first) {
            first = false;
          } else {
            data.append(COMMA);
          }
          data.append(value);
        }
      }
      final String authorization = getFactory().getSharedKeyLiteAuthorization(data);
      setHeader("Authorization", authorization);
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
    return super.build();
  }

  @Override
  public AzureSharedKeyLiteRequestBuilderFactory getFactory() {
    return (AzureSharedKeyLiteRequestBuilderFactory)super.getFactory();
  }

}
