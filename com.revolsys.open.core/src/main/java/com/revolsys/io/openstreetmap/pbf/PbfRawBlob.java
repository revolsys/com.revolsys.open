package com.revolsys.io.openstreetmap.pbf;

public class PbfRawBlob {
  private final String type;

  private final byte[] data;

  /**
   * Creates a new instance.
   * 
   * @param type
   *            The type of data represented by this blob. This corresponds to
   *            the type field in the blob header.
   * @param data
   *            The raw contents of the blob in binary undecoded form.
   */
  public PbfRawBlob(final String type, final byte[] data) {
    this.type = type;
    this.data = data;
  }

  /**
   * Gets the raw contents of the blob in binary undecoded form.
   * 
   * @return The raw blob data.
   */
  public byte[] getData() {
    return this.data;
  }

  /**
   * Gets the type of data represented by this blob. This corresponds to the
   * type field in the blob header.
   * 
   * @return The blob type.
   */
  public String getType() {
    return this.type;
  }
}
