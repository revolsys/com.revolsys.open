package com.revolsys.geometry.index.rstartree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;

public class RStarBranch<T> extends BoundingBoxDoubleXY implements RStarNode<T> {
  final List<RStarNode<T>> items;

  boolean hasLeaves;

  private double area = Double.NaN;

  public RStarBranch(final int capacity, final RStarBranch<T> item1, final RStarBranch<T> item2) {
    this.items = new ArrayList<>(capacity);
    this.hasLeaves = false;
    this.items.add(item1);
    this.items.add(item2);
    recalculateBoundingBox();
  }

  public RStarBranch(final int capacity, final RStarLeaf<T> item) {
    super(item.getBoundingBox());
    this.items = new ArrayList<>(capacity);
    this.hasLeaves = true;
    recalculateBoundingBox();
  }

  public RStarBranch(final RStarBranch<T> node, final int startIndex) {
    this.hasLeaves = node.hasLeaves;
    final int nodeItemCount = node.items.size();
    this.items = new ArrayList<>(nodeItemCount - startIndex);
    for (int i = startIndex; i < nodeItemCount; i++) {
      final RStarNode<T> item = node.items.get(i);
      this.items.add(item);
    }
    recalculateBoundingBox();
  }

  @SuppressWarnings("unchecked")
  private void addItemsToList(final List<RStarLeaf<T>> items) {

    if (this.hasLeaves) {
      for (final BoundingBoxProxy item : this.items) {
        final RStarLeaf<T> leaf = (RStarLeaf<T>)item;
        items.add(leaf);
      }
    } else {
      for (final BoundingBoxProxy item : this.items) {
        final RStarBranch<T> node = (RStarBranch<T>)item;
        node.addItemsToList(items);
      }
    }
  }

  public void expandBoundingBox(final RStarLeaf<T> leaf) {
    expand(leaf);
    this.area = Double.NaN;
  }

  @Override
  public void forEach(final Consumer<? super T> action) {
    for (final RStarNode<T> item : this.items) {
      item.forEach(action);
    }
  }

  @Override
  public void forEach(final double x, final double y, final Consumer<? super T> action) {
    if (covers(x, y)) {
      for (final RStarNode<T> item : this.items) {
        item.forEach(x, y, action);
      }
    }
  }

  @Override
  public void forEach(final double minX, final double minY, final double maxX, final double maxY,
    final Consumer<? super T> action) {
    if (intersects(minX, minY, maxX, maxY)) {
      for (final RStarNode<T> item : this.items) {
        item.forEach(minX, minY, maxX, maxY, action);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void forEach(final Predicate<RStarBranch<T>> nodeFilter,
    final Predicate<RStarLeaf<T>> leafFilter, final Consumer<RStarLeaf<T>> action) {
    if (this.hasLeaves) {
      for (final BoundingBoxProxy item : this.items) {
        final RStarLeaf<T> leaf = (RStarLeaf<T>)item;
        if (leafFilter.test(leaf)) {
          action.accept(leaf);
        }
      }
    } else {
      for (final BoundingBoxProxy item : this.items) {
        final RStarBranch<T> node = (RStarBranch<T>)item;
        if (nodeFilter.test(node)) {
          node.forEach(nodeFilter, leafFilter, action);
        }
      }
    }
  }

  @Override
  public double getArea() {
    if (!Double.isFinite(this.area)) {
      this.area = super.getArea();
    }
    return this.area;
  }

  public RStarBranch<T> getMinimum(final Comparator<RStarNode<T>> comparator) {
    RStarBranch<T> minItem = null;
    for (final BoundingBoxProxy boundable : this.items) {
      @SuppressWarnings("unchecked")
      final RStarBranch<T> item = (RStarBranch<T>)boundable;
      if (minItem == null) {
        minItem = item;
      } else if (comparator.compare(minItem, item) > 0) {
        minItem = item;
      }

    }
    return minItem;
  }

  public RStarBranch<T> getMinimum(final Comparator<RStarNode<T>> comparator, final int maxCount) {
    RStarBranch<T> minItem = null;
    int count = 0;
    for (final BoundingBoxProxy boundable : this.items) {
      if (count > maxCount) {
        return minItem;
      }
      @SuppressWarnings("unchecked")
      final RStarBranch<T> item = (RStarBranch<T>)boundable;
      if (minItem == null) {
        minItem = item;
      } else if (comparator.compare(minItem, item) > 0) {
        minItem = item;
      }
      count++;
    }
    return minItem;
  }

  public boolean isHasLeaves() {
    return this.hasLeaves;
  }

  public void recalculateBoundingBox() {
    clear();
    this.items.forEach(this::expand);
  }

  public boolean remove(final RStarTree<T> tree, final BoundingBox boundingBox,
    final Predicate<RStarLeaf<T>> leafRemoveFilter, final List<RStarLeaf<T>> itemsToReinsert,
    final boolean isRoot) {

    if (intersects(boundingBox)) {
      // this is the easy part: remove nodes if they need to be removed

      for (final Iterator<RStarNode<T>> iterator = this.items.iterator(); iterator.hasNext();) {
        final RStarNode<T> item = iterator.next();
        if (boundingBox.intersects(boundingBox)) {
          if (this.hasLeaves) {
            final RStarLeaf<T> leaf = (RStarLeaf<T>)item;
            if (leafRemoveFilter.test(leaf)) {
              iterator.remove();
              tree.size--;
            }
          } else {
            final RStarBranch<T> node = (RStarBranch<T>)item;
            if (node.remove(tree, boundingBox, leafRemoveFilter, itemsToReinsert, false)) {
              iterator.remove();
            }
          }
        }

      }

      return removeIfEmpty(tree, isRoot, itemsToReinsert);
    }

    return false;
  }

  public boolean remove(final RStarTree<T> tree, final Predicate<RStarLeaf<T>> leafRemoveFilter,
    final List<RStarLeaf<T>> itemsToReinsert, final boolean isRoot) {

    // this is the easy part: remove nodes if they need to be removed
    if (this.hasLeaves) {
      for (final Iterator<RStarNode<T>> iterator = this.items.iterator(); iterator.hasNext();) {
        final RStarLeaf<T> leaf = (RStarLeaf<T>)iterator.next();
        if (leafRemoveFilter.test(leaf)) {
          iterator.remove();
          tree.size--;
        }

      }
    } else {
      for (final Iterator<RStarNode<T>> iterator = this.items.iterator(); iterator.hasNext();) {
        final RStarBranch<T> node = (RStarBranch<T>)iterator.next();
        if (node.remove(tree, leafRemoveFilter, itemsToReinsert, false)) {
          iterator.remove();
        }

      }
    }

    return removeIfEmpty(tree, isRoot, itemsToReinsert);
  }

  protected boolean removeIfEmpty(final RStarTree<T> tree, final boolean isRoot,
    final List<RStarLeaf<T>> itemsToReinsert) {
    if (isRoot) {
      if (this.items.isEmpty()) {
        this.hasLeaves = true;
        clear();
      }
      return false;
    } else {
      if (this.items.isEmpty()) {
        return true;
      } else if (this.items.size() < tree.getNodeMinItemCount()) {
        addItemsToList(itemsToReinsert);
        return true;
      } else {
        return false;
      }
    }
  }

  public void setSize(final int size) {
    int itemCount = this.items.size();
    while (itemCount > size) {
      itemCount--;
      this.items.remove(itemCount);
    }
    recalculateBoundingBox();
  }

  public void sortItems(final Comparator<RStarNode<T>> comparator) {
    this.items.sort(comparator);
  }
}
