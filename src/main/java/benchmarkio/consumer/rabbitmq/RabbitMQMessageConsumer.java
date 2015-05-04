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

import java.io.IOException;

import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import benchmarkio.benchmark.Histograms;
import benchmarkio.consumer.MessageConsumer;
import benchmarkio.controlcenter.Consts;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class RabbitMQMessageConsumer implements MessageConsumer {
    private final static Logger logger = LoggerFactory.getLogger(RabbitMQMessageConsumer.class);

    private final Channel channel;
    private final Histogram histogram;
    private final String topic;

    public RabbitMQMessageConsumer(final String host,
                                final int port,
                                final String topic,
                                final boolean durable) {
        Preconditions.checkNotNull(host);
        Preconditions.checkNotNull(topic, "topic cannot be null");

        final ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);

        try {
            final Connection connection = factory.newConnection();

            this.channel = connection.createChannel();
            this.channel.exchangeDeclare(topic, "topic", durable);
        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }

        this.topic      = topic;
        this.histogram  = Histograms.create();
    }

    @Override
    public Histogram call() throws Exception {
        final String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, topic, "#");

        final QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, consumer);

        logger.info("topic: {}, queueName: {}", topic, queueName);

        int messageCount = 0;

        // Note that this is a polling consumer and will be terminated
        // whenever the Consts.POLLING_CONSUMER_MAX_IDLE_TIME_MS passes and no new messages have arrived.
        while (true) {
            // Start
            final Stopwatch stopwatch = Stopwatch.createStarted();

            final QueueingConsumer.Delivery delivery = consumer.nextDelivery(Consts.POLLING_CONSUMER_MAX_IDLE_TIME_MS);

            if (delivery == null) {
                logger.info("Consumer was terminated through timeout");

                break;
            }

            final byte[] message = delivery.getBody();

            messageCount++;

            final String routingKey = delivery.getEnvelope().getRoutingKey();

            // End
            stopwatch.stop();
            histogram.recordValue(stopwatch.elapsed(Consts.TIME_UNIT_FOR_REPORTING));
        }

        logger.info("In total consumed {} messages", messageCount);

        return histogram;
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
