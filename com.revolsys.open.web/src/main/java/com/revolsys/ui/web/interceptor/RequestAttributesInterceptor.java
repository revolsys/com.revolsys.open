package com.revolsys.ui.web.interceptor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UrlPathHelper;

public class RequestAttributesInterceptor implements HandlerInterceptor {
  private Map<String, Map<String, Object>> attributeMappings = new LinkedHashMap<>();

  private PathMatcher pathMatcher = new AntPathMatcher();

  /** The UrlPathHelper to use for resolution of lookup paths. */
  private UrlPathHelper urlPathHelper = new UrlPathHelper();

  public RequestAttributesInterceptor() {
    this.urlPathHelper.setAlwaysUseFullPath(true);
  }

  @Override
  public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response,
    final Object handler, final Exception ex) throws Exception {
  }

  public Map<String, Map<String, Object>> getAttributeMappings() {
    return this.attributeMappings;
  }

  @Override
  public void postHandle(final HttpServletRequest request, final HttpServletResponse response,
    final Object handler, final ModelAndView modelAndView) throws Exception {

  }

  @Override
  public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
    final Object handler) throws ServletException {
    final String path = this.urlPathHelper.getLookupPathForRequest(request);
    for (final Entry<String, Map<String, Object>> mapping : this.attributeMappings.entrySet()) {
      final String pattern = mapping.getKey();
      if (this.pathMatcher.match(pattern, path)) {
        final Map<String, Object> attributes = mapping.getValue();
        for (final Entry<String, Object> attribute : attributes.entrySet()) {
          final String name = attribute.getKey();
          final Object value = attribute.getValue();
          if (request.getAttribute(name) == null) {
            request.setAttribute(name, value);
          }
        }
      }
    }
    return true;
  }

  /**
   * Set if URL lookup should always use full path within current servlet
   * context. Else, the path within the current servlet mapping is used if
   * applicable (i.e. in the case of a ".../*" servlet mapping in web.xml).
   * Default is "false".
   * <p>
   * Only relevant for the "cacheMappings" setting.
   *
   * @see #setCacheMappings
   * @see org.springframework.web.util.UrlPathHelper#setAlwaysUseFullPath
   */
  public void setAlwaysUseFullPath(final boolean alwaysUseFullPath) {
    this.urlPathHelper.setAlwaysUseFullPath(alwaysUseFullPath);
  }

  public void setAttributeMappings(final Map<String, Map<String, Object>> attributeMappings) {
    this.attributeMappings = attributeMappings;
  }

  /**
   * Set the PathMatcher implementation to use for matching URL paths against
   * registered URL patterns, for determining cache mappings. Default is
   * AntPathMatcher.
   */
  public void setPathMatcher(final PathMatcher pathMatcher) {
    Assert.notNull(pathMatcher, "PathMatcher must not be null");
    this.pathMatcher = pathMatcher;
  }

  /**
   * Set if context path and request URI should be URL-decoded. Both are
   * returned <i>undecoded</i> by the Servlet API, in contrast to the servlet
   * path.
   * <p>
   * Uses either the request encoding or the default encoding according to the
   * Servlet spec (ISO-8859-1).
   * <p>
   * Only relevant for the "cacheMappings" setting.
   *
   * @see #setCacheMappings
   * @see org.springframework.web.util.UrlPathHelper#setUrlDecode
   */
  public void setUrlDecode(final boolean urlDecode) {
    this.urlPathHelper.setUrlDecode(urlDecode);
  }

  /**
   * Set the UrlPathHelper to use for resolution of lookup paths.
   *
   * @param urlPathHelper The UrlPathHelper to use for resolution of lookup
   *          paths.
   */
  public void setUrlPathHelper(final UrlPathHelper urlPathHelper) {
    Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
    this.urlPathHelper = urlPathHelper;
  }
}
