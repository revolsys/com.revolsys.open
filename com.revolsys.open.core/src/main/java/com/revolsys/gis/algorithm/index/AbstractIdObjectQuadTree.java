package com.revolsys.gis.algorithm.index;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import com.revolsys.gis.algorithm.index.quadtree.QuadTree;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.visitor.CreateListVisitor;

public abstract class AbstractIdObjectQuadTree<T> implements IdObjectIndex<T> {

  private final QuadTree<Integer> index = new QuadTree<>();

  public void add(final Collection<Integer> ids) {
    for (final Integer id : ids) {
      final T object = getObject(id);
      add(object);
    }
  }

  @Override
  public T add(final T object) {
    final BoundingBox envelope = getEnvelope(object);
    final int id = getId(object);
    this.index.insert(envelope, id);
    return object;
  }

  @Override
  public void forEach(final Consumer<? super T> action, final BoundingBox envelope) {
    this.index.forEach((id) -> {
      final T object = getObject(id);
      final BoundingBox e = getEnvelope(object);
      if (e.intersects(envelope)) {
        action.accept(object);
      }
    } , envelope);
  }

  @Override
  public Iterator<T> iterator() {
    return queryAll().iterator();
  }

  @Override
  public List<T> query(final BoundingBox envelope) {
    final CreateListVisitor<T> visitor = new CreateListVisitor<T>();
    forEach(visitor, envelope);
    return visitor.getList();
  }

  public List<T> queryAll() {
    final List<Integer> ids = this.index.getAll();
    return getObjects(ids);
  }

  @Override
  public boolean remove(final T object) {
    final BoundingBox envelope = getEnvelope(object);
    final int id = getId(object);
    return this.index.remove(envelope, id);
  }

  public void removeAll(final Collection<T> objects) {
    for (final T object : objects) {
      remove(object);
    }
  }

}
