package com.revolsys.spring.resource;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;

import com.revolsys.spring.config.AttributesBeanConfigurer;

public class SpringUtil {
  public static void close(final ConfigurableApplicationContext applicationContext) {
    if (applicationContext != null) {
      if (applicationContext.isActive()) {
        applicationContext.close();
      }
    }
  }

  public static GenericApplicationContext getApplicationContext(final ClassLoader classLoader,
    final Resource... resources) {
    final GenericApplicationContext applicationContext = new GenericApplicationContext();
    applicationContext.setClassLoader(classLoader);

    AnnotationConfigUtils.registerAnnotationConfigProcessors(applicationContext, null);
    final AttributesBeanConfigurer attributesConfig = new AttributesBeanConfigurer(
      applicationContext);
    applicationContext.addBeanFactoryPostProcessor(attributesConfig);

    final XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader(applicationContext);
    beanReader.setBeanClassLoader(classLoader);
    beanReader.loadBeanDefinitions(resources);
    applicationContext.refresh();
    return applicationContext;
  }
}
