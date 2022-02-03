package com.revolsys.http;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.revolsys.record.io.format.json.JsonObject;

public class AzureQueueSharedKeyRequestBuilderFactory extends ApacheHttpRequestBuilderFactory {

  public static AzureQueueSharedKeyRequestBuilderFactory forConnectionString(
    final JsonObject connectionParameters) {
    final String accountName = connectionParameters.getString("AccountName");
    final String accountKey = connectionParameters.getString("AccountKey");
    final AzureQueueSharedKeyRequestBuilderFactory requestBuilderFactory = new AzureQueueSharedKeyRequestBuilderFactory(
      accountName, accountKey);
    return requestBuilderFactory;
  }

  private final String accountName;

  private final byte[] accountKeyBytes;

  private final SecretKeySpec secretKey;

  public AzureQueueSharedKeyRequestBuilderFactory(final String accountName,
    final String accountKey) {
    this.accountName = accountName;
    this.accountKeyBytes = Base64.getDecoder().decode(accountKey);
    this.secretKey = new SecretKeySpec(this.accountKeyBytes, "HmacSHA256");

  }

  public String getAccountName() {
    return this.accountName;
  }

  String getSharedKeyAuthorization(final StringBuilder data)
    throws NoSuchAlgorithmException, InvalidKeyException {
    final String signature = sign(data);
    return String.format("SharedKey %s:%s", this.accountName, signature);
  }

  @Override
  public AzureQueueSharedKeyRequestBuilder newRequestBuilder() {
    return new AzureQueueSharedKeyRequestBuilder(this);
  }

  private String sign(final StringBuilder data)
    throws NoSuchAlgorithmException, InvalidKeyException {
    final Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(this.secretKey);
    final byte[] signatureBytes = mac.doFinal(data.toString().getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(signatureBytes);
  }

  @Override
  public String toString() {
    return this.accountName.toString();
  }

}
