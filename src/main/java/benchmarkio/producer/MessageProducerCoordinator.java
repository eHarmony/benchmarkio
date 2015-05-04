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

package benchmarkio.producer;

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
 * Defines common code for coordinating message producers.
 * Needs to be overridden to add specific implementations for message producer creation.
 */
public abstract class MessageProducerCoordinator implements AutoCloseable {
    private final static Logger log = LoggerFactory.getLogger(MessageProducerCoordinator.class);

    private final String topic;
    private final long totalNumberOfMessagesToProduce;
    private final int numThreads;

    private final CompletionService<Histogram> executorCompletionService;
    private final List<MessageProducer> producers;

    public MessageProducerCoordinator(final String topic, final long totalNumberOfMessagesToProduce, final int numThreads) {
        Preconditions.checkNotNull(topic);
        Preconditions.checkArgument(totalNumberOfMessagesToProduce > 0, "total number of messages to produce must be > 0");
        Preconditions.checkArgument(numThreads > 0, "number of threads must be > 0");

        this.topic                          = topic;
        this.totalNumberOfMessagesToProduce = totalNumberOfMessagesToProduce;
        this.numThreads                     = numThreads;
        this.executorCompletionService      = new ExecutorCompletionService<Histogram>(Executors.newFixedThreadPool(numThreads));
        this.producers                      = new ArrayList<>(numThreads);
    }

    public CompletionService<Histogram> startProducers() {
        final long numberOfMessagesToProduce = totalNumberOfMessagesToProduce / numThreads;

        for (int i = 0; i < numThreads; i++) {
            final MessageProducer producer = createProducer(numberOfMessagesToProduce);

            producers.add(producer);
            executorCompletionService.submit(producer);
        }

        return executorCompletionService;
    }

    public abstract MessageProducer createProducer(final long numberOfMessagesToProduce);

    @Override
    public void close() throws Exception {
        log.info("Shutting down Producer Coordinator for topic {}", topic);

        for (final MessageProducer producer : producers) {
            producer.close();
        }

        log.info("Finished shutting Producer Coordinator for topic {}", topic);
    }
}