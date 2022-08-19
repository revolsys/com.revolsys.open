package com.revolsys.http;

import org.apache.http.client.methods.RequestBuilder;

import com.revolsys.net.http.ApacheHttp;
import com.revolsys.record.io.format.json.JsonObject;

public class AzureManagedIdentityRequestBuilderFactory
  extends BearerTokenRequestBuilderFactory<AzureManagedIdentityBearerToken> {
  public static final String ENDPOINT_URL = System.getenv("IDENTITY_ENDPOINT");

  public static final String IDENTITY_HEADER = System.getenv("IDENTITY_HEADER");

  private static boolean AVAILABLE = ENDPOINT_URL != null && IDENTITY_HEADER != null;

  public static boolean isAvailable() {
    return AVAILABLE;
  }

  private final String resource;

  public AzureManagedIdentityRequestBuilderFactory(final String resource) {
    this.resource = resource;
  }

  @Override
  protected AzureManagedIdentityBearerToken tokenRefresh(
    final AzureManagedIdentityBearerToken token) {
    if (isAvailable()) {
      final RequestBuilder requestBuilder = RequestBuilder//
        .get(ENDPOINT_URL)
        .addHeader("X-IDENTITY-HEADER", IDENTITY_HEADER)
        .addParameter("resource", this.resource)
        .addParameter("api-version", "2019-08-01");
      final JsonObject response = ApacheHttp.getJson(requestBuilder);
      return new AzureManagedIdentityBearerToken(response, this.resource);
    } else {
      return null;
    }
  }
}
