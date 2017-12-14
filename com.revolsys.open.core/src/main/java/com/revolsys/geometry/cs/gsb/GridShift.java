/*
 * Copyright (c) 2003 Objectix Pty Ltd  All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL OBJECTIX PTY LTD BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.revolsys.geometry.cs.gsb;

import java.io.Serializable;

/**
 * A value object for storing Longitude and Latitude of a point, the
 * Lon and Lat shift values to get from one datum to another, and the
 * Lon and Lat accuracy of the shift values.
 * <p>All values are stored as Positive West Seconds, but accessors
 * are also provided for Positive East Degrees.
 *
 * @author Peter Yuill
 */
public class GridShift implements Serializable {

  private static final double METRE_PER_SECOND = 2.0 * Math.PI * 6378137.0 / 3600.0 / 360.0;

  private static final double RADIANS_PER_SECOND = 2.0 * Math.PI / 3600.0 / 360.0;

  private double lon;

  private double lat;

  private double lonShift;

  private double latShift;

  private double lonAccuracy;

  private double latAccuracy;

  boolean latAccuracyAvailable;

  boolean lonAccuracyAvailable;

  private String subGridName;

  public GridShift() {
  }

  /**
   * Make this object a copy of the supplied GridShift
   * @param gs
   */
  public void copy(final GridShift gs) {
    this.lon = gs.lon;
    this.lat = gs.lat;
    this.lonShift = gs.lonShift;
    this.latShift = gs.latShift;
    this.lonAccuracy = gs.lonAccuracy;
    this.latAccuracy = gs.latAccuracy;
    this.latAccuracyAvailable = gs.latAccuracyAvailable;
    this.lonAccuracyAvailable = gs.lonAccuracyAvailable;
    this.subGridName = gs.subGridName;
  }

  /**
   * @return
   */
  public double getLatAccuracyDegrees() {
    if (!this.latAccuracyAvailable) {
      throw new IllegalStateException("Latitude Accuracy not available");
    }
    return this.latAccuracy / 3600.0;
  }

  /**
   * @return
   */
  public double getLatAccuracyMetres() {
    if (!this.latAccuracyAvailable) {
      throw new IllegalStateException("Latitude Accuracy not available");
    }
    return this.latAccuracy * METRE_PER_SECOND;
  }

  /**
   * @return
   */
  public double getLatAccuracySeconds() {
    if (!this.latAccuracyAvailable) {
      throw new IllegalStateException("Latitude Accuracy not available");
    }
    return this.latAccuracy;
  }

  /**
   * @return
   */
  public double getLatDegrees() {
    return this.lat / 3600.0;
  }

  /**
   * @return
   */
  public double getLatSeconds() {
    return this.lat;
  }

  /**
   * @return
   */
  public double getLatShiftDegrees() {
    return this.latShift / 3600.0;
  }

  /**
   * @return
   */
  public double getLatShiftSeconds() {
    return this.latShift;
  }

  /**
   * @return
   */
  public double getLonAccuracyDegrees() {
    if (!this.lonAccuracyAvailable) {
      throw new IllegalStateException("Longitude Accuracy not available");
    }
    return this.lonAccuracy / 3600.0;
  }

  /**
   * @return
   */
  public double getLonAccuracyMetres() {
    if (!this.lonAccuracyAvailable) {
      throw new IllegalStateException("Longitude Accuracy not available");
    }
    return this.lonAccuracy * METRE_PER_SECOND * Math.cos(RADIANS_PER_SECOND * this.lat);
  }

  /**
   * @return
   */
  public double getLonAccuracySeconds() {
    if (!this.lonAccuracyAvailable) {
      throw new IllegalStateException("Longitude Accuracy not available");
    }
    return this.lonAccuracy;
  }

  /**
   * @return
   */
  public double getLonPositiveEastDegrees() {
    return this.lon / -3600.0;
  }

  /**
   * @return
   */
  public double getLonPositiveWestSeconds() {
    return this.lon;
  }

  /**
   * @return
   */
  public double getLonShiftPositiveEastDegrees() {
    return this.lonShift / -3600.0;
  }

  /**
   * @return
   */
  public double getLonShiftPositiveWestSeconds() {
    return this.lonShift;
  }

  /**
   * @return
   */
  public double getShiftedLatDegrees() {
    return (this.lat + this.latShift) / 3600.0;
  }

  /**
   * @return
   */
  public double getShiftedLatSeconds() {
    return this.lat + this.latShift;
  }

  /**
   * @return
   */
  public double getShiftedLonPositiveEastDegrees() {
    return (this.lon + this.lonShift) / -3600.0;
  }

  /**
   * @return
   */
  public double getShiftedLonPositiveWestSeconds() {
    return this.lon + this.lonShift;
  }

  /**
   * @return
   */
  public String getSubGridName() {
    return this.subGridName;
  }

  /**
   * @return
   */
  public boolean isLatAccuracyAvailable() {
    return this.latAccuracyAvailable;
  }

  /**
   * @return
   */
  public boolean isLonAccuracyAvailable() {
    return this.lonAccuracyAvailable;
  }

  /**
   * @param b
   */
  public void setLatAccuracyAvailable(final boolean b) {
    this.latAccuracyAvailable = b;
  }

  /**
   * @param d
   */
  public void setLatAccuracySeconds(final double d) {
    this.latAccuracy = d;
  }

  /**
   * @param d
   */
  public void setLatDegrees(final double d) {
    this.lat = d * 3600.0;
  }

  /**
   * @param d
   */
  public void setLatSeconds(final double d) {
    this.lat = d;
  }

  /**
   * @param d
   */
  public void setLatShiftSeconds(final double d) {
    this.latShift = d;
  }

  /**
   * @param b
   */
  public void setLonAccuracyAvailable(final boolean b) {
    this.lonAccuracyAvailable = b;
  }

  /**
   * @param d
   */
  public void setLonAccuracySeconds(final double d) {
    this.lonAccuracy = d;
  }

  /**
   * @param d
   */
  public void setLonPositiveEastDegrees(final double d) {
    this.lon = d * -3600.0;
  }

  /**
   * @param d
   */
  public void setLonPositiveWestSeconds(final double d) {
    this.lon = d;
  }

  /**
   * @param d
   */
  public void setLonShiftPositiveWestSeconds(final double d) {
    this.lonShift = d;
  }

  /**
   * @param string
   */
  public void setSubGridName(final String string) {
    this.subGridName = string;
  }

}
