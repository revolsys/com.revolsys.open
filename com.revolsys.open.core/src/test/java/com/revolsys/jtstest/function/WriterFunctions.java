package com.revolsys.jtstest.function;

import java.lang.reflect.InvocationTargetException;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.io.WKBWriter;
import com.revolsys.jtstest.util.ClassUtil;

public class WriterFunctions {
  public static String writeGML(final Geometry geom) {
    if (geom == null) {
      return "";
    }
    return "";// (new GMLWriter()).write(geom);
  }

  public static String writeKML(final Geometry geom) {
    if (geom == null) {
      return "";
    }
    // final KMLWriter writer = new KMLWriter();
    return "";// writer.write(geom);
  }

  public static String writeOra(final Geometry g) throws SecurityException,
    IllegalArgumentException, ClassNotFoundException, NoSuchMethodException,
    InstantiationException, IllegalAccessException, InvocationTargetException {
    if (g == null) {
      return "";
    }
    // call dynamically to avoid dependency on OraWriter
    final String sql = (String)ClassUtil.dynamicCall(
      "com.revolsys.jts.io.oracle.OraWriter", "writeSQL", new Class[] {
        Geometry.class
      }, new Object[] {
        g
      });
    return sql;
    // return (new OraWriter(null)).writeSQL(g);
  }

  public static String writeWKB(final Geometry g) {
    if (g == null) {
      return "";
    }
    return WKBWriter.toHex((new WKBWriter().write(g)));
  }

}
