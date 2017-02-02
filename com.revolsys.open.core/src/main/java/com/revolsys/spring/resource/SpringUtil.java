package com.revolsys.spring.resource;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;

import com.revolsys.io.FileNames;
import com.revolsys.io.FileUtil;
import com.revolsys.spring.config.AttributesBeanConfigurer;

public class SpringUtil {

  public static final Pattern KEY_PATTERN = Pattern
    .compile("(\\w[\\w\\d]*)(?:(?:\\[([\\w\\d]+)\\])|(?:\\.([\\w\\d]+)))?");

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

  public static File getFileOrCreateTempFile(final Resource resource) {
    try {
      if (resource instanceof FileSystemResource) {
        return resource.getFile();
      } else {
        final String filename = resource.getFilename();
        final String baseName = FileUtil.getBaseName(filename);
        final String fileExtension = FileNames.getFileNameExtension(filename);
        return File.createTempFile(baseName, fileExtension);
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to get file for " + resource, e);
    }
  }

  public static File getOrDownloadFile(final Resource resource) {
    try {
      return resource.getFile();
    } catch (final Throwable e) {
      if (resource.exists()) {
        final String baseName = resource.getBaseName();
        final String fileNameExtension = resource.getFileNameExtension();
        final File file = FileUtil.newTempFile(baseName, fileNameExtension);
        FileUtil.copy(resource.getInputStream(), file);
        return file;
      } else {
        throw new IllegalArgumentException("Cannot get File for resource " + resource, e);
      }
    }
  }
}
