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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import benchmarkio.consumer.activemq.ActiveMQMessageConsumerCoordinator;
import benchmarkio.consumer.kafka.BlockingKafkaMessageConsumerCoordinator;
import benchmarkio.consumer.rabbitmq.RabbitMQMessageConsumerCoordinator;
import benchmarkio.producer.activemq.ActiveMQMessageProducerCoordinator;
import benchmarkio.producer.kafka.KafkaMessageProducerCoordinator;
import benchmarkio.producer.rabbitmq.RabbitMQMessageProducerCoordinator;

import com.google.common.base.Optional;

/**
 * Tests {@link benchmarkio.controlcenter.BrokerType}.
 */
public class BrokerTypeTest {

    private String host;
    private int port;
    private String destinationName;
    private int numConsumers;
    private boolean durable;
    private Optional<String> optionalZookeeper;
    private Optional<String> optionalGroupId;
    private String message;
    private long totalNumberOfMessagesToProduce;
    private int numProducers;
    private Optional<String> optionalKafkaProducerType;

    @Before
    public void setup() {
        host                            = "localhost";
        port                            = 5672;
        destinationName                 = "test";
        numConsumers                    = 10;
        durable                         = false;
        optionalZookeeper               = Optional.of("localhost:2181");
        optionalGroupId                 = Optional.of("anyGroupId");
        message                         = "someMessage";
        totalNumberOfMessagesToProduce  = 10000;
        numProducers                    = 10;
        optionalKafkaProducerType       = Optional.of("sync");
    }

    @Test
    public void getMessageConsumerCoordinatorKafka() {
        assertEquals(BlockingKafkaMessageConsumerCoordinator.class, BrokerType.KAFKA.getMessageConsumerCoordinator(host, port, destinationName, numConsumers, durable, optionalZookeeper, optionalGroupId).getClass());
    }

    @Test
    public void getMessageProducerCoordinatorKafka() {
        assertEquals(KafkaMessageProducerCoordinator.class, BrokerType.KAFKA.getMessageProducerCoordinator(host, port, destinationName, message, totalNumberOfMessagesToProduce, numProducers, durable, optionalKafkaProducerType).getClass());
    }

    @Test
    public void getMessageConsumerCoordinatorRabbitMQ() {
        assertEquals(RabbitMQMessageConsumerCoordinator.class, BrokerType.RABBITMQ.getMessageConsumerCoordinator(host, port, destinationName, numConsumers, durable, optionalZookeeper, optionalGroupId).getClass());
    }

    @Test
    public void getMessageProducerCoordinatorRabbitMQ() {
        assertEquals(RabbitMQMessageProducerCoordinator.class, BrokerType.RABBITMQ.getMessageProducerCoordinator(host, port, destinationName, message, totalNumberOfMessagesToProduce, numProducers, durable, optionalKafkaProducerType).getClass());
    }

    @Test
    public void getMessageConsumerCoordinatorActiveMQ() {
        assertEquals(ActiveMQMessageConsumerCoordinator.class, BrokerType.ACTIVEMQ.getMessageConsumerCoordinator(host, port, destinationName, numConsumers, durable, optionalZookeeper, optionalGroupId).getClass());
    }

    @Test
    public void getMessageProducerCoordinatorActiveMQ() {
        assertEquals(ActiveMQMessageProducerCoordinator.class, BrokerType.ACTIVEMQ.getMessageProducerCoordinator(host, port, destinationName, message, totalNumberOfMessagesToProduce, numProducers, durable, optionalKafkaProducerType).getClass());
    }
}
