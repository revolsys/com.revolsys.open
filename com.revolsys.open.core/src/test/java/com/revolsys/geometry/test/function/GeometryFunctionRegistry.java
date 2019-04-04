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
package com.revolsys.geometry.test.function;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.geometry.model.Geometry;

/**
 * A registry to manage a collection of {@link GeometryFunction}s.
 *
 * @author Martin Davis
 *
 */
public class GeometryFunctionRegistry {
  public static boolean hasGeometryResult(final GeometryFunction func) {
    return Geometry.class.isAssignableFrom(func.getReturnType());
  }

  public static GeometryFunctionRegistry newTestBuilderRegistry() {
    final GeometryFunctionRegistry funcRegistry = new GeometryFunctionRegistry();

    funcRegistry.add(GeometryFunctions.class);
    funcRegistry.add(BufferFunctions.class);
    funcRegistry.add(BufferByUnionFunctions.class);
    funcRegistry.add(ConstructionFunctions.class);
    funcRegistry.add(LinearReferencingFunctions.class);
    funcRegistry.add(LineHandlingFunctions.class);
    funcRegistry.add(NodingFunctions.class);
    funcRegistry.add(PolygonizeFunctions.class);
    funcRegistry.add(PolygonOverlayFunctions.class);
    funcRegistry.add(PrecisionFunctions.class);
    funcRegistry.add(PreparedGeometryFunctions.class);
    funcRegistry.add(SelectionFunctions.class);
    funcRegistry.add(SimplificationFunctions.class);
    funcRegistry.add(AffineTransformationFunctions.class);
    funcRegistry.add(DissolveFunctions.class);
    funcRegistry.add(DistanceFunctions.class);
    funcRegistry.add(JTSFunctions.class);
    funcRegistry.add(CGAlgorithmFunctions.class);
    funcRegistry.add(OverlayFunctions.class);
    funcRegistry.add(OverlayNoSnapFunctions.class);
    funcRegistry.add(SnappingFunctions.class);
    funcRegistry.add(SortingFunctions.class);
    funcRegistry.add(TriangulationFunctions.class);
    funcRegistry.add(TriangleFunctions.class);
    funcRegistry.add(ValidationFunctions.class);

    return funcRegistry;
  }

  private final DoubleKeyMap categorizedFunctions = new DoubleKeyMap();

  private final DoubleKeyMap categorizedGeometryFunctions = new DoubleKeyMap();

  private final List functions = new ArrayList();

  private final Map sortedFunctions = new TreeMap();

  public GeometryFunctionRegistry() {
  }

  public GeometryFunctionRegistry(final Class clz) {
    add(clz);
  }

  /**
   * Adds functions for all the static methods in the given class.
   *
   * @param geomFuncClass
   */
  public void add(final Class geomFuncClass) {
    final List funcs = newFunctions(geomFuncClass);
    // sort list of functions so they appear nicely in the UI list
    Collections.sort(funcs);
    add(funcs);
  }

  public void add(final Collection funcs) {
    for (final Iterator i = funcs.iterator(); i.hasNext();) {
      final GeometryFunction f = (GeometryFunction)i.next();
      add(f);
    }
  }

  /**
   * Adds a function if it does not currently
   * exist in the registry, or replaces the existing one
   * with the same signature.
   *
   * @param func a function
   */
  public void add(final GeometryFunction func) {
    this.functions.add(func);
    this.sortedFunctions.put(func.getName(), func);
    this.categorizedFunctions.put(func.getCategory(), func.getName(), func);
    if (hasGeometryResult(func)) {
      this.categorizedGeometryFunctions.put(func.getCategory(), func.getName(), func);
    }
  }

  /**
   * Adds functions for all the static methods in the given class.
   *
   * @param geomFuncClassname the name of the class to load and extract functions from
   */
  public void add(final String geomFuncClassname) throws ClassNotFoundException {
    Class geomFuncClass = null;
    geomFuncClass = this.getClass().getClassLoader().loadClass(geomFuncClassname);
    add(geomFuncClass);
  }

  /**
   * Finds the first function which matches the given name.
   *
   * @param name
   * @return a matching function, or null
   */
  public GeometryFunction find(final String name) {
    for (final Iterator i = this.functions.iterator(); i.hasNext();) {
      final GeometryFunction func = (GeometryFunction)i.next();
      final String funcName = func.getName();
      if (funcName.equalsIgnoreCase(name)) {
        return func;
      }
    }
    return null;
  }

  /**
   * Finds the first function which matches the given signature.
   *
   * @param name
   * @param paramTypes
   * @return a matching function, or null
   */
  public GeometryFunction find(final String name, final Class[] paramTypes) {
    return null;
  }

  /**
   * Finds the first function which matches the given name and argument count.
   *
   * @param name
   * @return a matching function, or null
   */
  public GeometryFunction find(final String name, final int argCount) {
    for (final Iterator i = this.functions.iterator(); i.hasNext();) {
      final GeometryFunction func = (GeometryFunction)i.next();
      final String funcName = func.getName();
      if (funcName.equalsIgnoreCase(name) && func.getParameterTypes().length == argCount) {
        return func;
      }
    }
    return null;
  }

  public Collection getCategories() {
    return this.categorizedFunctions.keySet();
  }

  public DoubleKeyMap getCategorizedGeometryFunctions() {
    return this.categorizedGeometryFunctions;
  }

  public List getFunctions() {
    return this.functions;
  }

  public Collection getFunctions(final String category) {
    return this.categorizedFunctions.values(category);
  }

  /*
   * int index = functions.indexOf(func); if (index == -1) {
   * sortedFunctions.put(func.getName(), func); } else { functions.set(index,
   * func); } }
   */

  public List getGeometryFunctions() {
    final List funList = new ArrayList();
    for (final Iterator i = this.sortedFunctions.values().iterator(); i.hasNext();) {
      final GeometryFunction fun = (GeometryFunction)i.next();
      if (hasGeometryResult(fun)) {
        funList.add(fun);
      }
    }
    return funList;
  }

  public List getScalarFunctions() {
    final List scalarFun = new ArrayList();
    for (final Iterator i = this.sortedFunctions.values().iterator(); i.hasNext();) {
      final GeometryFunction fun = (GeometryFunction)i.next();
      if (!hasGeometryResult(fun)) {
        scalarFun.add(fun);
      }
    }
    return scalarFun;
  }

  /**
   * Create {@link GeometryFunction}s for all the static
   * methods in the given class
   *
   * @param functionClass
   * @return a list of the functions created
   */
  public List newFunctions(final Class functionClass) {
    final List funcs = new ArrayList();
    final Method[] method = functionClass.getMethods();
    for (final Method element : method) {
      final int mod = element.getModifiers();
      if (Modifier.isStatic(mod) && Modifier.isPublic(mod)) {
        funcs.add(StaticMethodGeometryFunction.newFunction(element));
      }
    }
    return funcs;
  }
}
