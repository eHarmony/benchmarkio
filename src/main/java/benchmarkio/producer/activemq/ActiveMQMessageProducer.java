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

package benchmarkio.producer.activemq;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.HdrHistogram.Histogram;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import benchmarkio.benchmark.Histograms;
import benchmarkio.controlcenter.Consts;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;

public class ActiveMQMessageProducer implements benchmarkio.producer.MessageProducer {
    private final static Logger log = LoggerFactory.getLogger(ActiveMQMessageProducer.class);

    private Connection connection       = null;
    private Session session             = null;
    private MessageProducer producer    = null;

    private final Histogram histogram;
    private final String topic;
    private final TextMessage textMessage;
    private final long numberOfMessagesToProduce;

    public ActiveMQMessageProducer(final String host, final int port, final String topic, final String message, final long numberOfMessagesToProduce, final boolean durable) {
        Preconditions.checkNotNull(host);

        try {
            // Create a ConnectionFactory
            final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://" + host + ":" + port);

            // Create a Connection
            connection = connectionFactory.createConnection();
            connection.start();

            // Create a Session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination (Topic or Queue)
            final Destination destination = session.createTopic(topic);

            // Create a MessageProducer from the Session to the Topic or Queue
            producer = session.createProducer(destination);

            if (durable) {
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            } else {
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            }

            // Create a messages
            textMessage = session.createTextMessage(message);

        } catch (final JMSException e) {
            this.close();

            throw Throwables.propagate(e);
        }

        this.histogram                  = Histograms.create();
        this.topic                      = Preconditions.checkNotNull(topic);
        this.numberOfMessagesToProduce  = Preconditions.checkNotNull(numberOfMessagesToProduce);
    }

    @Override
    public Histogram call() {
        this.produce(topic, textMessage);

        return histogram;
    }

    @Override
    public void close() {
        try {
            if (producer != null) {
                producer.close();
            }

            if (session != null) {
                session.close();
            }

            if (connection != null) {
                connection.close();
            }
        } catch (final JMSException jex) {
            throw Throwables.propagate(jex);
        }
    }

    private void produce(final String topic, final TextMessage message) {
        for (int i = 0; i < numberOfMessagesToProduce; i++) {
            try {
                log.debug("Publishing message to ActiveMQ topic {}\n{}", topic, message);

                // Start
                final Stopwatch stopwatch = Stopwatch.createStarted();

                producer.send(message);

                // End
                stopwatch.stop();
                histogram.recordValue(stopwatch.elapsed(Consts.TIME_UNIT_FOR_REPORTING));

            } catch (final Exception e) {
                log.error("Error publishing message to ActiveMQ topic {}\n{}", topic, message.toString());
            }
        }

        log.info("Finished production of {} messages", numberOfMessagesToProduce);
    }
}
