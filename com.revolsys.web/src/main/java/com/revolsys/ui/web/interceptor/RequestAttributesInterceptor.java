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
    private Map<String, Map<String, Object>> attributeMappings = new LinkedHashMap<String, Map<String, Object>>();

    /** The UrlPathHelper to use for resolution of lookup paths. */
    private UrlPathHelper urlPathHelper = new UrlPathHelper();

    private PathMatcher pathMatcher = new AntPathMatcher();

    public boolean preHandle(HttpServletRequest request,
        HttpServletResponse response, Object handler) throws ServletException {
        // TODO implement caching of attributes
        String path = urlPathHelper.getLookupPathForRequest(request);
        for (Entry<String, Map<String, Object>> mapping : attributeMappings
            .entrySet()) {
            String pattern = mapping.getKey();
            if (pathMatcher.match(pattern, path)) {
                Map<String, Object> attributes = mapping.getValue();
                for (Entry<String, Object> attribute : attributes.entrySet()) {
                    String name = attribute.getKey();
                    Object value = attribute.getValue();
                    request.setAttribute(name, value);
                }
            }
        }
        return true;
    }

    public Map<String, Map<String, Object>> getAttributeMappings() {
        return attributeMappings;
    }

    public void setAttributeMappings(
        final Map<String, Map<String, Object>> attributeMappings) {
        this.attributeMappings = attributeMappings;
    }

    public void afterCompletion(HttpServletRequest request,
        HttpServletResponse response, Object handler, Exception ex)
        throws Exception {
    }

    public void postHandle(HttpServletRequest request,
        HttpServletResponse response, Object handler, ModelAndView modelAndView)
        throws Exception {

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
    public void setAlwaysUseFullPath(boolean alwaysUseFullPath) {
        this.urlPathHelper.setAlwaysUseFullPath(alwaysUseFullPath);
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
    public void setUrlDecode(boolean urlDecode) {
        this.urlPathHelper.setUrlDecode(urlDecode);
    }

    /**
     * Set the UrlPathHelper to use for resolution of lookup paths.
     * 
     * @param urlPathHelper The UrlPathHelper to use for resolution of lookup
     *        paths.
     */
    public void setUrlPathHelper(UrlPathHelper urlPathHelper) {
        Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
        this.urlPathHelper = urlPathHelper;
    }

    /**
     * Set the PathMatcher implementation to use for matching URL paths against
     * registered URL patterns, for determining cache mappings. Default is
     * AntPathMatcher.
     */
    public void setPathMatcher(PathMatcher pathMatcher) {
        Assert.notNull(pathMatcher, "PathMatcher must not be null");
        this.pathMatcher = pathMatcher;
    }
}
