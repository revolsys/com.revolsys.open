package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
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
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.filter.MultipleAttributeValuesFilter;
import com.revolsys.gis.data.model.filter.SpringExpresssionLanguageFilter;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.layer.menu.TreeItemScaleMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeItemPropertyEnableCheck;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.model.ObjectTreeModel;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.JavaBeanUtil;

public abstract class AbstractDataObjectLayerRenderer extends
  AbstractLayerRenderer<AbstractDataObjectLayer> {

  static {
    final MenuFactory menu = ObjectTreeModel.getMenu(AbstractDataObjectLayerRenderer.class);
    menu.addMenuItem("layer", TreeItemRunnable.createAction("View/Edit Style",
      "palette", "showProperties"));
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

  private static final AcceptAllFilter<DataObject> DEFAULT_FILTER = new AcceptAllFilter<DataObject>();

  private static final Map<String, Object> FILTER_VARIABLES = new HashMap<String, Object>();

  public static void addFilterVariable(final String name, final Object value) {
    FILTER_VARIABLES.put(name, value);
  }

  public static Filter<DataObject> getFilter(final Map<String, Object> style) {
    @SuppressWarnings("unchecked")
    Map<String, Object> filterDefinition = (Map<String, Object>)style.get("filter");
    if (filterDefinition != null) {
      filterDefinition = new LinkedHashMap<String, Object>(filterDefinition);
      final String type = (String)filterDefinition.remove("type");
      if ("valueFilter".equals(type)) {
        return new MultipleAttributeValuesFilter(filterDefinition);
      } else if ("queryFilter".equals(type)) {
        final String query = (String)filterDefinition.remove("query");
        return new SpringExpresssionLanguageFilter(query, FILTER_VARIABLES);
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
    if ("geometryStyle".equals(type)) {
      return new GeometryStyleRenderer(layer, parent, style);
    } else if ("textStyle".equals(type)) {
      return new TextStyleRenderer(layer, parent, style);
    } else if ("markerStyle".equals(type)) {
      return new MarkerStyleRenderer(layer, parent, style);
    } else if ("multipleStyle".equals(type)) {
      return new MultipleRenderer(layer, parent, style);
    } else if ("scaleStyle".equals(type)) {
      return new ScaleMultipleRenderer(layer, parent, style);
    } else if ("filterStyle".equals(type)) {
      return new FilterMultipleRenderer(layer, parent, style);
    }
    LoggerFactory.getLogger(AbstractDataObjectLayerRenderer.class).error(
      "Unknown style type: " + style);
    return null;
  }

  public static LayerRenderer<AbstractDataObjectLayer> getRenderer(
    final AbstractDataObjectLayer layer, final Map<String, Object> style) {
    return getRenderer(layer, null, style);
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
    this.filter = getFilter(style);
  }

  @Override
  public AbstractDataObjectLayerRenderer clone() {
    final AbstractDataObjectLayerRenderer clone = (AbstractDataObjectLayerRenderer)super.clone();
    clone.filter = JavaBeanUtil.clone(filter);
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
    if (filter instanceof SpringExpresssionLanguageFilter) {
      final SpringExpresssionLanguageFilter expressionFilter = (SpringExpresssionLanguageFilter)filter;
      return expressionFilter.toString();
    } else {
      return null;
    }
  }

  protected boolean isFilterAccept(final LayerDataObject object) {
    try {
      return this.filter.accept(object);
    } catch (final Throwable e) {
      return false;
    }
  }

  public boolean isVisible(final LayerDataObject record) {
    if (isVisible() && !record.isDeleted()) {
      return isFilterAccept(record);
    } else {
      return false;
    }
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final AbstractDataObjectLayer layer) {
    final boolean saved = viewport.setUseModelCoordinates(true, graphics);
    try {
      final BoundingBox boundingBox = viewport.getBoundingBox();
      final List<LayerDataObject> dataObjects = layer.queryBackground(boundingBox);
      renderObjects(viewport, graphics, layer, dataObjects);
    } finally {
      viewport.setUseModelCoordinates(saved, graphics);
    }
  }

  protected void renderObject(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final AbstractDataObjectLayer layer, final LayerDataObject object) {
  }

  protected void renderObjects(final Viewport2D viewport,
    final Graphics2D graphics, final AbstractDataObjectLayer layer,
    final List<LayerDataObject> objects) {
    final BoundingBox visibleArea = viewport.getBoundingBox();
    for (final LayerDataObject object : objects) {
      if (object != null) {
        if (isVisible(object) && !layer.isHidden(object)) {
          try {
            renderObject(viewport, graphics, visibleArea, layer, object);
          } catch (final Throwable e) {
            ExceptionUtil.log(
              getClass(),
              "Unabled to render " + layer.getName() + " #"
                + object.getIdString(), e);
          }
        }
      }
    }
  }

  public void setQueryFilter(final String queryFilter) {
    if (filter instanceof SpringExpresssionLanguageFilter
      || filter instanceof AcceptAllFilter) {
      if (StringUtils.hasText(queryFilter)) {
        filter = new SpringExpresssionLanguageFilter(queryFilter,
          FILTER_VARIABLES);
      } else {
        filter = new AcceptAllFilter<DataObject>();
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
    if (parent == null) {
      layer.setRenderer(newRenderer);
    } else {
      parent.removeRenderer(this);
      parent.addRenderer(newRenderer);
    }
    newRenderer.addRenderer(this);
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
