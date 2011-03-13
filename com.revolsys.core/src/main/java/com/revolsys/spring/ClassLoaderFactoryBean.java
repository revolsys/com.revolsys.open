package com.revolsys.spring;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.springframework.beans.factory.FactoryBean;

import com.revolsys.collection.ArrayUtil;
import com.revolsys.io.filter.DirectoryFilenameFilter;
import com.revolsys.io.filter.ExtensionFilenameFilter;

public class ClassLoaderFactoryBean implements FactoryBean<ClassLoader> {

  private ClassLoader classLoader;

  private Collection<URL> urls = new LinkedHashSet<URL>();

  private Collection<URL> mergedUrls = new LinkedHashSet<URL>();

  private Collection<File> libDirectories = new LinkedHashSet<File>();

  public ClassLoader getObject() throws Exception {
    if (classLoader == null) {
      URL[] urls = ArrayUtil.create(mergedUrls);
      Class<? extends ClassLoaderFactoryBean> clazz = getClass();
      ClassLoader parentClassLoader = clazz.getClassLoader();
      classLoader = new URLClassLoader(urls, parentClassLoader);
    }
    return classLoader;
  }

  public Class<ClassLoader> getObjectType() {
    return ClassLoader.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public void setClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  public Collection<URL> getUrls() {
    return urls;
  }

  public void setUrls(Collection<URL> urls) {
    this.urls = urls;
    mergedUrls.addAll(urls);
  }

  public Collection<File> getLibDirectories() {
    return libDirectories;
  }

  public void setLibDirectories(Collection<File> libDirectories) {
    this.libDirectories = libDirectories;
    for (File directory : libDirectories) {
      addJars(directory);
    }
  }

  private void addJars(File directory) {
    if (directory.exists() && directory.isDirectory()) {
      File[] libFiles = directory.listFiles(new ExtensionFilenameFilter("jar",
        "zip"));
      for (File libFile : libFiles) {
        try {
          mergedUrls.add(libFile.toURL());
        } catch (MalformedURLException e) {
        }
      }
      File[] subDirs = directory.listFiles(new DirectoryFilenameFilter());
      for (File subDir : subDirs) {
        addJars(subDir);
      }
    }
  }
}
