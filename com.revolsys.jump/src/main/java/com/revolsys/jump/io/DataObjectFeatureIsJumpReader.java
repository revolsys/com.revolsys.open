package com.revolsys.jump.io;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import com.revolsys.gis.data.io.DataObjectReaderFactory;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.FileUtil;
import com.revolsys.jump.model.FeatureDataObjectFactory;
import com.revolsys.spring.InputStreamResource;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.JUMPReader;

public class DataObjectFeatureIsJumpReader implements JUMPReader {

  private DataObjectReaderFactory readerFactory;

  private FeatureDataObjectFactory dataObjectFactory = new FeatureDataObjectFactory();

  public DataObjectFeatureIsJumpReader(
    final DataObjectReaderFactory readerFactory) {
    this.readerFactory = readerFactory;
  }

  public FeatureCollection read(final DriverProperties dp) throws Exception {
    String fileName = dp.getProperty("File");
    final String localFileName = FileUtil.getFileName(fileName);
    InputStream in = new FileInputStream(fileName);
    Reader<DataObject> reader = readerFactory.createDataObjectReader(new InputStreamResource(localFileName,in),
      dataObjectFactory);

    Iterator<DataObject> featureIter = reader.iterator();
    if (featureIter.hasNext()) {
      Feature feature = (Feature)featureIter.next();
      FeatureSchema schema = feature.getSchema();
      FeatureCollection featureCollection = new FeatureDataset(schema);
      featureCollection.add(feature);
      while (featureIter.hasNext()) {
        feature = (Feature)featureIter.next();
        featureCollection.add(feature);
      }
      return featureCollection;
    } else {
      return null;
    }
  }

}
