package com.revolsys.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.Configurable;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.HeaderGroup;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;

import com.revolsys.net.http.ApacheHttp;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.util.UriBuilder;

public class ApacheHttpRequestBuilder {

  public static final StringEntity EMPTY_ENTITY = new StringEntity("", ContentType.TEXT_PLAIN);

  static class InternalEntityEclosingRequest extends HttpEntityEnclosingRequestBase {

    private final String method;

    InternalEntityEclosingRequest(final String method) {
      super();
      this.method = method;
    }

    @Override
    public String getMethod() {
      return this.method;
    }

  }

  static class InternalRequest extends HttpRequestBase {

    private final String method;

    InternalRequest(final String method) {
      super();
      this.method = method;
    }

    @Override
    public String getMethod() {
      return this.method;
    }

  }

  public static ApacheHttpRequestBuilder copy(final HttpRequest request) {
    Args.notNull(request, "HTTP request");
    return new ApacheHttpRequestBuilder().setRequest(request);
  }

  public static ApacheHttpRequestBuilder create(final String method) {
    Args.notBlank(method, "HTTP method");
    return new ApacheHttpRequestBuilder().setMethod(method);
  }

  public static ApacheHttpRequestBuilder delete() {
    return new ApacheHttpRequestBuilder().setMethod(HttpDelete.METHOD_NAME);
  }

  public static ApacheHttpRequestBuilder delete(final String uri) {
    return new ApacheHttpRequestBuilder().setMethod(HttpDelete.METHOD_NAME).setUri(uri);
  }

  public static ApacheHttpRequestBuilder delete(final URI uri) {
    return new ApacheHttpRequestBuilder().setMethod(HttpDelete.METHOD_NAME).setUri(uri);
  }

  public static ApacheHttpRequestBuilder get() {
    return new ApacheHttpRequestBuilder().setMethod(HttpGet.METHOD_NAME);
  }

  public static ApacheHttpRequestBuilder get(final String uri) {
    return new ApacheHttpRequestBuilder().setMethod(HttpGet.METHOD_NAME).setUri(uri);
  }

  public static ApacheHttpRequestBuilder get(final URI uri) {
    return new ApacheHttpRequestBuilder().setMethod(HttpGet.METHOD_NAME).setUri(uri);
  }

  public static ApacheHttpRequestBuilder head() {
    return new ApacheHttpRequestBuilder().setMethod(HttpHead.METHOD_NAME);
  }

  public static ApacheHttpRequestBuilder head(final String uri) {
    return new ApacheHttpRequestBuilder().setMethod(HttpHead.METHOD_NAME).setUri(uri);
  }

  public static ApacheHttpRequestBuilder head(final URI uri) {
    return new ApacheHttpRequestBuilder().setMethod(HttpHead.METHOD_NAME).setUri(uri);
  }

  public static ApacheHttpRequestBuilder options() {
    return new ApacheHttpRequestBuilder().setMethod(HttpOptions.METHOD_NAME);
  }

  public static ApacheHttpRequestBuilder options(final String uri) {
    return new ApacheHttpRequestBuilder().setMethod(HttpOptions.METHOD_NAME).setUri(uri);
  }

  public static ApacheHttpRequestBuilder options(final URI uri) {
    return new ApacheHttpRequestBuilder().setMethod(HttpOptions.METHOD_NAME).setUri(uri);
  }

  public static ApacheHttpRequestBuilder patch() {
    return new ApacheHttpRequestBuilder().setMethod(HttpPatch.METHOD_NAME);
  }

  public static ApacheHttpRequestBuilder patch(final String uri) {
    return new ApacheHttpRequestBuilder().setMethod(HttpPatch.METHOD_NAME).setUri(uri);
  }

  public static ApacheHttpRequestBuilder patch(final URI uri) {
    return new ApacheHttpRequestBuilder().setMethod(HttpPatch.METHOD_NAME).setUri(uri);
  }

  public static ApacheHttpRequestBuilder post() {
    return new ApacheHttpRequestBuilder().setMethod(HttpPost.METHOD_NAME);
  }

  public static ApacheHttpRequestBuilder post(final String uri) {
    return new ApacheHttpRequestBuilder().setMethod(HttpPost.METHOD_NAME).setUri(uri);
  }

  public static ApacheHttpRequestBuilder post(final URI uri) {
    return new ApacheHttpRequestBuilder().setMethod(HttpPost.METHOD_NAME).setUri(uri);
  }

  public static ApacheHttpRequestBuilder put() {
    return new ApacheHttpRequestBuilder().setMethod(HttpPut.METHOD_NAME);
  }

  public static ApacheHttpRequestBuilder put(final String uri) {
    return new ApacheHttpRequestBuilder().setMethod(HttpPut.METHOD_NAME).setUri(uri);
  }

  public static ApacheHttpRequestBuilder put(final URI uri) {
    return new ApacheHttpRequestBuilder().setMethod(HttpPut.METHOD_NAME).setUri(uri);
  }

  public static ApacheHttpRequestBuilder trace() {
    return new ApacheHttpRequestBuilder().setMethod(HttpTrace.METHOD_NAME);
  }

  public static ApacheHttpRequestBuilder trace(final String uri) {
    return new ApacheHttpRequestBuilder().setMethod(HttpTrace.METHOD_NAME).setUri(uri);
  }

  public static ApacheHttpRequestBuilder trace(final URI uri) {
    return new ApacheHttpRequestBuilder().setMethod(HttpTrace.METHOD_NAME).setUri(uri);
  }

  private String method;

  private Charset charset = Consts.UTF_8;

  private ProtocolVersion version;

  private URI uri;

  private HeaderGroup headerGroup;

  private HttpEntity entity;

  private List<NameValuePair> parameters;

  private RequestConfig config;

  private final Set<String> headerNames = new TreeSet<>();

  private ApacheHttpRequestBuilderFactory factory;

  ApacheHttpRequestBuilder() {
  }

  public ApacheHttpRequestBuilder(final ApacheHttpRequestBuilderFactory factory) {
    this.factory = factory;
  }

  public ApacheHttpRequestBuilder addHeader(final Header header) {
    this.headerNames.add(header.getName());
    if (this.headerGroup == null) {
      this.headerGroup = new HeaderGroup();
    }
    this.headerGroup.addHeader(header);
    return this;
  }

  public ApacheHttpRequestBuilder addHeader(final String name, final String value) {
    final BasicHeader header = new BasicHeader(name, value);
    return addHeader(header);
  }

  public ApacheHttpRequestBuilder addParameter(final NameValuePair parameter) {
    if (parameter != null) {
      if (this.parameters == null) {
        this.parameters = new LinkedList<NameValuePair>();
      }
      this.parameters.add(parameter);
    }
    return this;
  }

  public ApacheHttpRequestBuilder addParameter(final String name, final Object value) {
    String string;
    if (value == null) {
      string = null;
    } else {
      string = value.toString();
    }
    final BasicNameValuePair parameter = new BasicNameValuePair(name, string);
    return addParameter(parameter);
  }

  public ApacheHttpRequestBuilder addParameters(final Iterable<NameValuePair> parameters) {
    for (final NameValuePair parameter : parameters) {
      addParameter(parameter);
    }
    return this;
  }

  public ApacheHttpRequestBuilder addParameters(final NameValuePair... nvps) {
    for (final NameValuePair nvp : nvps) {
      addParameter(nvp);
    }
    return this;
  }

  public ApacheHttpRequestBuilder apply(final Consumer<ApacheHttpRequestBuilder> action) {
    action.accept(this);
    return this;
  }

  public HttpUriRequest build() {
    final HttpRequestBase result;
    URI uri = this.uri;
    if (uri == null) {
      uri = URI.create("/");
    }
    HttpEntity entityCopy = this.entity;
    if (this.parameters != null && !this.parameters.isEmpty()) {
      if (entityCopy == null && (HttpPost.METHOD_NAME.equalsIgnoreCase(this.method)
        || HttpPut.METHOD_NAME.equalsIgnoreCase(this.method))) {
        entityCopy = new UrlEncodedFormEntity(this.parameters,
          this.charset != null ? this.charset : HTTP.DEF_CONTENT_CHARSET);
      } else {
        uri = new UriBuilder(uri).setCharset(this.charset).addParameters(this.parameters).build();
      }
    }
    if (entityCopy == null) {
      result = new InternalRequest(this.method);
    } else {
      final InternalEntityEclosingRequest request = new InternalEntityEclosingRequest(this.method);
      request.setEntity(entityCopy);
      result = request;
    }
    result.setProtocolVersion(this.version);
    result.setURI(uri);
    if (this.headerGroup != null) {
      result.setHeaders(this.headerGroup.getAllHeaders());
    }
    result.setConfig(this.config);
    return result;
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

  /**
   * @since 4.4
   */
  public Charset getCharset() {
    return this.charset;
  }

  public RequestConfig getConfig() {
    return this.config;
  }

  public HttpEntity getEntity() {
    return this.entity;
  }

  public ApacheHttpRequestBuilderFactory getFactory() {
    return this.factory;
  }

  public Header getFirstHeader(final String name) {
    return this.headerGroup != null ? this.headerGroup.getFirstHeader(name) : null;
  }

  public Set<String> getHeaderNames() {
    return Collections.unmodifiableSet(this.headerNames);
  }

  public Header[] getHeaders(final String name) {
    return this.headerGroup != null ? this.headerGroup.getHeaders(name) : null;
  }

  public JsonObject getJson() {
    final Function<HttpResponse, JsonObject> function = ApacheHttp::getJson;
    return execute(function);
  }

  public Header getLastHeader(final String name) {
    return this.headerGroup != null ? this.headerGroup.getLastHeader(name) : null;
  }

  public String getMethod() {
    return this.method;
  }

  public List<NameValuePair> getParameters() {
    return this.parameters != null ? new ArrayList<NameValuePair>(this.parameters)
      : new ArrayList<NameValuePair>();
  }

  public String getString() {
    final Function<HttpResponse, String> function = ApacheHttp::getString;
    return execute(function);
  }

  public URI getUri() {
    return this.uri;
  }

  public ProtocolVersion getVersion() {
    return this.version;
  }

  public InputStream newInputStream() {
    final HttpUriRequest request = build();
    return ApacheHttp.getInputStream(request);
  }

  public ApacheHttpRequestBuilder removeHeader(final Header header) {
    if (this.headerGroup != null) {
      this.headerGroup.removeHeader(header);
    }
    return this;
  }

  public ApacheHttpRequestBuilder removeHeaders(final String name) {
    if (name != null && this.headerGroup != null) {
      this.headerNames.remove(name);
      for (final HeaderIterator i = this.headerGroup.iterator(); i.hasNext();) {
        final Header header = i.nextHeader();
        if (name.equalsIgnoreCase(header.getName())) {
          i.remove();
        }
      }
    }
    return this;
  }

  public ApacheHttpRequestBuilder removeParameters(final String name) {
    if (name != null && this.parameters != null) {
      for (final Iterator<NameValuePair> i = this.parameters.iterator(); i.hasNext();) {
        final NameValuePair parameter = i.next();
        if (name.equalsIgnoreCase(parameter.getName())) {
          i.remove();
        }
      }
    }
    return this;
  }

  public ApacheHttpRequestBuilder setCharset(final Charset charset) {
    this.charset = charset;
    return this;
  }

  public ApacheHttpRequestBuilder setConfig(final RequestConfig config) {
    this.config = config;
    return this;
  }

  public ApacheHttpRequestBuilder setEmptyEntity() {
    setEntity(EMPTY_ENTITY);
    return this;
  }

  public ApacheHttpRequestBuilder setEntity(final HttpEntity entity) {
    this.entity = entity;
    return this;
  }

  public ApacheHttpRequestBuilder setHeader(final Header header) {
    if (this.headerGroup == null) {
      this.headerGroup = new HeaderGroup();
    }
    this.headerGroup.updateHeader(header);
    return this;
  }

  public ApacheHttpRequestBuilder setHeader(final String name, final String value) {
    final BasicHeader header = new BasicHeader(name, value);
    return setHeader(header);
  }

  public ApacheHttpRequestBuilder setJsonEntity(final JsonObject value) {
    final String jsonString = value.toJsonString();
    final StringEntity entity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
    setEntity(entity);
    return this;
  }

  ApacheHttpRequestBuilder setMethod(final HttpMethod method) {
    this.method = method.name();
    return this;
  }

  ApacheHttpRequestBuilder setMethod(final String method) {
    this.method = method;
    return this;
  }

  public ApacheHttpRequestBuilder setParameter(final NameValuePair parameter) {
    removeParameters(parameter.getName());
    return addParameter(parameter);
  }

  public ApacheHttpRequestBuilder setParameter(final String name, final Object value) {
    removeParameters(name);
    return addParameter(name, value);
  }

  ApacheHttpRequestBuilder setRequest(final HttpRequest request) {
    this.method = request.getRequestLine().getMethod();
    this.version = request.getRequestLine().getProtocolVersion();

    if (this.headerGroup == null) {
      this.headerGroup = new HeaderGroup();
    }
    this.headerGroup.clear();
    this.headerGroup.setHeaders(request.getAllHeaders());

    this.parameters = null;
    this.entity = null;

    if (request instanceof HttpEntityEnclosingRequest) {
      final HttpEntity originalEntity = ((HttpEntityEnclosingRequest)request).getEntity();
      final ContentType contentType = ContentType.get(originalEntity);
      if (contentType != null && contentType.getMimeType()
        .equals(ContentType.APPLICATION_FORM_URLENCODED.getMimeType())) {
        try {
          final List<NameValuePair> formParams = URLEncodedUtils.parse(originalEntity);
          if (!formParams.isEmpty()) {
            this.parameters = formParams;
          }
        } catch (final IOException ignore) {
        }
      } else {
        this.entity = originalEntity;
      }
    }

    if (request instanceof HttpUriRequest) {
      this.uri = ((HttpUriRequest)request).getURI();
    } else {
      this.uri = URI.create(request.getRequestLine().getUri());
    }

    if (request instanceof Configurable) {
      this.config = ((Configurable)request).getConfig();
    } else {
      this.config = null;
    }
    return this;
  }

  ApacheHttpRequestBuilder setUri(final String uri) {
    if (uri == null) {
      this.uri = null;
    } else {
      this.uri = URI.create(uri);
    }
    return this;
  }

  ApacheHttpRequestBuilder setUri(final URI uri) {
    this.uri = uri;
    return this;
  }

  public ApacheHttpRequestBuilder setVersion(final ProtocolVersion version) {
    this.version = version;
    return this;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append(this.method);
    builder.append(' ');
    builder.append(this.uri);
    builder.append(' ');
    builder.append(this.parameters);

    builder.append(", charset=");
    builder.append(this.charset);
    builder.append(", version=");
    builder.append(this.version);
    builder.append(", headerGroup=");
    builder.append(this.headerGroup);
    builder.append(", entity=");
    builder.append(this.entity);
    builder.append(", config=");
    builder.append(this.config);
    return builder.toString();
  }

}
