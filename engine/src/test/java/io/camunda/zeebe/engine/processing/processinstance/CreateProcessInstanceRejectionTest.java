/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.1. You may not use this file
 * except in compliance with the Zeebe Community License 1.1.
 */
package io.camunda.zeebe.engine.processing.processinstance;

import static io.camunda.zeebe.protocol.record.Assertions.assertThat;

import io.camunda.zeebe.engine.util.EngineRule;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.protocol.record.RejectionType;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceCreationIntent;
import io.camunda.zeebe.test.util.record.RecordingExporter;
import io.camunda.zeebe.test.util.record.RecordingExporterTestWatcher;
import org.junit.Rule;
import org.junit.Test;

public class CreateProcessInstanceRejectionTest {

  private static final String PROCESS_ID = "process-id";
  @Rule public final EngineRule engine = EngineRule.singlePartition();

  @Rule
  public final RecordingExporterTestWatcher recordingExporterTestWatcher =
      new RecordingExporterTestWatcher();

  @Test
  public void shouldRejectCommandIfElementIdIsUnknown() {
    // given
    engine
        .deployment()
        .withXmlResource(
            Bpmn.createExecutableProcess(PROCESS_ID)
                .startEvent()
                .manualTask("task")
                .endEvent()
                .done())
        .deploy();

    // when
    engine
        .processInstance()
        .ofBpmnProcessId(PROCESS_ID)
        .withStartInstruction("task")
        .withStartInstruction("unknown-element")
        .withVariable("x", 1)
        .expectRejection()
        .create();

    // then
    final var rejectionRecord =
        RecordingExporter.processInstanceCreationRecords().onlyCommandRejections().getFirst();

    assertThat(rejectionRecord)
        .hasIntent(ProcessInstanceCreationIntent.CREATE)
        .hasRejectionType(RejectionType.INVALID_ARGUMENT)
        .hasRejectionReason(
            "Expected to create instance of process with start instructions but no element found with id 'unknown-element'.");
  }

  @Test
  public void shouldRejectCommandIfElementIsInsideMultiInstance() {
    // given
    engine
        .deployment()
        .withXmlResource(
            Bpmn.createExecutableProcess(PROCESS_ID)
                .startEvent()
                .subProcess(
                    "subprocess",
                    s ->
                        s.embeddedSubProcess()
                            .startEvent()
                            .manualTask("task-in-multi-instance")
                            .done())
                .multiInstance(m -> m.zeebeInputCollectionExpression("[1,2,3]"))
                .manualTask("task")
                .endEvent()
                .done())
        .deploy();

    // when
    engine
        .processInstance()
        .ofBpmnProcessId(PROCESS_ID)
        .withStartInstruction("task")
        .withStartInstruction("task-in-multi-instance")
        .withVariable("x", 1)
        .expectRejection()
        .create();

    // then
    final var rejectionRecord =
        RecordingExporter.processInstanceCreationRecords().onlyCommandRejections().getFirst();

    assertThat(rejectionRecord)
        .hasIntent(ProcessInstanceCreationIntent.CREATE)
        .hasRejectionType(RejectionType.INVALID_ARGUMENT)
        .hasRejectionReason(
            "Expected to create instance of process with start instructions but the element with id 'task-in-multi-instance' is inside a multi-instance subprocess. The creation of elements inside a multi-instance subprocess is not supported.");
  }

  @Test
  public void shouldRejectCommandIfEventBelongsToEventBasedGateway() {
    // given
    engine
        .deployment()
        .withXmlResource(
            Bpmn.createExecutableProcess(PROCESS_ID)
                .startEvent("start")
                .eventBasedGateway()
                .intermediateCatchEvent("timer1", c -> c.timerWithDuration("PT0.1S"))
                .endEvent()
                .moveToLastGateway()
                .intermediateCatchEvent("timer2", c -> c.timerWithDuration("PT0.1S"))
                .endEvent()
                .done())
        .deploy();

    // when
    engine
        .processInstance()
        .ofBpmnProcessId(PROCESS_ID)
        .withStartInstruction("timer1")
        .expectRejection()
        .create();

    // then
    final var rejectionRecord =
        RecordingExporter.processInstanceCreationRecords().onlyCommandRejections().getFirst();

    assertThat(rejectionRecord)
        .hasIntent(ProcessInstanceCreationIntent.CREATE)
        .hasRejectionType(RejectionType.INVALID_ARGUMENT)
        .hasRejectionReason(
            "Expected to create instance of process with start instructions but the element with id 'timer1' belongs to an event-based gateway. The creation of elements belonging to an event-based gateway is not supported.");
  }

  @Test
  public void shouldRejectCommandIfUnableToSubscribeToEvents() {
    // given
    engine
        .deployment()
        .withXmlResource(
            Bpmn.createExecutableProcess(PROCESS_ID)
                .startEvent()
                .subProcess(
                    "subprocess",
                    subprocess -> {
                      subprocess
                          .embeddedSubProcess()
                          .startEvent()
                          .serviceTask("task", t -> t.zeebeJobType("task"))
                          .endEvent();

                      subprocess
                          .boundaryEvent("message-boundary-event")
                          .cancelActivity(false)
                          .message(m -> m.name("msg").zeebeCorrelationKeyExpression("unknown_var"))
                          .endEvent();
                    })
                .endEvent()
                .done())
        .deploy();

    // when
    engine
        .processInstance()
        .ofBpmnProcessId(PROCESS_ID)
        .withStartInstruction("task")
        .expectRejection()
        .create();

    // then
    assertThat(
            RecordingExporter.processInstanceCreationRecords()
                .withBpmnProcessId(PROCESS_ID)
                .withStartInstruction("task")
                .onlyCommandRejections()
                .getFirst())
        .hasIntent(ProcessInstanceCreationIntent.CREATE)
        .hasRejectionType(RejectionType.PROCESSING_ERROR);
  }
}
