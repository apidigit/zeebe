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
package io.zeebe.client.event.impl;

import io.zeebe.client.event.*;
import io.zeebe.client.impl.data.MsgPackMapper;
import io.zeebe.client.task.impl.subscription.EventAcquisition;
import io.zeebe.util.EnsureUtil;

public class TopicSubscriptionBuilderImpl implements TopicSubscriptionBuilder
{
    protected TopicEventHandler defaultEventHandler;
    protected TaskEventHandler taskEventHandler;
    protected WorkflowInstanceEventHandler wfInstanceEventHandler;
    protected WorkflowEventHandler wfEventHandler;
    protected IncidentEventHandler incidentEventHandler;
    protected RaftEventHandler raftEventHandler;

    protected final TopicSubscriptionImplBuilder builder;
    protected final MsgPackMapper msgPackMapper;

    public TopicSubscriptionBuilderImpl(
            TopicClientImpl client,
            EventAcquisition<TopicSubscriptionImpl> acquisition,
            MsgPackMapper msgPackMapper,
            int prefetchCapacity)
    {
        builder = new TopicSubscriptionImplBuilder(client, acquisition, prefetchCapacity);
        this.msgPackMapper = msgPackMapper;
    }

    @Override
    public TopicSubscriptionBuilder handler(TopicEventHandler handler)
    {
        this.defaultEventHandler = handler;
        return this;
    }

    @Override
    public TopicSubscriptionBuilder taskEventHandler(TaskEventHandler handler)
    {
        this.taskEventHandler = handler;
        return this;
    }

    @Override
    public TopicSubscriptionBuilder workflowInstanceEventHandler(WorkflowInstanceEventHandler handler)
    {
        this.wfInstanceEventHandler = handler;
        return this;
    }

    @Override
    public TopicSubscriptionBuilder workflowEventHandler(WorkflowEventHandler handler)
    {
        this.wfEventHandler = handler;
        return this;
    }

    @Override
    public TopicSubscriptionBuilder incidentEventHandler(IncidentEventHandler handler)
    {
        this.incidentEventHandler = handler;
        return this;
    }

    @Override
    public TopicSubscriptionBuilderImpl raftEventHandler(final RaftEventHandler raftEventHandler)
    {
        this.raftEventHandler = raftEventHandler;
        return this;
    }

    @Override
    public TopicSubscription open()
    {
        EnsureUtil.ensureNotNull("name", builder.getName());
        if (defaultEventHandler == null && taskEventHandler == null && wfEventHandler == null && wfInstanceEventHandler == null && incidentEventHandler == null
                && raftEventHandler == null)
        {
            throw new RuntimeException("at least one handler must be set");
        }

        builder.handler(this::dispatchEvent);

        final TopicSubscriptionImpl subscription = builder.build();
        subscription.open();
        return subscription;
    }

    protected void dispatchEvent(TopicEventImpl event) throws Exception
    {
        final TopicEventType eventType = event.getEventType();

        if (TopicEventType.TASK == eventType && taskEventHandler != null)
        {
            final TaskEventImpl taskEvent = msgPackMapper.convert(event.getAsMsgPack(), TaskEventImpl.class);
            taskEventHandler.handle(event, taskEvent);
        }
        else if (TopicEventType.WORKFLOW_INSTANCE == eventType && wfInstanceEventHandler != null)
        {
            final WorkflowInstanceEventImpl wfInstanceEvent = msgPackMapper.convert(event.getAsMsgPack(), WorkflowInstanceEventImpl.class);
            wfInstanceEventHandler.handle(event, wfInstanceEvent);
        }
        else if (TopicEventType.WORKFLOW == eventType && wfEventHandler != null)
        {
            final WorkflowEventImpl wfEvent = msgPackMapper.convert(event.getAsMsgPack(), WorkflowEventImpl.class);
            wfEventHandler.handle(event, wfEvent);
        }
        else if (TopicEventType.INCIDENT == eventType && incidentEventHandler != null)
        {
            final IncidentEvent incidentEvent = msgPackMapper.convert(event.getAsMsgPack(), IncidentEventImpl.class);
            incidentEventHandler.handle(event, incidentEvent);
        }
        else if (TopicEventType.RAFT == eventType && raftEventHandler != null)
        {
            final RaftEvent raftEvent = msgPackMapper.convert(event.getAsMsgPack(), RaftEventImpl.class);
            raftEventHandler.handle(event, raftEvent);
        }
        else if (defaultEventHandler != null)
        {
            defaultEventHandler.handle(event, event);
        }
    }

    @Override
    public TopicSubscriptionBuilder startAtPosition(long position)
    {
        builder.startPosition(position);
        return this;
    }

    @Override
    public TopicSubscriptionBuilder startAtTailOfTopic()
    {
        builder.startAtTailOfTopic();
        return this;
    }

    @Override
    public TopicSubscriptionBuilder startAtHeadOfTopic()
    {
        builder.startAtHeadOfTopic();
        return this;
    }

    @Override
    public TopicSubscriptionBuilder name(String name)
    {
        builder.name(name);
        return this;
    }

    @Override
    public TopicSubscriptionBuilder forcedStart()
    {
        builder.forceStart();
        return this;
    }
}
