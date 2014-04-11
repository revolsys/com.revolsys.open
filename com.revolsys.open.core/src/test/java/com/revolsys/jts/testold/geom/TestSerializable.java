/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */

package com.revolsys.jts.testold.geom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;

/**
 * @version 1.7
 */
public class TestSerializable {

  public static final String FILENAME = "c:\\testSerial.txt";

  public static final GeometryFactory fact = GeometryFactory.getFactory();

  public static void main(final String[] args) {
    final TestSerializable test = new TestSerializable();
    test.run();
  }

  public TestSerializable() {
  }

  boolean compare(final Object o1, final Object o2) {
    boolean matched = false;
    if (o1 instanceof Envelope) {
      if (!((Envelope)o1).equals(o2)) {
        System.out.println("expected " + o1 + ", found " + o2);
      } else {
        matched = true;
      }
    } else if (o1 instanceof Geometry) {
      if (!((Geometry)o1).equalsExact((Geometry)o2)) {
        System.out.println("expected " + o1 + ", found " + o2);
      } else {
        matched = true;
      }
    }
    if (matched) {
      System.out.println("found match for object");
    }
    return true;
  }

  List createData() {
    final List objList = new ArrayList();

    final Envelope env = new Envelope(123, 456, 123, 456);
    objList.add(env);

    objList.add(GeometryTestFactory.createBox(fact, 0.0, 100.0, 10, 10.0));

    return objList;

  }

  void readData(final List objList) {
    File file; // simply a file name
    FileInputStream stream; // generic stream to the file
    ObjectInputStream objStream; // stream for objects to the file

    file = new File(FILENAME);

    try {
      // setup a stream to a physical file on the filesystem
      stream = new FileInputStream(file);

      // attach a stream capable of writing objects to the stream that is
      // connected to the file
      objStream = new ObjectInputStream(stream);

      int count = 0;
      final Object obj = objStream.readObject();
      final List inputList = (List)obj;
      for (final Iterator i = inputList.iterator(); i.hasNext();) {
        compare(objList.get(count++), i.next());
      }

      // while (objStream.available() > 0) {
      // Object obj = objStream.readObject();
      // compare(objList.get(count++), obj);
      // }
      objStream.close();

    } catch (final Exception e) {
      System.err.println("Things not going as planned.");
      e.printStackTrace();
    } // catch
  }

  public void run() {
    final List objList = createData();
    writeData(objList);
    readData(objList);
  }

  void writeData(final List objList) {
    File file; // simply a file name
    FileOutputStream outStream; // generic stream to the file
    ObjectOutputStream objStream; // stream for objects to the file

    file = new File(FILENAME);

    try {
      // setup a stream to a physical file on the filesystem
      outStream = new FileOutputStream(file);

      // attach a stream capable of writing objects to the stream that is
      // connected to the file
      objStream = new ObjectOutputStream(outStream);

      objStream.writeObject(objList);
      // for (Iterator i = objList.iterator(); i.hasNext(); )
      // {
      // objStream.writeObject(i.next());
      // }
      objStream.close();

    } catch (final IOException e) {
      System.err.println("Things not going as planned.");
      e.printStackTrace();
    } // catch
  }

}
