package com.revolsys.swing.map.layer.elevation.gridded.renderer;

import java.util.function.Predicate;

import javax.swing.Icon;

import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.MultipleLayerRenderer;
import com.revolsys.swing.map.layer.elevation.ElevationModelLayer;
import com.revolsys.swing.map.layer.menu.TreeItemScaleMenu;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.swing.menu.MenuFactory;

public abstract class AbstractGriddedElevationModelLayerRenderer
  extends AbstractLayerRenderer<ElevationModelLayer> {

  static {
    MenuFactory.addMenuInitializer(AbstractGriddedElevationModelLayerRenderer.class, menu -> {
      menu.addMenuItem("layer", -1, "View/Edit Style", "palette",
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

  public AbstractGriddedElevationModelLayerRenderer(final String type, final String name,
    final Icon icon) {
    super(type, name, icon);
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

  @Override
  public final void render(final ViewRenderer view, final ElevationModelLayer layer) {
    final BufferedGeoreferencedImage image = layer.newRenderImage();
    render(view, layer, image);
  }

  public abstract void render(final ViewRenderer view, final ElevationModelLayer layer,
    final BufferedGeoreferencedImage image);

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
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    return map;
  }
}
