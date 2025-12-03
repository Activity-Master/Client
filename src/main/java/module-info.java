import com.guicedee.activitymaster.fsdm.client.services.ConsoleLogActivityMasterProgressMaster;
import com.guicedee.activitymaster.fsdm.client.services.events.IOnCreateUser;
import com.guicedee.activitymaster.fsdm.client.services.events.IOnExpireUser;
import com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterProgressMonitor;
import com.guicedee.client.services.config.IGuiceScanModuleInclusions;

module com.guicedee.activitymaster.fsdm.client {
  exports com.guicedee.activitymaster.fsdm.client;
  exports com.guicedee.activitymaster.fsdm.client.services.administration;
  exports com.guicedee.activitymaster.fsdm.client.services.events;
  exports com.guicedee.activitymaster.fsdm.client.services.capabilities;
  exports com.guicedee.activitymaster.fsdm.client.services.systems;
  exports com.guicedee.activitymaster.fsdm.client.services.deserializers;

  requires transitive com.guicedee.guicedinjection;
//  requires transitive io.cloudevents;

  requires transitive io.vertx.core;

//	exports com.guicedee.activitymaster.fsdm.client.services.converters.providers;

  exports com.guicedee.activitymaster.fsdm.client.services.classifications;
  exports com.guicedee.activitymaster.fsdm.client.services.classifications.address;


  exports com.guicedee.activitymaster.fsdm.client.services.builders;


  exports com.guicedee.activitymaster.fsdm.client.services.builders.warehouse;
  exports com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.activeflag;
  exports com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.classifications;
  exports com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.enterprise;
  exports com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.systems;

  exports com.guicedee.activitymaster.fsdm.client.services;
  exports com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.resourceitem;
  exports com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.security;
  exports com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.products;
  exports com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.address;
  exports com.guicedee.activitymaster.fsdm.client.services.dto;

  //exports com.guicedee.activitymaster.fsdm.client.implementations;

  exports com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.arrangements;
  exports com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.geography;
  exports com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.rules;
  exports com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.party;
  exports com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.events;
  exports com.guicedee.activitymaster.fsdm.client.services.classifications.types;
  exports com.guicedee.activitymaster.fsdm.client.services.capabilities.contains;
  exports com.guicedee.activitymaster.fsdm.client.services.annotations;
  exports com.guicedee.activitymaster.fsdm.client.services.exceptions;
  exports com.guicedee.activitymaster.fsdm.client.implementations;

  opens com.guicedee.activitymaster.fsdm.client.services.builders.warehouse to com.guicedee.activitymaster.fsdm;

//	requires com.guicedee.services.openapi;

  requires transitive com.entityassist;
  requires transitive jakarta.persistence;

  requires static lombok;
  requires transitive jakarta.validation;
  requires transitive org.hibernate.reactive;
  requires transitive org.hibernate.validator;
  requires transitive org.apache.logging.log4j;

  /*requires com.guicedee.guicedpersistence;*/

  uses com.guicedee.activitymaster.fsdm.client.services.systems.IActivityMasterSystem;


  uses IOnCreateUser;
  uses IOnExpireUser;
  uses IActivityMasterProgressMonitor;

  provides IActivityMasterProgressMonitor with ConsoleLogActivityMasterProgressMaster;

  provides IGuiceScanModuleInclusions with com.guicedee.activitymaster.fsdm.client.implementations.ActivityMasterClientModuleInclusion;

  opens com.guicedee.activitymaster.fsdm.client.services.administration to com.google.guice;
  opens com.guicedee.activitymaster.fsdm.client.services.deserializers to com.google.guice, com.fasterxml.jackson.databind;
  exports com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base;
  opens com.guicedee.activitymaster.fsdm.client.services.builders.warehouse.base to com.guicedee.activitymaster.fsdm;
  //opens com.guicedee.activitymaster.fsdm.client.services.converters.providers to com.google.guice,com.fasterxml.jackson.databind;

}

