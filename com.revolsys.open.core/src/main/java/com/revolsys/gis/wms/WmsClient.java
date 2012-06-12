package com.revolsys.gis.wms;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.revolsys.gis.wms.capabilities.Parser;
import com.revolsys.gis.wms.capabilities.WmsCapabilities;
import com.revolsys.io.xml.SimpleXmlProcessorContext;
import com.revolsys.io.xml.StaxUtils;
import com.revolsys.io.xml.XmlProcessorContext;
import com.revolsys.util.Base64;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.UrlUtil;
import com.vividsolutions.jts.geom.Envelope;

public class WmsClient {
  private String name;

  private final URL serviceUrl;

  private WmsCapabilities capabilities;

  public WmsClient(final String url) throws MalformedURLException {
    this(new URL(url));
  }

  public WmsClient(final String name, final String url)
    throws MalformedURLException {
    this(name, new URL(url));
  }

  public WmsClient(final String name, final URL serviceUrl) {
    this.name = name;
    this.serviceUrl = serviceUrl;
  }

  public WmsClient(final URL serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  public WmsCapabilities getCapabilities() {
    if (capabilities == null) {
      loadCapabilities();
    }
    return capabilities;
  }

  public Image getMapImage(
    final List<String> layers,
    final List<String> styles,
    final String srid,
    final Envelope envelope,
    final String format,
    final int width,
    final int height) throws IOException {
    final URL mapUrl = getMapUrl(layers, styles, srid, envelope, format, width,
      height);
    final URLConnection connection = mapUrl.openConnection();
    final String userInfo = mapUrl.getUserInfo();
    if (userInfo != null) {
      connection.setRequestProperty("Authorization",
        "Basic " + Base64.encode(userInfo));
    }
    final InputStream in = connection.getInputStream();
    return ImageIO.read(in);
  }

  public URL getMapUrl(
    final List<String> layers,
    final List<String> styles,
    final String srid,
    final Envelope envelope,
    final String format,
    final int width,
    final int height) {
    final String version = getCapabilities().getVersion();
    final Map<String, Object> parameters = new LinkedHashMap<String, Object>();
    if (version.equals("1.0.0")) {
      parameters.put(WmsParameters.WMTVER, version);
      parameters.put(WmsParameters.REQUEST, WmsParameterValues.MAP);
    } else {
      parameters.put(WmsParameters.VERSION, version);
      parameters.put(WmsParameters.REQUEST, WmsParameterValues.GET_MAP);
    }
    parameters.put(WmsParameters.LAYERS, CollectionUtil.toString(layers));
    String style;
    if (styles == null) {
      style = "";
    } else {
      style = CollectionUtil.toString(styles);
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
    final String bbox = envelope.getMinX() + "," + envelope.getMinY() + ","
      + envelope.getMaxX() + "," + envelope.getMaxY();
    parameters.put(WmsParameters.BBOX, bbox);
    parameters.put(WmsParameters.WIDTH, width);
    parameters.put(WmsParameters.HEIGHT, height);
    parameters.put(WmsParameters.FORMAT, format);
    parameters.put(WmsParameters.TRANSPARENT, "TRUE");
    URL requestUrl = getCapabilities().getRequestUrl("GetMap", "GET");
    if (requestUrl == null) {
      requestUrl = serviceUrl;
    }
    final String urlString = UrlUtil.getUrl(requestUrl, parameters);
    try {
      return new URL(urlString);
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException(urlString, e);
    }
  }

  protected String getName() {
    return name;
  }

  public URL getUrl() {
    return serviceUrl;
  }

  public boolean isConnected() {
    return capabilities != null;
  }

  public void loadCapabilities() {
    final Map<String, Object> parameters = new LinkedHashMap<String, Object>();
    parameters.put(WmsParameters.SERVICE, WmsParameterValues.WMS);
    parameters.put(WmsParameters.REQUEST, WmsParameterValues.GET_CAPABILITIES);
    final String urlString = UrlUtil.getUrl(serviceUrl, parameters);

    final XmlProcessorContext context = new SimpleXmlProcessorContext();
    try {
      final URL url = new URL(urlString);
      final InputStream in = url.openStream();
      try {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setXMLReporter(context);
        final XMLStreamReader parser = factory.createXMLStreamReader(in);
        try {
          StaxUtils.skipToStartElement(parser);
          if (parser.getEventType() == XMLStreamConstants.START_ELEMENT) {
            capabilities = (WmsCapabilities)new Parser(context).process(parser);
          }
        } catch (final XMLStreamException e) {
          context.addError(e.getMessage(), e, parser.getLocation());
        }
      } finally {
        in.close();
      }
    } catch (final IOException e) {
      context.addError(e.getMessage(), e, null);
    } catch (final XMLStreamException e) {
      context.addError(e.getMessage(), e, null);
    }
    if (!context.getErrors().isEmpty()) {
      throw new IllegalArgumentException("Capabilities file is invalid"
        + context.getErrors());
    }
  }

  protected void setName(final String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
