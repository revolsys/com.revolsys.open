package com.revolsys.gis.esri.gdb.xml.model;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.esri.gdb.xml.model.enums.RelCardinality;
import com.revolsys.gis.esri.gdb.xml.model.enums.RelClassKey;
import com.revolsys.gis.esri.gdb.xml.model.enums.RelKeyType;
import com.revolsys.gis.esri.gdb.xml.model.enums.RelNotification;

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

  public RelCardinality getCardinality() {
    return cardinality;
  }

  public void setCardinality(RelCardinality cardinality) {
    this.cardinality = cardinality;
  }

  public RelNotification getNotification() {
    return notification;
  }

  public void setNotification(RelNotification notification) {
    this.notification = notification;
  }

  public boolean isAttributed() {
    return isAttributed;
  }

  public void setAttributed(boolean isAttributed) {
    this.isAttributed = isAttributed;
  }

  public boolean isComposite() {
    return isComposite;
  }

  public void setComposite(boolean isComposite) {
    this.isComposite = isComposite;
  }

  public List<String> getOriginClassNames() {
    return originClassNames;
  }

  public void setOriginClassNames(List<String> originClassNames) {
    this.originClassNames = originClassNames;
  }

  public List<String> getDestinationClassNames() {
    return destinationClassNames;
  }

  public void setDestinationClassNames(List<String> destinationClassNames) {
    this.destinationClassNames = destinationClassNames;
  }

  public RelKeyType getReyType() {
    return reyType;
  }

  public void setReyType(RelKeyType reyType) {
    this.reyType = reyType;
  }

  public RelClassKey getClassKey() {
    return classKey;
  }

  public void setClassKey(RelClassKey classKey) {
    this.classKey = classKey;
  }

  public String getForwardPathLabel() {
    return forwardPathLabel;
  }

  public void setForwardPathLabel(String forwardPathLabel) {
    this.forwardPathLabel = forwardPathLabel;
  }

  public String getBackwardPathLabel() {
    return backwardPathLabel;
  }

  public void setBackwardPathLabel(String backwardPathLabel) {
    this.backwardPathLabel = backwardPathLabel;
  }

  public boolean isReflexive() {
    return isReflexive;
  }

  public void setReflexive(boolean isReflexive) {
    this.isReflexive = isReflexive;
  }

  public List<RelationshipClassKey> getOriginClassKeys() {
    return originClassKeys;
  }

  public void setOriginClassKeys(List<RelationshipClassKey> originClassKeys) {
    this.originClassKeys = originClassKeys;
  }

  public List<RelationshipClassKey> getDestinationClassKeys() {
    return destinationClassKeys;
  }

  public void setDestinationClassKeys(
    List<RelationshipClassKey> destinationClassKeys) {
    this.destinationClassKeys = destinationClassKeys;
  }

  public List<RelationshipRule> getRelationshipRules() {
    return relationshipRules;
  }

  public void setRelationshipRules(List<RelationshipRule> relationshipRules) {
    this.relationshipRules = relationshipRules;
  }

}
