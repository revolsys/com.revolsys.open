package com.revolsys.gis.model.geometry;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class MultiLineString extends com.vividsolutions.jts.geom.MultiLineString implements List<LineString>{
  
  public MultiLineString(
    LineString[] lineStrings,
    GeometryFactory factory) {
    super(lineStrings, factory);
  }

  public boolean add(
    LineString e) {
    throw new UnsupportedOperationException("Modifying a MultiLineString is not allowed");
     }

  public void add(
    int index,
    LineString element) {
    throw new UnsupportedOperationException("Modifying a MultiLineString is not allowed");
  }

  public boolean addAll(
    Collection<? extends LineString> c) {
    throw new UnsupportedOperationException("Modifying a MultiLineString is not allowed");
  }

  public boolean addAll(
    int index,
    Collection<? extends LineString> c) {
    throw new UnsupportedOperationException("Modifying a MultiLineString is not allowed");
  }

  public void clear() {
    throw new UnsupportedOperationException("Modifying a MultiLineString is not allowed");
 }

  public boolean contains(
    Object o) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean containsAll(
    Collection<?> c) {
    // TODO Auto-generated method stub
    return false;
  }

  public LineString get(
    int index) {
    return (LineString)getGeometryN(index);
  }

  public int indexOf(
    Object o) {
    for (int i = 0; i < size(); i++) {
      if (o == get(i)) {
        return i;
      }
    }
    return -1;
  }

  public Iterator<LineString> iterator() {
    // TODO Auto-generated method stub
    return null;
  }

  public int lastIndexOf(
    Object o) {
    for (int i = size()-1; i >=1; i--) {
      if (o == get(i)) {
        return i;
      }
    }
    return -1;
   }

  public ListIterator<LineString> listIterator() {
    throw new UnsupportedOperationException("Modifying a MultiLineString is not allowed");
     }

  public ListIterator<LineString> listIterator(
    int index) {
    throw new UnsupportedOperationException("Modifying a MultiLineString is not allowed");
      }

  public boolean remove(
    Object o) {
    throw new UnsupportedOperationException("Modifying a MultiLineString is not allowed");
     }

  public LineString remove(
    int index) {
    throw new UnsupportedOperationException("Modifying a MultiLineString is not allowed");
     }

  public boolean removeAll(
    Collection<?> c) {
    throw new UnsupportedOperationException("Modifying a MultiLineString is not allowed");
      }

  public boolean retainAll(
    Collection<?> c) {
    throw new UnsupportedOperationException("Modifying a MultiLineString is not allowed");
     }

  public LineString set(
    int index,
    LineString element) {
    throw new UnsupportedOperationException("Modifying a MultiLineString is not allowed");
     }

  public int size() {
    return getNumGeometries();
  }

  public List<LineString> subList(
    int fromIndex,
    int toIndex) {
    // TODO Auto-generated method stub
    return null;
  }

  public Object[] toArray() {
    return geometries;
  }

  public <T> T[] toArray(
    T[] a) {
    // TODO Auto-generated method stub
    return null;
  }

}
