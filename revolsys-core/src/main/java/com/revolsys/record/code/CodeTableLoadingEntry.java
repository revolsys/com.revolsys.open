package com.revolsys.record.code;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public class CodeTableLoadingEntry {

  private final AbstractLoadingCodeTable codeTable;

  private Sinks.One<Boolean> loadedSubject;

  private final long expiry = System.currentTimeMillis() + 60 * 1000;

  private final Object value;

  private boolean hadResult;

  public CodeTableLoadingEntry(AbstractLoadingCodeTable codeTable, Object value) {
    this.codeTable = codeTable;
    this.value = value;
  }

  public boolean isExpired() {
    return this.expiry < System.currentTimeMillis();
  }

  public synchronized Mono<Boolean> subject() {
    if (this.loadedSubject == null) {
      this.loadedSubject = Sinks.one();
      return this.codeTable.loadValueDo(this.value)//
        .doOnNext(s -> {
          this.hadResult = true;
          this.loadedSubject.tryEmitValue(s);
        })
        .doOnError(e -> {
          this.hadResult = true;
          this.loadedSubject.tryEmitError(e);
        })
        .doOnTerminate(() -> {
          if (!this.hadResult) {
            this.loadedSubject.tryEmitEmpty();
          }
        });
    } else {
      return this.loadedSubject.asMono();
    }
  }
}
