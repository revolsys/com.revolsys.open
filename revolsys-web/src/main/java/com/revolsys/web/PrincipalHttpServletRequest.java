package com.revolsys.web;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

public class PrincipalHttpServletRequest extends HttpServletRequestWrapper {

  public static void doFilter(final FilterChain chain, final Principal principal,
    final HttpServletRequest request, final HttpServletResponse response)
    throws IOException, ServletException {
    final PrincipalHttpServletRequest pricipalRequest = new PrincipalHttpServletRequest(request,
      principal);
    chain.doFilter(pricipalRequest, response);
  }

  private Principal principal;

  public PrincipalHttpServletRequest(final HttpServletRequest request, final Principal principal) {
    super(request);
  }

  @Override
  public String getRemoteUser() {
    return this.principal.getName();
  }

  @Override
  public Principal getUserPrincipal() {
    return this.principal;
  }
}
