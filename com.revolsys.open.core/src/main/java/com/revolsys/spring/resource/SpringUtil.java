package com.revolsys.spring.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Pattern;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.support.GenericApplicationContext;

import com.revolsys.io.FileNames;
import com.revolsys.io.FileUtil;
import com.revolsys.spring.config.AttributesBeanConfigurer;
import com.revolsys.util.Property;
import com.revolsys.util.WrappedException;

public class SpringUtil {

  public static final Pattern KEY_PATTERN = Pattern
    .compile("(\\w[\\w\\d]*)(?:(?:\\[([\\w\\d]+)\\])|(?:\\.([\\w\\d]+)))?");

  private static final ThreadLocal<Resource> BASE_RESOURCE = new ThreadLocal<Resource>();

  public static Resource addExtension(final Resource resource, final String extension) {
    final String fileName = getFileName(resource);
    final String newFileName = fileName + "." + extension;
    return resource.createRelative(newFileName);
  }

  public static void close(final ConfigurableApplicationContext applicationContext) {
    if (applicationContext != null) {
      if (applicationContext.isActive()) {
        applicationContext.close();
      }
    }
  }

  public static Resource convertSpringResource(
    final org.springframework.core.io.Resource resource) {
    if (resource instanceof org.springframework.core.io.ClassPathResource) {
      final org.springframework.core.io.ClassPathResource springResource = (org.springframework.core.io.ClassPathResource)resource;
      return new ClassPathResource(springResource.getPath(), springResource.getClassLoader());
    } else if (resource instanceof org.springframework.core.io.FileSystemResource) {
      final org.springframework.core.io.FileSystemResource springResource = (org.springframework.core.io.FileSystemResource)resource;
      return new FileSystemResource(springResource.getFile());
    } else if (resource instanceof org.springframework.core.io.PathResource) {
      final org.springframework.core.io.PathResource springResource = (org.springframework.core.io.PathResource)resource;
      return new PathResource(springResource.getPath());
    } else if (resource instanceof org.springframework.core.io.UrlResource) {
      final org.springframework.core.io.UrlResource springResource = (org.springframework.core.io.UrlResource)resource;
      try {
        return new UrlResource(springResource.getURL());
      } catch (final IOException e) {
        throw new WrappedException(e);
      }
    }
    throw new IllegalArgumentException();
  }

  public static void copy(final InputStream in, final Resource target) {
    try {
      if (target instanceof FileSystemResource) {
        final FileSystemResource fileResource = (FileSystemResource)target;
        final File file = fileResource.getFile();
        final File parent = file.getParentFile();
        if (!parent.exists()) {
          parent.mkdirs();
        }
      }
      final OutputStream out = target.newBufferedOutputStream();
      try {
        FileUtil.copy(in, out);
      } finally {
        FileUtil.closeSilent(out);
      }
    } finally {
      FileUtil.closeSilent(in);
    }
  }

  public static void copy(final Resource source, final Resource target) {
    final InputStream in = source.getInputStream();
    copy(in, target);
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

  public static String getBaseName(final Resource resource) {
    return FileUtil.getBaseName(getFileName(resource));
  }

  public static Resource getBaseResource() {
    final Resource baseResource = SpringUtil.BASE_RESOURCE.get();
    if (baseResource == null) {
      return new FileSystemResource(FileUtil.getCurrentDirectory());
    } else {
      return baseResource;
    }
  }

  public static Resource getBaseResource(final String childPath) {
    final Resource baseResource = getBaseResource();
    return getResource(baseResource, childPath);
  }

  public static BufferedReader getBufferedReader(final Resource resource) {
    final Reader in = resource.newReader();
    return new BufferedReader(in);
  }

  public static String getFileName(final Resource resource) {
    if (resource instanceof UrlResource) {
      final UrlResource urlResoure = (UrlResource)resource;
      return urlResoure.getURL().getPath();
    }
    return resource.getFilename();
  }

  public static String getFileNameExtension(final Resource resource) {
    final String fileName = getFileName(resource);
    return FileUtil.getFileNameExtension(fileName);
  }

  public static File getFileOrCreateTempFile(final Resource resource) {
    try {
      if (resource instanceof FileSystemResource) {
        return resource.getFile();
      } else {
        final String filename = getFileName(resource);
        final String baseName = FileUtil.getBaseName(filename);
        final String fileExtension = FileNames.getFileNameExtension(filename);
        return File.createTempFile(baseName, fileExtension);
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to get file for " + resource, e);
    }
  }

  public static long getLastModified(final Resource resource) {
    try {
      return resource.lastModified();
    } catch (final IOException e) {
      return Long.MAX_VALUE;
    }
  }

  public static File getOrDownloadFile(final Resource resource) {
    try {
      return resource.getFile();
    } catch (final Throwable e) {
      if (resource.exists()) {
        final String baseName = getBaseName(resource);
        final String fileNameExtension = getFileNameExtension(resource);
        final File file = FileUtil.createTempFile(baseName, fileNameExtension);
        FileUtil.copy(resource.getInputStream(), file);
        return file;
      } else {
        throw new IllegalArgumentException("Cannot get File for resource " + resource, e);
      }
    }
  }

  public static PrintWriter getPrintWriter(final Resource resource) {
    final Writer writer = resource.newWriter();
    return new PrintWriter(writer);
  }

  public static Resource getResource(final File directory, final String fileName) {
    final File file = FileUtil.getFile(directory, fileName);
    return new FileSystemResource(file);
  }

  public static Resource getResource(final Resource resource, final CharSequence childPath) {
    if (resource instanceof FileSystemResource) {
      final FileSystemResource fileResource = (FileSystemResource)resource;
      final File file = fileResource.getFile();
      final File childFile = new File(file, childPath.toString());
      return new FileSystemResource(childFile);
    } else {
      return resource.createRelative(childPath.toString());
    }
  }

  public static com.revolsys.spring.resource.Resource getResource(final String location) {
    if (Property.hasValue(location)) {
      if (location.charAt(0) == '/' || location.length() > 1 && location.charAt(1) == ':') {
        return new PathResource(location);
      } else {
        return new UrlResource(location);
      }
    }
    return null;
  }

  public static Resource setBaseResource(final Resource baseResource) {
    final Resource oldResource = SpringUtil.BASE_RESOURCE.get();
    SpringUtil.BASE_RESOURCE.set(baseResource);
    return oldResource;
  }
}
