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

package benchmarkio.controlcenter;

import benchmarkio.consumer.MessageConsumerCoordinator;
import benchmarkio.consumer.activemq.ActiveMQMessageConsumerCoordinator;
import benchmarkio.consumer.kafka.BlockingKafkaMessageConsumerCoordinator;
import benchmarkio.consumer.rabbitmq.RabbitMQMessageConsumerCoordinator;
import benchmarkio.producer.MessageProducerCoordinator;
import benchmarkio.producer.activemq.ActiveMQMessageProducerCoordinator;
import benchmarkio.producer.kafka.KafkaMessageProducerCoordinator;
import benchmarkio.producer.rabbitmq.RabbitMQMessageProducerCoordinator;

import com.google.common.base.Optional;

/**
 * Defines all possible types of brokers.
 * Will be used when launching the tests.
 *
 * @see benchmarkio.controlcenter.LaunchRocket
 */
public enum BrokerType implements BrokerTypeInterface {
    KAFKA {
        @Override
        public MessageConsumerCoordinator getMessageConsumerCoordinator(final String host,
                                                                        final int port,
                                                                        final String destinationName,
                                                                        final int numConsumers,
                                                                        final boolean durable,
                                                                        final Optional<String> optionalZookeeper,
                                                                        final Optional<String> optionalGroupId) {
            return new BlockingKafkaMessageConsumerCoordinator(optionalZookeeper.get(), optionalGroupId.get(), destinationName, numConsumers);
        }

        @Override
        public MessageProducerCoordinator getMessageProducerCoordinator(final String host,
                                                                        final int port,
                                                                        final String destinationName,
                                                                        final String message,
                                                                        final long totalNumberOfMessagesToProduce,
                                                                        final int numProducers,
                                                                        final boolean durable,
                                                                        final Optional<String> optionalKafkaProducerType) {
            return new KafkaMessageProducerCoordinator(host + ":" + port, destinationName, message, totalNumberOfMessagesToProduce, numProducers, optionalKafkaProducerType.get());
        }
    },
    RABBITMQ {
        @Override
        public MessageConsumerCoordinator getMessageConsumerCoordinator(final String host,
                                                                        final int port,
                                                                        final String destinationName,
                                                                        final int numConsumers,
                                                                        final boolean durable,
                                                                        final Optional<String> optionalZookeeper,
                                                                        final Optional<String> optionalGroupId) {
            return new RabbitMQMessageConsumerCoordinator(host, port, destinationName, numConsumers, durable);
        }

        @Override
        public MessageProducerCoordinator getMessageProducerCoordinator(final String host,
                                                                        final int port,
                                                                        final String destinationName,
                                                                        final String message,
                                                                        final long totalNumberOfMessagesToProduce,
                                                                        final int numProducers,
                                                                        final boolean durable,
                                                                        final Optional<String> optionalKafkaProducerType) {
            return new RabbitMQMessageProducerCoordinator(host, port, destinationName, message, totalNumberOfMessagesToProduce, numProducers, durable);
        }
    },
    ACTIVEMQ {
        @Override
        public MessageConsumerCoordinator getMessageConsumerCoordinator(final String host,
                                                                        final int port,
                                                                        final String destinationName,
                                                                        final int numConsumers,
                                                                        final boolean durable,
                                                                        final Optional<String> optionalZookeeper,
                                                                        final Optional<String> optionalGroupId) {
            return new ActiveMQMessageConsumerCoordinator(host, port, destinationName, numConsumers);
        }

        @Override
        public MessageProducerCoordinator getMessageProducerCoordinator(final String host,
                                                                        final int port,
                                                                        final String destinationName,
                                                                        final String message,
                                                                        final long totalNumberOfMessagesToProduce,
                                                                        final int numProducers,
                                                                        final boolean durable,
                                                                        final Optional<String> optionalKafkaProducerType) {
            return new ActiveMQMessageProducerCoordinator(host, port, destinationName, message, totalNumberOfMessagesToProduce, numProducers, durable);
        }
    };
}

/**
 * Defines methods that each of the enumeration values need to override.
 */
interface BrokerTypeInterface {
    MessageConsumerCoordinator getMessageConsumerCoordinator(final String host,
                                                             final int port,
                                                             final String destinationName,
                                                             final int numConsumers,
                                                             final boolean durable,
                                                             final Optional<String> optionalZookeeper,
                                                             final Optional<String> optionalGroupId);

    MessageProducerCoordinator getMessageProducerCoordinator(final String host,
                                                             final int port,
                                                             final String destinationName,
                                                             final String message,
                                                             final long totalNumberOfMessagesToProduce,
                                                             final int numProducers,
                                                             final boolean durable,
                                                             final Optional<String> optionalKafkaProducerType);
}