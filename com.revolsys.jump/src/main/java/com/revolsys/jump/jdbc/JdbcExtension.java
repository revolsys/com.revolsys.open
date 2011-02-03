package com.revolsys.jump.jdbc;

import com.vividsolutions.jump.datastore.DataStoreDriver;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.registry.Registry;

public class JdbcExtension extends Extension {

  public void configure(final PlugInContext context) throws Exception {
    WorkbenchContext workbenchContext = context.getWorkbenchContext();
    Registry registry = workbenchContext.getRegistry();
    JdbcDataStoreDriver driver = new JdbcDataStoreDriver(workbenchContext);
    if (driver.isAvailable()) {
      registry.createEntry(DataStoreDriver.REGISTRY_CLASSIFICATION, driver);
    }
  }

}
