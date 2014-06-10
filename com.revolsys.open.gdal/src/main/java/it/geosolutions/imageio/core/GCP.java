/*
 *    ImageI/O-Ext - OpenSource Java Image translation Library
 *    http://www.geo-solutions.it/
 *    http://java.net/projects/imageio-ext/
 *    (C) 2007 - 2009, GeoSolutions
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    either version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.imageio.core;

/**
 * Class that holds information about a ground control point.
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public class GCP {

  private double easting;

  private double northing;

  private double elevation;

  private int row;

  private int column;

  private String id;

  private String description;

  /**
   * 
   */
  public GCP() {
    // TODO Auto-generated constructor stub
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  protected Object clone() throws CloneNotSupportedException {
    final GCP newGCP = new GCP();
    newGCP.setColumn(this.getColumn());
    newGCP.setDescription(this.getDescription());
    newGCP.setEasting(this.getEasting());
    newGCP.setElevation(this.getElevation());
    newGCP.setId(this.getId());
    newGCP.setNorthing(this.getNorthing());
    newGCP.setRow(this.getRow());
    return newGCP;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final GCP other = (GCP)obj;
    if (column != other.column) {
      return false;
    }
    if (description == null) {
      if (other.description != null) {
        return false;
      }
    } else if (!description.equals(other.description)) {
      return false;
    }
    if (Double.doubleToLongBits(easting) != Double.doubleToLongBits(other.easting)) {
      return false;
    }
    if (Double.doubleToLongBits(elevation) != Double.doubleToLongBits(other.elevation)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (Double.doubleToLongBits(northing) != Double.doubleToLongBits(other.northing)) {
      return false;
    }
    if (row != other.row) {
      return false;
    }
    return true;
  }

  /**
   * Pixel (x) location of GCP on raster. 
   * 
   * @return the column location of this gcp on the raster.
   */
  public int getColumn() {
    return column;
  }

  /**
   * Informational message or "". 
   * 
   * @return the description of this GCP.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Easting of this gcp.
   * 
   * @return the easting of this gcp.
   */
  public double getEasting() {
    return easting;
  }

  /**
   * The elevation of this GCP.
   * 
   * @return the elevation of thic gcp.
   */
  public double getElevation() {
    return elevation;
  }

  /**
   * Unique identifier, often numeric. 
   * 
   * @return the id for this GCP
   */
  public String getId() {
    return id;
  }

  /**
   * Northing of this gcp.
   * 
   * @return the northing of this gcp
   */
  public double getNorthing() {
    return northing;
  }

  /**
   * Line (y) location of GCP on raster. 
   * 
   * @return the row location of this GCP on the raster.
   */
  public int getRow() {
    return row;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + column;
    result = prime * result
      + ((description == null) ? 0 : description.hashCode());
    long temp;
    temp = Double.doubleToLongBits(easting);
    result = prime * result + (int)(temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(elevation);
    result = prime * result + (int)(temp ^ (temp >>> 32));
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    temp = Double.doubleToLongBits(northing);
    result = prime * result + (int)(temp ^ (temp >>> 32));
    result = prime * result + row;
    return result;
  }

  /**
   * @param column the column to set
   */
  public void setColumn(final int column) {
    this.column = column;
  }

  /**
   * @param description the description to set
   */
  public void setDescription(final String description) {
    this.description = description;
  }

  /**
   * @param easting the easting to set
   */
  public void setEasting(final double easting) {
    this.easting = easting;
  }

  /**
   * @param elevation the elevation to set
   */
  public void setElevation(final double elevation) {
    this.elevation = elevation;
  }

  /**
   * @param id the id to set
   */
  public void setId(final String id) {
    this.id = id;
  }

  /**
   * @param northing the northing to set
   */
  public void setNorthing(final double northing) {
    this.northing = northing;
  }

  /**
   * @param row the row to set
   */
  public void setRow(final int row) {
    this.row = row;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    final StringBuilder result = new StringBuilder();
    final String NEW_LINE = System.getProperty("line.separator");
    result.append(this.getClass().getName())
      .append(" Object {")
      .append(NEW_LINE);
    result.append(" id: ").append(id).append(NEW_LINE);
    result.append(" description: ").append(description).append(NEW_LINE);
    result.append(" easting: ").append(easting).append(NEW_LINE);
    result.append(" northing: ").append(northing).append(NEW_LINE);
    result.append(" elevation: ").append(elevation).append(NEW_LINE);
    result.append(" column: ").append(column).append(NEW_LINE);
    result.append(" row: ").append(row).append(NEW_LINE);
    result.append("}");
    return result.toString();

  }
}
