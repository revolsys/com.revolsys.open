package com.revolsys.jump.saif;

import org.openjump.core.ui.io.file.FileLayerLoader;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.registry.Registry;

public class SaifFileExtension extends Extension {
  public void configure(final PlugInContext context) throws Exception {
    WorkbenchContext workbenchContext = context.getWorkbenchContext();
    SaifFileLoader loader = new SaifFileLoader(context);
    Registry registry = workbenchContext.getRegistry();
    registry.createEntry(FileLayerLoader.KEY, loader);
  }
}
