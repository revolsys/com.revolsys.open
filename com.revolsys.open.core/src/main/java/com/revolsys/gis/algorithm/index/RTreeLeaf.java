package com.revolsys.gis.algorithm.index;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.vividsolutions.jts.geom.Envelope;

public class RTreeLeaf<T> extends RTreeNode<T> {

  public RTreeLeaf() {
  }

  public RTreeLeaf(int size) {
    this.objects = new Object[size];
    this.envelopes = new Envelope[size];
  }

  private Object[] objects;

  private Envelope[] envelopes;

  private int size = 0;

  public void add(Envelope envelope, T object) {
    envelopes[size] = envelope;
    objects[size] = object;
    size++;
    expandToInclude(envelope);
  }

  public int getSize() {
    return size;
  }

  @SuppressWarnings("unchecked")
  public List<RTreeNode<T>> split(Envelope envelope, T object) {
    RTreeLeaf<T> leaf1 = new RTreeLeaf<T>(objects.length);
    RTreeLeaf<T> leaf2 = new RTreeLeaf<T>(objects.length);

    // TODO Add some ordering to the results
    final int midPoint = (int)Math.ceil(size / 2.0);
    for (int i = 0; i <= midPoint; i++) {
      Envelope envelope1 = envelopes[i];
      T object1 = getObject(i);
      leaf1.add(envelope1, object1);
    }
    for (int i = midPoint + 1; i < size; i++) {
      Envelope envelope1 = envelopes[i];
      T object1 = getObject(i);
      leaf2.add(envelope1, object1);
    }
    leaf2.add(envelope, object);
    return Arrays.<RTreeNode<T>> asList(leaf1, leaf2);
  }

  @SuppressWarnings("unchecked")
  public T getObject(int index) {
    return (T)objects[index];
  }

  public boolean remove(LinkedList<RTreeNode<T>> path, Envelope envelope,
    T object) {
    for (int i = 0; i < size; i++) {
      Envelope envelope1 = envelopes[i];
      T object1 = getObject(i);
      if (object1 == object) {
        if (envelope1.equals(envelope)) {
          System.arraycopy(envelopes, i + 1, envelopes, i, size - i - 1);
          envelopes[size - 1] = null;
          System.arraycopy(objects, i + 1, objects, i, size - i - 1);
          objects[size - 1] = null;
          size--;
          path.add(this);
          updateEnvelope();
          return true;
        } else {
          System.err.println();
        }
      }
    }
    return false;
  }

  protected void updateEnvelope() {
    init();
    for (int i = 0; i < size; i++) {
      Envelope envelope = envelopes[i];
      expandToInclude(envelope);
    }
  }

  @Override
  public boolean visit(Envelope envelope, Filter<T> filter, Visitor<T> visitor) {
    for (int i = 0; i < size; i++) {
      Envelope objectEnvelope = envelopes[i];
      if (envelope.intersects(objectEnvelope)) {
        T object = getObject(i);
        if (filter.accept(object)) {
          if (!visitor.visit(object)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public boolean visit(Envelope envelope, Visitor<T> visitor) {
    for (int i = 0; i < size; i++) {
      Envelope objectEnvelope = envelopes[i];
      if (envelope.intersects(objectEnvelope)) {
        T object = getObject(i);
        if (!visitor.visit(object)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public boolean visit(Visitor<T> visitor) {
    for (int i = 0; i < size; i++) {
      T object = getObject(i);
      if (!visitor.visit(object)) {
        return false;
      }
    }
    return true;
  }
}
