package com.revolsys.net.http;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;

public class ApacheHttpException extends RuntimeException {
  public static ApacheHttpException create(final HttpResponse response) {
    final StatusLine statusLine = response.getStatusLine();
    String content;
    try {
      content = ApacheHttp.getString(response);
    } catch (final Exception e) {
      content = null;
    }
    return new ApacheHttpException(statusLine, content);
  }

  private final int statusCode;

  private final String reasonPhrase;

  private final String content;

  public ApacheHttpException(final StatusLine statusLine, final String content) {
    super(statusLine + "\n" + content);
    this.statusCode = statusLine.getStatusCode();
    this.reasonPhrase = statusLine.getReasonPhrase();
    this.content = content;
  }

  public String getContent() {
    return this.content;
  }

  public String getReasonPhrase() {
    return this.reasonPhrase;
  }

  public int getStatusCode() {
    return this.statusCode;
  }

}
