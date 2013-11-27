package com.revolsys.io.ecsv;


public interface EcsvConstants extends EcsvProperties {
  String _NS_PREFIX = "";

  String _NS_URI = "";

  String COLLECTION_END = ")";

  String COLLECTION_START = "(";

  String DESCRIPTION = "Enhanced CSV";

  char DOUBLE_QUOTE = '"';

  String DOUBLE_QUOTE_ESCAPE = "\"\"";

  char FIELD_SEPARATOR = ',';

  String FILE_EXTENSION = "ecsv";

  String GEOMETRY_FACTORY_TYPE = "GeometryFactoryImpl";

  String LIST_TYPE = "List";

  String MAP_TYPE = "Map";

  String MAP_END = "}";

  String MAP_START = "{";

  String TYPE_PARAMETER_START = "<";

  String TYPE_PARAMETER_END = ">";

  String MEDIA_TYPE = "text/x-ecsv";

  String MULTI_LINE_LIST_END = "]";

  String MULTI_LINE_LIST_START = "[";

  String RECORD_SEPARATOR = "\n";

  String VERSION_1_0_0_DRAFT1 = "1.0.0.DRAFT1";
}
