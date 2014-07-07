package com.revolsys.swing.map.component.layerchooser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.table.TableColumnExt;

import com.revolsys.awt.WebColors;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.table.object.ObjectListTable;
import com.revolsys.swing.tree.BaseTree;
import com.revolsys.swing.tree.file.FileSystemsTreeNode;
import com.revolsys.swing.tree.file.FolderConnectionsTreeNode;
import com.revolsys.swing.tree.model.node.AbstractTreeNode;
import com.revolsys.swing.tree.model.node.ListTreeNode;
import com.revolsys.swing.tree.record.RecordStoreConnectionsTreeNode;
import com.revolsys.swing.tree.renderer.BaseTreeCellRenderer;

public class LayerChooserPanel extends ValueField implements
  TreeSelectionListener, TableCellRenderer {
  private static final long serialVersionUID = 1L;

  public static BaseTree createTree() {
    final ListTreeNode root = new ListTreeNode();

    final RecordStoreConnectionsTreeNode recordStores = new RecordStoreConnectionsTreeNode(
      root);
    root.add(recordStores);

    final FileSystemsTreeNode fileSystems = new FileSystemsTreeNode(root);
    root.add(fileSystems);

    final FolderConnectionsTreeNode folderConnections = new FolderConnectionsTreeNode(
      root);
    root.add(folderConnections);

    final TreeModel treeModel = new DefaultTreeModel(root, true);
    final BaseTree tree = new BaseTree(treeModel);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setLargeModel(true);
    tree.setCellRenderer(new BaseTreeCellRenderer());

    final DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
    selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setSelectionModel(selectionModel);
    recordStores.expandChildren();
    fileSystems.expand();
    folderConnections.expandChildren();
    return tree;
  }

  private final BaseTree tree;

  private ObjectListTable<TreeNode> childNodesTable;

  private final DefaultTableCellRenderer tableCellRenderer = new DefaultTableCellRenderer();

  public LayerChooserPanel() {
    super(new BorderLayout());
    setPreferredSize(new Dimension(640, 480));

    tree = createTree();
    final TreeSelectionModel selectionModel = tree.getSelectionModel();
    selectionModel.addTreeSelectionListener(this);
    final JScrollPane treeScroll = new JScrollPane(tree);

    final JScrollPane list = createList();
    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
      true, treeScroll, list);
    splitPane.setDividerLocation(300);
    add(splitPane, BorderLayout.CENTER);
  }

  public JScrollPane createList() {
    childNodesTable = new ObjectListTable<TreeNode>("name", "type");
    childNodesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    childNodesTable.setSortable(true);
    childNodesTable.addHighlighter(new ColorHighlighter(HighlightPredicate.ODD,
      WebColors.LightSteelBlue, WebColors.Black, WebColors.Navy,
      WebColors.White));
    childNodesTable.addHighlighter(new ColorHighlighter(
      HighlightPredicate.EVEN, WebColors.White, WebColors.Black,
      WebColors.Blue, WebColors.White));
    for (int columnIndex = 0; columnIndex < childNodesTable.getColumnCount(); columnIndex++) {
      final TableColumnExt column = childNodesTable.getColumnExt(columnIndex);
      if (columnIndex == 0) {
        column.setPreferredWidth(70);
        column.setCellRenderer(this);
      } else {
        column.setPreferredWidth(30);
      }
    }
    return new JScrollPane(childNodesTable);
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table,
    final Object value, final boolean isSelected, final boolean hasFocus,
    final int row, final int column) {
    tableCellRenderer.getTableCellRendererComponent(table, value, isSelected,
      hasFocus, row, column);
    if (column == 0) {
      final TreeNode node = childNodesTable.getObjects().get(row);
      if (node instanceof AbstractTreeNode) {
        final AbstractTreeNode iconNode = (AbstractTreeNode)node;
        final Icon icon = iconNode.getIcon();
        tableCellRenderer.setIcon(icon);
      }
    }
    return tableCellRenderer;
  }

  @Override
  public void valueChanged(final TreeSelectionEvent event) {
    if (event.isAddedPath()) {
      List<TreeNode> children;
      final TreePath path = event.getPath();
      final Object selectedItem = path.getLastPathComponent();
      if (selectedItem instanceof AbstractTreeNode) {
        final AbstractTreeNode treeNode = (AbstractTreeNode)selectedItem;
        children = treeNode.getChildren();
      } else {
        children = Collections.emptyList();
      }
      childNodesTable.setObjects(children);
    }
  }
}
