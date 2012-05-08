package com.revolsys.gis.esri.gdb.file.test;

import java.io.File;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.ArrayDataObject;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.AttributeProperties;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.FileGdbDataObjectStore;
import com.revolsys.gis.esri.gdb.file.FileGdbDataObjectStoreFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.InPlaceIterator;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.FileUtil;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryTest {
  public static void createTestFile(
    final GeometryFactory geometryFactory,
    final String wkt) {
    final Geometry geometry = geometryFactory.createGeometry(wkt);

    final DataType geometryDataType = DataTypes.getType(geometry);

    String name = "/" + geometryDataType.getName();
    if (geometryFactory.hasZ()) {
      name += "Z";
      for (final CoordinatesList points : CoordinatesListUtil.getAll(geometry)) {
        for (final Coordinates point : new InPlaceIterator(points)) {
          if (Double.isNaN(point.getZ())) {
            point.setZ(0);
          }
        }
      }
    }
    final File file = new File("target/test-data/" + name + ".gdb");
    FileUtil.deleteDirectory(file);

    DataObjectMetaDataImpl metaData = new DataObjectMetaDataImpl(name);
    metaData.addAttribute("ID", DataTypes.INT, true);
    final Attribute geometryAttribute = metaData.addAttribute("Geometry",
      geometryDataType, true);
    geometryAttribute.setProperty(AttributeProperties.GEOMETRY_FACTORY,
      geometryFactory);
    metaData.setIdAttributeName("ID");

    final FileGdbDataObjectStore dataStore = FileGdbDataObjectStoreFactory.create(file);
    dataStore.initialize();
    metaData = (DataObjectMetaDataImpl)dataStore.getMetaData(metaData);
    final DataObject object = new ArrayDataObject(metaData);
    object.setIdValue(1);
    object.setGeometryValue(geometry);

    dataStore.insert(object);

    final DataObject object2 = dataStore.load(name, 1);
    if (!EqualsRegistry.INSTANCE.equals(object, object2)) {
      System.out.println("Not Equal");
      System.out.println(object);
      System.out.println(object2);
    }
    dataStore.close();
  }

  public static void main(final String[] args) {

    for (final GeometryFactory geometryFactory : new GeometryFactory[] {
      GeometryFactory.getFactory(3005, 1000.0),
      GeometryFactory.getFactory(3005, 1000.0, 1.0)
    }) {
      createTestFile(geometryFactory, "POINT(1185093.8356 385662.9221)");
      createTestFile(
        geometryFactory,
        "LINESTRINGZ(844395.448 1343937.441 1201.0,844304.98 1344019.53 1202.0,844299.206 1344024.791 1203.0,844245.375 1344075.229 1203.0,844206.127 1344116.019 1204.0,844205.172 1344117.062 1204.0)");
      createTestFile(
        geometryFactory,
        "POLYGON((1185074.5212745096 385696.922,1185127.0702941176 385696.6082745098,1185126.442843137 385650.490627451,1185074.5212745096 385651.4318039216,1185074.5212745096 385696.922))");
      createTestFile(
        geometryFactory,
        "MULTIPOINT((1184946.014533333 385736.96064117656),(1184946.9729294113 385705.81276862754),(1184964.2240588232 385731.210264706),(1184973.3288215683 385694.7912137255),(1184976.2040098037 385721.6263039217),(1184983.391980392 385735.52304705896),(1184983.871178431 385736.96064117656),(1184992.0175450977 385699.1039960785))");
      createTestFile(
        geometryFactory,
        "MULTILINESTRING((1184910.0746803917 385732.64785882365,1185008.310278431 385722.58470000006,1184961.828068627 385686.64484705887,1184910.0746803917 385711.0839470589,1184889.9483627446 385711.5631450981),(1184888.0315705878 385689.04083725496,1184936.909770588 385664.6017372549,1184907.6786901958 385646.3922117647,1184876.0516196075 385664.6017372549,1184899.0531254897 385628.6618843137,1184856.4044999995 385661.72654901963))");
      // createTestFile(
      // geometryFactory2d,
      // "MULTIPOLYGON(((1184983.391980392 385713.479937255,1184960.8696725487 385694.7912137255,1184977.162405882 385667.9561235294,1185007.3518823527 385664.6017372549,1185011.1854666665 385713.9591352942,1184983.391980392 385713.479937255)),((1184917.26265098 385712.52154117654,1184907.1994921565 385670.3521137255,1184958.4736823526 385669.8729156863,1184935.9513745094 385689.9992333334,1184958.9528803919 385713.00073921576,1184917.26265098 385712.52154117654)))");
    }
  }
}
