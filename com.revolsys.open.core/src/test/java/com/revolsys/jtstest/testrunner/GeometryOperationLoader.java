package com.revolsys.jtstest.testrunner;

import com.revolsys.jtstest.geomop.GeometryOperation;

/**
 * Loads a GeometryOperation class
 *
 * @author Martin Davis
 * @version 1.7
 */
public class GeometryOperationLoader {
  /**
   * If anything bad happens while creating the geometry operation, just print a message and fail
   * @param classLoader
   * @param geomOpClassname
   */
  public static GeometryOperation createGeometryOperation(
    final ClassLoader classLoader, final String geomOpClassname) {
    Class geomOpClass = null;
    try {
      geomOpClass = classLoader.loadClass(geomOpClassname);
    } catch (final ClassNotFoundException ex) {
      System.out.println("ERROR: Class not found - " + geomOpClassname);
      return null;
    }
    try {
      final GeometryOperation geometryOp = (GeometryOperation)geomOpClass.newInstance();
      return geometryOp;
    } catch (final Exception ex) {
      System.out.println(ex.getMessage());
      return null;
    }
  }

}
