package com.revolsys.swing.map.layer.geonames;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.unit.SI;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.io.Path;
import com.revolsys.io.json.JsonParser;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.util.UrlUtil;

public class GeoNamesService {
  public static final RecordDefinition NAME_RECORD_DEFINITION;

  public static final RecordDefinition WIKIPEDIA_RECORD_DEFINITION;

  static {
    final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(
      Path.toPath("/geoname.org", "name"));
    recordDefinition.addAttribute("geonameId", DataTypes.STRING, false);
    recordDefinition.addAttribute("name", DataTypes.STRING, false);
    recordDefinition.addAttribute("fcode", DataTypes.STRING, false);
    recordDefinition.addAttribute("fcodeName", DataTypes.STRING, false);
    recordDefinition.addAttribute("fcl", DataTypes.STRING, false);
    recordDefinition.addAttribute("fclName", DataTypes.STRING, false);
    recordDefinition.addAttribute("adminName1", DataTypes.STRING, false);
    recordDefinition.addAttribute("adminName2", DataTypes.STRING, false);
    recordDefinition.addAttribute("adminName3", DataTypes.STRING, false);
    recordDefinition.addAttribute("adminName4", DataTypes.STRING, false);
    recordDefinition.addAttribute("adminCode1", DataTypes.STRING, false);
    recordDefinition.addAttribute("population", DataTypes.INTEGER, false);
    recordDefinition.addAttribute("countryCode", DataTypes.STRING, false);
    recordDefinition.addAttribute("countryName", DataTypes.STRING, false);
    recordDefinition.addAttribute("timeZoneId", DataTypes.STRING, false);
    recordDefinition.addAttribute("geometry", DataTypes.POINT, false);
    NAME_RECORD_DEFINITION = recordDefinition;

    final RecordDefinitionImpl wikipediaRecordDefinition = new RecordDefinitionImpl(
      Path.toPath("/geoname.org", "wikipedia"));
    wikipediaRecordDefinition.addAttribute("summary", DataTypes.STRING, false);
    wikipediaRecordDefinition.addAttribute("title", DataTypes.STRING, false);
    wikipediaRecordDefinition.addAttribute("wikipediaUrl", DataTypes.STRING,
      false);
    wikipediaRecordDefinition.addAttribute("countryCode", DataTypes.STRING,
      false);
    wikipediaRecordDefinition.addAttribute("feature", DataTypes.STRING, false);
    wikipediaRecordDefinition.addAttribute("thumbnailImg", DataTypes.STRING,
      false);
    wikipediaRecordDefinition.addAttribute("lang", DataTypes.STRING, false);
    wikipediaRecordDefinition.addAttribute("population", DataTypes.INTEGER,
      false);
    wikipediaRecordDefinition.addAttribute("geometry", DataTypes.POINT, false);

    WIKIPEDIA_RECORD_DEFINITION = wikipediaRecordDefinition;
  }

  private URL searchJsonUrl;

  private URL wikipediaBoundingBoxJsonUrl;

  private URL findNearbyBoundingBoxJsonUrl;

  public GeoNamesService() {
    final String url = "Http://ws.geonames.org";
    try {
      init(new URL(url));
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException("Invalid URL:" + url);
    }
  }

  public GeoNamesService(final URL url) {
    init(url);
  }

  public List<Record> getNames(final BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = GeometryFactory.floating3(4326);
    final GeographicCoordinateSystem cs = (GeographicCoordinateSystem)geometryFactory.getCoordinateSystem();
    final BoundingBox geographicBoundingBox = boundingBox.convert(geometryFactory);
    final Map<String, Object> params = new HashMap<String, Object>();

    final double radius = cs.getDatum().getSpheroid().getSemiMajorAxis();
    final double height = geographicBoundingBox.getHeight();
    final double width = geographicBoundingBox.getWidth();
    final double diagonal = Math.sqrt(width * width + height * height);
    final double radiusKm = cs.getUnit()
        .getConverterTo(SI.RADIAN)
        .convert(diagonal)
        * radius / 1000;

    params.put("lat", geographicBoundingBox.getCentreY());
    params.put("lng", geographicBoundingBox.getCentreX());
    params.put("radius", radiusKm);
    params.put("maxRows", "50");
    final String searchUrlString = UrlUtil.getUrl(
      this.findNearbyBoundingBoxJsonUrl, params);
    try {
      final URL searchUrl = new URL(searchUrlString);
      final Map<String, Object> result = JsonParser.getMap(searchUrl.openStream());
      return mapToObjects(NAME_RECORD_DEFINITION, result);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to connect to URL:"
          + searchUrlString);
    }
  }

  public List<Record> getWikipediaArticles(final BoundingBox boundingBox) {
    final BoundingBox geographicBoundingBox = boundingBox.convert(GeometryFactory.floating3(4326));
    final Map<String, Object> params = new HashMap<String, Object>();

    params.put("north", geographicBoundingBox.getMaxY());
    params.put("east", geographicBoundingBox.getMaxX());
    params.put("south", geographicBoundingBox.getMinY());
    params.put("west", geographicBoundingBox.getMinX());
    params.put("maxRows", "50");
    final String searchUrlString = UrlUtil.getUrl(
      this.wikipediaBoundingBoxJsonUrl, params);
    try {
      final URL searchUrl = new URL(searchUrlString);
      final Map<String, Object> result = JsonParser.getMap(searchUrl.openStream());
      return mapToObjects(WIKIPEDIA_RECORD_DEFINITION, result);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to connect to URL:"
          + searchUrlString);
    }
  }

  private void init(final URL url) {
    try {
      this.searchJsonUrl = new URL(url.toString() + "/searchJSON");
      this.wikipediaBoundingBoxJsonUrl = new URL(url.toString()
        + "/wikipediaBoundingBoxJSON");
      this.findNearbyBoundingBoxJsonUrl = new URL(url.toString()
        + "/findNearbyJSON");
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException("Invalid URL:" + url);
    }
  }

  private List<Record> mapToObjects(final RecordDefinition recordDefinition,
    final Map<String, Object> result) {
    final List<Record> results = new ArrayList<Record>();
    final List<Map<String, Object>> names = (List<Map<String, Object>>)result.get("geonames");
    for (final Map<String, Object> name : names) {
      final Record record = recordDefinition.createRecord();
      for (final String attributeName : recordDefinition.getAttributeNames()) {
        final Object value = name.get(attributeName);
        if (value != null) {
          record.setValue(attributeName, value);
        }
      }
      final double lat = ((Number)name.get("lat")).doubleValue();
      final double lon = ((Number)name.get("lng")).doubleValue();

      Point coordinate = new PointDouble(lon, lat);
      final Number elevation = (Number)name.get("elevation");
      if (elevation == null) {
        coordinate = new PointDouble(lon, lat);
      } else {
        coordinate = new PointDouble(lon, lat, elevation.doubleValue());
      }
      record.setGeometryValue(GeometryFactory.floating3().point(coordinate));
      results.add(record);
    }
    return results;
  }

  public List<Record> searchByName(final String name) {
    return searchByName(name, null);
  }

  public List<Record> searchByName(final String name, final String countryCode) {
    final Map<String, String> params = new HashMap<String, String>();
    params.put("name", name);
    params.put("style", "FULL");
    params.put("type", "json");
    final String searchUrlString = UrlUtil.getUrl(this.searchJsonUrl, params);
    try {
      final URL searchUrl = new URL(searchUrlString);
      final Map<String, Object> result = JsonParser.getMap(searchUrl.openStream());
      return mapToObjects(NAME_RECORD_DEFINITION, result);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to connect to URL:"
          + searchUrlString);
    }
  }
}
