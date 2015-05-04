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
import java.util.Properties;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import benchmarkio.consumer.MessageConsumer;
import benchmarkio.consumer.MessageConsumerCoordinator;
import benchmarkio.controlcenter.Consts;

import com.google.common.base.Preconditions;

public class BlockingKafkaMessageConsumerCoordinator extends MessageConsumerCoordinator {
    private final static Logger log = LoggerFactory.getLogger(BlockingKafkaMessageConsumerCoordinator.class);

    private final String topic;
    private final int numThreads;
    private final Properties props;
    private final CompletionService<Histogram> executorCompletionService;

    private ConsumerConnector consumerConnector;

    public BlockingKafkaMessageConsumerCoordinator(final String zookeeper, final String groupId, final String topic, final int numThreads) {
        super(topic, numThreads);

        Preconditions.checkNotNull(zookeeper,           "zookeeper cannot be null");
        Preconditions.checkNotNull(groupId,             "groupId cannot be null");

        final Properties props = new Properties();
        props.put("zookeeper.connect",              zookeeper);
        props.put("group.id",                       groupId);
        props.put("zookeeper.session.timeout.ms",   String.valueOf(Consts.ZOOKEEPER_SESSION_TIMEOUT_MS));   // Zookeeper session timeout. If the consumer fails to heartbeat to zookeeper for this period of time it is considered dead and a rebalance will occur.
        props.put("zookeeper.sync.time.ms",         String.valueOf(Consts.ZOOKEEPER_SYNC_TIME_MS));         // How far a ZK follower can be behind a ZK leader.
        props.put("auto.commit.interval.ms",        String.valueOf(Consts.KAFKA_AUTO_COMMIT_INTERVAL_MS));  // The frequency in ms that the consumer offsets are committed to zookeeper.
        // XXX: Is there a better way to do this?
        // I have been thinking about other ways, such as using special "poison" message to indicate
        // end of consumption, however there is no guarantee that all of the consumers will recieve
        // the same message.
        // This will throw a timeout exception after specified time.
        props.put("consumer.timeout.ms",            String.valueOf(Consts.POLLING_CONSUMER_MAX_IDLE_TIME_MS));

        this.topic                      = topic;
        this.numThreads                 = numThreads;
        this.props                      = props;
        this.executorCompletionService  = new ExecutorCompletionService<Histogram>(Executors.newFixedThreadPool(numThreads));
    }

    @Override
    public CompletionService<Histogram> startConsumers() {
        final ConsumerConfig consumerConfig = new ConsumerConfig(props);

        consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);

        // Create message streams
        final Map<String, Integer> topicMap = new HashMap<>();
        topicMap.put(topic, numThreads);

        final Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConnector.createMessageStreams(topicMap);
        final List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        // Pass each stream to a consumer that will read from the stream in its own thread.
        for (final KafkaStream<byte[], byte[]> stream : streams) {
            executorCompletionService.submit(new BlockingKafkaMessageConsumer(stream));
        }

        return executorCompletionService;
    }

    @Override
    public void close() {
        log.info("Shutting down Consumer Coordinator for topic {}", topic);

        try {
            if (consumerConnector != null) {
                consumerConnector.shutdown();
            }
        } catch (final Exception e) {
            log.error("Exception on consumer connector shutdown", e);
        }

        log.info("Finished shutting Consumer Coordinator for topic {}", topic);
    }

    @Override
    public MessageConsumer createConsumer() {
        throw new UnsupportedOperationException("This should never be invoked, since startConsumers() is overridden");
    }
}
