package com.revolsys.web;

import java.io.IOException;
import java.security.Principal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

public class PrincipalHttpServletRequest extends HttpServletRequestWrapper {

  public static void doFilter(final FilterChain chain, final Principal principal,
    final HttpServletRequest request, final HttpServletResponse response)
    throws IOException, ServletException {
    final PrincipalHttpServletRequest pricipalRequest = new PrincipalHttpServletRequest(request,
      principal);
    chain.doFilter(pricipalRequest, response);
  }

  private final Principal principal;

  public PrincipalHttpServletRequest(final HttpServletRequest request, final Principal principal) {
    super(request);
    this.principal = principal;
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
