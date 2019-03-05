package com.revolsys.math.arithmeticcoding;

public interface ArithmeticCodingCodec {

  ArithmeticModel createSymbolModel(int symbolCount);

  default ArithmeticCodingInteger newCodecInteger(final int bits) {
    return newCodecInteger(bits, 1);
  }

  default ArithmeticCodingInteger newCodecInteger(final int bits, final int contexts) {
    return new ArithmeticCodingInteger(this, bits, contexts);
  }
}
