package com.revolsys.spring;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.springframework.beans.factory.FactoryBean;

import com.revolsys.io.FileUtil;
import com.revolsys.io.filter.DirectoryFilenameFilter;
import com.revolsys.io.filter.ExtensionFilenameFilter;

public class ClassLoaderFactoryBean implements FactoryBean<ClassLoader> {

  private static final ExtensionFilenameFilter JAR_FILTER = new ExtensionFilenameFilter(
    "jar", "zip");

  public static void addJars(final Collection<URL> urls, final File directory) {
    if (directory.exists() && directory.isDirectory()) {
      final File[] libFiles = directory.listFiles(JAR_FILTER);
      for (final File libFile : libFiles) {
        urls.add(FileUtil.toUrl(libFile));
      }
      final File[] subDirs = directory.listFiles(new DirectoryFilenameFilter());
      for (final File subDir : subDirs) {
        addJars(urls, subDir);
      }
    }
  }

  public static URLClassLoader createClassLoader(
    final ClassLoader parentClassLoader,
    final Collection<URL> urls) {
    URL[] urlArray = new URL[urls.size()];
    urlArray = urls.toArray(urlArray);
    return new URLClassLoader(urlArray, parentClassLoader);
  }

  public static URLClassLoader createClassLoader(
    final ClassLoader parentClassLoader,
    final File file) {
    Collection<URL> urls = new LinkedHashSet<URL>();
    if (file.isDirectory()) {
      addJars(urls, file);
    } else if (JAR_FILTER.accept(file.getParentFile(), file.getName())) {
      urls.add(FileUtil.toUrl(file));
    }
    return createClassLoader(parentClassLoader, urls);
  }

  private ClassLoader classLoader;

  private Collection<URL> urls = new LinkedHashSet<URL>();

  private final Collection<URL> mergedUrls = new LinkedHashSet<URL>();

  private Collection<File> libDirectories = new LinkedHashSet<File>();

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public Collection<File> getLibDirectories() {
    return libDirectories;
  }

  public ClassLoader getObject() throws Exception {
    if (classLoader == null) {
      final Class<? extends ClassLoaderFactoryBean> clazz = getClass();
      final ClassLoader parentClassLoader = clazz.getClassLoader();
      classLoader = createClassLoader(parentClassLoader, mergedUrls);
    }
    return classLoader;
  }

  public Class<ClassLoader> getObjectType() {
    return ClassLoader.class;
  }

  public Collection<URL> getUrls() {
    return urls;
  }

  public boolean isSingleton() {
    return true;
  }

  public void setClassLoader(final ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  public void setLibDirectories(final Collection<File> libDirectories) {
    this.libDirectories = libDirectories;
    for (final File directory : libDirectories) {
      addJars(mergedUrls, directory);
    }
  }

  public void setUrls(final Collection<URL> urls) {
    this.urls = urls;
    mergedUrls.addAll(urls);
  }
}
