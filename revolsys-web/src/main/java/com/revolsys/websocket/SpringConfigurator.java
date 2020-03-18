package com.revolsys.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig.Configurator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * This class is a copy of org.springframework.web.socket.server.standard.SpringConfigurator.<p>
 * A {@link javax.websocket.server.ServerEndpointConfig.Configurator} for initializing
 * {@link ServerEndpoint}-annotated classes through Spring.
 *
 * <p>
 * <pre class="code">
 * &#064;ServerEndpoint(value = "/echo", configurator = SpringConfigurator.class)
 * public class EchoEndpoint {
 *     // ...
 * }
 * </pre>
 *
 * @author Rossen Stoyanchev
 * @since 4.0
 *
 */
public class SpringConfigurator extends Configurator {

  private static final Map<String, Map<Class<?>, String>> cache = new ConcurrentHashMap<>();

  private static Log logger = LogFactory.getLog(SpringConfigurator.class);

  private static final String NO_VALUE = ObjectUtils.identityToString(new Object());

  private String getBeanNameByType(final WebApplicationContext wac, final Class<?> endpointClass) {

    final String wacId = wac.getId();

    Map<Class<?>, String> beanNamesByType = cache.get(wacId);
    if (beanNamesByType == null) {
      beanNamesByType = new ConcurrentHashMap<>();
      cache.put(wacId, beanNamesByType);
    }

    if (!beanNamesByType.containsKey(endpointClass)) {
      final String[] names = wac.getBeanNamesForType(endpointClass);
      if (names.length == 1) {
        beanNamesByType.put(endpointClass, names[0]);
      } else {
        beanNamesByType.put(endpointClass, NO_VALUE);
        if (names.length > 1) {
          final String message = "Found multiple @ServerEndpoint's of type " + endpointClass
            + ", names=" + names;
          logger.error(message);
          throw new IllegalStateException(message);
        }
      }
    }

    final String beanName = beanNamesByType.get(endpointClass);
    return NO_VALUE.equals(beanName) ? null : beanName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getEndpointInstance(final Class<T> endpointClass) throws InstantiationException {

    final WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
    if (wac == null) {
      final String message = "Failed to find the root WebApplicationContext. Was ContextLoaderListener not used?";
      logger.error(message);
      throw new IllegalStateException(message);
    }

    String beanName = ClassUtils.getShortNameAsProperty(endpointClass);
    if (wac.containsBean(beanName)) {
      final T endpoint = wac.getBean(beanName, endpointClass);
      if (logger.isTraceEnabled()) {
        logger.trace("Using @ServerEndpoint singleton " + endpoint);
      }
      return endpoint;
    }

    final Component annot = AnnotationUtils.findAnnotation(endpointClass, Component.class);
    if (annot != null && wac.containsBean(annot.value())) {
      final T endpoint = wac.getBean(annot.value(), endpointClass);
      if (logger.isTraceEnabled()) {
        logger.trace("Using @ServerEndpoint singleton " + endpoint);
      }
      return endpoint;
    }

    beanName = getBeanNameByType(wac, endpointClass);
    if (beanName != null) {
      return (T)wac.getBean(beanName);
    }

    if (logger.isTraceEnabled()) {
      logger.trace("Creating new @ServerEndpoint instance of type " + endpointClass);
    }
    return wac.getAutowireCapableBeanFactory().createBean(endpointClass);
  }

}
