package com.revolsys.gis.esri.gdb.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Test;

import com.revolsys.gis.esri.gdb.file.swig.EnumRows;
import com.revolsys.gis.esri.gdb.file.swig.EsriFileGdb;
import com.revolsys.gis.esri.gdb.file.swig.Geodatabase;
import com.revolsys.gis.esri.gdb.file.swig.Point;
import com.revolsys.gis.esri.gdb.file.swig.PointShapeBuffer;
import com.revolsys.gis.esri.gdb.file.swig.Row;
import com.revolsys.gis.esri.gdb.file.swig.ShapeType;
import com.revolsys.gis.esri.gdb.file.swig.Table;

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
  public void testFeatureDatasets() throws Exception {
    String datasetName = "target/FeatureDatasetDemo.gdb";
    int hr;
    String errorText;
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

  public void x() {
    int hr;
    String errorText;

    // Open the geodatabase.
    Geodatabase geodatabase = new Geodatabase();
    if ((hr = EsriFileGdb.OpenGeodatabase("../data/Shapes.gdb", geodatabase)) != 0) {
      throw new RuntimeException(
        "An error occurred while opening the geodatabase."
          + EsriFileGdb.getErrorDescription(hr));

    }

    System.out.println('\n' + "Testing Point Shapes, Read" + '\n' + '\n');

    // Open the point test table, cities.
    Table citiesTable = new Table();
    if ((hr = geodatabase.OpenTable("\\cities", citiesTable)) != 0) {
      System.out.println("An error occurred while opening the table, cities." + '\n');
      errorText = EsriFileGdb.getErrorDescription(hr);
      throw new RuntimeException(errorText + "(" + hr + ")." + '\n');
    }

    // Return all rows.
    EnumRows enumRows = new EnumRows();
    if ((hr = citiesTable.Search("", "CITY_NAME = 'Woodinville'", true,
      enumRows)) != 0) {
      System.out.println("An error occurred while performing the attribute query." + '\n');
      errorText = EsriFileGdb.getErrorDescription(hr);
      throw new RuntimeException(errorText + "(" + hr + ")." + '\n');
    }

    // Get the first returned row.
    Row row = new Row();
    if ((hr = enumRows.Next(row)) != 0) {
      System.out.println("An error occurred returning the first row." + '\n');
      errorText = EsriFileGdb.getErrorDescription(hr);
      throw new RuntimeException(errorText + "(" + hr + ")." + '\n');
    }

    PointShapeBuffer pointGeometry = new PointShapeBuffer();
    row.GetGeometry(pointGeometry);

    Point point = new Point();
    pointGeometry.GetPoint(point);
    System.out.println("Point test:" + '\n');
    System.out.println("x: " + point.getX() + " y: " + point.getY() + '\n');

    enumRows.Close();

    // Close the cities table
    if ((hr = geodatabase.CloseTable(citiesTable)) != 0) {
      System.out.println("An error occurred while closing the cities table." + '\n');
      errorText = EsriFileGdb.getErrorDescription(hr);
      throw new RuntimeException(errorText + "(" + hr + ")." + '\n');
    }

    System.out.println('\n' + "Testing Point Shapes, Write" + '\n' + '\n');

    // Open the point test table, cities2.
    Table cities2Table = new Table();
    if ((hr = geodatabase.OpenTable("\\cities2", cities2Table)) != 0) {
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
    Point point2 = new Point();
    point2Geometry.GetPoint(point2);
    point2.setX(point.getX());
    point2.setY(point.getY());

    hr = cities2Row.SetGeometry(point2Geometry);

    if ((hr = cities2Table.Insert(cities2Row)) != 0) {
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
    if ((hr = geodatabase.CloseTable(cities2Table)) != 0) {
      System.out.println("An error occurred while closing the cities2 table." + '\n');
      errorText = EsriFileGdb.getErrorDescription(hr);
      throw new RuntimeException(errorText + "(" + hr + ")." + '\n');
    }

    // Close the geodatabase
    if ((hr = EsriFileGdb.CloseGeodatabase(geodatabase)) != 0) {
      throw new RuntimeException(
        "An error occurred while closing the geodatabase."
          + EsriFileGdb.getErrorDescription(hr));
    }
  }
}
