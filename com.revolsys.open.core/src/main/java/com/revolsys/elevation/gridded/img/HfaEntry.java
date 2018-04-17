package com.revolsys.elevation.gridded.img;

import java.util.Set;

import com.revolsys.collection.map.MapEx;

public class HfaEntry implements MapEx {
  private boolean dirty;

  private final long filePosition;

  private final ImgGriddedElevationReader reader;

  private final HfaEntry parent;

  private final HfaEntry previous;

  private long nextPos;

  private HfaEntry next;

  private final long childPos;

  private HfaEntry child;

  private final String name;

  private final String typeName;

  private final HfaType type;

  private final long dataPos;

  private final int dataSize;

  private boolean isMifObject;

  boolean isMIFObject;

  // private GByte pabyData;

  private MapEx fieldValues;

  public HfaEntry(final ImgGriddedElevationReader reader, final int position) {
    this(reader, position, null, null);
  }

  HfaEntry(final ImgGriddedElevationReader reader, final long position, final HfaEntry parent,
    final HfaEntry previous) {
    this.reader = reader;
    this.reader.seek(position);
    this.filePosition = position;
    this.parent = parent;
    this.previous = previous;

    this.nextPos = reader.readInt();
    reader.readInt();
    reader.readInt();
    this.childPos = reader.readInt();
    this.dataPos = reader.readInt();
    this.dataSize = reader.readInt();

    this.name = reader.readString0(64);
    this.typeName = reader.readString0(32);
    this.type = reader.findType(this.typeName);
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    return getFieldValues().entrySet();
  }

  public boolean equalsName(final String name) {
    return name.equals(this.name);
  }

  public boolean equalsType(final String typeName) {
    return typeName.equals(this.typeName);
  }

  public HfaEntry GetChild() {
    if (this.child == null && this.childPos != 0) {
      this.child = new HfaEntry(this.reader, this.childPos, this, null);
    }

    return this.child;
  }

  public double GetDoubleField(final String fieldPath) {
    return getDouble(fieldPath);
  }

  private MapEx getFieldValues() {
    if (this.type == null) {
      return MapEx.EMPTY;
    } else if (this.fieldValues == null) {
      if (this.dataPos > 0) {
        this.reader.seek(this.dataPos);
      } else {
        this.reader.seek(this.filePosition);
      }
      this.fieldValues = this.type.readFieldValues(this.reader);
    }
    return this.fieldValues;
  }

  public long getFilePosition() {
    return this.filePosition;
  }

  public int GetIntField(final String fieldPath) {
    return getInteger(fieldPath);
  }

  public String getName() {
    return this.name;
  }

  public HfaEntry GetNamedChild(final String pszName) {
    for (HfaEntry poEntry = GetChild(); poEntry != null; poEntry = poEntry.GetNext()) {
      if (poEntry.equalsName(pszName)) {
        return poEntry;
      }
    }
    return null;
  }

  HfaEntry getNext() {
    // Do we need to create the next node?
    if (this.next == null && this.nextPos != 0) {
      // Check if we have a loop on the next node in this sibling chain.
      HfaEntry past;

      for (past = this; past != null && past.filePosition != this.nextPos; past = past.previous) {
      }

      if (past != null) {
        System.err
          .println(String.format("Corrupt (looping) entry in %s, ignoring some entries after %s.",
            this.reader, this.name));
        this.nextPos = 0;
        return null;
      }

      this.next = new HfaEntry(this.reader, this.nextPos, this.parent, this);
      if (this.next == null) {
        this.nextPos = 0;
      }
    }

    return this.next;
  }

  public HfaEntry GetNext() {
    if (this.next == null && this.nextPos != 0) {
      HfaEntry past;

      for (past = this; past != null && past.filePosition != this.nextPos; past = past.previous) {
      }

      if (past != null) {
        this.nextPos = 0;
        throw new IllegalArgumentException(
          String.format("Corrupt (looping) entry in %s, ignoring some entries after .", this.name));
      }

      return new HfaEntry(this.reader, this.nextPos, this.parent, this);
    }

    return this.next;
  }

  public String GetStringField(final String fieldPath) {
    return getString(fieldPath);
  }

  // HfaEntry( String dictionary,
  // String typeName,
  // int dataSize,
  // GByte pabyDataIn ) {
  // this.isMIFObject = true;
  //// Initialize Entry
  // memset(szName, 0, sizeof(szName));
  //
  //// Create a dummy HFAInfo_t.
  // psHFA = static_cast<HFAInfo_t *>(CPLCalloc(sizeof(HFAInfo_t), 1));
  //
  // psHFA.eAccess = HFA_ReadOnly;
  // psHFA.bTreeDirty = false;
  // psHFA.poRoot = this;
  //
  // psHFA.poDictionary = new HFADictionary(dictionary);
  //
  //// Work out the type for this MIFObject.
  // memset(szType, 0, sizeof(szType));
  // snprintf(szType, sizeof(szType), "%s", pszTypeName);
  //
  // type = psHFA.poDictionary.FindType(szType);
  //
  // nDataSize = nDataSizeIn;
  // pabyData = pabyDataIn;
  // }

  public String getTypeName() {
    return this.typeName;
  }

  @Override
  public <T> T getValue(final CharSequence name) {
    final MapEx fieldValues = getFieldValues();
    return fieldValues.getValue(name);
  }

  @Override
  public String toString() {
    return this.name + ": " + this.typeName + "\n" + getFieldValues();
  }
}
