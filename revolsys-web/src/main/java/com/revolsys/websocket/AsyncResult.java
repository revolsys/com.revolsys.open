package com.revolsys.websocket;

public interface AsyncResult<M> {
  <V> V getResult(M result);
}
