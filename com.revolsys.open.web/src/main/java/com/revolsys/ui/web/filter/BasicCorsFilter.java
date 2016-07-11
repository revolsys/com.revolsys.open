package com.revolsys.ui.web.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public class BasicCorsFilter extends OncePerRequestFilter {

  private Set<String> allowedHeaders;

  private String allowedHeadersString;

  private Set<String> allowedMethods;

  private String allowedMethodsString;

  private int maxAge;

  private String maxAgeString;

  public BasicCorsFilter() {
    setAllowedMethods(Arrays.asList("DELETE", "GET", "POST", "PUT"));
    setAllowedHeaders(Arrays.asList("Accept-Encoding", "Accept", "Accept-Language"));
    setMaxAge(3600);
  }

  @Override
  protected void doFilterInternal(final HttpServletRequest request,
    final HttpServletResponse response, final FilterChain filterChain)
    throws ServletException, IOException {
    final String method = request.getMethod();
    final String origin = request.getHeader("Origin");
    final String requestMethod = request.getHeader("Access-Control-Request-Method");
    response.addHeader("Access-Control-Allow-Origin", "*");
    if ("OPTIONS".equals(method) && Property.hasValue(origin) && Property.hasValue(requestMethod)) {
      response.addHeader("Access-Control-Allow-Methods", this.allowedMethodsString);
      final String requestHeaders = request.getHeader("Access-Control-Request-Headers");
      if (Property.hasValue(requestHeaders)) {
        response.addHeader("Access-Control-Allow-Headers", this.allowedHeadersString);
      }
      response.addHeader("Access-Control-Max-Age", this.maxAgeString);
    }
    filterChain.doFilter(request, response);
  }

  public Collection<String> getAllowedHeaders() {
    return this.allowedHeaders;
  }

  public Set<String> getAllowedMethods() {
    return this.allowedMethods;
  }

  public int getMaxAge() {
    return this.maxAge;
  }

  public void setAllowedHeaders(final Collection<String> allowedHeaders) {
    if (allowedHeaders == null) {
      this.allowedHeaders = Collections.emptySet();
    } else {
      this.allowedHeaders = new TreeSet<>(allowedHeaders);
    }
    this.allowedHeadersString = Strings.toString(" ,", allowedHeaders);
  }

  public void setAllowedMethods(final Collection<String> allowedMethods) {
    if (allowedMethods == null) {
      this.allowedMethods = Collections.emptySet();
    } else {
      this.allowedMethods = new TreeSet<>(allowedMethods);
    }
    this.allowedMethodsString = Strings.toString(" ,", allowedMethods);
  }

  public void setMaxAge(final int maxAge) {
    this.maxAge = maxAge;
    this.maxAgeString = String.valueOf(maxAge);
  }
}
