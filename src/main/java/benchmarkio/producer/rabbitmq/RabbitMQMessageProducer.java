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

package benchmarkio.producer.rabbitmq;

import java.io.IOException;

import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import benchmarkio.benchmark.Histograms;
import benchmarkio.controlcenter.Consts;
import benchmarkio.producer.MessageProducer;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQMessageProducer implements MessageProducer {
    private final static Logger log = LoggerFactory.getLogger(RabbitMQMessageProducer.class);

    private final Channel channel;
    private final Histogram histogram;
    private final String topic;
    private final String message;
    private final long numberOfMessagesToProduce;

    public RabbitMQMessageProducer(final String host, final int port, final String topic, final String message, final long numberOfMessagesToProduce, final boolean durable) {
        Preconditions.checkNotNull(host);

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

        // To declare a queue
        //channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        this.histogram                  = Histograms.create();
        this.topic                      = Preconditions.checkNotNull(topic);
        this.message                    = Preconditions.checkNotNull(message);
        this.numberOfMessagesToProduce  = Preconditions.checkNotNull(numberOfMessagesToProduce);
    }

    @Override
    public Histogram call() {
        this.produce(topic, message);

        return histogram;
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    private void produce(final String topic, final String message) {
        for (int i = 0; i < numberOfMessagesToProduce; i++) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Publishing message to RabbitMQ topic {}\n{}", topic, message.toString());
                }

                // Start
                final Stopwatch stopwatch = Stopwatch.createStarted();

                channel.basicPublish(topic, "#", null, message.getBytes());

                // End
                stopwatch.stop();
                histogram.recordValue(stopwatch.elapsed(Consts.TIME_UNIT_FOR_REPORTING));

            } catch (final Exception e) {
                log.error("Error publishing message to RabbitMQ topic {}\n{}", topic, message.toString());
            }
        }

        log.info("Finished production of {} messages", numberOfMessagesToProduce);
    }
}
