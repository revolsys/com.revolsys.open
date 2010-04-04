package com.revolsys.jump.ecsv;

import java.io.BufferedWriter;
import java.util.List;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.ecsv.io.EcsvWriter;
import com.revolsys.jump.model.DataObjectFeature;
import com.revolsys.jump.model.FeatureSchemaClassDefinition;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.JUMPWriter;

public class EcsvJumpWriter implements JUMPWriter {
  public EcsvJumpWriter() {
  }

  @SuppressWarnings("unchecked")
  public void write(final FeatureCollection featureCollection,
    final DriverProperties dp) throws  Exception {
    String fileName = dp.getProperty("File");

    BufferedWriter out = new java.io.BufferedWriter(new java.io.FileWriter(
      fileName));

    DataObjectMetaData type = new FeatureSchemaClassDefinition(
      featureCollection.getFeatureSchema(), fileName);
    EcsvWriter writer = new EcsvWriter(type, out);
    
    List<Feature> features = featureCollection.getFeatures();
    for (Feature feature : features) {
      DataObject object = new DataObjectFeature(type, feature);
      writer.write(object);
    }
    writer.close();
  }
}
