package com.revolsys.http;

import org.apache.http.client.methods.HttpUriRequest;

public class AzureTableSasRequestBuilder extends ApacheHttpRequestBuilder {

  public AzureTableSasRequestBuilder(final AzureTableSasRequestBuilderFactory factory) {
    super(factory);
  }

  @Override
  public HttpUriRequest build() {
    getFactory().applyToken(this);
    return super.build();
  }

  @Override
  public AzureTableSasRequestBuilderFactory getFactory() {
    return (AzureTableSasRequestBuilderFactory)super.getFactory();
  }

}
