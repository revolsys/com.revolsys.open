package com.revolsys.swing.map;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.net.ResponseCache;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import bibliothek.gui.dock.common.CContentArea;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.location.TreeLocationRoot;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.common.theme.CEclipseTheme;
import bibliothek.gui.dock.common.theme.ThemeMap;
import bibliothek.gui.dock.dockable.ScreencaptureMovingImageFactory;

import com.revolsys.gis.data.io.DataObjectStoreConnections;
import com.revolsys.io.FileSystemConnectionManager;
import com.revolsys.io.FileUtil;
import com.revolsys.net.urlcache.FileResponseCache;
import com.revolsys.swing.DockingFramesUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.I18nAction;
import com.revolsys.swing.action.file.Exit;
import com.revolsys.swing.log4j.Log4jTableModel;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.tree.ProjectTreeNodeModel;
import com.revolsys.swing.map.util.LayerUtil;
import com.revolsys.swing.table.worker.SwingWorkerTableModel;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.swing.tree.ObjectTreePanel;
import com.revolsys.swing.tree.datastore.DataObjectStoreConnectionsModel;
import com.revolsys.swing.tree.file.FileSystemConnectionManagerModel;
import com.revolsys.swing.tree.model.node.ListObjectTreeNodeModel;

@SuppressWarnings("serial")
public class ProjectFrame extends JFrame {
  static {
    ResponseCache.setDefault(new FileResponseCache());
  }

  private ObjectTreePanel tocPanel;

  private Project project;

  private CControl dockControl = new CControl(this);

  private MapPanel mapPanel;

  public ProjectFrame(final String title) {
    super(title);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    SwingUtil.setSizeAndMaximize(this, 100, 100);

    dockControl.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
    final CEclipseTheme theme = (CEclipseTheme)dockControl.getController()
      .getTheme();
    theme.intern().setMovingImageFactory(
      new ScreencaptureMovingImageFactory(new Dimension(2000, 2000)));

    final CContentArea dockContentArea = dockControl.getContentArea();
    add(dockContentArea, BorderLayout.CENTER);
    DockingFramesUtil.setFlapSizes(dockControl);

    initUi();
  }

  protected void addCatalogPanel() {

    final List<Object> connectionManagers = new ArrayList<Object>();

    connectionManagers.add(DataObjectStoreConnections.get());
    connectionManagers.add(FileSystemConnectionManager.get());
    /* connectionManagers.add(WmsConnectionManager.get()); */

    final ListObjectTreeNodeModel listModel = new ListObjectTreeNodeModel(
      new DataObjectStoreConnectionsModel(),
      new FileSystemConnectionManagerModel());
    final ObjectTreePanel catalogPanel = new ObjectTreePanel(
      connectionManagers, listModel);
    final Project project = getProject();
    DockingFramesUtil.addDockable(project, MapPanel.MAP_CONTROLS_WORKING_AREA,
      "catalog", "Catalog", catalogPanel);

    ((DefaultSingleCDockable)getDockControl().getSingleDockable("toc")).toFront();

  }

  protected void addControlWorkingArea() {
    final CLocation location = CLocation.base().normalWest(0.20);
    DockingFramesUtil.createCWorkingArea(dockControl, project,
      MapPanel.MAP_CONTROLS_WORKING_AREA, location);
  }

  protected void addLayers() {
    final File userHomeDirectory = FileUtil.getUserHomeDirectory();
    final File projectFile = new File(userHomeDirectory,
      "Documents/default.rgmap");
    final File resourcesDir = new File(projectFile, "Contents/Resources");
    LayerUtil.loadLayerGroup(project, resourcesDir);
  }

  protected void addLogPanel() {
    final JPanel panel = Log4jTableModel.createPanel();
    DockingFramesUtil.addDockable(project, MapPanel.MAP_TABLE_WORKING_AREA,
      "log4j", "Logging", panel);
  }

  protected MapPanel addMapPanel() {
    mapPanel = new MapPanel();
    project = mapPanel.getProject();

    final DefaultSingleCDockable dockable = new DefaultSingleCDockable("map",
      "Map", mapPanel);
    dockable.setStackable(false);
    dockable.setCloseable(false);
    dockable.setDefaultLocation(ExtendedMode.MINIMIZED, CLocation.base()
      .minimalWest());

    dockControl.addDockable(dockable);
    dockable.setVisible(true);
    return mapPanel;
  }

  protected DefaultSingleCDockable addTableOfContents() {
    final JPanel panel = new JPanel(new BorderLayout());

    final ToolBar toolBar = new ToolBar();
    panel.add(toolBar, BorderLayout.NORTH);

    final ProjectTreeNodeModel model = new ProjectTreeNodeModel();
    tocPanel = new ObjectTreePanel(project, model);
    panel.add(tocPanel, BorderLayout.CENTER);
    final DefaultSingleCDockable tableOfContents = DockingFramesUtil.addDockable(
      project, MapPanel.MAP_CONTROLS_WORKING_AREA, "toc", "TOC", panel);
    tableOfContents.toFront();
    return tableOfContents;
  }

  protected void addTableWorkingArea() {
    final TreeLocationRoot location = CLocation.base().normalSouth(0.25);
    DockingFramesUtil.createCWorkingArea(dockControl, project,
      MapPanel.MAP_TABLE_WORKING_AREA, location);
  }

  protected void addTasksPanel() {
    final JPanel panel = SwingWorkerTableModel.createPanel();
    DockingFramesUtil.addDockable(project, MapPanel.MAP_TABLE_WORKING_AREA,
      "tasks", "Background Tasks", panel);
  }

  protected void addWorkingAreas() {
    addControlWorkingArea();
    addTableWorkingArea();
  }

  protected JMenu createFileMenu(final JMenuBar menuBar) {
    final JMenu fileMenu = new JMenu(new I18nAction(ProjectFrame.class, "File"));
    menuBar.add(fileMenu);

    fileMenu.add(new Exit());
    return fileMenu;
  }

  protected JMenuBar createMenuBar() {
    final JMenuBar menuBar = new JMenuBar();
    setJMenuBar(menuBar);

    createFileMenu(menuBar);
    return menuBar;
  }

  public CControl getDockControl() {
    return dockControl;
  }

  public MapPanel getMapPanel() {
    return mapPanel;
  }

  public Project getProject() {
    return project;
  }

  public ObjectTreePanel getTocPanel() {
    return tocPanel;
  }

  protected void initUi() {
    addMapPanel();

    addWorkingAreas();

    final DefaultSingleCDockable toc = addTableOfContents();
    addCatalogPanel();
    toc.toFront();

    addTasksPanel();
    addLogPanel();

    addLayers();

    createMenuBar();
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    tocPanel = null;
    project = null;
    dockControl = null;
    mapPanel = null;
  }

}
