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

package benchmarkio.controlcenter;

import benchmarkio.benchmark.Benchmark;
import benchmarkio.benchmark.ConsumerOnly;
import benchmarkio.benchmark.ProducerAndConsumer;
import benchmarkio.benchmark.ProducerNoConsumerThenConsumer;
import benchmarkio.benchmark.ProducerOnly;

/**
 * Defines all possible types of benchmarks.
 * Will be used when launching the tests.
 *
 * @see benchmarkio.controlcenter.LaunchRocket
 */
public enum BenchmarkType {
    PRODUCER_ONLY(new ProducerOnly()),
    CONSUMER_ONLY(new ConsumerOnly()),
    PRODUCER_AND_CONSUMER(new ProducerAndConsumer()),
    PRODUCER_NO_CONSUMER_THEN_CONSUMER(new ProducerNoConsumerThenConsumer());

    private final Benchmark benchmark;

    private BenchmarkType(final Benchmark benchmark) {
        this.benchmark  = benchmark;
    }

    public Benchmark getBenchmark() {
        return benchmark;
    }
}