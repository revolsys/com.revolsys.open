package com.revolsys.swing.map.layer;

import java.awt.Image;

import com.revolsys.gis.cs.BoundingBox;

public class MapTile {
  private BoundingBox boundingBox;

  private Image image;

  public MapTile(BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  public Image getImage() {
    return image;
  }

  public void setImage(Image image) {
    this.image = image;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof MapTile) {
      MapTile tile = (MapTile)obj;
      return tile.getBoundingBox().equals(boundingBox);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return boundingBox.hashCode();
  }
}
