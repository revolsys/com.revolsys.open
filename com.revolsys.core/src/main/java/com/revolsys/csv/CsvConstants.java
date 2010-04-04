package com.revolsys.csv;

import java.nio.charset.Charset;

public interface CsvConstants {
  Charset CHARACTER_SET = Charset.forName("UTF-8");

  String DESCRIPTION = "Comma Separated Variable";

  char FIELD_SEPARATOR = ',';

  String FILE_EXTENSION = "csv";

  String MEDIA_TYPE = "text/csv";

  String NS_PREFIX = "";

  String NS_URI = "";

  char QUOTE_CHARACTER = '"';
}
