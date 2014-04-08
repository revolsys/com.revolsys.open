package com.revolsys.swing.map.layer.openstreetmap;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;

public class OpenStreetMapClient {
  private static final double[] METRES_PER_PIXEL = {
    78271.517, 39135.7585, 19567.8792, 9783.9396, 4891.9698, 2445.9849,
    1222.9925, 611.4962, 305.7481, 152.8741, 76.437, 38.2185, 19.1093, 9.5546,
    4.7773, 2.3887, 1.1943, 0.5972, 0.2986, 0.1493, 0.0746
  };

  private final String serverUrl;

  public OpenStreetMapClient() {
    this("http://tile.openstreetmap.org/");
  }

  public OpenStreetMapClient(final String serverUrl) {
    this.serverUrl = serverUrl;
  }

  public BoundingBox getBoundingBox(final int zoomLevel, final int tileX,
    final int tileY) {
    final double x1 = getLongitude(zoomLevel, tileX);
    final double y1 = getLatitude(zoomLevel, tileY);
    final double x2 = getLongitude(zoomLevel, tileX + 1);
    final double y2 = getLatitude(zoomLevel, tileY + 1);
    return new BoundingBox(GeometryFactory.wgs84(), x1, y1, x2, y2).convert(GeometryFactory.worldMercator());
  }

  protected BufferedImage getImage(final String url) {
    try {
      final URLConnection connection = new URL(url).openConnection();
      final InputStream in = connection.getInputStream();
      return ImageIO.read(in);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to download image from: " + url, e);
    }
  }

  public double getLatitude(final int zoomLevel, final int tileY) {
    final double n = Math.PI - 2.0 * Math.PI * tileY / Math.pow(2.0, zoomLevel);
    return Math.toDegrees(Math.atan(Math.sinh(n)));
  }

  public double getLongitude(final int zoomLevel, final int tileX) {
    return tileX / Math.pow(2.0, zoomLevel) * 360.0 - 180;
  }

  public BufferedImage getMapImage(final int zoomLevel, final double longitude,
    final double latitude) {
    final String url = getMapUrl(zoomLevel, longitude, latitude);
    return getImage(url);
  }

  public BufferedImage getMapImage(final int zoomLevel, final int tileX,
    final int tileY) {
    final String url = getMapUrl(zoomLevel, tileX, tileY);
    return getImage(url);
  }

  public String getMapUrl(final int zoomLevel, final double longitude,
    final double latitude) {
    final int tileX = getTileX(zoomLevel, longitude);
    final int tileY = getTileY(zoomLevel, latitude);
    return getMapUrl(zoomLevel, tileX, tileY);
  }

  public String getMapUrl(final int zoomLevel, final int tileX, final int tileY) {
    return this.serverUrl + zoomLevel + "/" + tileX + "/" + tileY + ".png";
  }

  public double getResolution(final int zoomLevel) {
    return METRES_PER_PIXEL[zoomLevel];
  }

  public String getServerUrl() {
    return this.serverUrl;
  }

  public int getTileX(final int zoomLevel, final double longitude) {
    final double ratio = (longitude + 180) / 360;
    int tileX = (int)Math.floor(ratio * (1 << zoomLevel));

    if (ratio >= 1) {
      tileX--;
    }
    return tileX;
  }

  public int getTileY(final int zoomLevel, final double latitude) {
    final double radians = Math.toRadians(latitude);
    final int tileY = (int)Math.floor((1 - Math.log(Math.tan(radians) + 1
      / Math.cos(radians))
      / Math.PI)
      / 2 * (1 << zoomLevel));
    return tileY;
  }

  public int getZoomLevel(final double metresPerPixel) {
    double previousLevelMetresPerPixel = METRES_PER_PIXEL[0];
    for (int i = 0; i < METRES_PER_PIXEL.length; i++) {
      final double levelMetresPerPixel = METRES_PER_PIXEL[i];
      if (metresPerPixel > levelMetresPerPixel) {
        if (i == 0) {
          return 0;
        } else {
          final double range = levelMetresPerPixel
            - previousLevelMetresPerPixel;
          final double ratio = (metresPerPixel - previousLevelMetresPerPixel)
            / range;
          if (ratio < 0.8) {
            return i - 1;
          } else {
            return i;
          }
        }
      }
      previousLevelMetresPerPixel = levelMetresPerPixel;
    }
    return METRES_PER_PIXEL.length - 1;
  }

}
