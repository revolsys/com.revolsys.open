package com.revolsys.swing.listener;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;

import org.apache.commons.beanutils.MethodUtils;

import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.preferences.PreferencesDialog;
import com.revolsys.util.Property;

public class MacApplicationListenerHandler implements InvocationHandler {

  public static void init(final Object application, final Collection<File> initialFiles) {
    try {
      final InvocationHandler handler = new MacApplicationListenerHandler(initialFiles);
      final Class<?> openFileHandlerClass = Class.forName("com.apple.eawt.OpenFilesHandler");
      final Class<?> openUriHandler = Class.forName("com.apple.eawt.OpenURIHandler");
      final Class<?> preferencesHandler = Class.forName("com.apple.eawt.PreferencesHandler");
      final ClassLoader classLoader = MacApplicationListenerHandler.class.getClassLoader();
      final Object listener = Proxy.newProxyInstance(classLoader, new Class[] {
        openFileHandlerClass, openUriHandler, preferencesHandler
      }, handler);
      MethodUtils.invokeMethod(application, "setOpenFileHandler", listener);
      MethodUtils.invokeMethod(application, "setOpenURIHandler", listener);
      MethodUtils.invokeMethod(application, "setPreferencesHandler", listener);
      System.setProperty("com.sun.media.jai.disableMediaLib", "true");
    } catch (final Exception e) {
      e.printStackTrace(System.out);
    }
  }

  private final Collection<File> initialFiles;

  public MacApplicationListenerHandler(final Collection<File> initialFiles) {
    this.initialFiles = initialFiles;
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args)
    throws Throwable {
    if (method.getName().equals("openFiles")) {
      final Object event = args[0];
      openFiles(event);
    } else if (method.getName().equals("handlePreferences")) {
      final PreferencesDialog preferencesDialog = new PreferencesDialog();
      preferencesDialog.showPanel();
    }
    return null;
  }

  private void openFiles(final Object event) {
    final List<File> files = Property.getSimple(event, "files");
    final LayerGroup layerGroup = Project.get();
    if (layerGroup == null) {
      this.initialFiles.addAll(files);
    } else {
      layerGroup.openFiles(files);
    }
  }

}
