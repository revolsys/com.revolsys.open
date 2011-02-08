package com.revolsys.gis.moep.io;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.grid.Bcgs20000RectangularMapGrid;
import com.revolsys.gis.grid.UtmRectangularMapGrid;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.io.AbstractObjectWithProperties;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class MoepBinaryIterator extends AbstractObjectWithProperties implements
  Iterator<DataObject> {
  private static final int COMPLEX_LINE = 3;

  private static final int CONSTRUCTION_COMPLEX_LINE = 5;

  private static final int CONSTRUCTION_LINE = 4;

  private static final int POINT = 1;

  private static final int SIMPLE_LINE = 2;

  private static final int TEXT = 6;

  private char actionName;

  private final byte[] buffer = new byte[512];

  private Coordinates center;

  private byte coordinateBytes;

  private DataObject currentDataObject;

  private final DataObjectFactory dataObjectFactory;

  private final MoepDirectoryReader directoryReader;

  private GeometryFactory factory;

  private String featureCode;

  private byte fileType;

  private boolean hasNext = true;

  private final InputStream in;

  private boolean loadNextObject = true;

  private String originalFileType;

  private long position = 0;

  private String mapsheet;

  public MoepBinaryIterator(final MoepDirectoryReader directoryReader,
    final String fileName, final InputStream in,
    final DataObjectFactory dataObjectFactory) {
    this.directoryReader = directoryReader;
    this.dataObjectFactory = dataObjectFactory;
    switch (fileName.charAt(fileName.length() - 5)) {
      case 'd':
        originalFileType = "dem";
      break;
      case 'm':
        originalFileType = "contours";
      break;
      case 'n':
        originalFileType = "nonPositional";
      break;
      case 'p':
        originalFileType = "planimetric";
      break;
      case 'g':
        originalFileType = "toponymy";
      break;
      case 'w':
        originalFileType = "woodedArea";
      break;
      case 's':
        originalFileType = "supplimentary";
      break;
      default:
        originalFileType = "unknown";
      break;
    }
    this.in = new BufferedInputStream(in, 10000);
    this.mapsheet = getMapsheetFromFileName(fileName);
    try {
      loadHeader();
    } catch (final IOException e) {
      throw new IllegalArgumentException("file cannot be opened", e);
    }
  }

  public void close() {
    FileUtil.closeSilent(in);
  }

  private String getMapsheetFromFileName(final String fileName) {
    final File file = new File(fileName);
    final String baseName = FileUtil.getFileNamePrefix(file);
    final Pattern pattern = Pattern.compile("\\d{2,3}[a-z]\\d{3}");
    final Matcher matcher = pattern.matcher(baseName);
    if (matcher.find()) {
      return matcher.group();
    } else {
      return baseName;
    }
  }

  public boolean hasNext() {
    if (!hasNext) {
      return false;
    } else if (loadNextObject) {
      return loadNextRecord() != null;
    } else {
      return true;
    }
  }

  protected DataObject loadDataObject() throws IOException {
    final int featureKey = read();
    if (featureKey != 255) {

      final boolean hasFeatureCode = (featureKey / 100) != 0;
      if (hasFeatureCode) {
        final String featureCode = readString(10);
        if (!featureCode.startsWith("HA9000")) {
          actionName = featureCode.charAt(6);
          this.featureCode = featureCode.substring(0, 6) + "0"
            + featureCode.substring(7);
        } else {
          actionName = 'W';
          this.featureCode = featureCode;
        }
      }

      final int extraParams = featureKey % 100 / 10;
      final int featureType = featureKey % 10;
      final byte numBytes = (byte)read();
      final DataObject object = dataObjectFactory.createDataObject(MoepConstants.META_DATA);
      object.setValue(MoepConstants.MAPSHEET_NAME, mapsheet);
      object.setValue(MoepConstants.FEATURE_CODE, featureCode);
      object.setValue(MoepConstants.ORIGINAL_FILE_TYPE, originalFileType);
      String attribute = null;
      if (numBytes > 0) {
        attribute = readString(numBytes);
        object.setValue(MoepConstants.ATTRIBUTE, attribute);
      }
      switch (featureType) {
        case POINT:
          object.setValue(MoepConstants.DISPLAY_TYPE, "primary");
          final Point point = readPoint(in);
          object.setGeometryValue(point);
          if (extraParams == 1 || extraParams == 3) {
            final int angle = readLEInt(in);
            object.setValue(MoepConstants.ANGLE, new Double(
              getAngle(((double)angle) / 10000)));
          }
        break;
        case CONSTRUCTION_LINE:
        case CONSTRUCTION_COMPLEX_LINE:
          object.setValue(MoepConstants.DISPLAY_TYPE, "constructionLine");
          readLineString(extraParams, object);
        break;
        case SIMPLE_LINE:
        case COMPLEX_LINE:
          object.setValue(MoepConstants.DISPLAY_TYPE, "primaryLine");
          readLineString(extraParams, object);
        break;
        case TEXT:
          object.setValue(MoepConstants.DISPLAY_TYPE, "primary");
          final Point textPoint = readPoint(in);
          object.setGeometryValue(textPoint);
          if (extraParams == 1) {
            final int angle = readLEInt(in);
            final double orientation = getAngle(((double)angle) / 10000.0);
            object.setValue(MoepConstants.ANGLE, orientation);
            JtsGeometryUtil.setGeometryProperty(textPoint,
              MoepConstants.ORIENTATION, orientation);
          }
          final int fontSize = readLEShort(in);
          final int numChars = read();
          final String text = readString(numChars);
          if (attribute != null) {
            final String fontName = new String(attribute.substring(0, 3).trim());
            JtsGeometryUtil.setGeometryProperty(textPoint,
              MoepConstants.FONT_NAME, fontName);
            if (attribute.length() > 3) {
              final String other = new String(attribute.substring(3,
                Math.min(attribute.length(), 5)).trim());
              JtsGeometryUtil.setGeometryProperty(textPoint,
                MoepConstants.OTHER, other);
            }
            if (attribute.length() > 5) {
              final String textGroup = new String(attribute.substring(4, 9)
                .trim());
              JtsGeometryUtil.setGeometryProperty(textPoint,
                MoepConstants.TEXT_GROUP, textGroup);
            }

            object.setValue(MoepConstants.ATTRIBUTE, null);
          }
          JtsGeometryUtil.setGeometryProperty(textPoint,
            MoepConstants.CHARACTER_HEIGHT, fontSize);
          object.setValue(MoepConstants.TEXT, text);
          JtsGeometryUtil.setGeometryProperty(textPoint, MoepConstants.TEXT,
            text);
          JtsGeometryUtil.setGeometryProperty(textPoint, "textType", "TextLine");
        break;
      }

      switch (actionName) {
        case 'W':
          setAdmissionHistory(object, MoepConstants.ADDITION_NEW);
        break;
        case 'Z':
          setAdmissionHistory(object, MoepConstants.ADDITION_MODIFIED);
        break;
        case 'X':
          setRetirementHistory(object,
            MoepConstants.DELETION_WITHOUT_REPLACEMENT);
        break;
        case 'Y':
          setRetirementHistory(object, MoepConstants.DELETION_WITH_REPLACEMENT);
        break;
        default:
          setAdmissionHistory(object, MoepConstants.ADDITION_MODIFIED);
        break;
      }
      currentDataObject = object;
      loadNextObject = false;
      return currentDataObject;
    } else {
      close();
      hasNext = false;
      return null;
    }
  }

  private double getAngle(double angle) {
    double orientation = (90 - angle) % 360;
    if (orientation < 0) {
      orientation = 360 + orientation;
    }
    return orientation;
  }

  private void loadHeader() throws IOException {
    fileType = (byte)read();
    if ((fileType / 100) == 0) {
      coordinateBytes = 2;
    } else {
      fileType %= 100;
      coordinateBytes = 4;
    }
    String mapsheet = readString(11);
    mapsheet = mapsheet.replaceAll("\\.", "").toLowerCase();
    final Bcgs20000RectangularMapGrid bcgsGrid = new Bcgs20000RectangularMapGrid();
    final UtmRectangularMapGrid utmGrid = new UtmRectangularMapGrid();
    final double latitude = bcgsGrid.getLatitude(mapsheet) + 0.05;
    final double longitude = bcgsGrid.getLongitude(mapsheet) - 0.1;
    final int crsId = utmGrid.getNad83Srid(longitude, latitude);
    final CoordinateSystem coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(crsId);

    final String submissionDateString = readString(6);

    final int centreX = readLEInt(in);
    final int centreY = readLEInt(in);
    center = new DoubleCoordinates(centreX, centreY);
    CoordinatesPrecisionModel precisionModel = new SimpleCoordinatesPrecisionModel(
      1);
    factory = new GeometryFactory(coordinateSystem, precisionModel);
    setProperty(IoConstants.GEOMETRY_FACTORY, factory);
  }

  protected DataObject loadNextRecord() {
    try {
      return loadDataObject();
    } catch (final IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Get the next data object read by this reader.
   * 
   * @return The next DataObject.
   * @exception NoSuchElementException If the reader has no more data objects.
   */
  public DataObject next() {
    if (hasNext()) {
      loadNextObject = true;
      return currentDataObject;
    } else {
      throw new NoSuchElementException();
    }
  }

  private int read() throws IOException {
    position++;
    return in.read();
  }

  private LineString readContourLine(final int numCoords) throws IOException {
    final CoordinatesList coords = new DoubleCoordinatesList(numCoords, 2);
    for (int i = 0; i < numCoords; i++) {
      readCoordinate(in, coords, i);
    }
    return factory.createLineString(coords);
  }

  private void readCoordinate(final InputStream in,
    final CoordinatesList coords, final int index) throws IOException {
    for (int i = 0; i < 2; i++) {
      int coordinate;
      if (coordinateBytes == 2) {
        coordinate = readLEShort(in);
      } else {
        coordinate = readLEInt(in);
      }
      coords.setValue(index, i, center.getValue(i) + coordinate);
    }
    if (coords.getNumAxis() > 2) {
      final int z = readLEShort(in);
      coords.setValue(index, 2, z);
    }
  }

  private int readLEInt(final InputStream in) throws IOException {
    final int ch1 = in.read();
    final int ch2 = in.read();
    final int ch3 = in.read();
    final int ch4 = in.read();
    position += 4;
    if ((ch1 | ch2 | ch3 | ch4) < 0) {
      throw new EOFException();
    }
    return ((ch1 << 0) + (ch2 << 8) + (ch3 << 16) + (ch4 << 24));

  }

  private short readLEShort(final InputStream in) throws IOException {
    final int ch1 = in.read();
    final int ch2 = in.read();
    position += 2;
    if ((ch1 | ch2) < 0) {
      throw new EOFException();
    }
    return (short)((ch1 << 0) + (ch2 << 8));

  }

  private void readLineString(final int extraParams, final DataObject object)
    throws IOException {
    int numCoords = 0;
    if (extraParams == 2 || extraParams == 4) {
      numCoords = readLEShort(in);

    } else {
      numCoords = read();
    }
    if (extraParams == 3 || extraParams == 4) {
      final int z = readLEShort(in);
      final LineString line = readContourLine(numCoords);
      object.setGeometryValue(line);
      object.setValue("elevation", new Integer(z));

    } else {
      final LineString line = readSimpleLine(numCoords);
      object.setGeometryValue(line);
    }
  }

  private Point readPoint(final InputStream in) throws IOException {
    final CoordinatesList coords = new DoubleCoordinatesList(1, 3);
    readCoordinate(in, coords, 0);
    return factory.createPoint(coords);
  }

  private LineString readSimpleLine(final int numCoords) throws IOException {
    final CoordinatesList coords = new DoubleCoordinatesList(numCoords, 3);
    for (int i = 0; i < numCoords; i++) {
      readCoordinate(in, coords, i);
    }
    return factory.createLineString(coords);
  }

  private String readString(final int length) throws IOException {
    final int read = in.read(buffer, 0, length);
    if (read > -1) {
      position += read;
      return new String(buffer, 0, read).trim();
    } else {
      return null;
    }
  }

  public void remove() {
  }

  private void setAdmissionHistory(final DataObject object,
    final String reasonForChange) {
    if (directoryReader != null) {
      object.setValue(MoepConstants.ADMIT_SOURCE_DATE,
        directoryReader.getSubmissionDate());
      object.setValue(MoepConstants.ADMIT_INTEGRATION_DATE,
        directoryReader.getIntegrationDate());
      object.setValue(MoepConstants.ADMIT_REVISION_KEY,
        directoryReader.getRevisionKey());
      object.setValue(MoepConstants.ADMIT_SPECIFICATIONS_RELEASE,
        directoryReader.getSpecificationsRelease());
    }
    object.setValue(MoepConstants.ADMIT_REASON_FOR_CHANGE, reasonForChange);
  }

  private void setRetirementHistory(final DataObject object,
    final String reasonForChange) {
    if (directoryReader != null) {
      object.setValue(MoepConstants.RETIRE_SOURCE_DATE,
        directoryReader.getSubmissionDate());
      object.setValue(MoepConstants.RETIRE_INTEGRATION_DATE,
        directoryReader.getIntegrationDate());
      object.setValue(MoepConstants.RETIRE_REVISION_KEY,
        directoryReader.getRevisionKey());
      object.setValue(MoepConstants.RETIRE_SPECIFICATIONS_RELEASE,
        directoryReader.getSpecificationsRelease());
    }
    object.setValue(MoepConstants.RETIRE_REASON_FOR_CHANGE, reasonForChange);
  }
}
