package com.revolsys.gis.parallel;

import com.revolsys.collection.ArrayUtil;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;
import com.revolsys.parallel.channel.MultiInputSelector;
import com.revolsys.parallel.channel.store.Buffer;
import com.revolsys.parallel.process.AbstractInOutProcess;

public abstract class AbstractMergeProcess extends
  AbstractInOutProcess<DataObject, DataObject> {

  private static final int OTHER_INDEX = 1;

  private static final int SOURCE_INDEX = 0;

  private Channel<DataObject> otherIn;

  private int otherInBufferSize = 0;

  protected boolean acceptObject(final DataObject object) {
    return true;
  }

  private void addObjectFromOtherChannel(
    final Channel<DataObject>[] channels,
    final boolean[] guard,
    final DataObject[] objects,
    final int channelIndex) {
    int otherIndex;
    if (channelIndex == SOURCE_INDEX) {
      otherIndex = OTHER_INDEX;
    } else {
      otherIndex = SOURCE_INDEX;
    }
    final Channel<DataObject> otherChannel = channels[otherIndex];
    if (otherChannel == null) {
      guard[otherIndex] = false;
      guard[channelIndex] = true;
    } else if (guard[otherIndex]) {
      while (objects[otherIndex] == null) {
        try {
          final DataObject object = otherChannel.read();
          if (acceptObject(object)) {
            objects[otherIndex] = object;
            return;
          }
        } catch (final ClosedException e) {
          guard[otherIndex] = false;
          guard[channelIndex] = true;
          return;
        }
      }
    }
  }

  /**
   * Add an object from the other (otherId) channel.
   * 
   * @param object The object to add.
   */
  protected abstract void addOtherObject(DataObject object);

  private DataObjectMetaData addSavedObjects(
    final DataObjectMetaData currentType,
    final String currentTypeName,
    final Channel<DataObject> out,
    final boolean[] guard,
    final DataObject[] objects) {
    final DataObject sourceObject = objects[SOURCE_INDEX];
    final DataObject otherObject = objects[OTHER_INDEX];
    if (sourceObject == null) {
      if (otherObject == null) {
        return null;
      } else {
        addOtherObject(otherObject);
        objects[OTHER_INDEX] = null;
        guard[OTHER_INDEX] = true;
        return otherObject.getMetaData();
      }
    } else if (otherObject == null) {
      if (sourceObject == null) {
        return null;
      } else {
        addSourceObject(sourceObject);
        objects[SOURCE_INDEX] = null;
        guard[SOURCE_INDEX] = true;
        return sourceObject.getMetaData();
      }
    } else {
      final DataObjectMetaData sourceType = sourceObject.getMetaData();
      final String sourceTypeName = sourceType.getPath();
      final DataObjectMetaData otherType = otherObject.getMetaData();
      final String otherTypeName = otherType.getPath();
      if (sourceTypeName.equals(currentTypeName)) {
        addSourceObject(sourceObject);
        objects[SOURCE_INDEX] = null;
        guard[SOURCE_INDEX] = true;
        objects[OTHER_INDEX] = otherObject;
        guard[OTHER_INDEX] = false;
        return currentType;
      } else if (otherTypeName.equals(currentTypeName)) {
        addOtherObject(otherObject);
        objects[SOURCE_INDEX] = sourceObject;
        guard[SOURCE_INDEX] = false;
        objects[OTHER_INDEX] = null;
        guard[OTHER_INDEX] = true;
        return currentType;
      } else {
        processObjects(currentType, out);
        final int nameCompare = sourceTypeName.toString().compareTo(
          otherTypeName.toString());
        if (nameCompare < 0) {
          // If the first feature type name is < second feature type
          // name
          // then add the first feature and save the second feature
          // for later
          addSourceObject(sourceObject);
          objects[SOURCE_INDEX] = null;
          guard[SOURCE_INDEX] = true;
          objects[OTHER_INDEX] = otherObject;
          guard[OTHER_INDEX] = false;
          return sourceType;
        } else if (nameCompare == 0) {
          // If both features have the same type them add them
          addSourceObject(sourceObject);
          addOtherObject(otherObject);
          objects[SOURCE_INDEX] = null;
          guard[SOURCE_INDEX] = true;
          objects[OTHER_INDEX] = null;
          guard[OTHER_INDEX] = true;
          return sourceType;
        } else {
          // If the first feature type name is > second feature type
          // name
          // then add the second feature and save the first feature
          // for later
          addOtherObject(otherObject);
          objects[SOURCE_INDEX] = sourceObject;
          guard[SOURCE_INDEX] = false;
          objects[OTHER_INDEX] = null;
          guard[OTHER_INDEX] = true;
          return otherType;
        }
      }
    }

  }

  /**
   * Add an object from the source (in) channel.
   * 
   * @param object The object to add.
   */
  protected abstract void addSourceObject(DataObject object);

  /**
   * @return the in
   */
  public Channel<DataObject> getOtherIn() {
    if (otherIn == null) {
      if (otherInBufferSize < 1) {
        setOtherIn(new Channel<DataObject>());
      } else {
        final Buffer<DataObject> buffer = new Buffer<DataObject>(
          otherInBufferSize);
        setOtherIn(new Channel<DataObject>(buffer));
      }
    }
    return otherIn;
  }

  public int getOtherInBufferSize() {
    return otherInBufferSize;
  }

  protected abstract void processObjects(
    DataObjectMetaData currentType,
    Channel<DataObject> out);

  @Override
  @SuppressWarnings("unchecked")
  protected void run(final Channel<DataObject> in, final Channel<DataObject> out) {
    setUp();
    try {
      DataObjectMetaData currentType = null;
      String currentTypeName = null;
      final Channel<DataObject>[] channels = ArrayUtil.create(in, otherIn);

      final boolean[] guard = new boolean[] {
        true, true
      };
      final DataObject[] objects = new DataObject[2];
      final String[] typePaths = new String[2];
      for (int i = 0; i < 2; i++) {
        try {
          final Channel<DataObject> channel = channels[i];
          if (channel == null) {
            guard[i] = false;
          } else {
            DataObject object = null;
            boolean accept = false;
            do {
              object = channel.read();
              accept = acceptObject(object);
            } while (!accept);
            if (accept) {
              objects[i] = object;
              typePaths[i] = objects[i].getMetaData().getPath();
            }

          }
        } catch (final ClosedException e) {
          guard[i] = false;
        }
      }
      final DataObject otherObject = objects[OTHER_INDEX];
      if (typePaths[SOURCE_INDEX] != null) {
        final DataObject sourceObject = objects[SOURCE_INDEX];
        if (typePaths[OTHER_INDEX] != null) {
          final int nameCompare = typePaths[SOURCE_INDEX].toString().compareTo(
            typePaths[OTHER_INDEX].toString());
          if (nameCompare <= 0) {
            currentType = sourceObject.getMetaData();
            currentTypeName = typePaths[SOURCE_INDEX];
            addSourceObject(sourceObject);
            objects[SOURCE_INDEX] = null;
            if (nameCompare != 0) {
              guard[OTHER_INDEX] = false;
            }
          }
          if (nameCompare >= 0) {
            currentType = otherObject.getMetaData();
            currentTypeName = typePaths[OTHER_INDEX];
            addOtherObject(otherObject);
            objects[OTHER_INDEX] = null;
            if (nameCompare != 0) {
              guard[SOURCE_INDEX] = false;
            }
          }
        } else {
          currentType = sourceObject.getMetaData();
          currentTypeName = typePaths[SOURCE_INDEX];
          if (otherObject != null) {
            addSourceObject(otherObject);
          }
        }
      } else {
        currentType = otherObject.getMetaData();
        currentTypeName = typePaths[OTHER_INDEX];
        if (otherObject != null) {
          addOtherObject(otherObject);
        }
        objects[OTHER_INDEX] = null;
      }
      try {
        final MultiInputSelector alt = new MultiInputSelector();
        final boolean running = true;
        while (running) {
          final int channelIndex = alt.select(channels, guard, 1000);
          if (channelIndex >= 0) {
            final DataObject object = channels[channelIndex].read();
            if (acceptObject(object)) {
              final DataObjectMetaData type = object.getMetaData();
              final String typePath = type.getPath();
              if (currentTypeName == null || typePath.equals(currentTypeName)) {
                currentTypeName = typePath;
                currentType = type;

                if (channelIndex == SOURCE_INDEX) {
                  addSourceObject(object);
                } else {
                  addOtherObject(object);
                }
              } else {
                objects[channelIndex] = object;
                addObjectFromOtherChannel(channels, guard, objects,
                  channelIndex);
                currentType = addSavedObjects(currentType, currentTypeName,
                  out, guard, objects);
                if (currentType != null) {
                  currentTypeName = currentType.getPath();
                }
              }
            }
          } else {
            if (channels[0].isClosed()) {
              guard[1] = true;
            } else if (channels[1].isClosed()) {
              guard[0] = true;
            }
          }
        }
      } finally {
        try {
          while (addSavedObjects(currentType, currentTypeName, out, guard,
            objects) != null) {
          }
          processObjects(currentType, out);
        } finally {

        }
      }
    } finally {
      otherIn.readDisconnect();
      tearDown();
    }
  }

  /**
   * @param in the in to set
   */
  public void setOtherIn(final Channel<DataObject> in) {
    this.otherIn = in;
    in.readConnect();
  }

  public void setOtherInBufferSize(final int otherInBufferSize) {
    this.otherInBufferSize = otherInBufferSize;
  }

  protected void setUp() {
  }

  protected void tearDown() {
  }
}
