package com.revolsys.swing.map.layer.webmercatortilecache;

import java.awt.image.BufferedImage;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.raster.BufferedImages;
import com.revolsys.util.Property;

public class WebMercatorTileCacheClient {
  private static final double[] METRES_PER_PIXEL = {
    156543, 78271.52, 39135.76, 19567.88, 9783.94, 4891.97, 2445.98, 1222.99, 611.4962, 305.7481,
    152.8741, 76.437, 38.2185, 19.1093, 9.5546, 4.7773, 2.3887, 1.1943, 0.5972, 0.2986, 0.1493,
    0.0746, 0.0373, 0.0187
  };

  private final String serverUrl;

  public WebMercatorTileCacheClient(final String serverUrl) {
    if (Property.hasValue(serverUrl)) {
      this.serverUrl = serverUrl;
    } else {
      throw new IllegalArgumentException("Open Street Map tile server URL must be specified");
    }
  }

  public BoundingBox getBoundingBox(final int zoomLevel, final int tileX, final int tileY) {
    final double lon1 = getLongitude(zoomLevel, tileX);
    final double lat1 = getLatitude(zoomLevel, tileY);
    final double lon2 = getLongitude(zoomLevel, tileX + 1);
    final double lat2 = getLatitude(zoomLevel, tileY + 1);
    return GeometryFactory.wgs84()
      .newBoundingBox(lon1, lat1, lon2, lat2)
      .bboxToCs(GeometryFactory.worldMercator());
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
    return BufferedImages.readImageIo(url);
  }

  public BufferedImage getMapImage(final int zoomLevel, final int tileX, final int tileY) {
    final String url = getMapUrl(zoomLevel, tileX, tileY);
    return BufferedImages.readImageIo(url);
  }

  public String getMapUrl(final int zoomLevel, final double longitude, final double latitude) {
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
    final int tileY = (int)Math.floor(
      (1 - Math.log(Math.tan(radians) + 1 / Math.cos(radians)) / Math.PI) / 2 * (1 << zoomLevel));
    return tileY;
  }

  public int getZoomLevel(final double metresPerPixel) {
    for (int i = 0; i < METRES_PER_PIXEL.length; i++) {
      final double levelMetresPerPixel = METRES_PER_PIXEL[i];
      if (metresPerPixel > levelMetresPerPixel) {
        if (i == 0) {
          return 0;
        } else {
          final double ratio = levelMetresPerPixel / metresPerPixel;
          if (ratio < 0.95) {
            return i - 1;
          } else {
            return i;
          }
        }
      }
    }
    return METRES_PER_PIXEL.length - 1;
  }

}
