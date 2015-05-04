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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.HdrHistogram.Histogram;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import benchmarkio.benchmark.Histograms;
import benchmarkio.consumer.MessageConsumer;
import benchmarkio.controlcenter.Consts;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

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
 * This code will be useful once an actual implementation is added for that method.
 */
public class KafkaMessageConsumer implements MessageConsumer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageConsumer.class);

    private final KafkaConsumer<String, String> consumer;
    private final Histogram histogram;
    private final String topic;

    public KafkaMessageConsumer(final String metadataBrokerList,
                                final String groupId,
                                final String topic) {
        Preconditions.checkNotNull(metadataBrokerList,  "metadataBrokerList cannot be null");
        Preconditions.checkNotNull(groupId,             "groupId cannot be null");
        Preconditions.checkNotNull(topic,               "topic cannot be null");

        final Properties props = new Properties();
        props.put("bootstrap.servers",              metadataBrokerList); // See org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG
        props.put("group.id",                       groupId);   // See org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG
        props.put("session.timeout.ms",             "1000");    // See org.apache.kafka.clients.consumer.ConsumerConfig.SESSION_TIMEOUT_MS
        props.put("enable.auto.commit",             "true");    // See org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG
        props.put("auto.commit.interval.ms",        "10000");   // See org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG
        props.put("key.deserializer",               "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",             "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("partition.assignment.strategy",  "roundrobin");

        this.consumer   = new KafkaConsumer<String, String>(props);
        this.topic      = topic;
        this.histogram  = Histograms.create();

        consumer.subscribe(topic);
    }

    @Override
    public Histogram call() throws Exception {
        // Note that this is a polling consumer and will be terminated
        // whenever the Consts.POLLING_CONSUMER_MAX_IDLE_TIME_MS passes and no new messages have arrived.
        while (true) {
            try {
                final Map<String, ConsumerRecords<String, String>> records = consumer.poll(Consts.POLLING_CONSUMER_MAX_IDLE_TIME_MS);
                final Map<TopicPartition, Long> processedOffsets  = new HashMap<TopicPartition, Long>();

                for (final Entry<String, ConsumerRecords<String, String>> recordMetadata : records.entrySet()) {

                    final List<ConsumerRecord<String, String>> recordsPerTopic = recordMetadata.getValue().records();
                    for (int i = 0; i < recordsPerTopic.size(); i++) {
                        final ConsumerRecord<String, String> record = recordsPerTopic.get(i);
                        // process record
                        processedOffsets.put(record.topicAndPartition(), record.offset());

                        // Start
                        final Stopwatch stopwatch = Stopwatch.createStarted();

                        final Object message = record.value();

                        // End
                        stopwatch.stop();
                        histogram.recordValue(stopwatch.elapsed(Consts.TIME_UNIT_FOR_REPORTING));
                    }
                }
            } catch (final Exception ex) {
                logger.error("Failed to consume messages: ", ex);

                break;
            }
        }

        return histogram;
    }

    @Override
    public void close() {
        consumer.close();
    }

}
