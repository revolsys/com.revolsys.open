package com.revolsys.parallel.tools;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.StringValueResolver;

public class AttributesBeanConfigurer implements BeanFactoryPostProcessor,
  BeanFactoryAware, BeanNameAware, PriorityOrdered {

  public static final String DEFAULT_BEAN_NAME_SEPARATOR = ".";

  /** Default placeholder prefix: "${" */
  public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

  /** Default placeholder suffix: "}" */
  public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

  private static final Logger LOG = LoggerFactory.getLogger(AttributesBeanConfigurer.class);

  private Map<String, Object> attributes = new LinkedHashMap<String, Object>();

  private BeanFactory beanFactory;

  private String beanName;

  private String beanNameSeparator = DEFAULT_BEAN_NAME_SEPARATOR;

  private boolean ignoreInvalidKeys = false;

  private boolean ignoreUnresolvablePlaceholders;

  private String nullValue;

  private int order = Ordered.LOWEST_PRECEDENCE;

  private String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

  private String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public BeanFactory getBeanFactory() {
    return beanFactory;
  }

  public String getBeanName() {
    return beanName;
  }

  public String getBeanNameSeparator() {
    return beanNameSeparator;
  }

  public String getNullValue() {
    return nullValue;
  }

  public int getOrder() {
    return this.order;
  }

  public String getPlaceholderPrefix() {
    return placeholderPrefix;
  }

  public String getPlaceholderSuffix() {
    return placeholderSuffix;
  }

  public boolean isIgnoreInvalidKeys() {
    return ignoreInvalidKeys;
  }

  public boolean isIgnoreUnresolvablePlaceholders() {
    return ignoreUnresolvablePlaceholders;
  }

  public void postProcessBeanFactory(
    final ConfigurableListableBeanFactory beanFactory)
    throws BeansException {
    Map<String, Object> attributes = new LinkedHashMap<String, Object>(
      getAttributes());
    attributes.putAll(ThreadSharedAttributes.getAttributes());

    String[] beanNames = beanFactory.getBeanDefinitionNames();
    for (int i = 0; i < beanNames.length; i++) {
      // Check that we're not parsing our own bean definition,
      // to avoid failing on unresolvable placeholders in properties file
      // locations.
      String beanName = beanNames[i];
      if (!(beanName.equals(this.beanName))) {
        BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
        String beanClassName = bd.getBeanClassName();

        if (beanClassName != null) {
          if (beanClassName.equals(AttributeMap.class.getName())) {

            processPlaceholderAttributes(beanFactory, beanName, attributes);
            Map<String, Object> otherAttributes = (Map<String, Object>)beanFactory.getBean(beanName);
            attributes.putAll(otherAttributes);
          } else if (beanClassName.equals(org.springframework.beans.factory.config.MapFactoryBean.class.getName())) {
            final PropertyValue targetMapClass = bd.getPropertyValues()
              .getPropertyValue("targetMapClass");
            if (targetMapClass != null) {
              final Object mapClass = targetMapClass.getValue();
              if (AttributeMap.class.getName().equals(mapClass)) {
                processPlaceholderAttributes(beanFactory, beanName, attributes);
                Map<String, Object> otherAttributes = (Map<String, Object>)beanFactory.getBean(beanName);
                attributes.putAll(otherAttributes);
              }
            }
          }
        }
      }
    }
    processOverrideAttributes(beanFactory, attributes);
    processPlaceholderAttributes(beanFactory, attributes);
  }

  /**
   * Process the given key as 'beanName.property' entry.
   */
  protected void processOverride(
    final ConfigurableListableBeanFactory factory,
    final String key,
    final Object value)
    throws BeansException {

    int separatorIndex = key.indexOf(this.beanNameSeparator);
    if (separatorIndex != -1) {
      String beanName = key.substring(0, separatorIndex);
      String beanProperty = key.substring(separatorIndex + 1);
      setAttributeValue(factory, beanName, beanProperty, value);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Property '" + key + "' set to value [" + value + "]");
      }
    }
  }

  protected void processOverrideAttributes(
    final ConfigurableListableBeanFactory beanFactory,
    final Map<String, Object> attributes)
    throws BeansException {

    for (Entry<String, Object> attribute : attributes.entrySet()) {
      String key = attribute.getKey();
      Object value = attribute.getValue();
      try {
        processOverride(beanFactory, key, value);
      } catch (BeansException ex) {
        String msg = "Could not process key '" + key
          + "' in PropertyOverrideConfigurer";
        if (!this.ignoreInvalidKeys) {
          throw new BeanInitializationException(msg, ex);
        }
        if (LOG.isDebugEnabled()) {
          LOG.debug(msg, ex);
        }
      }
    }
  }

  protected void processPlaceholderAttributes(
    final ConfigurableListableBeanFactory beanFactory,
    final Map<String, Object> attributes)
    throws BeansException {

    StringValueResolver valueResolver = new PlaceholderResolvingStringValueResolver(
      placeholderPrefix, placeholderSuffix, ignoreUnresolvablePlaceholders,
      nullValue, attributes);
    BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(valueResolver);

    String[] beanNames = beanFactory.getBeanDefinitionNames();
    for (int i = 0; i < beanNames.length; i++) {
      // Check that we're not parsing our own bean definition,
      // to avoid failing on unresolvable placeholders in properties file
      // locations.
      if (!(beanNames[i].equals(this.beanName) && beanFactory.equals(this.beanFactory))) {
        BeanDefinition bd = beanFactory.getBeanDefinition(beanNames[i]);
        try {
          visitor.visitBeanDefinition(bd);
        } catch (BeanDefinitionStoreException ex) {
          throw new BeanDefinitionStoreException(bd.getResourceDescription(),
            beanNames[i], ex.getMessage());
        }
      }
    }

    // New in Spring 2.5: resolve placeholders in alias target names and aliases
    // as well.
    beanFactory.resolveAliases(valueResolver);
  }

  protected void processPlaceholderAttributes(
    final ConfigurableListableBeanFactory beanFactory,
    final String beanName,
    final Map<String, Object> attributes)
    throws BeansException {

    StringValueResolver valueResolver = new PlaceholderResolvingStringValueResolver(
      placeholderPrefix, placeholderSuffix, ignoreUnresolvablePlaceholders,
      nullValue, attributes);
    BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(valueResolver);

    // Check that we're not parsing our own bean definition,
    // to avoid failing on unresolvable placeholders in properties file
    // locations.
    if (!(beanName.equals(this.beanName) && beanFactory.equals(this.beanFactory))) {
      BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
      try {
        visitor.visitBeanDefinition(bd);
      } catch (BeanDefinitionStoreException ex) {
        throw new BeanDefinitionStoreException(bd.getResourceDescription(),
          beanName, ex.getMessage());
      }
    }

    // New in Spring 2.5: resolve placeholders in alias target names and aliases
    // as well.
    beanFactory.resolveAliases(valueResolver);
  }

  public void setAttributes(
    final Map<String, ? extends Object> attributes) {
    this.attributes.clear();
    if (attributes != null) {
      this.attributes.putAll(attributes);
    }
  }

  /**
   * Apply the given property value to the corresponding bean.
   */
  protected void setAttributeValue(
    final ConfigurableListableBeanFactory factory,
    final String beanName,
    final String property,
    final Object value) {

    BeanDefinition bd = factory.getBeanDefinition(beanName);
    while (bd.getOriginatingBeanDefinition() != null) {
      bd = bd.getOriginatingBeanDefinition();
    }
    MutablePropertyValues propertyValues = bd.getPropertyValues();
    PropertyValue propertyValue = new PropertyValue(property, value);
    if (bd.getBeanClassName().equals(Parameter.class.getName())) {
      PropertyValue typeValue = propertyValues.getPropertyValue("type");
      if (typeValue != null) {
        String typeClassName = typeValue.getValue().toString();
        try {
          Class<?> typeClass = Class.forName(typeClassName);

          Object convertedValue = new SimpleTypeConverter().convertIfNecessary(
            value, typeClass);
          propertyValue = new PropertyValue(property, convertedValue);
        } catch (Throwable e) {
          LOG.error("Unable to set " + beanName + "." + property + "=" + value,
            e);
        }
      }
    }
    propertyValues.addPropertyValue(propertyValue);
  }

  public void setBeanFactory(
    final BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  public void setBeanName(
    final String beanName) {
    this.beanName = beanName;
  }

  /**
   * Set the separator to expect between bean name and property path. Default is
   * a dot (".").
   */
  public void setBeanNameSeparator(
    final String beanNameSeparator) {
    this.beanNameSeparator = beanNameSeparator;
  }

  /**
   * Set whether to ignore invalid keys. Default is "false".
   * <p>
   * If you ignore invalid keys, keys that do not follow the 'beanName.property'
   * format will just be logged as warning. This allows to have arbitrary other
   * keys in a properties file.
   */
  public void setIgnoreInvalidKeys(
    final boolean ignoreInvalidKeys) {
    this.ignoreInvalidKeys = ignoreInvalidKeys;
  }

  public void setIgnoreUnresolvablePlaceholders(
    final boolean ignoreUnresolvablePlaceholders) {
    this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
  }

  public void setNullValue(
    final String nullValue) {
    this.nullValue = nullValue;
  }

  public void setOrder(
    final int order) {
    this.order = order;
  }

  public void setPlaceholderPrefix(
    final String placeholderPrefix) {
    this.placeholderPrefix = placeholderPrefix;
  }

  public void setPlaceholderSuffix(
    final String placeholderSuffix) {
    this.placeholderSuffix = placeholderSuffix;
  }

}
