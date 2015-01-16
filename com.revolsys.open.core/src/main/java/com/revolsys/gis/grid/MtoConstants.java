package com.revolsys.gis.grid;

public interface MtoConstants {
  double HEIGHT_HUNDRETH = 5.0 / 600.0;

  double HEIGHT_QUARTER = 5.0 / 2400.0;

  double HEIGHT_TWELTH = 5.0 / 60.0;

  String REGEX_QUARTER_LETTER = "([a-dA-D])";

  String REGEX_TWELTH_LETTER = "([a-lA-L])";

  String REGEX_MTO = "([0-9]{2,3})" + NtsConstants.REGEX_SIXTEENTH_LETTER
      + NtsConstants.REGEX_SIXTEENTH_NUMBER + REGEX_TWELTH_LETTER
      + BcgsConstants.REGEX_HUNDRETH_NUMBER + REGEX_QUARTER_LETTER;

  double WIDTH_HUNDRETH = 0.0125;

  double WIDTH_QUARTER = 0.00625;

  double WIDTH_TWELTH = 0.125;
}
