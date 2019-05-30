package org.jeometry.coordinatesystem.model.systems;

public class EpsgId {

  public static final int NAD27 = 4267;

  public static final int NAD83 = 4269;

  public static final int WGS84 = 4326;

  public static final int MSL_HEIGHT_FOOT = 8050;

  public static final int MSL_HEIGHT_METRE = 5714;

  public static final int NAVD88_HEIGHT_METRE = 5703;

  public static final int NAVD88_HEIGHT_FOOT = 8228;

  public static final int NGVD29_HEIGHT_FOOT_US = 5702;

  public static final int NGVD29_HEIGHT_METRE = 7968;

  public static final int nad27Utm(final int zone) {
    if (zone >= 1 && zone <= 22) {
      return 26700 + zone;
    } else {
      throw new IllegalArgumentException("Invalid NAD27 / UTM zone " + zone);
    }
  }

  public static final int nad83Utm(final int zone) {
    if (zone >= 1 && zone <= 23) {
      return 26900 + zone;
    } else {
      throw new IllegalArgumentException("Invalid NAD83 / UTM zone " + zone);
    }
  }

  public static final int wgs72Utm(final int zone) {
    if (zone >= 1 && zone <= 60) {
      return 32200 + zone;
    } else {
      throw new IllegalArgumentException("Invalid WGS 72 / UTM zone " + zone);
    }
  }

  public static final int wgs84Utm(final int zone) {
    if (zone >= 1 && zone <= 60) {
      return 32600 + zone;
    } else {
      throw new IllegalArgumentException("Invalid WGS 84 / UTM zone " + zone);
    }
  }

  private EpsgId() {
  }
}
