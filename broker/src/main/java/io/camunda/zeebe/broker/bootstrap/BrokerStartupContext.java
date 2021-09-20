/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.broker.bootstrap;

import io.atomix.cluster.messaging.ManagedMessagingService;
import io.camunda.zeebe.broker.PartitionListener;
import io.camunda.zeebe.broker.SpringBrokerBridge;
import io.camunda.zeebe.broker.clustering.ClusterServicesImpl;
import io.camunda.zeebe.broker.system.configuration.BrokerCfg;
import io.camunda.zeebe.broker.system.monitoring.BrokerHealthCheckService;
import io.camunda.zeebe.broker.system.monitoring.DiskSpaceUsageListener;
import io.camunda.zeebe.broker.transport.commandapi.CommandApiServiceImpl;
import io.camunda.zeebe.protocol.impl.encoding.BrokerInfo;
import io.camunda.zeebe.transport.impl.AtomixServerTransport;
import io.camunda.zeebe.util.sched.ActorSchedulingService;
import io.camunda.zeebe.util.sched.ConcurrencyControl;
import java.util.List;

/**
 * Context that is utilized during broker startup and shutdown process. It contains dependencies
 * that are needed during the startup/shutdown. It is a modifiable context and will be updated
 * during startup or shutdown.
 */
public interface BrokerStartupContext {

  BrokerInfo getBrokerInfo();

  BrokerCfg getBrokerConfiguration();

  SpringBrokerBridge getSpringBrokerBridge();

  ActorSchedulingService getActorSchedulingService();

  ConcurrencyControl getConcurrencyControl();

  BrokerHealthCheckService getHealthCheckService();

  void addPartitionListener(PartitionListener partitionListener);

  void removePartitionListener(PartitionListener partitionListener);

  List<PartitionListener> getPartitionListeners();

  ClusterServicesImpl getClusterServices();

  void setClusterServices(ClusterServicesImpl o);

  void addDiskSpaceUsageListener(DiskSpaceUsageListener listener);

  void removeDiskSpaceUsageListener(DiskSpaceUsageListener listener);

  List<DiskSpaceUsageListener> getDiskSpaceUsageListeners();

  CommandApiServiceImpl getCommandApiService();

  void setCommandApiService(CommandApiServiceImpl commandApiService);

  AtomixServerTransport getCommandApiServerTransport();

  void setCommandApiServerTransport(AtomixServerTransport commandApiServerTransport);

  ManagedMessagingService getCommandApiMessagingService();

  void setCommandApiMessagingService(ManagedMessagingService commandApiMessagingService);
}