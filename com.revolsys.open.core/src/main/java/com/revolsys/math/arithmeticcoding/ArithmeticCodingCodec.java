package com.revolsys.math.arithmeticcoding;

public interface ArithmeticCodingCodec {

  ArithmeticModel createSymbolModel(int symbolCount);

  default void initModel(final ArithmeticModel model) {
    if (model != null) {
      model.init();
    }
  }

  default ArithmeticCodingInteger newCodecInteger(final int bits) {
    return newCodecInteger(bits, 1);
  }

  default ArithmeticCodingInteger newCodecInteger(final int bits, final int contexts) {
    return new ArithmeticCodingInteger(this, bits, contexts);
  }
}
