package com.revolsys.io.ecsv.type;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;

public class CollectionFieldType extends AbstractEcsvFieldType {

  private final String typePath;

  public CollectionFieldType(final DataType dataType) {
    super(dataType);
    this.typePath = dataType.getName();
  }

  @Override
  public String getTypeName() {
    return typePath;
  }

  @Override
  public Object parseValue(final String text) {
    return null;
  }

  @Override
  public void writeValue(final PrintWriter out, final Object value) {
    if (value instanceof Collection) {
      final Collection<Object> collection = (Collection<Object>)value;
      out.write(COLLECTION_START);

      final Iterator<Object> iterator = collection.iterator();
      while (iterator.hasNext()) {
        final Object object = iterator.next();
        if (object != null) {
          final DataType dataType = DataTypes.getType(object.getClass());
          final EcsvFieldType fieldType = EcsvFieldTypeRegistry.INSTANCE.getFieldType(dataType);
          fieldType.writeValue(out, object);
        }
        if (iterator.hasNext()) {
          out.write(FIELD_SEPARATOR);
        }
      }
      out.write(COLLECTION_END);

    }
  }

}
