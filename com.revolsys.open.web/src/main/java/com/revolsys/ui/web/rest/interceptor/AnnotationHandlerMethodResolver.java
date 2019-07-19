/*
 * Copyright 2002-2013 the original author or authors.
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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.revolsys.ui.web.annotation.RequestMapping;
import com.revolsys.util.Property;

/**
 * Support class for resolving web method annotations in a handler type.
 * Processes {@code @RequestMapping}, {@code @InitBinder},
 * {@code @ModelAttribute} and {@code @SessionAttributes}.
 *
 * <p>Used by {@link org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter}
 * and {@link org.springframework.web.portlet.mvc.annotation.AnnotationMethodHandlerAdapter}.
 *
 * @author Juergen Hoeller
 * @since 2.5.2
 * @see com.revolsys.ui.web.annotation.RequestMapping
 * @see org.springframework.web.bind.annotation.InitBinder
 * @see org.springframework.web.bind.annotation.ModelAttribute
 * @see org.springframework.web.bind.annotation.SessionAttributes
 */
public class AnnotationHandlerMethodResolver {
  private final Set<WebMethodHandler> handlerMethods = new LinkedHashSet<>();

  private final RequestMapping typeLevelMapping;

  private final WebAnnotationMethodHandlerAdapter adapter;

  public AnnotationHandlerMethodResolver(final WebAnnotationMethodHandlerAdapter adapter,
    final Class<?> handlerType) {
    this.adapter = adapter;
    final Set<Class<?>> handlerTypes = new LinkedHashSet<>();
    Class<?> specificHandlerType = null;
    if (!Proxy.isProxyClass(handlerType)) {
      handlerTypes.add(handlerType);
      specificHandlerType = handlerType;
    }
    handlerTypes.addAll(Arrays.asList(handlerType.getInterfaces()));
    for (final Class<?> currentHandlerType : handlerTypes) {
      final Class<?> targetClass;
      if (specificHandlerType == null) {
        targetClass = currentHandlerType;
      } else {
        targetClass = specificHandlerType;
      }
      ReflectionUtils.doWithMethods(currentHandlerType, (method) -> {
        final Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
        final Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
        if (isHandlerMethod(specificMethod)
          && (bridgedMethod == specificMethod || !isHandlerMethod(bridgedMethod))) {
          AnnotationHandlerMethodResolver.this.handlerMethods
            .add(new WebMethodHandler(AnnotationHandlerMethodResolver.this.adapter, method));
        }
      }, ReflectionUtils.USER_DECLARED_METHODS);
    }
    this.typeLevelMapping = AnnotationUtils.findAnnotation(handlerType, RequestMapping.class);
  }

  @SuppressWarnings("unchecked")
  private void extractHandlerMethodUriTemplates(final String mappedPath, final String lookupPath,
    final HttpServletRequest request) {
    Map<String, String> variables = null;
    final boolean hasSuffix = mappedPath.indexOf('.') != -1;
    if (!hasSuffix && this.adapter.pathMatcher.match(mappedPath + ".*", lookupPath)) {
      final String realPath = mappedPath + ".*";
      if (this.adapter.pathMatcher.match(realPath, lookupPath)) {
        variables = this.adapter.pathMatcher.extractUriTemplateVariables(realPath, lookupPath);
      }
    }
    if (variables == null && !mappedPath.startsWith("/")) {
      String realPath = "/**/" + mappedPath;
      if (this.adapter.pathMatcher.match(realPath, lookupPath)) {
        variables = this.adapter.pathMatcher.extractUriTemplateVariables(realPath, lookupPath);
      } else {
        realPath = realPath + ".*";
        if (this.adapter.pathMatcher.match(realPath, lookupPath)) {
          variables = this.adapter.pathMatcher.extractUriTemplateVariables(realPath, lookupPath);
        }
      }
    }
    if (!CollectionUtils.isEmpty(variables)) {
      final Map<String, String> typeVariables = (Map<String, String>)request
        .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
      if (typeVariables != null) {
        variables.putAll(typeVariables);
      }
      request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, variables);
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
  private String getMatchedPattern(final String methodLevelPattern, final String lookupPath,
    final HttpServletRequest request) {
    if (hasTypeLevelMapping() && !ObjectUtils.isEmpty(this.typeLevelMapping.value())) {
      final String[] typeLevelPatterns = this.typeLevelMapping.value();
      for (String typeLevelPattern : typeLevelPatterns) {
        if (!typeLevelPattern.startsWith("/")) {
          typeLevelPattern = "/" + typeLevelPattern;
        }
        final String combinedPattern = this.adapter.pathMatcher.combine(typeLevelPattern,
          methodLevelPattern);
        if (isPathMatchInternal(combinedPattern, lookupPath)) {
          return combinedPattern;
        }
      }
      return null;
    }
    final String bestMatchingPattern = (String)request
      .getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    if (Property.hasValue(bestMatchingPattern)) {
      final String combinedPattern = this.adapter.pathMatcher.combine(bestMatchingPattern,
        methodLevelPattern);
      if (!combinedPattern.equals(bestMatchingPattern)
        && isPathMatchInternal(combinedPattern, lookupPath)) {
        return combinedPattern;
      }
    }
    if (isPathMatchInternal(methodLevelPattern, lookupPath)) {
      return methodLevelPattern;
    }
    return null;
  }

  public final boolean hasHandlerMethods() {
    return !this.handlerMethods.isEmpty();
  }

  public boolean hasTypeLevelMapping() {
    return this.typeLevelMapping != null;
  }

  protected boolean isHandlerMethod(final Method method) {
    return AnnotationUtils.findAnnotation(method, RequestMapping.class) != null;
  }

  private boolean isPathMatchInternal(final String pattern, final String lookupPath) {
    if (pattern.equals(lookupPath) || this.adapter.pathMatcher.match(pattern, lookupPath)) {
      return true;
    }
    final boolean hasSuffix = pattern.indexOf('.') != -1;
    if (!hasSuffix && this.adapter.pathMatcher.match(pattern + ".*", lookupPath)) {
      return true;
    }
    final boolean endsWithSlash = pattern.endsWith("/");
    if (!endsWithSlash && this.adapter.pathMatcher.match(pattern + "/", lookupPath)) {
      return true;
    }
    return false;
  }

  public WebMethodHandler resolveHandlerMethod(final HttpServletRequest request)
    throws ServletException {
    final String lookupPath = this.adapter.urlPathHelper.getLookupPathForRequest(request);
    final Comparator<String> pathComparator = this.adapter.pathMatcher
      .getPatternComparator(lookupPath);
    final Map<RequestMappingInfo, WebMethodHandler> targetHandlerMethods = new LinkedHashMap<>();
    final Set<String> allowedMethods = new LinkedHashSet<>(7);
    String resolvedMethodName = null;
    for (final WebMethodHandler webMethodHandler : this.handlerMethods) {
      final Method handlerMethod = webMethodHandler.getMethod();
      final RequestMappingInfo mappingInfo = new RequestMappingInfo();
      final RequestMapping mapping = AnnotationUtils.findAnnotation(handlerMethod,
        RequestMapping.class);
      mappingInfo.paths = mapping.value();
      if (!hasTypeLevelMapping()
        || !Arrays.equals(mapping.method(), this.typeLevelMapping.method())) {
        mappingInfo.methods = mapping.method();
      }
      boolean match = false;
      if (mappingInfo.paths.length > 0) {
        final List<String> matchedPaths = new ArrayList<>(mappingInfo.paths.length);
        for (final String methodLevelPattern : mappingInfo.paths) {
          final String matchedPattern = getMatchedPattern(methodLevelPattern, lookupPath, request);
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
        if (match && mappingInfo.methods.length == 0 && resolvedMethodName != null
          && !resolvedMethodName.equals(handlerMethod.getName())) {
          match = false;
        } else {
          for (final RequestMethod requestMethod : mappingInfo.methods) {
            allowedMethods.add(requestMethod.toString());
          }
        }
      }
      if (match) {
        WebMethodHandler oldMappedMethod = targetHandlerMethods.put(mappingInfo, webMethodHandler);
        if (oldMappedMethod != null && oldMappedMethod != webMethodHandler) {
          if (this.adapter.methodNameResolver != null && mappingInfo.paths.length == 0) {
            if (!oldMappedMethod.getMethod().getName().equals(handlerMethod.getName())) {
              if (resolvedMethodName == null) {
                resolvedMethodName = this.adapter.methodNameResolver.getHandlerMethodName(request);
              }
              if (!resolvedMethodName.equals(oldMappedMethod.getMethod().getName())) {
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
            throw new IllegalStateException("Ambiguous handler methods mapped for HTTP path '"
              + lookupPath + "': {" + oldMappedMethod + ", " + handlerMethod
              + "}. If you intend to handle the same path in multiple methods, then factor "
              + "them out into a dedicated handler class with that path mapped at the type level!");
          }
        }
      }
    }
    if (!targetHandlerMethods.isEmpty()) {
      final List<RequestMappingInfo> matches = new ArrayList<>(targetHandlerMethods.keySet());
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
        throw new NoHandlerFoundException(request.getMethod(), lookupPath, null);
      }
    }
  }

}
