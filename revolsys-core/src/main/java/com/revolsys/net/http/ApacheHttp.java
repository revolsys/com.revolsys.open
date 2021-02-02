package com.revolsys.net.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.net.oauth.OAuthBadRequestException;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonParser;

public class ApacheHttp {

  public static JsonObject getJson(final HttpResponse response) throws IOException {
    final HttpEntity entity = response.getEntity();
    try (
      InputStream in = entity.getContent()) {
      return JsonParser.read(in);
    }
  }

  public static JsonObject getJson(final RequestBuilder requestBuilder) {
    final CloseableHttpClient httpClient = HttpClientBuilder//
      .create()
      .build();

    final HttpUriRequest request = requestBuilder.build();
    try {
      final HttpResponse response = httpClient.execute(request);
      final StatusLine statusLine = response.getStatusLine();
      if (statusLine.getStatusCode() == 200) {
        return getJson(response);
      } else if (statusLine.getStatusCode() == 400) {
        final JsonObject error = getJson(response);
        throw new OAuthBadRequestException(error);
      } else {
        final HttpEntity entity = response.getEntity();
        EntityUtils.consume(entity);
        throw new IllegalStateException("Invalid status: " + statusLine);
      }
    } catch (final OAuthBadRequestException e) {
      throw e;
    } catch (final Exception e) {
      throw Exceptions.wrap(request.getURI().toString(), e);
    }

  }

  public static StringEntity newEntity(final JsonObject body) {
    final String jsonString = body.toJsonString();
    final StringEntity entity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
    return entity;
  }

  public static RequestBuilder setJsonBody(final RequestBuilder requestBuilder,
    final JsonObject body) {
    final StringEntity entity = newEntity(body);
    requestBuilder.setEntity(entity);
    return requestBuilder;
  }
}
