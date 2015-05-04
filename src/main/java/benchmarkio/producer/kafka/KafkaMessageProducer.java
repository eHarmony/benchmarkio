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

import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import benchmarkio.benchmark.Histograms;
import benchmarkio.controlcenter.Consts;
import benchmarkio.producer.MessageProducer;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

public class KafkaMessageProducer implements MessageProducer {
    private final static Logger log = LoggerFactory.getLogger(KafkaMessageProducer.class);

    private final Producer<String, String> producer;
    private final Histogram histogram;
    private final String topic;
    private final String message;
    private final long numberOfMessagesToProduce;

    /**
     * metadata.broker.list => "broker1:9092,broker2:9092"
     */
    public KafkaMessageProducer(final String metadataBrokerList, final String topic, final String message, final long numberOfMessagesToProduce, final String kafkaProducerType) {
        final Properties props = new Properties();

        props.put("metadata.broker.list",   metadataBrokerList);
        props.put("serializer.class",       "kafka.serializer.StringEncoder");
        props.put("request.required.acks",  "1");
        props.put("producer.type",          kafkaProducerType);

        final Producer<String, String> producer = new Producer<>(new ProducerConfig(props));

        this.producer                   = Preconditions.checkNotNull(producer);
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
    public void close() {
        producer.close();
    }

    private void produce(final String topic, final String message) {
        for (int i = 0; i < numberOfMessagesToProduce; i++) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Publishing message to Kafka topic {}\n{}", topic, message.toString());
                }

                final KeyedMessage<String, String> data = new KeyedMessage<>(topic, message);

                // Start
                final Stopwatch stopwatch = Stopwatch.createStarted();

                producer.send(data);

                // End
                stopwatch.stop();
                histogram.recordValue(stopwatch.elapsed(Consts.TIME_UNIT_FOR_REPORTING));

            } catch (final Exception e) {
                log.error("Error publishing message to kafka topic {}\n{}", topic, message.toString());
            }
        }

        log.info("Finished production of {} messages", numberOfMessagesToProduce);
    }
}
