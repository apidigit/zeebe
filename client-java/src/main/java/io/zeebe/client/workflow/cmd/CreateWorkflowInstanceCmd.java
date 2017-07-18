/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.client.workflow.cmd;

import io.zeebe.client.cmd.SetPayloadCmd;

/**
 * Represents an command to create a workflow instance.
 */
public interface CreateWorkflowInstanceCmd extends SetPayloadCmd<WorkflowInstance, CreateWorkflowInstanceCmd>
{
    /**
     * Represents the latest version of a deployed workflow definition.
     */
    int LATEST_VERSION = -1;

    /**
     * Sets the BPMN process id, which identifies the workflow definition. Can
     * be combined with {@link #version(int)} or {@link #latestVersion()} but
     * not with {@link #workflowKey(long)}.
     *
     * @param id
     *            the id which identifies the workflow definition
     * @return the current create command
     */
    CreateWorkflowInstanceCmd bpmnProcessId(String id);

    /**
     * Sets the version, which corresponds to the deployed workflow definition.
     *
     * If the version is set to {@link #LATEST_VERSION}, the latest version of
     * the deployed workflow definition is used.
     *
     * @param version
     *            the version of the workflow definition
     * @return the current create command
     */
    CreateWorkflowInstanceCmd version(int version);

    /**
     * Sets the version, which corresponds to the deployed workflow definition,
     * to latest.
     *
     * @see {@link #version(int)}
     * @return the current create command
     */
    CreateWorkflowInstanceCmd latestVersion();

    /**
     * Sets the key which identifies the deployed workflow definition. Can not
     * be combined with {@link #bpmnProcessId(String)}, {@link #version(int)} or
     * {@link #latestVersion()}.
     *
     * @param workflowKey
     *            the key of the deployed workflow
     * @return the current create command
     */
    CreateWorkflowInstanceCmd workflowKey(long workflowKey);
}
