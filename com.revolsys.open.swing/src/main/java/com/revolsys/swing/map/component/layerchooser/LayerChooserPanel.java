package com.revolsys.swing.map.component.layerchooser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.springframework.util.StringUtils;

import com.revolsys.awt.WebColors;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.io.file.FolderConnection;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.list.BaseListModel;
import com.revolsys.swing.tree.file.FileModel;
import com.revolsys.swing.tree.model.node.ListTreeNode;
import com.revolsys.swing.tree.renderer.BaseTreeCellRenderer;

public class LayerChooserPanel extends ValueField implements
  TreeSelectionListener, ListCellRenderer {
  private static final long serialVersionUID = 1L;

  private JXTree tree;

  private JXList layerList;

  private final DefaultListCellRenderer listCellRenderer = new DefaultListCellRenderer();

  private BaseListModel<Object> layerListModel;

  public LayerChooserPanel() {
    super(new BorderLayout());
    setPreferredSize(new Dimension(640, 480));

    final JComponent tree = createTree();
    final JScrollPane list = createList();
    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
      true, tree, list);
    splitPane.setDividerLocation(300);
    add(splitPane, BorderLayout.CENTER);
  }

  public JScrollPane createList() {
    layerListModel = new BaseListModel<Object>();
    layerList = new JXList(layerListModel);
    layerList.setCellRenderer(this);
    layerList.addHighlighter(new ColorHighlighter(HighlightPredicate.ODD,
      WebColors.LightSteelBlue, WebColors.Black, WebColors.Navy,
      WebColors.White));
    layerList.addHighlighter(new ColorHighlighter(HighlightPredicate.EVEN,
      WebColors.White, WebColors.Black, WebColors.Blue, WebColors.White));

    return new JScrollPane(layerList);
  }

  private JComponent createTree() {
    final ListTreeNode root = new ListTreeNode();
    final FileTreeNode fileTreeNode = new FileTreeNode(root);
    root.add(fileTreeNode);
    final FolderConnectionsTreeNode folderConnections = new FolderConnectionsTreeNode(
      root);
    root.add(folderConnections);

    final TreeModel treeModel = new DefaultTreeModel(root, true);
    tree = new JXTree(treeModel);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setLargeModel(true);
    tree.setCellRenderer(new BaseTreeCellRenderer());

    final DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
    selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setSelectionModel(selectionModel);
    selectionModel.addTreeSelectionListener(this);
    final JScrollPane scrollPane = new JScrollPane(tree);
    return scrollPane;
  }

  @Override
  public Component getListCellRendererComponent(final JList list,
    final Object value, final int index, final boolean selected,
    final boolean cellHasFocus) {
    String text;
    if (value instanceof File) {
      final File file = (File)value;
      text = file.getName();
      if (!StringUtils.hasText(text)) {
        text = "/";
      }
    } else {
      text = StringConverterRegistry.toString(value);
    }
    listCellRenderer.getListCellRendererComponent(list, text, index, selected,
      cellHasFocus);
    if (value instanceof File) {
      final File file = (File)value;
      final Icon icon = FileModel.getIcon(file);
      listCellRenderer.setIcon(icon);
    }
    return listCellRenderer;
  }

  private void setFiles(final File[] files) {
    layerListModel.clear();
    if (files != null) {
      for (final File file : files) {
        layerListModel.add(file);
      }
    }
  }

  public void updateList(final File file) {
    if (file == null) {
      setFiles(File.listRoots());
    } else if (!file.exists()) {
    } else if (FileModel.isDataStore(file)) {

    } else if (file.isDirectory()) {
      setFiles(file.listFiles());
    }
  }

  @Override
  public void valueChanged(final TreeSelectionEvent event) {
    if (event.isAddedPath()) {
      final TreePath path = event.getPath();
      final Object selectedItem = path.getLastPathComponent();
      if (selectedItem instanceof FileTreeNode) {
        final FileTreeNode fileNode = (FileTreeNode)selectedItem;
        final File file = fileNode.getFile();
        updateList(file);
      } else if (selectedItem instanceof FolderConnectionsTreeNode) {
        final FolderConnectionsTreeNode fileConnectionNode = (FolderConnectionsTreeNode)selectedItem;
        final Object object = fileConnectionNode.getUserObject();
        if (object instanceof File) {
          final File file = (File)object;
          updateList(file);
        } else if (object instanceof FolderConnection) {
          final FolderConnection folderConnection = (FolderConnection)object;
          final File file = folderConnection.getFile();
          updateList(file);
        }
      }

    }
  }
}
