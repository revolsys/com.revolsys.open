package com.revolsys.gis.model.geometry.operation.geomgraph.index;

/**
 * @version 1.7
 */
public class SweepLineEvent implements Comparable {
  public static final int INSERT = 1;

  public static final int DELETE = 2;

  Object edgeSet; // used for red-blue intersection detection

  private final double xValue;

  private int eventType;

  private final SweepLineEvent insertEvent; // null if this is an INSERT event

  private int deleteEventIndex;

  private final Object obj;

  public SweepLineEvent(final Object edgeSet, final double x,
    final SweepLineEvent insertEvent, final Object obj) {
    this.edgeSet = edgeSet;
    xValue = x;
    this.insertEvent = insertEvent;
    this.eventType = INSERT;
    if (insertEvent != null) {
      eventType = DELETE;
    }
    this.obj = obj;
  }

  /**
   * ProjectionEvents are ordered first by their x-value, and then by their
   * eventType. It is important that Insert events are sorted before Delete
   * events, so that items whose Insert and Delete events occur at the same
   * x-value will be correctly handled.
   */
  @Override
  public int compareTo(final Object o) {
    final SweepLineEvent pe = (SweepLineEvent)o;
    if (xValue < pe.xValue) {
      return -1;
    }
    if (xValue > pe.xValue) {
      return 1;
    }
    if (eventType < pe.eventType) {
      return -1;
    }
    if (eventType > pe.eventType) {
      return 1;
    }
    return 0;
  }

  public int getDeleteEventIndex() {
    return deleteEventIndex;
  }

  public SweepLineEvent getInsertEvent() {
    return insertEvent;
  }

  public Object getObject() {
    return obj;
  }

  public boolean isDelete() {
    return insertEvent != null;
  }

  public boolean isInsert() {
    return insertEvent == null;
  }

  public void setDeleteEventIndex(final int deleteEventIndex) {
    this.deleteEventIndex = deleteEventIndex;
  }

}
