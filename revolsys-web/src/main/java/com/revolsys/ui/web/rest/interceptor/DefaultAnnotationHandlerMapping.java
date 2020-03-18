/*
 * Copyright 2002-2012 the original author or authors.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.handler.AbstractDetectingUrlHandlerMapping;

import com.revolsys.ui.web.annotation.RequestMapping;

public class DefaultAnnotationHandlerMapping extends AbstractDetectingUrlHandlerMapping {

  static final String USE_DEFAULT_SUFFIX_PATTERN = DefaultAnnotationHandlerMapping.class.getName()
    + ".useDefaultSuffixPattern";

  private boolean useDefaultSuffixPattern = true;

  private final Map<Class<?>, RequestMapping> cachedMappings = new HashMap<>();

  /**
   * Add URLs and/or URL patterns for the given path.
   * @param urls the Set of URLs for the current bean
   * @param path the currently introspected path
   */
  protected void addUrlsForPath(final Set<String> urls, final String path) {
    urls.add(path);
    if (this.useDefaultSuffixPattern && path.indexOf('.') == -1 && !path.endsWith("/")) {
      urls.add(path + ".*");
      urls.add(path + "/");
    }
  }

  /**
   * Checks for presence of the {@link org.springframework.web.bind.annotation.RequestMapping}
   * annotation on the handler class and on any of its methods.
   */
  @Override
  protected String[] determineUrlsForHandler(final String beanName) {
    final ApplicationContext context = getApplicationContext();
    final Class<?> handlerType = context.getType(beanName);
    final RequestMapping mapping = context.findAnnotationOnBean(beanName, RequestMapping.class);
    if (mapping != null) {
      // @RequestMapping found at type level
      this.cachedMappings.put(handlerType, mapping);
      final Set<String> urls = new LinkedHashSet<>();
      final String[] typeLevelPatterns = mapping.value();
      if (typeLevelPatterns.length > 0) {
        // @RequestMapping specifies paths at type level
        final String[] methodLevelPatterns = determineUrlsForHandlerMethods(handlerType, true);
        for (String typeLevelPattern : typeLevelPatterns) {
          if (!typeLevelPattern.startsWith("/")) {
            typeLevelPattern = "/" + typeLevelPattern;
          }
          boolean hasEmptyMethodLevelMappings = false;
          for (final String methodLevelPattern : methodLevelPatterns) {
            if (methodLevelPattern == null) {
              hasEmptyMethodLevelMappings = true;
            } else {
              final String combinedPattern = getPathMatcher().combine(typeLevelPattern,
                methodLevelPattern);
              addUrlsForPath(urls, combinedPattern);
            }
          }
          if (hasEmptyMethodLevelMappings
            || org.springframework.web.servlet.mvc.Controller.class.isAssignableFrom(handlerType)) {
            addUrlsForPath(urls, typeLevelPattern);
          }
        }
        return StringUtils.toStringArray(urls);
      } else {
        // actual paths specified by @RequestMapping at method level
        return determineUrlsForHandlerMethods(handlerType, false);
      }
    } else if (AnnotationUtils.findAnnotation(handlerType, Controller.class) != null) {
      // @RequestMapping to be introspected at method level
      return determineUrlsForHandlerMethods(handlerType, false);
    } else {
      return null;
    }
  }

  /**
   * Derive URL mappings from the handler's method-level mappings.
   * @param handlerType the handler type to introspect
   * @return the array of mapped URLs
   */
  protected String[] determineUrlsForHandlerMethods(final Class<?> handlerType) {
    return null;
  }

  /**
   * Derive URL mappings from the handler's method-level mappings.
   * @param handlerType the handler type to introspect
   * @param hasTypeLevelMapping whether the method-level mappings are nested
   * within a type-level mapping
   * @return the array of mapped URLs
   */
  protected String[] determineUrlsForHandlerMethods(final Class<?> handlerType,
    final boolean hasTypeLevelMapping) {
    final String[] subclassResult = determineUrlsForHandlerMethods(handlerType);
    if (subclassResult != null) {
      return subclassResult;
    }

    final Set<String> urls = new LinkedHashSet<>();
    final Set<Class<?>> handlerTypes = new LinkedHashSet<>();
    handlerTypes.add(handlerType);
    handlerTypes.addAll(Arrays.asList(handlerType.getInterfaces()));
    for (final Class<?> currentHandlerType : handlerTypes) {
      ReflectionUtils.doWithMethods(currentHandlerType, new ReflectionUtils.MethodCallback() {
        @Override
        public void doWith(final Method method) {
          final RequestMapping mapping = AnnotationUtils.findAnnotation(method,
            RequestMapping.class);
          if (mapping != null) {
            final String[] mappedPatterns = mapping.value();
            if (mappedPatterns.length > 0) {
              for (String mappedPattern : mappedPatterns) {
                if (!hasTypeLevelMapping && !mappedPattern.startsWith("/")) {
                  mappedPattern = "/" + mappedPattern;
                }
                addUrlsForPath(urls, mappedPattern);
              }
            } else if (hasTypeLevelMapping) {
              // empty method-level RequestMapping
              urls.add(null);
            }
          }
        }
      }, ReflectionUtils.USER_DECLARED_METHODS);
    }
    return StringUtils.toStringArray(urls);
  }

  /**
   * Set whether to register paths using the default suffix pattern as well:
   * i.e. whether "/users" should be registered as "/users.*" and "/users/" too.
   * <p>Default is "true". Turn this convention off if you intend to interpret
   * your {@code @RequestMapping} paths strictly.
   * <p>Note that paths which include a ".xxx" suffix or end with "/" already will not be
   * transformed using the default suffix pattern in any case.
   */
  public void setUseDefaultSuffixPattern(final boolean useDefaultSuffixPattern) {
    this.useDefaultSuffixPattern = useDefaultSuffixPattern;
  }

  @Override
  protected boolean supportsTypeLevelMappings() {
    return true;
  }

  /**
   * Validate the given annotated handler against the current request.
   * @see #validateMapping
   */
  @Override
  protected void validateHandler(final Object handler, final HttpServletRequest request)
    throws Exception {
    RequestMapping mapping = this.cachedMappings.get(handler.getClass());
    if (mapping == null) {
      mapping = AnnotationUtils.findAnnotation(handler.getClass(), RequestMapping.class);
    }
    if (mapping != null) {
      validateMapping(mapping, request);
    }
    request.setAttribute(USE_DEFAULT_SUFFIX_PATTERN, this.useDefaultSuffixPattern);
  }

  /**
   * Validate the given type-level mapping metadata against the current request,
   * checking HTTP request method and parameter conditions.
   * @param mapping the mapping metadata to validate
   * @param request current HTTP request
   * @throws Exception if validation failed
   */
  protected void validateMapping(final RequestMapping mapping, final HttpServletRequest request)
    throws Exception {
    final RequestMethod[] mappedMethods = mapping.method();
    if (!ServletAnnotationMappingUtils.checkRequestMethod(mappedMethods, request)) {
      final String[] supportedMethods = new String[mappedMethods.length];
      for (int i = 0; i < mappedMethods.length; i++) {
        supportedMethods[i] = mappedMethods[i].name();
      }
      throw new HttpRequestMethodNotSupportedException(request.getMethod(), supportedMethods);
    }
  }
}
