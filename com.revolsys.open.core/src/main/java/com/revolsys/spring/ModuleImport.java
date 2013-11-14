package com.revolsys.spring;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.revolsys.beans.ResourceEditorRegistrar;
import com.revolsys.collection.AttributeMap;
import com.revolsys.spring.config.AttributesBeanConfigurer;

public class ModuleImport implements BeanFactoryPostProcessor, BeanNameAware,
  DisposableBean {

  public static GenericBeanDefinition createTargetBeanDefinition(
    final BeanDefinitionRegistry beanFactory, final String beanName) {

    if (beanFactory.containsBeanDefinition(beanName)) {
      final BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

      final boolean singleton = beanDefinition.isSingleton();
      final GenericBeanDefinition proxyBeanDefinition = new GenericBeanDefinition();
      proxyBeanDefinition.setBeanClass(TargetBeanFactoryBean.class);
      final MutablePropertyValues values = new MutablePropertyValues();
      final String beanClassName = beanDefinition.getBeanClassName();
      final PropertyValue beanDefinitionProperty = new PropertyValue(
        "targetBeanDefinition", beanDefinition);
      beanDefinitionProperty.setConvertedValue(beanDefinition);
      values.addPropertyValue(beanDefinitionProperty);
      values.addPropertyValue("targetBeanName", beanName);
      values.addPropertyValue("targetBeanClass", beanClassName);
      values.addPropertyValue("targetBeanFactory", beanFactory);
      values.addPropertyValue("singleton", singleton);
      proxyBeanDefinition.setPropertyValues(values);
      return proxyBeanDefinition;
    } else {
      return null;
    }
  }

  public static void registerTargetBeanDefinition(
    final BeanDefinitionRegistry registry, final BeanFactory beanFactory,
    final String beanName, final String alias) {

    final BeanDefinition beanDefinition = createTargetBeanDefinition(
      (BeanDefinitionRegistry)beanFactory, beanName);
    if (beanDefinition != null) {
      registry.registerBeanDefinition(alias, beanDefinition);
    }
  }

  private GenericApplicationContext applicationContext;

  private Map<String, String> exportBeanAliases = Collections.emptyMap();

  private Map<String, String> importBeanAliases = Collections.emptyMap();

  private List<String> exportBeanNames = Collections.emptyList();

  private List<String> importBeanNames = Collections.emptyList();

  private boolean exportAllBeans = false;

  private Map<String, Object> parameters = new HashMap<String, Object>();

  private Collection<Resource> resources = new LinkedHashSet<Resource>();

  private boolean enabled = true;

  private ResourceEditorRegistrar resourceEditorRegistrar = new ResourceEditorRegistrar();

  private String beanName;

  private Set<String> beanNamesNotToExport = new HashSet<String>();

  public ModuleImport() {
    beanNamesNotToExport.add("com.revolsys.spring.config.AttributesBeanConfigurer");
  }

  protected void afterPostProcessBeanDefinitionRegistry(
    final BeanDefinitionRegistry registry) {
  }

  protected void beforePostProcessBeanDefinitionRegistry(
    final BeanDefinitionRegistry registry) throws BeansException {
  }

  @Override
  public void destroy() {
    if (applicationContext != null) {
      applicationContext.close();
      applicationContext = null;
      beanName = null;
      beanNamesNotToExport = null;
      exportBeanAliases = null;
      exportBeanNames = null;
      importBeanAliases = null;
      importBeanNames = null;
      parameters = null;
      resourceEditorRegistrar = null;
      resources = null;
    }
  }

  protected GenericApplicationContext getApplicationContext(
    final BeanDefinitionRegistry parentRegistry) {
    if (applicationContext == null) {
      applicationContext = new GenericApplicationContext();
      if (parentRegistry instanceof ResourceLoader) {
        final ResourceLoader resourceLoader = (ResourceLoader)parentRegistry;
        final ClassLoader classLoader = resourceLoader.getClassLoader();
        applicationContext.setClassLoader(classLoader);
      }
      AnnotationConfigUtils.registerAnnotationConfigProcessors(
        applicationContext, null);
      final DefaultListableBeanFactory beanFactory = applicationContext.getDefaultListableBeanFactory();

      final BeanFactory parentBeanFactory = (BeanFactory)parentRegistry;
      for (final String beanName : parentRegistry.getBeanDefinitionNames()) {
        final BeanDefinition beanDefinition = parentRegistry.getBeanDefinition(beanName);
        final String beanClassName = beanDefinition.getBeanClassName();
        if (beanClassName != null) {
          if (beanClassName.equals(AttributeMap.class.getName())) {
            registerTargetBeanDefinition(applicationContext, parentBeanFactory,
              beanName, beanName);
            beanNamesNotToExport.add(beanName);
          } else if (beanClassName.equals(MapFactoryBean.class.getName())) {
            final PropertyValue targetMapClass = beanDefinition.getPropertyValues()
              .getPropertyValue("targetMapClass");
            if (targetMapClass != null) {
              final Object mapClass = targetMapClass.getValue();
              if (AttributeMap.class.getName().equals(mapClass)) {
                registerTargetBeanDefinition(applicationContext,
                  parentBeanFactory, beanName, beanName);
                beanNamesNotToExport.add(beanName);
              }
            }
          }
        }
      }
      beanFactory.addPropertyEditorRegistrar(resourceEditorRegistrar);
      final AttributesBeanConfigurer attributesConfig = new AttributesBeanConfigurer(
        applicationContext, parameters);
      applicationContext.addBeanFactoryPostProcessor(attributesConfig);
      for (final String beanName : importBeanNames) {
        registerTargetBeanDefinition(applicationContext, parentBeanFactory,
          beanName, beanName);
        beanNamesNotToExport.add(beanName);
      }
      for (final Entry<String, String> entry : importBeanAliases.entrySet()) {
        final String beanName = entry.getKey();
        final String aliasName = entry.getValue();
        registerTargetBeanDefinition(applicationContext, parentBeanFactory,
          beanName, aliasName);
        beanNamesNotToExport.add(aliasName);
      }
      final XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader(
        applicationContext);
      for (final Resource resource : resources) {
        beanReader.loadBeanDefinitions(resource);
      }
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

  public Map<String, String> getImportBeanAliases() {
    return importBeanAliases;
  }

  public List<String> getImportBeanNames() {
    return importBeanNames;
  }

  public Map<String, Object> getParameters() {
    return parameters;
  }

  public ResourceEditorRegistrar getResourceEditorRegistrar() {
    return resourceEditorRegistrar;
  }

  public Collection<Resource> getResources() {
    return resources;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public boolean isExportAllBeans() {
    return exportAllBeans;
  }

  private void postProcessBeanDefinitionRegistry(
    final BeanDefinitionRegistry registry) throws BeansException {
    beforePostProcessBeanDefinitionRegistry(registry);
    if (enabled) {
      final GenericApplicationContext beanFactory = getApplicationContext(registry);
      if (exportAllBeans) {
        for (final String beanName : beanFactory.getBeanDefinitionNames()) {
          if (!beanNamesNotToExport.contains(beanName)) {
            registerTargetBeanDefinition(registry, beanFactory, beanName,
              beanName);
            for (final String alias : beanFactory.getAliases(beanName)) {
              if (!beanNamesNotToExport.contains(alias)) {
                registerTargetBeanDefinition(registry, beanFactory, beanName,
                  alias);
              }
            }
          }
        }
      } else {
        for (final String beanName : exportBeanNames) {
          if (!beanNamesNotToExport.contains(beanName)) {
            registerTargetBeanDefinition(registry, beanFactory, beanName,
              beanName);
          }
        }
      }

      for (final Entry<String, String> exportBeanAlias : exportBeanAliases.entrySet()) {
        final String beanName = exportBeanAlias.getKey();
        final String alias = exportBeanAlias.getValue();
        if (!beanNamesNotToExport.contains(alias)) {
          registerTargetBeanDefinition(registry, beanFactory, beanName, alias);
        }
      }
      afterPostProcessBeanDefinitionRegistry(registry);
    }
  }

  @Override
  public void postProcessBeanFactory(
    final ConfigurableListableBeanFactory beanFactory) throws BeansException {
    if (beanFactory instanceof BeanDefinitionRegistry) {
      final BeanDefinitionRegistry registry = (BeanDefinitionRegistry)beanFactory;
      postProcessBeanDefinitionRegistry(registry);
    }
  }

  @Override
  public void setBeanName(final String beanName) {
    this.beanName = beanName;
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

  public void setImportBeanAliases(final Map<String, String> importBeanAliases) {
    this.importBeanAliases = importBeanAliases;
  }

  public void setImportBeanNames(final List<String> importBeanNames) {
    this.importBeanNames = importBeanNames;
  }

  public void setParameters(final Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  public void setResource(final Resource resource) {
    this.resources.add(resource);
  }

  public void setResourceEditorRegistrar(
    final ResourceEditorRegistrar resourceEditorRegistrar) {
    this.resourceEditorRegistrar = resourceEditorRegistrar;
  }

  public void setResources(final Collection<Resource> resources) {
    this.resources = resources;
  }

  @Override
  public String toString() {
    return beanName;
  }
}
