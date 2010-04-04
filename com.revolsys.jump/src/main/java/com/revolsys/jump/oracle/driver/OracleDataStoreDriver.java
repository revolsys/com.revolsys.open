package com.revolsys.jump.oracle.driver;

import com.vividsolutions.jump.datastore.DataStoreConnection;
import com.vividsolutions.jump.datastore.DataStoreDriver;
import com.vividsolutions.jump.parameter.ParameterList;
import com.vividsolutions.jump.parameter.ParameterListSchema;

public class OracleDataStoreDriver implements DataStoreDriver {
  public static final String PASSWORD = "password";

  public static final String USER = "user";

  public static final String DB = "db";

  public static final String PORT = "port";

  public static final String HOST = "host";

  public static final String SCHEMA = "schema";

  public static final String URL = "url";

  private static final String[] PARAM_NAMES = {
    URL, HOST, PORT, DB, SCHEMA, USER, PASSWORD
  };

  private static final Class<?>[] PARAM_TYPES = {
    String.class, String.class, String.class, String.class, String.class,
    String.class, String.class, String.class,
  };

  private static final ParameterListSchema PARAMS_SCHEMA = new ParameterListSchema(
    PARAM_NAMES, PARAM_TYPES);

  public DataStoreConnection createConnection(
    final ParameterList params)
    throws Exception {
    return new OracleDataStoreConnection(params);
  }

  public String getName() {
    return "Oracle";
  }

  public ParameterListSchema getParameterListSchema() {
    return PARAMS_SCHEMA;
  }

  public boolean isAdHocQuerySupported() {
    return false;
  }

  public boolean isAvailable() {
    try {
      Class.forName("oracle.jdbc.driver.OracleDriver");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
