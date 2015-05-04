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

package benchmarkio.consumer.activemq;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.HdrHistogram.Histogram;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import benchmarkio.benchmark.Histograms;
import benchmarkio.controlcenter.Consts;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;

public class ActiveMQMessageConsumer implements benchmarkio.consumer.MessageConsumer, ExceptionListener {
    private static final Logger logger = LoggerFactory.getLogger(ActiveMQMessageConsumer.class);

    private Connection connection       = null;
    private Session session             = null;
    private MessageConsumer consumer    = null;

    private final Histogram histogram;

    public ActiveMQMessageConsumer(final String host, final int port, final String topic) {

        try {
            // Create a ConnectionFactory
            final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://" + host + ":" + port);

            // Create a Connection
            connection = connectionFactory.createConnection();
            connection.start();

            connection.setExceptionListener(this);

            // Create a Session
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create the destination (Topic or Queue)
            final Destination destination = session.createTopic(topic);

            // Create a MessageConsumer from the Session to the Topic or Queue
            consumer = session.createConsumer(destination);
        } catch (final JMSException e) {
            this.close();

            throw Throwables.propagate(e);
        }

        this.histogram = Histograms.create();
    }

    @Override
    public Histogram call() throws Exception {

        int messageCount = 0;

        try {
            // Note that this is a polling consumer and will be terminated
            // whenever the Consts.POLLING_CONSUMER_MAX_IDLE_TIME_MS passes and no new messages have arrived.
            while (true) {
                // Start
                final Stopwatch stopwatch = Stopwatch.createStarted();

                // Wait for a message
                // This is hilarious, according to the implementation javadoc comment:
                // Will return the next message produced for this message consumer, or null if
                // the timeout expires or this message consumer is concurrently closed
                final Message message = consumer.receive(Consts.POLLING_CONSUMER_MAX_IDLE_TIME_MS);

                if (message == null) {
                    break;
                }

                messageCount++;

//                if (message instanceof TextMessage) {
//                    final TextMessage textMessage = (TextMessage) message;
//                    final String text = textMessage.getText();
//                    System.out.println("Received: " + text);
//                } else {
//                    System.out.println("Received: " + message);
//                }

                // End
                stopwatch.stop();
                histogram.recordValue(stopwatch.elapsed(Consts.TIME_UNIT_FOR_REPORTING));
            }
        } catch (final Exception e) {
            // This is by purpose, hence no error logging.
            logger.info("Consumer was terminated through timeout");
        }

        logger.info("In total consumed {} messages", messageCount);

        return histogram;
    }

    @Override
    public void onException(final JMSException ex) {
        logger.error("JMS Exception occured.  Shutting down client.", ex);
    }

    @Override
    public void close() {
        try {
            if (consumer != null) {
                consumer.close();
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
}
