package com.revolsys.swing.map.layer.record.renderer;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.slf4j.LoggerFactory;

import com.revolsys.data.filter.MultipleAttributeValuesFilter;
import com.revolsys.data.record.Record;
import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.filter.AcceptAllFilter;
import com.revolsys.filter.Filter;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.TopologyException;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.menu.TreeItemScaleMenu;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.SqlLayerFilter;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.TreeUserDataPropertyEnableCheck;
import com.revolsys.swing.tree.TreeUserDataRunnable;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public abstract class AbstractRecordLayerRenderer extends
  AbstractLayerRenderer<AbstractRecordLayer> {

  public static void addRendererClass(final String name,
    final Class<? extends AbstractRecordLayerRenderer> clazz) {
    try {
      final Constructor<? extends AbstractRecordLayerRenderer> constructor = clazz.getConstructor(
        AbstractRecordLayer.class, LayerRenderer.class, Map.class);
      RENDERER_CONSTRUCTORS.put(name, constructor);
    } catch (final NoSuchMethodException e) {
      throw new IllegalArgumentException("Invalid constructor", e);
    } catch (final SecurityException e) {
      throw new IllegalArgumentException("No permissions for constructor", e);
    }
  }

  public static Filter<Record> getFilter(final AbstractRecordLayer layer,
    final Map<String, Object> style) {
    @SuppressWarnings("unchecked")
    Map<String, Object> filterDefinition = (Map<String, Object>)style.get("filter");
    if (filterDefinition != null) {
      filterDefinition = new LinkedHashMap<String, Object>(filterDefinition);
      final String type = (String)filterDefinition.remove("type");
      if ("valueFilter".equals(type)) {
        return new MultipleAttributeValuesFilter(filterDefinition);
      } else if ("queryFilter".equals(type)) {
        String query = (String)filterDefinition.remove("query");
        if (Property.hasValue(query)) {
          query = query.replaceAll("!= null", "IS NOT NULL");
          query = query.replaceAll("== null", "IS NULL");
          query = query.replaceAll("==", "=");
          query = query.replaceAll("!=", "<>");
          query = query.replaceAll("\\{(.*)\\}.contains\\((.*)\\)",
            "$2 IN ($1)");
          query = query.replaceAll("\\[(.*)\\]", "$1");
          query = query.replaceAll("(.*).startsWith\\('(.*)'\\)",
            "$1 LIKE '$2%'");
          query = query.replaceAll("#systemProperties\\['user.name'\\]",
            "'{gbaUsername}'");
          return new SqlLayerFilter(layer, query);
        }
      } else if ("sqlFilter".equals(type)) {
        final String query = (String)filterDefinition.remove("query");
        if (Property.hasValue(query)) {
          return new SqlLayerFilter(layer, query);
        }
      } else {
        LoggerFactory.getLogger(AbstractRecordLayerRenderer.class).error(
          "Unknown filter type " + type);
      }
    }
    return DEFAULT_FILTER;
  }

  public static AbstractRecordLayerRenderer getRenderer(
    final AbstractLayer layer, final LayerRenderer<?> parent,
    final Map<String, Object> style) {
    final String type = (String)style.remove("type");
    final Constructor<? extends AbstractRecordLayerRenderer> constructor = RENDERER_CONSTRUCTORS.get(type);
    if (constructor == null) {
      LoggerFactory.getLogger(AbstractRecordLayerRenderer.class).error(
        "Unknown style type: " + style);
      return null;
    } else {
      try {
        return constructor.newInstance(layer, parent, style);
      } catch (final Throwable e) {
        ExceptionUtil.log(AbstractRecordLayerRenderer.class,
          "Unable to create renderer", e);
        return null;
      }
    }
  }

  public static LayerRenderer<AbstractRecordLayer> getRenderer(
    final AbstractLayer layer, final Map<String, Object> style) {
    return getRenderer(layer, null, style);
  }

  private static final AcceptAllFilter<Record> DEFAULT_FILTER = new AcceptAllFilter<Record>();

  private static final Map<String, Constructor<? extends AbstractRecordLayerRenderer>> RENDERER_CONSTRUCTORS = new HashMap<>();

  static {
    addRendererClass("geometryStyle", GeometryStyleRenderer.class);
    addRendererClass("textStyle", TextStyleRenderer.class);
    addRendererClass("markerStyle", MarkerStyleRenderer.class);
    addRendererClass("multipleStyle", MultipleRenderer.class);
    addRendererClass("scaleStyle", ScaleMultipleRenderer.class);
    addRendererClass("filterStyle", FilterMultipleRenderer.class);

    final MenuFactory menu = MenuFactory.getMenu(AbstractRecordLayerRenderer.class);
    menu.addMenuItem("layer", TreeUserDataRunnable.createAction(
      "View/Edit Style", "palette", new TreeUserDataPropertyEnableCheck(
        "editing", false), "showProperties"));
    menu.addMenuItem("layer",
      TreeUserDataRunnable.createAction("Delete", "delete",
        new TreeItemPropertyEnableCheck("parent", null, true), "delete"));

    menu.addComponentFactory("scale", new TreeItemScaleMenu(true));
    menu.addComponentFactory("scale", new TreeItemScaleMenu(false));

    for (final String type : Arrays.asList("Multiple", "Filter", "Scale")) {
      final String iconName = ("style_" + type + "_wrap").toLowerCase();
      final ImageIcon icon = SilkIconLoader.getIcon(iconName);
      final InvokeMethodAction action = TreeUserDataRunnable.createAction(
        "Wrap With " + type + " Style", icon, null, "wrapWith" + type + "Style");
      menu.addMenuItem("wrap", action);
    }

  }

  private Filter<Record> filter = DEFAULT_FILTER;

  public AbstractRecordLayerRenderer(final String type, final String name,
    final AbstractRecordLayer layer, final LayerRenderer<?> parent) {
    this(type, name, layer, parent, Collections.<String, Object> emptyMap());
  }

  public AbstractRecordLayerRenderer(final String type, final String name,
    final AbstractRecordLayer layer, final LayerRenderer<?> parent,
    final Map<String, Object> style) {
    super(type, name, layer, parent, style);
    this.filter = getFilter(layer, style);
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
    if (this.filter instanceof SqlLayerFilter) {
      final SqlLayerFilter layerFilter = (SqlLayerFilter)this.filter;
      return layerFilter.getQuery();
    } else {
      return null;
    }
  }

  protected boolean isFilterAccept(final LayerRecord record) {
    try {
      return this.filter.accept(record);
    } catch (final Throwable e) {
      return false;
    }
  }

  public boolean isVisible(final LayerRecord record) {
    if (isVisible() && !record.isDeleted()) {
      final boolean filterAccept = isFilterAccept(record);
      return filterAccept;
    } else {
      return false;
    }
  }

  @Override
  public void render(final Viewport2D viewport, final AbstractRecordLayer layer) {
    if (layer.hasGeometryAttribute()) {
      final BoundingBox boundingBox = viewport.getBoundingBox();
      final List<LayerRecord> records = layer.queryBackground(boundingBox);
      renderRecords(viewport, layer, records);
    }
  }

  public void renderRecord(final Viewport2D viewport,
    final BoundingBox visibleArea, final AbstractLayer layer,
    final LayerRecord record) {
  }

  protected void renderRecords(final Viewport2D viewport,
    final AbstractRecordLayer layer, final List<LayerRecord> records) {
    final BoundingBox visibleArea = viewport.getBoundingBox();
    for (final LayerRecord record : records) {
      if (record != null) {
        if (isVisible(record) && !layer.isHidden(record)) {
          try {
            renderRecord(viewport, visibleArea, layer, record);
          } catch (final TopologyException e) {
          } catch (final Throwable e) {
            ExceptionUtil.log(
              getClass(),
              "Unabled to render " + layer.getName() + " #"
                + record.getIdentifier(), e);
          }
        }
      }
    }
  }

  public void renderSelectedRecord(final Viewport2D viewport,
    final AbstractLayer layer, final LayerRecord record) {
    final BoundingBox boundingBox = viewport.getBoundingBox();
    if (isVisible(record)) {
      try {
        renderRecord(viewport, boundingBox, layer, record);
      } catch (final TopologyException e) {
      }
    }
  }

  protected void replace(final AbstractLayer layer,
    final AbstractMultipleRenderer parent,
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

  public void setQueryFilter(final String query) {
    if (this.filter instanceof SqlLayerFilter
        || this.filter instanceof AcceptAllFilter) {
      if (Property.hasValue(query)) {
        final AbstractRecordLayer layer = getLayer();
        this.filter = new SqlLayerFilter(layer, query);
      } else {
        this.filter = new AcceptAllFilter<>();
      }
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    if (!(this.filter instanceof AcceptAllFilter)) {
      MapSerializerUtil.add(map, "filter", this.filter);
    }
    return map;
  }

  protected void wrap(final AbstractLayer layer,
    final AbstractMultipleRenderer parent,
    final AbstractMultipleRenderer newRenderer) {
    newRenderer.addRenderer(this.clone());
    replace(layer, parent, newRenderer);
  }

  public FilterMultipleRenderer wrapWithFilterStyle() {
    final AbstractRecordLayer layer = getLayer();
    final AbstractMultipleRenderer parent = (AbstractMultipleRenderer)getParent();
    final FilterMultipleRenderer newRenderer = new FilterMultipleRenderer(
      layer, parent);
    wrap(layer, parent, newRenderer);
    return newRenderer;
  }

  public MultipleRenderer wrapWithMultipleStyle() {
    final AbstractRecordLayer layer = getLayer();
    final AbstractMultipleRenderer parent = (AbstractMultipleRenderer)getParent();
    final MultipleRenderer newRenderer = new MultipleRenderer(layer, parent);
    wrap(layer, parent, newRenderer);
    return newRenderer;
  }

  public ScaleMultipleRenderer wrapWithScaleStyle() {
    final AbstractRecordLayer layer = getLayer();
    final AbstractMultipleRenderer parent = (AbstractMultipleRenderer)getParent();
    final ScaleMultipleRenderer newRenderer = new ScaleMultipleRenderer(layer,
      parent);
    wrap(layer, parent, newRenderer);
    return newRenderer;
  }
}
