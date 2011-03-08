package com.revolsys.spring;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;

import com.revolsys.beans.ResourceEditorRegistrar;
import com.revolsys.spring.config.AttributesBeanConfigurer;

public class ModuleImport implements BeanDefinitionRegistryPostProcessor {

  private GenericApplicationContext applicationContext;

  private Map<String, String> exportBeanAliases = Collections.emptyMap();

  private List<String> exportBeanNames = Collections.emptyList();

  private boolean exportAllBeans = false;

  private Map<String, Object> parameters = new HashMap<String, Object>();

  private Resource resource;

  private boolean enabled = true;

  private ResourceEditorRegistrar resourceEditorRegistrar = new ResourceEditorRegistrar();

  private GenericApplicationContext getApplicationContext() {
    if (applicationContext == null) {
      applicationContext = new GenericApplicationContext();
      final DefaultListableBeanFactory beanFactory = applicationContext.getDefaultListableBeanFactory();

      beanFactory.addPropertyEditorRegistrar(resourceEditorRegistrar);
      if (parameters != null && !parameters.isEmpty()) {
        AttributesBeanConfigurer attributesConfig = new AttributesBeanConfigurer(
          parameters);
        applicationContext.addBeanFactoryPostProcessor(attributesConfig);
      }
      final XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader(
        applicationContext);
      beanReader.loadBeanDefinitions(resource);
      applicationContext.refresh();
    }
    return applicationContext;
  }

  public Map<String, String> getExportBeanAliases() {
    return exportBeanAliases;
  }

  public List<String> getExportBeanNames() {
    return exportBeanNames;
  }

  public Map<String, Object> getParameters() {
    return parameters;
  }

  public Resource getResource() {
    return resource;
  }

  public ResourceEditorRegistrar getResourceEditorRegistrar() {
    return resourceEditorRegistrar;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public boolean isExportAllBeans() {
    return exportAllBeans;
  }


  public void postProcessBeanDefinitionRegistry(
    final BeanDefinitionRegistry registry) throws BeansException {
    if (enabled) {
      final GenericApplicationContext beanFactory = getApplicationContext();
      if (exportAllBeans) {
        for (final String beanName : beanFactory.getBeanDefinitionNames()) {
          registerTargetBeanDefinition(registry, beanFactory, beanName,
            beanName);
        }
      } else {
        for (final String beanName : exportBeanNames) {
          registerTargetBeanDefinition(registry, beanFactory, beanName,
            beanName);
        }
      }

      for (final Entry<String, String> exportBeanAlias : exportBeanAliases.entrySet()) {
        final String beanName = exportBeanAlias.getKey();
        final String alias = exportBeanAlias.getValue();
        registerTargetBeanDefinition(registry, beanFactory, beanName, alias);
      }
    }
  }

  public void postProcessBeanFactory(
    final ConfigurableListableBeanFactory beanFactory) throws BeansException {
  }

  private void registerTargetBeanDefinition(
    final BeanDefinitionRegistry registry,
    final GenericApplicationContext beanFactory, final String beanName,
    final String alias) {
    final BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
    if (beanDefinition != null) {
      final boolean singleton = beanDefinition.isSingleton();
      final GenericBeanDefinition proxyBeanDefinition = new GenericBeanDefinition();
      proxyBeanDefinition.setBeanClass(com.revolsys.spring.TargetBeanFactoryBean.class);
      final MutablePropertyValues values = new MutablePropertyValues();
      values.addPropertyValue("targetBeanName", beanName);
      values.addPropertyValue("targetBeanFactory", beanFactory);
      values.addPropertyValue("singleton", singleton);
      proxyBeanDefinition.setPropertyValues(values);
      registry.registerBeanDefinition(alias, proxyBeanDefinition);
    }
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public void setExportAllBeans(final boolean exportAllBeans) {
    this.exportAllBeans = exportAllBeans;
  }

  public void setExportBeanAliases(final Map<String, String> exportBeanAliases) {
    this.exportBeanAliases = exportBeanAliases;
  }

  public void setExportBeanNames(final List<String> exportBeanNames) {
    this.exportBeanNames = exportBeanNames;
  }


  public void setParameters(final Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  public void setResource(final Resource resource) {
    this.resource = resource;
  }

  public void setResourceEditorRegistrar(
    final ResourceEditorRegistrar resourceEditorRegistrar) {
    this.resourceEditorRegistrar = resourceEditorRegistrar;
  }

}
