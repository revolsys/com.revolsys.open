package com.revolsys.geometry.cs.gridshift.nadcon5;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.util.Exceptions;

public class Nadcon5Region {

  private static final Map<String, Nadcon5Region> REGION_BY_NAME = new LinkedHashMap<>();

  public static final List<String> REGION_NAMES = new ArrayList<>();

  public static final List<Nadcon5Region> REGIONS = new ArrayList<>();

  static {
    addRegion("StGeorge", "20160901", 56.3, 56.8, 190.0, 190.8, "SG1897", "SG1952", "NAD83(1986)",
      "NAD83(1992)", "NAD83(NSRS2007)", Nadcon5.NAD83_CURRENT);
    addRegion("StPaul", "20160901", 56.9, 57.4, 189.3, 190.4, "SP1897", "SP1952", "NAD83(1986)",
      "NAD83(1992)", "NAD83(NSRS2007)", Nadcon5.NAD83_CURRENT);
    addRegion("StLawrence", "20160901", 62.7, 64.0, 187.5, 192.0, "SL1952", "NAD83(1986)",
      "NAD83(1992)", "NAD83(NSRS2007)", Nadcon5.NAD83_CURRENT);
    addRegion("Alaska", "20160901", 50.0, 73.0, 172.0, 232.0, Nadcon5.NAD27, "NAD83(1986)",
      "NAD83(1992)", "NAD83(NSRS2007)", Nadcon5.NAD83_CURRENT);
    addRegion("Conus", "20160901", 24.0, 50.0, 235.0, 294.0, "USSD", Nadcon5.NAD27, "NAD83(1986)",
      "NAD83(HARN)", "NAD83(FBN)", "NAD83(NSRS2007)", Nadcon5.NAD83_CURRENT);
    addRegion("Hawaii", "20160901", 18.0, 23.0, 199.0, 206.0, "OHD", "NAD83(1986)", "NAD83(1993)",
      "NAD83(PA11)");
    addRegion("PRVI", "20160901", 17.0, 19.0, 291.0, 296.0, "PR40", "NAD83(1986)", "NAD83(1993)",
      "NAD83(1997)", "NAD83(2002)", "NAD83(NSRS2007)", Nadcon5.NAD83_CURRENT);
    addRegion("AS", "20160901", -16.0, -13.0, 188.0, 193.0, "AS62", "NAD83(1993)", "NAD83(2002)",
      "NAD83(PA11)");
    addRegion("GuamCNMI", "20160901", 12.0, 22.0, 143.0, 147.0, "GU63", "NAD83(1993)",
      "NAD83(2002)", "NAD83(MA11)");
  }

  private static void addRegion(final String name, final String dateString, final double minY,
    final double maxY, final double minX, final double maxX, final String... datumNames) {
    final Nadcon5Region region = new Nadcon5Region(name, dateString, minX, minY, maxX, maxY,
      datumNames);
    REGIONS.add(region);
    REGION_NAMES.add(name);
    REGION_BY_NAME.put(name, region);

  }

  public static Nadcon5Region getRegion(final double x, final double y) {
    for (final Nadcon5Region region : REGIONS) {
      if (region.covers(x, y)) {
        return region;
      }
    }
    return null;
  }

  public static Nadcon5Region getRegion(final String name) {
    return REGION_BY_NAME.get(name);
  }

  private final BoundingBox boundingBox;

  private final Date date;

  private String dateString;

  private final List<String> datumNames;

  private final Nadcon5FileGrid[] ehtAccuracies;

  private final Nadcon5FileGrid[] ehtShifts;

  private final Nadcon5FileGrid[] latAccuracies;

  private final Nadcon5FileGrid[] latShifts;

  private final Nadcon5FileGrid[] lonAccuracies;

  private final Nadcon5FileGrid[] lonShifts;

  private final String name;

  private final double minX;

  private final double minY;

  private final double maxX;

  private final double maxY;

  public Nadcon5Region(final String name, final String dateString, final double minX,
    final double minY, final double maxX, final double maxY, final String... datumNames) {
    this.name = name;
    try {
      this.dateString = dateString;
      this.date = new Date(new SimpleDateFormat("yyyyMMdd").parse(dateString).getTime());
    } catch (final ParseException e) {
      throw Exceptions.wrap("Invalid date " + dateString, e);
    }
    this.minX = minX;
    this.minY = minY;
    this.maxX = maxX;
    this.maxY = maxY;
    this.boundingBox = GeometryFactory.nad83().newBoundingBox(minX - 360, minY, maxX - 360, maxY);
    this.datumNames = Arrays.asList(datumNames);

    this.lonAccuracies = new Nadcon5FileGrid[datumNames.length - 1];
    this.lonShifts = new Nadcon5FileGrid[datumNames.length - 1];
    this.latAccuracies = new Nadcon5FileGrid[datumNames.length - 1];
    this.latShifts = new Nadcon5FileGrid[datumNames.length - 1];
    this.ehtAccuracies = new Nadcon5FileGrid[datumNames.length - 1];
    this.ehtShifts = new Nadcon5FileGrid[datumNames.length - 1];

    String sourceDatumName = datumNames[0];
    for (int datumIndex = 1; datumIndex < datumNames.length; datumIndex++) {
      final String targetDatumName = datumNames[datumIndex];
      final int fileIndex = datumIndex - 1;
      this.lonAccuracies[fileIndex] = new Nadcon5FileGrid(this, sourceDatumName, targetDatumName,
        "lon", "err");
      this.lonShifts[fileIndex] = new Nadcon5FileGrid(this, sourceDatumName, targetDatumName, "lon",
        "trn");
      this.latAccuracies[fileIndex] = new Nadcon5FileGrid(this, sourceDatumName, targetDatumName,
        "lat", "err");
      this.latShifts[fileIndex] = new Nadcon5FileGrid(this, sourceDatumName, targetDatumName, "lat",
        "trn");
      this.ehtAccuracies[fileIndex] = new Nadcon5FileGrid(this, sourceDatumName, targetDatumName,
        "eht", "err");
      this.ehtShifts[fileIndex] = new Nadcon5FileGrid(this, sourceDatumName, targetDatumName, "eht",
        "trn");
      sourceDatumName = targetDatumName;
    }
  }

  public boolean covers(final double x, final double y) {
    return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY;
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public Date getDate() {
    return this.date;
  }

  public String getDateString() {
    return this.dateString;
  }

  public int getDatumIndex(final String datumName) {
    final int datumIndex = this.datumNames.indexOf(datumName);

    if (datumIndex == -1) {
      throw new IllegalArgumentException("datum=" + datumName + " not found for " + this.name);
    } else {
      return datumIndex;
    }
  }

  public List<String> getDatumNames() {
    return this.datumNames;
  }

  public double getEhtAccuracy(final int fileIndex, final double lon, final double lat) {
    return Math.pow(this.ehtAccuracies[fileIndex].getValueBiquadratic(lon, lat), 2);
  }

  public double getEhtShift(final int fileIndex, final double lon, final double lat) {
    return this.ehtShifts[fileIndex].getValueBiquadratic(lon, lat);
  }

  public double getLatAccuracy(final int fileIndex, final double lon, final double lat) {
    return Math.pow(this.latAccuracies[fileIndex].getValueBiquadratic(lon, lat), 2);
  }

  public double getLatShift(final int fileIndex, final double lon, final double lat) {
    return this.latShifts[fileIndex].getValueBiquadratic(lon, lat) / 3600.0;
  }

  public double getLonAccuracy(final int fileIndex, final double lon, final double lat) {
    return Math.pow(this.lonAccuracies[fileIndex].getValueBiquadratic(lon, lat), 2);
  }

  public double getLonShift(final int fileIndex, final double lon, final double lat) {
    return this.lonShifts[fileIndex].getValueBiquadratic(lon, lat) / 3600.0;
  }

  public String getName() {
    return this.name;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
