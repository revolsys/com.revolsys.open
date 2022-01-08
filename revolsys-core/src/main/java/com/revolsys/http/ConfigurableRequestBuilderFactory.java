package com.revolsys.http;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.message.BasicHeader;

import com.revolsys.net.http.SimpleNameValuePair;

public class ConfigurableRequestBuilderFactory extends ApacheHttpRequestBuilderFactory {

  private final List<Header> headers = new ArrayList<>();

  private final List<NameValuePair> parameters = new ArrayList<>();

  public ConfigurableRequestBuilderFactory() {
  }

  public ConfigurableRequestBuilderFactory addHeader(final Header header) {
    this.headers.add(header);
    return this;
  }

  public ConfigurableRequestBuilderFactory addHeader(final String name, final Object value) {
    if (value != null) {
      final BasicHeader header = new BasicHeader(name, value.toString());
      addHeader(header);
    }
    return this;
  }

  public ConfigurableRequestBuilderFactory addParameter(final NameValuePair parameter) {
    this.parameters.add(parameter);
    return this;
  }

  public ConfigurableRequestBuilderFactory addParameter(final String name, final Object value) {
    if (value != null) {
      final SimpleNameValuePair parameter = new SimpleNameValuePair(name, value.toString());
      addParameter(parameter);
    }
    return this;
  }

  @Override
  protected ApacheHttpRequestBuilder newRequestBuilder(final RequestBuilder requestBuilder) {
    return new ConfigurableRequestBuilder(this, requestBuilder);
  }

  public void preBuild(final RequestBuilder builder) {
    for (final Header header : this.headers) {
      builder.addHeader(header);
    }
    for (final NameValuePair parameter : this.parameters) {
      builder.addParameter(parameter);
    }
  }
}
