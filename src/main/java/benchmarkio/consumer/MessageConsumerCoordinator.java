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

package benchmarkio.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

import org.HdrHistogram.Histogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Defines common code for coordinating message consumers.
 * Needs to be overridden to add specific implementations for message consumer creation.
 */
public abstract class MessageConsumerCoordinator implements AutoCloseable {
    private final static Logger log = LoggerFactory.getLogger(MessageConsumerCoordinator.class);

    private final String topic;
    private final int numThreads;

    private final CompletionService<Histogram> executorCompletionService;
    private final List<MessageConsumer> consumers;

    public MessageConsumerCoordinator(final String topic, final int numThreads) {
        Preconditions.checkNotNull(topic,           "topic cannot be null");
        Preconditions.checkArgument(numThreads > 0, "number of threads must be > 0");

        this.topic                      = topic;
        this.numThreads                 = numThreads;
        this.executorCompletionService  = new ExecutorCompletionService<Histogram>(Executors.newFixedThreadPool(numThreads));
        this.consumers                  = new ArrayList<>(numThreads);
    }

    public CompletionService<Histogram> startConsumers() {
        for (int i = 0; i < numThreads; i++) {
            final MessageConsumer consumer = createConsumer();

            consumers.add(consumer);
            executorCompletionService.submit(consumer);
        }

        return executorCompletionService;
    }

    public abstract MessageConsumer createConsumer();

    @Override
    public void close() throws Exception {
        log.info("Shutting down Consumer Coordinator for topic {}", topic);

        for (final MessageConsumer consumer : consumers) {
            consumer.close();
        }

        log.info("Finished shutting down Consumer Coordinator for topic {}", topic);
    }
}
