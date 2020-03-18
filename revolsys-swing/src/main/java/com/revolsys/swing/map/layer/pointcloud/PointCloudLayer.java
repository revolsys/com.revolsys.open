package com.revolsys.swing.map.layer.pointcloud;

import java.awt.Dimension;
import java.nio.file.Path;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import org.jdesktop.swingx.VerticalLayout;
import org.jeometry.common.logging.Logs;

import com.revolsys.collection.ValueHolder;
import com.revolsys.elevation.cloud.PointCloud;
import com.revolsys.elevation.cloud.PointCloudReadFactory;
import com.revolsys.elevation.cloud.las.LasPointCloudWriterFactory;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.file.Paths;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.Borders;
import com.revolsys.swing.RsSwingServiceInitializer;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.io.SwingIo;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.tree.TreeNodes;
import com.revolsys.swing.tree.node.file.PathTreeNode;
import com.revolsys.util.Property;

public class PointCloudLayer extends AbstractLayer {

  public static final String J_TYPE = "pointCloudLayer";

  private static void addMenuExportPointCloud(final EnableCheck enableCheck) {
    TreeNodes.addMenuItem(PathTreeNode.MENU, "point_cloud", "Export Point Cloud...",
      (final PathTreeNode node) -> {
        final Path sourceFile = node.getPath();
        SwingIo.exportToFile("Point Coud", "com.revolsys.swing.map.layer.pointcloud.export",
          LasPointCloudWriterFactory.class, "las", sourceFile, targetFile -> {
            if (!PointCloud.copyPointCloud(sourceFile, targetFile)) {
              Logs.error(PointCloudLayer.class, "Cannot read Point cloud: " + sourceFile);
            }
          });
      })
      .setVisibleCheck(enableCheck) //
      .setIconName("point_cloud", "save");
  }

  private static void addMenuPointCloudProperties(final EnableCheck enableCheck) {
    TreeNodes.addMenuItem(PathTreeNode.MENU, "point_cloud", "Point Cloud Properties",
      (final PathTreeNode node) -> {
        final Path file = node.getPath();
        Invoke.later(() -> {
          final Resource resource = Resource.getResource(file);
          final PointCloudLayer layer = new PointCloudLayer(resource);
          layer.initialize();
          layer.showProperties();
          layer.delete();
        });
      })
      .setVisibleCheck(enableCheck) //
      .setIconName("information");
  }

  private static void addMenuZoomToCloud(final EnableCheck enableCheck) {
    TreeNodes.addMenuItem(PathTreeNode.MENU, "point_cloud", "Zoom to Point Cloud",
      (final PathTreeNode node) -> {
        final Path file = node.getPath();
        final String baseName = Paths.getBaseName(file);
        Invoke.background("Zoom to Point Cloud: " + baseName, () -> {
          try (
            PointCloud<?> pointCloud = PointCloud.newPointCloud(file)) {
            MapPanel.zoomToBoundingBox(baseName, pointCloud);
          }
        });
      })
      .setVisibleCheck(enableCheck) //
      .setIconName("point_cloud", "magnifier");
  }

  public static void factoryInit() {
    MapObjectFactoryRegistry.newFactory("pointCloudLayer", "Point Cloud Layer",
      PointCloudLayer::new);

    // TODO Need to handle memory and rendering better
    // addIoFactoryMenuItem("pointCloud", "Add Point Cloud Layer",
    // "point_cloud",
    // PointCloudReadFactory.class);

    final EnableCheck enableCheck = RsSwingServiceInitializer
      .enableCheck(PointCloudReadFactory.class);
    addMenuExportPointCloud(enableCheck);
    addMenuZoomToCloud(enableCheck);
    addMenuPointCloudProperties(enableCheck);

  }

  private PointCloud<?> pointCloud;

  private Resource resource;

  private String url;

  public PointCloudLayer(final Map<String, ? extends Object> properties) {
    super(J_TYPE);
    setProperties(properties);
    setSelectSupported(false);
    setQuerySupported(false);
    setRenderer(new PointCloudLayerRenderer(this));
    setIcon("point_cloud");
  }

  public PointCloudLayer(final Resource resource) {
    super(J_TYPE);
    setSelectSupported(false);
    setQuerySupported(false);
    setRenderer(new PointCloudLayerRenderer(this));
    setIcon("point_cloud");
    setResource(resource);
    setName(resource.getBaseName());

  }

  public void cancelChanges() {
    if (this.pointCloud == null && this.resource != null) {
      PointCloud<?> pointCloud = null;
      final Resource pointCloudResource = Resource.getResource(this.url);
      if (pointCloudResource.exists()) {
        try {
          pointCloud = PointCloud.newPointCloud(pointCloudResource);
          if (pointCloud == null) {
            Logs.error(PointCloudLayer.class, "Cannot load: " + this.url);
          }
        } catch (final RuntimeException e) {
          Logs.error(PointCloudLayer.class, "Unable to load: " + this.url, e);
        }
      } else {
        Logs.error(PointCloudLayer.class, "URL does not exist: " + this.url);
      }
      setPointCloud(pointCloud);
    } else {
      // this.pointCloud.cancelChanges();
    }
    firePropertyChange("hasChanges", true, false);
  }

  @Override
  public BoundingBox getBoundingBox() {
    final PointCloud<?> pointCloud = getPointCloud();
    if (pointCloud == null) {
      return BoundingBox.empty();
    } else {
      final BoundingBox boundingBox = pointCloud.getBoundingBox();
      return boundingBox;
    }
  }

  @Override
  public BoundingBox getBoundingBox(final boolean visibleLayersOnly) {
    if (isExists() && (isVisible() || !visibleLayersOnly)) {
      return getBoundingBox();
    } else {
      return getGeometryFactory().bboxEmpty();
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    if (this.pointCloud == null) {
      return getBoundingBox().getGeometryFactory();
    } else {
      return this.pointCloud.getGeometryFactory();
    }
  }

  public PointCloud<?> getPointCloud() {
    return this.pointCloud;
  }

  @Override
  protected boolean initializeDo() {
    if (Property.hasValue(this.url)) {
      cancelChanges();
      return true;
    } else {
      Logs.error(this, "Layer definition does not contain a 'url' property");
      return false;
    }
  }

  @Override
  protected BasePanel newPropertiesTabGeneral(final TabbedValuePanel tabPanel) {
    final BasePanel generalPanel = super.newPropertiesTabGeneral(tabPanel);

    final ValueField propertiesPanel = new ValueField(this);
    propertiesPanel.setLayout(new VerticalLayout(5));
    Borders.titled(propertiesPanel, "Properties");
    generalPanel.add(propertiesPanel);
    final String propertiesHtml = this.pointCloud.toHtml();

    final JTextPane propertiesPane = new JTextPane();
    propertiesPane.setContentType("text/html");
    propertiesPane.setFont(SwingUtil.FONT);
    propertiesPane.setText(propertiesHtml);
    propertiesPane.setEditable(false);
    propertiesPane.setOpaque(false);
    propertiesPane.setBorder(null);
    propertiesPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    final JScrollPane scrollPane = new JScrollPane(propertiesPane);
    scrollPane.setMinimumSize(new Dimension(1000, 350));
    scrollPane.setPreferredSize(new Dimension(1000, 350));
    scrollPane.setMaximumSize(new Dimension(1000, 350));
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    propertiesPanel.add(scrollPane);
    final ValueHolder<JButton> buttonHolder = new ValueHolder<>();
    final JButton refreshButton = RunnableAction.newButton("Update Classification Counts", () -> {
      buttonHolder.getValue().setEnabled(false);
      Invoke.background("Classification Counts " + this.url, () -> {
        this.pointCloud.refreshClassificationCounts();
        return this.pointCloud.toHtml();
      }, html -> {
        propertiesPane.setText(html);
      });
    });
    propertiesPanel.add(refreshButton);
    buttonHolder.setValue(refreshButton);
    return generalPanel;
  }

  @Override
  protected ValueField newPropertiesTabGeneralPanelSource(final BasePanel parent) {
    final ValueField panel = super.newPropertiesTabGeneralPanelSource(parent);

    if (this.url.startsWith("file:")) {
      final String fileName = this.url.replaceFirst("file:(//)?", "");
      SwingUtil.addLabelledReadOnlyTextField(panel, "File", fileName);
    } else {
      SwingUtil.addLabelledReadOnlyTextField(panel, "URL", this.url);
    }
    final String fileNameExtension = FileUtil.getFileNameExtension(this.url);
    if (Property.hasValue(fileNameExtension)) {
      SwingUtil.addLabelledReadOnlyTextField(panel, "File Extension", fileNameExtension);
    }
    GroupLayouts.makeColumns(panel, 2, true);
    return panel;
  }

  public void setPointCloud(final PointCloud pointCloud) {
    final PointCloud old = this.pointCloud;
    Property.removeListener(this.pointCloud, this);
    this.pointCloud = pointCloud;
    if (pointCloud == null) {
      setExists(false);
    } else {
      setExists(true);
      Property.addListener(pointCloud, this);
    }
    firePropertyChange("pointCloud", old, this.pointCloud);
  }

  public void setResource(final Resource resource) {
    this.resource = resource;
    if (resource == null) {
      this.url = null;
    } else {
      this.url = resource.getUriString();
    }
  }

  public void setUrl(final String url) {
    this.url = url;
    this.resource = Resource.getResource(url);
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    addToMap(map, "url", this.url);
    return map;
  }

}
