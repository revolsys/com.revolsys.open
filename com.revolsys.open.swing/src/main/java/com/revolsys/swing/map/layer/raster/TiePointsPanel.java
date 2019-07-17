package com.revolsys.swing.map.layer.raster;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import org.jdesktop.swingx.table.TableColumnExt;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.MappedLocation;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.field.Slider;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.NumberTableCellRenderer;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.object.ObjectListTable;
import com.revolsys.swing.table.object.ObjectListTableModel;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.Property;

public class TiePointsPanel extends TablePanel implements PropertyChangeListener {
  private static final List<String> COLUMN_NAMES = Arrays.asList("#", "sourcePixelX",
    "sourcePixelY", "targetPointX", "targetPointY");

  private static final long serialVersionUID = 1L;

  private static final List<String> TITLES = Arrays.asList("#", "Source Pixel X", "Source Pixel Y",
    "Target Point X", "Target Point Y");

  private static final List<Class<?>> CLASSES = Arrays.asList(Integer.class, Integer.class,
    Integer.class, Double.class, Double.class);

  private final GeoreferencedImageLayer layer;

  private final Slider opacityField;

  public TiePointsPanel(final GeoreferencedImageLayer layer) {
    super(new ObjectListTable<>(layer.getImage().getTiePoints(), COLUMN_NAMES, TITLES, CLASSES));

    this.layer = layer;

    final BaseJTable table = getTable();
    final TableColumnExt column1 = table.getColumnExt(0);
    column1.setMinWidth(30);
    column1.setWidth(30);
    column1.setMaxWidth(30);
    for (int i = 3; i < table.getColumnCount(); i++) {
      final TableColumnExt column = table.getColumnExt(i);
      column.setMinWidth(150);
      column.setCellRenderer(new NumberTableCellRenderer("#,###.000"));
    }

    final EnableCheck editableEnableCheck = new ObjectPropertyEnableCheck(layer, "editable");

    final MenuFactory menu = getTableModel().getMenu();
    menu.addMenuItemTitleIcon("zoom", "Zoom to Tie Point", "magnifier_zoom_selected",
      this::zoomToTiePoint);

    menu.addMenuItemTitleIcon("record", "Delete Tie Point", "table_row_delete", editableEnableCheck,
      this::deleteTiePoint);

    final ToolBar toolBar = getToolBar();

    final MenuFactory menuFactory = MenuFactory.findMenu(layer);
    if (menuFactory != null) {
      toolBar.addButtonTitleIcon("menu", "Layer Menu", "menu",
        () -> menuFactory.showMenu(layer, this, 10, 10));
    }

    menu.<GeoreferencedImageLayer> addMenuItem("zoom", "Zoom to Layer", "magnifier",
      GeoreferencedImageLayer::zoomToLayer, true);

    toolBar.addButton("zoom", "Zoom to Layer", "magnifier", (EnableCheck)null, layer::zoomToLayer);

    toolBar.addButton("edit", "Fit to Screen", "arrow_out", editableEnableCheck,
      layer::fitToViewport);

    this.opacityField = new Slider("opacity", 0, 256, layer.getOpacity());
    this.opacityField.setMajorTickSpacing(64);
    this.opacityField.setMinorTickSpacing(16);
    this.opacityField.setPaintTicks(true);
    Property.addListener(this.opacityField, this);
    this.opacityField.setMaximumSize(new Dimension(100, 25));

    toolBar.addComponent("edit", this.opacityField);
    Property.addListener(layer, "opacity", this);
  }

  public void deleteTiePoint() {
    final MappedLocation tiePoint = getEventRowObject();
    this.layer.deleteTiePoint(tiePoint);
  }

  protected MappedLocation getEventRowObject() {
    final ObjectListTableModel<MappedLocation> model = getTableModel();
    final int row = getEventRow();
    final MappedLocation object = model.getObject(row);
    return object;
  }

  public GeoreferencedImage getImage() {
    return getLayer().getImage();
  }

  public GeoreferencedImageLayer getLayer() {
    return this.layer;
  }

  public List<MappedLocation> getTiePoints() {
    return getImage().getTiePoints();
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getPropertyName();
    if ("opacity".equals(propertyName)) {
      final Integer opacity = (Integer)event.getNewValue();
      if (opacity != null) {
        if (event.getSource() == this.opacityField) {
          this.layer.setOpacity(opacity);
        } else {
          this.opacityField.setValue(opacity);
        }
      }
    }
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    Property.removeListener(this.layer, "opacity", this);
  }

  public void zoomToTiePoint() {
    final MappedLocation object = getEventRowObject();
    final GeoreferencedImage image = this.layer.getImage();
    final Geometry geometry = object.getSourceToTargetLine(image, this.layer.getBoundingBox(),
      !this.layer.isShowOriginalImage());
    if (geometry != null) {
      final Project project = Project.get();
      final GeometryFactory geometryFactory = project.getGeometryFactory();
      final BoundingBox boundingBox = geometry //
        .bboxEditor() //
        .setGeometryFactory(geometryFactory) //
        .expandDelta(200)
        .newBoundingBox();
      project.setViewBoundingBox(boundingBox);

    }
  }

}
