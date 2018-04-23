package com.revolsys.swing.map.layer.elevation.gridded.renderer;

import java.util.function.Predicate;

import javax.swing.Icon;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.MultipleLayerRenderer;
import com.revolsys.swing.map.layer.elevation.ElevationModelLayer;
import com.revolsys.swing.map.layer.menu.TreeItemScaleMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.Menus;

public abstract class AbstractGriddedElevationModelLayerRenderer
  extends AbstractLayerRenderer<ElevationModelLayer> {

  static {
    MenuFactory.addMenuInitializer(AbstractGriddedElevationModelLayerRenderer.class, menu -> {
      Menus.addMenuItem(menu, "layer", "View/Edit Style", "palette",
        ((Predicate<AbstractGriddedElevationModelLayerRenderer>)AbstractGriddedElevationModelLayerRenderer::isEditing)
          .negate(),
        AbstractGriddedElevationModelLayerRenderer::showProperties, false);

      menu.addComponentFactory("scale",
        new TreeItemScaleMenu<>(true, null,
          AbstractGriddedElevationModelLayerRenderer::getMinimumScale,
          AbstractGriddedElevationModelLayerRenderer::setMinimumScale));
      menu.addComponentFactory("scale",
        new TreeItemScaleMenu<>(false, null,
          AbstractGriddedElevationModelLayerRenderer::getMaximumScale,
          AbstractGriddedElevationModelLayerRenderer::setMaximumScale));
    });
  }

  private GriddedElevationModel elevationModel;

  public AbstractGriddedElevationModelLayerRenderer(final String type, final String name) {
    super(type, name);
  }

  @Override
  public AbstractGriddedElevationModelLayerRenderer clone() {
    return (AbstractGriddedElevationModelLayerRenderer)super.clone();
  }

  public GriddedElevationModel getElevationModel() {
    return this.elevationModel;
  }

  public Icon newIcon() {
    return getIcon();
  }

  @Override
  public void refresh() {
  }

  protected void refreshIcon() {
    final Icon icon = newIcon();
    setIcon(icon);
  }

  public void setElevationModel(final GriddedElevationModel elevationModel) {
    this.elevationModel = elevationModel;
  }

  @Override
  public void setName(final String name) {
    final MultipleLayerRenderer<?, ?> parent = (MultipleLayerRenderer<?, ?>)getParent();
    String newName = name;
    if (parent != null) {
      int i = 1;
      while (parent.hasRendererWithSameName(this, newName)) {
        newName = name + i;
        i++;
      }
    }
    super.setName(newName);
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    return map;
  }
}
