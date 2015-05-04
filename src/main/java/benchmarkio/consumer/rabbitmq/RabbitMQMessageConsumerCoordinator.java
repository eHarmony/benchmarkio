/*
 * Copyright 2015 eHarmony, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package benchmarkio.consumer.rabbitmq;

import benchmarkio.consumer.MessageConsumer;
import benchmarkio.consumer.MessageConsumerCoordinator;

import com.google.common.base.Preconditions;

public class RabbitMQMessageConsumerCoordinator extends MessageConsumerCoordinator {

    private final String host;
    private final int port;
    private final String topic;
    private final boolean durable;

    public RabbitMQMessageConsumerCoordinator(final String host,
                                              final int port,
                                              final String topic,
                                              final int numThreads,
                                              final boolean durable) {
        super(topic, numThreads);

        Preconditions.checkNotNull(host);

        this.host       = host;
        this.port       = port;
        this.topic      = topic;
        this.durable    = durable;
    }

    @Override
    public MessageConsumer createConsumer() {
        return new RabbitMQMessageConsumer(host, port, topic, durable);
    }
}
