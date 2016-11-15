package com.revolsys.gis.wms;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.wms.capabilities.WmsCapabilities;
import com.revolsys.gis.wms.capabilities.WmsLayerDefinition;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.util.Base64;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;
import com.revolsys.util.UrlUtil;
import com.revolsys.webservice.WebService;
import com.revolsys.webservice.WebServiceResource;

public class WmsClient extends BaseObjectWithProperties implements WebService<WmsLayerDefinition> {

  public static final String J_TYPE = "ogcWmsServer";

  public static int getCoordinateSystemId(final String srs) {
    int coordinateSystemId = 4326;
    try {
      final int colonIndex = srs.indexOf(':');
      if (colonIndex != -1) {
        coordinateSystemId = Integer.valueOf(srs.substring(colonIndex + 1));
      }
    } catch (final Throwable e) {
    }
    return coordinateSystemId;
  }

  public static GeometryFactory getGeometryFactory(final String srs) {
    final int coordinateSystemId = getCoordinateSystemId(srs);
    final GeometryFactory geometryFactory = GeometryFactory.floating(coordinateSystemId, 2);
    return geometryFactory;
  }

  public static void mapObjectFactoryInit() {
    MapObjectFactoryRegistry.newFactory(J_TYPE, "OGC WMS Server", WmsClient::newOgcWmsClient);
  }

  public static WmsClient newOgcWmsClient(final Map<String, ? extends Object> properties) {
    final String serviceUrl = (String)properties.get("serviceUrl");
    if (Property.hasValue(serviceUrl)) {
      final WmsClient client = new WmsClient(serviceUrl);
      client.setProperties(properties);
      return client;
    } else {
      throw new IllegalArgumentException("Missing serviceUrl");
    }
  }

  private WmsCapabilities capabilities;

  private String name;

  private final URL serviceUrl;

  public WmsClient(final String url) {
    this(UrlUtil.getUrl(url));
  }

  public WmsClient(final String name, final String url) {
    this(name, UrlUtil.getUrl(url));
  }

  public WmsClient(final String name, final URL serviceUrl) {
    this.name = name;
    this.serviceUrl = serviceUrl;
  }

  public WmsClient(final URL serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  public WmsCapabilities getCapabilities() {
    if (this.capabilities == null) {
      loadCapabilities();
    }
    return this.capabilities;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends WebServiceResource> C getChild(final String name) {
    if (name == null) {
      return null;
    } else {
      final WmsCapabilities capabilities = getCapabilities();
      return (C)capabilities.getLayer(name);
    }
  }

  @Override
  public List<WmsLayerDefinition> getChildren() {
    final WmsCapabilities capabilities = getCapabilities();
    return capabilities.getLayers();
  }

  public WmsLayerDefinition getLayer(final String layerName) {
    final WmsCapabilities capabilities = getCapabilities();
    if (capabilities == null) {
      return null;
    } else {
      return capabilities.getLayer(layerName);
    }
  }

  public GeoreferencedImage getMapImage(final List<String> layers, final List<String> styles,
    final String srid, final BoundingBox boundingBox, final String format, final int width,
    final int height) {
    final URL mapUrl = getMapUrl(layers, styles, srid, boundingBox, format, width, height);
    try {
      final URLConnection connection = mapUrl.openConnection();
      final String userInfo = mapUrl.getUserInfo();
      if (userInfo != null) {
        connection.setRequestProperty("Authorization", "Basic " + Base64.encode(userInfo));
      }
      final InputStream in = connection.getInputStream();
      final BufferedImage image = ImageIO.read(in);
      if (image == null) {
        return new BufferedGeoreferencedImage(boundingBox, width, height);
      } else {
        return new BufferedGeoreferencedImage(boundingBox, image);
      }
    } catch (final IOException e) {
      throw Exceptions.wrap("Error loading: " + mapUrl, e);
    }
  }

  public GeoreferencedImage getMapImage(final String layer, final String style, final String srid,
    final BoundingBox boundingBox, final String format, final int width, final int height) {
    return getMapImage(Collections.singletonList(layer), Collections.singletonList(style), srid,
      boundingBox, format, width, height);
  }

  public URL getMapUrl(final List<String> layers, final List<String> styles, final String srid,
    final BoundingBox envelope, final String format, final int width, final int height) {
    final String version = getCapabilities().getVersion();
    final Map<String, Object> parameters = new LinkedHashMap<>();
    if (version.equals("1.0.0")) {
      parameters.put(WmsParameters.WMTVER, version);
      parameters.put(WmsParameters.REQUEST, WmsParameterValues.MAP);
    } else {
      parameters.put(WmsParameters.VERSION, version);
      parameters.put(WmsParameters.REQUEST, WmsParameterValues.GET_MAP);
    }
    parameters.put(WmsParameters.LAYERS, Strings.toString(layers));
    String style;
    if (styles == null) {
      style = "";
    } else {
      style = Strings.toString(styles);
      for (int i = styles.size(); i < layers.size(); i++) {
        style += ",";
      }
    }

    parameters.put(WmsParameters.STYLES, style);
    if (version.equals("1.3.0")) {
      parameters.put(WmsParameters.CRS, srid);
    } else {
      parameters.put(WmsParameters.SRS, srid);
    }
    final String bbox = envelope.getMinX() + "," + envelope.getMinY() + "," + envelope.getMaxX()
      + "," + envelope.getMaxY();
    parameters.put(WmsParameters.BBOX, bbox);
    parameters.put(WmsParameters.WIDTH, width);
    parameters.put(WmsParameters.HEIGHT, height);
    parameters.put(WmsParameters.FORMAT, format);
    // parameters.put(WmsParameters.EXCEPTIONS, "application/vnd.ogc.se_inimage");
    parameters.put(WmsParameters.TRANSPARENT, "TRUE");
    URL requestUrl = getCapabilities().getRequestUrl("GetMap", "GET");
    if (requestUrl == null) {
      requestUrl = this.serviceUrl;
    }
    final String urlString = UrlUtil.getUrl(requestUrl, parameters);
    try {
      return new URL(urlString);
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException(urlString, e);
    }
  }

  public URL getMapUrl(final String layer, final String style, final String srid,
    final BoundingBox envelope, final String format, final int width, final int height) {
    return getMapUrl(Collections.singletonList(layer), Collections.singletonList(style), srid,
      envelope, format, width, height);
  }

  @Override
  public String getName() {
    return this.name;
  }

  public URL getUrl() {
    return this.serviceUrl;
  }

  public boolean isConnected() {
    return this.capabilities != null;
  }

  public WmsCapabilities loadCapabilities() {
    final Map<String, Object> parameters = new LinkedHashMap<>();
    parameters.put(WmsParameters.SERVICE, WmsParameterValues.WMS);
    parameters.put(WmsParameters.REQUEST, WmsParameterValues.GET_CAPABILITIES);
    final String urlString = UrlUtil.getUrl(this.serviceUrl, parameters);
    try {
      final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setValidating(false);
      documentBuilderFactory.setNamespaceAware(true);
      final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      final Document document = documentBuilder.parse(urlString);
      this.capabilities = new WmsCapabilities(this, document.getDocumentElement());
      return this.capabilities;
    } catch (final Throwable e) {
      throw Exceptions.wrap("Unable to read capabilities: " + urlString, e);
    }
  }

  @Override
  public void refresh() {
    loadCapabilities();
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public MapEx toMap() {
    final MapEx map = newTypeMap(J_TYPE);
    map.put("serviceUrl", this.serviceUrl);
    final String name = getName();
    addToMap(map, "name", name, "");
    return map;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
