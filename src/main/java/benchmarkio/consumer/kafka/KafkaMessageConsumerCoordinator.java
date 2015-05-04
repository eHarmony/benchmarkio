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

package benchmarkio.consumer.kafka;

import benchmarkio.consumer.MessageConsumer;
import benchmarkio.consumer.MessageConsumerCoordinator;

import com.google.common.base.Preconditions;

/**
 * Note that this is the polling version of Kafka consumer, however the client code
 * {@link org.apache.kafka.clients.consumer.KafkaConsumer} doesn't have an implementation
 * for the poll method, here is a snippet:
 * <code>
 *     public Map<String, ConsumerRecords<K,V>> poll(long timeout) {
 *         // TODO Auto-generated method stub
 *         return null;
 *     }
 * </code>
 *
 * Example of instantiation:
 * <code>
 *      new KafkaMessageConsumerCoordinator(host + ":" + port, "anyGroupId", "test", numConsumers);
 * </code>
 *
 * This code will be useful once an actual implementation is added for that method.
 */
public class KafkaMessageConsumerCoordinator extends MessageConsumerCoordinator {

    private final String metadataBrokerList;
    private final String groupId;
    private final String topic;

    public KafkaMessageConsumerCoordinator(final String metadataBrokerList, final String groupId, final String topic, final int numThreads) {
        super(topic, numThreads);

        Preconditions.checkNotNull(metadataBrokerList,  "metadataBrokerList cannot be null");
        Preconditions.checkNotNull(groupId,             "groupId cannot be null");

        this.metadataBrokerList = metadataBrokerList;
        this.groupId            = groupId;
        this.topic              = topic;
    }

    @Override
    public MessageConsumer createConsumer() {
        return new KafkaMessageConsumer(metadataBrokerList, groupId, topic);
    }
}
