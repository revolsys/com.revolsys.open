package com.revolsys.fgdb.test;

import java.io.File;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.io.PathName;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStore;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStoreFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinitionImpl;

public class GeometryTest {
  public static void main(final String[] args) {

    for (final GeometryFactory geometryFactory : new com.revolsys.geometry.model.GeometryFactory[] {
      GeometryFactory.fixed2d(3005, 1000.0, 1000.0),
      GeometryFactory.fixed3d(3005, 1000.0, 1000.0, 1.0)
    }) {
      writeTestFile(geometryFactory, "POINT(1185093.8356 385662.9221)");
      writeTestFile(geometryFactory,
        "LINESTRINGZ(844395.448 1343937.441 1201.0,844304.98 1344019.53 1202.0,844299.206 1344024.791 1203.0,844245.375 1344075.229 1203.0,844206.127 1344116.019 1204.0,844205.172 1344117.062 1204.0)");
      writeTestFile(geometryFactory,
        "POLYGON((1185074.5212745096 385696.922,1185127.0702941176 385696.6082745098,1185126.442843137 385650.490627451,1185074.5212745096 385651.4318039216,1185074.5212745096 385696.922))");
      writeTestFile(geometryFactory,
        "MULTIPOINT((1184946.014533333 385736.96064117656),(1184946.9729294113 385705.81276862754),(1184964.2240588232 385731.210264706),(1184973.3288215683 385694.7912137255),(1184976.2040098037 385721.6263039217),(1184983.391980392 385735.52304705896),(1184983.871178431 385736.96064117656),(1184992.0175450977 385699.1039960785))");
      writeTestFile(geometryFactory,
        "MULTILINESTRING((1184910.0746803917 385732.64785882365,1185008.310278431 385722.58470000006,1184961.828068627 385686.64484705887,1184910.0746803917 385711.0839470589,1184889.9483627446 385711.5631450981),(1184888.0315705878 385689.04083725496,1184936.909770588 385664.6017372549,1184907.6786901958 385646.3922117647,1184876.0516196075 385664.6017372549,1184899.0531254897 385628.6618843137,1184856.4044999995 385661.72654901963))");
      // writeTestFile(
      // geometryFactory2d,
      // "MULTIPOLYGON(((1184983.391980392 385713.479937255,1184960.8696725487
      // 385694.7912137255,1184977.162405882
      // 385667.9561235294,1185007.3518823527
      // 385664.6017372549,1185011.1854666665
      // 385713.9591352942,1184983.391980392
      // 385713.479937255)),((1184917.26265098
      // 385712.52154117654,1184907.1994921565
      // 385670.3521137255,1184958.4736823526
      // 385669.8729156863,1184935.9513745094
      // 385689.9992333334,1184958.9528803919
      // 385713.00073921576,1184917.26265098 385712.52154117654)))");
    }
  }

  public static void writeTestFile(final GeometryFactory geometryFactory, final String wkt) {
    final Geometry geometry = geometryFactory.geometry(wkt);

    final DataType geometryDataType = DataTypes.getDataType(geometry);

    String name = "/" + geometryDataType.getName();
    if (geometryFactory.hasZ()) {
      name += "Z";

    }
    final File file = new File("target/test-data/" + name + ".gdb");
    FileUtil.deleteDirectory(file);

    final PathName pathName = PathName.newPathName(name);
    RecordDefinitionImpl recordDefinition = new RecordDefinitionImpl(pathName);
    recordDefinition.addField("ID", DataTypes.INT, true);
    final FieldDefinition geometryField = recordDefinition.addField("Geometry", geometryDataType,
      true);
    geometryField.setGeometryFactory(geometryFactory);
    recordDefinition.setIdFieldName("ID");

    final FileGdbRecordStore recordStore = FileGdbRecordStoreFactory.newRecordStore(file);
    recordStore.initialize();
    recordDefinition = (RecordDefinitionImpl)recordStore.getRecordDefinition(recordDefinition);
    final Record object = new ArrayRecord(recordDefinition);
    object.setIdentifier(Identifier.newIdentifier(1));
    object.setGeometryValue(geometry);

    recordStore.insertRecord(object);

    final Record object2 = recordStore.getRecord(pathName, Identifier.newIdentifier(1));
    if (!DataType.equal(object, object2)) {
      System.out.println("Not Equal");
      System.out.println(object);
      System.out.println(object2);
    }
    recordStore.close();
  }
}
