package com.revolsys.net;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jeometry.common.logging.Logs;

public class TrustAllX509TrustManager implements X509TrustManager {
  public static final TrustAllX509TrustManager INSTANCE = new TrustAllX509TrustManager();

  public static SSLContext install() {
    try {
      final SSLContext sc = SSLContext.getInstance("SSL");
      final TrustManager[] trustManagers = new TrustManager[] {
        INSTANCE
      };
      sc.init(null, trustManagers, new java.security.SecureRandom());
      final SSLSocketFactory socketFactory = sc.getSocketFactory();
      HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory);
      return sc;
    } catch (final GeneralSecurityException e) {
      Logs.error(TrustAllX509TrustManager.class, "Error installing trust manager", e);
      return null;
    }
  }

  @Override
  public void checkClientTrusted(final java.security.cert.X509Certificate[] certs,
    final String authType) {
  }

  @Override
  public void checkServerTrusted(final java.security.cert.X509Certificate[] certs,
    final String authType) {
  }

  @Override
  public java.security.cert.X509Certificate[] getAcceptedIssuers() {
    return new X509Certificate[0];
  }
}
