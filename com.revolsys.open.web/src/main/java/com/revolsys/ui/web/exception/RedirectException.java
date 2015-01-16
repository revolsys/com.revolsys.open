package com.revolsys.ui.web.exception;

/**
 * The RedirectException can be used to redirect a user to a specified url. This
 * is used instead of sendRedirect on the ServletResponse.
 *
 * @author P.D.Austin
 * @version 1.0
 */
public class RedirectException extends RuntimeException {
  /** The unique serial version UID for the class. */
  private static final long serialVersionUID = -8661796861568286704L;

  private String url;

  /**
   * Construct a new RedirectException.
   */
  public RedirectException() {
  }

  /**
   * Construct a new PageNotFoundException with the specified message.
   *
   * @param message The reason the exception was thrown
   */
  public RedirectException(final String url) {
    this.url = url;
  }

  public String getUrl() {
    return this.url;
  }
}
