package com.revolsys.gis.esri.gdb.file;

import java.io.File;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.esri.filegdb.jni.EsriFileGdb;
import com.revolsys.esri.filegdb.jni.Geodatabase;
import com.revolsys.esri.filegdb.jni.Table;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.record.io.format.esri.gdb.xml.model.DETable;
import com.revolsys.record.io.format.esri.gdb.xml.model.EsriGdbXmlSerializer;
import com.revolsys.record.io.format.esri.gdb.xml.model.EsriXmlRecordDefinitionUtil;
import com.revolsys.record.io.format.esri.gdb.xml.model.SpatialReference;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordDefinitionBuilder;

class FgdbApiInit implements Runnable {
  @Override
  public void run() {
    final File tempFile = FileUtil.newTempFile("test", ".gdb");
    final String fileName = tempFile.getName();
    final GeometryFactory geometryFactory = GeometryFactory.fixed3d(3005, 1000.0, 1000.0, 1000.0);
    final RecordDefinition recordDefinition = new RecordDefinitionBuilder("/test") //
      .addField(DataTypes.INT) //
      .addField(DataTypes.DATE) //
      .addField("text", DataTypes.STRING, 100) //
      .addField(GeometryDataTypes.LINE_STRING) //
      .setGeometryFactory(geometryFactory) //
      .getRecordDefinition();
    final String wkt = EsriFileGdb.getSpatialReferenceWkt(3005);
    final SpatialReference spatialReference = SpatialReference.get(geometryFactory, wkt);
    final DETable deTable = EsriXmlRecordDefinitionUtil.getDETable(recordDefinition,
      spatialReference, true, true);
    final String tableDefinition = EsriGdbXmlSerializer.toString(deTable);
    final Geodatabase geodatabase = EsriFileGdb.createGeodatabase(fileName);
    final Table table = geodatabase.createTable(tableDefinition, "\\");
    geodatabase.closeTable(table);
    EsriFileGdb.CloseGeodatabase(geodatabase);
    EsriFileGdb.DeleteGeodatabase(fileName);
  }
}
