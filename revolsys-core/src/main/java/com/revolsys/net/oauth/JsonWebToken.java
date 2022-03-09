package com.revolsys.net.oauth;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Base64.Decoder;

import com.revolsys.record.io.format.json.JsonList;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonParser;

public class JsonWebToken {

  public static JsonObject decodeJson(final String base64) {
    final byte[] decoded = Base64.getDecoder().decode(base64);
    final String string = new String(decoded, StandardCharsets.UTF_8);
    return JsonParser.read(string);
  }

  private final JsonObject header;

  private final JsonObject payload;

  private final byte[] signatureBytes;

  private final String token;

  private final byte[] payloadBytes;

  private final byte[] headerBytes;

  private final String headerText;

  private final String payloadText;

  public JsonWebToken(final String token) {
    this.token = token;
    final int firstDot = token.indexOf('.');
    final int secondDot = token.indexOf('.', firstDot + 1);
    final String headerBase64 = token.substring(0, firstDot);
    final Decoder decoder = Base64.getUrlDecoder();
    if ("0".equals(headerBase64)) {
      this.headerBytes = new byte[] {
        0
      };
      this.headerText = "0";
      this.header = JsonObject.EMPTY;
    } else {
      this.headerBytes = decoder.decode(headerBase64);
      this.headerText = new String(this.headerBytes, StandardCharsets.UTF_8);
      this.header = JsonParser.read(this.headerText);
    }
    final String payloadBase64 = token.substring(firstDot + 1, secondDot);
    this.payloadBytes = decoder.decode(payloadBase64);
    this.payloadText = new String(this.payloadBytes, StandardCharsets.UTF_8);
    this.payload = JsonParser.read(this.payloadText);

    final String signatureText = token.substring(secondDot + 1);
    this.signatureBytes = decoder.decode(signatureText);
  }

  public boolean equalsSubject(final String subject) {
    return this.payload.equalValue("subject", subject);
  }

  public String getAudience() {
    return getString("aud");
  }

  public Instant getExpiry() {
    return getTime("exp");
  }

  public JsonObject getHeader() {
    return this.header;
  }

  public String getId() {
    return getString("jti");
  }

  public Instant getIssuedAt() {
    return getTime("iat");
  }

  private String getIssuer() {
    return getString("iss");
  }

  public Instant getNotBefore() {
    return getTime("nbf");
  }

  public JsonObject getPayload() {
    return this.payload;
  }

  public byte[] getSignature() {
    return this.signatureBytes;
  }

  public String getString(final String name) {
    return this.payload.getString(name);
  }

  public String getSubject() {
    return getString("sub");
  }

  public Instant getTime(final String name) {
    final Integer seconds = this.payload.getInteger(name);
    if (seconds == null) {
      return null;
    } else {
      return Instant.ofEpochSecond(seconds);
    }
  }

  public String getToken() {
    return this.token;
  }

  public boolean isValid(final Iterable<String> issuers) {
    for (final String issuer : issuers) {
      if (isValid(issuer)) {
        return true;
      }
    }
    return false;
  }

  public boolean isValid(final String issuer) {
    try {
      final String iss = getIssuer();
      if (!issuer.equals(iss)) {
        return false;
      }
      if (!this.header.equalValue("typ", "JWT") && this.header.hasValue("typ")) {
        return false;
      }

      if (!this.header.equalValue("alg", "RS256")) {
        return false;
      }
      if (!this.header.hasValue("kid")) {
        return false;
      }
      final Instant now = Instant.now();
      final Instant notBefore = getNotBefore();
      if (notBefore != null) {
        if (now.isBefore(notBefore)) {
          return false;
        }
      }

      final Instant expiry = getExpiry();
      if (expiry != null) {
        if (now.isAfter(expiry)) {
          return false;
        }
      }

      final URI openidConfigUri = URI.create(issuer + "/.well-known/openid-configuration");
      final JsonObject openIdConfig = JsonWebTokenCache.getJson(openidConfigUri);
      if (openIdConfig == null) {
        return false;
      }

      final URI jsonWebKeySetUri = URI.create(openIdConfig.getString("jwks_uri"));
      final JsonObject jsonWebKeySet = JsonWebTokenCache.getJson(jsonWebKeySetUri);
      if (jsonWebKeySet == null) {
        return false;
      }

      final String tokenToSign = this.token.substring(0, this.token.lastIndexOf('.'));

      final String keyId = this.header.getString("kid");
      for (final JsonObject key : jsonWebKeySet.getJsonList("keys", JsonList.EMPTY).jsonObjects()) {
        if (key.equalValue("kid", keyId)) {
          final String n = key.getString("n");
          final String e = key.getString("e");
          if (n != null && e != null) {
            final byte exponentB[] = Base64.getUrlDecoder().decode(e);
            final byte modulusB[] = Base64.getUrlDecoder().decode(n);
            final BigInteger bigExponent = new BigInteger(1, exponentB);
            final BigInteger bigModulus = new BigInteger(1, modulusB);
            final PublicKey publicKey = KeyFactory.getInstance("RSA")
              .generatePublic(new RSAPublicKeySpec(bigModulus, bigExponent));
            if (verifySignature(tokenToSign, publicKey)) {
              return true;
            }
          }
          for (final String certBase64 : key.getValue("x5c", JsonList.EMPTY).<String> iterable()) {

            final byte[] cert = Base64.getDecoder().decode(certBase64);

            final CertificateFactory factory = CertificateFactory.getInstance("X.509");
            final X509Certificate x509 = (X509Certificate)factory
              .generateCertificate(new ByteArrayInputStream(cert));
            final RSAPublicKey publicKey = (RSAPublicKey)x509.getPublicKey();
            if (verifySignature(tokenToSign, publicKey)) {
              return true;
            }
          }
          return false;
        }
      }
    } catch (final Exception e) {
      return false;
    }

    return false;
  }

  @Override
  public String toString() {
    return this.header + "\n" + this.payload;
  }

  private boolean verifySignature(final String tokenToSign, final PublicKey publicKey)
    throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    final Signature s = Signature.getInstance("SHA256withRSA");
    s.initVerify(publicKey);
    s.update(tokenToSign.getBytes(StandardCharsets.UTF_8));
    final boolean verify = s.verify(this.signatureBytes);
    return verify;
  }

}
