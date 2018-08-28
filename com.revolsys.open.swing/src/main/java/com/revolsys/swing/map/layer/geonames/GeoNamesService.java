package com.revolsys.swing.map.layer.geonames;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.epsg.EpsgId;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;
import com.revolsys.io.PathName;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.json.JsonParser;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;
import com.revolsys.util.UrlUtil;

import si.uom.SI;

public class GeoNamesService {
  public static final RecordDefinition NAME_RECORD_DEFINITION;

  public static final RecordDefinition WIKIPEDIA_RECORD_DEFINITION;

  static {
    final RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(
      PathName.newPathName("/geoname.org/name"));
    recordDefinition.addField("geonameId", DataTypes.STRING, false);
    recordDefinition.addField("name", DataTypes.STRING, false);
    recordDefinition.addField("fcode", DataTypes.STRING, false);
    recordDefinition.addField("fcodeName", DataTypes.STRING, false);
    recordDefinition.addField("fcl", DataTypes.STRING, false);
    recordDefinition.addField("fclName", DataTypes.STRING, false);
    recordDefinition.addField("adminName1", DataTypes.STRING, false);
    recordDefinition.addField("adminName2", DataTypes.STRING, false);
    recordDefinition.addField("adminName3", DataTypes.STRING, false);
    recordDefinition.addField("adminName4", DataTypes.STRING, false);
    recordDefinition.addField("adminCode1", DataTypes.STRING, false);
    recordDefinition.addField("population", DataTypes.INT, false);
    recordDefinition.addField("countryCode", DataTypes.STRING, false);
    recordDefinition.addField("countryName", DataTypes.STRING, false);
    recordDefinition.addField("timeZoneId", DataTypes.STRING, false);
    recordDefinition.addField("geometry", DataTypes.POINT, false);
    NAME_RECORD_DEFINITION = recordDefinition;

    final RecordDefinitionImpl wikipediaRecordDefinition = new RecordDefinitionImpl(
      PathName.newPathName("/geoname.org/wikipedia"));
    wikipediaRecordDefinition.addField("summary", DataTypes.STRING, false);
    wikipediaRecordDefinition.addField("title", DataTypes.STRING, false);
    wikipediaRecordDefinition.addField("wikipediaUrl", DataTypes.STRING, false);
    wikipediaRecordDefinition.addField("countryCode", DataTypes.STRING, false);
    wikipediaRecordDefinition.addField("feature", DataTypes.STRING, false);
    wikipediaRecordDefinition.addField("thumbnailImg", DataTypes.STRING, false);
    wikipediaRecordDefinition.addField("lang", DataTypes.STRING, false);
    wikipediaRecordDefinition.addField("population", DataTypes.INT, false);
    wikipediaRecordDefinition.addField("geometry", DataTypes.POINT, false);

    WIKIPEDIA_RECORD_DEFINITION = wikipediaRecordDefinition;
  }

  private URL findNearbyBoundingBoxJsonUrl;

  private URL searchJsonUrl;

  private URL wikipediaBoundingBoxJsonUrl;

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
    final GeometryFactory geometryFactory = GeometryFactory.floating3d(EpsgId.WGS84);
    final GeographicCoordinateSystem cs = (GeographicCoordinateSystem)geometryFactory
      .getHorizontalCoordinateSystem();
    final BoundingBox geographicBoundingBox = boundingBox.bboxToCs(geometryFactory);
    final Map<String, Object> params = new HashMap<>();

    final double radius = cs.getDatum().getEllipsoid().getSemiMajorAxis();
    final double height = geographicBoundingBox.getHeight();
    final double width = geographicBoundingBox.getWidth();
    final double diagonal = Math.sqrt(width * width + height * height);
    final double radiusKm = cs.getUnit().getConverterTo(SI.RADIAN).convert(diagonal) * radius
      / 1000;

    params.put("lat", geographicBoundingBox.getCentreY());
    params.put("lng", geographicBoundingBox.getCentreY());
    params.put("radius", radiusKm);
    params.put("maxRows", "50");
    final String searchUrlString = UrlUtil.getUrl(this.findNearbyBoundingBoxJsonUrl, params);
    try {
      final URL searchUrl = new URL(searchUrlString);
      final Map<String, Object> result = JsonParser.getMap(searchUrl.openStream());
      return mapToObjects(NAME_RECORD_DEFINITION, result);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to connect to URL:" + searchUrlString);
    }
  }

  public List<Record> getWikipediaArticles(final BoundingBox boundingBox) {
    final BoundingBox geographicBoundingBox = boundingBox.bboxToCs(GeometryFactory.floating3d(EpsgId.WGS84));
    final Map<String, Object> params = new HashMap<>();

    params.put("north", geographicBoundingBox.getMaxY());
    params.put("east", geographicBoundingBox.getMaxX());
    params.put("south", geographicBoundingBox.getMinY());
    params.put("west", geographicBoundingBox.getMinX());
    params.put("maxRows", "50");
    final String searchUrlString = UrlUtil.getUrl(this.wikipediaBoundingBoxJsonUrl, params);
    try {
      final URL searchUrl = new URL(searchUrlString);
      final Map<String, Object> result = JsonParser.getMap(searchUrl.openStream());
      return mapToObjects(WIKIPEDIA_RECORD_DEFINITION, result);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to connect to URL:" + searchUrlString);
    }
  }

  private void init(final URL url) {
    try {
      this.searchJsonUrl = new URL(url.toString() + "/searchJSON");
      this.wikipediaBoundingBoxJsonUrl = new URL(url.toString() + "/wikipediaBoundingBoxJSON");
      this.findNearbyBoundingBoxJsonUrl = new URL(url.toString() + "/findNearbyJSON");
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException("Invalid URL:" + url);
    }
  }

  private List<Record> mapToObjects(final RecordDefinition recordDefinition,
    final Map<String, Object> result) {
    final List<Record> results = new ArrayList<>();
    final List<Map<String, Object>> names = (List<Map<String, Object>>)result.get("geonames");
    for (final Map<String, Object> name : names) {
      final Record record = recordDefinition.newRecord();
      for (final String fieldName : recordDefinition.getFieldNames()) {
        final Object value = name.get(fieldName);
        if (value != null) {
          record.setValue(fieldName, value);
        }
      }
      final double lat = ((Number)name.get("lat")).doubleValue();
      final double lon = ((Number)name.get("lng")).doubleValue();

      Point coordinate = new PointDoubleXY(lon, lat);
      final Number elevation = (Number)name.get("elevation");
      if (elevation == null) {
        coordinate = new PointDoubleXY(lon, lat);
      } else {
        coordinate = new PointDoubleXYZ(lon, lat, elevation.doubleValue());
      }
      record.setGeometryValue(GeometryFactory.DEFAULT_3D.point(coordinate));
      results.add(record);
    }
    return results;
  }

  public List<Record> searchByName(final String name) {
    return searchByName(name, null);
  }

  public List<Record> searchByName(final String name, final String countryCode) {
    final Map<String, String> params = new HashMap<>();
    params.put("name", name);
    params.put("style", "FULL");
    params.put("type", "json");
    final String searchUrlString = UrlUtil.getUrl(this.searchJsonUrl, params);
    try {
      final URL searchUrl = new URL(searchUrlString);
      final Map<String, Object> result = JsonParser.getMap(searchUrl.openStream());
      return mapToObjects(NAME_RECORD_DEFINITION, result);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to connect to URL:" + searchUrlString);
    }
  }
}
