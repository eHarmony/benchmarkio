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

import benchmarkio.benchmark.report.Report;
import benchmarkio.consumer.MessageConsumerCoordinator;
import benchmarkio.producer.MessageProducerCoordinator;

/**
 * Primary interface that defines benchmark structure.
 * Implement this if you want to add a new benchmark type.
 */
public interface Benchmark {

    public void run(final MessageConsumerCoordinator messageConsumerCoordinator,
                    final MessageProducerCoordinator messageProducerCoordinator,
                    final int numConsumers,
                    final int numProducers,
                    final long totalNumberOfMessages,
                    final Report report);
}
