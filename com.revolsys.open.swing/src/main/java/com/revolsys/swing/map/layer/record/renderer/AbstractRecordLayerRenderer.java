package com.revolsys.swing.map.layer.record.renderer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.swing.Icon;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.TopologyException;
import com.revolsys.geometry.model.impl.PointDoubleXYOrientation;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.logging.Logs;
import com.revolsys.predicate.Predicates;
import com.revolsys.record.Record;
import com.revolsys.record.filter.MultipleAttributeValuesFilter;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.menu.TreeItemScaleMenu;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.RecordDefinitionSqlFilter;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.Menus;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public abstract class AbstractRecordLayerRenderer extends AbstractLayerRenderer<AbstractRecordLayer>
  implements RecordDefinitionProxy {

  static {
    MenuFactory.addMenuInitializer(AbstractRecordLayerRenderer.class, (menu) -> {
      Menus.addMenuItem(menu, "layer", "View/Edit Style", "palette",
        ((Predicate<AbstractRecordLayerRenderer>)AbstractRecordLayerRenderer::isEditing).negate(),
        AbstractRecordLayerRenderer::showProperties, false);

      Menus.addMenuItem(menu, "layer", "Delete", "delete", AbstractRecordLayerRenderer::isHasParent,
        AbstractRecordLayerRenderer::delete, true);

      menu.addComponentFactory("scale",
        new TreeItemScaleMenu<>(true, null, AbstractRecordLayerRenderer::getMinimumScale,
          AbstractRecordLayerRenderer::setMinimumScale));
      menu.addComponentFactory("scale",
        new TreeItemScaleMenu<>(false, null, AbstractRecordLayerRenderer::getMaximumScale,
          AbstractRecordLayerRenderer::setMaximumScale));

      Menus.addMenuItem(menu, "wrap", "Wrap With Multiple Style", "style_multiple_wrap",
        AbstractRecordLayerRenderer::wrapWithMultipleStyle, false);

      Menus.addMenuItem(menu, "wrap", "Wrap With Filter Style", "style_filter_wrap",
        AbstractRecordLayerRenderer::wrapWithFilterStyle, false);

      Menus.addMenuItem(menu, "wrap", "Wrap With Scale Style", "style_scale_wrap",
        AbstractRecordLayerRenderer::wrapWithScaleStyle, false);
    });
  }

  public static Predicate<Record> getFilter(final RecordDefinitionProxy recordDefinitionProxy,
    final Map<String, ? extends Object> properties) {
    @SuppressWarnings("unchecked")
    Map<String, Object> filterDefinition = (Map<String, Object>)properties.get("filter");
    if (filterDefinition != null) {
      filterDefinition = new LinkedHashMap<>(filterDefinition);
      final String type = MapObjectFactory.getType(filterDefinition);
      if ("valueFilter".equals(type)) {
        return new MultipleAttributeValuesFilter(filterDefinition);
      } else if ("queryFilter".equals(type)) {
        String query = (String)filterDefinition.remove("query");
        if (Property.hasValue(query)) {
          query = query.replaceAll("!= null", "IS NOT NULL");
          query = query.replaceAll("== null", "IS NULL");
          query = query.replaceAll("==", "=");
          query = query.replaceAll("!=", "<>");
          query = query.replaceAll("\\{(.*)\\}.contains\\((.*)\\)", "$2 IN ($1)");
          query = query.replaceAll("\\[(.*)\\]", "$1");
          query = query.replaceAll("(.*).startsWith\\('(.*)'\\)", "$1 LIKE '$2%'");
          query = query.replaceAll("#systemProperties\\['user.name'\\]", "'{gbaUsername}'");
          return new RecordDefinitionSqlFilter(recordDefinitionProxy, query);
        }
      } else if ("sqlFilter".equals(type)) {
        final String query = (String)filterDefinition.remove("query");
        if (Property.hasValue(query)) {
          return new RecordDefinitionSqlFilter(recordDefinitionProxy, query);
        }
      } else {
        Logs.error(AbstractRecordLayerRenderer.class, "Unknown filter type " + type);
      }
    }
    return Predicates.all();
  }

  public static PointDoubleXYOrientation getPointWithOrientation(final ViewRenderer viewport,
    final Geometry geometry, final String placementType) {
    if (viewport == null) {
      return new PointDoubleXYOrientation(0.0, 0.0, 0);
    } else {
      return viewport.getPointWithOrientation(geometry, placementType);
    }
  }

  private Predicate<Record> filter = Predicates.all();

  public AbstractRecordLayerRenderer(final String type, final String name) {
    super(type, name);
  }

  public AbstractRecordLayerRenderer(final String type, final String name,
    final AbstractRecordLayer layer, final LayerRenderer<?> parent) {
    super(type, name, layer, parent);
  }

  @Override
  public AbstractRecordLayerRenderer clone() {
    final AbstractRecordLayerRenderer clone = (AbstractRecordLayerRenderer)super.clone();
    clone.filter = JavaBeanUtil.clone(this.filter);
    return clone;
  }

  public void delete() {
    final LayerRenderer<?> parent = getParent();
    if (parent instanceof AbstractMultipleRenderer) {
      final AbstractMultipleRenderer multiple = (AbstractMultipleRenderer)parent;
      multiple.removeRenderer(this);
    }
  }

  public String getQueryFilter() {
    if (this.filter instanceof RecordDefinitionSqlFilter) {
      final RecordDefinitionSqlFilter layerFilter = (RecordDefinitionSqlFilter)this.filter;
      return layerFilter.getQuery();
    } else {
      return null;
    }
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    final AbstractRecordLayer layer = getLayer();
    if (layer == null) {
      return null;
    } else {
      return layer.getRecordDefinition();
    }
  }

  protected boolean isFilterAccept(final LayerRecord record) {
    try {
      return this.filter.test(record);
    } catch (final Throwable e) {
      return false;
    }
  }

  @Override
  public boolean isHasParent() {
    return getParent() != null;
  }

  public boolean isVisible(final LayerRecord record) {
    if (isVisible() && !record.isDeleted()) {
      final boolean filterAccept = isFilterAccept(record);
      return filterAccept;
    } else {
      return false;
    }
  }

  public Icon newIcon() {
    return getIcon();
  }

  protected void refreshIcon() {
    final Icon icon = newIcon();
    setIcon(icon);
  }

  @Override
  public void render(final ViewRenderer viewport, final AbstractRecordLayer layer) {
    if (layer.hasGeometryField()) {
      final BoundingBox boundingBox = viewport.getBoundingBox();
      final List<LayerRecord> records = layer.getRecordsBackground(boundingBox);
      renderRecords(viewport, layer, records);
    }
  }

  public void renderRecord(final ViewRenderer viewport, final BoundingBox visibleArea,
    final AbstractRecordLayer layer, final LayerRecord record) {
  }

  protected void renderRecords(final ViewRenderer viewport, final AbstractRecordLayer layer,
    final List<LayerRecord> records) {
    final BoundingBox visibleArea = viewport.getBoundingBox();
    for (final LayerRecord record : viewport.cancellable(records)) {
      if (record != null) {
        if (isVisible(record) && !layer.isHidden(record)) {
          try {
            renderRecord(viewport, visibleArea, layer, record);
          } catch (final TopologyException e) {
          } catch (final Throwable e) {
            if (!viewport.isCancelled()) {
              Logs.error(this,
                "Unabled to render " + layer.getName() + " #" + record.getIdentifier(), e);
            }
          }
        }
      }
    }
  }

  public void renderSelectedRecord(final ViewRenderer viewport, final AbstractRecordLayer layer,
    final LayerRecord record) {
    final BoundingBox boundingBox = viewport.getBoundingBox();
    if (isVisible(record)) {
      try {
        renderRecord(viewport, boundingBox, layer, record);
      } catch (final TopologyException e) {
      }
    }
  }

  protected void replace(final AbstractLayer layer, final AbstractMultipleRenderer parent,
    final AbstractMultipleRenderer newRenderer) {
    if (parent == null) {
      if (isEditing()) {
        newRenderer.setEditing(true);
        firePropertyChange("replaceRenderer", this, newRenderer);
      } else {
        layer.setRenderer(newRenderer);
      }
    } else {
      final int index = parent.removeRenderer(this);
      parent.addRenderer(index, newRenderer);
    }
  }

  protected void setFilter(final Predicate<Record> filter) {
    final Object oldValue = this.filter;
    this.filter = filter;
    firePropertyChange("filter", oldValue, filter);
  }

  @Override
  public void setName(final String name) {
    final AbstractMultipleRenderer parent = (AbstractMultipleRenderer)getParent();
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
  public void setProperties(final Map<String, ? extends Object> properties) {
    super.setProperties(properties);
    this.filter = getFilter(this, properties);
  }

  public void setQueryFilter(final String query) {
    if (this.filter instanceof RecordDefinitionSqlFilter
      || this.filter == Predicates.<Record> all()) {
      Predicate<Record> filter;
      if (Property.hasValue(query)) {
        filter = new RecordDefinitionSqlFilter(this, query);
      } else {
        filter = Predicates.all();
      }
      setFilter(filter);
    }
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    if (!(this.filter == Predicates.<Record> all())) {
      addToMap(map, "filter", this.filter);
    }
    return map;
  }

  protected void wrap(final AbstractLayer layer, final AbstractMultipleRenderer parent,
    final AbstractMultipleRenderer newRenderer) {
    newRenderer.addRenderer(this.clone());
    replace(layer, parent, newRenderer);
  }

  public FilterMultipleRenderer wrapWithFilterStyle() {
    final AbstractRecordLayer layer = getLayer();
    final AbstractMultipleRenderer parent = (AbstractMultipleRenderer)getParent();
    final FilterMultipleRenderer newRenderer = new FilterMultipleRenderer(layer, parent);
    wrap(layer, parent, newRenderer);
    return newRenderer;
  }

  public MultipleRecordRenderer wrapWithMultipleStyle() {
    final AbstractRecordLayer layer = getLayer();
    final AbstractMultipleRenderer parent = (AbstractMultipleRenderer)getParent();
    final MultipleRecordRenderer newRenderer = new MultipleRecordRenderer(layer, parent);
    wrap(layer, parent, newRenderer);
    return newRenderer;
  }

  public ScaleMultipleRenderer wrapWithScaleStyle() {
    final AbstractRecordLayer layer = getLayer();
    final AbstractMultipleRenderer parent = (AbstractMultipleRenderer)getParent();
    final ScaleMultipleRenderer newRenderer = new ScaleMultipleRenderer(layer, parent);
    wrap(layer, parent, newRenderer);
    return newRenderer;
  }
}
