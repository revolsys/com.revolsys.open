package com.revolsys.swing.tree.node;

import java.awt.TextField;

import com.revolsys.collection.map.MapEx;
import com.revolsys.record.io.format.esri.rest.ArcGisRestCatalog;
import com.revolsys.swing.Borders;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.TreeNodes;
import com.revolsys.webservice.WebServiceConnectionManager;
import com.revolsys.webservice.WebServiceConnectionRegistry;

public class WebServiceConnectionTrees {

  static {
    final MenuFactory menu = MenuFactory.getMenu(WebServiceConnectionRegistry.class);
    TreeNodes.addMenuItemNodeValue(menu, "default", "Add ArcGIS REST Connection", "world:add",
      WebServiceConnectionTrees::addArcGISRestConnection);
  }

  private static void addArcGISRestConnection(final WebServiceConnectionRegistry registry) {
    final ValueField panel = new ValueField();
    panel.setTitle("Add Web Service Connection");
    Borders.titled(panel, "Web Service Connection");
    SwingUtil.addLabel(panel, "Name");
    final TextField nameField = new TextField(20);
    panel.add(nameField);

    SwingUtil.addLabel(panel, "Service URL");
    final TextField urlField = new TextField(50);
    panel.add(urlField);

    GroupLayouts.makeColumns(panel, 2, true);
    panel.showDialog();
    if (panel.isSaved()) {
      final String url = urlField.getText();
      if (url != null) {
        final ArcGisRestCatalog webService = new ArcGisRestCatalog(url);
        final String name = nameField.getText();
        webService.setName(name);
        final MapEx config = webService.toMap();
        registry.newConnection(config);
      }
    }
  }

  public static BaseTreeNode newWebServiceConnectionsTreeNode() {
    final WebServiceConnectionManager webServicesConnectionManager = WebServiceConnectionManager
      .get();
    final BaseTreeNode webServices = BaseTreeNode.newTreeNode(webServicesConnectionManager);
    webServices.setOpen(true);
    return webServices;
  }

}
