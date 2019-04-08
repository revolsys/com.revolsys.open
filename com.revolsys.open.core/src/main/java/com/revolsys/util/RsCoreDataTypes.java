package com.revolsys.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.measure.Quantity;

import org.jeometry.common.data.type.CollectionDataType;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.data.type.FunctionDataType;
import org.jeometry.common.data.type.ListDataType;
import org.jeometry.common.io.FileProxy;

import com.revolsys.io.FileUtil;
import com.revolsys.record.code.CodeDataType;

public class RsCoreDataTypes {

  public static final DataType FILE = new FunctionDataType("File", File.class, value -> {
    if (value == null) {
      return null;
    } else {
      File file = null;
      if (value instanceof File) {
        return FileUtil.getFile((File)value);
      } else if (value instanceof URL) {
        return FileUtil.getFile((URL)value);
      } else if (value instanceof URI) {
        return FileUtil.getFile((URI)value);
      } else if (value instanceof FileProxy) {
        final FileProxy proxy = (FileProxy)value;
        file = proxy.getOrDownloadFile();
      } else {
        // final String string = DataTypes.toString(value);
        // return getFile(string);
        file = null;
      }
      if (file == null) {
        return file;
      } else {
        try {
          return file.getCanonicalFile();
        } catch (final IOException e) {
          return file.getAbsoluteFile();
        }
      }
    }
  });

  public static final DataType CODE = new CodeDataType();

  public static final DataType COLLECTION = new CollectionDataType("Collection", Collection.class,
    DataTypes.OBJECT);

  public static final DataType LIST = new ListDataType(List.class, DataTypes.OBJECT);

  public static final DataType SET = new CollectionDataType("Set", Set.class, DataTypes.OBJECT);

  public static final DataType RELATION = new CollectionDataType("Relation", Collection.class,
    DataTypes.OBJECT);

  public static final DataType MEASURE = new FunctionDataType("measure", Quantity.class,
    QuantityType::newQuantity, QuantityType::toString);

}
