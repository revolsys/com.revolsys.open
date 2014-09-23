/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.jtstest.function;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Assert;

import com.revolsys.beans.Classes;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jtstest.testrunner.StringUtil;

/**
 * A {@link GeometryFunction} which calls a static
 * {@link Method}.
 * 
 * @author Martin Davis
 *
 */
public class StaticMethodGeometryFunction extends BaseGeometryFunction {
  private static final String FUNCTIONS_SUFFIX = "Functions";

  private static final String PARAMETERS_SUFFIX = "Parameters";

  private static final String DESCRIPTION_SUFFIX = "Description";

  /**
   * Creates an arg array which includes the target geometry as the first argument
   * 
   * @param g
   * @param arg
   * @return
   */
  private static Object[] createFullArgs(final Geometry g, final Object[] arg) {
    int fullArgLen = 1;
    if (arg != null) {
      fullArgLen = arg.length + 1;
    }
    final Object[] fullArg = new Object[fullArgLen];
    fullArg[0] = g;
    for (int i = 1; i < fullArgLen; i++) {
      fullArg[i] = arg[i - 1];
    }
    return fullArg;
  }

  public static StaticMethodGeometryFunction createFunction(final Method method) {
    Assert.assertTrue(Geometry.class.isAssignableFrom((method.getParameterTypes())[0]));

    final Class clz = method.getDeclaringClass();

    final String category = extractCategory(Classes.className(clz));
    final String funcName = method.getName();
    final String description = extractDescription(method);
    final String[] paramNames = extractParamNames(method);
    final Class[] paramTypes = extractParamTypes(method);
    final Class returnType = method.getReturnType();

    return new StaticMethodGeometryFunction(category, funcName, description,
      paramNames, paramTypes, returnType, method);
  }

  private static String extractCategory(final String className) {
    final String trim = StringUtil.removeFromEnd(className, FUNCTIONS_SUFFIX);
    return trim;
  }

  private static String extractDescription(final Method method) {
    // try to get names from predefined ones first
    final String paramsName = method.getName() + DESCRIPTION_SUFFIX;
    return StaticMethodGeometryFunction.getStringClassField(method.getDeclaringClass(), paramsName);
  }

  /**
   * Java doesn't permit accessing the original code parameter names, unfortunately.
   * 
   * @param method
   * @return
   */
  private static String[] extractParamNames(final Method method) {
    // try to get names from predefined ones first
    final String paramsName = method.getName() + PARAMETERS_SUFFIX;
    final String[] codeName = StaticMethodGeometryFunction.getStringArrayClassField(
      method.getDeclaringClass(), paramsName);
    if (codeName != null) {
      return codeName;
    }

    // Synthesize default names
    final String[] name = new String[method.getParameterTypes().length - 1];
    // Skip first parameter - it is the target geometry
    for (int i = 1; i < name.length; i++) {
      name[i] = "arg" + i;
    }
    return name;
  }

  private static Class[] extractParamTypes(final Method method) {
    final Class[] methodParamTypes = method.getParameterTypes();
    final Class[] types = new Class[methodParamTypes.length - 1];
    for (int i = 1; i < methodParamTypes.length; i++) {
      types[i - 1] = methodParamTypes[i];
    }
    return types;
  }

  public static String getClassname(final Class javaClass) {
    final String jClassName = javaClass.getName();
    final int lastDotPos = jClassName.lastIndexOf(".");
    return jClassName.substring(lastDotPos + 1, jClassName.length());
  }

  private static String invocationErrMsg(final InvocationTargetException ex) {
    final Throwable targetEx = ex.getTargetException();
    final String msg = getClassname(targetEx.getClass()) + ": "
      + targetEx.getMessage();
    return msg;
  }

  public static Object invoke(final Method method, final Object target,
    final Object[] args) {
    Object result;
    try {
      result = method.invoke(target, args);
    } catch (final InvocationTargetException ex) {
      final Throwable t = ex.getCause();
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      }
      throw new RuntimeException(invocationErrMsg(ex));
    } catch (final Exception ex) {
      System.out.println(ex.getMessage());
      throw new RuntimeException(ex.getMessage());
    }
    return result;
  }

  private final Method method;

  public StaticMethodGeometryFunction(final String category, final String name,
    final String description, final String[] parameterNames,
    final Class[] parameterTypes, final Class returnType, final Method method) {
    super(category, name, description, parameterNames, parameterTypes,
      returnType);
    this.method = method;
  }

  @Override
  public Object invoke(final Geometry g, final Object[] arg) {
    return invoke(method, null, createFullArgs(g, arg));
  }

  public static Object dynamicCall(final String clzName,
    final String methodName, final Class[] methodParamTypes,
    final Object[] methodArgs) throws ClassNotFoundException,
    SecurityException, NoSuchMethodException, IllegalArgumentException,
    InstantiationException, IllegalAccessException, InvocationTargetException {
    final Class clz = Class.forName(clzName);
  
    final Class[] constParTypes = new Class[] {
      String.class, String.class
    };
    final Constructor constr = clz.getConstructor(new Class[0]);
    final Object dummyto = constr.newInstance(new Object[0]);
  
    final Method meth = clz.getMethod(methodName, methodParamTypes);
    final Object result = meth.invoke(dummyto, methodArgs);
    return result;
  }

  public static String[] getStringArrayClassField(final Class clz,
    final String name) {
    try {
      final Field field = clz.getField(name);
      final String[] str = (String[])field.get(null);
      return str;
    } catch (final NoSuchFieldException ex) {
    } catch (final IllegalAccessException ex) {
    }
    return null;
  }

  public static String getStringClassField(final Class clz, final String name) {
    try {
      final Field field = clz.getField(name);
      final String str = (String)field.get(null);
      return str;
    } catch (final NoSuchFieldException ex) {
    } catch (final IllegalAccessException ex) {
    }
    return null;
  }
}
