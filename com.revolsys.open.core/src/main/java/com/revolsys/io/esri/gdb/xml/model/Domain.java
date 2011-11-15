package com.revolsys.io.esri.gdb.xml.model;

import com.revolsys.io.esri.gdb.xml.model.enums.FieldType;
import com.revolsys.io.esri.gdb.xml.model.enums.MergePolicyType;
import com.revolsys.io.esri.gdb.xml.model.enums.SplitPolicyType;

public class Domain implements Cloneable {

  private String domainName;

  private FieldType fieldType;

  private MergePolicyType mergePolicy;

  private SplitPolicyType splitPolicy;

  private String description;

  private String owner;

  @Override
  public Domain clone() {
    try {
      final Domain clone = (Domain)super.clone();
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public String getDescription() {
    return description;
  }

  public String getDomainName() {
    return domainName;
  }

  public FieldType getFieldType() {
    return fieldType;
  }

  public MergePolicyType getMergePolicy() {
    return mergePolicy;
  }

  public String getOwner() {
    return owner;
  }

  public SplitPolicyType getSplitPolicy() {
    return splitPolicy;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public void setDomainName(final String domainName) {
    this.domainName = domainName;
  }

  public void setFieldType(final FieldType fieldType) {
    this.fieldType = fieldType;
  }

  public void setMergePolicy(final MergePolicyType mergePolicy) {
    this.mergePolicy = mergePolicy;
  }

  public void setOwner(final String owner) {
    this.owner = owner;
  }

  public void setSplitPolicy(final SplitPolicyType splitPolicy) {
    this.splitPolicy = splitPolicy;
  }

  @Override
  public String toString() {
    return domainName;
  }

}
