package com.revolsys.swing.map.layer.record.renderer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.swing.Icon;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.list.Lists;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.impl.PointDoubleXYOrientation;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.predicate.Predicates;
import com.revolsys.record.Record;
import com.revolsys.record.filter.MultipleAttributeValuesFilter;
import com.revolsys.record.io.format.json.JsonObject;
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
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public abstract class AbstractRecordLayerRenderer extends AbstractLayerRenderer<AbstractRecordLayer>
  implements RecordDefinitionProxy {

  static {
    MenuFactory.addMenuInitializer(AbstractRecordLayerRenderer.class, (menu) -> {
      menu.addMenuItem("layer", -1, "View/Edit Style", "palette",
        ((Predicate<AbstractRecordLayerRenderer>)AbstractRecordLayerRenderer::isEditing).negate(),
        AbstractRecordLayerRenderer::showProperties, false);

      menu.addMenuItem("layer", -1, "Delete", "delete", AbstractRecordLayerRenderer::isHasParent,
        AbstractRecordLayerRenderer::delete, true);

      menu.addComponentFactory("scale",
        new TreeItemScaleMenu<>(true, null, AbstractRecordLayerRenderer::getMinimumScale,
          AbstractRecordLayerRenderer::setMinimumScale));
      menu.addComponentFactory("scale",
        new TreeItemScaleMenu<>(false, null, AbstractRecordLayerRenderer::getMaximumScale,
          AbstractRecordLayerRenderer::setMaximumScale));

      menu.addMenuItem("wrap", "Wrap With Multiple Style", "style_multiple_wrap",
        AbstractRecordLayerRenderer::wrapWithMultipleStyle, false);

      menu.addMenuItem("wrap", "Wrap With Filter Style", "style_filter_wrap",
        AbstractRecordLayerRenderer::wrapWithFilterStyle, false);

      menu.addMenuItem("wrap", "Wrap With Scale Style", "style_scale_wrap",
        AbstractRecordLayerRenderer::wrapWithScaleStyle, false);
    });
  }

  private static Predicate<Record> DEFAULT_FILTER = Predicates.all();

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

  public static PointDoubleXYOrientation getPointWithOrientation(final ViewRenderer view,
    final Geometry geometry, final String placementType) {
    if (view == null) {
      return new PointDoubleXYOrientation(0.0, 0.0, 0);
    } else {
      return view.getPointWithOrientation(geometry, placementType);
    }
  }

  private Predicate<Record> filter = DEFAULT_FILTER;

  protected Predicate<Record> filterNoException = DEFAULT_FILTER;

  public AbstractRecordLayerRenderer(final String type, final String name, final Icon icon) {
    super(type, name, icon);
  }

  @Override
  public AbstractRecordLayerRenderer clone() {
    final AbstractRecordLayerRenderer clone = (AbstractRecordLayerRenderer)super.clone();
    clone.setFilter(JavaBeanUtil.clone(this.filter));
    return clone;
  }

  public void delete() {
    final LayerRenderer<?> parent = getParent();
    if (parent instanceof AbstractMultipleRecordLayerRenderer) {
      final AbstractMultipleRecordLayerRenderer multiple = (AbstractMultipleRecordLayerRenderer)parent;
      multiple.removeRenderer(this);
    }
  }

  public Predicate<Record> getFilter() {
    return this.filter;
  }

  public String getQueryFilter() {
    final Predicate<Record> filter = getFilter();
    if (filter instanceof RecordDefinitionSqlFilter) {
      final RecordDefinitionSqlFilter layerFilter = (RecordDefinitionSqlFilter)filter;
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
    return this.filterNoException.test(record);
  }

  public boolean isHasFilter() {
    return this.filter != Predicates.<Record> all();
  }

  @Override
  public boolean isHasParent() {
    return getParent() != null;
  }

  public boolean isVisible(final LayerRecord record) {
    if (isVisible() && !record.isDeleted()) {
      return this.filterNoException.test(record);
    } else {
      return false;
    }
  }

  @Override
  public boolean isVisible(final ViewRenderer view) {
    final long scaleForVisible = (long)view.getScaleForVisible();
    return isVisible(scaleForVisible);
  }

  public Icon newIcon() {
    return getIcon();
  }

  protected void refreshIcon() {
    final Icon icon = newIcon();
    setIcon(icon);
  }

  @Override
  public void render(final ViewRenderer view, final AbstractRecordLayer layer) {
    if (layer.hasGeometryField()) {
      final BoundingBox boundingBox = view.getBoundingBox();
      List<LayerRecord> records = layer.getRecordsBackground(view.getCacheBoundingBox(),
        boundingBox);
      if (!view.isShowHiddenRecords()) {
        final Predicate<LayerRecord> filter = record -> {
          return !layer.isHidden(record);
        };
        records = Lists.filter(view, records, filter);
      }
      renderRecords(view, layer, records);
    }
  }

  protected abstract void renderRecords(final ViewRenderer view, final AbstractRecordLayer layer,
    final List<LayerRecord> records);

  public final void renderSelectedRecords(final ViewRenderer view, final AbstractRecordLayer layer,
    List<LayerRecord> records) {
    if (layer.hasGeometryField()) {
      records = Lists.filter(view, records, record -> {
        return !layer.isDeleted(record);
      });
      renderSelectedRecordsDo(view, layer, records);
    }
  }

  protected abstract void renderSelectedRecordsDo(final ViewRenderer view,
    final AbstractRecordLayer layer, final List<LayerRecord> records);

  protected void replace(final AbstractLayer layer,
    final AbstractMultipleRecordLayerRenderer parent,
    final AbstractMultipleRecordLayerRenderer newRenderer) {
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
    this.filterNoException = Predicates.noException(filter);
    firePropertyChange("filter", oldValue, filter);
  }

  @Override
  public void setName(final String name) {
    final AbstractMultipleRecordLayerRenderer parent = (AbstractMultipleRecordLayerRenderer)getParent();
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
    final Predicate<Record> filter = getFilter(this, properties);
    setFilter(filter);
  }

  public void setQueryFilter(final String query) {
    if (this.filter instanceof RecordDefinitionSqlFilter || this.filter == DEFAULT_FILTER) {
      Predicate<Record> filter;
      if (Property.hasValue(query)) {
        filter = new RecordDefinitionSqlFilter(this, query);
      } else {
        filter = DEFAULT_FILTER;
      }
      setFilter(filter);
    }
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    if (isHasFilter()) {
      addToMap(map, "filter", this.filter);
    }
    return map;
  }

  protected void wrap(final AbstractLayer layer, final AbstractMultipleRecordLayerRenderer parent,
    final AbstractMultipleRecordLayerRenderer newRenderer) {
    newRenderer.addRenderer(this.clone());
    replace(layer, parent, newRenderer);
  }

  public FilterMultipleRenderer wrapWithFilterStyle() {
    final AbstractRecordLayer layer = getLayer();
    final AbstractMultipleRecordLayerRenderer parent = (AbstractMultipleRecordLayerRenderer)getParent();
    final FilterMultipleRenderer newRenderer = new FilterMultipleRenderer(parent);
    wrap(layer, parent, newRenderer);
    return newRenderer;
  }

  public MultipleRecordRenderer wrapWithMultipleStyle() {
    final AbstractRecordLayer layer = getLayer();
    final AbstractMultipleRecordLayerRenderer parent = (AbstractMultipleRecordLayerRenderer)getParent();
    final MultipleRecordRenderer newRenderer = new MultipleRecordRenderer(parent);
    wrap(layer, parent, newRenderer);
    return newRenderer;
  }

  public ScaleMultipleRenderer wrapWithScaleStyle() {
    final AbstractRecordLayer layer = getLayer();
    final AbstractMultipleRecordLayerRenderer parent = (AbstractMultipleRecordLayerRenderer)getParent();
    final ScaleMultipleRenderer newRenderer = new ScaleMultipleRenderer(parent);
    wrap(layer, parent, newRenderer);
    return newRenderer;
  }
}
