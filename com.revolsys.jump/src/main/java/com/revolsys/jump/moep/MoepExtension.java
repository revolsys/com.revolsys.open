package com.revolsys.jump.moep;

import org.openjump.core.ui.io.file.FileLayerLoader;

import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.registry.Registry;

public class MoepExtension extends Extension {
  public void configure(final PlugInContext context) throws Exception {
    WorkbenchContext workbenchContext = context.getWorkbenchContext();
    MoepFileLayerLoader loader = new MoepFileLayerLoader(context);
    Registry registry = workbenchContext.getRegistry();
    registry.createEntry(FileLayerLoader.KEY, loader);
  }
}
