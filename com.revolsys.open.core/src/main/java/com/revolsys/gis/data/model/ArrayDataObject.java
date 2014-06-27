package com.revolsys.gis.data.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.model.data.equals.EqualsInstance;

/**
 * The ArrayDataObject is an implementation of {@link DataObject} which uses an
 * array of Objects as the storage for the attribute values.
 *
 * @author Paul Austin
 */
public class ArrayDataObject extends BaseDataObject {
  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /** The object's attribute values. */
  private Object[] attributes;

  /**
   * Construct a new ArrayDataObject as a deep clone of the attribute values.
   * Objects can only be cloned if they have a publicly accessible
   * {@link #clone()} method.
   *
   * @param object The object to clone.
   */
  public ArrayDataObject(final DataObject object) {
    this(object.getMetaData(), object);
  }

  /**
   * Construct a new empty ArrayDataObject using the metaData.
   *
   * @param metaData The metaData defining the object type.
   */
  public ArrayDataObject(final DataObjectMetaData metaData) {
    this(metaData, null);
  }

  public ArrayDataObject(final DataObjectMetaData metaData,
    final Map<String, ? extends Object> values) {
    super(metaData);
    if (metaData == null) {
      this.attributes = new Object[0];
    } else {
      final int attributeCount = metaData.getAttributeCount();
      this.attributes = new Object[attributeCount];
      final Map<String, Object> defaultValues = metaData.getDefaultValues();
      setValuesByPath(defaultValues);
      setValues(values);
    }
    setState(DataObjectState.New);
  }

  /**
   * Create a clone of the object.
   *
   * @return The cloned object.
   */
  @Override
  public ArrayDataObject clone() {
    final ArrayDataObject clone = (ArrayDataObject)super.clone();
    clone.attributes = this.attributes.clone();
    return clone;
  }

  /**
   * Get the value of the attribute with the specified index.
   *
   * @param index The index of the attribute.
   * @return The attribute value.
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T extends Object> T getValue(final int index) {
    if (index < 0) {
      return null;
    } else {
      return (T)this.attributes[index];
    }
  }

  /**
   * Get the values of all attributes.
   *
   * @return The attribute value.
   */
  @Override
  public List<Object> getValues() {
    return Arrays.asList(this.attributes);
  }

  @Override
  public int hashCode() {
    return this.attributes.hashCode();
  }

  /**
   * Set the value of the attribute with the specified name.
   *
   * @param index The index of the attribute. param value The attribute value.
   * @param value The new value.
   */
  @Override
  public void setValue(final int index, final Object value) {
    if (index >= 0) {
      if (value instanceof SingleRecordIdentifier) {
        final SingleRecordIdentifier new_name = (SingleRecordIdentifier)value;
        System.out.println(value);
      }
      final Object oldValue = this.attributes[index];
      if (!EqualsInstance.INSTANCE.equals(oldValue, value)) {
        updateState();
      }
      this.attributes[index] = value;
    }
  }
}
