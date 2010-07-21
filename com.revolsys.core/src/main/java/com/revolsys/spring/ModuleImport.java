package com.revolsys.spring;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;

import com.revolsys.parallel.process.ResourceEditorRegistrar;

public class ModuleImport implements BeanDefinitionRegistryPostProcessor {

  private Resource resource;

  private Map<String, Object> parameters;

  private ResourceEditorRegistrar resourceEditorRegistrar = new ResourceEditorRegistrar();

  private GenericApplicationContext getApplicationContext() {
    GenericApplicationContext beans = new GenericApplicationContext();
    final DefaultListableBeanFactory beanFactory = beans.getDefaultListableBeanFactory();
    
    beanFactory.addPropertyEditorRegistrar(resourceEditorRegistrar);

    final XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader(
      beans);
    beanReader.loadBeanDefinitions(resource);
    beans.refresh();
    return beans;
  }

  public void postProcessBeanDefinitionRegistry(
    BeanDefinitionRegistry registry)
    throws BeansException {
    // TODO Auto-generated method stub

  }

  public void postProcessBeanFactory(
    ConfigurableListableBeanFactory beanFactory)
    throws BeansException {
    // TODO Auto-generated method stub

  }

  public Resource getResource() {
    return resource;
  }

  public void setResource(
    Resource resource) {
    this.resource = resource;
  }

  public Map<String, Object> getParameters() {
    return parameters;
  }

  public void setParameters(
    Map<String, Object> parameters) {
    this.parameters = parameters;
  }

  public ResourceEditorRegistrar getResourceEditorRegistrar() {
    return resourceEditorRegistrar;
  }

  public void setResourceEditorRegistrar(
    ResourceEditorRegistrar resourceEditorRegistrar) {
    this.resourceEditorRegistrar = resourceEditorRegistrar;
  }
  
}
