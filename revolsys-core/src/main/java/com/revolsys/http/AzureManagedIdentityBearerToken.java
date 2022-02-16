package com.revolsys.http;

import org.jeometry.common.logging.Logs;

import com.revolsys.net.oauth.BearerToken;
import com.revolsys.record.io.format.json.JsonObject;

public class AzureManagedIdentityBearerToken extends BearerToken {

  public AzureManagedIdentityBearerToken(final JsonObject config, final String resource) {
    super(config, resource);
    final long expiresOn = config.getLong("expires_on");
    final long expireTime = expiresOn * 1000;
    setExpireTime(expireTime);
    final String returnedResource = config.getString("resource");
    setScope(resource, returnedResource);
    Logs.error(this, resource);
  }

}
