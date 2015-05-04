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

package benchmarkio.benchmark;

import java.util.concurrent.CompletionService;

import org.HdrHistogram.Histogram;

import benchmarkio.benchmark.report.CoordinatorType;
import benchmarkio.benchmark.report.Report;
import benchmarkio.consumer.MessageConsumerCoordinator;
import benchmarkio.producer.MessageProducerCoordinator;

import com.google.common.base.Stopwatch;

/**
 * Useful to test the message "Load" cases.
 */
public class ProducerOnly implements Benchmark {

    @Override
    public void run(final MessageConsumerCoordinator messageConsumerCoordinator,
                    final MessageProducerCoordinator messageProducerCoordinator,
                    final int numConsumers,
                    final int numProducers,
                    final long totalNumberOfMessages,
                    final Report report) {
        final CompletionService<Histogram> producerCompletionService = messageProducerCoordinator.startProducers();

        // Note that the timer is started after startProducers(), this is by purpose to exclude the initialization time.
        final Stopwatch producerStartTime = Stopwatch.createStarted();

        report.aggregateAndPrintResults(CoordinatorType.PRODUCER, producerCompletionService, numProducers, totalNumberOfMessages, producerStartTime);
    }
}
