package com.revolsys.swing.map.layer.dataobject.renderer;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.filter.AcceptAllFilter;
import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.filter.MultipleAttributeValuesFilter;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.TopologyException;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.layer.dataobject.SqlLayerFilter;
import com.revolsys.swing.map.layer.menu.TreeItemScaleMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.JavaBeanUtil;

public abstract class AbstractDataObjectLayerRenderer extends
AbstractLayerRenderer<AbstractDataObjectLayer> {

  public static void addRendererClass(final String name,
    final Class<? extends AbstractDataObjectLayerRenderer> clazz) {
    try {
      final Constructor<? extends AbstractDataObjectLayerRenderer> constructor = clazz.getConstructor(
        AbstractDataObjectLayer.class, LayerRenderer.class, Map.class);
      RENDERER_CONSTRUCTORS.put(name, constructor);
    } catch (final NoSuchMethodException e) {
      throw new IllegalArgumentException("Invalid constructor", e);
    } catch (final SecurityException e) {
      throw new IllegalArgumentException("No permissions for constructor", e);
    }
  }

  public static Filter<DataObject> getFilter(
    final AbstractDataObjectLayer layer, final Map<String, Object> style) {
    @SuppressWarnings("unchecked")
    Map<String, Object> filterDefinition = (Map<String, Object>)style.get("filter");
    if (filterDefinition != null) {
      filterDefinition = new LinkedHashMap<String, Object>(filterDefinition);
      final String type = (String)filterDefinition.remove("type");
      if ("valueFilter".equals(type)) {
        return new MultipleAttributeValuesFilter(filterDefinition);
      } else if ("queryFilter".equals(type)) {
        String query = (String)filterDefinition.remove("query");
        if (StringUtils.hasText(query)) {
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
        if (StringUtils.hasText(query)) {
          return new SqlLayerFilter(layer, query);
        }
      } else {
        LoggerFactory.getLogger(AbstractDataObjectLayerRenderer.class).error(
          "Unknown filter type " + type);
      }
    }
    return DEFAULT_FILTER;
  }

  public static AbstractDataObjectLayerRenderer getRenderer(
    final AbstractDataObjectLayer layer, final LayerRenderer<?> parent,
    final Map<String, Object> style) {
    final String type = (String)style.remove("type");
    final Constructor<? extends AbstractDataObjectLayerRenderer> constructor = RENDERER_CONSTRUCTORS.get(type);
    if (constructor == null) {
      LoggerFactory.getLogger(AbstractDataObjectLayerRenderer.class).error(
        "Unknown style type: " + style);
      return null;
    } else {
      try {
        return constructor.newInstance(layer, parent, style);
      } catch (final Throwable e) {
        ExceptionUtil.log(AbstractDataObjectLayerRenderer.class,
          "Unable to create renderer", e);
        return null;
      }
    }
  }

  public static LayerRenderer<AbstractDataObjectLayer> getRenderer(
    final AbstractDataObjectLayer layer, final Map<String, Object> style) {
    return getRenderer(layer, null, style);
  }

  private static final AcceptAllFilter<DataObject> DEFAULT_FILTER = new AcceptAllFilter<DataObject>();

  private static final Map<String, Constructor<? extends AbstractDataObjectLayerRenderer>> RENDERER_CONSTRUCTORS = new HashMap<>();

  static {
    addRendererClass("geometryStyle", GeometryStyleRenderer.class);
    addRendererClass("textStyle", TextStyleRenderer.class);
    addRendererClass("markerStyle", MarkerStyleRenderer.class);
    addRendererClass("multipleStyle", MultipleRenderer.class);
    addRendererClass("scaleStyle", ScaleMultipleRenderer.class);
    addRendererClass("filterStyle", FilterMultipleRenderer.class);

    final MenuFactory menu = ObjectTreeModel.getMenu(AbstractDataObjectLayerRenderer.class);
    menu.addMenuItem("layer", TreeItemRunnable.createAction("View/Edit Style",
      "palette", new TreeItemPropertyEnableCheck("editing", false),
        "showProperties"));
    menu.addMenuItem("layer", TreeItemRunnable.createAction("Delete", "delete",
      new TreeItemPropertyEnableCheck("parent", null, true), "delete"));

    menu.addComponentFactory("scale", new TreeItemScaleMenu(true));
    menu.addComponentFactory("scale", new TreeItemScaleMenu(false));

    for (final String type : Arrays.asList("Multiple", "Filter", "Scale")) {
      final String iconName = ("style_" + type + "_wrap").toLowerCase();
      final ImageIcon icon = SilkIconLoader.getIcon(iconName);
      final InvokeMethodAction action = TreeItemRunnable.createAction(
        "Wrap With " + type + " Style", icon, null, "wrapWith" + type + "Style");
      menu.addMenuItem("wrap", action);
    }

  }

  private Filter<DataObject> filter = DEFAULT_FILTER;

  public AbstractDataObjectLayerRenderer(final String type, final String name,
    final AbstractDataObjectLayer layer, final LayerRenderer<?> parent) {
    this(type, name, layer, parent, Collections.<String, Object> emptyMap());
  }

  public AbstractDataObjectLayerRenderer(final String type, final String name,
    final AbstractDataObjectLayer layer, final LayerRenderer<?> parent,
    final Map<String, Object> style) {
    super(type, name, layer, parent, style);
    this.filter = getFilter(layer, style);
  }

  @Override
  public AbstractDataObjectLayerRenderer clone() {
    final AbstractDataObjectLayerRenderer clone = (AbstractDataObjectLayerRenderer)super.clone();
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

  protected boolean isFilterAccept(final LayerDataObject record) {
    try {
      return this.filter.accept(record);
    } catch (final Throwable e) {
      return false;
    }
  }

  public boolean isVisible(final LayerDataObject record) {
    if (isVisible() && !record.isDeleted()) {
      final boolean filterAccept = isFilterAccept(record);
      return filterAccept;
    } else {
      return false;
    }
  }

  @Override
  public void render(final Viewport2D viewport,
    final AbstractDataObjectLayer layer) {
    if (layer.hasGeometryAttribute()) {
      final BoundingBox boundingBox = viewport.getBoundingBox();
      final List<LayerDataObject> dataObjects = layer.queryBackground(boundingBox);
      renderRecords(viewport, layer, dataObjects);
    }
  }

  public void renderRecord(final Viewport2D viewport,
    final BoundingBox visibleArea, final AbstractDataObjectLayer layer,
    final LayerDataObject record) {
  }

  protected void renderRecords(final Viewport2D viewport,
    final AbstractDataObjectLayer layer, final List<LayerDataObject> records) {
    final BoundingBox visibleArea = viewport.getBoundingBox();
    for (final LayerDataObject record : records) {
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
    final AbstractDataObjectLayer layer, final LayerDataObject record) {
    final BoundingBox boundingBox = viewport.getBoundingBox();
    if (isVisible(record)) {
      try {
        renderRecord(viewport, boundingBox, layer, record);
      } catch (final TopologyException e) {
      }
    }
  }

  protected void replace(final AbstractDataObjectLayer layer,
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
      if (StringUtils.hasText(query)) {
        final AbstractDataObjectLayer layer = getLayer();
        this.filter = new SqlLayerFilter(layer, query);
      } else {
        this.filter = new AcceptAllFilter<DataObject>();
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

  protected void wrap(final AbstractDataObjectLayer layer,
    final AbstractMultipleRenderer parent,
    final AbstractMultipleRenderer newRenderer) {
    newRenderer.addRenderer(this.clone());
    replace(layer, parent, newRenderer);
  }

  public FilterMultipleRenderer wrapWithFilterStyle() {
    final AbstractDataObjectLayer layer = getLayer();
    final AbstractMultipleRenderer parent = (AbstractMultipleRenderer)getParent();
    final FilterMultipleRenderer newRenderer = new FilterMultipleRenderer(
      layer, parent);
    wrap(layer, parent, newRenderer);
    return newRenderer;
  }

  public MultipleRenderer wrapWithMultipleStyle() {
    final AbstractDataObjectLayer layer = getLayer();
    final AbstractMultipleRenderer parent = (AbstractMultipleRenderer)getParent();
    final MultipleRenderer newRenderer = new MultipleRenderer(layer, parent);
    wrap(layer, parent, newRenderer);
    return newRenderer;
  }

  public ScaleMultipleRenderer wrapWithScaleStyle() {
    final AbstractDataObjectLayer layer = getLayer();
    final AbstractMultipleRenderer parent = (AbstractMultipleRenderer)getParent();
    final ScaleMultipleRenderer newRenderer = new ScaleMultipleRenderer(layer,
      parent);
    wrap(layer, parent, newRenderer);
    return newRenderer;
  }
}
