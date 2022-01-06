package com.revolsys.http;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.revolsys.net.http.ApacheHttp;
import com.revolsys.record.io.format.json.JsonObject;

public class ApacheHttpRequestBuilder {

  private final RequestBuilder builder;

  private final Set<String> headerNames = new TreeSet<>();

  private final ApacheHttpRequestBuilderFactory factory;

  public ApacheHttpRequestBuilder(final ApacheHttpRequestBuilderFactory factory,
    final RequestBuilder builder) {
    this.builder = builder;
    this.factory = factory;
  }

  public ApacheHttpRequestBuilder addHeader(final Header header) {
    this.headerNames.add(header.getName());
    this.builder.addHeader(header);
    return this;
  }

  public ApacheHttpRequestBuilder addHeader(final String name, final String value) {
    this.headerNames.add(name);
    this.builder.addHeader(name, value);
    return this;
  }

  public ApacheHttpRequestBuilder addParameter(final NameValuePair parameter) {
    this.builder.addParameter(parameter);
    return this;
  }

  public ApacheHttpRequestBuilder addParameter(final String name, final String value) {
    this.builder.addParameter(name, value);
    return this;
  }

  public ApacheHttpRequestBuilder addParameters(final List<NameValuePair> parameters) {
    for (final NameValuePair parameter : parameters) {
      this.builder.addParameter(parameter);
    }
    return this;
  }

  public ApacheHttpRequestBuilder addParameters(final NameValuePair... parameters) {
    this.builder.addParameters(parameters);
    return this;
  }

  public ApacheHttpRequestBuilder apply(final Consumer<ApacheHttpRequestBuilder> action) {
    action.accept(this);
    return this;
  }

  public HttpUriRequest build() {
    preBuild(this.builder);
    return this.builder.build();
  }

  public void execute() {
    final Consumer<HttpResponse> noop = r -> {
    };
    execute(noop);
  }

  public void execute(final Consumer<HttpResponse> action) {
    final HttpUriRequest request = build();
    ApacheHttp.execute(request, action);
  }

  public <V> V execute(final Function<HttpResponse, V> action) {
    final HttpUriRequest request = build();
    return ApacheHttp.execute(request, action);
  }

  public Charset getCharset() {
    return this.builder.getCharset();
  }

  public RequestConfig getConfig() {
    return this.builder.getConfig();
  }

  public HttpEntity getEntity() {
    return this.builder.getEntity();
  }

  public ApacheHttpRequestBuilderFactory getFactory() {
    return this.factory;
  }

  public Header getFirstHeader(final String name) {
    return this.builder.getFirstHeader(name);
  }

  public Set<String> getHeaderNames() {
    return Collections.unmodifiableSet(this.headerNames);
  }

  public Header[] getHeaders(final String name) {
    return this.builder.getHeaders(name);
  }

  public JsonObject getJson() {
    final Function<HttpResponse, JsonObject> function = ApacheHttp::getJson;
    return execute(function);
  }

  public Header getLastHeader(final String name) {
    return this.builder.getLastHeader(name);
  }

  public String getMethod() {
    return this.builder.getMethod();
  }

  public List<NameValuePair> getParameters() {
    return this.builder.getParameters();
  }

  public String getString() {
    final Function<HttpResponse, String> function = ApacheHttp::getString;
    return execute(function);
  }

  public URI getUri() {
    return this.builder.getUri();
  }

  public ProtocolVersion getVersion() {
    return this.builder.getVersion();
  }

  public InputStream newInputStream() {
    final HttpUriRequest request = build();
    return ApacheHttp.getInputStream(request);
  }

  protected void preBuild(final RequestBuilder builder2) {
  }

  public ApacheHttpRequestBuilder removeHeader(final Header header) {
    this.builder.removeHeader(header);
    return this;
  }

  public ApacheHttpRequestBuilder removeHeaders(final String name) {
    this.headerNames.remove(name);
    this.builder.removeHeaders(name);
    return this;
  }

  public ApacheHttpRequestBuilder setCharset(final Charset charset) {
    this.builder.setCharset(charset);
    return this;
  }

  public ApacheHttpRequestBuilder setConfig(final RequestConfig config) {
    this.builder.setConfig(config);
    return this;
  }

  public ApacheHttpRequestBuilder setEntity(final HttpEntity entity) {
    this.builder.setEntity(entity);
    return this;
  }

  public ApacheHttpRequestBuilder setJsonEntity(final JsonObject value) {
    final String jsonString = value.toJsonString();
    final StringEntity entity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
    this.builder.setEntity(entity);
    return this;
  }

  @Override
  public String toString() {
    return this.builder.toString();
  }

}
