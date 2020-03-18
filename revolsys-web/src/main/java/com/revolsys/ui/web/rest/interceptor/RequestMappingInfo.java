package com.revolsys.ui.web.rest.interceptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Holder for request mapping metadata. Allows for finding a best matching
 * candidate.
 */
public class RequestMappingInfo {

  List<String> matchedPaths = Collections.emptyList();

  RequestMethod[] methods = new RequestMethod[0];

  String[] paths = new String[0];

  public String bestMatchedPath() {
    return !this.matchedPaths.isEmpty() ? this.matchedPaths.get(0) : null;
  }

  @Override
  public boolean equals(final Object obj) {
    final RequestMappingInfo other = (RequestMappingInfo)obj;
    return Arrays.equals(this.paths, other.paths) && Arrays.equals(this.methods, other.methods);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(this.paths) * 23 + Arrays.hashCode(this.methods) * 29;
  }

  public boolean matches(final HttpServletRequest request) {
    return ServletAnnotationMappingUtils.checkRequestMethod(this.methods, request);
  }
}
