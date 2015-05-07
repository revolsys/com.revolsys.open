package com.revolsys.gis.esri.gdb.file;

import java.util.ArrayList;
import java.util.List;

public class FileGdbException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private final List<String> errors = new ArrayList<>();

  public FileGdbException() {
  }

  public FileGdbException(final String message) {
    super(message);
    initErrors();
  }

  public FileGdbException(final String message, final Throwable cause) {
    super(message, cause);
    initErrors();
  }

  public FileGdbException(final Throwable cause) {
    super(cause);
    initErrors();
  }

  public List<String> getErrors() {
    return this.errors;
  }

  private void initErrors() {
    // synchronized (FileGdbRecordStoreImpl.API_SYNC) {
    // final VectorOfWString errors = EsriFileGdb.getErrors();
    // final long errorCount = errors.size();
    // for (int i = 0; i < errorCount; i++) {
    // final String error = errors.get(i);
    // this.errors.add(error);
    // }
    // }
  }
  //
  // @Override
  // public String toString() {
  // final String superString = super.toString();
  // if (this.errors.isEmpty()) {
  // return superString;
  // } else {
  // final StringBuilder string = new StringBuilder(superString);
  // for (final String error : this.errors) {
  // string.append("\n  ");
  // string.append(error);
  // }
  // return string.toString();
  // }
  // }
}
