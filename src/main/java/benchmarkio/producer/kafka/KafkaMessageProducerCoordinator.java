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

package benchmarkio.producer.kafka;

import benchmarkio.producer.MessageProducer;
import benchmarkio.producer.MessageProducerCoordinator;

import com.google.common.base.Preconditions;

public class KafkaMessageProducerCoordinator extends MessageProducerCoordinator {
    private final String metadataBrokerList;
    private final String topic;
    private final String message;
    private final String kafkaProducerType;

    public KafkaMessageProducerCoordinator(final String metadataBrokerList,
                                           final String topic,
                                           final String message,
                                           final long totalNumberOfMessagesToProduce,
                                           final int numThreads,
                                           final String kafkaProducerType) {
        super(topic, totalNumberOfMessagesToProduce, numThreads);

        Preconditions.checkNotNull(metadataBrokerList);
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(kafkaProducerType);

        this.metadataBrokerList = metadataBrokerList;
        this.topic              = topic;
        this.message            = message;
        this.kafkaProducerType  = kafkaProducerType;
    }

    @Override
    public MessageProducer createProducer(final long numberOfMessagesToProduce) {
        return new KafkaMessageProducer(metadataBrokerList, topic, message, numberOfMessagesToProduce, kafkaProducerType);
    }
}
