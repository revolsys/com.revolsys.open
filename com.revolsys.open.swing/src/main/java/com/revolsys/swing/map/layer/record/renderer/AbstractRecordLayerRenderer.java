package com.revolsys.swing.map.layer.record.renderer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.TopologyException;
import com.revolsys.geometry.model.coordinates.PointWithOrientation;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.predicate.Predicates;
import com.revolsys.record.Record;
import com.revolsys.record.filter.MultipleAttributeValuesFilter;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionProxy;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.menu.TreeItemScaleMenu;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.RecordDefinitionSqlFilter;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.util.Cancellable;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public abstract class AbstractRecordLayerRenderer extends AbstractLayerRenderer<AbstractRecordLayer>
  implements RecordDefinitionProxy {
  public static final Pattern PATTERN_INDEX_FROM_END = Pattern.compile("n(?:\\s*-\\s*(\\d+)\\s*)?");

  public static final Pattern PATTERN_SEGMENT_INDEX = Pattern.compile("segment\\((.*)\\)");

  public static final Pattern PATTERN_VERTEX_INDEX = Pattern.compile("vertex\\((.*)\\)");

  static {
    final MenuFactory menu = MenuFactory.getMenu(AbstractRecordLayerRenderer.class);

    menu.addMenuItem("layer", -1, "View/Edit Style", "palette",
      ((Predicate<AbstractRecordLayerRenderer>)AbstractRecordLayerRenderer::isEditing).negate(),
      AbstractRecordLayerRenderer::showProperties, false);

    menu.addMenuItem("layer", -1, "Delete", "delete", AbstractRecordLayerRenderer::isHasParent,
      AbstractRecordLayerRenderer::delete, true);

    menu.addComponentFactory("scale", new TreeItemScaleMenu<>(true, null,
      AbstractRecordLayerRenderer::getMinimumScale, AbstractRecordLayerRenderer::setMinimumScale));
    menu.addComponentFactory("scale", new TreeItemScaleMenu<>(false, null,
      AbstractRecordLayerRenderer::getMaximumScale, AbstractRecordLayerRenderer::setMaximumScale));

    menu.addMenuItem("wrap", "Wrap With Multiple Style", "style_multiple_wrap",
      AbstractRecordLayerRenderer::wrapWithMultipleStyle, false);

    menu.addMenuItem("wrap", "Wrap With Filter Style", "style_filter_wrap",
      AbstractRecordLayerRenderer::wrapWithFilterStyle, false);

    menu.addMenuItem("wrap", "Wrap With Scale Style", "style_scale_wrap",
      AbstractRecordLayerRenderer::wrapWithScaleStyle, false);
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

  protected static int getIndex(final Matcher matcher) {
    int index;
    final String argument = matcher.group(1);
    if (PATTERN_INDEX_FROM_END.matcher(argument).matches()) {
      final String indexString = argument.replaceAll("[^0-9\\-]+", "");
      if (indexString.isEmpty()) {
        index = -1;
      } else {
        index = Integer.parseInt(indexString) - 1;
      }
    } else {
      index = Integer.parseInt(argument);
    }
    return index;
  }

  public static PointWithOrientation getPointWithOrientation(final Viewport2D viewport,
    final Geometry geometry, final String placementType) {
    if (viewport == null) {
      return new PointWithOrientation(new PointDouble(0.0, 0.0), 0);
    } else {
      final GeometryFactory viewportGeometryFactory = viewport.getGeometryFactory();
      if (viewportGeometryFactory != null && geometry != null && !geometry.isEmpty()) {
        Point point = null;
        double orientation = 0;
        if (geometry instanceof Point) {
          point = (Point)geometry;
        } else {
          final Matcher vertexIndexMatcher = PATTERN_VERTEX_INDEX.matcher(placementType);
          if (vertexIndexMatcher.matches()) {
            final int vertexCount = geometry.getVertexCount();
            final int vertexIndex = getIndex(vertexIndexMatcher);
            if (vertexIndex >= -vertexCount && vertexIndex < vertexCount) {
              final Vertex vertex = geometry.getVertex(vertexIndex);
              orientation = vertex.getOrientaton(viewportGeometryFactory);
              point = viewportGeometryFactory.convertGeometry(vertex, 2);
            }
          } else {
            final Matcher segmentIndexMatcher = PATTERN_SEGMENT_INDEX.matcher(placementType);
            if (segmentIndexMatcher.matches()) {
              final int segmentCount = geometry.getSegmentCount();
              if (segmentCount > 0) {
                final int index = getIndex(segmentIndexMatcher);
                LineSegment segment = geometry.getSegment(index);
                segment = viewportGeometryFactory.convertGeometry(segment, 2);
                if (segment != null) {
                  point = segment.midPoint();
                  orientation = segment.getOrientaton();
                }
              }
            } else {
              PointWithOrientation pointWithOrientation = getPointWithOrientationCentre(
                viewportGeometryFactory, geometry);
              if (!viewport.getBoundingBox().covers(pointWithOrientation)) {
                try {
                  final Geometry clippedGeometry = viewport.getBoundingBox()
                    .toPolygon()
                    .intersection(geometry);
                  if (!clippedGeometry.isEmpty()) {
                    double maxArea = 0;
                    double maxLength = 0;
                    for (int i = 0; i < clippedGeometry.getGeometryCount(); i++) {
                      final Geometry part = clippedGeometry.getGeometry(i);
                      if (part instanceof Polygon) {
                        final double area = part.getArea();
                        if (area > maxArea) {
                          maxArea = area;
                          pointWithOrientation = getPointWithOrientationCentre(
                            viewportGeometryFactory, part);
                        }
                      } else if (part instanceof LineString) {
                        if (maxArea == 0 && "auto".equals(placementType)) {
                          final double length = part.getLength();
                          if (length > maxLength) {
                            maxLength = length;
                            pointWithOrientation = getPointWithOrientationCentre(
                              viewportGeometryFactory, part);
                          }
                        }
                      } else if (part instanceof Point) {
                        if (maxArea == 0 && maxLength == 0 && "auto".equals(placementType)) {
                          pointWithOrientation = getPointWithOrientationCentre(
                            viewportGeometryFactory, part);
                        }
                      }
                    }
                  }
                } catch (final Throwable t) {
                }
              }
              return pointWithOrientation;
            }
          }
        }
        if (Property.hasValue(point)) {
          if (viewport.getBoundingBox().covers(point)) {
            point = point.convertPoint2d(viewportGeometryFactory);
            return new PointWithOrientation(point, orientation);
          }
        }
      }
      return null;
    }
  }

  public static PointWithOrientation getPointWithOrientationCentre(
    final GeometryFactory viewportGeometryFactory, final Geometry geometry) {
    double orientation = 0;
    Point point = null;
    if (geometry instanceof LineString) {
      final LineString line = geometry.convertGeometry(viewportGeometryFactory, 2);

      final double totalLength = line.getLength();
      final double centreLength = totalLength / 2;
      double currentLength = 0;
      for (final Segment segment : line.segments()) {
        final double segmentLength = segment.getLength();
        currentLength += segmentLength;
        if (currentLength >= centreLength) {
          final double segmentFraction = 1 - (currentLength - centreLength) / segmentLength;
          point = segment.pointAlong(segmentFraction);
          orientation = segment.getOrientaton();
          break;
        }
      }
    } else {
      point = geometry.getPointWithin();
      point = point.convertPoint2d(viewportGeometryFactory);
    }
    return new PointWithOrientation(point, orientation);
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
    if (parent instanceof AbstractMultipleRecordLayerRenderer) {
      final AbstractMultipleRecordLayerRenderer multiple = (AbstractMultipleRecordLayerRenderer)parent;
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
  public void render(final Viewport2D viewport, final Cancellable cancellable,
    final AbstractRecordLayer layer) {
    if (layer.hasGeometryField()) {
      final BoundingBox boundingBox = viewport.getBoundingBox();
      final List<LayerRecord> records = layer.getRecordsBackground(boundingBox);
      try (
        BaseCloseable transformCloseable = viewport.setUseModelCoordinates(true)) {
        renderRecords(viewport, cancellable, layer, records);
      }
    }
  }

  public void renderRecord(final Viewport2D viewport, final BoundingBox visibleArea,
    final AbstractLayer layer, final LayerRecord record) {
  }

  protected void renderRecords(final Viewport2D viewport, final Cancellable cancellable,
    final AbstractRecordLayer layer, final List<LayerRecord> records) {
    final BoundingBox visibleArea = viewport.getBoundingBox();
    for (final LayerRecord record : cancellable.cancellable(records)) {
      if (record != null) {
        if (isVisible(record) && !layer.isHidden(record)) {
          try {
            renderRecord(viewport, visibleArea, layer, record);
          } catch (final TopologyException e) {
          } catch (final Throwable e) {
            if (!cancellable.isCancelled()) {
              Logs.error(this,
                "Unabled to render " + layer.getName() + " #" + record.getIdentifier(), e);
            }
          }
        }
      }
    }
  }

  public void renderSelectedRecord(final Viewport2D viewport, final AbstractLayer layer,
    final LayerRecord record) {
    final BoundingBox boundingBox = viewport.getBoundingBox();
    if (isVisible(record)) {
      try {
        renderRecord(viewport, boundingBox, layer, record);
      } catch (final TopologyException e) {
      }
    }
  }

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

  protected void wrap(final AbstractLayer layer, final AbstractMultipleRecordLayerRenderer parent,
    final AbstractMultipleRecordLayerRenderer newRenderer) {
    newRenderer.addRenderer(this.clone());
    replace(layer, parent, newRenderer);
  }

  public FilterMultipleRenderer wrapWithFilterStyle() {
    final AbstractRecordLayer layer = getLayer();
    final AbstractMultipleRecordLayerRenderer parent = (AbstractMultipleRecordLayerRenderer)getParent();
    final FilterMultipleRenderer newRenderer = new FilterMultipleRenderer(layer, parent);
    wrap(layer, parent, newRenderer);
    return newRenderer;
  }

  public MultipleRecordRenderer wrapWithMultipleStyle() {
    final AbstractRecordLayer layer = getLayer();
    final AbstractMultipleRecordLayerRenderer parent = (AbstractMultipleRecordLayerRenderer)getParent();
    final MultipleRecordRenderer newRenderer = new MultipleRecordRenderer(layer, parent);
    wrap(layer, parent, newRenderer);
    return newRenderer;
  }

  public ScaleMultipleRenderer wrapWithScaleStyle() {
    final AbstractRecordLayer layer = getLayer();
    final AbstractMultipleRecordLayerRenderer parent = (AbstractMultipleRecordLayerRenderer)getParent();
    final ScaleMultipleRenderer newRenderer = new ScaleMultipleRenderer(layer, parent);
    wrap(layer, parent, newRenderer);
    return newRenderer;
  }
}
