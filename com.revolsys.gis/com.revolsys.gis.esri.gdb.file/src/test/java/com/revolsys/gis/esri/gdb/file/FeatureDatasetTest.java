package com.revolsys.gis.esri.gdb.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Test;

import com.revolsys.gis.data.io.DataObjectStoreSchema;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.esri.gdb.file.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.swig.EsriFileGdb;
import com.revolsys.gis.esri.gdb.file.swig.Geodatabase;
import com.revolsys.gis.esri.gdb.file.swig.Point;
import com.revolsys.gis.esri.gdb.file.swig.PointShapeBuffer;
import com.revolsys.gis.esri.gdb.file.swig.Row;
import com.revolsys.gis.esri.gdb.file.swig.ShapeType;
import com.revolsys.gis.esri.gdb.file.swig.Table;
import com.vividsolutions.jts.geom.Geometry;

public class FeatureDatasetTest {
  static {
    System.loadLibrary("EsriFileGdb");
  }

  public static long copy(final Reader in, final Writer out) throws IOException {
    final char[] buffer = new char[4906];
    long numBytes = 0;
    int count;
    while ((count = in.read(buffer)) > -1) {
      out.write(buffer, 0, count);
      numBytes += count;
    }
    return numBytes;
  }

  public String getResourceAsString(String name) throws IOException {
    InputStream in = getClass().getResourceAsStream(name);
    if (in == null) {
      throw new IllegalArgumentException(name);
    } else {
      Reader reader = new InputStreamReader(in);
      StringWriter writer = new StringWriter();
      copy(reader, writer);
      reader.close();
      return writer.toString();
    }
  }

  @Test
  public void testSchemas() throws Exception {
    String datasetName = "data/Topo.gdb";
    final EsriFileGeodatabaseDataObjectStore dataStore = new EsriFileGeodatabaseDataObjectStore(
      datasetName);
    dataStore.initialize();
    for (DataObjectStoreSchema schema : dataStore.getSchemas()) {
      System.out.println(schema.getName());
      for (DataObjectMetaData metaData : schema.getTypes()) {
        System.out.println(metaData.getName());
        final com.revolsys.gis.data.io.Reader<DataObject> reader = dataStore.query(metaData.getName());
        try {
          for (DataObject dataObject : reader) {
            final Geometry geometry = dataObject.getGeometryValue();
            if (geometry==null || geometry.isEmpty()) {
              System.out.println(dataObject);
            }
          }
        } finally {
          reader.close();
        }
      }
    }

  }

  @Test
  public void testFeatureDatasets() throws Exception {
    String datasetName = "target/FeatureDatasetDemo.gdb";
    int hr;
    Geodatabase geodatabase = new Geodatabase();
    hr = EsriFileGdb.DeleteGeodatabase(datasetName);
    if (hr == 0) {
      System.out.println("The geodatabase has been deleted");
    } else if (hr == -2147024893) {
      System.out.println("The geodatabase does not exist, no need to delete");
    } else {
      System.out.println("An error occurred while deleting the geodatabase."
        + EsriFileGdb.getErrorDescription(hr));
    }

    hr = EsriFileGdb.CreateGeodatabase(datasetName, geodatabase);
    if (hr != 0) {
      throw new RuntimeException(
        "An error occurred while creating the geodatabase.");
    }
    System.out.println("The geodatabase has been created.");

    String featureDatasetDef = getResourceAsString("/TransitFD.xml");
    // Create the feature dataset.
    hr = geodatabase.CreateFeatureDataset(featureDatasetDef);
    if (hr != 0) {
      throw new RuntimeException(
        "An error occurred while creating the feature dataset."
          + EsriFileGdb.getErrorDescription(hr));
    }
    System.out.println("The feature dataset has been created.");

    String tableDef = getResourceAsString("/BusStopsTable.xml");

    Table table = new Table();
    hr = geodatabase.CreateTable(tableDef, "\\Transit", table);
    if (hr != 0) {
      throw new RuntimeException("An error occurred while creating the table."
        + EsriFileGdb.getErrorDescription(hr));
    }
    System.out.println("The table has been created.");

    hr = geodatabase.CloseTable(table);
    if (hr != 0) {
      throw new RuntimeException("An error occurred while closing Cities.");
    }

    // Close the geodatabase
    hr = EsriFileGdb.CloseGeodatabase(geodatabase);
    if (hr != 0) {
      throw new RuntimeException(
        "An error occurred while closing the geodatabase.");
    }

  }

  @Test
  public void testShapes() {
    int hr;
    String errorText;

    // Open the geodatabase.
    Geodatabase geodatabase = new Geodatabase();
    if ((hr = EsriFileGdb.OpenGeodatabase("data/Shapes.gdb", geodatabase)) != 0) {
      throw new RuntimeException(
        "An error occurred while opening the geodatabase."
          + EsriFileGdb.getErrorDescription(hr));

    }

    System.out.println('\n' + "Testing Point Shapes, Read" + '\n' + '\n');

    // Open the point test table, cities.
    Table citiesTable = geodatabase.openTable("\\cities");

    // Return all rows.
    EnumRows enumRows = new EnumRows();
    hr = citiesTable.Search("*", "CITY_NAME = 'Woodinville'", true, enumRows);
    if (hr != 0) {
      System.out.println("An error occurred while performing the attribute query." + '\n');
      errorText = EsriFileGdb.getErrorDescription(hr);
      throw new RuntimeException(errorText + "(" + hr + ")." + '\n');
    }

    // Get the first returned row.
    Row row = enumRows.next();

    PointShapeBuffer pointGeometry = new PointShapeBuffer();
    row.GetGeometry(pointGeometry);

    Point point = pointGeometry.getPoint();
    System.out.println("Point test:" + '\n');
    System.out.println("x: " + point.getX() + " y: " + point.getY() + '\n');

    enumRows.Close();

    // Close the cities table
    hr = geodatabase.CloseTable(citiesTable);
    if (hr != 0) {
      System.out.println("An error occurred while closing the cities table." + '\n');
      errorText = EsriFileGdb.getErrorDescription(hr);
      throw new RuntimeException(errorText + "(" + hr + ")." + '\n');
    }

    System.out.println('\n' + "Testing Point Shapes, Write" + '\n' + '\n');

    // Open the point test table, cities2.
    Table cities2Table = geodatabase.openTable("\\cities2");
    if (hr != 0) {
      System.out.println("An error occurred while opening the table, cities2." + '\n');
      errorText = EsriFileGdb.getErrorDescription(hr);
      throw new RuntimeException(errorText + "(" + hr + ")." + '\n');
    }

    // Begin load only mode. This shuts off the update of all indexes.
    cities2Table.LoadOnlyMode(true);

    // Create a new feature.
    Row cities2Row = new Row();
    cities2Table.CreateRowObject(cities2Row);

    PointShapeBuffer point2Geometry = new PointShapeBuffer();
    point2Geometry.Setup(ShapeType.shapePoint);

    Point point2 = point2Geometry.getPoint();
    point2.setX(point.getX());
    point2.setY(point.getY());

    hr = cities2Row.SetGeometry(point2Geometry);

    hr = cities2Table.Insert(cities2Row);
    if (hr != 0) {
      System.out.println("An error occurred while inserting a row." + '\n');
      errorText = EsriFileGdb.getErrorDescription(hr);
      throw new RuntimeException(errorText + "(" + hr + ")." + '\n');
    } else {
      System.out.println("Inserted a point." + '\n');
    }

    // End load only mode. This rebuilds all indexes including the spatial
    // index.
    // If all grid size values in the spatial index are zero, the values will be
    // calculated based on the existing data.
    cities2Table.LoadOnlyMode(false);

    // Close the cities table.
    hr = geodatabase.CloseTable(cities2Table);
    if (hr != 0) {
      System.out.println("An error occurred while closing the cities2 table." + '\n');
      errorText = EsriFileGdb.getErrorDescription(hr);
      throw new RuntimeException(errorText + "(" + hr + ")." + '\n');
    }

    // Close the geodatabase
    hr = EsriFileGdb.CloseGeodatabase(geodatabase);
    if (hr != 0) {
      throw new RuntimeException(
        "An error occurred while closing the geodatabase."
          + EsriFileGdb.getErrorDescription(hr));
    }
  }
}
