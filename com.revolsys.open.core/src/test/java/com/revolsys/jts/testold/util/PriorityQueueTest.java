package com.revolsys.jts.testold.util;

import junit.framework.TestCase;

import com.revolsys.jts.util.PriorityQueue;

/**
 * @version 1.7
 */
public class PriorityQueueTest extends TestCase {
  public static void main(final String[] args) {
    junit.textui.TestRunner.run(PriorityQueueTest.class);
  }

  public PriorityQueueTest(final String name) {
    super(name);
  }

  private void addRandomItems(final PriorityQueue q, final int num) {
    for (int i = 0; i < num; i++) {
      q.add(new Integer((int)(num * Math.random())));
    }
  }

  private void checkOrder(final PriorityQueue q) {
    Comparable curr = null;

    while (!q.isEmpty()) {
      final Comparable next = (Comparable)q.poll();
      //  System.out.println(next);
      if (curr == null) {
        curr = next;
      } else {
        assertTrue(next.compareTo(curr) >= 0);
      }
    }
  }

  public void testOrder1() throws Exception {
    final PriorityQueue q = new PriorityQueue();
    q.add(new Integer(1));
    q.add(new Integer(10));
    q.add(new Integer(5));
    q.add(new Integer(8));
    q.add(new Integer(-1));
    checkOrder(q);
  }

  public void testOrderRandom1() throws Exception {
    final PriorityQueue q = new PriorityQueue();
    addRandomItems(q, 100);
    checkOrder(q);
  }
}
