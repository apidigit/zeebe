/*
 * Copyright © 2020 camunda services GmbH (info@camunda.com)
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
package io.atomix.cluster.messaging.grpc;

import io.camunda.zeebe.util.CloseableSilently;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import java.net.SocketAddress;

@SuppressWarnings("java:S1452")
public interface TransportFactory extends CloseableSilently {
  ServerBuilder<?> createServerBuilder(final SocketAddress address);

  ManagedChannelBuilder<?> createClientBuilder(final SocketAddress address);
}