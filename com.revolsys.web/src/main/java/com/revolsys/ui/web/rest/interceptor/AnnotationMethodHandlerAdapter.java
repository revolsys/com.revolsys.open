/*
 * Copyright 2002-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.revolsys.ui.web.rest.interceptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.validation.support.BindingAwareModelMap;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpSessionRequiredException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.annotation.support.HandlerMethodInvoker;
import org.springframework.web.bind.annotation.support.HandlerMethodResolver;
import org.springframework.web.bind.support.DefaultSessionAttributeStore;
import org.springframework.web.bind.support.SessionAttributeStore;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestScope;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.annotation.ModelAndViewResolver;
import org.springframework.web.servlet.mvc.multiaction.InternalPathMethodNameResolver;
import org.springframework.web.servlet.mvc.multiaction.MethodNameResolver;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.support.WebContentGenerator;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

import com.revolsys.io.IoConstants;

/**
 * Implementation of the {@link org.springframework.web.servlet.HandlerAdapter}
 * interface that maps handler methods based on HTTP paths, HTTP methods and
 * request parameters expressed through the {@link RequestMapping} annotation.
 * <p>
 * Supports request parameter binding through the {@link RequestParam}
 * annotation. Also supports the {@link ModelAttribute} annotation for exposing
 * model attribute values to the view, as well as {@link InitBinder} for binder
 * initialization methods and {@link SessionAttributes} for automatic session
 * management of specific attributes.
 * <p>
 * This adapter can be customized through various bean properties. A common use
 * case is to apply shared binder initialization logic through a custom
 * {@link #setWebBindingInitializer WebBindingInitializer}.
 * 
 * @author Juergen Hoeller
 * @author Arjen Poutsma
 * @see #setPathMatcher
 * @see #setMethodNameResolver
 * @see #setWebBindingInitializer
 * @see #setSessionAttributeStore
 * @since 2.5
 */
public class AnnotationMethodHandlerAdapter extends WebContentGenerator
  implements HandlerAdapter, Ordered, BeanFactoryAware {

  /**
   * Holder for request mapping metadata. Allows for finding a best matching
   * candidate.
   */
  static class RequestMappingInfo {

    String[] headers = new String[0];

    List<String> matchedPaths = Collections.emptyList();

    RequestMethod[] methods = new RequestMethod[0];

    String[] params = new String[0];

    String[] paths = new String[0];

    public String bestMatchedPath() {
      return (!this.matchedPaths.isEmpty() ? this.matchedPaths.get(0) : null);
    }

    @Override
    public boolean equals(
      final Object obj) {
      final RequestMappingInfo other = (RequestMappingInfo)obj;
      return (Arrays.equals(this.paths, other.paths)
        && Arrays.equals(this.methods, other.methods)
        && Arrays.equals(this.params, other.params) && Arrays.equals(
        this.headers, other.headers));
    }

    @Override
    public int hashCode() {
      return (Arrays.hashCode(this.paths) * 23 + Arrays.hashCode(this.methods)
        * 29 + Arrays.hashCode(this.params) * 31 + Arrays.hashCode(this.headers));
    }

    public boolean matches(
      final HttpServletRequest request) {
      return ServletAnnotationMappingUtils.checkRequestMethod(this.methods,
        request)
        && ServletAnnotationMappingUtils.checkParameters(this.params, request)
        && ServletAnnotationMappingUtils.checkHeaders(this.headers, request);
    }
  }

  /**
   * Comparator capable of sorting {@link RequestMappingInfo}s (RHIs) so that
   * sorting a list with this comparator will result in:
   * <ul>
   * <li>RHIs with {@linkplain RequestMappingInfo#matchedPaths better matched
   * paths} take prescedence over those with a weaker match (as expressed by the
   * {@linkplain PathMatcher#getPatternComparator(String) path pattern
   * comparator}.) Typically, this means that patterns without wild cards and
   * uri templates will be ordered before those without.</li>
   * <li>RHIs with one single {@linkplain RequestMappingInfo#methods request
   * method} will be ordered before those without a method, or with more than
   * one method.</li>
   * <li>RHIs with more {@linkplain RequestMappingInfo#params request
   * parameters} will be ordered before those with less parameters</li> </ol>
   */
  static class RequestMappingInfoComparator implements
    Comparator<RequestMappingInfo> {

    private final Comparator<String> pathComparator;

    RequestMappingInfoComparator(
      final Comparator<String> pathComparator) {
      this.pathComparator = pathComparator;
    }

    public int compare(
      final RequestMappingInfo info1,
      final RequestMappingInfo info2) {
      final int pathComparison = pathComparator.compare(
        info1.bestMatchedPath(), info2.bestMatchedPath());
      if (pathComparison != 0) {
        return pathComparison;
      }
      final int info1ParamCount = info1.params.length;
      final int info2ParamCount = info2.params.length;
      if (info1ParamCount != info2ParamCount) {
        return info2ParamCount - info1ParamCount;
      }
      final int info1HeaderCount = info1.headers.length;
      final int info2HeaderCount = info2.headers.length;
      if (info1HeaderCount != info2HeaderCount) {
        return info2HeaderCount - info1HeaderCount;
      }
      final int info1MethodCount = info1.methods.length;
      final int info2MethodCount = info2.methods.length;
      if (info1MethodCount == 0 && info2MethodCount > 0) {
        return 1;
      } else if (info2MethodCount == 0 && info1MethodCount > 0) {
        return -1;
      } else if (info1MethodCount == 1 & info2MethodCount > 1) {
        return -1;
      } else if (info2MethodCount == 1 & info1MethodCount > 1) {
        return 1;
      }
      return 0;
    }
  }

  /**
   * Servlet-specific subclass of {@link HandlerMethodInvoker}.
   */
  private class ServletHandlerMethodInvoker extends HandlerMethodInvoker {

    private boolean responseArgumentUsed = false;

    private ServletHandlerMethodInvoker(
      final HandlerMethodResolver resolver) {
      super(resolver, webBindingInitializer, sessionAttributeStore,
        parameterNameDiscoverer, customArgumentResolvers, messageConverters);
    }

    /**
     * Resolves the given {@link RequestBody @RequestBody} annotation.
     */
    @SuppressWarnings("unchecked")
    protected Object resolveRequestBody(
      MethodParameter methodParam,
      NativeWebRequest webRequest,
      Object handler)
      throws Exception {

      HttpInputMessage inputMessage = createHttpInputMessage(webRequest);
      Class paramType = methodParam.getParameterType();

      final HttpServletRequest httpRequest = ((ServletWebRequest)webRequest).getRequest();
      MediaType contentType = MediaTypeUtil.getContentType(httpRequest);
      if (contentType == null) {
        contentType = new MediaType("application", "x-www-form-urlencoded");
      }
      if (!new MediaType("application", "x-www-form-urlencoded").includes(contentType)) {
        contentType = MediaTypeUtil.getRequestMediaType(httpRequest,
          mediaTypes, mediaTypeOrder, urlPathHelper, parameterName,
          defaultMediaType);
      }

      final HttpHeaders headers = inputMessage.getHeaders();
      if (contentType == null) {
        StringBuilder builder = new StringBuilder(
          ClassUtils.getShortName(methodParam.getParameterType()));
        String paramName = methodParam.getParameterName();
        if (paramName != null) {
          builder.append(' ');
          builder.append(paramName);
        }
        throw new HttpMediaTypeNotSupportedException(
          "Cannot extract @RequestBody parameter (" + builder.toString()
            + "): no Content-Type found");
      } else {
        headers.setContentType(contentType);
      }
      List<MediaType> allSupportedMediaTypes = new ArrayList<MediaType>();
      if (messageConverters != null) {
        for (HttpMessageConverter<?> messageConverter : messageConverters) {
          allSupportedMediaTypes.addAll(messageConverter.getSupportedMediaTypes());
          if (messageConverter.canRead(paramType, contentType)) {
            return messageConverter.read(paramType, inputMessage);
          }
        }
        if (new MediaType("application", "x-www-form-urlencoded").includes(contentType)) {
          Charset charset = contentType.getCharSet();
          if (charset == null) {
            charset = Charset.forName(WebUtils.DEFAULT_CHARACTER_ENCODING);
          }
          String urlBody = FileCopyUtils.copyToString(new InputStreamReader(
            inputMessage.getBody(), charset));

          String[] pairs = StringUtils.tokenizeToStringArray(urlBody, "&");

          MultiValueMap<String, String> values = new LinkedMultiValueMap<String, String>(
            pairs.length);

          for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx == -1) {
              values.add(URLDecoder.decode(pair, charset.name()), null);
            } else {
              String name = URLDecoder.decode(pair.substring(0, idx),
                charset.name());
              String value = URLDecoder.decode(pair.substring(idx + 1),
                charset.name());
              values.add(name, value);
            }
          }
          String body = values.getFirst("body");
          if (body == null) {
            body = httpRequest.getParameter("body");
          }
          if (body != null) {
            contentType = MediaTypeUtil.getRequestMediaType(httpRequest,
              mediaTypes, mediaTypeOrder, urlPathHelper, parameterName,
              defaultMediaType);
            headers.setContentType(contentType);
            byte[] bytes;
            bytes = body.getBytes();
            final InputStream bodyIn = new ByteArrayInputStream(bytes);
            HttpInputMessage newInputMessage = new HttpInputMessage() {

              public HttpHeaders getHeaders() {
                return headers;
              }

              public InputStream getBody()
                throws IOException {
                return bodyIn;
              }
            };
            for (HttpMessageConverter<?> messageConverter : messageConverters) {
              if (messageConverter.canRead(paramType, contentType)) {
                return messageConverter.read(paramType, newInputMessage);
              }
            }
          }
        }
      }
      throw new HttpMediaTypeNotSupportedException(contentType,
        allSupportedMediaTypes);
    }

    @Override
    protected WebDataBinder createBinder(
      final NativeWebRequest webRequest,
      final Object target,
      final String objectName)
      throws Exception {

      return AnnotationMethodHandlerAdapter.this.createBinder(
        (HttpServletRequest)webRequest.getNativeRequest(), target, objectName);
    }

    @Override
    protected HttpInputMessage createHttpInputMessage(
      final NativeWebRequest webRequest)
      throws Exception {
      final HttpServletRequest servletRequest = (HttpServletRequest)webRequest.getNativeRequest();
      return new ServletServerHttpRequest(servletRequest);
    }

    @Override
    protected void doBind(
      final WebDataBinder binder,
      final NativeWebRequest webRequest)
      throws Exception {
      final ServletRequestDataBinder servletBinder = (ServletRequestDataBinder)binder;
      servletBinder.bind((ServletRequest)webRequest.getNativeRequest());
    }

    @SuppressWarnings("unchecked")
    public ModelAndView getModelAndView(
      final Method handlerMethod,
      final Class handlerType,
      final Object returnValue,
      final ExtendedModelMap implicitModel,
      final ServletWebRequest webRequest)
      throws Exception {

      final ResponseStatus responseStatusAnn = AnnotationUtils.findAnnotation(
        handlerMethod, ResponseStatus.class);
      if (responseStatusAnn != null) {
        final HttpStatus responseStatus = responseStatusAnn.value();
        // to be picked up by the RedirectView
        webRequest.getRequest().setAttribute(View.RESPONSE_STATUS_ATTRIBUTE,
          responseStatus);
        webRequest.getResponse().setStatus(responseStatus.value());
        responseArgumentUsed = true;
      }

      // Invoke custom resolvers if present...
      if (customModelAndViewResolvers != null) {
        for (final ModelAndViewResolver mavResolver : customModelAndViewResolvers) {
          final ModelAndView mav = mavResolver.resolveModelAndView(
            handlerMethod, handlerType, returnValue, implicitModel, webRequest);
          if (mav != ModelAndViewResolver.UNRESOLVED) {
            return mav;
          }
        }
      }

      if (returnValue != null
        && AnnotationUtils.findAnnotation(handlerMethod, ResponseBody.class) != null) {
        handleResponseBody(returnValue, webRequest);
        return null;
      }

      if (returnValue instanceof ModelAndView) {
        final ModelAndView mav = (ModelAndView)returnValue;
        mav.getModelMap().mergeAttributes(implicitModel);
        return mav;
      } else if (returnValue instanceof Model) {
        return new ModelAndView().addAllObjects(implicitModel).addAllObjects(
          ((Model)returnValue).asMap());
      } else if (returnValue instanceof View) {
        return new ModelAndView((View)returnValue).addAllObjects(implicitModel);
      } else if (AnnotationUtils.findAnnotation(handlerMethod,
        ModelAttribute.class) != null) {
        addReturnValueAsModelAttribute(handlerMethod, handlerType, returnValue,
          implicitModel);
        return new ModelAndView().addAllObjects(implicitModel);
      } else if (returnValue instanceof Map) {
        return new ModelAndView().addAllObjects(implicitModel).addAllObjects(
          (Map)returnValue);
      } else if (returnValue instanceof String) {
        return new ModelAndView((String)returnValue).addAllObjects(implicitModel);
      } else if (returnValue == null) {
        // Either returned null or was 'void' return.
        if (this.responseArgumentUsed || webRequest.isNotModified()) {
          return null;
        } else {
          // Assuming view name translation...
          return new ModelAndView().addAllObjects(implicitModel);
        }
      } else if (!BeanUtils.isSimpleProperty(returnValue.getClass())) {
        // Assume a single model attribute...
        addReturnValueAsModelAttribute(handlerMethod, handlerType, returnValue,
          implicitModel);
        return new ModelAndView().addAllObjects(implicitModel);
      } else {
        throw new IllegalArgumentException(
          "Invalid handler method return value: " + returnValue);
      }
    }

    @SuppressWarnings("unchecked")
    private void handleResponseBody(
      final Object returnValue,
      final ServletWebRequest webRequest)
      throws ServletException,
      IOException {

      final HttpServletRequest request = webRequest.getRequest();
      String jsonp = request.getParameter("jsonp");
      if (jsonp == null) {
        jsonp = request.getParameter("callback");
      }
      request.setAttribute(IoConstants.JSONP_PROPERTY, jsonp);
      List<MediaType> acceptedMediaTypes = MediaTypeUtil.getAcceptedMediaTypes(
        request, mediaTypes, mediaTypeOrder, urlPathHelper, parameterName,
        defaultMediaType);
      if (acceptedMediaTypes.isEmpty()) {
        acceptedMediaTypes = Collections.singletonList(MediaType.ALL);
      }
      final HttpOutputMessage outputMessage = new ServletServerHttpResponse(
        webRequest.getResponse());
      final Class<?> returnValueType = returnValue.getClass();
      final Set<MediaType> allSupportedMediaTypes = new LinkedHashSet<MediaType>();
      if (messageConverters != null) {
        for (final MediaType acceptedMediaType : acceptedMediaTypes) {
          for (final HttpMessageConverter messageConverter : messageConverters) {
            allSupportedMediaTypes.addAll(messageConverter.getSupportedMediaTypes());
            if (messageConverter.canWrite(returnValueType, acceptedMediaType)) {
              MediaType mediaType = getMediaType(
                messageConverter.getSupportedMediaTypes(), acceptedMediaType);
              messageConverter.write(returnValue, mediaType, outputMessage);
              this.responseArgumentUsed = true;
              return;
            }
          }
        }
      }
      throw new HttpMediaTypeNotAcceptableException(new ArrayList<MediaType>(
        allSupportedMediaTypes));
    }

    private MediaType getMediaType(
      List<MediaType> supportedMediaTypes,
      MediaType acceptedMediaType) {
      for (MediaType mediaType : supportedMediaTypes) {
        if (mediaType.equals(acceptedMediaType)) {
          return mediaType;
        }
      }
      for (MediaType mediaType : supportedMediaTypes) {
        if (acceptedMediaType.isWildcardType()
          || mediaType.includes(acceptedMediaType)) {
          return mediaType;
        }
      }
      return null;
    }

    @Override
    protected void raiseMissingParameterException(
      final String paramName,
      final Class paramType)
      throws Exception {
      throw new MissingServletRequestParameterException(paramName,
        paramType.getName());
    }

    @Override
    protected void raiseSessionRequiredException(
      final String message)
      throws Exception {
      throw new HttpSessionRequiredException(message);
    }

    @Override
    protected Object resolveCookieValue(
      final String cookieName,
      final Class paramType,
      final NativeWebRequest webRequest)
      throws Exception {

      final HttpServletRequest servletRequest = (HttpServletRequest)webRequest.getNativeRequest();
      final Cookie cookieValue = WebUtils.getCookie(servletRequest, cookieName);
      if (Cookie.class.isAssignableFrom(paramType)) {
        return cookieValue;
      } else if (cookieValue != null) {
        return cookieValue.getValue();
      } else {
        return null;
      }
    }

    @Override
    protected Object resolveDefaultValue(
      final String value) {
      if (beanFactory == null) {
        return value;
      }
      final String placeholdersResolved = beanFactory.resolveEmbeddedValue(value);
      final BeanExpressionResolver exprResolver = beanFactory.getBeanExpressionResolver();
      if (exprResolver == null) {
        return value;
      }
      return exprResolver.evaluate(placeholdersResolved, expressionContext);
    }

    @Override
    @SuppressWarnings( {
      "unchecked"
    })
    protected String resolvePathVariable(
      final String pathVarName,
      final Class paramType,
      final NativeWebRequest webRequest)
      throws Exception {

      final HttpServletRequest servletRequest = (HttpServletRequest)webRequest.getNativeRequest();
      final Map<String, String> uriTemplateVariables = (Map<String, String>)servletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
      if (uriTemplateVariables == null
        || !uriTemplateVariables.containsKey(pathVarName)) {
        throw new IllegalStateException("Could not find @PathVariable ["
          + pathVarName + "] in @RequestMapping");
      }
      return uriTemplateVariables.get(pathVarName);
    }

    @Override
    protected Object resolveStandardArgument(
      final Class parameterType,
      final NativeWebRequest webRequest)
      throws Exception {
      final HttpServletRequest request = (HttpServletRequest)webRequest.getNativeRequest();
      final HttpServletResponse response = (HttpServletResponse)webRequest.getNativeResponse();

      if (ServletRequest.class.isAssignableFrom(parameterType)) {
        return request;
      } else if (ServletResponse.class.isAssignableFrom(parameterType)) {
        this.responseArgumentUsed = true;
        return response;
      } else if (HttpSession.class.isAssignableFrom(parameterType)) {
        return request.getSession();
      } else if (Principal.class.isAssignableFrom(parameterType)) {
        return request.getUserPrincipal();
      } else if (Locale.class.equals(parameterType)) {
        return RequestContextUtils.getLocale(request);
      } else if (InputStream.class.isAssignableFrom(parameterType)) {
        return request.getInputStream();
      } else if (Reader.class.isAssignableFrom(parameterType)) {
        return request.getReader();
      } else if (OutputStream.class.isAssignableFrom(parameterType)) {
        this.responseArgumentUsed = true;
        return response.getOutputStream();
      } else if (Writer.class.isAssignableFrom(parameterType)) {
        this.responseArgumentUsed = true;
        return response.getWriter();
      }
      return super.resolveStandardArgument(parameterType, webRequest);
    }
  }

  /**
   * Servlet-specific subclass of {@link HandlerMethodResolver}.
   */
  private class ServletHandlerMethodResolver extends HandlerMethodResolver {

    private ServletHandlerMethodResolver(
      final Class<?> handlerType) {
      init(handlerType);
    }

    @SuppressWarnings("unchecked")
    private void extractHandlerMethodUriTemplates(
      final String mappedPath,
      final String lookupPath,
      final HttpServletRequest request) {
      Map<String, String> variables = null;
      final boolean hasSuffix = (mappedPath.indexOf('.') != -1);
      if (!hasSuffix && pathMatcher.match(mappedPath + ".*", lookupPath)) {
        final String realPath = mappedPath + ".*";
        if (pathMatcher.match(realPath, lookupPath)) {
          variables = pathMatcher.extractUriTemplateVariables(realPath,
            lookupPath);
        }
      }
      if (variables == null && !mappedPath.startsWith("/")) {
        String realPath = "/**/" + mappedPath;
        if (pathMatcher.match(realPath, lookupPath)) {
          variables = pathMatcher.extractUriTemplateVariables(realPath,
            lookupPath);
        } else {
          realPath = realPath + ".*";
          if (pathMatcher.match(realPath, lookupPath)) {
            variables = pathMatcher.extractUriTemplateVariables(realPath,
              lookupPath);
          }
        }
      }
      if (!CollectionUtils.isEmpty(variables)) {
        final Map<String, String> typeVariables = (Map<String, String>)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (typeVariables != null) {
          variables.putAll(typeVariables);
        }
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
          variables);
      }
    }

    /**
     * Determines the matched pattern for the given methodLevelPattern and path.
     * <p>
     * Uses the following algorithm:
     * <ol>
     * <li>If there is a type-level mapping with path information, it is
     * {@linkplain PathMatcher#combine(String, String) combined} with the
     * method-level pattern.
     * <li>If there is a
     * {@linkplain HandlerMapping#BEST_MATCHING_PATTERN_ATTRIBUTE best matching
     * pattern} in the request, it is combined with the method-level pattern.
     * <li>Otherwise,
     */
    private String getMatchedPattern(
      final String methodLevelPattern,
      final String lookupPath,
      final HttpServletRequest request) {
      if (hasTypeLevelMapping()
        && (!ObjectUtils.isEmpty(getTypeLevelMapping().value()))) {
        final String[] typeLevelPatterns = getTypeLevelMapping().value();
        for (String typeLevelPattern : typeLevelPatterns) {
          if (!typeLevelPattern.startsWith("/")) {
            typeLevelPattern = "/" + typeLevelPattern;
          }
          final String combinedPattern = pathMatcher.combine(typeLevelPattern,
            methodLevelPattern);
          if (isPathMatchInternal(combinedPattern, lookupPath)) {
            return combinedPattern;
          }
        }
        return null;
      }
      final String bestMatchingPattern = (String)request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
      if (StringUtils.hasText(bestMatchingPattern)) {
        final String combinedPattern = pathMatcher.combine(bestMatchingPattern,
          methodLevelPattern);
        if (!combinedPattern.equals(bestMatchingPattern)
          && (isPathMatchInternal(combinedPattern, lookupPath))) {
          return combinedPattern;
        }
      }
      if (isPathMatchInternal(methodLevelPattern, lookupPath)) {
        return methodLevelPattern;
      }
      return null;
    }

    private boolean isPathMatchInternal(
      final String pattern,
      final String lookupPath) {
      if (pattern.equals(lookupPath) || pathMatcher.match(pattern, lookupPath)) {
        return true;
      }
      final boolean hasSuffix = pattern.indexOf('.') != -1;
      if (!hasSuffix && pathMatcher.match(pattern + ".*", lookupPath)) {
        return true;
      }
      final boolean endsWithSlash = pattern.endsWith("/");
      if (!endsWithSlash && pathMatcher.match(pattern + "/", lookupPath)) {
        return true;
      }
      return false;
    }

    public Method resolveHandlerMethod(
      final HttpServletRequest request)
      throws ServletException {
      final String lookupPath = urlPathHelper.getLookupPathForRequest(request);
      final Comparator<String> pathComparator = pathMatcher.getPatternComparator(lookupPath);
      final Map<RequestMappingInfo, Method> targetHandlerMethods = new LinkedHashMap<RequestMappingInfo, Method>();
      final Set<String> allowedMethods = new LinkedHashSet<String>(7);
      String resolvedMethodName = null;
      for (final Method handlerMethod : getHandlerMethods()) {
        final RequestMappingInfo mappingInfo = new RequestMappingInfo();
        final RequestMapping mapping = AnnotationUtils.findAnnotation(
          handlerMethod, RequestMapping.class);
        mappingInfo.paths = mapping.value();
        if (!hasTypeLevelMapping()
          || !Arrays.equals(mapping.method(), getTypeLevelMapping().method())) {
          mappingInfo.methods = mapping.method();
        }
        if (!hasTypeLevelMapping()
          || !Arrays.equals(mapping.params(), getTypeLevelMapping().params())) {
          mappingInfo.params = mapping.params();
        }
        if (!hasTypeLevelMapping()
          || !Arrays.equals(mapping.headers(), getTypeLevelMapping().headers())) {
          mappingInfo.headers = mapping.headers();
        }
        boolean match = false;
        if (mappingInfo.paths.length > 0) {
          final List<String> matchedPaths = new ArrayList<String>(
            mappingInfo.paths.length);
          for (final String methodLevelPattern : mappingInfo.paths) {
            final String matchedPattern = getMatchedPattern(methodLevelPattern,
              lookupPath, request);
            if (matchedPattern != null) {
              if (mappingInfo.matches(request)) {
                match = true;
                matchedPaths.add(matchedPattern);
              } else {
                for (final RequestMethod requestMethod : mappingInfo.methods) {
                  allowedMethods.add(requestMethod.toString());
                }
                break;
              }
            }
          }
          Collections.sort(matchedPaths, pathComparator);
          mappingInfo.matchedPaths = matchedPaths;
        } else {
          // No paths specified: parameter match sufficient.
          match = mappingInfo.matches(request);
          if (match && mappingInfo.methods.length == 0
            && mappingInfo.params.length == 0 && resolvedMethodName != null
            && !resolvedMethodName.equals(handlerMethod.getName())) {
            match = false;
          } else {
            for (final RequestMethod requestMethod : mappingInfo.methods) {
              allowedMethods.add(requestMethod.toString());
            }
          }
        }
        if (match) {
          Method oldMappedMethod = targetHandlerMethods.put(mappingInfo,
            handlerMethod);
          if (oldMappedMethod != null && oldMappedMethod != handlerMethod) {
            if (methodNameResolver != null && mappingInfo.paths.length == 0) {
              if (!oldMappedMethod.getName().equals(handlerMethod.getName())) {
                if (resolvedMethodName == null) {
                  resolvedMethodName = methodNameResolver.getHandlerMethodName(request);
                }
                if (!resolvedMethodName.equals(oldMappedMethod.getName())) {
                  oldMappedMethod = null;
                }
                if (!resolvedMethodName.equals(handlerMethod.getName())) {
                  if (oldMappedMethod != null) {
                    targetHandlerMethods.put(mappingInfo, oldMappedMethod);
                    oldMappedMethod = null;
                  } else {
                    targetHandlerMethods.remove(mappingInfo);
                  }
                }
              }
            }
            if (oldMappedMethod != null) {
              throw new IllegalStateException(
                "Ambiguous handler methods mapped for HTTP path '"
                  + lookupPath
                  + "': {"
                  + oldMappedMethod
                  + ", "
                  + handlerMethod
                  + "}. If you intend to handle the same path in multiple methods, then factor "
                  + "them out into a dedicated handler class with that path mapped at the type level!");
            }
          }
        }
      }
      if (!targetHandlerMethods.isEmpty()) {
        final List<RequestMappingInfo> matches = new ArrayList<RequestMappingInfo>(
          targetHandlerMethods.keySet());
        final RequestMappingInfoComparator requestMappingInfoComparator = new RequestMappingInfoComparator(
          pathComparator);
        Collections.sort(matches, requestMappingInfoComparator);
        final RequestMappingInfo bestMappingMatch = matches.get(0);
        final String bestMatchedPath = bestMappingMatch.bestMatchedPath();
        if (bestMatchedPath != null) {
          extractHandlerMethodUriTemplates(bestMatchedPath, lookupPath, request);
        }
        return targetHandlerMethods.get(bestMappingMatch);
      } else {
        if (!allowedMethods.isEmpty()) {
          throw new HttpRequestMethodNotSupportedException(request.getMethod(),
            StringUtils.toStringArray(allowedMethods));
        } else {
          throw new NoSuchRequestHandlingMethodException(lookupPath,
            request.getMethod(), request.getParameterMap());
        }
      }
    }
  }

  /**
   * Log category to use when no mapped handler is found for a request.
   * 
   * @see #pageNotFoundLogger
   */
  public static final String PAGE_NOT_FOUND_LOG_CATEGORY = "org.springframework.web.servlet.PageNotFound";

  /**
   * Additional logger to use when no mapped handler is found for a request.
   * 
   * @see #PAGE_NOT_FOUND_LOG_CATEGORY
   */
  protected static final Log pageNotFoundLogger = LogFactory.getLog(PAGE_NOT_FOUND_LOG_CATEGORY);

  private UrlPathHelper urlPathHelper = new UrlPathHelper();

  private ConfigurableBeanFactory beanFactory;

  private int cacheSecondsForSessionAttributeHandlers = 0;

  private WebArgumentResolver[] customArgumentResolvers;

  private ModelAndViewResolver[] customModelAndViewResolvers;

  private MediaType defaultMediaType;

  private BeanExpressionContext expressionContext;

  private final ConcurrentMap<String, MediaType> mediaTypes = new ConcurrentHashMap<String, MediaType>();

  private HttpMessageConverter<?>[] messageConverters = new HttpMessageConverter[] {
    new ByteArrayHttpMessageConverter(), new StringHttpMessageConverter(),
    new FormHttpMessageConverter(), new SourceHttpMessageConverter()
  };

  private MethodNameResolver methodNameResolver = new InternalPathMethodNameResolver();

  private final Map<Class<?>, ServletHandlerMethodResolver> methodResolverCache = new ConcurrentHashMap<Class<?>, ServletHandlerMethodResolver>();

  private int order = Ordered.LOWEST_PRECEDENCE;

  private String parameterName = "format";

  private ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

  private PathMatcher pathMatcher = new AntPathMatcher();

  private SessionAttributeStore sessionAttributeStore = new DefaultSessionAttributeStore();

  private boolean synchronizeOnSession = false;

  private WebBindingInitializer webBindingInitializer;

  public AnnotationMethodHandlerAdapter() {
    // no restriction of HTTP methods by default
    super(false);
  }

  /**
   * Template method for creating a new ServletRequestDataBinder instance.
   * <p>
   * The default implementation creates a standard ServletRequestDataBinder.
   * This can be overridden for custom ServletRequestDataBinder subclasses.
   * 
   * @param request current HTTP request
   * @param target the target object to bind onto (or <code>null</code> if the
   *          binder is just used to convert a plain parameter value)
   * @param objectName the objectName of the target object
   * @return the ServletRequestDataBinder instance to use
   * @throws Exception in case of invalid state or arguments
   * @see ServletRequestDataBinder#bind(javax.servlet.ServletRequest)
   * @see ServletRequestDataBinder#convertIfNecessary(Object, Class,
   *      MethodParameter)
   */
  protected ServletRequestDataBinder createBinder(
    final HttpServletRequest request,
    final Object target,
    final String objectName)
    throws Exception {

    return new ServletRequestDataBinder(target, objectName);
  }

  public long getLastModified(
    final HttpServletRequest request,
    final Object handler) {
    return -1;
  }

  private List<String> mediaTypeOrder = Arrays.asList("pathExtension",
    "parameter", "acceptHeader", "defaultMediaType");

  public List<String> getMediaTypeOrder() {
    return mediaTypeOrder;
  }

  public void setMediaTypeOrder(
    List<String> mediaTypeOrder) {
    this.mediaTypeOrder = mediaTypeOrder;
  }

  /**
   * Return the message body converters that this adapter has been configured
   * with.
   */
  public HttpMessageConverter<?>[] getMessageConverters() {
    return messageConverters;
  }

  /**
   * Build a HandlerMethodResolver for the given handler type.
   */
  private ServletHandlerMethodResolver getMethodResolver(
    final Object handler) {
    final Class handlerClass = ClassUtils.getUserClass(handler);
    ServletHandlerMethodResolver resolver = this.methodResolverCache.get(handlerClass);
    if (resolver == null) {
      resolver = new ServletHandlerMethodResolver(handlerClass);
      this.methodResolverCache.put(handlerClass, resolver);
    }
    return resolver;
  }

  public int getOrder() {
    return this.order;
  }

  public ModelAndView handle(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final Object handler)
    throws Exception {

    if (AnnotationUtils.findAnnotation(handler.getClass(),
      SessionAttributes.class) != null) {
      // Always prevent caching in case of session attribute management.
      checkAndPrepare(request, response,
        this.cacheSecondsForSessionAttributeHandlers, true);
      // Prepare cached set of session attributes names.
    } else {
      // Uses configured default cacheSeconds setting.
      checkAndPrepare(request, response, true);
    }

    // Execute invokeHandlerMethod in synchronized block if required.
    if (this.synchronizeOnSession) {
      final HttpSession session = request.getSession(false);
      if (session != null) {
        final Object mutex = WebUtils.getSessionMutex(session);
        synchronized (mutex) {
          return invokeHandlerMethod(request, response, handler);
        }
      }
    }

    return invokeHandlerMethod(request, response, handler);
  }

  protected ModelAndView invokeHandlerMethod(
    final HttpServletRequest request,
    final HttpServletResponse response,
    final Object handler)
    throws Exception {

    final ServletHandlerMethodResolver methodResolver = getMethodResolver(handler);
    final Method handlerMethod = methodResolver.resolveHandlerMethod(request);
    final ServletHandlerMethodInvoker methodInvoker = new ServletHandlerMethodInvoker(
      methodResolver);
    final ServletWebRequest webRequest = new ServletWebRequest(request,
      response);
    final ExtendedModelMap implicitModel = new BindingAwareModelMap();

    final Object result = methodInvoker.invokeHandlerMethod(handlerMethod,
      handler, webRequest, implicitModel);
    final ModelAndView mav = methodInvoker.getModelAndView(handlerMethod,
      handler.getClass(), result, implicitModel, webRequest);
    methodInvoker.updateModelAttributes(handler, (mav != null ? mav.getModel()
      : null), implicitModel, webRequest);
    return mav;
  }

  /**
   * Set if URL lookup should always use the full path within the current
   * servlet context. Else, the path within the current servlet mapping is used
   * if applicable (that is, in the case of a ".../*" servlet mapping in
   * web.xml).
   * <p>
   * Default is "false".
   * 
   * @see org.springframework.web.util.UrlPathHelper#setAlwaysUseFullPath
   */
  public void setAlwaysUseFullPath(
    final boolean alwaysUseFullPath) {
    this.urlPathHelper.setAlwaysUseFullPath(alwaysUseFullPath);
  }

  public void setBeanFactory(
    final BeanFactory beanFactory) {
    if (beanFactory instanceof ConfigurableBeanFactory) {
      this.beanFactory = (ConfigurableBeanFactory)beanFactory;
      this.expressionContext = new BeanExpressionContext(this.beanFactory,
        new RequestScope());
    }
  }

  /**
   * Cache content produced by <code>@SessionAttributes</code> annotated
   * handlers for the given number of seconds. Default is 0, preventing caching
   * completely.
   * <p>
   * In contrast to the "cacheSeconds" property which will apply to all general
   * handlers (but not to <code>@SessionAttributes</code> annotated handlers),
   * this setting will apply to <code>@SessionAttributes</code> annotated
   * handlers only.
   * 
   * @see #setCacheSeconds
   * @see org.springframework.web.bind.annotation.SessionAttributes
   */
  public void setCacheSecondsForSessionAttributeHandlers(
    final int cacheSecondsForSessionAttributeHandlers) {
    this.cacheSecondsForSessionAttributeHandlers = cacheSecondsForSessionAttributeHandlers;
  }

  /**
   * Set a custom WebArgumentResolvers to use for special method parameter
   * types.
   * <p>
   * Such a custom WebArgumentResolver will kick in first, having a chance to
   * resolve an argument value before the standard argument handling kicks in.
   */
  public void setCustomArgumentResolver(
    final WebArgumentResolver argumentResolver) {
    this.customArgumentResolvers = new WebArgumentResolver[] {
      argumentResolver
    };
  }

  /**
   * Set one or more custom WebArgumentResolvers to use for special method
   * parameter types.
   * <p>
   * Any such custom WebArgumentResolver will kick in first, having a chance to
   * resolve an argument value before the standard argument handling kicks in.
   */
  public void setCustomArgumentResolvers(
    final WebArgumentResolver[] argumentResolvers) {
    this.customArgumentResolvers = argumentResolvers;
  }

  /**
   * Set a custom ModelAndViewResolvers to use for special method return types.
   * <p>
   * Such a custom ModelAndViewResolver will kick in first, having a chance to
   * resolve a return value before the standard ModelAndView handling kicks in.
   */
  public void setCustomModelAndViewResolver(
    final ModelAndViewResolver customModelAndViewResolver) {
    this.customModelAndViewResolvers = new ModelAndViewResolver[] {
      customModelAndViewResolver
    };
  }

  /**
   * Set one or more custom ModelAndViewResolvers to use for special method
   * return types.
   * <p>
   * Any such custom ModelAndViewResolver will kick in first, having a chance to
   * resolve a return value before the standard ModelAndView handling kicks in.
   */
  public void setCustomModelAndViewResolvers(
    final ModelAndViewResolver[] customModelAndViewResolvers) {
    this.customModelAndViewResolvers = customModelAndViewResolvers;
  }

  /**
   * Sets the default content type.
   * <p>
   * This content type will be used when file extension, parameter, nor {@code
   * Accept} header define a content-type, either through being disabled or
   * empty.
   */
  public void setDefaultMediaType(
    final MediaType defaultContentType) {
    this.defaultMediaType = defaultContentType;
  }

  /**
   * Sets the mapping from file extensions to media types.
   * <p>
   */
  public void setMediaTypes(
    final Map<String, String> mediaTypes) {
    for (final Map.Entry<String, String> entry : mediaTypes.entrySet()) {
      final String extension = entry.getKey().toLowerCase(Locale.ENGLISH);
      final MediaType mediaType = MediaType.parseMediaType(entry.getValue());
      this.mediaTypes.put(extension, mediaType);
    }
  }

  /**
   * Set the message body converters to use.
   * <p>
   * These converters are used to convert from and to HTTP requests and
   * responses.
   */
  public void setMessageConverters(
    final HttpMessageConverter<?>[] messageConverters) {
    this.messageConverters = messageConverters;
  }

  /**
   * Set the MethodNameResolver to use for resolving default handler methods
   * (carrying an empty <code>@RequestMapping</code> annotation).
   * <p>
   * Will only kick in when the handler method cannot be resolved uniquely
   * through the annotation metadata already.
   */
  public void setMethodNameResolver(
    final MethodNameResolver methodNameResolver) {
    this.methodNameResolver = methodNameResolver;
  }

  /**
   * Specify the order value for this HandlerAdapter bean.
   * <p>
   * Default value is <code>Integer.MAX_VALUE</code>, meaning that it's
   * non-ordered.
   * 
   * @see org.springframework.core.Ordered#getOrder()
   */
  public void setOrder(
    final int order) {
    this.order = order;
  }

  /**
   * Sets the parameter name that can be used to determine the requested media
   * type if the {@link #setFavorParameter(boolean)} property is {@code true}.
   * The default parameter name is {@code format}.
   */
  public void setParameterName(
    final String parameterName) {
    this.parameterName = parameterName;
  }

  /**
   * Set the ParameterNameDiscoverer to use for resolving method parameter names
   * if needed (e.g. for default attribute names).
   * <p>
   * Default is a
   * {@link org.springframework.core.LocalVariableTableParameterNameDiscoverer}.
   */
  public void setParameterNameDiscoverer(
    final ParameterNameDiscoverer parameterNameDiscoverer) {
    this.parameterNameDiscoverer = parameterNameDiscoverer;
  }

  /**
   * Set the PathMatcher implementation to use for matching URL paths against
   * registered URL patterns.
   * <p>
   * Default is {@link org.springframework.util.AntPathMatcher}.
   */
  public void setPathMatcher(
    final PathMatcher pathMatcher) {
    Assert.notNull(pathMatcher, "PathMatcher must not be null");
    this.pathMatcher = pathMatcher;
  }

  /**
   * Specify the strategy to store session attributes with.
   * <p>
   * Default is
   * {@link org.springframework.web.bind.support.DefaultSessionAttributeStore},
   * storing session attributes in the HttpSession, using the same attribute
   * name as in the model.
   */
  public void setSessionAttributeStore(
    final SessionAttributeStore sessionAttributeStore) {
    Assert.notNull(sessionAttributeStore,
      "SessionAttributeStore must not be null");
    this.sessionAttributeStore = sessionAttributeStore;
  }

  /**
   * Set if controller execution should be synchronized on the session, to
   * serialize parallel invocations from the same client.
   * <p>
   * More specifically, the execution of the <code>handleRequestInternal</code>
   * method will get synchronized if this flag is "true". The best available
   * session mutex will be used for the synchronization; ideally, this will be a
   * mutex exposed by HttpSessionMutexListener.
   * <p>
   * The session mutex is guaranteed to be the same object during the entire
   * lifetime of the session, available under the key defined by the
   * <code>SESSION_MUTEX_ATTRIBUTE</code> constant. It serves as a safe
   * reference to synchronize on for locking on the current session.
   * <p>
   * In many cases, the HttpSession reference itself is a safe mutex as well,
   * since it will always be the same object reference for the same active
   * logical session. However, this is not guaranteed across different servlet
   * containers; the only 100% safe way is a session mutex.
   * 
   * @see org.springframework.web.util.HttpSessionMutexListener
   * @see org.springframework.web.util.WebUtils#getSessionMutex(javax.servlet.http.HttpSession)
   */
  public void setSynchronizeOnSession(
    final boolean synchronizeOnSession) {
    this.synchronizeOnSession = synchronizeOnSession;
  }

  /**
   * Set if context path and request URI should be URL-decoded. Both are
   * returned <i>undecoded</i> by the Servlet API, in contrast to the servlet
   * path.
   * <p>
   * Uses either the request encoding or the default encoding according to the
   * Servlet spec (ISO-8859-1).
   * 
   * @see org.springframework.web.util.UrlPathHelper#setUrlDecode
   */
  public void setUrlDecode(
    final boolean urlDecode) {
    this.urlPathHelper.setUrlDecode(urlDecode);
  }

  /**
   * Set the UrlPathHelper to use for resolution of lookup paths.
   * <p>
   * Use this to override the default UrlPathHelper with a custom subclass, or
   * to share common UrlPathHelper settings across multiple HandlerMappings and
   * HandlerAdapters.
   */
  public void setUrlPathHelper(
    final UrlPathHelper urlPathHelper) {
    Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
    this.urlPathHelper = urlPathHelper;
  }

  /**
   * Specify a WebBindingInitializer which will apply pre-configured
   * configuration to every DataBinder that this controller uses.
   */
  public void setWebBindingInitializer(
    final WebBindingInitializer webBindingInitializer) {
    this.webBindingInitializer = webBindingInitializer;
  }

  public boolean supports(
    final Object handler) {
    return getMethodResolver(handler).hasHandlerMethods();
  }

}
