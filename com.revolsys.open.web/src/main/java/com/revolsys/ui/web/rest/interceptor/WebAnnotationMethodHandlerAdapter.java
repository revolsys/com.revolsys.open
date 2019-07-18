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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.Conventions;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;
import org.springframework.validation.support.BindingAwareModelMap;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.annotation.ModelAndViewResolver;
import org.springframework.web.servlet.support.WebContentGenerator;
import org.springframework.web.util.UrlPathHelper;

import com.revolsys.io.IoConstants;
import com.revolsys.ui.web.annotation.RequestMapping;
import com.revolsys.ui.web.utils.HttpServletUtils;

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
public class WebAnnotationMethodHandlerAdapter extends WebContentGenerator
  implements HandlerAdapter, Ordered {

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

  private ModelAndViewResolver[] customModelAndViewResolvers;

  protected MediaType defaultMediaType;

  protected List<String> mediaTypeOrder = Arrays.asList("attribute", "parameter", "fileName",
    "pathExtension", "acceptHeader", "defaultMediaType");

  protected final ConcurrentMap<String, MediaType> mediaTypes = new ConcurrentHashMap<>();

  protected HttpMessageConverter<?>[] messageConverters = new HttpMessageConverter[] {
    new ByteArrayHttpMessageConverter(), new StringHttpMessageConverter(),
    new FormHttpMessageConverter()
  };

  protected InternalPathMethodNameResolver methodNameResolver = new InternalPathMethodNameResolver();

  private final Map<Class<?>, AnnotationHandlerMethodResolver> methodResolverCache = new ConcurrentHashMap<>();

  private int order = Ordered.LOWEST_PRECEDENCE;

  protected String parameterName = "format";

  protected PathMatcher pathMatcher = new AntPathMatcher();

  protected UrlPathHelper urlPathHelper = new UrlPathHelper();

  public WebAnnotationMethodHandlerAdapter() {
    super(false);
  }

  protected final void addReturnValueAsModelAttribute(final Method handlerMethod,
    final Class<?> handlerType, final Object returnValue, final ExtendedModelMap implicitModel) {

    final ModelAttribute attr = AnnotationUtils.findAnnotation(handlerMethod, ModelAttribute.class);
    String attrName = attr != null ? attr.value() : "";
    if ("".equals(attrName)) {
      final Class<?> resolvedType = GenericTypeResolver.resolveReturnType(handlerMethod,
        handlerType);
      attrName = Conventions.getVariableNameForReturnType(handlerMethod, resolvedType, returnValue);
    }
    implicitModel.addAttribute(attrName, returnValue);
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
  protected ServletRequestDataBinder createBinder(final HttpServletRequest request,
    final Object target, final String objectName) throws Exception {

    return new ServletRequestDataBinder(target, objectName);
  }

  protected WebDataBinder createBinder(final NativeWebRequest webRequest, final Object target,
    final String objectName) throws Exception {

    return createBinder((HttpServletRequest)webRequest.getNativeRequest(), target, objectName);
  }

  @Override
  public long getLastModified(final HttpServletRequest request, final Object handler) {
    return -1;
  }

  private MediaType getMediaType(final List<MediaType> supportedMediaTypes,
    final MediaType acceptedMediaType) {
    for (final MediaType mediaType : supportedMediaTypes) {
      if (mediaType.equals(acceptedMediaType)) {
        return mediaType;
      }
    }
    for (final MediaType mediaType : supportedMediaTypes) {
      if (acceptedMediaType.isWildcardType() || mediaType.includes(acceptedMediaType)) {
        return mediaType;
      }
    }
    return null;
  }

  public List<String> getMediaTypeOrder() {
    return this.mediaTypeOrder;
  }

  /**
   * Return the message body converters that this adapter has been configured
   * with.
   */
  public HttpMessageConverter<?>[] getMessageConverters() {
    return this.messageConverters;
  }

  /**
   * Build a HandlerMethodResolver for the given handler type.
   */
  private AnnotationHandlerMethodResolver getMethodResolver(final Object handler) {
    final Class<?> handlerClass = ClassUtils.getUserClass(handler);
    AnnotationHandlerMethodResolver resolver = this.methodResolverCache.get(handlerClass);
    if (resolver == null) {
      resolver = new AnnotationHandlerMethodResolver(this, handlerClass);
      this.methodResolverCache.put(handlerClass, resolver);
    }
    return resolver;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public ModelAndView getModelAndView(final Method handlerMethod, final Class<?> handlerType,
    final Object returnValue, final ExtendedModelMap implicitModel,
    final ServletWebRequest webRequest) throws Exception {
    boolean responseArgumentUsed = false;
    final ResponseStatus responseStatusAnn = AnnotationUtils.findAnnotation(handlerMethod,
      ResponseStatus.class);
    if (responseStatusAnn != null) {
      final HttpStatus responseStatus = responseStatusAnn.value();
      // to be picked up by the RedirectView
      webRequest.getRequest().setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, responseStatus);
      webRequest.getResponse().setStatus(responseStatus.value());
      responseArgumentUsed = true;
    }

    // Invoke custom resolvers if present...
    if (WebAnnotationMethodHandlerAdapter.this.customModelAndViewResolvers != null) {
      for (final ModelAndViewResolver mavResolver : WebAnnotationMethodHandlerAdapter.this.customModelAndViewResolvers) {
        final ModelAndView mav = mavResolver.resolveModelAndView(handlerMethod, handlerType,
          returnValue, implicitModel, webRequest);
        if (mav != ModelAndViewResolver.UNRESOLVED) {
          return mav;
        }
      }
    }

    if (returnValue != null
      && AnnotationUtils.findAnnotation(handlerMethod, ResponseBody.class) != null) {
      final View view = handleResponseBody(returnValue, webRequest);
      return new ModelAndView(view).addAllObjects(implicitModel);
    }

    if (returnValue instanceof ModelAndView) {
      final ModelAndView mav = (ModelAndView)returnValue;
      mav.getModelMap().mergeAttributes(implicitModel);
      return mav;
    } else if (returnValue instanceof Model) {
      return new ModelAndView().addAllObjects(implicitModel)
        .addAllObjects(((Model)returnValue).asMap());
    } else if (returnValue instanceof View) {
      return new ModelAndView((View)returnValue).addAllObjects(implicitModel);
    } else if (AnnotationUtils.findAnnotation(handlerMethod, ModelAttribute.class) != null) {
      addReturnValueAsModelAttribute(handlerMethod, handlerType, returnValue, implicitModel);
      return new ModelAndView().addAllObjects(implicitModel);
    } else if (returnValue instanceof Map) {
      return new ModelAndView().addAllObjects(implicitModel).addAllObjects((Map)returnValue);
    } else if (returnValue instanceof String) {
      return new ModelAndView((String)returnValue).addAllObjects(implicitModel);
    } else if (returnValue == null) {
      // Either returned null or was 'void' return.
      if (responseArgumentUsed || webRequest.isNotModified()) {
        return null;
      } else {
        // Assuming view name translation...
        return new ModelAndView().addAllObjects(implicitModel);
      }
    } else if (!BeanUtils.isSimpleProperty(returnValue.getClass())) {
      // Assume a single model attribute...
      addReturnValueAsModelAttribute(handlerMethod, handlerType, returnValue, implicitModel);
      return new ModelAndView().addAllObjects(implicitModel);
    } else {
      throw new IllegalArgumentException("Invalid handler method return value: " + returnValue);
    }
  }

  @Override
  public int getOrder() {
    return this.order;
  }

  @Override
  public ModelAndView handle(final HttpServletRequest request, final HttpServletResponse response,
    final Object handler) throws Exception {
    final HttpServletRequest savedRequest = HttpServletUtils.getRequest();
    final HttpServletResponse savedResponse = HttpServletUtils.getResponse();
    try {
      HttpServletUtils.setRequestAndResponse(request, response);
      checkAndPrepare(request, response, true);

      return invokeHandlerMethod(request, response, handler);
    } finally {
      if (savedRequest == null) {
        HttpServletUtils.clearRequestAndResponse();
      } else {
        HttpServletUtils.setRequestAndResponse(savedRequest, savedResponse);
      }
    }
  }

  private View handleResponseBody(final Object returnValue, final ServletWebRequest webRequest)
    throws ServletException, IOException {

    final HttpServletRequest request = webRequest.getRequest();
    String jsonp = request.getParameter("jsonp");
    if (jsonp == null) {
      jsonp = request.getParameter("callback");
    }
    request.setAttribute(IoConstants.JSONP_PROPERTY, jsonp);
    List<MediaType> acceptedMediaTypes = MediaTypeUtil.getAcceptedMediaTypes(request,
      WebAnnotationMethodHandlerAdapter.this.mediaTypes,
      WebAnnotationMethodHandlerAdapter.this.mediaTypeOrder,
      WebAnnotationMethodHandlerAdapter.this.urlPathHelper,
      WebAnnotationMethodHandlerAdapter.this.parameterName,
      WebAnnotationMethodHandlerAdapter.this.defaultMediaType);
    if (acceptedMediaTypes.isEmpty()) {
      acceptedMediaTypes = Collections.singletonList(MediaType.ALL);
    }
    final Class<?> returnValueType = returnValue.getClass();
    final Set<MediaType> allSupportedMediaTypes = new LinkedHashSet<>();
    if (WebAnnotationMethodHandlerAdapter.this.messageConverters != null) {
      for (final MediaType acceptedMediaType : acceptedMediaTypes) {
        for (final HttpMessageConverter<?> messageConverter : WebAnnotationMethodHandlerAdapter.this.messageConverters) {
          allSupportedMediaTypes.addAll(messageConverter.getSupportedMediaTypes());
          if (messageConverter.canWrite(returnValueType, acceptedMediaType)) {
            final MediaType mediaType = getMediaType(messageConverter.getSupportedMediaTypes(),
              acceptedMediaType);
            return new HttpMessageConverterView(messageConverter, mediaType, returnValue);
          }
        }
      }
    }
    throw new HttpMediaTypeNotAcceptableException(new ArrayList<>(allSupportedMediaTypes));
  }

  protected ModelAndView invokeHandlerMethod(final HttpServletRequest request,
    final HttpServletResponse response, final Object handler) throws Exception {

    final AnnotationHandlerMethodResolver methodResolver = getMethodResolver(handler);
    final WebMethodHandler handlerMethod = methodResolver.resolveHandlerMethod(request);
    final ServletWebRequest webRequest = new ServletWebRequest(request, response);
    final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    try {
      RequestContextHolder.setRequestAttributes(webRequest);
      final ExtendedModelMap implicitModel = new BindingAwareModelMap();

      final Object result = handlerMethod.invokeMethod(handler, request, response);
      if (result == null) {
        return null;
      } else {
        final ModelAndView mav = getModelAndView(handlerMethod.getMethod(), handler.getClass(),
          result, implicitModel, webRequest);
        return mav;
      }
    } finally {
      RequestContextHolder.setRequestAttributes(requestAttributes);
    }
  }

  /**
   * Determine whether the given value qualifies as a "binding candidate", i.e. might potentially be subject to
   * bean-style data binding later on.
   */
  protected boolean isBindingCandidate(final Object value) {
    return value != null && !value.getClass().isArray() && !(value instanceof Collection)
      && !(value instanceof Map) && !BeanUtils.isSimpleValueType(value.getClass());
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
  public void setAlwaysUseFullPath(final boolean alwaysUseFullPath) {
    this.urlPathHelper.setAlwaysUseFullPath(alwaysUseFullPath);
  }

  /**
   * Set a custom ModelAndViewResolvers to use for special method return types.
   * <p>
   * Such a custom ModelAndViewResolver will kick in first, having a chance to
   * resolve a return value before the standard ModelAndView handling kicks in.
   */
  public void setCustomModelAndViewResolver(final ModelAndViewResolver customModelAndViewResolver) {
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
   * This content type will be used when file extension, parameter, nor
   * {@code Accept} header define a content-type, either through being disabled
   * or empty.
   */
  public void setDefaultMediaType(final MediaType defaultContentType) {
    this.defaultMediaType = defaultContentType;
  }

  public void setMediaTypeOrder(final List<String> mediaTypeOrder) {
    this.mediaTypeOrder = mediaTypeOrder;
  }

  /**
   * Sets the mapping from file extensions to media types.
   * <p>
   */
  public void setMediaTypes(final Map<String, String> mediaTypes) {
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
  public void setMessageConverters(final HttpMessageConverter<?>[] messageConverters) {
    this.messageConverters = messageConverters;
  }

  /**
   * Set the MethodNameResolver to use for resolving default handler methods
   * (carrying an empty <code>@RequestMapping</code> annotation).
   * <p>
   * Will only kick in when the handler method cannot be resolved uniquely
   * through the annotation metadata already.
   */
  public void setMethodNameResolver(final InternalPathMethodNameResolver methodNameResolver) {
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
  public void setOrder(final int order) {
    this.order = order;
  }

  /**
   * Sets the parameter name that can be used to determine the requested media
   * type if the {@link #setFavorParameter(boolean)} property is {@code true}.
   * The default parameter name is {@code format}.
   */
  public void setParameterName(final String parameterName) {
    this.parameterName = parameterName;
  }

  /**
   * Set the PathMatcher implementation to use for matching URL paths against
   * registered URL patterns.
   * <p>
   * Default is {@link org.springframework.util.AntPathMatcher}.
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
   *
   * @see org.springframework.web.util.UrlPathHelper#setUrlDecode
   */
  public void setUrlDecode(final boolean urlDecode) {
    this.urlPathHelper.setUrlDecode(urlDecode);
  }

  /**
   * Set the UrlPathHelper to use for resolution of lookup paths.
   * <p>
   * Use this to override the default UrlPathHelper with a custom subclass, or
   * to share common UrlPathHelper settings across multiple HandlerMappings and
   * HandlerAdapters.
   */
  public void setUrlPathHelper(final UrlPathHelper urlPathHelper) {
    Assert.notNull(urlPathHelper, "UrlPathHelper must not be null");
    this.urlPathHelper = urlPathHelper;
  }

  @Override
  public boolean supports(final Object handler) {
    final AnnotationHandlerMethodResolver methodResolver = getMethodResolver(handler);
    return methodResolver.hasHandlerMethods();
  }
}
