/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.revolsys.jndi;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Simple implementation of a JNDI naming context. Only supports binding plain
 * Objects to String names. Mainly for test environments, but also usable for
 * standalone applications.
 * <p>
 * This class is not intended for direct usage by applications, although it can
 * be used for example to override JndiTemplate's
 * <code>createInitialContext</code> method in unit tests. Typically,
 * SimpleNamingContextBuilder will be used to set up a JVM-level JNDI
 * environment.
 * 
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.mock.jndi.SimpleNamingContextBuilder
 * @see org.springframework.jndi.JndiTemplate#createInitialContext
 */
public class SimpleNamingContext implements Context {

  private static abstract class AbstractNamingEnumeration<T> implements
    NamingEnumeration<T> {

    private final Iterator<T> iterator;

    private AbstractNamingEnumeration(final SimpleNamingContext context,
      String proot) throws NamingException {
      if (!"".equals(proot) && !proot.endsWith("/")) {
        proot = proot + "/";
      }
      final String root = context.root + proot;
      final Map<String, T> contents = new HashMap<String, T>();
      for (final String boundName : context.boundObjects.keySet()) {
        if (boundName.startsWith(root)) {
          final int startIndex = root.length();
          final int endIndex = boundName.indexOf('/', startIndex);
          final String strippedName = (endIndex != -1 ? boundName.substring(
            startIndex, endIndex) : boundName.substring(startIndex));
          if (!contents.containsKey(strippedName)) {
            try {
              contents.put(
                strippedName,
                createObject(strippedName, context.lookup(proot + strippedName)));
            } catch (final NameNotFoundException ex) {
              // cannot happen
            }
          }
        }
      }
      if (contents.size() == 0) {
        throw new NamingException("Invalid root: [" + context.root + proot
          + "]");
      }
      this.iterator = contents.values().iterator();
    }

    public void close() {
    }

    protected abstract T createObject(String strippedName, Object obj);

    public boolean hasMore() {
      return this.iterator.hasNext();
    }

    public boolean hasMoreElements() {
      return this.iterator.hasNext();
    }

    public T next() {
      return this.iterator.next();
    }

    public T nextElement() {
      return this.iterator.next();
    }
  }

  private static class BindingEnumeration extends
    AbstractNamingEnumeration<Binding> {

    private BindingEnumeration(final SimpleNamingContext context,
      final String root) throws NamingException {
      super(context, root);
    }

    @Override
    protected Binding createObject(final String strippedName, final Object obj) {
      return new Binding(strippedName, obj);
    }
  }

  private static class NameClassPairEnumeration extends
    AbstractNamingEnumeration<NameClassPair> {

    private NameClassPairEnumeration(final SimpleNamingContext context,
      final String root) throws NamingException {
      super(context, root);
    }

    @Override
    protected NameClassPair createObject(
      final String strippedName,
      final Object obj) {
      return new NameClassPair(strippedName, obj.getClass().getName());
    }
  }

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final String root;

  private final Hashtable<String, Object> boundObjects;

  private final Hashtable<String, Object> environment = new Hashtable<String, Object>();

  // Actual implementations of Context methods follow

  /**
   * Create a new naming context.
   */
  public SimpleNamingContext() {
    this("");
  }

  /**
   * Create a new naming context with the given naming root.
   */
  public SimpleNamingContext(final String root) {
    this.root = root;
    this.boundObjects = new Hashtable<String, Object>();
  }

  /**
   * Create a new naming context with the given naming root, the given
   * name/object map, and the JNDI environment entries.
   */
  public SimpleNamingContext(final String root,
    final Hashtable<String, Object> boundObjects,
    final Hashtable<String, Object> env) {
    this.root = root;
    this.boundObjects = boundObjects;
    if (env != null) {
      this.environment.putAll(env);
    }
  }

  public Object addToEnvironment(final String propName, final Object propVal) {
    return this.environment.put(propName, propVal);
  }

  public void bind(final Name name, final Object obj) throws NamingException {
    throw new OperationNotSupportedException(
      "SimpleNamingContext does not support [javax.naming.Name]");
  }

  /**
   * Bind the given object to the given name. Note: Not intended for direct use
   * by applications if setting up a JVM-level JNDI environment. Use
   * SimpleNamingContextBuilder to set up JNDI bindings then.
   * 
   * @see org.springframework.mock.jndi.SimpleNamingContextBuilder#bind
   */
  public void bind(final String name, final Object obj) {
    if (logger.isInfoEnabled()) {
      logger.info("Static JNDI binding: [" + this.root + name + "] = [" + obj
        + "]");
    }
    this.boundObjects.put(this.root + name, obj);
  }

  public void close() {
  }

  public Name composeName(final Name name, final Name prefix)
    throws NamingException {
    throw new OperationNotSupportedException(
      "SimpleNamingContext does not support [javax.naming.Name]");
  }

  public String composeName(final String name, final String prefix) {
    return prefix + name;
  }

  public Context createSubcontext(final Name name) throws NamingException {
    throw new OperationNotSupportedException(
      "SimpleNamingContext does not support [javax.naming.Name]");
  }

  public Context createSubcontext(final String name) {
    String subcontextName = this.root + name;
    if (!subcontextName.endsWith("/")) {
      subcontextName += "/";
    }
    final Context subcontext = new SimpleNamingContext(subcontextName,
      this.boundObjects, this.environment);
    bind(name, subcontext);
    return subcontext;
  }

  public void destroySubcontext(final Name name) throws NamingException {
    throw new OperationNotSupportedException(
      "SimpleNamingContext does not support [javax.naming.Name]");
  }

  public void destroySubcontext(final String name) {
    unbind(name);
  }

  public Hashtable<String, Object> getEnvironment() {
    return this.environment;
  }

  public String getNameInNamespace() throws NamingException {
    throw new OperationNotSupportedException(
      "SimpleNamingContext does not support [javax.naming.Name]");
  }

  // Unsupported methods follow: no support for javax.naming.Name

  public NameParser getNameParser(final Name name) throws NamingException {
    throw new OperationNotSupportedException(
      "SimpleNamingContext does not support [javax.naming.Name]");
  }

  public NameParser getNameParser(final String name) throws NamingException {
    throw new OperationNotSupportedException(
      "SimpleNamingContext does not support [javax.naming.Name]");
  }

  public NamingEnumeration<NameClassPair> list(final Name name)
    throws NamingException {
    throw new OperationNotSupportedException(
      "SimpleNamingContext does not support [javax.naming.Name]");
  }

  public NamingEnumeration<NameClassPair> list(final String root)
    throws NamingException {
    if (logger.isDebugEnabled()) {
      logger.debug("Listing name/class pairs under [" + root + "]");
    }
    return new NameClassPairEnumeration(this, root);
  }

  public NamingEnumeration<Binding> listBindings(final Name name)
    throws NamingException {
    throw new OperationNotSupportedException(
      "SimpleNamingContext does not support [javax.naming.Name]");
  }

  public NamingEnumeration<Binding> listBindings(final String root)
    throws NamingException {
    if (logger.isDebugEnabled()) {
      logger.debug("Listing bindings under [" + root + "]");
    }
    return new BindingEnumeration(this, root);
  }

  public Object lookup(final Name name) throws NamingException {
    throw new OperationNotSupportedException(
      "SimpleNamingContext does not support [javax.naming.Name]");
  }

  /**
   * Look up the object with the given name.
   * <p>
   * Note: Not intended for direct use by applications. Will be used by any
   * standard InitialContext JNDI lookups.
   * 
   * @throws javax.naming.NameNotFoundException if the object could not be found
   */
  public Object lookup(final String lookupName) throws NameNotFoundException {
    String name = this.root + lookupName;
    if (logger.isDebugEnabled()) {
      logger.debug("Static JNDI lookup: [" + name + "]");
    }
    if ("".equals(name)) {
      return new SimpleNamingContext(this.root, this.boundObjects,
        this.environment);
    }
    final Object found = this.boundObjects.get(name);
    if (found == null) {
      if (!name.endsWith("/")) {
        name = name + "/";
      }
      for (final String boundName : this.boundObjects.keySet()) {
        if (boundName.startsWith(name)) {
          return new SimpleNamingContext(name, this.boundObjects,
            this.environment);
        }
      }
      throw new NameNotFoundException("Name ["
        + this.root
        + lookupName
        + "] not bound; "
        + this.boundObjects.size()
        + " bindings: ["
        + StringUtils.collectionToDelimitedString(this.boundObjects.keySet(),
          ",") + "]");
    }
    return found;
  }

  public Object lookupLink(final Name name) throws NamingException {
    throw new OperationNotSupportedException(
      "SimpleNamingContext does not support [javax.naming.Name]");
  }

  public Object lookupLink(final String name) throws NameNotFoundException {
    return lookup(name);
  }

  public void rebind(final Name name, final Object obj) throws NamingException {
    throw new OperationNotSupportedException(
      "SimpleNamingContext does not support [javax.naming.Name]");
  }

  public void rebind(final String name, final Object obj) {
    bind(name, obj);
  }

  public Object removeFromEnvironment(final String propName) {
    return this.environment.remove(propName);
  }

  public void rename(final Name oldName, final Name newName)
    throws NamingException {
    throw new OperationNotSupportedException(
      "SimpleNamingContext does not support [javax.naming.Name]");
  }

  public void rename(final String oldName, final String newName)
    throws NameNotFoundException {
    final Object obj = lookup(oldName);
    unbind(oldName);
    bind(newName, obj);
  }

  public void unbind(final Name name) throws NamingException {
    throw new OperationNotSupportedException(
      "SimpleNamingContext does not support [javax.naming.Name]");
  }

  public void unbind(final String name) {
    if (logger.isInfoEnabled()) {
      logger.info("Static JNDI remove: [" + this.root + name + "]");
    }
    this.boundObjects.remove(this.root + name);
  }

}
