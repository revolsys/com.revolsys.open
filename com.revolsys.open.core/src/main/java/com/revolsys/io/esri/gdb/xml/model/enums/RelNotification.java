package com.revolsys.io.esri.gdb.xml.model.enums;

public enum RelNotification {
  /** None - no messages are sent */
  esriRelNotificationNone,

  /**
   * Forward - messages are sent only from origin objects to destination objects
   */
  esriRelNotificationForward,

  /**
   * Backward - messages are sent only from destination objects to source
   * objects
   */
  esriRelNotificationBackward,

  /** Both - messages are sent in both directions */
  esriRelNotificationBoth;
}
