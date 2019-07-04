package com.revolsys.swing.tree.node;

import org.jeometry.common.io.PathName;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.connection.ConnectionRegistry;
import com.revolsys.gis.wms.WmsClient;
import com.revolsys.record.io.format.esri.rest.ArcGisRestCatalog;
import com.revolsys.record.io.format.mapguide.MapGuideWebService;
import com.revolsys.swing.Borders;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.PasswordField;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.BaseTreeNode;
import com.revolsys.swing.tree.TreeNodes;
import com.revolsys.swing.tree.node.file.FolderConnectionsTrees;
import com.revolsys.util.PasswordUtil;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;
import com.revolsys.webservice.WebService;
import com.revolsys.webservice.WebServiceConnection;
import com.revolsys.webservice.WebServiceConnectionManager;
import com.revolsys.webservice.WebServiceConnectionRegistry;
import com.revolsys.webservice.WebServiceResource;

public class WebServiceConnectionTrees extends ConnectionManagerTrees {

  static {
    MenuFactory.addMenuInitializer(WebServiceConnectionRegistry.class, (menu) -> {

      TreeNodes.addMenuItemNodeValue(menu, "default", 0, "Add ArcGIS REST Connection", "world:add",
        ConnectionRegistry::isEditable, WebServiceConnectionTrees::addArcGISRestConnection);

      TreeNodes.addMenuItemNodeValue(menu, "default", 1, "Add OGC WMS Connection", "world:add",
        ConnectionRegistry::isEditable, WebServiceConnectionTrees::addOgcWmsConnection);

      TreeNodes.addMenuItemNodeValue(menu, "default", 2, "Add MapGuide Connection", "world:add",
        ConnectionRegistry::isEditable, WebServiceConnectionTrees::addMapGuideRestConnection);

      TreeNodes.addMenuItemNodeValue(menu, "default", 3, "Import Connection...", "world:import",
        WebServiceConnectionRegistry::isEditable, FolderConnectionsTrees::importConnection);
    });

    // WebServiceConnection
    MenuFactory.addMenuInitializer(WebServiceConnection.class, (menu) -> {
      TreeNodes.addMenuItemNodeValue(menu, "default", 0, "Edit Connection", "world:edit",
        WebServiceConnection::isEditable, WebServiceConnectionTrees::editConnection);
      TreeNodes.<WebServiceConnection> addMenuItemNodeValue(menu, "default", 1, "Export Connection",
        "world:export", ConnectionManagerTrees::exportConnection);
    });
  }

  private static void addArcGISRestConnection(final WebServiceConnectionRegistry registry) {
    addWebServiceConnection(registry, ArcGisRestCatalog.J_TYPE);
  }

  private static void addMapGuideRestConnection(final WebServiceConnectionRegistry registry) {
    addWebServiceConnection(registry, MapGuideWebService.J_TYPE);
  }

  private static void addOgcWmsConnection(final WebServiceConnectionRegistry registry) {
    addWebServiceConnection(registry, WmsClient.J_TYPE);
  }

  @SuppressWarnings("deprecation")
  private static void addWebServiceConnection(final WebServiceConnectionRegistry registry,
    final String type) {
    final ValueField panel = new ValueField();
    panel.setTitle("Add Web Service Connection");
    Borders.titled(panel, "Web Service Connection");
    SwingUtil.addLabel(panel, "Name");
    final TextField nameField = new TextField(20);
    panel.add(nameField);

    SwingUtil.addLabel(panel, "Service URL");
    final TextField urlField = new TextField(50);
    panel.add(urlField);

    SwingUtil.addLabel(panel, "Username");
    final TextField usernameField = new TextField(30);
    panel.add(usernameField);

    SwingUtil.addLabel(panel, "Password");
    final PasswordField passwordField = new PasswordField(30);
    panel.add(passwordField);

    GroupLayouts.makeColumns(panel, 2, true);
    panel.showDialog();
    if (panel.isSaved()) {
      final String url = urlField.getText();
      if (url != null) {
        final String name = nameField.getText();
        final String username = usernameField.getText();
        final String password = passwordField.getText();
        final MapEx config = new LinkedHashMapEx();
        config.put("j:type", type);
        config.put("name", name);
        config.put("serviceUrl", url);
        config.put("username", username);
        config.put("password", PasswordUtil.encrypt(password));
        registry.newConnection(config);
      }
    }
  }

  @SuppressWarnings("deprecation")
  private static void editConnection(final WebServiceConnection connection) {
    final WebServiceConnectionRegistry registry = connection.getRegistry();
    final ValueField panel = new ValueField();
    panel.setTitle("Edit Web Service Connection");
    Borders.titled(panel, "Web Service Connection");
    SwingUtil.addLabel(panel, "Name");

    final MapEx config = connection.getConfig();

    final String oldName = connection.getName();
    final TextField nameField = new TextField("name", oldName, 20);
    panel.add(nameField);

    SwingUtil.addLabel(panel, "Service URL");
    String serviceUrl = config.getString("serviceUrl");
    final TextField urlField = new TextField("serviceUrl", serviceUrl, 50);
    panel.add(urlField);

    SwingUtil.addLabel(panel, "Username");
    String username = config.getString("username");
    final TextField usernameField = new TextField("username", username, 30);
    panel.add(usernameField);

    SwingUtil.addLabel(panel, "Password");
    String password = PasswordUtil.decrypt(config.getString("password"));
    final PasswordField passwordField = new PasswordField("password", password, 30);
    panel.add(passwordField);

    GroupLayouts.makeColumns(panel, 2, true);
    panel.showDialog();
    if (panel.isSaved()) {
      serviceUrl = urlField.getText();
      if (serviceUrl != null) {
        final String name = nameField.getText();
        username = usernameField.getText();
        password = passwordField.getText();
        config.put("name", name);
        config.put("serviceUrl", serviceUrl);
        config.put("username", username);
        config.put("password", PasswordUtil.encrypt(password));
        if (Strings.equals(oldName, name)) {
          connection.setProperties(config);
        } else {
          registry.removeConnection(connection);
          registry.newConnection(config);
        }
      }
    }
  }

  public static LayerGroup getLayerGroup(final WebServiceResource webServiceResource) {
    LayerGroup layerGroup = Project.get();
    if (layerGroup != null) {
      final WebService<?> webService = webServiceResource.getWebService();
      if (webService != null) {
        final String webServiceName = webService.getName();
        if (Property.hasValue(webServiceName)) {
          layerGroup = layerGroup.addLayerGroup(webServiceName);
        }
      }
      final PathName layerPath = webServiceResource.getPathName();
      if (layerPath != null) {
        final PathName parent = layerPath.getParent();
        if (parent != null) {
          for (final String groupName : parent.getElements()) {
            layerGroup = layerGroup.addLayerGroup(groupName);
          }
        }
      }
    }
    return layerGroup;
  }

  public static BaseTreeNode newWebServiceConnectionsTreeNode() {
    final WebServiceConnectionManager webServicesConnectionManager = WebServiceConnectionManager
      .get();
    final BaseTreeNode webServices = BaseTreeNode.newTreeNode(webServicesConnectionManager);
    webServices.setOpen(true);
    return webServices;
  }
}
