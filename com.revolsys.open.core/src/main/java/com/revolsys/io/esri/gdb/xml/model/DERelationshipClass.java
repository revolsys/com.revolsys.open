package com.revolsys.io.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.io.esri.gdb.xml.model.enums.RelCardinality;
import com.revolsys.io.esri.gdb.xml.model.enums.RelClassKey;
import com.revolsys.io.esri.gdb.xml.model.enums.RelKeyType;
import com.revolsys.io.esri.gdb.xml.model.enums.RelNotification;

public class DERelationshipClass extends DETable {
  private RelCardinality cardinality;

  private RelNotification notification;

  private boolean isAttributed;

  private boolean isComposite;

  private List<String> originClassNames = new ArrayList<String>();

  private List<String> destinationClassNames = new ArrayList<String>();

  private RelKeyType reyType;

  private RelClassKey classKey;

  private String forwardPathLabel;

  private String backwardPathLabel;

  private boolean isReflexive;

  private List<RelationshipClassKey> originClassKeys = new ArrayList<RelationshipClassKey>();

  private List<RelationshipClassKey> destinationClassKeys;

  private List<RelationshipRule> relationshipRules = new ArrayList<RelationshipRule>();

  public DERelationshipClass() {
    super("");
  }

  public String getBackwardPathLabel() {
    return backwardPathLabel;
  }

  public RelCardinality getCardinality() {
    return cardinality;
  }

  public RelClassKey getClassKey() {
    return classKey;
  }

  public List<RelationshipClassKey> getDestinationClassKeys() {
    return destinationClassKeys;
  }

  public List<String> getDestinationClassNames() {
    return destinationClassNames;
  }

  public String getForwardPathLabel() {
    return forwardPathLabel;
  }

  public RelNotification getNotification() {
    return notification;
  }

  public List<RelationshipClassKey> getOriginClassKeys() {
    return originClassKeys;
  }

  public List<String> getOriginClassNames() {
    return originClassNames;
  }

  public List<RelationshipRule> getRelationshipRules() {
    return relationshipRules;
  }

  public RelKeyType getReyType() {
    return reyType;
  }

  public boolean isAttributed() {
    return isAttributed;
  }

  public boolean isComposite() {
    return isComposite;
  }

  public boolean isReflexive() {
    return isReflexive;
  }

  public void setAttributed(final boolean isAttributed) {
    this.isAttributed = isAttributed;
  }

  public void setBackwardPathLabel(final String backwardPathLabel) {
    this.backwardPathLabel = backwardPathLabel;
  }

  public void setCardinality(final RelCardinality cardinality) {
    this.cardinality = cardinality;
  }

  public void setClassKey(final RelClassKey classKey) {
    this.classKey = classKey;
  }

  public void setComposite(final boolean isComposite) {
    this.isComposite = isComposite;
  }

  public void setDestinationClassKeys(
    final List<RelationshipClassKey> destinationClassKeys) {
    this.destinationClassKeys = destinationClassKeys;
  }

  public void setDestinationClassNames(final List<String> destinationClassNames) {
    this.destinationClassNames = destinationClassNames;
  }

  public void setForwardPathLabel(final String forwardPathLabel) {
    this.forwardPathLabel = forwardPathLabel;
  }

  public void setNotification(final RelNotification notification) {
    this.notification = notification;
  }

  public void setOriginClassKeys(
    final List<RelationshipClassKey> originClassKeys) {
    this.originClassKeys = originClassKeys;
  }

  public void setOriginClassNames(final List<String> originClassNames) {
    this.originClassNames = originClassNames;
  }

  public void setReflexive(final boolean isReflexive) {
    this.isReflexive = isReflexive;
  }

  public void setRelationshipRules(
    final List<RelationshipRule> relationshipRules) {
    this.relationshipRules = relationshipRules;
  }

  public void setReyType(final RelKeyType reyType) {
    this.reyType = reyType;
  }

}
