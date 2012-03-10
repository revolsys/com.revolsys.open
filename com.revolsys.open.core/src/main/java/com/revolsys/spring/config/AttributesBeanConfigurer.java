package com.revolsys.spring.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.StringValueResolver;

import com.revolsys.collection.AttributeMap;
import com.revolsys.collection.ThreadSharedAttributes;
import com.revolsys.spring.BeanReference;
import com.revolsys.spring.SpringUtil;
import com.revolsys.spring.TargetBeanFactoryBean;
import com.revolsys.spring.factory.Parameter;
import com.revolsys.spring.util.PlaceholderResolvingStringValueResolver;

public class AttributesBeanConfigurer implements BeanFactoryPostProcessor,
  ApplicationContextAware, BeanNameAware, PriorityOrdered {

  public static final String DEFAULT_BEAN_NAME_SEPARATOR = ".";

  /** Default placeholder prefix: "${" */
  public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

  /** Default placeholder suffix: "}" */
  public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

  private static final Logger LOG = LoggerFactory.getLogger(AttributesBeanConfigurer.class);

  /**
   * Apply the given property value to the corresponding bean.
   */
  public static void setAttributeValue(
    final ConfigurableListableBeanFactory factory,
    final String beanName,
    final String property,
    final Object value) {
    ClassLoader classLoader = factory.getBeanClassLoader();

    BeanDefinition bd = factory.getBeanDefinition(beanName);
    while (bd.getOriginatingBeanDefinition() != null) {
      bd = bd.getOriginatingBeanDefinition();
    }
    final MutablePropertyValues propertyValues = bd.getPropertyValues();
    PropertyValue propertyValue = new PropertyValue(property, value);
    final String beanClassName = bd.getBeanClassName();
    if (!TargetBeanFactoryBean.class.getName().equals(beanClassName)) {
      if (Parameter.class.getName().equals(beanClassName)) {
        final PropertyValue typeValue = propertyValues.getPropertyValue("type");
        if (typeValue != null) {
          final String typeClassName = typeValue.getValue().toString();
          try {
            final Class<?> typeClass = Class.forName(typeClassName, true,
              classLoader);

            final Object convertedValue = new SimpleTypeConverter().convertIfNecessary(
              value, typeClass);
            propertyValue = new PropertyValue(property, convertedValue);
          } catch (final Throwable e) {
            LOG.error("Unable to set " + beanName + "." + property + "="
              + value, e);
          }
        }
      }
      propertyValues.addPropertyValue(propertyValue);
    }
  }

  private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();

  private ApplicationContext applicationContext;

  private String beanName;

  private boolean ignoreInvalidKeys = true;

  private boolean ignoreUnresolvablePlaceholders = true;

  private String nullValue;

  private int order = Ordered.LOWEST_PRECEDENCE;

  private String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

  private String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;

  public AttributesBeanConfigurer() {
  }

  public AttributesBeanConfigurer(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public AttributesBeanConfigurer(ApplicationContext applicationContext,
    final Map<String, Object> attributes) {
    this.applicationContext = applicationContext;
    if (attributes != null) {
      this.attributes.putAll(attributes);
    }
  }

  @SuppressWarnings("unchecked")
  protected void addAttributes(
    final Map<String, Object> attributes,
    final ConfigurableListableBeanFactory beanFactory,
    final BeanDefinition beanDefinition,
    final String beanName,
    final String beanClassName) {
    if (beanClassName != null) {
      if (beanClassName.equals(AttributeMap.class.getName())) {
        processPlaceholderAttributes(beanFactory, beanName, attributes);
        final Map<String, Object> otherAttributes = (Map<String, Object>)beanFactory.getBean(beanName);
        processPlaceholderAttributes(beanFactory, otherAttributes);
        attributes.putAll(otherAttributes);
      } else if (beanClassName.equals(MapFactoryBean.class.getName())) {
        final MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
        final PropertyValue targetMapClass = propertyValues.getPropertyValue("targetMapClass");
        if (targetMapClass != null) {
          final Object mapClass = targetMapClass.getValue();
          if (AttributeMap.class.getName().equals(mapClass)) {
            processPlaceholderAttributes(beanFactory, beanName, attributes);
            final Map<String, Object> otherAttributes = (Map<String, Object>)beanFactory.getBean(beanName);
            processPlaceholderAttributes(beanFactory, otherAttributes);
            attributes.putAll(otherAttributes);
          }
        }
      }
    }
  }

  public void createParameterBeanDefinition(
    final ConfigurableListableBeanFactory factory,
    final String beanName,
    final Object value) {
    final GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
    beanDefinition.setBeanClass(Parameter.class);
    final MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
    propertyValues.add("type", value.getClass());
    propertyValues.add("value", value);
    ((DefaultListableBeanFactory)factory).registerBeanDefinition(beanName,
      beanDefinition);
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public String getBeanName() {
    return beanName;
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
    final ConfigurableListableBeanFactory beanFactory) throws BeansException {
    final Map<String, Object> allAttributes = new LinkedHashMap<String, Object>();
    final Map<String, Object> threadAttributes = ThreadSharedAttributes.getAttributes();
    allAttributes.putAll(threadAttributes);
    processPlaceholderAttributes(beanFactory, threadAttributes);
    processPlaceholderAttributes(beanFactory, attributes);
    for (final Entry<String, Object> entry : attributes.entrySet()) {
      final String key = entry.getKey();
      if (!allAttributes.containsKey(key)) {
        final Object value = entry.getValue();
        allAttributes.put(key, value);
      }
    }

    final String[] beanNames = beanFactory.getBeanDefinitionNames();
    for (int i = 0; i < beanNames.length; i++) {
      // Check that we're not parsing our own bean definition,
      // to avoid failing on unresolvable placeholders in properties file
      // locations.
      final String beanName = beanNames[i];
      if (!(beanName.equals(this.beanName))) {
        final BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
        final String beanClassName = bd.getBeanClassName();

        if (beanClassName != null) {
          addAttributes(allAttributes, beanFactory, bd, beanName, beanClassName);
          if (beanClassName.equals(TargetBeanFactoryBean.class.getName())) {
            final MutablePropertyValues propertyValues = bd.getPropertyValues();
            final BeanDefinition targetBeanDefinition = (BeanDefinition)propertyValues.getPropertyValue(
              "targetBeanDefinition")
              .getValue();
            final String targetBeanClassName = targetBeanDefinition.getBeanClassName();
            addAttributes(allAttributes, beanFactory, targetBeanDefinition,
              beanName, targetBeanClassName);
          }
        }
      }
    }

    processOverrideAttributes(beanFactory, allAttributes);
  }

  /**
   * Process the given key as 'beanName.property' entry.
   */
  protected void processOverride(
    final ConfigurableListableBeanFactory factory,
    final String key,
    Object value) {

    try {
      if (value instanceof BeanReference) {
        final BeanReference reference = (BeanReference)value;
        value = reference.getBean();
      }
      final Matcher matcher = SpringUtil.KEY_PATTERN.matcher(key);
      if (matcher.matches()) {
        final String beanName = matcher.group(1);
        final String mapKey = matcher.group(2);
        final String propertyName = matcher.group(3);

        if (mapKey == null) {
          if (propertyName == null) {
            if (factory.containsBean(beanName)) {
              BeanDefinition beanDefinition = factory.getBeanDefinition(beanName);
              try {
                ClassLoader classLoader = applicationContext.getClassLoader();
                String beanClassName = beanDefinition.getBeanClassName();
                final Class<?> beanClass = Class.forName(beanClassName, true,
                  classLoader);
                if (Parameter.class.isAssignableFrom(beanClass)) {
                  while (beanDefinition.getOriginatingBeanDefinition() != null) {
                    beanDefinition = beanDefinition.getOriginatingBeanDefinition();
                  }
                  final MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
                  PropertyValue propertyValue = new PropertyValue("value",
                    value);
                  final PropertyValue typeValue = propertyValues.getPropertyValue("type");
                  if (typeValue != null) {
                    try {
                      final Class<?> typeClass;
                      Object typeValueObject = typeValue.getValue();
                      if (typeValueObject instanceof Class<?>) {
                        typeClass = (Class<?>)typeValueObject;
                      } else {
                        final String typeClassName = typeValueObject.toString();
                        typeClass = Class.forName(typeClassName, true,
                          classLoader);
                      }
                      final Object convertedValue = new SimpleTypeConverter().convertIfNecessary(
                        value, typeClass);
                      propertyValue = new PropertyValue("value", convertedValue);
                    } catch (final Throwable e) {
                      LOG.error(
                        "Unable to set " + beanName + ".value=" + value, e);
                    }
                  }
                  propertyValues.addPropertyValue(propertyValue);
                }
              } catch (final ClassNotFoundException e) {
                LOG.error("Unable to set " + beanName + ".value=" + value, e);
              }
            } else if (value != null) {
              createParameterBeanDefinition(factory, beanName, value);
            }
          } else {
            setAttributeValue(factory, beanName, propertyName, value);
            if (LOG.isDebugEnabled()) {
              LOG.debug("Property '" + key + "' set to value [" + value + "]");
            }
          }
        } else if (propertyName == null) {
          setMapValue(factory, key, beanName, mapKey, value);
        } else {
          LOG.error("Invalid syntax unable to set " + key + "=" + value);
        }
      }
    } catch (final BeansException ex) {
      final String msg = "Could not process key '" + key
        + "' in PropertyOverrideConfigurer";
      if (!this.ignoreInvalidKeys) {
        throw new BeanInitializationException(msg, ex);
      }
      if (LOG.isDebugEnabled()) {
        LOG.debug(msg, ex);
      }
    }
  }

  protected void processOverrideAttributes(
    final ConfigurableListableBeanFactory beanFactory,
    final Map<String, Object> attributes) {

    for (final Entry<String, Object> attribute : attributes.entrySet()) {
      final String key = attribute.getKey();
      final Object value = attribute.getValue();
      processOverride(beanFactory, key, value);
    }
  }

  protected void processPlaceholderAttributes(
    final ConfigurableListableBeanFactory beanFactory,
    final Map<String, Object> attributes) throws BeansException {
    final Map<String, Object> attributeMap = new LinkedHashMap<String, Object>();
    for (final Entry<String, Object> entry : attributes.entrySet()) {
      final String key = entry.getKey();
      final Object value = entry.getValue();
      if (!(value instanceof BeanReference)) {
        attributeMap.put(key, value);
      }
    }
    final StringValueResolver valueResolver = new PlaceholderResolvingStringValueResolver(
      placeholderPrefix, placeholderSuffix, ignoreUnresolvablePlaceholders,
      nullValue, attributeMap);
    final BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(
      valueResolver);

    final String[] beanNames = beanFactory.getBeanDefinitionNames();
    for (int i = 0; i < beanNames.length; i++) {
      // Check that we're not parsing our own bean definition,
      // to avoid failing on unresolvable placeholders in properties file
      // locations.
      if (!(beanNames[i].equals(this.beanName) && beanFactory.equals(this.applicationContext))) {
        final BeanDefinition bd = beanFactory.getBeanDefinition(beanNames[i]);
        try {
          visitor.visitBeanDefinition(bd);
        } catch (final BeanDefinitionStoreException ex) {
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
    final Map<String, Object> attributes) throws BeansException {

    final StringValueResolver valueResolver = new PlaceholderResolvingStringValueResolver(
      placeholderPrefix, placeholderSuffix, ignoreUnresolvablePlaceholders,
      nullValue, attributes);
    final BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(
      valueResolver);

    // Check that we're not parsing our own bean definition,
    // to avoid failing on unresolvable placeholders in properties file
    // locations.
    if (!(beanName.equals(this.beanName) && beanFactory.equals(this.applicationContext))) {
      final BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
      try {
        visitor.visitBeanDefinition(bd);
      } catch (final BeanDefinitionStoreException ex) {
        throw new BeanDefinitionStoreException(bd.getResourceDescription(),
          beanName, ex.getMessage());
      }
    }

    // New in Spring 2.5: resolve placeholders in alias target names and aliases
    // as well.
    beanFactory.resolveAliases(valueResolver);
  }

  public void setAttributes(final Map<String, ? extends Object> attributes) {
    this.attributes.clear();
    if (attributes != null) {
      this.attributes.putAll(attributes);
    }
  }

  public void setApplicationContext(final ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public void setBeanName(final String beanName) {
    this.beanName = beanName;
  }

  /**
   * Set whether to ignore invalid keys. Default is "false".
   * <p>
   * If you ignore invalid keys, keys that do not follow the 'beanName.property'
   * format will just be logged as warning. This allows to have arbitrary other
   * keys in a properties file.
   */
  public void setIgnoreInvalidKeys(final boolean ignoreInvalidKeys) {
    this.ignoreInvalidKeys = ignoreInvalidKeys;
  }

  public void setIgnoreUnresolvablePlaceholders(
    final boolean ignoreUnresolvablePlaceholders) {
    this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
  }

  public void setMapValue(
    final ConfigurableListableBeanFactory factory,
    final String key,
    final String beanName,
    final String mapKey,
    final Object value) {
    final BeanDefinition beanDefinition = factory.getBeanDefinition(beanName);
    final String beanClassName = beanDefinition.getBeanClassName();
    try {
      ClassLoader classLoader = applicationContext.getClassLoader();
      final Class<?> beanClass = Class.forName(beanClassName, true, classLoader);
      if (MapFactoryBean.class.isAssignableFrom(beanClass)) {
        final MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
        final PropertyValue sourceMapProperty = propertyValues.getPropertyValue("sourceMap");
        @SuppressWarnings("unchecked")
        final Map<Object, Object> sourceMap = (Map<Object, Object>)sourceMapProperty.getValue();
        boolean found = false;
        for (final Entry<Object, Object> entry : sourceMap.entrySet()) {
          final Object mapEntryKey = entry.getKey();
          if (mapEntryKey instanceof TypedStringValue) {
            final TypedStringValue typedKey = (TypedStringValue)mapEntryKey;
            if (typedKey.getValue().equals(mapKey)) {
              entry.setValue(value);
              found = true;
            }
          }
        }
        if (!found) {
          sourceMap.put(new TypedStringValue(mapKey), value);
        }
      } else if (!TargetBeanFactoryBean.class.isAssignableFrom(beanClass)) {
        LOG.error("Bean class must be a MapFactoryBean, unable to set " + key
          + "=" + value);
      }
    } catch (final ClassNotFoundException e) {
      LOG.error("Unable to set " + key + "=" + value, e);
    }
  }

  public void setNullValue(final String nullValue) {
    this.nullValue = nullValue;
  }

  public void setOrder(final int order) {
    this.order = order;
  }

  public void setPlaceholderPrefix(final String placeholderPrefix) {
    this.placeholderPrefix = placeholderPrefix;
  }

  public void setPlaceholderSuffix(final String placeholderSuffix) {
    this.placeholderSuffix = placeholderSuffix;
  }

}
