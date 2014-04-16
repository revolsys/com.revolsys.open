package com.revolsys.gis.algorithm.index;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Envelope;

public class RTreeLeaf<T> extends RTreeNode<T> {

  /**
   * 
   */
  private static final long serialVersionUID = 5073275000676209987L;

  private Object[] objects;

  private BoundingBox[] envelopes;

  private int size = 0;

  public RTreeLeaf() {
  }

  public RTreeLeaf(final int size) {
    this.objects = new Object[size];
    this.envelopes = new Envelope[size];
  }

  public void add(final BoundingBox envelope, final T object) {
    envelopes[size] = envelope;
    objects[size] = object;
    size++;
    expandToInclude(envelope);
  }

  @SuppressWarnings("unchecked")
  public T getObject(final int index) {
    return (T)objects[index];
  }

  public int getSize() {
    return size;
  }

  @Override
  public boolean remove(final LinkedList<RTreeNode<T>> path,
    final BoundingBox envelope, final T object) {
    for (int i = 0; i < size; i++) {
      final BoundingBox envelope1 = envelopes[i];
      final T object1 = getObject(i);
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

  @SuppressWarnings("unchecked")
  public List<RTreeNode<T>> split(final BoundingBox envelope, final T object) {
    final RTreeLeaf<T> leaf1 = new RTreeLeaf<T>(objects.length);
    final RTreeLeaf<T> leaf2 = new RTreeLeaf<T>(objects.length);

    // TODO Add some ordering to the results
    final int midPoint = (int)Math.ceil(size / 2.0);
    for (int i = 0; i <= midPoint; i++) {
      final BoundingBox envelope1 = envelopes[i];
      final T object1 = getObject(i);
      leaf1.add(envelope1, object1);
    }
    for (int i = midPoint + 1; i < size; i++) {
      final BoundingBox envelope1 = envelopes[i];
      final T object1 = getObject(i);
      leaf2.add(envelope1, object1);
    }
    leaf2.add(envelope, object);
    return Arrays.<RTreeNode<T>> asList(leaf1, leaf2);
  }

  @Override
  protected void updateEnvelope() {
    setToNull();
    for (int i = 0; i < size; i++) {
      final BoundingBox envelope = envelopes[i];
      expandToInclude(envelope);
    }
  }

  @Override
  public boolean visit(final BoundingBox envelope, final Filter<T> filter,
    final Visitor<T> visitor) {
    for (int i = 0; i < size; i++) {
      final BoundingBox objectEnvelope = envelopes[i];
      if (envelope.intersects(objectEnvelope)) {
        final T object = getObject(i);
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
  public boolean visit(final BoundingBox envelope, final Visitor<T> visitor) {
    for (int i = 0; i < size; i++) {
      final BoundingBox objectEnvelope = envelopes[i];
      if (envelope.intersects(objectEnvelope)) {
        final T object = getObject(i);
        if (!visitor.visit(object)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public boolean visit(final Visitor<T> visitor) {
    for (int i = 0; i < size; i++) {
      final T object = getObject(i);
      if (!visitor.visit(object)) {
        return false;
      }
    }
    return true;
  }
}
