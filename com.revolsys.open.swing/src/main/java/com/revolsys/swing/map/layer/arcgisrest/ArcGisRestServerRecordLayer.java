package com.revolsys.swing.map.layer.arcgisrest;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.io.PathName;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.logging.Logs;
import com.revolsys.record.io.format.esri.rest.ArcGisRestCatalog;
import com.revolsys.record.io.format.esri.rest.map.RecordLayerDescription;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.renderer.AbstractRecordLayerRenderer;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.record.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.record.renderer.MultipleRenderer;
import com.revolsys.swing.map.layer.record.renderer.TextStyleRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.Menus;
import com.revolsys.util.Property;

public class ArcGisRestServerRecordLayer extends AbstractRecordLayer {
  private static final String J_TYPE = "arcGisRestServerRecordLayer";

  private static void actionAddLayer(final RecordLayerDescription layerDescription) {
    final Project project = Project.get();
    if (project != null) {

      LayerGroup layerGroup = project;
      final PathName layerPath = layerDescription.getPathName();
      for (final String groupName : layerPath.getParent().getElements()) {
        layerGroup = layerGroup.addLayerGroup(groupName);
      }
      final ArcGisRestServerRecordLayer layer = new ArcGisRestServerRecordLayer(layerDescription);
      layerGroup.addLayer(layer);
    }
  }

  public static Color getColor(final MapEx properties) {
    final String fieldName = "color";
    return getColor(properties, fieldName);
  }

  public static Color getColor(final MapEx properties, final String fieldName) {
    final List<Number> colorValues = properties.getValue(fieldName);
    if (colorValues != null && colorValues.size() == 4) {
      final int red = colorValues.get(0).intValue();
      final int green = colorValues.get(1).intValue();
      final int blue = colorValues.get(2).intValue();
      final int alpha = colorValues.get(3).intValue();
      return new Color(red, green, blue, alpha);
    }
    return null;
  }

  public static void mapObjectFactoryInit() {
    MapObjectFactoryRegistry.newFactory(J_TYPE, "Arc GIS REST Server Record Layer",
      ArcGisRestServerRecordLayer::new);

    final MenuFactory recordLayerDescriptionMenu = MenuFactory
      .getMenu(RecordLayerDescription.class);

    Menus.addMenuItem(recordLayerDescriptionMenu, "default", "Add Layer", "map_add",
      ArcGisRestServerRecordLayer::actionAddLayer);
  }

  private RecordLayerDescription layerDescription;

  private String url;

  private PathName layerPath;

  public ArcGisRestServerRecordLayer() {
    super(J_TYPE);
  }

  public ArcGisRestServerRecordLayer(final Map<String, ? extends Object> properties) {
    this();
    setProperties(properties);
  }

  public ArcGisRestServerRecordLayer(final RecordLayerDescription layerDescription) {
    this();
    setLayerDescription(layerDescription);
  }

  private void addTextRenderer(final List<AbstractRecordLayerRenderer> renderers,
    final MapEx labelProperties) {
    final TextStyle textStyle = new TextStyle();
    final String alignment = labelProperties.getString("labelPlacement");
    if (alignment.endsWith("Left")) {
      textStyle.setTextHorizontalAlignment("right");
    } else if (alignment.endsWith("Right")) {
      textStyle.setTextHorizontalAlignment("left");
    } else if (alignment.endsWith("Before")) {
      textStyle.setTextHorizontalAlignment("right");
      textStyle.setTextPlacementType("vertex(0)");
    } else if (alignment.endsWith("Start")) {
      textStyle.setTextHorizontalAlignment("left");
      textStyle.setTextPlacementType("vertex(0)");
    } else if (alignment.endsWith("After")) {
      textStyle.setTextHorizontalAlignment("left");
      textStyle.setTextPlacementType("vertex(n)");
    } else if (alignment.endsWith("End")) {
      textStyle.setTextHorizontalAlignment("right");
      textStyle.setTextPlacementType("vertex(n)");
    } else {
      textStyle.setTextHorizontalAlignment("center");
    }
    if (alignment.contains("Above")) {
      textStyle.setTextVerticalAlignment("bottom");
    } else if (alignment.endsWith("Below")) {
      textStyle.setTextVerticalAlignment("top");
    } else {
      textStyle.setTextVerticalAlignment("center");
    }

    final String textName = labelProperties.getString("labelExpression");
    textStyle.setTextName(textName);
    final MapEx symbol = labelProperties.getValue("symbol");
    if ("esriTS".equals(symbol.getString("type"))) {
      final Color textFill = getColor(symbol);
      textStyle.setTextFill(textFill);

      final Color backgroundColor = getColor(symbol, "backgroundColor");
      textStyle.setTextBoxColor(backgroundColor);

      // "useCodedValues": false,
      // "borderLineColor": null,
      // "verticalAlignment": "bottom",
      // "horizontalAlignment": "left",
      // "rightToLeft": false,
      // "kerning": true,

      final double angle = symbol.getDouble("angle", 0);
      textStyle.setTextOrientation(angle);

      final Measure<Length> textDx = Measure.valueOf(symbol.getDouble("xoffset", 0), NonSI.PIXEL);
      textStyle.setTextDx(textDx);

      final Measure<Length> textDy = Measure.valueOf(symbol.getDouble("yoffset", 0), NonSI.PIXEL);
      textStyle.setTextDx(textDy);

      final MapEx font = symbol.getValue("font");
      if (font != null) {
        final String faceName = font.getString("family", "Arial");
        textStyle.setTextFaceName(faceName);

        final Measure<Length> size = Measure.valueOf(font.getDouble("size", 10), NonSI.PIXEL);
        textStyle.setTextSize(size);

      }

      // "font": {
      // "style": "normal",
      // "weight": "bold",
      // "decoration": "none"
    }
    final TextStyleRenderer textRenderer = new TextStyleRenderer(this, textStyle);

    long minimumScale = labelProperties.getLong("minScale", Long.MAX_VALUE);
    if (minimumScale == 0) {
      minimumScale = Long.MAX_VALUE;
    }
    textRenderer.setMinimumScale(minimumScale);
    final long maximumScale = labelProperties.getLong("maxScale", 0);
    textRenderer.setMaximumScale(maximumScale);

    final String where = labelProperties.getString("where");
    textRenderer.setQueryFilter(where);

    renderers.add(textRenderer);
  }

  @Override
  public ArcGisRestServerRecordLayer clone() {
    final ArcGisRestServerRecordLayer clone = (ArcGisRestServerRecordLayer)super.clone();
    return clone;
  }

  public RecordLayerDescription getLayerDescription() {
    return this.layerDescription;
  }

  public PathName getLayerPath() {
    return this.layerPath;
  }

  @Override
  public List<LayerRecord> getRecords(BoundingBox boundingBox) {
    if (hasGeometryField()) {
      boundingBox = convertBoundingBox(boundingBox);
      if (Property.hasValue(boundingBox)) {
        return this.layerDescription.getRecords(this::newLayerRecord, boundingBox);
      }
    }
    return Collections.emptyList();
  }

  public String getUrl() {
    return this.url;
  }

  @Override
  protected boolean initializeDo() {
    RecordLayerDescription layerDescription = getLayerDescription();
    if (layerDescription == null) {
      final String url = getUrl();
      final PathName layerPath = getLayerPath();

      if (url == null) {
        Logs.error(this, "An ArcGIS Rest server requires a url: " + getPath());
        return false;
      }
      if (layerPath == null) {
        Logs.error(this, "An ArcGIS Rest server requires a layerPath: " + getPath());
        return false;
      }
      ArcGisRestCatalog server;
      try {
        server = new ArcGisRestCatalog(url);
      } catch (final Throwable e) {
        Logs.error(this, "Unable to connect to server: " + url + " for " + getPath(), e);
        return false;
      }
      try {
        layerDescription = server.getCatalogElement(layerPath, RecordLayerDescription.class);
      } catch (final IllegalArgumentException e) {
        Logs.error(this, "ArcGIS Rest service is not a layer " + getPath(), e);
        return false;
      }
      if (layerDescription == null) {
        Logs.error(this, "No ArcGIS Rest layer with name: " + layerPath + " for " + getPath());
        return false;
      } else {
        setLayerDescription(layerDescription);
      }
    }

    if (layerDescription != null) {
      initRenderer();
      final RecordDefinition recordDefinition = layerDescription.getRecordDefinition();
      if (recordDefinition != null) {
        setRecordDefinition(recordDefinition);
        setBoundingBox(layerDescription.getBoundingBox());
        return super.initializeDo();
      }
    }
    return false;
  }

  private void initRenderer() {
    final MapEx drawingInfo = this.layerDescription.getValue("drawingInfo");
    final MapEx rendererProperties = drawingInfo.getValue("renderer");
    final List<AbstractRecordLayerRenderer> renderers = new ArrayList<>();
    if (rendererProperties != null) {
      final String rendererType = rendererProperties.getString("type");
      if ("simple".equals(rendererType)) {
        final MapEx symbolProperties = rendererProperties.getValue("symbol");
        final String symbolType = symbolProperties.getString("type");
        if ("esriSMS".equals(symbolType)) {
          renderers.add(newSimpleMarkerRenderer(symbolProperties));
        } else if ("esriSLS".equals(symbolType)) {
          renderers.add(newSimpleLineRenderer(symbolProperties));
        }
      }
    }
    final List<MapEx> labellingInfo = drawingInfo.getValue("labelingInfo");
    if (labellingInfo != null) {
      for (final MapEx labelProperties : labellingInfo) {
        addTextRenderer(renderers, labelProperties);
      }
    }
    if (renderers.size() == 1) {
      setRenderer(renderers.get(0));
    } else {
      setRenderer(new MultipleRenderer(this, renderers));
    }
  }

  private AbstractRecordLayerRenderer newSimpleLineRenderer(final MapEx symbolProperties) {
    final double lineWidth = symbolProperties.getDouble("width", 10.0);
    final Color lineColor = getColor(symbolProperties);

    final GeometryStyle markerStyle = GeometryStyle.line(lineColor, lineWidth);
    return new GeometryStyleRenderer(this, markerStyle);
  }

  private AbstractRecordLayerRenderer newSimpleMarkerRenderer(final MapEx symbolProperties) {
    String markerName = symbolProperties.getString("style", "esriSMSCirlce");
    markerName = markerName.replace("esriSMS", "").toLowerCase();
    final int markerSize = symbolProperties.getInteger("size", 10);
    final Color markerFill = getColor(symbolProperties);
    Color markerColor = new Color(0, 0, 0, 0);
    final MapEx outline = symbolProperties.getValue("outline");
    int lineWidth = 0;
    if (outline != null) {
      markerColor = getColor(outline);
      lineWidth = outline.getInteger("width", lineWidth);
    }
    final MarkerStyle markerStyle = MarkerStyle.marker(markerName, markerSize, markerColor,
      lineWidth, markerFill);
    return new MarkerStyleRenderer(this, markerStyle);
  }

  public void setLayerDescription(final RecordLayerDescription layerDescription) {
    this.layerDescription = layerDescription;
    if (layerDescription != null) {
      setName(layerDescription.getName());

      final String url = layerDescription.getRootServiceUrl();
      setUrl(url);

      final PathName pathName = layerDescription.getPathName();
      setLayerPath(pathName);
    }
  }

  public void setLayerPath(final PathName layerPath) {
    this.layerPath = layerPath;
  }

  public void setUrl(final String url) {
    this.url = url;
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addToMap(map, "url", this.url);
    addToMap(map, "layerPath", this.layerPath);
    return map;
  }
}
