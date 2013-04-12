package com.revolsys.swing.map.tree;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.TreePath;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.grid.GridLayer;
import com.revolsys.swing.map.layer.grid.ZoomToMapSheet;
import com.revolsys.swing.map.layer.menu.SetLayerScaleMenu;
import com.revolsys.swing.map.tree.renderer.LayerTreeCellRenderer;
import com.revolsys.swing.map.util.LayerUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.ObjectTree;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.swing.tree.model.node.AbstractObjectTreeNodeModel;

public class BaseLayerTreeNodeModel extends
  AbstractObjectTreeNodeModel<AbstractLayer, LayerRenderer<Layer>> implements
  MouseListener {

  @Override
  public void setObjectTreeModel(ObjectTreeModel objectTreeModel) {
    super.setObjectTreeModel(objectTreeModel);
    MenuFactory abstractLayerMenu = objectTreeModel.getMenu(AbstractLayer.class);
    abstractLayerMenu.addComponentFactory("scale", new SetLayerScaleMenu(false));
    abstractLayerMenu.addComponentFactory("scale", new SetLayerScaleMenu(true));

    MenuFactory gridLayerMenu = objectTreeModel.getMenu(GridLayer.class);
    gridLayerMenu.addMenuItem("zoom", new ZoomToMapSheet());

    MenuFactory dataObjectLayerMenu = objectTreeModel.getMenu(AbstractDataObjectLayer.class);

    dataObjectLayerMenu.addMenuItem("table", new InvokeMethodAction(
      "View Attributes", "View Attributes", SilkIconLoader.getIcon("table_go"),
      LayerUtil.class, "showViewAttributes"));

    dataObjectLayerMenu.addMenuItem("zoom", new InvokeMethodAction(
      "Zoom to Layer", "Zoom to Layer", SilkIconLoader.getIcon("magnifier"),
      LayerUtil.class, "zoomToLayer"));

    dataObjectLayerMenu.addMenuItem("zoom",
      new InvokeMethodAction("Zoom to Selected", "Zoom to Selected",
        SilkIconLoader.getIcon("magnifier_zoom_selected"), LayerUtil.class,
        "zoomToLayerSelected"));

    EnableCheck editable = new TreeItemPropertyEnableCheck("editable");
    EnableCheck readonly = new TreeItemPropertyEnableCheck("readOnly", false);
    EnableCheck hasChanges = new TreeItemPropertyEnableCheck("hasChanges");

    dataObjectLayerMenu.addCheckboxMenuItem("edit", new InvokeMethodAction(
      "Editable", "Editable", SilkIconLoader.getIcon("pencil"), readonly,
      LayerUtil.class, "toggleEditable"), editable);

    dataObjectLayerMenu.addMenuItem("edit", TreeItemRunnable.createAction(
      "Save Changes", "table_save", hasChanges, "saveChanges"));

    dataObjectLayerMenu.addMenuItem("edit", TreeItemRunnable.createAction(
      "Cancel Changes", "table_cancel", hasChanges, "cancelChanges"));

    EnableCheck canAdd = new TreeItemPropertyEnableCheck("canAddObjects");
    dataObjectLayerMenu.addMenuItem("edit", TreeItemRunnable.createAction(
      "Add New Record", "table_row_insert", canAdd, "addNewRecord"));

    dataObjectLayerMenu.addComponentFactory("scale", new SetLayerScaleMenu(
      false));
    dataObjectLayerMenu.addComponentFactory("scale",
      new SetLayerScaleMenu(true));

  }

  public static BaseLayerTreeNodeModel create(final String name,
    final Class<? extends AbstractLayer> layerClass) {
    final BaseLayerTreeNodeModel model = new BaseLayerTreeNodeModel(name,
      layerClass);
    return model;
  }

  @Override
  protected List<LayerRenderer<Layer>> getChildren(AbstractLayer node) {
    LayerRenderer<Layer> renderer = node.getRenderer();
    if (renderer == null) {
      return Collections.emptyList();
    } else {
      return Collections.singletonList(renderer);
    }
  }

  private final Set<Class<?>> SUPPORTED_CHILD_CLASSES = Collections.<Class<?>> singleton(LayerRenderer.class);

  private final String name;

  public BaseLayerTreeNodeModel(final String name,
    final Class<?>... supportedClasses) {
    this.name = name;
    if (supportedClasses.length == 0) {
      setSupportedClasses(AbstractLayer.class);
    } else {
      setSupportedClasses(supportedClasses);
    }

    setSupportedChildClasses(SUPPORTED_CHILD_CLASSES);
    setObjectTreeNodeModels(new MultipleLayerRendererTreeNodeModel(),
      new BaseLayerRendererTreeNodeModel());
    setRenderer(new LayerTreeCellRenderer());
    setMouseListener(this);
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    final Object source = e.getSource();
    if (source instanceof ObjectTree) {
      final ObjectTree tree = (ObjectTree)source;
      int clickCount = e.getClickCount();
      if (clickCount == 2 && SwingUtilities.isLeftMouseButton(e)) {
        final int x = e.getX();
        final int y = e.getY();
        final TreePath path = tree.getPathForLocation(x, y);
        if (path != null) {
          final Object node = path.getLastPathComponent();
          if (node instanceof Layer) {
            final Layer layer = (Layer)node;
            final TreeUI ui = tree.getUI();
            final Rectangle bounds = ui.getPathBounds(tree, path);
            final int cX = x - bounds.x;
            final int index = cX / 21;
            int offset = 0;
            if (index == offset) {
              layer.setVisible(!layer.isVisible());
            }
            offset++;
            // if (layer.isQuerySupported()) {
            // if (index == offset) {
            // layer.setQueryable(!layer.isQueryable());
            // }
            // offset++;
            // }

            if (layer.isSelectSupported()) {
              if (index == offset) {
                layer.setSelectable(!layer.isSelectable());
              }
              offset++;
            }

            if (!layer.isReadOnly()) {
              if (index == offset) {
                layer.setEditable(!layer.isEditable());
              }
              offset++;
            }

            tree.repaint();
          }
        }
      }
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent e) {
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
  }

  @Override
  public String toString() {
    return name;
  }

}
