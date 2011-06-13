package com.revolsys.gis.esri.gdb.file;

import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import com.revolsys.gis.esri.gdb.file.swig.EsriFileGdb;
import com.revolsys.gis.esri.gdb.file.swig.Geodatabase;
import com.revolsys.gis.esri.gdb.file.swig.Table;

import org.junit.Test;

public class FeatureDatasetTest {
  static {
    System.loadLibrary("EsriFileGdb");
  }
  
  public static long copy(
    final Reader in,
    final Writer out)
    throws IOException {
    final char[] buffer = new char[4906];
    long numBytes = 0;
    int count;
    while ((count = in.read(buffer)) > -1) {
      out.write(buffer, 0, count);
      numBytes += count;
    }
    return numBytes;
  }


  public String getResourceAsString(String name) throws IOException{
    InputStream in = getClass().getResourceAsStream(name);
    if (in == null) {
      throw new IllegalArgumentException(name);
    } else {
      Reader reader = new InputStreamReader(in);
      StringWriter writer = new StringWriter();
      copy(reader,writer);
      reader.close();
      return writer.toString();
    }
  }

  @Test
  public void testFeatureDatasets() throws Exception {
    String datasetName ="target/FeatureDatasetDemo.gdb";
    int  hr;
    String errorText;
    Geodatabase geodatabase = new Geodatabase();
    hr = EsriFileGdb.DeleteGeodatabase(datasetName);
    if (hr == 0) {
      System.out.println("The geodatabase has been deleted");
    } else if (hr == -2147024893) {
      System.out.println("The geodatabase does not exist, no need to delete");
    } else {
      System.out.println("An error occurred while deleting the geodatabase." + EsriFileGdb.getErrorDescription(hr));
    }

    hr = EsriFileGdb.CreateGeodatabase(datasetName, geodatabase);
    if (hr != 0) {
      throw new RuntimeException("An error occurred while creating the geodatabase.");
    }
    System.out.println("The geodatabase has been created.");

    String featureDatasetDef = getResourceAsString("/TransitFD.xml");
    // Create the feature dataset.
    hr = geodatabase.CreateFeatureDataset(featureDatasetDef);
    if (hr != 0) {
      throw new RuntimeException("An error occurred while creating the feature dataset." + EsriFileGdb.getErrorDescription(hr));
    }
    System.out.println("The feature dataset has been created.");

    String tableDef = getResourceAsString("/BusStopsTable.xml");

    Table table = new Table();
    hr = geodatabase.CreateTable(tableDef,"\\Transit", table);
    if (hr != 0) {
      throw new RuntimeException("An error occurred while creating the table." + EsriFileGdb.getErrorDescription(hr));
    }
    System.out.println("The table has been created.");

    hr = geodatabase.CloseTable(table);
    if (hr != 0) {
      throw new RuntimeException("An error occurred while closing Cities.");
    }

  // Close the geodatabase
    hr = EsriFileGdb.CloseGeodatabase(geodatabase);
    if (hr !=0 ) {
      throw new RuntimeException("An error occurred while closing the geodatabase.");
    }
  }

}
